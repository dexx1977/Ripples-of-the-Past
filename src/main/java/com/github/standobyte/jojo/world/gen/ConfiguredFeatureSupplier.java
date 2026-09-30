package com.github.standobyte.jojo.world.gen;

import java.util.function.Supplier;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ConfiguredFeatureSupplier<FC extends FeatureConfiguration, F extends Feature<FC>> implements Supplier<ConfiguredFeature<FC, ? extends Feature<FC>>> {
    private final Supplier<F> structure;
    private final FC config;
    private ConfiguredFeature<FC, ? extends Feature<FC>> configured = null;
    
    public ConfiguredFeatureSupplier(Supplier<F> structure, FC config) {
        this.structure = structure;
        this.config = config;
    }
    
    public ConfiguredFeature<FC, ? extends Feature<FC>> get() {
        if (configured == null) {
            configured = new ConfiguredFeature<>(structure.get(), config);
        }
        return configured;
    }

}
