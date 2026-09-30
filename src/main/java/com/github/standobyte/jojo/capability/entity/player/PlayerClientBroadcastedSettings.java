package com.github.standobyte.jojo.capability.entity.player;

import java.util.Optional;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClBroadcastedModSettingsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerModSettingsPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;

public class PlayerClientBroadcastedSettings {
    public HumanoidArm standSide = HumanoidArm.RIGHT;
    public boolean vampireGlowingEyes = true;
    
    
    public void toBuf(FriendlyByteBuf buf) {
        buf.writeEnum(standSide);
        buf.writeBoolean(vampireGlowingEyes);
    }
    
    public void fromBuf(FriendlyByteBuf buf) {
        standSide = buf.readEnum(HumanoidArm.class);
        vampireGlowingEyes = buf.readBoolean();
    }
    
    
    public void broadcastToServer() {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketManager.sendToServer(new ClBroadcastedModSettingsPacket(this));
        }
    }
    
    public void syncToAll(Player player) {
        PacketManager.sendToClientsTracking(new TrPlayerModSettingsPacket(player.getId(), this), player);
    }
    
    public void syncToTracking(Player player, ServerPlayer tracking) {
        PacketManager.sendToClient(new TrPlayerModSettingsPacket(player.getId(), this), tracking);
    }
    
    public static Optional<PlayerClientBroadcastedSettings> getPlayerSettings(Player player) {
        if (player.isLocalPlayer()) {
            return Optional.of(ClientModSettings.getSettingsReadOnly().broadcasted);
        }
        return player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getBroadcastedSettings);
    }
}
