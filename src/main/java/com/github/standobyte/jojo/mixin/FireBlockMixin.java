package com.github.standobyte.jojo.mixin;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "tryCatchFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    public void jojoOnFireRemovedBlock(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        cdRememberBurntBlock(pLevel, pPos);
    }
    
    @Inject(method = "tryCatchFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    public void jojoOnFireReplacedBlock(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        cdRememberBurntBlock(pLevel, pPos);
    }
    
    private static void cdRememberBurntBlock(Level world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, blockState, 
                Optional.ofNullable(world.getBlockEntity(blockPos)), Collections.emptyList());
    }
    
    // FIXME crossfire hurricane deleting blocks
    
}
