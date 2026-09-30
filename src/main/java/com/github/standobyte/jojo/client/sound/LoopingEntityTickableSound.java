package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public class LoopingEntityTickableSound extends EntityBoundSoundInstance {

    public LoopingEntityTickableSound(SoundEvent soundEvent, SoundSource soundCategory, 
            float volume, float pitch, boolean looping, Entity entity) {
        super(soundEvent, soundCategory, volume, pitch, entity);
        this.looping = looping;
    }

}
