package com.github.standobyte.jojo.util.mc.damage;

import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.resources.ResourceKey;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.block.WoodenCoffinBlock;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;

public class DamageUtil {
    // 1.20.1 keeps the properties in the damage type registry, so these are the
    // type keys; use damageSource(...) below to build the source for a level.
    public static final ResourceKey<DamageType> ULTRAVIOLET = ModDamageTypes.ULTRAVIOLET;
    public static final String BLOOD_DRAIN_MSG = "bloodDrain";
    public static final ResourceKey<DamageType> COLD = ModDamageTypes.COLD;
    public static final ResourceKey<DamageType> HAMON = ModDamageTypes.HAMON;
    public static final ResourceKey<DamageType> PILLAR_MAN_ABSORPTION = ModDamageTypes.PILLAR_MAN_ABSORPTION;
    public static final ResourceKey<DamageType> STAND_VIRUS = ModDamageTypes.STAND_VIRUS;
    public static final ResourceKey<DamageType> STAND_VIRUS_METEORITE = ModDamageTypes.STAND_VIRUS_METEORITE;
    public static final ResourceKey<DamageType> SUFFOCATION = ModDamageTypes.SUFFOCATION;
    public static final ResourceKey<DamageType> EYE_OF_ENDER_SHARDS = ModDamageTypes.EYE_OF_ENDER_SHARDS;
    public static final String ROAD_ROLLER_MSG = "roadRoller";
    public static final ResourceKey<DamageType> STONE_MASK = ModDamageTypes.STONE_MASK;

    public static DamageSource damageSource(Level level, ResourceKey<DamageType> type) {
        return ModDamageTypes.source(level, type);
    }

    public static DamageSource damageSource(Entity entity, ResourceKey<DamageType> type) {
        return ModDamageTypes.source(entity, type);
    }
    
    public static float knockbackReduction(DamageSource source) {
        if (source instanceof StandLinkDamageSource || ROAD_ROLLER_MSG.equals(source.getMsgId())) {
            return 0;
        }
        if (source instanceof IModdedDamageSource) {
            IModdedDamageSource moddedSrc = (IModdedDamageSource) source;
            return moddedSrc.getKnockbackFactor();
        }
        if (source instanceof EntityDamageSource) {
            if (source.getDirectEntity() instanceof LivingEntity && (INonStandPower.getNonStandPowerOptional((LivingEntity) source.getDirectEntity())
                    .map(power -> {
                        Action<?> heldAction = power.getHeldAction();
                        return heldAction == ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE.get()
                                || heldAction == ModPillarmanActions.PILLARMAN_BLADE_BARRAGE.get();
                    }).orElse(false))) {
                return 0.05F;
            }
            String msgId = source.getMsgId();
            if (msgId != null && (msgId.startsWith(BLOOD_DRAIN_MSG) || msgId.startsWith(COLD.location().getPath()) || msgId.startsWith(ROAD_ROLLER_MSG))) {
                return 0;
            }
            if (source.getDirectEntity() instanceof HamonSendoOverdriveEntity) {
                return HamonSendoOverdriveEntity.KNOCKBACK_FACTOR;
            }
        }
        return 1;
    }
    
    public static DamageSource bloodDrainDamage(Entity srcDirect) {
        return ModDamageTypes.source(srcDirect, BLOOD_DRAIN_MSG);
    }
    
