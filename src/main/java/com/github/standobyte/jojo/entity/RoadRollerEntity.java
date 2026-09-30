package com.github.standobyte.jojo.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkHooks;

public class RoadRollerEntity extends Entity implements IHasHealth {
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(RoadRollerEntity.class, EntityDataSerializers.FLOAT);
    private static final float MAX_HEALTH = 50;
    private int ticksBeforeExplosion = -1;
    private int ticksInAir = 0;
    @Nullable
    private Entity owner;
    @Nullable
    private UUID ownerId;
    private double tickDamageMotion = 0;
    private boolean punchedFromBelow = false;

    public RoadRollerEntity(Level world) {
        this(ModEntityTypes.ROAD_ROLLER.get(), world);
    }

    public RoadRollerEntity(EntityType<RoadRollerEntity> type, Level world) {
        super(type, world);
    }
    
    public void setOwner(Entity entity) {
        this.owner = entity;
        this.ownerId = entity != null ? entity.getUUID() : null;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
       return this.getBbHeight() * 0.95D + Math.abs(xRot) / 90D;
    }

    @Override
    public void push(Entity entity) {}
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    @Override
    public boolean canUpdate() {
        return super.canUpdate() || isVehicle() && getPassengers().get(0).canUpdate();
    }

    @Override
    public void tick() {
        boolean wasOnGround = onGround;
        super.tick();
        if (!super.canUpdate()) {
            tickCount--;
        }
        Vec3 movement = getDeltaMovement();
        if (!isNoGravity() && !punchedFromBelow) {
            setDeltaMovement(movement.add(-movement.x, -0.0467D, -movement.z));
        }
        
        tickDamageMotion = 0;
        punchedFromBelow = false;
        
        DamageSource dmgSource = DamageUtil.roadRollerDamage(this);
        float damage = (float) -getDeltaMovement().y * 10F;
        if (damage > 0) {
            AABB aabb = getBoundingBox().contract(0, getBbHeight() * 0.75, 0).expandTowards(0, -getBbHeight() * 0.25, 0);
            level.getEntitiesOfClass(LivingEntity.class, aabb, 
                    EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(entity -> !this.is(entity.getVehicle()))).forEach(entity -> {
                        if (!entity.isInvulnerableTo(dmgSource)) {
                            if (!level.isClientSide()) {
                                entity.hurt(dmgSource, damage);
                            }
                            entity.setDeltaMovement(Vec3.ZERO);
                        }
                    });
        }
        
        move(MoverType.SELF, getDeltaMovement());
        if (onGround) {
            setDeltaMovement(movement.x, 0, movement.z);
            if (xRot > 0) {
                xRot = Math.max(xRot - 6, 0);
            }
            else {
                xRot = Math.min(xRot + 6, 0);
            }
        }
        if (level.isClientSide() && !wasOnGround) {
            ticksInAir++;
            if (onGround) {
                level.playSound(ClientUtil.getClientPlayer(), getX(), getY(), getZ(), ModSounds.ROAD_ROLLER_LAND.get(), 
                        getSoundSource(), (float) ticksInAir * 0.05F, 1.0F);
                ticksInAir = 0;
            }
        }
        
        if (ticksBeforeExplosion > 0) {
            ticksBeforeExplosion--;
        }
        else if (ticksBeforeExplosion == 0) {
            discard();
        }
        Entity owner = getOwner();
        if (!level.isClientSide() && (ticksBeforeExplosion == 0 || 
                (ticksBeforeExplosion > 0 && ticksBeforeExplosion < 40 && owner != null && distanceToSqr(owner) > 100))) {
            explode();
            discard();
        }
    }
    
    private Entity getOwner() {
        if (!level.isClientSide() && owner == null && ownerId != null) {
            owner = ((ServerLevel) level).getEntity(ownerId);
        }
        return owner;
    }

    private void explode() {
        level.explode(this, getX(), getY(0.0625D), getZ(), 4.0F, Level.ExplosionInteraction.NONE); // the old Explosion.Mode.NONE
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    
    @Override
    public boolean isPickable() {
        return true;
    }

    private static final Vec3 UPWARDS_VECTOR = new Vec3(0.0D, 1.0D, 0.0D);
    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        } else {
            if (!level.isClientSide()) {
                double cos = -1;
                if (dmgSource.getDirectEntity() != null) {
                    Vec3 dmgPos = dmgSource.getDirectEntity().getEyePosition(1.0F);
                    Vec3 dmgVec = dmgPos.vectorTo(position()).normalize();
                    cos = dmgVec.dot(UPWARDS_VECTOR);
                    double damageMotion = cos * amount * 0.08D;
                    if (damageMotion > 0) {
                        damageMotion = Math.min(-this.tickDamageMotion, damageMotion);
                        punchedFromBelow = true;
                    }
                    setDeltaMovement(getDeltaMovement().add(0, damageMotion, 0));
                    this.tickDamageMotion += damageMotion;
                }
                if (getHealth() > 0) {
                    setHealth(cos < 0 ? getHealth() - amount : getHealth() + amount);
                }
                markHurt();
                level.playSound(null, getX(), getY(), getZ(), ModSounds.ROAD_ROLLER_HIT.get(), 
                        getSoundSource(), amount * 0.25F, 1.0F + (random.nextFloat() - 0.5F) * 0.3F);
            }
            return true;
        }
    }

    @Override
    public float getHealth() {
        return entityData.get(HEALTH);
    }

    @Override
    public void setHealth(float health) {
        entityData.set(HEALTH, Mth.clamp(health, 0.0F, getMaxHealth()));
    }

    @Override
    public float getMaxHealth() {
        return MAX_HEALTH;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
        super.onSyncedDataUpdated(dataParameter);
        if (HEALTH.equals(dataParameter)) {
            if (getHealth() <= 0.0F) {
                ticksBeforeExplosion = 60;
                if (!level.isClientSide()) {
                    ejectPassengers();
                }
            }
            else {
                ticksBeforeExplosion = -1;
            }
        }
    }
    
    public int getTicksBeforeExplosion() {
        return ticksBeforeExplosion;
    }
    
    @Override
    protected void defineSynchedData() {
        entityData.define(HEALTH, MAX_HEALTH);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("Health")) {
            setHealth(nbt.getFloat("Health"));
        }
        tickCount = nbt.getInt("Age");
        if (nbt.contains("ExplosionTime")) {
            ticksBeforeExplosion = nbt.getInt("ExplosionTime");
        }
        if (nbt.hasUUID("Owner")) {
            ownerId = nbt.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putFloat("Health", getHealth());
        nbt.putInt("Age", tickCount);
        nbt.putInt("ExplosionTime", ticksBeforeExplosion);
        if (owner != null) {
            nbt.putUUID("Owner", ownerId);
        }
    }

}
