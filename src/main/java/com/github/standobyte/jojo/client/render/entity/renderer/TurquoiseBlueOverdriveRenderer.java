package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.damaging.projectile.HamonTurquoiseBlueOverdriveEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class TurquoiseBlueOverdriveRenderer extends EntityRenderer<HamonTurquoiseBlueOverdriveEntity> {

    public TurquoiseBlueOverdriveRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(HamonTurquoiseBlueOverdriveEntity p_110775_1_) {
        return null;
    }

    @Override
    public void render(HamonTurquoiseBlueOverdriveEntity entity, float yRotation, float partialTick, 
            PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (shouldRender(entity)) {
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected boolean shouldRender(HamonTurquoiseBlueOverdriveEntity entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
}
