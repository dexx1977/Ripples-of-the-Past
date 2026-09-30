package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

@Mixin(SimpleContainer.class)
public abstract class InventoryMixin implements Container {
    @Shadow
    @Final private NonNullList<ItemStack> items;
    @Shadow
    private List<ContainerListener> listeners;
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        if (listeners != null) {
            for (ContainerListener shouldBeHorse : listeners) {
                if (shouldBeHorse instanceof AbstractHorse) {
                    AbstractHorse horse = (AbstractHorse) shouldBeHorse;
                    if (!horse.level.isClientSide()) {
                        TrackerItemStack.getItemTrackerInInventory(item, items.stream(), false)
                        .ifPresent(tracker -> {
                            tracker.setAtEntity(horse.getId(), horse.level, KnownItemState.ENTITY_HAS_ITEM);
                            tracker.setItemStillThereCheck(trackerId -> items.stream()
                                    .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                        });
                    }
                    break;
                }
            }
        }
    }
}
