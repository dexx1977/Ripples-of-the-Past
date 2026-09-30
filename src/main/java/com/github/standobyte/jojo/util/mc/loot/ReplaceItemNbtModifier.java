package com.github.standobyte.jojo.util.mc.loot;

import java.util.List;

import com.github.standobyte.jojo.util.mc.MCUtil;
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

public class ReplaceItemNbtModifier extends LootModifier {
    private final Item item;
    private final CompoundTag tagToReplace;
    private final CompoundTag replacingTag;

    public ReplaceItemNbtModifier(LootItemCondition[] conditions, Item item, CompoundTag tagToReplace, CompoundTag replacingTag) {
        super(conditions);
        this.item = item;
        this.tagToReplace = tagToReplace;
        this.replacingTag = replacingTag;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.forEach(stack -> {
            if (stack.getItem() == item) {
                MCUtil.replaceNbtValues(stack.getOrCreateTag(), tagToReplace, replacingTag);
            }
        });
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<ReplaceItemNbtModifier> {

        @Override
        public ReplaceItemNbtModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            JsonObject entryReplacement = GsonHelper.getAsJsonObject(object, "replace_nbt");
            Item item = GsonHelper.getAsItem(entryReplacement, "item");
            try {
                CompoundTag tagToReplace = TagParser.parseTag(GsonHelper.getAsString(entryReplacement, "to_replace"));
                CompoundTag replacingTag = TagParser.parseTag(GsonHelper.getAsString(entryReplacement, "replace_with"));
                return new ReplaceItemNbtModifier(conditions, item, tagToReplace, replacingTag);
            } 
            catch (CommandSyntaxException commandSyntaxException) {
                throw new JsonSyntaxException(commandSyntaxException.getMessage());
            }
        }

        @Override
        public JsonObject write(ReplaceItemNbtModifier instance) {
            JsonObject json = makeConditions(instance.conditions);
            JsonObject entryReplacement = new JsonObject();
            entryReplacement.addProperty("item", ForgeRegistries.ITEMS.getKey(instance.item).toString());
            entryReplacement.addProperty("to_replace", instance.tagToReplace.toString());
            entryReplacement.addProperty("replace_with", instance.replacingTag.toString());
            json.add("replace_nbt", entryReplacement);
            return json;
        }
        
    }

}
