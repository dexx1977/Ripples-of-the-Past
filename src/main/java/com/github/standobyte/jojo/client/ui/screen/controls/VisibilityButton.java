package com.github.standobyte.jojo.client.ui.screen.controls;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
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

    public VisibilityButton(int x, int y, Button.OnPress onPress) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), onPress);
    }

    public VisibilityButton(int x, int y, Button.OnPress onPress, Tooltip tooltip) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), onPress, tooltip);
    }
    
    public void setVisibilityState(boolean elementVisible) {
        this.elementVisible = elementVisible;
    }
    
    @SuppressWarnings("deprecation")
    protected void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        Minecraft minecraft = Minecraft.getInstance();
        GuiDraw.bind(ClientUtil.ADDITIONAL_UI);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int texX = 0;
        int texY = 80;
        if (!elementVisible) texX += width;
        if (isHovered()) texY += height;
        GuiDraw.blit(matrixStack, x, y, texX, texY, width, height);
        renderBg(guiGraphics, minecraft, x, y);
    }

}
