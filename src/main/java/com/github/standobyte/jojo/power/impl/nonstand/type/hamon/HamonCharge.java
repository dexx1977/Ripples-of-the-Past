package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil.HamonAttackProperties;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class HamonCharge {
    private float damage;
    private final int chargeTicksInitial;
    private int chargeTicks;
    private UUID hamonUserId;
    private Entity hamonUser;
    private boolean gavePoints;
    private boolean doLargeChargeDmg = true;
    private float energySpent;
    
    public HamonCharge(float damage, int chargeTicks, @Nullable LivingEntity hamonUser, float energySpent) {
        this.damage = damage;
        this.chargeTicksInitial = chargeTicks;
        this.chargeTicks = chargeTicks;
        if (hamonUser != null) {
            this.hamonUserId = hamonUser.getUUID();
        }
        this.energySpent = energySpent;
    }
    
    public float getDamage() {
        return damage;
    }
    
    public void tick(@Nullable Entity chargedEntity, @Nullable BlockPos chargedBlock, Level world, AABB aabb) {
        if (!world.isClientSide() && (chargedEntity == null || chargedEntity.canUpdate())) {
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, aabb, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            for (LivingEntity target : entities) {
                if (!target.is(chargedEntity) && target.isAlive() && !target.getUUID().equals(hamonUserId)) {
                    Entity user = getUser((ServerLevel) world);
                    LivingEntity userLiving = user instanceof LivingEntity ? (LivingEntity) user : null;
                    float dmgAmount = this.damage;
                    if (!doLargeChargeDmg) {
                        dmgAmount *= 0.1F;
                    }
                    
                    if (DamageUtil.dealHamonDamage(target, dmgAmount, chargedEntity, 
                            chargedEntity instanceof LivingEntity ? null : user, HamonAttackProperties::noSrcEntityHamonMultiplier)) {
                        if (!target.isAlive() && user instanceof ServerPlayer) {
                            ModCriteriaTriggers.HAMON_CHARGE_KILL.get().trigger((ServerPlayer) user, target, chargedEntity, chargedBlock);
                        }
                        if (!gavePoints && userLiving != null) {
                            INonStandPower.getNonStandPowerOptional(userLiving).resolve()
                            .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get())).ifPresent(hamon -> {
                                hamon.hamonPointsFromAction(HamonStat.STRENGTH, energySpent);
                            });
                        }
                        
                        Vec3 chargePos = null;
                        if (chargedBlock != null) {
                            chargePos = Vec3.atCenterOf(chargedBlock);
                        }
                        else if (chargedEntity != null) {
                            chargePos = chargedEntity.getBoundingBox().getCenter();
                        }
                        if (chargePos != null && doLargeChargeDmg) {
                            HamonUtil.emitHamonSparkParticles(world, null, chargePos, 4.0F, null);
                        }
                        
                        //adds knockback to Infused Blocks
                        if (!world.isClientSide()) {
                            if (doLargeChargeDmg) {
                                if (chargePos != null) {
                                    Vec3 knockbackVec = new Vec3(chargePos.x - target.getX(), 0, chargePos.z - target.getZ()).normalize();
                                    target.knockback(0.75F, knockbackVec.x, knockbackVec.z);
                                }
                                // If Hamon Shock is learned Entity Infuse will shock aswell
                                if (userLiving != null && HamonUtil.isLiving(target) && !ModStatusEffects.isStunned(target)
                                        && chargedEntity instanceof LivingEntity) {
                                    INonStandPower.getNonStandPowerOptional(userLiving).resolve()
                                    .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get())).ifPresent(hamon -> {
                                        if (hamon.isSkillLearned(ModHamonSkills.HAMON_SHOCK.get())) {
                                            target.addEffect(new MobEffectInstance(ModStatusEffects.HAMON_SHOCK.get(), 30, 0, false, false));
                                        }
                                    });
                                }
                            }
                            if (chargedBlock != null) {
                                if (doLargeChargeDmg && world.getBlockState(chargedBlock).getBlock() != Blocks.COBWEB) {
                                    chargeTicks = 0;
                                }
                            } else if (chargedEntity != null) {
                                // mobs keep the charge, but will deal less damage and no knockback
                                doLargeChargeDmg = false;
                            } else {
                                chargeTicks = 0;
                            }
                        }
                        gavePoints = true;
                        // One time charge
                    }
                }
            }
            chargeTicks--;
        }
    }
    
    public Entity getUser(ServerLevel world) {
        if (hamonUser == null && world instanceof ServerLevel) {
            hamonUser = ((ServerLevel) world).getEntity(hamonUserId);
        }
        return hamonUser;
    }
    
    public boolean shouldBeRemoved() {
        return chargeTicks < 0;
    }
    
    public void setTicks(int ticks) {
        this.chargeTicks = ticks;
    }
    
    public void decreaseTicks(int ticks) {
        setTicks(chargeTicks - ticks);
    }
    
    public int getTicks() {
        return chargeTicks;
    }
    
    public int getInitialTicks() {
        return chargeTicksInitial;
    }
    
    
    
    public static HamonCharge fromNBT(CompoundTag nbt) {
        HamonCharge charge = new HamonCharge(nbt.getFloat("Charge"), nbt.getInt("ChargeTicksInitial"), null, nbt.getFloat("EnergySpent"));
        charge.chargeTicks = nbt.getInt("ChargeTicks");
        if (nbt.hasUUID("HamonUser")) {
            charge.hamonUserId = nbt.getUUID("HamonUser");
        }
        charge.gavePoints = nbt.getBoolean("GavePoints");
        charge.doLargeChargeDmg = nbt.getBoolean("LargeDmg");
        return charge;
    }
    
    public CompoundTag toNBT() {
        CompoundTag chargeNbt = new CompoundTag();
        chargeNbt.putFloat("Charge", damage);
        chargeNbt.putInt("ChargeTicksInitial", chargeTicksInitial);
        chargeNbt.putInt("ChargeTicks", chargeTicks);
        if (hamonUserId != null) {
            chargeNbt.putUUID("HamonUser", hamonUserId);
        }
        chargeNbt.putBoolean("GavePoints", gavePoints);
        chargeNbt.putFloat("EnergySpent", energySpent);
        chargeNbt.putBoolean("LargeDmg", doLargeChargeDmg);
        return chargeNbt;
    }

}
