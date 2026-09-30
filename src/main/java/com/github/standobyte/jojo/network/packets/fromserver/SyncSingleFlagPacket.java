package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncSingleFlagPacket {
    private final Type packetType;
    private final boolean value;
    
    private SyncSingleFlagPacket(Type packetType, boolean value) {
        this.packetType = packetType;
        this.value = value;
    }
    
    public static enum Type {
    }
    
    
    
    public static class Handler implements IModPacketHandler<SyncSingleFlagPacket> {

        @Override
        public void encode(SyncSingleFlagPacket msg, FriendlyByteBuf buf) {
            byte val = (byte) msg.packetType.ordinal();
            if (msg.value) val |= 0x80;
            buf.writeByte(val);
        }

        @Override
        public SyncSingleFlagPacket decode(FriendlyByteBuf buf) {
            byte val = buf.readByte();
            boolean flagValue = (val & 0x80) > 0;
            int ordinal = (int) (val & 0x7F);
            return new SyncSingleFlagPacket(Type.values()[ordinal], flagValue);
        }

        @Override
        public void handle(SyncSingleFlagPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ClientUtil.getClientPlayer();
            switch (msg.packetType) {
            }
        }

        @Override
        public Class<SyncSingleFlagPacket> getPacketClass() {
            return SyncSingleFlagPacket.class;
        }
    }
}
