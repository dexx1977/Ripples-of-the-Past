package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class HamonSpreadEffect extends StatusEffect implements IApplicableEffect {

    public HamonSpreadEffect(MobEffectCategory type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level.isClientSide() && livingEntity instanceof FlyingMob) {
            double gravity = livingEntity.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
            Vec3 deltaMovement = livingEntity.getDeltaMovement();
            livingEntity.setDeltaMovement(new Vec3(deltaMovement.x, -gravity * (amplifier + 1) * 0.5, deltaMovement.z));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return JojoModUtil.isAffectedByHamon(entity);
    }
    
    
    
    public static float reduceUndeadHealing(MobEffectInstance effectInstance, float healAmount) {
        float multiplier = 1 - (float) Math.min(effectInstance.getAmplifier() + 1, 5) * 0.2F;
        return healAmount * multiplier;
    }
    
    public static void giveEffectTo(LivingEntity entity, int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, false, true));
        entity.addEffect(new MobEffectInstance(ModStatusEffects.HAMON_SPREAD.get(), duration, amplifier, false, false, false));
    }
}
