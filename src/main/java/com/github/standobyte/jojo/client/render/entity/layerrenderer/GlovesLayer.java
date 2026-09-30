package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.item.GlovesItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import com.github.standobyte.jojo.util.mc.MCUtil;

public class GlovesLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> implements IFirstPersonHandLayer {
    private final M glovesModel;
    private final boolean slim;
    
    public GlovesLayer(RenderLayerParent<T, M> renderer, M glovesModel, boolean slim) {
        super(renderer);
        this.glovesModel = glovesModel;
        this.slim = slim;
        PlayerAnimationHandler.getPlayerAnimator().onArmorLayerInit(this);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        ItemStack glovesItemStack = getRenderedGlovesItem(entity);
        if (!glovesItemStack.isEmpty()) {
            GlovesItem gloves = (GlovesItem) glovesItemStack.getItem();
            M playerModel = getParentModel();
            glovesModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            playerModel.copyPropertiesTo(glovesModel);
            glovesModel.setupAnim(entity, limbSwing, limbSwingAmount, ticks, yRot, xRot);
            
            glovesModel.leftArm.visible = playerModel.leftArm.visible;
            glovesModel.leftSleeve.visible = playerModel.leftArm.visible;
            glovesModel.rightArm.visible = playerModel.rightArm.visible;
            glovesModel.rightSleeve.visible = playerModel.rightArm.visible;
            ResourceLocation texture = getTexture(gloves);
            VertexConsumer vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, glovesItemStack.hasFoil());
            glovesModel.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        }
    }
    
    @Override
    public void renderHandFirstPerson(HumanoidArm side, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, AbstractClientPlayer player, 
            PlayerRenderer playerRenderer) {
        ItemStack glovesItemStack = getRenderedGlovesItem(player);
        if (glovesItemStack.isEmpty()) return;
        GlovesItem glovesItem = (GlovesItem) glovesItemStack.getItem();
        PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>) glovesModel;
        ResourceLocation texture = getTexture(glovesItem);
        
        ClientUtil.setupForFirstPersonRender(model, player);
        VertexConsumer vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, glovesItemStack.hasFoil());
        ModelPart glove = ClientUtil.getArm(model, side);
        ModelPart gloveOuter = ClientUtil.getArmOuter(model, side);
        glove.xRot = 0.0F;
        glove.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY);
        gloveOuter.xRot = 0.0F;
        gloveOuter.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY);
    }
    
    private ResourceLocation getTexture(GlovesItem gloves) {
        return new ResourceLocation(
                MCUtil.id(gloves).getNamespace(), 
                "textures/entity/layer/" + MCUtil.id(gloves).getPath() + (slim ? "_slim" : "") + ".png");
    }
    
    
    
    // if the returned stack isn't empty, the return result's (ItemStack#getItem() instanceof GlovesItem) is guaranteed to be true
    public static ItemStack getRenderedGlovesItem(LivingEntity entity) {
        ItemStack checkedItem = entity.getMainHandItem();
        if (areGloves(checkedItem)) return checkedItem;
        checkedItem = entity.getOffhandItem();
        if (areGloves(checkedItem)) return checkedItem;
        return ItemStack.EMPTY;
    }
    
    public static boolean areGloves(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof GlovesItem;
    }
}