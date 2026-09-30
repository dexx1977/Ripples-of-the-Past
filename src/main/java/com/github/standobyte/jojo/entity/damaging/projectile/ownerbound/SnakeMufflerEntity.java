package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class SnakeMufflerEntity extends OwnerBoundProjectileEntity {
    private Entity entityToJumpOver;
    private UUID targetId;
    
    public SnakeMufflerEntity(Level world, LivingEntity entity) {
        super(ModEntityTypes.SNAKE_MUFFLER.get(), entity, world);
    }

    public SnakeMufflerEntity(EntityType<? extends SnakeMufflerEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    public void setEntityToJumpOver(Entity entity) {
        this.entityToJumpOver = entity;
        this.targetId = entity != null ? entity.getUUID() : null;
    }
    
    @Override
    public void tick() {
        super.tick();
        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            return;
        }
        if (tickCount < 5) {
            Vec3 jumpVec = new Vec3(0, 0.45, 0);
            if (entityToJumpOver != null && entityToJumpOver.isAlive()) {
                Vec3 posDiff = entityToJumpOver.position().subtract(owner.position());
                posDiff = posDiff.subtract(0, posDiff.y, 0);
                double lengthSqr = posDiff.lengthSqr();
                if (lengthSqr < 25) {
                    if (lengthSqr > 0.04) {
                        posDiff = posDiff.scale(0.4 / Math.sqrt(lengthSqr));
                    }
                    jumpVec = posDiff.add(0, (entityToJumpOver.getBbHeight() + 0.5) / ticksLifespan(), 0);
                }
            }
            owner.setDeltaMovement(jumpVec);
        }
        owner.fallDistance = 0;
    }

    @Override
    public int ticksLifespan() {
        return 10;
    }

    @Override
    public float getBaseDamage() {
        return 0;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    protected float movementSpeed() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putUUID("TargetEntity", targetId);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        targetId = nbt.getUUID("TargetEntity");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        if (entityToJumpOver == null && targetId != null) {
            entityToJumpOver = ((ServerLevel) level).getEntity(targetId);
        }
        buffer.writeBoolean(entityToJumpOver != null);
        if (entityToJumpOver != null) {
            buffer.writeInt(entityToJumpOver.getId());
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        entityToJumpOver = additionalData.readBoolean() ? level.getEntity(additionalData.readInt()) : null;
    }
}
