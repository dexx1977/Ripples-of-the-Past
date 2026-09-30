package com.github.standobyte.jojo.client.sound;

import javax.annotation.Nullable;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.sounds.SoundInstance;

public class ResolvedLocationSimpleSound extends AbstractSoundInstance {
    protected final Component subtitle;

    public ResolvedLocationSimpleSound(Sound sound, SoundSource source) {
        this(sound, source, null);
    }

    public ResolvedLocationSimpleSound(Sound sound, SoundSource source, @Nullable Component subtitle) {
        super((sound != null ? sound : SoundManager.EMPTY_SOUND).getLocation(), source, SoundInstance.createUnseededRandom());
        this.sound = sound != null ? sound : SoundManager.EMPTY_SOUND;
        this.subtitle = subtitle;
    }
    
    @Override
    public ResourceLocation getLocation() {
        return sound.getLocation();
    }
    
    @Override
    public WeighedSoundEvents resolve(SoundManager soundManager) {
        return new EventlessSoundAccessor(sound.getLocation(), subtitle, sound);
    }

}
