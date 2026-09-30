package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

//@OnlyIn(Dist.CLIENT)
public class WindCloakLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> implements IFirstPersonHandLayer {
    public static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/wind_cloak.png");
    private final PlayerModel<T> model = new PlayerModel<>(0.50F, false);
    
    public WindCloakLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        PlayerAnimationHandler.getPlayerAnimator().onArmorLayerInit(this);
    }
    
    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, 
    		T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
    	if (INonStandPower.getNonStandPowerOptional(pLivingEntity)
                .map(power -> power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_WIND_CLOAK.get())
                .orElse(false)) {
	    	float f = (float)pLivingEntity.tickCount + pPartialTicks;
	        PlayerModel<T> entitymodel = model;
	        entitymodel.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
	        this.getParentModel().copyPropertiesTo(entitymodel);
	        VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.energySwirl(TEXTURE, this.xOffset(f), f * 0.01F));
	        entitymodel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
	        entitymodel.renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 0.2F, 0.2F, 0.2F, 0.25F);
    	}
    }
    
    @Override
    public void renderHandFirstPerson(HumanoidArm side, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, AbstractClientPlayer player, 
            PlayerRenderer playerRenderer) {
        if (!player.isSpectator() && INonStandPower.getNonStandPowerOptional(player)
                .map(power -> power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_WIND_CLOAK.get())
                .orElse(false)) {
            float partialTick = ClientUtil.getPartialTick();
            float f = (float)player.tickCount + partialTick;
            PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
            ClientUtil.setupForFirstPersonRender(model, player);
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.energySwirl(TEXTURE, this.xOffset(f), f * 0.01F));
            ModelPart arm = ClientUtil.getArm(model, side);
            ModelPart armOuter = ClientUtil.getArmOuter(model, side);
            arm.xRot = 0.0F;
            arm.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 0.2F, 0.2F, 0.2F, 0.25F);
            armOuter.xRot = 0.0F;
            armOuter.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 0.2F, 0.2F, 0.2F, 0.25F);
        }
    }
    
    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.01F;
     }
}

