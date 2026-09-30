package com.github.standobyte.jojo.mixin.itemtracking.equip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntityMixin {
    
    protected MobEntityMixin(EntityType<? extends Mob> type, Level world) {
        super(type, world);
    }
    
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);
    
    @Override
    public void onTake(Entity entity, int amount, CallbackInfo ci) {
        if (amount == 1 && entity instanceof ItemEntity) {
            ItemStack item = ((ItemEntity) entity).getItem();
            if (!item.isEmpty()) {
                TrackerItemStack.getItemTracker(item).ifPresent(tracker -> {
                    tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(null);
                });
            }
        }
    }
    
    @Inject(method = "setItemSlot", at = @At("TAIL"))
    public void jojoOnMobItemEquip(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
        if (!level.isClientSide()) {
            TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_HAS_ITEM);
                if (this.getType() != EntityType.PIGLIN) {
                    tracker.setItemStillThereCheck(trackerId -> TrackerItemStack.trackerIdCheck(trackerId).test(this.getItemBySlot(pSlot)));
                }
            });
        }
    }
}
