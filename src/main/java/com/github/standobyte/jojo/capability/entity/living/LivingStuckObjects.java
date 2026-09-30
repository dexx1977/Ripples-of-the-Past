package com.github.standobyte.jojo.capability.entity.living;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class LivingStuckObjects implements INBTSerializable<CompoundTag> {
    private final LivingEntity entity;
    private final StuckObjectsTracker knives;
    
    public LivingStuckObjects(LivingEntity entity) {
        this.entity = entity;
        this.knives = new StuckObjectsTracker(entity, Type.KNIFE);
    }
    
    public StuckObjectsTracker getKnives() {
        return knives;
    }
    
    public void tick() {
        if (!entity.level.isClientSide()) {
            knives.tick();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        knives.toNBT(nbt, "Knives");
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        knives.fromNBT(nbt, "Knives");
    }
    
    public void onTracking(ServerPlayer tracking) {
        knives.onTracking(tracking);
    }
    
    public void syncWithClient() {
        if (entity instanceof ServerPlayer) {
            knives.syncWithPlayerEntity((ServerPlayer) entity);
        }
    }
    
    
    
    public static enum Type {
        KNIFE
    }
    
    public class StuckObjectsTracker {
        private final Type type;
        private final LivingEntity entity;
        private int count;
        private int removeTime;
        
        public StuckObjectsTracker(LivingEntity entity, Type type) {
            this.entity = entity;
            this.type = type;
        }
        
        public int getCount() {
            return count;
        }
        
        public void tick() {
            if (count > 0) {
                if (removeTime <= 0) {
                    removeTime = 20 * (30 - count);
                }
                removeTime--;
                if (removeTime <= 0) {
                    setCount(count - 1);
                }
            }
        }
        
        public void increment() {
            setCount(count + 1);
        }
        
        public void setCount(int count) {
            count = Math.max(count, 0);
            if (this.count != count) {
                this.count = count;
                if (!entity.level.isClientSide()) {
                    PacketManager.sendToClientsTrackingAndSelf(makePacket(), entity);
                }
            }
        }
        
        
        public void toNBT(CompoundTag mainNbt, String key) {
            mainNbt.putInt(key, count);
        }
        
        public void fromNBT(CompoundTag mainNbt, String key) {
            count = mainNbt.getInt(key);
        }
        
        
        public void onTracking(ServerPlayer tracking) {
            PacketManager.sendToClient(makePacket(), tracking);
        }
        
        public void syncWithPlayerEntity(ServerPlayer entityAsPlayer) {
            PacketManager.sendToClient(makePacket(), entityAsPlayer);
        }
        
        public Object makePacket() {
            switch (type) {
            case KNIFE:
                return new TrKnivesCountPacket(entity.getId(), count);
            default:
                throw new AssertionError();
            }
        }
    }
}
