package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {

    @Inject(method = "Lnet/minecraft/block/AbstractBlock;getCollisionShape("
            + "Lnet/minecraft/block/BlockState;"
            + "Lnet/minecraft/world/IBlockReader;"
            + "Lnet/minecraft/util/math/BlockPos;"
            + "Lnet/minecraft/util/math/shapes/ISelectionContext;"
            + ")Lnet/minecraft/util/math/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void changeCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext, CallbackInfoReturnable<VoxelShape> ci) {}
}
