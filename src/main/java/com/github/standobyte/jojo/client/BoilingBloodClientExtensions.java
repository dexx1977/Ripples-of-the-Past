package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/** The boiling blood textures, which lived in the old FluidAttributes. */
public class BoilingBloodClientExtensions implements IClientFluidTypeExtensions {
    private static final ResourceLocation STILL = new ResourceLocation(JojoMod.MOD_ID, "block/boiling_blood_still");
    private static final ResourceLocation FLOWING = new ResourceLocation(JojoMod.MOD_ID, "block/boiling_blood_flow");

    @Override
    public ResourceLocation getStillTexture() {
        return STILL;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return FLOWING;
    }

}
