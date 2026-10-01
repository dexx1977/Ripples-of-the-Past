package com.github.standobyte.jojo.util.mc;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.item.GlovesItem;
import com.github.standobyte.jojo.mrpresident.CocoJumboTurtleEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.LotsOfBlocksBrokenPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SpawnParticlePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrResetDeathTimePacket;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.Advancement;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.scores.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.registries.IForgeRegistry;
import com.github.standobyte.jojo.init.power.RegistryEntry;
import net.minecraftforge.registries.RegistryManager;

public class MCUtil {
    public static final MutableComponent EMPTY_TEXT = Component.literal("");
    public static final MutableComponent NEW_LINE = Component.literal("\n");
    
    /**
     * Runs a command for the user entity, but with the permissions of the server.
     * 
     * @return The success value of the command, or 0 if an exception occured.
     */
    public static int runCommand(LivingEntity user, String command) {
        if (user.level.isClientSide()) {
            throw new IllegalLogicalSideException("Tried to run a command on client side!");
        }
        MinecraftServer server = ((ServerLevel) user.level).getServer();
        CommandSourceStack src = user.createCommandSourceStack()
                .withMaximumPermission(4)
                .withSuppressedOutput();
        return server.getCommands().performPrefixedCommand(src, command);
    }
    
    // NBT helper functions
    private static final ImmutableMap<Class<? extends Tag>, Integer> NBT_ID = new ImmutableMap.Builder<Class<? extends Tag>, Integer>()
            .put(EndTag.class, 0)           .put(ByteTag.class, 1)      .put(ShortTag.class, 2)         .put(IntTag.class, 3)
            .put(LongTag.class, 4)          .put(FloatTag.class, 5)     .put(DoubleTag.class, 6)        .put(ByteArrayTag.class, 7)
            .put(StringTag.class, 8)        .put(ListTag.class, 9)      .put(CompoundTag.class, 10)     .put(IntArrayTag.class, 11)
            .put(LongArrayTag.class, 12)
            .build();
    
