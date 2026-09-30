package com.github.standobyte.jojo.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.joml.Matrix4f;

public class BlitFloat {
    
    public static void blitFloat(PoseStack pMatrixStack, float pX, float pY, int pBlitOffset, 
            float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
        innerBlitFloat(pMatrixStack, 
                pX, pX + pUWidth, pY, pY + pVHeight, pBlitOffset, 
                pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
    }
    
    public static void blitFloat(PoseStack pMatrixStack, 
            float pX, float pY, float pBlitOffset, float pWidth, float pHeight, TextureAtlasSprite pSprite) {
        innerBlitFloat(pMatrixStack.last().pose(), 
                pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, 
                pSprite.getU0(), pSprite.getU1(), pSprite.getV0(), pSprite.getV1());
    }
    
    public static void blitFloat(PoseStack pMatrixStack, float pX, float pY, 
            float pUOffset, float pVOffset, float pWidth, float pHeight, float pTextureWidth, float pTextureHeight) {
        blitFloat(pMatrixStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight);
    }
    
    public static void blitFloat(PoseStack pMatrixStack, float pX, float pY, 
            float pWidth, float pHeight, float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
        innerBlitFloat(pMatrixStack, pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
    }
    
    public static void blitFloat(PoseStack pMatrixStack, 
            float pX, float pY, float pBlitOffset, float pWidth, float pHeight, TextureAtlasSprite pSprite,
            float uOffsetMult, float uWidthMult, float vOffsetMult, float vHeightMult) {
        float u0 = pSprite.getU0();
        float u1 = pSprite.getU1();
        float v0 = pSprite.getV0();
        float v1 = pSprite.getV1();
        float width = u1 - u0;
        float height = v1 - v0;
        u0 += width * uOffsetMult;
        v0 += height * vOffsetMult;
        u1 = u0 + width * uWidthMult;
        v1 = v0 + height * vHeightMult;
        innerBlitFloat(pMatrixStack.last().pose(), 
                pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, 
                u0, u1, v0, v1);
    }
    
    public static void innerBlitFloat(PoseStack pMatrixStack, 
            float pX1, float pX2, float pY1, float pY2, float pBlitOffset, 
            float pUWidth, float pVHeight, float pUOffset, float pVOffset, float pTextureWidth, float pTextureHeight) {
        innerBlitFloat(pMatrixStack.last().pose(), 
                pX1, pX2, pY1, pY2, pBlitOffset, 
                (pUOffset + 0.0F) / pTextureWidth, 
                (pUOffset + pUWidth) / pTextureWidth, 
                (pVOffset + 0.0F) / pTextureHeight, 
                (pVOffset + pVHeight) / pTextureHeight);
    }
    
    public static void innerBlitFloat(Matrix4f pMatrix, 
            float pX1, float pX2, float pY1, float pY2, float pBlitOffset, 
            float pMinU, float pMaxU, float pMinV, float pMaxV) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pMatrix, pX1, pY2, pBlitOffset).uv(pMinU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, pX2, pY2, pBlitOffset).uv(pMaxU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, pX2, pY1, pBlitOffset).uv(pMaxU, pMinV).endVertex();
        bufferbuilder.vertex(pMatrix, pX1, pY1, pBlitOffset).uv(pMinU, pMinV).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        BufferUploader.end(bufferbuilder);
    }
}
