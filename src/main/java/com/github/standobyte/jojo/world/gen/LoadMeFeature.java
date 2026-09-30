package com.github.standobyte.jojo.world.gen;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public interface LoadMeFeature {
    default void loadTemplate(MinecraftServer server) {
        loadTemplate(server.getStructureManager());
    }
    void loadTemplate(StructureTemplateManager templateManager);
}
