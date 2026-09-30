package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class HamonSkillElementTechniquePerk extends HamonSkillGuiElement {
    private final List<FormattedCharSequence> perkDesc;
    @Nullable private ItemStack itemIcon;

    public HamonSkillElementTechniquePerk(AbstractHamonSkill skill, int x, int y, Font font) {
        super(skill, x, y, 16, 16);
        this.perkDesc = Stream.concat(
                font.split(Component.translatable("hamon.technique_perk", skill.getNameTranslated()), 200).stream(), 
                skill.getDescTranslated().stream().flatMap(desc -> font.split(desc.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), 200).stream()))
                .collect(Collectors.toList());
    }

    public HamonSkillElementTechniquePerk(int x, int y, Font font, FormattedText... desc) {
        super(null, Component.literal(""), x, y, 16, 16);
        this.perkDesc = Arrays.stream(desc)
                .flatMap(line -> font.split(line, 200).stream())
                .collect(Collectors.toList());
    }

    @Override
    void drawTooltip(HamonScreen hamonScreen, PoseStack matrixStack, int mouseX, int mouseY) {
        com.github.standobyte.jojo.client.ui.render.GuiDraw.renderToolTip(matrixStack, perkDesc, mouseX, mouseY);
    }
    
    
    
    public HamonSkillElementTechniquePerk withItemIcon(ItemStack item) {
        this.itemIcon = item;
        return this;
    }
    
    @Override
    public void renderSkillIcon(PoseStack matrixStack, int x, int y) {
        if (skill != null) {
            super.renderSkillIcon(matrixStack, x, y);
        }
        if (itemIcon != null) {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(itemIcon, this.x + x, this.y + y);
        }
    }
    
    boolean isVisible() {
        if (skill == ModHamonSkills.DEEP_PASS.get() || skill == ModHamonSkills.CRIMSON_BUBBLE.get()) {
            return !JojoModConfig.getCommonConfigInstance(true).keepHamonOnDeath.get();
        }
        return true;
    }
}
