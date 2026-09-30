package com.github.standobyte.jojo.entity.damaging.projectile;

import net.minecraft.world.damagesource.DamageTypes;
import com.github.standobyte.jojo.util.mc.damage.ModDamageTypes;
import java.util.List;

import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCap;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class MolotovEntity extends ThrowableItemProjectile implements ItemSupplier {

    public MolotovEntity(EntityType<? extends MolotovEntity> type, Level world) {
        super(type, world);
    }

    public MolotovEntity(Level world, LivingEntity shooter) {
        super(ModEntityTypes.MOLOTOV.get(), shooter, world);
    }

    public MolotovEntity(Level world, double x, double y, double z) {
        super(ModEntityTypes.MOLOTOV.get(), x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOLOTOV.get();
    }
    
    @Override
    protected float getGravity() {
        return 0.05F;
    }
    
    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!level.isClientSide) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.SPLASH_POTION_BREAK, 
                    SoundSource.NEUTRAL, 1.0F, random.nextFloat() * 0.1F + 0.9F);
            remove();
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        if (!level.isClientSide) {
            setBlocksOnFire(pResult.getBlockPos(), 3);
            setEntitiesOnFire(pResult.getBlockPos(), 3);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!level.isClientSide) {
            Entity entity = pResult.getEntity();
            Entity owner = getOwner();
            entity.hurt(owner != null ? ModDamageTypes.source(owner, DamageTypes.IN_FIRE) : ModDamageTypes.source(entity, DamageTypes.IN_FIRE), 2);
            entity.setSecondsOnFire(10);
            setBlocksOnFire(this.blockPosition(), 2);
            setEntitiesOnFire(this.blockPosition(), 2);
        }
    }
    
    protected void setBlocksOnFire(BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) <= radius) {
                        BlockPos pos = center.offset(x, y, z);
                        if (level.isEmptyBlock(pos)) {
                            level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                        }
                    }
                }
            }
        }
    }
    
    protected void setEntitiesOnFire(BlockPos center, double radius) {
        List<Entity> targets = level.getEntities(this, new AABB(center).inflate(radius));
        for (Entity target : targets) {
            if (target.distanceToSqr(center.getX(), center.getY(), center.getZ()) < radius * radius) {
                target.setSecondsOnFire(4);
            }
        }
    }
    
    public void onHitWithHamonCharge(HitResult target, ProjectileHamonChargeCap bottleCharge) {
        if (!level.isClientSide()) {
            if (target.getType() == HitResult.Type.BLOCK) {
                LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;
                BlockHitResult blockTarget = (BlockHitResult) target;
                
                HamonSendoOverdriveEntity sendoOverdrive1 = new HamonSendoOverdriveEntity(level, 
                        owner, blockTarget.getDirection().getAxis())
                        .setRadius(3f)
                        .setWaveDamage(bottleCharge.getHamonDamage() / 4)
                        .setWavesCount(2);
                sendoOverdrive1.moveTo(Vec3.atCenterOf(blockTarget.getBlockPos())
                        .subtract(0, sendoOverdrive1.getDimensions(null).height * 0.5, 0));
                sendoOverdrive1.setBlockTarget(blockTarget.getBlockPos(), blockTarget.getDirection());
                level.addFreshEntity(sendoOverdrive1);
                
                HamonSendoOverdriveEntity sendoOverdrive2 = new HamonSendoOverdriveEntity(level, 
                        owner, blockTarget.getDirection().getAxis())
                        .setRadius(2)
                        .setWaveDamage(bottleCharge.getHamonDamage() / 4)
                        .setWavesCount(4);
                sendoOverdrive2.moveTo(Vec3.atCenterOf(blockTarget.getBlockPos())
                        .subtract(0, sendoOverdrive2.getDimensions(null).height * 0.5, 0));
                sendoOverdrive2.setBlockTarget(blockTarget.getBlockPos(), blockTarget.getDirection());
                level.addFreshEntity(sendoOverdrive2);
            }
        }
    }
    
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
