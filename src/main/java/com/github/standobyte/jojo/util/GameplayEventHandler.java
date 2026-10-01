package com.github.standobyte.jojo.util;

import net.minecraftforge.event.entity.living.LivingEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.JojoModConfig.Common;
import com.github.standobyte.jojo.action.non_stand.HamonPlantItemInfusion;
import com.github.standobyte.jojo.action.non_stand.HamonRebuffOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonRopeTrap;
import com.github.standobyte.jojo.action.non_stand.HamonSnakeMuffler;
import com.github.standobyte.jojo.action.non_stand.PillarmanBladeBarrage;
import com.github.standobyte.jojo.action.non_stand.PillarmanUnnaturalAgility;
import com.github.standobyte.jojo.action.non_stand.VampirismFreeze;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.effect.BoyIIManStandPartTakenEffect;
import com.github.standobyte.jojo.action.stand.effect.CDTurnIntoAngeloRockEffect;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.block.WoodenCoffinBlock;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.enchantment.GlovesSpeedEnchantment;
import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModPaintings;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.GlovesItem;
import com.github.standobyte.jojo.item.InkPastaItem;
import com.github.standobyte.jojo.item.OilItem;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.mrpresident.CocoJumboTurtleEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveEffectStartPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SpawnParticlePacket;
import com.github.standobyte.jojo.potion.BleedingEffect;
import com.github.standobyte.jojo.potion.HamonSpreadEffect;
import com.github.standobyte.jojo.potion.IApplicableEffect;
import com.github.standobyte.jojo.potion.StatusEffect;
import com.github.standobyte.jojo.potion.VampireSunBurnEffect;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.bowcharge.BowChargeEffectInstance;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismData;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.ResolveCounter;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mc.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.mc.damage.ModdedDamageSourceWrapper;
import com.github.standobyte.jojo.util.mc.damage.NoKnockbackOnBlocking;
import com.github.standobyte.jojo.util.mc.damage.StandLinkDamageSource;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.world.gen.LoadMeFeature;
import com.google.common.collect.Iterables;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

