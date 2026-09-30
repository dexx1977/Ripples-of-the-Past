package com.github.standobyte.jojo.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

public class MagiciansRedFireBlock extends FireBlock {

    public MagiciansRedFireBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockGetter world, BlockPos blockPos) {
        return super.getStateForPlacement(world, blockPos);
    }
    
//    @Override
//    public void animateTick(BlockState blockState, World world, BlockPos blockPos, Random rand) {
//        if (ClientUtil.canSeeStands()) {
//            super.animateTick(blockState, world, blockPos, rand);
//        }
//    }
    
    @Override
    public BlockState getStateWithAge(LevelAccessor pLevel, BlockPos pPos, int pAge) {
        return getStateForPlacement(pLevel, pPos).setValue(AGE, Integer.valueOf(pAge));
    }
}
