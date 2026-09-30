package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TrStaminaPacket {
    private final int userId;
    private final float stamina;
    
    public TrStaminaPacket(int userId, float stamina) {
        this.userId = userId;
        this.stamina = stamina;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrStaminaPacket> {

        @Override
        public void encode(TrStaminaPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.userId);
            buf.writeFloat(msg.stamina);
        }

        @Override
        public TrStaminaPacket decode(FriendlyByteBuf buf) {
            return new TrStaminaPacket(buf.readInt(), buf.readFloat());
        }

        @Override
        public void handle(TrStaminaPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity userEntity = ClientUtil.getEntityById(msg.userId);
            if (userEntity instanceof LivingEntity) {
                LivingEntity userLiving = (LivingEntity) userEntity;
                IStandPower.getStandPowerOptional(userLiving).ifPresent(power -> {
                    power.clPacketSetStamina(msg.stamina);
                });
            }
        }

        @Override
        public Class<TrStaminaPacket> getPacketClass() {
            return TrStaminaPacket.class;
        }
    }
}
