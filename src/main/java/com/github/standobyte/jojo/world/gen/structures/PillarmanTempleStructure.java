package com.github.standobyte.jojo.world.gen.structures;

import com.github.standobyte.jojo.JojoModConfig;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class PillarmanTempleStructure extends Structure {
    public static final Codec<PillarmanTempleStructure> CODEC = simpleCodec(PillarmanTempleStructure::new);

    public PillarmanTempleStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return com.github.standobyte.jojo.init.ModStructures.PILLARMAN_TEMPLE_TYPE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // the old biome predicate also carried the config flag that switches this
        // structure off; the biome part is in the structure json now
        if (!JojoModConfig.getCommonConfigInstance(false).pillarManTempleSpawn.get()) {
            return Optional.empty();
        }
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        // Extra hard biome check: on 1.20.1 the structure json biome tag is not
        // always honored for mod structures, which made the temple spawn in
        // non-jungle biomes (taiga etc.) while the advancement text still calls it
        // a "jungle temple".
        Holder<Biome> biome = context.chunkGenerator().getBiomeSource()
                .getNoiseBiome(centerX >> 2, 0, centerZ >> 2, context.randomState().sampler());
        if (!biome.is(BiomeTags.IS_JUNGLE)) {
            return Optional.empty();
        }
        int minY = Integer.MAX_VALUE;
        for (int x = centerX - 26; x < centerX + 30; x += 8) {
            for (int z = centerZ - 26; z < centerZ + 30; z += 8) {
                minY = Math.min(minY, context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState()));
            }
        }
        BlockPos blockPos = new BlockPos(centerX, minY - 3, centerZ);
        return Optional.of(new GenerationStub(blockPos, pieces -> 
                PillarmanTemplePieces.start(context.structureTemplateManager(), blockPos, pieces, context.random())));
    }
}
