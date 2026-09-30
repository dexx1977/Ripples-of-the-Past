package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class PillarmanVeinEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private float xRotOffset;
    protected float knockback = 0;
    private double yOriginOffset;
    private double xOriginOffset;
    private double zOriginOffset;

    public PillarmanVeinEntity(Level world, LivingEntity entity, float angleXZ, float angleYZ, double offsetX, double offsetY, double offsetZ) {
        super(ModEntityTypes.PILLARMAN_VEINS.get(), entity, world);
        this.xRotOffset = angleXZ;
        this.yRotOffset = angleYZ;
        this.xOriginOffset = offsetX;
        this.yOriginOffset = offsetY;
        this.zOriginOffset = offsetZ;
    }
    
    public PillarmanVeinEntity(EntityType<? extends PillarmanVeinEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            Vec3 center = getBoundingBox().getCenter();
            for (int i = 0; i < 1; i++) {
                Vec3 sparkVec = center.add(new Vec3(
                        (random.nextDouble() - 0.5), 
                        (random.nextDouble() - 0.5),
                        (random.nextDouble() - 0.5))
                        .normalize().scale(random.nextDouble() * 2));
                level.addParticle(ModParticles.BLOOD.get(), false, sparkVec.x, sparkVec.y, sparkVec.z, 0, -1, 0);
            }
        }
    }
    
    @Override
    public float getBaseDamage() {
        return 0.35F;
    }
    
    public void addKnockback(float knockback) {
        this.knockback = knockback;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
    	if(!isRetracting()) {
    		return DamageUtil.dealDamageAndSetOnFire(target, 
                    entity -> super.hurtTarget(entity, owner), 10, true);
    	}
    	return false;
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }
    
    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
                if (knockback > 0 && target instanceof LivingEntity) {
                    DamageUtil.knockback((LivingEntity) target, knockback, yRot);
                }
                setIsRetracting(true);
            }
    }
    
    @Override
    protected void afterBlockHit(BlockHitResult blockRayTraceResult, boolean blockDestroyed) {
        if (!level.isClientSide) {
            if (ForgeEventFactory.getMobGriefingEvent(level, getOwner())) {
                BlockPos blockPos = blockRayTraceResult.getBlockPos();
                BlockState blockState = level.getBlockState(blockPos);
                if (!MRFlameEntity.meltIceAndSnow(level, blockState, blockPos) && 
                		blockState.getCollisionShape(level, blockPos) != Shapes.empty()) {
                    blockPos = blockPos.relative(blockRayTraceResult.getDirection());
                    if (level.isEmptyBlock(blockPos) && !isRetracting()) {
                        level.setBlockAndUpdate(blockPos, ModBlocks.BOILING_BLOOD.get().defaultBlockState().setValue(LiquidBlock.LEVEL, 4));
                        Vec3 center = getBoundingBox().getCenter();
                        level.playSound(null, center.x, center.y, center.z, SoundEvents.LAVA_EXTINGUISH, 
                                SoundSource.AMBIENT, 0.2F, 1.0F);
                    }
                }
            }
        }
    }
    
    @Override
    protected float knockbackMultiplier() {
        return 0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }

    @Override
    public int ticksLifespan() {
        int ticks = super.ticksLifespan();
        return ticks;
    }
    
    @Override
    protected float movementSpeed() {
        return 16 / (float) ticksLifespan();
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }
    
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return new Vec3(xOriginOffset, yOriginOffset, zOriginOffset);
    }

    @Override
    protected Vec3 originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot + xRotOffset, distance);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("YRotOffset", yRotOffset);
        nbt.putFloat("XRotOffset", xRotOffset);
        nbt.putFloat("Knockback", knockback);
        nbt.putDouble("XOriginOffset", xOriginOffset);
        nbt.putDouble("YOriginOffset", yOriginOffset);
        nbt.putDouble("ZOriginOffset", zOriginOffset);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        yRotOffset = nbt.getFloat("YRotOffset");
        xRotOffset = nbt.getFloat("XRotOffset");
        knockback = nbt.getFloat("Knockback");
        xOriginOffset = nbt.getDouble("XOriginOffset");
        yOriginOffset = nbt.getDouble("YOriginOffset");
        zOriginOffset = nbt.getDouble("ZOriginOffset");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(yRotOffset);
        buffer.writeFloat(xRotOffset);
        buffer.writeDouble(xOriginOffset);
        buffer.writeDouble(yOriginOffset);
        buffer.writeDouble(zOriginOffset);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.yRotOffset = additionalData.readFloat();
        this.xRotOffset = additionalData.readFloat();
        this.xOriginOffset = additionalData.readDouble();
        this.yOriginOffset = additionalData.readDouble();
        this.zOriginOffset = additionalData.readDouble();
    }
}
