package com.github.standobyte.jojo.client.render.world.shader;

import java.io.IOException;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceManager;

public class TimeStopShader extends PostPass {
    private final float effectLength;

    public TimeStopShader(ResourceManager resourceManager, String name, 
            RenderTarget inTarget, RenderTarget outTarget, float effectLength) throws IOException {
        super(resourceManager, name, inTarget, outTarget);
        this.effectLength = effectLength;
    }

    @Override
    public void process(float partialSecond) {
        ShaderEffectApplier.getInstance().addTsShaderUniforms(getEffect(), partialSecond, effectLength);
        super.process(partialSecond);
    }
}
