package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class SpaceRipperStingyEyesEntity extends OwnerBoundProjectileEntity {
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(SpaceRipperStingyEyesEntity.class, EntityDataSerializers.FLOAT);
    private boolean rightEye;
    private Vec3 detachedOriginPos;

    public SpaceRipperStingyEyesEntity(Level world, LivingEntity owner, boolean rightEye) {
        super(ModEntityTypes.SPACE_RIPPER_STINGY_EYES.get(), owner, world);
        this.rightEye = rightEye;
    }
    
    public SpaceRipperStingyEyesEntity(EntityType<? extends SpaceRipperStingyEyesEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!isBoundToOwner()) {
            detachedOriginPos = detachedOriginPos.add(position().subtract(xOld, yOld, zOld));
        }
        if (tickCount > 20) {
            detach();
        }
    }
    
    public void detach() {
        if (isBoundToOwner() && !level.isClientSide()) {
            setBoundToOwner(false);
            setDeltaMovement(position().subtract(getOriginPoint()).normalize().scale(movementSpeed()));
        }
    }
    
    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource != DamageSource.OUT_OF_WORLD && !damageSource.isCreativePlayer();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
        if (IS_BOUND_TO_OWNER.equals(dataParameter) && !isBoundToOwner() && getOwner() != null) {
            detachedOriginPos = getOriginPoint();
            setLength((float) position().subtract(detachedOriginPos).length());
        }
        super.onSyncedDataUpdated(dataParameter);
    }
    
    @Override
    public void setOwner(Entity owner) {
        super.setOwner(owner);
    }
    
    private void setLength(float length) {
        entityData.set(LENGTH, length);
    }
    
    public float getLength() {
        return entityData.get(LENGTH);
    }
    
    private static final Vec3 OFFSET_LEFT_EYE = new Vec3(0.09375, -0.2, 0.0);
    private static final Vec3 OFFSET_RIGHT_EYE = new Vec3(-OFFSET_LEFT_EYE.x, OFFSET_LEFT_EYE.y, OFFSET_LEFT_EYE.z);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return rightEye ? OFFSET_RIGHT_EYE : OFFSET_LEFT_EYE;
    }
    
    private static final Vec3 OFFSET_XROT = new Vec3(0, 0.2, 0.0);
    @Override
    protected Vec3 getXRotOffset() {
        return OFFSET_XROT;
    }
    
    @Override
    public Vec3 getOriginPoint(float partialTick) {
        if (!isBoundToOwner()) {
            if (detachedOriginPos == null) {
                detachedOriginPos = super.getOriginPoint(partialTick);
            }
            return detachedOriginPos;
        }
        return super.getOriginPoint(partialTick);
    }

    @Override
    public int ticksLifespan() {
        if (isBoundToOwner()) {
            return 50;
        }
        return Mth.floor(getLength() / (float) movementSpeed() * 20F) + 20;
    }
    
    @Override
    protected float movementSpeed() {
        return 0.5F + level.getDifficulty().getId() * 0.25F;
    }
    
    @Override
    protected void updateMotionFlags() {}

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public float getBaseDamage() {
        return 1.0F + level.getDifficulty().getId();
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 3.0F;
    }
    
    @Override
    protected float knockbackMultiplier() {
        return 0;
    }
    
    @Override
    public boolean canBeDeflected(Entity context) {
        return context != null && context.getType() == ModEntityTypes.HAMON_PROJECTILE_SHIELD.get();
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LENGTH, 0F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Length", getLength());
        nbt.putBoolean("IsRightEye", rightEye);
        if (detachedOriginPos != null) {
            nbt.put("DetachedOrigin", newDoubleList(detachedOriginPos.x, detachedOriginPos.y, detachedOriginPos.z));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("DetachedOrigin", MCUtil.getNbtId(ListTag.class))) {
            ListTag detachedPosList = nbt.getList("DetachedOrigin", MCUtil.getNbtId(DoubleTag.class));
            if (detachedPosList.size() >= 3) {
                detachedOriginPos = new Vec3(detachedPosList.getDouble(0), detachedPosList.getDouble(1), detachedPosList.getDouble(2));
            }
        }
        super.readAdditionalSaveData(nbt);
        setLength(nbt.getFloat("Length"));
        rightEye = nbt.getBoolean("IsRightEye");
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(rightEye);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        rightEye = additionalData.readBoolean();
    }
}
