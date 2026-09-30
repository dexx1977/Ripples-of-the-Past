package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mc.entitysubtype.SubtypeResourceLocation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MetEntityTypesPacket {
    private final Collection<SubtypeResourceLocation> metEntityTypeIds;

    public MetEntityTypesPacket(Collection<SubtypeResourceLocation> metEntityTypeIds) {
        this.metEntityTypeIds = metEntityTypeIds;
    }
    
    
    
    public static class Handler implements IModPacketHandler<MetEntityTypesPacket> {

        @Override
        public void encode(MetEntityTypesPacket msg, FriendlyByteBuf buf) {
            NetworkUtil.writeCollection(buf, msg.metEntityTypeIds, id -> buf.writeUtf(id.toString()), false);
        }

        @Override
        public MetEntityTypesPacket decode(FriendlyByteBuf buf) {
            return new MetEntityTypesPacket(NetworkUtil.readCollection(buf, () -> new SubtypeResourceLocation(buf.readUtf())));
        }

        @Override
        public void handle(MetEntityTypesPacket msg, Supplier<Context> ctx) {
            Player player = ClientUtil.getClientPlayer();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                msg.metEntityTypeIds.forEach(id -> cap.addMetEntityTypeId(id));
            });
        }

        @Override
        public Class<MetEntityTypesPacket> getPacketClass() {
            return MetEntityTypesPacket.class;
        }
    }
}
