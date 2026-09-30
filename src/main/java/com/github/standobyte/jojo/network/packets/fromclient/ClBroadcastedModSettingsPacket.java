package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.player.PlayerClientBroadcastedSettings;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClBroadcastedModSettingsPacket {
    private PlayerClientBroadcastedSettings settings;
    private FriendlyByteBuf settingsData;
    
    public ClBroadcastedModSettingsPacket(PlayerClientBroadcastedSettings settings) {
        this.settings = settings;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClBroadcastedModSettingsPacket> {

        @Override
        public void encode(ClBroadcastedModSettingsPacket msg, FriendlyByteBuf buf) {
            msg.settings.toBuf(buf);
        }

        @Override
        public ClBroadcastedModSettingsPacket decode(FriendlyByteBuf buf) {
            ClBroadcastedModSettingsPacket packet = new ClBroadcastedModSettingsPacket(null);
            packet.settingsData = buf;
            return packet;
        }

        @Override
        public void handle(ClBroadcastedModSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            PlayerClientBroadcastedSettings.getPlayerSettings(player).ifPresent(serverSettings -> {
                serverSettings.fromBuf(msg.settingsData);
                serverSettings.syncToAll(player);
            });
        }

        @Override
        public Class<ClBroadcastedModSettingsPacket> getPacketClass() {
            return ClBroadcastedModSettingsPacket.class;
        }
    }
}
