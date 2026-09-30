package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.HumanoidModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

public class LadybugBroochLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final LadybugBroochesModel<T> broochesModel;
    
    public LadybugBroochLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.broochesModel = new LadybugBroochesModel<>();
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(brooches -> {
            this.getParentModel().copyPropertiesTo(broochesModel);
            renderSingleBrooch(matrixStack, buffer, packedLight, entity, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot, 
                    brooches.getBroochWorn(0), broochesModel.broochLeft);
            renderSingleBrooch(matrixStack, buffer, packedLight, entity, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot, 
                    brooches.getBroochWorn(1), broochesModel.broochRight);
            renderSingleBrooch(matrixStack, buffer, packedLight, entity, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot, 
                    brooches.getBroochWorn(2), broochesModel.broochBottom);
        });
    }
    
    private void renderSingleBrooch(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot,
            DyeColor color, ModelPart broochPart) {
        if (color != null) {
            broochesModel.broochLeft.visible = false;
            broochesModel.broochRight.visible = false;
            broochesModel.broochBottom.visible = false;
            broochPart.visible = true;
            ResourceLocation texture = TEXTURE_BY_COLOR.get(color);
            VertexConsumer vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, false);
            broochesModel.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    private static final Map<DyeColor, ResourceLocation> TEXTURE_BY_COLOR = Util.make(new EnumMap<>(DyeColor.class), map -> {
        for (DyeColor dye : DyeColor.values()) {
            map.put(dye, new ResourceLocation("jojo_clothes", "textures/misc/brooch/ladybug_" + dye.getName() + ".png"));
        }
    });
}
