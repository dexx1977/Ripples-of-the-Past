package com.github.standobyte.jojo.client.ui.screen.walkman;

import net.minecraft.client.gui.components.Button;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

@SuppressWarnings("deprecation")
public class WalkmanButton extends CustomButton {
    private final Supplier<Component> message;
    private final int texX;

    public WalkmanButton(int x, int y, int width, int height, Button.OnPress onPress, Supplier<Component> message, Screen screen, int texX) {
        this(x, y, width, height, onPress, (button, matrixStack, mouseX, mouseY) -> screen.renderTooltip(matrixStack, button.getMessage(), mouseX, mouseY), message, texX);
    }

    public WalkmanButton(int x, int y, int width, int height, Button.OnPress onPress, ITooltip tooltip, Supplier<Component> message, int texX) {
        super(x, y, width, height, Component.empty(), onPress, tooltip);
        this.message = message;
        this.texX = texX;
    }

    @Override
    protected void renderCustomButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, WalkmanScreen.WALKMAN_SCREEN_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrixStack, x, y, texX, active && isHovered() ? 240 : 227, width, height);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.5F, 0.1F));
    }

    @Override
    public void renderToolTip(PoseStack matrixStack, int mouseX, int mouseY) {
        if (active && isHovered()) {
            super.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
    
    @Override
    public Component getMessage() {
        Component message = this.message.get();
        return message != null ? message : super.getMessage();
    }
}