//TODO move all event handlers to their respective classes, leave the method links here
@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class GameplayEventHandler {
    public static final boolean DELETE_ME = true;
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        VampirismUtil.tickSunDamage(entity);
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.tick();
        });
        NoKnockbackOnBlocking.tickAttribute(entity);
    }

    private static final int AFK_PARTICLE_SECONDS = 30;
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        Player player = event.player;
        switch (event.phase) {
        case START:
            if (ModStatusEffects.isStunned(player)) {
                player.setSprinting(false);
            }
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.tick();
            });
            if (event.side == LogicalSide.SERVER) {
                if (player.tickCount % 60 == 0 && !player.isInvisible() && player instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    long timeNotActive = Util.getMillis() - serverPlayer.getLastActionTime();
                    if (timeNotActive > AFK_PARTICLE_SECONDS * 1000 &&
                            serverPlayer.getCapability(PlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getNoClientInputTimer() > AFK_PARTICLE_SECONDS * 20).orElse(true)) {
                        MCUtil.sendParticles((ServerLevel) player.level, ModParticles.MENACING.get(), player.getX(), player.getEyeY(), player.getZ(), 
                                0, Mth.cos(player.yRot * MathUtil.DEG_TO_RAD), 0.5F, Mth.sin(player.yRot * MathUtil.DEG_TO_RAD), 0.005F, 
                                SpawnParticlePacket.SpecialContext.AFK);
                        ModCriteriaTriggers.AFK.get().trigger(serverPlayer);
                    }
                }
                if (player.tickCount % 20 == 0 && player instanceof ServerPlayer) {
                    checkStandStructures((ServerPlayer) player);
                }
            }
            
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.tick();
            });
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                MagiciansRedEntity.removeFireUnderPlayer(player, power);
                power.tick();
            }); 
            break;
        case END:
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.postTick();
            });
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                power.postTick();
            }); 
            break;
        }
    }
    
    private static void checkStandStructures(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(player.blockPosition())) {
            return;
        }
        ResourceKey<Structure>[] structures = new ResourceKey[] {
                ModStructures.METEORITE, ModStructures.PILLARMAN_TEMPLE, ModStructures.HAMON_TEMPLE };
        for (ResourceKey<Structure> structureKey : structures) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition(), structureKey);
            if (start.isValid()) {
                ModCriteriaTriggers.STAND_STRUCTURE.get().trigger(player, structureKey);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void replaceStrayArrow(EntityJoinLevelEvent event) {
        Entity newEntity = event.getEntity();
        if (newEntity instanceof Arrow) {
            Arrow arrow = (Arrow) newEntity;
            if (arrow.getOwner() instanceof Stray) {
                arrow.setEffectsFromItem(new ItemStack(Items.ARROW));
                arrow.addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), 300));
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.side == LogicalSide.SERVER /* actually only ticks on server but ok */) {
            ServerLevel world = (ServerLevel) event.level;
            switch (event.phase) {
            case START:
                break;
            case END:
                world.getAllEntities().forEach(entity -> {
                    entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                    entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                    entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                });

                world.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
                    LevelChunk chunk = chunkHolder.getTickingChunk();
                    if (chunk != null) {
                        chunk.getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                    }
                });
                
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (event.phase == TickEvent.Phase.END) {
            SidedItemTrackerMap.tick(server);
        }
    }
    
    @SubscribeEvent
    public static void onChunkLoad(ChunkWatchEvent.Watch event) {
        LevelChunk chunk = event.getLevel().getChunkSource().getChunk(event.getPos().x, event.getPos().z, false);
        if (chunk != null) {
            chunk.getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> cap.onChunkLoad(event.getPlayer()));
        }
    }
    
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel) {
            MinecraftServer server = ((ServerLevel) event.getLevel()).getServer();
            for (RegistryObject<? extends Feature<?>> featureSupplier : ModStructures.FEATURES.getEntries()) {
                Feature<?> feature = (Feature<?>) featureSupplier.get();
                if (feature instanceof LoadMeFeature) {
                    ((LoadMeFeature) feature).loadTemplate(server);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelStunnedPlayerInteraction(PlayerInteractEvent event) {
        if (event.isCancelable() && ModStatusEffects.isStunned(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelItemPickupInStun(EntityItemPickupEvent event) {
        if (ModStatusEffects.isStunned(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void addEntityDrops(LivingDropsEvent event) {
        Common config = JojoModConfig.getCommonConfigInstance(false);
        if (config.dropStandDisc.get() && !config.keepStandOnDeath.get()) {
            LivingEntity entity = event.getEntity();
            IStandPower.getStandPowerOptional(entity).ifPresent(power -> {
                if (power.hasPower()) {
                    ItemStack disc = StandDiscItem.withStand(new ItemStack(ModItems.STAND_DISC.get()), power.getStandInstance().get());
                    event.getDrops().add(new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), disc));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level.isClientSide() && entity instanceof Mob) {
            VampirismUtil.editMobAiGoals((Mob) entity);
        }
//        else if (entity.getType() == EntityType.PAINTING) {
//            cutOutHands((PaintingEntity) event.getEntity());
//        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (ModInteractionUtil.isSquidInkPasta(event.getItemStack())) {
            InkPastaItem.useWithHamon(event.getLevel(), event.getEntity(), event.getHand()).ifPresent(result -> {
                event.setCanceled(true);
                event.setCancellationResult(result.getResult());
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBowDrawStart(LivingEntityUseItemEvent.Start event) {
        if (BowChargeEffectInstance.itemFits(event.getItem())) {
            LivingEntity entity = event.getEntity();
            for (PowerClassification powerClassification : PowerClassification.values()) {
                IPower.getPowerOptional(entity, powerClassification).ifPresent(
                        power -> power.onItemUseStart(event.getItem(), event.getDuration()));
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBowDrawStop(LivingEntityUseItemEvent.Stop event) {
        if (BowChargeEffectInstance.itemFits(event.getItem())) {
            LivingEntity entity = event.getEntity();
            for (PowerClassification powerClassification : PowerClassification.values()) {
                IPower.getPowerOptional(entity, powerClassification).ifPresent(
                        power -> power.onItemUseStop(event.getItem(), event.getDuration()));
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        ItemStack item = event.getItem();
        if (item.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            VampirismUtil.onEnchantedGoldenAppleEaten(event.getEntity());
        }
        else if (ModInteractionUtil.isSquidInkPasta(item)) {
            InkPastaItem.onEaten(entity);
        }
        
        if (event.getItem().isEdible()) {
            FoodProperties food = item.getItem().getFoodProperties();
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).ifPresent(pillarman -> {
                    power.addEnergy(food.getNutrition() * 10);
                });
                power.getTypeSpecificData(ModPowers.ZOMBIE.get()).ifPresent(zombie -> {
                    if (food.isMeat()) {
                        power.addEnergy(food.getNutrition() * 10); 
                        entity.heal(food.getNutrition());
                    }
                });
            });
        }
    }
    
    @SubscribeEvent
    public static void itemAttributeModifiers(ItemAttributeModifierEvent event) {
        GlovesSpeedEnchantment.addAtrributeModifiersFromEvent(event.getItemStack(), event);
    }
    
    private static void cutOutHands(Painting painting) {
        if (!painting.level.isClientSide()) {
            boolean monaLisaFull = painting.getVariant().value() == ModPaintings.MONA_LISA.get();
            boolean monaLisaHands = painting.getVariant().value() == ModPaintings.MONA_LISA_HANDS.get();
            if (monaLisaFull || monaLisaHands) {
                List<LivingEntity> KQUsers = painting.level.getEntitiesOfClass(
                        LivingEntity.class, painting.getBoundingBox().expandTowards(painting.getLookAngle().scale(3)).inflate(1), 
                            entity -> IStandPower.getStandPowerOptional(entity).map(
                                    stand -> stand.hasPower() && stand.getType() == ModStandsInit.KILLER_QUEEN.get())
                            .orElse(false));
                if (!KQUsers.isEmpty()) {
                    if (monaLisaFull) {
                        painting.setVariant(ModPaintings.MONA_LISA_HANDS.getHolder().orElseThrow());
                        double x = painting.getX();
                        double z = painting.getZ();
                        if (x - (int) x != 0 && (int) (x + 0.04) != (int) x) {
                            z -= 1;
                        }
                        if (z - (int) z != 0 && (int) (z - 0.04) != (int) z) {
                            x -= 1;
                        }
                        painting.setPos(x, painting.getY(), z);
                    }
                }
                else if (monaLisaHands) {
                    painting.setVariant(net.minecraft.core.registries.BuiltInRegistries.PAINTING_VARIANT.getHolderOrThrow(net.minecraft.world.entity.decoration.PaintingVariants.KEBAB));
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        float amount = event.getAmount();
        if (entity.hasEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get())) {
            amount = VampireSunBurnEffect.reduceUndeadHealing();
        }
        if (amount > 0 && entity.hasEffect(ModStatusEffects.HAMON_SPREAD.get())) {
            amount = HamonSpreadEffect.reduceUndeadHealing(entity.getEffect(ModStatusEffects.HAMON_SPREAD.get()), amount);
        }
        if (amount <= 0) {
            event.setCanceled(true);
        }
        event.setAmount(amount);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHeal(LivingHealEvent event) {
        VampirismUtil.consumeEnergyOnHeal(event);
    }
    
    @SubscribeEvent()
    public static void transferStandEffects(LivingConversionEvent.Post event) {
        LivingEntity original = event.getEntity();
        LivingEntity newEntity = event.getOutcome();
        original.revive(); // bruh
        original.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(oldData -> {
            List<StandEffectInstance> _standEffects = oldData.getEffectsTargetedBy();
            if (!_standEffects.isEmpty()) {
                // we need to make a deep copy, because changing the effects target removes the elements
                // from the original list, which would throw ConcurrentModificationException in this case
                List<StandEffectInstance> standEffects = new ArrayList<>(_standEffects);
                for (StandEffectInstance effect : standEffects) {
                    effect.setTargetEntity(newEntity);
                }
            }
        });
        original.discard();
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void releaseStun(LivingConversionEvent.Post event) {
        if (event.getOutcome() instanceof Mob) {
            LivingEntity pre = event.getEntity();
            Mob converted = (Mob) event.getOutcome();
            if (converted.isNoAi() && ModStatusEffects.isStunned(pre) && !ModStatusEffects.isStunned(converted)) {
                converted.setNoAi(false);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtStart(LivingAttackEvent event) {
        DamageSource dmgSource = event.getSource();
        LivingEntity target = event.getEntity();
        Entity attacker = dmgSource.getEntity();
        
        if (target.level.isClientSide() && JojoModUtil.isDyingBody(target)) {
            event.setCanceled(true);
        }
        
        if (attacker != null && attacker instanceof LivingEntity) {
            LivingEntity attackerLiving = (LivingEntity) attacker;
            if (attacker.is(dmgSource.getDirectEntity())) {
                // redirect melee attacks on Boy II Man user who has taken the attacker's arms
                if (IStandPower.getStandPowerOptional(attackerLiving).resolve().flatMap(attackerStand -> {
                    return IStandPower.getStandPowerOptional(target).map(boyIIManStand -> {
                        Stream<StandEffectInstance> takenArmsEffects = boyIIManStand.getContinuousEffects().getEffects()
                                .filter(effect -> {
                                    if (effect.effectType == ModStandEffects.BOY_II_MAN_PART_TAKE.get() && attacker.is(effect.getTarget())) {
                                        StandInstance partsTaken = ((BoyIIManStandPartTakenEffect) effect).getPartsTaken();
                                        return partsTaken.getType() == attackerStand.getType() && partsTaken.hasPart(StandPart.ARMS);
                                    }
                                    return false;
                                });
                        return takenArmsEffects.findAny().isPresent();
                    });
                }).orElse(false)) {
                    attacker.hurt(dmgSource, event.getAmount());
                    event.setCanceled(true);
                    return;
                }
            }
            
            // redirect attacks on mobs created by Gold Experience
            Optional<GECreatedLifeformEffect> targetLifeform = StandEffectsTracker.getEffectsTargetedBy(target, ModStandEffects.GE_CREATED_LIFEFORM.get()).findAny();
            if (targetLifeform.isPresent() && !StandEffectsTracker.isTargetedBy(attackerLiving, ModStandEffects.GE_CREATED_LIFEFORM.get())) {
                if (dmgSource instanceof IStandDamageSource) {
                    ((IStandDamageSource) dmgSource).setStandCanHitSelf();
                }
                boolean dealtDamage = attackerLiving.hurt(dmgSource, event.getAmount());
                event.setCanceled(true);
                if (dealtDamage) {
                	IStandPower geUserPower = targetLifeform.get().getUserPower();
                	ResolveCounter.addResolve(geUserPower, target, event.getAmount());
                }
                return;
            }
        }
        
        // Attack the entity from a different DamageSource if the attacker has the effect that lets them hit Stands
        if (target instanceof StandEntity && !((StandEntity) target).canTakeDamageFrom(dmgSource)
                && !(dmgSource instanceof ModdedDamageSourceWrapper && ((ModdedDamageSourceWrapper) dmgSource).canHurtStands())
                && attacker instanceof LivingEntity) {
            LivingEntity attackerLiving = (LivingEntity) attacker;
            boolean canHitStands = attackerLiving.hasEffect(ModStatusEffects.INTEGRATED_STAND.get());
            if (canHitStands) {
                event.setCanceled(true);
                target.hurt(new ModdedDamageSourceWrapper(dmgSource).setCanHurtStands(), event.getAmount());
                return;
            }
        }
        
        // Deal Hamon damage through oiled weapons
        if (!dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) && !dmgSource.getMsgId().startsWith(DamageUtil.HAMON.location().getPath()) && 
                attacker != null && attacker.is(dmgSource.getDirectEntity()) && attacker instanceof LivingEntity) {
            LivingEntity hamonUser = (LivingEntity) attacker;
            ItemStack weapon = hamonUser.getMainHandItem();
            
            INonStandPower.getNonStandPowerOptional(hamonUser).ifPresent(power -> {
                OilItem.remainingOiledUses(weapon).ifPresent(oilUses -> {
                    float energyCost = 500F;
                    if (power.hasPower() && power.getEnergy() >= energyCost) {
                        power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                            power.consumeEnergy(energyCost);
                            DamageUtil.dealHamonDamage(target, 1.5F, hamonUser, null);
                            hamon.hamonPointsFromAction(HamonStat.STRENGTH, 500);
                            
                            OilItem.setWeaponOilUses(weapon, oilUses - 1);
                        });
                    }
                });
            });
        }
        
        if (target.invulnerableTime > 0 && dmgSource instanceof IModdedDamageSource && 
                ((IModdedDamageSource) dmgSource).bypassInvulTicks()) {
            event.setCanceled(true);
            DamageUtil.hurtThroughInvulTicks(target, dmgSource, event.getAmount());
            return;
        }
        
        standBlockUserAttack(dmgSource, target, stand -> {
            if (!stand.isInvulnerableTo(dmgSource)) {
                stand.hurt(dmgSource, event.getAmount());
                event.setCanceled(true);
            };
        });
        
        if (HamonUtil.cancelDamageFromBlock(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
        if (VampirismFreeze.onUserAttacked(event)) {
            event.setCanceled(true);
        }
        if (PillarmanUnnaturalAgility.onUserAttacked(event)) {
            event.setCanceled(true);
        }
        if (PillarmanBladeBarrage.onUserAttacked(event)) {
        	event.setCanceled(true);
        }
        
        if (GeneralUtil.orElseFalse(target.getSleepingPos(), sleepingPos -> {
            BlockState blockState = target.level.getBlockState(sleepingPos);
            return blockState.getBlock() instanceof WoodenCoffinBlock && blockState.getValue(WoodenCoffinBlock.CLOSED);
        })) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void cancelLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource dmgSource = event.getSource();
        float dmgAmount = event.getAmount();
        if (GeneralUtil.orElseFalse(ContinuousActionInstance.getCurrentAction(entity), 
                action -> action.cancelIncomingDamage(dmgSource, dmgAmount))
                || HamonSnakeMuffler.snakeMuffler(entity, dmgSource, dmgAmount)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void reduceDamageFromConfig(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.canUpdate() && target.getCapability(EntityUtilCapProvider.CAPABILITY)
                .map(cap -> cap.wasStoppedInTime()).orElse(false)) {
            event.setAmount(event.getAmount() * JojoModConfig.getCommonConfigInstance(false)
                    .timeStopDamageMultiplier.get().floatValue());
        }
        
        if (!target.level.isClientSide()) {
            ResolveCounter.resolveOnHurtEvent(event.getSource(), target, event.getAmount());
        }
    }
    
    // TODO unsummoned stand auto-block
    @SuppressWarnings("unused")
    private double getAttackSpeed(DamageSource damageSrc) {
        Entity entity = damageSrc.getDirectEntity();
        if (entity == null) {
            return -1;
        }
        if (entity instanceof Projectile) {
            double velocity = entity.getDeltaMovement().length();
            //
        }
        if (entity instanceof LivingEntity) {
            LivingEntity entityLiving = (LivingEntity) entity;
            if (entity instanceof StandEntity) {
                StandEntity entityStand = ((StandEntity) entity);
                Optional<StandEntityAction> attack = entityStand.getCurrentTaskActionOptional();
                if (attack.isPresent()) {
                    //
                }
            }
            AttributeInstance attackSpeedAttribute = entityLiving.getAttribute(Attributes.ATTACK_SPEED);
            if (attackSpeedAttribute != null) {
                double attackSpeed = ((LivingEntity) entity).getAttributeValue(Attributes.ATTACK_SPEED);
                //
            }
            //
        }
        //
        return 1234;
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void blockDamage(LivingHurtEvent event) {
        DamageSource dmgSource = event.getSource();
        LivingEntity target = event.getEntity();
        // block explosion with stand
        if (dmgSource.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
                Explosion explosion = util.getSourceExplosion(dmgSource);
                if (explosion != null) {
                    StandEntity stand = getTargetStand(target);
                    if (stand != null && stand.isFollowingUser() && stand.isStandBlocking()) {
                        double standDurability = stand.getDurability();
                        if (standDurability > 4) {
                            double cos = explosion.getPosition().subtract(target.position()).normalize().dot(stand.getLookAngle());
                            if (cos > 0) {
                                float multiplier = Math.max((1F - (float) cos), 4F / (float) standDurability);
                                event.setAmount(event.getAmount() * multiplier);
                            }
                        }
                    }
                }
            });
        }
        // block other physical damage with stand
        else {
            standBlockUserAttack(dmgSource, target, stand -> {
                if (stand.isInvulnerableTo(dmgSource)) {
                    double standDurability = stand.getDurability();
                    if (standDurability > 0) {
                        stand.playAttackBlockSound();
                        float reducedDamage = Math.max(event.getAmount() - (float) standDurability / 2F, 0);
                        if (reducedDamage == 0) {
                            event.setCanceled(true);
                        }
                        else {
                            event.setAmount(reducedDamage);
                        }
                        NoKnockbackOnBlocking.setOneTickKbRes(stand);
                    }
                }
            });
        }
        if (event.isCanceled()) {
            return;
        }
        
        // block physical damage with hamon
        if (!dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) && dmgSource.getDirectEntity() != null) {
            INonStandPower.getNonStandPowerOptional(target).ifPresent(power -> {
                if (
                        target.getType() == ModEntityTypes.HAMON_MASTER.get() || 
                        power.getTypeSpecificData(ModPowers.HAMON.get()).map(HamonData::isProtectionEnabled).orElse(false)) {
                    float amount = ModHamonActions.HAMON_PROTECTION.get().reduceDamageAmount(
                            power, power.getUser(), dmgSource, event.getAmount());
                    event.setAmount(amount);
                }
                else {
                    HamonRebuffOverdrive.getCurRebuff(target).ifPresent(rebuff -> {
                        float amount = rebuff.reduceDamageAmount(dmgSource, event.getAmount());
                        event.setAmount(amount);
                    });
                }
            });
        }
    }
    
    @Nullable
    private static StandEntity getTargetStand(LivingEntity target) {
        return IStandPower.getStandPowerOptional(target).map(stand -> {
            return Optional.ofNullable(stand.getStandManifestation() instanceof StandEntity ? (StandEntity) stand.getStandManifestation() : null);
        }).orElse(Optional.empty()).orElse(null);
    }
    
    private static void standBlockUserAttack(DamageSource dmgSource, LivingEntity target, Consumer<StandEntity> standBehavior) {
        if (dmgSource.getDirectEntity() != null && dmgSource.getSourcePosition() != null) {
            StandEntity stand = getTargetStand(target);
            if (stand != null && stand.isFollowingUser() && stand.isStandBlocking()
                    && stand.canBlockDamage(dmgSource) && stand.canBlockOrParryFromAngle(dmgSource.getSourcePosition())) {
                standBehavior.accept(stand);
            }
        }
    }
//    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void preventDamagingArmor(LivingHurtEvent event) {
//        DamageSource dmgSource = event.getSource();
//        if (!dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) && dmgSource instanceof IModdedDamageSource
//                && ((IModdedDamageSource) dmgSource).preventsDamagingArmor()) {
//            dmgSource.bypassArmor();
//            LivingEntity target = event.getEntityLiving();
//            event.setAmount(CombatRules.getDamageAfterAbsorb(event.getAmount(), 
//                    (float) target.getArmorValue(), (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
//        }
//    }


    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void resolveOnTakingDamage(LivingDamageEvent event) {
        IStandPower.getStandPowerOptional(event.getEntity()).ifPresent(stand -> {
            if (stand.usesResolve()) {
                stand.getResolveCounter().onGettingAttacked(event.getSource(), event.getAmount(), event.getEntity());
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void reduceDamageFromResolve(LivingDamageEvent event) {
        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
            return;
        }
        float dmgReduction = IStandPower.getStandPowerOptional(event.getEntity()).map(stand -> {
            return stand.getResolveDmgReduction();
        }).orElse(0F);
        if (dmgReduction > 0F) {
            event.setAmount(event.getAmount() * (1 - dmgReduction));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource dmgSource = event.getSource();
        float dmgAmount = event.getAmount();
        LivingEntity target = event.getEntity();
        
        bleed(dmgSource, dmgAmount, target);
        StandType.onHurtByStand(dmgSource, dmgAmount, target);
        
        if (target instanceof StandEntity) {
            StandEntity standTarget = (StandEntity) target;
            if (standTarget.isCurrentAttackBlocked()) {
                NoKnockbackOnBlocking.setOneTickKbRes(standTarget);
            }
        }
        
        for (PowerClassification powerClassification : PowerClassification.values()) {
            IPower.getPowerOptional(target, powerClassification).ifPresent(power -> 
            power.onUserGettingAttacked(dmgSource, dmgAmount));
        }
        
        target.removeEffect(ModStatusEffects.HAMON_SHOCK.get());
    }


    @SubscribeEvent(receiveCanceled = true)
    public static void prepareToReduceKnockback(LivingHurtEvent event) {
        float knockbackReduction = DamageUtil.knockbackReduction(event.getSource());
        
        if (knockbackReduction >= 0 && knockbackReduction < 1) {
            event.getEntity().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
                util.setFutureKnockbackFactor(knockbackReduction);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityKnockback(LivingKnockBackEvent event) {
        event.getEntity().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
            if (util.shouldReduceKnockback()) {
                float factor = util.getKnockbackFactorOneTime();
                event.setStrength(event.getStrength() * factor);
            }
        });
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void stackKnockbackInstead(LivingKnockBackEvent event) {
        LivingEntity target = event.getEntity();
        
        if (!target.canUpdate()) {
            event.setCanceled(true);
            DamageUtil.applyKnockbackStack(target, event.getStrength(), event.getRatioX(), event.getRatioZ());
        }
    }

    public static void bleed(DamageSource dmgSource, float dmgAmount, LivingEntity target) {
        if (dmgSource instanceof StandLinkDamageSource) {
            dmgSource = ((StandLinkDamageSource) dmgSource).getOriginalDamageSource();
        }
        Level world = target.level;
        if (world.isClientSide()
                || dmgAmount < 0.98F
                || dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) && !dmgSource.is(net.minecraft.world.damagesource.DamageTypes.FALL)
                || dmgSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                || dmgSource.is(com.github.standobyte.jojo.init.ModTags.MAGIC)
                || dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_RESISTANCE)
                || dmgSource.getMsgId().startsWith(DamageUtil.PILLAR_MAN_ABSORPTION.location().getPath())
                || !JojoModUtil.canBleed(target)) return;

        
        IStandPower.getStandPowerOptional(target).ifPresent(power -> {
            if (ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get().isUnlocked(power)) {
                power.setCooldownTimer(ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get(), 0);
            }
        });
        
        splashBlood(world, target.getBoundingBox().getCenter(), 2, dmgAmount, Optional.of(target));
    }
    
    @Deprecated
    public static boolean splashBlood(Level world, Vec3 splashPos, double radius, 
            float bleedAmount, Optional<LivingEntity> ownerEntity) {
        return BleedingEffect.splashBlood(world, splashPos, radius, bleedAmount, OptionalInt.empty(), ownerEntity);
    }
    
    @Deprecated
    public static boolean applyStoneMask(LivingEntity entity, ItemStack headStack) {
        return BleedingEffect.applyStoneMask(entity, headStack);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionApply(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffect effect = event.getEffectInstance().getEffect();
        if (JojoModUtil.isDyingBody(entity)) {
            if (effect == MobEffects.HUNGER || effect == MobEffects.POISON || effect == MobEffects.REGENERATION) {
                event.setResult(Result.DENY);
            }
        }
        if (entity instanceof Player && JojoModUtil.isPlayerJojoVampiric((Player) entity)) {
            if (effect == MobEffects.HUNGER/* || effect == Effects.POISON */) {
                event.setResult(Result.DENY);
            }
            else if (effect == MobEffects.REGENERATION) {
                event.setResult(Result.ALLOW);
            }
        }
        if (effect instanceof IApplicableEffect && !((IApplicableEffect) effect).isApplicable(entity)) {
            event.setResult(Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void changePotionAmplifier(MobEffectEvent.Added event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance.getEffect() == ModStatusEffects.BLEEDING.get()) {
            int amplifier = BleedingEffect.limitAmplifier(event.getEffectSource() instanceof LivingEntity ? (LivingEntity) event.getEffectSource() : null, effectInstance.getAmplifier());
            if (amplifier != effectInstance.getAmplifier() && amplifier >= 0) {
                effectInstance.amplifier = amplifier;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectInstance = event.getEffectInstance();
        EntityStandType.giveEffectSharedWithStand(entity, effectInstance);
        
        if (!entity.level.isClientSide()) {
            if (ModStatusEffects.isEffectTracked(effectInstance.getEffect())) {
                ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, 
                        new ClientboundUpdateMobEffectPacket(entity.getId(), effectInstance));
            }
            if (entity instanceof ServerPlayer) {
                MobEffect effect = effectInstance.getEffect();
                if (effect == ModStatusEffects.RESOLVE.get()) {
                    PacketManager.sendToClient(new ResolveEffectStartPacket(effectInstance.getAmplifier()), (ServerPlayer) entity);
                }
                else if (effect == ModStatusEffects.SENSORY_OVERLOAD.get()) {
                    entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(PlayerUtilCap::setSendLifeshotNextTick);
                }
            }
            if (effectInstance.getEffect() == ModStatusEffects.BLEEDING.get()) {
                int effectLvl = effectInstance.getAmplifier();
                MobEffectInstance prevEffect = entity.getEffect(effectInstance.getEffect());
                if (prevEffect == null || prevEffect.getAmplifier() < effectLvl) {
                    BleedingEffect.onAddedBleeding(entity, effectLvl);
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelPotionRemoval(MobEffectEvent.Remove event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null) {
            LivingEntity entity = event.getEntity();
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                if (power.hasPower()) {
                    Iterable<MobEffect> effects = power.getType().getAllPossibleEffects();
                    MobEffect effect = event.getEffect();
                    if (Iterables.contains(effects, effect) && 
                            power.getType().getPassiveEffectLevel(effect, power) == effectInstance.getAmplifier() && 
                            !effectInstance.isVisible() && !effectInstance.showIcon()) {
                        event.setCanceled(true);
                    }
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void trackedPotionRemoved(MobEffectEvent.Remove event) {
        EntityStandType.removeEffectSharedWithStand(event.getEntity(), event.getEffect());
        
        Entity entity = event.getEntity();
        if (!entity.level.isClientSide() && event.getEffectInstance() != null && ModStatusEffects.isEffectTracked(event.getEffectInstance().getEffect())) {
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, 
                    new ClientboundRemoveMobEffectPacket(entity.getId(), event.getEffect()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void trackedPotionExpired(MobEffectEvent.Expired event) {
        EntityStandType.removeEffectSharedWithStand(event.getEntity(), event.getEffectInstance().getEffect());
        
        Entity entity = event.getEntity();
        if (!entity.level.isClientSide() && ModStatusEffects.isEffectTracked(event.getEffectInstance().getEffect())) {
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, 
                    new ClientboundRemoveMobEffectPacket(entity.getId(), event.getEffectInstance().getEffect()));
        }
    }
    
    @SubscribeEvent
    public static void syncTrackedEffects(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            LivingEntity tracked = (LivingEntity) event.getTarget();
            for (Map.Entry<MobEffect, MobEffectInstance> effectEntry : tracked.getActiveEffectsMap().entrySet()) {
                if (ModStatusEffects.isEffectTracked(effectEntry.getKey())) {
                    player.connection.send(new ClientboundUpdateMobEffectPacket(tracked.getId(), effectEntry.getValue()));
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!player.level.isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
            ServerLevel world = (ServerLevel) player.level;
            Entity target = event.getTarget();
            
            INonStandPower.getNonStandPowerOptional(player).resolve()
            .flatMap(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()))
            .filter(pillarMan -> pillarMan.getMode() == PillarmanData.Mode.HEAT).ifPresent(acdc -> {
                int fuse = -1;
                if (target instanceof PrimedTnt) {
                    PrimedTnt tnt = (PrimedTnt) target;
                    fuse = tnt.getFuse();
                    tnt.discard();
                }
                else if (target instanceof MinecartTNT) {
                    MinecartTNT tntMinecart = (MinecartTNT) target;
                    if (tntMinecart.isPrimed()) {
                        // mfw the getter is client-only
                        fuse = CommonReflection.getFuse(tntMinecart);
                        CompoundTag nbt = new CompoundTag();
                        tntMinecart.saveWithoutId(nbt);
                        nbt.putString("id", MCUtil.id(EntityType.MINECART).toString());
                        Entity regularMinecart = EntityType.loadEntityRecursive(nbt, world, e -> e);
                        tntMinecart.discard();
                        tntMinecart.discard();
                        if (regularMinecart != null) {
                            world.tryAddFreshEntityWithPassengers(regularMinecart);
                        }
                    }
                }
                
                if (fuse > -1) {
                    net.minecraft.util.RandomSource random = world.random;
                    world.playSound(null, 
                            player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 
                            1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
                    acdc.addEatenTntFuse(fuse);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void cancelChestOpenWhenPossessing(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (MCUtil.getGameMode(player) == GameType.SPECTATOR && IPlayerPossess.getPossessedEntity(player) != null) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public static void tripwireInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getUseBlock() != Event.Result.DENY) {
            Player player = event.getEntity();
            if (!player.isSpectator() && MCUtil.isHandFree(player, InteractionHand.MAIN_HAND)) {
                Level world = player.level;
                BlockPos pos = event.getHitVec().getBlockPos();
                BlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock() == Blocks.TRIPWIRE) {
                    INonStandPower.getNonStandPowerOptional(event.getEntity()).ifPresent(power -> {
                        power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                            if (hamon.isSkillLearned(ModHamonSkills.ROPE_TRAP.get())) {
                                event.setCanceled(true);
                                event.setCancellationResult(InteractionResult.SUCCESS);
                                if (!world.isClientSide()) {
                                    HamonRopeTrap.ropeTrap(player, pos, blockState, world, power, hamon);
                                }
                            }
                        });
                    });
                }
            }
        }
    }
    
    private static final int LIT_TICKS = 12000;
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void furnaceInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getUseBlock() != Event.Result.DENY) {
            Player player = event.getEntity();
            if (!player.isSpectator()) {
                Level world = player.level;
                BlockPos pos = event.getHitVec().getBlockPos();
                BlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock() instanceof AbstractFurnaceBlock) {
                    IStandPower.getStandPowerOptional(event.getEntity()).ifPresent(power -> {
                        if (power.isActive() && power.getType() == ModStands.MAGICIANS_RED.getStandType()) {
                            BlockEntity tileEntity = world.getBlockEntity(pos);
                            if (tileEntity instanceof AbstractFurnaceBlockEntity) {
                                AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) tileEntity;
                                int timeLeft = CommonReflection.getFurnaceLitTime(furnace);
                                if (timeLeft < LIT_TICKS) {
                                    CommonReflection.setFurnaceLitTime(furnace, LIT_TICKS);
                                    CommonReflection.setFurnaceLitDuration(furnace, LIT_TICKS);
                                    StandEntity magiciansRed = (StandEntity) power.getStandManifestation();
                                    magiciansRed.playSound(ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 1.0F, 1.0F, player);
                                    world.setBlock(pos, blockState.setValue(AbstractFurnaceBlock.LIT, true), 3);
                                }
                            }
                        }
                    });
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        IStandPower.getStandPowerOptional(player).ifPresent(stand -> {
            stand.getContinuousEffects().onStandUserLogout((ServerPlayer) player);
        });
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleCheatDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        DamageSource damageSource = event.getSource();
        if (!dead.level.isClientSide()) {
            cheatDeath(event);
            
            if (!event.isCanceled()) {
                if (damageSource instanceof IModdedDamageSource && ((IModdedDamageSource) damageSource).isNonLethal()) {
                    event.setCanceled(true);
                    event.getEntity().setHealth(0.0001f);
                }
            }
            
            if (!event.isCanceled()) {
                if (StandEffectsTracker.getEffectsTargetedBy(dead, ModStandEffects.TURN_INTO_ANGELO_ROCK.get())
                        .anyMatch(CDTurnIntoAngeloRockEffect::preventTargetDeath)) {
                    event.setCanceled(true);
                    event.getEntity().setHealth(0.0001f);
                }
            }
        }
    }
    
    // FIXME (!!!) corpse mod doesn't trigger (WHY)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void beforeLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead instanceof IPlayerPossess) {
            ((IPlayerPossess) dead).jojoOnPossessingDead();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        DamageSource dmgSource = event.getSource();
        if (!dead.level.isClientSide()) {
            HamonUtil.hamonPerksOnDeath(dead);
            Entity killer = dmgSource.getEntity();
            if (killer instanceof StandEntity) {
                StandEntity killerStand = (StandEntity) killer;
                if (killerStand.getUser() != null) {
                    killer = killerStand.getUser();
                }
            }
            if (!dead.is(killer)) {
                if (killer instanceof ServerPlayer) {
                    ModCriteriaTriggers.PLAYER_KILLED_ENTITY.get().trigger((ServerPlayer) killer, dead, dmgSource);
                    ModCriteriaTriggers.PLAYER_KILLED_PILLAR_MAN.get().trigger((ServerPlayer) killer, dead, dmgSource);
                }
                if (dead instanceof ServerPlayer && killer != null) {
                    ModCriteriaTriggers.ENTITY_KILLED_PLAYER.get().trigger((ServerPlayer) dead, killer, dmgSource);
                }
            }
            
            LazyOptional<IStandPower> standOptional = IStandPower.getStandPowerOptional(dead);
            standOptional.ifPresent(stand -> {
                stand.getContinuousEffects().onStandUserDeath(dead);
                stand.spawnSoulOnDeath();
            });
            

            LivingEntity killerCredited = dead.getKillCredit();
            if (killerCredited != null) {
                LivingEntity killerStandUser = StandUtil.getStandUser(killerCredited);
                if (!killerCredited.is(killerStandUser)) {
                    killerStandUser.awardKillScore(dead, 
                            0 /* the deathScore variable seems to be unused in vanilla, 
                                 and i don't feel like using reflection here */, 
                            dmgSource);
                }
            }
            
            MobEffectInstance freezeEffect = dead.getEffect(ModStatusEffects.FREEZE.get());
            if (freezeEffect != null && freezeEffect.getAmplifier() >= 3 && !(dead instanceof ServerPlayer)) {
                ((ServerLevel) dead.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), 
                        dead.getX(), dead.getY(0.5), dead.getZ(), 128, 
                        dead.getBbWidth() / 2, dead.getBbHeight() / 2, dead.getBbWidth() / 2, 0.25);
                SoundType soundtype = Blocks.ICE.defaultBlockState().getSoundType(dead.level, dead.blockPosition(), null);
                dead.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GLASS_BREAK, dead.getSoundSource(), 
                        (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            }
        }
    }

    private static void cheatDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.hasEffect(ModStatusEffects.CHEAT_DEATH.get())) {
            event.setCanceled(true);
            dead.setHealth(dead.getMaxHealth() / 2F);
            dead.removeEffect(ModStatusEffects.CHEAT_DEATH.get());
            dead.clearFire();
            ((ServerLevel) dead.level).sendParticles(ParticleTypes.POOF, dead.getX(), dead.getY(), dead.getZ(), 
                    20, (double) dead.getBbWidth() * 2D - 1D, (double) dead.getBbHeight(), (double) dead.getBbWidth() * 2D - 1D, 0.02D);
            dead.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, false, true));
            chorusFruitTeleport(dead);
            dead.level.getEntitiesOfClass(Mob.class, dead.getBoundingBox().inflate(8), 
                    mob -> mob.getTarget() == dead).forEach(mob -> MCUtil.loseTarget(mob, dead));
            INonStandPower.getNonStandPowerOptional(dead).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
                        if (dead instanceof ServerPlayer) {
                            ServerPlayer joseph = (ServerPlayer) dead;
                            sendMemeDeathMessage(joseph, event.getSource().getLocalizedDeathMessage(dead));
                            sendSoundToOnePlayer(joseph, ModSounds.JOSEPH_GIGGLE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                });
            });
        }
    }

    private static void chorusFruitTeleport(LivingEntity entity) {
        net.minecraft.util.RandomSource random = entity.getRandom();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        for (int i = 0; i < 16; ++i) {
            double xRandom = x + (random.nextDouble() - 0.5D) * 16.0D;
            double yRandom = Mth.clamp(y + (double) (random.nextInt(16) - 8), 0.0D, (double)(entity.level.getHeight() - 1));
            double zRandom = z + (random.nextDouble() - 0.5D) * 16.0D;
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            if (entity.randomTeleport(xRandom, yRandom, zRandom, false)) {
                break;
            }
        }
    }
    
    private static void sendMemeDeathMessage(ServerPlayer player, Component deathMessage) {
        if (player.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
            Team team = player.getTeam();
            if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                    player.server.getPlayerList().broadcastSystemToTeam(player, deathMessage);
                }
                else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                    player.server.getPlayerList().broadcastSystemToAllExceptTeam(player, deathMessage);
                }
            } else {
                player.server.getPlayerList().broadcastSystemMessage(deathMessage, false);
            }
        }
    }

    private static void sendSoundToOnePlayer(ServerPlayer player, SoundEvent sound, SoundSource category, float volume, float pitch) {
        net.minecraft.core.Holder<SoundEvent> soundHolder = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
        PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtEntity(player, soundHolder, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        soundHolder = event.getSound();
        category = event.getSource();
        volume = event.getOriginalVolume();
        player.connection.send(new ClientboundSoundPacket(soundHolder, category, player.getX(), player.getY(), player.getZ(), volume, pitch, player.level().getRandom().nextLong()));
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelXpDrop(LivingExperienceDropEvent event) {
        LivingEntity mob = event.getEntity();
        if (StandEffectsTracker.isTargetedBy(mob, ModStandEffects.GE_CREATED_LIFEFORM.get())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelHitSound(PlayLevelSoundEvent event) {
        SoundEvent sound = event.getSound().value();
        if (AngeloRockEntity.cancelPlayerHitSound && (sound == SoundEvents.PLAYER_ATTACK_STRONG || sound == SoundEvents.PLAYER_ATTACK_WEAK/* || sound == SoundEvents.PLAYER_ATTACK_KNOCKBACK*/ /* is played before AngeloRockEntity#hurt is called so nope */)) {
            AngeloRockEntity.cancelPlayerHitSound = false;
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void changeLooting(LootingLevelEvent event) {
        LivingEntity usePickaxeFortune = AngeloRockEntity.mobLootFortune;
        if (usePickaxeFortune != null && event.getEntity() == usePickaxeFortune) {
            Entity killer = event.getDamageSource().getEntity();
            if (killer instanceof LivingEntity) {
                int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE, (LivingEntity) killer);
                event.setLootingLevel(Math.max(event.getLootingLevel(), fortune));
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileShot(EntityJoinLevelEvent event) {
        HamonUtil.chargeNewEntity(event.getEntity(), event.getLevel());
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileHit(ProjectileImpactEvent event) {
        HamonUtil.onProjectileImpact(event.getEntity(), event.getRayTraceResult());
    }
    
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        if (explosion.getExploder() instanceof Creeper) {
            Creeper creeper = (Creeper) explosion.getExploder();
            Collection<MobEffect> effects = new ArrayList<>(creeper.getActiveEffectsMap().keySet());
            effects.forEach(effect -> {
                if (effect == ModStatusEffects.BLEEDING.get() || effect instanceof StatusEffect && ((StatusEffect) effect).isUncurable()) {
                    creeper.removeEffect(effect);
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosionDetonate2(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        
        event.getAffectedEntities().forEach(entity -> {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
                    util.setLatestExplosion(explosion);
                });
            }
        });
        
        HamonUtil.hamonChargedCreeperBlast(explosion, event.getLevel());
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemThrown(ItemTossEvent event) {
        HamonPlantItemInfusion.chargeItemEntity(event.getPlayer(), event.getEntity());
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getDistance() > 3) {
            LivingEntity entity = event.getEntity();
            float leapStrength = Math.max(
                    IStandPower.getStandPowerOptional(entity).map(power -> 
                    power.hasPower() && power.isLeapUnlocked() ? power.leapStrength() : 0).orElse(0F), 
                    INonStandPower.getNonStandPowerOptional(entity).map(power -> 
                    power.hasPower() && power.isLeapUnlocked() ? power.leapStrength() : 0).orElse(0F));
            if (leapStrength > 0) {
                event.setDistance(Math.max(event.getDistance() - (leapStrength + 5) * 3, 0));
            }
        }
    }
    
    private static Component getDisplayNameWithUser(StandEntity stand, Player user) {
        return PlayerTeam.formatNameForTeam(stand.getTeam(), stand.getName()).withStyle(style -> {
            return style
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, 
                            new HoverEvent.EntityTooltipInfo(stand.getType(), stand.getUUID(), 
                                    Component.translatable("chat.stand_remote_reveal_name", stand.getName(), user.getName()))))
                    .withInsertion(user.getGameProfile().getName());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onChatMessage(ServerChatEvent event) {
        boolean messageAsStand = messageAsStand(event);
        if (messageAsStand) {
            event.setCanceled(true);
        }
        
        List<ServerPlayer> josephTechniqueUsers = MCUtil.entitiesAround(ServerPlayer.class, event.getPlayer(), 8, false, 
                pl -> (!messageAsStand || StandUtil.playerCanHearStands(pl)) &&
                INonStandPower.getNonStandPowerOptional(pl).map(power -> 
                power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> 
                hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())).orElse(false)).orElse(false));
        for (ServerPlayer joseph : josephTechniqueUsers) {
            if (joseph.getChatVisibility() != ChatVisiblity.HIDDEN && joseph.getRandom().nextFloat() < 0.05F) {
                String tlKey = "jojo.chat.joseph.next_line." + (joseph.getRandom().nextInt(3) + 1);
                // 1.20.1 fires the chat event on the server side, so the message is used as built
                Component message = Component.translatable("chat.type.text", joseph.getDisplayName(), 
                        Component.translatable(tlKey, event.getMessage()));
                {
                    JojoModUtil.sayVoiceLine(joseph, ModSounds.JOSEPH_GIGGLE.get());
                    joseph.server.getPlayerList().broadcastSystemMessage(message, false);
                }
            }
        }
        
        IStandPower.getStandPowerOptional(event.getPlayer()).ifPresent(stand -> stand.getResolveCounter().onChatMessage(event.getMessage().getString()));
    }

    private static final double STAND_MESSAGE_RANGE = 16;
    private static boolean messageAsStand(ServerChatEvent event) {
        ServerPlayer playerSending = event.getPlayer();
        return GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(playerSending), stand -> {
            if (stand.hasPower() && stand.isActive() && stand.getStandManifestation() instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) stand.getStandManifestation();
                if (standEntity.isManuallyControlled()) {
                    MinecraftServer server = playerSending.server;
                    
                    Component msg = Component.translatable("chat.type.text", 
                            standEntity.getDisplayName(), event.getMessage());
                    Component msgUserTooltip = Component.translatable("chat.type.text", 
                            getDisplayNameWithUser(standEntity, playerSending), event.getMessage());
                    ClientboundSystemChatPacket messagePacket = new ClientboundSystemChatPacket(msg, false);
                    ClientboundSystemChatPacket messagePacketUser = new ClientboundSystemChatPacket(msgUserTooltip, false);
                    
                    // 1.20.1 cannot forge a signed player chat message, so the original
                    // text is broadcast as a system message
                    server.getPlayerList().broadcastSystemMessage(event.getMessage(), false);
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        if (player == playerSending || 
                                player.level.dimension() == playerSending.level.dimension() 
                                && player.position().subtract(standEntity.position()).lengthSqr() < STAND_MESSAGE_RANGE * STAND_MESSAGE_RANGE
                                && (StandUtil.playerCanHearStands(player) || standEntity.isVisibleForAll())) {
                            player.connection.send(server.getProfilePermissions(player.getGameProfile()) >= 3 ? messagePacketUser : messagePacket);
                        }
                    }
                    
                    playerSending.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(
                            cap -> cap.onChatMsgBypassingSpamCheck(server, playerSending));
                    
                    return true;
                }
            }
            return false;
        });
    }
    
    @SubscribeEvent
    public static void onMobInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        InteractionHand hand = event.getHand();
        ItemStack item = player.getItemInHand(hand);
        
        if (target.isAlive() && target instanceof Cow && (
                item.getItem() == Items.BUCKET
                || item.getItem() == Items.BOWL && target instanceof MushroomCow)) {
            Cow cow = (Cow) target;
            if (cow.getLeashHolder() != player && !cow.isBaby()) {
                Optional<List<MobEffectInstance>> potion = cow.getCapability(LivingUtilCapProvider.CAPABILITY).resolve()
                        .map(cap -> cap.getProductEffects());
                
                if (potion.isPresent()) {
                    if (item.getItem() == Items.BUCKET) {
                        player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
                        ItemStack milkBucketItem = ItemUtils.createFilledResult(item, player, 
                                PotionUtils.setCustomEffects(Items.MILK_BUCKET.getDefaultInstance(), potion.get()));
                        milkBucketItem.getOrCreateTag().putBoolean(MOD_ADDS_EFFECTS_TO_ITEM, true);
                        player.setItemInHand(hand, milkBucketItem);
                    }
                    else {
                        ItemStack stewItem;
                        MushroomCow mooshroomEntity = (MushroomCow) target;
                        MobEffect susEffect = CommonReflection.getEffect(mooshroomEntity);
                        if (susEffect != null) {
                            stewItem = new ItemStack(Items.SUSPICIOUS_STEW);
                            int duration = CommonReflection.getEffectDuration(mooshroomEntity);
                            SuspiciousStewItem.saveMobEffect(stewItem, susEffect, duration);
                            CommonReflection.clearEffect(mooshroomEntity);
                        } else {
                            stewItem = new ItemStack(Items.MUSHROOM_STEW);
                        }
                        
                        PotionUtils.setCustomEffects(stewItem, potion.get());
                        ItemStack stewBowlItem = ItemUtils.createFilledResult(item, player, stewItem, false);
                        stewItem.getOrCreateTag().putBoolean(MOD_ADDS_EFFECTS_TO_ITEM, true);
                        player.setItemInHand(hand, stewBowlItem);
                        
                        target.playSound(susEffect != null ? SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY : SoundEvents.MOOSHROOM_MILK, 1.0F, 1.0F);
                    }
                    
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.sidedSuccess(player.level.isClientSide()));
                }
            }
        }
    }
    
    public static final String MOD_ADDS_EFFECTS_TO_ITEM = "JojoItemUseEffects";
    @SubscribeEvent
    public static void usePotionCowProduct(LivingEntityUseItemEvent.Finish event) {
        ItemStack item = event.getItem();
        LivingEntity entity = event.getEntity();
        if (!item.isEmpty() && item.hasTag() && item.getTag().getBoolean(MOD_ADDS_EFFECTS_TO_ITEM)) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(item);
            if (!effects.isEmpty()) {
                effects.forEach(effect -> entity.addEffect(effect));
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAnimalOffspring(BabyEntitySpawnEvent event) {
        List<MobEffectInstance> effectsA = event.getParentA().getCapability(LivingUtilCapProvider.CAPABILITY).resolve().map(
                cap -> cap.getProductEffects()).orElse(null);
        List<MobEffectInstance> effectsB = event.getParentB().getCapability(LivingUtilCapProvider.CAPABILITY).resolve().map(
                cap -> cap.getProductEffects()).orElse(null);
        boolean hasA = effectsA != null && !effectsA.isEmpty();
        boolean hasB = effectsB != null && !effectsB.isEmpty();
        if (hasA || hasB) {
            List<MobEffectInstance> effectsRes = new ArrayList<>();
            if (hasA) {
                Map<MobEffect, MobEffectInstance> effectsMap = effectsA.stream()
                        .collect(Collectors.toMap(MobEffectInstance::getEffect, Function.identity(), 
                                (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); }, 
                                HashMap::new));
                for (MobEffectInstance effectB : effectsB) {
                    MobEffect effectKey = effectB.getEffect();
                    MobEffectInstance effectA = effectsMap.get(effectKey);
                    if (effectA != null) {
                        MobEffectInstance effectSum = new MobEffectInstance(effectKey, 
                                Math.max(effectA.getDuration(), effectB.getDuration()),
                                Math.max(effectA.getAmplifier(), effectB.getAmplifier()));
                        effectsMap.put(effectKey, effectSum);
                    }
                    else {
                        effectsMap.put(effectKey, effectB);
                    }
                }
                effectsRes.addAll(effectsMap.values());
            }
            else {
                effectsRes.addAll(effectsB);
            }
            
            event.getChild().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.setProductEffects(effectsRes);
            });
        }
    }
    
    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        
        if (!event.wakeImmediately() && !event.updateLevel()) {
            IStandPower.getStandPowerOptional(player).ifPresent(stand -> {
                if (stand.hasPower()) {
                    stand.setStamina(stand.getMaxStamina());
                }
            });
        }
        
        VampirismData.finishCuringOnWakingUp(player);
        
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(
                playerData -> playerData.onWakeUp());
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getLevel().isClientSide()) {
            Level world = (Level) event.getLevel();
            BlockPos pos = event.getPos();
            
            int xp = event.getExpToDrop();
            if (xp > 0) {
                ChunkAccess chunk = world.getChunk(pos);
                if (chunk instanceof LevelChunk) {
                    ((LevelChunk) chunk).getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.setDroppedXp(pos, xp);
                    });
                }
            }
            
            if (event.getPlayer().abilities.instabuild) {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, 
                        pos, event.getState(), Optional.ofNullable(world.getBlockEntity(pos)), 
                        Collections.emptyList());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getNewGameMode() == GameType.CREATIVE) {
            Player player = event.getEntity();
            INonStandPower.getNonStandPowerOptional(event.getEntity()).ifPresent(power -> power.resetCooldowns());
            IStandPower.getStandPowerOptional(event.getEntity()).ifPresent(stand -> stand.resetCooldowns());
            player.removeEffect(ModStatusEffects.IMMOBILIZE.get());
            player.removeEffect(ModStatusEffects.STUN.get());
            player.removeEffect(ModStatusEffects.HAMON_SHOCK.get());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMobSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getResult() != Event.Result.DENY && event.getEntity().getType() == EntityType.TURTLE
                && JojoModConfig.getCommonConfigInstance(false).spawnCocoJumboTurtle.get()) {
            CocoJumboTurtleEntity.onRegularTutelSpawn(event);
        }
    }
    
    @SubscribeEvent
    public static void anvilUnrepairableItems(AnvilUpdateEvent event) {
        GlovesItem.combineInAnvil(event);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (IPlayerPossess.getPossessedEntity(entity) != null) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelOtherDimensionTeleport(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if (IPlayerPossess.getPossessedEntity(entity) != null) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getCurrentGameMode() == GameType.SPECTATOR || event.getNewGameMode() != GameType.SPECTATOR) {
            Entity entity = event.getEntity();
            if (entity instanceof IPlayerPossess) {
                IPlayerPossess player = (IPlayerPossess) entity;
                if (player.jojoGetPossessedEntity() != null) {
                    player.jojoSetPrePossessGameMode(Optional.of(event.getNewGameMode()));
                    event.setCanceled(true);
                }
            }
        }
    }
}