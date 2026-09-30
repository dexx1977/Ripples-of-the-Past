package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.DeflectedBulletPacket;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public abstract class ModdedProjectileEntity extends DamagingEntity {
    private static final EntityDataAccessor<Boolean> IS_DEFLECTED = SynchedEntityData.defineId(ModdedProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private int ownerId = -1;
    
    protected ModdedProjectileEntity(EntityType<? extends ModdedProjectileEntity> type, LivingEntity shooter, Level world) {
        super(type, shooter, world);
        if (!hasGravity()) {
            setNoGravity(true);
        }
    }

    public ModdedProjectileEntity(EntityType<? extends ModdedProjectileEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void setOwner(Entity owner) {
        super.setOwner(owner);
        this.ownerId = owner == null ? -1 : owner.getId();
    }

    @Override
    public LivingEntity getOwner() {
        LivingEntity owner = super.getOwner();
        if (owner == null) {
            setOwner(level.getEntity(ownerId));
            owner = super.getOwner();
        }
        return owner;
    }
    
    public void shootFromRotation(Entity shooter, float velocity, float inaccuracy) {
        shootFromRotation(shooter, shooter.xRot, shooter.yRot, 0, velocity, inaccuracy);
    }

    @Override
    public void shootFromRotation(Entity shooter, float xRot, float yRot, float yAxisRotOffset, float velocity, float inaccuracy) {
        Vec3 shootingVec = Vec3.directionFromRotation(xRot, yRot);
        shoot(shootingVec.x, shootingVec.y, shootingVec.z, velocity, inaccuracy);
    }
    
    public void shoot(Entity shooter, Entity target, float velocity, float inaccuracy) {
        shoot(target.getX(), target.getY(), target.getZ(), velocity, inaccuracy);
    }
    
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        Vec3 movement = getDeltaMovement();
        yRot = MathUtil.yRotDegFromVec(movement);
        xRot = MathUtil.xRotDegFromVec(movement);
        yRotO = yRot;
        xRotO = xRot;
    }

    @Override
    public void tick() {
        Entity owner = getOwner();
        if (!level.isClientSide() && (tickCount > ticksLifespan() || owner != null && !owner.isAlive())) {
            discard();
            return;
        }
        super.tick();
        moveProjectile();
    }
    
    protected void moveProjectile() {
        Vec3 movementVec = getDeltaMovement();
        double x = getX();
        double y = getY();
        double z = getZ();
        double nextX = x + movementVec.x;
        double nextY = y + movementVec.y;
        double nextZ = z + movementVec.z;
        
        if (!constVelocity()) {
            if (!isNoGravity()) {
                movementVec = movementVec.subtract(0, getGravityAcceleration(), 0);
                setDeltaMovement(movementVec);
            }   
            
            rotateTowardsMovement(1.0F);
            
            double inertia = getInertia();
            if (isInWater()) {
                double bubblePosFactor = 0.25D;
                for (int j = 0; j < 4; ++j) {
                    level.addParticle(ParticleTypes.BUBBLE, 
                            nextX - movementVec.x * bubblePosFactor, 
                            nextY - movementVec.y * bubblePosFactor, 
                            nextZ - movementVec.z * bubblePosFactor, 
                            movementVec.x, movementVec.y, movementVec.z);
                }
                inertia *= getWaterInertiaFactor();
            }
            setDeltaMovement(movementVec.scale(inertia));
        }
        
        xo = x;
        yo = y;
        zo = z;
        xOld = x;
        yOld = y;
        zOld = z;
        setPos(nextX, nextY, nextZ);
        
//        IParticleData particle = getParticle();
//        if (particle != null) {
//            level.addParticle(particle, nextX, nextY, nextZ, 0.0D, 0.0D, 0.0D);
//        }
//        particle = getTrailParticle();
//        if (particle != null) {
//            for (int i = 0; i < 4; ++i) {
//                level.addParticle(getParticle(), x + movementVec.x * i / 4.0D, y + movementVec.y * i / 4.0D, z + movementVec.z * i / 4.0D, -movementVec.x, -movementVec.y + 0.2D, -movementVec.z);
//            }
//        }
    }
    
    protected boolean constVelocity() {
        return true;
    }
    
    protected boolean hasGravity() {
        return false;
    }

    protected double getGravityAcceleration() {
        return 0.03;
    }
    
    protected double getInertia() {
        return 0.99;
    }
    
    protected double getWaterInertiaFactor() {
        return 0.8;
    }
    
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
        if (!level.isClientSide()) {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (super.canHitEntity(entity)) {
            LivingEntity owner = getOwner();
            if (owner == null) {
                return true;
            }
            if (entity instanceof Projectile) {
                Entity otherOwner = ((Projectile) entity).getOwner();
                return otherOwner == null || owner.getUUID() != otherOwner.getUUID();
            }
            return true;
        }
        return false;
    }
    
    
    @Override
    public boolean canHitOwner() {
        return entityData.get(IS_DEFLECTED);
    }
    
    @Deprecated
    public void setIsDeflected() {
        setIsDeflected(null, this.position());
    }
    
    public void setIsDeflected(Vec3 deflectVec, Vec3 deflectPos) {
        if (!level.isClientSide()) {
            entityData.set(IS_DEFLECTED, true);
            if (hasDeflectedVisuals() && deflectVec != null) {
                PacketManager.sendToClientsTracking(new DeflectedBulletPacket(getId(), deflectVec, deflectPos, position()), this);
            }
        }
    }
    
    public boolean canBeDeflected(@Nullable Entity context) {
        return true;
    }
    
    public boolean hasDeflectedVisuals() {
        return false;
    }
    
    public boolean canBeEvaded(@Nullable Entity context) {
        return true;
    }
    
    
    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        breakProjectile(TargetType.ENTITY, entityRayTraceResult);
    }
    
    @Override
    protected void onHitBlock(BlockHitResult blockRayTraceResult) {
        super.onHitBlock(blockRayTraceResult);
        breakProjectile(TargetType.BLOCK, blockRayTraceResult);
    }
    
    protected void rotateTowardsMovement(float rotationSpeed) {
        Vec3 motionVec = getDeltaMovement();
        if (motionVec.lengthSqr() != 0) {
            yRot = MathUtil.yRotDegFromVec(motionVec);
            xRot = MathUtil.xRotDegFromVec(motionVec);
            while(xRot - xRotO < -180.0F) {
                xRotO -= 360.0F;
            }
            while(xRot - xRotO >= 180.0F) {
                xRotO += 360.0F;
            }
            while(yRot - yRotO < -180.0F) {
                yRotO -= 360.0F;
            }
            while(yRot - yRotO >= 180.0F) {
                yRotO += 360.0F;
            }
            yRot = Mth.lerp(rotationSpeed, yRotO, yRot);
            xRot = Mth.lerp(rotationSpeed, xRotO, xRot);
        }
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        markHurt();
        breakProjectile(TargetType.EMPTY, null);
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }
    
//    @Nullable
//    protected IParticleData getParticle() {
//        return null;
//    }
//    
//    @Nullable
//    protected IParticleData getTrailParticle() {
//        return null;
//    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = getBoundingBox().getSize() * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }
        d0 = d0 * 64.0D * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_DEFLECTED, false);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        if (ownerId < 0) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerId = owner.getId();
            }
        }
        buffer.writeInt(ownerId);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.ownerId = additionalData.readInt();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("IsDeflected", entityData.get(IS_DEFLECTED));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        entityData.set(IS_DEFLECTED, nbt.getBoolean("IsDeflected"));
    }
}
