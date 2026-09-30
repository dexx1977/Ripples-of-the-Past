package com.github.standobyte.jojo.mixin.itemtracking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.server.level.ServerLevel;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
    @Shadow @Final private MerchantContainer slots;
    @Shadow @Final private Merchant merchant;

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = 
            "Lnet/minecraft/item/MerchantOffer;take(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 0))
    public void onTradePerform(Player player, ItemStack item, CallbackInfoReturnable<ItemStack> ci) {
        if (!player.level.isClientSide()) {
            ItemStack playerOfferA = slots.getItem(0);
            ItemStack playerOfferB = slots.getItem(1);
            MerchantOffer offer = slots.getActiveOffer();
            if ((offer.satisfiedBy(playerOfferA, playerOfferB) || offer.satisfiedBy(playerOfferB, playerOfferA)) && merchant instanceof Entity) {
                Entity merchantEntity = (Entity) merchant;
                ServerLevel world = (ServerLevel) player.level;
                TrackerItemStack.getItemTracker(playerOfferA).ifPresent(tracker -> trackTradeCost(tracker, merchantEntity, world));
                TrackerItemStack.getItemTracker(playerOfferB).ifPresent(tracker -> trackTradeCost(tracker, merchantEntity, world));
            }
        }
    }
    
    private static void trackTradeCost(TrackerItemStack tracker, Entity merchantEntity, ServerLevel world) {
        ItemStack itemCopy = tracker.getItem().copy();
        tracker.moveToItem(itemCopy, world);
        TrackerItemStack.getItemTracker(itemCopy).ifPresent(newTracker -> {
            newTracker.setAtEntity(merchantEntity.getId(), merchantEntity.level, KnownItemState.ENTITY_HAS_ITEM);
            newTracker.setItemStillThereCheck(null);
        });
    }
}
