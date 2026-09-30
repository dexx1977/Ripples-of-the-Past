package com.github.standobyte.jojo.world.gen.structures;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class MeteoriteStructure extends Structure<NoneFeatureConfiguration> {

    public MeteoriteStructure(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public IStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return Start::new;
    }
    
    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.TOP_LAYER_MODIFICATION;
    }
    
    private static class Start extends StructureStart<NoneFeatureConfiguration> {
        public Start(Structure<NoneFeatureConfiguration> structure, int chunkPosX, int chunkPosZ, BoundingBox bounds, int references, long seed) {
            super(structure, chunkPosX, chunkPosZ, bounds, references, seed);
        }

        @Override
        public void generatePieces(RegistryAccess dynamicRegistryManager, ChunkGenerator chunkGenerator, 
                StructureTemplateManager templateManager, int chunkX, int chunkZ, Biome biome, NoneFeatureConfiguration config) {
            int x = (chunkX << 4) + 7;
            int z = (chunkZ << 4) + 7;
            BlockPos blockPos = new BlockPos(x, chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG) - 1, z);
            MeteoritePieces.start(templateManager, blockPos, pieces, random);
            calculateBoundingBox();
        }
    }
}