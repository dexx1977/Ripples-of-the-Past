package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;

public class CategoryWithButtonsEntry extends KeyBindsList.CategoryEntry {
    private final List<Button> buttons;

    public CategoryWithButtonsEntry(KeyBindsList keyBindingList, 
            Component name, Button... buttons) {
        keyBindingList.super(name);
        this.buttons = Arrays.asList(buttons);
    }
    
    @Override
    public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        super.render(com.github.standobyte.jojo.client.ui.render.GuiDraw.graphics(), pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
        for (Button button : buttons) {
            button.y = pTop;
            button.render(com.github.standobyte.jojo.client.ui.render.GuiDraw.graphics(), pMouseX, pMouseY, pPartialTicks);
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return buttons;
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (GuiEventListener button : children()) {
            if (button.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        for (GuiEventListener button : children()) {
            if (button.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
}
