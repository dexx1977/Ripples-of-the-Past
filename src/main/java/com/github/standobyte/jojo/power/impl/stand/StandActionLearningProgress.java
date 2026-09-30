package com.github.standobyte.jojo.power.impl.stand;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.LegacyUtil;
import com.google.common.collect.ImmutableList;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class StandActionLearningProgress {
    private final EntriesMap map = new EntriesMap();
    private CompoundTag savedInvalidEntries;
    
    public float getLearningProgressPoints(StandAction action, @Nullable StandType<?> currentlyUsedStand) {
        StandActionLearningEntry entry = map.getEntry(currentlyUsedStand, action);
        return entry != null ? Math.max(entry.getPoints(), 0) : -1;
    }
    
    private static final Iterable<StandAction> EMPTY = ImmutableList.of();
    public Iterable<StandAction> getAllUnlocked(IStandPower power) {
        StandType<?> currentType = power.getType();
        if (currentType == null) {
            return EMPTY;
        }
        Map<ResourceLocation, StandActionLearningEntry> standActions = map._mapOfMaps
                .get(currentType.getRegistryName());
        if (standActions == null) {
            return Collections.emptyList();
        }
        return standActions
                .values()
                .stream()
                .map(entry -> entry.action)
                .filter(action -> action.isUnlocked(power))
                .collect(Collectors.toList());
    }
    
    boolean addEntry(StandAction action, StandType<?> standType) {
        if (standType == null || map.contains(standType, action)) {
            return false;
        }
        
        map.putEntry(new StandActionLearningEntry(action, standType, 0));
        return true;
    }
    
    @Nullable
    StandActionLearningEntry setLearningProgressPoints(StandAction action, float points, IStandPower power) {
        StandActionLearningEntry entry = map.computeIfAbsent(power.getType(), action, 0);
        if (entry != null) {
            entry.setPoints(points);
        }
        return entry;
    }
    
    void setEntryDirectly(StandActionLearningEntry entry) {
        map.putEntry(entry);
    }
    
    void syncEntryWithUser(StandActionLearningEntry entry, ServerPlayer user) {
        PacketManager.sendToClient(new StandActionLearningPacket(entry, true), user);
    }
    
    void syncFullWithUser(ServerPlayer user) {
        map.forEach(entry -> {
            PacketManager.sendToClient(new StandActionLearningPacket(entry, false), user);
        });
    }
    
    

    public void fromNBT(CompoundTag nbt) {
        savedInvalidEntries = new CompoundTag();
        map.fromNBT(nbt, savedInvalidEntries);
    }
    
    public CompoundTag toNBT() {
        return map.toNBT(savedInvalidEntries);
    }
    
    
    
    private static class EntriesMap {
        private final Map<ResourceLocation, Map<ResourceLocation, StandActionLearningEntry>> _mapOfMaps = new HashMap<>();
        
        @Nullable
        public StandActionLearningEntry getEntry(@Nullable StandType<?> standType, StandAction action) {
            if (standType == null || !_mapOfMaps.containsKey(standType.getRegistryName())) {
                return null;
            }
            
            Map<ResourceLocation, StandActionLearningEntry> map = _mapOfMaps.get(standType.getRegistryName());
            return map.get(action.getRegistryName());
        }
        
        public void putEntry(StandActionLearningEntry entry) {
            Map<ResourceLocation, StandActionLearningEntry> map = _mapOfMaps.computeIfAbsent(
                    entry.standType.getRegistryName(), type -> new HashMap<>());
            map.put(entry.action.getRegistryName(), entry);
        }
        
        @Nullable
        public StandActionLearningEntry computeIfAbsent(StandType<?> standType, StandAction action, float newEntryPoints) {
            if (standType == null) {
                return null;
            }
            
            Map<ResourceLocation, StandActionLearningEntry> map = _mapOfMaps.computeIfAbsent(
                    standType.getRegistryName(), type -> new HashMap<>());
            return map.computeIfAbsent(action.getRegistryName(), a -> new StandActionLearningEntry(action, standType, newEntryPoints));
        }
        
        public boolean contains(StandType<?> standType, StandAction action) {
            if (standType == null || !_mapOfMaps.containsKey(standType.getRegistryName())) {
                return false;
            }
            
            Map<ResourceLocation, StandActionLearningEntry> map = _mapOfMaps.get(standType.getRegistryName());
            return map.containsKey(action.getRegistryName());
        }
        
        public void forEach(Consumer<StandActionLearningEntry> action) {
            _mapOfMaps.values().stream()
            .flatMap(map -> map.values().stream())
            .forEach(action);
        }
        
        public CompoundTag toNBT(CompoundTag invalidEntriedSrc) {
            CompoundTag nbt = new CompoundTag();
            _mapOfMaps.forEach((standType, map) -> {
                CompoundTag standTypeNbt = new CompoundTag();
                map.forEach((action, entry) -> {
                    standTypeNbt.putFloat(action.toString(), entry.getPoints());
                });
                nbt.put(standType.toString(), standTypeNbt);
            });
            if (invalidEntriedSrc != null) nbt.merge(invalidEntriedSrc);
            return nbt;
        }
        
        public void fromNBT(CompoundTag nbt, CompoundTag invalidEntriedDest) {
            _mapOfMaps.clear();
            
            nbt.getAllKeys().forEach(standTypeName -> {
                if (standTypeName.isEmpty()) return;
                CompoundTag standTypeNbt = nbt.getCompound(standTypeName);
                StandType<?> standType = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standTypeName));
                if (standType == null) {
                    
                    Optional<StandActionLearningEntry> entryLegacy = LegacyUtil.readOldStandActionLearning(nbt, standTypeName);
                    if (entryLegacy.isPresent()) {
                        putEntry(entryLegacy.get());
                    }
                    else {
                        CompoundTag invalidStandTypeNbt = MCUtil.getOrCreateCompound(invalidEntriedDest, standTypeName);
                        standTypeNbt.getAllKeys().forEach(actionName -> {
                            invalidStandTypeNbt.put(actionName, standTypeNbt.get(actionName));
                        });
                    }
                    
                    return;
                }

                standTypeNbt.getAllKeys().forEach(actionName -> {
                    if (actionName.isEmpty()) return;
                    Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(new ResourceLocation(actionName));
                    
                    if (action instanceof StandAction) {
                        StandActionLearningEntry entry = new StandActionLearningEntry((StandAction) action, standType, standTypeNbt.getFloat(actionName));
                        putEntry(entry);
                    }
                    else {
                        MCUtil.getOrCreateCompound(invalidEntriedDest, standTypeName)
                        .put(actionName, standTypeNbt.get(actionName));
                    }
                });
            });
        }
    }
    
    
    
    public static class StandActionLearningEntry {
        public final StandAction action;
        public final StandType<?> standType;
        private float points;
        
        public StandActionLearningEntry(StandAction action, StandType<?> standType, float points) {
            this.action = action;
            this.standType = standType;
            this.points = points;
        }
        
        private void setPoints(float points) {
            this.points = points;
        }
        
        private float getPoints() {
            return points;
        }
        
        
        public CompoundTag toNBT() {
            CompoundTag nbt = new CompoundTag();
            MCUtil.nbtPutRegistryEntry(nbt, "Stand", standType);
            nbt.putFloat("Points", points);
            return nbt;
        }
        
        public static Optional<StandActionLearningEntry> fromNBT(StandAction action, CompoundTag nbt) {
            Optional<StandType<?>> standFromNbt = MCUtil.nbtGetRegistryEntry(nbt, "Stand", JojoCustomRegistries.STANDS.getRegistry());
            return standFromNbt.flatMap(stand -> {
                if (nbt.contains("Points", MCUtil.getNbtId(FloatTag.class))) {
                    return Optional.of(new StandActionLearningEntry(action, stand, nbt.getFloat("Points")));
                }
                return Optional.empty();
            });
        }
        
        
        public void toBuf(FriendlyByteBuf buffer) {
            buffer.writeRegistryId(com.github.standobyte.jojo.init.power.JojoCustomRegistries.ACTIONS.getRegistry(), action);
            buffer.writeFloat(points);
            buffer.writeRegistryId(standType.getRegistry(), standType);
        }
        
        public static StandActionLearningEntry fromBuf(FriendlyByteBuf buffer) {
            StandAction action = (StandAction) buffer.readRegistryIdSafe(Action.class);
            float points = buffer.readFloat();
            StandType<?> standType = buffer.readRegistryIdSafe(StandType.class);
            return new StandActionLearningEntry(action, standType, points);
        }
    }
}
