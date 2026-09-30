package com.github.standobyte.jojo.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;

import io.netty.handler.codec.DecoderException;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import com.github.standobyte.jojo.init.power.RegistryEntry;
import net.minecraftforge.registries.RegistryManager;

public class NetworkUtil {
    public static boolean blockPacketsToServer = false;

    public static void broadcastWithCondition(List<ServerPlayer> players, @Nullable Player clientHandled, 
            double x, double y, double z, double radius, Level world, 
            Packet<ClientGamePacketListener> packet, Predicate<Player> condition) {
        for (ServerPlayer player : players) {
            if (player != clientHandled && player.level.dimension() == world.dimension()
                    && condition.test(player) && player.position().subtract(x, y, z).lengthSqr() < radius * radius) {
                player.connection.send(packet);
            }
        }
    }

    public static void broadcastWithCondition(List<ServerPlayer> players, 
            Packet<ClientGamePacketListener> packet, Predicate<Player> condition) {
        for (ServerPlayer player : players) {
            if (condition.test(player)) {
                player.connection.send(packet);
            }
        }
    }
    
    
    
    public static <T extends RegistryEntry<T>> void writeRegistryIds(IForgeFriendlyByteBuf buf, @Nonnull List<T> entries) {
        Objects.requireNonNull(entries, "Cannot write a null registry entries list!");
        buf.writeBoolean(!entries.isEmpty());
        if (entries.isEmpty()) return;
        IForgeRegistry<T> retrievedRegistry = null;
        for (T entry : entries) {
            Objects.requireNonNull(entry, "Cannot write a null registry entry!");
            IForgeRegistry<T> entryRegistry = entry.getRegistry();
            Preconditions.checkArgument(entryRegistry != null, "Cannot write registry id for an unknown registry type: %s", entry.getClass().getName());
            if (retrievedRegistry == null) retrievedRegistry = entryRegistry;
            Preconditions.checkArgument(retrievedRegistry == entryRegistry, "Cannot write entries of different registry types: %s, %s",
                    retrievedRegistry.getRegistrySuperType().getName(), entryRegistry.getRegistrySuperType().getName());
            Preconditions.checkArgument(retrievedRegistry.containsValue(entry), "Cannot find %s in %s",
                    entry.getRegistryName() != null ? entry.getRegistryName() : entry, retrievedRegistry.getRegistryName());
        }
        ResourceLocation name = retrievedRegistry.getRegistryName();
        ForgeRegistry<T> reg = (ForgeRegistry<T>) retrievedRegistry;
        buf.writeResourceLocation(name);
        buf.writeVarInt(entries.size());
        for (T entry : entries) {
            buf.writeVarInt(reg.getID(entry));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends RegistryEntry<T>> List<T> readRegistryIds(IForgeFriendlyByteBuf buf) {
        if (!buf.readBoolean()) return Collections.emptyList();
        ResourceLocation location = buf.readResourceLocation();
        ForgeRegistry<T> registry = (ForgeRegistry<T>) (ForgeRegistry<?>) RegistryManager.ACTIVE.getRegistry(location);
        int size = buf.readVarInt();
        List<T> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(registry.getValue(buf.readVarInt()));
        }
        return entries;
    }

    public static <T extends RegistryEntry<T>> List<T> readRegistryIdsSafe(IForgeFriendlyByteBuf buf, Class<? super T> registrySuperType) {
        List<T> values = readRegistryIds(buf);
        for (T value : values) {
            if (!registrySuperType.isAssignableFrom(value.getClass()))
                throw new IllegalArgumentException("Attempted to read an registryValue of the wrong type from the Buffer!");
        }
        return values;
    }

    public static void writeBlockState(FriendlyByteBuf buf, BlockState blockState) {
        buf.writeVarInt(Block.getId(blockState));
    }
    
    public static BlockState readBlockState(FriendlyByteBuf buf) {
        return GameData.getBlockStateIDMap().byId(buf.readVarInt());
    }
    
    
    public static FriendlyByteBuf writeFloatArray(FriendlyByteBuf buf, float[] arr) {
        buf.writeVarInt(arr.length);
        for (float num : arr) {
            buf.writeFloat(num);
        }
        return buf;
    }
    
    public static float[] readFloatArray(FriendlyByteBuf buf) {
        return readFloatArray(buf, buf.readableBytes() / 4);
    }

    public static float[] readFloatArray(FriendlyByteBuf buf, int maxAllowed) {
        int n = buf.readVarInt();
        if (n > maxAllowed) {
            throw new DecoderException("FloatArray with size " + n + " is bigger than allowed " + maxAllowed);
        } else {
            float[] arr = new float[n];
            for (int i = 0; i < n; i++) {
                arr[i] = buf.readFloat();
            }
            return arr;
        }
    }

    public static FriendlyByteBuf writeIntArray(FriendlyByteBuf buf, int[] arr) {
        buf.writeVarInt(arr.length);

        for (int i : arr) {
            buf.writeInt(i);
        }

        return buf;
    }

    public static int[] readIntArray(FriendlyByteBuf buf) {
        return readIntArray(buf, buf.readableBytes());
    }

    public static int[] readIntArray(FriendlyByteBuf buf, int maxAllowed) {
        int n = buf.readVarInt();
        if (n > maxAllowed) {
            throw new DecoderException("IntArray with size " + n + " is bigger than allowed " + maxAllowed);
        } else {
            int[] arr = new int[n];

            for(int i = 0; i < arr.length; ++i) {
                arr[i] = buf.readInt();
            }

            return arr;
        }
    }
    
    /**
     * Supports enums with up to 255 elements
     */
    public static <T extends Enum<T>> FriendlyByteBuf writeSmallEnumArray(FriendlyByteBuf buf, T[] input) {
        int[] ordinals = GeneralUtil.toOrdinals(input);
        buf.writeVarInt(input.length);
        for (int i = 0; i < input.length; i++) {
            int ordinal = ordinals[i];
            if (ordinal >= 0 && ordinal < 255) {
                buf.writeByte(ordinal);
            }
            else {
                buf.writeByte(255);
            }
        }
        return buf;
    }
    
    public static <T extends Enum<T>> T[] readSmallEnumArray(FriendlyByteBuf buf, Class<T> enumClass) {
        int length = buf.readVarInt();
        T[] enumValues = enumClass.getEnumConstants();
        T[] ret = ObjectArrays.newArray(enumClass, length);
        for (int i = 0; i < length; i++) {
            int ordinal = 0xFF & buf.readByte();
            if (ordinal >= 0 && ordinal < 255 && ordinal < enumValues.length) {
                ret[i] = enumValues[ordinal];
            }
        }
        return ret;
    }
    
    public static void writeVecApproximate(FriendlyByteBuf buf, Vec3 vec) {
        buf.writeInt((int) (vec.x * 8.0));
        buf.writeInt((int) (vec.y * 8.0));
        buf.writeInt((int) (vec.z * 8.0));
    }
    
    public static Vec3 readVecApproximate(FriendlyByteBuf buf) {
        return new Vec3(
                buf.readInt() / 8.0, 
                buf.readInt() / 8.0, 
                buf.readInt() / 8.0);
    }
    
    
    public static <T> void writeOptionally(FriendlyByteBuf buf, @Nullable T obj, Consumer<T> write) {
        buf.writeBoolean(obj != null);
        if (obj != null) {
            write.accept(obj);
        }
    }
    
    public static <T> void writeOptionally(FriendlyByteBuf buf, @Nullable T obj, BiConsumer<T, FriendlyByteBuf> write) {
        buf.writeBoolean(obj != null);
        if (obj != null) {
            write.accept(obj, buf);
        }
    }

    public static <T> void writeOptional(FriendlyByteBuf buf, @Nonnull Optional<T> objOptional, Consumer<T> write) {
        buf.writeBoolean(objOptional.isPresent());
        objOptional.ifPresent(obj -> write.accept(obj));
    }
    
    public static <T> void writeOptional(FriendlyByteBuf buf, @Nonnull Optional<T> objOptional, BiConsumer<T, FriendlyByteBuf> write) {
        buf.writeBoolean(objOptional.isPresent());
        objOptional.ifPresent(obj -> write.accept(obj, buf));
    }

    public static <T> Optional<T> readOptional(FriendlyByteBuf buf, Supplier<T> read) {
        return buf.readBoolean() ? Optional.ofNullable(read.get()) : Optional.empty();
    }
    
    public static <T> Optional<T> readOptional(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> read) {
        return buf.readBoolean() ? Optional.ofNullable(read.apply(buf)) : Optional.empty();
    }
    
    public static void writeOptionalInt(FriendlyByteBuf buf, OptionalInt optional, boolean varInt) {
        buf.writeBoolean(optional.isPresent());
        optional.ifPresent(value -> {
            if (varInt) {
                buf.writeVarInt(value);
            }
            else {
                buf.writeInt(value);
            }
        });
    }
    
    public static OptionalInt readOptionalInt(FriendlyByteBuf buf, boolean varInt) {
        if (!buf.readBoolean()) {
            return OptionalInt.empty();
        }
        int value = varInt ? buf.readVarInt() : buf.readInt();
        return OptionalInt.of(value);
    }
    
    
    public static <T> int writeCollection(FriendlyByteBuf buf, Collection<T> collection, Consumer<T> writeElement, 
            boolean removeWrittenFromCollection) {
        int i = 0;
        int initialWriterIndex = buf.writerIndex();
        buf.writeInt(0);
        
        int lastWriterIndex = initialWriterIndex;
        int maxElemSize = 0;
        Iterator<T> iter = collection.iterator();
        while (iter.hasNext()) {
            if (buf.capacity() < maxElemSize) break;
            T element = iter.next();
            writeElement.accept(element);
            i++;
            if (removeWrittenFromCollection) {
                iter.remove();
            }
            int writerIndex = buf.writerIndex();
            maxElemSize = Math.max(maxElemSize, writerIndex - lastWriterIndex);
            lastWriterIndex = writerIndex;
        }
                
        buf.setInt(initialWriterIndex, i);
        return i;
    }
    
    public static <T, C extends Collection<T>> C readCollection(Supplier<C> createCollection, FriendlyByteBuf buf, Supplier<T> readElement) {
        C collection = createCollection.get();
        int size = buf.readInt();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                collection.add(readElement.get());
            }
        }
        return collection;
    }
    
    public static <T> List<T> readCollection(FriendlyByteBuf buf, Supplier<T> readElement) {
        return readCollection(ArrayList::new, buf, readElement);
    }
    
    public static <T> int writeCollection(FriendlyByteBuf buf, Collection<T> collection, BiConsumer<T, FriendlyByteBuf> writeElement, 
            boolean removeWrittenFromCollection) {
        int i = 0;
        int initialWriterIndex = buf.writerIndex();
        buf.writeInt(0);
        
        int lastWriterIndex = initialWriterIndex;
        int maxElemSize = 0;
        Iterator<T> iter = collection.iterator();
        while (iter.hasNext()) {
            if (buf.capacity() < maxElemSize) break;
            T element = iter.next();
            writeElement.accept(element, buf);
            i++;
            if (removeWrittenFromCollection) {
                iter.remove();
            }
            int writerIndex = buf.writerIndex();
            maxElemSize = Math.max(maxElemSize, writerIndex - lastWriterIndex);
            lastWriterIndex = writerIndex;
        }
                
        buf.setInt(initialWriterIndex, i);
        return i;
    }
    
    public static <T, C extends Collection<T>> C readCollection(Supplier<C> createCollection, FriendlyByteBuf buf, Function<FriendlyByteBuf, T> readElement) {
        C collection = createCollection.get();
        int size = buf.readInt();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                collection.add(readElement.apply(buf));
            }
        }
        return collection;
    }
    
    public static <T> List<T> readCollection(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> readElement) {
        return readCollection(ArrayList::new, buf, readElement);
    }
    
    
    public static void writePowerType(FriendlyByteBuf buf, IPowerType<?, ?> powerType, PowerClassification powerClassification) {
        switch (powerClassification) {
        case STAND:
            StandType<?> standType = (StandType<?>) powerType;
            buf.writeRegistryId(standType.getRegistry(), standType);
            break;
        case NON_STAND:
            NonStandPowerType<?> nonStandType = (NonStandPowerType<?>) powerType;
            buf.writeRegistryId(nonStandType.getRegistry(), nonStandType);
            break;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static IPowerType<?, ?> readPowerType(FriendlyByteBuf buf, PowerClassification powerClassification) {
        switch (powerClassification) {
        case STAND:
            return buf.readRegistryIdSafe(StandType.class);
        case NON_STAND:
            return buf.readRegistryIdSafe(NonStandPowerType.class);
        default:
            throw new IllegalArgumentException();
        }
    }
    
    
    public static void writeEntity(FriendlyByteBuf buf, @Nullable Entity entity) {
        buf.writeBoolean(entity != null);
        if (entity != null) {
            buf.writeInt(entity.getId());
        }
    }
    
    @Nullable
    public static Entity readEntity(FriendlyByteBuf buf, Level world) {
        boolean hasEntity = buf.readBoolean();
        if (hasEntity) {
            int entityId = buf.readInt();
            return world.getEntity(entityId);
        }
        return null;
    }
}
