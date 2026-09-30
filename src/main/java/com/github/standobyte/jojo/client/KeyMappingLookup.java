package com.github.standobyte.jojo.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Finds the key mappings bound to a key.
 *
 * <p>Forge's {@code KeyBindingMap} was removed in 1.20.1; the mappings the client
 * knows about are in the options, so the lookups are done over those.</p>
 */
public class KeyMappingLookup {

    public List<KeyMapping> lookupAll(InputConstants.Key key) {
        List<KeyMapping> mappings = new ArrayList<>();
        // 1.16.5 read Forge's KeyBindingMap, which is KeyMapping.ALL in 1.20.1
        java.util.Map<String, KeyMapping> all = com.github.standobyte.jojo.util.mc.reflection.ClientReflection.getAllKeybindingMap();
        Iterable<KeyMapping> mappingsToCheck = all != null ? all.values() : java.util.Arrays.asList(Minecraft.getInstance().options.keyMappings);
        for (KeyMapping mapping : mappingsToCheck) {
            if (key.equals(mapping.getKey())) {
                mappings.add(mapping);
            }
        }
        return mappings;
    }

    /** The mapping that should consume the key, preferring one that is held down. */
    public KeyMapping lookupActive(InputConstants.Key key) {
        List<KeyMapping> mappings = lookupAll(key);
        for (KeyMapping mapping : mappings) {
            if (mapping.isDown()) {
                return mapping;
            }
        }
        return mappings.isEmpty() ? null : mappings.get(0);
    }
}
