package com.github.standobyte.jojo.power.impl.stand.type;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

public class EntityStandType<T extends StandStats> extends StandType<T> {
    private Supplier<? extends StandEntityType<? extends StandEntity>> entityTypeSupplier = null;
    private final boolean manualControlEnabled;
    private final boolean standLeapEnabled;
    private Optional<StandAction> finisherPunch = Optional.empty();
    
    @Deprecated
    public EntityStandType(int color, Component partName, 
            StandAction[] attacks, StandAction[] abilities, 
            Class<T> statsClass, T defaultStats, @Nullable StandTypeOptionals additions) {
        super(color, partName, attacks, abilities, abilities.length > 0 ? abilities[0] : null, statsClass, defaultStats, additions);
        manualControlEnabled = true;
        standLeapEnabled = true;
    }
    
    protected EntityStandType(EntityStandType.AbstractBuilder<?, T> builder) {
        super(builder);
        this.manualControlEnabled = builder.manualControlEnabled;
        this.standLeapEnabled = builder.standLeapEnabled;
    }
    
    
    
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends StandStats> extends StandType.AbstractBuilder<B, T> {
        private boolean manualControlEnabled = true;
        private boolean standLeapEnabled = true;
        
        public B disableManualControl() {
            this.manualControlEnabled = false;
            return getThis();
        }
        
        public B disableStandLeap() {
            this.standLeapEnabled = false;
            return getThis();
        }
        
        @Override
        public B rightClickHotbar(StandAction... rightClickHotbar) {
            if (rightClickHotbar.length > 0) {
                defaultMMB(rightClickHotbar[0]);
            }
            else {
                defaultMMB(null);
            }
            return super.rightClickHotbar(rightClickHotbar);
        }
    }
    
    public static class Builder<T extends StandStats> extends EntityStandType.AbstractBuilder<Builder<T>, T> {
        
        @Override
        protected Builder<T> getThis() {
            return this;
        }
        
        @Override
        public EntityStandType<T> build() {
            return new EntityStandType<>(this);
        }
        
    }
    
    
    
    @Override
    public void onCommonSetup() {
        super.onCommonSetup();
        finisherPunch = StreamSupport.stream(getAllUnlockableActions().spliterator(), false)
                .filter(attack -> attack instanceof StandEntityHeavyAttack && ((StandEntityHeavyAttack) attack).isFinisher())
                .findFirst();
    }
    
    public void setEntityType(Supplier<? extends StandEntityType<? extends StandEntity>> entityTypeSupplier) {
        if (this.entityTypeSupplier == null) {
            this.entityTypeSupplier = entityTypeSupplier;
        }
    }


    public StandEntityType<? extends StandEntity> getEntityType() {
        return entityTypeSupplier.get();
    }
    
    @Override
    public HitResult clientHitResult(IStandPower power, Entity cameraEntity, HitResult vanillaHitResult) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            if (JojoModUtil.isAnotherEntityTargeted(vanillaHitResult, stand)) {
                return super.clientHitResult(power, cameraEntity, vanillaHitResult);
            }
            
            HitResult miss = null;
            HitResult hitResult;
            if (vanillaHitResult.getType() == HitResult.Type.MISS) {
                miss = vanillaHitResult;
                hitResult = stand.precisionRayTrace(cameraEntity);
            }
            else {
                hitResult = vanillaHitResult;
            }
            
            if (hitResult.getType() == HitResult.Type.ENTITY && !JojoModUtil.isAnotherEntityTargeted(hitResult, stand)) {
                if (miss == null) {
                    Vec3 lookVec = cameraEntity.getLookAngle();
                    Vec3 eyePos = cameraEntity.getEyePosition(1);
                    miss = BlockHitResult.miss(eyePos, Direction.getNearest(lookVec.x, lookVec.y, lookVec.z), BlockPos.containing(eyePos));
                }
                return miss;
            }

