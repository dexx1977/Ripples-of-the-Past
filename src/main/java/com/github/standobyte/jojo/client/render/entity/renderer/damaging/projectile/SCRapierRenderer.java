package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.projectile.SCRapierFlameModel;
import com.github.standobyte.jojo.client.render.entity.model.projectile.SCRapierModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SCRapierRenderer extends SimpleEntityRenderer<SCRapierEntity, SCRapierModel> {
    private final SCRapierFlameModel flameModel;

    public SCRapierRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new SCRapierModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/silver_chariot.png"));
        this.flameModel = new SCRapierFlameModel();
    }
    
    @Override
    public ResourceLocation getTextureLocation(SCRapierEntity entity) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(entity.getStandSkin()), texPath);
    }

    
    protected void rotateModel(SCRapierModel model, SCRapierEntity entity, float partialTick, float yRotation, float xRotation, PoseStack matrixStack) {
        super.rotateModel(model, entity, partialTick, yRotation, xRotation, matrixStack);
        flameModel.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    @Override
    protected void doRender(SCRapierEntity entity, SCRapierModel model, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        super.doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
        if (entity.isOnFire()) {
            flameModel.renderToBuffer(matrixStack, buffer.getBuffer(flameModel.renderType(null)), 
                    ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

}
