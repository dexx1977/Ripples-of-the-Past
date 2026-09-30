package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class TrPillarmanParticlesPacket {
    private final int entityId;
    private final int quantity;
     
    public static TrPillarmanParticlesPacket emitter(int entityId, int quantity, @Nullable ParticleOptions particleType) {
        return new TrPillarmanParticlesPacket(entityId, quantity, particleType, null, false, -1);
    }

    
    private TrPillarmanParticlesPacket(int entityId, int quantity, @Nullable ParticleOptions particleType,
            Vec3 pos, boolean followEntity, int count) {
        this.entityId = entityId;
        this.quantity = quantity;

    }
    
    public static class Handler implements IModPacketHandler<TrPillarmanParticlesPacket> {

        @Override
        public void encode(TrPillarmanParticlesPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
            buf.writeInt(msg.quantity);     
        }

        @Override
        public TrPillarmanParticlesPacket decode(FriendlyByteBuf buf) {
            int entityId = buf.readInt();
            return emitter(entityId, buf.readInt(), ModParticles.LIGHT_SPARK.get());

        }
        
        @Override
        public void handle(TrPillarmanParticlesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
            	for (int i = 0; i <= msg.quantity; i++) {
    	    		entity.level.addParticle(ModParticles.LIGHT_SPARK.get(), true, 
    	    				entity.getX(), entity instanceof LivingEntity ? entity.getY() + 1 : entity.getY(), entity.getZ(), 
    	    				(Math.random() - 0.5F) / 3, (Math.random() - 0.5F) / 3, (Math.random() - 0.5F) / 3);
    	        }
            }
        }

        @Override
        public Class<TrPillarmanParticlesPacket> getPacketClass() {
            return TrPillarmanParticlesPacket.class;
        }
    }
}
