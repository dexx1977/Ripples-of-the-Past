package com.github.standobyte.jojo.client.ui.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/** Draws the text of the line and its sprites, like the old tooltip extra. */
public class JojoSpriteClientTooltipComponent implements ClientTooltipComponent {
    private final JojoTextComponentWrapper wrapper;

    public JojoSpriteClientTooltipComponent(JojoSpriteTooltipComponent component) {
        this.wrapper = component.wrapper();
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(wrapper);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        guiGraphics.drawString(font, wrapper, x, y, 0xFFFFFF, false);
        wrapper.tooltipRenderExtra(guiGraphics.pose(), x, y);
    }
}
