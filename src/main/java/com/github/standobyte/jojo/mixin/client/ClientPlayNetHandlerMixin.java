package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetHandlerMixin {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void jojoCancelVanillaClPacket(Packet<?> packet, CallbackInfo ci) {
        if (NetworkUtil.blockPacketsToServer && !(
                packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundMovePlayerPacket
                || packet instanceof ServerboundChatPacket && ((ServerboundChatPacket) packet).message().startsWith("/"))) {
            ci.cancel();
        }
    }
    
}
