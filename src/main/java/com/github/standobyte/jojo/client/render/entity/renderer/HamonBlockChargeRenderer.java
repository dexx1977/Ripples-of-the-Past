package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class HamonBlockChargeRenderer extends EntityRenderer<HamonBlockChargeEntity> {

    public HamonBlockChargeRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(HamonBlockChargeEntity entity) {
        return null;
    }

    @Override
    public void render(HamonBlockChargeEntity entity, float yRotation, float partialTick, 
            PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {}

}
