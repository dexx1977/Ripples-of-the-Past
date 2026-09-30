package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public class TrHamonSyncPlayerLearnerPacket {
    private final int teacherId;
    private final int learnerId;
    private final boolean add;

    public TrHamonSyncPlayerLearnerPacket(int teacherId, int learnerId, boolean add) {
        this.teacherId = teacherId;
        this.learnerId = learnerId;
        this.add = add;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonSyncPlayerLearnerPacket> {

        @Override
        public void encode(TrHamonSyncPlayerLearnerPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.teacherId);
            buf.writeInt(msg.learnerId);
            buf.writeBoolean(msg.add);
        }

        @Override
        public TrHamonSyncPlayerLearnerPacket decode(FriendlyByteBuf buf) {
            return new TrHamonSyncPlayerLearnerPacket(buf.readInt(), buf.readInt(), buf.readBoolean());
        }

        @Override
        public void handle(TrHamonSyncPlayerLearnerPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.teacherId);
            if (entity instanceof LivingEntity) {
                LivingEntity teacherPlayer = (LivingEntity) entity;
                Entity learnerEntity = ClientUtil.getEntityById(msg.learnerId);
                INonStandPower.getNonStandPowerOptional(teacherPlayer).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get())).ifPresent(hamon -> {
                    if (learnerEntity instanceof Player) {
                        if (msg.add) {
                            hamon.addNewPlayerLearner((Player) learnerEntity);
                            Entity clientPlayer = ClientUtil.getClientPlayer();
                            if (clientPlayer == teacherPlayer) {
                                ClientUtil.setOverlayMessage(Component.translatable(
                                        "jojo.chat.message.new_hamon_learner", 
                                        learnerEntity.getDisplayName(), 
                                        Component.keybind(JojoMod.MOD_ID + ".key.hamon_skills_window")));
                            }
                            else if (clientPlayer == learnerEntity) {
                                ClientUtil.setOverlayMessage(Component.translatable(
                                        "jojo.chat.message.asked_hamon_teacher", 
                                        teacherPlayer.getDisplayName()));

                            }
                        }
                        else {
                            hamon.removeNewLearner((Player) learnerEntity);
                        }
                    }
                });
            }
        }

        @Override
        public Class<TrHamonSyncPlayerLearnerPacket> getPacketClass() {
            return TrHamonSyncPlayerLearnerPacket.class;
        }
    }
}