package com.github.standobyte.jojo.item.cassette;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class TrackSourceMusicDisc extends TrackSource {
    private final RecordItem musicDisc;
    
    public TrackSourceMusicDisc(RecordItem musicDisc) {
        super(TrackSourceType.MUSIC_DISC);
        this.musicDisc = musicDisc;
    }
    
    static TrackSource fromNBT(CompoundTag nbt) {
        if (nbt.contains("MusicDisc", MCUtil.getNbtId(StringTag.class))) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("MusicDisc")));
            if (item instanceof RecordItem) {
                return new TrackSourceMusicDisc((RecordItem) item);
            }
        }
        
        return BROKEN_CASSETTE;
    }
    
    @Override
    protected CompoundTag toNBT() {
        CompoundTag nbt = super.toNBT();
        nbt.putString("MusicDisc", MCUtil.id(musicDisc).toString());
        return nbt;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return musicDisc.getSound();
    }

    @Override
    protected String getTranslationKey(ResourceLocation trackId) {
        return musicDisc.getDescriptionId() + ".desc";
    }

}