package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ThrowableItemProjectile.class)
public abstract class ProjectileItemEntityMixin extends ThrowableProjectile {

    protected ProjectileItemEntityMixin(EntityType<? extends ThrowableProjectile> p_i48540_1_, Level p_i48540_2_) {
        super(p_i48540_1_, p_i48540_2_);
    }

    @Inject(method = "setItem", at = @At("HEAD"))
    public void onSetItem(ItemStack item, CallbackInfo ci) {
        if (!level.isClientSide()) {
            TrackerItemStack.getItemTracker(item).ifPresent(tracker -> {
                tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_IS_ITEM);
                tracker.setItemStillThereCheck(null);
            });
        }
    }
}
