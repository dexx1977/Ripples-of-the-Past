package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PreviousPowerTypesPacket {
    private final Collection<NonStandPowerType<?>> types;
    
    public PreviousPowerTypesPacket(Collection<NonStandPowerType<?>> types) {
        this.types = types;
    }
    
    
    public static class Handler implements IModPacketHandler<PreviousPowerTypesPacket> {

        @Override
        public void encode(PreviousPowerTypesPacket msg, FriendlyByteBuf buf) {
            NetworkUtil.writeCollection(buf, msg.types, (value, b) -> b.writeRegistryId(com.github.standobyte.jojo.init.power.JojoCustomRegistries.NON_STAND_POWERS.getRegistry(), value), false);
        }

        @Override
        public PreviousPowerTypesPacket decode(FriendlyByteBuf buf) {
            return new PreviousPowerTypesPacket(NetworkUtil.readCollection(buf, () -> buf.readRegistryIdSafe(NonStandPowerType.class)));
        }

        @Override
        public void handle(PreviousPowerTypesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                msg.types.forEach(type -> {
                    power.addHadPowerBefore(type);
                });
            });
        }

        @Override
        public Class<PreviousPowerTypesPacket> getPacketClass() {
            return PreviousPowerTypesPacket.class;
        }
    }
}
