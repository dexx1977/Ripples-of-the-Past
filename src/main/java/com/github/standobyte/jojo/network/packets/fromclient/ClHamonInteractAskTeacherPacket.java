package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClHamonInteractAskTeacherPacket {
    private final int entityId;
    
    public ClHamonInteractAskTeacherPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonInteractAskTeacherPacket> {
    
        @Override
        public void encode(ClHamonInteractAskTeacherPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public ClHamonInteractAskTeacherPacket decode(FriendlyByteBuf buf) {
            return new ClHamonInteractAskTeacherPacket(buf.readInt());
        }

        @Override
        public void handle(ClHamonInteractAskTeacherPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            Entity targetEntity = player.level.getEntity(msg.entityId);
            HamonUtil.interactWithHamonTeacher(player.level, player, targetEntity);
        }

        @Override
        public Class<ClHamonInteractAskTeacherPacket> getPacketClass() {
            return ClHamonInteractAskTeacherPacket.class;
        }
    }
}
