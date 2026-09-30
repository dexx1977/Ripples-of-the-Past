package com.github.standobyte.jojo.entity;

import java.util.UUID;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ControllerSoul;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRemovePlayerSoulEntityPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSoulRotationPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class SoulEntity extends Entity implements IEntityAdditionalSpawnData {
    private LivingEntity originEntity;
    private UUID originUuid;
    public int lifeSpan;
    private boolean resolveCanLvlUp;
    private Entity noResolveEntity;
    private UUID noResolveEntityUUID;
    
    public SoulEntity(Level world, LivingEntity originEntity, int lifeSpan, boolean resolveCanLvlUp) {
        this(ModEntityTypes.SOUL.get(), world);
        setOriginEntity(originEntity);
        this.lifeSpan = lifeSpan;
        this.resolveCanLvlUp = resolveCanLvlUp;
    }

    public SoulEntity(EntityType<?> type, Level world) {
        super(type, world);
        noPhysics = true;
    }
    
    private void setOriginEntity(LivingEntity entity) {
        this.originEntity = entity;
        if (entity != null) {
            copyPosition(entity);
            entity.setRemainingFireTicks(-20);
        }
    }
    
    public void setNoResolveToEntity(LivingEntity entity) {
        this.noResolveEntity = entity;
    }
    
    public LivingEntity getOriginEntity() {
        return originEntity;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (originEntity == null
                || originEntity.isRemoved()
                || tickCount > 1 && !originEntity.isDeadOrDying()
                || tickCount > lifeSpan) {
            discard();
            return;
        }
        if (level.isClientSide()) {
            if (tickCount % 10 == 5) {
                level.addParticle(ParticleTypes.POOF, 
                        originEntity.getRandomX(1.0D), originEntity.getY(random.nextDouble() * 0.25D), originEntity.getRandomZ(1.0D), 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D);
            }
        }
        else {
            if (noResolveEntity == null && noResolveEntityUUID != null) {
                noResolveEntity = ((ServerLevel) level).getEntity(noResolveEntityUUID);
                noResolveEntityUUID = null;
            }
            
            level.getEntitiesOfClass(LivingEntity.class, 
                    new AABB(getBoundingBox().getCenter(), getBoundingBox().getCenter()).inflate(24), 
                    entity -> !entity.is(originEntity)
                            && originEntity.isAlliedTo(entity) && !(entity instanceof StandEntity))
            .forEach(entity -> {
                IStandPower.getStandPowerOptional(entity).ifPresent(stand -> {
                    if (giveResolve(entity, stand)) {
                        stand.getResolveCounter().soulAddResolveTeammate();
                    }
                });
            });
            HitResult rayTrace = JojoModUtil.rayTrace(this, 32, 
                    entity -> entity instanceof LivingEntity
                            && !StandUtil.getStandUser((LivingEntity) entity).is(originEntity)
                    , 1.0);
            if (rayTrace.getType() == HitResult.Type.ENTITY) {
                Entity lookEntity = ((EntityHitResult) rayTrace).getEntity();
                if (lookEntity instanceof LivingEntity) {
                    LivingEntity entity = StandUtil.getStandUser((LivingEntity) lookEntity);
                    IStandPower.getStandPowerOptional(entity)
                    .ifPresent(stand -> {
                        if (giveResolve(entity, stand)) {
                            stand.getResolveCounter().soulAddResolveLook();
                        }
                    });
                }
            }
        }
        if (this.isClientPlayer()) {
            setRot(originEntity.yRot, originEntity.xRot);
            setYHeadRot(originEntity.yRot);
        }
        
        tickRotation();
        originEntity.deathTime = Math.min(originEntity.deathTime, 18);
        move(MoverType.SELF, getDeltaMovement());
    }
    
    private boolean giveResolve(LivingEntity target, IStandPower targetStandPower) {
        return target != noResolveEntity && (resolveCanLvlUp || targetStandPower.getResolveLevel() >= targetStandPower.getMaxResolveLevel());
    }
    
    

    @Override
    public boolean isControlledByLocalInstance() {
        return super.isControlledByLocalInstance() || 
                level.isClientSide() && originEntity instanceof Player && ((Player) originEntity).isLocalPlayer();
    }

    private void addCloudParticles() {
        if (!isInvisibleTo(ClientUtil.getClientPlayer())) {
            for (int i = 0; i < 20; ++i) {
                level.addParticle(ModParticles.SOUL_CLOUD.get(), 
                        getRandomX(1.0D), getRandomY(), getRandomZ(1.0D), 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D);
            }
        }
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (level.isClientSide()) {
            addCloudParticles();
        }
    }
    
    public void skipAscension() {
        if (level.isClientSide()) {
            PacketManager.sendToServer(new ClRemovePlayerSoulEntityPacket(getId()));
        }
        tickCount = lifeSpan - 1;
    }
    
    private static final Vec3 UPWARDS_MOVEMENT = new Vec3(0, 0.04D, 0);
    @Override
    public Vec3 getDeltaMovement() {
        return UPWARDS_MOVEMENT;
    }

    @Override
    protected void defineSynchedData() {}
    
    @Override
    public boolean isInvisible() {
        return true;
    }

    public boolean invisibleFlag() {
        return super.isInvisible();
    }
    
    @Override
    public boolean isInvisibleTo(Player player) {
        return !player.is(originEntity) && (!StandUtil.clStandEntityVisibleTo(player) 
                || !JojoModUtil.seesInvisibleAsSpectator(player) && invisibleFlag());
    }
    
    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (originEntity != null) {
            return originEntity.getDimensions(pose);
        }
        return super.getDimensions(pose);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.tickCount = nbt.getInt("Age");
        this.lifeSpan = nbt.getInt("LifeSpan");
        this.resolveCanLvlUp = nbt.getBoolean("Resolve");
        if (nbt.hasUUID("Origin")) {
            this.originUuid = nbt.getUUID("Origin");
        }
        if (nbt.hasUUID("NoResolve")) {
            this.noResolveEntityUUID = nbt.getUUID("NoResolve");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("LifeSpan", lifeSpan);
        nbt.putBoolean("Resolve", resolveCanLvlUp);
        if (originUuid != null) {
            nbt.putUUID("Origin", originEntity.getUUID());
        }
        if (noResolveEntity != null) {
            nbt.putUUID("NoResolve", noResolveEntity.getUUID());
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (originUuid != null) {
            Entity entity = ((ServerLevel) level).getEntity(originUuid);
            if (entity instanceof LivingEntity) {
                setOriginEntity((LivingEntity) entity);
            }
            else {
                discard();
            }
        }
        buffer.writeInt(originEntity == null ? -1 : originEntity.getId());
        buffer.writeInt(lifeSpan);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            setOriginEntity((LivingEntity) entity);
            ControllerSoul.getInstance().onSoulSpawn(this);
            entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(playerData -> playerData.soulEntity = this);
            addCloudParticles();
        }
        else {
            discard();
            return;
        }
        lifeSpan = additionalData.readInt();
    }
    
    // rotation stuff copied from LivingEntity

    public float yBodyRot;
    public float yBodyRotO;
    public float yHeadRot;
    public float yHeadRotO;
    protected float animStep;
    protected float animStepO;
    protected int lerpSteps;
    protected double lerpYRot;
    protected double lerpXRot;
    protected double lyHeadRot;
    protected int lerpHeadSteps;
    
    private void tickRotation() {
        this.aiStep();
        double d0 = this.getX() - this.xo;
        double d1 = this.getZ() - this.zo;
        float horizontalDistSqr = (float)(d0 * d0 + d1 * d1);
        float f1 = this.yBodyRot;
        float f2 = 0.0F;
        if (horizontalDistSqr > 0.0025F) {
            f2 = (float)Math.sqrt((double)horizontalDistSqr) * 3.0F;
            float f4 = (float)Mth.atan2(d1, d0) * (180F / (float)Math.PI) - 90.0F;
            float f5 = Mth.abs(Mth.wrapDegrees(this.yRot) - f4);
            if (95.0F < f5 && f5 < 265.0F) {
                f1 = f4 - 180.0F;
            } else {
                f1 = f4;
            }
        }

        f2 = this.tickHeadTurn(f1, f2);

        while(this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO < -180.0F) {
            this.yBodyRotO -= 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
            this.yBodyRotO += 360.0F;
        }

        while(this.xRot - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while(this.xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO < -180.0F) {
            this.yHeadRotO -= 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO >= 180.0F) {
            this.yHeadRotO += 360.0F;
        }
        this.animStep += f2;
        
        if (isClientPlayer()) {
            this.sendPosition();
        }
    }
    
    @Override
    public void baseTick() {
        super.baseTick();
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    protected float tickHeadTurn(float p_110146_1_, float p_110146_2_) {
        float f = Mth.wrapDegrees(p_110146_1_ - this.yBodyRot);
        this.yBodyRot += f * 0.3F;
        float f1 = Mth.wrapDegrees(this.yRot - this.yBodyRot);
        boolean flag = f1 < -90.0F || f1 >= 90.0F;
        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        this.yBodyRot = this.yRot - f1;
        if (f1 * f1 > 2500.0F) {
            this.yBodyRot += f1 * 0.2F;
        }

        if (flag) {
            p_110146_2_ *= -1.0F;
        }

        return p_110146_2_;
    }

    public void aiStep() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            this.yRot = (float)((double)this.yRot + Mth.wrapDegrees(this.lerpYRot - (double)this.yRot) / (double)this.lerpSteps);
            this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
            --this.lerpSteps;
            this.setRot(this.yRot, this.xRot);
        }

        if (this.lerpHeadSteps > 0) {
            this.yHeadRot = (float)((double)this.yHeadRot + Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
            --this.lerpHeadSteps;
        }

        if (this.isEffectiveAi()) {
            this.serverAiStep();
        }
    }

    public boolean isEffectiveAi() {
        return !this.level.isClientSide || isClientPlayer();
    }
    
    private boolean isClientPlayer() {
        return this.level.isClientSide && originEntity.is(ClientUtil.getClientPlayer());
    }

    protected void serverAiStep() {
        this.yHeadRot = this.yRot;
    }

    public void lerpTo(double p_180426_1_, double p_180426_3_, double p_180426_5_, float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
        this.lerpYRot = (double)p_180426_7_;
        this.lerpXRot = (double)p_180426_8_;
        this.lerpSteps = p_180426_9_;
    }

    public void lerpHeadTo(float p_208000_1_, int p_208000_2_) {
        this.lyHeadRot = (double)p_208000_1_;
        this.lerpHeadSteps = p_208000_2_;
    }

    @Override
    public float getViewYRot(float p_195046_1_) {
        return p_195046_1_ == 1.0F ? this.yHeadRot : Mth.lerp(p_195046_1_, this.yHeadRotO, this.yHeadRot);
    }

    public float getYHeadRot() {
        return this.yHeadRot;
    }

    public void setYHeadRot(float p_70034_1_) {
        this.yHeadRot = p_70034_1_;
    }

    public void setYBodyRot(float p_181013_1_) {
        this.yBodyRot = p_181013_1_;
    }

    public void lookAt(EntityAnchorArgument.Anchor p_200602_1_, Vec3 p_200602_2_) {
        super.lookAt(p_200602_1_, p_200602_2_);
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRot = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
    }

    private float yRotLast;
    private float xRotLast;

    private void sendPosition() {
        if (isClientPlayer()) {
            float d2 = this.yRot - this.yRotLast;
            float d3 = this.xRot - this.xRotLast;
            boolean flag2 = d2 != 0.0F || d3 != 0.0F;
            PacketManager.sendToServer(new ClSoulRotationPacket(getId(), this.yRot, this.xRot));

            if (flag2) {
                this.yRotLast = this.yRot;
                this.xRotLast = this.xRot;
            }
        }
    }
    
    public void handleRotationPacket(float msgYRot, float msgXRot) {
        this.yRot = msgYRot % 360.0F;
        this.xRot = Mth.clamp(msgXRot, -90.0F, 90.0F) % 360.0F;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }
}
