package com.github.standobyte.jojo.util.mod;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

@Deprecated
public class TimeUtil {

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static void stopTime(Level world, TimeStopInstance instance) {
        TimeStopHandler.stopTime(world, instance);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static void resumeTime(Level world, int instanceId) {
        TimeStopHandler.resumeTime(world, instanceId);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static void resumeTime(Level world, TimeStopInstance instance) {
        TimeStopHandler.resumeTime(world, instance);
    }
    
    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static TimeStopInstance getTimeStopInstance(Level world, int instanceId) {
        return TimeStopHandler.getTimeStopInstance(world, instanceId);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean canPlayerSeeInStoppedTime(Player player) {
        return TimeStopHandler.canPlayerSeeInStoppedTime(player);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean canPlayerSeeInStoppedTime(boolean canMove, boolean hasTimeStopAbility) {
        return TimeStopHandler.canPlayerSeeInStoppedTime(canMove, hasTimeStopAbility);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean canPlayerMoveInStoppedTime(Player player, boolean checkEffect) {
        return TimeStopHandler.canPlayerMoveInStoppedTime(player, checkEffect);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean hasTimeStopAbility(LivingEntity entity) {
        return TimeStopHandler.hasTimeStopAbility(entity);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean isTimeStopped(Level world, BlockPos blockPos) {
        return TimeStopHandler.isTimeStopped(world, blockPos);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean isTimeStopped(Level world, ChunkPos chunkPos) {
        return TimeStopHandler.isTimeStopped(world, chunkPos);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static int getTimeStopTicksLeft(Level world, ChunkPos chunkPos) {
        return TimeStopHandler.getTimeStopTicksLeft(world, chunkPos);
    }
}