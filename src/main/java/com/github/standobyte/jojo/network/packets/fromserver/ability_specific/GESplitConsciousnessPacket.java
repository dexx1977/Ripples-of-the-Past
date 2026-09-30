package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ControllerConsciousness;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class GESplitConsciousnessPacket {
    private final Vec3 deltaMovement;
    
    public GESplitConsciousnessPacket(Vec3 deltaMovement) {
        this.deltaMovement = deltaMovement;
    }
    
    
    
    public static class Handler implements IModPacketHandler<GESplitConsciousnessPacket> {

        @Override
        public void encode(GESplitConsciousnessPacket msg, FriendlyByteBuf buf) {
            buf.writeDouble(msg.deltaMovement.x);
            buf.writeDouble(msg.deltaMovement.y);
            buf.writeDouble(msg.deltaMovement.z);
        }

        @Override
        public GESplitConsciousnessPacket decode(FriendlyByteBuf buf) {
            return new GESplitConsciousnessPacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        @Override
        public void handle(GESplitConsciousnessPacket msg, Supplier<Context> ctx) {
            if (ClientUtil.getClientPlayer().hasEffect(ModStatusEffects.SENSORY_OVERLOAD.get())) {
                ControllerConsciousness csns = ControllerConsciousness.getInstance();
                csns.spawnConsciousness();
                csns.getCsnsEntity().lerpMotion(msg.deltaMovement.x, msg.deltaMovement.y, msg.deltaMovement.z);
            }
        }

        @Override
        public Class<GESplitConsciousnessPacket> getPacketClass() {
            return GESplitConsciousnessPacket.class;
        }
    }
}
