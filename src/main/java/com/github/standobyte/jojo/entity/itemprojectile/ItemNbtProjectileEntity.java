package com.github.standobyte.jojo.entity.itemprojectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public abstract class ItemNbtProjectileEntity extends ItemProjectileEntity {
    protected ItemStack thrownStack = ItemStack.EMPTY;

    public ItemNbtProjectileEntity(EntityType<? extends ItemNbtProjectileEntity> type, Level world, LivingEntity thrower, ItemStack thrownStack) {
        super(type, thrower, world);
        setPickupItem(thrownStack);
    }

    public ItemNbtProjectileEntity(EntityType<? extends ItemNbtProjectileEntity> type, Level world, double x, double y, double z, ItemStack thrownStack) {
        super(type, x, y, z, world);
        setPickupItem(thrownStack);
    }

    protected ItemNbtProjectileEntity(EntityType<? extends ItemNbtProjectileEntity> type, Level world) {
        super(type, world);
    }
    
    protected void setPickupItem(ItemStack item) {
        this.thrownStack = item.copy();
    }

    @Override
    protected ItemStack getPickupItem() {
        return thrownStack.copy();
    }

    @Override
    protected boolean isRemovedOnEntityHit() {
        return false;
    }

    @Override
    protected void onHit(HitResult rayTraceResult) {
        Entity shooter = getOwner();
        if (shooter instanceof LivingEntity) {
            thrownStack.hurtAndBreak(1, (LivingEntity) shooter, entity -> remove());
        }
        super.onHit(rayTraceResult);
    }

    @Override
    public void tickDespawn() {
        if (this.pickup != AbstractArrow.PickupStatus.ALLOWED) {
            super.tickDespawn();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Item", 10)) {
            thrownStack = ItemStack.of(compound.getCompound("Item"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Item", thrownStack.save(new CompoundTag()));
    }
}
