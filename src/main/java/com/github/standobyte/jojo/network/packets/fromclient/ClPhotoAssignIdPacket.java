package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.PhotoItem;
import com.github.standobyte.jojo.item.polaroid.PhotosHandler;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromserver.PhotoIdAssignedPacket;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClPhotoAssignIdPacket {
    private final UUID photoUuid;
    private final int giveItemToPlayer;
    
    public ClPhotoAssignIdPacket(UUID photoUuid, int giveItemToPlayer) {
        this.photoUuid = photoUuid;
        this.giveItemToPlayer = giveItemToPlayer;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClPhotoAssignIdPacket> {
    
        @Override
        public void encode(ClPhotoAssignIdPacket msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.photoUuid);
            buf.writeInt(msg.giveItemToPlayer);
        }

        @Override
        public ClPhotoAssignIdPacket decode(FriendlyByteBuf buf) {
            UUID photoUuid = buf.readUUID();
            int giveItemToPlayer = buf.readInt();
            return new ClPhotoAssignIdPacket(photoUuid, giveItemToPlayer);
        }

        @Override
        public void handle(ClPhotoAssignIdPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            PhotosHandler serverPhotos = SaveFileUtilCapProvider.getSaveFileCap(player).getPolaroidPhotos();
            long photoIntId = serverPhotos.incPolaroidPhotoId();
            PacketManager.sendToClient(new PhotoIdAssignedPacket(msg.photoUuid, photoIntId, player.getId() == msg.giveItemToPlayer), player);
            
            Entity entity = player.level().getEntity(msg.giveItemToPlayer);
            if (entity instanceof LivingEntity) {
                ItemStack photo = new ItemStack(ModItems.PHOTO.get());
                PhotoItem.setPhotoId(photo, photoIntId);
                PhotoItem.setPhotoAnimTicks(photo);
                MCUtil.giveItemTo((LivingEntity) entity, photo, true);
            }
        }

        @Override
        public Class<ClPhotoAssignIdPacket> getPacketClass() {
            return ClPhotoAssignIdPacket.class;
        }
    }

}
