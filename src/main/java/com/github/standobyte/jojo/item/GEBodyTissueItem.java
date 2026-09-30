package com.github.standobyte.jojo.item;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.stand.GoldExperienceHeal;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class GEBodyTissueItem extends Item {

    public GEBodyTissueItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        ActionConditionResult canUse = GoldExperienceHeal.canHeal(player, player, true, GoldExperienceHeal.MAX_REGEN_LVL - 1);
        if (canUse.isPositive()) {
            if (!world.isClientSide()) {
                Optional<LivingEntity> userCreator = getGEUser((ServerLevel) world, item);
                if (userCreator == null) { // item was created by other means, does not have data about the user
                    healAfterDelay(player, null);
                }
                else { // in this case only heal if the user is still in the world and has Gold Experience
                    userCreator.flatMap(e -> IStandPower.getStandPowerOptional(e).resolve()).ifPresent(userPower -> {
                        if (ModStandsInit.GOLD_EXPERIENCE_HEALING_ITEM.get().isUnlocked(userPower)) {
                            healAfterDelay(player, userPower);
                        }
                    });
                }
                
                if (!player.abilities.instabuild) {
                    item.shrink(1);
                }
            }
            return InteractionResultHolder.consume(item);
        }
        else {
            if (!world.isClientSide()) {
                ((ServerPlayer) player).sendMessage(canUse.getWarning(), ChatType.GAME_INFO, Util.NIL_UUID);
            }
            return InteractionResultHolder.fail(item);
        }
    }
    
    private static void healAfterDelay(LivingEntity player, @Nullable IStandPower geUserPower) {
        GoldExperienceHeal.giveGEHealEffect(player, geUserPower, 100);
    }
    
    public static void onCreated(IStandPower userPower, ItemStack item) {
        if (userPower != null) {
            LivingEntity user = userPower.getUser();
            if (user != null) {
                item.getOrCreateTag().putUUID("GEUser", user.getUUID());
            }
        }
    }
    
    @Nullable
    private static Optional<LivingEntity> getGEUser(ServerLevel world, ItemStack item) {
        if (item.hasTag() && item.getTag().hasUUID("GEUser")) {
            UUID id = item.getTag().getUUID("GEUser");
            Entity entity = world.getEntity(id);
            if (entity instanceof LivingEntity) {
                return Optional.of((LivingEntity) entity);
            }
            else {
                return Optional.empty();
            }
        }
        
        return null;
    }
}
