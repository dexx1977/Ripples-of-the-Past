package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

public class SoapItem extends Item {

    public SoapItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
        if (pEntity instanceof Player) {
            Inventory inventory = ((Player) pEntity).inventory;
            ItemStack emptyGloves = MCUtil.findInInventory(inventory, 
                    item -> !item.isEmpty() 
                    && item.getItem() == ModItems.BUBBLE_GLOVES.get()
                    && TommyGunItem.getAmmo(item) <= 0);
            if (!emptyGloves.isEmpty()) {
                BubbleGlovesItem.reload(emptyGloves, pEntity, pLevel, pStack);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
        super.finishUsingItem(pStack, pLevel, pEntityLiving);
        Player playerentity = pEntityLiving instanceof Player ? (Player)pEntityLiving : null;
        
        if (!pLevel.isClientSide) {
            pEntityLiving.addEffect(new MobEffectInstance(
                     MobEffects.POISON, 100, 0, false, true, true));
            pEntityLiving.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, 300, 1, false, true, true));
         }
        
        if (playerentity == null || !playerentity.abilities.instabuild) {
            if (pStack.isEmpty()) {
               return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (playerentity != null) {
               playerentity.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
            }
            pStack.shrink(1);
         }
           return pStack;
     }
    
    @Override
    public int getUseDuration(ItemStack pStack) {
        return 32;
     }

    @Override
     public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK;
     }
     
     @Override
     public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
     }

     @Override
     public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return ItemUtils.useDrink(pLevel, pPlayer, pHand);
     }
}
