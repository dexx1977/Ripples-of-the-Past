package com.github.standobyte.jojo.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mc.CustomTargetIconMap;
import com.github.standobyte.jojo.util.mc.CustomTargetIconMap.IMapDataMixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

@Mixin(MapItemSavedData.class)
public abstract class MapDataMixin implements IMapDataMixin {
    @Shadow @Final public int centerX;
    @Shadow @Final public int centerZ;
    @Shadow public ResourceKey<Level> dimension;
    @Shadow public byte scale;
    @Shadow @Final public Map<String, MapDecoration> decorations;
    
    @Inject(method = "tickCarriedBy", at = @At("TAIL"))
    public void jojoAddMapTargetDecoration(Player player, ItemStack mapStack, CallbackInfo ci) {
        CustomTargetIconMap.mixinMakeIconDecoration(player, mapStack, this);
    }

    @Override
    public Map<String, MapDecoration> decorations() {
        return decorations;
    }

    @Override
    public int x() {
        return centerX;
    }

    @Override
    public int z() {
        return centerZ;
    }

    @Override
    public byte scale() {
        return scale;
    }

    @Override
    public ResourceKey<Level> dimension() {
        return dimension;
    }
}
