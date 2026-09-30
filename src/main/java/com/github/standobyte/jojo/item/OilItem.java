package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.OptionalInt;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

public class OilItem extends Item {

    public OilItem(Properties properties) {
        super(properties);
    }
    
    public static final int MAX_USES = 120;
     

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        InteractionHand opposite = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack oilStack = player.getItemInHand(hand);
        ItemStack weaponStack = player.getItemInHand(opposite);
        
        if (MCUtil.isItemWeapon(weaponStack)) {
            if (!world.isClientSide()) {
                setWeaponOilUses(weaponStack, MAX_USES);
                if (!player.abilities.instabuild) {
                    oilStack.shrink(1);
                    player.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
            world.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.BOTTLE_EMPTY, player.getSoundSource(), 1F, 1F);
            return InteractionResultHolder.consume(oilStack);
        }
        
        return InteractionResultHolder.fail(oilStack);
    }
    
     @Override
     public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
             tooltip.add(Component.translatable("item.jojo.oil.hint").withStyle(ChatFormatting.GRAY)); 
     }
     
     public static OptionalInt remainingOiledUses(ItemStack stack) {
         if (!stack.isEmpty() && stack.hasTag()) {
             CompoundTag nbt = stack.getTag();
             if (nbt.contains("HamonOiled")) {
                 int usesLeft = nbt.getInt("HamonOiled");
                 return OptionalInt.of(usesLeft); 
             }
         }
         return OptionalInt.empty();
     } 
     
     public static void setWeaponOilUses(ItemStack weaponStack, int uses) {
         if (weaponStack.isEmpty()) return;
         
         if (uses > 0) {
             weaponStack.getOrCreateTag().putInt("HamonOiled", uses);
         }
         else if (weaponStack.hasTag()) {
             weaponStack.getTag().remove("HamonOiled");
         }
     }
         
}
