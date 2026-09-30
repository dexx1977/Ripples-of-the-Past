package com.github.standobyte.jojo.world.gen.structures;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class HamonTempleStructure extends Structure<NoneFeatureConfiguration> {

    public HamonTempleStructure(Codec<NoneFeatureConfiguration> codec) {
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
    
    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed, 
            WorldgenRandom chunkRandom, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos, NoneFeatureConfiguration featureConfig) {
        int x = (chunkX << 4) + 7;
        int z = (chunkZ << 4) + 7;
        return chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG) >= 90;
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
            for (int x = centerX - 24; x <= centerX + 24; x += 8) {
                for (int z = centerZ - 24; z <= centerZ + 24; z += 8) {
                    minY = Mth.clamp(chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG), 80, minY);
                }
            }
            BlockPos blockPos = new BlockPos(centerX, minY - 3, centerZ);
            HamonTemplePieces.start(templateManager, blockPos, pieces, random);
            calculateBoundingBox();
        }
    }
}
