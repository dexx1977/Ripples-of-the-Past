package com.github.standobyte.jojo.client.ui.screen;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.network.chat.Component;
import com.github.standobyte.jojo.client.KeyMappingLookup;
import net.minecraftforge.client.settings.KeyConflictContext;

public class WasdAllowingScreen extends Screen {
    /* 
     * TODO
     * sprint
     * keep the previous motion
     */

    public WasdAllowingScreen(Component pTitle) {
        super(pTitle);
        saveHeldKeyBinds();
        heldKeyBinds.forEach(keybind -> keybind.setDown(true));
    }
    
    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void setFocused(@Nullable GuiEventListener pListener) {
    } // otherwise space will press focused buttons instead of jumping
    
    protected void doSetFocused(@Nullable GuiEventListener pListener) {
        super.setFocused(pListener);
    }
    
    
    protected Collection<KeyMapping> heldKeyBinds;
    private void saveHeldKeyBinds() {
        Collection<KeyMapping> allKeyBindings = ClientReflection.getKeyBindingsMap().values();
        heldKeyBinds = allKeyBindings.stream().filter(KeyMapping::isDown).collect(Collectors.toList());
    }
    
    @Override
    protected void init() {
        super.init();
    }
    
    
    
    public void tickInput(Minecraft mc, LocalPlayer player, Input input) {
        if (!KeyConflictContext.IN_GAME.isActive() && input instanceof KeyboardInput && acceptsKeyInput()) {
            boolean isMovingSlowly = player.isMovingSlowly();
            
            input.up = isDownNoConflictContext(mc.options.keyUp);
            input.down = isDownNoConflictContext(mc.options.keyDown);
            input.left = isDownNoConflictContext(mc.options.keyLeft);
            input.right = isDownNoConflictContext(mc.options.keyRight);
            input.jumping = isDownNoConflictContext(mc.options.keyJump);
            input.shiftKeyDown = isDownNoConflictContext(mc.options.keyShift);
            
            input.forwardImpulse = input.up == input.down ? 0.0F : (input.up ? 1.0F : -1.0F);
            input.leftImpulse = input.left == input.right ? 0.0F : (input.left ? 1.0F : -1.0F);
            if (isMovingSlowly) {
                input.leftImpulse *= 0.3F;
                input.forwardImpulse *= 0.3F;
            }
        }
    }
    
    public void clickKey(Minecraft mc, int key, int scanCode, int action, int modifiers, KeyMappingLookup keyBindingMap) {
        if (action == GLFW.GLFW_RELEASE || !acceptsKeyInput()) return;
        
        InputConstants.Key inputmappings$input = InputConstants.getKey(key, scanCode);
        
        for (KeyMapping keybinding : keyBindingMap.lookupAll(inputmappings$input)) {
            if (keybinding != null) {
                clickIfKeyIs(keybinding, mc.options.keyTogglePerspective);
            }
        }
    }
    
    private void clickIfKeyIs(KeyMapping keyPressed, KeyMapping keyNeeded) {
        if (keyPressed == keyNeeded && keyPressed.getKeyModifier().isActive(null) && !keyPressed.getKeyConflictContext().isActive()) {
            ClientReflection.setClickCount(keyPressed, ClientReflection.getClickCount(keyPressed) + 1);
        }
    }
    
    /**
     * Some keybinds only work when there's no screen opened, so they need to bypass the check
     * {@link net.minecraft.client.GameSettings#setForgeKeybindProperties}
     */
    private static boolean isDownNoConflictContext(KeyMapping keyBinding) {
        return keyBinding.getKeyModifier().isActive(null) && ClientReflection.isDownFieldOnly(keyBinding);
    }
    
    public boolean acceptsKeyInput() {
        return true;
    }
}
