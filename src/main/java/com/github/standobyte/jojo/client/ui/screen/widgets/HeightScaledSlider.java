package com.github.standobyte.jojo.client.ui.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;

@SuppressWarnings("deprecation")
public abstract class HeightScaledSlider extends AbstractSliderButton {

    public HeightScaledSlider(int pX, int pY, int pWidth, int pHeight, Component pMessage, double pValue) {
        super(pX, pY, pWidth, pHeight, pMessage, pValue);
    }
    
    @Override
    public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font fontrenderer = minecraft.font;
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int texY = 46 + getYImage(isHovered()) * 20;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(pMatrixStack, 
                x, y,            
                0, texY, 
                width / 2, height / 2);
        blit(pMatrixStack, 
                x + width / 2, y,            
                200 - width / 2, texY, 
                width - width / 2, height / 2);
        blit(pMatrixStack, 
                x, y + height / 2,            
                0, texY + 20 - height / 2, 
                width / 2, height - height / 2);
        blit(pMatrixStack, 
                x + width / 2, y + height / 2,            
                200 - width / 2, texY + 20 - height / 2, 
                width - width / 2, height - height / 2);
        this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
        int j = getFGColor();
        drawCenteredString(pMatrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }
    
    @Override
    protected void renderBg(PoseStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
        pMinecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (isHovered() ? 2 : 1) * 20;
        blit(pMatrixStack, 
                x + (int)(value * (width - 8)), y, 
                0, 46 + i, 4, height / 2);
        blit(pMatrixStack, 
                x + (int)(value * (width - 8)) + 4, y, 
                196, 46 + i, 4, height / 2);
        blit(pMatrixStack, 
                x + (int)(value * (width - 8)), y + height / 2, 
                0, 46 + i + 20 - height / 2, 4, height - height / 2);
        blit(pMatrixStack, 
                x + (int)(value * (width - 8)) + 4, y + height / 2, 
                196, 46 + i + 20 - height / 2, 4, height - height / 2);
    }

}
