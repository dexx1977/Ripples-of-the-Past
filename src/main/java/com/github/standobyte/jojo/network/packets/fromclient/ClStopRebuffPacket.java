package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.non_stand.HamonRebuffOverdrive;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClStopRebuffPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClStopRebuffPacket> {

        @Override
        public void encode(ClStopRebuffPacket msg, FriendlyByteBuf buf) {}

        @Override
        public ClStopRebuffPacket decode(FriendlyByteBuf buf) {
            return new ClStopRebuffPacket();
        }
    
        @Override
        public void handle(ClStopRebuffPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ctx.get().getSender();
            HamonRebuffOverdrive.onWASDInput(player);
        }

        @Override
        public Class<ClStopRebuffPacket> getPacketClass() {
            return ClStopRebuffPacket.class;
        }
    }

}
