package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class SpawnParticlePacket {
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float maxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final ParticleOptions particle;
    @Nullable private final SpecialContext context;
    
    public <T extends ParticleOptions> SpawnParticlePacket(T particle, boolean force, 
            double x, double y, double z, float xDist, float yDist, float zDist, float maxSpeed, int count, 
            @Nullable SpecialContext context) {
        this.particle = particle;
        this.overrideLimiter = force;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xDist = xDist;
        this.yDist = yDist;
        this.zDist = zDist;
        this.maxSpeed = maxSpeed;
        this.count = count;
        this.context = context;
    }
    
    public static enum SpecialContext {
        AFK
    }
    
    
    
    public static class Handler implements IModPacketHandler<SpawnParticlePacket> {
        private final Random random = new Random();
        
        @Override
        public void encode(SpawnParticlePacket msg, FriendlyByteBuf buf) {
            buf.writeInt(Registry.PARTICLE_TYPE.getId(msg.particle.getType()));
            buf.writeBoolean(msg.overrideLimiter);
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
            buf.writeFloat(msg.xDist);
            buf.writeFloat(msg.yDist);
            buf.writeFloat(msg.zDist);
            buf.writeFloat(msg.maxSpeed);
            buf.writeInt(msg.count);
            msg.particle.writeToNetwork(buf);
            NetworkUtil.writeOptionally(buf, msg.context, buf::writeEnum);
        }
        
        @Override
        public SpawnParticlePacket decode(FriendlyByteBuf buf) {
            ParticleType<?> particleType = Registry.PARTICLE_TYPE.byId(buf.readInt());
            if (particleType == null) {
                particleType = ParticleTypes.BARRIER;
            }
            
            boolean overrideLimiter = buf.readBoolean();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float xDist = buf.readFloat();
            float yDist = buf.readFloat();
            float zDist = buf.readFloat();
            float maxSpeed = buf.readFloat();
            int count = buf.readInt();
            ParticleOptions particle = readParticle(buf, particleType);
            SpecialContext context = NetworkUtil.readOptional(buf, () -> buf.readEnum(SpecialContext.class)).orElse(null);
            
            return new SpawnParticlePacket(particle, overrideLimiter, x, y, z, xDist, yDist, zDist, maxSpeed, count, context);
        }
        
        private <T extends ParticleOptions> T readParticle(FriendlyByteBuf buf, ParticleType<T> particleType) {
            return particleType.getDeserializer().fromNetwork(particleType, buf);
        }
        
        @Override
        public void handle(SpawnParticlePacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.context == SpecialContext.AFK && !ClientModSettings.getSettingsReadOnly().menacingParticles) {
                return;
            }
            
            Level world = ClientUtil.getClientWorld();
            if (msg.count == 0) {
                addParticle(world, msg, 
                        msg.x, 
                        msg.y, 
                        msg.z, 
                        msg.maxSpeed * msg.xDist, 
                        msg.maxSpeed * msg.yDist, 
                        msg.maxSpeed * msg.zDist);
            } else {
                for (int i = 0; i < msg.count; ++i) {
                    if (!addParticle(world, msg, 
                            msg.x + random.nextGaussian() * msg.xDist, 
                            msg.y + random.nextGaussian() * msg.yDist, 
                            msg.z + random.nextGaussian() * msg.zDist, 
                            random.nextGaussian() * msg.maxSpeed, 
                            random.nextGaussian() * msg.maxSpeed, 
                            random.nextGaussian() * msg.maxSpeed)) {
                        break;
                    }
                }
            }
        }
        
        private boolean addParticle(Level world, SpawnParticlePacket msg, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            try {
                world.addParticle(msg.particle, msg.overrideLimiter, x, y, z, xSpeed, ySpeed, zSpeed);
                return true;
            } catch (Throwable throwable) {
                JojoMod.getLogger().warn("Could not spawn particle effect {}", msg.particle);
                return false;
            }
        }
        
        @Override
        public Class<SpawnParticlePacket> getPacketClass() {
            return SpawnParticlePacket.class;
        }
    }
}
