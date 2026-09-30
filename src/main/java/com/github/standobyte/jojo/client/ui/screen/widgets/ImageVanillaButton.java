package com.github.standobyte.jojo.client.ui.screen.widgets;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class ImageVanillaButton extends Button {
    private final ResourceLocation resourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int textureWidth;
    private final int textureHeight;
    private final int iconWidth;
    private final int iconHeight;

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, 
            Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, 
                pResourceLocation, 256, 256, 
                pOnPress);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, 
                pResourceLocation, pTextureWidth, pTextureHeight, 
                pOnPress, Component.empty());
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.OnPress pOnPress, Component pMessage) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, pWidth, pHeight, 
                pResourceLocation, pTextureWidth, pTextureHeight, 
                pOnPress, NO_TOOLTIP, pMessage);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.OnPress pOnPress, Button.ITooltip pOnTooltip, Component pMessage) {
        this(pX, pY, pWidth, pHeight,
                pXTexStart, pYTexStart, pWidth, pHeight,
                pResourceLocation, pTextureWidth, pTextureHeight,
                pOnPress, pOnTooltip, pMessage);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, int iconWidth, int iconHeight, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, iconWidth, iconHeight, 
                pResourceLocation, pTextureWidth, pTextureHeight, 
                pOnPress, NO_TOOLTIP, Component.empty());
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, int iconWidth, int iconHeight, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.OnPress pOnPress, Button.ITooltip pOnTooltip, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.resourceLocation = pResourceLocation;
    }

    public void setPosition(int pX, int pY) {
        this.x = pX;
        this.y = pY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        GuiDraw.bind(WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GuiDraw.blit(pMatrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        GuiDraw.blit(pMatrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
        renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
        
        GuiDraw.bind(resourceLocation);
        RenderSystem.enableDepthTest();
        int iconX = x + (width - iconWidth) / 2;
        int iconY = y + (height - iconHeight) / 2;
        GuiDraw.blit(pMatrixStack, iconX, iconY, (float)xTexStart, (float)yTexStart, 
                iconWidth, iconHeight, textureWidth, textureHeight);
        
        if (isHovered()) {
            renderToolTip(pMatrixStack, pMouseX, pMouseY);
         }
    }

}
