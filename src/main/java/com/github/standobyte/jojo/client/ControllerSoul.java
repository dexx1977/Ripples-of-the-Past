package com.github.standobyte.jojo.client;

import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ControllerSoul {
    private static ControllerSoul instance = null;
    
    private final Minecraft mc;
    private SoulEntity playerSoulEntity = null;
    private IStandPower standPower = null;
    private boolean firstDeathFrame = false;
    private boolean soulEntityWaiting = false;

    private ControllerSoul(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ControllerSoul(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }
    
    public static ControllerSoul getInstance() {
        return instance;
    }
    
    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (mc.player != null) {
            if (mc.player.isDeadOrDying()) {
                if (soulEntityWaiting && playerSoulEntity != null) {
                    soulEntityWaiting = false;
                }
                if (soulEntityWaiting || isCameraEntityPlayerSoul()) {
                    setOverlayMessage(true);
                }
                mc.player.deathTime = Math.min(mc.player.deathTime, 18);
            }
            else {
                if (!firstDeathFrame) {
                    firstDeathFrame = true;
                }
                soulEntityWaiting = false;
                if (playerSoulEntity != null && !playerSoulEntity.isAlive()) {
                    ClientUtil.setCameraEntityPreventShaderSwitch(mc.player);
                    playerSoulEntity = null;
                    setOverlayMessage(false);
                }
                
                if (standPower == null) {
                    updateStandCache();
                }
            }
        }
    }
    
    public void onSoulSpawn(SoulEntity soulEntity) {
        if (!mc.player.isSpectator() && soulEntity.getOriginEntity() == mc.player) {
            ClientUtil.setCameraEntityPreventShaderSwitch(soulEntity);
            playerSoulEntity = soulEntity;
        }
    }
    
    public boolean isCameraEntityPlayerSoul() {
        return playerSoulEntity != null && playerSoulEntity.isAlive() && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelHandsRender(RenderHandEvent event) {
        if (playerSoulEntity != null && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator() && mc.player.isDeadOrDying()) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderSoulTimer(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id()) && 
                playerSoulEntity != null && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator() && mc.player.isDeadOrDying()) {
            event.setCanceled(true);
            
            GuiDraw.setGraphics(event.getGuiGraphics());
            PoseStack matrixStack = event.getGuiGraphics().pose();
            mc.getProfiler().push("expBar");
            RenderSystem.setShaderTexture(0, ClientUtil.ADDITIONAL_UI);
            int i = mc.player.getXpNeededForNextLevel();
            if (i > 0) {
                int xPos = mc.getWindow().getGuiScaledWidth() / 2 - 91;
                int yPos = mc.getWindow().getGuiScaledHeight() - 32 + 3;
                int width = 182;
                int fill = (int)((1.0F - ((float) playerSoulEntity.tickCount / playerSoulEntity.lifeSpan)) * (width + 1));
                GuiDraw.blit(matrixStack, xPos, yPos, 0, 0, 208, width, 5, 256, 256);
                if (fill > 0) {
                    GuiDraw.blit(matrixStack, xPos, yPos, 0, 0, 213, fill, 5, 256, 256);
                }
            }
            
            mc.getProfiler().pop();
        }
    }
    
    public void skipAscension() {
        if (isCameraEntityPlayerSoul()) {
            playerSoulEntity.skipAscension();
            setOverlayMessage(false);
        }
        else if (soulEntityWaiting) {
            soulEntityWaiting = false;
            setOverlayMessage(false);
        }
    }
    
    private static final Component OVERLAY_MESSAGE = Component.translatable("jojo.message.skip_soul_ascension", Component.keybind("key.jump"));
    private void setOverlayMessage(boolean message) {
        if (message) {
            mc.gui.setOverlayMessage(OVERLAY_MESSAGE, false);
        }
        else {
            Component overlayMessage = ClientReflection.getOverlayMessageString(mc.gui);
            if (OVERLAY_MESSAGE.equals(overlayMessage)) {
                mc.gui.setOverlayMessage(null, false);
                ClientReflection.setOverlayMessageTime(mc.gui, 0);
            }
        }
    }
    
    public void updateStandCache() {
        standPower = IStandPower.getPlayerStandPower(mc.player);
    }
    
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelRespawnScreen(GuiOpenEvent event) {
        boolean soul = isCameraEntityPlayerSoul();
        if (event.getGui() instanceof DeathScreen) {
        	if (mc.screen instanceof DeathScreen) {
        		/* When the player dies in nether handle, this causes the game to repeatedly open DeathScreen
        		 * (ClientPlayerEntity#handleNetherPortalClient() tries to close the screen, but the game opens DeathScreen instead).
        		 * The vanilla crutch is the fact that the player's entity is removed after 20 death ticks, 
        		 * so ClientPlayerEntity#handleNetherPortalClient() isn't called anymore, escaping the loop.
        		 * We don't want the player entity to be removed, so this ACTUALLY patches the bug.
        		 */
        		event.setCanceled(true);
        		return;
        	}
            if (!soulEntityWaiting && firstDeathFrame && standPower.willSoulSpawn()) {
                soulEntityWaiting = true;
                firstDeathFrame = false;
            }
            if (soul || soulEntityWaiting) {
                event.setGui(null);
                if (playerSoulEntity != null && !playerSoulEntity.isAlive() && !soulEntityWaiting) {
                    mc.player.respawn();
                }
            }
        }
    }
    
    public void onSoulFailedSpawn() {
        soulEntityWaiting = false;
    }
}
