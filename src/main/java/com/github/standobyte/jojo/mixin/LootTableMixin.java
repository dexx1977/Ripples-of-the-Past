package com.github.standobyte.jojo.mixin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    public void jojoRememberBlockLoot(LootContext context, CallbackInfoReturnable<it.unimi.dsi.fastutil.objects.ObjectArrayList<ItemStack>> ci) {
        if (!LootContextParamSets.BLOCK.getRequired().stream().anyMatch(param -> !context.hasParam(param))) {
            Level world = context.getLevel();
            if (world != null) {
                List<ItemStack> generatedLoot = ci.getReturnValue();
                BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
                Optional<BlockEntity> tileEntity = Optional.ofNullable(context.getParamOrNull(LootContextParams.BLOCK_ENTITY));
                Vec3 posCenter = context.getParamOrNull(LootContextParams.ORIGIN);
                BlockPos blockPos = BlockPos.containing(posCenter);
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(
                        world, blockPos, blockState, tileEntity, world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) ? generatedLoot : Collections.emptyList());
            }
        }
    }
}
