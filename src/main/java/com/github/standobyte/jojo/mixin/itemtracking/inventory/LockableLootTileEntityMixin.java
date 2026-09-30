package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class LockableLootTileEntityMixin extends BaseContainerBlockEntity {
    
    protected LockableLootTileEntityMixin(BlockEntityType<?> type) {
        super(type);
    }
    
    @Shadow
    protected abstract NonNullList<ItemStack> getItems();
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        Level world = getLevel();
        if (world != null && !world.isClientSide()) {
            TrackerItemStack.getItemTrackerInInventory(item, getItems().stream(), false)
            .ifPresent(tracker -> {
                tracker.setAtBlockPos(this.getBlockPos(), level, KnownItemState.BLOCK_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> getItems().stream()
                        .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
            });
        }
    }
}
