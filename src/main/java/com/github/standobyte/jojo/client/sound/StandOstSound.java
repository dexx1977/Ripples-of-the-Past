package com.github.standobyte.jojo.client.sound;

import java.util.ConcurrentModificationException;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public class StandOstSound extends AbstractTickableSoundInstance implements TickableSoundInstance {
    private int fadeAwayTicks = -1;
    private int fadeAwayInitialTicks = -1;
    
    @Nullable
    private final Options options;
    private final float musicVolume;

    public StandOstSound(SoundEvent sound, Minecraft mc) {
        super(sound, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.looping = false;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
        
        Options options = mc.options;
        this.musicVolume = options.getSoundSourceVolume(SoundSource.MUSIC);
        try {
            // 1.20.1 sets a sound source volume through its option instance
            options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.0D);
        }
        catch (ConcurrentModificationException e) {
            JojoMod.getLogger().warn("Failed setting Minecraft music volume to 0 when playing OST.");
            options = null;
        }
        this.options = options;
    }

    @Override
    public void tick() {
        if (!isStopped()) {
            if (fadeAwayInitialTicks > -1 && fadeAwayTicks > 0) {
                volume = (float) fadeAwayTicks-- / (float) fadeAwayInitialTicks;
            }
            if (fadeAwayTicks == 0) {
                stopOst();
            }
        }
    }
    
    private void stopOst() {
        stop();
        if (options != null) {
            options.getSoundSourceOptionInstance(SoundSource.MUSIC).set((double) musicVolume);
        }
    }
    
    public void setFadeAway(int ticks) {
        if (ticks > -1 && this.fadeAwayInitialTicks == -1) {
            this.fadeAwayTicks = ticks;
            this.fadeAwayInitialTicks = ticks;
        }
    }
}
