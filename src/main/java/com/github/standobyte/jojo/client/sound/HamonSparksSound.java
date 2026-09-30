package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.resources.sounds.SoundInstance;

public class HamonSparksSound extends AbstractTickableSoundInstance {
    private final Entity entity;

    public HamonSparksSound(Entity entity, float volume, float pitch) {
        super(ModSounds.HAMON_SPARKS_LONG.get(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.volume = volume;
        this.pitch = pitch;
        this.entity = entity;
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
    }

    public void tick() {
        if (entity.isAlive()) {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        } 
    }
}
