package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TrDoubleShiftPacket {
    private final int entityId;
    
    public TrDoubleShiftPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrDoubleShiftPacket> {

        @Override
        public void encode(TrDoubleShiftPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public TrDoubleShiftPacket decode(FriendlyByteBuf buf) {
            return new TrDoubleShiftPacket(buf.readInt());
        }

        @Override
        public void handle(TrDoubleShiftPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof Player) {
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setDoubleShiftPress());
            }
        }

        @Override
        public Class<TrDoubleShiftPacket> getPacketClass() {
            return TrDoubleShiftPacket.class;
        }
    }
}
