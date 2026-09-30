package com.github.standobyte.jojo.crafting;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class StandUserRecipe<R extends CraftingRecipe> extends PlayerPredicateRecipeWrapper<R> {
    private final NonNullList<ResourceLocation> standIds;
    private final List<StandType<?>> standTypes;
    private final List<ResourceLocation> missingIds;

    public StandUserRecipe(R recipe, NonNullList<ResourceLocation> stands) {
        super(recipe);
        this.standIds = stands;
        
        IForgeRegistry<StandType<?>> standsRegistry = JojoCustomRegistries.STANDS.getRegistry();
        this.standTypes = standIds.stream()
                .filter(standsRegistry::containsKey)
                .map(standsRegistry::getValue)
                .collect(Collectors.toList());
        
        this.missingIds = standIds.stream()
                .filter(id -> !standsRegistry.containsKey(id))
                .collect(Collectors.toList());
    }
    
    @Override
    protected boolean playerMatches(Player player) {
        IStandPower standPower = IStandPower.getPlayerStandPower(player);
        return standPower.hasPower() && (standIds.isEmpty() || standIds.contains(standPower.getType().getRegistryName()));
    }
    
    public List<StandType<?>> getStandTypesView() {
        return standTypes;
    }
    
    public Collection<ResourceLocation> getMissingIdsView() {
        return missingIds;
    }

    public static class Serializer<R extends CraftingRecipe> implements RecipeSerializer<StandUserRecipe<R>> {
        private final RecipeSerializer<R> wrappedRecipeSerializer;
        private final Factory<R> factory;
        
        public Serializer(RecipeSerializer<R> wrappedRecipeSerializer, Factory<R> factory) {
            this.wrappedRecipeSerializer = wrappedRecipeSerializer;
            this.factory = factory;
        }

        @Override
        public StandUserRecipe<R> fromJson(ResourceLocation id, JsonObject json) {
            R recipe = wrappedRecipeSerializer.fromJson(id, json);
            NonNullList<ResourceLocation> standsList = NonNullList.create();
            JsonArray standsJson = json.getAsJsonArray("stand");
            standsJson.forEach(element -> {
                standsList.add(new ResourceLocation(element.getAsJsonObject().get("name").getAsString()));
            });
            return factory.create(recipe, standsList);
        }

        @Override
        public StandUserRecipe<R> fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            R recipe = wrappedRecipeSerializer.fromNetwork(id, buf);
            NonNullList<ResourceLocation> stands = NonNullList.create();
            int standsCount = buf.readVarInt();
            for (int i = 0; i < standsCount; i++) {
                stands.add(buf.readResourceLocation());
            }
            return factory.create(recipe, stands);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, StandUserRecipe<R> recipe) {
            wrappedRecipeSerializer.toNetwork(buf, recipe.recipe);
            
            buf.writeVarInt(recipe.standIds.size());
            for (ResourceLocation resLoc : recipe.standIds) {
                buf.writeResourceLocation(resLoc);
            }
        }
        
    }
    
    public static interface Factory<R extends CraftingRecipe> {
        public StandUserRecipe<R> create(R recipe, NonNullList<ResourceLocation> stands);
    }

}
