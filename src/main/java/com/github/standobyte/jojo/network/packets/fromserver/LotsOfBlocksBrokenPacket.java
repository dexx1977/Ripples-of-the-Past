package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.TriConsumer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.google.common.collect.Streams;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

public class LotsOfBlocksBrokenPacket {
    public List<BrokenBlock> brokenBlocks;
    
    public LotsOfBlocksBrokenPacket() {
        this(new ArrayList<>());
    }
    
    private LotsOfBlocksBrokenPacket(List<BrokenBlock> brokenBlocks) {
        this.brokenBlocks = brokenBlocks;
    }
    
    public void addBlock(BlockPos blockPos, BlockState blockState) {
        brokenBlocks.add(new BrokenBlock(blockPos, blockState));
    }
    
    public void sendToPlayers(ServerLevel world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (brokenBlocks.isEmpty()) return;
        
        brokenBlocks = GeneralUtil.limitRandom(brokenBlocks, 256);
        
        final double radius = 64;
        for (ServerPlayer player : world.players()) {
            if (player.level.dimension() == world.dimension()) {
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();

                double xDiff = x < minX ? minX - x : x > maxX ? x - maxX : 0;
                double yDiff = y < minY ? minY - y : y > maxY ? y - maxY : 0;
                double zDiff = z < minZ ? minZ - z : z > maxZ ? z - maxZ : 0;
                if (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff < radius * radius) {
                    PacketManager.sendToClient(this, player);
                }
            }
        }
    }
    
    
    private static class BrokenBlock {
        private final BlockPos blockPos;
        private final int blockStateData;
        private BlockState blockState;
        
        private BrokenBlock(BlockPos blockPos, int data) {
            this.blockPos = blockPos;
            this.blockStateData = data;
        }
        
        private BrokenBlock(BlockPos blockPos, BlockState blockState) {
            this(blockPos, Block.getId(blockState));
            this.blockState = blockState;
        }
        
        void toBuf(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(this.blockPos);
            buffer.writeInt(this.blockStateData);
        }
        
        static BrokenBlock fromBuf(FriendlyByteBuf buffer) {
            BlockPos blockPos = buffer.readBlockPos();
            int data = buffer.readInt();
            return new BrokenBlock(blockPos, data);
        }
        
        void handleResolveBlockState() {
            this.blockState = Block.stateById(blockStateData);
        }
    }
    
    public void forEachBlock(boolean network, TriConsumer<BlockPos, BlockState, Long> action) {
        Stream<BrokenBlock> stream = brokenBlocks.stream();
        if (brokenBlocks.size() > 128) {
            Vec3 cameraPos = ClientUtil.getCameraPos();
            stream = stream
                    .sorted(Comparator.comparingDouble(block -> block.blockPos.distToCenterSqr(cameraPos.x, cameraPos.y, cameraPos.z)))
                    .limit(128);
        }
        Streams.mapWithIndex(stream, (block, index) -> {
            if (network) {
                block.handleResolveBlockState();
            }
            action.accept(block.blockPos, block.blockState, index);
            return block;
        }).forEach(block -> {});
    }
    
    public static void blockBreakVisuals(BlockPos blockPos, BlockState blockState, long i) {
        Level world = ClientUtil.getClientWorld();
        if (!blockState.isAir()) {
            int particlesSetting = ClientUtil.particlesSetting();
            if (particlesSetting < 2 && (particlesSetting < 1 || i % 2 == 0)) {
                CustomParticlesHelper.addBlockBreakParticles(blockPos, blockState);
            }
            SoundType soundType = blockState.getSoundType(world, blockPos, null);
            if (i % 8 == 0) {
                world.playLocalSound(
                        blockPos.getX() + 0.5, 
                        blockPos.getY() + 0.5, 
                        blockPos.getZ() + 0.5, 
                        soundType.getBreakSound(), SoundSource.BLOCKS, 
                        (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            }
        }
    }
    
    
    
    public static class Handler implements IModPacketHandler<LotsOfBlocksBrokenPacket> {

        @Override
        public void encode(LotsOfBlocksBrokenPacket msg, FriendlyByteBuf buf) {
            NetworkUtil.writeCollection(buf, msg.brokenBlocks, BrokenBlock::toBuf, false);
        }

        @Override
        public LotsOfBlocksBrokenPacket decode(FriendlyByteBuf buf) {
            return new LotsOfBlocksBrokenPacket(NetworkUtil.readCollection(buf, BrokenBlock::fromBuf));
        }

        @Override
        public void handle(LotsOfBlocksBrokenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            msg.forEachBlock(true, LotsOfBlocksBrokenPacket::blockBreakVisuals);
        }

        @Override
        public Class<LotsOfBlocksBrokenPacket> getPacketClass() {
            return LotsOfBlocksBrokenPacket.class;
        }
    }
}
