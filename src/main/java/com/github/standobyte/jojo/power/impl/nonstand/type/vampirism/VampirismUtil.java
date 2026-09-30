package com.github.standobyte.jojo.power.impl.nonstand.type.vampirism;

import java.util.Set;
import java.util.function.Predicate;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.potion.VampireSunBurnEffect;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHealEvent;

public class VampirismUtil {

    public static void tickSunDamage(LivingEntity entity) {
        if (!entity.level.isClientSide() && entity.invulnerableTime <= 10 && DamageUtil.entityTakesUVDamage(entity, true)) {
            float sunDamage = getSunDamage(entity);
            if (sunDamage > 0 && DamageUtil.dealUltravioletDamage(entity, sunDamage, null, null, true)) {
                incSunBurn(entity, 1);
            }
        }
    }
    
    public static void incSunBurn(LivingEntity entity, int tickUpAmount) {
        MobEffectInstance sunBurnEffect = entity.getEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get());
        int duration;
        int amplifier;
        if (sunBurnEffect == null) {
            duration = 60 * tickUpAmount;
            amplifier = tickUpAmount - 1;
        }
        else {
            int difficulty = Math.max(entity.level.getDifficulty().getId(), 1);
            duration = sunBurnEffect.getDuration() + 60 * tickUpAmount / difficulty;
            amplifier = duration / 60;
        }
        VampireSunBurnEffect.giveEffectTo(entity, duration, amplifier);
    }
    
//    private static final float MAX_SUN_DAMAGE = 10;
//    private static final float MIN_SUN_DAMAGE = 2;
    private static float getSunDamage(LivingEntity entity) {
        Level world = entity.level;
        if (isSunny(world)) {
            float brightness = entity.getBrightness();
            BlockPos blockPos = entity.getVehicle() instanceof Boat ? 
                    (BlockPos.containing(entity.getX(), (double)Math.round(entity.getY(1.0)), entity.getZ())).above()
                    : BlockPos.containing(entity.getX(), (double)Math.round(entity.getY(1.0)), entity.getZ());
            if (brightness > 0.5F && world.canSeeSky(blockPos)) {
                return 4;
//                int time = (int) (world.getDayTime() % 24000L);
//                float damage = MAX_SUN_DAMAGE;
//                float diff = MAX_SUN_DAMAGE - MIN_SUN_DAMAGE;
//
//                // sunrise
//                if (time > 23460) { 
//                    time -= 24000;
//                }
//                if (time <= 60) {
//                    damage -= diff * (1F - (float) (time + 540) / 600F);
//                }
//
//                // sunset
//                else if (time > 11940 && time <= 12540) {
//                    damage -= diff * (float) (time - 11940) / 600F;
//                }
//
//                return damage;
            }
        }
        return 0;
    }
    
    public static boolean isSunny(Level world) {
        if (world.isClientSide()) {
            world.updateSkyBrightness();
        }
        return world.dimensionType().hasSkyLight()
                && !world.dimensionType().hasCeiling()
                && world.isDay()
                && !world.isRaining()
                && !world.isThundering();
    }
    
    
    
    public static void editMobAiGoals(Mob mob) {
        if (mob.getClassification(false) == MobCategory.MONSTER) {
            VampirismUtil.makeMobNeutralToVampirePlayers(mob);
        }
        else if (mob instanceof IronGolem) {
            mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, Player.class, 5, false, false, 
                    target -> target instanceof Player && JojoModUtil.isPlayerJojoVampiric((Player) target)));
        }
    }
    
    private static void makeMobNeutralToVampirePlayers(Mob mob) {
        if (JojoModConfig.getCommonConfigInstance(false).vampiresAggroMobs.get()) return;
        
        Set<WrappedGoal> goals = CommonReflection.getGoalsSet(mob.targetSelector);
        for (WrappedGoal prGoal : goals) {
            Goal goal = prGoal.getGoal();
            if (goal instanceof NearestAttackableTargetGoal) {
                NearestAttackableTargetGoal<?> targetGoal = (NearestAttackableTargetGoal<?>) goal;
                Class<? extends LivingEntity> targetClass = CommonReflection.getTargetClass(targetGoal);
                
                if (targetClass == Player.class) {
                    TargetingConditions selector = CommonReflection.getTargetConditions(targetGoal);
                    if (selector != null) {
                        Predicate<LivingEntity> oldPredicate = CommonReflection.getTargetSelector(selector);
                        Predicate<LivingEntity> undeadPredicate = target -> 
                            target instanceof Player && !(
//                                    JojoModUtil.isPlayerUndead((PlayerEntity) target) &&
                                    INonStandPower.getNonStandPowerOptional(target).map(
                                            power -> power.getTypeSpecificData(ModPowers.VAMPIRISM.get())
                                            .map(vampirism -> vampirism.getCuringStage() < 3).orElse(false)).orElse(false)) 
                            && !(INonStandPower.getNonStandPowerOptional(target).map(power ->power.getType() == ModPowers.ZOMBIE.get()).orElse(false)) 
                            && !(INonStandPower.getNonStandPowerOptional(target).map(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                                    .map(pillarman -> pillarman.isStoneFormEnabled()).orElse(false)).orElse(false) || 
                                    INonStandPower.getNonStandPowerOptional(target).map(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                                            .map(pillarman -> pillarman.getEvolutionStage() > 1).orElse(false)).orElse(false));
                        CommonReflection.setTargetConditions(targetGoal, new TargetingConditions().range(CommonReflection.getTargetDistance(targetGoal)).selector(
                                oldPredicate != null ? oldPredicate.and(undeadPredicate) : undeadPredicate));
                    }
                }
            }
        }
    }
    
    
    
    public static void onEnchantedGoldenAppleEaten(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            MobEffectInstance weakness = entity.getEffect(MobEffects.WEAKNESS);
            if (!(weakness != null && weakness.getAmplifier() >= 4)) {
                return;
            }
            
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                    if (!entity.isSilent()) {
                        entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                                ModSounds.VAMPIRE_CURE_START.get(), entity.getSoundSource(), 1.0F, 1.0F);
                    }
                    vampirism.setCuringTicks(1);
                });
            });
        }
    }
    
    
    
    public static void consumeEnergyOnHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.isAlive()) {
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                if (power.getType() == ModPowers.VAMPIRISM.get() 
                        || (power.getType() == ModPowers.PILLAR_MAN.get() 
                        && power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().getEvolutionStage() > 1)) {
                    float healCost = healCost(entity.level);
                    if (healCost > 0) {
                        float actualHeal = Math.min(event.getAmount(), power.getEnergy() / healCost);
                        actualHeal = Math.min(actualHeal, entity.getMaxHealth() - entity.getHealth());
                        if (actualHeal > 0) {
                            power.consumeEnergy(Math.min(actualHeal, entity.getMaxHealth() - entity.getHealth()) * healCost);
                            event.setAmount(actualHeal);
                        }
                        else {
                            event.setCanceled(true);
                        }
                    }
                }
            });
        }
    }
    
    public static float healCost(Level world) {
        return GeneralUtil.getOrLast(
                JojoModConfig.getCommonConfigInstance(world.isClientSide()).bloodHealCost.get(), 
                world.getDifficulty().getId()).floatValue();
    }
    
}
