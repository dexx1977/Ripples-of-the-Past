package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class PillarmanWindCloak extends PillarmanAction {

    public PillarmanWindCloak(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.WIND;
    }
    
    @Override
    public void holdTick(Level world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
        	user.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, false, false));
            user.addEffect(new MobEffectInstance(ModStatusEffects.SUN_RESISTANCE.get(), 5, 0, false, false));
        }
    }
      
    @Override
    public void startedHolding(Level world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
    	if (requirementsFulfilled) {
    		windEffect(user, ModParticles.SANDSTORM.get(), 15);
    	}
    }
    
    @Override
    public void stoppedHolding(Level world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
    	windEffect(user, ModParticles.SANDSTORM.get(), 15);
    }
    
    public static void windEffect(LivingEntity user, ParticleOptions particles, int intensity) {
        for (int i = 0; i < intensity; i++) {
            Vec3 particlePos = user.position().add(
                    (Math.random() - 0.5) * (user.getBbWidth() + 0.5), 
                    Math.random() * (user.getBbHeight()), 
                    (Math.random() - 0.5) * (user.getBbWidth() + 0.5));
            user.level.addParticle(particles, particlePos.x, particlePos.y, particlePos.z, Math.random() - 0.5, Math.random(), Math.random() - 0.5);
        }
    }
}
