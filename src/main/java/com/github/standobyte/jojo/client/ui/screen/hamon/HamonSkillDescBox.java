package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_THIN_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_UPPER_BORDER;

import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class HamonSkillDescBox {
    protected static final int WIDTH = 198;
    protected static final int HEIGHT = 44;
    protected static final int EDGE_TEXT_OFFSET = 3;
    protected final Font font;
    protected final AbstractHamonSkill skill;
    protected final int x;
    protected final int y;
    protected final int textWidth;
    protected final int textHeight;
    protected final boolean hasScrolling;
    protected float yTextScroll = 0;
    protected boolean isDragged = false;
    protected boolean isScrollBarDragged = false;
    protected List<FormattedCharSequence> skillDesc;
    
    public HamonSkillDescBox(AbstractHamonSkill skill, Font font, int textWidth, int x, int y) {
        this.skill = skill;
        this.font = font;
        this.x = x;
        this.y = y;
        
        List<FormattedCharSequence> skillDesc = createFullDescText(skill, font, textWidth);
        int textHeight = EDGE_TEXT_OFFSET + font.lineHeight * skillDesc.size();
        boolean scroll = false;
        if (textHeight > HEIGHT) {
            textWidth -= 6;
            skillDesc = createFullDescText(skill, font, textWidth);
            textHeight = EDGE_TEXT_OFFSET + font.lineHeight * skillDesc.size() + EDGE_TEXT_OFFSET;
            scroll = true;
        }
        this.skillDesc = skillDesc;
        this.textWidth = textWidth;
        this.textHeight = textHeight;
        this.hasScrolling = scroll;
    }
    
    protected List<FormattedCharSequence> createFullDescText(AbstractHamonSkill skill, Font font, int textWidth) {
        List<FormattedCharSequence> lines = skill.getDescTranslated().stream()
                .flatMap(desc -> font.split(desc, textWidth).stream())
                .collect(Collectors.toList());
        return lines;
    }
    
    public void renderBg(PoseStack matrixStack, int x, int y, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(HamonSkillsTabGui.HAMON_SKILLS);
        GuiDraw.blit(matrixStack, this.x + x - 3, this.y + y - 3, 52, 206, WIDTH + 6, HEIGHT + 6, 256, 256);
        
        GuiDraw.blit(matrixStack, this.x + x - 3, this.y + y - 3, 52, 156, WIDTH + 6, HEIGHT + 6, 256, 256);
        
        if (hasScrolling) {
            renderScrollBar(matrixStack, x, y, mouseX, mouseY);
        }
    }
    
    private void renderScrollBar(PoseStack matrixStack, int xOffset, int yOffset, int mouseX, int mouseY) {
        int brightness;
        if (isScrollBarDragged) {
            brightness = 255;
        }
        else if (isMouseOverScrollBar(mouseX, mouseY, xOffset, yOffset)) {
            brightness = 191;
        }
        else {
            brightness = 127;
        }
        float[] scrollBar = getScrollBarPosSize(xOffset, yOffset);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        ClientUtil.fillRect(bufferBuilder, scrollBar[0], scrollBar[1], scrollBar[2], scrollBar[3], brightness, brightness, brightness, 127);
    }
    
    private float[] getScrollBarPosSize(int xOffset, int yOffset) {
        if (!hasScrolling) return null;
        float maxLength = HEIGHT - EDGE_TEXT_OFFSET * 2;
        float rectWidth = 3;
        float rectHeight = (float) (HEIGHT - EDGE_TEXT_OFFSET) / (float) textHeight * maxLength;
        float rectX = this.x + xOffset + WIDTH - EDGE_TEXT_OFFSET * 2;
        float rectY = this.y + yOffset + EDGE_TEXT_OFFSET - yTextScroll / getMaxYTextScroll() * (rectHeight - maxLength);
        return new float[] { rectX, rectY, rectWidth, rectHeight };
    }
    
    private boolean isMouseOverScrollBar(int mouseX, int mouseY, int xOffset, int yOffset) {
        if (!hasScrolling) return false;
        float[] scrollBar = getScrollBarPosSize(xOffset, yOffset);
        return mouseX >= scrollBar[0] && mouseX <= scrollBar[0] + scrollBar[2] &&
                mouseY >= scrollBar[1] && mouseY <= scrollBar[1] + scrollBar[3];
    }
    
    public void drawDesc(PoseStack matrixStack, Font font, Screen screen, int x, int y) {
        int scissorX = HamonScreen.screenX + this.x + x + WINDOW_THIN_BORDER;
        int scissorY = HamonScreen.screenY + this.y + y + WINDOW_UPPER_BORDER;
        
        ClientUtil.enableGlScissor(scissorX, scissorY, WIDTH, HEIGHT);
        
        for (int i = 0; i < skillDesc.size(); i++) {
            float lineY = this.y + y + EDGE_TEXT_OFFSET - yTextScroll + i * font.lineHeight;
            if (lineY + font.lineHeight >= this.y + y && lineY <= this.y + y + HEIGHT) {
                GuiDraw.drawString(matrixStack, font, skillDesc.get(i), this.x + x + EDGE_TEXT_OFFSET, lineY, 0xFFFFFF);
            }
        }
        
        ClientUtil.disableGlScissor();
    }
    
    public boolean isMouseOver(double mouseX, double mouseY, double offsetX, double offsetY) {
        double x = this.x + offsetX;
        double y = this.y + offsetY;
        return mouseX > x && mouseX <= x + WIDTH && mouseY > y && mouseY <= y + HEIGHT;
    }
    
    public void drawTooltips(PoseStack matrixStack, Screen screen, double mouseX, double mouseY, double x, double y) {}
    
    public boolean scroll(float yMovement) {
        if (hasScrolling) {
            yTextScroll = Mth.clamp(yTextScroll + yMovement, 0, getMaxYTextScroll());
            return true;
        }
        return false;
    }
    
    public boolean onClick(int mouseX, int mouseY, int x, int y) {
        if (hasScrolling && isMouseOver(mouseX, mouseY, x, y)) {
            if (isMouseOverScrollBar(mouseX, mouseY, x, y)) {
                isScrollBarDragged = true;
            }
            else {
                isDragged = true;
            }
            return true;
        }
        return false;
    }
    
    public boolean onDrag(double yMovement) {
        if (isScrollBarDragged) {
            scroll((float) yMovement);
            return true;
        }
        else if (isDragged) {
            scroll((float) -yMovement);
            return true;
        }
        return false;
    }
    
    public boolean onRelease() {
        if (isDragged || isScrollBarDragged) {
            isDragged = false;
            isScrollBarDragged = false;
            return true;
        }
        return false;
    }
    
    private float getMaxYTextScroll() {
        return hasScrolling ? textHeight - HEIGHT : -1;
    }

}
