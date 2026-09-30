package com.github.standobyte.jojo.client.ui.tooltip;


import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;

public class CustomTooltipRender {
    // the vanilla tooltip colours Forge's GuiUtils used to expose
    private static final int DEFAULT_BACKGROUND_COLOR = 0xF0100010;
    private static final int DEFAULT_BORDER_COLOR_START = 0x505000FF;
    private static final int DEFAULT_BORDER_COLOR_END = 0x5028007F;


    public static void renderWrappedToolTip(PoseStack matrixStack, List<? extends ITooltipLine> tooltipLines, int mouseX, int mouseY, Font font) {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        drawHoveringText(matrixStack, tooltipLines, 
                mouseX, mouseY, width, height, -1, 
                DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR_START, DEFAULT_BORDER_COLOR_END, 
                font, true);
    }
    
    @Deprecated
    public static void drawHoveringText(PoseStack mStack, List<? extends ITooltipLine> tooltipLines, int mouseX, int mouseY,
                                        int screenWidth, int screenHeight, int maxTextWidth,
                                        int backgroundColor, int borderColorStart, int borderColorEnd, Font font) {
        drawHoveringText(mStack, tooltipLines, mouseX, mouseY, 
                screenWidth, screenHeight, maxTextWidth, 
                backgroundColor, borderColorStart, borderColorEnd, font, true);
    }
    
    @SuppressWarnings("deprecation")
    public static void drawHoveringText(PoseStack mStack, List<? extends ITooltipLine> tooltipLines, int mouseX, int mouseY,
                                        int screenWidth, int screenHeight, int maxTextWidth,
                                        int backgroundColor, int borderColorStart, int borderColorEnd, Font font, boolean firstLineTitle) {
        if (!tooltipLines.isEmpty())
        {
            List<? extends FormattedText> eventTextOnlyLines = tooltipLines.stream().flatMap(ITooltipLine::getTextOnly).collect(Collectors.toList());
            // 1.20.1 hands the tooltip events the GuiGraphics and the client components
            List<ClientTooltipComponent> eventComponents = eventTextOnlyLines.stream()
                    .map(text -> ClientTooltipComponent.create(net.minecraft.locale.Language.getInstance().getVisualOrder(text)))
                    .collect(Collectors.toList());
            RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(ItemStack.EMPTY, GuiDraw.graphics(), mouseX, mouseY, 
                    screenWidth, screenHeight, font, eventComponents, null);
            if (MinecraftForge.EVENT_BUS.post(event))
                return;
            mouseX = event.getX();
            mouseY = event.getY();
            screenWidth = event.getScreenWidth();
            screenHeight = event.getScreenHeight();
            font = event.getFont();
            RenderSystem.disableDepthTest();
            int tooltipTextWidth = 0;

            for (ITooltipLine textLine : tooltipLines)
            {
                int textLineWidth = textLine.getWidth(font);
                if (textLineWidth > tooltipTextWidth)
                    tooltipTextWidth = textLineWidth;
            }

            boolean needsWrap = false;

            int titleLinesCount = firstLineTitle ? 1 : 0;
            int tooltipX = mouseX + 12;
            if (tooltipX + tooltipTextWidth + 4 > screenWidth)
            {
                tooltipX = mouseX - 16 - tooltipTextWidth;
                if (tooltipX < 4) // if the tooltip doesn't fit on the screen
                {
                    if (mouseX > screenWidth / 2)
                        tooltipTextWidth = mouseX - 12 - 8;
                    else
                        tooltipTextWidth = screenWidth - 16 - mouseX;
                    needsWrap = true;
                }
            }

            if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth)
            {
                tooltipTextWidth = maxTextWidth;
                needsWrap = true;
            }

            if (needsWrap)
            {
                int wrappedTooltipWidth = 0;
                List<ITooltipLine> wrappedTextLines = new ArrayList<>();
                for (int i = 0; i < tooltipLines.size(); i++)
                {
                    ITooltipLine textLine = tooltipLines.get(i);
                    List<ITooltipLine> wrappedLine = textLine.split(tooltipTextWidth, font, Style.EMPTY);
                    if (i == 0)
                        titleLinesCount = wrappedLine.size();

                    for (ITooltipLine line : wrappedLine)
                    {
                        int lineWidth = line.getWidth(font);
                        if (lineWidth > wrappedTooltipWidth)
                            wrappedTooltipWidth = lineWidth;
                        wrappedTextLines.add(line);
                    }
                }
                tooltipTextWidth = wrappedTooltipWidth;
                tooltipLines = wrappedTextLines;
                eventTextOnlyLines = tooltipLines.stream().flatMap(ITooltipLine::getTextOnly).collect(Collectors.toList());

                if (mouseX > screenWidth / 2)
                    tooltipX = mouseX - 16 - tooltipTextWidth;
                else
                    tooltipX = mouseX + 12;
            }

            int tooltipY = mouseY - 12;
            int tooltipHeight = titleLinesCount * 2; // gap between title lines and next lines
            
            for (int i = 0; i < tooltipLines.size(); ++i) {
                tooltipHeight += tooltipLines.get(i).getHeight(font);
            }

            if (tooltipY < 4)
                tooltipY = 4;
            else if (tooltipY + tooltipHeight + 4 > screenHeight)
                tooltipY = screenHeight - tooltipHeight - 4;

            final int zLevel = 400;
            RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(ItemStack.EMPTY, GuiDraw.graphics(), tooltipX, tooltipY, 
                    font, backgroundColor, borderColorStart, borderColorEnd, eventComponents);
            MinecraftForge.EVENT_BUS.post(colorEvent);
            backgroundColor = colorEvent.getBackgroundStart();
            borderColorStart = colorEvent.getBorderStart();
            borderColorEnd = colorEvent.getBorderEnd();

            mStack.pushPose();
            ClientUtil.drawTooltipRectangle(mStack, tooltipX, tooltipY, tooltipTextWidth, tooltipHeight, backgroundColor, borderColorStart, borderColorEnd, zLevel);
            
            // 1.20.1 has no PostBackground/PostText tooltip events any more

            mStack.translate(0.0D, 0.0D, zLevel);

            int tooltipTop = tooltipY;

            for (int lineNumber = 0; lineNumber < tooltipLines.size(); ++lineNumber)
            {
                ITooltipLine line = tooltipLines.get(lineNumber);
                if (line != null)
                    line.draw(mStack, tooltipX, tooltipY, font);

                if (lineNumber + 1 == titleLinesCount)
                    tooltipY += 2;

                tooltipY += line.getHeight(font);
            }

            mStack.popPose();


            RenderSystem.enableDepthTest();
        }
    }
}
