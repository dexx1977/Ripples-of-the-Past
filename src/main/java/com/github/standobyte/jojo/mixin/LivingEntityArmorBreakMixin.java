package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorBreakMixin extends Entity {

    public LivingEntityArmorBreakMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    protected abstract void hurtArmor(DamageSource damageSource, float damageAmount);

    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hurtArmor(Lnet/minecraft/util/DamageSource;F)V"))
    public void jojoBarrageLessArmorBreaking(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        if (!(damageSource instanceof IModdedDamageSource && ((IModdedDamageSource) damageSource).preventsDamagingArmor())) {
            hurtArmor(damageSource, damageAmount);
        }
    }

}
