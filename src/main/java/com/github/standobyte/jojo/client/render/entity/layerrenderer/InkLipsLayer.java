package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

public class InkLipsLayer<T extends Player, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/ink_lips.png");
    public InkLipsLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            T player, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        if (!player.isInvisible()) {
            int atePastaTicks = player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getInkPastaVisuals()).orElse(0);
            if (atePastaTicks > 0) {
                M model = getParentModel();
                float alpha = atePastaTicks > 200 ? 1 : (float) atePastaTicks / 200;
                VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
                model.renderToBuffer(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, LivingEntityRenderer.getOverlayCoords(player, 0.0F), 1.0F, 1.0F, 1.0F, alpha);
            }
        }
    }
    
}
