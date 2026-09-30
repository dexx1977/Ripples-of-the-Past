package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.AngeloRockModel;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBlockBulletRenderer;
import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AngeloRockRenderer extends SimpleEntityRenderer<AngeloRockEntity, AngeloRockModel> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/block/stone.png");
    public static final ResourceLocation SHADOW_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/angelo_rock_shadow.png");

    public AngeloRockRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new AngeloRockModel(), TEXTURE);
    }
    
    @Override
    protected void doRender(AngeloRockEntity entity, AngeloRockModel model, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        model.shadow.visible = false;
        BlockState blockUpper = entity.getUpperBlock();
        BlockState blockLower = entity.getLowerBlock();
        if (blockUpper.equals(blockLower)) {
            model.upperHalf.visible = true;
            model.lowerHalf.visible = true;
            renderRockPart(entity, blockUpper, model, partialTick, matrixStack, buffer, packedLight);
        }
        else {
            model.upperHalf.visible = true;
            model.lowerHalf.visible = false;
            renderRockPart(entity, blockUpper, model, partialTick, matrixStack, buffer, packedLight);
            model.upperHalf.visible = false;
            model.lowerHalf.visible = true;
            renderRockPart(entity, blockLower, model, partialTick, matrixStack, buffer, packedLight);
        }
        
        if (entity.getCreationAnimProgress(partialTick) >= 1) {
            model.shadow.visible = true;
            model.upperHalf.visible = false;
            model.lowerHalf.visible = false;
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(SHADOW_TEXTURE));
            model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    private void renderRockPart(AngeloRockEntity entity, BlockState blockState, AngeloRockModel model, 
            float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        ResourceLocation texture = CDBlockBulletRenderer.getBlockTexture(blockState);
        if (texture == null) texture = TEXTURE;
        VertexConsumer vertexBuilder = buffer.getBuffer(model.renderType(texture));
        model.setCreationAnim(entity, entity.getCreationAnimProgress(partialTick));
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    
}
