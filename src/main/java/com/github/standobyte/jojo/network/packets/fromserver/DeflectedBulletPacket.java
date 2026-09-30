package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class DeflectedBulletPacket {
    private final int entityId;
    private final Vec3 deflectVec;
    private final Vec3 deflectedPos;
    private final Vec3 bulletPos;
    
    public DeflectedBulletPacket(int entityId, Vec3 deflectVec, Vec3 deflectedPos, Vec3 bulletPos) {
        this.entityId = entityId;
        this.deflectVec = deflectVec;
        this.deflectedPos = deflectedPos;
        this.bulletPos = bulletPos;
    }
    
    
    
    public static class Handler implements IModPacketHandler<DeflectedBulletPacket> {

        @Override
        public void encode(DeflectedBulletPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
            buf.writeDouble(msg.deflectVec.x);
            buf.writeDouble(msg.deflectVec.y);
            buf.writeDouble(msg.deflectVec.z);
            buf.writeDouble(msg.deflectedPos.x);
            buf.writeDouble(msg.deflectedPos.y);
            buf.writeDouble(msg.deflectedPos.z);
            buf.writeDouble(msg.bulletPos.x);
            buf.writeDouble(msg.bulletPos.y);
            buf.writeDouble(msg.bulletPos.z);
        }

        @Override
        public DeflectedBulletPacket decode(FriendlyByteBuf buf) {
            return new DeflectedBulletPacket(buf.readInt(), 
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        @Override
        public void handle(DeflectedBulletPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof ModdedProjectileEntity) {
                entity.setPacketCoordinates(msg.bulletPos.x, msg.bulletPos.y, msg.bulletPos.z);
                entity.xo = msg.deflectVec.x;
                entity.yo = msg.deflectVec.y;
                entity.zo = msg.deflectVec.z;
                entity.xOld = msg.deflectVec.x;
                entity.yOld = msg.deflectVec.y;
                entity.zOld = msg.deflectVec.z;
                entity.setPos(msg.bulletPos.x, msg.bulletPos.y, msg.bulletPos.z);
                ((ModdedProjectileEntity) entity).setIsDeflected(msg.deflectVec, msg.deflectedPos);
            }
        }

        @Override
        public Class<DeflectedBulletPacket> getPacketClass() {
            return DeflectedBulletPacket.class;
        }
    }
}
