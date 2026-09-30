package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class PillarmanBladeBarrage extends PillarmanAction {

    public PillarmanBladeBarrage(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.LIGHT;
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!MCUtil.isHandFree(user, InteractionHand.MAIN_HAND)) {
            return conditionMessage("hand");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    public static boolean onUserAttacked(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getDirectEntity();
        if (attacker instanceof Projectile || attacker instanceof ModdedProjectileEntity) {
            LivingEntity targetLiving = event.getEntity();
            return INonStandPower.getNonStandPowerOptional(targetLiving).map(power -> 
            {Action<?> heldAction = power.getHeldAction(true);
                if (heldAction == ModPillarmanActions.PILLARMAN_BLADE_BARRAGE.get()) {
                    Level world = attacker.level;
                    if (attacker instanceof ModdedProjectileEntity) {
                        ModdedProjectileEntity projectile = (ModdedProjectileEntity) attacker;
                        return projectile.canBeEvaded(targetLiving) && (!projectile.standDamage());
                	}
                    world.getEntitiesOfClass(Projectile.class, targetLiving.getBoundingBox()
                    		.inflate(targetLiving.getAttributeValue(ForgeMod.REACH_DISTANCE.get())), 
                            entity -> entity.isAlive() && !entity.isPickable()).forEach(projectile -> {
                                if (targetLiving.getLookAngle().dot(projectile.getDeltaMovement().reverse().normalize())
                                        >= Mth.cos((float) (30.0 + Mth.clamp(10F, 0, 16) * 30.0 / 16.0) * MathUtil.DEG_TO_RAD)) {
                                	event.setCanceled(true);
                                	if (!projectile.onGround()) {
                                		PillarmanUtil.sparkEffect(projectile, 12);
                                    	world.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), 
                                    			SoundEvents.ANVIL_LAND, projectile.getSoundSource(), 0.4F, 1.35F);
                                	}
                                }
                            });
                    return false;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }
    
    @Override
    protected void holdTick(Level world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
        	Entity targetEntity = target.getEntity();
            switch (target.getType()) {
            case BLOCK:
                BlockPos pos = target.getBlockPos();
                if (!world.isClientSide() && JojoModUtil.canEntityDestroy((ServerLevel) world, pos, world.getBlockState(pos), user)) {
                    if (!world.isEmptyBlock(pos)) {
                        BlockState blockState = world.getBlockState(pos);
                        float digDuration = blockState.getDestroySpeed(world, pos);
                        boolean dropItem = true;
                        if (user instanceof Player) {
                            Player player = (Player) user;
                            digDuration /= player.getDigSpeed(blockState, pos);
                            if (player.abilities.instabuild) {
                                digDuration = 0;
                                dropItem = false;
                            }
                            else if (!ForgeHooks.canHarvestBlock(blockState, player, world, pos)) {
                                digDuration *= 10F / 3F;
//                                dropItem = false;
                            }
                        }
                        if (digDuration >= 0 && digDuration <= 2.5F * Math.sqrt(user.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                            MCUtil.destroyBlock(world, pos, dropItem, user);
                        }
                        else {
                            SoundType soundType = blockState.getSoundType(world, pos, user);
                            world.playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
                        }
                    }
                }
                break;
            case ENTITY:
            	if (targetEntity instanceof LivingEntity) {
            		LivingEntity targetLiving = (LivingEntity) targetEntity;
	                if (user instanceof Player) {
	                    int invulTicks = targetEntity.invulnerableTime;
	                    targetEntity.invulnerableTime = invulTicks;
	                }
	                if (!world.isClientSide()) {
	                    if (DamageUtil.hurtThroughInvulTicks(targetLiving, EntityDamageSource.playerAttack((Player) user), 
	                            (DamageUtil.getDamageWithoutHeldItem(user) * 0.2F))) {
	                    	PillarmanUtil.sparkEffect(targetLiving, 12);
	                    }
	                }
            	}
                break;
            default:
                break;
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.SILVER_CHARIOT_BARRAGE_SWIPE.get(), user.getSoundSource(), 0.5F, 1.0F);
        }
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (requirementsFulfilled) {
            if (ticksHeld % 2 == 0) {
                user.swinging = false;
                user.swing(InteractionHand.MAIN_HAND);
            }
        }
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
        return ModPlayerAnimations.bladeBarrage.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.bladeBarrage.setAnimEnabled(user, false);
    }
}
