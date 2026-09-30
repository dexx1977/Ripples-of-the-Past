package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

public class PillarmanLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> implements IFirstPersonHandLayer {
    public static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/pillarman_layer.png");
    
    public PillarmanLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        if (!entity.isInvisible()) {
            M model = getParentModel();
            ResourceLocation texture = getTexture(model, entity);
            if (texture == null) return;
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
            model.renderToBuffer(matrixStack, vertexBuilder, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    @Nullable
    private ResourceLocation getTexture(EntityModel<?> model, LivingEntity entity) {
        if (INonStandPower.getNonStandPowerOptional(entity).resolve().flatMap(
                power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()))
                .map(PillarmanData::isStoneFormEnabled).orElse(false)) {
            return TEXTURE;
        }
        return null;
    }
    
    @Override
    public void renderHandFirstPerson(HumanoidArm side, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, AbstractClientPlayer player, 
            PlayerRenderer playerRenderer) {
        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
        IFirstPersonHandLayer.defaultRender(side, matrixStack, buffer, light, player, playerRenderer, 
                model, getTexture(model, player));
    }
    
}
