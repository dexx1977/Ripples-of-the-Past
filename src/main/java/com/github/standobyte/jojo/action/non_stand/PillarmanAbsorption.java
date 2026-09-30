package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

public class PillarmanAbsorption extends PillarmanAction {

    public PillarmanAbsorption(NonStandAction.Builder builder) {
        super(builder.holdType());
        stage = 2;
        canBeUsedInStone = true;
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        Entity entityTarget = target.getEntity();
        if (entityTarget instanceof LivingEntity) {
            return super.checkTarget(target, user, power);
        }
        return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void holdTick(Level world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide() && target.getEntity() instanceof LivingEntity) {
                LivingEntity targetEntity = (LivingEntity) target.getEntity();
                if (!targetEntity.isDeadOrDying()) {
                    boolean hurt = absorb(world, user, targetEntity, 2);
                    if (hurt) {
                        float bloodAndHealModifier = GeneralUtil.getOrLast(
                                JojoModConfig.getCommonConfigInstance(false).bloodDrainMultiplier.get(), 
                                world.getDifficulty().getId()).floatValue();
                        power.addEnergy(bloodAndHealModifier * 35F);
                    }
                }
            }
        }
    }

    private static final MobEffect[] BLOOD_DRAIN_EFFECTS = {
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.CONFUSION
    };
    
    public static boolean absorb(Level world, LivingEntity attacker, LivingEntity target, float absorbDamage) {
        if (HamonUtil.preventBlockDamage(target, attacker.level, null, null, 
                DamageUtil.damageSource(attacker, DamageUtil.PILLAR_MAN_ABSORPTION), absorbDamage)) {
            Vec3 userPos = attacker.getEyePosition(1.0F);
            double distanceToTarget = JojoModUtil.getDistance(attacker, target.getEntity().getBoundingBox());
            Vec3 targetPos = attacker.getEyePosition(1.0F).add(attacker.getLookAngle().scale(distanceToTarget));
            Vec3 particlesPos = userPos.add(targetPos.subtract(userPos).scale(0.5));
            if (world.isClientSide()) {
            	HamonSparksLoopSound.playSparkSound(attacker, particlesPos, 1.0F, true);
            	CustomParticlesHelper.createHamonSparkParticles(null, particlesPos, 1);
            }
            return false;
        }
        
        boolean hurt = DamageUtil.dealPillarmanAbsorptionDamage(target, absorbDamage, null);
        if (hurt) {
            for (MobEffect effect : BLOOD_DRAIN_EFFECTS) {
                int duration = Mth.floor(20F * absorbDamage);
                MobEffectInstance effectInstance = target.getEffect(effect);
                MobEffectInstance newInstance = effectInstance == null ? 
                        new MobEffectInstance(effect, duration, 1)
                        : new MobEffectInstance(effect, effectInstance.getDuration() + duration, 1);
                target.addEffect(newInstance);
            }
        }
        return hurt;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public double getMaxRangeSqEntityTarget() {
        return 4;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqStateChanged && reqFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.PILLAR_MAN_ABSORPTION.get(), 1.25F, 0.8F, true, user, power, this);
        }
    }
    
    @Override
    public boolean heldAllowsOtherAction(INonStandPower power, Action<INonStandPower> action) {
        return true;
    }
    
}
