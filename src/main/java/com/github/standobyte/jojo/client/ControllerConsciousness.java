package com.github.standobyte.jojo.client;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceEntityLifeshot;
import com.github.standobyte.jojo.client.entity.ClientConsciousnessEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.client.player.Input;
import net.minecraft.client.Timer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ControllerConsciousness {
    public static final int SLOW_DOWN_MULTIPLIER = 10;
    private static ControllerConsciousness instance = null;
    
    public final Minecraft mc;
    private final Timer mcTimer;
//    public Timer normalSpeedTimer;
    private ClientConsciousnessEntity playerCsnsEntity = null;
//    private boolean slowedDown = false;

    public Input input;
    
    private ControllerConsciousness(Minecraft mc) {
        this.mc = mc;
        this.mcTimer = ClientReflection.getTimer(mc);
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ControllerConsciousness(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }
    
    public static ControllerConsciousness getInstance() {
        return instance;
    }
    
    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        
        LocalPlayer player = mc.player;
        if (mc.level != null && player != null) {
            boolean hasEffect = mc.player.hasEffect(ModStatusEffects.SENSORY_OVERLOAD.get());
            boolean splitCsns = isControllingConsciousnessEntity();
            if (hasEffect != splitCsns) {
                if (hasEffect) {
                    if (player.isAlive()) {
                        spawnConsciousness();
//                        normalSpeedTimer = new Timer(20, Util.getMillis());
//                        normalSpeedTimer.partialTick = mcTimer.partialTick;
//                        normalSpeedTimer.tickDelta = mcTimer.tickDelta;
//                        ClientReflection.setMsPerTick(mcTimer, 50 * SLOW_DOWN_MULTIPLIER);
//                        slowedDown = true;
                    }
                }
                else {
                    stop();
                }
            }
            
            if (playerCsnsEntity != null) {
                if (!playerCsnsEntity.isAlive() || !player.isAlive()) {
                    ClientUtil.setCameraEntityPreventShaderSwitch(mc, player);
                    playerCsnsEntity.discard();
                    playerCsnsEntity = null;
                }
                else {
                    player.connection.send(new ServerboundMovePlayerPacket.PositionRotationPacket(
                            player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot, player.onGround()));
                }
            }
        }
        
//        else if (slowedDown) {
//            stop();
//        }
    }
    
    public void stop() {
        if (playerCsnsEntity != null) {
            playerCsnsEntity.discard();
        }
//        ClientReflection.setMsPerTick(mcTimer, 50);
//        normalSpeedTimer = null;
//        slowedDown = false;
    }
    
    @SubscribeEvent
    public void mcDoTick(RenderTickEvent event) {
//        if (event.phase == TickEvent.Phase.END) return;
//        
//        if (normalSpeedTimer != null && playerConsciousnessEntity != null) {
//            int j = normalSpeedTimer.advanceTime(Util.getMillis());
//            for (int k = 0; k < Math.min(10, j); ++k) {
//                playerConsciousnessEntity.doTick();
//            }
//        }
    }
    
//    public float getConsciousnessPartialTick(float slowedPartialTick) {
//        return normalSpeedTimer != null ? normalSpeedTimer.partialTick : slowedPartialTick;
//    }
    
    public boolean isControllingConsciousnessEntity() {
        return playerCsnsEntity != null && playerCsnsEntity.isAlive();
    }
    
    @Nullable
    public ClientConsciousnessEntity getCsnsEntity() {
        return isControllingConsciousnessEntity() ? playerCsnsEntity : null;
    }
    
    public void spawnConsciousness() {
        if (!isControllingConsciousnessEntity()) {
            LocalPlayer playerEntity = mc.player;
            ClientLevel world = playerEntity.clientLevel;
            ClientConsciousnessEntity entity = new ClientConsciousnessEntity(this);
            
            Vec3 pos = playerEntity.position();
            entity.setPosRaw(pos.x, pos.y, pos.z);
            entity.absMoveTo(pos.x, pos.y, pos.z, playerEntity.yRot, playerEntity.xRot);
            entity.setYHeadRot(playerEntity.yHeadRot);
            entity.setYBodyRot(playerEntity.yBodyRot);
            
            entity.setId(GoldExperienceEntityLifeshot.ENTITY_ID);
            world.putNonPlayerEntity(entity.getId(), entity);
            
            this.playerCsnsEntity = entity;
            this.input = playerEntity.input;
            playerEntity.xxa = 0;
            playerEntity.yya = 0;
            playerEntity.zza = 0;
            ClientUtil.setCameraEntityPreventShaderSwitch(mc, playerCsnsEntity);
        }
    }
    
    


    @SubscribeEvent
    public void checkPlayerFrustum(RenderLevelStageEvent event) {
        if (isControllingConsciousnessEntity()) {
            getCsnsEntity().checkPlayerFrustum(event.getPoseStack(), 
                    event.getProjectionMatrix(), mc.gameRenderer.getMainCamera().getPosition());
        }
    }
    
    // FIXME do not render entity shadow (impossible)
    // FIXME do not cancel if it is rendered in inventory
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelPlayerRender(RenderPlayerEvent.Pre event) {
        if (event.getEntity() == mc.player) {
            boolean cancelRender = isControllingConsciousnessEntity() && getCsnsEntity().cancelPlayerRender;
            if (cancelRender) {
                event.setCanceled(true);
            }
        }
    }
    
}
