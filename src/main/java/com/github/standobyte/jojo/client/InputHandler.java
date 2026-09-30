package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.ClientSetup;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSLASH;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.non_stand.HamonRebuffOverdrive;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry.KeyActiveType;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry.OnKeyPress;
import com.github.standobyte.jojo.client.controls.ActionsHotbar;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.ControlScheme.Hotbar;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.ActionUseTry;
import com.github.standobyte.jojo.client.ui.screen.IJojoScreen;
import com.github.standobyte.jojo.client.ui.screen.WasdAllowingScreen;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.entity.itemprojectile.ItemProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClDoubleShiftPressPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonInteractAskTeacherPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonInteractTeachPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHasInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHeldActionTargetPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnLeapPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnStandDashPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandManualControlPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.CollisionUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.IPlayerLeap;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.mco.mcrecog.CommandsMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.player.Input;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.Util;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InputHandler {
    private static InputHandler instance = null;

    private Minecraft mc;
    private ActionsOverlayGui actionsOverlay;
    private IStandPower standPower;
    private INonStandPower nonStandPower;
    
    public HitResult mouseTarget;

    public static final String MAIN_CATEGORY = new String("key.categories." + JojoMod.MOD_ID);
    public KeyMapping toggleStand;
    public KeyMapping standRemoteControl;
    public KeyMapping hamonSkillsWindow;

    public static final String HUD_CATEGORY = new String("key.categories." + JojoMod.MOD_ID + ".hud");
    public KeyMapping nonStandMode;
    public KeyMapping standMode;
    public KeyMapping jojoStuffMenu;
    public KeyMapping jojoLmbRmbKeybind;
    public KeyMapping disableHotbars;
    public KeyMapping attackHotbar;
    public KeyMapping abilityHotbar;

    public static final String HUD_ALTERNATIVE_CATEGORY = new String("key.categories." + JojoMod.MOD_ID + ".hud.alternative");
    public KeyMapping scrollMode;
    public KeyMapping scrollAttack;
    public KeyMapping scrollAbility;
    public KeyMapping hamonMeditation;
    
    // it works because the actual map is static, not sure if that's intended or just an implementation detail
    // but hey, i'll take it
    public final KeyBindingMap keyBindingMap = new KeyBindingMap();
    
    private int leftClickBlockDelay;
    
    public boolean hasInput;
    private boolean wallClimbMoving;
    
    private boolean canLeap;
    
    private DoubleShiftDetector doubleShift = new DoubleShiftDetector();

    private InputHandler(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new InputHandler(mc);
            instance.registerKeyBindings();
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }
    
    public static InputHandler getInstance() {
        return instance;
    }
    
    public void setActionsOverlay(ActionsOverlayGui instance) {
        this.actionsOverlay = instance;
    }
    
    public void registerKeyBindings() {
        ClientSetup.registerKeyMapping(toggleStand = new KeyMapping(JojoMod.MOD_ID + ".key.toggle_stand", GLFW_KEY_M, MAIN_CATEGORY));
        ClientSetup.registerKeyMapping(standRemoteControl = new KeyMapping(JojoMod.MOD_ID + ".key.stand_remote_control", GLFW_KEY_O, MAIN_CATEGORY));
        ClientSetup.registerKeyMapping(hamonSkillsWindow = new KeyMapping(JojoMod.MOD_ID + ".key.hamon_skills_window", GLFW_KEY_H, MAIN_CATEGORY));
        ClientSetup.registerKeyMapping(jojoLmbRmbKeybind = new KeyMapping(JojoMod.MOD_ID + ".key.jojo_test", GLFW_KEY_UNKNOWN, MAIN_CATEGORY) {
            private boolean wasJustReset = false;
            @Override
            public void setToDefault() {
                super.setToDefault();
                ClientModSettings.getInstance().editSettings(settings -> settings.poseOnLmbRmb = true);
                wasJustReset = true;
            }

            @Override
            public void setKeyModifierAndCode(KeyModifier keyModifier, InputConstants.Input keyCode) {
                if (!keyCode.equals(this.getKey()) || !keyCode.equals(this.getDefaultKey())) {
                    ClientModSettings.getInstance().editSettings(settings -> settings.poseOnLmbRmb = false);
                }
                super.setKeyModifierAndCode(keyModifier, keyCode);
            }
            
            @Override
            public void setKey(InputConstants.Input pInput) {
                if (wasJustReset) {
                    wasJustReset = false;
                }
                else {
                    ClientModSettings.getInstance().editSettings(settings -> settings.poseOnLmbRmb = false);
                }
                super.setKey(pInput);
            }
            
            @Override
            public boolean isDefault() {
                return super.isDefault() && ClientModSettings.getSettingsReadOnly().poseOnLmbRmb;
            }
            
            @Override
            public Component getTranslatedKeyMessage() {
                if (isDefault()) {
                    return Component.translatable("key.mouse.lmb_and_rmb");
                }
                return super.getTranslatedKeyMessage();
            }
        });
        
        ClientSetup.registerKeyMapping(jojoStuffMenu = new KeyMapping(JojoMod.MOD_ID + ".key.jojo_menu", GLFW_KEY_BACKSLASH, HUD_CATEGORY));
        
        ClientSetup.registerKeyMapping(nonStandMode = new KeyMapping(JojoMod.MOD_ID + ".key.non_stand_mode", GLFW_KEY_J, HUD_CATEGORY));
        ClientSetup.registerKeyMapping(standMode = new KeyMapping(JojoMod.MOD_ID + ".key.stand_mode", GLFW_KEY_K, HUD_CATEGORY));
        
        ClientSetup.registerKeyMapping(attackHotbar = new KeyMapping(JojoMod.MOD_ID + ".key.attack_hotbar", GLFW_KEY_V, HUD_CATEGORY));
        ClientSetup.registerKeyMapping(abilityHotbar = new KeyMapping(JojoMod.MOD_ID + ".key.ability_hotbar", GLFW_KEY_B, HUD_CATEGORY));
        ClientSetup.registerKeyMapping(disableHotbars = new KeyMapping(JojoMod.MOD_ID + ".key.disable_hotbars", GLFW_KEY_LEFT_ALT, HUD_CATEGORY));
        
        ClientSetup.registerKeyMapping(scrollMode = new KeyMapping(JojoMod.MOD_ID + ".key.scroll_mode", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        ClientSetup.registerKeyMapping(scrollAttack = new KeyMapping(JojoMod.MOD_ID + ".key.scroll_attack", GLFW_KEY_V, HUD_ALTERNATIVE_CATEGORY));
        scrollAttack.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW_KEY_UNKNOWN));
        ClientSetup.registerKeyMapping(scrollAbility = new KeyMapping(JojoMod.MOD_ID + ".key.scroll_ability", GLFW_KEY_B, HUD_ALTERNATIVE_CATEGORY));
        scrollAbility.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW_KEY_UNKNOWN));
        ClientSetup.registerKeyMapping(hamonMeditation = new KeyMapping(JojoMod.MOD_ID + ".key.meditation", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        
        initHeldKeybindTimers();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMouseScroll(MouseScrollEvent event) {
        if (standPower == null || nonStandPower == null || actionsOverlay == null) {
            return;
        }

        if (actionsOverlay.isActive() && !JojoModUtil.tmpSpectatorCantUsePowers(mc.player)) {
            boolean scrollAttack = controlsAreOnHotbar(ControlScheme.Hotbar.LEFT_CLICK);
            boolean scrollAbility = controlsAreOnHotbar(ControlScheme.Hotbar.RIGHT_CLICK);
            if (scrollAttack || scrollAbility) {
                if (scrollAttack) {
                    actionsOverlay.scrollAction(ControlScheme.Hotbar.LEFT_CLICK, event.getScrollDelta() > 0.0D);
                    preventHotbarScrollIfSameKey(ControlScheme.Hotbar.LEFT_CLICK);
                }
                if (scrollAbility) {
                    actionsOverlay.scrollAction(ControlScheme.Hotbar.RIGHT_CLICK, event.getScrollDelta() > 0.0D);
                    preventHotbarScrollIfSameKey(ControlScheme.Hotbar.RIGHT_CLICK);
                }
                event.setCanceled(true);
            }
        }
    }
    
    
    /**
     * On the very same tick the key is released, the value is negative, and signifies for how many ticks the key has been held.
     */
    private Map<KeyMapping, MutableInt> keybindHeldTimer;
    
    private void initHeldKeybindTimers() {
        keybindHeldTimer = Util.make(new HashMap<>(), map -> {
            map.put(scrollAttack, new MutableInt(0));
            map.put(scrollAbility, new MutableInt(0));
        });
    }
    
    private void tickHeldKeybindTimers() {
        keybindHeldTimer.entrySet().forEach(heldKeyTimer -> {
            MutableInt timer = heldKeyTimer.getValue();
            if (heldKeyTimer.getKey().isDown()) {
                if (timer.intValue() <= 0) {
                    timer.setValue(1);
                }
                else {
                    timer.increment();
                }
            }
            else {
                if (timer.intValue() > 0) {
                    timer.setValue(-timer.intValue());
                }
                else if (timer.intValue() < 0) {
                    timer.setValue(0);
                }
            }
        });
    }
    
    private void preventHotbarScrollIfSameKey(ControlScheme.Hotbar hotbar) {
        KeyMapping scrollKey;
        boolean sameKeyAsNewerHotbarControls;
        switch (hotbar) {
        case LEFT_CLICK:
            scrollKey = scrollAttack;
            sameKeyAsNewerHotbarControls = scrollAttack.getKey().equals(attackHotbar.getKey());
            break;
        case RIGHT_CLICK:
            scrollKey = scrollAbility;
            sameKeyAsNewerHotbarControls = scrollAbility.getKey().equals(abilityHotbar.getKey());
            break;
        default:
            throw new AssertionError();
        }
        if (sameKeyAsNewerHotbarControls) {
            MutableInt timer = keybindHeldTimer.get(scrollKey);
            if (timer.intValue() > 0) {
                timer.setValue(1337);
            }
        }
    }
    
    
    @SubscribeEvent
    public void handleKeyBindings(ClientTickEvent event) {
        if (mc.overlay != null || (mc.screen != null && !mc.screen.passEvents)
                || mc.level == null || standPower == null || nonStandPower == null
                || actionsOverlay == null || JojoModUtil.tmpSpectatorCantUsePowers(mc.player)) {
            return;
        }
        
        if (event.phase == TickEvent.Phase.START) {
            if (ClientModSettings.getSettingsReadOnly().toggleDisableHotbars && disableHotbars.consumeClick()) {
                setToggleHotbarsDisabled(!toggledHotbarsDisabled);
            }
            actionsOverlay.setHotbarsEnabled(!areHotbarsDisabled());
            tickHeldKeybindTimers();
            
            if (jojoLmbRmbKeybind.isDefault() && mc.options.keyAttack.clickCount > 0 && mc.options.keyUse.clickCount > 0
                    && doTheThing()) {
                mc.options.keyAttack.clickCount = 0;
                mc.options.keyUse.clickCount = 0;
                mc.options.keyAttack.setDown(false);
                mc.options.keyUse.setDown(false);
            }
            
            if (actionsOverlay.isActive()) {
                boolean chooseAttack = controlsAreOnHotbar(ControlScheme.Hotbar.LEFT_CLICK);
                boolean chooseAbility = controlsAreOnHotbar(ControlScheme.Hotbar.RIGHT_CLICK);
                actionsOverlay.setHotbarButtonsDows(chooseAttack, chooseAbility);
                if (chooseAttack || chooseAbility) {
                    for (int i = 0; i < 9; i++) {
                        if (mc.options.keyHotbarSlots[i].consumeClick()) {
                            if (chooseAttack) {
                                actionsOverlay.selectAction(ControlScheme.Hotbar.LEFT_CLICK, i);
                                preventHotbarScrollIfSameKey(ControlScheme.Hotbar.LEFT_CLICK);
                            }
                            if (chooseAbility) {
                                actionsOverlay.selectAction(ControlScheme.Hotbar.RIGHT_CLICK, i);
                                preventHotbarScrollIfSameKey(ControlScheme.Hotbar.RIGHT_CLICK);
                            }
                        }
                    }
                }
                
                // i don't feel like making a separate method for that today
                int scrollKeyHeldFor = keybindHeldTimer.get(scrollAttack).intValue();
                boolean sameKey = scrollAttack.getKey().equals(attackHotbar.getKey());
                if (sameKey && scrollKeyHeldFor < 0 && scrollKeyHeldFor >= -20 || !sameKey && scrollKeyHeldFor == 1) {
                    actionsOverlay.scrollAction(ControlScheme.Hotbar.LEFT_CLICK, mc.player.isShiftKeyDown());
                }
                
                scrollKeyHeldFor = keybindHeldTimer.get(scrollAbility).intValue();
                sameKey = scrollAbility.getKey().equals(abilityHotbar.getKey());
                if (sameKey && scrollKeyHeldFor < 0 && scrollKeyHeldFor >= -20 || !sameKey && scrollKeyHeldFor == 1) {
                    actionsOverlay.scrollAction(ControlScheme.Hotbar.RIGHT_CLICK, mc.player.isShiftKeyDown());
                }
                
                if (ClientModSettings.getSettingsReadOnly().toggleLmbHotbar && attackHotbar.consumeClick()) {
                    switchToggledHotbarControls(ControlScheme.Hotbar.LEFT_CLICK);
                }

                if (ClientModSettings.getSettingsReadOnly().toggleRmbHotbar && abilityHotbar.consumeClick()) {
                    switchToggledHotbarControls(ControlScheme.Hotbar.RIGHT_CLICK);
                }
            }

            actionsOverlay.resetHeldThisTick();
            if (nonStandPower.hasPower()) {
                tickCustomKeybinds(nonStandPower, actionsOverlay.getCurrentMode() == PowerClassification.NON_STAND);
            }
            if (standPower.hasPower()) {
                tickCustomKeybinds(standPower, actionsOverlay.getCurrentMode() == PowerClassification.STAND);
            }
            
            if (mc.options.keyJump.isDown()) {
                ControllerSoul.getInstance().skipAscension();
            }
            tickEffects();
            clickWithBusyHands();
        }
        else {
            boolean targetChanged = pickMouseTarget();
            
            if (leftClickBlockDelay > 0) {
                leftClickBlockDelay--;
            }
            
            if (standMode.consumeClick()) {
                actionsOverlay.switchMode(PowerClassification.STAND);
            }
            
            if (nonStandMode.consumeClick()) {
                actionsOverlay.switchMode(PowerClassification.NON_STAND);
            }
            
            if (scrollMode.consumeClick()) {
                actionsOverlay.scrollMode();
            }
            

            if (toggleStand.consumeClick()) {
                if (standPower.hasPower() && !standPower.isActive()) {
                    actionsOverlay.onStandSummon();
                }
//                else {
//                    actionsOverlay.onStandUnsummon();
//                }
                PacketManager.sendToServer(new ClToggleStandSummonPacket());
            }
            
            if (standRemoteControl.consumeClick()) {
                PacketManager.sendToServer(new ClToggleStandManualControlPacket());
            }
            
            if (hamonSkillsWindow.consumeClick()) {
                if (nonStandPower.hasPower() && nonStandPower.getType() == ModPowers.HAMON.get()) {
                    boolean taughtHamon = false;
                    if (mouseTarget instanceof EntityHitResult) {
                        Entity mouseTargetEntity = ((EntityHitResult) mouseTarget).getEntity();
                        taughtHamon = nonStandPower.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                            if (mouseTargetEntity instanceof Player) {
                                return hamon.interactWithNewLearner((Player) mouseTargetEntity);
                            }
                            return false;
                        }).orElse(false);
                        if (taughtHamon) {
                            PacketManager.sendToServer(new ClHamonInteractTeachPacket(mouseTargetEntity.getId()));
                        }
                    }
                    if (!taughtHamon) {
                        ClientUtil.openHamonTeacherUi();
                    }
                }
                else if (nonStandPower.canGetPower(ModPowers.HAMON.get())) {
                    boolean askedForHamonTraining = false;
                    if (mouseTarget instanceof EntityHitResult) {
                        Entity mouseTargetEntity = ((EntityHitResult) mouseTarget).getEntity();
                        if (mouseTargetEntity instanceof LivingEntity) {
                            askedForHamonTraining = HamonUtil.interactWithHamonTeacher(mc.level, mc.player, (LivingEntity) mouseTargetEntity);
                            if (askedForHamonTraining) {
                                PacketManager.sendToServer(new ClHamonInteractAskTeacherPacket(mouseTargetEntity.getId()));
                            }
                        }
                    }
                    if (!askedForHamonTraining) {
                        Component message;
                        if (nonStandPower.getType() == ModPowers.VAMPIRISM.get()) {
                            message = Component.translatable("jojo.chat.message.no_hamon_vampire");
                        }
                        else if (nonStandPower.hadPowerBefore(ModPowers.HAMON.get())) {
                            message = Component.translatable("jojo.chat.message.no_hamon_abandoned");
                        }
                        else {
                            message = Component.translatable("jojo.chat.message.no_hamon");
                        }
                        mc.gui.handleChat(ChatType.GAME_INFO, message, Util.NIL_UUID);
                    }
                }
            }
            
            if (hamonMeditation.consumeClick()) {
                PacketManager.sendToServer(new ClHamonMeditationPacket());
            }
            
            if (jojoStuffMenu.consumeClick()) {
                IJojoScreen.onScreenKeyPress();
            }
            
            while (jojoLmbRmbKeybind.consumeClick()) {
                doTheThing();
            }
            
            if (!mc.options.keyAttack.isDown()) {
                leftClickBlockDelay = 0;
            }
            
            checkHeldActionAndTarget(standPower, targetChanged);
            checkHeldActionAndTarget(nonStandPower, targetChanged);
            
            if (targetChanged) {
                ClientEventHandler.onMouseTargetChanged(mouseTarget);
            }
        }
    }
    
    private <P extends IPower<P, T>, T extends IPowerType<P, T>> void tickCustomKeybinds(P power, boolean isHudActive) {
        for (ActionKeybindEntry keybindEntry : HudControlSettings.getInstance()
                .getControlScheme(power)
                .getCustomKeybinds()) {
            KeyMapping keybind = keybindEntry.getKeybind();
            OnKeyPress onPress = keybindEntry.getOnKeyPress();
            KeyActiveType needsOpenHud = keybindEntry.getHudInteraction();
            
            if (keybind.isDown() && needsOpenHud.canTrigger(isHudActive)) {
                actionsOverlay.setHeldThisTick(keybindEntry);
                if (keybindEntry.delay <= 0) {
                    switch (onPress) {
                    case PERFORM:
                        HudClickResult result = handleCustomKeybind(keybindEntry, power);
                        if (result.vanillaInput == HudClickResult.Behavior.CANCEL) {
                            KeyMapping keybinding = keyBindingMap.lookupActive(keybind.getKey());
                            if (keybinding != null) {
                                while (keybinding.consumeClick());
                            }
                        }
                        if (result.handSwing == HudClickResult.Behavior.FORCE) {
                            mc.player.swing(InteractionHand.MAIN_HAND);
                        }
                        keybindEntry.delay = 4;
                        break;
                    case SELECT:
                        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
                        ControlScheme controls = HudControlSettings.getInstance().getControlScheme(power.getPowerClassification());
                        if (controls.hotbarsEnabled) {
                            Hotbar foundHotbar = null;
                            int foundIndex = -1;
                            for (Hotbar hotbarType : Hotbar.values()) {
                                ActionsHotbar hotbar = controls.getActionsHotbar(hotbarType);
                                List<Action<?>> actions = hotbar.getEnabledActions();
                                for (int i = 0; i < actions.size() && foundIndex < 0; i++) {
                                    Action<?> action = actions.get(i);
                                    if (action == keybindEntry.getAction() || action.getShiftVariationIfPresent() == keybindEntry.getAction()) {
                                        foundIndex = i;
                                        foundHotbar = hotbarType;
                                    }
                                }
                                if (foundHotbar != null) break;
                            }
                            
                            if (foundHotbar != null && foundIndex >= 0) {
                                hud.setMode(power.getPowerClassification());
                                hud.selectAction(foundHotbar, foundIndex);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            
            if (!keybind.isDown()) {
                keybindEntry.delay = 0;
            }
            else if (keybindEntry.delay > 0) {
                --keybindEntry.delay;
            }
        }
    }
    
    
    public void switchToggledHotbarControls(ControlScheme.Hotbar hotbar) {
        setToggledHotbarControls(hotbar, !areControlsLockedForHotbar(hotbar));
    }
    
    public void setToggledHotbarControls(ControlScheme.Hotbar hotbar, boolean value) {
        switch (hotbar) {
        case LEFT_CLICK:
            toggledAttacksHotbar = value;
            if (value) toggledAbilitiesHotbar = false;
            break;
        case RIGHT_CLICK:
            if (value) toggledAttacksHotbar = false;
            toggledAbilitiesHotbar = value;
            break;
        }
    }
    
    private boolean toggledAttacksHotbar;
    private boolean toggledAbilitiesHotbar;
    public boolean areControlsLockedForHotbar(ControlScheme.Hotbar hotbar) {
        if (hotbar == null) return false;
        switch (hotbar) {
        case LEFT_CLICK:
            return toggledAttacksHotbar;
        case RIGHT_CLICK:
            return toggledAbilitiesHotbar;
        }
        return false;
    }
    
    private HotbarInterceptingBy controlsOnLmbHotbar = HotbarInterceptingBy.NONE;
    private HotbarInterceptingBy controlsOnRmbHotbar = HotbarInterceptingBy.NONE;
    private enum HotbarInterceptingBy {
        HOLD,
        TOGGLE,
        NONE
    }
    
    private void updateHotbarsControlsState() {
        if (ClientModSettings.getSettingsReadOnly().toggleLmbHotbar) {
            controlsOnLmbHotbar = toggledAttacksHotbar ? HotbarInterceptingBy.TOGGLE : HotbarInterceptingBy.NONE;
        }
        else {
            controlsOnLmbHotbar = attackHotbar.isDown() ? HotbarInterceptingBy.HOLD : HotbarInterceptingBy.NONE;
        }
        
        
        if (ClientModSettings.getSettingsReadOnly().toggleRmbHotbar) {
            controlsOnRmbHotbar = toggledAbilitiesHotbar ? HotbarInterceptingBy.TOGGLE : HotbarInterceptingBy.NONE;
        }
        else {
            controlsOnRmbHotbar = abilityHotbar.isDown() ? HotbarInterceptingBy.HOLD : HotbarInterceptingBy.NONE;
        }
    }
    
    private boolean controlsAreOnHotbar(ControlScheme.Hotbar hotbar) {
        updateHotbarsControlsState();
        HotbarInterceptingBy askedHotbar;
        HotbarInterceptingBy otherHotbar;
        switch (hotbar) {
        case LEFT_CLICK:
            askedHotbar = controlsOnLmbHotbar;
            otherHotbar = controlsOnRmbHotbar;
            break;
        case RIGHT_CLICK:
            askedHotbar = controlsOnRmbHotbar;
            otherHotbar = controlsOnLmbHotbar;
            break;
        default:
            return false;
        }
        
        switch (askedHotbar) {
        case NONE:
            return false;
        case TOGGLE:
            return otherHotbar != HotbarInterceptingBy.HOLD;
        case HOLD:
            return true;
        default:
            return false;
        }
    }
    
    
    private boolean toggledHotbarsDisabled = false;
    private boolean areHotbarsDisabled() {
        if (ClientModSettings.getSettingsReadOnly().toggleDisableHotbars) {
            return toggledHotbarsDisabled;
        }
        else {
            return disableHotbars.isDown();
        }
    }
    
    public void setToggleHotbarsDisabled(boolean value) {
        this.toggledHotbarsDisabled = value;
    }
    
    
    private boolean doTheThing() {
        if (actionsOverlay.getCurrentMode() == PowerClassification.STAND) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARROW_HIT_PLAYER, 1.0F));
            return true;
        }
        return false;
    }
    
    
    private static final Random RANDOM = new Random();
    public void setRandomStandSkin() {
        if (standPower.hasPower()) {
            ResourceLocation standId = standPower.getType().getRegistryName();
            List<StandSkin> allSkins = StandSkinsManager.getInstance().getStandSkinsView(standId);
            int i = RANDOM.nextInt(allSkins.size());
            Optional<StandSkin> standSkin = Optional.of(allSkins.get(i));
            PacketManager.sendToServer(new ClSetStandSkinPacket(standSkin.map(skin -> skin.resLoc), standId));
        }
    }

    private boolean pickMouseTarget() {
        HitResult target = mc.hitResult;
        if (actionsOverlay != null && actionsOverlay.getCurrentPower() != null) {
            IPower<?, ?> power = actionsOverlay.getCurrentPower();
            if (power.hasPower()) {
                target = power.clientHitResult(mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player, target);
            }
        }
        
        if (target != null && !MCUtil.rayTraceTargetEquals(target, mouseTarget)) {
            this.mouseTarget = target;
            return true;
        }
        
        return false;
    }
    
    private final Map<IPower<?, ?>, KeyMapping> heldKeys = new HashMap<>();
    
    public enum ActionKey {
        ATTACK(ControlScheme.Hotbar.LEFT_CLICK) {
            @Override
            protected KeyMapping getKey(Minecraft mc, InputHandler modInput) { return mc.options.keyAttack; }
        },
        ABILITY(ControlScheme.Hotbar.RIGHT_CLICK) {
            @Override
            protected KeyMapping getKey(Minecraft mc, InputHandler modInput) { return mc.options.keyUse; }
        };
        
        private final ControlScheme.Hotbar hotbar;
        
        private ActionKey(ControlScheme.Hotbar hotbar) {
            this.hotbar = hotbar;
        }
        
        protected abstract KeyMapping getKey(Minecraft mc, InputHandler modInput);
        
        @Nullable
        public ControlScheme.Hotbar getHotbar() {
            return hotbar;
        }
    }
    
    private EnumSet<PowerClassification> prevTargetUpdateTick = EnumSet.noneOf(PowerClassification.class);
    private void checkHeldActionAndTarget(IPower<?, ?> power, boolean targetChanged) {
        boolean keyHeld;
        if (heldKeys.containsKey(power)) {
            keyHeld = heldKeys.get(power).isDown();
            if (!keyHeld) {
                heldKeys.remove(power);
            }
        }
        else {
            keyHeld = mc.options.keyAttack.isDown() || mc.options.keyUse.isDown() || mc.options.keyPickItem.isDown();
        }
        
        PowerClassification powerClass = power.getPowerClassification();
        
        if (!keyHeld && !CommandsMap.dontStopHeld) {
            Action<?> action = power.getHeldAction();
            if (action != null && !action.commitToWindup) {
                stopHeldAction(power);
            }
        }
        
        boolean targetUpdatePrevTick = prevTargetUpdateTick.contains(powerClass);
        boolean targetUpdateThisTick = power.isTargetUpdateTick();
        if (targetUpdateThisTick)   prevTargetUpdateTick.add(powerClass);
        else                        prevTargetUpdateTick.remove(powerClass);
        
        if (targetUpdateThisTick && (!targetUpdatePrevTick || targetChanged)) {
            PacketManager.sendToServer(ClHeldActionTargetPacket.withRayTraceResult(powerClass, mouseTarget));
        }
    }
    
    public <P extends IPower<P, ?>> void stopHeldAction(IPower<?, ?> p) {
        P power = (P) p;
        Action<P> heldAction = power.getHeldAction();
        if (heldAction != null) {
            boolean shouldFire = false;
            if (!heldAction.holdOnly(power)) {
                int heldForTicks = power.getHeldActionTicks();
                int ticksToFire = heldAction.getHoldDurationToFire(power);
                shouldFire = heldForTicks >= ticksToFire;
            }
            PacketManager.sendToServer(new ClStopHeldActionPacket(power.getPowerClassification(), shouldFire));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelClickInput(ClickInputEvent event) {
        if (ControllerSoul.getInstance().isCameraEntityPlayerSoul()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
        else if (nonStandPower != null) {
            nonStandPower.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (hamon.isMeditating()) {
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            });
            nonStandPower.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).ifPresent(pillarman -> {
            	if (pillarman.isStoneFormEnabled()) {
            		event.setSwingHand(false);
            	}
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void modActionClick(ClickInputEvent event) {
        doubleShift.reset();
        
        if (JojoModUtil.tmpSpectatorCantUsePowers(mc.player) || event.getHand() == InteractionHand.OFF_HAND) {
            return;
        }

        ActionKey key;
        KeyMapping keyBinding;
        if (event.isAttack()) {
            key = ActionKey.ATTACK;
            keyBinding = mc.options.keyAttack;
        }
        else if (event.isUseItem()) {
            key = ActionKey.ABILITY;
            keyBinding = mc.options.keyUse;
        }
        else {
            return;
        }
        
        HudClickResult clickResult = handleMouseClickPowerHud(key, keyBinding);
        if (clickResult.vanillaInput == HudClickResult.Behavior.CANCEL) {
            event.setCanceled(true);
        }
        if (clickResult.handSwing == HudClickResult.Behavior.CANCEL) {
            event.setSwingHand(false);
        }
    }
    
    private void clickWithBusyHands() {
        if (ClientUtil.arePlayerHandsBusy()) {
            while (mc.options.keyAttack.consumeClick()) {
                handleMouseClickPowerHud(ActionKey.ATTACK, mc.options.keyAttack);
            }
            while (mc.options.keyUse.consumeClick()) {
                handleMouseClickPowerHud(ActionKey.ABILITY, mc.options.keyUse);
            }
        }
    }
    
    private <P extends IPower<P, ?>> HudClickResult handleCustomKeybind(ActionKeybindEntry entry, P power) {
        HudClickResult result = new HudClickResult();
        if (entry.getAction() == null) return result;

        if (power != null) {
            boolean leftClickedBlock = false;
            boolean sneak = useShiftActionVariant(mc);
            boolean shiftActionVar = useShiftActionVariant(mc);
            Action<P> action = (Action<P>) entry.getAction();
            action = ActionsOverlayGui.resolveVisibleActionInSlot(
                    action, shiftActionVar, power, ActionsOverlayGui.getInstance().getMouseTarget());
            
            ActionUseTry<P> click = actionsOverlay.onActionClick(power, action, sneak, entry.getKeybind());
            if (click != null) {
                if (action != null && action.withUserPunch()) {
                    mcPlayerAttack();
                }
                if (click.wentOff) {
                    result.cancelVanillaInput();
                    if (action.getHoldDurationMax(power) > 0) {
                        heldKeys.put(power, entry.getKeybind());
                    }
                    if (!click.clientOnly) {
                        if (action != null) {
                            result.handSwing = actionSwingsHand(action, power);
                        }
                        if (leftClickedBlock && leftClickBlockDelay <= 0) {
                            leftClickBlockDelay = 4;
                        }
                    }
                    else {
                        result.handSwing = HudClickResult.Behavior.CANCEL;
                        result.cancelVanillaInput();
                    }
                }
            }
            else {
                if (heldKeys.get(power) == entry.getKeybind()) {
                    result.cancelHandSwing();
                    result.cancelVanillaInput();
                }
                else if (shouldVanillaInputStun()) {
                    result.cancelHandSwing();
                }
            }
            actionsOverlay.setCustomKeybindAction(power.getPowerClassification(), entry);
        }
        
        return result;
    }
    
    public void mcPlayerAttack() {
        if (mc.hitResult != null && !mc.player.isHandsBusy() && 
                mc.hitResult.getType() == HitResult.Type.ENTITY && isValidPlayerAttackTarget(mc.hitResult)) {
            mc.gameMode.attack(mc.player, ((EntityHitResult) mc.hitResult).getEntity());
        }
    }
    
    public boolean isValidPlayerAttackTarget(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();
            if (entity == mc.player || entity instanceof ItemProjectileEntity) {
                return false;
            }
        }
        return true;
    }
    
    private <P extends IPower<P, ?>> HudClickResult handleMouseClickPowerHud(ActionKey key, KeyMapping keyBinding) {
        HudClickResult result = new HudClickResult();
        if (JojoModUtil.tmpSpectatorCantUsePowers(mc.player)) {
            return result;
        }

        P power = (P) actionsOverlay.getCurrentPower();

        ControlScheme.Hotbar hotbar = key.getHotbar();
        boolean actionClick = false;
        if (power != null && actionsOverlay.areHotbarsEnabled()) {
            actionClick = !actionsOverlay.noActionSelected(hotbar);
        }
        
        if (!actionClick) {
            // cancel vanilla click
            if (shouldVanillaInputStun() || ContinuousActionInstance.getCurrentAction(mc.player).isPresent()) {
                result.cancelVanillaInput();
            }
            return result;
        }
        
        if (key == ActionKey.ATTACK && leftClickBlockDelay > 0) {
            result.cancelHandSwing();
            result.cancelVanillaInput();
            return result;
        }
        
        if (power != null) {
            boolean leftClickedBlock = key == ActionKey.ATTACK && mc.hitResult.getType() == Type.BLOCK;
            boolean sneak = useShiftActionVariant(mc);
            boolean shiftActionVar = useShiftActionVariant(mc);
            
            ActionUseTry<P> click = null;
//            if (key == ActionKey.QUICK_ACCESS) {
//                click = actionsOverlay.onQuickAccessClick(power, shiftActionVar, sneak);
//            } else 
            if (!(leftClickedBlock && leftClickBlockDelay > 0)) {
                click = actionsOverlay.onClick(power, key.getHotbar(), shiftActionVar, sneak, keyBinding);
            }
            if (click != null) {
                Action<P> action = click.action;
                if (action != null && action.withUserPunch()) {
                    mcPlayerAttack();
                }
                if (click.wentOff) {
                    if (action != null && action.getHoldDurationMax(power) > 0) {
                        heldKeys.put(power, key.getKey(mc, this));
                    }
                    if (!click.clientOnly) {
                        if (action != null) {
                            result.handSwing = actionSwingsHand(action, power);
                            if (!(action.withUserPunch() && key == ActionKey.ATTACK)) result.cancelVanillaInput();
                        }
                        if (leftClickedBlock && leftClickBlockDelay <= 0) {
                            leftClickBlockDelay = 4;
                        }
                    }
                    else {
                        result.handSwing = HudClickResult.Behavior.CANCEL;
                        result.cancelVanillaInput();
                    }
                }
            }
            else {
                if (heldKeys.get(power) == key.getKey(mc, this)) {
                    result.cancelHandSwing();
                    result.cancelVanillaInput();
                }
                else if (shouldVanillaInputStun()) {
                    result.cancelHandSwing();
                }
            }
        }
        
        return result;
    }
    
    public static <P extends IPower<P, ?>> HudClickResult.Behavior actionSwingsHand(Action<P> action, P power) {
        if (action.getHoldDurationMax(power) <= 0 && action.swingHand()) {
            return HudClickResult.Behavior.FORCE;
        }
        return HudClickResult.Behavior.CANCEL;
    }
    
    public static KeyMapping lastActionKey;
    
    public static boolean useShiftActionVariant(Minecraft mc) {
        return mc.player.isShiftKeyDown();
    }
    
    public static boolean renderShiftVarInScreenUI(Minecraft mc, int key, int scanCode) {
        return mc.options.keyShift.matches(key, scanCode);
    }
    
    public static class HudClickResult {
        public Behavior vanillaInput = Behavior.PASS;
        public Behavior handSwing = Behavior.PASS;
        
        public void cancelVanillaInput() {
            vanillaInput = Behavior.CANCEL;
        }
        
        public void cancelHandSwing() {
            handSwing = Behavior.CANCEL;
        }
        
        public enum Behavior {
            CANCEL,
            PASS,
            FORCE
        }
    }
    
    private boolean shouldVanillaInputStun() {
        return ModStatusEffects.isStunned(mc.player) || !mc.player.canUpdate();
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fixArrowPunchKick(ClickInputEvent event) {
        if (event.isAttack() && !isValidPlayerAttackTarget(mc.hitResult)) {
            event.setCanceled(true); // prevents kick for "Attempting to attack an invalid entity"
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void invertMovementInput(InputUpdateEvent event) {
        if (event.getPlayer() == mc.player && mc.screen instanceof WasdAllowingScreen) {
            ((WasdAllowingScreen) mc.screen).tickInput(mc, mc.player, event.getMovementInput());
        }
        
        Input input = event.getMovementInput();
        boolean hasInput = input.up || input.down || input.left || input.right || input.jumping;
        
        HamonRebuffOverdrive.onWASDInput(mc.player);
        if (GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(event.getPlayer()).resolve().flatMap(
                power -> power.getTypeSpecificData(ModPowers.HAMON.get())), hamon -> {
                    if (hamon.isMeditating()) {
                        if (hamon.getMeditationTicks() >= 40) {
                            if (hasInput) {
                                PacketManager.sendToServer(new ClHamonMeditationPacket(false));
                            }
                        }
                        input.up = false;
                        input.down = false;
                        input.left = false;
                        input.right = false;
                        input.jumping = false;
                        input.forwardImpulse = 0;
                        input.leftImpulse = 0;
                        return true;
                    }
                    return false;
                })) {
            return;
        }
        
        if (event.getPlayer().hasEffect(ModStatusEffects.MISSHAPEN_LEGS.get())) {
            input.forwardImpulse *= -1;
            input.leftImpulse *= -1;
            
            boolean tmp = input.down;
            input.down = input.up;
            input.up = tmp;
            
            tmp = input.left;
            input.left = input.right;
            input.right = tmp;
            
            tmp = input.jumping;
            input.jumping = input.shiftKeyDown;
            input.shiftKeyDown = tmp;
        }
    }
    
    private boolean wasStunned = false;
    private double prevSensitivity = 0.5;
    private static final double ZERO_SENSITIVITY = -1.0 / 3.0;
    @SubscribeEvent
    public void setMouseSensitivity(ClientTickEvent event) {
        if (mc.player == null) {
            if (mc.options.sensitivity <= ZERO_SENSITIVITY) {
                mc.options.sensitivity = prevSensitivity;
            }
            return;
        }
        
        if (ModStatusEffects.isStunned(mc.player)) {
            if (!wasStunned) {
                prevSensitivity = mc.options.sensitivity;
                wasStunned = true;
            }
            mc.options.sensitivity = ZERO_SENSITIVITY;
            return;
        }
        else if (wasStunned) {
            mc.options.sensitivity = prevSensitivity;
            wasStunned = false;
        }
        
        boolean invert = mc.player.hasEffect(ModStatusEffects.MISSHAPEN_FACE.get());
        if (invert ^ mc.options.sensitivity < 0) {
            mc.options.sensitivity = -mc.options.sensitivity + ZERO_SENSITIVITY * 2;
        }
    }
    
    private boolean mouseButtonsSwapped = false;
    private InputConstants.Input lmbKey;
    private InputConstants.Input rmbKey;
    
    public void mouseButtonsInvertTick() {
        if (!mouseButtonsSwapped) {
            lmbKey = mc.options.keyAttack.getKey();
            rmbKey = mc.options.keyUse.getKey();
            mouseButtonsSwapped = true;
        }
        
        if (mc.options.keyAttack.getKey() != rmbKey || mc.options.keyUse.getKey() != lmbKey) {
            mc.options.keyAttack.setKey(rmbKey);
            mc.options.keyUse.setKey(lmbKey);
            KeyMapping.resetMapping();
        }
    }
    
    public void mouseButtonsInvertEnd() {
        if (mouseButtonsSwapped) {
            mc.options.keyAttack.setKey(lmbKey);
            mc.options.keyUse.setKey(rmbKey);
            mouseButtonsSwapped = false;
            KeyMapping.resetMapping();
        }
    }
    
    private void tickEffects() {
        if (mc.player != null && mc.player.hasEffect(ModStatusEffects.MISSHAPEN_ARMS.get())) {
            mouseButtonsInvertTick();
        }
        else {
            mouseButtonsInvertEnd();
        }
    }
    
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInputUpdate(InputUpdateEvent event) {
        Input input = event.getMovementInput();

        Player player = (Player) event.getEntity();
        if (INonStandPower.getNonStandPowerOptional(player).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()))
                .map(PillarmanData::isStoneFormEnabled).orElse(false)) {
            input.shiftKeyDown = false;
        }
        
        mc.player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(entity -> {
            if (entity.isDyingBody() && entity.getDyingBodyTicksLeft() == 0) {
                mc.player.setSprinting(false);
            }
        });
        
        boolean hasInput = input.up || input.down || input.left || input.right || input.jumping || input.shiftKeyDown;
        if (this.hasInput != hasInput) {
            PacketManager.sendToServer(new ClHasInputPacket(hasInput));
            this.hasInput = hasInput;
        }
        
//        if (hasInput) {
            boolean slowedDown = slowDownFromHeldAction(mc.player, input, standPower);
            slowedDown = slowDownFromHeldAction(mc.player, input, nonStandPower) || slowedDown;
            slowedDown = slowDownFromStandEntity(mc.player, input) || slowedDown;
            slowedDown = slowDownFromContinuousAction(mc.player, input) || slowedDown;
            slowedDown = actionsOverlay.isPlayerOutOfBreath() && slowDown(mc.player, input, 0.8F) || slowedDown;

            canLeap = false;
            if (!mc.player.isFallFlying()) {
                IPower<?, ?> power = actionsOverlay.getCurrentPower();
                if (power != null) {
                    if (power.canLeap() && !slowedDown) {
                        Entity playerVehicle = mc.player.getVehicle();
                        // FIXME let passengers other than the controlling player leap
                        boolean onGround = false;
                        if (playerVehicle != null && playerVehicle.getType() != ModEntityTypes.ROAD_ROLLER.get()) {
                            onGround = playerVehicle.onGround()
                                    || playerVehicle.getType() == ModEntityTypes.LEAVES_GLIDER.get()
                                    && CollisionUtil.collide(playerVehicle, new Vec3(0, -1, 0)).y > -1;
                        }
                        onGround |= mc.player.onGround();
                        // TODO wall leap
                        boolean atWall = false && mc.player.horizontalCollision;
                        
                        boolean groundLeap = onGround && (mc.player.isPassenger() || input.shiftKeyDown) && input.jumping;
                        // TODO wall leap without pressing shift
                        boolean wallLeap = false;
//                                atWall && !groundLeap && input.jumping &&
//                                (!leapNeedsShiift || input.shiftKeyDown || false);
                        
                        if (groundLeap || wallLeap) {
                            float leapStrength = power.leapStrength();
                            if (leapStrength > 0) {
                                if (!mc.player.isPassenger()) {
                                    input.shiftKeyDown = false;
                                }
                                input.jumping = false;
                                
                                Entity entity = playerVehicle != null ? playerVehicle : mc.player;
                                PacketManager.sendToServer(new ClOnLeapPacket(power.getPowerClassification()));
                                IPlayerLeap.onLeapFixWrongMovement(mc.player);
                                if (groundLeap) {
                                    MCUtil.leap(entity, leapStrength);
                                }
                                else if (wallLeap) {
                                    wallLeap(mc.player, input, leapStrength);
                                }
                            }
                        }
//                        if (onGround && power.getPowerClassification() == PowerClassification.STAND) {
//                            leftDash.inputUpdate(input.left, input.right || input.down, mc.player);
//                            rightDash.inputUpdate(input.right, input.left || input.down, mc.player);
//                            backDash.inputUpdate(input.down, input.left || input.right, mc.player);
//                        }
                        canLeap = onGround || atWall;
                    }
                }
            }
//        }
        
        Entity vehicle = mc.player.getVehicle();
        if (vehicle instanceof LeavesGliderEntity) {
            ((LeavesGliderEntity) vehicle).setInput(input.left, input.right);
        }
        
        int shiftPress = doubleShift.inputUpdate(input);
        boolean pressedDoubleShift = shiftPress == 2;
        if (pressedDoubleShift && ClDoubleShiftPressPacket.Handler.sendOnPress(mc.player)) {
            mc.player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setDoubleShiftPress());
            PacketManager.sendToServer(new ClDoubleShiftPressPacket());
        }
    }
    
    public boolean canPlayerLeap() {
        return canLeap;
    }
    
    private boolean slowDownFromStandEntity(Player player, Input input) {
        if (standPower == null) return false;
        IStandManifestation stand = standPower.getStandManifestation();
        if (stand instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) stand;
            float speed = standEntity.getUserWalkSpeed();
            return slowDown(player, input, speed);
        }
        return false;
    }
    
    private boolean slowDownFromHeldAction(Player player, Input input, IPower<?, ?> power) {
        if (power == null) return false;
        Action<?> heldAction = power.getHeldAction();
        if (heldAction != null) {
            float speed = heldAction.getHeldWalkSpeed();
            return slowDown(player, input, speed);
        }
        return false;
    }
    
    private boolean slowDownFromContinuousAction(Player player, Input input) {
        Optional<ContinuousActionInstance<?, ?>> action = ContinuousActionInstance.getCurrentAction(player);
        if (action.isPresent()) {
            float speed = action.get().getWalkSpeed();
            return slowDown(player, input, speed);
        }
        return false;
    }
    
    private boolean slowDown(Player player, Input input, float speed) {
        if (speed < 1.0F) {
            input.leftImpulse *= speed;
            input.forwardImpulse *= speed;
            player.setSprinting(false);
            KeyMapping.set(mc.options.keySprint.getKey(), false);
            if (speed == 0) {
                input.jumping = false;
            }
            return true;
        }
        return false;
    }
    
    private void wallLeap(LocalPlayer player, Input input, float strength) {
        player.hasImpulse = true;
        Vec3 inputVec = new Vec3(player.xxa, 0, player.zza)
                .yRot((-player.yRot) * MathUtil.DEG_TO_RAD);
        Vec3 collide = CollisionUtil.collide(player, inputVec);
        Vec3 leap = collide.subtract(inputVec).normalize().scale(strength);
        float leapYRot = (float) -Mth.atan2(leap.x, leap.z);
        leap = leap.yRot(leapYRot)
                .xRot(-Mth.clamp(player.xRot, -82.5F, -30F) * MathUtil.DEG_TO_RAD)
                .yRot(-leapYRot);
        player.setDeltaMovement(leap.x, leap.y * 0.5, leap.z);
    }
    
    private final DashTrigger leftDash = new DashTrigger(-90F);
    private final DashTrigger rightDash = new DashTrigger(90F);
    private final DashTrigger backDash = new DashTrigger(180F);
    
    private void dash(LocalPlayer player, float yRot) {
        PacketManager.sendToServer(new ClOnStandDashPacket());
        player.setOnGround(false);
        player.hasImpulse = true;
        Vec3 dash = Vec3.directionFromRotation(0, player.yRot + yRot).scale(0.5).add(0, 0.2, 0);
        player.setDeltaMovement(player.getDeltaMovement().add(dash));
    }
    
    public void wallClimbClientTick(boolean isMoving, LivingWallClimbing wallClimbData) {
        if (this.wallClimbMoving != isMoving) {
            PacketManager.sendToServer(ClHasInputPacket.wallClimbing(isMoving));
            this.wallClimbMoving = isMoving;
            wallClimbData.wallClimbIsMoving = isMoving;
        }
    }
    
    
    
    @SubscribeEvent
    public void onKeyClick(KeyInputEvent event) {
        if (mc.screen instanceof WasdAllowingScreen) {
            ((WasdAllowingScreen) mc.screen).clickKey(mc, event.getKey(), event.getScanCode(), 
                    event.getAction(), event.getModifiers(), keyBindingMap);
        }
    }
    
    
    
    public void updatePowersCache() {
        standPower = IStandPower.getPlayerStandPower(mc.player);
        nonStandPower = INonStandPower.getPlayerNonStandPower(mc.player);
        if (standPower != null) standPower.clUpdateHud();
        if (nonStandPower != null) nonStandPower.clUpdateHud();
        heldKeys.clear();
    }
    
    
    public boolean hasPower(PowerClassification power) {
        switch (power) {
        case STAND:
            return standPower != null && standPower.hasPower();
        case NON_STAND:
            return nonStandPower != null && nonStandPower.hasPower();
        default:
            return false;
        }
    }

    public IPower<?, ?> getPowerCache(PowerClassification power) {
        switch (power) {
        case STAND:
            return standPower;
        case NON_STAND:
            return nonStandPower;
        default:
            return null;
        }
    }
    
    
    
    private class DashTrigger {
        private final float yRot;
        private int triggerTime;
        private boolean triggerGap;
        
        private DashTrigger(float yRot) {
            this.yRot = yRot;
        }
        
        private void inputUpdate(boolean keyPress, boolean anotherKeyPress, LocalPlayer player) {
            if (anotherKeyPress) {
                triggerTime = 0;
                return;
            }
            if (triggerTime > 0) {
                triggerTime--;
            }
            if (keyPress) {
                if (triggerTime > 0 && triggerGap) {
                    dash(player, yRot);
                }
                triggerTime = 7;
            }
            triggerGap = !keyPress;
        }
    }
    
    
    
    private class DoubleShiftDetector {
        private Input prevTickInput = null;
        private int shiftPresses = 0;
        private int triggerTime = 0;
        private boolean triggerGap = false;
        
        private int inputUpdate(Input playerInput) {
            boolean isShiftPressed = playerInput.shiftKeyDown;
            boolean trigger = false;
            
            if (shiftPresses > 0 && (playerInput.jumping || !checkInputUpdate(prevTickInput, playerInput))) {
                reset();
            }
            
            if (triggerTime > 0) {
                if (--triggerTime == 0) {
                    reset();
                }
            }
            
            if (isShiftPressed) {
                trigger = triggerGap && (shiftPresses == 0 || triggerTime > 0);
                if (trigger) {
                    triggerTime = 7;
                    if (shiftPresses++ == 0) {
                        saveInputState(playerInput);
                    }
                }
            }
            
            triggerGap = !isShiftPressed;
            
            return trigger ? shiftPresses : 0;
        }
        
        private boolean checkInputUpdate(@Nullable Input prevTick, Input thisTick) {
            if (prevTick == null) {
                saveInputState(thisTick);
                return true;
            }
            
            return  prevTick.up == thisTick.up &&
                    prevTick.down == thisTick.down &&
                    prevTick.left == thisTick.left &&
                    prevTick.right == thisTick.right;
        }
        
        private void saveInputState(Input input) {
            this.prevTickInput = new Input();
            this.prevTickInput.up = input.up;
            this.prevTickInput.down = input.down;
            this.prevTickInput.left = input.left;
            this.prevTickInput.right = input.right;
        }
        
        private void reset() {
            shiftPresses = 0;
            triggerTime = 0;
        }
    }
    
    
    
    public enum MouseButton {
        LEFT,
        RIGHT,
        MIDDLE;
        
        public static MouseButton getButtonFromId(int id) {
            if (id >= 0 && id < MouseButton.values().length) {
                return MouseButton.values()[id];
            }
            return null;
        }
    }
    
    private static final Int2ObjectMap<Direction2D> ARROW_KEYS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        map.put(GLFW.GLFW_KEY_LEFT,  Direction2D.LEFT);
        map.put(GLFW.GLFW_KEY_UP,    Direction2D.UP);
        map.put(GLFW.GLFW_KEY_RIGHT, Direction2D.RIGHT);
        map.put(GLFW.GLFW_KEY_DOWN,  Direction2D.DOWN);
    });
    
    @Nullable
    public static Direction2D getArrowKey(int keyCode) {
        return ARROW_KEYS.get(keyCode);
    }
}
