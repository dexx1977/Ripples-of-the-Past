package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

public class SuperAjaStoneItem extends AjaStoneItem {

    public SuperAjaStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void useStone(Level world, LivingEntity player, ItemStack itemStack, float damage, boolean perk, boolean checkLight) {
        super.useStone(world, player, itemStack, damage * 4F, perk, checkLight);
    }

    @Override
    protected void breakItem(Level world, Player player, ItemStack itemStack, boolean perk) {
        if (!player.abilities.instabuild) {
            itemStack.hurtAndBreak(1, player, pl -> {
                pl.addItem(new ItemStack(Items.REDSTONE));
            });
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 50;
    }
    
    @Override
    protected int getCooldown() {
        return 250;
    }
    
    @Override
    protected float getHamonChargeCost() {
        return 1000;
    }
    
    protected SoundEvent getHamonChargeVoiceLine() {
        return ModSounds.LISA_LISA_SUPER_AJA.get();
    }
}
