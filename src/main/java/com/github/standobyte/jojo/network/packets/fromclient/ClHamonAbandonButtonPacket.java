package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClHamonAbandonButtonPacket {
    
    public ClHamonAbandonButtonPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonAbandonButtonPacket> {
        
        @Override
        public void encode(ClHamonAbandonButtonPacket msg, FriendlyByteBuf buf) {
            
        }

        @Override
        public ClHamonAbandonButtonPacket decode(FriendlyByteBuf buf) {
            return new ClHamonAbandonButtonPacket();
        }

        @Override
        public void handle(ClHamonAbandonButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                if (power.getType() == ModPowers.HAMON.get()) {
                    power.clear();
                    ModCriteriaTriggers.ABANDON_HAMON.get().trigger(player);
                }
            });
        }

        @Override
        public Class<ClHamonAbandonButtonPacket> getPacketClass() {
            return ClHamonAbandonButtonPacket.class;
        }
    }
    

}
