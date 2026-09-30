package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.entity.EntityMadeFromBlock;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class BlockShardEntity extends ModdedProjectileEntity implements EntityMadeFromBlock {
    private static final EntityDataAccessor<Boolean> CRAZY_D_RESTORED = SynchedEntityData.defineId(BlockShardEntity.class, EntityDataSerializers.BOOLEAN);
    private BlockState blockState;
    private Optional<BlockPos> originBlockPos = Optional.empty();
    private int crazyDRestoreTick = 1;
    
    public BlockShardEntity(LivingEntity shooter, Level world, BlockState blockState, BlockPos originBlockPos) {
        super(ModEntityTypes.BLOCK_SHARD.get(), shooter, world);
        this.blockState = blockState;
        this.originBlockPos = Optional.ofNullable(originBlockPos);
    }

    public BlockShardEntity(EntityType<? extends BlockShardEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    public BlockState getBlock() {
        if (blockState == null) {
            blockState = Blocks.COBBLESTONE.defaultBlockState();
        }
        return blockState;
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return !canUpdate();
    }
    
    @Override
    public int ticksLifespan() {
        return 100;
    }

    // TODO damage based on the block hardness
    @Override
    protected float getBaseDamage() {
        return 2.5f;
    }
    
    public boolean isGlass() {
        return isGlassBlock(getBlock());
    }
    
    @Override
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        if (super.hurtTarget(target, owner)) {
            if (isGlass() && target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (random.nextFloat() < glassShardBleedingChance(livingTarget)) {
                    glassShardBleeding(livingTarget);
                }
            }
            
            return true;
        }
        return false;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    protected boolean constVelocity() {
        return false;
    }
    
    @Override
    protected double getGravityAcceleration() {
        return 0.05;
    }
    
    @Override
    protected boolean hasGravity() {
        return true;
    }
    
    @Override
    public boolean crazyDRestore(BlockPos blockPos) {
        entityData.set(CRAZY_D_RESTORED, true);
        return true;
    }
    
    protected boolean isCrazyDRestored() {
        return entityData.get(CRAZY_D_RESTORED);
    }
    
    @Override
    protected void moveProjectile() {
        if (isCrazyDRestored()) {
            originBlockPos.ifPresent(target -> {
                if (crazyDRestoreTick-- == 0 && !level.isClientSide()) {
                    remove();
                    return;
                }
                
                Vec3 targetPos = Vec3.atCenterOf(target);
                Vec3 vecToTarget = targetPos.subtract(this.position());
                setDeltaMovement(vecToTarget.scale(0.5));
                getUserStandPower().ifPresent(stand -> {
                    stand.consumeStamina(stand.getStaminaTickGain() + ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get().getStaminaCostTicking(stand), true);
                });
                if (level.isClientSide()) {
                    if (ClientUtil.canSeeStands()) {
                        CrazyDiamondHeal.addParticlesAround(this);
                    }
                }
            });
        }
        super.moveProjectile();
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
        if (level.isClientSide() && blockState != null) {
            Vec3 position = position();
            SoundType soundType = blockState.getSoundType();
            SoundEvent sound = soundType.getBreakSound();
            if (sound != null) {
                level.playLocalSound(position.x, position.y, position.z, 
                        sound, SoundSource.BLOCKS, 
                        (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            }
            
            CustomParticlesHelper.addBlockShardBreakParticles(position, blockState);
        }
        super.breakProjectile(targetType, hitTarget);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CRAZY_D_RESTORED, false);
    }
    
    
    
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (blockState != null) {
            nbt.put("Block", NbtUtils.writeBlockState(blockState));
        }
        originBlockPos.ifPresent(pos -> nbt.put("OriginPos", NbtUtils.writeBlockPos(pos)));
        nbt.putBoolean("CDRestore", isCrazyDRestored());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        blockState = NbtUtils.readBlockState(nbt.getCompound("Block"));
        if (blockState.getBlock() == Blocks.AIR) {
            blockState = Blocks.COBBLESTONE.defaultBlockState();
        }
        originBlockPos = MCUtil.nbtGetCompoundOptional(nbt, "OriginPos").map(NbtUtils::readBlockPos);
        entityData.set(CRAZY_D_RESTORED, nbt.getBoolean("CDRestore"));
    }

    

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(Block.getId(getBlock()));
        NetworkUtil.writeOptional(buffer, originBlockPos, buffer::writeBlockPos);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.blockState = Block.stateById(additionalData.readInt());
        this.originBlockPos = NetworkUtil.readOptional(additionalData, FriendlyByteBuf::readBlockPos);
    }
    
    
    public static boolean isGlassBlock(BlockState blockState) {
        return blockState.getMaterial() == Material.GLASS;
    }
    
    public static float glassShardBleedingChance(LivingEntity entity) {
        float armorCover = entity.getArmorCoverPercentage();
        return Math.max(1 - armorCover, 0.05f);
    }
    
    public static void glassShardBleeding(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(ModStatusEffects.BLEEDING.get(), 100, 0, false, false, true));
    }

}
