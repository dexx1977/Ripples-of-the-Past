package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleBarrierEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;

public class HamonBubbleBarrier extends HamonAction {

    public HamonBubbleBarrier(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        return HamonBubbleLauncher.checkSoap(user);
    }
    
    @Override
    public void startedHolding(Level world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            world.addFreshEntity(new HamonBubbleBarrierEntity(world, user, power));
        }
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonBubbleLauncher.consumeSoap(user, 50);
        }
    }
    
    @Override
    public boolean renderHamonAuraOnItem(ItemStack item, HumanoidArm handSide) {
        return item.getItem() == ModItems.SOAP.get();
    }
}
