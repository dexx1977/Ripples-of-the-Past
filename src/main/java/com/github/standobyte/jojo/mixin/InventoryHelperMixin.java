package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;

import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(Containers.class)
public abstract class InventoryHelperMixin {
    
    @SuppressWarnings("unlikely-arg-type")
    @Inject(method = "dropContents", at = @At("HEAD"), cancellable = true)
    private static void jojoKeepItemsOnTEBreak(Level world, BlockPos pos, Container teInventory, CallbackInfo ci) {
        if (GoldExperienceCreateLifeform.KEEP_ITEMS.contains(teInventory)) {
            ci.cancel();
        }
    }

}
