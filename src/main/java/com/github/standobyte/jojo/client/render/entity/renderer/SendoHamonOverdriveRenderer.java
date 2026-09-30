package com.github.standobyte.jojo.client.render.entity.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class SendoHamonOverdriveRenderer extends EntityRenderer<HamonSendoOverdriveEntity> {

    public SendoHamonOverdriveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HamonSendoOverdriveEntity p_110775_1_) {
        return null;
    }

    @Override
    public void render(HamonSendoOverdriveEntity entity, float yRotation, float partialTick, 
            PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (shouldRender(entity)) {
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected boolean shouldRender(HamonSendoOverdriveEntity entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
}
