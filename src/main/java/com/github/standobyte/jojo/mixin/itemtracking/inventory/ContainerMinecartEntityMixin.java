package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

@Mixin(AbstractMinecartContainer.class)
public abstract class ContainerMinecartEntityMixin extends AbstractMinecart {
    
    protected ContainerMinecartEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }
    
    @Shadow
    private NonNullList<ItemStack> itemStacks;
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        if (!level.isClientSide()) {
            TrackerItemStack.getItemTrackerInInventory(item, itemStacks.stream(), false)
            .ifPresent(tracker -> {
                tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> itemStacks.stream()
                        .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
            });
        }
    }
}
