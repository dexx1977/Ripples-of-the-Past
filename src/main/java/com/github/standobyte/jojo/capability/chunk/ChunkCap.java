package com.github.standobyte.jojo.capability.chunk;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.entity.EntityMadeFromBlock;
import com.github.standobyte.jojo.init.ModTileEntities;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.BrokenChunkBlocksPacket;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkCap {
    private final LevelChunk chunk;
    
    private boolean loadedNBT = false;
    private final Map<BlockPos, PrevBlockInfo> brokenBlocks = new HashMap<>();
    private final Map<BlockPos, Integer> brokenBlocksXp = new HashMap<>();
    private final List<PrevBlockInfo> blocksToSync = new ArrayList<>();
//    private Set<ServerPlayerEntity> syncedTo = new HashSet<>();

    public ChunkCap(LevelChunk chunk) {
        this.chunk = chunk;
    }

    public void saveBrokenBlock(BlockPos pos, BlockState state, Optional<BlockEntity> tileEntity, List<ItemStack> drops) {
        // FIXME remember blocks with inventory
        if (tileEntity.filter(te -> te instanceof Container || te.getType() == ModTileEntities.STONE_MASK.get()).isPresent()) return;
        
        saveBrokenBlock(new PrevBlockInfo(pos, state, drops, false));
    }
    
    private void saveBrokenBlock(PrevBlockInfo prevBlock) {
        if (!chunk.getLevel().isClientSide() && brokenBlocksXp.containsKey(prevBlock.pos)) {
            prevBlock.setDroppedXp(brokenBlocksXp.remove(prevBlock.pos));
        }
        brokenBlocks.put(prevBlock.pos, prevBlock);
        if (!chunk.getLevel().isClientSide()) {
            blocksToSync.add(prevBlock);
        }
    }

    public void removeBrokenBlock(BlockPos blockPos) {
        brokenBlocks.remove(blockPos);
        if (!chunk.getLevel().isClientSide()) {
            blocksToSync.add(PrevBlockInfo.clientInstance(blockPos, Blocks.AIR.defaultBlockState()));
        }
    }
    
    public void reset() {
        brokenBlocks.clear();
        if (!chunk.getLevel().isClientSide()) {
            PacketManager.sendToTrackingChunk(new BrokenChunkBlocksPacket(Collections.emptyList(), true), chunk);
        }
    }

    public void tick() {
        if (!chunk.getLevel().isClientSide()) {
            if (loadedNBT) {
                Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
                    if (CrazyDiamondRestoreTerrain.blockCanBePlaced(chunk.getLevel(), entry.getKey(), entry.getValue().state)) {
                        blocksToSync.add(entry.getValue());
                    }
                    else {
                        it.remove();
                    }
                }
                loadedNBT = false;
            }
            
//            else {
//                Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
//                    if (entry.getValue().forget()) {
//                        it.remove();
//                        blocksToSync.add(PrevBlockInfo.clientInstance(entry.getKey(), Blocks.AIR.defaultBlockState()));
//                    }
//                }
//            }
    
            if (!blocksToSync.isEmpty()) {
                PacketManager.sendToTrackingChunk(new BrokenChunkBlocksPacket(blocksToSync, false), chunk);
//                syncedTo = ((ServerChunkProvider) chunk.level().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false)
//                        .collect(Collectors.toSet());
            }
        }
    }

    // FIXME fix the blocks resetting on client after being synced
    public void onChunkLoad(ServerPlayer player) {
//        if (!chunk.level().isClientSide() && !syncedTo.contains(player) && !brokenBlocks.isEmpty()) {
//            PacketManager.sendToClient(new BrokenChunkBlocksPacket(brokenBlocks.values(), true), player);
//            syncedTo.add(player);
//        }
    }
    
    public PrevBlockInfo getBrokenBlockAt(BlockPos blockPos) {
        return brokenBlocks.get(blockPos);
    }
    
    public Stream<PrevBlockInfo> getBrokenBlocks() {
        return brokenBlocks.values().stream();
    }
    
    public boolean wasBlockBroken(BlockPos pos) {
        return brokenBlocks.containsKey(pos);
    }
    
    public void setDroppedXp(BlockPos blockPos, int xp) {
        if (brokenBlocks.containsKey(blockPos)) {
            brokenBlocks.get(blockPos).setDroppedXp(xp);
        }
        else {
            brokenBlocksXp.put(blockPos, xp);
        }
    }
    
    
    CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        if (JojoModConfig.getCommonConfigInstance(false).saveDestroyedBlocks.get()) {
            ListTag blocksBroken = new ListTag();
            for (PrevBlockInfo block : brokenBlocks.values()) {
                blocksBroken.add(block.toNBT());
            }
            nbt.put("Blocks", blocksBroken);
        }
        return nbt;
    }
    
    void load(CompoundTag nbt) {
        if (JojoModConfig.getCommonConfigInstance(false).saveDestroyedBlocks.get()
                && nbt.contains("Blocks", MCUtil.getNbtId(ListTag.class))) {
            nbt.getList("Blocks", MCUtil.getNbtId(CompoundTag.class)).forEach(blockNBT -> {
                PrevBlockInfo block = PrevBlockInfo.fromNBT((CompoundTag) blockNBT);
                if (block != null) {
                    brokenBlocks.put(block.pos, block);
                }
            });
        }
        loadedNBT = true;
    }

    
    
    public static class PrevBlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        
        public final List<ItemStack> drops;
        private int xp = 0;
        private List<WeakReference<EntityMadeFromBlock>> blockShards;
        
        public final boolean keep;
        private int tickCount = 0;
        
        public PrevBlockInfo(BlockPos pos, BlockState state, List<ItemStack> drops, boolean keep) {
            this.pos = pos;
            this.state = state;
            this.drops = drops.stream().map(stack -> stack.copy()).collect(Collectors.toCollection(ArrayList::new));
            this.keep = keep;
        }
        
        public static PrevBlockInfo clientInstance(BlockPos pos, BlockState state) {
            return new PrevBlockInfo(pos, state, new ArrayList<>(), true);
        }
        
        public void setDroppedXp(int xp) {
            this.xp = xp;
        }
        
        public int getDroppedXp() {
            return xp;
        }
        
        public void withEntities(EntityMadeFromBlock... blockShardEntities) {
            this.blockShards = Arrays.stream(blockShardEntities).map(WeakReference::new).collect(Collectors.toList());
        }
        
        public boolean onRestore() {
            if (blockShards != null) {
                for (WeakReference<EntityMadeFromBlock> shardRef : blockShards) {
                    EntityMadeFromBlock shard = shardRef.get();
                    if (shard != null && shard.isEntityAlive()) {
                        return shard.crazyDRestore(pos);
                    }
                }
            }
            return true;
        }
        
        private boolean forget() {
            return !keep && tickCount++ == 24000;
        }

        public CompoundTag toNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("Pos", NbtUtils.writeBlockPos(pos));
            nbt.put("State", NbtUtils.writeBlockState(state));
            nbt.putBoolean("Keep", keep);
            nbt.putInt("TickCount", tickCount);
            
            ListTag itemsNBT = new ListTag();
            for (ItemStack stack : drops) {
                itemsNBT.add(stack.save(new CompoundTag()));
            }
            nbt.put("Drops", itemsNBT);
            nbt.putInt("Xp", xp);
            
            return nbt;
        }

        @Nullable
        public static PrevBlockInfo fromNBT(CompoundTag nbt) {
            if (!(
                    nbt.contains("Pos", MCUtil.getNbtId(CompoundTag.class)) &&
                    nbt.contains("State", MCUtil.getNbtId(CompoundTag.class)) && 
                    nbt.contains("Drops", MCUtil.getNbtId(ListTag.class)))) {
                return null;
            }
            
            List<ItemStack> drops = new ArrayList<>();
            ListTag dropsNBT = nbt.getList("Drops", MCUtil.getNbtId(CompoundTag.class));
            for (Tag nbtElement : dropsNBT) {
                CompoundTag itemNBT = (CompoundTag) nbtElement;
                ItemStack item = ItemStack.of(itemNBT);
                if (!item.isEmpty()) {
                    drops.add(item);
                }
            }
            
            PrevBlockInfo block = new PrevBlockInfo(
                    NbtUtils.readBlockPos(nbt.getCompound("Pos")), 
                    NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("State")), 
                    drops, 
                    nbt.getBoolean("Keep"));
            block.tickCount = nbt.getInt("TickCount");
            block.xp = nbt.getInt("Xp");
            return block;
        }
        
        public void toBuf(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeVarInt(Block.getId(state));
        }
        
        public static PrevBlockInfo fromBuf(FriendlyByteBuf buf) {
            return new PrevBlockInfo(buf.readBlockPos(), Block.stateById(buf.readVarInt()), new ArrayList<>(), true);
        }
    }
}
