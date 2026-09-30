package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

public class CDBlocksRestoredPacket {
    private final Collection<BlockPos> positions;

    public CDBlocksRestoredPacket(Collection<BlockPos> positions) {
        this.positions = positions;
    }
    
    
    
    public static class Handler implements IModPacketHandler<CDBlocksRestoredPacket> {

        @Override
        public void encode(CDBlocksRestoredPacket msg, FriendlyByteBuf buf) {
            NetworkUtil.writeCollection(buf, msg.positions, buf::writeBlockPos, false);
        }

        @Override
        public CDBlocksRestoredPacket decode(FriendlyByteBuf buf) {
            return new CDBlocksRestoredPacket(NetworkUtil.readCollection(buf, FriendlyByteBuf::readBlockPos));
        }

        @Override
        public void handle(CDBlocksRestoredPacket msg, Supplier<Context> ctx) {
            // FIXME do not send these packets to non-stand users at all
            if (ClientUtil.canSeeStands()) {
                Level world = ClientUtil.getClientWorld();
                msg.positions.forEach(pos -> CrazyDiamondRestoreTerrain.addParticlesAroundBlock(world, pos, world.getRandom()));
            }
        }

        @Override
        public Class<CDBlocksRestoredPacket> getPacketClass() {
            return CDBlocksRestoredPacket.class;
        }
    }
}
