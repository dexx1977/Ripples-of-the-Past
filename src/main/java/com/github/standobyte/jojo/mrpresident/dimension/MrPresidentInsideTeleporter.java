package com.github.standobyte.jojo.mrpresident.dimension;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.init.ModStructures;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.ITeleporter;

public class MrPresidentInsideTeleporter implements ITeleporter {
    private final UUID turtleUUID;

    public MrPresidentInsideTeleporter(UUID turtleUUID) {
        this.turtleUUID = turtleUUID;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        MrPresidentWorldData rooms = destinationWorld.getCapability(MrPresidentWorldDataProvider.CAPABILITY).orElse(null);
        if (rooms != null) {
            entity = repositionEntity.apply(false);
            MrPresidentWorldData.ChunkSectionPos roomChunkSectionPos = rooms.getAllocatedRoom(turtleUUID);
            boolean generateRoom = false;
            if (roomChunkSectionPos == null) {
                roomChunkSectionPos = rooms.posForNewRoom(turtleUUID);
                generateRoom = true;
            }
            BlockPos cornerPos = new BlockPos(
                    roomChunkSectionPos.x << 4,
                    roomChunkSectionPos.y << 4,
                    roomChunkSectionPos.z << 4);
            
            if (generateRoom) {
                ModStructures.CONFIGURED_MR_PRESIDENT_ROOM.get().place(destinationWorld, 
                        destinationWorld.getChunkSource().getGenerator(), destinationWorld.getRandom(), cornerPos);
            }
            Vec3 pos = new Vec3(cornerPos.getX() + 8, cornerPos.getY() + 6, cornerPos.getZ() + 8);
            entity.teleportTo(pos.x, pos.y, pos.z);
        }
        
        return entity;
    }
    
    @Nullable
    public static BlockPos getLowerCornerRoomPos(ServerLevel mrPresidentDimension, UUID roomId) {
        MrPresidentWorldData rooms = mrPresidentDimension.getCapability(MrPresidentWorldDataProvider.CAPABILITY).orElse(null);
        if (rooms != null) {
            MrPresidentWorldData.ChunkSectionPos roomChunkSectionPos = rooms.getAllocatedRoom(roomId);
            if (roomChunkSectionPos != null) {
                return new BlockPos(
                        roomChunkSectionPos.x << 4,
                        roomChunkSectionPos.y << 4,
                        roomChunkSectionPos.z << 4);
            }
        }
        return null;
    }
    
    public static final Vec3i ROOM_SIZE = new Vec3i(16, 16, 16);
    
    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
        return false;
    }

}
