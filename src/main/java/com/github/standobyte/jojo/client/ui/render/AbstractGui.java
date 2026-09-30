package com.github.standobyte.jojo.client.ui.render;

/**
 * Marker for the mod's GUI code.
 *
 * <p>1.16.5's AbstractGui was the drawing base class (blit, fill, drawString and
 * the item renderer). 1.20.1 replaced it with the instance-based GuiGraphics, which
 * the mod reaches through {@link GuiDraw}, so this class only preserves the type
 * for code that extends it or takes it as a parameter. Drawing itself never goes
 * through it.</p>
 */
public abstract class AbstractGui {
}
