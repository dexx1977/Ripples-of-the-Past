package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class RPSOpponentPickThoughtsPacket {
    @Nullable private final Pick pickThoughts;
    
    public RPSOpponentPickThoughtsPacket(Pick playerPick) {
        this.pickThoughts = playerPick;
    }
    
    
    
    public static class Handler implements IModPacketHandler<RPSOpponentPickThoughtsPacket> {
        
        @Override
        public void encode(RPSOpponentPickThoughtsPacket msg, FriendlyByteBuf buf) {
            buf.writeBoolean(msg.pickThoughts != null);
            if (msg.pickThoughts != null) {
                buf.writeEnum(msg.pickThoughts);
            }
        }

        @Override
        public RPSOpponentPickThoughtsPacket decode(FriendlyByteBuf buf) {
            return new RPSOpponentPickThoughtsPacket(buf.readBoolean() ? buf.readEnum(Pick.class) : null);
        }

        @Override
        public void handle(RPSOpponentPickThoughtsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ClientUtil.getClientPlayer();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.getCurrentRockPaperScissorsGame().ifPresent(game -> game.setOpponentThoughts(player, msg.pickThoughts));
            });
        }

        @Override
        public Class<RPSOpponentPickThoughtsPacket> getPacketClass() {
            return RPSOpponentPickThoughtsPacket.class;
        }
    }
}
