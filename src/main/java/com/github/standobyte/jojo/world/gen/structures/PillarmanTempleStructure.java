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

public class PillarmanTempleStructure extends Structure<NoneFeatureConfiguration> {
    // 1.16.5's Item exposed a shared Random; 1.20.1 items carry their own.
    protected static final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();


    public PillarmanTempleStructure(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public IStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return Start::new;
    }
    
    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }
    
    private static class Start extends StructureStart<NoneFeatureConfiguration> {
        public Start(Structure<NoneFeatureConfiguration> structure, int chunkPosX, int chunkPosZ, BoundingBox bounds, int references, long seed) {
            super(structure, chunkPosX, chunkPosZ, bounds, references, seed);
        }

        @Override
        public void generatePieces(RegistryAccess dynamicRegistryManager, ChunkGenerator chunkGenerator, 
                StructureTemplateManager templateManager, int chunkX, int chunkZ, Biome biome, NoneFeatureConfiguration config) {
            int centerX = (chunkX << 4) + 7;
            int centerZ = (chunkZ << 4) + 7;
            int minY = Integer.MAX_VALUE;
            for (int x = centerX - 26; x < centerX + 30; x += 8) {
                for (int z = centerZ - 26; z < centerZ + 30; z += 8) {
                    minY = Math.min(minY, chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG));
                }
            }
            BlockPos blockPos = new BlockPos(centerX, minY - 3, centerZ);
            PillarmanTemplePieces.start(templateManager, blockPos, pieces, random);
            calculateBoundingBox();
        }
    }
}
