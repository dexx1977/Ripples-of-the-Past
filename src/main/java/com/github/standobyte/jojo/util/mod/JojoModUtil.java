package com.github.standobyte.jojo.util.mod;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModGamerules;
import com.github.standobyte.jojo.init.ModTags;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.item.ClothesSet;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlayVoiceLinePacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.PlayLevelSoundEvent;

public class JojoModUtil {
    
    public static HitResult rayTrace(Entity entity, double reachDistance, @Nullable Predicate<Entity> entityFilter) {
        return rayTrace(entity, reachDistance, entityFilter, 0);
    }

    public static HitResult rayTrace(Entity entity, double reachDistance, @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate) {
        return rayTrace(entity, reachDistance, entityFilter, rayTraceInflate, 0);
    }

    public static HitResult rayTrace(Entity entity, double reachDistance, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(entity, reachDistance, entityFilter, rayTraceInflate, standPrecision)[0];
    }
    
    public static HitResult[] rayTraceMultipleEntities(Entity entity, double reachDistance, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(entity.getEyePosition(1.0F), entity.getViewVector(1.0F), reachDistance, 
                entity.level, entity, 
                entityFilter, ClipContext.Block.OUTLINE, 
                rayTraceInflate, standPrecision);
    }
    
    public static HitResult[] rayTraceMultipleEntities(Entity entity, double reachDistance, 
            @Nullable Predicate<Entity> entityFilter, ClipContext.Block blockMode, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(entity.getEyePosition(1.0F), entity.getViewVector(1.0F), reachDistance, 
                entity.level, entity, entityFilter, blockMode, rayTraceInflate, standPrecision);
    }
    
    public static HitResult[] rayTraceMultipleEntities(Vec3 startPos, Vec3 rayVec, double distance, 
            Level world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, ClipContext.Block blockMode, 
            double rayTraceInflate, double standPrecision) {
        Vec3 rtVec = rayVec.normalize().scale(distance);
        Vec3 endPos = startPos.add(rtVec);
        AABB aabb = entity.getBoundingBox().expandTowards(rtVec).inflate(1.0D);
        return rayTraceMultipleEntities(startPos, endPos, aabb, 
                distance, entity.level, entity, entityFilter, blockMode, 
                rayTraceInflate, standPrecision);
    }

    public static HitResult rayTrace(Vec3 startPos, Vec3 rayVec, double distance, 
            Level world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(startPos, rayVec, distance, 
                world, entity, 
                entityFilter, ClipContext.Block.OUTLINE, 
                rayTraceInflate, standPrecision)[0];
    }

    public static HitResult[] rayTraceMultipleEntities(Vec3 startPos, Vec3 endPos, AABB aabb, 
            double minDistance, Level world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(startPos, endPos, aabb, 
                minDistance, world, entity, 
                entityFilter, ClipContext.Block.OUTLINE, 
                rayTraceInflate, standPrecision);
    }

    public static HitResult[] rayTraceMultipleEntities(Vec3 startPos, Vec3 endPos, AABB aabb, 
            double minDistance, Level world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, ClipContext.Block blockMode, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(startPos, endPos, aabb, 
                minDistance, world, entity, 
                entityFilter, true, blockMode, 
                rayTraceInflate, standPrecision);
    }

