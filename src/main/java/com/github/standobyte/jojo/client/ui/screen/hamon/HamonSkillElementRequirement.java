package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;

public class HamonSkillElementRequirement extends HamonSkillGuiElement {

    public HamonSkillElementRequirement(AbstractHamonSkill skill, int x, int y) {
        super(skill, x, y, 16, 16);
    }

    @Override
    void drawTooltip(HamonScreen hamonScreen, PoseStack matrixStack, int mouseX, int mouseY) {
        com.github.standobyte.jojo.client.ui.render.GuiDraw.renderToolTip(matrixStack, 
                name.withStyle(hamonScreen.hamon.isSkillLearned(getHamonSkill()) ? ChatFormatting.GREEN : ChatFormatting.RED), 
                mouseX, mouseY);
    }
}
