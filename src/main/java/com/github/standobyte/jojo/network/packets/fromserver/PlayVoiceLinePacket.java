package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PlayVoiceLinePacket {
    private final SoundEvent sound;
    private final SoundSource source;
    private final int entityId;
    private final float volume;
    private final float pitch;
    private final boolean interrupt;

    public PlayVoiceLinePacket(SoundEvent sound, SoundSource source, int entityId, float volume, float pitch, boolean interrupt) {
        this.sound = sound;
        this.source = source;
        this.entityId = entityId;
        this.volume = volume;
        this.pitch = pitch;
        this.interrupt = interrupt;
    }
    
    public static PlayVoiceLinePacket notTriggered(int entityId) {
        return new PlayVoiceLinePacket(null, null, entityId, 0, 0, false);
    }
    
    
    
    public static class Handler implements IModPacketHandler<PlayVoiceLinePacket> {

        @Override
        public void encode(PlayVoiceLinePacket msg, FriendlyByteBuf buf) {
            boolean triggerVoiceLine = msg.sound != null;
            buf.writeBoolean(triggerVoiceLine);
            buf.writeInt(msg.entityId);
            if (triggerVoiceLine) {
                buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
                buf.writeEnum(msg.source);
                buf.writeFloat(msg.volume);
                buf.writeFloat(msg.pitch);
                buf.writeBoolean(msg.interrupt);
            }
        }

        @Override
        public PlayVoiceLinePacket decode(FriendlyByteBuf buf) {
            boolean triggerVoiceLine = buf.readBoolean();
            int entityId = buf.readInt();
            return triggerVoiceLine ? new PlayVoiceLinePacket(buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), 
                    buf.readEnum(SoundSource.class), entityId, buf.readFloat(), buf.readFloat(), buf.readBoolean())
                    : notTriggered(entityId);
        }

        @Override
        public void handle(PlayVoiceLinePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                ClientTickingSoundsHelper.playVoiceLine(entity, msg.sound, msg.source, msg.volume, msg.pitch, msg.interrupt);
            }
        }

        @Override
        public Class<PlayVoiceLinePacket> getPacketClass() {
            return PlayVoiceLinePacket.class;
        }
    }

}
