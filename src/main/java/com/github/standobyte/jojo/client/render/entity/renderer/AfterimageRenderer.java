package com.github.standobyte.jojo.client.render.entity.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class AfterimageRenderer<T extends AfterimageEntity> extends EntityRenderer<T> {

    public AfterimageRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return null;
    }

    @Override
    public void render(T entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        Entity originEntity = entity.getOriginEntity();
        if (entity.tickCount % 20 == 0) {
            System.out.println("[DBG-AFT] render id=" + entity.getId() + " origin=" + originEntity + " shouldRender=" + (originEntity != null && entity.shouldRender()) + " speedLower=" + entity.getSpeedLowerLimitForDebug());
        }
        if (originEntity != null) {
            Minecraft mc = Minecraft.getInstance();
            if (!entity.shouldRender() || originEntity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) {
                return;
            }
            entityRenderDispatcher.getRenderer(originEntity).render(originEntity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }

}
