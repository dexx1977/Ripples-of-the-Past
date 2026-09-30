package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

public class PillarmanLightFlash extends PillarmanAction {

    public PillarmanLightFlash(NonStandAction.Builder builder) {
        super(builder);
        mode = Mode.LIGHT;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled && ticksHeld > 10) {
        	for (int i = 0; i <= 24; i++) {
        		user.level.addParticle(ModParticles.LIGHT_SPARK.get(), true, user.getX(), user.getY() + 0.8, user.getZ(), 
        				(Math.random() - 0.5F) / 4, (Math.random() - 0.5F) / 4, (Math.random() - 0.5F) / 4);
            }
        }
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        int range = 16;
        if (!world.isClientSide()) {
            for (LivingEntity entity : MCUtil.entitiesAround(
                    LivingEntity.class, user, range, false, entity -> 
                    entity.canSee(user) && !(entity instanceof StandEntity && user.is(((StandEntity) entity).getUser())))) {
                if (user.distanceTo(entity) < 5) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, true, true, false));
                } else {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, true, true, false));
                }
                if (!(entity instanceof Player) && !(entity instanceof StandEntity)) {
                    entity.addEffect(new MobEffectInstance(ModStatusEffects.STUN.get(), 60, 0, true, true, false));
                }
            }
        }
        user.playSound(ModSounds.AJA_STONE_BEAM.get(), (float) (range + 16) / 16F, 1.0F); // TODO replace the light flash sound
        HamonUtil.createHamonSparkParticlesEmitter(user, 2F, 0, ModParticles.LIGHT_MODE_FLASH.get());
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
        return ModPlayerAnimations.lightFlash.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.lightFlash.setAnimEnabled(user, false);
    }
}
