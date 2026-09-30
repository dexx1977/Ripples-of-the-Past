package com.github.standobyte.jojo.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class SimpleEntityRenderer<T extends Entity, M extends EntityModel<T>> extends EntityRenderer<T> {
    protected M model;
    protected final ResourceLocation texPath;

    public SimpleEntityRenderer(EntityRenderDispatcher renderManager, M model, ResourceLocation texPath) {
        super(renderManager);
        this.model = model;
        this.texPath = texPath;
    }
    
    protected M getEntityModel() {
        return model;
    }
    
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texPath;
    }

    @Override
    public void render(T entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (shouldRender(entity)) {
            M model = getEntityModel();
            if (model == null) return;
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            float xRotation = Mth.lerp(partialTick, entity.xRotO, entity.xRot);
            rotateModel(model, entity, partialTick, yRotation, xRotation, matrixStack);
            doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
            matrixStack.popPose();
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected boolean shouldRender(T entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
    protected void rotateModel(M model, T entity, float partialTick, float yRotation, float xRotation, PoseStack matrixStack) {
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    protected void doRender(T entity, M model, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(model.renderType(getTextureLocation(entity))), packedLight);
    }
    
    protected void renderModel(T entity, M model, float partialTick, PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight) {
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

}
