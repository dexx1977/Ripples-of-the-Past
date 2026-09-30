package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClMetEntityTypePacket {
    private final int entityId;
    
    public ClMetEntityTypePacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClMetEntityTypePacket> {

        @Override
        public void encode(ClMetEntityTypePacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public ClMetEntityTypePacket decode(FriendlyByteBuf buf) {
            return new ClMetEntityTypePacket(buf.readInt());
        }
        
        @Override
        public void handle(ClMetEntityTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                Entity entity = player.level.getEntity(msg.entityId);
                if (entity != null && entity.distanceToSqr(player) <= 144) {
                    EntitySubtype.getMatchingSubtypes(entity).forEach(cap::addMetEntityType);
                }
            });
        }

        @Override
        public Class<ClMetEntityTypePacket> getPacketClass() {
            return ClMetEntityTypePacket.class;
        }
    }
}
