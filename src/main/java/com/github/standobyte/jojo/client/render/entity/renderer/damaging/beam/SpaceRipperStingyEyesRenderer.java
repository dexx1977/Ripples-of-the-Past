package com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SpaceRipperStingyEyesEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class SpaceRipperStingyEyesRenderer extends EntityRenderer<SpaceRipperStingyEyesEntity> {
    private static final ResourceLocation BEAM_TEX = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/space_ripper_stingy_eyes.png");

    public SpaceRipperStingyEyesRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(SpaceRipperStingyEyesEntity entity) {
        return BEAM_TEX;
    }

    @Override
    public void render(SpaceRipperStingyEyesEntity entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        packedLight = ClientUtil.MAX_MODEL_LIGHT;
        Vec3 beamVec = entity.getOriginPoint(partialTick).subtract(entity.getPosition(partialTick));
        float yRot = MathUtil.yRotDegFromVec(beamVec);
        float xRot = MathUtil.xRotDegFromVec(beamVec);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-90.0F - yRot));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
        float beamWidth = 0.15f;
        matrixStack.scale(1.0F, beamWidth, beamWidth);
        Matrix3f lighting = matrixStack.last().normal();
        lighting.setIdentity();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        lighting.mul(Axis.XP.rotationDegrees(camera.getXRot()));
        VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.entityTranslucentCull(getTextureLocation(entity)));
        float length = (float) beamVec.length();
        
        renderSide(matrixStack, new Vector3f(0, -1, 0), length, ivertexbuilder, packedLight);
        renderSide(matrixStack, new Vector3f(0, 0, -1), length, ivertexbuilder, packedLight);
        renderSide(matrixStack, new Vector3f(0, 1, 0), length, ivertexbuilder, packedLight);
        renderSide(matrixStack, new Vector3f(0, 0, 1), length, ivertexbuilder, packedLight);
        
        matrixStack.popPose();
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }
    
    
    private void renderSide(PoseStack matrixStack, Vector3f lightNormal, float length, VertexConsumer ivertexbuilder, int packedLight) {
        matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        matrixStack.pushPose();
        
        matrixStack.translate(0, 0, 0.125f);

        PoseStack.Entry matrix = matrixStack.last();
        Matrix4f pose = matrix.pose();
        Matrix3f normal = matrix.normal();
        
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, -1, 0, 0.0F, 0.0F, lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, -1, 0, length, 0.0F, lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, 1, 0, length, 1.0f, lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, 1, 0, 0.0F, 1.0F, lightNormal.x(), lightNormal.y(), lightNormal.z());

        matrixStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, -1, 0, 0.0F, 0.0F, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, -1, 0, length, 0.0F, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, 1, 0, length, 1.0f, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, 1, 0, 0.0F, 1.0F, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        
        matrixStack.popPose();
    }

}
