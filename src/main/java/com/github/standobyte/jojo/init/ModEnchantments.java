package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.enchantment.GlovesDamageEnchantment;
import com.github.standobyte.jojo.enchantment.GlovesSpeedEnchantment;
import com.github.standobyte.jojo.enchantment.StandArrowXpReductionEnchantment;
import com.github.standobyte.jojo.enchantment.VirusInhibitionEnchantment;
import com.github.standobyte.jojo.item.GlovesItem;
import com.github.standobyte.jojo.item.StandArrowItem;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, JojoMod.MOD_ID);
    
    public static final EnchantmentCategory STAND_ARROW = EnchantmentCategory.create("JOJO_STAND_ARROW", item -> item instanceof StandArrowItem);
    public static final EnchantmentCategory GLOVES = EnchantmentCategory.create("JOJO_GLOVES", item -> item instanceof GlovesItem);
    
    public static final RegistryObject<Enchantment> VIRUS_INHIBITION = ENCHANTMENTS.register("virus_inhibition", 
            () -> new VirusInhibitionEnchantment(Rarity.RARE, EquipmentSlot.MAINHAND));
    
    public static final RegistryObject<Enchantment> STAND_ARROW_XP_REDUCTION = ENCHANTMENTS.register("stand_arrow_xp_reduction", 
            () -> new StandArrowXpReductionEnchantment(Rarity.COMMON, EquipmentSlot.MAINHAND));
    
    
    public static final RegistryObject<Enchantment> GLOVES_DAMAGE = ENCHANTMENTS.register("gloves_damage", 
            () -> new GlovesDamageEnchantment(Rarity.COMMON, GLOVES, EquipmentSlot.MAINHAND));
    
    public static final RegistryObject<GlovesSpeedEnchantment> GLOVES_SPEED = ENCHANTMENTS.register("gloves_speed", 
            () -> new GlovesSpeedEnchantment(Rarity.RARE, GLOVES, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));

}
