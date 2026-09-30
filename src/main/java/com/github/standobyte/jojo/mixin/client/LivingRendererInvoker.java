package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public interface LivingRendererInvoker<T extends LivingEntity, M extends EntityModel<T>> {

    @Invoker
    void invokeScale(T entity, PoseStack matrixStack, float partialTick);
}
