package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.Playlist;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

public class WalkmanRewindSound extends AbstractTickableSoundInstance {

    public WalkmanRewindSound() {
        super(ModSounds.WALKMAN_REWIND.get(), SoundSource.MASTER);
        looping = true;
        x = 0;
        y = 0;
        z = 0;
        attenuation = SoundInstance.AttenuationType.NONE;
        relative = true;
    }

    @Override
    public void tick() {
        Playlist walkmanPlaylist = WalkmanSoundHandler.getCurrentPlaylist();
        if (walkmanPlaylist == null || walkmanPlaylist.getRewindSoundTicks() <= 0) {
            stop();
        }
    }

}
