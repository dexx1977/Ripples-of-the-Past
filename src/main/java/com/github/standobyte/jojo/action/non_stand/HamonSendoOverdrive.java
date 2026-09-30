package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.general.ObjectWrapper;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HamonSendoOverdrive extends HamonAction {

    public HamonSendoOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
//    @Override
//    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
//        if (target.getEntity() instanceof LivingEntity && !getTargetRequirement().checkTargetType(target.getType())) {
//            return ModHamonActions.HAMON_OVERDRIVE.get().getVisibleAction(power, target);
//        }
//        return super.replaceAction(power, target);
//    }
    
    @Override
    public void overrideVanillaMouseTarget(ObjectWrapper<ActionTarget> targetContainer, Level world, LivingEntity user, INonStandPower power) {
        ActionTarget target = targetContainer.get();
        if (target.getType() == TargetType.BLOCK) {
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
                Vec3 pos1 = user.getEyePosition(1.0F);
                Vec3 pos2 = pos1.add(user.getViewVector(1.0F).scale(Math.sqrt(getMaxRangeSqBlockTarget())));
                HitResult targetCollisionBlocks = user.level.clip(new ClipContext(
                        pos1, pos2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user)); // to not target plant blocks like grass
                targetContainer.set(ActionTarget.fromRayTraceResult(targetCollisionBlocks));
            }
        }
    }
    
    @Override
    public void stoppedHolding(Level world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
        ActionTarget target = power.getMouseTarget();
        if (target.getType() == TargetType.BLOCK) {
            if (!world.isClientSide()) {
                BlockPos blockPos = target.getBlockPos();
                Direction face = target.getFace();
                
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                float energyCost = getEnergyCost(power, target);
                float hamonEfficiency = hamon.getActionEfficiency(energyCost, false, getUnlockingSkill());
                
                HamonSendoOverdriveEntity sendoOverdrive = new HamonSendoOverdriveEntity(world, 
                        user, face.getAxis());
                float heldRatio = Mth.clamp((float) (power.getHeldActionTicks() - 1) / this.getHoldDurationToFire(power), 0, 1);
                sendoOverdrive.yRot = user.yRot;
                sendoOverdrive.xRot = user.xRot;
                sendoOverdrive.sparksAngle = (float) Math.PI / 4 + heldRatio * (float) Math.PI / 4 * 7;
                sendoOverdrive.radius = (2 + hamon.getHamonControlLevelRatio() * 3) * hamonEfficiency;
                sendoOverdrive.damage = 0.75F * hamonEfficiency;
                sendoOverdrive.setWavesCount(2 + (int) ((2 + Math.min(hamon.getHamonControlLevelRatio() * 3, 2)) * hamonEfficiency));
                sendoOverdrive.setStatPoints(Math.min(energyCost, power.getEnergy()) * hamonEfficiency);
                        
                sendoOverdrive.moveTo(Vec3.atCenterOf(blockPos).subtract(0, sendoOverdrive.getDimensions(null).height * 0.5, 0));
                sendoOverdrive.setBlockTarget(target.getBlockPos(), target.getFace());
                world.addFreshEntity(sendoOverdrive);
                
                if (!willFire) power.consumeEnergy(energyCost);
            }
            user.swing(InteractionHand.MAIN_HAND, false);
        }
    }
}
