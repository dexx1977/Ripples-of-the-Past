package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class GliderFlightSound extends AbstractTickableSoundInstance {
    private final LeavesGliderEntity glider;
    private int time;
    private float trueVolume;
    
    public GliderFlightSound(LeavesGliderEntity glider) {
        super(ModSounds.GLIDER_FLIGHT.get(), SoundSource.AMBIENT);
        this.glider = glider;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.05F;
    }
    
    private static final float VOLUME_HIGHER_PITCH = 0.8F;
    public void tick() {
        ++time;
        if (glider.isAlive() && (time <= 20 || glider.isFlying())) {
            volume = trueVolume;
            
            x = glider.getX();
            y = glider.getY();
            z = glider.getZ();
            double movementSqr = glider.getDeltaMovement().lengthSqr();
            if (movementSqr >= 1.0E-7D) {
                volume = Mth.clamp((float) movementSqr * 1.5F, 0.0F, 1.0F);
            } 
            else {
                volume = 0;
            }

            if (time < 20) {
                volume = 0;
            } 
            else if (time < 40) {
                volume *= (float) (time - 20) / 20.0F;
            }

            if (volume > VOLUME_HIGHER_PITCH) {
                pitch = 1.0F + (volume - VOLUME_HIGHER_PITCH);
            } 
            else {
                pitch = 1.0F;
            }
            
            trueVolume = volume;
            manualAttenuation();
        } 
        else {
            stop();
        }
    }
    
    // looping sounds only change position when the sound plays over, the elytra loop sound is too long for that
    private void manualAttenuation() {
        if (attenuation == SoundInstance.AttenuationType.LINEAR) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.getVehicle() != glider) {
                Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
                Vec3 vecTo = new Vec3(x, y, z).subtract(cameraPos);
                double maxDist = getSound().getAttenuationDistance();
                volume *= Math.max(1 - vecTo.length() / maxDist, 0);
            }
        }
    }

}
