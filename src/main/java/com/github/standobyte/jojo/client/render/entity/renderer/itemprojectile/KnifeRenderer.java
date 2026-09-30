package com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class KnifeRenderer extends ArrowRenderer<KnifeEntity> {

    public KnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(KnifeEntity entity) {
        return entity.getKnifeTexture();
    }

    @Override
    public void render(KnifeEntity entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        float yRot = Mth.lerp(partialTick, entity.yRotO, entity.yRot) - 90.0F;
        matrixStack.mulPose(Axis.YP.rotationDegrees(yRot));
//        matrixStack.translate(0, 0.0625 * (1 - MathHelper.sin(yRot * MathUtil.DEG_TO_RAD)), 0);
        matrixStack.translate(0, 0.125, 0);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.xRot)));
        float scale = 0.0375F;
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(1, 0, 0);
        VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        PoseStack.Pose matrixstack$entry = matrixStack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        
        
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, 
                2f/32, 27f/32, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, 
                5f/32, 27f/32, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, 
                5f/32, 30f/32, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, 
                2f/32, 30f/32, -1, 0, 0, packedLight);
        
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, 
                2f/32, 27f/32, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, 
                5f/32, 27f/32, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, 
                5f/32, 30f/32, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, 
                2f/32, 30f/32, 1, 0, 0, packedLight);
        
        
        
        matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      12f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 12f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 21f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      21f/32, 0, 1, 0, packedLight);
        
        matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      0f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 0f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 9f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      9f/32, 0, 1, 0, packedLight);
        
        matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      21f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 21f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 12f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      12f/32, 0, 1, 0, packedLight);
        
        matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      9f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 9f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 0f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      0f/32, 0, 1, 0, packedLight);
        
        matrixStack.popPose();
    }
 }