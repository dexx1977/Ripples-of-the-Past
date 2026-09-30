package com.github.standobyte.jojo.action.stand.punch;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

public class StandMissedPunch implements IPunch {
    public final StandEntity stand;
    private Supplier<SoundEvent> swingSound = () -> null;
    
    public StandMissedPunch(StandEntity stand) {
        this.stand = stand;
    }

    @Override
    public boolean doHit(StandEntityTask standTask) {
        return false;
    }

    @Override
    public boolean targetWasHit() {
        return false;
    }
    
    @Override
    public StandEntity getStand() {
        return stand;
    }
    
    public StandMissedPunch swingSound(Supplier<SoundEvent> sound) {
        this.swingSound = sound;
        return this;
    }

    @Override
    public SoundEvent getImpactSound() {
        return swingSound != null ? swingSound.get() : null;
    }

    @Override
    public Vec3 getImpactSoundPos() {
        return stand.position().add(
                new Vec3(0, stand.getBbHeight() * 0.75F, stand.getBbWidth())
                .yRot((180 - stand.yRot) * MathUtil.DEG_TO_RAD));
    }
    
    @Override
    public TargetType getType() {
        return TargetType.EMPTY;
    }
}
