package com.github.standobyte.jojo.client.ui.screen.widgets;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class TextButton extends Button {
    private Font font;
    
    public TextButton(int pX, int pY, Component pMessage, 
            Button.OnPress pOnPress, Tooltip pOnTooltip, Font font) {
        super(pX, pY, font.width(pMessage), font.lineHeight, pMessage, pOnPress, pOnTooltip);
        this.font = font;
    }

    public TextButton(int pX, int pY, Component pMessage, 
            Button.OnPress pOnPress, Font font) {
        this(pX, pY, pMessage, pOnPress, NO_TOOLTIP, font);
    }

    @Override
    public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        PoseStack pMatrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        Minecraft mc = Minecraft.getInstance();
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        renderBg(guiGraphics, mc, pMouseX, pMouseY);
        
        int j = getFGColor();
        Component text = makeText();
        GuiDraw.drawString(pMatrixStack, font, text, x, y + (height - 8) / 2, j | Mth.ceil(alpha * 255.0F) << 24);
        width = font.width(text);
    }
    
    public Component makeText() {
        Component text = getMessage();
        if (isHovered()) {
            text = Component.translatable("jojo.ui.text_button_hovered", text).withStyle(ChatFormatting.GREEN);
        }
        return text;
    }
}
