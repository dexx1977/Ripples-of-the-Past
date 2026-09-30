package com.github.standobyte.jojo.entity.stand.stands;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.IHasStandPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.client.sound.barrage.BarrageHitSoundHandler;
import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotEntity extends StandEntity {
    private static final AttributeModifier NO_ARMOR_MOVEMENT_SPEED_BOOST = new AttributeModifier(
            UUID.fromString("a31ffbee-5a26-4022-a298-59c839e5048d"), "Movement speed boost with no armor", 1.5, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_ARMOR_ATTACK_SPEED_BOOST = new AttributeModifier(
            UUID.fromString("c3e4ddb0-daa9-4cbb-acb9-dbc7eecad3f1"), "Attack speed boost with no armor", 0.5, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_ARMOR = new AttributeModifier(
            UUID.fromString("d4987f5f-55e8-45db-9a5e-b2fd0a98c2ec"), "No armor", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier NO_ARMOR_TOUGHNESS = new AttributeModifier(
            UUID.fromString("8dfd5e42-a578-4f4a-aafd-b86ef965b9f3"), "No armor toughness", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier NO_ARMOR_DURABILITY_DECREASE = new AttributeModifier(
            UUID.fromString("47c93a42-b04f-44f3-97be-5f542d97c000"), "No durability without armor", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    
    public static final AttributeModifier NO_RAPIER_DAMAGE_DECREASE = new AttributeModifier(
            UUID.fromString("84331a3b-73f1-4461-b240-6d688897e3f4"), "Attack damage decrease without rapier", -0.25, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_RAPIER_ATTACK_SPEED_DECREASE = new AttributeModifier(
            UUID.fromString("485642f9-5475-4d74-8b54-dea9c53fe62e"), "Attack speed decrease without rapier", -0.5, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final double RAPIER_RANGE = 1;
    private static final AttributeModifier NO_RAPIER_ATTACK_RANGE_DECREASE = new AttributeModifier(
            UUID.fromString("ba319644-fab3-4d4c-bcdf-26fd05dd62f5"), "Attack range decrease without rapier", -RAPIER_RANGE, AttributeModifier.Operation.ADDITION);
    
    private static final EntityDataAccessor<Boolean> HAS_RAPIER = SynchedEntityData.defineId(SilverChariotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_ARMOR = SynchedEntityData.defineId(SilverChariotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RAPIER_ON_FIRE = SynchedEntityData.defineId(SilverChariotEntity.class, EntityDataSerializers.BOOLEAN);
    
    private int ticksAfterArmorRemoval;
    private int rapierFireTicks = 0;

    public SilverChariotEntity(StandEntityType<SilverChariotEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HAS_RAPIER, true);
        entityData.define(HAS_ARMOR, true);
        entityData.define(RAPIER_ON_FIRE, false);
    }

    @Override
    public double getDefaultMeleeAttackRange() {
        return super.getDefaultMeleeAttackRange() + RAPIER_RANGE;
    }

    @Override
    public void swing(InteractionHand hand) {
        if (hasRapier()) {
            super.swing(InteractionHand.MAIN_HAND);
        }
        else {
            super.swing(hand);
        }
    }

    // TODO render rapier in left arm if the user is left-handed
    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
    
    public boolean hasRapier() {
        return entityData.get(HAS_RAPIER);
    }

    public void setRapier(boolean rapier) {
        entityData.set(HAS_RAPIER, rapier);
        updateModifier(getAttribute(Attributes.ATTACK_DAMAGE), NO_RAPIER_DAMAGE_DECREASE, !rapier);
        updateModifier(getAttribute(Attributes.ATTACK_SPEED), NO_RAPIER_ATTACK_SPEED_DECREASE, !rapier);
        updateModifier(getAttribute(ForgeMod.ENTITY_REACH.get()), NO_RAPIER_ATTACK_RANGE_DECREASE, !rapier);
        if (!rapier) {
            entityData.set(RAPIER_ON_FIRE, false);
        }
    }

    public boolean hasArmor() {
        return entityData.get(HAS_ARMOR);
    }

    public void setArmor(boolean armor) {
        entityData.set(HAS_ARMOR, armor);
        updateModifier(getAttribute(Attributes.MOVEMENT_SPEED), NO_ARMOR_MOVEMENT_SPEED_BOOST, !armor);
        updateModifier(getAttribute(Attributes.ATTACK_SPEED), NO_ARMOR_ATTACK_SPEED_BOOST, !armor);
        updateModifier(getAttribute(Attributes.ARMOR), NO_ARMOR, !armor);
        updateModifier(getAttribute(Attributes.ARMOR_TOUGHNESS), NO_ARMOR_TOUGHNESS, !armor);
        updateModifier(getAttribute(ModEntityAttributes.STAND_DURABILITY.get()), NO_ARMOR_DURABILITY_DECREASE, !armor);
    }
    
    @Override
    protected float getPhysicalResistance(float blockedRatio, float damageDealt) {
        return StandStatFormulas.getPhysicalResistance(0, 0, blockedRatio, damageDealt);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            if (!hasArmor()) {
                ticksAfterArmorRemoval++;
            }
            else {
                ticksAfterArmorRemoval = 0;
            }
            List<Entity> entities = level.getEntities(this, getBoundingBox());
            for (Entity entity : entities) {
                if (entity.isAlive() && entity.getType() == ModEntityTypes.SC_RAPIER.get()) {
                    ((SCRapierEntity) entity).takeRapier(this);
                }
            }
            
            if (rapierFireTicks > 0) {
                rapierFireTicks--;
                if (rapierFireTicks == 0) {
                    entityData.set(RAPIER_ON_FIRE, false);
                }
            }
        }
    }
    
    public int getTicksAfterArmorRemoval() {
        return ticksAfterArmorRemoval;
    }
    
    @Override
    public HumanoidArm getPunchingHand() {
        return hasRapier() ? HumanoidArm.RIGHT : super.getPunchingHand();
    }
    
    @Override
    public boolean canBreakBlock(float blockHardness, int blockHarvestLevel) {
        if (hasRapier()) {
            return blockHardness <= 1 && blockHarvestLevel <= 0;
        }
        return super.canBreakBlock(blockHardness, blockHarvestLevel);
    }
    
    public boolean isRapierOnFire() {
        return entityData.get(RAPIER_ON_FIRE);
    }
    
    @Override
    public boolean attackEntity(Supplier<Boolean> doAttack, StandEntityPunch punch, StandEntityTask task) {
        if (hasRapier() && isRapierOnFire()) {
            return DamageUtil.dealDamageAndSetOnFire(punch.target, 
                    entity -> attackOrDeflect(doAttack, punch, task), 4, true);
        }
        else {
            return attackOrDeflect(doAttack, punch, task);
        }
    }

    private boolean attackOrDeflect(Supplier<Boolean> doAttack, StandEntityPunch punch, StandEntityTask task) {
        if (canDeflectProjectiles() && hasRapier() && punch.target instanceof Projectile) {
            Projectile projectile = (Projectile) punch.target;
            if (projectile.getOwner() == null || !projectile.getOwner().is(getUser())) {
                return deflectProjectile(punch.target);
            }
        }
        return super.attackEntity(doAttack, punch, task);
    }
    
    @Override
    public boolean attackTarget(ActionTarget target, IHasStandPunch punch, StandEntityTask task) {
        if (canDeflectProjectiles()) {
            level.getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(getAttributeValue(ForgeMod.ENTITY_REACH.get())), 
                    entity -> entity.isAlive() && !entity.isPickable()).forEach(projectile -> {
                        if (this.getLookAngle().dot(projectile.getDeltaMovement().reverse().normalize())
                                >= Mth.cos((float) (30.0 + Mth.clamp(getPrecision(), 0, 16) * 30.0 / 16.0) * MathUtil.DEG_TO_RAD)) {
                            deflectProjectile(projectile);
                        }
                    });
        }
        
        return super.attackTarget(target, punch, task);
    }
    
    private boolean canDeflectProjectiles() {
        return getUserPower() == null || getUserPower().getResolveLevel() >= 4;
    }
    
    private boolean deflectProjectile(Entity projectile) {
        if (projectile == null || projectile instanceof ModdedProjectileEntity && !((ModdedProjectileEntity) projectile).canBeDeflected(this)) return false;
        
        JojoModUtil.deflectProjectile(projectile, getLookAngle().normalize().scale(projectile.getDeltaMovement().length()), projectile.position());
        if (projectile instanceof DamagingEntity && ((DamagingEntity) projectile).isFiery()) {
            entityData.set(RAPIER_ON_FIRE, true);
            rapierFireTicks = 300;
        }
        return true;
    }
    
    public void removeRapierFire() {
        if (!level.isClientSide()) {
            rapierFireTicks = 0;
            entityData.set(RAPIER_ON_FIRE, false);
        }
    }
    
    @Override
    protected double leapBaseStrength() {
        return getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
    }
    
    @Override
    public float getUserWalkSpeed() {
        float factor = super.getUserWalkSpeed();
        if (getUserPower() != null && getUserPower().getResolveLevel() >= 4) {
            factor += (1 - factor) * (hasArmor() ? 0.5F : 1F);
        }
        return factor;
    }
    
    @Override
    protected SoundEvent getAttackBlockSound() {
        return hasRapier() ? ModSounds.SILVER_CHARIOT_BLOCK.get() : super.getAttackBlockSound();
    }
    
    @Override
    protected BarrageHitSoundHandler initBarrageHitSoundHandler() {
        return new BarrageHitSoundHandler() {
            @Override
            protected float getSoundGap(StandEntity entity) {
                return Math.min(super.getSoundGap(entity) * 0.5F, 1.5F);
            }
        };
    }
}
