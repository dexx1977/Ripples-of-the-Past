package com.github.standobyte.jojo.mrpresident.dimension;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.MrPresidentTurtlePos;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.ITeleporter;

public class MrPresidentBackTeleporter implements ITeleporter {
    public final ServerLevel world;
    public final Vec3 pos;
    @Nullable public final Entity turtle;
    
    public MrPresidentBackTeleporter(ServerLevel world, Vec3 pos, Entity turtle) {
        this.world = world;
        this.pos = pos;
        this.turtle = turtle;
    }
    
    @Nullable
    public static MrPresidentBackTeleporter teleportBackToTurtle(MinecraftServer server, UUID turtleId) {
        for (ServerLevel world : server.getAllLevels()) {
            Entity turtle = world.getEntity(turtleId);
            if (turtle != null) {
                Vec3 pos = posToTeleportTo(turtle);
                return new MrPresidentBackTeleporter(world, pos, turtle);
            }
        }

        MrPresidentWorldData rooms = MrPresidentWorldData.get(server).orElse(null);
        if (rooms != null) {
            MrPresidentTurtlePos turtleTrackedPos = rooms.getTurtlePosition(turtleId);
            if (turtleTrackedPos != null && turtleTrackedPos.turtleDimension != null && turtleTrackedPos.turtlePos != null) {
                ServerLevel world = server.getLevel(turtleTrackedPos.turtleDimension);
                if (world != null) {
                    return new MrPresidentBackTeleporter(world, turtleTrackedPos.turtlePos, null);
                }
            }
        }
        
        return null;
    }
    
    public static Vec3 posToTeleportTo(Entity turtle) {
        return new Vec3(turtle.getX(), turtle.getY(1), turtle.getZ());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);
        entity.teleportTo(pos.x, pos.y, pos.z);
        return entity;
    }
    
    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
        return false;
    }

}
