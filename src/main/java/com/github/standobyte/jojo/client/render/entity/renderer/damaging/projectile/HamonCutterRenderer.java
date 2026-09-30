package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.projectile.HamonCutterModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonCutterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HamonCutterRenderer extends SimpleEntityRenderer<HamonCutterEntity, HamonCutterModel> {

    public HamonCutterRenderer(EntityRendererProvider.Context context) {
        super(context, new HamonCutterModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hamon_cutter.png"));
    }
    
    @Override
    protected void renderModel(HamonCutterEntity entity, HamonCutterModel model, float partialTick, 
            PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight) {
        float[] rgb = ClientUtil.rgb(entity.getColor());
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 
                rgb[0], rgb[1], rgb[2], 1.0F);
    }
}
