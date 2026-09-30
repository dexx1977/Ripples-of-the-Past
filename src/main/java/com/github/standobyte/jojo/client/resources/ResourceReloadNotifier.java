package com.github.standobyte.jojo.client.resources;

import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.sound.barrage.StandCrySoundHandler;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;

public class ResourceReloadNotifier extends SimplePreparableReloadListener<Void> {

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void __, ResourceManager resourceManager, ProfilerFiller profiler) {
        ResourcePathChecker.onResourcesReload();
        StandCrySoundHandler.clearCache();
    }
}
