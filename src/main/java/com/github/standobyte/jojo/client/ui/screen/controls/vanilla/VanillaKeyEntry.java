package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class VanillaKeyEntry extends KeyBindsList.Entry {
    private final KeyMapping key;
    private final Component name;
    final Button changeButton;
    private final Button resetButton;
    
    private final Minecraft mc;
    private final int maxNameWidth;
    
    private final Supplier<KeyMapping> getSelectedKey;
//    private final Consumer<KeyBinding> setSelectedKey;
    
    public VanillaKeyEntry(KeyMapping key, 
            ControlsScreen controlsScreen, KeyBindsList controlsList) {
        this(key, () -> controlsScreen.selectedKey, k -> controlsScreen.selectedKey = k, 
                ClientReflection.getMaxNameWidth(controlsList));
    }

    public VanillaKeyEntry(KeyMapping key, 
            Supplier<KeyMapping> getSelectedKey, Consumer<KeyMapping> setSelectedKey, int maxNameWidth) {
        this(key, Component.translatable(key.getName()), getSelectedKey, setSelectedKey, maxNameWidth);
    }

    public VanillaKeyEntry(KeyMapping key, Component name, 
            Supplier<KeyMapping> getSelectedKey, Consumer<KeyMapping> setSelectedKey, int maxNameWidth) {
        this.key = key;
        this.name = name;
        this.getSelectedKey = getSelectedKey;
//        this.setSelectedKey = setSelectedKey;
        this.maxNameWidth = maxNameWidth;
        this.mc = Minecraft.getInstance();
        this.changeButton = new Button(0, 0, 75 + 20, 20, name, button -> {
            setSelectedKey.accept(key);
        }) {
            protected MutableComponent createNarrationMessage() {
                return key.isUnbound() ? Component.translatable("narrator.controls.unbound", name) : Component.translatable("narrator.controls.bound", name, super.createNarrationMessage());
            }
        };
        this.resetButton = new Button(0, 0, 50, 20, Component.translatable("controls.reset"), (p_214387_2_) -> {
            key.setToDefault();
            mc.options.setKey(key, key.getDefaultKey());
            KeyMapping.resetMapping();
        }) {
            protected MutableComponent createNarrationMessage() {
                return Component.translatable("narrator.controls.reset", name);
            }
        };
    }

    public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, 
            int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        boolean isSelected = getSelectedKey.get() == this.key;
        mc.font.draw(pMatrixStack, name, 
                (float)(pLeft + 90 - maxNameWidth), (float)(pTop + pHeight / 2 - 9 / 2), 
                0xFFFFFF);
        this.resetButton.x = pLeft + 190 + 20;
        this.resetButton.y = pTop;
        this.resetButton.active = !this.key.isDefault();
        this.resetButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.changeButton.x = pLeft + 105;
        this.changeButton.y = pTop;
        this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
        boolean isConflicting = false;
        boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
        if (!this.key.isUnbound()) {
            for(KeyMapping keybinding : mc.options.keyMappings) {
                if (keybinding != this.key && this.key.same(keybinding)) {
                    isConflicting = true;
                    keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.key);
                }
            }
        }

        if (isSelected) {
            this.changeButton.setMessage(
                    Component.literal("> ")
                    .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.YELLOW))
                    .append(" <").withStyle(ChatFormatting.YELLOW));
        } else if (isConflicting) {
            this.changeButton.setMessage(
                    this.changeButton.getMessage().copy()
                    .withStyle(keyCodeModifierConflict ? ChatFormatting.GOLD : ChatFormatting.RED));
        }

        this.changeButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return ImmutableList.of(this.changeButton, this.resetButton);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.changeButton.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        } else {
            return this.resetButton.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return this.changeButton.mouseReleased(pMouseX, pMouseY, pButton)
                || this.resetButton.mouseReleased(pMouseX, pMouseY, pButton);
    }

}
