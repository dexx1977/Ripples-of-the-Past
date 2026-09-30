package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public interface ITooltipLine {
    void draw(PoseStack matrixStack, float x, float y, Font font);
    int getWidth(Font font);
    int getHeight(Font font);
    List<ITooltipLine> split(int width, Font font, Style style);
    Stream<FormattedText> getTextOnly();
}
