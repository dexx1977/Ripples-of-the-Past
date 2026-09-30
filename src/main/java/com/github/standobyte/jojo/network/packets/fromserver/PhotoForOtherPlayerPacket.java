package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraftforge.network.NetworkEvent;

public class PhotoForOtherPlayerPacket {
    private final int giveToPlayerId;
    
    public PhotoForOtherPlayerPacket(int giveToPlayerId) {
        this.giveToPlayerId = giveToPlayerId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<PhotoForOtherPlayerPacket> {

        @Override
        public void encode(PhotoForOtherPlayerPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.giveToPlayerId);
        }

        @Override
        public PhotoForOtherPlayerPacket decode(FriendlyByteBuf buf) {
            int giveToPlayerId = buf.readInt();
            return new PhotoForOtherPlayerPacket(giveToPlayerId);
        }

        @Override
        public void handle(PhotoForOtherPlayerPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Player player = ClientUtil.getClientPlayer();
            float randomAngle = player.getRandom().nextFloat() * (float) (2 * Math.PI);
            Vec3 playerPos = player.getEyePosition(1.0F);
            Vec3 cameraPos = playerPos.add(new Vec3(0, 0, 2).yRot(randomAngle));
            PolaroidHelper.takePicture(cameraPos, rot -> new Vector3f(0, 180 - randomAngle * MathUtil.RAD_TO_DEG, 0), true, msg.giveToPlayerId);
        }

        @Override
        public Class<PhotoForOtherPlayerPacket> getPacketClass() {
            return PhotoForOtherPlayerPacket.class;
        }
    }
}
