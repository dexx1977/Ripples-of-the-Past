package com.github.standobyte.jojo.init.power;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Registry identity for ROTP's custom value types. Forge 1.20 no longer stores
 * registry names in the values themselves: the owning registry is authoritative.
 * Keeping this small interface preserves the power/action architecture without
 * duplicating names or relying on the removed ForgeRegistryEntry implementation.
 */
public interface RegistryEntry<T> {
    IForgeRegistry<T> getRegistry();

    @Nullable
    @SuppressWarnings("unchecked")
    default ResourceLocation getRegistryName() {
        return getRegistry().getKey((T) this);
    }
}
