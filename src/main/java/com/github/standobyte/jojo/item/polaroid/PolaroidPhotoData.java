package com.github.standobyte.jojo.item.polaroid;

import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.BatchSender;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class PolaroidPhotoData extends SavedData {
    private byte[] photoBytes = new byte[0];
    private UUID senderPlayer;
    
    // 1.20.1's SavedData has no id field, the DimensionDataStorage key carries it
    public PolaroidPhotoData() {}
    
    public PolaroidPhotoData(byte[] photoBytes, @Nullable UUID senderPlayer) {
        this.photoBytes = photoBytes;
        this.senderPlayer = senderPlayer;
    }
    
    public void sendTo(ServerPlayer player, UUID serverId, long photoId) {
        BatchSender sender = new SrvPhotoSender(photoBytes, serverId, photoId, player);
        sender.sendAll();
    }

    /** The loader the 1.20.1 storage takes in place of the old {@code read} override. */
    public static PolaroidPhotoData load(CompoundTag nbt) {
        PolaroidPhotoData data = new PolaroidPhotoData();
        data.photoBytes = nbt.getByteArray("Photo");
        data.senderPlayer = nbt.hasUUID("Sender") ? nbt.getUUID("Sender") : null;
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putByteArray("Photo", photoBytes);
        if (senderPlayer != null) {
            nbt.putUUID("Sender", senderPlayer);
        }
        return nbt;
    }

}
