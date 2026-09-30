package com.github.standobyte.jojo.mixin.itemtracking.equip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ArmorStand.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {
    
    protected ArmorStandEntityMixin(EntityType<? extends Mob> type, Level world) {
        super(type, world);
    }
    
    @Inject(method = "setItemSlot", at = @At("TAIL"))
    public void jojoOnArmorStandItemEquip(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
        if (!level.isClientSide()) {
            TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> TrackerItemStack.trackerIdCheck(trackerId).test(this.getItemBySlot(pSlot)));
            });
        }
    }
}
