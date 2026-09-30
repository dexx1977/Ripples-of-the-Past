package com.github.standobyte.jojo.util.mc.loot;

import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * Replaces nbt values on the generated items of one type. The codec keeps the json
 * shape the data files use: {@code replace_nbt: {item, to_replace, replace_with}}.
 */
public class ReplaceItemNbtModifier extends LootModifier {
    private static final Codec<CompoundTag> TAG_CODEC = Codec.STRING.comapFlatMap(tag -> {
        try {
            return com.mojang.serialization.DataResult.success(TagParser.parseTag(tag));
        }
        catch (CommandSyntaxException e) {
            return com.mojang.serialization.DataResult.error(() -> e.getMessage());
        }
    }, CompoundTag::toString);

    
    /** The data files keep the 1.16.5 nesting: replace_nbt: {item, to_replace, replace_with}. */
    private static final Codec<Replacement> REPLACEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(Replacement::itemId),
            TAG_CODEC.fieldOf("to_replace").forGetter(Replacement::tagToReplace),
            TAG_CODEC.fieldOf("replace_with").forGetter(Replacement::replacingTag)
    ).apply(instance, Replacement::new));
    
    private record Replacement(ResourceLocation itemId, CompoundTag tagToReplace, CompoundTag replacingTag) {}

    public static final Codec<ReplaceItemNbtModifier> CODEC = RecordCodecBuilder.create(instance -> 
            codecStart(instance)
                    .and(REPLACEMENT_CODEC.fieldOf("replace_nbt").forGetter(modifier -> new Replacement(
                            BuiltInRegistries.ITEM.getKey(modifier.item), modifier.tagToReplace, modifier.replacingTag)))
                    .apply(instance, ReplaceItemNbtModifier::new));

    private final Item item;
    private final CompoundTag tagToReplace;
    private final CompoundTag replacingTag;

    public ReplaceItemNbtModifier(LootItemCondition[] conditions, Replacement replacement) {
        this(conditions, replacement.itemId(), replacement.tagToReplace(), replacement.replacingTag());
    }

    public ReplaceItemNbtModifier(LootItemCondition[] conditions, ResourceLocation itemId, CompoundTag tagToReplace, CompoundTag replacingTag) {
        this(conditions, BuiltInRegistries.ITEM.get(itemId), tagToReplace, replacingTag);
    }

    public ReplaceItemNbtModifier(LootItemCondition[] conditions, Item item, CompoundTag tagToReplace, CompoundTag replacingTag) {
        super(conditions);
        this.item = item;
        this.tagToReplace = tagToReplace;
        this.replacingTag = replacingTag;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.forEach(stack -> {
            if (stack.getItem() == item) {
                MCUtil.replaceNbtValues(stack.getOrCreateTag(), tagToReplace, replacingTag);
            }
        });
        return generatedLoot;
    }
}
