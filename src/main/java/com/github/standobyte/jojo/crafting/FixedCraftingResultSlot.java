package com.github.standobyte.jojo.crafting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.NonNullList;
import net.minecraftforge.common.ForgeHooks;

public class FixedCraftingResultSlot<C extends CraftingContainer, T extends Recipe<C>> extends ResultSlot {
    protected final RecipeType<T> recipeType;
    protected final C craftSlots;
    protected final Player player;

    public FixedCraftingResultSlot(Player pPlayer, C pCraftSlots, 
            Container pContainer, int pSlot, int pXPosition, int pYPosition, RecipeType<T> recipeType) {
        super(pPlayer, pCraftSlots, pContainer, pSlot, pXPosition, pYPosition);
        this.recipeType = recipeType;
        this.craftSlots = pCraftSlots;
        this.player = pPlayer;
    }
    
    @Override
    public ItemStack onTake(Player pPlayer, ItemStack pStack) {
        checkTakeAchievements(pStack);
        ForgeHooks.setCraftingPlayer(pPlayer);
        NonNullList<ItemStack> nonnulllist = pPlayer.level.getRecipeManager().getRemainingItemsFor(recipeType, craftSlots, pPlayer.level);
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = craftSlots.getItem(i);
            ItemStack itemstack1 = nonnulllist.get(i);
            if (!itemstack.isEmpty()) {
                craftSlots.removeItem(i, 1);
                itemstack = craftSlots.getItem(i);
            }

            if (!itemstack1.isEmpty()) {
                if (itemstack.isEmpty()) {
                    craftSlots.setItem(i, itemstack1);
                } else if (ItemStack.isSame(itemstack, itemstack1) && ItemStack.tagMatches(itemstack, itemstack1)) {
                    itemstack1.grow(itemstack.getCount());
                    craftSlots.setItem(i, itemstack1);
                } else if (!player.inventory.add(itemstack1)) {
                    player.drop(itemstack1, false);
                }
            }
        }

        return pStack;
    }
    
}