            return hitResult;
        }
        return super.clientHitResult(power, cameraEntity, vanillaHitResult);
    }
    
    @Override
    public boolean usesStamina() {
        return true;
    }
    
    @Override
    public float getStaminaRegen(IStandPower power) {
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            return stand.getCurrentTaskActionOptional()
                    .map(action -> action.canStaminaRegen(power, stand))
                    .orElse(true) ? 1.5F : 0;
        }
        return super.getStaminaRegen(power);
    }

    @Override
    public boolean usesResolve() {
        return true;
    }
    
    @Override
    public void onNewResolveLevel(IStandPower power) {
        super.onNewResolveLevel(power);
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            stand.modifiersFromResolveLevel(power.getStatsDevelopment());
        }
    }
    
    @Override
    public Optional<StandAction> getStandFinisherPunch() {
        return finisherPunch;
    }
    
    @Override
    public void toggleSummon(IStandPower standPower) {
        if (!standPower.isActive()) {
            summon(standPower.getUser(), standPower, false);
        }
        else {
            StandEntity standEntity = (StandEntity) standPower.getStandManifestation();
            if (standEntity.isArmsOnlyMode()) {
                standEntity.fullSummonFromArms();
                triggerAdvancement(standPower, standPower.getStandManifestation());
            }
            else {
                unsummon(standPower.getUser(), standPower);
            }
        }
    }

    @Override
    public boolean summon(LivingEntity user, IStandPower standPower, boolean withoutNameVoiceLine) {
        return summon(user, standPower, entity -> {}, withoutNameVoiceLine, true);
    }

    public boolean summon(LivingEntity user, IStandPower standPower, Consumer<StandEntity> beforeTheSummon, boolean withoutNameVoiceLine, boolean addToWorld) {
        if (!super.summon(user, standPower, withoutNameVoiceLine)) {
            return false;
        }
        if (!user.level.isClientSide()) {
            StandEntity standEntity = getEntityType().create(user.level);
            standEntity.copyPosition(user);
            standPower.setStandManifestation(standEntity);
            beforeTheSummon.accept(standEntity);
            
            if (addToWorld) {
                finalizeStandSummonFromAction(user, standPower, standEntity, true);
            }
            
            standEntity.onStandSummonServerSide();
        }
        return true;
    }
    
    public void finalizeStandSummonFromAction(LivingEntity user, IStandPower standPower, StandEntity standEntity, boolean addToWorld) {
        if (!user.level.isClientSide() && !standEntity.isAddedToWorld()) {
            if (addToWorld) {
                user.level.addFreshEntity(standEntity);
                standEntity.playStandSummonSound();
                PacketManager.sendToClientsTrackingAndSelf(new TrSetStandEntityPacket(user.getId(), standEntity.getId()), user);
                triggerAdvancement(standPower, standPower.getStandManifestation());
            }
            else {
                forceUnsummon(user, standPower);
            }
        }
    }
    
    protected void triggerAdvancement(IStandPower standPower, IStandManifestation stand) {
        if (stand instanceof StandEntity && !((StandEntity) stand).isArmsOnlyMode()) {
            super.triggerAdvancement(standPower, stand);
        }
    }

    @Override
    public void unsummon(LivingEntity user, IStandPower standPower) {
        if (!user.level.isClientSide()) {
            StandEntity standEntity = ((StandEntity) standPower.getStandManifestation());
            if (standEntity != null) {
                if (!standEntity.isBeingRetracted()) {
                    standEntity.retractStand(true);
                }
                else if (standEntity.isManuallyControlled()) {
                    standEntity.stopRetraction();
                }
                else {
                    standEntity.retractStand(true);
                }
            }
        }
    }

    @Override
    public void forceUnsummon(LivingEntity user, IStandPower standPower) {
        if (!user.level.isClientSide()) {
            IStandManifestation stand = standPower.getStandManifestation();
            if (stand instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) stand;
                standPower.setStandManifestation(null);
                PacketManager.sendToClientsTrackingAndSelf(new TrSetStandEntityPacket(user.getId(), -1), user);
                standEntity.discard();
            }
        }
        else if (user.is(ClientUtil.getClientPlayer())) {
            StandUtil.setManualControl(ClientUtil.getClientPlayer(), false, false);
        }
    }
    
    @Override
    public boolean canBeManuallyControlled() {
        return manualControlEnabled;
    }
    
    @Override
    public boolean canLeap() {
        return standLeapEnabled;
    }

    @Override
    public void tickUser(LivingEntity user, IStandPower power) {
        super.tickUser(user, power);
        IStandManifestation stand = power.getStandManifestation();
        if (stand instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) stand;
            if (standEntity.level != user.level) {
                forceUnsummon(user, power);
            }
        }
        if (!user.level.isClientSide()) {
            power.getStandInstance().ifPresent(standInstance -> {
                if (!standInstance.hasPart(StandPart.ARMS)) {
                    user.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 319, 1));
                    user.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 319, 1));
                }
                if (!standInstance.hasPart(StandPart.LEGS)) {
                    user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 319, 1));
                }
            });
        }
    }
    
    @Override
    public void onStandSkinSet(IStandPower power, Optional<ResourceLocation> skin) {
        if (power.getStandManifestation() instanceof StandEntity) {
            ((StandEntity) power.getStandManifestation()).setStandSkin(skin);
        }
    }
    

    
    public static void giveEffectSharedWithStand(LivingEntity user, MobEffectInstance effectInstance) {
        IStandPower.getStandPowerOptional(user).ifPresent(power -> {
            if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
                StandEntity stand = (StandEntity) power.getStandManifestation();
                if (stand.isEffectSharedFromUser(effectInstance.getEffect())) {
                    stand.addEffect(new MobEffectInstance(effectInstance));
                }
            }
        });
    }
    
    public static void removeEffectSharedWithStand(LivingEntity user, MobEffect effect) {
        IStandPower.getStandPowerOptional(user).ifPresent(power -> {
            if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
                StandEntity stand = (StandEntity) power.getStandManifestation();
                if (stand.isEffectSharedFromUser(effect)) {
                    stand.removeEffect(effect);
                }
            }
        });
    }
}
