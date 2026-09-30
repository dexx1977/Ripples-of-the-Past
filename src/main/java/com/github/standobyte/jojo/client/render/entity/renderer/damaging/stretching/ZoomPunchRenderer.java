package com.github.standobyte.jojo.client.render.entity.renderer.damaging.stretching;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.ZoomPunchModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

public class ZoomPunchRenderer extends StretchingEntityRenderer<ZoomPunchEntity, ZoomPunchModel> {

    public ZoomPunchRenderer(EntityRendererProvider.Context context) {
        super(context, new ZoomPunchModel(0.0F), DefaultPlayerSkin.getDefaultSkin());
    }
    
    protected float getModelLength() {
        return 12F;
    }
    
    protected float getModelRotationPointOffset() {
        return 2F;
    } 

    @Override
    public ResourceLocation getTextureLocation(ZoomPunchEntity entity) {
        Entity owner = entity.getOwner();
        return owner != null ? entityRenderDispatcher.getRenderer(owner).getTextureLocation(owner) : 
            Minecraft.getInstance().player.getSkinTextureLocation();
    }

    @Override
    public void render(ZoomPunchEntity entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            boolean isPlayer = owner.getType() == EntityType.PLAYER;
            boolean slimPlayerModel = isPlayer && "slim".equals(((AbstractClientPlayer) owner).getModelName());
            getEntityModel().setVisibility(entity.getSide() == HumanoidArm.LEFT, isPlayer, slimPlayerModel);
        }
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }
}
