package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromclient.ClSyncMotionAnimPacket;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncMotionAnimStatePacket {
    private final int playerId;
    
    private final boolean isMoving;
    private final double movementUp;
    private final double movementLeft;
    private final float speed;
    
    public SyncMotionAnimStatePacket(int playerId, ClSyncMotionAnimPacket sentData) {
        this.playerId = playerId;
        
        this.isMoving = sentData.isMoving;
        this.movementUp = sentData.movementUp;
        this.movementLeft = sentData.movementLeft;
        this.speed = sentData.speed;
    }
    
    public SyncMotionAnimStatePacket(int playerId, boolean isMoving, double movementUp, double movementLeft, float speed) {
        this.playerId = playerId;
        
        this.isMoving = isMoving;
        this.movementUp = movementUp;
        this.movementLeft = movementLeft;
        this.speed = speed;
    }
    
    
    
    public static class Handler implements IModPacketHandler<SyncMotionAnimStatePacket> {

        @Override
        public void encode(SyncMotionAnimStatePacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.playerId);
            
            buf.writeBoolean(msg.isMoving);
            buf.writeDouble(msg.movementUp);
            buf.writeDouble(msg.movementLeft);
            buf.writeFloat(msg.speed);
        }

        @Override
        public SyncMotionAnimStatePacket decode(FriendlyByteBuf buf) {
            int playerId = buf.readInt();
            return new SyncMotionAnimStatePacket(playerId, buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readFloat());
        }

        @Override
        public void handle(SyncMotionAnimStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.playerId);
            if (entity instanceof Player) {
                ModPlayerAnimations.wallClimbing.tickAnimProperties((Player) entity, msg.isMoving, 
                        msg.movementUp, msg.movementLeft, msg.speed);
            }
        }

        @Override
        public Class<SyncMotionAnimStatePacket> getPacketClass() {
            return SyncMotionAnimStatePacket.class;
        }
    }
}
