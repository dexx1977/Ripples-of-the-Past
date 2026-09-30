package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.itemprojectile.ClackersEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class ClackersItem extends Item {
    // 1.16.5's Item exposed a shared Random; 1.20.1 items carry their own.
    protected static final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();

    public static final int TICKS_MAX_POWER = 20;
    
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public ClackersItem(Properties properties) {
        super(properties);
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 6.0, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        INonStandPower power = INonStandPower.getPlayerNonStandPower(player);
        if (power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.isSkillLearned(ModHamonSkills.CLACKER_VOLLEY.get())) {
                return true;
            }
            return false;
        }).orElse(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.pass(stack);
        }
        else {
            playClackSound(world, player);
            ding(world, player);
            return InteractionResultHolder.fail(stack);
        }
    }
    
    private void ding(Level world, Player player) {
    }

    private static final float CHARGE_TICK_COST = 5;
    private static final float UPKEEP_TICK_COST = CHARGE_TICK_COST / 5;
    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int remainingTicks) {
        int ticksUsed = getUseDuration(stack) - remainingTicks;
        int ticksMaxPower = TICKS_MAX_POWER;
        if (clackersTexVariant(ticksUsed, ticksMaxPower) > 0) {
            //playClackSound(world, entity);
            if (ticksUsed >= ticksMaxPower / 2 && !world.isClientSide()) {
                Vec3 sparkVec = entity.getLookAngle().scale(0.75)
                        .add(entity.getX(), entity.getY(0.6), entity.getZ());
//                HamonUtil.emitHamonSparkParticles(world, entity instanceof PlayerEntity ? (PlayerEntity) entity : null, 
//                        sparkVec, ticksUsed >= ticksMaxPower ? 0.25F : 0.1F);
            }
        }
        if (!world.isClientSide()) {
            if (!INonStandPower.getNonStandPowerOptional(entity).map(power -> 
            power.consumeEnergy(ticksUsed <= ticksMaxPower ? CHARGE_TICK_COST : UPKEEP_TICK_COST)).orElse(false)) {
                entity.releaseUsingItem();
                return;
            }
            if (ticksUsed == ticksMaxPower) {
                JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_CLACKER_VOLLEY.get());
            }
        }
    }
    
    public static int clackersTexVariant(int ticksUsed, int ticksMax) {
        if (ticksUsed < ticksMax / 2) {
            return ticksUsed % 20 == 10 ? 1 : 0;
        }
        if (ticksUsed < ticksMax) {
            return ticksUsed % 8 == 4 ? 1 : 0;
        }
        return 2 + ticksUsed % 2;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level world, LivingEntity entity, int ticksLeft) {
        int ticksUsed = getUseDuration(itemStack) - ticksLeft;
        float power = (float) Math.min(ticksUsed, TICKS_MAX_POWER) / (float) TICKS_MAX_POWER;
        if (power > 0) {
            if (power < 0.15) {
                playClackSound(world, entity);
                if (!world.isClientSide()) {
                    entity.hurt(entity instanceof Player ? ((Player) entity).level().damageSources().playerAttack((Player) entity) : entity.level().damageSources().mobAttack((net.minecraft.world.entity.LivingEntity) (entity)), 1.0F);
                    JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_OH_NO.get());
                }
            }
            else if (!world.isClientSide() && power > 0.5) {
                ClackersEntity clackers = new ClackersEntity(world, entity);
                float projectileSpeed = power == 1.0F ? 3 : power * 2;
                float hamonDmg = projectileSpeed * 0.5F;
                clackers.setHamonDamage(hamonDmg);
                clackers.setHamonEnergySpent(Math.min(ticksUsed, TICKS_MAX_POWER) * CHARGE_TICK_COST + Math.max(ticksUsed - TICKS_MAX_POWER, 0) * UPKEEP_TICK_COST);
                clackers.shootFromRotation(entity, projectileSpeed, 0.5F);
                
                TrackerItemStack.getItemTracker(itemStack).ifPresent(tracker -> {
                    if (tracker.isTracked()) {
                        tracker.setAtEntity(clackers.getId(), world, KnownItemState.ENTITY_IS_ITEM);
                        tracker.setItemStillThereCheck(null);
                        clackers.saveItemTrackerNBT(tracker.toNBT());
                    }
                });
                
                world.addFreshEntity(clackers);
            }
        }
        if (power > 0.5 && !(entity instanceof Player && ((Player) entity).abilities.instabuild)) {
            itemStack.shrink(1);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        return INonStandPower.getNonStandPowerOptional(user).map(power -> 
        power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.isSkillLearned(ModHamonSkills.CLACKER_VOLLEY.get())) {
                if (!user.level.isClientSide()) {
                    if (power.consumeEnergy(200) && DamageUtil.dealHamonDamage(target, 0.15F, user, null)) {
                        target.invulnerableTime = 0;
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, 200);
                        return true;
                    }
                    return false;
                }
                return true;
            }
            return false;
        }).orElse(false)).orElse(false);
    }

    public static void playClackSound(Level world, LivingEntity entity) {
        world.playSound(entity instanceof Player ? (Player) entity : null, entity.getX(), entity.getY(), entity.getZ(), 
                ModSounds.CLACKERS.get(), entity.getSoundSource(), 0.5F, 1.0F + (random.nextFloat() - 0.5F) * 0.1F);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

}
