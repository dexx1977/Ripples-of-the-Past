package com.github.standobyte.jojo.client.render.rendertype;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderStateShard;

public class ScaledTexturingState extends RenderStateShard.TexturingStateShard {
    private final float xScale;
    private final float yScale;

    @SuppressWarnings("deprecation")
    public ScaledTexturingState(float xScale, float yScale) {
        // 1.20.1 scales the texture through its matrix
        super("jojo_scaled_texturing", 
                () -> RenderSystem.setTextureMatrix(new org.joml.Matrix4f().scale(xScale, yScale, 1.0F)), 
                RenderSystem::resetTextureMatrix);
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ScaledTexturingState scaledState = (ScaledTexturingState)obj;
            return this.xScale == scaledState.xScale && this.yScale == scaledState.yScale;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Float.hashCode(this.xScale) + Float.hashCode(this.yScale);
    }
}
