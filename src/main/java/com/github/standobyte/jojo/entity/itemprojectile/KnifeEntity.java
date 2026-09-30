package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class KnifeEntity extends ItemProjectileEntity {
    private boolean timeStop = false;
    private Vec3 timeStopHitMotion;
    private int tsFlightTicks = 0;
    private TexVariant knifeTexVariant = TexVariant.KNIFE;

    public KnifeEntity(Level world, LivingEntity shooter) {
       super(ModEntityTypes.KNIFE.get(), shooter, world);
    }

    public KnifeEntity(Level world, double x, double y, double z) {
       super(ModEntityTypes.KNIFE.get(), x, y, z, world);
    }

    public KnifeEntity(EntityType<? extends KnifeEntity> type, Level world) {
       super(type, world);
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return withPickupItemTracking(new ItemStack(ModItems.KNIFE.get()));
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
       return ModSounds.KNIFE_HIT.get();
    }

    @Override
    protected SoundEvent getActualHitGroundSound(BlockState blockState, BlockPos blockPos) {
       return ModSounds.KNIFE_HIT.get();
    }
    
    public void setTimeStopFlightTicks(int ticks) {
        this.tsFlightTicks = ticks;
    }
    
    @Override
    public void tick() {
        if (timeStop) {
            if (tsFlightTicks > 0) {
                tsFlightTicks--;
            }
            else {
                super.canUpdate(false);
                return;
            }
        }
        
        super.tick();
        
        if (timeStopHitMotion != null) {
            setDeltaMovement(timeStopHitMotion);
            timeStopHitMotion = null;
        }
        
        if (!inGround && !level.isClientSide()) {
            Vec3 posVec = position();
            HitResult rayTraceResult = level.clip(new ClipContext(posVec, posVec.add(getDeltaMovement()), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
            if (rayTraceResult.getType() == Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) rayTraceResult).getBlockPos();
                Block block = level.getBlockState(blockPos).getBlock();
                if (block == Blocks.COBWEB) {
                    MCUtil.destroyBlock(level, blockPos, true, null);
                    setDeltaMovement(getDeltaMovement().scale(0.8D));
                }
                if (block == Blocks.TRIPWIRE) {
                    MCUtil.destroyBlock(level, blockPos, true, null);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult rayTraceResult) {
        if (timeStop && rayTraceResult.getType() == HitResult.Type.ENTITY) {
            if (!level.isClientSide()) {
                timeStopHitMotion = getDeltaMovement();
                setDeltaMovement(Vec3.ZERO);
            }
            tsFlightTicks = 0;
            super.canUpdate(false);
        }
        else {
            super.onHit(rayTraceResult);
        }
    }

    @Override
    public void canUpdate(boolean canUpdate) {
        this.timeStop = !canUpdate;
        if (canUpdate) {
            super.canUpdate(canUpdate);
        }
    }
//    
//    @Override
//    public void setSecondsOnFire(int seconds) {}
    
    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        float dmgAmount = getActualDamage();
        DamageSource damagesource = DamageSource.arrow(this, thrower == null ? this : thrower);
        return DamageUtil.hurtThroughInvulTicks(target, damagesource, dmgAmount);
    }
    
    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                SidedItemTrackerMap.getSidedTrackers(level).values().stream()
                .filter(tracker -> tracker.getAtEntity(level) == this)
                .forEach(tracker -> {
                    tracker.setAtEntity(entity.getId(), level, KnownItemState.STUCK_KNIFE);
                });
                
                cap.getStuckObjects().getKnives().increment();
            });
        }
    }

    @Override
    public boolean throwerCanCatch() {
        return false;
    }
    
    public void setKnifeType(TexVariant type) {
        this.knifeTexVariant = type;
    }
    
    public ResourceLocation getKnifeTexture() {
        return knifeTexVariant.texPath;
    }
    
    public static enum TexVariant {
        KNIFE(new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/knife.png")),
        SCALPEL(new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/knife_scalpel.png")),
        FISH(new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/knife_fish.png"));
        
        private final ResourceLocation texPath;
        private TexVariant(ResourceLocation texPath) {
            this.texPath = texPath;
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        timeStop = nbt.getBoolean("TimeStop");
        tsFlightTicks = nbt.getInt("TimeStopTicks");
        knifeTexVariant = MCUtil.nbtGetEnum(nbt, "KnifeType", TexVariant.class);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("TimeStop", timeStop);
        nbt.putInt("TimeStopTicks", tsFlightTicks);
        MCUtil.nbtPutEnum(nbt, "KnifeType", knifeTexVariant);
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(timeStop);
        buffer.writeVarInt(tsFlightTicks);
        buffer.writeEnum(knifeTexVariant);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        timeStop = additionalData.readBoolean();
        tsFlightTicks = additionalData.readVarInt();
        knifeTexVariant = additionalData.readEnum(TexVariant.class);
    }
}
