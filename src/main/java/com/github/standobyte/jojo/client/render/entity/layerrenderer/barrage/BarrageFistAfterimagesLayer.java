package com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage;

import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

public class BarrageFistAfterimagesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private IPlayerBarrageAnimation barrageAnim = null;
    private final PlayerModel<AbstractClientPlayer> model;
    private final PlayerModel<AbstractClientPlayer> modelSlim;

    public BarrageFistAfterimagesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        this.model = new PlayerModel<>(0.0F, false);
        this.modelSlim = new PlayerModel<>(0.0F, true);
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            AbstractClientPlayer entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        if (lazyInitBarrageAnim() != null) {
            if (isBarraging(entity)) {
                barrageAnim.addSwings(entity, entity.getMainArm(), ticks);
            }

            Minecraft mc = Minecraft.getInstance();
            BarrageSwingsHolder<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> swings = getSwings(entity);
            
            if (swings != null && swings.hasSwings()) {
                boolean visible = !entity.isInvisible();
                boolean spectatorVisibility = !visible && !entity.isInvisibleTo(mc.player);
                boolean glowing = mc.shouldEntityAppearGlowing(entity);
                RenderType renderType = null;
                ResourceLocation texture = getTextureLocation(entity);
                if (spectatorVisibility) {
                    renderType = RenderType.itemEntityTranslucentCull(texture);
                } else if (visible) {
                    renderType = model.renderType(texture);
                } else if (glowing) {
                    renderType = RenderType.outline(texture);
                }
                if (renderType == null) return;
                
                PlayerModel<AbstractClientPlayer> model = getModel(entity);
                PlayerModel<AbstractClientPlayer> parentModel = getParentModel();
                parentModel.copyPropertiesTo(model);
                model.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
                model.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, headYRotation, headXRotation);
                
                matrixStack.pushPose();
                barrageAnim.beforeSwingsRender(matrixStack, this);
                
                VertexConsumer vertexBuilder = buffer.getBuffer(renderType);
                swings.renderBarrageSwings(model, entity, matrixStack, vertexBuilder, 
                        packedLight, OverlayTexture.NO_OVERLAY, 
                        headYRotation * MathUtil.DEG_TO_RAD, headXRotation * MathUtil.DEG_TO_RAD, 
                        1.0F, 1.0F, 1.0F, spectatorVisibility ? 0.15F : 1.0F);
                
                matrixStack.popPose();
                
                swings.updateSwings(mc);
            }
        }
    }
    
    private PlayerModel<AbstractClientPlayer> getModel(AbstractClientPlayer entity) {
        boolean slim = "slim".equals(entity.getModelName());
        return slim ? modelSlim : model;
    }
    
    private IPlayerBarrageAnimation lazyInitBarrageAnim() {
        if (barrageAnim == null) {
            barrageAnim = ModPlayerAnimations.playerBarrageAnim.createBarrageAfterimagesAnim(model, this);
        }
        return barrageAnim;
    }
    
    public void setArmsVisibility(PlayerModel<AbstractClientPlayer> model, HumanoidArm punchingHand) {
        setVisibility(model.head, false);
        setVisibility(model.hat, false);
        setVisibility(model.body, false);
        setVisibility(model.jacket, false);
        setVisibility(model.leftLeg, false);
        setVisibility(model.leftPants, false);
        setVisibility(model.rightLeg, false);
        setVisibility(model.rightPants, false);
        boolean rightSide = punchingHand == HumanoidArm.RIGHT;
        setVisibility(model.leftArm, !rightSide);
        setVisibility(model.leftSleeve, false);
        setVisibility(model.rightArm, rightSide);
        setVisibility(model.rightSleeve, false);
    }
    
    private void setVisibility(net.minecraft.client.model.geom.ModelPart part, boolean visible) {
        part.visible = visible;
    }
    
    public static BarrageSwingsHolder<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> getSwings(AbstractClientPlayer player) {
        return player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getBarrageSwings()).orElse(null);
    }
    
    public static void setIsBarraging(Player player, boolean isBarraging) {
        player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setIsBarraging(isBarraging));
    }
    
    private static boolean isBarraging(Player player) {
        return player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(cap -> cap.isBarraging()).orElse(false);
    }

}
