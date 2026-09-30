package com.github.standobyte.jojo.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.render.entity.layerrenderer.IFirstPersonHandLayer;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends LivingRendererMixin<T, M> {
    private final List<IFirstPersonHandLayer> jojoFirstPersonHandLayers = new ArrayList<>();
    
    @Override
    public void jojoOnAddLayer(RenderLayer<T, M> layer, CallbackInfoReturnable<Boolean> ci) {
        if (layer instanceof IFirstPersonHandLayer) {
            jojoFirstPersonHandLayers.add((IFirstPersonHandLayer) layer);
        }
    }

    @Inject(method = "renderRightHand", at = @At("TAIL"))
    public void jojoOnRenderHand(PoseStack matrixStack, MultiBufferSource buffer, int light, 
            AbstractClientPlayer player, CallbackInfo ci) {
        for (IFirstPersonHandLayer layer : jojoFirstPersonHandLayers) {
            layer.renderHandFirstPerson(HumanoidArm.RIGHT, matrixStack, buffer, light, player, (PlayerRenderer) (Object) this);
        }
    }

    @Inject(method = "renderLeftHand", at = @At("TAIL"))
    public void jojoOnRenderLeftHand(PoseStack matrixStack, MultiBufferSource buffer, int light, 
            AbstractClientPlayer player, CallbackInfo ci) {
        for (IFirstPersonHandLayer layer : jojoFirstPersonHandLayers) {
            layer.renderHandFirstPerson(HumanoidArm.LEFT, matrixStack, buffer, light, player, (PlayerRenderer) (Object) this);
        }
    }
    
    
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void jojoHoldItemArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> ci) {
        ItemStack item = player.getItemInHand(hand);
        if (!player.swinging && !item.isEmpty() && item.getItem() == ModItems.TOMMY_GUN.get()) {
            ci.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }
    
}
