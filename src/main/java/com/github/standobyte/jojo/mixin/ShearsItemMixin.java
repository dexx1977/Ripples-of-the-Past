package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

    @Inject(method = "interactLivingEntity", at = @At(
            value = "INVOKE", remap = false, 
            target = "Lnet/minecraftforge/common/IForgeShearable;onSheared("
                    + "Lnet/minecraft/entity/player/PlayerEntity;"
                    + "Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/util/math/BlockPos;"
                    + "I"
                    + ")Ljava/util/List;"), 
            cancellable = true)
    public void nuEtoUzheSovsemPizdecKakoiTo(ItemStack stack, Player playerIn, LivingEntity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        boolean isGELifeform = StandEffectsTracker.getEffectsTargetedBy(entity, ModStandEffects.GE_CREATED_LIFEFORM.get()).findAny().isPresent();
        if (isGELifeform) {
            playerIn.hurt(playerIn.level().damageSources().playerAttack(playerIn), 1);
            ci.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
