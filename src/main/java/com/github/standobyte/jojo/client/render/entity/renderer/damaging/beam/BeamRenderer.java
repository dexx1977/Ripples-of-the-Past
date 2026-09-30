package com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public abstract class BeamRenderer<T extends DamagingEntity> extends EntityRenderer<T> {

    public BeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    protected abstract Vec3 pointB(T entity, float partialTick);
    
    protected abstract float getBeamWidth(T entity);

    @Override
    public void render(T entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        packedLight = ClientUtil.MAX_MODEL_LIGHT;
        Vec3 beamVec = pointB(entity, partialTick).subtract(entity.getPosition(partialTick));
        float yRot = MathUtil.yRotDegFromVec(beamVec);
        float xRot = MathUtil.xRotDegFromVec(beamVec);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-90.0F - yRot));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
        float beamWidth = getBeamWidth(entity);
        matrixStack.scale(1.0F, beamWidth, beamWidth);
        VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        PoseStack.Entry matrixstack$entry = matrixStack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrixNormal = matrixstack$entry.normal();

        float length = (float) beamVec.length();
        int lengthInt = (int) length / 2;
        matrixStack.translate(length / 2, 0.0D, 0.0D);
        float beamLength = length / 64F;
        matrixStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        for (int j = 0; j < 4; ++j) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            ClientUtil.vertex(matrix4f, matrixNormal, ivertexbuilder, 
                    packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                    -lengthInt, -1, 0, 0.0F, 0.0F, 0, 1, 0);
            ClientUtil.vertex(matrix4f, matrixNormal, ivertexbuilder, 
                    packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                    lengthInt, -1, 0, beamLength, 0.0F, 0, 1, 0);
            ClientUtil.vertex(matrix4f, matrixNormal, ivertexbuilder, 
                    packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                    lengthInt, 1, 0, beamLength, 0.046875F, 0, 1, 0);
            ClientUtil.vertex(matrix4f, matrixNormal, ivertexbuilder, 
                    packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                    -lengthInt, 1, 0, 0.0F, 0.046875F, 0, 1, 0);
        }

        matrixStack.popPose();
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }
}
