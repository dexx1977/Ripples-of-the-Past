package com.github.standobyte.jojo.mixin.itemtracking;

import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;

@Mixin(Slot.class)
public abstract class ContainerSlotMixin {
    @Shadow
    @Final public Container container;
    
    @Inject(method = "set", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(ItemStack pStack, CallbackInfo ci) {
        if (container instanceof Entity) {
            Entity entity = (Entity) container;
            if (!entity.level.isClientSide()) {
                TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                    tracker.setAtEntity(entity.getId(), entity.level, KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> 
                            IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
                            .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                });
            }
        }
        else if (container instanceof BlockEntity) {
            BlockEntity tileEntity = (BlockEntity) container;
            Level world = tileEntity.getLevel();
            if (world != null && !world.isClientSide()) {
                TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                    tracker.setAtBlockPos(tileEntity.getBlockPos(), world, KnownItemState.BLOCK_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> 
                            IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
                            .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                });
            }
        }
        else if (container instanceof Inventory) {
            Player player = ((Inventory) container).player;
            if (!player.level.isClientSide()) {
                TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                    tracker.setAtEntity(player.getId(), player.level, KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> 
                            IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
                            .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                });
            }
        }
    }
}
