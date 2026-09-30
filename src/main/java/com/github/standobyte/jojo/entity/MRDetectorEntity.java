package com.github.standobyte.jojo.entity;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class MRDetectorEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Boolean> ENTITY_DETECTED = SynchedEntityData.defineId(MRDetectorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DETECTED_X = SynchedEntityData.defineId(MRDetectorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DETECTED_Y = SynchedEntityData.defineId(MRDetectorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DETECTED_Z = SynchedEntityData.defineId(MRDetectorEntity.class, EntityDataSerializers.FLOAT);
    private LivingEntity owner;
    
    public MRDetectorEntity(LivingEntity owner, Level world) {
        this(ModEntityTypes.MR_DETECTOR.get(), world);
        this.owner = owner;
    }
    
    public MRDetectorEntity(EntityType<?> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
            return;
        }
        super.tick();
        if (tickCount < 600 && owner != null && owner.isAlive()) {
            Vec3 newPos = owner.getEyePosition(1.0F).add(new Vec3(-0.5, -0.5, 1.5).yRot(-owner.yRot * MathUtil.DEG_TO_RAD));
            setPos(newPos.x, newPos.y, newPos.z);
        }
        else {
            if (!level.isClientSide()) discard();
            return;
        }
        if (!level.isClientSide()) {
            Vec3 detectedOffset = detectEntities();
            setDetectedOffset(detectedOffset);
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerLevel) level);
        }
    }
    
    public static final double DETECTION_RADIUS = 15;
    @Nullable
    private Vec3 detectEntities() {
        AABB aabb = new AABB(position(), position()).inflate(DETECTION_RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, 
                EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS)
                .and(entity -> entity.getType() != EntityType.ARMOR_STAND && entity != owner && 
                (owner == null || 
                !(entity.isAlliedTo(owner) || IStandPower.getStandPowerOptional(owner).map(stand -> entity == stand.getStandManifestation()).orElse(false)))));
        Optional<LivingEntity> closestDetected = entities.stream().min((e1, e2) -> (int) (e1.distanceToSqr(this) - e2.distanceToSqr(this)));
        closestDetected.ifPresent(entity -> {
            if (this.getBoundingBox().intersects(entity.getBoundingBox())) {
                DamageUtil.setOnFire(entity, 4, true);
            }
        });
        return closestDetected.isPresent() ? closestDetected.get().position().subtract(position()) : null;
    }
    
    private void setDetectedOffset(@Nullable Vec3 detected) {
        if (detected == null) {
            entityData.set(ENTITY_DETECTED, false);
        }
        else {
            entityData.set(ENTITY_DETECTED, true);
            entityData.set(DETECTED_X, (float) detected.x);
            entityData.set(DETECTED_Y, (float) detected.y);
            entityData.set(DETECTED_Z, (float) detected.z);
        }
    }
    
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> parameter) {
        if (level.isClientSide() && ENTITY_DETECTED.equals(parameter) && isEntityDetected()
                && ClientUtil.canHearStands()) {
            ClientTickingSoundsHelper.playMagiciansRedDetectorSound(this);
        }
        super.onSyncedDataUpdated(parameter);
    }
    
    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return !StandUtil.clStandEntityVisibleTo(player)
                || !JojoModUtil.seesInvisibleAsSpectator(player) && super.isInvisible();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ENTITY_DETECTED, false);
        entityData.define(DETECTED_X, 0F);
        entityData.define(DETECTED_Y, 0F);
        entityData.define(DETECTED_Z, 0F);
    }
    
    public boolean isEntityDetected() {
        return entityData.get(ENTITY_DETECTED);
    }
    
    public Vector3f getDetectedDirection() {
        return new Vector3f(entityData.get(DETECTED_X), entityData.get(DETECTED_Y), entityData.get(DETECTED_Z));
    }
    
    public Entity getOwner() {
        return owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        // no save
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        // no save
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(owner != null ? owner.getId() : -1);
    }
    
    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity owner = level.getEntity(additionalData.readInt());
        if (owner instanceof LivingEntity) {
            this.owner = (LivingEntity) owner;
        }
    }
    
}
