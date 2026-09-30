package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.HamonBubbleBarrierModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleBarrierEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HamonBubbleBarrierRenderer extends SimpleEntityRenderer<HamonBubbleBarrierEntity, HamonBubbleBarrierModel> {

    public HamonBubbleBarrierRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new HamonBubbleBarrierModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hamon_bubble_barrier.png"));
    }
    
    @Override
    protected void renderModel(HamonBubbleBarrierEntity entity, HamonBubbleBarrierModel model, 
            float partialTick, PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight) {
        matrixStack.pushPose();
        float size = entity.getSize(partialTick);
        if (size < 1) {
            matrixStack.scale(size, size, size);
        }
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.popPose();
    }

}
