package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

public class MrPresidentKeyItem extends Item {
    public final boolean masterKey;

    public MrPresidentKeyItem(Properties pProperties, boolean masterKey) {
        super(pProperties);
        this.masterKey = masterKey;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (masterKey) {
            tooltip.add(Component.translatable(getOrCreateDescriptionId() + ".hint").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.jojo.creative_only_tooltip").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

}
