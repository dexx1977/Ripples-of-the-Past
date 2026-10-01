package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.HGStringModel;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public abstract class HGStringAbstractRenderer<T extends OwnerBoundProjectileEntity> extends ExtendingEntityRenderer<T, HGStringModel<T>> {
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hg_string_glow.png");

    public HGStringAbstractRenderer(EntityRendererProvider.Context context, HGStringModel<T> model) {
        super(context, model, new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hg_string.png"));
    }
    
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(entity.getStandSkin()), texPath);
    }
    
    @Override
    protected void doRender(T entity, HGStringModel<T> model, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (entity.tickCount % 20 == 0) {
            System.out.println("[DBG-HG] doRender id=" + entity.getId() + " pos=" + entity.position() + " owner=" + entity.getOwner() + " invisible=" + entity.isInvisible() + " alpha=" + getAlpha(entity, partialTick) + " skin=" + entity.getStandSkin());
        }
        super.doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
        ResourceLocation glowTexture = StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(entity.getStandSkin()), GLOW_TEXTURE);
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(model.renderType(glowTexture)), ClientUtil.MAX_MODEL_LIGHT);
    }
}
