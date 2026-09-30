package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.LiquidOnlyRayTraceContext;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class VampirismFreeze extends VampirismAction {

    public VampirismFreeze(NonStandAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        if (user.isOnFire()) {
            return conditionMessage("fire");
        }
        if (user.level.dimensionType().ultraWarm()) {
            return conditionMessage("ultrawarm");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void holdTick(Level world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            if (target.getType() == TargetType.ENTITY) {
                Entity entityTarget = target.getEntity();
                if (entityTarget instanceof LivingEntity && !entityTarget.isOnFire()) {
                    int difficulty = world.getDifficulty().getId();
                    LivingEntity targetLiving = (LivingEntity) entityTarget;
                    float damage = (float) Math.pow(2, difficulty) * 0.5f;
                    if (targetLiving.getType() == EntityType.SKELETON && targetLiving.isAlive() && targetLiving.getHealth() <= damage) {
                        turnSkeletonIntoStray(targetLiving);
                    }
                    else if (DamageUtil.dealColdDamage(targetLiving, damage, user, null)) {
                        MobEffectInstance freezeInstance = targetLiving.getEffect(ModStatusEffects.FREEZE.get());
                        if (freezeInstance == null) {
                            world.playSound(null, targetLiving, ModSounds.VAMPIRE_FREEZE.get(), targetLiving.getSoundSource(), 1.0F, 1.0F);
                            targetLiving.addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), (difficulty + 1) * 50, 0));
                        }
                        else {
                            int additionalDuration = (difficulty - 1) * 5 + 1;
                            int duration = freezeInstance.getDuration() + additionalDuration;
                            int lvl = duration / 100;
                            targetLiving.addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), duration, lvl));
                        }
                    }
                }
            }
            frostWalkerImitation(user, world, user.blockPosition(), 4);
        }
    }
    
    public static boolean turnSkeletonIntoStray(LivingEntity skeleton) {
        if (skeleton.level.isClientSide()) return false;
        ServerLevel world = (ServerLevel) skeleton.level;
        if ((world.getDifficulty() == Difficulty.NORMAL && skeleton.getRandom().nextBoolean() || world.getDifficulty() == Difficulty.HARD)) {
            Stray stray = null;
            if (ForgeEventFactory.canLivingConvert(skeleton, EntityType.STRAY, (timer) -> {})) {
                stray = ((Mob) skeleton).convertTo(EntityType.STRAY, true);
            }
            else {
                return false;
            }
//            stray.finalizeSpawn(
//                    world, 
//                    world.getCurrentDifficultyAt(stray.blockPosition()), 
//                    SpawnReason.CONVERSION, 
//                    null, 
//                    null);
            ForgeEventFactory.onLivingConvert(skeleton, stray);
            if (!skeleton.isSilent()) {
                world.levelEvent(null, 1026, skeleton.blockPosition(), 0);
            }
            return true;
        }
        return false;
    }
    
    private void frostWalkerImitation(LivingEntity entity, Level world, BlockPos entityPos, float radius) {
        if (entity.onGround()) {
            BlockPos.MutableBlockPos posMutable = new BlockPos.MutableBlockPos();
            for (BlockPos blockPos : BlockPos.betweenClosed(entityPos.offset(-radius, -1.0, -radius), entityPos.offset(radius, -1.0, radius))) {
                if (blockPos.closerThan(entity.position(), (double) radius)) {
                    posMutable.set(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
                    BlockState blockState = world.getBlockState(posMutable);
                    if (blockState.getBlock().isAir()) {
                        freezeWaterBlock(world, blockPos, entity);
                    }
                }
            }
        }

        Vec3 eyePos = entity.getEyePosition(1.0F);
        Vec3 lookVec = entity.getLookAngle();
        HitResult rayTraceResult = world.clip(new LiquidOnlyRayTraceContext(
                eyePos.add(lookVec), eyePos.add(lookVec.scale(8)), ClipContext.Fluid.SOURCE_ONLY, entity));
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            freezeWaterBlock(world, ((BlockHitResult) rayTraceResult).getBlockPos(), entity);
        }
    }

    private static final BlockState ICE = Blocks.FROSTED_ICE.defaultBlockState();
    private void freezeWaterBlock(Level world, BlockPos blockPos, LivingEntity vampireEntity) {
        BlockState blockState = world.getBlockState(blockPos);
        boolean isFull = blockState.getBlock() == Blocks.WATER && blockState.getValue(LiquidBlock.LEVEL) == 0;
        if (isFull && ICE.canSurvive(world, blockPos)
                && world.isUnobstructed(ICE, blockPos, CollisionContext.empty())
                && !ForgeEventFactory.onBlockPlace(vampireEntity, BlockSnapshot.create(world.dimension(), world, blockPos), Direction.UP)) {
            world.setBlockAndUpdate(blockPos, ICE);
            world.getBlockTicks().scheduleTick(blockPos, Blocks.FROSTED_ICE, Mth.nextInt(vampireEntity.getRandom(), 20, 40));
        }
    }

    public static boolean onUserAttacked(LivingAttackEvent event) {
        Entity attacker = event.getSource().getDirectEntity();
        if (attacker instanceof LivingEntity && !attacker.isOnFire() && !DamageUtil.isImmuneToCold(attacker)) {
            LivingEntity targetLiving = event.getEntity();
            return INonStandPower.getNonStandPowerOptional(targetLiving).map(power -> {
                if (power.getHeldAction(true) == ModVampirismActions.VAMPIRISM_FREEZE.get()) {
                    Level world = attacker.level;
                    int difficulty = world.getDifficulty().getId();
                    ((LivingEntity) attacker).addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), difficulty * 100, difficulty));
                    world.playSound(null, attacker, ModSounds.VAMPIRE_FREEZE.get(), attacker.getSoundSource(), 1.0F, 1.0F);
                    return true;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }
    
    @Override
    public double getMaxRangeSqEntityTarget() {
        return 4;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            Vec3 particlePos = user.position().add(
                    (Math.random() - 0.5) * (user.getBbWidth() + 1.0), 
                    Math.random() * (user.getBbHeight() + 1.0), 
                    (Math.random() - 0.5) * (user.getBbWidth() + 1.0));
            user.level.addParticle(ParticleTypes.CLOUD, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
    }
    
    @Override
    public boolean heldAllowsOtherAction(INonStandPower power, Action<INonStandPower> action) {
        return true;
    }
    
    @Override
    protected int maxCuringStage() {
        return 1;
    }
    
}
