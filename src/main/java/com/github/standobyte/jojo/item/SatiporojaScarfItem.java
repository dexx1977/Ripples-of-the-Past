package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfBindingEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;

public class SatiporojaScarfItem extends CustomModelArmorItem {

    public SatiporojaScarfItem(ArmorMaterial material, EquipmentSlot slot, Properties builder) {
        super(material, slot, builder);
    }

    public static final float SCARF_SWING_ENERGY_COST = 600;
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        INonStandPower power = INonStandPower.getPlayerNonStandPower(player);
        if (power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.isSkillLearned(ModHamonSkills.SATIPOROJA_SCARF.get())) {
                if (!world.isClientSide()) {
                    if (power.consumeEnergy(SCARF_SWING_ENERGY_COST)) {
                        SatiporojaScarfEntity scarf = new SatiporojaScarfEntity(world, player, 
                                hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
                        world.addFreshEntity(scarf);
                        player.getCooldowns().addCooldown(this, scarf.ticksLifespan());
                        return true;
                    }
                    return false;
                }
                return power.hasEnergy(SCARF_SWING_ENERGY_COST);
            }
            return false;
        }).orElse(false)) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        if (user instanceof Player && ((Player) user).getCooldowns().isOnCooldown(itemStack.getItem())) {
            return false;
        }
        return INonStandPower.getNonStandPowerOptional(user).map(power -> 
        power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (!user.level.isClientSide()) {
                if (power.consumeEnergy(500) && DamageUtil.dealHamonDamage(target, 0.6F, user, null)) {
                    if (user.isShiftKeyDown() && hamon.isSkillLearned(ModHamonSkills.SNAKE_MUFFLER.get()) && power.consumeEnergy(100)) {
                        SatiporojaScarfBindingEntity scarf = new SatiporojaScarfBindingEntity(user.level, user);
                        scarf.attachToEntity(target);
                        target.addEffect(new MobEffectInstance(ModStatusEffects.STUN.get(), scarf.ticksLifespan()));
                        user.level.addFreshEntity(scarf);
                        if (user instanceof Player) {
                            ((Player) user).getCooldowns().addCooldown(this, scarf.ticksLifespan());
                        }
                    }
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, 500);
                    return true;
                }
                return false;
            }
            return true;
        }).orElse(false)).orElse(false);
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }

}
