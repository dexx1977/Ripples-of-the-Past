package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClReadHamonBreathTabPacket {
    
    public ClReadHamonBreathTabPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClReadHamonBreathTabPacket> {
        
        @Override
        public void encode(ClReadHamonBreathTabPacket msg, FriendlyByteBuf buf) {
        }
        
        @Override
        public ClReadHamonBreathTabPacket decode(FriendlyByteBuf buf) {
            return new ClReadHamonBreathTabPacket();
        }
        
        @Override
        public void handle(ClReadHamonBreathTabPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.setSentNotification(OneTimeNotification.HAMON_BREATH_GUIDE, true);
            });
        }
        
        @Override
        public Class<ClReadHamonBreathTabPacket> getPacketClass() {
            return ClReadHamonBreathTabPacket.class;
        }
    }
}