    public static boolean entityTakesUVDamage(Entity target, boolean sun) {
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            
            if (JojoModUtil.isUndeadOrVampiric(living)
                    && !WoodenCoffinBlock.isSleepingInCoffin(living)
                    && INonStandPower.getNonStandPowerOptional(living).resolve().flatMap(
                            power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()))
                    .map(pillarman -> !pillarman.isStoneFormEnabled()).orElse(true)) {
                
                if (sun) {
                    return (living instanceof Player || JojoModConfig.getCommonConfigInstance(false).undeadMobsSunDamage.get())
                            && target.getType() != EntityType.WITHER
                            && !living.hasEffect(ModStatusEffects.SUN_RESISTANCE.get());
                }
                
                return true;
            }
        }
        
        return false;
    }

    public static boolean dealUltravioletDamage(Entity target, float amount, @Nullable Entity srcDirect, @Nullable Entity srcIndirect, boolean sun) {
        if (target instanceof LivingEntity) {
            DamageSource dmgSource = srcDirect == null ? ModDamageTypes.source(target, ULTRAVIOLET) : 
                ModDamageTypes.source(srcDirect, srcIndirect, ModDamageTypes.key("ultraviolet.entity"));
            return target.hurt(dmgSource, amount);
        }
        return false;
    }
    
    public static boolean isImmuneToCold(Entity target) {
        if (target.isInvulnerableTo(ModDamageTypes.source(target, COLD))) {
            return true;
        }
        EntityType<?> type = target.getType();
        return type == EntityType.SNOW_GOLEM || type == EntityType.STRAY || type == EntityType.POLAR_BEAR;
    }
    
    public static boolean dealColdDamage(Entity target, float amount, @Nullable Entity srcDirect, @Nullable Entity srcIndirect) { // FIXME backport the vanilla mechanic
        if (target instanceof LivingEntity) {
            if (isImmuneToCold(target)) {
                return false;
            }
            EntityType<?> type = target.getType();
            if (type == EntityType.BLAZE || type == EntityType.MAGMA_CUBE || type == EntityType.STRIDER) {
                amount *= 5F;
            }
            else if (((LivingEntity) target).getMobType() == MobType.UNDEAD) {
                amount *= 0.5F;
            }
            DamageSource dmgSource = srcDirect == null ? ModDamageTypes.source(target, COLD) : 
                ModDamageTypes.source(srcDirect, srcIndirect, ModDamageTypes.key("cold.entity"));
            return target.hurt(dmgSource, amount);
        }
        return false;
    }
    
    public static boolean dealHamonDamage(Entity target, float amount, @Nullable Entity srcDirect, @Nullable Entity srcIndirect) {
        return dealHamonDamage(target, amount, srcDirect, srcIndirect, null);
    }

    public static boolean dealHamonDamage(Entity target, float amount, @Nullable Entity srcDirect, @Nullable Entity srcIndirect, @Nullable Consumer<HamonAttackProperties> attackProperties) {
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            
            HamonAttackProperties attack = new HamonAttackProperties();
            if (attackProperties != null) {
                attackProperties.accept(attack);
            }
            
            if (livingTarget.getCapability(EntityHamonChargeCapProvider.CAPABILITY).map(EntityHamonChargeCap::hasHamonCharge).orElse(false)) {
                return false;
            }
            
            Optional<INonStandPower> targetPower = INonStandPower.getNonStandPowerOptional(livingTarget).resolve();
            
            if (INonStandPower.getNonStandPowerOptional(livingTarget).resolve().flatMap(
                    power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                    .map(pillarman -> pillarman.isStoneFormEnabled())).orElse(false)) {
                return false;
            }
            
            boolean scarf = livingTarget.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.SATIPOROJA_SCARF.get();
            if (scarf) {
                if (targetPower.map(power -> power.getType() == ModPowers.HAMON.get()).orElse(false)) {
                    return false;
                }
                amount *= 0.25F;
            }
            
            DamageSource dmgSource = srcDirect == null ? ModDamageTypes.source(target, HAMON) : 
                    ModDamageTypes.source(srcDirect, srcIndirect, ModDamageTypes.key("hamon.entity"));
                    
            boolean undeadTarget = JojoModUtil.isAffectedByHamon(livingTarget);
            if (!undeadTarget) {
                amount *= 0.2F;
            }
            else if (INonStandPower.getNonStandPowerOptional(livingTarget)
                    .map(power -> power.getType() == ModPowers.PILLAR_MAN.get()).orElse(false)) {
                amount *= 0.5F;
            }
            
            final float dmgAmount = amount;
            Optional<HamonData> attackerHamon;
            float hamonMultiplier;
            if (attack.srcEntityHamonMultiplier && dmgSource.getEntity() instanceof LivingEntity) {
                LivingEntity sourceLiving = (LivingEntity) dmgSource.getEntity();
                attackerHamon = INonStandPower.getNonStandPowerOptional(sourceLiving).resolve().flatMap(
                        power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
                hamonMultiplier = attackerHamon.map(HamonData::getHamonDamageMultiplier).orElse(1F);
                amount *= hamonMultiplier;
            }
            else {
                attackerHamon = Optional.empty();
                hamonMultiplier = 1;
            }
            amount *= JojoModConfig.getCommonConfigInstance(false).hamonDamageMultiplier.get().floatValue();
            
//            JojoMod.LOGGER.debug(amount);
            
            if (hurtThroughInvulTicks(target, dmgSource, amount)) {
                HamonUtil.createHamonSparkParticlesEmitter(target, amount / (HamonData.MAX_HAMON_STRENGTH_MULTIPLIER * 5), attack.soundVolumeMultiplier, attack.hamonParticle);
                if (scarf && undeadTarget && livingTarget instanceof ServerPlayer) {
                    ModCriteriaTriggers.VAMPIRE_HAMON_DAMAGE_SCARF.get().trigger((ServerPlayer) livingTarget);
                }
                attackerHamon.ifPresent(hamon -> {
                    if (undeadTarget && !scarf && hamon.isSkillLearned(ModHamonSkills.HAMON_SPREAD.get())) {
                        livingTarget.getCapability(LivingUtilCapProvider.CAPABILITY)
                        .ifPresent(cap -> cap.hamonSpread(dmgAmount * hamonMultiplier));
                    }
                });
                return true;
            }
        }
        return false;
    }
    
    public static class HamonAttackProperties {
        private ParticleOptions hamonParticle = ModParticles.HAMON_SPARK.get();
        private boolean srcEntityHamonMultiplier = true;
        private float soundVolumeMultiplier = 1.0F;
        
        public HamonAttackProperties hamonParticle(ParticleOptions particleType) {
            this.hamonParticle = particleType != null ? particleType : ModParticles.HAMON_SPARK.get();
            return this;
        }
        
        public HamonAttackProperties noSrcEntityHamonMultiplier() {
            this.srcEntityHamonMultiplier = false;
            return this;
        }
        
        public HamonAttackProperties soundVolumeMultiplier(float multiplier) {
            this.soundVolumeMultiplier = multiplier;
            return this;
        }
    }
    
    public static boolean dealPillarmanAbsorptionDamage(Entity target, float amount, @Nullable Entity src) {
        if (target instanceof LivingEntity) {
            /*LivingEntity livingTarget = (LivingEntity) target;
            if (!JojoModUtil.canBleed(livingTarget)) {
                return false;
            }*/
            DamageSource dmgSource = 
                    src == null ? ModDamageTypes.source(target, PILLAR_MAN_ABSORPTION) : ModDamageTypes.source(src, ModDamageTypes.key("pillarManAbsorption.entity"));
            return target.hurt(dmgSource, amount);
        }
        return false;
    }
    
    public static DamageSource roadRollerDamage(RoadRollerEntity entity) {
        return new EntityDamageSource(ROAD_ROLLER_MSG, entity).bypassArmor();
    }
    
    public static boolean dealDamageAndSetOnFire(Entity entity, Predicate<Entity> hurtEntity, int fireSeconds, boolean stand) {
        int fireTicks = entity.getRemainingFireTicks();
        setOnFire(entity, fireSeconds, stand);
        boolean dealtDamage = hurtEntity.test(entity);
        if (!dealtDamage) {
            entity.setRemainingFireTicks(fireTicks);
        }
        return dealtDamage;
    }
    
    public static void setOnFire(Entity entity, int fireSeconds, boolean stand) {
        if (stand && entity instanceof StandEntity) {
            ((StandEntity) entity).setFireFromStand(fireSeconds);
        }
        else {
            entity.setSecondsOnFire(fireSeconds);
        }
    }
    
    public static boolean hurtThroughInvulTicks(Entity target, DamageSource dmgSource, float amount) {
        int invulTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        LivingEntity targetLiving = target instanceof LivingEntity ? (LivingEntity) target : null;
        float lastHurt = targetLiving != null ? targetLiving.lastHurt : 0;
        
        if (!dmgSource.isBypassArmor() && dmgSource instanceof IModdedDamageSource && targetLiving != null) {
            targetLiving.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(
                    cap -> cap.onHurtThroughInvul((IModdedDamageSource) dmgSource));
        }
        boolean dealtDamage = target.hurt(dmgSource, amount);
        
        target.invulnerableTime = invulTime;
        if (targetLiving != null) {
            targetLiving.lastHurt = lastHurt;
        }
        return dealtDamage;
    }
    
    public static DamageSource enderDragonDamageHack(DamageSource damageSource, Entity target) {
        if (target instanceof EnderDragon || target instanceof EnderDragonPart) {
            damageSource.setExplosion();
        }
        return damageSource;
    }
    
    public static float addArmorPiercing(float damage, float armorPiercing, @Nullable LivingEntity armoredTarget) {
        if (armoredTarget != null && armorPiercing > 0) {
            float armor = (float) armoredTarget.getArmorValue();
            if (armor > 0) {
                float toughness = (float) armoredTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                armorPiercing = Mth.clamp(armorPiercing, 0, 1);
                float damagePierced = Mth.lerp(armorPiercing, CombatRules.getDamageAfterAbsorb(damage, armor, toughness), damage);
                damage = MathUtil.inverseArmorProtectionDamage(damagePierced, armor, toughness);
            }
        }
        return damage;
    }
    
    public static void disableShield(Player target, float chance) {
        if (!target.level.isClientSide() && target.getRandom().nextFloat() < chance) {
            target.getCooldowns().addCooldown(target.getUseItem().getItem(), 100);
            target.stopUsingItem();
            target.level.broadcastEntityEvent(target, (byte) 30);
        }
    }
    
    public static boolean isMeleeAttack(DamageSource dmgSource) {
        return getMeleeAttacker(dmgSource) != null;
    }
    
    @Nullable
    public static LivingEntity getMeleeAttacker(DamageSource dmgSource) {
        if (dmgSource.getEntity() != null && dmgSource.getDirectEntity() != null
                && dmgSource.getEntity().is(dmgSource.getDirectEntity()) && dmgSource.getEntity() instanceof LivingEntity) {
            return (LivingEntity) dmgSource.getEntity();
        }
        return null;
    }
    
    public static void knockback(LivingEntity target, float strength, float yRotDeg) {
        target.knockback(strength, 
                (double) Mth.sin(yRotDeg * MathUtil.DEG_TO_RAD), 
                (double) (-Mth.cos(yRotDeg * MathUtil.DEG_TO_RAD)));
    }
    
    public static void upwardsKnockback(LivingEntity target, float strength) {
        strength *= (1.0F - (float) target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        if (strength != 0) {
            target.setDeltaMovement(target.getDeltaMovement().add(0, strength, 0));
        }
        
        if (target instanceof StandEntity) {
            LivingEntity standUser = ((StandEntity) target).getUser();
            if (standUser != null && !standUser.is(target)) {
                upwardsKnockback(standUser, strength);
            }
        }
    }
    
    public static void knockback3d(LivingEntity target, float strength, float xRot, float yRot) {
        Vec3 knockbackVec = Vec3.directionFromRotation(xRot, yRot);
        LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(target, strength, knockbackVec.x, knockbackVec.z);
        boolean addVertical = true;
        if (event.isCanceled()) {
            addVertical = target.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.didStackKnockbackInstead).orElse(false);
        }
        if (!addVertical) {
            return;
        }
        
        strength = event.getStrength();
        knockbackVec = new Vec3(event.getRatioX(), knockbackVec.y, event.getRatioZ()).normalize();
        strength *= (1.0F - (float) target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        
        if (strength != 0) {
            target.setDeltaMovement(target.getDeltaMovement().add(knockbackVec.scale(strength)));
        }
        
        if (target instanceof StandEntity) {
            LivingEntity standUser = ((StandEntity) target).getUser();
            if (standUser != null && !standUser.is(target)) {
                upwardsKnockback(standUser, (float) knockbackVec.y * strength);
            }
        }
    }
    
    public static void applyKnockbackStack(LivingEntity target, float pStrength, double pRatioX, double pRatioZ) {
        pStrength *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        if (pStrength > 0) {
            target.hasImpulse = true;
            Vec3 speedCur = target.getDeltaMovement();
            Vec3 knockback = (new Vec3(pRatioX, 0.0D, pRatioZ)).normalize().scale(pStrength);
            target.setDeltaMovement(
                    speedCur.x - knockback.x, 
                    Math.min(0.4D, speedCur.y + (double)pStrength), 
                    speedCur.z - knockback.z);
        }
        
        target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
            util.didStackKnockbackInstead = true;
        });
    }
    
    public static boolean isShieldBlockAngle(LivingEntity target, DamageSource damageSource) {
        Vec3 damagePos = damageSource.getSourcePosition();
        if (damagePos != null) {
            Vec3 targetViewVec = target.getViewVector(1.0F);
            Vec3 vecToTarget = damagePos.vectorTo(target.position());
            vecToTarget = vecToTarget.normalize();
            vecToTarget = new Vec3(vecToTarget.x, 0, vecToTarget.z); // it's not normalized anymore though?
            if (vecToTarget.dot(targetViewVec) < 0.0D) {
                return true;
            }
        }
        
        return false;
    }
    
    public static void suffocateTick(LivingEntity entity, float speed) {
        if (entity.canBreatheUnderwater() || entity instanceof Player && JojoModUtil.isUndeadOrVampiric((Player) entity)
                || JojoModUtil.isDyingBody(entity) || entity instanceof IronGolem) return;
        
        if (entity.getAirSupply() > 0) {
            Optional<HamonData> hamonOptional = INonStandPower.getNonStandPowerOptional(entity).resolve().flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
            if (hamonOptional.isPresent()) {
                HamonData hamon = hamonOptional.get();
                speed /= 1 + hamonOptional.get().getBreathingLevel() * 0.04F;
                hamon.suffocateTick(speed);
            }
            
            int airReduction = MathUtil.fractionRandomInc((double) entity.getMaxAirSupply() * Mth.clamp(speed, 0.0, 1.0)) + 4;
            entity.setAirSupply(Math.max(entity.getAirSupply() - airReduction, -18));
        }
        else {
            entity.hurt(ModDamageTypes.source(entity, SUFFOCATION), 1F);
        }
    }
    
    public static float getDamageWithoutHeldItem(@Nullable LivingEntity entity) {
        if (entity == null) {
            return (float) Attributes.ATTACK_DAMAGE.getDefaultValue();
        }
        ItemStack heldItem = entity.getMainHandItem();
        if (!heldItem.isEmpty()) {
            Multimap<Attribute, AttributeModifier> itemModifiers = heldItem.getAttributeModifiers(EquipmentSlot.MAINHAND);
            if (itemModifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
                AttributeInstance attackDamageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
                Collection<AttributeModifier> attackDamageModifiers = itemModifiers.get(Attributes.ATTACK_DAMAGE);
                
                double damage = MCUtil.calcValueWithoutModifiers(attackDamageAttribute, 
                        attackDamageModifiers.stream().map(AttributeModifier::getId));
                return (float) damage;
            }
        }
        return (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
}
