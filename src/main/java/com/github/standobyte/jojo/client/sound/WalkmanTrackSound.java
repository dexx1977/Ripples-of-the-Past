package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;

public class WalkmanTrackSound extends ResolvedLocationTickingSound {
    private int ticks = 0;
    private final int distortionLevel;

    public WalkmanTrackSound(Sound sound, SoundSource source, Component subtitle, int distortionLevel) {
        super(sound, source, subtitle);
        this.distortionLevel = distortionLevel;
        switch (distortionLevel) {
        case 1:
            pitch = 0.9875F;
            break;
        case 2:
            pitch = 0.975F;
            break;
        case 3:
            pitch = 0.95F;
            break;
        }
        
        attenuation = SoundInstance.AttenuationType.NONE;
        relative = true;
    }

    @Override
    public void tick() {
        ticks++;
        if (distortionLevel >= 4) {
            // i'm not a sadist
            // , but...
            pitch = 0.8F + (Mth.sin((float) ticks * 0.05F) + 1) * 0.05F;
        }
        
//         /*Nightcore remixes be like:*/ pitch = 1.35F;
    }
    
    public void setVolume(float volume) {
        this.volume = volume;
    }
    
    @Override
    public boolean canStartSilent() {
        return true;
    }
}
