package com.github.standobyte.jojo.capability.world;

import net.minecraftforge.event.entity.living.MobEffectEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket.Phase;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityDataPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.HashBiMap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeStopHandler {
    private final Level world;
    private final Set<Entity> stoppedInTime = new HashSet<>();
    private final Set<ServerPlayer> playersVisionFrozen = new HashSet<>();
    private final Map<Integer, TimeStopInstance> timeStopInstances = HashBiMap.create();
    
    public TimeStopHandler(Level world) {
        this.world = world;
    }
    
    @SuppressWarnings("deprecation")
    public void tick() {
        Iterator<Entity> entityIter = stoppedInTime.iterator();
        
        while (entityIter.hasNext()) {
            Entity entity = entityIter.next();
            if (entity.isRemoved()) {
                entityIter.remove();
            }

            else if (!entity.canUpdate()) {
                tickInStoppedTime(entity);
            }
        }

        if (!timeStopInstances.isEmpty()) {
            Iterator<Map.Entry<Integer, TimeStopInstance>> instanceIter = timeStopInstances.entrySet().iterator();
            while (instanceIter.hasNext()) {
                Map.Entry<Integer, TimeStopInstance> entry = instanceIter.next();
                if (entry.getValue().tick() && !world.isClientSide()) {
                    instanceIter.remove();
                    onRemovedTimeStop(entry.getValue());
                }
            }
            
            if (!playersVisionFrozen.isEmpty()) {
                manualEntitiesDataSync();
            }
        }
    }
    
    private void manualEntitiesDataSync() {
        if (!world.isClientSide()) {
            for (Entity entity : MCUtil.getAllEntities(world)) {
                SynchedEntityData entityData = entity.getEntityData();
                if (entityData.isDirty()) {
                    Set<ServerPlayer> trackingPlayers = MCUtil.getTrackingPlayers(entity);
                    List<ServerPlayer> frozenPlayers = new ArrayList<>();
                    
                    List<SynchedEntityData.DataValue<?>> packedData = null;
                    Iterator<ServerPlayer> trackingIterator = trackingPlayers.iterator();
                    while (trackingIterator.hasNext()) {
                        ServerPlayer player = trackingIterator.next();
                        if (playersVisionFrozen.contains(player)) {
                            frozenPlayers.add(player);
                            packedData = entityData.packDirty();
                        }
                    }
                    
                    boolean manualSelectiveSync = packedData != null;
                    if (manualSelectiveSync) {
                        List<SynchedEntityData.DataValue<?>> dataToKeep = packedData;
                        for (ServerPlayer tracking : trackingPlayers) {
                            if (frozenPlayers.contains(tracking)) {
                                tracking.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                                    cap.addDataForTSUnfreeze(entity, dataToKeep);
                                });
                            }
                            else {
                                PacketManager.sendToClient(new TrDirectEntityDataPacket(entity.getId(), packedData), tracking);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void tickInStoppedTime(Entity entity) {
        if (!world.isClientSide()) {
            if (entity instanceof LivingEntity) {
                if (entity.invulnerableTime > 0) {
                    entity.invulnerableTime--;
                }
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.lastHurtByStandTick());
            }
            else if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity) entity;
                if (itemEntity.pickupDelay > 0 && itemEntity.pickupDelay != 32767) {
                    --itemEntity.pickupDelay;
                }
            }
        }
        entity.tickCount--;
    }
    
    public boolean isTimeStopped(ChunkPos chunkPos) {
        return timeStopInstances.values().stream()
                .anyMatch(instance -> instance.isTimeStopped(chunkPos));
    }
    
    public int getTimeStopTicks(ChunkPos chunkPos) {
        return timeStopInstances.values().stream()
                .filter(instance -> instance.inRange(chunkPos))
                .mapToInt(TimeStopInstance::getTicksLeft)
                .max()
                .orElse(0);
    }
    
    public Set<TimeStopInstance> getInstancesInPos(ChunkPos chunkPos) {
        return timeStopInstances.values().stream()
                .filter(instance -> instance.inRange(chunkPos))
                .collect(Collectors.toSet());
    }
    
    
    
    public void addTimeStop(TimeStopInstance instance) {
        if (!timeStopInstances.containsKey(instance.getId()) && !userStoppedTime(instance.user).isPresent()) {
            timeStopInstances.put(instance.getId(), instance);
            onAddedTimeStop(instance);
        }
    }
    
    public Optional<TimeStopInstance> userStoppedTime(LivingEntity user) {
        return timeStopInstances.values().stream()
                .filter(instance -> instance.user != null && instance.user.is(user))
                .findFirst();
    }
    
    private void onAddedTimeStop(TimeStopInstance instance) {
        MCUtil.getAllEntities(world).forEach(entity -> {
            if (instance.inRange(TimeStopHandler.getChunkPos(entity))) {
                updateEntityTimeStop(entity, false, true);
            }
        });
        
        if (!world.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) world;
            
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    instance.syncToClient(player);
                    sendPlayerState(player);
                }
            });
            
            if (timeStopInstances.size() == 1) {
                SaveFileUtilCapProvider.getSaveFileCap(serverWorld.getServer()).setTimeStopGamerules(serverWorld);
            }
            else {
                timeStopInstances.values().forEach(existingInstance -> existingInstance.removeSoundsIfCrosses(instance));
            }
        }
    }
    
    boolean hasTimeStopInstances() {
        return !timeStopInstances.isEmpty();
    }

    public void updateEntityTimeStop(Entity entity, boolean canMove, boolean checkEffect) {
        Entity entityToCheck = entity;
        if (entity instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) entity;
            if (standEntity.getUser() != null) {
                entityToCheck = standEntity.getUser();
            }
        }
        else if (entity instanceof SoulEntity) {
            SoulEntity soulEntity = (SoulEntity) entity;
            if (soulEntity.getOriginEntity() != null) {
                entityToCheck = soulEntity.getOriginEntity();
            }
        }
        
        canMove = canMove || checkEffect && entityToCheck instanceof LivingEntity && ((LivingEntity) entityToCheck).hasEffect(ModStatusEffects.TIME_STOP.get()) || 
                entityToCheck instanceof Player && canPlayerMoveInStoppedTime((Player) entityToCheck, false)
                || JojoModConfig.getCommonConfigInstance(entity.level.isClientSide()).endermenBeyondTimeSpace.get() && ModInteractionUtil.isEntityEnderman(entityToCheck); // for even more lulz
        
        boolean stopInTime = !canMove;
        entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.updateEntityTimeStop(stopInTime));
        
        if (stopInTime) {
            stoppedInTime.add(entity);
        }
        else {
            stoppedInTime.remove(entity);
        }
    }
    
    
    
    public void removeTimeStop(TimeStopInstance instance) {
        if (instance != null) {
            timeStopInstances.remove(instance.getId());
            onRemovedTimeStop(instance);
        }
    }
    
    public void reset() {
        timeStopInstances.clear();
        MCUtil.getAllEntities(world).forEach(entity -> {
            updateEntityTimeStop(entity, true, true);
        });
    }
    
    private void onRemovedTimeStop(TimeStopInstance instance) {
        MCUtil.getAllEntities(world).forEach(entity -> {
            ChunkPos pos = TimeStopHandler.getChunkPos(entity);
            if (instance.inRange(pos)) {
                updateEntityTimeStop(entity, !isTimeStopped(pos), true);
            }
        });
        
        if (!world.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) world;
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    PacketManager.sendToClient(TimeStopInstancePacket.timeResumed(instance.getId()), player);
                    sendPlayerState(player);
                }
            });
            if (timeStopInstances.isEmpty()) {
                SaveFileUtilCapProvider.getSaveFileCap(serverWorld.getServer()).restoreTimeStopGamerules(serverWorld);
            }
        }
        
        instance.onRemoved(world);
    }
    
    public TimeStopInstance getById(int id) {
        return timeStopInstances.get(id);
    }
    
    public void sendPlayerState(ServerPlayer player) {
        boolean canMove = true;
        boolean canSee = true;
        if (isTimeStopped(player.level, player.blockPosition())) {
            canMove = canPlayerMoveInStoppedTime(player, true);
            canSee = canPlayerSeeInStoppedTime(canMove, hasTimeStopAbility(player));
        }
        PacketManager.sendToClient(new TimeStopPlayerStatePacket(canSee, canMove), player);
        
        if (canSee) {
            playersVisionFrozen.remove(player);
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.sendDataOnTSUnfreeze());
        }
        else {
            playersVisionFrozen.add(player);
        }
    }
    
    public Stream<TimeStopInstance> getAllTimeStopInstances() {
        return timeStopInstances.values().stream();
    }
    
    
    
    public static void stopTime(Level world, TimeStopInstance instance) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        cap.getTimeStopHandler().addTimeStop(instance);
    }
    
    public static void resumeTime(Level world, int instanceId) {
        TimeStopHandler timeStopHandler = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler();
        timeStopHandler.removeTimeStop(timeStopHandler.getById(instanceId));
    }
    
    public static void resumeTime(Level world, TimeStopInstance instance) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        cap.getTimeStopHandler().removeTimeStop(instance);
    }
    
    public static TimeStopInstance getTimeStopInstance(Level world, int instanceId) {
        TimeStopHandler timeStopHandler = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler();
        return timeStopHandler.getById(instanceId);
    }
    
    public static boolean canPlayerSeeInStoppedTime(Player player) {
        return canPlayerSeeInStoppedTime(canPlayerMoveInStoppedTime(player, true), hasTimeStopAbility(player));
    }
    
    public static boolean canPlayerSeeInStoppedTime(boolean canMove, boolean hasTimeStopAbility) {
        return canMove || hasTimeStopAbility;
    }
    
    public static boolean canPlayerMoveInStoppedTime(Player player, boolean checkEffect) {
        return checkEffect && player.hasEffect(ModStatusEffects.TIME_STOP.get()) || gamemodeIgnoresTimeStop(player) || 
                player instanceof ServerPlayer && ((ServerPlayer) player).server.isSingleplayerOwner(player.getGameProfile());
    }
    
    public static boolean gamemodeIgnoresTimeStop(Player player) {
        return JojoModUtil.getActualGameModeWhilePossessing(player)
                .map(gameMode -> gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR)
                .orElseGet(() -> player.isCreative() || player.isSpectator());
    }
    
    public static boolean hasTimeStopAbility(LivingEntity entity) {
        return IStandPower.getStandPowerOptional(entity).map(stand -> 
        stand.hasUnlockedMatching(action -> allowsToSeeInStoppedTime(action, stand, entity)))
                .orElse(false);
    }
    
    private static <P extends IPower<P, ?>> boolean allowsToSeeInStoppedTime(Action<P> action, P power, LivingEntity user) {
        return action.canUserSeeInStoppedTime(user, power) && action.isUnlocked(power);
    }
    
    

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            stopNewEntityInTime(entity, event.getLevel());
        }
    }
    
    public static void stopNewEntityInTime(Entity entity, Level world) {
        if (isTimeStopped(world, entity.blockPosition())) {
            world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> 
            cap.getTimeStopHandler().updateEntityTimeStop(entity, false, true));
        }
    }

    
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        sendWorldTimeStopData((ServerPlayer) event.getEntity());
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        sendWorldTimeStopData((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        sendWorldTimeStopData((ServerPlayer) event.getEntity());
    }
    
    private static void sendWorldTimeStopData(ServerPlayer player) {
        player.level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            PacketManager.sendToClient(new TimeStopPlayerJoinPacket(Phase.PRE), player);
            cap.getTimeStopHandler().getInstancesInPos(new ChunkPos(player.blockPosition())).forEach(instance -> {
                instance.syncToClient(player);
            });
            
            cap.getTimeStopHandler().sendPlayerState(player);
            
            stopNewEntityInTime(player, player.level);
            PacketManager.sendToClient(new TimeStopPlayerJoinPacket(Phase.POST), player);
        });
    }
    
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (serverPlayer.getServer().getPlayerList().getPlayerCount() <= 1) {
                serverPlayer.getServer().getAllLevels().forEach(world -> {
                    world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        TimeStopHandler handler = cap.getTimeStopHandler();
                        handler.reset();
                    });
                });
            }
            else {
                player.level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    TimeStopHandler handler = cap.getTimeStopHandler();
                    handler.userStoppedTime(player).ifPresent(instance -> handler.removeTimeStop(instance));
                });
            }
        }
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerWorldTick(WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        event.world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.tick();
        });
        if (event.world.dimension() == Level.OVERWORLD) {
            event.world.getCapability(SaveFileUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.tick();
            });
        }
    }



    @SubscribeEvent
    public static void onTSEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getOldEffectInstance() == null && event.getEffectInstance().getEffect() == ModStatusEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler().updateEntityTimeStop(entity, true, false);
            if (!entity.level.isClientSide()) {
                ((ServerLevel) entity.level).getChunkSource().broadcast(entity, (new ClientboundUpdateMobEffectPacket(entity.getId(), event.getEffectInstance())));
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, true), entity);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTSEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getEffectInstance().getEffect() == ModStatusEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            WorldUtilCap worldCap = entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
            worldCap.getTimeStopHandler().updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, false), entity);
                if (worldCap.getTimeStopHandler().getTimeStopTicks(new ChunkPos(entity.blockPosition())) >= 40 && 
                        IStandPower.getStandPowerOptional(entity).map(stand -> 
                        stand.hasPower() && stand.getType() == ModStands.THE_WORLD.getStandType()).orElse(false)) {
                    JojoModUtil.sayVoiceLine(entity, ModSounds.DIO_CANT_MOVE.get());
                };
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTSEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getEffect() == ModStatusEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler().updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, false), entity);
            }
        }
    }
    
    
    
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void cancelBlockNeighborUpdate(NeighborNotifyEvent event) {
//        if (isTimeStopped((World) event.level(), event.getPos())) {
//            event.setCanceled(true);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void cancelFluidPlacingBlock(FluidPlaceBlockEvent event) {
//        if (isTimeStopped((World) event.level(), event.getPos())) {
//            event.setNewState(event.getOriginalState());
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void cancelFluidSourceCreation(CreateFluidSourceEvent event) {
//        if (isTimeStopped((World) event.level(), event.getPos())) {
//            event.setResult(Result.DENY);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void cancelCropGrowth(CropGrowEvent.Pre event) {
//        if (isTimeStopped((World) event.level(), event.getPos())) {
//            event.setResult(Result.DENY);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void cancelPistonMovement(PistonEvent.Pre event) {
//        if (isTimeStopped((World) event.level(), event.getPos())) {
//            event.setCanceled(true);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void cancelNoteBlock(NoteBlockEvent.Play event) {
//        if (isTimeStopped((World) event.level(), event.getPos())) {
//            event.setCanceled(true);
//        }
//    }
    
    
    
    public static boolean isTimeStopped(Level world, BlockPos blockPos) {
        return isTimeStopped(world, new ChunkPos(blockPos));
    }
    
    public static boolean isTimeStopped(Level world, ChunkPos chunkPos) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).map(cap -> cap.getTimeStopHandler().isTimeStopped(chunkPos)).orElse(false);
    }
    
    public static int getTimeStopTicksLeft(Level world, ChunkPos chunkPos) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve()
                .flatMap(cap -> cap.getTimeStopHandler().getInstancesInPos(chunkPos).stream()
                        .max((i1, i2) -> i1.getTicksLeft() - i2.getTicksLeft())
                        .map(TimeStopInstance::getTicksLeft))
                .orElse(0);
    }
    

    
    public static ChunkPos getChunkPos(Entity entity) {
        return new ChunkPos(entity.blockPosition());
    }
}
