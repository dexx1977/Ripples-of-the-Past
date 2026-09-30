package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class LadybugBroochItem extends Item {
    private final DyeColor dye;
    
    public LadybugBroochItem(Properties pProperties, DyeColor dye) {
        super(pProperties);
        this.dye = dye;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean success = player.getCapability(LivingUtilCapProvider.CAPABILITY).map(entity -> {
            return entity.addLadybugBrooch(dye);
        }).orElse(false);
        if (success && !world.isClientSide() && !player.abilities.instabuild) {
            itemStack.shrink(1);
        }
        return success ? InteractionResultHolder.consume(itemStack) : InteractionResultHolder.fail(itemStack);
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this, JojoMod.MOD_ID + ".ladybug_brooch");
        tooltip.add(ClientUtil.donoItemTooltip("Abreolitus"));
    }
}
