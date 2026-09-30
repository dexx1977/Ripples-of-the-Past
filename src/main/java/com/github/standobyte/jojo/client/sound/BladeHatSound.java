package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;

public class BladeHatSound extends AbstractTickableSoundInstance {
    private final BladeHatEntity hat;

    public BladeHatSound(BladeHatEntity hat) {
        super(ModSounds.BLADE_HAT_SPINNING.get(), hat.getSoundSource(), SoundInstance.createUnseededRandom());
        this.hat = hat;
        this.looping = true;
        this.delay = 0;
    }

    public void tick() {
        if (!hat.canUpdate()) {
            volume = 0;
        }
        else if (hat.isAlive() && !hat.isInGround()) {
            x = hat.getX();
            y = hat.getY();
            z = hat.getZ();
            volume = (float) hat.getDeltaMovement().length();
        } 
        else {
            stop();
        }
    }
}
