package com.github.standobyte.jojo.client.ui.screen.walkman;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;

@SuppressWarnings("deprecation")
public class WalkmanVolumeWheel extends AbstractWidget {
    private static final float FULL_WHEEL_LENGTH = 100;
    private final WalkmanScreen screen;
    private float value;

    public WalkmanVolumeWheel(WalkmanScreen screen, int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("walkman.volume"));
        this.screen = screen;
    }
    
    void setValue(float value) {
        value = Mth.clamp(value, 0, 1);
        if (this.value != value) {
            this.value = value;
            screen.onVolumeChanged(value);
        }
    }
    
    float getValue() {
        return value;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        setValue(value - (float) dragY / FULL_WHEEL_LENGTH);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        setValue(value + (float) scroll * 0.05F);
        return true;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft.getInstance().getTextureManager().bind(WalkmanScreen.WALKMAN_SCREEN_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiDraw.blit(matrixStack, x, y, isHovered() ? 229 : 245 , 61 + (int) (value * FULL_WHEEL_LENGTH), width, height);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {}

}
