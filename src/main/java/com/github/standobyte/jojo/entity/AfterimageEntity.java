package com.github.standobyte.jojo.entity;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class AfterimageEntity extends Entity implements IEntityAdditionalSpawnData {
    private LivingEntity originEntity;
    private UUID originUuid;
    private int ticksDelayed;
    private int delay;
    private int lifeSpan;
    private double speedLowerLimit;
    private Queue<PosData> originPosQueue = new LinkedList<PosData>();
    
    public AfterimageEntity(Level world, LivingEntity originEntity, int delay) {
        this(ModEntityTypes.AFTERIMAGE.get(), world);
        setOriginEntity(originEntity);
        this.delay = delay;
        this.lifeSpan = 1200;
    }

    public AfterimageEntity(EntityType<?> type, Level world) {
        super(type, world);
        noPhysics = true;
    }
    
    private void setOriginEntity(LivingEntity entity) {
        this.originEntity = entity;
        if (entity != null) {
            copyPosition(entity);
        }
    }
    
    public LivingEntity getOriginEntity() {
        return originEntity;
    }
    
    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
    
    public void setMinSpeed(double speed) {
        this.speedLowerLimit = speed;
    }

    /** debug helper */
    public double getSpeedLowerLimitForDebug() {
        return speedLowerLimit;
    }
    
    public boolean shouldRender() {
        return originEntity != null && originEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) >= speedLowerLimit;
    }
    
    @Override
    public void tick() {
        super.tick();
        ticksDelayed++;
        if (originEntity == null || !originEntity.isAlive() || !level.isClientSide() && tickCount > lifeSpan) {
            discard();
            return;
        }
        originPosQueue.add(new PosData(originEntity.position(), originEntity.xRot, originEntity.yRot));
        if (ticksDelayed > delay) {
            PosData posData = originPosQueue.remove();
            moveTo(posData.pos.x, posData.pos.y, posData.pos.z, posData.yRot, posData.xRot);
        }
        
        if (!level.isClientSide() && originEntity.isSprinting() && shouldRender()) {
            level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(8), mob -> 
            mob.getTarget() == originEntity && mob.hasLineOfSight(this)).forEach(mob -> {
                if (mob.getRandom().nextDouble() < 0.01) {
                    MCUtil.loseTarget(mob, originEntity);
                }
            });
        }
    }

    @Override
    protected void defineSynchedData() {}
    
    @Override
    public boolean isInvisible() {
        return super.isInvisible() || originEntity != null && originEntity.isInvisible();
    }
    
    @Override
    public boolean isInvisibleTo(Player player) {
        return super.isInvisibleTo(player) || originEntity != null && originEntity.isInvisibleTo(player);
    }
    
    @Override
    public boolean displayFireAnimation() {
        return originEntity != null && originEntity.displayFireAnimation();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.delay = nbt.getInt("Delay");
        this.tickCount = nbt.getInt("Age");
        this.lifeSpan = nbt.getInt("LifeSpan");
        this.speedLowerLimit = nbt.getDouble("Speed");
        if (nbt.hasUUID("Origin")) {
            this.originUuid = nbt.getUUID("Origin");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Delay", delay);
        nbt.putInt("Age", tickCount);
        nbt.putInt("LifeSpan", lifeSpan);
        nbt.putDouble("Speed", speedLowerLimit);
        if (originUuid != null) {
            nbt.putUUID("Origin", originEntity.getUUID());
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (originUuid != null) {
            Entity entity = ((ServerLevel) level).getEntity(originUuid);
            if (entity instanceof LivingEntity) {
                setOriginEntity((LivingEntity) entity);
            }
        }
        buffer.writeInt(originEntity == null ? -1 : originEntity.getId());
        buffer.writeVarInt(delay);
        buffer.writeInt(lifeSpan);
        buffer.writeDouble(speedLowerLimit);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            setOriginEntity((LivingEntity) entity);
        }
        delay = additionalData.readVarInt();
        lifeSpan = additionalData.readInt();
        speedLowerLimit = additionalData.readDouble();
    }

    private static class PosData {
        private final Vec3 pos;
        private final float xRot;
        private final float yRot;
        
        private PosData(Vec3 pos, float xRot, float yRot) {
            this.pos = pos;
            this.xRot = xRot;
            this.yRot = yRot;
        }
    }
}
