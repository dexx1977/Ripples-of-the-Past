package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.effect.MobEffectCategory;

public class ResolveEffect extends StatusEffect {

    public ResolveEffect(MobEffectCategory type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        IStandPower.getStandPowerOptional(entity).ifPresent(stand -> {
            if (stand.usesResolve()) {
                stand.getResolveCounter().onResolveEffectStarted(amplifier);
            }
        });
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        IStandPower.getStandPowerOptional(entity).ifPresent(stand -> {
            if (stand.usesResolve()) {
                stand.getResolveCounter().onResolveEffectEnded(amplifier);
            }
        });
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.isInvisible()) {
            entity.level.addParticle(ModParticles.RESOLVE.get(), 
                    entity.getRandomX(2.5D), entity.getY(entity.getRandom().nextDouble() * 1.5), entity.getRandomZ(2.5D), 0, 0, 0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 3 == 0;
    }
}
