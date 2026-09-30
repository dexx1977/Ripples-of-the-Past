package com.github.standobyte.jojo.world.dimension;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

public class ModDimensions {
    public static ResourceKey<Level> MR_PRESIDENT;
    
    public static void init() {
        MR_PRESIDENT = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(JojoMod.MOD_ID, "mr_president"));
    }
}
