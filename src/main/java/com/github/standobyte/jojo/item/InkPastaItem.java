package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class InkPastaItem extends Item {

    public InkPastaItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return useWithHamon(world, player, hand).orElse(super.use(world, player, hand));
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity entity) {
        ItemStack item = super.finishUsingItem(pStack, pLevel, entity);
        if (entity instanceof Player) {
            onEaten(entity);
            if (((Player) entity).abilities.instabuild) {
                return item;
            }
        }
        return new ItemStack(Items.BOWL);
    }
    
    public static void onEaten(LivingEntity player) {
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setInkPastaVisuals());
    }
    
    public static Optional<InteractionResultHolder<ItemStack>> useWithHamon(Level world, Player player, InteractionHand hand) {
        boolean shootPasta = INonStandPower.getNonStandPowerOptional(player).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get())
                        .map(hamon -> {
                            return hamon.isSkillLearned(ModHamonSkills.THROWABLES_INFUSION.get()) && power.consumeEnergy(150);
                        })).orElse(false);
        
        if (shootPasta) {
            ItemStack pastaItem = player.getItemInHand(hand);
            
            JojoMod.LOGGER.debug("PEW");
            
            if (!player.abilities.instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BOWL));
            }
            
            return Optional.of(InteractionResultHolder.consume(pastaItem));
        }
        
        return Optional.empty();
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("Scorpivan"));
    }

}
