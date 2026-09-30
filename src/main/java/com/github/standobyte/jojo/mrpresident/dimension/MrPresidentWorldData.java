package com.github.standobyte.jojo.mrpresident.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MrPresidentWorldData implements INBTSerializable<CompoundTag> {
    private final BiMap<UUID, ChunkSectionPos> allocatedRooms = HashBiMap.create();
    private final Map<UUID, MrPresidentTurtlePos> trackedTurtlePos = new HashMap<>();

    public MrPresidentWorldData(ServerLevel world) {}
    
    public static LazyOptional<MrPresidentWorldData> get(MinecraftServer server) {
        ServerLevel world = server.getLevel(ModDimensions.MR_PRESIDENT);
        if (world != null) {
            return world.getCapability(MrPresidentWorldDataProvider.CAPABILITY);
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        
        ListTag roomsMapNbt = new ListTag();
        for (Map.Entry<UUID, ChunkSectionPos> entry : allocatedRooms.entrySet()) {
            CompoundTag roomNbt = new CompoundTag();
            roomNbt.putUUID("Turtle", entry.getKey());
            roomNbt.put("Pos", entry.getValue().toNBT());
            
            MrPresidentTurtlePos turtleTracked = trackedTurtlePos.get(entry.getKey());
            if (turtleTracked != null) {
                turtleTracked.addToNbtEntry(roomNbt);
            }
            
            roomsMapNbt.add(roomNbt);
        }
        nbt.put("Rooms", roomsMapNbt);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag inbt) {
        CompoundTag nbt = (CompoundTag) inbt;
        
        ListTag roomsNbt = nbt.getList("Rooms", Tag.TAG_COMPOUND);
        for (Tag elem : roomsNbt) {
            CompoundTag roomNbt = (CompoundTag) elem;
            ChunkSectionPos pos = ChunkSectionPos.fromNBT(roomNbt.getList("Pos", Tag.TAG_INT));
            if (pos != null && roomNbt.hasUUID("Turtle")) {
                UUID id = roomNbt.getUUID("Turtle");
                allocatedRooms.put(id, pos);
                
                MrPresidentTurtlePos turtleTracked = MrPresidentTurtlePos.fromNbtEntry(roomNbt);
                if (turtleTracked != null) {
                    trackedTurtlePos.put(id, turtleTracked);
                }
            }
        }
    }
    
    @Nullable
    public ChunkSectionPos getAllocatedRoom(UUID turtleId) {
        return allocatedRooms.get(turtleId);
    }
    
    @Nullable
    public UUID getTurtleId(ChunkSectionPos roomPos) {
        return allocatedRooms.inverse().get(roomPos);
    }
    
    public void rememberTurtlePosition(Entity user) {
        MrPresidentTurtlePos pos = this.trackedTurtlePos.computeIfAbsent(user.getUUID(), id -> new MrPresidentTurtlePos());
        pos.turtleDimension = user.level.dimension();
        pos.turtlePos = MrPresidentBackTeleporter.posToTeleportTo(user);
    }
    
    @Nullable
    public MrPresidentTurtlePos getTurtlePosition(UUID turtleId) {
        return trackedTurtlePos.get(turtleId);
    }
    
    // TODO algorithm for generating rooms position
    public ChunkSectionPos posForNewRoom(UUID turtleId) {
        int x = 0;
        int y = 0;
        int z = 0;
        
        int ring = 0;
        ChunkSectionPos pos = new ChunkSectionPos(x, y, z);
        while (allocatedRooms.containsValue(pos)) {
            if (y < 15) {
                y++;
            }
            else {
                y = 0;
                
                if (x == ring) {
                    ring++;
                    x = ring - 1;
                    z = 1;
                }
                else if (z == ring)  { z--; x = -1; }
                else if (x == -ring) { x++; z = -1; }
                else if (z == -ring) { z++; x = 1;  }
                else if (x > 0 && z > 0) { x--; z++; }
                else if (x < 0 && z > 0) { x--; z--; }
                else if (x < 0 && z < 0) { x++; z--; }
                else if (x > 0 && z < 0) { x++; z++; }
                else {
                    throw new RuntimeException("I'm a dumbass");
                }
            }
            
            pos = new ChunkSectionPos(x, y, z);
        }
        
        allocatedRooms.put(turtleId, pos);
        return pos;
    }
    
    
    public static class MrPresidentTurtlePos {
        ResourceKey<Level> turtleDimension;
        Vec3 turtlePos;
        
        private MrPresidentTurtlePos() {}
        
        MrPresidentTurtlePos(ResourceKey<Level> userDimension, Vec3 userPos) {
            this.turtleDimension = userDimension;
            this.turtlePos = userPos;
        }
        
        void addToNbtEntry(CompoundTag nbt) {
            if (turtleDimension != null && turtlePos != null) {
                nbt.putString("TurtleDim", turtleDimension.location().toString());
                MCUtil.nbtPutVec3d(nbt, "TurtlePos", turtlePos);
            }
        }
        
        @Nullable
        static MrPresidentTurtlePos fromNbtEntry(CompoundTag nbt) {
            if (nbt.contains("TurtleDim", Tag.TAG_STRING)) {
                ResourceLocation dimensionId = new ResourceLocation(nbt.getString("TurtleDim"));
                ResourceKey<Level> dimension = MCUtil.getRegistryKeyIfPresent(Registry.DIMENSION_REGISTRY, dimensionId);
                if (dimension == null) {
                    return null;
                }
                
                Vec3 pos = MCUtil.nbtGetVec3d(nbt, "TurtlePos");
                if (pos == null) {
                    return null;
                }
                
                return new MrPresidentTurtlePos(dimension, pos);
            }
            
            return null;
        }
    }
    
    public static class ChunkSectionPos {
        public final int x;
        public final int y;
        public final int z;
        
        public ChunkSectionPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public ChunkSectionPos(BlockPos blockPos) {
            this.x = blockPos.getX() >> 4;
            this.y = blockPos.getY() >> 4;
            this.z = blockPos.getZ() >> 4;
        }
        
        public boolean isPosInsideSection(BlockPos blockPos) {
            return 
                    this.x == blockPos.getX() >> 4 &&
                    this.y == blockPos.getY() >> 4 &&
                    this.z == blockPos.getZ() >> 4;
        }
        
        public static ChunkSectionPos fromNBT(ListTag nbt) {
            if (nbt.size() == 3 && nbt.getElementType() == Tag.TAG_INT) {
                int x = nbt.getInt(0);
                int y = nbt.getInt(1);
                int z = nbt.getInt(2);
                return new ChunkSectionPos(x, y, z);
            }
            
            return null;
        }
        
        public BlockPos blockPosition(int xLLOffset, int yLLOffset, int zLLOffset) {
            return new BlockPos((this.x << 4) + xLLOffset, (this.y << 4) + yLLOffset, (this.z << 4) + zLLOffset);
        }
        
        public BlockPos blockPosition(BlockPos llOffset) {
            return llOffset.offset(this.x << 4, this.y << 4, this.z << 4);
        }
        
        public ListTag toNBT() {
            ListTag nbt = new ListTag();
            nbt.add(IntTag.valueOf(x));
            nbt.add(IntTag.valueOf(y));
            nbt.add(IntTag.valueOf(z));
            return nbt;
        }
        
        @Override
        public String toString() {
            return "[" + x + ", " + y + ", " + z + "]";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof ChunkSectionPos)) {
                return false;
            } else {
                ChunkSectionPos other = (ChunkSectionPos) obj;
                if (this.x != other.x) {
                    return false;
                } else if (this.y != other.y) {
                    return false;
                } else {
                    return this.z == other.z;
                }
            }
        }
        
        @Override
        public int hashCode() {
            return (y + z * 31) * 31 + x;
        }
    }
}
