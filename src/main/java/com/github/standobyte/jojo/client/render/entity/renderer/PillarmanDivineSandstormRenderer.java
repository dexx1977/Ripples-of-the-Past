package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.damaging.projectile.PillarmanDivineSandstormEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class PillarmanDivineSandstormRenderer extends EntityRenderer<PillarmanDivineSandstormEntity> {

    public PillarmanDivineSandstormRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(PillarmanDivineSandstormEntity p_110775_1_) {
        return null;
    }

    @Override
    public void render(PillarmanDivineSandstormEntity entity, float yRotation, float partialTick, 
            PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {}
    
}
