package com.github.standobyte.jojo.container;

import com.github.standobyte.jojo.capability.item.walkman.WalkmanCassetteSlotCap;
import com.github.standobyte.jojo.init.ModContainers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.items.SlotItemHandler;

public class WalkmanItemContainer extends AbstractContainerMenu {
    private final ItemStack walkmanItem;
    private final WalkmanCassetteSlotCap cassetteSlot;

    public WalkmanItemContainer(int id, Inventory inventory, FriendlyByteBuf dataFromServer) {
        this(id, inventory, new WalkmanCassetteSlotCap(ItemStack.EMPTY), dataFromServer.readItem());
    }
    
    public static void writeAdditionalData(FriendlyByteBuf buffer, ItemStack walkmanItem) {
        buffer.writeItem(walkmanItem);
    }

    public WalkmanItemContainer(int id, Inventory inventory, WalkmanCassetteSlotCap itemCap, ItemStack walkmanItem) {
        super(ModContainers.WALKMAN.get(), id);
        this.walkmanItem = walkmanItem;
        this.cassetteSlot = itemCap;
        addSlot(new SlotItemHandler(cassetteSlot, 0, 10, 111));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9 + 9,  18 + j * 18,    i * 18 + 142));
            }
        }
        for (int k = 0; k < 9; ++k) {
            addSlot(new Slot(inventory, k,                  18 + k * 18,    142 + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return (player.getMainHandItem() == walkmanItem || player.getOffhandItem() == walkmanItem) && !walkmanItem.isEmpty();
    }
    
    public ItemStack getWalkmanItem() {
        return walkmanItem;
    }
    
    public ItemStack getCassetteItem() {
        return cassetteSlot.getStackInSlot(0);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            } else if (index >= 1 && index < 28) {
                if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 28 && index < 37) {
                if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 1, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
