package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

public class StandRemoverItem extends Item {
    private final boolean oneTimeUse;
    private final Mode mode;

    public StandRemoverItem(Properties properties, Mode mode, boolean oneTimeUse) {
        super(properties);

        this.mode = mode;
        this.oneTimeUse = oneTimeUse;
        
        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
                if (MCUtil.dispenseOnNearbyEntity(blockSource, stack, entity -> {
                    return IStandPower.getStandPowerOptional(entity).map(power -> {
                        return useOn(entity, power);
                    }).orElse(false);
                }, oneTimeUse)) {
                    return stack;
                }
                return super.execute(blockSource, stack);
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (useOn(player, power)) {
                if (oneTimeUse && !player.abilities.instabuild) {
                    stack.shrink(1);
                }
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.fail(stack);
        }
        else if (power.hasPower()) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }
    
    private boolean useOn(LivingEntity entity, IStandPower power) {
        if (power.hasPower()) {
            switch (mode) {
            case REMOVE:
                power.clear();
                break;
            case EJECT:
                Optional<StandInstance> previousDiscStand = power.putOutStand();
                previousDiscStand.ifPresent(prevStand -> MCUtil.giveItemTo(entity, 
                        StandDiscItem.withStand(new ItemStack(ModItems.STAND_DISC.get()), prevStand), true));
                break;
            case FULL_CLEAR:
                power.clear();
                power.fullStandClear();
                break;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (((StandRemoverItem) stack.getItem()).mode == Mode.FULL_CLEAR) {
            tooltip.add(Component.translatable("item.jojo.stand_full_clear.hint").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.jojo.creative_only_tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
    
    public static enum Mode {
        REMOVE,
        EJECT,
        FULL_CLEAR
    }
}
