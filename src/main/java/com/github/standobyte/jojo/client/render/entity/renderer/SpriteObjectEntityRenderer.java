package com.github.standobyte.jojo.client.render.entity.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.util.function.Consumer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class SpriteObjectEntityRenderer extends EntityRenderer<ObjectEntity> {

    public SpriteObjectEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static final ResourceLocation TOOTH_PARTICLE = new ResourceLocation(JojoMod.MOD_ID, "textures/particle/tooth.png");
    @Override
    public ResourceLocation getTextureLocation(ObjectEntity pEntity) {
        return TOOTH_PARTICLE;
    }
    
    @Override
    public void render(ObjectEntity entity, float entityYaw, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();

        renderSprite(matrixStack, 
                stack -> {
//                    float height = entity.getBbHeight();
//                    matrixStack.translate(0, height / 2, 0);
                }, 
                stack -> {
                    float height = entity.getBbHeight();
                    float width = entity.getBbWidth();
                    float scale = width * 0.4F;
                    matrixStack.scale(scale, scale, scale);
                    matrixStack.translate(width * -2, height * -2, 0);
                }, 
                buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))), 
                packedLight, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
        super.render(entity, entityYaw, partialTick, matrixStack, buffer, packedLight);
    }
    
    public void renderSprite(PoseStack matrixStack, Consumer<PoseStack> beforeRotate, Consumer<PoseStack> afterRotate, 
            VertexConsumer vertexBuilder, int packedLight, int packedOverlay) {
        beforeRotate.accept(matrixStack);
        matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
        afterRotate.accept(matrixStack);
        
        PoseStack.Pose pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        
        Vector3f normalVec = new Vector3f(0, 0, -1);
        normalVec.transform(matrix3f);

        for (ModelPart.PositionTextureVertex vertex : VERTICES) {
            float vertexX = vertex.pos.x();
            float vertexY = vertex.pos.y();
            float vertexZ = vertex.pos.z();
            Vector4f vector4f = new Vector4f(vertexX, vertexY, vertexZ, 1.0F);
            vector4f.transform(matrix4f);

            vertexBuilder.vertex(
                    vector4f.x(), vector4f.y(), vector4f.z(), 
                    1, 1, 1, 1, 
                    vertex.u, vertex.v,
                    packedOverlay, 
                    packedLight, 
                    normalVec.x(), normalVec.y(), normalVec.z());
        }
    }
    
    private static final ModelPart.PositionTextureVertex[] VERTICES = new ModelPart.PositionTextureVertex[] {
            new ModelPart.PositionTextureVertex(0, 1, 0, 1, 0),
            new ModelPart.PositionTextureVertex(1, 1, 0, 0, 0),
            new ModelPart.PositionTextureVertex(1, 0, 0, 0, 1),
            new ModelPart.PositionTextureVertex(0, 0, 0, 1, 1)
    };
}
