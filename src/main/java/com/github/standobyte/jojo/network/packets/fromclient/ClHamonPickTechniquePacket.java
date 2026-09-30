package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClHamonPickTechniquePacket {
    private final CharacterHamonTechnique technique;
    
    public ClHamonPickTechniquePacket(CharacterHamonTechnique technique) {
        this.technique = technique;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonPickTechniquePacket> {
    
        @Override
        public void encode(ClHamonPickTechniquePacket msg, FriendlyByteBuf buf) {
            buf.writeRegistryId(com.github.standobyte.jojo.init.power.JojoCustomRegistries.HAMON_CHARACTER_TECHNIQUES.getRegistry(), msg.technique);
        }

        @Override
        public ClHamonPickTechniquePacket decode(FriendlyByteBuf buf) {
            return new ClHamonPickTechniquePacket(buf.readRegistryIdSafe(CharacterHamonTechnique.class));
        }

        @Override
        public void handle(ClHamonPickTechniquePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.pickHamonTechnique(player, msg.technique);
                });
            });
        }

        @Override
        public Class<ClHamonPickTechniquePacket> getPacketClass() {
            return ClHamonPickTechniquePacket.class;
        }
    }

}
