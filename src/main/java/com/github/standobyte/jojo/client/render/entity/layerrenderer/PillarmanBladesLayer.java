package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

public class PillarmanBladesLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> implements IFirstPersonHandLayer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/pillarman_blades.png");
    
    private final PillarmanBladesModel<T> bladesModel;
    public final boolean slim;
    
    public PillarmanBladesLayer(RenderLayerParent<T, M> renderer, boolean slim) {
        super(renderer);
        this.slim = slim;
        this.bladesModel = new PillarmanBladesModel<>(slim);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        if (INonStandPower.getNonStandPowerOptional(entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()))
                .filter(PillarmanData::getBladesVisible).isPresent()) {
            matrixStack.pushPose();
            if (getParentModel().young) {
                matrixStack.translate(0.0D, 0.75D, 0.0D);
                matrixStack.scale(0.5F, 0.5F, 0.5F);
            }

            renderBlade(entity, HumanoidArm.RIGHT, matrixStack, buffer);
            renderBlade(entity, HumanoidArm.LEFT, matrixStack, buffer);
            matrixStack.popPose();
        }
    }

    private void renderBlade(LivingEntity entity, HumanoidArm side, PoseStack matrixStack, MultiBufferSource buffer) {
        matrixStack.pushPose();
        getParentModel().translateToHand(side, matrixStack);
        PlayerAnimationHandler.getPlayerAnimator().onItemLikeLayerRender(matrixStack, entity, side);
        
        boolean enchantGlint = true;
        VertexConsumer vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(TEXTURE), false, enchantGlint);;
        ModelPart blade;
        switch (side) {
        case LEFT:
            blade = bladesModel.bladeLeft;
            break;
        case RIGHT:
            blade = bladesModel.bladeRight;
            break;
        default:
            throw new AssertionError();
        }
        
        blade.render(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        matrixStack.popPose();
    }

    @Override
    public void renderHandFirstPerson(HumanoidArm side, PoseStack matrixStack, MultiBufferSource buffer, int light,
            AbstractClientPlayer player, PlayerRenderer playerRenderer) {
        // FIXME render blades in 1st person
    }
}
