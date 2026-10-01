package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.init.ModParticles;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class FlameRenderer<T extends Entity> extends EntityRenderer<T> {

    public FlameRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }

    private static final double STEP_LENGTH = 0.4D;
    @Override
    public void render(T entity, float yRotation, float partialTick, 
            PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
            Vec3 pos = entity.getPosition(partialTick);
            Vec3 start = getStartingPos(entity);
            if (start == null) {
                start = pos;
            }
            Vec3 vec = start.subtract(pos);
            double length = vec.length();
            if (length <= 0) {
                return;
            }
            Vec3 step = vec.scale(STEP_LENGTH / length);
            for (int i = Mth.floor(length / STEP_LENGTH); i > 0; i--) {
                entity.level.addAlwaysVisibleParticle(ModParticles.FLAME_ONE_TICK.get(), true, pos.x, pos.y, pos.z, 0, 0, 0);
                pos = pos.add(step);
            }
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected abstract Vec3 getStartingPos(T entity);
}
