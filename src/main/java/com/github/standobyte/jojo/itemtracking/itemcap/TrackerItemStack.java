package com.github.standobyte.jojo.itemtracking.itemcap;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.MerchantDataProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrackedItemPacket;
import com.github.standobyte.jojo.util.ForgeBusEventSubscriber;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.Constants;

/**
 * Currently item stacks are being tracked in:
 *   ItemEntity
 *   ItemFrameEntity
 *   LockableLootTileEntity
 *   Hoppers
 *   Jukeboxes
 *   Player inventory
 *   Horse chest inventory
 *   Minecart with chest inventory
 *   Mobs equipment
 *   Armor stands equipment
 *   Items picked up by mobs
 *   Shot arrows, knives, clackers, blade hats
 *   Thrown ender pearls, snowballs, eggs, splash/lingering potions
 *   Fireworks
 *
 */
public class TrackerItemStack {
    private static final Random RANDOM = new Random();
    private final ItemStack itemStack;
    @Nullable private UUID trackerUuid;
    private UUID trackingPlayerId;
    
    private ResourceKey<Level> positionDimension;
    private OptionalInt positionEntity = OptionalInt.empty();
    private BlockPos positionBlock = null;
    private BlockState containerBlockState;
    private Predicate<UUID> itemStillThere;
    private KnownItemState itemState;
    
    public TrackerItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    public TrackerItemStack(ItemStack itemStack, UUID trackerId) {
        this.itemStack = itemStack;
        this.trackerUuid = trackerId;
        updateSyncedTag();
    }
    
    
    @Nullable
    public static TrackerItemStack setTracked(ItemStack itemStack, ServerPlayer player) {
        return setTracked(itemStack, player, Mth.createInsecureUUID(RANDOM));
    }
    
