package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.ITrackedArrowEntity;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStackProvider;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

@Mixin(Arrow.class)
public abstract class ArrowEntityMixin extends AbstractArrow implements ITrackedArrowEntity {
    @Nullable private Tag itemTrackerNBT;

    protected ArrowEntityMixin(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void saveItemTrackerNBT(Tag nbt) {
        this.itemTrackerNBT = nbt;
    }
    
    @Inject(method = "getPickupItem", at = @At("RETURN"))
    public void jojoTrackPickedUpArrow(CallbackInfoReturnable<ItemStack> ci) {
        if (this.itemTrackerNBT != null) {
            ItemStack item = ci.getReturnValue();
            if (!item.isEmpty()) {
                item.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(tracker -> tracker.fromNBT(itemTrackerNBT));
            }
        }
    }

}
