package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import net.minecraft.client.gui.GuiGraphics;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ControlSettingToggleButton extends Button {
    private final Supplier<Boolean> settingGetter;

    public ControlSettingToggleButton(int pWidth, int pHeight, 
            Button.OnPress onPress, Supplier<Boolean> settingGetter) {
        super(-1, -1, pWidth, pHeight, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.settingGetter = settingGetter;
        setMessageFromSetting(settingGetter.get());
    }
    
    public void setMessageFromSetting(boolean setting) {
        setMessage(Component.translatable(setting ? "options.key.toggle" : "options.key.hold"));
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        PoseStack pMatrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        setMessageFromSetting(settingGetter.get());
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }
}
