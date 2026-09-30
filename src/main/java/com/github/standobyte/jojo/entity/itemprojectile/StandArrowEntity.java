package com.github.standobyte.jojo.entity.itemprojectile;

import java.util.Arrays;
import java.util.Optional;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkHooks;

public class StandArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> LOYALTY = SynchedEntityData.defineId(StandArrowEntity.class, EntityDataSerializers.BYTE);
    
    private ItemStack arrowItem = new ItemStack(ModItems.STAND_ARROW.get());
    private boolean dealtDamage;
    
    public StandArrowEntity(Level world, double x, double y, double z, ItemStack arrowItem) {
        super(ModEntityTypes.STAND_ARROW.get(), x, y, z, world);
        setArrowStack(arrowItem);
    }
    
    public StandArrowEntity(Level world, LivingEntity thrower, ItemStack arrowItem) {
        super(ModEntityTypes.STAND_ARROW.get(), thrower, world);
        setArrowStack(arrowItem);
    }
    
    public StandArrowEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
    }
    
    private void setArrowStack(ItemStack arrowItem) {
        this.arrowItem = arrowItem.copy();
        this.entityData.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(arrowItem));
    }

    @Override
    protected void defineSynchedData() {
       super.defineSynchedData();
       entityData.define(LOYALTY, (byte)0);
    }
    
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        if (!level.isClientSide() && target.isAlive()) {
            StandArrowItem.onPiercedByArrow(target, arrowItem, level, Optional.ofNullable(getOwner()));

            if (!arrowItem.isEmpty()) {
                Entity shooter = getOwner();
                LivingEntity living = shooter instanceof LivingEntity ? (LivingEntity) shooter : null;
                ServerPlayer player = living instanceof ServerPlayer ? (ServerPlayer) living : null;
                if ((!(shooter instanceof Player) || !((Player) shooter).abilities.instabuild)) {
                    if (arrowItem.isDamageableItem()) {
                        Item itemType = arrowItem.getItem();
                        if (arrowItem.hurt(1, random, player)) {
                            if (player != null) {
                                ((Player) player).awardStat(Stats.ITEM_BROKEN.get(itemType));
                            }
                            
                            if (!isSilent()) {
                                level.playSound(null, 
                                        getX(), getY(), getZ(), 
                                        SoundEvents.ITEM_BREAK, getSoundSource(), 
                                        0.8F, 0.8F + level.random.nextFloat() * 0.4F);
                            }
                            
                            for (int i = 0; i < 25; ++i) {
                                Vec3 offset = new Vec3((random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0);
                                offset = offset.xRot(-xRot * MathUtil.DEG_TO_RAD);
                                offset = offset.yRot(-yRot * MathUtil.DEG_TO_RAD);
                                
                                if (level instanceof ServerLevel) {
                                    ((ServerLevel) level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, arrowItem), 
                                            getX(), getY(0.5), getZ(), 5, 0, 0, 0, 0);
                                }
                            }
                            
                            remove();
                            arrowItem.setDamageValue(0);
                        }

                    }
                }
            }
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return arrowItem.copy();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        Entity target = entityRayTraceResult.getEntity();
        
        int damage = Mth.ceil(Mth.clamp(getDeltaMovement().length() * getBaseDamage(), 0.0D, 2.147483647E9D));
        if (isCritArrow()) {
            damage = (int) Math.min((long) random.nextInt(damage / 2 + 2) + (long) damage, 2147483647L);
        }

        Entity shooter = getOwner();
        DamageSource damageSource;
        if (shooter == null) {
            damageSource = DamageSource.arrow(this, this);
        }
        else {
            damageSource = DamageSource.arrow(this, shooter);
            if (shooter instanceof LivingEntity) {
                ((LivingEntity) shooter).setLastHurtMob(target);
            }
        }
        dealtDamage = true;
        
        if (target instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) target;
            if (StandUtil.isEntityStandUser(targetLiving) || StandVirusEffect.mobMayGetStand(targetLiving)) {
                damage /= 4;
            }
        }
        
        boolean dodge = target.getType() == EntityType.ENDERMAN;
        int prevTargetFireTimer = target.getRemainingFireTicks();
        if (isOnFire() && !dodge) {
            target.setSecondsOnFire(5);
        }
        
        if (shooter != null) {
            arrowItem.hurtAndBreak(1, (LivingEntity) shooter, entity -> remove());
        }
        
        if (target.hurt(damageSource, (float) damage)) {
            if (dodge) {
                return;
            }

            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;

                if (!level.isClientSide && shooter instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingTarget, shooter);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) shooter, livingTarget);
                }

                doPostHurtEffects(livingTarget);
                if (shooter != null && livingTarget != shooter && livingTarget instanceof Player
                        && shooter instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) shooter).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!level.isClientSide && shooter instanceof ServerPlayer) {
                    if (!target.isAlive() && shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger((ServerPlayer) shooter, Arrays.asList(target));
                    }
                }
            }

            playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            setDeltaMovement(getDeltaMovement().scale(-0.05D));
        } 
        else {
            target.setRemainingFireTicks(prevTargetFireTimer);
            setDeltaMovement(getDeltaMovement().scale(-0.05D));
            yRot += 180.0F;
            yRotO += 180.0F;
        }
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 pos, Vec3 nextPos) {
        return dealtDamage ? null : super.findHitEntity(pos, nextPos);
    }
    
    @Override
    public double getBaseDamage() {
        return super.getBaseDamage() + EnchantmentHelper.getDamageBonus(arrowItem, MobType.UNDEFINED);
    }
    
    @Override
    public void tick() {
        if (inGroundTime > 4) {
            dealtDamage = true;
        }
        Entity owner = getOwner();
        if ((dealtDamage || isNoPhysics()) && owner != null) {
            int loyalty = entityData.get(LOYALTY);
            if (loyalty > 0) {
                if (!owner.isAlive() || owner instanceof Player && JojoModUtil.getGameModeConsiderPossessing((Player) owner) == GameType.SPECTATOR) {
                    if (!level.isClientSide && pickup == AbstractArrow.Pickup.ALLOWED) {
                        spawnAtLocation(getPickupItem(), 0.1F);
                    }
                    remove();
                }
                else {
                    setNoPhysics(true);
                    Vec3 posDiffToOwner = new Vec3(owner.getX() - getX(), owner.getEyeY() - getY(), owner.getZ() - getZ());
                    setPosRaw(getX(), getY() + posDiffToOwner.y * 0.015D * (double) loyalty, getZ());
                    if (level.isClientSide) {
                        yOld = getY();
                    }
                    setDeltaMovement(getDeltaMovement().scale(0.95D).add(posDiffToOwner.normalize().scale(0.05D * (double) loyalty)));
                }
            }
        }
        super.tick();
    }

    @Override
    public void playerTouch(Player player) {
        Entity owner = this.getOwner();
        if (entityData.get(LOYALTY) == 0 || owner == null || owner.getUUID() == player.getUUID()) {
            super.playerTouch(player);
        }
    }

    @Override
    public void tickDespawn() {
        if (pickup != AbstractArrow.Pickup.ALLOWED || entityData.get(LOYALTY) <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Arrow", 10)) {
            arrowItem = ItemStack.of(compound.getCompound("Arrow"));
        }
        entityData.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(arrowItem));
        dealtDamage = compound.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Arrow", arrowItem.save(new CompoundTag()));
        compound.putBoolean("DealtDamage", dealtDamage);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
