package com.github.standobyte.jojo.client.sound.loopplayer;

import java.util.function.Predicate;

import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

@Deprecated
public abstract class EntitySoundLoopPlayer<T extends Entity> extends SoundLoopPlayer {
    protected final T entity;
    protected final Predicate<T> playWhile;
    
    public EntitySoundLoopPlayer(T entity, Predicate<T> playWhile, SoundEvent sound, SoundSource soundCategory, float volume, float pitch) {
        super(entity.level, sound, soundCategory, volume, pitch);
        this.entity = entity;
        this.playWhile = playWhile;
    }
    
    @Override
    protected boolean continuePlaying() {
        return entity.isAlive() && playWhile.test(entity);
    }
}
