package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class HGBarrierEntity extends OwnerBoundProjectileEntity {
    protected static final EntityDataAccessor<Boolean> WAS_RIPPED = SynchedEntityData.defineId(HGBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Optional<Vec3>> RIPPED_POINT = SynchedEntityData.defineId(HGBarrierEntity.class, 
            ModDataSerializers.OPTIONAL_VECTOR3D.get());
    public static final EntityDataAccessor<Optional<ResourceLocation>> DATA_PARAM_STAND_SKIN = SynchedEntityData.defineId(HGBarrierEntity.class, 
            ModDataSerializers.OPTIONAL_RES_LOC.get());
    private boolean rippedHurtOwner = false;
    private LivingEntity standUser;
    private BlockPos originBlockPos;
    private int rippedTicks = -1;
    private boolean timeStop = false;
    
    public HGBarrierEntity(Level world, StandEntity entity) {
        super(ModEntityTypes.HG_BARRIER.get(), entity, world);
        this.standUser = entity.getUser();
    }
    
    public HGBarrierEntity(EntityType<? extends HGBarrierEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    public void setOriginBlockPos(BlockPos blockPos) {
        this.originBlockPos = blockPos;
    }
    
    public BlockPos getOriginBlockPos() {
        return originBlockPos;
    }
    
    @Override
    public void tick() {
        if (!timeStop) {
            if (rippedTicks > 0) {
                if (--rippedTicks == 0) {
                    if (!level.isClientSide()) {
                        remove();
                    }
                    return;
                }
                if (!level.isClientSide() && !rippedHurtOwner) {
                    DamageUtil.hurtThroughInvulTicks(standUser, DamageSource.GENERIC, 0.2F);
                    rippedHurtOwner = true;
                }
            }
            else {
                if (standUser == null) {
                    remove();
                    return;
                }
                super.tick();
                if (!isAlive()) {
                    return;
                }
            }
        }
        else if (!level.isClientSide()) {
            if (!wasRipped()) {
                HitResult[] rayTrace = rayTrace();
                for (HitResult result : rayTrace) {
                    if (result.getType() == HitResult.Type.ENTITY && !ForgeEventFactory.onProjectileImpact(this, result)) {
                        ripAt(result.getLocation());
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    protected float knockbackMultiplier() {
        return 0;
    }

    @Override
    protected boolean moveBoundToOwner() {
        moveTo(getOwner().getEyePosition(1.0F).add(getOwnerRelativeOffset()));
        return true;
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }
    
    @Override
    public Vec3 getOriginPoint(float partialTick) {
        return originBlockPos == null
                ? standUser == null ? position() : MCUtil.getEntityPosition(standUser, partialTick)
                        : Vec3.atCenterOf(originBlockPos);
    }

    @Deprecated
    @Override
    protected Vec3 getNextOriginOffset() { return Vec3.ZERO; }
    
    @Override
    protected void onHitBlock(BlockHitResult result) {}

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(WAS_RIPPED, false);
        entityData.define(RIPPED_POINT, Optional.empty());
        entityData.define(DATA_PARAM_STAND_SKIN, Optional.empty());
    }
    
    private static final Vec3 OFFSET = new Vec3(0.15D, -1.4D, 0);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
        super.onSyncedDataUpdated(dataParameter);
        if (WAS_RIPPED.equals(dataParameter) && wasRipped()) {
            rippedTicks = 40;
        }
    }
    
    public boolean wasRipped() {
        return entityData.get(WAS_RIPPED);
    }
    
    public Optional<Vec3> wasRippedAt() {
        return entityData.get(RIPPED_POINT);
    }
    
    private void ripAt(@Nonnull Vec3 pos) {
        entityData.set(WAS_RIPPED, true);
        entityData.set(RIPPED_POINT, Optional.of(pos));
    }
    
    @Override
    public boolean isGlowing() {
        return level.isClientSide() && !timeStop && wasRipped() && getOwner() instanceof StandEntity && 
                ((StandEntity) getOwner()).getUser() == ClientUtil.getClientPlayer() || super.isGlowing();
    }
    
    @Override
    public int getTeamColor() {
        return wasRipped() ? 0xFF0000 : super.getTeamColor();
    }
    
    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        if (getBlockPosAttachedTo().isPresent()) {
            Entity target = entityRayTraceResult.getEntity();
            target.setDeltaMovement(Vec3.ZERO);
            if (!level.isClientSide()) {
                super.onHitEntity(entityRayTraceResult);
                ripAt(entityRayTraceResult.getLocation());
                if (getOwner() instanceof HierophantGreenEntity) {
                    HierophantGreenEntity stand = (HierophantGreenEntity) getOwner();
                    stand.getBarriersNet().shootEmeraldsFromBarriers(stand.getUserPower(), stand, 
                            target.getBoundingBox().getCenter(), 0, 20 * stand.getStaminaCondition(), 
                            ModStandsInit.HIEROPHANT_GREEN_EMERALD_SPLASH.get().getStaminaCostTicking(stand.getUserPower()) * 0.5F, 2, false);
                }
                MCUtil.playSound(level, null, target.getX(), target.getY(), target.getZ(), 
                        ModSounds.HIEROPHANT_GREEN_BARRIER_RIPPED.get(), getSoundSource(), 1.0F, 1.0F, StandUtil::playerCanHearStands);
            }
        }
    }

    @Override
    public int ticksLifespan() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    protected float movementSpeed() {
        return 0F;
    }

    @Override
    public float getBaseDamage() {
        return 2.0F;
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public void canUpdate(boolean canUpdate) {
        timeStop = !canUpdate;
    }
    
    @Override
    public boolean canUpdate() {
        return true;
    }
    
    @Override
    public void withStandSkin(Optional<ResourceLocation> skinLocation) {
        entityData.set(DATA_PARAM_STAND_SKIN, skinLocation);
    }

    @Override
    public Optional<ResourceLocation> getStandSkin() {
        return entityData.get(DATA_PARAM_STAND_SKIN);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(standUser != null ? standUser.getId() : -1);
        boolean isBlockOrigin = originBlockPos != null;
        buffer.writeBoolean(isBlockOrigin);
        if (isBlockOrigin) {
            buffer.writeBlockPos(originBlockPos);
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            standUser = (LivingEntity) entity;
        }
        if (additionalData.readBoolean()) {
            originBlockPos = additionalData.readBlockPos();
        }
    }

}
