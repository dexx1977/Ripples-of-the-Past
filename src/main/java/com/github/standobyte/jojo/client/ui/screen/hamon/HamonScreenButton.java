package com.github.standobyte.jojo.client.ui.screen.hamon;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import net.minecraft.client.gui.components.Button;
import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;

@SuppressWarnings("deprecation")
public class HamonScreenButton extends CustomButton {
    
    public HamonScreenButton(int x, int y, int width, int height, 
            Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }
    
    public HamonScreenButton(int x, int y, int width, int height, 
            Component message, Button.OnPress onPress, Tooltip tooltip) {
        super(x, y, width, height, message, onPress, tooltip);
    }
    
    @Override
    public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        Minecraft minecraft = Minecraft.getInstance();
        GuiDraw.bind(WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.enableDepthTest();
        GuiDraw.blit(matrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        GuiDraw.blit(matrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
    }
    
    public void drawName(PoseStack matrixStack) {
        // shadow is gone for whatever reason so it's rendered here
        GuiDraw.drawCenteredString(matrixStack, Minecraft.getInstance().font, this.getMessage(), 
                this.x + this.getWidth() / 2 + 1, this.y + (this.getHeight() - 8) / 2 + 1, 
                0x3E3E3E | Mth.ceil(this.alpha * 255.0F) << 24);
        
        GuiDraw.drawCenteredString(matrixStack, Minecraft.getInstance().font, this.getMessage(), 
                this.x + this.getWidth() / 2, this.y + (this.getHeight() - 8) / 2, 
                getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
