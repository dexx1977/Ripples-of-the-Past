package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.item.polaroid.PhotosHandler;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClPhotoRequestPacket {
    private final long photoId;
    
    public ClPhotoRequestPacket(long photoId) {
        this.photoId = photoId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClPhotoRequestPacket> {

        @Override
        public void encode(ClPhotoRequestPacket msg, FriendlyByteBuf buf) {
            buf.writeLong(msg.photoId);
        }

        @Override
        public ClPhotoRequestPacket decode(FriendlyByteBuf buf) {
            long photoId = buf.readLong();
            return new ClPhotoRequestPacket(photoId);
        }

        @Override
        public void handle(ClPhotoRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            PhotosHandler serverPhotos = SaveFileUtilCapProvider.getSaveFileCap(player).getPolaroidPhotos();
            UUID serverId = SaveFileUtilCapProvider.getSaveFileCap(player).getServerUUID();
            serverPhotos.requestPhoto(msg.photoId, player, serverId);
        }

        @Override
        public Class<ClPhotoRequestPacket> getPacketClass() {
            return ClPhotoRequestPacket.class;
        }
    }
}
