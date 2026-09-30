package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.client.render.item.CustomIconItem;
import com.github.standobyte.jojo.init.ModEnchantments;

import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab extends CreativeModeTab {

    public ModCreativeTab(String label) {
        super(label);
        this.setEnchantmentCategories(new EnchantmentCategory[]{ ModEnchantments.STAND_ARROW, ModEnchantments.GLOVES });
    }

    @Override
    public ItemStack makeIcon() {
        return CustomIconItem.makeIconItem(CustomIconItem.CustomModelIcon.MOD_LOGO);
    }

}
