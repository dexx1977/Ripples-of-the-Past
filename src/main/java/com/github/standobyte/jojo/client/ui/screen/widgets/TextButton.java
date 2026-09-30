package com.github.standobyte.jojo.client.ui.screen.widgets;

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
            IPressable pOnPress, ITooltip pOnTooltip, Font font) {
        super(pX, pY, font.width(pMessage), font.lineHeight, pMessage, pOnPress, pOnTooltip);
        this.font = font;
    }

    public TextButton(int pX, int pY, Component pMessage, 
            IPressable pOnPress, Font font) {
        this(pX, pY, pMessage, pOnPress, NO_TOOLTIP, font);
    }

    @Override
    public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft mc = Minecraft.getInstance();
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        renderBg(pMatrixStack, mc, pMouseX, pMouseY);
        
        int j = getFGColor();
        Component text = makeText();
        font.drawShadow(pMatrixStack, text, x, y + (height - 8) / 2, j | Mth.ceil(alpha * 255.0F) << 24);
        width = font.width(text);
        
        if (isHovered()) {
            renderToolTip(pMatrixStack, pMouseX, pMouseY);
        }
    }
    
    public Component makeText() {
        Component text = getMessage();
        if (isHovered()) {
            text = Component.translatable("jojo.ui.text_button_hovered", text).withStyle(ChatFormatting.GREEN);
        }
        return text;
    }
}
