package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public class ClToggleStandManualControlPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClToggleStandManualControlPacket> {

        @Override
        public void encode(ClToggleStandManualControlPacket msg, FriendlyByteBuf buf) {}

        @Override
        public ClToggleStandManualControlPacket decode(FriendlyByteBuf buf) {
            return new ClToggleStandManualControlPacket();
        }
    
        @Override
        public void handle(ClToggleStandManualControlPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ctx.get().getSender();
            if (player.isAlive()) {
                IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                    if (power.hasPower()) {
                        if (power.getType().canBeManuallyControlled()) {
                            if (power.isActive()) {
                                StandEntity standEntity = (StandEntity) power.getStandManifestation();
                                ActionConditionResult canControlEntity = standEntity.canBeManuallyControlled();
                                if (canControlEntity.isPositive()) {
                                    boolean keepPosition = player.isShiftKeyDown();
                                    StandUtil.setManualControl(player, !standEntity.isManuallyControlled(), keepPosition);
                                }
                                else if (canControlEntity.getWarning() != null) {
                                    player.displayClientMessage(canControlEntity.getWarning(), true);
                                }
                            }
                        }
                        else {
                            player.displayClientMessage(Component.translatable("jojo.chat.message.no_entity_stand"), true);
                        }
                    }
                    else {
                        player.displayClientMessage(Component.translatable("jojo.chat.message.no_stand"), true);
                    }
                });
            }
        }

        @Override
        public Class<ClToggleStandManualControlPacket> getPacketClass() {
            return ClToggleStandManualControlPacket.class;
        }
    }

}
