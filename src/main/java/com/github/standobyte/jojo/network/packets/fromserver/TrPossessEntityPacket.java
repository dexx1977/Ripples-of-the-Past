package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.network.NetworkEvent;
import com.github.standobyte.jojo.init.power.RegistryEntry;

public class TrPossessEntityPacket {
    private final int entityId;
    private final int hostEntityId;
    private final boolean asAlive;
    private final Optional<GameType> prevGameMode;
    private final @Nullable RegistryEntry<?> context;

    public TrPossessEntityPacket(int entityId, int hostEntityId, boolean asAlive, 
            @Nonnull Optional<GameType> prevGameMode, @Nullable RegistryEntry<?> context) {
        this.entityId = entityId;
        this.hostEntityId = hostEntityId;
        this.asAlive = asAlive;
        this.prevGameMode = prevGameMode;
        this.context = context;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrPossessEntityPacket> {

        @Override
        public void encode(TrPossessEntityPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
            buf.writeInt(msg.hostEntityId);
            buf.writeBoolean(msg.asAlive);
            NetworkUtil.writeOptional(buf, msg.prevGameMode, buf::writeEnum);
            NetworkUtil.writeOptionally(buf, msg.context, this::fuckingGenerics);
        }
        
        private <T extends RegistryEntry<T>> void fuckingGenerics(RegistryEntry<?> entry, FriendlyByteBuf buf) {
            buf.writeRegistryId((T) entry);
        }
        
        @Override
        public TrPossessEntityPacket decode(FriendlyByteBuf buf) {
            int entityId = buf.readInt();
            int hostEntityId = buf.readInt();
            boolean asAlive = buf.readBoolean(); 
            Optional<GameType> prevGameMode = NetworkUtil.readOptional(buf, buffer -> buffer.readEnum(GameType.class));
            RegistryEntry<?> context = NetworkUtil.readOptional(buf, this::fuckingGenericsAgain).orElse(null);
            return new TrPossessEntityPacket(entityId, hostEntityId, asAlive, prevGameMode, context);
        }
        
        private RegistryEntry<?> fuckingGenericsAgain(FriendlyByteBuf buf) {
            return buf.readRegistryId();
        }
        
        @Override
        public void handle(TrPossessEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof IPlayerPossess) {
                Entity hostEntity = ClientUtil.getEntityById(msg.hostEntityId);
                IPlayerPossess player = (IPlayerPossess) entity;
                player.jojoPossessEntity(hostEntity, msg.asAlive, msg.context);
                player.jojoSetPrePossessGameMode(msg.prevGameMode);
                ForgeGui.renderSpectatorTooltip = hostEntity == null;
            }
        }

        @Override
        public Class<TrPossessEntityPacket> getPacketClass() {
            return TrPossessEntityPacket.class;
        }
    }

}
