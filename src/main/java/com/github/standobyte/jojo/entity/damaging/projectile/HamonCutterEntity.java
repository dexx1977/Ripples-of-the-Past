package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.List;

import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HamonCutterEntity extends ModdedProjectileEntity {
    private static final Vec3 MOUTH_POS_OFFSET = new Vec3(0.0D, -0.1D, 0.0D);
    private ItemStack potionItem;
    private int color = -1;
    private float hamonStatPoints;

    public HamonCutterEntity(LivingEntity shooter, Level world, ItemStack potionItem) {
        super(ModEntityTypes.HAMON_CUTTER.get(), shooter, world);
        this.potionItem = potionItem == null ? ItemStack.EMPTY : potionItem;
        this.color = PotionUtils.getColor(potionItem);
    }
    
    public HamonCutterEntity withColor(int color) {
        this.color = color;
        return this;
    }

    public HamonCutterEntity(EntityType<? extends HamonCutterEntity> type, Level world) {
        super(type, world);
    }
    
    public void setHamonStatPoints(float points) {
        this.hamonStatPoints = points;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            HamonSparksLoopSound.playSparkSound(this, position(), 0.25F);
            CustomParticlesHelper.createHamonSparkParticles(this, position(), 1);
        }
    }
    
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return MOUTH_POS_OFFSET;
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        boolean projectileAttack = super.hurtTarget(target, owner);
        boolean hamonAttack = DamageUtil.dealHamonDamage(target, 0.075F, this, owner);
        return projectileAttack || hamonAttack;
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionItem);
            Entity entity = entityRayTraceResult.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entity;
                if (target.isAffectedByPotions()) {
                    for (MobEffectInstance effectInstance : effects) {
                        MobEffect effect = effectInstance.getEffect();
                        if (effect.isInstantenous()) {
                            effect.applyInstantenousEffect(this, getOwner(), target, effectInstance.getAmplifier(), 3 / 16);
                        } else {
                            target.addEffect(new MobEffectInstance(effect, Mth.floor(effectInstance.getDuration()), 
                                    effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible()));
                        }
                    }
                }
            }

            LivingEntity owner = getOwner();
            if (owner != null) {
                INonStandPower.getNonStandPowerOptional(owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, hamonStatPoints);
                    });
                });
            }
        }
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public float getBaseDamage() {
        return 2.0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    public int ticksLifespan() {
        return 100;
    }
    
    public int getColor() {
        return color;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (!potionItem.isEmpty()) {
            nbt.put("Potion", potionItem.save(new CompoundTag()));
        }
        nbt.putInt("Color", color);
        nbt.putFloat("Points", hamonStatPoints);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        potionItem = ItemStack.of(nbt.getCompound("Potion"));
        color = nbt.getInt("Color");
        hamonStatPoints = nbt.getFloat("Points");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(color);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        color = additionalData.readInt();
    }
}
