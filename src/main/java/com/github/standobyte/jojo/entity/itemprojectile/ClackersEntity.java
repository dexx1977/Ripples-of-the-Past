package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class ClackersEntity extends ItemProjectileEntity {
    private float hamonDmg;
    private float hamonEnergySpent;
    private boolean boomerangHit = false;
    
    public ClackersEntity(EntityType<? extends ItemProjectileEntity> type, Level world) {
        super(type, world);
    }

    public ClackersEntity(Level world, LivingEntity thrower) {
        super(ModEntityTypes.CLACKERS.get(), thrower, world);
    }
    
    public void setHamonEnergySpent(float energy) {
        this.hamonEnergySpent = energy;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            if (boomerangHit && inGroundTime > 10) {
                boomerangHit = false;
            }
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        boolean projectileAttack = super.hurtTarget(target, thrower);
        boolean hamonAttack = DamageUtil.dealHamonDamage(target, hamonDmg, this, thrower);
        boolean hitTarget = projectileAttack || hamonAttack;
        if (!level.isClientSide() && hitTarget) {
            Entity owner = getOwner();
            if (owner instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, hamonEnergySpent);
                    });
                });
            }
            boomerangHit = true;
        }
        return hitTarget;
    }
    
    public void setHamonDamage(float hamonDmg) {
        this.hamonDmg = hamonDmg;
    }

    @Override
    protected ItemStack getPickupItem() {
        return withPickupItemTracking(new ItemStack(ModItems.CLACKERS.get()));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !entity.is(getOwner()) && (entity.getType() != ModEntityTypes.CLACKERS.get() || super.canHitEntity(entity));
    }
    
    @Override
    protected boolean canHitOwnerProjectile() {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        Entity entity = entityRayTraceResult.getEntity();
        if (entity.getType() == ModEntityTypes.CLACKERS.get()) {
            ClackersEntity otherClackers = (ClackersEntity) entity;
            this.hamonDmg += otherClackers.hamonDmg;
            otherClackers.hamonDmg = 0;
            changeMovementAfterHit();
        }
        else {
            super.onHitEntity(entityRayTraceResult);
        }
    }

    @Override
    protected boolean isRemovedOnEntityHit() {
        return false;
    }

    @Override
    protected void changeMovementAfterHit() {
        if (!level.isClientSide()) {
            Entity owner = getOwner();
            if (owner != null) {
                HitResult rayTrace = JojoModUtil.rayTrace(this, distanceTo(owner), 
                        e -> !e.is(owner) && canHitEntity(e), 2.5D);
                if (rayTrace.getType() == HitResult.Type.ENTITY) {
                    Entity target = ((EntityHitResult) rayTrace).getEntity();
                    setDeltaMovement(target.getEyePosition(1.0F).subtract(position()).normalize().scale(getDeltaMovement().length()));
                }
                else {
                    setDeltaMovement(getDeltaMovement().reverse());
                }
                if (boomerangHit) {
                    setDeltaMovement(owner.getEyePosition(1.0F).subtract(position()).normalize().scale(getDeltaMovement().length() / 2));
                }
                else {
                    super.changeMovementAfterHit();
                }
            }
            else {
                super.changeMovementAfterHit();
            }
        }
    }

    @Override
    protected void pickUp(Player player) {
        super.pickUp(player);
        if (boomerangHit) {
            JojoModUtil.sayVoiceLine(player, ModSounds.JOSEPH_CLACKER_BOOMERANG.get());
        }
    }

    @Override
    public void tickDespawn() {
        if (this.pickup != AbstractArrow.Pickup.ALLOWED) {
            super.tickDespawn();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        hamonDmg = compound.getFloat("HamonDamage");
        hamonEnergySpent = compound.getFloat("HamonSpent");
        boomerangHit = compound.getBoolean("BoomerangHit");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("HamonDamage", hamonDmg);
        compound.putFloat("HamonSpent", hamonEnergySpent);
        if (boomerangHit) {
            compound.putBoolean("BoomerangHit", boomerangHit);
        }
    }

}
