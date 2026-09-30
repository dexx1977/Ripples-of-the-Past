package com.github.standobyte.jojo.util.mc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class CommonReflection {
    private static final Field GOAL_SELECTOR_AVAILABLE_GOALS = ObfuscationReflectionHelper.findField(GoalSelector.class, "f_25345_");
    public static Set<WrappedGoal> getGoalsSet(GoalSelector targetGoals) {
        return ReflectionUtil.getFieldValue(GOAL_SELECTOR_AVAILABLE_GOALS, targetGoals);
    }

    private static final Field NEAREST_TARGET_GOAL_TARGET_TYPE = ObfuscationReflectionHelper.findField(NearestAttackableTargetGoal.class, "f_26048_");
    public static Class<? extends LivingEntity> getTargetClass(NearestAttackableTargetGoal<?> goal) {
        return ReflectionUtil.getFieldValue(NEAREST_TARGET_GOAL_TARGET_TYPE, goal);
    }

    private static final Field NEAREST_TARGET_GOAL_TARGET_CONDITIONS = ObfuscationReflectionHelper.findField(NearestAttackableTargetGoal.class, "f_26051_");
    public static TargetingConditions getTargetConditions(NearestAttackableTargetGoal<?> goal) {
        return ReflectionUtil.getFieldValue(NEAREST_TARGET_GOAL_TARGET_CONDITIONS, goal);
    }

    public static void setTargetConditions(NearestAttackableTargetGoal<?> goal, TargetingConditions conditions) {
        ReflectionUtil.setFieldValue(NEAREST_TARGET_GOAL_TARGET_CONDITIONS, goal, conditions);
    }

    private static final Field ENTITY_PREDICATE_SELECTOR = ObfuscationReflectionHelper.findField(TargetingConditions.class, "f_26879_");
    public static Predicate<LivingEntity> getTargetSelector(TargetingConditions conditions) {
        return ReflectionUtil.getFieldValue(ENTITY_PREDICATE_SELECTOR, conditions);
    }

    private static final Method TARGET_GOAL_GET_FOLLOW_DISTANCE = ObfuscationReflectionHelper.findMethod(TargetGoal.class, "func_111175_f");
    public static double getTargetDistance(NearestAttackableTargetGoal<?> goal) {
        return ReflectionUtil.invokeMethod(TARGET_GOAL_GET_FOLLOW_DISTANCE, goal);
    }

    private static final Field HURT_BY_TARGET_GOAL_TO_IGNORE = ObfuscationReflectionHelper.findField(HurtByTargetGoal.class, "f_26035_");
    public static Class<?>[] getToIgnoreDamage(HurtByTargetGoal goal) {
        return ReflectionUtil.getFieldValue(HURT_BY_TARGET_GOAL_TO_IGNORE, goal);
    }
    
    

    private static final Field CREEPER_ENTITY_SWELL = ObfuscationReflectionHelper.findField(Creeper.class, "f_32270_");
    public static void setCreeperSwell(Creeper entity, int swell) {
        ReflectionUtil.setIntFieldValue(CREEPER_ENTITY_SWELL, entity, swell);
    }
    
    

    private static final Field PROJECTILE_ENTITY_LEFT_OWNER = ObfuscationReflectionHelper.findField(Projectile.class, "f_37246_");
    public static boolean getProjectileLeftOwner(Projectile entity) {
        return ReflectionUtil.getBooleanFieldValue(PROJECTILE_ENTITY_LEFT_OWNER, entity);
    }
    
    
    
    private static final Method CHUNK_GENERATOR_CODEC = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
    public static Codec<? extends ChunkGenerator> getCodec(ChunkGenerator chunkGenerator) {
        return ReflectionUtil.invokeMethod(CHUNK_GENERATOR_CODEC, chunkGenerator);
    }
    
    
    
    
    private static final Field REGISTRY_KEY_VALUES_FIELD = ObfuscationReflectionHelper.findField(ResourceKey.class, "f_135775_");
    private static Map<String, ResourceKey<?>> REGISTRY_KEY_VALUES;
    public static Map<String, ResourceKey<?>> registryKeyValues() {
        if (REGISTRY_KEY_VALUES == null) {
            REGISTRY_KEY_VALUES = ReflectionUtil.getFieldValue(REGISTRY_KEY_VALUES_FIELD, null);
        }
        return REGISTRY_KEY_VALUES;
    }
    
    
    
    private static final Field CRAFTING_INVENTORY_MENU = ObfuscationReflectionHelper.findField(CraftingContainer.class, "field_70465_c");
    public static AbstractContainerMenu getCraftingInventoryMenu(CraftingContainer inventory) {
        return ReflectionUtil.getFieldValue(CRAFTING_INVENTORY_MENU, inventory);
    }
    
    private static final Field PLAYER_CONTAINER_OWNER = ObfuscationReflectionHelper.findField(InventoryMenu.class, "f_39703_");
    public static Player getPlayer(InventoryMenu container) {
        return ReflectionUtil.getFieldValue(PLAYER_CONTAINER_OWNER, container);
    }
    
    private static final Field WORKBENCH_CONTAINER_PLAYER = ObfuscationReflectionHelper.findField(CraftingMenu.class, "f_39351_");
    public static Player getPlayer(CraftingMenu container) {
        return ReflectionUtil.getFieldValue(WORKBENCH_CONTAINER_PLAYER, container);
    }
    
    
    
    private static final Field FURNACE_TE_LIT_TIME = ObfuscationReflectionHelper.findField(AbstractFurnaceBlockEntity.class, "f_58316_");
    public static int getFurnaceLitTime(AbstractFurnaceBlockEntity tileEntity) {
        return ReflectionUtil.getIntFieldValue(FURNACE_TE_LIT_TIME, tileEntity);
    }
    
    public static void setFurnaceLitTime(AbstractFurnaceBlockEntity tileEntity, int ticks) {
        ReflectionUtil.setIntFieldValue(FURNACE_TE_LIT_TIME, tileEntity, ticks);
    }
    
    private static final Field FURNACE_TE_LIT_DURATION = ObfuscationReflectionHelper.findField(AbstractFurnaceBlockEntity.class, "f_58317_");
    public static void setFurnaceLitDuration(AbstractFurnaceBlockEntity tileEntity, int ticks) {
        ReflectionUtil.setIntFieldValue(FURNACE_TE_LIT_DURATION, tileEntity, ticks);
    }
    
    
    
    private static final Field LIVING_ENTITY_LERP_STEPS = ObfuscationReflectionHelper.findField(LivingEntity.class, "f_20903_");
    public static int getLerpSteps(LivingEntity entity) {
        return ReflectionUtil.getIntFieldValue(LIVING_ENTITY_LERP_STEPS, entity);
    }
    
    public static void setLerpSteps(LivingEntity entity, int steps) {
        ReflectionUtil.setIntFieldValue(LIVING_ENTITY_LERP_STEPS, entity, steps);
    }
    
    
    
    private static final Field FIREWORK_ROCKET_ENTITY_LIFETIME = ObfuscationReflectionHelper.findField(FireworkRocketEntity.class, "f_37023_");
    public static void setLifetime(FireworkRocketEntity entity, int ticks) {
        ReflectionUtil.setIntFieldValue(FIREWORK_ROCKET_ENTITY_LIFETIME, entity, ticks);
    }
    
    private static final Field FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM_FIELD = ObfuscationReflectionHelper.findField(FireworkRocketEntity.class, "f_37019_");
    private static EntityDataAccessor<ItemStack> FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM = null;
    public static EntityDataAccessor<ItemStack> getFireworkItemParameter() {
        if (FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM == null) {
            FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM = ReflectionUtil.getFieldValue(FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM_FIELD, null);
        }
        return FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM;
    }
    
    private static final Field CREEPER_DATA_IS_POWERED_FIELD = ObfuscationReflectionHelper.findField(Creeper.class, "f_32274_");
    private static EntityDataAccessor<Boolean> CREEPER_DATA_IS_POWERED = null;
    public static EntityDataAccessor<Boolean> getCreeperPoweredParameter() {
        if (CREEPER_DATA_IS_POWERED == null) {
            CREEPER_DATA_IS_POWERED = ReflectionUtil.getFieldValue(CREEPER_DATA_IS_POWERED_FIELD, null);
        }
        return CREEPER_DATA_IS_POWERED;
    }
    
    private static final Field EXPLOSION_RADIUS = ObfuscationReflectionHelper.findField(Explosion.class, "f_46017_");
    public static float getRadius(Explosion explosion) {
        return ReflectionUtil.getFloatFieldValue(EXPLOSION_RADIUS, explosion);
    }
    
    
    
    private static final Field TNT_MINECART_ENTITY_FUSE = ObfuscationReflectionHelper.findField(MinecartTNT.class, "f_38647_");
    public static int getFuse(MinecartTNT entity) {
        return ReflectionUtil.getIntFieldValue(TNT_MINECART_ENTITY_FUSE, entity);
    }
    
    public static void setFuse(MinecartTNT entity, int fuse) {
        ReflectionUtil.setIntFieldValue(TNT_MINECART_ENTITY_FUSE, entity, fuse);
    }
    
    
    
    private static final Field LIVING_ENTITY_ATTACK_STRENGTH_TICKER = ObfuscationReflectionHelper.findField(LivingEntity.class, "f_20922_");
    public static int getAttackStrengthTicker(LivingEntity entity) {
        return ReflectionUtil.getIntFieldValue(LIVING_ENTITY_ATTACK_STRENGTH_TICKER, entity);
    }
    
    public static void setAttackStrengthTicker(LivingEntity entity, int attackStrengthTicker) {
        ReflectionUtil.setIntFieldValue(LIVING_ENTITY_ATTACK_STRENGTH_TICKER, entity, attackStrengthTicker);
    }
    
    
    
    private static final Method LIVING_ENTITY_ON_EFFECT_UPDATED = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_70695_b", MobEffectInstance.class, boolean.class);
    public static void onEffectUpdated(LivingEntity entity, MobEffectInstance effect, boolean resetAttributes) {
        ReflectionUtil.invokeMethod(LIVING_ENTITY_ON_EFFECT_UPDATED, entity, effect, resetAttributes);
    }
    
    
    
    private static final Field PLAYER_ENTITY_SLEEP_COUNTER = ObfuscationReflectionHelper.findField(Player.class, "f_36110_");
    public static void setSleepCounter(Player entity, int sleepCounter) {
        ReflectionUtil.setIntFieldValue(PLAYER_ENTITY_SLEEP_COUNTER, entity, sleepCounter);
    }
    
    

    private static final Field MERCHANT_CONTAINER_TRADER = ObfuscationReflectionHelper.findField(MerchantMenu.class, "f_40027_");
    public static Merchant getMerchant(MerchantMenu merchantContainer) {
        return ReflectionUtil.getFieldValue(MERCHANT_CONTAINER_TRADER, merchantContainer);
    }
    
    private static final Field MERCHANT_INVENTORY_MERCHANT = ObfuscationReflectionHelper.findField(MerchantContainer.class, "f_39997_");
    public static Merchant getMerchant(MerchantContainer merchantContainer) {
        return ReflectionUtil.getFieldValue(MERCHANT_INVENTORY_MERCHANT, merchantContainer);
    }
    
    
    
    private static final Method PROJECTILE_ITEM_ENTITY_GET_ITEM_RAW = ObfuscationReflectionHelper.findMethod(ThrowableItemProjectile.class, "func_213882_k");
    public static ItemStack getItemRaw(ThrowableItemProjectile entity) {
        return ReflectionUtil.invokeMethod(PROJECTILE_ITEM_ENTITY_GET_ITEM_RAW, entity);
    }
    
    private static final Method PROJECTILE_ITEM_ENTITY_GET_DEFAULT_ITEM = ObfuscationReflectionHelper.findMethod(ThrowableItemProjectile.class, "func_213885_i");
    public static Item getDefaultItem(ThrowableItemProjectile entity) {
        return ReflectionUtil.invokeMethod(PROJECTILE_ITEM_ENTITY_GET_DEFAULT_ITEM, entity);
    }
    
    
    
    private static final Field MOOSHROOM_ENTITY_EFFECT = ObfuscationReflectionHelper.findField(MushroomCow.class, "f_28909_");
    public static MobEffect getEffect(MushroomCow entity) {
        return ReflectionUtil.getFieldValue(MOOSHROOM_ENTITY_EFFECT, entity);
    }
    
    private static final Field MOOSHROOM_ENTITY_EFFECT_DURATION = ObfuscationReflectionHelper.findField(MushroomCow.class, "f_28910_");
    public static int getEffectDuration(MushroomCow entity) {
        return ReflectionUtil.getIntFieldValue(MOOSHROOM_ENTITY_EFFECT_DURATION, entity);
    }
    
    public static void clearEffect(MushroomCow entity) {
        ReflectionUtil.setFieldValue(MOOSHROOM_ENTITY_EFFECT, entity, null);
        ReflectionUtil.setIntFieldValue(MOOSHROOM_ENTITY_EFFECT_DURATION, entity, 0);
    }
    
    
    
    private static final Method LIVING_ENTITY_DROP_EQUIPMENT = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_213337_cE");
    public static void dropEquipment(LivingEntity entity) {
        ReflectionUtil.invokeMethod(LIVING_ENTITY_DROP_EQUIPMENT, entity);
    }
    
    
    
    private static final Method ZOMBIE_VILLAGER_ENTITY_START_CONVERTING = ObfuscationReflectionHelper.findMethod(ZombieVillager.class, "func_191991_a", UUID.class, int.class);
    public static void startConverting(ZombieVillager entity, @Nullable UUID conversionStarter, int villagerConversionTime) {
        ReflectionUtil.invokeMethod(ZOMBIE_VILLAGER_ENTITY_START_CONVERTING, entity, 
                conversionStarter, villagerConversionTime);
    }
    
    
    
    private static final Method MOB_ENTITY_GET_AMBIENT_SOUND = ObfuscationReflectionHelper.findMethod(Mob.class, "func_184639_G");
    public static SoundEvent getAmbientSound(Mob entity) {
        return ReflectionUtil.invokeMethod(MOB_ENTITY_GET_AMBIENT_SOUND, entity);
    }
    
    private static final Method LIVING_ENTITY_PLAY_HURT_SOUND = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_184581_c", DamageSource.class);
    public static void playHurtSound(LivingEntity entity, DamageSource damageSource) {
        ReflectionUtil.invokeMethod(LIVING_ENTITY_PLAY_HURT_SOUND, entity, damageSource);
    }
    
    
    private static final Method LIVING_ENTITY_DROP_ALL_DEATH_LOOT = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_213345_d", DamageSource.class);
    public static void dropAllDeathLoot(LivingEntity entity, DamageSource damageSource) {
        ReflectionUtil.invokeMethod(LIVING_ENTITY_DROP_ALL_DEATH_LOOT, entity, damageSource);
    }
    
    
    
    private static final Field ENTITY_DATA_CUSTOM_NAME_FIELD = ObfuscationReflectionHelper.findField(Entity.class, "f_19833_");
    private static EntityDataAccessor<Optional<Component>> ENTITY_DATA_CUSTOM_NAME = null;
    public static EntityDataAccessor<Optional<Component>> getEntityCustomNameParameter() {
        if (ENTITY_DATA_CUSTOM_NAME == null) {
            ENTITY_DATA_CUSTOM_NAME = ReflectionUtil.getFieldValue(ENTITY_DATA_CUSTOM_NAME_FIELD, null);
        }
        return ENTITY_DATA_CUSTOM_NAME;
    }
    
    
    
    private static final Method GAME_RULES_BOOLEAN_VALUE_CREATE = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "m_46250_", boolean.class);
    public static GameRules.Type<GameRules.BooleanValue> createBooleanGameRule(boolean defaultValue) {
        return ReflectionUtil.invokeMethod(GAME_RULES_BOOLEAN_VALUE_CREATE, null, defaultValue);
    }
}
