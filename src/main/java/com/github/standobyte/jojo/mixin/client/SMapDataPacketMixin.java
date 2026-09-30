package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mc.CustomTargetIconMap;

import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

@Mixin(ClientboundMapItemDataPacket.class)
public class SMapDataPacketMixin {
    @Shadow private MapDecoration[] decorations;

    @Inject(method = "applyToMap", at = @At("HEAD"))
    public void jojoReplaceMapDecorations(CallbackInfo ci) {
        CustomTargetIconMap.CustomIconMapDecoration.replaceWithCustomIcons(decorations);
    }
}
