package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClOnStandDashPacket {
    
    public ClOnStandDashPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClOnStandDashPacket> {

        @Override
        public void encode(ClOnStandDashPacket msg, FriendlyByteBuf buf) {
        }

        @Override
        public ClOnStandDashPacket decode(FriendlyByteBuf buf) {
            return new ClOnStandDashPacket();
        }
        
        @Override
        public void handle(ClOnStandDashPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.canLeap()) {
                    power.onDash();
                    player.hasImpulse = true;
                }
            });
        }

        @Override
        public Class<ClOnStandDashPacket> getPacketClass() {
            return ClOnStandDashPacket.class;
        }
    }
}
