package com.github.standobyte.jojo.client.sound.loopplayer;

import java.util.Random;

import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

@Deprecated
public abstract class SoundLoopPlayer {
    protected static final Random RANDOM = new Random();
    protected final Level world;
    private SoundEvent sound;
    private SoundSource soundCategory;
    private float volume;
    private float pitch;
    private int tickCount = 0;
    private int nextSoundIn = 0;
    protected boolean playedSoundThisTick;
    private boolean stopped = false;
    
    public SoundLoopPlayer(Level world, SoundEvent sound, SoundSource soundCategory, float volume, float pitch) {
        this.world = world;
        this.sound = sound;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public void tick() {
        if (!continuePlaying()) {
            stopped = true;
            return;
        }
        
        if (tickCount++ >= nextSoundIn) {
            Vec3 pos = soundPos();
            world.playLocalSound(pos.x, pos.y, pos.z, sound, soundCategory, volume, pitch, true);
            nextSoundIn = tickCount + soundDelayTicks();
            playedSoundThisTick = true;
        }
        else {
            playedSoundThisTick = false;
        }
    }
    
    protected abstract int soundDelayTicks();
    protected abstract boolean continuePlaying();
    protected abstract Vec3 soundPos();
    
    public boolean isStopped() {
        return stopped;
    }
}
