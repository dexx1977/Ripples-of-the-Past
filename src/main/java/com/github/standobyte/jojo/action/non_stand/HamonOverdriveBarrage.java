package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;

public class HamonOverdriveBarrage extends HamonAction {

    public HamonOverdriveBarrage(HamonAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!MCUtil.areHandsFree(user, InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)) {
            return conditionMessage("hands");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void holdTick(Level world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
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
                            else if (!ForgeHooks.isCorrectToolForDrops(blockState, player)) {
                                digDuration *= 10F / 3F;
//                                dropItem = false;
                            }
                        }
                        if (digDuration >= 0 && digDuration <= 2.5F * Math.sqrt(user.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                            MCUtil.destroyBlock(world, pos, dropItem, user);
                            power.getTypeSpecificData(ModPowers.HAMON.get()).get().hamonPointsFromAction(HamonStat.STRENGTH, getHeldTickEnergyCost(power));
                        }
                        else {
                            SoundType soundType = blockState.getSoundType(world, pos, user);
                            world.playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
                        }
                    }
                }
                break;
            case ENTITY:
                Entity targetEntity = target.getEntity();
                
                double atkAttribute = user.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double enchBonus;
                if (targetEntity instanceof LivingEntity) {
                    enchBonus = EnchantmentHelper.getDamageBonus(user.getMainHandItem(), ((LivingEntity) targetEntity).getMobType());
                } else {
                    enchBonus = EnchantmentHelper.getDamageBonus(user.getMainHandItem(), MobType.UNDEFINED);
                }
                atkAttribute += enchBonus;
                double strength = atkAttribute + 8;
                double speed = user.getAttributeValue(Attributes.ATTACK_SPEED) + 8;
                float damage = StandStatFormulas.getBarrageHitDamage(strength, 0) * StandStatFormulas.getBarrageHitsPerSecond(speed) / 20;
                
                if (user instanceof Player) {
                    int invulTicks = targetEntity.invulnerableTime;
                    attack((Player) user, targetEntity, damage, enchBonus > 0);
                    targetEntity.invulnerableTime = invulTicks;
                }
                if (!world.isClientSide()) {
                    DamageUtil.dealHamonDamage(targetEntity, 0.1F, user, null, null);
                }
                break;
            default:
                break;
            }
        }
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            if (ticksHeld % 2 == 0) {
                user.swinging = false;
                user.swing(ticksHeld % 4 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            }
        }
    }

    @Override
    public boolean clHeldStartAnim(Player user) {
        return ModPlayerAnimations.playerBarrageAnim.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.playerBarrageAnim.setAnimEnabled(user, false);
    }
    
    

    public static void attack(Player attacker, Entity target, float damage, boolean sharpnessParticles) {
        if (!ForgeHooks.onPlayerAttackTarget(attacker, target)) return;
        if (target.isAttackable()) {
            if (!target.skipAttackInteraction(attacker)) {
                if (damage > 0.0F) {
                    float kbValue = (float)attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK); // Forge: Initialize attacker value to the attack knockback attribute of the player, which is by default 0
                    kbValue = kbValue + EnchantmentHelper.getKnockbackBonus(attacker);

                    float targetHp = 0.0F;
                    boolean setOnFire = false;
                    int fireAspect = EnchantmentHelper.getFireAspect(attacker);
                    if (target instanceof LivingEntity) {
                        targetHp = ((LivingEntity)target).getHealth();
                        if (fireAspect > 0 && !target.isOnFire()) {
                            setOnFire = true;
                            target.setSecondsOnFire(1);
                        }
                    }

                    Vec3 speed = target.getDeltaMovement();
                    boolean dealtDamage = target.hurt(attacker.level().damageSources().playerAttack(attacker), damage);
                    if (dealtDamage) {
                        if (kbValue > 0) {
                            if (target instanceof LivingEntity) {
                                ((LivingEntity)target).knockback((float)kbValue * 0.5F, (double)Mth.sin(attacker.yRot * ((float)Math.PI / 180F)), (double)(-Mth.cos(attacker.yRot * ((float)Math.PI / 180F))));
                            } else {
                                target.push((double)(-Mth.sin(attacker.yRot * ((float)Math.PI / 180F)) * (float)kbValue * 0.5F), 0.1D, (double)(Mth.cos(attacker.yRot * ((float)Math.PI / 180F)) * (float)kbValue * 0.5F));
                            }

                            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            attacker.setSprinting(false);
                        }

                        if (target instanceof ServerPlayer && target.hurtMarked) {
                            ((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
                            target.hurtMarked = false;
                            target.setDeltaMovement(speed);
                        }

                        attacker.level.playSound((Player)null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, attacker.getSoundSource(), 1.0F, 1.0F);

                        if (sharpnessParticles) {
                            attacker.magicCrit(target);
                        }

                        attacker.setLastHurtMob(target);
                        if (target instanceof LivingEntity) {
                            EnchantmentHelper.doPostHurtEffects((LivingEntity)target, attacker);
                        }

                        EnchantmentHelper.doPostDamageEffects(attacker, target);
                        ItemStack heldItem = attacker.getMainHandItem();
                        Entity actualTarget = target;
                        if (target instanceof PartEntity) {
                            actualTarget = ((PartEntity<?>) target).getParent();
                        }

                        if (!attacker.level.isClientSide && !heldItem.isEmpty() && actualTarget instanceof LivingEntity) {
                            ItemStack copy = heldItem.copy();
                            heldItem.hurtEnemy((LivingEntity)actualTarget, attacker);
                            if (heldItem.isEmpty()) {
                                ForgeEventFactory.onPlayerDestroyItem(attacker, copy, InteractionHand.MAIN_HAND);
                                attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (target instanceof LivingEntity) {
                            float f5 = targetHp - ((LivingEntity)target).getHealth();
                            attacker.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                            if (fireAspect > 0) {
                                target.setSecondsOnFire(fireAspect * 4);
                            }

                            if (attacker.level instanceof ServerLevel && f5 > 2.0F) {
                                int k = (int)((double)f5 * 0.5D);
                                ((ServerLevel)attacker.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5D), target.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        attacker.causeFoodExhaustion(0.1F);
                    } else {
                        attacker.level.playSound((Player)null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource(), 1.0F, 1.0F);
                        if (setOnFire) {
                            target.clearFire();
                        }
                    }
                }

            }
        }
    }
}
