package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class PillarmanUnnaturalAgility extends PillarmanAction {

    public PillarmanUnnaturalAgility(PillarmanAction.Builder builder) {
        super(builder.holdType());
        stage = 2;
    }

//    @Override
//    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
//        
//    }
    
    private static boolean canSeeStands(LivingEntity user) {
        return StandUtil.isEntityStandUser(user) || user.hasEffect(ModStatusEffects.SPIRIT_VISION.get());
    }
    
    public static boolean onUserAttacked(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getDirectEntity();
        if (!source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && (attacker instanceof LivingEntity || attacker instanceof Projectile)) {
            LivingEntity targetLiving = event.getEntity();
            return INonStandPower.getNonStandPowerOptional(targetLiving).map(power -> {
                Action<?> heldAction = power.getHeldAction(true);
                if (heldAction == ModPillarmanActions.PILLARMAN_UNNATURAL_AGILITY.get() 
                		|| heldAction == ModPillarmanActions.PILLARMAN_EVASION.get()) {
                    Level world = attacker.level;
                    if (attacker instanceof StandEntity && !canSeeStands(targetLiving)) {
                        return false;
                    }
                    if (attacker instanceof ModdedProjectileEntity) {
                        ModdedProjectileEntity projectile = (ModdedProjectileEntity) attacker;
                        return projectile.canBeEvaded(targetLiving) && (!projectile.standDamage() || canSeeStands(targetLiving));
                	}
                    if (power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_UNNATURAL_AGILITY.get() 
                    		&& attacker instanceof LivingEntity && !(attacker instanceof StandEntity)) {
                    	double counterAttack = Math.random();
                    	if (counterAttack < 0.3) {
                    		attacker.hurt(((Player) targetLiving).level().damageSources().playerAttack((Player) targetLiving), 
	                            (DamageUtil.getDamageWithoutHeldItem(targetLiving) * 0.75F));
                    	}
                    }
                    world.playSound(null, attacker, ModSounds.PILLAR_MAN_EVASION.get(), attacker.getSoundSource(), 1.0F, 1.0F);
                    return true;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
//            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.PILLAR_MAN_EVASION.get(), 1.0F, 1.25F, true, user, power, this);
        }
    }
    
    @Override
    public boolean clHeldStartAnim(Player user) {
        return ModPlayerAnimations.unnaturalAgility.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.unnaturalAgility.setAnimEnabled(user, false);
    }
}
