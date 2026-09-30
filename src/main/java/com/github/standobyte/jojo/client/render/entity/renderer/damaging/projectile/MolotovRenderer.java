package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class MolotovRenderer<T extends Entity & ItemSupplier> extends ThrownItemRenderer<T> {

    public MolotovRenderer(EntityRendererProvider.Context context, ItemRenderer itemRenderer, 
            float scale, boolean fullBright) {
        super(context, itemRenderer, scale, fullBright);
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, 
            PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        Camera camera = entityRenderDispatcher.camera;
        Matrix3f lighting = pMatrixStack.last().normal();
        lighting.identity();
        lighting.mul(Axis.XP.rotationDegrees(90));
        lighting.mul(Axis.YP.rotationDegrees(camera.getYRot()));
        
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        pMatrixStack.popPose();
        
        if (!pEntity.isInWaterOrRain()) {
            pMatrixStack.pushPose();
            float scale = pEntity.getBbWidth();
            pMatrixStack.scale(scale, scale, scale);
            float f1 = 0.5F;
            float f3 = pEntity.getBbHeight() / scale;
            float f4 = 0;
            pMatrixStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            pMatrixStack.translate(0, 0.5, -0.3 + f3 * 0.02);
            float f5 = 0;
            int i = 0;
            VertexConsumer ivertexbuilder = pBuffer.getBuffer(Sheets.cutoutBlockSheet());

            for(PoseStack.Pose matrixstack$entry = pMatrixStack.last(); f3 > 0.0F; ++i) {
                TextureAtlasSprite sprite = i % 2 == 0 ? ModelBakery.FIRE_0.sprite() : ModelBakery.FIRE_1.sprite();
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU1();
                float v1 = sprite.getV1();
                if (i / 2 % 2 == 0) {
                    float f10 = u1;
                    u1 = u0;
                    u0 = f10;
                }

                fireVertex(matrixstack$entry, ivertexbuilder, f1 - 0.0F, 0.0F - f4, f5, u1, v1);
                fireVertex(matrixstack$entry, ivertexbuilder, -f1 - 0.0F, 0.0F - f4, f5, u0, v1);
                fireVertex(matrixstack$entry, ivertexbuilder, -f1 - 0.0F, 1.4F - f4, f5, u0, v0);
                fireVertex(matrixstack$entry, ivertexbuilder, f1 - 0.0F, 1.4F - f4, f5, u1, v0);
                f3 -= 0.45F;
                f4 -= 0.45F;
                f1 *= 0.9F;
                f5 += 0.03F;
            }

            pMatrixStack.popPose();
        }
    }

    private static void fireVertex(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, 
            float pX, float pY, float pZ, float pTexU, float pTexV) {
        pBuffer.vertex(pMatrixEntry.pose(), pX, pY, pZ)
        .color(255, 255, 255, 255)
        .uv(pTexU, pTexV)
        .overlayCoords(0, 10)
        .uv2(240)
        .normal(pMatrixEntry.normal(), 0.0F, 1.0F, 0.0F)
        .endVertex();
    }

}
