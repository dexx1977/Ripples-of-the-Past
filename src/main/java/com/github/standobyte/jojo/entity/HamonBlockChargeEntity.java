package com.github.standobyte.jojo.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonCharge;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

// TODO hitbox depending on the block hitbox
public class HamonBlockChargeEntity extends Entity {
    private static final EntityDataAccessor<Boolean> CACTUS_EXPLOSION = SynchedEntityData.defineId(HamonBlockChargeEntity.class, EntityDataSerializers.BOOLEAN);
    private HamonCharge hamonCharge;
    
    public HamonBlockChargeEntity(Level world, BlockPos blockPos) {
        this(ModEntityTypes.HAMON_BLOCK_CHARGE.get(), world);
        this.moveTo(Vec3.atBottomCenterOf(blockPos));
    }

    public HamonBlockChargeEntity(EntityType<?> type, Level world) {
        super(type, world);
        noPhysics = true;
        setNoGravity(true);
    }
    
    public void setCharge(float tickDamage, int chargeTicks, @Nullable LivingEntity hamonUser, float energySpent) {
        this.hamonCharge = new HamonCharge(tickDamage, chargeTicks, hamonUser, energySpent);
    }
    
    private static final int CACTUS_EXPLOSION_RANGE = 4;
    @Override
    public void tick() {
        super.tick();
        BlockPos blockPos = blockPosition();
        Vec3 pos = Vec3.atCenterOf(blockPos);
        if (!level.isClientSide()) {
            if (hamonCharge == null || hamonCharge.shouldBeRemoved() || blockPos == null || level.isEmptyBlock(blockPos)) {
                if (level.getBlockState(blockPos).getBlock() == Blocks.COBWEB) {
                    level.setBlock(blockPos, Blocks.TRIPWIRE.defaultBlockState(), 3);
                }
                remove();
                return;
            }
            hamonCharge.tick(null, blockPos, level, getBoundingBox().inflate(0.1D));
            if (tickCount == 60) {
                Block block = level.getBlockState(blockPos).getBlock();
                if (block == Blocks.CACTUS || block == Blocks.POTTED_CACTUS) {
                    int range = CACTUS_EXPLOSION_RANGE;
                    AABB aabb = new AABB(blockPos).inflate(range);
                    List<Entity> targets = level.getEntities(this, aabb);
                    targets.forEach(entity -> {
                        entity.hurt(DamageSource.CACTUS, 0.2F * (3F * range * range - (float) entity.distanceToSqr(pos)));
                    });
                    entityData.set(CACTUS_EXPLOSION, true);
                    MCUtil.destroyBlock(level, blockPos, false, null);
                }
            }
        }
        else {
            HamonSparksLoopSound.playSparkSound(this, pos, 1.0F, true);
            CustomParticlesHelper.createHamonSparkParticles(null, getRandomX(0.5), getRandomY(), getRandomZ(0.5), 1);
        }
    }
    
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> parameter) {
        super.onSyncedDataUpdated(parameter);
        if (level.isClientSide() && CACTUS_EXPLOSION.equals(parameter) && entityData.get(CACTUS_EXPLOSION)) {
            for (int i = 0; i < 12; i++) {
                level.addParticle(ParticleTypes.SPLASH, 
                        getX() + random.nextDouble() - 0.5D, 
                        getY() + random.nextDouble(), 
                        getZ() + random.nextDouble() - 0.5D, 0.0D, 0.0D, 0.0D);
            }
            level.addParticle(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 1.0D, 0.0D, 0.0D);
            level.playLocalSound(getX(), getY() + 0.5, getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 
                    0.5F, 1.35F + random.nextFloat() * 0.15F, false);
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CACTUS_EXPLOSION, false);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.tickCount = nbt.getInt("Age");
        if (nbt.contains("HamonCharge", 10)) {
            this.hamonCharge = HamonCharge.fromNBT(nbt.getCompound("HamonCharge"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Age", tickCount);
        if (hamonCharge != null) {
            nbt.put("HamonCharge", hamonCharge.toNBT());
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
