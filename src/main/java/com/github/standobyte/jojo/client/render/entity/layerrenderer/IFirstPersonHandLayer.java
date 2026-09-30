package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

// TODO render the layers in 1st person when the player is invisible
public interface IFirstPersonHandLayer {
    void renderHandFirstPerson(HumanoidArm side, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, AbstractClientPlayer player, 
            PlayerRenderer playerRenderer);
    
    static void defaultRender(HumanoidArm side, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, AbstractClientPlayer player, 
            PlayerRenderer playerRenderer, 
            PlayerModel<AbstractClientPlayer> model, ResourceLocation texture) {
        defaultRender(side, matrixStack, buffer, light, player, playerRenderer, model, texture,
                1, 1, 1, 1);
    }
    
    static void defaultRender(HumanoidArm side, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, AbstractClientPlayer player, 
            PlayerRenderer playerRenderer, 
            PlayerModel<AbstractClientPlayer> model, ResourceLocation texture,
            float red, float green, float blue, float alpha) {
        if (texture == null || player.isSpectator()) return;
        ClientUtil.setupForFirstPersonRender(model, player);
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
        ModelPart arm = ClientUtil.getArm(model, side);
        ModelPart armOuter = ClientUtil.getArmOuter(model, side);
        arm.xRot = 0.0F;
        arm.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        armOuter.xRot = 0.0F;
        armOuter.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }
}
