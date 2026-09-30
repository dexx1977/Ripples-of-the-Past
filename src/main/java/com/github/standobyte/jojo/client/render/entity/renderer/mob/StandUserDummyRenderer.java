package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.text.DecimalFormat;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.StandUserDummyEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.network.chat.Component;

public class StandUserDummyRenderer extends HumanoidMobRenderer<StandUserDummyEntity, PlayerModel<StandUserDummyEntity>> {

    public StandUserDummyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, 
                new HumanoidModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_INNER_ARMOR)), 
                new HumanoidModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_OUTER_ARMOR)), 
                Minecraft.getInstance().getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }
    
    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(StandUserDummyEntity entity) {
        // the dummy is rendered like a player without a skin, as the 1.16.5 biped
        // renderer's default did
        return net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
    }

    @Override
    public void render(StandUserDummyEntity pEntity, float pEntityYaw, float pPartialTicks, 
            PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        pMatrixStack.pushPose();
        
        if (Minecraft.renderNames() && pEntity == this.entityRenderDispatcher.crosshairPickEntity) {
            IStandPower stand = pEntity.getStandPower();
            if (stand.hasPower()) {
                
                pMatrixStack.translate(0, 0.25, 0);
                float staminaRatio = stand.getStamina() / stand.getMaxStamina();
                float staminaCondition = 0.25F + Math.min(staminaRatio * 1.5F, 0.75F);
                int color = ClientUtil.fromRgb(1 - staminaCondition, staminaCondition, 0f);
                this.renderNameTag(pEntity, 
                        Component.translatable("Stamina: %s", 
                                Component.literal(String.format("%.2f%%", staminaRatio * 100)).withStyle(ClientUtil.textColor(color))), 
                        pMatrixStack, pBuffer, pPackedLight);
                
//                // doesn't sync
//                pMatrixStack.translate(0, 0.25, 0);
//                float resolveRatio = stand.getResolve() / stand.getMaxResolve();
//                this.renderNameTag(pEntity, 
//                        Component.literal(String.format("Resolve: %.2f%% (level %d)", resolveRatio * 100, stand.getResolveLevel())), 
//                        pMatrixStack, pBuffer, pPackedLight);
                
                pMatrixStack.translate(0, 0.25, 0);
                this.renderNameTag(pEntity, 
                        stand.getName(), 
                        pMatrixStack, pBuffer, pPackedLight);
            }
        }
        
        DecimalFormat format = new DecimalFormat("#.##");
        pMatrixStack.translate(0, 0.25, 0);
        String hp = format.format(pEntity.getHealth());
        String maxHp = format.format(pEntity.getMaxHealth());
        this.renderNameTag(pEntity, 
                Component.literal("❤ " + hp + "/" + maxHp), 
                pMatrixStack, pBuffer, pPackedLight);
        
        pMatrixStack.popPose();
    }
}
