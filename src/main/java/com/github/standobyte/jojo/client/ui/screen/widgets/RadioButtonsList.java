package com.github.standobyte.jojo.client.ui.screen.widgets;

import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen.stand.ge.ChooseLifeformScreen;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RadioButtonsList<V> implements ContainerEventHandler {
    protected List<Button> radioButtons = new ArrayList<>();
    protected V selectedValue;
    protected Consumer<V> onNewValue;
    
    public RadioButtonsList() {}
    
    public RadioButtonsList(V defaultValue) {
        this.selectedValue = defaultValue;
    }
    
    public RadioButtonsList(V defaultValue, Consumer<V> onNewValue) {
        this(defaultValue);
        this.onNewValue = onNewValue;
    }
    
    public RadioButtonsList<V> addRenderableWidget(int x, int y, Component name, V value) {
        RadioButton button = new RadioButton(x, y, name, b -> {
            this.selectedValue = value;
            onNewValue(value);
        }, this, value);
        radioButtons.add(button);
        return this;
    }
    
    protected void onNewValue(V value) {
        if (onNewValue != null) {
            onNewValue.accept(value);
        }
    }
    
    public V getSelectedValue() {
        return selectedValue;
    }
    
    
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        for (Button button : radioButtons) {
            button.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
    
    
    
    private static class RadioButton extends Button {
        private RadioButtonsList<?> list;
        private Object value;

        public RadioButton(int x, int y, Component pMessage, Button.OnPress pOnPress, 
                RadioButtonsList<?> list, Object value) {
            super(x, y, 13, 13, pMessage, pOnPress, Button.DEFAULT_NARRATION);
            this.list = list;
            this.value = value;
        }
        
        @Override
        public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        PoseStack pMatrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
            Minecraft minecraft = Minecraft.getInstance();
            GuiDraw.bind(ChooseLifeformScreen.LIFEFORM_CHOOSE_LOCATION);
            int texY = list.getSelectedValue() == value ? 40 : 53;
            GuiDraw.blit(pMatrixStack, x, y, 115, texY, width, height, 128, 128);
            minecraft.font.drawShadow(pMatrixStack, getMessage(), x + 16, y + (height - minecraft.font.lineHeight) / 2, 0xFFFFFF);
        }
        
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return radioButtons;
    }
    
    
    @Nullable
    private GuiEventListener focused;
    private boolean dragging;
    
    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean pDragging) {
        this.dragging = pDragging;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener pListener) {
        this.focused = pListener;
    }
    
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }
}
