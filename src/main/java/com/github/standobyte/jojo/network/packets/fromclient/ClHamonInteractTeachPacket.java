package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClHamonInteractTeachPacket {
    private final int entityId;
    
    public ClHamonInteractTeachPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonInteractTeachPacket> {
    
        @Override
        public void encode(ClHamonInteractTeachPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public ClHamonInteractTeachPacket decode(FriendlyByteBuf buf) {
            return new ClHamonInteractTeachPacket(buf.readInt());
        }

        @Override
        public void handle(ClHamonInteractTeachPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            Entity targetEntity = player.level.getEntity(msg.entityId);
            INonStandPower.getNonStandPowerOptional(player).resolve()
            .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()))
            .ifPresent(hamon -> {
                if (targetEntity instanceof Player) {
                    hamon.interactWithNewLearner((Player) targetEntity);
                }
            });
        }

        @Override
        public Class<ClHamonInteractTeachPacket> getPacketClass() {
            return ClHamonInteractTeachPacket.class;
        }
    }
}
