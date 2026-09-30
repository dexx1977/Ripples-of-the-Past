package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBloodCutterEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class CrazyDiamondBloodCutter extends StandEntityAction {
    public static final StandPose BLOOD_CUTTER_SHOT_POSE = new StandPose("bloodCutter");

    public CrazyDiamondBloodCutter(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user.getHealth() >= user.getMaxHealth()
                && !(user instanceof Player && ((Player) user).abilities.invulnerable)) {
            return conditionMessage("full_health");
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    @Override
    public void standPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            CDBloodCutterEntity cutter = new CDBloodCutterEntity(user, world);
            cutter.setShootingPosOf(user);
            cutter.shootFromRotation(user, 2.0F, standEntity.getProjectileInaccuracy(1.0F));
            standEntity.addProjectile(cutter);
        }
    }

    @Override
    protected int getCooldownAdditional(IStandPower power, int ticksHeld) {
        int cooldown = super.getCooldownAdditional(power, ticksHeld);
        if (!power.isUserCreative() && power.getUser() != null) {
            LivingEntity user = power.getUser();
            cooldown = Mth.ceil((float) cooldown * user.getHealth() / user.getMaxHealth());
        }
        return cooldown;
    }
    
    @Override
    protected void playSoundAtStand(Level world, StandEntity standEntity, SoundEvent sound, IStandPower standPower, Phase phase) {
        if (world.isClientSide() && phase == Phase.WINDUP && sound == ModSounds.CRAZY_DIAMOND_FIX_STARTED.get()) {
            ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, sound, this, phase, 1.0F, 1.0F, false);
        }
        else {
            super.playSoundAtStand(world, standEntity, sound, standPower, phase);
        }
    }
}
