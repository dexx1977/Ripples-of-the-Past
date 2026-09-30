package com.github.standobyte.jojo.util.mc.loot;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class AdditionalSingleItemLootModifier extends LootModifier {
    private final ItemStack additionalItem;
    private final boolean replace;

    public AdditionalSingleItemLootModifier(LootItemCondition[] conditions, ItemStack additionalItem, boolean replace) {
        super(conditions);
        this.additionalItem = additionalItem;
        this.replace = replace;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if (replace) {
            generatedLoot.clear();
        }
        generatedLoot.add(additionalItem.copy());
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AdditionalSingleItemLootModifier> {

        @Override
        public AdditionalSingleItemLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            JsonObject itemObject = GsonHelper.getAsJsonObject(object, "additional_item");
            
            Item additionalItem = GsonHelper.getAsItem(itemObject, "name");
            ItemStack itemStack = new ItemStack(additionalItem);
            if (itemObject.has("nbt")) {
                try {
                    CompoundTag nbt = TagParser.parseTag(GsonHelper.convertToString(itemObject.get("nbt"), "nbt"));
                    itemStack.setTag(nbt);
                } catch (CommandSyntaxException commandsyntaxexception) {
                    throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
                }
            }
            boolean replace = GsonHelper.getAsBoolean(object, "replace", false);
            
            return new AdditionalSingleItemLootModifier(conditions, itemStack, replace);
        }

        @Override
        public JsonObject write(AdditionalSingleItemLootModifier instance) {
            JsonObject json = makeConditions(instance.conditions);
            JsonObject itemObject = new JsonObject();
            
            itemObject.addProperty("name", ForgeRegistries.ITEMS.getKey(instance.additionalItem.getItem()).toString());
            if (instance.additionalItem.hasTag()) {
                itemObject.addProperty("nbt", instance.additionalItem.getTag().toString());
            }
            
            json.add("additional_item", itemObject);
            return json;
        }
        
    }
}
