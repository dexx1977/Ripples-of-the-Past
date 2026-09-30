package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsKidEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;

public class RockPaperScissorsKidRenderer extends MobRenderer<RockPaperScissorsKidEntity, VillagerModel<RockPaperScissorsKidEntity>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public RockPaperScissorsKidRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        addLayer(new VillagerProfessionLayer<>(this, Minecraft.getInstance().getResourceManager(), "villager"));
        addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(RockPaperScissorsKidEntity entity) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    public void scale(RockPaperScissorsKidEntity entity, PoseStack matrixStack, float partialTick) { // LivingEntityRenderer#scale is public in 1.20.1
        float f = 0.9375F;
        if (entity.isBaby()) {
            f = (float)((double)f * 0.5D);
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        matrixStack.scale(f, f, f);
    }
}