    public static HitResult[] rayTraceMultipleEntities(Vec3 startPos, Vec3 endPos, AABB aabb, 
            double minDistance, Level world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, boolean checkPickable, ClipContext.Block blockMode, 
            double rayTraceInflate, double standPrecision) {
        aabb.inflate(rayTraceInflate);
        double minDistanceSqr = minDistance * minDistance;
        Map<EntityHitResult, Double> rayTracedWithDistance = new HashMap<>();
        List<Entity> entities = world.getEntities(entity, aabb, e -> (!checkPickable || !e.isSpectator() && e.isPickable()) && (entityFilter == null || entityFilter.test(e)));
        for (Entity potentialTarget : entities) {
            AABB targetCollisionAABB = potentialTarget.getBoundingBox().inflate((double) potentialTarget.getPickRadius() + rayTraceInflate);
            targetCollisionAABB = standPrecisionTargetHitbox(targetCollisionAABB, standPrecision);
            Optional<Vec3> clipOptional = targetCollisionAABB.clip(startPos, endPos);
            if (targetCollisionAABB.contains(startPos)) {
                rayTracedWithDistance.put(new EntityHitResult(potentialTarget, clipOptional.orElse(startPos)), 0.0);
            } else if (clipOptional.isPresent()) {
                Vec3 clipVec = clipOptional.get();
                double clipDistanceSqr = startPos.distanceToSqr(clipVec);
                if (clipDistanceSqr < minDistanceSqr || minDistanceSqr == 0.0D) {
                    if (entity != null && potentialTarget.getRootVehicle() == entity.getRootVehicle() && !potentialTarget.canRiderInteract()) {
                        if (minDistanceSqr == 0.0D) {
                            rayTracedWithDistance.put(new EntityHitResult(potentialTarget, clipVec), 0.0);
                        }
                    } else {
                        rayTracedWithDistance.put(new EntityHitResult(potentialTarget, clipVec), clipDistanceSqr);
                    }
                }
            }
        }
        if (rayTracedWithDistance.isEmpty()) {
            return new HitResult[] { 
                    world.clip(new ClipContext(startPos, endPos, blockMode, ClipContext.Fluid.NONE, entity))
                    };
        }
        return rayTracedWithDistance.entrySet().stream()
        .sorted(Comparator.comparingDouble(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .toArray(EntityHitResult[]::new);
        
//        return new EntityRayTraceResult(targetEntity, targetEntityPos);
    }
    
    private static AABB standPrecisionTargetHitbox(AABB aabb, double precision) {
        if (precision > 4) {
            double smallAabbAddFraction = Math.min(Math.pow(2, precision - 16), 1);
            aabb.inflate(
                    Math.max(1.0 - aabb.getXsize(), 0) * smallAabbAddFraction, 
                    Math.max(1.0 - aabb.getYsize(), 0) * smallAabbAddFraction, 
                    Math.max(1.0 - aabb.getZsize(), 0) * smallAabbAddFraction);
            
            double scale = precision / 5 + 0.2;
            double xSize = aabb.getXsize();
            double ySize = aabb.getYsize();
            double zSize = aabb.getZsize();
            aabb = MCUtil.scale(aabb, 
                    Math.min(scale, 1 + 4 / xSize), 
                    Math.min(scale, 1 + 4 / ySize), 
                    Math.min(scale, 1 + 4 / zSize));
        }
        return aabb;
    }

    public static HitResult getHitResult(Entity projectile, @Nullable Predicate<Entity> targetPredicate, ClipContext.Block blockMode) {
        Level world = projectile.level;
        Vec3 pos = projectile.position();
        Vec3 nextPos = pos.add(projectile.getDeltaMovement());
        HitResult rayTraceResult = world.clip(new ClipContext(pos, nextPos, blockMode, ClipContext.Fluid.NONE, projectile));
        if (rayTraceResult.getType() != HitResult.Type.MISS) {
            nextPos = rayTraceResult.getLocation();
        }
        HitResult entityRayTraceResult = ProjectileUtil.getEntityHitResult(world, projectile, pos, nextPos, 
                projectile.getBoundingBox().expandTowards(projectile.getDeltaMovement()).inflate(1.0D), targetPredicate);
        if (entityRayTraceResult != null) {
            rayTraceResult = entityRayTraceResult;
        }
        return rayTraceResult;
    }
    
    // Item.getPlayerPOVHitResult
    protected static BlockHitResult getPlayerPOVHitResult(Level world, LivingEntity entity, ClipContext.Fluid fluidMode) {
        float xRot = entity.xRot;
        float yRot = entity.yRot;
        Vec3 eyePos = entity.getEyePosition(1.0F);
        float f2 = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-xRot * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-xRot * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double distance = Optional.ofNullable(entity.getAttribute(ForgeMod.ENTITY_REACH.get())).map(AttributeInstance::getValue).orElse(5D);
        Vec3 vector3d1 = eyePos.add((double)f6 * distance, (double)f5 * distance, (double)f7 * distance);
        return world.clip(new ClipContext(eyePos, vector3d1, ClipContext.Block.OUTLINE, fluidMode, entity));
    }

    public static boolean isAnotherEntityTargeted(HitResult rayTraceResult, Entity targettingEntity) {
        return (rayTraceResult.getType() == HitResult.Type.ENTITY
                && !((EntityHitResult) rayTraceResult).getEntity().is(targettingEntity));
    }

    public static double getDistance(Entity entity, AABB targetAabb) {
        Vec3 startPos = entity.getEyePosition(1.0F);
        if (targetAabb.contains(startPos)) {
            return 0;
        }
        Vec3 endPos = new Vec3(
                Mth.lerp(0.5D, targetAabb.minX, targetAabb.maxX), 
                Mth.lerp(entity.getBbHeight() == 0 ? 0 : 
                    entity.getEyeHeight() / entity.getBbHeight(), targetAabb.minY, targetAabb.maxY), 
                Mth.lerp(0.5D, targetAabb.minZ, targetAabb.maxZ));
        Optional<Vec3> clipOptional = targetAabb.clip(startPos, endPos);
        return clipOptional.map(clipVec -> startPos.distanceTo(clipVec) - entity.getBbWidth() / 2).orElse(-1D);
    }



    public static boolean canEntityDestroy(ServerLevel world, BlockPos blockPos, BlockState blockState, LivingEntity entity) {
        if (breakingBlocksEnabled(world)
                && blockState.canEntityDestroy(world, blockPos, entity)
                && ForgeEventFactory.onEntityDestroyBlock(entity, blockPos, blockState)) {
            Player player = null;
            if (entity instanceof Player) {
                player = (Player) entity;
            }
            else if (entity instanceof StandEntity) {
                LivingEntity standUser = ((StandEntity) entity).getUser();
                if (standUser instanceof Player) {
                    player = (Player) standUser;
                }
            }
            return player == null || world.mayInteract(player, blockPos);
        }
        return false;
    }
    
    public static boolean breakingBlocksEnabled(Level world) {
        return world.getGameRules().getBoolean(ModGamerules.BREAK_BLOCKS);
    }



    public static ResourceLocation makeTextureLocation(String folderName, String namespace, String path) {
        return makeTextureLocation(folderName, namespace, path, true);
    }

    public static ResourceLocation makeTextureLocation(String folderName, String namespace, String path, boolean addPngExtension) {
        return new ResourceLocation(namespace, "textures/"+ folderName + "/" + path + (addPngExtension ? ".png" : ""));
    }
    
    
    
    public static boolean playerHasClientInput(Player player) {
        return player.level.isClientSide() ? InputHandler.getInstance().hasInput
                : player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::hasClientInput).orElse(false);
    }
    
    
    
    /**
     * In case the player temporarily has a specific game mode for gameplay reasons 
     * (e.g. spectator mode when possessing an entity)
     */
    @Nullable
    public static Optional<GameType> getActualGameModeWhilePossessing(Player player) {
        if (player instanceof IPlayerPossess) {
            IPlayerPossess possessing = (IPlayerPossess) player;
            if (possessing.jojoGetPossessedEntity() != null) {
                return possessing.jojoGetPrePossessGameMode();
            }
        }
        return Optional.empty();
    }
    
    public static GameType getGameModeConsiderPossessing(Player player) {
        return getActualGameModeWhilePossessing(player).orElse(MCUtil.getGameMode(player));
    }
    
    public static boolean seesInvisibleAsSpectator(Player player) {
        return getActualGameModeWhilePossessing(player).map(gameMode -> gameMode == GameType.SPECTATOR).orElse(player.isSpectator());
    }
    
    // TODO let players possessing other entities use power HUD and certain abilities, depending on the possession context
    public static boolean tmpSpectatorCantUsePowers(LivingEntity entity) {
        return entity.isSpectator();
    }
    


    @Deprecated
    public static boolean isUndead(LivingEntity entity) {
        return isUndeadOrVampiric(entity);
    }

    @Deprecated
    public static boolean isPlayerUndead(Player player) {
        return isPlayerJojoVampiric(player);
    }

    public static boolean isUndeadOrVampiric(LivingEntity entity) {
        if (entity.getMobType() == MobType.UNDEAD) {
            return true;
        }
        if (entity instanceof Player) {
            return isPlayerJojoVampiric((Player) entity);
        }
        return false;
    }
    
    /** 
     * You don't have to call this, it's just a condition to change the PlayerEntity's getMobType() to CreatureAttribute.UNDEAD via a mixin
     */
    public static boolean playerUndeadAttribute(LivingEntity player) {
        return INonStandPower.getNonStandPowerOptional(player).map(power -> {
            NonStandPowerType<?> powerType = power.getType();
            return powerType == ModPowers.VAMPIRISM.get() || powerType == ModPowers.ZOMBIE.get();
        }).orElse(false);
    }
    
    /**
     * Is treated differently from the conventional vanilla "undead"
     */
    public static boolean isPlayerJojoVampiric(Player player) {
        return INonStandPower.getNonStandPowerOptional(player).map(power -> {
            NonStandPowerType<?> powerType = power.getType();
            return powerType == ModPowers.VAMPIRISM.get() || powerType == ModPowers.PILLAR_MAN.get();
        }).orElse(false); 
    }

    public static boolean isAffectedByHamon(LivingEntity entity) {
        if (entity.getType().builtInRegistryHolder().is(ModTags.NO_HAMON_DAMAGE)) {
            return false;
        }
        if (entity.getType().builtInRegistryHolder().is(ModTags.HAMON_DAMAGE)) {
            return true;
        }
        return JojoModUtil.isUndeadOrVampiric(entity) || OptionalDependencyHelper.vampirism().isEntityVampire(entity);
    }

    public static boolean isDyingBody(LivingEntity entity) {
        return entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(LivingUtilCap::isDyingBody).orElse(false);
    }

    public static boolean canBleed(LivingEntity entity) {
        if (entity.getMobType() == MobType.UNDEAD) {
            return entity instanceof Player
                    || entity instanceof Zombie && !(entity instanceof Husk)
                    || entity instanceof Zoglin
                    || entity instanceof ZombieHorse;
        }
        if (isDyingBody(entity)) {
            return false;
        }
        return entity instanceof Player
                || entity instanceof AgeableMob
                || entity instanceof Npc
                || entity instanceof AbstractIllager
                || entity instanceof WaterAnimal
                || entity instanceof IMobStandUser;
    }

    public static void extinguishFieryStandEntity(Entity entity, ServerLevel world) {
        MCUtil.playSound(world, null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.FIRE_EXTINGUISH, entity.getSoundSource(), 1.0F, 1.0F, StandUtil::playerCanHearStands);
        world.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 
                8, entity.getBbWidth() / 2F, entity.getBbHeight() / 2F, entity.getBbWidth() / 2F, 0);
        entity.discard();
    }
    
