package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;

public class KnifeLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;
    private KnifeEntity knife;

    public KnifeLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.dispatcher = renderer.getDispatcher();
    }

    @Override
    protected int numStuck(T entity) {
        return entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(
                cap -> cap.getStuckObjects().getKnives().getCount()).orElse(0);
    }

    protected void renderStuckItem(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            Entity entity, float x, float y, float z, float partialTick) {
        float f = Mth.sqrt(x * x + z * z);
        knife = new KnifeEntity(entity.level, entity.getX(), entity.getY(), entity.getZ());
        knife.yRot = (float)(Math.atan2((double)x, (double)z) * (double)(180F / (float)Math.PI));
        knife.xRot = (float)(Math.atan2((double)y, (double)f) * (double)(180F / (float)Math.PI));
        knife.yRotO = knife.yRot;
        knife.xRotO = knife.xRot;
        dispatcher.render(knife, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, matrixStack, buffer, packedLight);
    }
}
