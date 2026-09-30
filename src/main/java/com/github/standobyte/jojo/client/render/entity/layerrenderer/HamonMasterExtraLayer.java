package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HamonMasterExtraLayer extends RenderLayer<HamonMasterEntity, HamonMasterModel> {
    private final ResourceLocation texture;
    private HamonMasterModel model = new HamonMasterModel(true);

    public HamonMasterExtraLayer(RenderLayerParent<HamonMasterEntity, HamonMasterModel> renderer, ResourceLocation texture) {
        super(renderer);
        this.texture = texture;
    }
    
    protected ResourceLocation getTextureLocation(HamonMasterEntity pEntity) {
        return texture;
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight,
            HamonMasterEntity entity, float limbSwing, float limbSwingAmount, float partialTick,
            float ticks, float yRot, float xRot) {
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        getParentModel().copyPropertiesTo(model);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ticks, yRot, xRot);

        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

}
