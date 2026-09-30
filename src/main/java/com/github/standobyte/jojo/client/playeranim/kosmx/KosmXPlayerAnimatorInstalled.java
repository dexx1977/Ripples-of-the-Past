package com.github.standobyte.jojo.client.playeranim.kosmx;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler.BendablePart;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.KosmXKeyframeAnimPlayer;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.kosmx.playerAnim.api.AnimUtils;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.Helper;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.IBendHelper;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.impl.IPlayerModel;
import dev.kosmx.playerAnim.impl.IUpperPartHelper;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.math.Axis;

public class KosmXPlayerAnimatorInstalled extends PlayerAnimationHandler.PlayerAnimator {
    private static final List<AnimHandler<? extends IAnimation>> PREVENT_CROUCH = new ArrayList<>();
    public static KosmXPlayerAnimatorInstalled.EventHandler eventHandler;
    
    public KosmXPlayerAnimatorInstalled() {
        super();
        MinecraftForge.EVENT_BUS.register(eventHandler = new KosmXPlayerAnimatorInstalled.EventHandler());
    }
    
    @Override
    public boolean kosmXAnimatorInstalled() {
        return true;
    }
    
    @Override
    public void onRenderFrameStart(float partialTick) {
    }
    
    @Override
    public void onRenderFrameEnd(float partialTick) {
    }
    
    @Override
    protected void registerWithAnimatorMod(Object animLayer, ResourceLocation id, int priority) {
        register((AnimHandler<?>) animLayer, id, priority);
    }
    