    @Deprecated
    public static void deflectProjectile(Entity projectile, @Nullable Vec3 deflectVec) {
        deflectProjectile(projectile, deflectVec, null);
    }
    
    public static void deflectProjectile(Entity projectile, @Nullable Vec3 deflectVec, @Nullable Vec3 deflectPos) {
        if (deflectVec == null) {
            deflectVec = projectile.getDeltaMovement().reverse();
        }
        projectile.setDeltaMovement(deflectVec);
        projectile.move(MoverType.SELF, projectile.getDeltaMovement());
        deflectVec = projectile.getDeltaMovement();
        if (!projectile.level.isClientSide() && projectile instanceof ModdedProjectileEntity) {
            if (deflectPos == null) deflectPos = projectile.position();
            ((ModdedProjectileEntity) projectile).setIsDeflected(deflectVec, deflectPos);
        }
    }
    
    public static boolean isTargetBlocking(LivingEntity target) {
        ItemStack usedItem = target.getUseItem();
        if (!usedItem.isEmpty() && target.isBlocking()) {
            return true;
        }
        if (target instanceof StandEntity) {
            StandEntity stand = (StandEntity) target;
            if (stand.isBlocking()) {
                return true;
            }
        }
        return false;
    }
    
    
    
    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine) {
        sayVoiceLine(entity, voiceLine, null);
    }
    
    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine, int voiceLineDelay) {
        sayVoiceLine(entity, voiceLine, null, 1.0F, 1.0F, voiceLineDelay, false);
    }

    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine, 
            @Nullable ClothesSet character) {
        sayVoiceLine(entity, voiceLine, character, 1.0F, 1.0F, false);
    }

    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine, 
            @Nullable ClothesSet character, boolean interrupt) {
        sayVoiceLine(entity, voiceLine, character, 1.0F, 1.0F, interrupt);
    }

    public static void sayVoiceLine(LivingEntity entity, SoundEvent sound, 
            @Nullable ClothesSet character, float volume, float pitch, boolean interrupt) {
        sayVoiceLine(entity, sound, character, volume, pitch, 200, interrupt);
    }

    public static boolean sayVoiceLine(LivingEntity entity, SoundEvent sound, 
            @Nullable ClothesSet character, float volume, float pitch, int voiceLineDelay, boolean interrupt) {
        if (entity.level.isClientSide() || entity.hasEffect(MobEffects.INVISIBILITY) ||
                character != null && character != ClothesSet.getClothesSet(entity)) {
            return false;
        }
        SoundSource category = SoundSource.VOICE;
        boolean triggered = false;
        if (entity instanceof Player) {
            PlayVoiceLinePacket packet;
            if (!canPlayVoiceLine((Player) entity, sound, voiceLineDelay)) {
                packet = PlayVoiceLinePacket.notTriggered(entity.getId());
            }
            else {
                PlayLevelSoundEvent event = ForgeEventFactory.onPlaySoundAtPosition(entity.level, entity.getX(), entity.getY(), entity.getZ(), 
                        net.minecraft.core.Holder.direct(sound), category, volume, pitch);
                if (event.isCanceled() || event.getSound() == null) {
                    packet = PlayVoiceLinePacket.notTriggered(entity.getId());
                }
                else {
                    sound = event.getSound().value();
                    category = event.getSource();
                    volume = event.getOriginalVolume();
                    packet = new PlayVoiceLinePacket(sound, category, entity.getId(), volume, pitch, interrupt);
                    triggered = true;
                }
            }
            PacketManager.sendToNearby(packet, null, entity.getX(), entity.getY(), entity.getZ(), 
                    volume > 1.0F ? (double) (16.0F * volume) : 16.0D, entity.level.dimension());
            return triggered;
        }
        else {
            entity.level.playSound(null, entity, sound, category, volume, pitch);
            return true;
        }
    }

    private static boolean canPlayVoiceLine(Player entity, SoundEvent voiceLine, int voiceLineDelay) {
        return entity.getCapability(PlayerUtilCapProvider.CAPABILITY)
                .map(cap -> cap.checkNotRepeatingVoiceLine(voiceLine, voiceLineDelay)).orElse(true);
    }
    
    
    @Deprecated
    public static boolean useShiftVar(LivingEntity user) {
        return user.isShiftKeyDown();
    }
    
    


    public static enum Direction2D {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }
}
