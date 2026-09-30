package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;

public class WalkmanDataCap {
    private int id;
    private boolean idInitialized = false;
    private float volume = 1.0F;
    private PlaybackMode playbackMode = PlaybackMode.STOP_AT_THE_END;
    
    public WalkmanDataCap(ItemStack cassetteItem) {}
    
    public void initId(ServerLevel world) {
        if (!idInitialized) {
            id = SaveFileUtilCapProvider.getSaveFileCap(((ServerLevel) world).getServer()).incWalkmanId();
            idInitialized = true;
        }
    }
    
    public int getId() {
        if (!idInitialized) {
            throw new IllegalStateException("The walkman's id hasn't been initialized yet!");
        }
        return id;
    }
    
    public boolean isIdInitialized() {
        return idInitialized;
    }
    
    public boolean checkId(int id) {
        return idInitialized && this.id == id;
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setVolume(float volume) {
        this.volume = Mth.clamp(volume, 0, 1);
    }
    
    public PlaybackMode getPlaybackMode() {
        return playbackMode;
    }
    
    public void setPlaybackMode(PlaybackMode mode) {
        if (mode != null) {
            this.playbackMode = mode;
        }
    }
    
    
    
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        if (idInitialized) nbt.putInt("Id", id);
        nbt.putFloat("Volume", volume);
        nbt.putBoolean("Loop", playbackMode == PlaybackMode.LOOP);
        return nbt;
    }
    
    public void fromNBT(CompoundTag nbt) {
        if (nbt.contains("Id", MCUtil.getNbtId(IntTag.class))) {
            id = nbt.getInt("Id");
            idInitialized = true;
        }
        else {
            idInitialized = false;
        }
        this.volume = Mth.clamp(nbt.getFloat("Volume"), 0, 1);
        this.playbackMode = nbt.getBoolean("Loop") ? PlaybackMode.LOOP : PlaybackMode.STOP_AT_THE_END;
    }
    
    
    
    public static enum PlaybackMode {
        STOP_AT_THE_END { @Override public PlaybackMode getOpposite() { return LOOP; }},
        LOOP { @Override public PlaybackMode getOpposite() { return STOP_AT_THE_END; }};
        
        public abstract PlaybackMode getOpposite();
    }
}
