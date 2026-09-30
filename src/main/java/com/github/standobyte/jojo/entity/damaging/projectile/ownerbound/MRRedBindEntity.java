package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class MRRedBindEntity extends OwnerBoundProjectileEntity {
    protected static final EntityDataAccessor<Boolean> KICK_FINISHER = SynchedEntityData.defineId(MRRedBindEntity.class, EntityDataSerializers.BOOLEAN);
    
    private StandEntity ownerStand;
    private MobEffectInstance immobilizedEffect = null;
    private int ticksTargetClose = 0;

    public MRRedBindEntity(Level world, StandEntity entity) {
        super(ModEntityTypes.MR_RED_BIND.get(), entity, world);
        this.ownerStand = entity;
    }
    
    public MRRedBindEntity(EntityType<? extends MRRedBindEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
            return;
        }
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!level.isClientSide()) {
            if (ownerStand == null || ownerStand.getCurrentTaskAction() != ModStandsInit.MAGICIANS_RED_RED_BIND.get() && !isInKickAttack()) {
                discard();
                return;
            }
        }
        LivingEntity bound = getEntityAttachedTo();
        if (bound != null) {
            LivingEntity owner = getOwner();
            if (!bound.isAlive() || owner.distanceToSqr(bound) > 100) {
                if (!level.isClientSide()) {
                    discard();
                }
            }
            else {
                if (!level.isClientSide()) {
                    if (bound.getRemainingFireTicks() % 20 == 0 || bound.getRemainingFireTicks() <= 0) {
                        DamageUtil.setOnFire(bound, 3, true);
                    }
                    DamageUtil.suffocateTick(bound, isInKickAttack() ? 1 : 0.0025F);
                }
                Vec3 vecToOwner = owner.position().subtract(bound.position());
                if (vecToOwner.lengthSqr() > 4) {
                    dragTarget(bound, vecToOwner.normalize().scale(0.2));
                    ticksTargetClose = 0;
                }
                else if (!level.isClientSide() && !isInKickAttack() && ticksTargetClose++ > 10) {
                    discard();
                }
            }
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerLevel) level);
        }
    }
    
    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean isFiery() {
        return true;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (getEntityAttachedTo() == null) {
            if (target instanceof LivingEntity && !target.isInvulnerableTo(getDamageSource(owner))) {
                LivingEntity targetLiving = (LivingEntity) target;
                if (!JojoModUtil.isTargetBlocking(targetLiving)) {
                    attachToEntity(targetLiving);
                    if (!level.isClientSide()) {
                        boolean thisEffect = immobilizedEffect == targetLiving.getEffect(ModStatusEffects.IMMOBILIZE.get());
                        targetLiving.addEffect(new MobEffectInstance(ModStatusEffects.IMMOBILIZE.get(), ticksLifespan() - tickCount));
                        if (thisEffect) {
                            immobilizedEffect = targetLiving.getEffect(ModStatusEffects.IMMOBILIZE.get());
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    protected double attachedTargetHeight() {
        return this.isInKickAttack() ? 0.75 : super.attachedTargetHeight();
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level.isClientSide() && immobilizedEffect != null) {
            LivingEntity bound = getEntityAttachedTo();
            if (bound != null) {
                MCUtil.removeEffectInstance(bound, immobilizedEffect);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(KICK_FINISHER, false);
    }
    
    public void setKickAttack() {
        entityData.set(KICK_FINISHER, true);
        LivingEntity target = getEntityAttachedTo();
        if (target != null) {
            MCUtil.removeEffectInstance(target, immobilizedEffect);
            target.addEffect(new MobEffectInstance(ModStatusEffects.STUN.get(), ticksLifespan() - tickCount));
            immobilizedEffect = target.getEffect(ModStatusEffects.STUN.get());
        }
    }
    
    public boolean isInKickAttack() {
        return entityData.get(KICK_FINISHER);
    }
    
    public StandEntity getOwnerAsStand() {
        if (ownerStand == null) {
            LivingEntity owner = getOwner();
            ownerStand = owner instanceof StandEntity ? (StandEntity) owner : null;
        }
        return ownerStand;
    }

    private static final Vec3 OFFSET = new Vec3(0, -0.25, 0.5);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return 0.0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    public int ticksLifespan() {
        return isAttachedToAnEntity() ? 
                isInKickAttack() ? Integer.MAX_VALUE : 100
                : 7;
    }
    
    @Override
    protected float movementSpeed() {
        return 1.0F;
    }
    
    @Override
    protected void updateMotionFlags() {}
}
