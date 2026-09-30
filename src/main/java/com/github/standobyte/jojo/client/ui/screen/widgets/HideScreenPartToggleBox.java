package com.github.standobyte.jojo.client.ui.screen.widgets;

import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HideScreenPartToggleBox extends ToggleBox {
    protected final Screen screen;
    private final Direction elementDirection;

    public HideScreenPartToggleBox(int x, int y, Direction elementDirection, Screen screen) {
        super(x, y, 12, 12, Component.empty(), false);
        this.screen = screen;
        this.elementDirection = elementDirection;
    }
    
    @Override
    public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        GuiDraw.bind(ClientUtil.ADDITIONAL_UI);
        int texX = 208;
        int texY = 104;
        if (getState()) texX += width;
        if (isHovered()) texX += width * 2;
        GuiDraw.blit(matrixStack, x, y, texX, texY, width, height);
        Direction direction = getState() ? elementDirection.getOpposite() : elementDirection;
        texX = direction.getTexX();
        texY += height;
        GuiDraw.blit(matrixStack, x, y, texX, texY, width, height);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (isHovered()) {
            renderToolTip(matrixStack, mouseX, mouseY);
        }
    }

    public void renderToolTip(PoseStack matrixStack, int mouseX, int mouseY) {
        Component text = getState() ? 
                Component.translatable("jojo.ui.spoiler.hide")
                : Component.translatable("jojo.ui.spoiler.show");
        GuiDraw.renderToolTip(matrixStack, text, mouseX, mouseY);
    }
    
    public enum Direction {
        UP(220),
        DOWN(208),
        LEFT(244),
        RIGHT(232);
        
        private final int texX;
        
        private Direction(int texX) {
            this.texX = texX;
        }
        
        private Direction getOpposite() {
            switch (this) {
            case UP:    return DOWN;
            case DOWN:  return UP;
            case LEFT:  return RIGHT;
            case RIGHT: return LEFT;
            default:    return null;
            }
        }
        
        private int getTexX() {
            return texX;
        }
    }
}
