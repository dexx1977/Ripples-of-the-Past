package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class BloodParticlesPacket {
    private final Vec3 posSource;
    private final Optional<Vec3> posDest;
    private final float speed;
    private final int count;
    private final int entityId;

    public BloodParticlesPacket(Vec3 posSource, float speed, int count, int entityId) {
        this(posSource, Optional.empty(), speed, count, entityId);
    }

    public BloodParticlesPacket(Vec3 posSource, Vec3 posDest, float speed, int count, int entityId) {
        this(posSource, Optional.of(posDest), speed, count, entityId);
    }

    public BloodParticlesPacket(Vec3 posSource, Optional<Vec3> posDest, float speed, int count, int entityId) {
        this.posSource = posSource;
        this.posDest = posDest;
        this.speed = speed;
        this.count = count;
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<BloodParticlesPacket> {

        @Override
        public void encode(BloodParticlesPacket msg, FriendlyByteBuf buf) {
            buf.writeDouble(msg.posSource.x);
            buf.writeDouble(msg.posSource.y);
            buf.writeDouble(msg.posSource.z);
            NetworkUtil.writeOptional(buf, msg.posDest, (vec, buffer) -> {
                buffer.writeDouble(vec.x);
                buffer.writeDouble(vec.y);
                buffer.writeDouble(vec.z);
            });
            buf.writeFloat(msg.speed);
            buf.writeVarInt(msg.count);
            buf.writeInt(msg.entityId);
        }

        @Override
        public BloodParticlesPacket decode(FriendlyByteBuf buf) {
            return new BloodParticlesPacket(
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), 
                    NetworkUtil.readOptional(buf, vec -> new Vec3(
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble())
                            ),
                    buf.readFloat(), buf.readVarInt(), buf.readInt());
        }

        private static final Random RANDOM = new Random();
        @Override
        public void handle(BloodParticlesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Optional<Vec3> diff = msg.posDest.map(vec -> vec.subtract(msg.posSource).normalize().scale(msg.speed));
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            for (int i = 0; i < msg.count; i++) {
                Vec3 speedVec = diff.orElseGet(() -> {
                    float xRot = (RANDOM.nextFloat() - 0.5f) * (float) Math.PI;
                    float yRot = RANDOM.nextFloat() * (float) Math.PI * 2;
                    return MathUtil.vecFromAngles(xRot, yRot).scale(msg.speed);
                });
                CustomParticlesHelper.createBloodParticle(ModParticles.BLOOD.get(), entity, 
                        msg.posSource.x, msg.posSource.y, msg.posSource.z, 
                        speedVec.x, speedVec.y, speedVec.z);
            }
        }

        @Override
        public Class<BloodParticlesPacket> getPacketClass() {
            return BloodParticlesPacket.class;
        }
    }

}
