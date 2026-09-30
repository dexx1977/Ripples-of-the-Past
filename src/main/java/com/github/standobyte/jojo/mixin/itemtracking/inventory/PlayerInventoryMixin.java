package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin implements Container {
    @Shadow
    @Final public Player player;
    @Shadow
    @Final private List<NonNullList<ItemStack>> compartments;
    
    @Inject(method = "add(ILnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"))
    public void jojoOnItemAddedToInv(int slot, ItemStack item, CallbackInfoReturnable<Boolean> ci) {
        if (!player.level.isClientSide() && Boolean.TRUE.equals(ci.getReturnValue())) {
            TrackerItemStack.getItemTrackerInInventory(item, compartments.stream().flatMap(Collection::stream), true)
            .ifPresent(tracker -> {
                tracker.setAtEntity(player.getId(), player.level, KnownItemState.ENTITY_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> compartments.stream().flatMap(Collection::stream)
                        .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
            });
        }
    }
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        if (!player.level.isClientSide()) {
            TrackerItemStack.getItemTracker(item)
            .ifPresent(tracker -> {
                tracker.setAtEntity(player.getId(), player.level, KnownItemState.ENTITY_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> compartments.stream().flatMap(Collection::stream)
                        .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
            });
        }
    }
}
