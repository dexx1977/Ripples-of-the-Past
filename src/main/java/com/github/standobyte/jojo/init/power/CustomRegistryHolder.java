package com.github.standobyte.jojo.init.power;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.power.IPowerType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import com.github.standobyte.jojo.init.power.RegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class CustomRegistryHolder<V> {
    private final DeferredRegister<V> deferredRegister;
    private Supplier<IForgeRegistry<V>> registrySupplier = null;
    
    public CustomRegistryHolder(DeferredRegister<V> deferredRegister, String name) {
        this.deferredRegister = deferredRegister;
        if (!deferredRegister.getRegistryName().getPath().equals(name)) {
            throw new IllegalArgumentException("Mismatched custom registry name: " + name);
        }
    }
    
    public void initRegistry(IEventBus modEventBus) {
        if (registrySupplier == null) {
            registrySupplier = deferredRegister.makeRegistry(() -> new RegistryBuilder<>());
            deferredRegister.register(modEventBus);
        }
    }
    
    public IForgeRegistry<V> getRegistry() {
        return registrySupplier.get();
    }
    
    @Nullable
    public V getValue(ResourceLocation id) { // why do registries even have default values?
        IForgeRegistry<V> registry = getRegistry();
        return registry.containsKey(id) ? registry.getValue(id) : null;
    }
    
    @Nonnull
    public String getKeyAsString(V powerType) {
        ResourceLocation resourceLocation = getRegistry().getKey(powerType);
        if (resourceLocation == null) {
           return IPowerType.NO_POWER_NAME;
        }
        return resourceLocation.toString();
    }
    
    public int getNumericId(ResourceLocation regName) {
        return ((ForgeRegistry<V>) getRegistry()).getID(regName);
    }
    
    /**
     * @deprecated A long time ago I made two methods that do the same thing on accident. 
     * Please use {@link CustomRegistryHolder#getValue(ResourceLocation)} instead.
     */
    @Deprecated
    public V fromId(ResourceLocation id) {
        return getValue(id);
    }
    
}
