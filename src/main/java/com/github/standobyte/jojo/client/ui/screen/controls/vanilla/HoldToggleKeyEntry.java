package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.controls.KeyBindsList;

public class HoldToggleKeyEntry extends KeyBindsList.Entry {
    private final KeyBindsList.Entry wrappedEntry;
    private final Button holdToggleButton;
    private final Button changeButton;

    public HoldToggleKeyEntry(VanillaKeyEntry wrappedEntry, Button holdToggleButton) {
        this(wrappedEntry, wrappedEntry.changeButton, holdToggleButton);
    }

    public HoldToggleKeyEntry(KeyBindsList.Entry wrappedEntry, Button entryChangeKeyButton, Button holdToggleButton) {
        this.wrappedEntry = wrappedEntry;
        this.holdToggleButton = holdToggleButton;
        changeButton = entryChangeKeyButton;
        changeButton.setWidth(changeButton.getWidth() - holdToggleButton.getWidth() - 0);
    }
    
    @Override
    public void refreshEntry() {
        wrappedEntry.refreshEntry();
    }

    @Override
    public List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
        List<net.minecraft.client.gui.narration.NarratableEntry> narratables = new ArrayList<>();
        narratables.add(holdToggleButton);
        return narratables;
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, 
            int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        holdToggleButton.x = pLeft + 105 + changeButton.getWidth() - 1;
        holdToggleButton.y = pTop;
        holdToggleButton.render(com.github.standobyte.jojo.client.ui.render.GuiDraw.graphics(), pMouseX, pMouseY, pPartialTicks);
        
        wrappedEntry.render(com.github.standobyte.jojo.client.ui.render.GuiDraw.graphics(), pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        List<? extends GuiEventListener> vanillaButtons = wrappedEntry.children();
        List<Button> buttons = new ArrayList<>();
        for (GuiEventListener button : vanillaButtons) {
            buttons.add((Button) button);
        }
        buttons.add(holdToggleButton);
        return buttons;
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        List<? extends GuiEventListener> buttons = children();
        for (GuiEventListener button : buttons) {
            if (button.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        List<? extends GuiEventListener> buttons = children();
        for (GuiEventListener button : buttons) {
            if (button.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
}
