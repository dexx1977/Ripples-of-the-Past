package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandEntityActionModifier;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TrStandTaskModifierPacket {
    private final int standEntityId;
    private final Action<?> action;

    public TrStandTaskModifierPacket(int standEntityId, Action<?> action) {
        this.standEntityId = standEntityId;
        this.action = action;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrStandTaskModifierPacket> {

        @Override
        public void encode(TrStandTaskModifierPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.standEntityId);
            buf.writeRegistryId(com.github.standobyte.jojo.init.power.JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
        }

        @Override
        public TrStandTaskModifierPacket decode(FriendlyByteBuf buf) {
            return new TrStandTaskModifierPacket(buf.readInt(), buf.readRegistryIdSafe(Action.class));
        }

        @Override
        public void handle(TrStandTaskModifierPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.action instanceof StandEntityActionModifier) {
                Entity entity = ClientUtil.getEntityById(msg.standEntityId);
                if (entity instanceof StandEntity) {
                    StandEntity standEntity = (StandEntity) entity;
                    standEntity.getCurrentTask().ifPresent(task -> {
                        task.addModifierAction((StandEntityActionModifier) msg.action, standEntity);
                    });
                }
            }
        }

        @Override
        public Class<TrStandTaskModifierPacket> getPacketClass() {
            return TrStandTaskModifierPacket.class;
        }
    }
}
