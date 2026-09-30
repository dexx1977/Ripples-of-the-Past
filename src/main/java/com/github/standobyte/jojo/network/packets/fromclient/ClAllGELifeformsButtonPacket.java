package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClAllGELifeformsButtonPacket {
    
    public ClAllGELifeformsButtonPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClAllGELifeformsButtonPacket> {
    
        @Override
        public void encode(ClAllGELifeformsButtonPacket msg, FriendlyByteBuf buf) {
        }

        @Override
        public ClAllGELifeformsButtonPacket decode(FriendlyByteBuf buf) {
            return new ClAllGELifeformsButtonPacket();
        }

        @Override
        public void handle(ClAllGELifeformsButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player.abilities.instabuild) {
                GoldExperienceChooseLifeform.unlockAllEntityTypes(player);
            }
        }

        @Override
        public Class<ClAllGELifeformsButtonPacket> getPacketClass() {
            return ClAllGELifeformsButtonPacket.class;
        }
    }
}
