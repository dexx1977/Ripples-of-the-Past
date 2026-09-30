package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.util.mc.CustomVillagerTrades;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(MerchantResultSlot.class)
public abstract class MerchantResultSlotMixin {
    @Shadow @Final public MerchantContainer slots;
    
    @Inject(method = "onTake", at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/entity/player/PlayerEntity;awardStat(Lnet/minecraft/util/ResourceLocation;)V", 
            ordinal = 0))
    public void jojoOnVillagerTrade(Player pPlayer, ItemStack pStack, CallbackInfoReturnable<ItemStack> ci) {
        CustomVillagerTrades.onTrade(pPlayer, pStack, slots, slots.getActiveOffer());
    }

}
