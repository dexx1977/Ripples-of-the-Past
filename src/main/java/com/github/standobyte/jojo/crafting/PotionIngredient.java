package com.github.standobyte.jojo.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.crafting.NBTIngredient;

public class PotionIngredient extends NBTIngredient {
    
    public PotionIngredient(Item potionItem, Potion potion) {
        this(PotionUtils.setPotion(new ItemStack(potionItem), potion));
    }

    public PotionIngredient(ItemStack stack) {
        super(stack);
    }

}
