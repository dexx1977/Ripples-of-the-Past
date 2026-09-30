package com.github.standobyte.jojo.client.render.world.shader;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

public class CustomShaderGroup extends PostChain {

    public CustomShaderGroup(TextureManager textureManager, ResourceManager resourceManager, 
            RenderTarget screenTarget, ResourceLocation name) throws IOException, JsonSyntaxException {
        super(textureManager, resourceManager, screenTarget, name);
    }

    @Override
    public PostPass addPass(String name, RenderTarget inTarget, RenderTarget outTarget) throws IOException {
        PostPass shader = getCustomParametersShader(Minecraft.getInstance().getResourceManager(), name, inTarget, outTarget);
        if (shader == null) {
            return super.addPass(name, inTarget, outTarget);
        }
        List<PostPass> passes = ClientReflection.getShaderGroupPasses(this);
        passes.add(passes.size(), shader);
        return shader;
    }
    
    @Nullable
    protected PostPass getCustomParametersShader(ResourceManager resourceManager, String name, 
            RenderTarget inTarget, RenderTarget outTarget) throws IOException {
        if ("jojo:time_stop".equals(name)) {
            return new TimeStopShader(resourceManager, name, inTarget, outTarget, 35F);
        }
        return null;
    }
}
