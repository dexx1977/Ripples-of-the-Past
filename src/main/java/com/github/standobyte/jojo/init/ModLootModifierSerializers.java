package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.loot.AdditionalSingleItemLootModifier;
import com.github.standobyte.jojo.util.mc.loot.ReplaceItemNbtModifier;
import com.mojang.serialization.Codec;

import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The mod's global loot modifiers; 1.20.1 registers their codecs. */
public class ModLootModifierSerializers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = 
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, JojoMod.MOD_ID);
    
    public static final RegistryObject<Codec<AdditionalSingleItemLootModifier>> ADDITIONAL_SINGLE_ITEM = 
            LOOT_MODIFIER_SERIALIZERS.register("additional_single_item", () -> AdditionalSingleItemLootModifier.CODEC);
    
    public static final RegistryObject<Codec<ReplaceItemNbtModifier>> REPLACE_ITEM_NBT = 
            LOOT_MODIFIER_SERIALIZERS.register("replace_item_nbt", () -> ReplaceItemNbtModifier.CODEC);
}
