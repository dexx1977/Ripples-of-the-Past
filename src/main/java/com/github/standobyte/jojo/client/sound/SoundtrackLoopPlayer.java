package com.github.standobyte.jojo.client.sound;

import java.util.OptionalInt;

import org.lwjgl.openal.AL10;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.SoundManager;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;

public class SoundtrackLoopPlayer {
    protected final LivingEntity bossEntity;
    protected final SoundEvent start;
    protected final SoundEvent main;
    protected final SoundEvent finish;
    protected SoundSource category;
    protected float volume;
    protected float pitch;

    boolean queuedMain = false;
    boolean setLooped = false;
    boolean finished = false;

    protected SoundInstance startingSound;
    protected ChannelAccess.Entry channelEntry;
    protected OptionalInt soundSourceID = OptionalInt.empty();
    protected OptionalInt startingSoundBuffer = OptionalInt.empty();

    public SoundtrackLoopPlayer(LivingEntity entity, SoundEvent start, SoundEvent main, SoundEvent finish) {
        this(entity, start, main, finish, SoundSource.RECORDS, 0.4f, 1);
    }

    public SoundtrackLoopPlayer(LivingEntity entity, SoundEvent start, SoundEvent main, SoundEvent finish, 
            SoundSource category, float volume, float pitch) {
        this.start = start;
        this.main = main;
        this.finish = finish;
        this.bossEntity = entity;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    protected void start() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        startingSound = new BackgroundSound(start.getLocation(), category, volume, pitch, false, 0, SoundInstance.AttenuationType.NONE, 0, 0, 0, false);
        soundManager.play(startingSound);
    }
    
    protected void onSoundSourceEvent(SoundSourceEvent event) {
        if (!queuedMain && startingSound != null && event.getSound() == startingSound) {
            SoundEngine soundEngine = event.getManager();
            Channel startSoundSource = event.getSource();

            SoundInstance iMainSound = new SimpleSoundInstance(main.getLocation(), startingSound.getSource(), 
                    startingSound.getVolume(), startingSound.getPitch(), true, 0, startingSound.getAttenuation(), 
                    startingSound.getX(), startingSound.getY(), startingSound.getZ(), startingSound.isRelative());
            WeighedSoundEvents mainSoundAccessor = iMainSound.resolve(soundEngine.soundManager);
            if (mainSoundAccessor != null) {
                Sound mainSound = iMainSound.getSound();
                if (mainSound != SoundManager.EMPTY_SOUND) {
                    ResourceLocation mainSoundPath = mainSound.getPath();
//                    boolean isStartSoundStream = event instanceof PlayStreamingSourceEvent;
//                    boolean isMainSoundStream = mainSound.shouldStream();
                    SoundBufferLibrary soundBuffers = ClientReflection.getSoundBuffers(soundEngine); // TODO cache this
                    
//                    if (!isMainSoundStream) {
                        soundBuffers.getCompleteBuffer(startingSound.getSound().getPath()).thenAccept(startingAudioStream -> 
                        soundBuffers.getCompleteBuffer(mainSoundPath).thenAccept(mainAudioStream -> {
                            ClientReflection.getAlBuffer(startingAudioStream).ifPresent(buffer1 -> 
                            ClientReflection.getAlBuffer(mainAudioStream).ifPresent(buffer2 -> {
                                startingSoundBuffer = OptionalInt.of(buffer1);
                                soundSourceID = OptionalInt.of(ClientReflection.getSourceId(startSoundSource));
                                startSoundSource.stop();
                                AL10.alSourcei(soundSourceID.getAsInt(), AL10.AL_BUFFER, 0);
                                AL10.alSourceQueueBuffers(soundSourceID.getAsInt(), buffer1);
                                AL10.alSourceQueueBuffers(soundSourceID.getAsInt(), buffer2);
                                startSoundSource.play();
                            }));
                        }));
//                    } else {
                        // TODO ???
//                    }
                }
            }
            
            queuedMain = true;
        }
    }
    
    @SuppressWarnings("deprecation")
    public void tick() {
        if (bossEntity != null) {
            if (bossEntity.isDeadOrDying()) {
                finish();
                return;
            }
            else if (bossEntity.isRemoved()) {
                forceStop();
                return;
            }
        }
        
        
        if (!setLooped) {
            soundSourceID.ifPresent(soundSource -> {
                startingSoundBuffer.ifPresent(buffer -> {
                    int curBuffer = AL10.alGetSourcei(soundSource, AL10.AL_BUFFERS_PROCESSED);
                    boolean startingSoundStopped = curBuffer == 1;
                    if (startingSoundStopped) {
                        AL10.alSourceUnqueueBuffers(soundSource, new int[] { buffer });
                        AL10.alSourcei(soundSource, AL10.AL_LOOPING, 1);
                        setLooped = true;
                    }
                });
            });
        }
    }
    
    public void finish() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (!finished) {
            SoundInstance sound = new SimpleSoundInstance(finish.getLocation(), category, volume, pitch, false, 0, SoundInstance.AttenuationType.NONE, 0, 0, 0, false);
            soundManager.play(sound);
        }
        forceStop();
    }
    
    public void forceStop() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (startingSound != null) {
            soundManager.stop(startingSound);
            startingSound = null;
        }
        finished = true;
    }
    
    public boolean hasFinished() {
        return finished;
    }
    
    
    
    public static class BackgroundSound extends SimpleSoundInstance {

        public BackgroundSound(ResourceLocation location, SoundSource source, float volume,
                float pitch, boolean looping, int delay, AttenuationType attenuation,
                double x, double y, double z, boolean isRelative) {
            super(location, source, volume, pitch, looping, delay, attenuation, x,
                    y, z, isRelative);
        }
        
        @Override
        public boolean canStartSilent() {
            return true;
        }
        
    }
    
}
