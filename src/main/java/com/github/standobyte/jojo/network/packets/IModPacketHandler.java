package com.github.standobyte.jojo.network.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface IModPacketHandler<MSG> {
    void encode(MSG msg, FriendlyByteBuf buf);
    
    MSG decode(FriendlyByteBuf buf);
    
    default void enqueueHandleSetHandled(MSG msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handle(msg, ctx);
        });
        ctx.get().setPacketHandled(true);
    }
    void handle(MSG msg, Supplier<NetworkEvent.Context> ctx);
    
    Class<MSG> getPacketClass();
}
