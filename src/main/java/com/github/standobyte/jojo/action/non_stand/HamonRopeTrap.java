package com.github.standobyte.jojo.action.non_stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.google.common.collect.ImmutableMap;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class HamonRopeTrap {
    
    public static boolean ropeTrap(LivingEntity user, BlockPos pos, BlockState blockState, Level world, INonStandPower power, HamonData hamon) {
        if (hamon.isSkillLearned(ModHamonSkills.ROPE_TRAP.get())) {
            float efficiency = hamon.getActionEfficiency(STRING_CHARGE_COST, false, ModHamonSkills.ROPE_TRAP.get());
            createChargedCobweb(user, pos, blockState, world, 64, null, power, 
                    40 + (int) ((float) (160 * hamon.getHamonStrengthLevel()) / (float) HamonData.MAX_STAT_LEVEL * efficiency), 
                    0.02F * hamon.getHamonDamageMultiplier() * efficiency, hamon);
            return true;
        }
        return false;
    }
    
    private static final Map<BooleanProperty, Direction> PROPERTY_TO_DIRECTION = ImmutableMap.of(
            TripWireBlock.EAST, Direction.EAST, 
            TripWireBlock.SOUTH, Direction.SOUTH, 
            TripWireBlock.WEST, Direction.WEST, 
            TripWireBlock.NORTH, Direction.NORTH);
    private static final float STRING_CHARGE_COST = 10;
    private static void createChargedCobweb(LivingEntity user, BlockPos pos, BlockState blockState, Level world, 
            int range, @Nullable Direction from, INonStandPower power, int chargeTicks, float charge, HamonData hamon) {
        if (range > 0 && blockState.getBlock() == Blocks.TRIPWIRE && power.consumeEnergy(STRING_CHARGE_COST)) {
            hamon.hamonPointsFromAction(HamonStat.CONTROL, STRING_CHARGE_COST / 2);
            Map<Property<?>, Comparable<?>> values = blockState.getValues();
            List<Direction> directions = new ArrayList<Direction>();
            for (Map.Entry<BooleanProperty, Direction> entry: PROPERTY_TO_DIRECTION.entrySet()) {
                if (entry.getValue() != from && (Boolean) values.get(entry.getKey())) {
                    directions.add(entry.getValue());
                }
            }
            world.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
            HamonBlockChargeEntity chargeEntity = new HamonBlockChargeEntity(world, pos);
            chargeEntity.setCharge(charge, chargeTicks, user, STRING_CHARGE_COST / 2);
            world.addFreshEntity(chargeEntity);
            for (Direction direction : directions) {
                BlockPos nextPos = pos.relative(direction);
                createChargedCobweb(user, nextPos, world.getBlockState(nextPos), world, 
                        range - 1, direction.getOpposite(), power, chargeTicks, charge, hamon);
            }
        }
    }
}
