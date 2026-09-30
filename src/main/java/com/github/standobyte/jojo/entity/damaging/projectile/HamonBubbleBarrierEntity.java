package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class HamonBubbleBarrierEntity extends ModdedProjectileEntity {
    private int barrierTicks;
    private int barrierMaxTicks;
    private boolean barrier;
    private boolean shot;
    private INonStandPower power;
    
    public HamonBubbleBarrierEntity(Level world, LivingEntity shooter, INonStandPower power) {
        super(ModEntityTypes.HAMON_BUBBLE_BARRIER.get(), shooter, world);
        this.power = power;
        barrierMaxTicks = (int) (100F * power.getTypeSpecificData(ModPowers.HAMON.get())
                .map(hamon -> hamon.getActionEfficiency(0, true, ModHamonSkills.BUBBLE_BARRIER.get())).orElse(1F));
    }

    public HamonBubbleBarrierEntity(EntityType<? extends HamonBubbleBarrierEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) { 
            if (barrier && (barrierTicks++ >= barrierMaxTicks || !isVehicle()) || power == null) {
                discard();
            }
            else if (!shot) {
                if (power.getHeldAction() != ModHamonActions.CAESAR_BUBBLE_BARRIER.get()) {
                    discard();
                }
                else if (power.getHeldActionTicks() >= ModHamonActions.CAESAR_BUBBLE_BARRIER.get().getHoldDurationToFire(power) - 1) {
                    Entity owner = getOwner();
                    shootFromRotation(owner != null ? owner : this, 1.0F, 0.0F);
                    shot = true;
                }
            }
            else if (isVehicle() && tickCount % 5 % 2 == 0) {
                DamageUtil.dealHamonDamage(getPassengers().get(0), 0.002F, this, getOwner());
            }
        }
        else {
            Vec3 sparkVec = Vec3.directionFromRotation(random.nextFloat() * 360F, random.nextFloat() * 360F)
                    .scale(getBbWidth() / 2).add(getX(), getY(0.5), getZ());
            // FIXME ! (hamon 2) sfx
            HamonUtil.emitHamonSparkParticles(level, ClientUtil.getClientPlayer(), sparkVec, 0.1F);
        }
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level.isClientSide()) {
            getPassengers().forEach(entity -> {
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).removeEffect(ModStatusEffects.STUN.get());
                }
            });
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, 0.1F, this, owner);
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
            if (target instanceof LivingEntity && target.startRiding(this)) {
                barrier = true;
                ((LivingEntity) target).addEffect(new MobEffectInstance(ModStatusEffects.STUN.get(), barrierMaxTicks));
                setDeltaMovement(new Vec3(0, 0.05D, 0));
            }
            LivingEntity owner = getOwner();
            if (owner != null) {
                INonStandPower.getNonStandPowerOptional(owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, ModHamonActions.CAESAR_BUBBLE_BARRIER.get().getHeldTickEnergyCost(power) / 4F);
                    });
                });
            }
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult blockRayTraceResult) {
        super.onHitBlock(blockRayTraceResult);
        if (blockRayTraceResult.getDirection().getAxis() == Axis.Y) {
            setDeltaMovement(getDeltaMovement().subtract(0, getDeltaMovement().y, 0));
        }
        else {
            setDeltaMovement(getDeltaMovement().subtract(getDeltaMovement().x, 0, getDeltaMovement().z));
        }
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
        if (targetType != TargetType.ENTITY && !isVehicle()) {
            super.breakProjectile(targetType, hitTarget);
        }
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
    public boolean standDamage() {
        return false;
    }
    
    public float getSize(float partialTick) {
        return Math.min((tickCount + partialTick) / (float) ModHamonActions.CAESAR_BUBBLE_BARRIER.get().getHoldDurationToFire(null), 1);
    }

    @Override
    public void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
       if (hasPassenger(entity)) {
           moveFunction.accept(entity, getX(), getY() + (getBbHeight() - entity.getBbHeight()) / 2, getZ());
        }
    }
    
    @Override
    public double getPassengersRidingOffset() {
        return getBbHeight() / 2;
    }
    
    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (barrier) {
            nbt.putBoolean("Barrier", barrier);
            nbt.putInt("BarrierTicks", barrierTicks);
        }
        nbt.putInt("BarrierTicksMax", barrierMaxTicks);
        nbt.putBoolean("Shot", shot);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        this.barrier = nbt.getBoolean("Barrier");
        this.barrierTicks = nbt.getInt("BarrierTicks");
        this.barrierMaxTicks = nbt.getInt("BarrierTicksMax");
        this.shot = nbt.getBoolean("Shot");
    }

    @Override
    public int ticksLifespan() {
        return barrier ? 100 : 100 + barrierMaxTicks;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeVarInt(barrierMaxTicks);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.barrierMaxTicks = additionalData.readVarInt();
    }
}
