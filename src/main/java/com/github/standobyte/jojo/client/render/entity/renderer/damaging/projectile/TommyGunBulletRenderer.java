package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class TommyGunBulletRenderer extends EntityRenderer<TommyGunBulletEntity> {
    protected double maxTrailLen = 4;
    protected float V1 = 0.015625f;
    protected float BEAM_WIDTH = 0.015f;
    protected double BULLET_U = 0.015625;

    public TommyGunBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    private static final ResourceLocation TRAIL_TEX = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/bullet_trace.png");
    @Override
    public ResourceLocation getTextureLocation(TommyGunBulletEntity entity) {
        return TRAIL_TEX;
    }
    
    @Override
    public boolean shouldRender(TommyGunBulletEntity entity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return super.shouldRender(entity, pCamera, pCamX, pCamY, pCamZ) || 
                entity.initialPos != null && pCamera.isVisible(new AABB(entity.initialPos, entity.position()));
    }
    
    @Override
    public void render(TommyGunBulletEntity entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        List<Vec3> trace = entity.tracePos;
        if (trace.isEmpty()) {
            trace = Util.make(new ArrayList<>(), list -> {
                Vec3 pos = entity.position();
                list.add(pos.subtract(entity.getDeltaMovement().normalize().scale(maxTrailLen * BULLET_U)));
                list.add(pos);
            });
        }
        
        matrixStack.pushPose();
        matrixStack.translate(0, entity.getBbHeight() / 2, 0);
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucentCull(getTextureLocation(entity)));
        
        double traceLen = maxTrailLen;
        int i;
        boolean first = true;
        for (i = trace.size() - 1; i > 0 && traceLen > 0; i--) {
            Vec3 posCur = trace.get(i);
            Vec3 posPrev = trace.get(i - 1);
            float u0;
            float u1 = (float) (traceLen / maxTrailLen);
            
            Vec3 diffBack = posPrev.subtract(posCur);
            double len = diffBack.length();
            
            // render the bullet if there is no trail long enough yet
            double bulletStart = maxTrailLen * (1 - BULLET_U);
            if (i == 1 && len < traceLen - bulletStart) {
                double ratio = (traceLen - bulletStart) / len;
                len = traceLen - bulletStart;
                posPrev = posCur.add(diffBack.scale(ratio));
            }
            
            if (len > traceLen) {
                posPrev = posCur.add(diffBack.normalize().scale(traceLen));
                traceLen = 0;
            }
            else {
                traceLen -= len;
            }
            u0 = (float) (traceLen / maxTrailLen);
            
            trailSegment(posPrev, posCur, u0, u1, matrixStack, vertexBuilder, entity, yRotation, partialTick, first);
            first = false;
        }
        
        matrixStack.popPose();
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }
    
    protected void trailSegment(Vec3 pos1, Vec3 pos2, float u0, float u1, 
            PoseStack matrixStack, VertexConsumer vertexBuilder, 
            TommyGunBulletEntity entity, float yRotation, float partialTick, boolean first) {
        matrixStack.pushPose();
        Vec3 trailSegmentVec = pos1.subtract(pos2);
        float yRot = MathUtil.yRotDegFromVec(trailSegmentVec);
        float xRot = MathUtil.xRotDegFromVec(trailSegmentVec);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-90.0F - yRot));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
        matrixStack.scale(1.0F, BEAM_WIDTH, BEAM_WIDTH);
        Matrix3f lighting = matrixStack.last().normal();
        lighting.identity();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        lighting.mul(Axis.XP.rotationDegrees(camera.getXRot()));
        float length = (float) trailSegmentVec.length();
        
        if (first) {
            renderFront(matrixStack, new Vector3f(0, 0, 1), vertexBuilder);
        }
        renderSide(matrixStack, new Vector3f(0, -1,  0),  length, u0, u1, vertexBuilder);
        renderSide(matrixStack, new Vector3f(0,  0, -1),  length, u0, u1, vertexBuilder);
        renderSide(matrixStack, new Vector3f(0,  1,  0),  length, u0, u1, vertexBuilder);
        renderSide(matrixStack, new Vector3f(0,  0,  1),  length, u0, u1, vertexBuilder);
        
        matrixStack.popPose();
        matrixStack.translate(trailSegmentVec.x, trailSegmentVec.y, trailSegmentVec.z);
    }
    
    
    private void renderSide(PoseStack matrixStack, Vector3f lightNormal, float length, float u0, float u1, VertexConsumer vertexBuilder) {
        int packedLight = ClientUtil.MAX_MODEL_LIGHT;
        float v0 = 0;
        float v1 = V1;
        matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        matrixStack.pushPose();
        
        matrixStack.translate(0, 0, 1f);

        PoseStack.Pose matrix = matrixStack.last();
        Matrix4f pose = matrix.pose();
        Matrix3f normal = matrix.normal();
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, -1, 0, 
                u1, v0, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, -1, 0, 
                u0, v0, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, 1, 0, 
                u0, v1, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, 1, 0, 
                u1, v1, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());

        matrixStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, -1, 0, 
                u1, v0, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, -1, 0, 
                u0, v0, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, 1, 0, 
                u0, v1, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, 1, 0, 
                u1, v1, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        
        matrixStack.popPose();
    }
    
    private void renderFront(PoseStack matrixStack, Vector3f lightNormal, VertexConsumer vertexBuilder) {
        int packedLight = ClientUtil.MAX_MODEL_LIGHT;
        float u0 = 0;
        float u1 = u0 + V1;
        float v0 = V1;
        float v1 = v0 + V1;
        matrixStack.pushPose();
        
        matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        PoseStack.Pose matrix = matrixStack.last();
        Matrix4f pose = matrix.pose();
        Matrix3f normal = matrix.normal();
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                -1, -1, 0, 
                u1, v0, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                1, -1, 0, 
                u0, v0, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                1, 1, 0, 
                u0, v1, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                -1, 1, 0, 
                u1, v1, 
                lightNormal.x(), lightNormal.y(), lightNormal.z());

        matrixStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                -1, -1, 0, 
                u1, v0, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                1, -1, 0, 
                u0, v0, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                1, 1, 0, 
                u0, v1, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                -1, 1, 0, 
                u1, v1, 
                -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        
        matrixStack.popPose();
        
    }
}
