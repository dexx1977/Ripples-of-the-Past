package com.github.standobyte.jojo.client.ui.screen;

import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.github.standobyte.jojo.JojoMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class PlaceholderScreen extends Screen implements IJojoScreen {
    private static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/empty.png");
    private static final int WINDOW_WIDTH = 230;
    private static final int WINDOW_HEIGHT = 180;

    public PlaceholderScreen() {
        super(Component.empty());
    }
    
    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public IJojoScreen.TabCategory getTabCategory() {
        return IJojoScreen.TabCategory.GENERAL;
    }
    
    @Override
    public IJojoScreen.Tab getTab() {
        return IJojoScreen.GeneralTab.WIP_CATEGORY.get();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        renderBackground(com.github.standobyte.jojo.client.ui.render.GuiDraw.graphics());
        renderWindow(matrixStack);
        defaultRenderTabs(matrixStack, mouseX, mouseY, this);
    }
    
    private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
    private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }
    
    private void renderWindow(PoseStack matrixStack) {
        RenderSystem.enableBlend();
        GuiDraw.bind(WINDOW);
        GuiDraw.blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return defaultClickTab(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
}
