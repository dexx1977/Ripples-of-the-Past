package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LifeformsMetMobs;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class GENativeMobsPacket {
    private LifeformsMetMobs serverData;
    private FriendlyByteBuf receivedPacketBuf;
    
    public GENativeMobsPacket(LifeformsMetMobs serverData) {
        this.serverData = serverData;
    }
    
    private GENativeMobsPacket(FriendlyByteBuf toDecode) {
        this.receivedPacketBuf = toDecode;
    }
    
    
    
    public static class Handler implements IModPacketHandler<GENativeMobsPacket> {

        @Override
        public void encode(GENativeMobsPacket msg, FriendlyByteBuf buf) {
            msg.serverData.nativeMobsToBuf(buf);
        }

        @Override
        public GENativeMobsPacket decode(FriendlyByteBuf buf) {
            return new GENativeMobsPacket(buf);
        }

        @Override
        public void handle(GENativeMobsPacket msg, Supplier<Context> ctx) {
            Player player = ClientUtil.getClientPlayer();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.getMetMobs().nativeMobsFromBuf(msg.receivedPacketBuf);
            });
        }

        @Override
        public Class<GENativeMobsPacket> getPacketClass() {
            return GENativeMobsPacket.class;
        }
    }
}
