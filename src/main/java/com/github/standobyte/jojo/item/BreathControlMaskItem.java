package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

public class BreathControlMaskItem extends CustomModelArmorItem {

    public BreathControlMaskItem(Properties builder) {
        super(ModArmorMaterials.BREATH_CONTROL_MASK, EquipmentSlot.HEAD, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.jojo.breath_control_mask.hint").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" ").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.jojo.breath_control_mask.hint2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" "));
    }
}
