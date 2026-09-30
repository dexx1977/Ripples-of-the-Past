package com.github.standobyte.jojo.item;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

public class BubbleGlovesItem extends GlovesItem {

    public BubbleGlovesItem(Properties properties) {
        super(properties);
    }
    
    public static final int MAX_AMMO = 500;
    
    public static boolean consumeAmmo(ItemStack gloves, int amount, LivingEntity user) {
        boolean consumed = TommyGunItem.consumeAmmo(gloves, amount);
        
        if (TommyGunItem.getAmmo(gloves) <= 0) {
            reload(gloves, user, user.level, null);
        }
        
        return consumed;
    }
    
    public static boolean reload(ItemStack glovesItem, Entity entity, Level world, @Nullable ItemStack soapBottleItem) {
        int ammoToLoad = MAX_AMMO - TommyGunItem.getAmmo(glovesItem);
        if (ammoToLoad > 0) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                ammoToLoad = 500;
                Container inventory = player.inventory;
                ItemStack soapItem = null;
                soapItem = soapBottleItem != null ? soapBottleItem : MCUtil.findInInventory(inventory, item -> useSoap(item));
                if (!player.abilities.instabuild) {
                    if (!soapItem.isEmpty()) {
                        soapItem.shrink(1);
                        MCUtil.giveItemTo(player, new ItemStack(Items.GLASS_BOTTLE), true);
                    } else {
                        return false;
                    }
                }
                if (!world.isClientSide()) {
                    glovesItem.getTag().putInt("Ammo", ammoToLoad);
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return reload(stack, player, world, null) ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }
    
    public static boolean useSoap(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof SoapItem;
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return TommyGunItem.getAmmo(stack) < MAX_AMMO;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - ((double) TommyGunItem.getAmmo(stack) / (double) MAX_AMMO);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putInt("Ammo", MAX_AMMO);
            items.add(stack);
        }
    }
    
}
