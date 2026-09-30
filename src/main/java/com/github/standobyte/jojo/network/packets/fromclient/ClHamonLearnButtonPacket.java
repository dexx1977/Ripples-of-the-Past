package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClHamonLearnButtonPacket {
    private final AbstractHamonSkill skill;
    
    public ClHamonLearnButtonPacket(AbstractHamonSkill skill) {
        this.skill = skill;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonLearnButtonPacket> {

        @Override
        public void encode(ClHamonLearnButtonPacket msg, FriendlyByteBuf buf) {
            buf.writeRegistryId(msg.skill);
        }

        @Override
        public ClHamonLearnButtonPacket decode(FriendlyByteBuf buf) {
            return new ClHamonLearnButtonPacket(buf.readRegistryIdSafe(AbstractHamonSkill.class));
        }

        @Override
        public void handle(ClHamonLearnButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    msg.skill.learnNewSkill(hamon, player);
                });
            });
        }

        @Override
        public Class<ClHamonLearnButtonPacket> getPacketClass() {
            return ClHamonLearnButtonPacket.class;
        }
    }

}
