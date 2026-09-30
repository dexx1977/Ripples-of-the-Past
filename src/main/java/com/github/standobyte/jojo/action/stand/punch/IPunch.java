package com.github.standobyte.jojo.action.stand.punch;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

public interface IPunch {
    boolean doHit(StandEntityTask task);
    boolean targetWasHit();
    
    StandEntity getStand();
    
    SoundEvent getImpactSound();
    Vec3 getImpactSoundPos();
    default boolean playImpactSound() {
        return targetWasHit();
    }
    
    TargetType getType();
}
