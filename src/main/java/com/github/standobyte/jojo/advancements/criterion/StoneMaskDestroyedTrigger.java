package com.github.standobyte.jojo.advancements.criterion;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraft.advancements.critereon.ContextAwarePredicate;

public class StoneMaskDestroyedTrigger extends SimpleCriterionTrigger<StoneMaskDestroyedTrigger.Instance> {
    private final ResourceLocation id;

    public StoneMaskDestroyedTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player, Block stoneMaskBlock, ItemStack itemUsed, ItemStack stoneMaskItem) {
        trigger(player, (criterion) -> {
            return criterion.matches(stoneMaskBlock, itemUsed, stoneMaskItem);
        });
    }

    public StoneMaskDestroyedTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate, 
            DeserializationContext conditionArrayParser) {
        Block block = deserializeBlock(json);
        ItemPredicate itemUsed = ItemPredicate.fromJson(json.get("item_used"));
        ItemPredicate stoneMaskItem = ItemPredicate.fromJson(json.get("stone_mask_item"));
        return new StoneMaskDestroyedTrigger.Instance(id, playerPredicate, block, itemUsed, stoneMaskItem);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject json) {
        if (json.has("block")) {
            ResourceLocation resLoc = new ResourceLocation(GsonHelper.getAsString(json, "block"));
            return Optional.ofNullable(((ForgeRegistry<Block>) ForgeRegistries.BLOCKS)
                    .getRaw(resLoc)).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown block type '" + resLoc + "'");
                    });
        }
        else {
            return null;
        }
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Block block;
        private final ItemPredicate itemUsed;
        private final ItemPredicate stoneMaskItem;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, @Nullable Block block, 
                ItemPredicate itemUsed, ItemPredicate stoneMaskItem) {
            super(criterion, player);
            this.block = block;
            this.itemUsed = itemUsed;
            this.stoneMaskItem = stoneMaskItem;
        }

        public boolean matches(Block block, ItemStack itemUsed, ItemStack stoneMaskItem) {
            return (this.block == null || this.block == block)
                    && this.itemUsed.matches(itemUsed) && this.stoneMaskItem.matches(stoneMaskItem);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            if (this.block != null) {
                jsonobject.addProperty("block", ForgeRegistries.BLOCKS.getKey(block).toString());
            }
            jsonobject.add("item_used", itemUsed.serializeToJson());
            jsonobject.add("stone_mask_item", stoneMaskItem.serializeToJson());
            return jsonobject;
        }
    }

}
