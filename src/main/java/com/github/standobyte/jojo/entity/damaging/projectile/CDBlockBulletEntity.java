package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Optional;
import java.util.UUID;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class CDBlockBulletEntity extends ModdedProjectileEntity {
    private Block block;
    private ResourceLocation blockTex = null;
    private Optional<Entity> homingTarget = Optional.empty();
    private boolean soundStarted = false;
    
    public CDBlockBulletEntity(LivingEntity shooter, Level world) {
        super(ModEntityTypes.CD_BLOCK_BULLET.get(), shooter, world);
    }

    public CDBlockBulletEntity(EntityType<? extends CDBlockBulletEntity> type, Level world) {
        super(type, world);
    }
    
    public void setTarget(Entity target) {
        this.homingTarget = Optional.ofNullable(target);
    }
    
    public void setBlock(Block block) {
        this.block = block;
    }
    
    public Block getBlock() {
        return block;
    }
    
    public ResourceLocation getBlockTex() {
        return blockTex;
    }
    
    public void setBlockTex(ResourceLocation texture) {
        this.blockTex = texture;
    }
    

    @Override
    public int ticksLifespan() {
        return 6000;
    }

    @Override
    protected void moveProjectile() {
        super.moveProjectile();
        homingTarget.ifPresent(target -> {
            if (!target.isAlive()) {
                homingTarget = Optional.empty();
            }
            else if (tickCount >= 10) {
                Vec3 targetPos = target.getBoundingBox().getCenter();
                Vec3 vecToTarget = targetPos.subtract(this.position());
                setDeltaMovement(vecToTarget.normalize().scale(this.getDeltaMovement().length()));
                getUserStandPower().ifPresent(stand -> {
                    stand.consumeStamina(stand.getStaminaTickGain() + ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get().getStaminaCostTicking(stand), true);
                });
                if (level.isClientSide()) {
                    if (ClientUtil.canSeeStands()) {
                        CrazyDiamondHeal.addParticlesAround(this);
                        
                        target.getBoundingBox().clip(position(), targetPos).ifPresent(pos -> {
                            level.addParticle(ModParticles.CD_RESTORATION.get(), 
                                    pos.x + (random.nextDouble() - 0.5) * 0.25, 
                                    pos.y + (random.nextDouble() - 0.5) * 0.25, 
                                    pos.z + (random.nextDouble() - 0.5) * 0.25, 
                                    0, 0, 0);
                        });
                    }
                    if (!soundStarted && ClientUtil.canHearStands()) {
                        ClientTickingSoundsHelper.playEntitySound(this, ModSounds.CRAZY_DIAMOND_FIX_STARTED.get(), 1, 1, false);
                        ClientTickingSoundsHelper.playEntitySound(this, ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), 1, 1, true);
                        
                        ClientTickingSoundsHelper.playStoppableEntitySound(target, ModSounds.CRAZY_DIAMOND_FIX_STARTED.get(), 1, 1, false, e -> this.isAlive());
                        ClientTickingSoundsHelper.playStoppableEntitySound(target, ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), 1, 1, true, e -> this.isAlive());
                        soundStarted = true;
                    }
                }
            }
        });
    }

    @Override
    protected float getBaseDamage() {
        return 5;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (block == Blocks.MAGMA_BLOCK) {
            return DamageUtil.dealDamageAndSetOnFire(target, 
                    entity -> super.hurtTarget(target, owner), 4, true);
        }
        boolean hurt = super.hurtTarget(target, owner);
        
        if (hurt) {
            if (block == Blocks.NOTE_BLOCK) {
                target.playSound(NoteBlockInstrument.values()[random.nextInt(NoteBlockInstrument.values().length)].getSoundEvent(), 
                        5.0F, (float) Math.pow(2.0D, (double) (random.nextInt(24) - 12) / 12.0D));
            }
            
            else if (target instanceof LivingEntity) {
                if (block == Blocks.ICE) {
                    ((LivingEntity) target).addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), 60, 0));
                }
                else if (block == Blocks.PACKED_ICE) {
                    ((LivingEntity) target).addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), 60, 1));
                }
                else if (block == Blocks.BLUE_ICE) {
                    ((LivingEntity) target).addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), 60, 2));
                }
            }
        }
        return hurt;
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
        if (!level.isClientSide() && (
                block == Blocks.INFESTED_CHISELED_STONE_BRICKS ||
                block == Blocks.INFESTED_COBBLESTONE ||
                block == Blocks.INFESTED_CRACKED_STONE_BRICKS || 
                block == Blocks.INFESTED_MOSSY_STONE_BRICKS || 
                block == Blocks.INFESTED_STONE || 
                block == Blocks.INFESTED_STONE_BRICKS)) {
            Silverfish silverfish = EntityType.SILVERFISH.create(level);
            silverfish.moveTo(getX(), getY(0.5), getZ(), 0.0F, 0.0F);
            level.addFreshEntity(silverfish);
            silverfish.spawnAnim();
        }
        super.breakProjectile(targetType, hitTarget);
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 1.0F;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

    private UUID targetUUID;
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        homingTarget.ifPresent(target -> {
            nbt.putUUID("HomingTarget", target.getUUID());
        });
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.hasUUID("HomingTarget")) {
            targetUUID = nbt.getUUID("HomingTarget");
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (targetUUID != null) {
            setTarget(((ServerLevel) level).getEntity(targetUUID));
        }
        super.writeSpawnData(buffer);
        NetworkUtil.writeOptionally(buffer, block, buffer::writeRegistryId);
        NetworkUtil.writeOptionally(buffer, homingTarget.map(target -> target.getId()).orElse(null), buffer::writeInt);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        NetworkUtil.readOptional(additionalData, () -> additionalData.readRegistryIdSafe(Block.class)).ifPresent(block -> setBlock(block));
        NetworkUtil.readOptional(additionalData, additionalData::readInt).ifPresent(id -> setTarget(level.getEntity(id)));
    }
}
