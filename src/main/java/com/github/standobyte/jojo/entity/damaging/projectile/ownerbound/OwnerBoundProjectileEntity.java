package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.potion.ImmobilizeEffect;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public abstract class OwnerBoundProjectileEntity extends ModdedProjectileEntity {
    protected static final EntityDataAccessor<Boolean> IS_BOUND_TO_OWNER = SynchedEntityData.defineId(OwnerBoundProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> BLOCK_ATTACHED_TO = SynchedEntityData.defineId(OwnerBoundProjectileEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> ENTITY_ATTACHED_TO = SynchedEntityData.defineId(OwnerBoundProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MOVING_FORWARD = SynchedEntityData.defineId(OwnerBoundProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_RETRACTING = SynchedEntityData.defineId(OwnerBoundProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private double distance;
    private LivingEntity attachedEntity;
    private UUID attachedEntityUUID;
    private int lifeSpan;

    public OwnerBoundProjectileEntity(EntityType<? extends OwnerBoundProjectileEntity> entityType, @Nonnull LivingEntity owner, Level world) {
        super(entityType, owner, world);
    }

    public OwnerBoundProjectileEntity(EntityType<? extends OwnerBoundProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        if (isBoundToOwner()) {
            LivingEntity owner = getOwner();
            if (owner == null) {
                if (!level.isClientSide()) {
                    discard();
                }
                return;
            }
        }
        if (!level.isClientSide() && attachedEntityUUID != null && attachedEntity == null) {
            Entity entity = ((ServerLevel) level).getEntity(attachedEntityUUID);
            if (entity instanceof LivingEntity) {
                attachToEntity((LivingEntity) entity);
                attachedEntityUUID = null;
            }
        }
        dragged.forEach(entity -> entity.setDeltaMovement(Vec3.ZERO));
        dragged.clear();
        super.tick();
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        dragged.forEach(entity -> entity.setDeltaMovement(Vec3.ZERO));
    }
    
    @Override
    protected void moveProjectile() {
        if (!moveToEntityAttached() && !moveToBlockAttached() && !moveBoundToOwner()) {
            super.moveProjectile();
        }
    }
    
    protected boolean moveBoundToOwner() {
        if (isBoundToOwner()) {
            Entity owner = getOwner();
            setRot(owner.yRot, owner.xRot);
    
            Vec3 originPoint = ownerPosition(1.0F, false);
            Vec3 nextOriginOffset = getNextOriginOffset();
            if (nextOriginOffset == null) {
                if (!level.isClientSide()) {
                    discard();
                }
                return true;
            }
    
            double x = getX();
            double y = getY();
            double z = getZ();
            double nextX = originPoint.x + nextOriginOffset.x;
            double nextY = originPoint.y + nextOriginOffset.y;
            double nextZ = originPoint.z + nextOriginOffset.z;
            if (!level.getChunkSource().hasChunk(Mth.floor(nextX) >> 4, Mth.floor(nextZ) >> 4)) {
                if (level.isClientSide()) {
                    discard();
                }
                return false;
            }
            setDeltaMovement(new Vec3(nextX - getX(), nextY - getY(), nextZ - getZ()));
    
            xo = x;
            yo = y;
            zo = z;
            xOld = x;
            yOld = y;
            zOld = z;
            setPos(nextX, nextY, nextZ);
            return true;
        }
        return false;
    }
    
    protected boolean moveToEntityAttached() {
        LivingEntity bound = getEntityAttachedTo();
        if (bound != null) {
            moveTo(bound.getX(), bound.getY(attachedTargetHeight()), bound.getZ(), bound.yRot, bound.xRot);
            return true;
        }
        return false;
    }
    
    protected double attachedTargetHeight() {
        return 0.5;
    }
    
    protected boolean moveToBlockAttached() {
        Optional<BlockPos> blockPosOptional = getBlockPosAttachedTo();
        if (blockPosOptional.isPresent()) {
            BlockPos blockPos = blockPosOptional.get();
            moveTo(
                    blockPos.getX() + 0.5D, 
                    blockPos.getY() + 0.5D, 
                    blockPos.getZ() + 0.5D);
            return true;
        }
        return false;
    }
    
    protected final Vec3 getOriginPoint() {
        return getOriginPoint(1.0F);
    }
    
    public Vec3 getOriginPoint(float partialTick) {
        return ownerPosition(partialTick, isBodyPart());
    }
    
    protected final Vec3 ownerPosition(float partialTick, boolean useBodyRotation) {
        LivingEntity owner = getOwner();
        if (owner != null) {
            return getPos(owner, partialTick, 
                    useBodyRotation ? Mth.lerp(partialTick, owner.yBodyRotO, owner.yBodyRot) : Mth.lerp(partialTick, owner.yRotO, owner.yRot), 
                            Mth.lerp(partialTick, owner.xRotO, owner.xRot));
        }
        return MCUtil.getEntityPosition(this, partialTick);
    }
    
    public boolean isBodyPart() {
        return false;
    }
    
    @Nullable
    protected Vec3 getNextOriginOffset() {
        LivingEntity owner = getOwner();
        double distance = updateDistance();
        updateMotionFlags();
        if (isRetracting() && distance <= 0) {
            return null;
        }
        setDistance(distance);
        return originOffset(owner.yRot, owner.xRot, distance);
    }
    
    protected float updateDistance() {
        if (isRetracting()) {
            return (float) (getDistance() - retractSpeed() * speedFactor);
        }
        if (isMovingForward()) {
            return (float) (getDistance() + movementSpeed() * speedFactor);
        }
        return (float) getDistance();
    }
    
    protected abstract float movementSpeed();
    
    protected int timeAtFullLength() {
        return 0;
    }

    protected float retractSpeed() {
        return movementSpeed();
    }
    
    protected void updateMotionFlags() {
        int stopForwardMotionMark = (int) (maxDistance() / movementSpeed());
        if (isMovingForward() && tickCount >= stopForwardMotionMark) {
            setIsMovingForward(false);
        }
        if (!isRetracting() && tickCount >= stopForwardMotionMark + timeAtFullLength()) {
            setIsRetracting(true);
        }
    }
    
    private double maxDistance() {
        return movementSpeed() * retractSpeed() * (ticksLifespan() - timeAtFullLength()) / (movementSpeed() + retractSpeed());
    }
    
    protected Vec3 originOffset(float yRot, float xRot, double distance) {
        return Vec3.directionFromRotation(xRot, yRot).scale(distance);
    }
    
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().expandTowards(getOriginPoint().subtract(position()));
    }

    @Override
    protected HitResult[] rayTrace() {
        Vec3 startPos = getOriginPoint();
        Vec3 endPos = position().add(getDeltaMovement());
        Vec3 rtVec = startPos.subtract(endPos);
        AABB aabb = getBoundingBox().expandTowards(rtVec).inflate(1.0D);
        double minDistance = rtVec.length();
        return JojoModUtil.rayTraceMultipleEntities(startPos, endPos, aabb, 
                minDistance, level, this, this::canHitEntity, 
                getBbWidth() / 2, 0);
    }
    
    @Override
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float dmgAmount) {
        return shouldHurtThroughInvulTicks() ? super.hurtTarget(target, dmgSource, dmgAmount) : 
            target.hurt(DamageUtil.enderDragonDamageHack(dmgSource, target), dmgAmount);
    }
    
    protected boolean shouldHurtThroughInvulTicks() {
        return false;
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {}

    @Override
    protected void afterBlockHit(BlockHitResult blockRayTraceResult, boolean blockDestroyed) {
        if (!blockDestroyed) {
            setIsRetracting(true);
        }
    }
    
    protected void setBoundToOwner(boolean value) {
        entityData.set(IS_BOUND_TO_OWNER, value);
    }
    
    public boolean isBoundToOwner() {
        return entityData.get(IS_BOUND_TO_OWNER);
    }
    
    public void attachToEntity(LivingEntity boundTarget) {
        this.attachedEntity = boundTarget;
        entityData.set(ENTITY_ATTACHED_TO, boundTarget.getId());
    }
    
    @Nullable
    public LivingEntity getEntityAttachedTo() {
        if (attachedEntity == null) {
            int id = entityData.get(ENTITY_ATTACHED_TO);
            if (id == -1) {
                return null;
            }
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity) {
                attachedEntity = (LivingEntity) entity;
            }
        }
        return attachedEntity;
    }
    
    public boolean isAttachedToAnEntity() {
        return entityData.get(ENTITY_ATTACHED_TO) > -1;
    }
    
    private final Set<Entity> dragged = new HashSet<>();
    protected void dragTarget(Entity entity, Vec3 vec) {
        entity = entity.getRootVehicle();
        doDragEntity(entity, vec);
        if (entity instanceof StandEntity) {
            LivingEntity standUser = ((StandEntity) entity).getUser();
            if (standUser != null) {
                doDragEntity(entity, vec);
            }
        }
    }
    
    private void doDragEntity(Entity entity, Vec3 vec) {
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;
            for (MobEffect effect : target.getActiveEffectsMap().keySet()) {
                if (effect instanceof ImmobilizeEffect && ((ImmobilizeEffect) effect).resetsDeltaMovement()) {
                    entity.move(MoverType.PLAYER, vec);
                    return;
                }
            }
        }
        entity.setDeltaMovement(vec);
        dragged.add(entity);
    }
    
    public void attachToBlockPos(BlockPos blockPos) {
        entityData.set(BLOCK_ATTACHED_TO, Optional.of(blockPos));
    }
    
    public Optional<BlockPos> getBlockPosAttachedTo() {
        return entityData.get(BLOCK_ATTACHED_TO);
    }
    
    protected void setDistance(double distance) {
        this.distance = distance;
    }
    
    protected double getDistance() {
        return distance;
    }
    
    protected void setIsMovingForward(boolean isMovingForward) {
        entityData.set(IS_MOVING_FORWARD, isMovingForward);
    }
    
    protected boolean isMovingForward() {
        return entityData.get(IS_MOVING_FORWARD);
    }
    
    protected void setIsRetracting(boolean isRetracting) {
        entityData.set(IS_RETRACTING, isRetracting);
    }
    
    protected boolean isRetracting() {
        return entityData.get(IS_RETRACTING);
    }
    
    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
    
    @Override
    public int ticksLifespan() {
        return lifeSpan;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_BOUND_TO_OWNER, true);
        entityData.define(ENTITY_ATTACHED_TO, -1);
        entityData.define(BLOCK_ATTACHED_TO, Optional.empty());
        entityData.define(IS_MOVING_FORWARD, true);
        entityData.define(IS_RETRACTING, false);
    }
    
    @Override
    public boolean isInvisible() {
        boolean ownerInvisible = false;
        if (ownerInvisibility()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerInvisible = owner.isInvisible();
            }
        }
        return ownerInvisible || super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        boolean ownerInvisible = false;
        if (ownerInvisibility()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerInvisible = owner.isInvisibleTo(player);
            }
        }
        return ownerInvisible || super.isInvisibleTo(player);
    }
    
    public boolean ownerInvisibility() {
        return isBodyPart();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return isBoundToOwner() && getOwner() == ClientUtil.getClientPlayer() ? true : super.shouldRenderAtSqrDistance(distance);
    }
    
    @Override
    public SoundSource getSoundSource() {
        return isBoundToOwner() && getOwner() != null ? getOwner().getSoundSource() : super.getSoundSource();
    }
    
    @Override
    public boolean isSilent() {
        return isBoundToOwner() && getOwner() != null ? getOwner().isSilent() : super.isSilent();
    }
    
    @Override
    public boolean canUpdate() {
        return isBoundToOwner() && getOwner() != null ? getOwner().canUpdate() : super.canUpdate();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("BoundToOwner", isBoundToOwner());
        Optional<BlockPos> blockAttachedTo = getBlockPosAttachedTo();
        if (blockAttachedTo.isPresent()) {
            BlockPos pos = blockAttachedTo.get();
            nbt.putIntArray("AttachedBlock", new int[] { pos.getX(), pos.getY(), pos.getZ() } );
        }
        else if (attachedEntity != null) {
            nbt.putUUID("AttachedEntity", attachedEntity.getUUID());
        }
        nbt.putDouble("Distance", getDistance());
        nbt.putBoolean("IsMovingForward", isMovingForward());
        nbt.putBoolean("IsRetracting", isRetracting());
        nbt.putInt("LifeSpan", lifeSpan);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setBoundToOwner(nbt.getBoolean("BoundToOwner"));
        int[] posArray = nbt.getIntArray("AttachedBlock");
        if (posArray.length == 3) {
            attachToBlockPos(new BlockPos(posArray[0], posArray[1], posArray[2]));
        }
        else if (nbt.hasUUID("BoundTarget")) {
            this.attachedEntityUUID = nbt.getUUID("AttachedEntity");
        }
        setDistance(nbt.getDouble("Distance"));
        setIsMovingForward(nbt.getBoolean("IsMovingForward"));
        setIsRetracting(nbt.getBoolean("IsRetracting"));
        lifeSpan = nbt.getInt("LifeSpan");
     }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeVarInt(lifeSpan);
        buffer.writeDouble(distance);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        lifeSpan = additionalData.readVarInt();
        distance = additionalData.readDouble();
    }
}
