package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class HamonSpeedBoost extends HamonAction {

    public HamonSpeedBoost(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        float hamonEfficiency = hamon.getActionEfficiency(getEnergyCost(power, target), false, getUnlockingSkill());
        float effectStr = (float) hamon.getHamonControlLevel() / (float) HamonData.MAX_STAT_LEVEL * hamonEfficiency;
        int speedLvl = Mth.floor(1.5F * effectStr);
        int hasteLvl = Mth.floor(1.5F * effectStr);
        if (hamon.isSkillLearned(ModHamonSkills.AFTERIMAGES.get())) {
            speedLvl++;
            hasteLvl++;
        }
        if (!world.isClientSide()) {
            int duration = 20 + Mth.floor(180F * effectStr);
            if (hamonEfficiency == 1 && hamon.isSkillLearned(ModHamonSkills.AFTERIMAGES.get())) {
                user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.addAfterimages(Math.min((int) (effectStr * 7F / 1.5F), 7), duration);
                });
            }
            if (!user.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                hamon.hamonPointsFromAction(HamonStat.CONTROL, getEnergyCost(power, target));
            }
            user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, speedLvl));
            user.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, hasteLvl));
        }
        HamonUtil.emitHamonSparkParticles(world, user instanceof Player ? (Player) user : null, user.position(), (speedLvl + 1) * 0.25F);
    }
}
