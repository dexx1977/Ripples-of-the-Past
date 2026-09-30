package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;

public class ResolvedLocationTickingSound extends ResolvedLocationSimpleSound implements TickableSoundInstance {
    private boolean stopped;

    public ResolvedLocationTickingSound(Sound sound, SoundSource source, Component subtitle) {
        super(sound, source, subtitle);
    }

    public ResolvedLocationTickingSound(Sound sound, SoundSource source) {
        super(sound, source);
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void tick() {}
    
    public void stop() {
        stopped = true;
        looping = false;
    }

}
