package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

public class PillarmanRegeneration extends PillarmanAction {

    public PillarmanRegeneration(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
        canBeUsedInStone = true;
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int duration = 60;
            int level = 4;
            duration = HamonHealing.updateRegenEffect(user, duration, level, MobEffects.REGENERATION);
            user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, level, false, false, true));
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), ModSounds.PILLAR_MAN_STRONG_REGEN.get(), user.getSoundSource(), 1.5F, 1.2F);
        }
    }
}
