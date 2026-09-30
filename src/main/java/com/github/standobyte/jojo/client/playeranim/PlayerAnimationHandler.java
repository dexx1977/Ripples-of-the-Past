package com.github.standobyte.jojo.client.playeranim;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PlayerAnimationHandler {

    private static IPlayerAnimator instance = null;
    
    public static interface IPlayerAnimator {
        
        boolean kosmXAnimatorInstalled();
        
        public void onRenderFrameStart(float partialTick);
        
        public void onRenderFrameEnd(float partialTick);
        
        BasicToggleAnim registerBasicAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority);
        
        <I> I registerAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority, Supplier<? extends I> fallbackEmptyConstructor);
        
        <T> T getAnimLayer(Class<T> layerInterface, ResourceLocation id);
        
        <T extends LivingEntity, M extends HumanoidModel<T>> void onArmorLayerInit(RenderLayer<T, M> layer);
        
        float[] getBend(HumanoidModel<?> model, BendablePart part);
        
        void setBend(HumanoidModel<?> model, BendablePart part, float axis, float angle);
        
        Vec3 getBodyPos(AbstractClientPlayer player, float partialTick);
        
        <T extends LivingEntity, M extends HumanoidModel<T>> void heldItemLayerRender(LivingEntity livingEntity, PoseStack matrices, HumanoidArm arm);
        
        <T extends LivingEntity, M extends HumanoidModel<T>> void heldItemLayerChangeItemLocation(LivingEntity livingEntity, PoseStack matrices, HumanoidArm arm);
        
        void setupLayerFirstPersonRender(HumanoidModel<?> layerModel);
        
        void onItemLikeLayerRender(PoseStack matrixStack, LivingEntity entity, HumanoidArm side);
        
        @Deprecated
        default void setBarrageAnim(Player player, boolean val) {
            ModPlayerAnimations.playerBarrageAnim.setAnimEnabled(player, val);
        }
    }
    
    
    public static boolean canAnimate(Player player) {
        return !player.isPassenger() && !LivingWallClimbing.getHandler(player).map(wallClimb -> wallClimb.isWallClimbing()).orElse(false);
    }
    
    
    public static class PlayerAnimator implements IPlayerAnimator {
        protected Map<ResourceLocation, Object> animationLayers = new HashMap<>();
        
        @Override
        public boolean kosmXAnimatorInstalled() { return false; }
        
        @Override
        public void onRenderFrameStart(float partialTick) {}
        
        @Override
        public void onRenderFrameEnd(float partialTick) {}

        @Override
        public BasicToggleAnim registerBasicAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority) {
            return registerAnimLayer(classNameWithKosmXMod, id, priority, () -> BasicToggleAnim.NoPlayerAnimator.DUMMY);
        }

        @Override
        public <I> I registerAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority, 
                Supplier<? extends I> fallbackEmptyConstructor) {
            if (animationLayers.containsKey(id)) {
                IllegalArgumentException e = new IllegalArgumentException();
                JojoMod.getLogger().error("An animation layer with id {} is already present!", id, e);
                throw e;
            }
            
            I animationHandler = null;
            if (kosmXAnimatorInstalled()) {
                I instance;
                try {
                    Class<? extends I> animatorClass = (Class<? extends I>) Class.forName(classNameWithKosmXMod);
                    Constructor<? extends I> constructor = animatorClass.getConstructor(ResourceLocation.class);
                    instance = constructor.newInstance(id);
                    animationHandler = instance;
                    registerWithAnimatorMod(animationHandler, id, priority);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                    JojoMod.getLogger().error("Failed to create player animation layer of class " + classNameWithKosmXMod, e);
                }
            }
            if (animationHandler == null) {
                animationHandler = fallbackEmptyConstructor.get();
            }
            
            animationLayers.put(id, animationHandler);
            return animationHandler;
        }
        
        protected void registerWithAnimatorMod(Object animLayer, ResourceLocation id, int priority) {}

        @Override
        public <T> T getAnimLayer(Class<T> layerInterface, ResourceLocation id) {
            Object layer = animationLayers.get(id);
            if (layer == null) {
                JojoMod.getLogger().error("An animation layer with id {} was not registered!", id);
                throw new IllegalArgumentException();
            }
            return (T) layer;
        }
        

        @Override
        public <T extends LivingEntity, M extends HumanoidModel<T>> void onArmorLayerInit(RenderLayer<T, M> layer) {}
        
        
        static final float[] ZERO_BEND = new float[] {0, 0};
        @Override
        public float[] getBend(HumanoidModel<?> model, BendablePart part) { return ZERO_BEND; }
        
        @Override
        public void setBend(HumanoidModel<?> model, BendablePart part, float axis, float angle) {}
        
        @Override
        public Vec3 getBodyPos(AbstractClientPlayer player, float partialTick) {
            return Vec3.ZERO;
        }
        
        @Override
        public <T extends LivingEntity, M extends HumanoidModel<T>> void heldItemLayerRender(
                LivingEntity livingEntity, PoseStack matrices, HumanoidArm arm) {}

        @Override
        public <T extends LivingEntity, M extends HumanoidModel<T>> void heldItemLayerChangeItemLocation(
                LivingEntity livingEntity, PoseStack matrices, HumanoidArm arm) {}

        @Override
        public void setupLayerFirstPersonRender(HumanoidModel<?> layerModel) {}
        
        @Override
        public void onItemLikeLayerRender(PoseStack matrixStack, LivingEntity entity, HumanoidArm side) {}
    }
    
    public static enum BendablePart {
        TORSO,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG
    }
    
    public static IPlayerAnimator getPlayerAnimator() {
        return instance;
    }
    
    
    
    public static void initAnimator() {
        if (instance != null) {
            Exception e = new RedundantAddonCodeException();
            JojoMod.getLogger().error("Player animation interface is already initialized!", e);
            return;
        }
        instance = OptionalDependencyHelper.initModHandlingInterface(
                "playeranimator", 
                "com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled", 
                PlayerAnimator::new, "Player Animator lib");
    }
    
    
    
    private static class RedundantAddonCodeException extends Exception {}
    
}