//    public static class EntityPierce {
//        private static final List<PierceBehavior> ARROW_PIERCE_BEHAVIOR = new ArrayList<>();
//        private static final Random RANDOM = new Random();
//        
//        public static void addBehavior(@Nonnull Supplier<Predicate<LivingEntity>> entityCheck, @Nonnull Supplier<Consumer<LivingEntity>> entityConverter) {
//            ARROW_PIERCE_BEHAVIOR.add(new PierceBehavior(entityCheck, entityConverter));
//        }
//        
//        public static boolean onArrowPierce(LivingEntity entity) {
//            List<Consumer<LivingEntity>> converters = 
//                    ARROW_PIERCE_BEHAVIOR.stream()
//                    .filter(behavior -> behavior.entityCheck.get().test(entity))
//                    .map(behavior -> behavior.entityConverter.get())
//                    .collect(Collectors.toCollection(ArrayList::new));
//            if (!converters.isEmpty()) {
//                converters.get(RANDOM.nextInt(converters.size())).accept(entity);
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        
//        private static class PierceBehavior {
//            private final Supplier<Predicate<LivingEntity>> entityCheck;
//            private final Supplier<Consumer<LivingEntity>> entityConverter;
//            
//            private PierceBehavior(@Nonnull Supplier<Predicate<LivingEntity>> entityCheck, @Nonnull Supplier<Consumer<LivingEntity>> entityConverter) {
//                this.entityCheck = entityCheck;
//                this.entityConverter = entityConverter;
//            }
//        }
//    }
}
