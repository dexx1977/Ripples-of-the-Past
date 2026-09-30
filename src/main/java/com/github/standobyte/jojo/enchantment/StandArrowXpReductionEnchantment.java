package com.github.standobyte.jojo.enchantment;

import com.github.standobyte.jojo.init.ModEnchantments;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;

public class StandArrowXpReductionEnchantment extends Enchantment {
    
    public StandArrowXpReductionEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, ModEnchantments.STAND_ARROW, slots);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
    
    public static int getXpRequirementReduction(int level) {
        return level * 2;
    }

    @Override
    public int getMinCost(int level) {
        return 1 + level * 11;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }
    
    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }
}
