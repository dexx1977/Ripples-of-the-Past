package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClStopWallClimbPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClStopWallClimbPacket> {

        @Override
        public void encode(ClStopWallClimbPacket msg, FriendlyByteBuf buf) {}

        @Override
        public ClStopWallClimbPacket decode(FriendlyByteBuf buf) {
            return new ClStopWallClimbPacket();
        }
    
        @Override
        public void handle(ClStopWallClimbPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ctx.get().getSender();
            if (player.isAlive()) {
                LivingWallClimbing.getHandler(player).ifPresent(cap -> cap.stopWallClimbing());
            }
        }

        @Override
        public Class<ClStopWallClimbPacket> getPacketClass() {
            return ClStopWallClimbPacket.class;
        }
    }

}
