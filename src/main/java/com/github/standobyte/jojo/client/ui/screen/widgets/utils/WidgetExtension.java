package com.github.standobyte.jojo.client.ui.screen.widgets.utils;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetExtension {
    private final AbstractWidget originWidget;
    
    private int yStarting;
    
    public WidgetExtension(AbstractWidget originWidget) {
        this.originWidget = originWidget;
        this.yStarting = originWidget.y;
    }
    
    
    public void setY(int y) {
        originWidget.y = y;
        this.yStarting = y;
    }
    
    public int getYStarting() {
        return yStarting;
    }
    
    public void updateY(int scrollY) {
        originWidget.y = this.yStarting + scrollY;
    }
}
