package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.client.render.entity.model.projectile.MRCrossfireHurricaneModel;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class MRCrossfireHurricaneSpecialRenderer extends MRCrossfireHurricaneRenderer {

    public MRCrossfireHurricaneSpecialRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    protected void renderModel(MRCrossfireHurricaneEntity entity, MRCrossfireHurricaneModel model, float partialTick, PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight) {
        matrixStack.pushPose();
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        super.renderModel(entity, model, partialTick, matrixStack, vertexBuilder, packedLight);
        matrixStack.popPose();
    }

}
