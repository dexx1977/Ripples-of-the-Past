package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ControlSettingToggleButton extends Button {
    private final Supplier<Boolean> settingGetter;

    public ControlSettingToggleButton(int pWidth, int pHeight, 
            Button.IPressable onPress, Supplier<Boolean> settingGetter) {
        super(-1, -1, pWidth, pHeight, Component.empty(), onPress);
        this.settingGetter = settingGetter;
        setMessageFromSetting(settingGetter.get());
    }
    
    public void setMessageFromSetting(boolean setting) {
        setMessage(Component.translatable(setting ? "options.key.toggle" : "options.key.hold"));
    }
    
    @Override
    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        setMessageFromSetting(settingGetter.get());
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