    private static <A extends IAnimation, T extends AnimHandler<A>> void register(T animHandler, ResourceLocation id, int priority) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(id, priority, player -> animHandler.createAnimLayer(player));
        if (animHandler.isForgeEventHandler()) {
            MinecraftForge.EVENT_BUS.register(animHandler);
        }
        if (animHandler.preventsCrouch()) {
            PREVENT_CROUCH.add(animHandler);
        }
    }
    
    
    public static class EventHandler {
        private PlayerModel<?> modelPreventedCrouch;
        
        @SubscribeEvent
        public void preRender(RenderPlayerEvent.Pre event) {
            modelPreventedCrouch = null;
            
            PlayerModel<?> model = event.getRenderer().getModel();
            AbstractClientPlayer player = (AbstractClientPlayer) event.getPlayer();
            for (AnimHandler<?> animHandler : PREVENT_CROUCH) {
                IAnimation animLayer = animHandler.getAnimLayer(player);
                if (animLayer != null && animLayer.isActive()) {
                    model.crouching = false;
                    modelPreventedCrouch = model;
                    break;
                }
            }
        }
        
        public void removeAttackAnim() {
            if (modelPreventedCrouch != null) {
                modelPreventedCrouch.attackTime = 0;
            }
        }
    }
    
    
    @Override
    public float[] getBend(HumanoidModel<?> model, BendablePart part) {
        if (Helper.isBendEnabled() && model instanceof IMutableModel) {
            IMutableModel bendyModel = (IMutableModel) model;
            AnimationProcessor anim = bendyModel.getEmoteSupplier().get();
            if (anim != null && anim.isActive()) {
                IBendHelper mutablePart = getMutablePart(bendyModel, part);
                if (mutablePart != null) {
                    return KosmXBendyLibHelper.getBend(mutablePart);
                }
            }
        }
        return super.getBend(model, part);
    }
    
    @Override
    public void setBend(HumanoidModel<?> model, BendablePart part, float axis, float angle) {
        if (Helper.isBendEnabled() && model instanceof IMutableModel) {
            IBendHelper mutablePart = getMutablePart((IMutableModel) model, part);
            if (mutablePart != null) {
                mutablePart.bend(axis, angle);
            }
        }
    }
    
    private IBendHelper getMutablePart(IMutableModel model, BendablePart neededPart) {
        switch (neededPart) {
        case TORSO:
            return model.getTorso();
        case LEFT_ARM:
            return model.getLeftArm();
        case RIGHT_ARM:
            return model.getRightArm();
        case LEFT_LEG:
            return model.getLeftLeg();
        case RIGHT_LEG:
            return model.getRightLeg();
        }
        return null;
    }
    
    @Override
    public Vec3 getBodyPos(AbstractClientPlayer player, float partialTick) {
        PlayerRenderer renderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<?> model = renderer.getModel();
        if (model instanceof IMutableModel) {
            AnimationProcessor anim = ((IMutableModel) model).getEmoteSupplier().get();
            if (anim != null && anim.isActive()) {
                Vec3f pos = anim.get3DTransform("body", ItemDisplayContext.POSITION, Vec3f.ZERO);
                float yRot = Mth.clamp(partialTick, player.yBodyRotO, player.yBodyRot);
                yRot = -yRot * MathUtil.DEG_TO_RAD;
                return new Vec3(-pos.getX(), -pos.getY(), -pos.getZ()).yRot(yRot);
            }
        }
        return Vec3.ZERO;
    }
    
    
    @Override
    public <T extends LivingEntity, M extends HumanoidModel<T>> void onArmorLayerInit(RenderLayer<T, M> layer) {
        ((IUpperPartHelper) layer).setUpperPart(false);
    }
    
    
    @Override
    public <T extends LivingEntity, M extends HumanoidModel<T>> void heldItemLayerRender(
            LivingEntity livingEntity, PoseStack matrices, HumanoidArm arm) {
        if(Helper.isBendEnabled() && livingEntity instanceof IAnimatedPlayer){
            IAnimatedPlayer player = (IAnimatedPlayer) livingEntity;
            if(player.playerAnimator_getAnimation().isActive()){
                AnimationProcessor anim = player.playerAnimator_getAnimation();

                Vec3f data = anim.get3DTransform(arm == HumanoidArm.LEFT ? "leftArm" : "rightArm", ItemDisplayContext.BEND, new Vec3f(0f, 0f, 0f));

                Pair<Float, Float> pair = new Pair<>(data.getX(), data.getY());

                float offset = 0.25f;
                matrices.translate(0, offset, 0);
                float bend = pair.getRight();
                float axisf = - pair.getLeft();
                Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
                //return this.setRotation(axis.getRadialQuaternion(bend));
                matrices.mulPose(axis.rotation(bend));
                matrices.translate(0, - offset, 0);

            }
        }
    }
    
    @Override
    public <T extends LivingEntity, M extends HumanoidModel<T>> void heldItemLayerChangeItemLocation(
            LivingEntity livingEntity, PoseStack matrices, HumanoidArm arm) {
        if (livingEntity instanceof IAnimatedPlayer) {
            IAnimatedPlayer player = (IAnimatedPlayer) livingEntity;
            if (player.playerAnimator_getAnimation().isActive()) {
                AnimationProcessor anim = player.playerAnimator_getAnimation();

                Vec3f rot = anim.get3DTransform(arm == HumanoidArm.LEFT ? "leftItem" : "rightItem", ItemDisplayContext.ROTATION, Vec3f.ZERO);
                Vec3f pos = anim.get3DTransform(arm == HumanoidArm.LEFT ? "leftItem" : "rightItem", ItemDisplayContext.POSITION, Vec3f.ZERO).scale(1/16f);

                matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                matrices.mulPose(Axis.ZP.rotation(rot.getZ()));    //roll
                matrices.mulPose(Axis.YP.rotation(rot.getY()));    //pitch
                matrices.mulPose(Axis.XP.rotation(rot.getX()));    //yaw
            }
        }
    }
    
    @Override
    public void setupLayerFirstPersonRender(HumanoidModel<?> layerModel) {
        if (layerModel instanceof IPlayerModel && AnimUtils.disableFirstPersonAnim) {
            ((IPlayerModel) layerModel).playerAnimator_prepForFirstPersonRender();
        }
    }
    
    @Override
    public void onItemLikeLayerRender(PoseStack matrixStack, LivingEntity entity, HumanoidArm side) {
        if (Helper.isBendEnabled() && entity instanceof IAnimatedPlayer) {
            IAnimatedPlayer player = (IAnimatedPlayer) entity;
            if (player.playerAnimator_getAnimation().isActive()) {
                AnimationProcessor anim = player.playerAnimator_getAnimation();

                Vec3f data = anim.get3DTransform(side == HumanoidArm.LEFT ? "leftArm" : "rightArm", ItemDisplayContext.BEND, new Vec3f(0f, 0f, 0f));

                Pair<Float, Float> pair = new Pair<>(data.getX(), data.getY());

                float offset = 0.25f;
                matrixStack.translate(0, offset, 0);
                float bend = pair.getRight();
                float axisf = - pair.getLeft();
                Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
                matrixStack.mulPose(axis.rotation(bend));
                matrixStack.translate(0, - offset, 0);

            }
        }
    }
    
    
    
    public static abstract class AnimHandler<T extends IAnimation> {
        private final ResourceLocation id;
        
        public AnimHandler(ResourceLocation id) {
            this.id = id;
        }
        
        protected abstract T createAnimLayer(AbstractClientPlayer player);
        
        public boolean isForgeEventHandler() {
            return false;
        }
        
        public boolean preventsCrouch() {
            return true;
        }
        
        public ResourceLocation getId() {
            return id;
        }
        
        @SuppressWarnings("unchecked")
        @Nullable
        protected final T getAnimLayer(AbstractClientPlayer player) {
            return (T) PlayerAnimationAccess.getPlayerAssociatedData(player).get(id);
        }
    }
    
    public static abstract class AnimLayerHandler<T extends ModifierLayer<IAnimation>> extends AnimHandler<T> {

        public AnimLayerHandler(ResourceLocation id) {
            super(id);
        }
        
        protected boolean setAnimFromName(Player player, ResourceLocation name) {
            return setAnimFromName(player, name, KosmXKeyframeAnimPlayer::new);
        }
        
        protected boolean setAnimFromName(Player player, ResourceLocation name, Function<KeyframeAnimation, IAnimation> createAnimPlayer) {
            IAnimation anim = getAnimFromName(name, createAnimPlayer);
            if (anim == null) {
                return false;
            }
            return setAnim(player, anim);
        }
        
        protected boolean setAnim(Player player, IAnimation anim) {
            if (player == null) return false;
            ModifierLayer<IAnimation> animLayer = getAnimLayer((AbstractClientPlayer) player);
            if (animLayer == null) return false;
            animLayer.setAnimation(anim);
            return true;
        }
        
        @Nullable
        protected IAnimation getAnimFromName(ResourceLocation name) {
            return getAnimFromName(name, KosmXKeyframeAnimPlayer::new);
        }

        @Nullable
        protected IAnimation getAnimFromName(ResourceLocation name, Function<KeyframeAnimation, IAnimation> createAnimPlayer) {
            if (name == null) return null;
            KeyframeAnimation keyframes = PlayerAnimationRegistry.getAnimation(name);
            if (keyframes == null) return null;
            return createAnimPlayer.apply(keyframes);
        }
        
        @Deprecated
        protected boolean fadeOutAnim(Player player, @Nullable AbstractFadeModifier fadeModifier, 
                @Nullable IAnimation newAnimation) {
            return fadeOutAnim(player, null, newAnimation);
        }
        
        protected boolean fadeOutAnim(Player player, @Nullable KosmXFixedFadeModifier fadeModifier, 
                @Nullable IAnimation newAnimation) {
            if (player == null) return false;
            ModifierLayer<IAnimation> animLayer = getAnimLayer((AbstractClientPlayer) player);
            if (animLayer != null) {
                if (fadeModifier != null) {
                    boolean fadeInFromNothing = true;
                    animLayer.replaceAnimationWithFade(fadeModifier, newAnimation, fadeInFromNothing);
                }
                else {
                    animLayer.setAnimation(newAnimation);
                }
                return true;
            }
            return false;
        }
        
    }
}
