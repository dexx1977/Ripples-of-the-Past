package com.github.standobyte.jojo.util.mc.loot;

import java.util.Optional;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * Adds one item to the generated loot (or replaces it with that item).
 *
 * <p>1.20.1 serialises loot modifiers through their codec instead of a
 * GlobalLootModifierSerializer, so the codec reads the same json shape the data
 * files already use: {@code additional_item: {name, nbt}} and {@code replace}.</p>
 */
public class AdditionalSingleItemLootModifier extends LootModifier {
    private static final Codec<ItemStack> ADDITIONAL_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem())),
            Codec.STRING.optionalFieldOf("nbt").forGetter(stack -> Optional.ofNullable(stack.getTag()).map(CompoundTag::toString))
    ).apply(instance, (itemId, nbt) -> {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        nbt.ifPresent(tag -> {
            try {
                stack.setTag(TagParser.parseTag(tag));
            }
            catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid nbt tag: " + e.getMessage());
            }
        });
        return stack;
    }));

    public static final Codec<AdditionalSingleItemLootModifier> CODEC = RecordCodecBuilder.create(instance -> 
            codecStart(instance)
                    .and(ADDITIONAL_ITEM_CODEC.fieldOf("additional_item").forGetter(modifier -> modifier.additionalItem))
                    .and(Codec.BOOL.optionalFieldOf("replace", false).forGetter(modifier -> modifier.replace))
                    .apply(instance, AdditionalSingleItemLootModifier::new));

    private final ItemStack additionalItem;
    private final boolean replace;

    public AdditionalSingleItemLootModifier(LootItemCondition[] conditions, ItemStack additionalItem, boolean replace) {
        super(conditions);
        this.additionalItem = additionalItem;
        this.replace = replace;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (replace) {
            generatedLoot.clear();
        }
        generatedLoot.add(additionalItem.copy());
        return generatedLoot;
    }
}
