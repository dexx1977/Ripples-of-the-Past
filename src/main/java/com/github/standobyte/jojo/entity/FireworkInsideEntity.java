package com.github.standobyte.jojo.entity;

import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkHooks;

public class FireworkInsideEntity extends FireworkRocketEntity {
    private Entity entityInsideOf;
    private UUID entityInsideOfUUID;

    public FireworkInsideEntity(EntityType<? extends FireworkRocketEntity> type, Level world) {
        super(type, world);
    }

    public FireworkInsideEntity(Level world, ItemStack item, LivingEntity entity) {
        super(ModEntityTypes.FIREWORK_INSIDE.get(), world);
        this.setPos(entity.getX(), entity.getY(0.5), entity.getZ());
        entity.startRiding(this, true);
        this.entityInsideOf = entity;
        this.entityInsideOfUUID = entity.getUUID();
        
        int flight = 1;
        if (!item.isEmpty() && item.hasTag()) {
           entityData.set(CommonReflection.getFireworkItemParameter(), item.copy());
           flight += item.getOrCreateTagElement("Fireworks").getByte("Flight");
        }

        setDeltaMovement(random.nextGaussian() * 0.001D, 0.05D, random.nextGaussian() * 0.001D);
        int lifetime = flight * 10;
        CommonReflection.setLifetime(this, lifetime);
    }

    @Override
    public void positionRider(Entity rider) {
        if (this.hasPassenger(rider)) {
            rider.setPos(getX(), getY() + (getBbHeight() - rider.getBbHeight()) / 2, getZ());
        }
    }
    
    @Override
    public double getPassengersRidingOffset() {
        return getBbHeight() / 2;
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            if (entityInsideOf == null) {
                entityInsideOf = ((ServerLevel) level).getEntity(entityInsideOfUUID);
            }
            // FIXME dismount prevention
            if (entityInsideOf != null && !this.is(entityInsideOf.getVehicle())) {
                entityInsideOf.startRiding(this, true);
            }
        }
        super.tick();
    }
    
    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return !isVehicle() && super.shouldRender(cameraX, cameraY, cameraZ);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }
    
    @Override
    protected Component getTypeName() {
        return EntityType.FIREWORK_ROCKET.getDescription();
    }
    
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
