package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowEntityMixin extends Projectile {

    public AbstractArrowEntityMixin(EntityType<? extends Projectile> type, Level world) {
        super(type, world);
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setArrowCount(I)V"))
    public void jojoOnArrowStuck(EntityHitResult pResult, CallbackInfo ci) {
        SidedItemTrackerMap.getSidedTrackers(level).values().stream()
        .filter(tracker -> tracker.getAtEntity(level) == this)
        .forEach(tracker -> {
            tracker.setAtEntity(pResult.getEntity().getId(), level, KnownItemState.STUCK_ARROW);
        });
    }
}
