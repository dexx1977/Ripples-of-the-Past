package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClHasInputPacket {
    private final boolean hasInput;
    private final boolean wallClimbing;
    
    public ClHasInputPacket(boolean hasInput) {
        this(hasInput, false);
    }
    
    public static ClHasInputPacket wallClimbing(boolean hasMotion) {
        return new ClHasInputPacket(hasMotion, true);
    }
    
    private ClHasInputPacket(boolean hasInput, boolean wallClimbing) {
        this.hasInput = hasInput;
        this.wallClimbing = wallClimbing;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHasInputPacket> {
    
        @Override
        public void encode(ClHasInputPacket msg, FriendlyByteBuf buf) {
            buf.writeBoolean(msg.hasInput);
            buf.writeBoolean(msg.wallClimbing);
        }

        @Override
        public ClHasInputPacket decode(FriendlyByteBuf buf) {
            return new ClHasInputPacket(buf.readBoolean(), buf.readBoolean());
        }

        @Override
        public void handle(ClHasInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (msg.wallClimbing) {
                LivingWallClimbing.getHandler(player).ifPresent(cap -> cap.wallClimbIsMoving = msg.hasInput);
            }
            else {
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setHasClientInput(msg.hasInput));
            }
        }

        @Override
        public Class<ClHasInputPacket> getPacketClass() {
            return ClHasInputPacket.class;
        }
    }
}
