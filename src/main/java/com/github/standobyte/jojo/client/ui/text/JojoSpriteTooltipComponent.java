package com.github.standobyte.jojo.client.ui.text;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** A tooltip line of the mod that also draws story part sprites. */
public record JojoSpriteTooltipComponent(JojoTextComponentWrapper wrapper) implements TooltipComponent {}
