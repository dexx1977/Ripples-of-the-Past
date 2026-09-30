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
    
    public PolaroidPhotoData(String id) {
        super(id);
    }
    
    public PolaroidPhotoData(String id, byte[] photoBytes, @Nullable UUID senderPlayer) {
        super(id);
        this.photoBytes = photoBytes;
        this.senderPlayer = senderPlayer;
    }
    
    public void sendTo(ServerPlayer player, UUID serverId, long photoId) {
        BatchSender sender = new SrvPhotoSender(photoBytes, serverId, photoId, player);
        sender.sendAll();
    }

    @Override
    public void load(CompoundTag nbt) {
        this.photoBytes = nbt.getByteArray("Photo");
        this.senderPlayer = nbt.hasUUID("Sender") ? nbt.getUUID("Sender") : null;
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
