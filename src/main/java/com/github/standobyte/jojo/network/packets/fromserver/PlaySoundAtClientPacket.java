package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaySoundAtClientPacket {
    private final SoundEvent sound;
    private final SoundSource source;
    private final BlockPos soundPos;
    private final float volume;
    private final float pitch;

    public PlaySoundAtClientPacket(SoundEvent sound, SoundSource source, BlockPos soundPos, float volume, float pitch) {
        Validate.notNull(sound, "sound");
        this.sound = sound;
        this.source = source;
        this.soundPos = soundPos;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    
    
    public static class Handler implements IModPacketHandler<PlaySoundAtClientPacket> {

        @Override
        public void encode(PlaySoundAtClientPacket msg, FriendlyByteBuf buf) {
            buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
            buf.writeEnum(msg.source);
            buf.writeBlockPos(msg.soundPos);
            buf.writeFloat(msg.volume);
            buf.writeFloat(msg.pitch);
        }

        @Override
        public PlaySoundAtClientPacket decode(FriendlyByteBuf buf) {
            return new PlaySoundAtClientPacket(buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), 
                    buf.readEnum(SoundSource.class), buf.readBlockPos(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void handle(PlaySoundAtClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientUtil.playSoundAtClient(msg.sound, msg.source, msg.soundPos, msg.volume, msg.pitch);
        }

        @Override
        public Class<PlaySoundAtClientPacket> getPacketClass() {
            return PlaySoundAtClientPacket.class;
        }
    }

}
