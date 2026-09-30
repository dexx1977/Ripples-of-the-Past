package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;

public class PillarmanLightFlashDecoy extends PillarmanAction {

    public PillarmanLightFlashDecoy(NonStandAction.Builder builder) {
        super(builder);
        mode = Mode.LIGHT;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RAINBOW.get(), 10);
        }
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        int range = 16;
        if (!world.isClientSide()) {
        	user.addEffect(new MobEffectInstance(ModStatusEffects.FULL_INVISIBILITY.get(), 100, 0, false, false, false));
        }
        user.playSound(ModSounds.AJA_STONE_BEAM.get(), (float) (range + 16) / 16F, 1.0F); // TODO replace the light flash sound
        HamonUtil.createHamonSparkParticlesEmitter(user, 2F, 0, ParticleTypes.FLASH);
    }
    
    @Override
    public void startedHolding(Level world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
    	if (requirementsFulfilled) {
        	power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().setBladesVisible(true);
    	}
    }

    @Override
    public void stoppedHolding(Level world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
    	power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().setBladesVisible(false);
    }
    
    @Override
    public boolean clHeldStartAnim(Player user) {
        return ModPlayerAnimations.lightFlashDecoy.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.lightFlashDecoy.setAnimEnabled(user, false);
    }
    
}
