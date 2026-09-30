package com.github.standobyte.jojo.client.ui.screen.widgets;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class ItemButton extends Button {
    private final ItemStack item;

    public ItemButton(int pX, int pY, int pWidth, int pHeight, 
            ItemStack item, 
            Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                item, 
                pOnPress, NO_TOOLTIP, Component.empty());
    }

    public ItemButton(int pX, int pY, int pWidth, int pHeight, 
            ItemStack item, 
            Button.OnPress pOnPress, Tooltip pOnTooltip) {
        this(pX, pY, pWidth, pHeight, 
                item, 
                pOnPress, pOnTooltip, Component.empty());
    }

    public ItemButton(int pX, int pY, int pWidth, int pHeight, 
            ItemStack item, 
            Button.OnPress pOnPress, Tooltip pOnTooltip, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.item = item;
    }

    public void setPosition(int pX, int pY) {
        this.x = pX;
        this.y = pY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        PoseStack pMatrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        Minecraft minecraft = Minecraft.getInstance();
        GuiDraw.bind(WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GuiDraw.blit(pMatrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        GuiDraw.blit(pMatrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
        renderBg(guiGraphics, minecraft, pMouseX, pMouseY);
        
        minecraft.getItemRenderer().renderGuiItem(item, x + (width - 16) / 2, y + (height - 16) / 2);
    }

}
