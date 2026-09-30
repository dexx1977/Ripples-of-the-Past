package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.POTION_ICONS;
import static net.minecraftforge.event.TickEvent.Phase.END;
import static net.minecraftforge.fml.LogicalSide.CLIENT;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClStandManualMovementPacket;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ControllerStand {
    private static ControllerStand instance = null;
    
    private final Minecraft mc;
    private boolean isProbablyControllingStand;
    private StandEntity stand;

    private ControllerStand(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ControllerStand(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }

    public static ControllerStand getInstance() {
        return instance;
    }
    
    public static void setStartedControllingStand() {
        if (instance.mc.getCameraEntity() instanceof StandEntity) {
            instance.isProbablyControllingStand = true;
            instance.stand = (StandEntity) instance.mc.getCameraEntity();
        }
    }

    public boolean isControllingStand() {
        if (isProbablyControllingStand) {
            isProbablyControllingStand = mc.getCameraEntity() instanceof StandEntity;
            if (!isProbablyControllingStand) {
                stand = null;
            }
        }
        return isProbablyControllingStand;
    }
    
    @Nullable
    public StandEntity getManuallyControlledStand() {
        if (isControllingStand()) {
            return stand;
        }
        return null;
    }
    
    

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInputUpdate(InputUpdateEvent event) {
        if (isControllingStand()) {
            Input input = event.getMovementInput();
            stand.moveStandManually(input.leftImpulse, input.forwardImpulse, input.jumping, input.shiftKeyDown);
            // FIXME do not reset deltaMovement in manual control
            PacketManager.sendToServer(new ClStandManualMovementPacket(
                    stand.getX(), stand.getY(), stand.getZ(), stand.xRot, stand.yRot, stand.hadInput()));
        }
        else {
            if ((mc.getCameraEntity() == mc.player || mc.getCameraEntity() == null) && ModStatusEffects.isStunned(mc.player)) {
                Input input = event.getMovementInput();
                input.forwardImpulse = 0;
                input.leftImpulse = 0;
                input.jumping = false;
                if (mc.player.getVehicle() != null) {
                    input.shiftKeyDown = false;
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onMouseScroll(InputEvent.MouseScrollEvent event) {
        if (isControllingStand()) {
            stand.manualMovementSpeed = Mth.clamp(stand.manualMovementSpeed + 0.025f * (float) event.getScrollDelta(), 0, 1);
        }
    }
    
    @SubscribeEvent
    public void sendPlayerPositionRotation(PlayerTickEvent event) {
        if (event.side != CLIENT || event.phase != END || !isControllingStand()) {
            return;
        }

        LocalPlayer player = mc.player;
        if (!stand.isAlive()) {
            ClientUtil.setCameraEntityPreventShaderSwitch(player);
        }
        else {
            player.connection.send(new ServerboundMovePlayerPacket.PositionRotationPacket(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot, player.onGround()));
        }
    }
    
    

    @SuppressWarnings("rawtypes")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderStandHands(RenderHandEvent event) {
        if (!isControllingStand()) {
            return;
        }

        LocalPlayer player = mc.player;
        player.yBobO = player.yBob;
        player.xBobO = player.xBob;
        player.xBob = (float)((double)player.xBob + (double)(player.xRot - player.xBob) * 0.5D);
        player.yBob = (float)((double)player.yBob + (double)(player.yRot - player.yBob) * 0.5D);
        PoseStack matrixStack = event.getMatrixStack();
        MultiBufferSource buffer = event.getBuffers();
        float partialTick = event.getPartialTicks();
        int light = mc.getEntityRenderDispatcher().getPackedLightCoords(stand, partialTick);
        StandEntityRenderer renderer = (StandEntityRenderer<?, ?>)mc.getEntityRenderDispatcher().<StandEntity>getRenderer(stand);
//        renderer.renderFirstPersonArms(matrixStack, buffer, light, stand, partialTick);
        renderer.renderFirstPerson(stand, partialTick, matrixStack, buffer, light);
        event.setCanceled(true);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelBlockOverlayRender(RenderBlockOverlayEvent event) {
        if (isControllingStand()) {
            event.setCanceled(true);
        }
    }
    
    

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderStandEffectsGui(RenderGameOverlayEvent.Pre event) {
        if (!isControllingStand()) {
            return;
        }
        if (event.getType() == POTION_ICONS) {
            PoseStack matrixStack = event.getMatrixStack();
            event.setCanceled(true);
            Gui gui = mc.gui;
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            renderStandPotionEffects(matrixStack, gui, event, width, height);     
        }
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderStandGui(RenderGameOverlayEvent.Pre event) {
        if (!isControllingStand()) {
            return;
        }

        PoseStack matrixStack = event.getMatrixStack();
        if (!mc.options.hideGui) {
            switch (event.getType()) {
            case ALL:
                if (mc.gameMode.canHurtPlayer()) {
                    Gui gui = mc.gui;
                    int width = mc.getWindow().getGuiScaledWidth();
                    int height = mc.getWindow().getGuiScaledHeight();
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    if (ForgeGui.renderHealth) renderCameraStandHealth(matrixStack, gui, event, width, height);
                    if (ForgeGui.renderArmor)  renderCameraStandArmor(matrixStack, gui, event, width, height);
                }
                break;
            default:
                break;
            }
        }
    }

    private void renderCameraStandHealth(PoseStack matrixStack, Gui gui, RenderGameOverlayEvent event, int width, int height) {
        LivingEntity entity = StandUtil.getStandUser(stand);
        ClientEventHandler.getInstance().renderHealthWithBleeding(entity, matrixStack, gui, event, width, height);
    }

    private void renderCameraStandArmor(PoseStack matrixStack, Gui gui, RenderGameOverlayEvent event, int width, int height) {
        mc.getProfiler().push("armor");

        RenderSystem.enableBlend();
        int left = width / 2 - 91;
        int top = height - ForgeGui.left_height;

        int level = stand.getArmorValue();
        for (int i = 1; level > 0 && i < 20; i += 2)
        {
            if (i < level)
            {
                gui.blit(matrixStack, left, top, 34, 9, 9, 9);
            }
            else if (i == level)
            {
                gui.blit(matrixStack, left, top, 25, 9, 9, 9);
            }
            else if (i > level)
            {
                gui.blit(matrixStack, left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        ForgeGui.left_height += 10;

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }

    @SuppressWarnings("deprecation")
    private void renderStandPotionEffects(PoseStack matrixStack, Gui gui, RenderGameOverlayEvent event, int width, int height) {
        Collection<MobEffectInstance> collection = stand.getActiveEffects();
        if (!collection.isEmpty()) {
            RenderSystem.enableBlend();
            int i = 0;
            int j = 0;
            MobEffectTextureManager potionspriteuploader = mc.getMobEffectTextures();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
            mc.getTextureManager().bind(AbstractContainerScreen.INVENTORY_LOCATION);

            for(MobEffectInstance effectinstance : Ordering.natural().reverse().sortedCopy(collection)) {
                MobEffect effect = effectinstance.getEffect();
                if (!effectinstance.shouldRenderHUD()) continue;
                // Rebind in case previous renderHUDEffect changed texture
                mc.getTextureManager().bind(AbstractContainerScreen.INVENTORY_LOCATION);
                if (effectinstance.showIcon()) {
                    int k = width;
                    int l = 1;
                    if (mc.isDemo()) {
                        l += 15;
                    }

                    if (effect.isBeneficial()) {
                        ++i;
                        k = k - 25 * i;
                    } else {
                        ++j;
                        k = k - 25 * j;
                        l += 26;
                    }

                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    float f = 1.0F;
                    if (effectinstance.isAmbient()) {
                        gui.blit(matrixStack, k, l, 165, 166, 24, 24);
                    } else {
                        gui.blit(matrixStack, k, l, 141, 166, 24, 24);
                        if (effectinstance.getDuration() <= 200) {
                            int i1 = 10 - effectinstance.getDuration() / 20;
                            f = Mth.clamp((float)effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float)effectinstance.getDuration() * (float)Math.PI / 5.0F) * Mth.clamp((float)i1 / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effect);
                    int j1 = k;
                    int k1 = l;
                    float f1 = f;
                    list.add(() -> {
                        mc.getTextureManager().bind(textureatlassprite.atlas().location());
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, f1);
                        AbstractGui.blit(matrixStack, j1 + 3, k1 + 3, gui.getBlitOffset(), 18, 18, textureatlassprite);
                    });
                    effectinstance.renderHUDEffect(gui, matrixStack, k, l, gui.getBlitOffset(), f);
                }
            }

            list.forEach(Runnable::run);
        }
    }
}
