package com.github.standobyte.jojo.client.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

public class CustomTextResource extends SimplePreparableReloadListener<String> {
    private String text;
    private final ResourceLocation location;

    public CustomTextResource(ResourceLocation location) {
        this.location = location;
    }

    @Override
    protected String prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        // 1.16.5's getResource threw when the file was absent; 1.20.1 returns an empty Optional
        Optional<Resource> optionalResource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (optionalResource.isEmpty()) {
            return "";
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(optionalResource.get().open(), StandardCharsets.UTF_8));
                ) {
            return reader.lines().reduce("", (l1, l2) -> l1 + l2 + "\n");
        } catch (IOException ioexception) {
            return "";
        }
    }

    @Override
    protected void apply(String text, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.text = text;
    }
    
    @Nullable
    public String getText() {
        return text;
    }
}
