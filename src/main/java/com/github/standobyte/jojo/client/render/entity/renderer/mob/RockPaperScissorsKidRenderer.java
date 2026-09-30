package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsKidEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.resources.ResourceLocation;

public class RockPaperScissorsKidRenderer extends MobRenderer<RockPaperScissorsKidEntity, VillagerModel<RockPaperScissorsKidEntity>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public RockPaperScissorsKidRenderer(EntityRenderDispatcher manager) {
        super(manager, new VillagerModel<>(0.0F), 0.5F);
        addLayer(new CustomHeadLayer<>(this));
        addLayer(new VillagerProfessionLayer<>(this, (ReloadableResourceManager) Minecraft.getInstance().getResourceManager(), "villager"));
        addLayer(new CrossedArmsItemLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(RockPaperScissorsKidEntity entity) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected void scale(RockPaperScissorsKidEntity entity, PoseStack matrixStack, float partialTick) {
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
