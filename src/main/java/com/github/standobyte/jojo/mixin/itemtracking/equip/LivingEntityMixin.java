package com.github.standobyte.jojo.mixin.itemtracking.equip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Inject(method = "take", at = @At("HEAD"))
    public void onTake(Entity entity, int amount, CallbackInfo ci) {}
}
