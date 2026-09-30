package com.github.standobyte.jojo.network.packets.fromclient;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import io.netty.buffer.Unpooled;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class ClSetStandSkinPacket {
    private final Optional<ResourceLocation> standSkin;
    private final ResourceLocation standId;
    
    public ClSetStandSkinPacket(Optional<ResourceLocation> standSkin, ResourceLocation standId) {
        this.standSkin = standSkin;
        this.standId = standId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClSetStandSkinPacket> {
    
        @Override
        public void encode(ClSetStandSkinPacket msg, FriendlyByteBuf buf) {
            NetworkUtil.writeOptional(buf, msg.standSkin, buf::writeResourceLocation);
            buf.writeResourceLocation(msg.standId);
        }

        @Override
        public ClSetStandSkinPacket decode(FriendlyByteBuf buf) {
            return new ClSetStandSkinPacket(NetworkUtil.readOptional(buf, buf::readResourceLocation), 
                    buf.readResourceLocation());
        }

        @Override
        public void handle(ClSetStandSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.hasPower() && msg.standId.equals(power.getType().getRegistryName())) {
                    power.getStandInstance().ifPresent(stand -> {
                        stand.setCustomSkin(msg.standSkin, power);
                        
                        
                        boolean isSingleplayer = player.server.isSingleplayer();
                        if (isSingleplayer) {
                            stand.syncIfDirty(player);
                            if (power.getStandManifestation() instanceof StandEntity) {
                                StandEntity standEntity = (StandEntity) power.getStandManifestation();
                                
                                FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
                                data.writeVarInt(standEntity.getId());
                                EntityDataAccessor<Optional<ResourceLocation>> dataParameter = StandEntity.DATA_PARAM_STAND_SKIN;
                                int serializerId = EntityDataSerializers.getSerializedId(dataParameter.getSerializer());
                                if (serializerId >= 0) {
                                    data.writeByte(dataParameter.getId());
                                    data.writeVarInt(serializerId);
                                    dataParameter.getSerializer().write(data, stand.getSelectedSkin());
                                    data.writeByte(255);
                                    // 1.20.1's packet reads its own buffer, there is no read(ByteBuf) anymore
                                    player.connection.send(new ClientboundSetEntityDataPacket(data));
                                }
                            }
                        }
                    });
                }
            });
        }

        @Override
        public Class<ClSetStandSkinPacket> getPacketClass() {
            return ClSetStandSkinPacket.class;
        }
    }

}
