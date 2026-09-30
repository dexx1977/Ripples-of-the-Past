package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends Projectile {

    public FireworkRocketEntityMixin(EntityType<? extends Projectile> p_i231584_1_, Level p_i231584_2_) {
        super(p_i231584_1_, p_i231584_2_);
    }
    
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    public void onEntityCreated(Level world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        TrackerItemStack.getItemTracker(stack).ifPresent(tracker -> {
            tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_IS_ITEM);
            tracker.setItemStillThereCheck(null);
        });
    }

}
