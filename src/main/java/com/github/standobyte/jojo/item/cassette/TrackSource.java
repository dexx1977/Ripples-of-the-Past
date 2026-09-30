package com.github.standobyte.jojo.item.cassette;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

public abstract class TrackSource {
    protected final TrackSourceType type;
    
    protected TrackSource(TrackSourceType type) {
        this.type = type;
    }
    
    protected CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Type", type.ordinal() + 1);
        return nbt;
    }

    public abstract SoundEvent getSoundEvent();
    
    public MutableComponent trackName(ResourceLocation trackId, boolean shortened) {
        String key = getTranslationKey(trackId);
        if (shortened) {
            key = ClientUtil.getShortenedTranslationKey(key);
        }
        return Component.translatable(key);
    }
    
    protected abstract String getTranslationKey(ResourceLocation trackId);
    
    
    public static final TrackSource BROKEN_CASSETTE = new TrackSource(null) {
        @Override
        protected CompoundTag toNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("Type", 0);
            return nbt;
        }
        
        @Override
        public SoundEvent getSoundEvent() {
            return null;
        }
        
        @Override
        public MutableComponent trackName(ResourceLocation trackId, boolean shortened) {
            return MCUtil.EMPTY_TEXT;
        }
        
        @Override
        protected String getTranslationKey(ResourceLocation trackId) { return ""; }
    };

    
    
    public static enum TrackSourceType {
        MUSIC_DISC(
                false,
                TrackSourceMusicDisc::fromNBT) {
            
            @Override
            public boolean isItemMusicSource(ItemStack item) {
                return item.getItem() instanceof RecordItem;
            }
            
            @Override
            public TrackSource getMusic(ItemStack item) {
                return new TrackSourceMusicDisc((RecordItem) item.getItem());
            }
        },
        
        STAND_DISC(
                false,
                TrackSourceStandDisc::fromNBT) {
            
            @Override
            public boolean isItemMusicSource(ItemStack item) {
                return item.getItem() == ModItems.STAND_DISC.get();
            }
            
            @Override
            public TrackSource getMusic(ItemStack item) {
                StandInstance stand = StandDiscItem.getStandFromStack(item);
                if (stand != null) {
                    return new TrackSourceStandDisc(stand.getType());
                }
                return null;
            }
        },
        
        DYE_COLOR(
                true,
                TrackSourceDye::fromNBT) {
            
            @Override
            public boolean isItemMusicSource(ItemStack item) {
                return item.getItem() instanceof DyeItem;
            }
            
            @Override
            public TrackSource getMusic(ItemStack item) {
                DyeColor dye = ((DyeItem) item.getItem()).getDyeColor();
                if (dye != null) {
                    return new TrackSourceDye(dye);
                }
                return null;
            }
        };

        private final boolean spendCraftingItem;
        private final Function<CompoundTag, TrackSource> read;
        private TrackSourceType(boolean spendCraftingItem, Function<CompoundTag, TrackSource> read) {
            this.spendCraftingItem = spendCraftingItem;
            this.read = read;
        }
        
        
        @Nullable
        public static TrackSourceType getTrackSourceType(ItemStack item) {
            for (TrackSourceType type : values()) {
                if (type.isItemMusicSource(item)) {
                    return type;
                }
            }
            return null;
        }

        @Nullable
        public abstract boolean isItemMusicSource(ItemStack item);
        
        
        @Nullable
        public static TrackSource getMusicFromItem(ItemStack item) {
            TrackSourceType type = getTrackSourceType(item);
            return type != null ? type.getMusic(item) : null;
        }

        @Nullable
        public abstract TrackSource getMusic(ItemStack item);
        
        
        public boolean isRecordingSourceItemSpent() {
            return spendCraftingItem;
        }
        
        
        public static TrackSource fromNBT(CompoundTag nbt) {
            TrackSource source = null;
            int type = nbt.getInt("Type");
            if (type > 0 && type <= values().length) {
                source = values()[type - 1].read.apply(nbt);
            }
            return source != null ? source : TrackSource.BROKEN_CASSETTE;
        }
    }
}
