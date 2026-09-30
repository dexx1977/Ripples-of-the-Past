package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;

import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerChunkCache.class)
public class ServerChunkProviderMixin {
    @Shadow @Final private ServerLevel level;

    @Inject(method = "isTickingChunk", at = @At("TAIL"), cancellable = true)
    public void jojoTsCancelBlockTick(BlockPos blockPos, CallbackInfoReturnable<Boolean> ci) {
        if (ci.getReturnValue() && TimeStopHandler.isTimeStopped(level, blockPos)
                && (/*cancelCommandBlocks || */ !(level.getBlockState(blockPos).getBlock() instanceof CommandBlock))) {
            ci.setReturnValue(false);
        }
    }

}
