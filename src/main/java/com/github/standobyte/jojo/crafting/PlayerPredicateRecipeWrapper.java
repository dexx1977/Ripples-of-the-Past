package com.github.standobyte.jojo.crafting;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class PlayerPredicateRecipeWrapper<R extends CraftingRecipe> implements CraftingRecipe {
    protected final R recipe;
    
    protected PlayerPredicateRecipeWrapper(R recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        return recipe.matches(inventory, world) && playerMatches(getPlayer(inventory));
    }
    
    protected abstract boolean playerMatches(Player player);

    @Override
    public ItemStack assemble(CraftingContainer inventory, net.minecraft.core.RegistryAccess registryAccess) {
        return recipe.assemble(inventory, registryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return recipe.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return recipe.getResultItem(registryAccess);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipe.getIngredients();
    }

    @Override
    public ResourceLocation getId() {
        return recipe.getId();
    }
    
    @Nullable
    private static Player getPlayer(CraftingContainer inventory) {
        Player player = null;
        AbstractContainerMenu menu = CommonReflection.getCraftingInventoryMenu(inventory);
        if (menu instanceof InventoryMenu) {
            player = CommonReflection.getPlayer((InventoryMenu) menu);
        }
        else if (menu instanceof CraftingMenu) {
            player = CommonReflection.getPlayer((CraftingMenu) menu);
        }
        return player;
    }

}
