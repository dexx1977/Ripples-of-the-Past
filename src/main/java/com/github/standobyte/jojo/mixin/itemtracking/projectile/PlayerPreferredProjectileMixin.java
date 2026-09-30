package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerPreferredProjectileMixin extends LivingEntity {
    
    protected PlayerPreferredProjectileMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }
    
    private static final Predicate<ItemStack> JOJO_PREFER_TRACKED_PROJECTILES = 
            item -> TrackerItemStack.getItemTracker(item).map(TrackerItemStack::isTracked).orElse(false);
    @ModifyVariable(method = "getProjectile", at = @At("STORE"), ordinal = 1)
    public ItemStack jojoPreferShootableProjectile(ItemStack offHandAmmo, ItemStack pShootable) {
        if (JOJO_PREFER_TRACKED_PROJECTILES.test(offHandAmmo)) {
            return offHandAmmo;
        }
        
        Player player = (Player) ((LivingEntity) this);
        Predicate<ItemStack> predicate = ((ProjectileWeaponItem) pShootable.getItem()).getAllSupportedProjectiles().and(JOJO_PREFER_TRACKED_PROJECTILES);
        
        for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
            ItemStack invTrackedAmmo = player.inventory.getItem(i);
            if (predicate.test(invTrackedAmmo)) {
                return invTrackedAmmo;
            }
        }
        
        return offHandAmmo;
    }
}
