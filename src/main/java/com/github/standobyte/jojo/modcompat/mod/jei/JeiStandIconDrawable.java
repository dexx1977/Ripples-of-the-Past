package com.github.standobyte.jojo.modcompat.mod.jei;

import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.gui.elements.DrawableResource;
import mezz.jei.library.gui.ingredients.CycleTimer;

public class JeiStandIconDrawable implements IDrawable {
    private final CycleTimer iconsCycle = CycleTimer.create(0);
    private final List<IDrawable> standIcons;
    
    public JeiStandIconDrawable(List<StandType<?>> standIcons) {
        this.standIcons = standIcons.stream()
                .map(stand -> new DrawableResource(stand.getIconTexture(null), 
                        0, 0, 16, 16, 0, 0, 0, 0, 16, 16))
                .collect(Collectors.toList());
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(net.minecraft.client.gui.GuiGraphics guiGraphics, int xOffset, int yOffset) {
        if (!standIcons.isEmpty()) {
            iconsCycle.getCycled(standIcons).ifPresent(standIcon -> standIcon.draw(guiGraphics, xOffset, yOffset));
        }
    }

}
