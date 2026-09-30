package com.github.standobyte.jojo.util.mc;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class EntityOwnerResolver {
    protected Entity owner;
    protected LivingEntity ownerLiving;
    protected UUID ownerUUID;
    protected int ownerNetworkId;
    
    public void setOwner(@Nullable Entity owner) {
        this.ownerUUID = owner != null ? owner.getUUID() : null;
        this.ownerNetworkId = owner != null ? owner.getId() : 0;
        _setNewOwnerEntity(owner);
    }
    
    public void setOwnerUUID(UUID ownerUuid) {
        this.ownerUUID = ownerUuid;
    }
    
    public Entity getEntity(Level world) {
        updateEntity(world);
        return owner;
    }
    
    public LivingEntity getEntityLiving(Level world) {
        updateEntity(world);
        return ownerLiving;
    }
    
    protected void updateEntity(Level world) {
        if (owner != null && owner.isRemoved()) {
            _setNewOwnerEntity(null);
        }
        if (owner == null) {
            if (ownerUUID != null && world instanceof ServerLevel) {
                _setNewOwnerEntity(((ServerLevel) world).getEntity(ownerUUID));
            } else if (ownerNetworkId != 0) {
                _setNewOwnerEntity(world.getEntity(ownerNetworkId));
            }
        }
    }
    
    public boolean hasEntityId() {
        return ownerNetworkId > 0;
    }
    
    protected void _setNewOwnerEntity(Entity entity) {
        this.owner = entity;
        this.ownerLiving = entity instanceof LivingEntity ? (LivingEntity) entity : null;
        this.ownerNetworkId = owner != null ? owner.getId() : 0;
    }
    
    
    
    public void saveNbt(CompoundTag nbt, String key) {
        if (ownerUUID != null) {
            nbt.putUUID(key, ownerUUID);
        }
    }
    
    public void loadNbt(CompoundTag nbt, String key) {
        setOwnerUUID(nbt.hasUUID(key) ? nbt.getUUID(key) : null);
    }
    
    public void writeNetwork(FriendlyByteBuf buf) {
        buf.writeInt(ownerNetworkId);
    }
    
    public void readNetwork(FriendlyByteBuf buf) {
        ownerNetworkId = buf.readInt();
    }
    
    public int getNetworkId() {
        return ownerNetworkId;
    }
    
    
    public static class Generic<T extends Entity> extends EntityOwnerResolver {
        protected final Class<T> entityClass;
        protected T castEntity;
        
        public Generic(Class<T> entityClass) {
            this.entityClass = entityClass;
        }
        
        public T getEntityCast(Level world) {
            updateEntity(world);
            return castEntity;
        }
        
        @Override
        protected void _setNewOwnerEntity(Entity entity) {
            super._setNewOwnerEntity(entity);
            this.castEntity = entity != null && entityClass.isAssignableFrom(entity.getClass()) ? (T) entity : null;
        }
    }
}
