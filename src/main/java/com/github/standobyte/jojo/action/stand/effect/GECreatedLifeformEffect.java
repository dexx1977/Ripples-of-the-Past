package com.github.standobyte.jojo.action.stand.effect;

import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.GETransformationEntity.FollowTargetMode;
import com.github.standobyte.jojo.entity.GETransformationEntity.GETransformationData;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class GECreatedLifeformEffect extends StandEffectInstance {
    private GETransformationData source = new GETransformationData();
    private ItemStack originalAsItem = ItemStack.EMPTY;
    private MutableComponent originalName = Component.empty();
    
    public GECreatedLifeformEffect() {
        this(ModStandEffects.GE_CREATED_LIFEFORM.get());
    }
    
    public GECreatedLifeformEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    public GETransformationData getSource() {
        return source;
    }
    
    public void setSource(GETransformationData source) {
        this.source = source;
        followTarget.setOwnerUUID(source.getFollowTarget());
    }
    
    public ItemStack getItemView() {
        return originalAsItem;
    }
    
    public MutableComponent getName() {
        return originalName;
    }
    
    @Override
    protected void start() {}
    
    @Override
    protected void updateTarget(Level world) {
        if (!world.isClientSide()) {
            Entity target = getTarget();
            if (target != null && !target.isAlive()) {
                if (target instanceof GETransformationEntity) {
                    GETransformationEntity tfEntity = (GETransformationEntity) target;
                    Entity tfTarget = tfEntity.getTransformationTarget();
                    if (tfTarget != null) {
                        setTargetEntity(tfTarget);
                    }
                    else {
                        clearTarget();
                        return;
                    }
                }
                else if (target instanceof LivingEntity && ((LivingEntity) target).isDeadOrDying()) {
                    remove();
                    return;
                }
            }
        }
        
        super.updateTarget(world);
    }
    
    @Override
    public void setTargetEntity(Entity target) {
        super.setTargetEntity(target);
        if (target != null) {
            if (target instanceof Mob && user != null) {
                Mob lifeformMob = (Mob) target;
                MCUtil.makeMobNeutralTo(lifeformMob, user);
            }
            
            List<MobEffectInstance> effects = getSource().getItemEffects();
            if (!effects.isEmpty()) {
                target.getCapability(LivingUtilCapProvider.CAPABILITY)
                .ifPresent(entity -> entity.setProductEffects(effects));
            }
        }
    }
    
    @Override
    protected void tick() {
        Entity entity = getTarget();
        Entity lifeform = entity;
        if (lifeform == null && entity instanceof GETransformationEntity) {
            lifeform = ((GETransformationEntity) entity).getTransformationTarget();
        }
        
        float staminaCost = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().getStaminaCostTicking(userPower, lifeform);
        if (!userPower.consumeStamina(staminaCost, true)) {
            if (!world.isClientSide()) {
                remove();
                return;
            }
        }
        
        if (!world.isClientSide()) {
            if (entity != null) {
                double maxDist = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().maxLifeformDistance;
                if (entity.distanceToSqr(user) > maxDist * maxDist) {
                    remove();
                    return;
                }
                
                UUID followTargetId = source.getFollowTarget();
                if (followTargetId != null) {
                    if (source.getFollowTargetMode() == FollowTargetMode.DELIVERY) {
                        Entity deliveryDest = ((ServerLevel) entity.level).getEntity(followTargetId);
                        if (deliveryDest != null && deliveryDest.distanceToSqr(entity) < 4) {
                            remove();
                            return;
                        }
                    }

                    if (entity instanceof Mob) {
                        mobAI((Mob) entity);
                    }
                }
            }
        }
    }
    
    private EntityOwnerResolver followTarget = new EntityOwnerResolver();
    // doing this separately from vanilla AI code lets us not occupy Goal.Flag.MOVE and Goal.Flag.LOOK flags used for attack goals
    private void mobAI(Mob entityAsMob) {
        if (!entityAsMob.isAggressive()) {
            Entity followTarget = this.followTarget.getEntity(world);
            if (followTarget != null) {
                entityAsMob.getNavigation().moveTo(followTarget, 1);
            }
        }
    }

    @Override
    protected void stop() {
        if (!world.isClientSide()) {
            Entity target = getTarget();
            if (target != null) {
                GETransformationEntity.turnEntityBack(target, source, user);
            }
        }
    }
    
    @Override
    protected boolean needsTarget() {
        return true;
    }
    
    @Override
    public void onTick() {
        if (!world.isClientSide()) {
            source.resolveNbtRead(world);
        }
        super.onTick();
    }

    @Override
    protected void writeAdditionalSaveData(CompoundTag nbt) {
        source.writeNbt(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        source.readNbt(nbt);
    }

    @Override
    public void writeAdditionalPacketData(FriendlyByteBuf buf, boolean sendingToUser) {
        if (sendingToUser) {
            source.resolveNbtRead(world);
            source.toBuf(buf);
        }
    }

    @Override
    public void readAdditionalPacketData(FriendlyByteBuf buf, boolean clientIsUser) {
        if (clientIsUser) {
            source.fromBuf(buf, world);
            originalAsItem = source.clMakeSourceItemView();
            originalName = source.clMakeSourceName();
        }
    }
}
