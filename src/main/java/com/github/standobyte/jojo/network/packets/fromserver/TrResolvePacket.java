package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TrResolvePacket {
    private final int entityId;
    private final float resolve;
    private final int noDecayTicks;
    private final boolean reset;
    
    public TrResolvePacket(int entityId, float resolve, int noDecayTicks) {
        this(entityId, resolve, noDecayTicks, false);
    }
    
    public static TrResolvePacket reset(int entityId) {
        return new TrResolvePacket(entityId, 0, 0, true);
    }
    
    private TrResolvePacket(int entityId, float resolve, int noDecayTicks, boolean reset) {
        this.entityId = entityId;
        this.resolve = resolve;
        this.noDecayTicks = noDecayTicks;
        this.reset = reset;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrResolvePacket> {

        @Override
        public void encode(TrResolvePacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
            buf.writeFloat(msg.resolve);
            buf.writeVarInt(msg.noDecayTicks);
            buf.writeBoolean(msg.reset);
        }

        @Override
        public TrResolvePacket decode(FriendlyByteBuf buf) {
            return new TrResolvePacket(buf.readInt(), buf.readFloat(), buf.readVarInt(), buf.readBoolean());
            
        }

        @Override
        public void handle(TrResolvePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IStandPower.getStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    if (msg.reset) {
                        power.getResolveCounter().resetResolveValue();
                    }
                    else {
                        power.getResolveCounter().setResolveValue(msg.resolve, msg.noDecayTicks);
                    }
                });
            }
        }

        @Override
        public Class<TrResolvePacket> getPacketClass() {
            return TrResolvePacket.class;
        }
    }
}
