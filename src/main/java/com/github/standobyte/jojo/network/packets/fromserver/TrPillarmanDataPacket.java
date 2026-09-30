package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TrPillarmanDataPacket {
    private final int entityId;
    private final boolean stoneFormEnabled;
    private final boolean bladesVisible;
    private final int stage;
    public PillarmanData.Mode mode;
    
    public TrPillarmanDataPacket(int entityId, PillarmanData pillarmanData) {
        this(entityId, pillarmanData.isStoneFormEnabled(), pillarmanData.getBladesVisible(), pillarmanData.getEvolutionStage(), pillarmanData.getMode());
    }
    
    public TrPillarmanDataPacket(int entityId, boolean stoneFormEnabled, boolean bladesVisible, int stage, PillarmanData.Mode mode) {
        this.entityId = entityId;
        this.stoneFormEnabled = stoneFormEnabled;
        this.bladesVisible = bladesVisible;
        this.stage = stage;
        this.mode = mode;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrPillarmanDataPacket> {

        @Override
        public void encode(TrPillarmanDataPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
            buf.writeBoolean(msg.stoneFormEnabled);
            buf.writeBoolean(msg.bladesVisible);
            buf.writeVarInt(msg.stage);
            buf.writeEnum(msg.mode);
        }

        @Override
        public TrPillarmanDataPacket decode(FriendlyByteBuf buf) {
            return new TrPillarmanDataPacket(buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readEnum(PillarmanData.Mode.class));
        }

        @Override
        public void handle(TrPillarmanDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()))
                .ifPresent(pillarman -> {
                    boolean prevStoneForm = pillarman.isStoneFormEnabled();
                    pillarman.setStoneFormEnabled(msg.stoneFormEnabled);
                    pillarman.setBladesVisible(msg.bladesVisible);
                    pillarman.setEvolutionStage(msg.stage);
                    pillarman.setMode(msg.mode);
                    if (entity instanceof Player) {
                        Player userPlayer = (Player) entity;
                        ModPlayerAnimations.stoneForm.setAnimEnabled(userPlayer, msg.stoneFormEnabled);
                        /*if (!prevStoneForm && msg.stoneFormEnabled && userPlayer == ClientUtil.getClientPlayer()) {
                            ClientUtil.setThirdPerson();
                        }*/
                    }
                });
            }
        }

        @Override
        public Class<TrPillarmanDataPacket> getPacketClass() {
            return TrPillarmanDataPacket.class;
        }
    }

}
