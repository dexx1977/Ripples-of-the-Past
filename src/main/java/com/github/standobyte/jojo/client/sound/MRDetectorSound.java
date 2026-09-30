package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.MRDetectorEntity;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class MRDetectorSound extends AbstractTickableSoundInstance {
    private final MRDetectorEntity detector;

    public MRDetectorSound(MRDetectorEntity detector) {
        super(SoundEvents.FIRE_AMBIENT, detector.getSoundSource());
        this.detector = detector;
        this.looping = true;
        this.delay = 0;
    }

    public void tick() {
        if (!detector.canUpdate()) {
            volume = 0;
        }
        else if (detector.isAlive() && detector.isEntityDetected()) {
            x = detector.getX();
            y = detector.getY();
            z = detector.getZ();
            Vector3f detectedVec = detector.getDetectedDirection();
            volume = 1 - (Mth.sqrt(detectedVec.dot(detectedVec)) / (float) MRDetectorEntity.DETECTION_RADIUS) * 0.5F;
        } 
        else {
            stop();
        }
    }

}
