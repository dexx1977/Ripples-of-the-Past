package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClHamonMeditationPacket {
    private final Boolean value;
    
    public ClHamonMeditationPacket() {
        this.value = null;
    }
    
    public ClHamonMeditationPacket(boolean value) {
        this.value = value;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonMeditationPacket> {
    
        @Override
        public void encode(ClHamonMeditationPacket msg, FriendlyByteBuf buf) {
            buf.writeBoolean(msg.value != null);
            if (msg.value != null) {
                buf.writeBoolean(msg.value);
            }
        }

        @Override
        public ClHamonMeditationPacket decode(FriendlyByteBuf buf) {
            boolean hasValue = buf.readBoolean();
            return hasValue ? new ClHamonMeditationPacket(buf.readBoolean()) : new ClHamonMeditationPacket();
        }

        @Override
        public void handle(ClHamonMeditationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    if (player.onGround() || hamon.isMeditating()) {
                        hamon.setIsMeditating(player, msg.value != null ? msg.value : !hamon.isMeditating());
                    }
                });
            });
        }

        @Override
        public Class<ClHamonMeditationPacket> getPacketClass() {
            return ClHamonMeditationPacket.class;
        }
    }

}
