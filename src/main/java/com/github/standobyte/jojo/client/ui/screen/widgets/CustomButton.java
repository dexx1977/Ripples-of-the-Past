package com.github.standobyte.jojo.client.ui.screen.widgets;

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

    public CustomButton(int x, int y, int width, int height, Button.Button.OnPress onPress) {
        this(x, y, width, height, Component.empty(), onPress);
    }

    public CustomButton(int x, int y, int width, int height, Button.Button.OnPress onPress, Button.ITooltip tooltip) {
        this(x, y, width, height, Component.empty(), onPress, tooltip);
    }

    public CustomButton(int x, int y, int width, int height, Component message, Button.Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.extension = new WidgetExtension(this);
    }

    public CustomButton(int x, int y, int width, int height, Component message, Button.Button.OnPress onPress, Button.ITooltip tooltip) {
        super(x, y, width, height, message, onPress, tooltip);
        this.extension = new WidgetExtension(this);
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        renderCustomButton(matrixStack, mouseX, mouseY, partialTick);
        if (isHovered()) {
            renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
    
    protected void renderCustomButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        blit(matrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
        renderBg(matrixStack, minecraft, x, y);
    }
    
    
    
    @Override
    public WidgetExtension getWidgetExtension() {
        return extension;
    }
    
    @Override
    public AbstractWidget thisAsWidget() {
        return this;
    }
}
