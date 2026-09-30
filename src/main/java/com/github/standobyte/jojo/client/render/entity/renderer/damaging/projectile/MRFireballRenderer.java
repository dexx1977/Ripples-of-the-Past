package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFireballEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class MRFireballRenderer extends ThrownItemRenderer<MRFireballEntity> {
    
    public MRFireballRenderer(EntityRendererProvider.Context context) {
        super(context, 1F, true);
    }

    @Override
    public void render(MRFireballEntity entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
}