    @Nullable
    public static TrackerItemStack setTracked(ItemStack itemStack, ServerPlayer player, UUID trackerId) {
        if (itemStack.getCount() != 1) {
            throw new IllegalArgumentException("Cannot track stacked items, only item stacks with count == 1 are supported");
        }
        return itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).resolve().map(cap -> {
            if (cap.trackerUuid == null) {
                cap.trackerUuid = trackerId;
                cap.trackingPlayerId = player.getUUID();
                cap.updateSyncedTag();
                SidedItemTrackerMap serverItemTracking = SaveFileUtilCapProvider.getSaveFileCap(player).getItemsTracker();
                serverItemTracking.addServerTrackedId(cap.trackerUuid);
                serverItemTracking.updateTracker(cap.trackerUuid, cap, player.level);
            }
            return cap;
        }).orElse(null);
    }
    
    
    public static Optional<TrackerItemStack> getItemTracker(ItemStack itemStack) {
        return getItemTracker(itemStack, false);
    }
    
    public static Optional<TrackerItemStack> getItemTracker(ItemStack itemStack, boolean allowEmpty) {
        if (!allowEmpty && itemStack.isEmpty()) {
            return Optional.empty();
        }
        return itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).resolve().map(
                cap -> cap.isTracked() ? cap : null);
    }
    
    /* when an item is being added to inventory, the original ItemStack's count is being taken from (to split the item between slots),
     * so we have to find the new ItemStack inside the inventory first
     */
    public static Optional<TrackerItemStack> getItemTrackerInInventory(ItemStack originalItemStack, Stream<ItemStack> inventoryItems, boolean allowEmpty) {
        return getItemTracker(originalItemStack, allowEmpty).flatMap(oldTracker -> {
            UUID trackerId = oldTracker.getTrackerId();
            Optional<TrackerItemStack> newTracker = inventoryItems
                    .map(movedItem -> movedItem.getCapability(TrackerItemStackProvider.CAPABILITY).resolve().map(tracker -> {
                        if (trackerId.equals(tracker.getTrackerId())) {
                            return tracker;
                        }
                        return null;
                    }))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            return newTracker;
        });
    }
    
    public static Predicate<ItemStack> trackerIdCheck(UUID trackerId) {
        return invItem -> hasTrackerId(invItem, trackerId);
    }
    
    public static boolean hasTrackerId(ItemStack item, UUID trackerId) {
        return trackerId.equals(TrackerItemStack.getItemTracker(item).map(TrackerItemStack::getTrackerId).orElse(null));
    }
    
    public void onUpdate(ServerLevel world) {
        SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker().updateTracker(trackerUuid, this, world);
        Player player = getTrackingPlayer(world);
        if (player instanceof ServerPlayer) {
            PacketManager.sendToClient(new TrackedItemPacket(
                    trackerUuid, itemStack, positionEntity, Optional.ofNullable(positionBlock)), 
                    (ServerPlayer) player);
        }
    }
    
    public Player getTrackingPlayer(ServerLevel world) {
        return trackingPlayerId != null ? world.getPlayerByUUID(trackingPlayerId) : null;
    }
    
    public void setAtEntity(int entityId, Level world, KnownItemState itemState) {
        this.positionEntity = OptionalInt.of(entityId);
        this.positionBlock = null;
        this.containerBlockState = null;
        this.positionDimension = world.dimension();
        this.itemState = itemState;
        if (!world.isClientSide()) {
            onUpdate((ServerLevel) world);
        }
    }
    
    public void setAtBlockPos(BlockPos blockPos, Level world, KnownItemState itemState) {
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = blockPos;
        this.containerBlockState = world.getBlockState(blockPos);
        this.positionDimension = world.dimension();
        this.itemState = itemState;
        if (!world.isClientSide()) {
            onUpdate((ServerLevel) world);
        }
    }
    
    public void setItemStillThereCheck(@Nullable Predicate<UUID> check) {
        this.itemStillThere = check;
    }
    
    public void setDisappeared(ServerLevel world) {
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = null;
        this.containerBlockState = null;
        this.positionDimension = null;
        this.itemStillThere = null;
        this.itemState = null;
        onUpdate(world);
    }
    
    @Nullable
    public Entity getAtEntity(Level world) {
        return positionEntity.isPresent() ? world.getEntity(positionEntity.getAsInt()) : null;
    }
    
    public OptionalInt getAtEntityId() {
        return positionEntity;
    }
    
    @Nullable
    public BlockPos getAtBlockPos() {
        return positionBlock;
    }
    
    @Nullable
    public KnownItemState getItemState() {
        return itemState;
    }
    
    public void tick(MinecraftServer server) {
        if (this.positionDimension != null) {
            ServerLevel world = server.getLevel(positionDimension);
            if (world != null && !checkItemIsThere(world)) {
                setDisappeared(world);
            }
        }
    }
    
    public boolean checkItemIsThere(ServerLevel world) {
        if (this.positionDimension == null) return false;
        
        if (positionEntity.isPresent()) {
            Entity entity = world.getEntity(positionEntity.getAsInt());
            if (entity == null || entity.removed) {
                return false;
            }
        }
        else if (positionBlock != null && containerBlockState != null) {
            BlockState blockState = world.getBlockState(positionBlock);
            if (this.containerBlockState.getBlock() != blockState.getBlock()) {
                return false;
            }
        }
        
        return itemStillThere == null || itemStillThere.test(trackerUuid);
    }
    
    public void copy(TrackerItemStack oldTracker) {
        this.trackerUuid = oldTracker.trackerUuid;
        this.trackingPlayerId = oldTracker.trackingPlayerId;
        
        this.positionDimension = oldTracker.positionDimension;
        this.positionEntity = oldTracker.positionEntity;
        this.positionBlock = oldTracker.positionBlock;
        this.containerBlockState = oldTracker.containerBlockState;
        this.itemStillThere = oldTracker.itemStillThere;
        this.itemState = oldTracker.itemState;
        updateSyncedTag();
    }
    
    public void clear() {
        this.trackerUuid = null;
        
        this.trackingPlayerId = null;
        
        this.positionDimension = null;
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = null;
        this.containerBlockState = null;
        this.itemStillThere = null;
        this.itemState = null;
        updateSyncedTag();
//        forceItemNbtToSync();
    }
    
    public void moveToItem(ItemStack newItem, ServerLevel world) {
        TrackerItemStack.getItemTracker(newItem).ifPresent(newTracker -> {
            newTracker.copy(this);
            SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker().updateTracker(newTracker.getTrackerId(), newTracker, world);
        });
        this.clear();
    }
    
    public Vec3 markerPos(Level world, float partialTick) {
        if (positionEntity.isPresent()) {
            Entity entity = world.getEntity(positionEntity.getAsInt());
            if (entity != null) {
                Vec3 position;
                if (entity.level.isClientSide()) {
                    position = entity.getPosition(partialTick);
                }
                else {
                    position = entity.position();
                }
                return position.add(0, entity.getBbHeight() + 0.25, 0);
            }
        }
        if (positionBlock != null) {
            return Vec3.upFromBottomCenterOf(positionBlock, 1.0);
        }
        
        return null;
    }
    
    public ItemStack getItem() {
        return itemStack;
    }
    
    public boolean isTracked() {
        return trackerUuid != null;
    }
    
    public UUID getTrackerId() {
        return trackerUuid;
    }

    
    public Tag toNBT() {
        return toNBT(true);
    }
    
    public Tag toNBT(boolean savePlayerId) {
        CompoundTag nbt = new CompoundTag();
        if (trackerUuid != null) {
            nbt.putUUID("Id", trackerUuid);
            if (trackingPlayerId != null) {
                nbt.putUUID("Player", trackingPlayerId);
            }
        }
        return nbt;
    }
    
    public void fromNBT(Tag inbt) {
        CompoundTag nbt = (CompoundTag) inbt;
        trackerUuid = null;
        trackingPlayerId = null;
        if (nbt.hasUUID("Id")) {
            trackerUuid = nbt.getUUID("Id");
            if (nbt.hasUUID("Player")) {
                trackingPlayerId = nbt.getUUID("Player");
            }
        }
        updateSyncedTag();
    }
    
    
    public static enum KnownItemState {
        ENTITY_IS_ITEM,
        ENTITY_HAS_ITEM,
        STUCK_ARROW,
        STUCK_KNIFE,
        BLOCK_IS_ITEM,
        BLOCK_HAS_ITEM
    }
    
    
    // the shit below is cursed, but it seems like the only way to get this shit to sync properly from remote servers to the client
    // i hate hacking into forge's systems, but i hate the implementation of item capabilities more
    
    private static final String CAP_NBT_KEY = ForgeBusEventSubscriber.ITEM_TRACK_CAP.toString();
    private void updateSyncedTag() {
        if (trackerUuid == null) {
            CompoundTag itemNBT = itemStack.getTag();
            if (itemNBT != null && !itemNBT.isEmpty()) {
                MCUtil.nbtGetCompoundOptional(itemNBT, "ForgeCaps").ifPresent(capsNBT -> {
                    if (capsNBT.contains(CAP_NBT_KEY)) {
                        capsNBT.remove(CAP_NBT_KEY);
                        if (capsNBT.isEmpty()) {
                            itemNBT.remove("ForgeCaps");
                        }
                    }
                });
                itemNBT.remove("ReadCapOnSet");
                if (itemNBT.isEmpty()) {
                    itemStack.setTag(null); // so that this stack is once again stackable
                }
            }
        }
        else {
            CompoundTag itemNBT = itemStack.getOrCreateTag();
            CompoundTag capsNBT = MCUtil.nbtGetOrCreateCompound(itemNBT, "ForgeCaps");
            capsNBT.put(CAP_NBT_KEY, this.toNBT(false));
            setDeserializeForgeCaps(itemNBT);
        }
    }
    
    /**
     * Is called on the server to let the client know that we want to deserialize the ForgeCaps tag (item tracking) too
     */
    public static void setDeserializeForgeCaps(CompoundTag itemTag) {
        itemTag.putByte("ReadCapOnSet", (byte) 0);
    }
    
    /**
     * Is intended as a check for when the ItemStack is deserialized on client side, to tell if we need to also deserialize ForgeCaps
     */
    public static boolean deserializesForgeCaps(CompoundTag itemTag) {
        return itemTag != null && itemTag.contains("ReadCapOnSet") && itemTag.contains("ForgeCaps", Tag.TAG_COMPOUND);
    }
    

    public void onShrink(ServerLevel world) {
        if (positionBlock != null) {
            BlockEntity tileEntity = world.getBlockEntity(positionBlock);
            if (tileEntity instanceof JukeboxBlockEntity) {
                JukeboxBlockEntity jukebox = (JukeboxBlockEntity) tileEntity;
                BlockState blockState = world.getBlockState(positionBlock);
                world.levelEvent(1010, positionBlock, 0);
                jukebox.clearContent();
                blockState = blockState.setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(false));
                world.setBlock(positionBlock, blockState, 2);
            }
        }
        else if (positionEntity.isPresent()) {
            Entity entity = getAtEntity(world);
            if (entity instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) entity;
                itemFrame.setItem(ItemStack.EMPTY);
            }
            else if (entity instanceof Villager) {
                Player thiefPlayer = getTrackingPlayer(world);
                if (thiefPlayer != null) {
                    ((Villager) entity).getGossips().add(thiefPlayer.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
                    world.broadcastEntityEvent(entity, MCUtil.EntityEvents.VILLAGER_ANGRY);
                    entity.getCapability(MerchantDataProvider.CAPABILITY).ifPresent(merchantData -> {
                        merchantData.setRefuseTrading(thiefPlayer.getUUID(), true);
                    });
                }
            }
            else if (entity instanceof Piglin && itemStack.getItem() == PiglinAi.BARTERING_ITEM) {
                Player thiefPlayer = getTrackingPlayer(world);
                if (thiefPlayer != null) {
                    PiglinTasksAccess.onPiglinScammed((Piglin) entity, thiefPlayer);
                }
            }
        }
    }
    
    private static class PiglinTasksAccess extends PiglinAi {
        
        protected static void onPiglinScammed(Piglin piglin, LivingEntity player) {
            PiglinAi.wasHurtBy(piglin, player);
            /*
             * TODO piglin scam counter
             *     if > 0, when receiving a gold ingot, they don't give an item back and instead decrement the counter
             *         if after the decrement scam counter == 0, stop attacking
             */
//            incrementScamCounter(piglin);
        }
    }
}
