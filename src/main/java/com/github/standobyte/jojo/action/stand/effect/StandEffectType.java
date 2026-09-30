package com.github.standobyte.jojo.action.stand.effect;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;

import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import com.github.standobyte.jojo.init.power.RegistryEntry;

public class StandEffectType<T extends StandEffectInstance> implements RegistryEntry<StandEffectType<?>> {
    @Override
    public IForgeRegistry<StandEffectType<?>> getRegistry() {
        return JojoCustomRegistries.STAND_EFFECTS.getRegistry();
    }

    private IFactory<T> factory;
    
    public StandEffectType(IFactory<T> factory) {
        this.factory = factory;
    }
    
    @Deprecated
    public T create() {
        return create(null);
    }
    
    public T create(Level world) {
        T effect = factory.create(this);
        effect.world = world;
        return effect;
    }
    
    
    
    public interface IFactory<T extends StandEffectInstance> {
        T create(StandEffectType<T> effect);
    }
}
