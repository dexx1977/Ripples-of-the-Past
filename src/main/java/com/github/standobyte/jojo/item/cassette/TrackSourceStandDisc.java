package com.github.standobyte.jojo.item.cassette;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.OstSoundList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class TrackSourceStandDisc extends TrackSource {
    private final StandType<?> standType;
    
    public TrackSourceStandDisc(StandType<?> standType) {
        super(TrackSourceType.STAND_DISC);
        this.standType = standType;
    }
    
    static TrackSource fromNBT(CompoundTag nbt) {
        if (nbt.contains("Stand", MCUtil.getNbtId(StringTag.class))) {
            StandType<?> standType = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(nbt.getString("Stand")));
            if (standType != null) {
                return new TrackSourceStandDisc(standType);
            }
        }
        
        return BROKEN_CASSETTE;
    }
    
    @Override
    protected CompoundTag toNBT() {
        CompoundTag nbt = super.toNBT();
        nbt.putString("Stand", standType.getRegistryName().toString());
        return nbt;
    }

    @Override
    public SoundEvent getSoundEvent() {
        OstSoundList ost = standType.getOst(null);
        return ost != null ? ost.getForCassette() : null;
    }

    @Override
    protected String getTranslationKey(ResourceLocation trackId) {
        return trackId.getNamespace() + "." + trackId.getPath().replace("/", ".") + ".name";
    }

}
