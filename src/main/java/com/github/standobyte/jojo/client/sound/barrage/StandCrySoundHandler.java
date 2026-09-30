package com.github.standobyte.jojo.client.sound.barrage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.RandomSource;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.client.sound.EventlessSoundAccessor;
import com.github.standobyte.jojo.client.sound.StoppableEntityTickableSound;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

public class StandCrySoundHandler<T extends Entity> {
    public SoundEvent[] soundEvents;
    protected Sound sound;
    protected SoundsResolved soundsInfo;
    
    protected AbstractTickableSoundInstance currentSoundPlaying;
    private boolean isStopped = false;
    
    protected List<Sound> notPlayed;
    protected int soundsPlayed = 0;
    
    private final SoundSource category;
    private final float volume;
    private final float pitch;
    private final T entity;
    private final Predicate<T> playWhile;
    
    public static <T extends Entity> void create(
            SoundSource category, float volume, float pitch, boolean looping,
            T entity, Predicate<T> playWhile, SoundEvent... sounds) {
        SoundsResolved cachedOrNull = SoundsResolved.getCached(sounds);
        StandCrySoundHandler<T> handler;
        if (sounds.length > 0 && sounds[0] == ModSounds.GOLD_EXPERIENCE_MUDA_RUSH.get()) {
            handler = new StandCryGESoundHandler<T>(category, pitch, pitch, looping, entity, playWhile, cachedOrNull, sounds);
        }
        else {
            handler = new StandCrySoundHandler<T>(category, pitch, pitch, looping, entity, playWhile, cachedOrNull, sounds);
        }
        TICKING_HANDLERS.add(handler);
    }
    
    protected StandCrySoundHandler(SoundSource category, float volume, float pitch, boolean looping,
            T entity, Predicate<T> playWhile, @Nullable SoundsResolved soundsResolved, SoundEvent... sounds) {
        this.soundEvents = sounds;
        this.soundsInfo = soundsResolved;
        
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.entity = entity;
        this.playWhile = playWhile;
        
        resolve();
    }
    
    protected void resolve() {
        if (this.soundsInfo == null) {
            this.soundsInfo = new SoundsResolved();
            soundsInfo.resolve(soundEvents);
        }
        
        if (soundsInfo.allSounds.isEmpty()) {
            sound = SoundManager.EMPTY_SOUND;
        }
        else {
            notPlayed = new ArrayList<>();
            sound = pickNextSound(notPlayed);
        }
        
        if (sound == SoundManager.EMPTY_SOUND) {
            stop();
        }
    }
    
    public void tick() {
        if (isStopped) {
            return;
        }
        SoundManager soundHandler = Minecraft.getInstance().getSoundManager();
        if (currentSoundPlaying == null) {
            currentSoundPlaying = playNewSound(soundHandler, sound);
        }
        else if (currentSoundPlaying.isStopped()) {
            stop();
            return;
        }
        
        if (!soundHandler.isActive(currentSoundPlaying)) {
            sound = pickNextSound(notPlayed);
            
            if (sound == SoundManager.EMPTY_SOUND) {
                stop();
                return;
            }
            
            currentSoundPlaying = playNewSound(soundHandler, sound);
        }
    }
    
    private AbstractTickableSoundInstance playNewSound(SoundManager soundHandler, Sound soundToPlay) {
        Component subtitle = soundsInfo.soundSubtitles.get(soundToPlay);
        
        AbstractTickableSoundInstance soundInstance = new StoppableEntityTickableSound<T>(SoundEvents.CAT_AMBIENT, category, volume, pitch, false, entity, playWhile) {
            @Override
            public WeighedSoundEvents resolve(SoundManager soundManager) {
                this.sound = soundToPlay;
                this.location = sound.getLocation();
                return new EventlessSoundAccessor(sound.getLocation(), subtitle, sound);
            }
        };
        notPlayed.remove(soundToPlay);
        soundHandler.play(soundInstance);
        return soundInstance;
    }
    
