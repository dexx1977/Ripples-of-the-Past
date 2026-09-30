package com.github.standobyte.jojo.entity;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.PlaneRectangle;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class HamonProjectileShieldEntity extends Entity implements IEntityAdditionalSpawnData {
    private LivingEntity user;
    private INonStandPower power;
    private HamonData hamon;
    
    private float width;
    private float height;
    private PlaneRectangle shieldPlane;
    
    public HamonProjectileShieldEntity(Level world, @Nonnull LivingEntity hamonUser, float width, float height) {
        this(ModEntityTypes.HAMON_PROJECTILE_SHIELD.get(), world);
        this.user = hamonUser;
        this.power = INonStandPower.getNonStandPowerOptional(hamonUser).orElse(null);
        if (power != null) {
            hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).orElse(null);
        }
        this.width = width;
        this.height = height;
        refreshDimensions();
    }

    public HamonProjectileShieldEntity(EntityType<?> type, Level world) {
        super(type, world);
    }
    
    
    @Override
    public void tick() {
        super.tick();
        if (user == null || !user.isAlive() || !level.isClientSide() && 
                (power == null || power.getHeldAction() != ModHamonActions.HAMON_PROJECTILE_SHIELD.get() || hamon == null)) {
            if (!level.isClientSide()) discard();
            return;
        }
        updateShieldPos();
        
        level.getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(24), 
                entity -> entity.isAlive()).forEach(projectile -> {
                    HitResult rayTrace = ProjectileUtil.getHitResultOnMoveVector(projectile, 
                            target -> target != this && !target.isSpectator() && target.isAlive() && !target.is(projectile.getOwner()));
                    if (rayTrace.getType() != HitResult.Type.BLOCK) {
                        Vec3 intersectionPoint = shieldPlane.projectileIsPassing(projectile);
                        if (intersectionPoint != null) {
                            deflectProjectile(projectile, intersectionPoint);
                        }
                    }
                    
                });
        
        if (level.isClientSide()) {
            int particlesCount = (int) (width * height * 0.1F);
            for (int i = 0; i < particlesCount; i++) {
                Vec3 pos = shieldPlane.getUniformRandomPos();
                level.addParticle(ModParticles.HAMON_SPARK.get(), pos.x, pos.y, pos.z, 0, 0, 0);
            }
            HamonSparksLoopSound.playSparkSound(this, getBoundingBox().getCenter(), 1.0F);
        }
    }
    
    
    public void updateShieldPos() {
        Vec3 shieldPos = new Vec3(user.getX(), user.getY(0.5F) - height * 0.5F, user.getZ())
                .add(new Vec3(0, 0, 2F)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD));
        setPos(shieldPos.x, shieldPos.y, shieldPos.z);
    }
    
    @Override
    public void setPos(double pX, double pY, double pZ) {
        this.setPosRaw(pX, pY, pZ);
        AABB aabb = this.getDimensions(null).makeBoundingBox(pX, pY, pZ);
        this.setBoundingBox(aabb);
        Vec3 center = aabb.getCenter();
        this.shieldPlane = PlaneRectangle.create(center, xRot, yRot, width, height);
    }
    
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions defaultSize = super.getDimensions(pose);
        return new EntityDimensions(width, height, defaultSize.fixed);
    }
    
    
    public PlaneRectangle getShieldRectangle() {
        return shieldPlane;
    }
    
    @Override
    public void push(Entity entity) {}

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        return false;
    }

//    @Override
//    public void onAddedToWorld() {
//        super.onAddedToWorld();
//        level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(data -> {
//            data.projectileShields.put(this.getUUID(), this);
//        });
//    }
//
//    @Override
//    public void onRemovedFromWorld() {
//        super.onRemovedFromWorld();
//        level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(data -> {
//            data.projectileShields.remove(this.getUUID());
//        });
//    }
    
    private void deflectProjectile(Projectile projectile, Vec3 intersectionPoint) {
        if (projectile == null || projectile instanceof ModdedProjectileEntity && !((ModdedProjectileEntity) projectile).canBeDeflected(this)) return;
        
        float speed = (float) projectile.getDeltaMovement().length();
        if (power != null && hamon != null) {
            float energyCost = speed * 20;
            if (power.hasEnergy(energyCost)) {
                JojoModUtil.deflectProjectile(projectile, null);
            }
            if (!level.isClientSide()) {
                if (power.consumeEnergy(energyCost)) {
                    DamageUtil.dealHamonDamage(projectile, 0.1F, this, user);
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, energyCost);
                }
                else {
                    power.setEnergy(0);
                    discard();
                }
            }
        }
        if (level.isClientSide()) {
            HamonUtil.emitHamonSparkParticles(level, ClientUtil.getClientPlayer(), intersectionPoint, 5F);
        }
    }
    
    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        width = nbt.getFloat("Width");
        height = nbt.getFloat("Height");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putFloat("Width", width);
        nbt.putFloat("Height", height);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(width);
        buffer.writeFloat(height);
        
        buffer.writeInt(user.getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.width = additionalData.readFloat();
        this.height = additionalData.readFloat();
        absMoveTo(xo, yo, zo, yRot, xRot);
        
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            user = (LivingEntity) entity;
            this.power = INonStandPower.getNonStandPowerOptional(user).orElse(null);
            if (power != null) {
                hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).orElse(null);
            }
        }
    }

}
