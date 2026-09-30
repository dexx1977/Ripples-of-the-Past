package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;

import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

@Mixin(BarrierBlock.class)
public abstract class BarrierBlockWallClimbMixin extends AbstractBlockMixin {

    @Override
    public void changeCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext, CallbackInfoReturnable<VoxelShape> ci) {
        if (HamonWallClimbing2.disableBlockCollisionShape(pContext)) {
            ci.setReturnValue(Shapes.empty());
        }
    }
    
}
