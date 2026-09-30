package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

public class VampirismDarkAura extends VampirismAction {

    public VampirismDarkAura(NonStandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        int difficulty = world.getDifficulty().getId();
        int range = 16 * difficulty - 8;
        if (!world.isClientSide()) {
            for (LivingEntity entity : MCUtil.entitiesAround(
                    LivingEntity.class, user, range, false, entity -> 
                    !(JojoModUtil.isUndeadOrVampiric(entity) || OptionalDependencyHelper.vampirism().isEntityVampire(entity))
                            && !(entity instanceof StandEntity && user.is(((StandEntity) entity).getUser())))) {
                boolean passive = entity instanceof AgeableMob;
                int amplifier = Mth.floor((difficulty - 1) * 1.5);
                int duration = passive ? 600 : 200;
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier));
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, amplifier));
                if (passive) {
                    entity.addEffect(new MobEffectInstance(ModStatusEffects.STUN.get(), duration));
                }
            }
            if (world.getDifficulty() == Difficulty.HARD) {
                for (HungryZombieEntity zombie : MCUtil.entitiesAround(
                        HungryZombieEntity.class, user, range, false, zombie -> zombie.isEntityOwner(user))) {
                    zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                    zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 0));
                    zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0));
                }
            }
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack item = user.getItemInHand(hand);
                if (!item.isEmpty() && item.getItem() == Items.POPPY) {
                    ItemStack witherRose = new ItemStack(Items.WITHER_ROSE);
                    if (item.hasTag()) {
                        witherRose.setTag(item.getTag().copy());
                    }
                    witherRose.setCount(item.getCount());
                    user.setItemInHand(hand, witherRose);
                }
            }
        }
        user.playSound(ModSounds.VAMPIRE_EVIL_ATMOSPHERE.get(), (float) (range + 16) / 16F, 1.0F);
    }
    
    @Override
    protected int maxCuringStage() {
        return 3;
    }
}
