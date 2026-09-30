package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import dev.kosmx.playerAnim.api.TransformType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WallClimbAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedMirrorModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHeadRotationModifier;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.EnergyRippleLayer;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClSyncMotionAnimPacket;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.general.MathUtil;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KosmXWallClimbHandler extends AnimLayerHandler<KosmXWallClimbHandler.PerPlayerModifiersLayer<IAnimation>> implements WallClimbAnim {
    private Map<UUID, KosmXWallClimbKeyframePlayer> animStuff = new HashMap<>();
    
    public KosmXWallClimbHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected PerPlayerModifiersLayer<IAnimation> createAnimLayer(AbstractClientPlayer player) {
        PerPlayerModifiersLayer<IAnimation> anim = new PerPlayerModifiersLayer<>();
        anim.addModifierLast(new KosmXHeadRotationModifier());
        return anim;
    }
    
    
    static class PerPlayerModifiersLayer<T extends IAnimation> extends ModifierLayer<T> {
        public final SpeedModifier speed;
        public final MirrorModifier mirror;
        
        PerPlayerModifiersLayer() {
            this.speed = new SpeedModifier(1);
            this.mirror = new KosmXFixedMirrorModifier() {
                
                @Override
                public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
                    if (isEnabled() && "head".equals(modelName)) {
                        value0 = transformVector(value0, type);
                        value0 = transformVector(value0, type);
                    }
                    return super.get3DTransform(modelName, type, tickDelta, value0);
                }
            };
            mirror.setEnabled(false);
            addModifierLast(speed);
            addModifierLast(mirror);
        }
    }
    
    
    private static final ResourceLocation CLIMB_UP = new ResourceLocation("jojo", "wall_climb_up");
    private static final ResourceLocation CLIMB_DOWN = new ResourceLocation("jojo", "wall_climb_down");
    private static final ResourceLocation CLIMB_LEFT = new ResourceLocation("jojo", "wall_climb_left");
    private static final ResourceLocation CLIMB_RIGHT = new ResourceLocation("jojo", "wall_climb_right");
    @Override
    public boolean setAnimEnabled(Player player, boolean enabled) {
        if (enabled) {
            KeyframeAnimation up = PlayerAnimationRegistry.getAnimation(CLIMB_UP);
            KeyframeAnimation down = PlayerAnimationRegistry.getAnimation(CLIMB_DOWN);
            KeyframeAnimation left = PlayerAnimationRegistry.getAnimation(CLIMB_LEFT);
            KeyframeAnimation right = PlayerAnimationRegistry.getAnimation(CLIMB_RIGHT);
            if (up == null || down == null || left == null || right == null) return false;

            PerPlayerModifiersLayer<?> modifierLayer = getAnimLayer((AbstractClientPlayer) player);
            KosmXWallClimbKeyframePlayer keyframePlayer = new KosmXWallClimbKeyframePlayer(
                    up, down, left, right, modifierLayer);
            animStuff.put(player.getUUID(), keyframePlayer);
            return setAnim(player, keyframePlayer);
        }
        else {
            animStuff.put(player.getUUID(), null);
            return setAnim(player, null);
        }
    }

    @Override
    public void tickAnimProperties(Player player, boolean isMoving, 
            double movementUp, double movementLeft, float speed) {
        KosmXWallClimbKeyframePlayer climbAnim = getWallClimbAnimPlayer(player);
        if (climbAnim != null) {
            climbAnim.tickProperties(isMoving, movementUp, movementLeft, speed);
        }
        if (player == Minecraft.getInstance().player) {
            PacketManager.sendToServer(new ClSyncMotionAnimPacket(isMoving, movementUp, movementLeft, speed));
        }
    }
    
    @Nullable
    private KosmXWallClimbKeyframePlayer getWallClimbAnimPlayer(Player player) {
        return animStuff.get(player.getUUID());
    }
    
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }
    
    @SubscribeEvent
    public void onEntityRender(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        KosmXWallClimbKeyframePlayer animStuff = getWallClimbAnimPlayer(player);
        if (animStuff != null && animStuff.isActive()) {
            animStuff.onRender();
            
            HumanoidArm handTouch = animStuff.handTouchFrame();
            if (handTouch != null) {
                Vec3 particlesOffset = EnergyRippleLayer.handTipPos(event.getRenderer().getModel(), handTouch, Vec3.ZERO, player.yBodyRot);
                Vec3 particlesPos = player.position().add(particlesOffset);
                HamonUtil.emitHamonSparkParticles(player.level, ClientUtil.getClientPlayer(), 
                        particlesPos.x, particlesPos.y, particlesPos.z, 0.25f, 0.125f);
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderFirstPerson(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson()) {
            Player player = mc.player;
            KosmXWallClimbKeyframePlayer animStuff = getWallClimbAnimPlayer(player);
            if (animStuff != null && animStuff.isActive()) {
                animStuff.onRender();
                
                HumanoidArm handTouch = animStuff.handTouchFrame();
                if (handTouch != null) {
                    Camera camera = mc.gameRenderer.getMainCamera();
                    Vec3 particlesOffset = new Vec3(handTouch == HumanoidArm.LEFT ? 0.25 : -0.25, 0, 0.25)
                            .yRot((180 + mc.player.yBodyRot) * MathUtil.DEG_TO_RAD);
                    Vec3 particlesPos = camera.getPosition().add(particlesOffset);
                    HamonUtil.emitHamonSparkParticles(player.level, ClientUtil.getClientPlayer(), 
                            particlesPos.x, particlesPos.y, particlesPos.z, 0.25f, 0.125f);
                }
            }
        }
    }
    
}
