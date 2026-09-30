package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.player.PlayerClientBroadcastedSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

public class SRSEEyesLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> {
	public static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/srse_eyes.png");
    public SRSEEyesLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        PlayerAnimationHandler.getPlayerAnimator().onArmorLayerInit(this);
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            T player, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
    	if (!player.isInvisible()) {
    	    boolean eyesEnabled = INonStandPower.getNonStandPowerOptional(player).resolve().filter(power -> {
    	        return power.getHeldAction() == ModVampirismActions.VAMPIRISM_SPACE_RIPPER_STINGY_EYES.get();
    	    }).isPresent();
    	    if (eyesEnabled && player instanceof Player) {
    	        Optional<PlayerClientBroadcastedSettings> settings = PlayerClientBroadcastedSettings.getPlayerSettings((Player) player);
    	        eyesEnabled = settings.map(s -> s.vampireGlowingEyes).orElse(true);
    	    }
    	    if (!eyesEnabled) return;
    	    
            M model = getParentModel();
            ResourceLocation texture = getTexture(model, player);
            if (texture == null) return;
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
            model.renderToBuffer(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, LivingEntityRenderer.getOverlayCoords(player, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    @Nullable
    private ResourceLocation getTexture(EntityModel<?> model, LivingEntity entity) {
        return TEXTURE;
    } 
}
