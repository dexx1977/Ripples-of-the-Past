package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public class MultiLineOverlayMsgPacket {
    private List<Component> lines;
    private boolean animateColor;
    
    public MultiLineOverlayMsgPacket(List<Component> lines) {
        this(lines, false);
    }
    
    public MultiLineOverlayMsgPacket(List<Component> lines, boolean animateColor) {
        this.lines = lines;
        this.animateColor = animateColor;
    }
    
    
    
    public static class Handler implements IModPacketHandler<MultiLineOverlayMsgPacket> {

        @Override
        public void encode(MultiLineOverlayMsgPacket msg, FriendlyByteBuf buf) {
            NetworkUtil.writeCollection(buf, msg.lines, buf::writeComponent, false);
            buf.writeBoolean(msg.animateColor);
        }

        @Override
        public MultiLineOverlayMsgPacket decode(FriendlyByteBuf buf) {
            List<Component> lines = NetworkUtil.readCollection(buf, FriendlyByteBuf::readComponent);
            boolean animateColor = buf.readBoolean();
            return new MultiLineOverlayMsgPacket(lines, animateColor);
        }
        
        @Override
        public void handle(MultiLineOverlayMsgPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientEventHandler.getInstance().setMultiLineOverlayMessage(msg.lines, msg.animateColor);
        }

        @Override
        public Class<MultiLineOverlayMsgPacket> getPacketClass() {
            return MultiLineOverlayMsgPacket.class;
        }
    }
}
