package com.github.standobyte.jojo.entity.itemprojectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.itemtracking.ITrackedArrowEntity;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStackProvider;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public abstract class ItemProjectileEntity extends AbstractArrow implements IEntityAdditionalSpawnData, ITrackedArrowEntity {
    protected boolean leftOwner;

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> type, LivingEntity thrower, Level world) {
        super(type, thrower, world);
        if (thrower instanceof Player && ((Player) thrower).abilities.instabuild) {
            pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
    }

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world);
    }

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> type, Level world) {
        super(type, world);
    }

    public void shootFromRotation(Entity shooter, float velocity, float inaccuracy) {
        shootFromRotation(shooter, shooter.xRot, shooter.yRot, 0, velocity, inaccuracy);
    }

    @Override
    protected void onHit(HitResult rayTraceResult) {
        if (rayTraceResult.getType() == Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) rayTraceResult).getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            setSoundEvent(getActualHitGroundSound(blockState, blockPos));
            super.onHit(rayTraceResult);
            shakeTime = 0;
            setNoGravity(false);
            setSoundEvent(getDefaultHitGroundSoundEvent());
        }
        else {
            super.onHit(rayTraceResult);
        }
    }

    protected SoundEvent getActualHitGroundSound(BlockState blockState, BlockPos blockPos) {
        return blockState.getBlock().getSoundType(blockState, level, blockPos, this).getBreakSound();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (super.canHitEntity(entity)) {
            if (!canHitOwnerProjectile() && entity instanceof Projectile) {
                Entity ownerThis = getOwner();
                Entity ownerThat = ((Projectile) entity).getOwner();
                return ownerThis == null || ownerThat == null || ownerThis.getUUID() != ownerThat.getUUID();
            }
            return true;
        }
        return false;
    }
    
    protected boolean canHitOwnerProjectile() {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        Entity target = entityRayTraceResult.getEntity();
        Entity thrower = getOwner();
        if (thrower instanceof LivingEntity) {
            ((LivingEntity) thrower).setLastHurtMob(target);
        }
        boolean dodge = target.getType() == EntityType.ENDERMAN;
        int prevTargetFireTimer = target.getRemainingFireTicks();
        if (isOnFire() && !dodge) {
            target.setSecondsOnFire(5);
        }
        if (hurtTarget(target, thrower)) {
            if (dodge) {
                return;
            }
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity)target;
                if (!level.isClientSide() && thrower instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingTarget, thrower);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) thrower, livingTarget);
                }
                doPostHurtEffects(livingTarget);
            }
            playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            if (isRemovedOnEntityHit()) {
                discard();
            }
            else {
                changeMovementAfterHit();
            }
        } else {
            target.setRemainingFireTicks(prevTargetFireTimer);
            setDeltaMovement(getDeltaMovement().scale(-0.1D));
            yRot += 180.0F;
            yRotO += 180.0F;
            if (!level.isClientSide() && getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (isRemovedOnEntityHit()) {
                    if (pickup == AbstractArrow.Pickup.ALLOWED) {
                        spawnAtLocation(getPickupItem(), 0.1F);
                    }
                    discard();
                }
                else {
                    changeMovementAfterHit();
                }
            }
        }
    }

    protected boolean hurtTarget(Entity target, Entity thrower) {
        float dmgAmount = getActualDamage();
        DamageSource damagesource = DamageSource.arrow(this, thrower == null ? this : thrower);
        return target.hurt(damagesource, (float) dmgAmount);
    }

    @Override
    public void playerTouch(Player player) {
        if (!level.isClientSide()) {
            Entity shooter = getOwner();
            if (inGround || shooter == null || shooter.getUUID() == player.getUUID()) {
                boolean canPickUp = (pickup == AbstractArrow.Pickup.ALLOWED 
                        || pickup == AbstractArrow.Pickup.CREATIVE_ONLY && player.abilities.instabuild)
                        && (inGround || isNoPhysics() || throwerCanCatch());
                if (canPickUp && pickup == AbstractArrow.Pickup.ALLOWED && !player.inventory.add(getPickupItem())) {
                    canPickUp = false;
                }
                if (canPickUp) {
                    pickUp(player);
                    return;
                }
            }
            super.playerTouch(player);
        }
    }
    
    protected void pickUp(Player player) {
        player.take(this, 1);
        discard();
    }

    public final boolean isInGround() {
        return inGround;
    }

    protected boolean throwerCanCatch() {
        if (!leftOwner) {
            leftOwner = CommonReflection.getProjectileLeftOwner(this);
        }
        return leftOwner;
    }

    protected float getActualDamage() {
        float dmgAmount = (float) (getDeltaMovement().length() * getBaseDamage());
        if (isCritArrow()) {
            dmgAmount += random.nextInt((int) (dmgAmount / 2) + 2);
        }
        return dmgAmount;
    }

    protected boolean isRemovedOnEntityHit() {
        return true;
    }

    protected void changeMovementAfterHit() {
        setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
    }

    @Override
    public boolean isPickable() {
        return true;
    }
    
    
    @Nullable private Tag itemTrackerNBT;
    
    @Override
    public void saveItemTrackerNBT(Tag nbt) {
        this.itemTrackerNBT = nbt;
    }
    
    protected ItemStack withPickupItemTracking(ItemStack item) {
        if (this.itemTrackerNBT != null && !item.isEmpty()) {
            if (!item.isEmpty()) {
                item.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(tracker -> tracker.fromNBT(itemTrackerNBT));
            }
        }
        return item;
    }
    
    

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        tickCount = compound.getInt("Age");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Age", tickCount);
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(getOwner() != null ? getOwner().getId() : -1);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        int ownerId = additionalData.readInt();
        if (ownerId > -1) {
            setOwner(level.getEntity(ownerId));
        }
    }
}