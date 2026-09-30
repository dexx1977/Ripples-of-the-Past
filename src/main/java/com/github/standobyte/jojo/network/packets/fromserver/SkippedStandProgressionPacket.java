package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SkippedStandProgressionPacket {
    
    public SkippedStandProgressionPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<SkippedStandProgressionPacket> {

        @Override
        public void encode(SkippedStandProgressionPacket msg, FriendlyByteBuf buf) {
        }

        @Override
        public SkippedStandProgressionPacket decode(FriendlyByteBuf buf) {
            return new SkippedStandProgressionPacket();
        }

        @Override
        public void handle(SkippedStandProgressionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setProgressionSkipped();
            });
        }

        @Override
        public Class<SkippedStandProgressionPacket> getPacketClass() {
            return SkippedStandProgressionPacket.class;
        }
    }
}
