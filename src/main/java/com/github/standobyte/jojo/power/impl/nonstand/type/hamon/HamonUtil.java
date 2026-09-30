package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.non_stand.HamonLiquidWalking;
import com.github.standobyte.jojo.action.non_stand.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.non_stand.HamonPlantItemInfusion;
import com.github.standobyte.jojo.action.non_stand.HamonRopeTrap;
import com.github.standobyte.jojo.action.non_stand.HamonSnakeMuffler;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.power.bowcharge.BowChargeEffectInstance;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mc.damage.explosion.HamonBlastExplosion;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.Util;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class HamonUtil {
    
    @Deprecated
    public static void chargeItemEntity(Player throwerPlayer, ItemEntity itemEntity) {
        HamonPlantItemInfusion.chargeItemEntity(throwerPlayer, itemEntity);
    }
    
    @Deprecated
    public static boolean onLiquidWalkingEvent(LivingEntity entity, FluidState fluidState) {
        return HamonLiquidWalking.onLiquidWalkingEvent(entity, fluidState);
    }
    
    @Deprecated
    public static boolean ropeTrap(LivingEntity user, BlockPos pos, BlockState blockState, Level world, INonStandPower power, HamonData hamon) {
        return HamonRopeTrap.ropeTrap(user, pos, blockState, world, power, hamon);
    }
    
    @Deprecated
    public static boolean snakeMuffler(LivingEntity target, DamageSource dmgSource, float dmgAmount) {
        return HamonSnakeMuffler.snakeMuffler(target, dmgSource, dmgAmount);
    }
    
    
    
    public static void chargeNewEntity(Entity entity, Level world) {
        if (!world.isClientSide()) {
            if (entity instanceof Projectile) {
                Projectile projectile = (Projectile) entity;
                if (projectile.getOwner() instanceof LivingEntity) {
                    LivingEntity shooter = (LivingEntity) projectile.getOwner();
                    if (projectile instanceof AbstractArrow) {
                        // TODO stand effects on arrows
                        IStandPower.getStandPowerOptional(shooter).ifPresent(stand -> {
                            BowChargeEffectInstance<?, ?> bowCharge = stand.getBowChargeEffect();
                            if (bowCharge != null) {
                                bowCharge.onArrowShot((AbstractArrow) projectile);
                            }
                        });
                    }
                    
                    ProjectileChargeProperties hamonChargeProperties = ProjectileChargeProperties.getChargeProperties(projectile);
                    if (hamonChargeProperties != null) {
                        projectile.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(projCharge -> {
                            
                            // projectiles charged by a hamon user
                            INonStandPower.getNonStandPowerOptional(shooter).ifPresent(power -> {
                                if (power.getEnergy() > 0) {
                                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                                        AbstractHamonSkill skillRequired = projectile instanceof AbstractArrow
                                                ? ModHamonSkills.ARROW_INFUSION.get() : ModHamonSkills.THROWABLES_INFUSION.get();
                                        if (hamon.isSkillLearned(skillRequired)) {
                                            hamon.consumeHamonEnergyTo(efficiency -> {
                                                hamonChargeProperties.applyCharge(projCharge, efficiency, power);
                                                projCharge.setMultiplyWithUserStrength(true);
                                                return null;
                                            }, hamonChargeProperties.energyRequired, skillRequired);
                                        }
                                    });
                                }
                            });

                            // projectiles charged by an infused entity
                            shooter.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                                if (cap.hasHamonCharge()) {
                                    HamonCharge hamonCharge = cap.getHamonCharge();
                                    hamonCharge.decreaseTicks((int) (hamonCharge.getInitialTicks() * hamonChargeProperties.energyRequired / 1000F));
                                    hamonChargeProperties.applyCharge(projCharge, hamonCharge.getDamage(), null);
                                    projCharge.setMultiplyWithUserStrength(false);
                                }
                            });
                        });
                    }
                }
            }
            
            // charge chicken coming out of a charged egg
            if (entity instanceof Chicken) {
                world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve()
                .flatMap(worldCap -> worldCap.eggChargingChicken(entity)).ifPresent(eggEntity -> {
                    eggEntity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(eggCharge -> {
                        entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(chickenCharge -> {
                            Entity eggThrower = eggEntity.getOwner();
                            LivingEntity throwerLiving = eggThrower instanceof LivingEntity ? (LivingEntity) eggThrower : null;
                            Optional<HamonData> userHamon = throwerLiving != null ? INonStandPower.getNonStandPowerOptional(throwerLiving)
                                    .map(power -> power.getTypeSpecificData(ModPowers.HAMON.get())).map(Optional::get)
                                    : Optional.empty();
                            chickenCharge.setHamonCharge(
                                    eggCharge.getHamonDamage() * userHamon.map(hamon -> hamon.getHamonDamageMultiplier() * hamon.getBloodstreamEfficiency()).orElse(1F), 
                                    Integer.MAX_VALUE, 
                                    throwerLiving, 0);
                        });
                    });
                });
            }
        }
    }
    
    public static final class ProjectileChargeProperties {
        public static final ProjectileChargeProperties ABSTRACT_ARROW = new ProjectileChargeProperties(1.5F, OptionalInt.of(10), 1000);
        public static final ProjectileChargeProperties SNOWBALL = new ProjectileChargeProperties(0.75F, OptionalInt.of(20), 500);
        public static final ProjectileChargeProperties EGG = new ProjectileChargeProperties(0.75F, OptionalInt.empty(), 200);
        public static final ProjectileChargeProperties WATER_BOTTLE = new ProjectileChargeProperties(1.0F, OptionalInt.of(30), 750);
        public static final ProjectileChargeProperties MOLOTOV = new ProjectileChargeProperties(1.0F, OptionalInt.of(200), 500);
        
        private final float baseMultiplier;
        private final OptionalInt chargeTicks;
        public final float energyRequired;
        
        private ProjectileChargeProperties(float baseMultiplier, OptionalInt chargeTicks, float energyRequired) {
            this.baseMultiplier = baseMultiplier;
            this.chargeTicks = chargeTicks;
            this.energyRequired = energyRequired;
        }
        
        public static boolean canBeChargedWithHamon(Entity entity) {
            return entity.getType() == EntityType.POTION // potion item hasn't been set yet, so we can't check if it's a water bottle
                    || ProjectileChargeProperties.getChargeProperties(entity) != null;
        }
        
        @Nullable 
        public static ProjectileChargeProperties getChargeProperties(Entity projectile) {
            if (projectile instanceof AbstractArrow && !isChargedInOtherWay(projectile)) {
                return ABSTRACT_ARROW;
            }
            EntityType<?> type = projectile.getType();
            if (type == EntityType.SNOWBALL) {
                return SNOWBALL;
            }
            else if (type == EntityType.EGG) {
                return EGG;
            }
            else if (type == EntityType.POTION && MCUtil.isPotionWaterBottle((ThrownPotion) projectile)) {
                return WATER_BOTTLE;
            }
            else if (type == ModEntityTypes.MOLOTOV.get()) {
                return MOLOTOV;
            }
            return null;
        }
        
        public void applyCharge(ProjectileHamonChargeCap chargeCap, float damageMultiplier, @Nullable INonStandPower spendingEnergy) {
            chargeCap.setBaseDmg(this.baseMultiplier * damageMultiplier);
            if (chargeTicks.isPresent()) {
                chargeCap.setMaxChargeTicks(chargeTicks.getAsInt());
            }
            else {
                chargeCap.setInfiniteChargeTime();
            }
            if (spendingEnergy != null) {
                chargeCap.setSpentEnergy(Math.min(spendingEnergy.getEnergy(), energyRequired));
            }
        }
    }
    
    private static boolean isChargedInOtherWay(Entity projectile) {
        return projectile.getType() == ModEntityTypes.CLACKERS.get();
    }
    
    
    
    public static void hamonPerksOnDeath(LivingEntity dead) {
        if (JojoModConfig.getCommonConfigInstance(false).keepHamonOnDeath.get() && !dead.level.getLevelData().isHardcore()) return;
        INonStandPower.getNonStandPowerOptional(dead).ifPresent(power -> {
            power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (hamon.isSkillLearned(ModHamonSkills.CRIMSON_BUBBLE.get())) {
                    CrimsonBubbleEntity bubble = new CrimsonBubbleEntity(dead.level);
                    ItemStack heldItem = dead.getMainHandItem();
                    if (!heldItem.isEmpty()) {
                        dead.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        ItemEntity item = new ItemEntity(dead.level, dead.getX(), dead.getEyeY() - 0.3D, dead.getZ(), heldItem);
                        item.setPickUpDelay(2);
                        dead.level.addFreshEntity(item);
                        bubble.putItem(item);
                    }
                    dead.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), 
                            ModSounds.CAESAR_LAST_HAMON.get(), dead.getSoundSource(), 1.0F, 1.0F);
                    bubble.moveTo(dead.getX(), dead.getEyeY(), dead.getZ(), dead.yRot, dead.xRot);
                    bubble.setHamonPoints(hamon.getHamonStrengthPoints(), hamon.getHamonControlPoints());
                    dead.level.addFreshEntity(bubble);
                }
                else if (hamon.isSkillLearned(ModHamonSkills.DEEP_PASS.get())) {
                    Player closestHamonUser = MCUtil.entitiesAround(Player.class, dead, 8, false, player -> 
                    INonStandPower.getNonStandPowerOptional(player).map(pwr -> pwr.getType() == ModPowers.HAMON.get()).orElse(false))
                            .stream()
                            .min(Comparator.comparingDouble(player -> player.distanceToSqr(dead)))
                            .orElse(null);
                    if (closestHamonUser != null) {
                        dead.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), 
                                ModSounds.ZEPPELI_DEEP_PASS.get(), dead.getSoundSource(), 1.0F, 1.0F);
                        HamonData receiverHamon = INonStandPower.getPlayerNonStandPower(closestHamonUser).getTypeSpecificData(ModPowers.HAMON.get()).get();
                        if (receiverHamon.characterIs(ModHamonSkills.CHARACTER_JONATHAN.get())) {
                            JojoModUtil.sayVoiceLine(closestHamonUser, ModSounds.JONATHAN_DEEP_PASS_REACTION.get());
                        }
                        receiverHamon.setHamonStatPoints(HamonStat.STRENGTH, 
                                receiverHamon.getHamonStrengthPoints() + hamon.getHamonStrengthPoints(), true, false);
                        receiverHamon.setHamonStatPoints(HamonStat.CONTROL, 
                                receiverHamon.getHamonControlPoints() + hamon.getHamonControlPoints(), true, false);
                        if (closestHamonUser instanceof ServerPlayer) {
                            ModCriteriaTriggers.LAST_HAMON.get().trigger((ServerPlayer) closestHamonUser, dead);
                        }
                        createHamonSparkParticlesEmitter(closestHamonUser, 1.0F);
                    }
                }
            });
        });
    }

    public static void updateCheatDeathEffect(LivingEntity user) {
        user.addEffect(new MobEffectInstance(ModStatusEffects.CHEAT_DEATH.get(), 120000, 0, false, false, true));
    }
    
    public static boolean isLiving(LivingEntity entity) {
        // not the best way to determine living mobs in other mods
        return !(JojoModUtil.isUndeadOrVampiric(entity) ||
                entity instanceof AbstractGolem ||
                entity instanceof ArmorStand || 
                entity instanceof StandEntity);
    }

    @Nullable
    public static Set<AbstractHamonSkill> nearbyTeachersSkills(LivingEntity learner) {
        Set<AbstractHamonSkill> skills = new HashSet<>();
        if (MCUtil.entitiesAround(LivingEntity.class, learner, 3D, false, 
                entity -> INonStandPower.getNonStandPowerOptional(entity).map(power -> 
                power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                    hamon.getLearnedSkills().forEach(skill -> {
                        if (skill.requiresTeacher()) {
                            skills.add(skill);
                        }
                    });
                    return true;
                }).orElse(false)).orElse(false)).isEmpty()) {
            return null;
        }
        return skills;
    }
    
    public static boolean interactWithHamonTeacher(Level world, Player player, Entity targetEntity) {
        if (targetEntity instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) targetEntity;
            Optional<HamonData> targetHamon = INonStandPower.getNonStandPowerOptional(targetLiving).resolve()
                    .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
            if (targetHamon.isPresent()) {
                HamonUtil.interactWithHamonTeacher(targetLiving.level, player, targetLiving, targetHamon.get());
                return true;
            }
        }
        return false;
    }
    
    public static void interactWithHamonTeacher(Level world, Player player, LivingEntity teacher, HamonData teacherHamon) {
        INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
            Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
            if (!hamonOptional.isPresent() && !world.isClientSide()) {
                if (teacher instanceof Player) {
                    teacherHamon.addNewPlayerLearner(player);
                }
                else {
                    startLearningHamon(world, player, power, teacher, teacherHamon);
                }
            }
            hamonOptional.ifPresent(hamon -> {
                if (world.isClientSide()) {
                    ClientUtil.openHamonTeacherUi();
                }
                else {
                    if (player.abilities.instabuild) {
                        hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
                        hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
                        hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
                        hamon.tcsa(false);
                    }
                }
            });
        });
    }
    
    public static void startLearningHamon(Level world, Player player, INonStandPower playerPower, LivingEntity teacher, HamonData teacherHamon) {
        if (playerPower.canGetPower(ModPowers.HAMON.get()) && teacherHamon.characterIs(ModHamonSkills.CHARACTER_ZEPPELI.get())) {
            JojoModUtil.sayVoiceLine(teacher, ModSounds.ZEPPELI_FORCE_BREATH.get());
            teacher.swing(InteractionHand.MAIN_HAND, true);
            if (player.getRandom().nextFloat() <= 0.01F) {
                player.hurt(DamageUtil.SUFFOCATION, Math.min(10.0F, player.getHealth() - 0.0001F));
                player.setAirSupply(0);
                return;
            }
            else {
                player.hurt(DamageUtil.SUFFOCATION, Math.min(0.1F, player.getHealth() - 0.0001F));
            }
        } 
        if (playerPower.givePower(ModPowers.HAMON.get())) {
            playerPower.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (player.abilities.instabuild) {
                    hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
                    hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
                    hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
                    hamon.tcsa(false);
                }
                player.sendMessage(Component.translatable("jojo.chat.message.learnt_hamon"), Util.NIL_UUID);
                PlayerUtilCap utilCap = player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseThrow(() -> new IllegalStateException());
                utilCap.sendNotification(OneTimeNotification.HAMON_WINDOW, 
                        Component.translatable("jojo.chat.message.hamon_window_hint", Component.keybind("jojo.key.hamon_skills_window")));
            });
        }
        else {
            player.displayClientMessage(Component.translatable("jojo.chat.message.cant_learn_hamon"), true);
        }
        return;
    }
    
    public static void hamonExplosion(Level world, @Nullable Entity source, @Nullable Entity hamonUser, 
            Vec3 position, float radius, float damage) {
        HamonBlastExplosion hamonBlast = new HamonBlastExplosion(world, source, null, 
                position.x, position.y, position.z, radius);
        hamonBlast.setHamonDamage(damage);
        CustomExplosion.explode(hamonBlast);
    }
    

    
    public static boolean cancelDamageFromBlock(LivingEntity entity, DamageSource dmgSource, float dmgAmount) {
        DamagingBlockType type = DamagingBlockType.getType(dmgSource);
        if (type != null) {
            Level world = entity.level;
            boolean protectedFromDamage = true;
            boolean fromBlocks = false;
            
            AABB hitbox = entity.getBoundingBox();
            BlockPos posMin = new BlockPos(hitbox.minX + 0.001D, hitbox.minY + 0.001D, hitbox.minZ + 0.001D);
            BlockPos posMax = new BlockPos(hitbox.maxX - 0.001D, hitbox.maxY - 0.001D, hitbox.maxZ - 0.001D);
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            if (world.hasChunksAt(posMin, posMax)) {
                for (int x = posMin.getX(); x <= posMax.getX() && protectedFromDamage; ++x) {
                    for (int y = posMin.getY(); y <= posMax.getY() && protectedFromDamage; ++y) {
                        for (int z = posMin.getZ(); z <= posMax.getZ() && protectedFromDamage; ++z) {
                            blockPos.set(x, y, z);
                            BlockState blockState = world.getBlockState(blockPos);
                            if (type.rightBlock(blockState)) {
                                protectedFromDamage &= preventBlockDamage(entity, world, blockPos, blockState, dmgSource, dmgAmount);
                                fromBlocks = true;
                            }
                        }
                    }
                }
            }
            
            if (!fromBlocks) {
                protectedFromDamage = preventBlockDamage(entity, world, null, null, dmgSource, dmgAmount);
            }
            
            return protectedFromDamage;
        }
        
        return false;
    }
    
    private static enum DamagingBlockType {
        CACTUS {
            @Override public boolean rightBlock(BlockState blockState) {
                return blockState.getBlock() instanceof CactusBlock;
            }
        },
        BERRY_BUSH {
            @Override public boolean rightBlock(BlockState blockState) {
                return blockState.getBlock() instanceof SweetBerryBushBlock;
            }
        };
        
        public abstract boolean rightBlock(BlockState blockState);
        
        @Nullable public static DamagingBlockType getType(DamageSource dmgSource) {
            if (dmgSource == DamageSource.CACTUS) {
                return CACTUS;
            }
            else if (dmgSource == DamageSource.SWEET_BERRY_BUSH) {
                return BERRY_BUSH;
            }
            return null;
        }
    }
    
    public static boolean preventBlockDamage(LivingEntity entity, Level world, 
            @Nullable BlockPos blockPos, @Nullable BlockState blockState, DamageSource dmgSource, float dmgAmount) {
        if (world.isClientSide()) {
            return false;
        }
        
        boolean damagePrevented = GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(entity), power -> {
            if (power.getType() == ModPowers.HAMON.get()) {
                if (entity.getType() == ModEntityTypes.HAMON_MASTER.get()) {
                    return true;
                }

                float energyCost = dmgAmount * 0.5F;
                if (power.getEnergy() >= energyCost) {
                    power.consumeEnergy(energyCost);
                    return true;
                }
                else {
                    power.consumeEnergy(power.getEnergy());
                }
            }
            return false;
            
        }) || GeneralUtil.orElseFalse(entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY), cap -> {
            if (cap.hasHamonCharge()) {
                cap.getHamonCharge().decreaseTicks(Math.max((int) dmgAmount, 1));
                return true;
            }
            return false;
        });
        
        if (damagePrevented) {
            Vec3 sparkPos = null;
            if (blockPos != null && blockState != null) {
                AABB entityHitbox = entity.getBoundingBox();
                VoxelShape blockShape = blockState.getCollisionShape(world, blockPos);
                if (!blockShape.isEmpty()) {
                    AABB blockAABB = blockShape.bounds().move(blockPos);
                    AABB intersection = entityHitbox.intersect(blockAABB);
                    sparkPos = new Vec3(
                            Mth.lerp(Math.random(), intersection.minX, intersection.maxX), 
                            Mth.lerp(Math.random(), intersection.minY, intersection.maxY), 
                            Mth.lerp(Math.random(), intersection.minZ, intersection.maxZ));
                }
            }
            
            if (sparkPos == null) {
                sparkPos = dmgSource.getSourcePosition();
            }
            
            if (sparkPos != null) {
                PacketManager.sendToClientsTrackingAndSelf(TrHamonParticlesPacket.shortSpark(
                        entity.getId(), sparkPos, false, 
                        Math.max((int) (dmgAmount * 0.5F), 1), Math.min(dmgAmount * 0.25F, 1)), entity);
            }
            return true;
        }
        return false;
    }
    
    public static void onProjectileImpact(Entity entity, HitResult target) {
        entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.onTargetHit(target);
        });
    }
    
    
    public static void hamonChargedCreeperBlast(Explosion explosion, Level world) {
        if (!world.isClientSide() && !(explosion instanceof HamonBlastExplosion)) {
            Entity exploder = explosion.getExploder();
            if (exploder != null) {
                exploder.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                    if (cap.hasHamonCharge()) {
                        HamonCharge hamonCharge = cap.getHamonCharge();
                        float radius = CommonReflection.getRadius(explosion);
                        HamonUtil.hamonExplosion(exploder.level, exploder, 
                                hamonCharge.getUser((ServerLevel) world), explosion.getPosition(),
                                radius, hamonCharge.getDamage());
                    }
                });
            }
        }
    }
    
    public static boolean isItemLivingMatter(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            return HamonOrganismInfusion.isBlockLiving(((BlockItem) item).getBlock().defaultBlockState());
        }
        
        return item == ModItems.GOLD_EXPERIENCE_BODY_TISSUE.get() ||
                item instanceof EggItem || 
                ItemTags.getAllTags().getTagOrEmpty(RAW_FISH_TAG).contains(item) || item == Items.COD || item == Items.SALMON || item == Items.TROPICAL_FISH || item == Items.PUFFERFISH ||
                item instanceof MobBucketItem;
    }
    private static final ResourceLocation RAW_FISH_TAG = new ResourceLocation("forge", "raw_fishes");
    
    
    
    // one-time particle emit (like crit particles) + 'generic electricity sound'
    public static void emitHamonSparkParticles(Level world, @Nullable Player clientHandled, 
            double x, double y, double z, float intensity, float volumeMult, @Nullable SoundEvent hamonSound) {
        if (intensity > 0) {
            intensity = Math.min(intensity, 4F);
            int count = Math.max((int) (intensity * 16.5F), 1);
            if (!world.isClientSide()) {
                ((ServerLevel) world).sendParticles(ModParticles.HAMON_SPARK.get(), x, y, z, count, 0.05, 0.05, 0.05, 0.25);
            }
            else if (clientHandled == ClientUtil.getClientPlayer()) {
                CustomParticlesHelper.createHamonSparkParticles(null, x, y, z, count);
            }
            if (hamonSound != null) {
                float volume = Math.min(intensity * 2, 1.0F) * volumeMult;
                world.playSound(clientHandled, x, y, z, hamonSound, 
                        SoundSource.AMBIENT, volume, 1.0F + (world.random.nextFloat() - 0.5F) * 0.15F);
            }
        }
    }
    
    public static void emitHamonSparkParticles(Level world, @Nullable Player clientHandled, double x, double y, double z, float intensity, float volumeMult) {
        emitHamonSparkParticles(world, clientHandled, x, y, z, intensity, volumeMult, ModSounds.HAMON_SPARK.get());
    }
    
    public static void emitHamonSparkParticles(Level world, @Nullable Player clientHandled, double x, double y, double z, float intensity) {
        emitHamonSparkParticles(world, clientHandled, x, y, z, intensity, 1);
    }
    
    public static void emitHamonSparkParticles(Level world, @Nullable Player clientHandled, Vec3 vec, float intensity) {
        emitHamonSparkParticles(world, clientHandled, vec.x, vec.y, vec.z, intensity);
    }
    
    public static void emitHamonSparkParticles(Level world, @Nullable Player clientHandled, Vec3 vec, float intensity, @Nullable SoundEvent hamonSound) {
        emitHamonSparkParticles(world, clientHandled, vec.x, vec.y, vec.z, intensity, 1, hamonSound);
    }
    
    
    public static void createHamonSparkParticlesEmitter(Entity entity, float intensity) {
        createHamonSparkParticlesEmitter(entity, intensity, 1, ModParticles.HAMON_SPARK.get());
    }

    // particles emitter accompanied by 'generic electricity sound but longer'
    public static void createHamonSparkParticlesEmitter(Entity entity, float intensity, float soundVolumeMultiplier, ParticleOptions hamonParticle) {
        if (intensity > 0) {
            intensity = Math.min(intensity, 4F);
            Level world = entity.level;
            if (!world.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(TrHamonParticlesPacket.emitter(entity.getId(), intensity, soundVolumeMultiplier, 
                        hamonParticle != ModParticles.HAMON_SPARK.get() ? hamonParticle : null), entity);
            }
            else {
                float volume = intensity * 2 * soundVolumeMultiplier;
                for (int i = (int) (intensity * 9.5F); i >= 0; i--) {
                    CustomParticlesHelper.createParticlesEmitter(entity, hamonParticle, Math.max(1, (int) (intensity * 9.5) - i));
                    if (i % 2 == 0 && i < 4) {
                        ClientTickingSoundsHelper.playHamonSparksSound(entity, Math.min(volume, 1.0F), 1.0F + (world.random.nextFloat() - 0.5F) * 0.15F);
                    }
                }
            }
        }
    }
}