    /** 1.20.1 has no harvest level, the requirement lives in the tool tags. */
    public static int harvestLevel(net.minecraft.world.level.block.state.BlockState state) {
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return 3;
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) return 2;
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) return 1;
        return 0;
    }

    public static int getNbtId(Class<? extends Tag> clazz) {
        return NBT_ID.getOrDefault(clazz, -1);
    }
    
    public static <T extends Tag> Optional<T> getNbtElement(CompoundTag nbt, String key, Class<T> clazz) {
        int id = getNbtId(clazz);
        if (nbt.contains(key, id)) {
            try {
                return Optional.of((T) nbt.get(key));
            }
            catch (ClassCastException e) {
                TagType<?> nbtType = TagTypes.getType(id);
                CrashReport crashreport = CrashReport.forThrowable(e, "Reading NBT data");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Corrupt NBT tag", 1);
                crashreportcategory.setDetail("Tag type found", () -> {
                    return nbt.get(key).getType().getName();
                });
                crashreportcategory.setDetail("Tag type expected", nbtType::getName);
                crashreportcategory.setDetail("Tag name", key);
                throw new ReportedException(crashreport);
            }
        }
        return Optional.empty();
    }
    
    public static CompoundTag replaceNbtValues(CompoundTag original, CompoundTag replacedEntries, CompoundTag replacingEntries) {
        int compoundId = getNbtId(CompoundTag.class);
        for (String key : replacedEntries.getAllKeys()) {
            if (replacedEntries.contains(key) && original.contains(key) && replacedEntries.contains(key)) {
                Tag originalValue = original.get(key);
                Tag replacedValue = replacedEntries.get(key);
                Tag replacingValue = replacingEntries.get(key);
                if (originalValue.getId() == compoundId) {
                    if (replacedValue.getId() == compoundId && replacingValue.getId() == compoundId) {
                        replaceNbtValues((CompoundTag) originalValue, (CompoundTag) replacedValue, (CompoundTag) replacingValue);
                    }
                }
                else if (originalValue.equals(replacedValue)) {
                    original.put(key, replacingValue.copy());
                }
            }
        }
        return original;
    }
    
    public static <T extends Enum<T>> void nbtPutEnum(CompoundTag nbt, String key, T enumVal) {
        nbt.putInt(key, enumVal.ordinal());
    }
    
    @Nullable
    public static <T extends Enum<T>> T nbtGetEnum(CompoundTag nbt, String key, Class<T> enumClass) {
        if (!nbt.contains(key, getNbtId(IntTag.class))) {
            return null;
        }
        
        int ordinal = nbt.getInt(key);
        T[] values = enumClass.getEnumConstants();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }
    
    public static <T extends RegistryEntry<T>> void nbtPutRegistryEntry(CompoundTag nbt, String key, T entry) {
        nbt.put(key, StringTag.valueOf(entry.getRegistryName().toString()));
    }
    
    public static <T> Optional<T> nbtGetRegistryEntry(CompoundTag nbt, String key, IForgeRegistry<T> registry) {
        if (nbt.contains(key, getNbtId(StringTag.class))) {
            String idString = nbt.getString(key);
            return registryEntryFromId(idString, registry);
        }
        
        return Optional.empty();
    }
    
    public static Optional<CompoundTag> nbtGetCompoundOptional(CompoundTag nbt, String key) {
        if (nbt.contains(key, getNbtId(CompoundTag.class))) {
            return Optional.of(nbt.getCompound(key));
        }
        return Optional.empty();
    }
    
    public static CompoundTag nbtGetOrCreateCompound(CompoundTag nbt, String key) {
        if (nbt.contains(key, getNbtId(CompoundTag.class))) {
            return nbt.getCompound(key);
        }
        CompoundTag element = new CompoundTag();
        nbt.put(key, element);
        return element;
    }
    
    public static Optional<ListTag> nbtGetList(CompoundTag nbt, String key, Class<? extends Tag> nbtClass) {
        if (nbt.contains(key, getNbtId(ListTag.class))) {
            return Optional.of(nbt.getList(key, getNbtId(nbtClass)));
        }
        return Optional.empty();
    }
    
    
    
    public static <T> Optional<T> registryEntryFromId(String idString, IForgeRegistry<T> registry) {
        if (!idString.isEmpty()) {
            ResourceLocation id = new ResourceLocation(idString);
            if (registry.containsKey(id)) {
                return Optional.of(registry.getValue(id));
            }
        }
        
        return Optional.empty();
    }
    
    public static void nbtPutVec3d(CompoundTag nbt, String key, Vec3 vec) {
        if (vec != null) {
            ListTag list = new ListTag();
            list.add(DoubleTag.valueOf(vec.x));
            list.add(DoubleTag.valueOf(vec.y));
            list.add(DoubleTag.valueOf(vec.z));
            nbt.put(key, list);
        }
    }
    
    public static void nbtPutOptionalIntArr(CompoundTag nbt, String key, OptionalInt[] array, int emptyVal) {
        int[] value = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            value[i] = array[i].orElse(emptyVal);
        }
        nbt.putIntArray(key, value);
    }
    
    public static OptionalInt[] nbtGetOptionalIntArr(CompoundTag nbt, String key, int emptyVal) {
        int[] value = nbt.getIntArray(key);
        OptionalInt[] array = new OptionalInt[value.length];
        for (int i = 0; i < array.length; i++) {
            int num = value[i];
            array[i] = num != emptyVal ? OptionalInt.of(num) : OptionalInt.empty();
        }
        return array;
    }
    
    public static <T extends Enum<T>> void nbtPutEnumArray(CompoundTag nbt, String key, T[] array) {
        nbt.putIntArray(key, GeneralUtil.toOrdinals(array));
    }
    
    public static <T extends Enum<T>> T[] nbtGetEnumArray(CompoundTag nbt, String key, Class<T> enumClass) {
        int[] nbtArray = nbt.getIntArray(key);
        return GeneralUtil.fromOrdinals(nbtArray, enumClass);
    }
    
    @Nullable
    public static Vec3 nbtGetVec3d(CompoundTag nbt, String key) {
        return getNbtElement(nbt, key, ListTag.class).map(list -> {
            if (list.size() == 3) {
                double[] nums = new double[3];
                for (int i = 0; i < 3; i++) {
                    Tag nbtElem = list.get(i);
                    if (nbtElem.getId() == 6) {
                        nums[i] = ((DoubleTag) nbtElem).getAsDouble();
                    }
                    else {
                        return null;
                    }
                }
                return new Vec3(nums[0], nums[1], nums[2]);
            }
            
            return null;
        }).orElse(null);
    }
    
    public static CompoundTag getOrCreateCompound(CompoundTag mainNbt, String key) {
        return nbtGetCompoundOptional(mainNbt, key).orElseGet(() -> {
            CompoundTag nbt = new CompoundTag();
            mainNbt.put(key, nbt);
            return nbt;
        });
    }
    
    //
    
    public static class ResLocJson implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
        public static final ResLocJson SERIALIZATION = new ResLocJson();
        
        private ResLocJson() {}

        @Override
        public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new ResourceLocation(json.getAsString());
        }

        @Override
        public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
    
    
    public static boolean isLocalServer(MinecraftServer server, Entity serverPlayer) {
        if (server.isDedicatedServer() || !(serverPlayer instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer player = (ServerPlayer) serverPlayer;
        Player clientPlayer = ClientUtil.getClientPlayer();
        return clientPlayer != null && player.getUUID().equals(clientPlayer.getUUID());
    }
    
    
    
    public static Collection<BlockPos> explosionBlocks(BlockPos center, float radius, Level world) {
        Set<BlockPos> set = new HashSet<>();
        for(int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (j / 15.0F * 2.0F - 1.0F);
                        double d1 = (k / 15.0F * 2.0F - 1.0F);
                        double d2 = (l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = radius * (0.7F + world.random.nextFloat() * 0.6F);
                        double d4 = center.getX();
                        double d6 = center.getY();
                        double d8 = center.getZ();

                        for (; f > 0.0F; f -= 0.225F) {
                            BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                            BlockState blockstate = world.getBlockState(blockpos);
                            FluidState fluidstate = world.getFluidState(blockpos);
                            Optional<Float> optional = blockstate.isAir() && fluidstate.isEmpty()
                                    ? Optional.empty()
                                    : Optional.of(Math.max(blockstate.getBlock().getExplosionResistance(), fluidstate.getExplosionResistance()));
                            if (optional.isPresent()) {
                                f -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F) {
                                set.add(blockpos);
                            }

                            d4 += d0 * (double)0.3F;
                            d6 += d1 * (double)0.3F;
                            d8 += d2 * (double)0.3F;
                        }
                    }
                }
            }
        }
        
        return set;
    }
    
    public static void iterateOverBlocks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Consumer<BlockPos> action) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    action.accept(pos);
                }
            }
        }
    }
    
    
    
    public static Set<ServerPlayer> getTrackingPlayers(Entity entity) {
        if (entity.level.isClientSide()) {
            throw new IllegalStateException();
        }
        
        ChunkMap chunkMap = ((ServerLevel) entity.level).getChunkSource().chunkMap;
        Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = chunkMap.entityMap;
        ChunkMap.TrackedEntity tracker = entityMap.get(entity.getId());
        // 1.20.1 tracks player connections, the old set held the players themselves
        return tracker != null 
                ? tracker.seenBy.stream().map(net.minecraft.server.network.ServerPlayerConnection::getPlayer).collect(java.util.stream.Collectors.toSet())
                : Collections.emptySet();
    }
    
    
    
    public static GameType getGameMode(Player player) {
        if (!player.level.isClientSide()) {
            return ((ServerPlayer) player).gameMode.getGameModeForPlayer();
        }
        else {
            return ClientUtil.getPlayerGameMode(player);
        }
    }
    
    
    
    @Nonnull
    public static ItemStack findInInventory(Container inventory, Predicate<ItemStack> itemMatches) {
        int size = inventory.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inventory.getItem(i);
            if (itemMatches.test(item)) {
                return item;
            }
        }
        
        return ItemStack.EMPTY;
    }

    public static boolean dispenseOnNearbyEntity(BlockSource blockSource, ItemStack itemStack, Predicate<LivingEntity> action, boolean shrinkStack) {
        BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
        List<LivingEntity> entities = blockSource.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS);
        for (LivingEntity entity : entities) {
            if (action.test(entity)) {
                if (shrinkStack) {
                    itemStack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }
    
    public static void giveItemTo(LivingEntity entity, ItemStack item, boolean drop) {
        if (!entity.level.isClientSide() && !item.isEmpty()) {
            if (entity instanceof Player) {
                drop = !(((Player) entity).inventory.add(item) && item.isEmpty());
            }
            if (drop) {
                entity.level.addFreshEntity(dropAt(entity, item));
            }
        }
    }
    
    public static ItemEntity dropAt(LivingEntity entity, ItemStack item) {
        if (item.isEmpty()) {
            return null;
        }
        else {
            ItemEntity itemEntity = new ItemEntity(entity.level, entity.getX(), entity.getEyeY() - 0.3, entity.getZ(), item);
            itemEntity.setNoPickUpDelay();
            itemEntity.setThrower(entity.getUUID());
            return itemEntity;
        }
    }
    
    
    
    // i ain't using access transformers for this, this is ridiculous
    public static boolean itemAllowedIn(Item item, CreativeModeTab creativeTab) {
        // 1.20.1 has no item to tab mapping; the tab lists its own items
        return creativeTab == net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.get(net.minecraft.world.item.CreativeModeTabs.SEARCH)
                || creativeTab.getDisplayItems().stream().anyMatch(stack -> stack.is(item));
    }
    

    
    public static Optional<Entity> cloneEntity(Entity entity) {
        CompoundTag entityNbt = entity.serializeNBT();
        return EntityType.create(entityNbt, entity.level);
    }
    
    
    
    @Deprecated
    public static Vec3 collide(Entity entity, Vec3 offsetVec) {
        return CollisionUtil.collide(entity, offsetVec);
    }
    
    @Deprecated
    public static Vec3 collide(Entity entity, AABB collisionBox, Vec3 offsetVec) {
        return CollisionUtil.collide(entity, collisionBox, offsetVec);
    }

    public static void rotateTowards(Entity entity, Vec3 targetPos, float maxAngle) {
        Vec3 targetVec = targetPos.subtract(entity.getEyePosition(1.0F));

        float yRot = MathUtil.yRotDegFromVec(targetVec);
        float xRot = MathUtil.xRotDegFromVec(targetVec);

        yRot = entity.yRot + Mth.clamp(Mth.degreesDifference(entity.yRot, yRot), -maxAngle, maxAngle);
        xRot = entity.xRot + Mth.clamp(Mth.degreesDifference(entity.xRot, xRot), -maxAngle, maxAngle);

        entity.yRot = yRot % 360.0F;
        entity.xRot = xRot % 360.0F;
        entity.setYHeadRot(yRot);
    }
    
    public static <T extends Entity> List<T> entitiesAround(Class<? extends T> clazz, Entity centerEntity, double radius, boolean includeSelf, @Nullable Predicate<? super T> filter) {
        Vec3 centerPos = centerEntity.getBoundingBox().getCenter();
        AABB aabb = new AABB(centerPos.subtract(radius, radius, radius), centerPos.add(radius, radius, radius));
        return centerEntity.level.getEntitiesOfClass((Class<T>) clazz, aabb, entity -> (includeSelf || entity != centerEntity) && (filter == null || filter.test(entity)));
    }

    public static Iterable<Entity> getAllEntities(Level world) {
        return world.isClientSide() ? ((ClientLevel) world).entitiesForRendering() : ((ServerLevel) world).getAllEntities();
    }
    
    public static Vec3 getEntityPosition(Entity entity, float partialTick) {
        return partialTick == 1.0F ? entity.position() : entity.getPosition(partialTick);
    }
    
    
    public static boolean hasIndirectPassenger(Entity vehicle, Entity passenger) {
        for (Entity entity : vehicle.getPassengers()) {
            if (entity.equals(passenger)) {
                return true;
            }
            
            if (entity.hasIndirectPassenger(passenger)) {
                return true;
            }
        }
        
        return false;
    }
    
    
    public static void trySpawnMob(ServerLevelAccessor world, EntityType<?> type, MobSpawnType spawnReason, Random random) {
//        boolean spawned = false;
//
//        for (int tryNum = 0; !spawned && tryNum < 4; ++tryNum) {
//            BlockPos blockPos = WorldEntitySpawner.getTopNonCollidingPos(world, type, l, i1);
//            if (type.canSummon() && WorldEntitySpawner.isSpawnPositionOk(EntitySpawnPlacementRegistry.getPlacementType(type), world, blockPos, type)) {
//                float width = type.getWidth();
//                double x = MathHelper.clamp((double)l, (double)i + (double)width, (double)i + 16.0D - (double)width);
//                double z = MathHelper.clamp((double)i1, (double)j + (double)width, (double)j + 16.0D - (double)width);
//                if (!world.noCollision(type.getAABB(x, (double)blockPos.getY(), z)) || !EntitySpawnPlacementRegistry.checkSpawnRules(
//                        type, world, spawnReason, new BlockPos(x, (double)blockPos.getY(), z), world.getRandom())) {
//                    continue;
//                }
//
//                Entity entity;
//                try {
//                    entity = type.create(world.level());
//                } catch (Exception exception) {
//                    JojoMod.getLogger().warn("Failed to create mob", (Throwable)exception);
//                    continue;
//                }
//
//                entity.moveTo(x, (double)blockPos.getY(), z, random.nextFloat() * 360.0F, 0.0F);
//                if (entity instanceof MobEntity) {
//                    MobEntity mobentity = (MobEntity)entity;
//                    if (ForgeHooks.canEntitySpawn(mobentity, world, x, blockPos.getY(), z, null, spawnReason) == -1) continue;
//                    if (mobentity.checkSpawnRules(world, spawnReason) && mobentity.checkSpawnObstruction(world)) {
//                        entityData = mobentity.finalizeSpawn(world, world.getCurrentDifficultyAt(mobentity.blockPosition()), spawnReason, entityData, (CompoundNBT)null);
//                        world.addFreshEntityWithPassengers(mobentity);
//                        spawned = true;
//                    }
//                }
//            }
//
//            l += random.nextInt(5) - random.nextInt(5);
//
//            for(i1 += random.nextInt(5) - random.nextInt(5); l < i || l >= i + 16 || i1 < j || i1 >= j + 16; i1 = k1 + random.nextInt(5) - random.nextInt(5)) {
//                l = j1 + random.nextInt(5) - random.nextInt(5);
//            }
//        }
    }
    
    
    
    public static boolean rayTraceTargetEquals(HitResult r1, HitResult r2) {
        if (r1 == null || r2 == null) return r1 == null && r2 == null;
        if (r1.getType() != r2.getType()) return false;
        
        switch (r1.getType()) {
        case MISS:
            return true;
        case BLOCK:
            BlockHitResult br1 = (BlockHitResult) r1;
            BlockHitResult br2 = (BlockHitResult) r2;
            return br1.getBlockPos().equals(br2.getBlockPos()) && br1.getDirection() == br2.getDirection();
        case ENTITY:
            EntityHitResult er1 = (EntityHitResult) r1;
            EntityHitResult er2 = (EntityHitResult) r2;
            return er1.getEntity() == er2.getEntity();
        default:
            throw new IllegalArgumentException("Unknown RayTraceResult type (it's an enum wtf)");
        }
    }
    
    public static double getPickRange(LivingEntity entity) {
        AttributeInstance reachDist = entity.getAttribute(ForgeMod.ENTITY_REACH.get());
        double value = reachDist != null ? reachDist.getValue() : 5;
        if (entity instanceof Player && !((Player) entity).isCreative()) {
            value -= 0.5;
        }
        return value;
    }
    
    
    
    public static AABB scale(AABB aabb, double scale) {
        return scale(aabb, scale, scale, scale);
    }
    
    public static AABB scale(AABB aabb, double scaleX, double scaleY, double scaleZ) {
        Vec3 center = aabb.getCenter();
        double inflX = aabb.getXsize() * scaleX / 2;
        double inflY = aabb.getYsize() * scaleY / 2;
        double inflZ = aabb.getZsize() * scaleZ / 2;
        return new AABB(
                center.x - inflX, center.y - inflY, center.z - inflZ,
                center.x + inflX, center.y + inflY, center.z + inflZ);
    }
    
    public static double getManhattanDist(AABB aabb1, AABB aabb2) {
        double xDist = 0;
        double yDist = 0;
        double zDist = 0;
        
        if      (aabb1.maxX < aabb2.minX) xDist = aabb2.minX - aabb1.maxX;
        else if (aabb2.maxX < aabb1.minX) xDist = aabb1.minX - aabb2.maxX;
        
        if      (aabb1.maxY < aabb2.minY) yDist = aabb2.minY - aabb1.maxY;
        else if (aabb2.maxY < aabb1.minY) yDist = aabb1.minY - aabb2.maxY;
        
        if      (aabb1.maxZ < aabb2.minZ) zDist = aabb2.minZ - aabb1.maxZ;
        else if (aabb2.maxZ < aabb1.minZ) zDist = aabb1.minZ - aabb2.maxZ;
        
        return xDist + yDist + zDist;
    }
    
    
    
    public static boolean canHarm(LivingEntity attacker, LivingEntity target) {
        if (attacker.is(target)) {
            return false;
        }
        if (!attacker.canAttack(target)) {
            return false;
        }
        
        Team team1 = attacker.getTeam();
        Team team2 = target.getTeam();
        if (team1 != null && team1.isAlliedTo(team2) && !team1.isAllowFriendlyFire()) {
            return false;
        }
        
        return true;
    }
    
    
    
    public static boolean isControlledThisSide(Entity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).isLocalPlayer();
        }
        return !entity.level.isClientSide() || entity.isControlledByLocalInstance();
    }
    
    
    
    public static boolean canHarm(LivingEntity attacker, Entity target) {
        if (attacker == target) return false;
        Team team = attacker.getTeam();
        Team team1 = target.getTeam();
        if (team != null && team.isAlliedTo(team1) && !team.isAllowFriendlyFire()) {
            return false;
        }
        if (attacker instanceof StandEntity) {
            return ((StandEntity) attacker).canHarm(target);
        }
        return target instanceof LivingEntity && attacker.canAttack((LivingEntity) target);
    }
    
    
    /**
     *  Limits the amount of particles and break sounds that the blocks produce, sending it all in one packet
     */
    public static int destroyBlocksInBulk(Collection<BlockPos> blocks, ServerLevel world, @Nullable LivingEntity entity, boolean dropItems) {
        if (!world.isClientSide() && world.isDebug()) {
            return -1;
        }
        
        Iterator<BlockPos> iter = blocks.iterator();
        while (iter.hasNext()) {
            BlockPos blockPos = iter.next();
            BlockState blockState = world.getBlockState(blockPos);
            if (world.isOutsideBuildHeight(blockPos) || blockState.isAir()
                    || !JojoModUtil.canEntityDestroy(world, blockPos, blockState, entity)) {
                iter.remove();
            }
        }
        if (blocks.isEmpty()) return 0;
        int blocksBroken = 0;
        
        LotsOfBlocksBrokenPacket packet = new LotsOfBlocksBrokenPacket();
        int minX = 30000001;
        int minY = 999;
        int minZ = 30000001;
        int maxX = -30000001;
        int maxY = -999;
        int maxZ = -30000001;
        
        ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositions = new ObjectArrayList<>();
        
        for (BlockPos blockPos : blocks) {
            FluidState fluidState = world.getFluidState(blockPos);
            BlockState newState = fluidState.createLegacyBlock();
            
            BlockState oldState = world.getBlockState(blockPos);

            if (!(oldState.getBlock() instanceof BaseFireBlock)) {
                minX = Math.min(minX, blockPos.getX());
                minY = Math.min(minY, blockPos.getY());
                minZ = Math.min(minZ, blockPos.getZ());
                maxX = Math.max(maxX, blockPos.getX());
                maxY = Math.max(maxY, blockPos.getY());
                maxZ = Math.max(maxZ, blockPos.getZ());
                packet.addBlock(blockPos, oldState);
            }
            if (dropItems) {
                BlockEntity tileentity = oldState.hasBlockEntity() ? world.getBlockEntity(blockPos) : null;

                Block.getDrops(oldState, world, blockPos, tileentity, entity, ItemStack.EMPTY).forEach(itemStack -> {
                    CustomExplosion.addBlockDrops(dropPositions, itemStack, blockPos);
                });
            }
            else {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, oldState, 
                        Optional.ofNullable(world.getBlockEntity(blockPos)), 
                        Collections.emptyList());
            }
            
            if (world.setBlock(blockPos, newState, 3)) {
                ++blocksBroken;
            }
        }
        
        for (Pair<ItemStack, BlockPos> pair : dropPositions) {
            Block.popResource(world, pair.getSecond(), pair.getFirst());
        }
        
        packet.sendToPlayers(world, minX, minY, minZ, maxX, maxY, maxZ);
        
        return blocksBroken;
    }
    
    public static void blockCatchFire(Level world, BlockPos blockPos, BlockState blockState, @Nullable Direction face, @Nullable LivingEntity igniter) {
        // BlockState#catchFire is gone; the fire block is placed directly
        if (blockState.isAir() || net.minecraft.world.level.block.BaseFireBlock.canBePlacedAt(world, blockPos, face != null ? face : Direction.UP)) {
            world.setBlockAndUpdate(blockPos, net.minecraft.world.level.block.BaseFireBlock.getState(world, blockPos));
        }
        if (blockState.getBlock() instanceof TntBlock) {
            CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, blockState, 
                    Optional.ofNullable(world.getBlockEntity(blockPos)), Collections.emptyList());
            world.removeBlock(blockPos, false);
        }
    }
    
    public static boolean destroyBlock(Level world, BlockPos blockPos, boolean dropBlock, @Nullable Entity entity) {
        BlockState oldState = dropBlock ? null /*no need to call it in this case*/ : world.getBlockState(blockPos);
        boolean res = world.destroyBlock(blockPos, dropBlock, entity);
        if (!dropBlock) {
            CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, oldState, 
                    Optional.ofNullable(world.getBlockEntity(blockPos)), 
                    Collections.emptyList());
        }
        return res;
    }
    
    public static boolean dropBrokenBlock(LivingEntity entity) {
        return !(entity instanceof Player && ((Player) entity).abilities.instabuild);
    }
    
    
    
    
    public static void playSound(Level world, @Nullable Player clientHandled, BlockPos blockPos, 
            SoundEvent sound, SoundSource category, float volume, float pitch, Predicate<Player> condition) {
        playSound(world, clientHandled, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, 
                sound, category, volume, pitch, condition);
    }

    public static void playSound(Level world, @Nullable Player clientHandled, double x, double y, double z, 
            SoundEvent sound, SoundSource category, float volume, float pitch, Predicate<Player> condition) {
        if (!world.isClientSide()) {
            net.minecraft.core.Holder<SoundEvent> soundHolder = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
            // 1.20.1's onPlaySoundAtEntity requires a non-null entity; the mod always
            // played positional sounds at (x, y, z), so use the position variant.
            PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtPosition(world, x, y, z, soundHolder, category, volume, pitch);
            if (event.isCanceled() || event.getSound() == null) return;
            soundHolder = event.getSound();
            category = event.getSource();
            volume = event.getOriginalVolume();
            pitch = event.getOriginalPitch();
            NetworkUtil.broadcastWithCondition(((ServerLevel) world).getServer().getPlayerList().getPlayers(), clientHandled, 
                    x, y, z, volume > 1.0F ? (double)(16.0F * volume) : 16.0D, world, 
                            new ClientboundSoundPacket(soundHolder, category, x, y, z, volume, pitch, world.getRandom().nextLong()), condition);
        }
        else if (clientHandled != null && condition.test(clientHandled)) {
            world.playSound(clientHandled, x, y, z, sound, category, volume, pitch);
        }
    }

    public static void playEitherSound(Level world, @Nullable Player clientHandled, double x, double y, double z, 
            Predicate<Player> predicate, SoundEvent soundTrue, SoundEvent soundFalse, SoundSource category, float volume, float pitch) {
        if (soundTrue != null) playSound(world, clientHandled, x, y, z, soundTrue, category, volume, pitch, predicate);
        if (soundFalse != null) playSound(world, clientHandled, x, y, z, soundFalse, category, volume, pitch, predicate.negate());
    }

    public static void playSound(Level world, @Nullable Player clientHandled, Entity entity, 
            SoundEvent sound, SoundSource category, float volume, float pitch, Predicate<Player> condition) {
        if (!world.isClientSide()) {
            net.minecraft.core.Holder<SoundEvent> soundHolder = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
            PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtEntity(entity, soundHolder, category, volume, pitch);
            if (event.isCanceled() || event.getSound() == null) return;
            soundHolder = event.getSound();
            category = event.getSource();
            volume = event.getOriginalVolume();
            pitch = event.getOriginalPitch();
            NetworkUtil.broadcastWithCondition(((ServerLevel) world).getServer().getPlayerList().getPlayers(), clientHandled, 
                    entity.getX(), entity.getY(), entity.getZ(), volume > 1.0F ? (double)(16.0F * volume) : 16.0D, world, 
                            new ClientboundSoundEntityPacket(soundHolder, category, entity, volume, pitch, world.getRandom().nextLong()), condition);
        }
        else if (clientHandled != null && condition.test(clientHandled)) {
            world.playSound(clientHandled, entity, sound, category, volume, pitch);
        }
    }
    
    
    
    public static boolean isItemWeapon(ItemStack itemStack) {
        if (itemStack.isEmpty() || itemStack.getItem() instanceof GlovesItem) {
            return false;
        }
        
        if (itemStack.getItem() instanceof TieredItem) {
            return true;
        }
        
        // other items dealing extra damage (trident, knife, potentially unique modded weapons)
        Collection<AttributeModifier> damageModifiers = itemStack
                .getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, itemStack).get(Attributes.ATTACK_DAMAGE);
        if (damageModifiers != null) {
            return damageModifiers.stream().anyMatch(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION && modifier.getAmount() > 0);
        }
        
        // TODO compatibility with Tinkers Construct
        
        return false;
    }
    
    public static void multipliedAttrModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier, float mult) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifier);
            if (mult != 0) {
                attributeInstance.addTransientModifier(new AttributeModifier(modifier.getId(), 
                        modifier.getName() + " " + mult, modifier.getAmount() * mult, modifier.getOperation()));
            }
        }
    }
    
    public static double calcValueWithoutModifiers(AttributeInstance entityAttribute, UUID... modifierIds) {
        return calcValueWithoutModifiers(entityAttribute, Arrays.stream(modifierIds));
    }
    
    public static double calcValueWithoutModifiers(AttributeInstance entityAttribute, Stream<UUID> modifierIds) {
        Collection<UUID> exclude = modifierIds.collect(Collectors.toCollection(HashSet::new));
        if (exclude.isEmpty()) return entityAttribute.getValue();
        
        double valueBase = entityAttribute.getBaseValue();
        
        for (AttributeModifier modifier : entityAttribute.getModifiers(AttributeModifier.Operation.ADDITION)) {
            if (!exclude.contains(modifier.getId())) valueBase += modifier.getAmount();
        }
        
        double value = valueBase;
        for (AttributeModifier modifier : entityAttribute.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
            if (!exclude.contains(modifier.getId())) value += valueBase * modifier.getAmount();
        }
        
        for (AttributeModifier modifier : entityAttribute.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            if (!exclude.contains(modifier.getId())) value *= 1.0D + modifier.getAmount();
        }

        return entityAttribute.getAttribute().sanitizeValue(value);
    }
    
    public static void applyAttributeModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifier);
            attributeInstance.addTransientModifier(modifier);
        }
    }
    
    public static void removeAttributeModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null && instance.hasModifier(modifier)) {
            instance.removeModifier(modifier);
        }
    }
    
    public static void applyAttributeModifierMultiplied(LivingEntity entity, Attribute attribute, AttributeModifier modifier, double multiplier) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifier);
            attributeInstance.addTransientModifier(new AttributeModifier(modifier.getId(), 
                    modifier.getName(), modifier.getAmount() * multiplier, modifier.getOperation()));
        }
    }
    
    public static double getValueIfPresent(LivingEntity entity, Attribute attribute, double or) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : or;
    }
    
    
    
    
    public static boolean removeEffectInstance(LivingEntity entity, MobEffectInstance effectInstance) {
        if (entity.getActiveEffectsMap().get(effectInstance.getEffect()) == effectInstance) {
            return entity.removeEffect(effectInstance.getEffect());
        }
        return false;
    }
    
    public static int getEffectLevel(LivingEntity entity, MobEffect effect) {
        MobEffectInstance effInstance = entity.getEffect(effect);
        return effInstance != null ? effInstance.getAmplifier() : -1;
    }

    public static boolean reduceEffect(LivingEntity entity, MobEffect effect, int reduceDuration, int reduceAmplifier) {
        MobEffectInstance mainEffectInstance = entity.getEffect(effect);
        if (mainEffectInstance == null) {
            return false;
        }
        
        MobEffectInstance effectInstance = mainEffectInstance;
        MobEffectInstance prevInstance = null;
        
        while (effectInstance != null) {
            if (effectInstance.getAmplifier() < reduceAmplifier || effectInstance.getDuration() <= reduceDuration) {
                if (effectInstance == mainEffectInstance) {
                    return entity.removeEffect(effect);
                }
                else {
                    prevInstance.hiddenEffect = null;
                    break;
                }
            }

            effectInstance.duration -= reduceDuration;
            if (reduceAmplifier > 0) {
                effectInstance.amplifier -= reduceAmplifier;
            }
            
            prevInstance = effectInstance;
            effectInstance = effectInstance.hiddenEffect;
        }
        
        CommonReflection.onEffectUpdated(entity, mainEffectInstance, true);
        return true;
    }
    
    
    
    public static <T extends ParticleOptions> int sendParticles(ServerLevel world, T particleType, 
            double x, double y, double z, int count, float xDist, float yDist, float zDist, float maxSpeed, 
            SpawnParticlePacket.SpecialContext context) {
        SpawnParticlePacket packet = new SpawnParticlePacket(particleType, false, x, y, z, xDist, yDist, zDist, maxSpeed, count, context);
        int i = 0;

        for (ServerPlayer player : world.players()) {
            if (sendParticles(world, player, false, x, y, z, packet)) {
                ++i;
            }
        }

        return i;
    }

    private static boolean sendParticles(ServerLevel world, ServerPlayer player, boolean force, double x, double y, double z, Object packet) {
        if (player.level() != world) {
            return false;
        } else {
            BlockPos blockpos = player.blockPosition();
            if (net.minecraft.world.phys.Vec3.atCenterOf(blockpos).closerThan(new Vec3(x, y, z), force ? 512.0D : 32.0D)) {
                PacketManager.sendToClient(packet, player);
                return true;
            } else {
                return false;
            }
        }
    }
    
    // i sure love copy-pasting private methods
    public static void spawnItemParticles(LivingEntity entity, ItemStack item, int particlesCount) {
        net.minecraft.util.RandomSource random = entity.getRandom();
        for (int i = 0; i < particlesCount; ++i) {
            Vec3 motion = new Vec3((random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0);
            motion = motion.xRot(-entity.xRot * ((float) Math.PI / 180F));
            motion = motion.yRot(-entity.yRot * ((float) Math.PI / 180F));
            double d0 = -random.nextFloat() * 0.6 - 0.3;
            Vec3 pos = new Vec3(((random.nextFloat() - 0.5)) * 0.3, d0, 0.6);
            pos = pos.xRot(-entity.xRot * ((float) Math.PI / 180F));
            pos = pos.yRot(-entity.yRot * ((float) Math.PI / 180F));
            pos = pos.add(entity.getX(), entity.getEyeY(), entity.getZ());
            if (entity.level instanceof ServerLevel) { //Forge: Fix MC-2518 spawnParticle is nooped on server, need to use server specific variant
                ((ServerLevel)entity.level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, item), 
                        pos.x, pos.y, pos.z, 1, motion.x, motion.y + 0.05D, motion.z, 0.0D);
            }
            else {
                entity.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, item), 
                        pos.x, pos.y, pos.z, motion.x, motion.y + 0.05D, motion.z);
            }
        }
    }
    
    public static AttributeModifier modifierWithAmount(AttributeModifier modifier, double amount) {
        return new AttributeModifier(modifier.getId(), modifier.getName(), amount, modifier.getOperation());
    }
    

    
    public static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementPath) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(advancementPath);
        if (advancement != null) {
            return player.getAdvancements().getOrStartProgress(advancement).isDone();
        }
        return false;
    }
    
    
    
    public static boolean isHandFree(LivingEntity entity, InteractionHand hand) {
        return areHandsFree(entity, hand);
    }
    
    public static boolean areBothHandsFree(LivingEntity entity) {
        return areHandsFree(entity, InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND);
    }
    
    public static boolean areHandsFree(LivingEntity entity, InteractionHand... hands) {
        if (entity.level.isClientSide() && entity.is(ClientUtil.getClientPlayer()) && ClientUtil.arePlayerHandsBusy()) {
            return false;
        }
        for (InteractionHand hand : hands) {
            if (!itemHandFree(entity.getItemInHand(hand))
                    || hand == InteractionHand.OFF_HAND && entity.getPassengers().stream().anyMatch(passenger -> CocoJumboTurtleEntity.isCarriedTurtle(passenger, entity))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean itemHandFree(ItemStack item) {
        if (item.isEmpty()) {
            return true;
        }
        if (item.getItem() instanceof GlovesItem) {
            return ((GlovesItem) item.getItem()).openFingers();
        }
        return false;
    }
    
    public static HumanoidArm getHandSide(LivingEntity entity, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? entity.getMainArm() : getOppositeSide(entity.getMainArm());
    }
    
    public static InteractionHand getHand(LivingEntity entity, HumanoidArm handSide) {
        return entity.getMainArm() == handSide ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }
    
    public static HumanoidArm getOppositeSide(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }
    
    
    
    public static void loseTarget(Mob attackingMob, LivingEntity target) {
        if (attackingMob.getTarget() == target) {
            attackingMob.setTarget(null);
            attackingMob.targetSelector.getRunningGoals()
            .forEach(goal -> goal.stop());
        }
    }
    
    public static void makeMobNeutralTo(Mob mob, LivingEntity neutralTo) {
        Class<? extends LivingEntity> clazz = neutralTo.getClass();
        UUID userUuid = neutralTo.getUUID();
        Set<WrappedGoal> goals = CommonReflection.getGoalsSet(mob.targetSelector);
        for (WrappedGoal prGoal : goals) {
            Goal goal = prGoal.getGoal();
            if (goal instanceof NearestAttackableTargetGoal) {
                NearestAttackableTargetGoal<?> targetGoal = (NearestAttackableTargetGoal<?>) goal;
                Class<? extends LivingEntity> targetClass = CommonReflection.getTargetClass(targetGoal);
                
                if (targetClass == null || targetClass.isAssignableFrom(clazz)) {
                    TargetingConditions selector = CommonReflection.getTargetConditions(targetGoal);
                    if (selector != null) {
                        Predicate<LivingEntity> oldPredicate = CommonReflection.getTargetSelector(selector);
                        Predicate<LivingEntity> geUserPredicate = target -> !userUuid.equals(target.getUUID());
                        CommonReflection.setTargetConditions(targetGoal, TargetingConditions.forCombat().range(CommonReflection.getTargetDistance(targetGoal)).selector(
                                oldPredicate != null ? oldPredicate.and(geUserPredicate) : geUserPredicate));
                    }
                }
            }
        }
    }
    
    
    
    public static void onLivingResurrect(LivingEntity entity) {
        entity.deathTime = 0;
        if (!entity.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrResetDeathTimePacket(entity.getId()), entity);
            if (entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer) entity;
                if (!player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator()) {
                    player.setExperienceLevels(0);
                    player.setExperiencePoints(0);
                }
            }
        }
    }
    
    
    
    public static boolean isPotionWaterBottle(ThrownPotion entity) {
        ItemStack potionItem = entity.getItem();
        return PotionUtils.getPotion(potionItem) == Potions.WATER && PotionUtils.getMobEffects(potionItem).isEmpty();
    }
    
    public static ItemStack getItemOnServer(ThrowableItemProjectile entity) {
        ItemStack item = CommonReflection.getItemRaw(entity);
        return item.isEmpty() ? new ItemStack(CommonReflection.getDefaultItem(entity)) : item;
    }
    
    
    
    public static void leap(Entity entity, float leapStrength) {
        entity.setOnGround(false);
        entity.hasImpulse = true;
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setJumping(true);
        }
        Vec3 leap = Vec3.directionFromRotation(Math.min(entity.xRot, -30F), entity.yRot).scale(leapStrength);
        entity.setDeltaMovement(leap.x, leap.y * 0.5, leap.z);
    }
    
    
    
    public static String getLanguageCode(MinecraftServer server) {
        return server.isDedicatedServer() ? "en_us" : ClientUtil.getCurrentLanguageCode();
    }
    
    
    public static <V extends RegistryEntry<V>> IForgeRegistry<V> getRegistry(RegistryEntry<?> regEntry) {
        return ((RegistryEntry<V>) regEntry).getRegistry();
    }
    
    
    @Nullable
    public static <T> ResourceKey<T> getRegistryKeyIfPresent(ResourceKey<? extends Registry<T>> parent, ResourceLocation location) {
        String s = (parent.location() + ":" + location).intern();
        return (ResourceKey<T>) CommonReflection.registryKeyValues().get(s);
    }
    
    
    
    public static class EntityEvents { // TODO entity event constants
        public static final byte HURT                           = 2;
        public static final byte VILLAGER_BREEDING              = 12;
        public static final byte VILLAGER_ANGRY                 = 13;
        public static final byte VILLAGER_HAPPY                 = 14;
        public static final byte VILLAGER_PANIC_SWEAT           = 42;
        public static final byte SILVERFISH_SPAWN_PARTICLES     = 20;
        public static final byte PLAYER_PERM_LEVEL_0            = 24;
        public static final byte PLAYER_PERM_LEVEL_1            = 25;
        public static final byte PLAYER_PERM_LEVEL_2            = 26;
        public static final byte PLAYER_PERM_LEVEL_3            = 27;
        public static final byte PLAYER_PERM_LEVEL_4            = 28;
        public static final byte SHIELD_BLOCK_SOUND             = 29;
        public static final byte SHIELD_BREAK_SOUND             = 30;
        public static final byte ARMOR_STAND_HIT                = 32;
        public static final byte HURT_THORNS                    = 33;
        public static final byte HURT_DROWN                     = 36;
        public static final byte HURT_ON_FIRE                   = 37;
        public static final byte HURT_SWEET_BERRY_BUSH          = 44;
        public static final byte BREAK_MAIN_HAND_ITEM           = 47;
        public static final byte BREAK_OFF_HAND_ITEM            = 48;
        public static final byte BREAK_HEAD_ITEM                = 49;
        public static final byte BREAK_CHEST_ITEM               = 50;
        public static final byte BREAK_LEGS_ITEM                = 51;
        public static final byte BREAK_FEET_ITEM                = 52;
        public static final byte HONEY_SLIDE_PARTICLES          = 53;
        public static final byte HONEY_JUMP_PARTICLES           = 54;
        public static final byte SWAP_HAND_ITEMS                = 55;
        /*
         * AnimalEntity
         * AsbtractHorseEntity
         * FoxEntity
         * HoglinEntity
         * OcelotEntity
         * RabbitEntity
         * SheepEntity
         * TameableEntity
         * WolfEntity
         * IronGolemEntity
         * RavagerEntity
         * WitchEntity
         * ZoglinEntity
         * ZombieVillagerEntity
         * DolphinEntity
         * SquidEntity
         * PlayerEntity
         */
    }

    /**
     * 1.20.1 removed IForgeRegistryEntry#getRegistryName: a value no longer knows
     * which registry holds it, so the registry is asked instead. These overloads
     * keep the original call sites readable and are dispatched by static type,
     * exactly like the old interface method was.
     */
    public static ResourceLocation id(EntityType<?> type) {
        return ForgeRegistries.ENTITY_TYPES.getKey(type);
    }

    public static ResourceLocation id(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static ResourceLocation id(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    public static ResourceLocation id(BlockEntityType<?> type) {
        return ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type);
    }

    public static ResourceLocation id(MobEffect effect) {
        return ForgeRegistries.MOB_EFFECTS.getKey(effect);
    }

    public static ResourceLocation id(Fluid fluid) {
        return ForgeRegistries.FLUIDS.getKey(fluid);
    }

    public static ResourceLocation id(ParticleType<?> type) {
        return ForgeRegistries.PARTICLE_TYPES.getKey(type);
    }

    public static ResourceLocation id(SoundEvent sound) {
        return ForgeRegistries.SOUND_EVENTS.getKey(sound);
    }

    public static ResourceLocation id(Enchantment enchantment) {
        return ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
    }

    public static ResourceLocation id(Potion potion) {
        return ForgeRegistries.POTIONS.getKey(potion);
    }

    public static ResourceLocation id(RecipeType<?> type) {
        return ForgeRegistries.RECIPE_TYPES.getKey(type);
    }

    public static ResourceLocation id(RecipeSerializer<?> serializer) {
        return ForgeRegistries.RECIPE_SERIALIZERS.getKey(serializer);
    }

    public static ResourceLocation id(StructurePieceType type) {
        return net.minecraft.core.registries.BuiltInRegistries.STRUCTURE_PIECE.getKey(type);
    }

    public static ResourceLocation id(PaintingVariant variant) {
        return ForgeRegistries.PAINTING_VARIANTS.getKey(variant);
    }

    /**
     * 1.20.1 removed Material, which 1.16.5 used to classify blocks. These helpers
     * keep the same groupings using the block's own properties, which is what the
     * old materials were defined by (each material had exactly one sound type), and
     * vanilla tags where the group matches a tag one to one. The tag contents below
     * were read from the 1.20.1 vanilla data:
     *   snow = {snow, snow_block, powder_snow} - the old Material.SNOW + TOP_SNOW
     *   ice  = {ice, packed_ice, blue_ice, frosted_ice} - the old ICE + ICE_SOLID
     *   replaceable = grass, fern, dead_bush, seagrass, fire, snow, vine, light, ...
     */
    public static boolean isGlassLike(BlockState state) {
        return state.getSoundType() == SoundType.GLASS;
    }

    public static boolean isWoodLike(BlockState state) {
        return state.getSoundType() == SoundType.WOOD;
    }

    public static boolean isMetalLike(BlockState state) {
        return state.getSoundType() == SoundType.METAL;
    }

    public static boolean isStoneLike(BlockState state) {
        return state.getSoundType() == SoundType.STONE;
    }

    public static boolean isSnowOrIce(BlockState state) {
        return state.is(BlockTags.SNOW) || state.is(BlockTags.ICE);
    }

    public static boolean isReplaceablePlant(BlockState state) {
        return state.is(BlockTags.REPLACEABLE);
    }

    /** The old Material.CLOTH_DECORATION / WEB / WOOL / SAND / DIRT / GRASS / SNOW / VEGETABLE / LEAVES / CACTUS / TOP_SNOW / SPONGE / CLAY group. */
    public static boolean isSoftMaterial(BlockState state) {
        SoundType sound = state.getSoundType();
        return sound == SoundType.WOOL || sound == SoundType.SAND || sound == SoundType.SNOW
                || sound == SoundType.GRASS || sound == SoundType.GRAVEL || sound == SoundType.CROP
                || state.is(BlockTags.WOOL) || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAND)
                || state.is(BlockTags.DIRT) || state.is(BlockTags.SNOW) || state.is(BlockTags.ICE)
                || state.is(Blocks.CACTUS) || state.is(Blocks.COBWEB) || state.is(Blocks.CLAY)
                || state.is(Blocks.SPONGE) || state.is(Blocks.WET_SPONGE);
    }


    /** The old Material.PLANT / REPLACEABLE_PLANT / CORAL / VEGETABLE / LEAVES group. */
    public static boolean isPlantLike(BlockState state) {
        SoundType sound = state.getSoundType();
        return sound == SoundType.GRASS || sound == SoundType.WET_GRASS || sound == SoundType.CROP
                || sound == SoundType.HARD_CROP || sound == SoundType.VINE || sound == SoundType.LILY_PAD
                || sound == SoundType.BAMBOO || sound == SoundType.BAMBOO_SAPLING || sound == SoundType.CORAL_BLOCK
                || sound == SoundType.SWEET_BERRY_BUSH
                || state.is(BlockTags.LEAVES) || state.is(BlockTags.CORALS) || state.is(BlockTags.CROPS)
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS) || state.is(Blocks.CACTUS)
                || state.is(BlockTags.REPLACEABLE);
    }

    /** The old Material.REPLACEABLE_PLANT / PLANT / WATER_PLANT / GRASS / BAMBOO / LEAVES / CORAL / VEGETABLE / EGG group used by the Hamon infusion. */
    public static boolean isLivingBlockMaterial(BlockState state) {
        SoundType sound = state.getSoundType();
        return sound == SoundType.GRASS || sound == SoundType.WET_GRASS || sound == SoundType.CROP
                || sound == SoundType.HARD_CROP || sound == SoundType.VINE || sound == SoundType.LILY_PAD
                || sound == SoundType.BAMBOO || sound == SoundType.BAMBOO_SAPLING || sound == SoundType.CORAL_BLOCK
                || sound == SoundType.SWEET_BERRY_BUSH
                || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.CORALS)
                || state.is(BlockTags.CORAL_BLOCKS) || state.is(BlockTags.CORAL_PLANTS)
                || state.is(BlockTags.CROPS) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS)
                || state.is(Blocks.CACTUS) || state.is(Blocks.TURTLE_EGG) || state.is(Blocks.DRAGON_EGG)
                || state.is(BlockTags.REPLACEABLE)
                || state.getBlock() instanceof SnowyDirtBlock;
    }
}
