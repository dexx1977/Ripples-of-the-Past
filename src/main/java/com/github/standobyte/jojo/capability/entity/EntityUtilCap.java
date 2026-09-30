package com.github.standobyte.jojo.capability.entity;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.OptionalInt;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.GoldExperienceLifeDetector;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.IEntityGlowColor;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class EntityUtilCap {
    private final Entity entity;
    private final Mob asMob;
    private Boolean prevNoAi;
    
    private KnockbackCollisionImpact kbImpact;
    
    private boolean stoppedInTime = false;
    private Queue<Runnable> runOnTimeResume = new LinkedList<>();
    
    private OptionalInt glowingColor = OptionalInt.empty();
    private int glowColorTicks = -1;
    
    @Nullable private MrPresidentWorldData.ChunkSectionPos mrPresidentRoomPos;
    
    public EntityUtilCap(Entity entity) {
        this.entity = entity;
        this.asMob = entity instanceof Mob ? (Mob) entity : null;
        this.kbImpact = new KnockbackCollisionImpact(entity);
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (!entity.canUpdate() && wasStoppedInTime()) {
            nbt.putBoolean("StoppedInTime", true);
            if (prevNoAi != null) nbt.putBoolean("PrevNoAi", prevNoAi);
        }
        nbt.put("KbImpact", kbImpact.serializeNBT());
        return nbt;
    }
    
    public void deserializeNBT(CompoundTag nbt) {
        stoppedInTime = nbt.getBoolean("StoppedInTime");
        if (stoppedInTime) {
            boolean isStoppedInTime = TimeStopHandler.isTimeStopped(entity.level, entity.blockPosition());
            prevNoAi = MCUtil.getNbtElement(nbt, "PrevNoAi", ByteTag.class).map(byteNbt -> byteNbt.getAsByte() != 0).orElse(null);
            // updates the Entity#canUpdate field that Forge adds, since it is saved in NBT
            updateEntityTimeStop(isStoppedInTime);
        }
        
        MCUtil.nbtGetCompoundOptional(nbt, "KbImpact").ifPresent(kbImpact::deserializeNBT);
    }
    
    /**
     *  currently is not called on server side, 
     *  uncomment in
     *  {@link GameplayEventHandler.onWorldTick(WorldTickEvent)}
     *  if that's needed
     */
    public void tick() {
        if (entity.level.isClientSide()) {
            tickGlowingColor();
        }
        else {
            kbImpact.tick();
        }
        
        tickMrPresidentOutOfBounds();
    }
    
    public void updateEntityTimeStop(boolean stopInTime) {
        if (stopInTime) {
            stoppedInTime = true;
            entity.canUpdate(false);
            
            if (asMob != null && prevNoAi == null) {
                prevNoAi = asMob.isNoAi();
                asMob.setNoAi(true);
            }
        }
        else if (stoppedInTime) {
            entity.canUpdate(true);
            
            if (asMob != null) {
                if (prevNoAi != null && !prevNoAi) {
                    asMob.setNoAi(false);
                }
                prevNoAi = null;
            }
            
            runOnTimeResume.forEach(Runnable::run);
            runOnTimeResume.clear();
        }
    }
    
    public boolean wasStoppedInTime() {
        return stoppedInTime;
    }
    
    
    
    public static void queueOnTimeResume(Entity entity, Runnable action) {
        GeneralUtil.ifPresentOrElse(entity.getCapability(EntityUtilCapProvider.CAPABILITY).resolve(), 
                cap -> {
                    if (cap.stoppedInTime) {
                        cap.runOnTimeResume.add(action);
                    }
                    else if (entity.canUpdate()) {
                        action.run();
                    }
                }, 
                action);
    }
    
    
    public final KnockbackCollisionImpact getKbImpact() {
        return kbImpact;
    }
    
    
    public void setClGlowingColor(@Nonnull OptionalInt color, int ticks) {
        setClGlowingColor(color, ticks, null);
    }
    
    public void setClGlowingColor(@Nonnull OptionalInt color, int ticks, @Nullable Object additionalCtx) {
        if (entity instanceof IEntityGlowColor) {
            this.glowingColor = color;
            this.glowColorTicks = ticks;
            ((IEntityGlowColor) entity).setGlowColor(glowingColor);
            setShowHpGEDetector(color.isPresent() && additionalCtx == GoldExperienceLifeDetector.GE_DETECTOR_CTX);
        }
    }
    
    public void setClGlowingColor(@Nonnull OptionalInt color) {
        setClGlowingColor(color, -1);
    }
    
    public void resetClGlowingColor() {
        setClGlowingColor(OptionalInt.empty(), -1);
    }
    
    public void refreshClEntityGlowing() {
        if (entity instanceof IEntityGlowColor) {
            IEntityGlowColor colorData = (IEntityGlowColor) entity;
            colorData.setGlowColor(glowingColor);
        }
    }
    
    private void tickGlowingColor() {
        if (glowingColor.isPresent() && glowColorTicks > 0 && --glowColorTicks == 0 && entity instanceof IEntityGlowColor) {
            IEntityGlowColor colorData = (IEntityGlowColor) entity;
            if (colorData.getGlowColor() == this.glowingColor) {
                resetClGlowingColor();
            }
        }
    }
    
    private void setShowHpGEDetector(boolean value) {
        if (entity.level.isClientSide()) {
            if (value) {
                ClientEventHandler.getInstance().addGEDetectedEntity(entity);
            }
            else {
                ClientEventHandler.getInstance().removeGEDetectedEntity(entity);
            }
        }
    }
    
    
    private void tickMrPresidentOutOfBounds() {
        if (entity.level.isClientSide()) return;
        if (entity.level.dimension() != ModDimensions.MR_PRESIDENT
                || entity.isSpectator()
                || (entity instanceof Player) && ((Player) entity).isCreative()) {
            mrPresidentRoomPos = null;
            return;
        }
        if (mrPresidentRoomPos == null) {
            mrPresidentRoomPos = new MrPresidentWorldData.ChunkSectionPos(entity.blockPosition());
        }
        else if (!mrPresidentRoomPos.isPosInsideSection(entity.blockPosition())) {
            BlockPos posMoveTo = mrPresidentRoomPos.blockPosition(8, 6, 8);
            Vec3 pos = Vec3.atBottomCenterOf(posMoveTo);
            entity.moveTo(pos.x, pos.y, pos.z, entity.yRot, entity.xRot);
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer) entity).connection.send(
                        new ClientboundPlayerPositionPacket(pos.x, pos.y, pos.z, 
                                0, 0, Util.make(EnumSet.noneOf(RelativeMovement.class), set -> {
                                    set.add(RelativeMovement.X_ROT);
                                    set.add(RelativeMovement.Y_ROT);
                                }), -1));
            }
        }
    }
}
