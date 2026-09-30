package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.non_stand.HamonHealing;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.TreeLeavesDecay;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class GoldExperienceBoneMeal extends StandEntityAction {

    public GoldExperienceBoneMeal(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        switch (target.getType()) {
        case ENTITY:
            Entity targetEntity = target.getEntity();
            return ActionConditionResult.noMessage(targetEntity instanceof Animal && ((Animal) targetEntity).isBaby());
        case BLOCK:
            Level world = user.level;
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof BonemealableBlock || TreeLeavesDecay.isTreeStemBlock(block)) {
                return ActionConditionResult.POSITIVE;
            }
            
            blockPos = blockPos.relative(target.getFace());
            blockState = world.getBlockState(blockPos);
            if (blockState.is(Blocks.WATER) && world.getFluidState(blockPos).getAmount() == 8) {
                return ActionConditionResult.POSITIVE;
            }

            return ActionConditionResult.NEGATIVE;
        default:
            return ActionConditionResult.NEGATIVE;
        }
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ANY;
    }
    
    @Override
    public void standPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        ActionTarget target = standEntity.aimWithThisOrUser(64, task.getTarget());
        standEntity.setTaskTarget(target);
        Entity targetEntity = target.getType() == TargetType.ENTITY ? target.getEntity() : null;
        
        if (targetEntity instanceof Animal) {
            Animal animal = (Animal) targetEntity;
            int age = animal.getAge();
            // particles don't appear if it's only called on the client side
            ((Animal) targetEntity).ageUp((int) ((-age / 20) * 0.2F), true);
        }
        
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            if (user instanceof Player && target.getType() == TargetType.BLOCK) {
                BlockPos blockPos = target.getBlockPos();
                TreeLeavesDecay tree = TreeLeavesDecay.startDecay(world, blockPos, Integer.MAX_VALUE, 1);
                if (tree != null) {
                    tree.logs.forEach(logPos -> {
                        world.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, logPos, 5);
                    });
                }
                
                Direction face = target.getType() == TargetType.BLOCK ? target.getFace() : Direction.UP;
                HamonHealing.bonemealEffect(user.level, (Player) user, blockPos, face);
            }
        }
    }
}
