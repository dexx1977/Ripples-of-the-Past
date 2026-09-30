package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.client.render.entity.model.projectile.MRCrossfireHurricaneModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public class MRCrossfireHurricaneRenderer extends SimpleEntityRenderer<MRCrossfireHurricaneEntity, MRCrossfireHurricaneModel> {

    public MRCrossfireHurricaneRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new MRCrossfireHurricaneModel(), null);
    }
    
    @Override
    protected void doRender(MRCrossfireHurricaneEntity entity, MRCrossfireHurricaneModel model, 
            float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        float scale = entity.getScale();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, scale);
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(Sheets.cutoutBlockSheet()), packedLight);
        matrixStack.popPose();
    }

}
