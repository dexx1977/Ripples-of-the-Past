package com.github.standobyte.jojo.client.particle.custom;

import org.lwjgl.opengl.GL11;

import com.github.standobyte.jojo.client.ClientModSettings;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

/*
 *  Thanks to desht from TeamPneumatic
 *  https://github.com/TeamPneumatic/pnc-repressurized/blob/9f722043d49d248222e0117a7eea6afef75af069/src/main/java/me/desht/pneumaticcraft/client/particle/AirParticle.java#L107
 */
public class HamonAuraParticleRenderType implements ParticleRenderType {
    public static final HamonAuraParticleRenderType HAMON_AURA = new HamonAuraParticleRenderType();
    
    protected HamonAuraParticleRenderType() {}
    
    @SuppressWarnings("deprecation")
    @Override
    public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        RenderSystem.disableLighting();

        textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
        if (ClientModSettings.getSettingsReadOnly().hamonAuraBlur) {
            textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
        }
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.PARTICLE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void end(Tesselator tessellator) {
        tessellator.end();

        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    @Override
    public String toString() {
        return "jojo:hamon_aura";
    }

}
