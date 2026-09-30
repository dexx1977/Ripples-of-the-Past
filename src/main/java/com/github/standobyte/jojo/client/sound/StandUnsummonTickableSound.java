package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.client.resources.sounds.SoundInstance;

public class StandUnsummonTickableSound extends EntityBoundSoundInstance {
    private StandEntity stand;
    
    public StandUnsummonTickableSound(SoundEvent sound, SoundSource category, 
            float volume, float pitch, LivingEntity standUser, StandEntity stand) {
        super(sound, category, volume, pitch, standUser, SoundInstance.createUnseededRandom().nextLong());
        this.stand = stand;
    }

    @Override
    public void tick() {
        if (stand != null && !stand.isAlive()) {
            stand = null;
        }
        if (stand != null && stand.getCurrentTaskAction() != ModStandsInit.UNSUMMON_STAND_ENTITY.get()) {
            stop();
        }
        else {
            super.tick();
        }
    }
}
