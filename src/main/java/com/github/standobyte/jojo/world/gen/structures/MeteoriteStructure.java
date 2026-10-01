package com.github.standobyte.jojo.world.gen.structures;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.JojoMod;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class MeteoriteStructure extends Structure {
    public static final Codec<MeteoriteStructure> CODEC = simpleCodec(MeteoriteStructure::new);

    public MeteoriteStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return com.github.standobyte.jojo.init.ModStructures.METEORITE_TYPE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // the old biome predicate also carried the config flag that switches this
        // structure off; the biome part is in the structure json now
        if (!JojoModConfig.getCommonConfigInstance(false).meteoriteSpawn.get()) {
            return Optional.empty();
        }
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        // Extra hard biome check: on 1.20.1 the structure json biome tag is not
        // always honored for mod structures, which let meteorites spawn in the
        // plains and trigger the advancement for every new world.
        Holder<Biome> biome = context.chunkGenerator().getBiomeSource()
                .getNoiseBiome(centerX >> 2, 0, centerZ >> 2, context.randomState().sampler());
        if (!biome.is(TagKey.create(Registries.BIOME, new ResourceLocation(JojoMod.MOD_ID, "meteorite_biomes")))) {
            return Optional.empty();
        }
        BlockPos blockPos = new BlockPos(centerX, context.chunkGenerator()
                .getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()) - 1, centerZ);
        return Optional.of(new GenerationStub(blockPos, pieces -> 
                MeteoritePieces.start(context.structureTemplateManager(), blockPos, pieces, context.random())));
    }
}
