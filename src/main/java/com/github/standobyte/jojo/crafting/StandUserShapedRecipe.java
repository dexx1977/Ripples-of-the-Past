package com.github.standobyte.jojo.crafting;

import com.github.standobyte.jojo.init.ModRecipeSerializers;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class StandUserShapedRecipe extends StandUserRecipe<ShapedRecipe> implements IShapedRecipe<CraftingContainer> {

    public StandUserShapedRecipe(ShapedRecipe recipe, NonNullList<ResourceLocation> stands) {
        super(recipe, stands);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STAND_USER_SHAPED_RECIPE.get();
    }

    @Override
    public int getRecipeWidth() {
        return recipe.getRecipeWidth();
    }

    @Override
    public int getRecipeHeight() {
        return recipe.getRecipeHeight();
    }

}
