package com.github.standobyte.jojo.world.gen.structures;

import com.github.standobyte.jojo.JojoModConfig;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * The Hamon temple structure.
 *
 * <p>1.20.1 structures are data driven: the biome set, the generation step and the
 * terrain adaptation come from the structure json, and the placement (spacing,
 * separation and salt) from the structure set json - see
 * {@code data/jojo/worldgen} - while the class only decides where in a chunk the
 * temple starts. The old {@code isFeatureChunk} height check and the piece layout
 * are unchanged.</p>
 */
public class HamonTempleStructure extends Structure {
    public static final Codec<HamonTempleStructure> CODEC = simpleCodec(HamonTempleStructure::new);

    public HamonTempleStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return com.github.standobyte.jojo.init.ModStructures.HAMON_TEMPLE_TYPE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // the old biome predicate also carried the config flag that switches this
        // structure off; the biome part is in the structure json now
        if (!JojoModConfig.getCommonConfigInstance(false).hamonTempleSpawn.get()) {
            return Optional.empty();
        }
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        // Extra hard biome check: on 1.20.1 the structure json biome tag is not
        // always honored for mod structures (the old json used a non-existent
        // #minecraft:is_hill tag), which let the temple spawn on flat plains and
        // trigger the advancement for every new world.
        Holder<Biome> biome = context.chunkGenerator().getBiomeSource()
                .getNoiseBiome(centerX >> 2, 0, centerZ >> 2, context.randomState().sampler());
        if (!biome.is(BiomeTags.IS_HILL)) {
            return Optional.empty();
        }
        if (context.chunkGenerator().getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState()) < 90) {
            return Optional.empty();
        }

        int minY = Integer.MAX_VALUE;
        for (int x = centerX - 24; x <= centerX + 24; x += 8) {
            for (int z = centerZ - 24; z <= centerZ + 24; z += 8) {
                minY = Mth.clamp(context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState()), 80, minY);
            }
        }
        BlockPos blockPos = new BlockPos(centerX, minY - 3, centerZ);
        return Optional.of(new GenerationStub(blockPos, pieces -> 
                HamonTemplePieces.start(context.structureTemplateManager(), blockPos, pieces, context.random())));
    }
}
