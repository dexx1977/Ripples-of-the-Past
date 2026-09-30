package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mc.damage.NoKnockbackOnBlocking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class KnockbackResTickPacket {
    private final int entityId;
    
    public KnockbackResTickPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<KnockbackResTickPacket> {

        @Override
        public void encode(KnockbackResTickPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public KnockbackResTickPacket decode(FriendlyByteBuf buf) {
            return new KnockbackResTickPacket(buf.readInt());
        }

        @Override
        public void handle(KnockbackResTickPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                NoKnockbackOnBlocking.setOneTickKbRes(living);
            }
        }

        @Override
        public Class<KnockbackResTickPacket> getPacketClass() {
            return KnockbackResTickPacket.class;
        }
    }
}
