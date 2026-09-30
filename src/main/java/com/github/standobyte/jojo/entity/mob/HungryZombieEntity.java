package com.github.standobyte.jojo.entity.mob;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.non_stand.VampirismBloodDrain;
import com.github.standobyte.jojo.entity.ai.ZombieFollowOwnerGoal;
import com.github.standobyte.jojo.entity.ai.ZombieNearestAttackableTargetGoal;
import com.github.standobyte.jojo.entity.ai.ZombieOwnerHurtByTargetGoal;
import com.github.standobyte.jojo.entity.ai.ZombieOwnerHurtTargetGoal;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.scores.Team;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.ForgeEventFactory;

public class HungryZombieEntity extends Zombie {
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(HungryZombieEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private double distanceFromOwner;
    private boolean summonedFromAbility = false;

    public HungryZombieEntity(Level world) {
        this(ModEntityTypes.HUNGRY_ZOMBIE.get(), world);
    }

    public HungryZombieEntity(EntityType<? extends HungryZombieEntity> type, Level world) {
        super(type, world);
        xpReward *= 1.5;
    }
    
    public void setSummonedFromAbility() {
        this.summonedFromAbility = true;
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_UUID, Optional.empty());
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(6, new ZombieFollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.targetSelector.addGoal(1, new ZombieOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new ZombieOwnerHurtTargetGoal(this));
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new ZombieNearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (getOwnerUUID() != null) {
            compound.putUUID("Owner", getOwnerUUID());
        }
        compound.putBoolean("AbilitySummon", summonedFromAbility);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        UUID ownerId = compound.hasUUID("Owner") ? compound.getUUID("Owner") : null;
        if (ownerId != null) {
            setOwnerUUID(ownerId);
        }
        summonedFromAbility = compound.getBoolean("AbilitySummon");
    }

    @Nullable
    private UUID getOwnerUUID() {
        return entityData.get(OWNER_UUID).orElse(null);
    }

    private void setOwnerUUID(@Nullable UUID uuid) {
        entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public void setOwner(LivingEntity owner) {
        setOwnerUUID(owner != null ? owner.getUUID() : null);
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uuid = this.getOwnerUUID();
            if (uuid == null) return null;
            Player owner = level.getPlayerByUUID(uuid);
            if (owner != null && INonStandPower.getNonStandPowerOptional(owner).map(
                    power -> power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).map(
                    vampirism -> vampirism.getCuringStage() >= 4).orElse(true)).orElse(false)) {
                setOwner(null);
                return null;
            }
            return owner;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public boolean isEntityOwner(LivingEntity entity) {
        return entityData.get(OWNER_UUID).map(ownerId -> entity.getUUID().equals(ownerId)).orElse(false);
    }
    
    @Override
    protected int getExperienceReward(Player player) {
        return isEntityOwner(player) ? 0 : super.getExperienceReward(player);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            LivingEntity owner = getOwner();
            distanceFromOwner = owner != null ? distanceToSqr(owner) : -1;
        }
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
    
    public boolean farFromOwner(double distance) {
        return distanceFromOwner > Math.pow(distance, 2);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isEntityOwner(target) && super.canAttack(target);
    }

    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Player && owner instanceof Player && !((Player) owner).canHarmPlayer((Player) target)) {
            return false;
        }
        return true;
    }

    @Override
    public Team getTeam() {
        LivingEntity owner = getOwner();
        if (owner != null) {
            return owner.getTeam();
        }
        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        LivingEntity owner = getOwner();
        if (entity == owner) {
            return true;
        }
        if (owner != null) {
            return owner.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }
    
    @Override
    public boolean canBeLeashed(Player player) {
        return !this.isLeashed() && isEntityOwner(player);
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {}

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (getMainHandItem().isEmpty() && target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (livingTarget.getArmorCoverPercentage() <= random.nextFloat() && JojoModUtil.canBleed(livingTarget)) {
                    float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
                    if (VampirismBloodDrain.drainBlood(this, livingTarget, damage / 5F)) {
                        heal(damage / 2F);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void killed(ServerLevel world, LivingEntity entityDead) {
        if (world.getDifficulty() != Difficulty.EASY) createZombie(world, getOwner(), entityDead, isPersistenceRequired());
    }
    
    public static boolean createZombie(ServerLevel world, @Nullable LivingEntity owner, LivingEntity dead, boolean makePersistent) {
        if ((world.getDifficulty() == Difficulty.HARD
                || world.getDifficulty() == Difficulty.NORMAL && dead.getRandom().nextBoolean()
                || world.getDifficulty() == Difficulty.EASY && dead.getRandom().nextFloat() <= 0.125F)) {
            HungryZombieEntity zombie;
            if ((dead instanceof Villager || dead instanceof AbstractIllager) 
                    && ForgeEventFactory.canLivingConvert(dead, ModEntityTypes.HUNGRY_ZOMBIE.get(), (timer) -> {})) {
                Mob deadMob = (Mob) dead;
                zombie = deadMob.convertTo(ModEntityTypes.HUNGRY_ZOMBIE.get(), true);
            }
            else {
                return false;
            }
            zombie.finalizeSpawn(
                    world, 
                    world.getCurrentDifficultyAt(zombie.blockPosition()), 
                    MobSpawnType.CONVERSION, 
                    new Zombie.GroupData(false, true), 
                    null);
            zombie.setOwner(owner);
            ForgeEventFactory.onLivingConvert(dead, zombie);
            if (!dead.isSilent()) {
                world.levelEvent(null, 1026, dead.blockPosition(), 0);
            }
            return true;
        }
        return false;
    }
}
