package com.github.standobyte.jojo.client.ui.screen.widgets.utils;

import net.minecraft.client.gui.components.AbstractWidget;

public interface IExtendedWidget {
    WidgetExtension getWidgetExtension();
    AbstractWidget thisAsWidget();
}
