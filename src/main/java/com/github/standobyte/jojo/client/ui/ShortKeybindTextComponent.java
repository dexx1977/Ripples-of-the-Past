package com.github.standobyte.jojo.client.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;

public class ShortKeybindTextComponent implements Component {
    protected static final Map<String, Component> SHORT_NAMES = new HashMap<>();
    protected final KeyMapping key;
    protected Supplier<Component> nameResolver;

    public ShortKeybindTextComponent(@Nonnull KeyMapping key) {
        this.key = key;
    }
    
    protected Component getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = resolveKey(key);
        }

        return this.nameResolver.get();
    }
    
    protected Supplier<Component> resolveKey(KeyMapping key) {
        return () -> key.getKeyModifier().getCombinedName(key.getKey(), () -> getDisplayName(key.getKey()));
    }
    
    protected Component getDisplayName(InputConstants.Key input) {
        if (SHORT_NAMES.containsKey(input.getName())) {
//            return SHORT_NAMES.get(input.getName());
        }
        Component translatedName;
        int value = input.getValue();
        String name = input.getName();
        switch (input.getType()) {
        case KEYSYM:
            String s = GLFW.glfwGetKeyName(value, -1);
            if (s != null) {
                translatedName = Component.literal(s);
            }
            else {
                translatedName = Component.translatable(ClientUtil.getShortenedTranslationKey(name));
            }
            break;
        case SCANCODE:
            String s2 = GLFW.glfwGetKeyName(-1, value);
            if (s2 != null) {
                translatedName = Component.literal(s2);
            }
            else {
                translatedName = Component.translatable(ClientUtil.getShortenedTranslationKey(name));
            }
            break;
        case MOUSE:
            if (Language.getInstance().has(name)) {
                translatedName = Component.translatable(ClientUtil.getShortenedTranslationKey(name));
            }
            else {
                translatedName = Component.translatable(ClientUtil.getShortenedTranslationKey("key.mouse"), value + 1);
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
        SHORT_NAMES.put(input.getName(), translatedName);
        return translatedName;
    }

    // 1.20.1 has no visitSelf hook, the component contents are delegated instead
    @Override
    public Style getStyle() {
        return getNestedComponent().getStyle();
    }

    @Override
    public net.minecraft.network.chat.ComponentContents getContents() {
        return getNestedComponent().getContents();
    }

    @Override
    public List<Component> getSiblings() {
        return getNestedComponent().getSiblings();
    }

    @Override
    public net.minecraft.util.FormattedCharSequence getVisualOrderText() {
        return getNestedComponent().getVisualOrderText();
    }

    public ShortKeybindTextComponent plainCopy() {
        return new ShortKeybindTextComponent(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Component)) {
            return false;
        } else {
            return this.key.equals(((ShortKeybindTextComponent) obj).key) && super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return "ShortKeybindComponent{keybind='" + key.getName() + '\'' + ", siblings=" + getSiblings() + ", style=" + getStyle() + '}';
    }

}
