package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.crafting.CassetteCopyRecipe;
import com.github.standobyte.jojo.crafting.CassetteRecordingRecipe;
import com.github.standobyte.jojo.crafting.StandUserRecipe;
import com.github.standobyte.jojo.crafting.StandUserShapedRecipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, JojoMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<StandUserRecipe<ShapedRecipe>>> STAND_USER_SHAPED_RECIPE = SERIALIZERS.register("crafting_shaped_stand", 
            () -> new StandUserRecipe.Serializer<>(RecipeSerializer.SHAPED_RECIPE, StandUserShapedRecipe::new));

    public static final RegistryObject<RecipeSerializer<CassetteRecordingRecipe>> CASSETTE_RECORD = SERIALIZERS.register("cassette_record", 
            () -> new SimpleCraftingRecipeSerializer<>(CassetteRecordingRecipe::new));

    public static final RegistryObject<RecipeSerializer<CassetteCopyRecipe>> CASSETTE_COPY = SERIALIZERS.register("cassette_copy", 
            () -> new SimpleCraftingRecipeSerializer<>(CassetteCopyRecipe::new));
}
