package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.github.standobyte.jojo.itemtracking.ITrackedArrowEntity;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ProjectileWeaponItem {

    public CrossbowItemMixin(Properties properties) {
        super(properties);
    }
    
    @Inject(method = "shootProjectile", at = @At(value = "INVOKE", 
                target = "Lnet/minecraft/entity/projectile/ProjectileEntity;shoot(DDDFF)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void jojoModifyCrossbowArrow(Level pLevel, LivingEntity pShooter, InteractionHand arg2, 
            ItemStack pCrossbowStack, ItemStack pAmmoStack, float pSoundPitch, boolean pIsCreativeMode, 
            float pVelocity, float pInaccuracy, float pProjectileAngle, CallbackInfo ci, 
            boolean firework, Projectile projectileEntity) {
        if (pProjectileAngle == 0) { // in case of multishot
            TrackerItemStack.getItemTracker(pAmmoStack).ifPresent(tracker -> {
                if (tracker.isTracked()) {
                    tracker.setAtEntity(projectileEntity.getId(), pLevel, KnownItemState.ENTITY_IS_ITEM);
                    tracker.setItemStillThereCheck(null);
                    if (projectileEntity instanceof ITrackedArrowEntity) {
                        ((ITrackedArrowEntity) projectileEntity).saveItemTrackerNBT(tracker.toNBT());
                    }
                }
            });
        }
    }
}
