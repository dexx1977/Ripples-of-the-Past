package com.github.standobyte.jojo.client.ui.screen.controls;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class VisibilityButton extends CustomButton {
    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;
    
    private boolean elementVisible;

    public VisibilityButton(int x, int y, Button.IPressable onPress) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), onPress);
    }

    public VisibilityButton(int x, int y, Button.IPressable onPress, Button.ITooltip tooltip) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), onPress, tooltip);
    }
    
    public void setVisibilityState(boolean elementVisible) {
        this.elementVisible = elementVisible;
    }
    
    @SuppressWarnings("deprecation")
    protected void renderCustomButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(ClientUtil.ADDITIONAL_UI);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int texX = 0;
        int texY = 80;
        if (!elementVisible) texX += width;
        if (isHovered()) texY += height;
        blit(matrixStack, x, y, texX, texY, width, height);
        renderBg(matrixStack, minecraft, x, y);
    }

}
