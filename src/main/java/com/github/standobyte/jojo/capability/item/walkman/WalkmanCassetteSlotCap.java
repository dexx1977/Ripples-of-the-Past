package com.github.standobyte.jojo.capability.item.walkman;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.container.WalkmanItemContainer;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.items.ItemStackHandler;

public class WalkmanCassetteSlotCap extends ItemStackHandler implements MenuProvider {
    private final ItemStack walkmanItem;

    public WalkmanCassetteSlotCap(ItemStack walkmanItem) {
        super(1);
        this.walkmanItem = walkmanItem;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() == ModItems.CASSETTE_RECORDED.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new WalkmanItemContainer(id, inventory, this, walkmanItem);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }
}
