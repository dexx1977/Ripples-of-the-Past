package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class CrazyDiamondHeavyPunch extends StandEntityHeavyAttack {

    public CrazyDiamondHeavyPunch(Builder builder) {
        super(builder);
    }
    
    @Override
    public void onTaskSet(Level world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        super.onTaskSet(world, standEntity, standPower, phase, task, ticks);
        if (!world.isClientSide()) {
            LivingEntity user = standPower.getUser();
            ItemStack item = user.getOffhandItem();
            if (user != null && !item.isEmpty() && CrazyDiamondLeaveObject.canUseItem(item)) {
                ItemStack itemForStand = item.split(1);
                standEntity.takeItem(standEntity.handItemSlot(InteractionHand.MAIN_HAND), itemForStand, true, user);
            }
        }
    }

    @Override
    protected void onTaskStopped(Level world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {
        if (!world.isClientSide()) {
            standEntity.dropItemTo(standEntity.handItemSlot(InteractionHand.MAIN_HAND), standPower.getUser());
        }
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .armorPiercing((float) stand.getAttackDamage() * 0.01F);
    }
}
