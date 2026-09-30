package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClToggleStandSummonPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClToggleStandSummonPacket> {

        @Override
        public void encode(ClToggleStandSummonPacket msg, FriendlyByteBuf buf) {}

        @Override
        public ClToggleStandSummonPacket decode(FriendlyByteBuf buf) {
            return new ClToggleStandSummonPacket();
        }
    
        @Override
        public void handle(ClToggleStandSummonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ctx.get().getSender();
            if (player.isAlive()) {
                IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                    power.toggleSummon();
                });
            }
        }

        @Override
        public Class<ClToggleStandSummonPacket> getPacketClass() {
            return ClToggleStandSummonPacket.class;
        }
    }

}
