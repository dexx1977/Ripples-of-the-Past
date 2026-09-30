package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;

public class TextTooltipLine implements ITooltipLine {
    private final FormattedText text;
    
    public TextTooltipLine(FormattedText textLine) {
        this.text = textLine;
    }
    
    @Override
    public void draw(PoseStack matrixStack, float x, float y, Font font) {
        MultiBufferSource.BufferSource renderType = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(Language.getInstance().getVisualOrder(text), x, y, -1, 
                true, matrixStack.last().pose(), renderType, false, 0, 0xF000F0);
        renderType.endBatch();
    }
    
    @Override
    public int getWidth(Font font) {
        return font.width(text);
    }

    @Override
    public int getHeight(Font font) {
        return font.lineHeight + 1;
    }
    
    @Override
    public List<ITooltipLine> split(int width, Font font, Style style) {
        return font.getSplitter().splitLines(text, width, style)
                .stream().map(TextTooltipLine::new).collect(Collectors.toList());
    }

    @Override
    public Stream<FormattedText> getTextOnly() {
        return Stream.of(text);
    }

}
