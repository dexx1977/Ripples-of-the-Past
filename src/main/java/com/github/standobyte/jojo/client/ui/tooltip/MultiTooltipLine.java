package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class MultiTooltipLine implements ITooltipLine {
    private final List<ITooltipLine> lineParts;
    private final FormattedText textOnly;

    public MultiTooltipLine(ITooltipLine... parts) {
        this.lineParts = new ArrayList<>();
        Collections.addAll(this.lineParts, parts);
        this.textOnly = Component.empty();
    }
    
    
    @Override
    public void draw(PoseStack matrixStack, float x, float y, Font font) {
        for (ITooltipLine line : lineParts) {
            line.draw(matrixStack, x, y, font);
            x += line.getWidth(font) + 1;
        }
    }
    
    @Override
    public int getWidth(Font font) {
        return lineParts.stream().map(line -> line.getWidth(font) + 1).reduce(-1, Integer::sum);
    }

    @Override
    public int getHeight(Font font) {
        return lineParts.stream().map(line -> line.getHeight(font) + 1).max(Comparator.naturalOrder()).orElse(0);
    }
    
    @Override
    public List<ITooltipLine> split(int width, Font font, Style style) {
        return Collections.singletonList(this);
    }
    
    @Override
    public Stream<FormattedText> getTextOnly() {
        return Stream.of(textOnly);
    }
}
