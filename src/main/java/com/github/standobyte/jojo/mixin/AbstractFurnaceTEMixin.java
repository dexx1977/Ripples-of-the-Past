package com.github.standobyte.jojo.mixin;

import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceTEMixin extends BlockEntity {
    
    public AbstractFurnaceTEMixin(BlockEntityType<?> type, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(type, pos, state);
    }
    
    @Inject(method = "getRecipesToAwardAndPopExperience", at = @At("HEAD"), cancellable = true)
    public void jojoKeepXpOnTEBreak(net.minecraft.server.level.ServerLevel world, Vec3 pos, CallbackInfoReturnable<List<Recipe<?>>> ci) {
        if (GoldExperienceCreateLifeform.KEEP_ITEMS.contains(this)) {
            ci.setReturnValue(Collections.emptyList());
        }
    }
}
