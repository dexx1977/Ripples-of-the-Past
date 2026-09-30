package com.github.standobyte.jojo.action.non_stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.LazySupplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HamonProtection extends HamonAction {
    
    public HamonProtection(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getHeldAction() != this) {
            if (power.getEnergy() <= 0) {
                return conditionMessage("some_energy");
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    private final LazySupplier<ResourceLocation> protectionTex = 
            new LazySupplier<>(() -> makeIconVariant(this, "_on"));

    @Override
    public ResourceLocation getIconTexturePath(@Nullable INonStandPower power) {
        if (power != null && power.getTypeSpecificData(ModPowers.HAMON.get()).get().isProtectionEnabled()) {
            return protectionTex.get();
        }
        else {
            return super.getIconTexturePath(power);
        }
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {  
        if (!world.isClientSide()) {
            power.getTypeSpecificData(ModPowers.HAMON.get()).get().toggleHamonProtection();
        }
    }
    
    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return power.getTypeSpecificData(ModPowers.HAMON.get())
                .map(HamonData::isProtectionEnabled).orElse(false);
    }

    public float reduceDamageAmount(INonStandPower power, LivingEntity user, 
            DamageSource dmgSource, float dmgAmount) {
        float damageReductionMult = power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (user.getType() == ModEntityTypes.HAMON_MASTER.get()) {
                return 1f;
            }
            else {
                float energyCost = dmgAmount * 75;
                return hamon.consumeHamonEnergyTo(efficiency -> {
                    float baseReduction = 0.4F + hamon.getHamonControlLevelRatio() * 0.2F;
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, Math.min(energyCost, power.getEnergy()) * efficiency);
                    return Mth.clamp(baseReduction * efficiency, 0, 1);
                }, energyCost, getUnlockingSkill());
            }
        }).orElse(0f);
        
        if (damageReductionMult > 0) {
            float damageReduced = dmgAmount * damageReductionMult;
            
            Entity sourceEntity = dmgSource.getDirectEntity();
            Vec3 sourcePos = sourceEntity.getEyePosition(1.0F);
            AABB userHitbox = user.getBoundingBox();
            Vec3 damagePos;
            if (userHitbox.contains(sourcePos)) {
                damagePos = sourcePos;
            }
            else {
                Vec3 userEyePos = user.getEyePosition(1.0F);
                damagePos = userHitbox.clip(sourcePos, sourcePos.add(sourceEntity.getLookAngle().scale(16))).orElse(userEyePos);
            }
            HamonUtil.emitHamonSparkParticles(user.level, null, damagePos, damageReduced * 0.25F);
            return dmgAmount - damageReduced;
        }
        else {
            return dmgAmount;
        }
    }
}
