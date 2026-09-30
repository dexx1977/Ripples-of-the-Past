package com.github.standobyte.jojo.client.sound;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.barrage.StandCrySoundHandler;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public abstract class ClientTickingSoundsHelper {
    
    public static void tickBossMusic() {
        if (tickLoopPlayer != null) {
            tickLoopPlayer.tick();
        }
    }
    
    public static boolean playVoiceLine(Entity entity, SoundEvent soundEvent, SoundSource category, float volume, float pitch, boolean interrupt) {
        if (soundEvent == null || !ClientModSettings.getSettingsReadOnly().characterVoiceLines) {
            voiceLineNotTriggered(entity);
            return false;
        }
        
        Minecraft mc = Minecraft.getInstance();
        
        PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtEntity(mc.player, soundEvent, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) {
            voiceLineNotTriggered(entity);
            return false;
        }
        soundEvent = event.getSound();
        category = event.getSource();
        volume = event.getOriginalVolume();
        pitch = event.getPitch();

        SoundInstance sound = new EntityBoundSoundInstance(soundEvent, category, volume, pitch, entity);
        if (entity instanceof AbstractClientPlayer && GeneralUtil.orElseFalse(entity.getCapability(ClientPlayerUtilCapProvider.CAPABILITY), cap -> {
            boolean alreadyPlaying = !interrupt && cap.isVoiceLinePlaying();
            if (alreadyPlaying) {
                cap.lastVoiceLineTriggered = false;
            }
            else {
                cap.lastVoiceLineTriggered = true;
                cap.setCurrentVoiceLine(sound);
            }
            return !alreadyPlaying;
        })) {
            mc.getSoundManager().play(sound);
            return true;
        }
        else {
            return false;
        }
    }
    
    public static void voiceLineNotTriggered(Entity entity) {
        if (entity instanceof AbstractClientPlayer) {
            entity.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.lastVoiceLineTriggered = false;
            });
        }
    }
    
    public static void playStandEntityCancelableActionSound(StandEntity stand, SoundEvent sound, 
            StandEntityAction action, @Nullable StandEntityAction.Phase phase, float volume, float pitch, boolean looping) {
        Minecraft mc = Minecraft.getInstance();
        if (!stand.isVisibleForAll() && !ClientUtil.canHearStands()) {
            return;
        }
        
        SoundSource category = stand.getSoundSource();
        PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtEntity(stand, sound, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        category = event.getSource();
        volume = event.getOriginalVolume();
        pitch = event.getPitch();
        
        SoundInstance soundPlayed = new StoppableEntityTickableSound<StandEntity>(sound, category, volume, pitch, looping, stand, 
                e -> e.getCurrentTaskAction() == action && (phase == null || e.getCurrentTaskPhase().map(stPhase -> stPhase == phase).orElse(false)));
        mc.getSoundManager().play(soundPlayed);
    }
    
    public static void playEndlessStandCrySound(StandEntity stand, SoundEvent sound, 
            StandEntityAction action, @Nullable StandEntityAction.Phase phase, float volume, float pitch) {
        if (!stand.isVisibleForAll() && !ClientUtil.canHearStands()) {
            return;
        }
        
        SoundSource category = stand.getSoundSource();
        PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtEntity(stand, sound, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        category = event.getSource();
        volume = event.getOriginalVolume();
        pitch = event.getPitch();
        
        StandCrySoundHandler.create(category, volume, pitch, false, stand, 
                e -> e.getCurrentTaskAction() == action && (phase == null || e.getCurrentTaskPhase().map(stPhase -> stPhase == phase).orElse(false)),
                sound);
    }
    
    public static void playStandEntityUnsummonSound(StandEntity stand, SoundEvent sound, float volume, float pitch) {
        if (stand.tickCount > 20) {
            LivingEntity user = stand.getUser();
            if (user != null) {
                Minecraft mc = Minecraft.getInstance();
                if (!stand.isVisibleForAll() && !ClientUtil.canHearStands()) {
                    return;
                }

                SoundSource category = stand.getSoundSource();
                PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtEntity(stand, sound, category, volume, pitch);
                if (event.isCanceled() || event.getSound() == null) return;
                sound = event.getSound();
                category = event.getSource();
                volume = event.getOriginalVolume();
                pitch = event.getPitch();

                mc.getSoundManager().play(new StandUnsummonTickableSound(sound, category, volume, pitch, user, stand));
            }
        }
    }
    
    public static void playEntitySound(Entity entity, SoundEvent sound, float volume, float pitch) {
        if (entity instanceof StandEntity && !((StandEntity) entity).isVisibleForAll() && !ClientUtil.canHearStands()) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        SoundSource category = entity.getSoundSource();
        PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtEntity(mc.player, sound, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        category = event.getSource();
        volume = event.getOriginalVolume();
        mc.getSoundManager().play(new EntityBoundSoundInstance(sound, category, volume, pitch, entity));
    }
    
    public static void playHeldActionSound(SoundEvent sound, float volume, float pitch, boolean looping, 
            LivingEntity entity, IPower<?, ?> power, Action<?> action) {
        playHeldActionSound(sound, volume, pitch, looping, entity, power, action, 0);
    }
    
    public static void playHeldActionSound(SoundEvent sound, float volume, float pitch, boolean looping, 
            LivingEntity entity, IPower<?, ?> power, Action<?> action, int fadeOut) {
        Minecraft.getInstance().getSoundManager().play(new StoppableEntityTickableSound<LivingEntity>(sound, entity.getSoundSource(), 
                volume, pitch, looping, entity, 
                e -> power.getHeldAction(true) == action)
                .withFadeOut(fadeOut));
    }
    
    public static void playEntitySound(Entity entity, SoundEvent sound, float volume, float pitch, boolean looping) {
        Minecraft.getInstance().getSoundManager().play(new LoopingEntityTickableSound(sound, entity.getSoundSource(), volume, pitch, looping, entity));
    }
    
    public static void playEntitySound(Entity entity, SoundEvent sound, SoundSource soundSource, float volume, float pitch, boolean looping) {
        Minecraft.getInstance().getSoundManager().play(new LoopingEntityTickableSound(sound, soundSource, volume, pitch, looping, entity));
    }
    
    public static <T extends Entity> void playStoppableEntitySound(T entity, SoundEvent sound, 
            float volume, float pitch, boolean looping, Predicate<T> playWhile) {
        playStoppableEntitySound(entity, sound, volume, pitch, looping, playWhile, 0);
    }
    
    public static <T extends Entity> void playStoppableEntitySound(T entity, SoundEvent sound, 
            float volume, float pitch, boolean looping, Predicate<T> playWhile, int fadeOut) {
        Minecraft.getInstance().getSoundManager().play(new StoppableEntityTickableSound<T>(sound, entity.getSoundSource(), 
                volume, pitch, looping, entity, playWhile).withFadeOut(fadeOut));
    }
    
    public static void playHamonSparksSound(Entity entity, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new HamonSparksSound(entity, volume, pitch));
    }
    
    public static void playHamonEnergyConcentrationSound(LivingEntity entity, float volume, Action<?> action) {
        Minecraft.getInstance().getSoundManager().play(new HamonEnergySound(entity, volume, 1.0F, action));
    }
    
    public static void playGliderFlightSound(LeavesGliderEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new GliderFlightSound(entity));
    }
    
    public static void playBladeHatSound(BladeHatEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new BladeHatSound(entity));
    }
    
    public static void playTommyGunLoop(LivingEntity entity, SoundEvent sound, float volume, ItemStack stack) {
        Minecraft.getInstance().getSoundManager().play(new TommyGunLoopSound(sound, entity.getSoundSource(), volume, entity, stack));
    }
    
    public static void playItemUseSound(LivingEntity entity, SoundEvent sound, float volume, float pitch, boolean looping, ItemStack stack) {
        Minecraft.getInstance().getSoundManager().play(new StoppableEntityTickableSound<LivingEntity>(sound, 
                entity.getSoundSource(), volume, pitch, looping, entity, 
                e -> e.isUsingItem() && e.getUseItem().sameItem(stack)));
    }
    
    public static void playMagiciansRedDetectorSound(MRDetectorEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new MRDetectorSound(entity));
    }
    
    static SoundtrackLoopPlayer tickLoopPlayer;
    public static void playBossEntitySoundtrack(SoundtrackLoopPlayer loopPlayer) {
        if (tickLoopPlayer != null) {
            tickLoopPlayer.forceStop();
        }
        tickLoopPlayer = loopPlayer;
        loopPlayer.start();
    }

    @SubscribeEvent
    public static void onSoundSourcePlayed(SoundSourceEvent event) {
        if (tickLoopPlayer != null) {
            tickLoopPlayer.onSoundSourceEvent(event);
        }
    }
}
