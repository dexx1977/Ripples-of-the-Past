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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem {

    public BowItemMixin(Properties properties) {
        super(properties);
    }
    
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", 
                target = "Lnet/minecraft/entity/projectile/AbstractArrowEntity;shootFromRotation(Lnet/minecraft/entity/Entity;FFFFF)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    public void jojoModifyBowArrow(ItemStack bowItem, Level world, LivingEntity entity, int holdTimeLeft, CallbackInfo ci, 
            Player player, boolean hasAmmo, ItemStack projectileItem, int charge, float arrowPower, boolean infinity, 
            ArrowItem arrowitem, AbstractArrow arrowEntity) {
        TrackerItemStack.getItemTracker(projectileItem).ifPresent(tracker -> {
            if (tracker.isTracked()) {
                tracker.setAtEntity(arrowEntity.getId(), world, KnownItemState.ENTITY_IS_ITEM);
                tracker.setItemStillThereCheck(null);
                if (arrowEntity instanceof ITrackedArrowEntity) {
                    ((ITrackedArrowEntity) arrowEntity).saveItemTrackerNBT(tracker.toNBT());
                }
                if (infinity) {
                    tracker.moveToItem(projectileItem.copy(), (ServerLevel) world);
                }
            }
        });
    }
}
