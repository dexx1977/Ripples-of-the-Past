package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.HamonHealing;
import com.github.standobyte.jojo.action.stand.effect.GEHealingEffect;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class GoldExperienceHeal extends StandEntityAction {
    
    public GoldExperienceHeal(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user.isDeadOrDying()) {
            return ActionConditionResult.NEGATIVE;
        }
        return canHeal(user, user, false, MAX_REGEN_LVL);
    }
    
    @Override
    public void standPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            spendAndHeal(world, user, user, userPower, standEntity);
        }
    }
    

    public static final int MAX_REGEN_LVL = 3;
    
    public static boolean isLiving(LivingEntity entity) {
        return !(!(entity instanceof Player) && JojoModUtil.isUndead(entity) ||
                entity instanceof AbstractGolem ||
                entity instanceof ArmorStand);
    }
    
    public static ActionConditionResult canHeal(LivingEntity entity, LivingEntity userGE, 
            boolean tissueItem, int effectMax) {
        if (entity != null) {
            if (!isLiving(entity)) {
                return conditionMessage("ge_heal_non_living");
            }
            if (StandUtil.getStandUser(entity) != entity) {
                return conditionMessage("ge_heal_stand");
            }
            
            if (entity.isDeadOrDying()) {
                boolean canResurrect = !JojoModUtil.isDyingBody(entity) && !JojoModUtil.isUndead(entity);
                return canResurrect ? ActionConditionResult.POSITIVE : conditionMessage("resurrect_dead");
            }
            
            if (!tissueItem) {
                if (
                        GoldExperienceCreateLifeform.getStuckArrows(entity) > 0 || 
                        GoldExperienceCreateLifeform.getStuckKnives(entity) > 0) {
                    return ActionConditionResult.POSITIVE;
                }
                
                ItemStack offHandItem = userGE.getOffhandItem();
                if (offHandItem.isEmpty()) {
                    return conditionMessage("ge_lifeform_material_only_item");
                }
                if (!GoldExperienceCreateLifeform.canGiveLifeTo(offHandItem)) {
                    return conditionMessage("ge_lifeform_material_item");
                }
            }
            
            
            if (!entity.hasEffect(ModStatusEffects.BLEEDING.get())) {
                int currentRegen = MCUtil.getEffectLevel(entity, regenEffectFor(entity));
                if (currentRegen >= effectMax) {
                    if (entity == userGE) {
                        return conditionMessage("ge_heal_stronger");
                    }
                    else {
                        return conditionMessage("ge_heal_stronger.other", entity.getDisplayName());
                    }
                }
                
                if (entity.getHealth() >= entity.getMaxHealth()) {
                    if (entity == userGE) {
                        return conditionMessage("ge_heal_full_hp");
                    }
                    else {
                        return conditionMessage("ge_heal_full_hp.other", entity.getDisplayName());
                    }
                }
            }
            
            return ActionConditionResult.POSITIVE;
        }
        
        return ActionConditionResult.NEGATIVE;
    }
    
    private static MobEffect regenEffectFor(LivingEntity entity) {
        if (entity instanceof Player && JojoModUtil.isPlayerUndead((Player) entity)) {
            return ModStatusEffects.UNDEAD_REGENERATION.get();
        }
        return MobEffects.REGENERATION;
    }
    
    public static void spendAndHeal(Level world, LivingEntity entity, 
            LivingEntity user, IStandPower userPower, StandEntity standEntity) {
        if (entity != null && !world.isClientSide()) {
            if (entity.isDeadOrDying() && entity != user) {
                boolean resurrect = entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(data -> {
                    if (data.soulEntity != null && data.soulEntity.isAlive()) {
                        int timeLeft = data.soulEntity.lifeSpan - data.soulEntity.tickCount;
                        return timeLeft <= 20 || entity.getRandom().nextFloat() <= 0.2F;
                    }
                    return false;
                }).orElse(false);
                
                if (resurrect) {
                    entity.setHealth(entity.getMaxHealth());
                    MCUtil.onLivingResurrect(entity);
                    entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(data -> data.setDyingBodyTimer(24000));
                }
                playHealSound(entity);
                return;
            }
            
            
            boolean stuckProjectile = false;
            if (GoldExperienceCreateLifeform.getStuckArrows(entity) > 0) {
                GoldExperienceCreateLifeform.decrementStuckArrow(entity);
                stuckProjectile = true;
            }
            else if (GoldExperienceCreateLifeform.getStuckKnives(entity) > 0) {
                GoldExperienceCreateLifeform.decrementStuckKnife(entity);
                stuckProjectile = true;
            }
            if (stuckProjectile) {
                if (JojoModUtil.isDyingBody(entity)) {
                    entity.setHealth(entity.getHealth() + 2.0F);
                }
                else {
                    entity.hurt(entity.level().damageSources().generic(), 0.0001F);
                    MobEffect regen = regenEffectFor(entity);
                    int lvl = Math.min(MCUtil.getEffectLevel(entity, regen) + 1, MAX_REGEN_LVL);
                    entity.addEffect(new MobEffectInstance(regen, HamonHealing.updateRegenEffect(entity, 105, lvl, regen), lvl));
                }
                playHealSound(entity);
                return;
            }
            
            
            ItemStack offHandItem = user.getOffhandItem();
            if (offHandItem.getItem() instanceof BucketItem) {
                BucketItem bucketType = (BucketItem) offHandItem.getItem();
                bucketType.checkExtraContent(user instanceof Player ? (Player) user : null, world, offHandItem, getControlledEntity(user, userPower).blockPosition());
            }
            if (!(user instanceof Player && ((Player) user).abilities.instabuild)) {
                offHandItem.shrink(1);
            }
            
            giveGEHealEffect(entity, userPower, 6000);
        }
    }
    
    public static void giveGEHealEffect(LivingEntity entity, IStandPower userPower, int durationMax) {
        playHealSound(entity);
        
        
        if (JojoModUtil.isDyingBody(entity)) {
            entity.setHealth(entity.getHealth() + 2.0F);
            return;
        }
        
        
        MobEffect regenEffect = regenEffectFor(entity);
        MobEffectInstance currentRegen = entity.getEffect(regenEffect);
        
        int lvl;
        int duration = durationMax;
        if (currentRegen != null) {
            lvl = currentRegen.getAmplifier() + 1;
            duration = HamonHealing.updateRegenEffect(entity, duration, lvl, regenEffect);
        }
        else {
            lvl = 0;
        }
        
        if (lvl <= MAX_REGEN_LVL) {
            if (userPower != null) {
                GEHealingEffect healingTracker = userPower.getContinuousEffects()
                        .getOrCreateEffect(ModStandEffects.GE_HEALING.get(), entity);
                healingTracker.fullHpTicks = 0;
                healingTracker.regenLevel = lvl;
                if (healingTracker.tickCount == 0 && currentRegen != null) {
                    healingTracker.prevEffect = new MobEffectInstance(currentRegen);
                }
            }
            
            MobEffectInstance newRegen = new MobEffectInstance(regenEffect, duration, lvl, false, true, true, currentRegen, java.util.Optional.empty());
            entity.addEffect(newRegen);
        }
        entity.hurt(entity.level().damageSources().generic(), 0.0001F);
        
        
        MobEffectInstance bleeding = entity.getEffect(ModStatusEffects.BLEEDING.get());
        if (bleeding != null) {
            int reduceDuration = durationMax / 20;
            MCUtil.reduceEffect(entity, ModStatusEffects.BLEEDING.get(), 
                    Mth.clamp(bleeding.getDuration() - reduceDuration, 0, reduceDuration), 1);
        }
    }
    
    public static void playHealSound(LivingEntity entity) {
        MCUtil.playSound(entity.level, null, entity, ModSounds.GOLD_EXPERIENCE_HEAL.get(), 
                SoundSource.AMBIENT, 1.0F, 0.95F + entity.getRandom().nextFloat() * 0.1F, StandUtil::playerCanHearStands);
    }
    
    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String postfix = getPostfix(power.getUser());
        String key = super.getTranslationKey(power, target);
        return postfix != null ? key + postfix : key;
    }
    
    protected String getPostfix(LivingEntity entityToHeal) {
        if (GoldExperienceCreateLifeform.getStuckArrows(entityToHeal) > 0) {
            return ".arrow";
        }
        
        if (GoldExperienceCreateLifeform.getStuckKnives(entityToHeal) > 0) {
            return ".knife";
        }
        
        return null;
    }
    
}