    private static final RandomSource RANDOM = RandomSource.create();
    protected Sound pickNextSound(List<Sound> pickFrom) {
        if (notPlayed.isEmpty()) {
            notPlayed.addAll(soundsInfo.allSounds);
        }
        
        int weight = pickFrom.stream()
                .map(Weighted::getWeight)
                .reduce(0, Integer::sum);
        if (weight > 0) {
            int rand = RANDOM.nextInt(weight);
            
            for (Sound sound : pickFrom) {
                rand -= sound.getWeight();
                if (rand < 0) {
                    ++soundsPlayed;
                    return sound.getSound(RANDOM);
                }
            }
        }
        
        return SoundManager.EMPTY_SOUND;
    }
    
    public boolean isStopped() {
        return isStopped;
    }
    
    protected void stop() {
        isStopped = true;
    }
    
    
    
    protected static final class SoundsResolved {
        public Map<SoundEvent, List<Sound>> soundsPerEvent;
        public List<Sound> allSounds;
        public Map<Sound, Component> soundSubtitles;
        
        void resolve(SoundEvent[] soundEvents) {
            SoundManager handler = Minecraft.getInstance().getSoundManager();
            
            List<Pair<SoundEvent, Sound>> unpacked = 
                    Arrays.stream(soundEvents).flatMap(WalkmanSoundHandler::unpackSoundsEvent)
                    .filter(pair -> Objects.nonNull(pair.getValue()))
                    .collect(Collectors.toList());
            this.soundsPerEvent = 
                    unpacked.stream()
                    .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));
                    
            this.allSounds = unpacked.stream().map(Pair::getValue).collect(Collectors.toList());
            
//            this.soundSubtitles = unpacked.stream().collect(Collectors.toMap(Pair::getValue, entry -> {
//                SoundEventAccessor accessor = handler.getSoundEvent(entry.getKey().getLocation());
//                return accessor.getSubtitle();
//            }));
            this.soundSubtitles = new HashMap<>();
            for (Pair<SoundEvent, Sound> entry : unpacked) {
                WeighedSoundEvents accessor = handler.getSoundEvent(entry.getKey().getLocation());
                Component subtitle = accessor.getSubtitle();
                soundSubtitles.put(entry.getValue(), subtitle);
            }
            
            this.cache(soundEvents);
        }
        
        @Nullable
        private static SoundsResolved getCached(SoundEvent[] soundEvents) {
            ResourceLocation key = makeKey(soundEvents);
            if (key != null && SOUNDS_CACHE.containsKey(key)) {
                return SOUNDS_CACHE.get(key);
            }
            return null;
        }
        
        private void cache(SoundEvent[] soundEvents) {
            ResourceLocation key = makeKey(soundEvents);
            if (key != null) {
                SOUNDS_CACHE.put(key, this);
            }
        }
        
        @Nullable private static ResourceLocation makeKey(SoundEvent[] soundEvents) {
            if (soundEvents.length == 1 || soundEvents.length == 2 && soundEvents[1] == ModSounds.GOLD_EXPERIENCE_WRY.get()) {
                return soundEvents[0].getLocation();
            }
            
            return null;
            
        }
    }
    
    private static final Map<ResourceLocation, SoundsResolved> SOUNDS_CACHE = new HashMap<>();
    private static final List<StandCrySoundHandler<?>> TICKING_HANDLERS = new ArrayList<>();
    
    public static void clearCache() {
        SOUNDS_CACHE.clear();
    }

    // TODO call this
    public static void tickAll() {
        Iterator<StandCrySoundHandler<?>> iter = TICKING_HANDLERS.iterator();
        while (iter.hasNext()) {
            StandCrySoundHandler<?> handler = iter.next();
            handler.tick();
            if (handler.isStopped()) {
                iter.remove();
            }
        }
    }
}
