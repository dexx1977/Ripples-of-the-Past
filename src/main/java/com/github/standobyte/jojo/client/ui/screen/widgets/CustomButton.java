package com.github.standobyte.jojo.client.ui.screen.widgets;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.github.standobyte.jojo.client.ui.screen.widgets.utils.IExtendedWidget;
import com.github.standobyte.jojo.client.ui.screen.widgets.utils.WidgetExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

@SuppressWarnings("deprecation")
public class CustomButton extends Button implements IExtendedWidget {
    private final WidgetExtension extension;

    public CustomButton(int x, int y, int width, int height, Button.OnPress onPress) {
        this(x, y, width, height, Component.empty(), onPress);
    }

    public CustomButton(int x, int y, int width, int height, Button.OnPress onPress, Tooltip tooltip) {
        this(x, y, width, height, Component.empty(), onPress, tooltip);
    }

    public CustomButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.extension = new WidgetExtension(this);
    }

    public CustomButton(int x, int y, int width, int height, Component message, Button.OnPress onPress, Tooltip tooltip) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        setTooltip(tooltip);
        this.extension = new WidgetExtension(this);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // the tooltip the widget was constructed with is drawn by the widget
        // framework from setTooltip, as it was in 1.16.5
        GuiDraw.setGraphics(guiGraphics);
        renderCustomButton(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    protected void renderCustomButton(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack matrixStack = guiGraphics.pose();
        Minecraft minecraft = Minecraft.getInstance();
        GuiDraw.bind(WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
        GuiDraw.blit(matrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        GuiDraw.blit(matrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
        renderBg(guiGraphics, minecraft, x, y);
    }
    
    /** The texture row the button uses, as the old vanilla method returned it. */
    protected int getYImage(boolean hovered) {
        return !this.active ? 0 : (hovered ? 2 : 1);
    }
    
    /** Hook for subclasses; the vanilla method of the same role rendered nothing. */
    protected void renderBg(GuiGraphics guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {}
    
    
    
    @Override
    public WidgetExtension getWidgetExtension() {
        return extension;
    }
    
    @Override
    public AbstractWidget thisAsWidget() {
        return this;
    }
}
