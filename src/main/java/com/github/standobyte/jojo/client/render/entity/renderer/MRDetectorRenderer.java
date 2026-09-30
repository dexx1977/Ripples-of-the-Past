package com.github.standobyte.jojo.client.render.entity.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.MRDetectorModel;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MRDetectorRenderer extends SimpleEntityRenderer<MRDetectorEntity, MRDetectorModel> {

    public MRDetectorRenderer(EntityRendererProvider.Context context) {
        super(context, new MRDetectorModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/mr_detector.png"));
    }    
    
    @Override
    protected void doRender(MRDetectorEntity entity, MRDetectorModel model, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0.0D, Mth.sin((entity.tickCount + partialTick) * 0.04F) * 0.04F, 0.0D);
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(model.renderType(getTextureLocation(entity))), packedLight);
        model.renderFlames(matrixStack, buffer, entityRenderDispatcher.camera);
        matrixStack.popPose();
    }
}
