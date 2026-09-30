package com.github.standobyte.jojo.util;

import java.util.HashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.chunk.ChunkCap;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.MerchantData;
import com.github.standobyte.jojo.capability.entity.MerchantDataProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.power.NonStandCapProvider;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCap;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.capability.world.WorldUtilCap;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.command.ConfigPackCommand;
import com.github.standobyte.jojo.command.HamonStatCommand;
import com.github.standobyte.jojo.command.JojoCommandsCommand;
import com.github.standobyte.jojo.command.JojoControlsCommand;
import com.github.standobyte.jojo.command.JojoEnergyCommand;
import com.github.standobyte.jojo.command.JojoPowerCommand;
import com.github.standobyte.jojo.command.PillarmanModeCommand;
import com.github.standobyte.jojo.command.RockPaperScissorsCommand;
import com.github.standobyte.jojo.command.StandCommand;
import com.github.standobyte.jojo.command.StandDiscGiveCommand;
import com.github.standobyte.jojo.command.StandLevelCommand;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStackProvider;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStackStorage;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.UpdateClientCapCachePacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntityTypeToInstance;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ForgeBusEventSubscriber {
    public static final ResourceLocation STAND_CAP = new ResourceLocation(JojoMod.MOD_ID, "stand");
    public static final ResourceLocation NON_STAND_CAP = new ResourceLocation(JojoMod.MOD_ID, "non_stand");
    public static final ResourceLocation PLAYER_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "player_util");
    public static final ResourceLocation CLIENT_PLAYER_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "client_player_util");
    public static final ResourceLocation LIVING_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "living_util");
    public static final ResourceLocation ENTITY_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "entity_util");
    public static final ResourceLocation ENTITY_HAMON_CHARGE_CAP = new ResourceLocation(JojoMod.MOD_ID, "entity_hamon_charge");
    public static final ResourceLocation PROJECTILE_HAMON_CAP = new ResourceLocation(JojoMod.MOD_ID, "projectile_hamon");
    public static final ResourceLocation MERCHANT_CAP = new ResourceLocation(JojoMod.MOD_ID, "merchant");
    public static final ResourceLocation WORLD_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "world_util");
    public static final ResourceLocation SAVE_FILE_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "save_file_util");
    public static final ResourceLocation MR_PRESIDENT_CAP = new ResourceLocation(JojoMod.MOD_ID, "mr_president");
    public static final ResourceLocation CHUNK_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "chunk_util");
    public static final ResourceLocation ITEM_TRACK_CAP = new ResourceLocation(JojoMod.MOD_ID, "item_track");
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        StandCommand.register(dispatcher);
        StandDiscGiveCommand.register(dispatcher);
        StandLevelCommand.register(dispatcher);
        JojoPowerCommand.register(dispatcher);
        JojoEnergyCommand.register(dispatcher);
        JojoControlsCommand.register(dispatcher);
        HamonStatCommand.register(dispatcher);
        RockPaperScissorsCommand.register(dispatcher);
        ConfigPackCommand.register(dispatcher);
        JojoCommandsCommand.register(dispatcher);
        PillarmanModeCommand.register(dispatcher);
    }
    
    
    
    @SubscribeEvent
    public static void onAttachCapabilitiesWorld(AttachCapabilitiesEvent<Level> event) {
        Level world = event.getObject();
        event.addCapability(WORLD_UTIL_CAP, new WorldUtilCapProvider(world));
        if (!world.isClientSide()) {
            if (world.dimension() == Level.OVERWORLD) {
                event.addCapability(SAVE_FILE_UTIL_CAP, new SaveFileUtilCapProvider((ServerLevel) world));
            }
            else if (ModDimensions.MR_PRESIDENT != null && world.dimension() == ModDimensions.MR_PRESIDENT) {
                event.addCapability(MR_PRESIDENT_CAP, new MrPresidentWorldDataProvider((ServerLevel) world));
            }
        }
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk chunk = event.getObject();
        event.addCapability(CHUNK_UTIL_CAP, new ChunkCapProvider(chunk));
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        event.addCapability(ENTITY_UTIL_CAP, new EntityUtilCapProvider(entity));
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            if (entity instanceof Player) {
                Player player = (Player) living;
                event.addCapability(STAND_CAP, new StandCapProvider(player));
                event.addCapability(NON_STAND_CAP, new NonStandCapProvider(player));
                event.addCapability(PLAYER_UTIL_CAP, new PlayerUtilCapProvider(player));
                if (player.level.isClientSide()) {
                    event.addCapability(CLIENT_PLAYER_UTIL_CAP, new ClientPlayerUtilCapProvider(player));
                }
            }
            event.addCapability(LIVING_UTIL_CAP, new LivingUtilCapProvider(living));
            if (entity instanceof Merchant) {
                event.addCapability(MERCHANT_CAP, new MerchantDataProvider(living, (Merchant) living));
            }
            event.addListener(() -> {
                IStandPower.getStandPowerOptional(living).ifPresent(
                        stand -> stand.getContinuousEffects().onStandUserRemoved(living));
            });
        }
        if (entity instanceof Projectile && (HamonUtil.ProjectileChargeProperties.canBeChargedWithHamon(entity))) {
            event.addCapability(PROJECTILE_HAMON_CAP, new ProjectileHamonChargeCapProvider(entity));
        }
        if (entity instanceof LivingEntity || entity instanceof ItemEntity) {
            event.addCapability(ENTITY_HAMON_CHARGE_CAP, new EntityHamonChargeCapProvider(entity));
        }
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesItem(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(ITEM_TRACK_CAP, new TrackerItemStackProvider(event.getObject()));
    }
    
    /**
     * 1.19+ registers capabilities by type only: the storage interface is gone
     * and the default instances are no longer supplied here. The same set of
     * capabilities is registered as in 1.16.5, and the class-level
     * CapabilityManager.get tokens in the providers resolve against these.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IStandPower.class);
        event.register(INonStandPower.class);
        event.register(PlayerUtilCap.class);
        event.register(ClientPlayerUtilCap.class);
        event.register(LivingUtilCap.class);
        event.register(EntityUtilCap.class);
        event.register(EntityHamonChargeCap.class);
        event.register(ProjectileHamonChargeCap.class);
        event.register(MerchantData.class);

        event.register(WorldUtilCap.class);
        event.register(SaveFileUtilCap.class);
        event.register(MrPresidentWorldData.class);

        event.register(ChunkCap.class);

        event.register(TrackerItemStack.class);
    }
    
    
    
    @SubscribeEvent
    public static void onEntityTracking(PlayerEvent.StartTracking event) {
        Entity entityTracked = event.getTarget();
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (entityTracked instanceof LivingEntity) {
            LivingEntity livingTracked = (LivingEntity) entityTracked;
            INonStandPower.getNonStandPowerOptional(livingTracked).ifPresent(power -> {
                power.syncWithTrackingOrUser(player);
            });
            IStandPower.getStandPowerOptional(livingTracked).ifPresent(power -> {
                power.syncWithTrackingOrUser(player);
            });
            livingTracked.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.onTracking(player);
            });
            if (livingTracked instanceof Player) {
                livingTracked.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.onTracking(player);
                });
            }
        }
        entityTracked.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.onTracking(player);
        });
        entityTracked.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.onTracking(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        // When the original player is removed (death or leaving a dimension) its
        // capabilities may already be invalidated by the time this fires, which
        // would silently lose the Stand/non-Stand data. Reviving them for the copy
        // and invalidating them again afterwards is the pattern Forge documents
        // for providers that are already torn down. isWasDeath() still separates a
        // real respawn from returning to the Overworld, exactly as in 1.16.5.
        original.reviveCaps();
        try {
            cloneCap(INonStandPower.getNonStandPowerOptional(original), INonStandPower.getNonStandPowerOptional(player), 
                    event.isWasDeath(), "Stand capability");
            cloneCap(IStandPower.getStandPowerOptional(original), IStandPower.getStandPowerOptional(player), 
                    event.isWasDeath(), "non-Stand capability");
            
            original.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(oldCap -> {
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(newCap -> {
                    newCap.onClone(oldCap, event.isWasDeath());
                });
            });
            
            original.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(oldCap -> {
                player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(newCap -> {
                    newCap.onClone(oldCap, event.isWasDeath());
                });
            });
        }
        finally {
            original.invalidateCaps();
        }
    }
    
    private static <T extends IPower<T, ?>> void cloneCap(LazyOptional<T> oldCap, LazyOptional<T> newCap, boolean wasDeath, String warning) {
        if (oldCap.isPresent() && newCap.isPresent()) {
            newCap.resolve().get().onClone(oldCap.resolve().get(), wasDeath);
        }
        else {
            JojoMod.getLogger().warn("Failed to copy " + " data!");
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        SaveFileUtilCapProvider.getSaveFileCap(player).onPlayerLogIn(player);
        JojoModConfig.Common.SyncedValues.syncWithClient(player);
        syncPowerData(event.getEntity());
        IStandPower.getStandPowerOptional(event.getEntity()).ifPresent(power -> {
            if (power.hasPower()) {
                power.getType().unlockNewActions(power);
            }
        });
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        syncPowerData(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        syncPowerData(event.getEntity());
    }
    
    private static void syncPowerData(Player player) {
        INonStandPower.getPlayerNonStandPower(player).syncWithUserOnly();
        IStandPower.getPlayerStandPower(player).syncWithUserOnly();
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.syncWithClient();
        });
        player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.syncWithClient((ServerPlayer) player);
        });
        PacketManager.sendToClient(new UpdateClientCapCachePacket(), (ServerPlayer) player);
    }
    
    
    
    @SubscribeEvent
    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        JojoModConfig.Common.SyncedValues.onPlayerLogout((ServerPlayer) event.getEntity());
    }
    
    
    
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level) {
            if (event.getLevel() instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel) event.getLevel();
            }
            EntityTypeToInstance.init((Level) event.getLevel());
        }
        EntityTypeToInstance.init((Level) event.getLevel());
    }
    

}
