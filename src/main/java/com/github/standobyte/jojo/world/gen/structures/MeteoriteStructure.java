package com.github.standobyte.jojo.world.gen.structures;

import com.github.standobyte.jojo.JojoModConfig;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
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
        BlockPos blockPos = new BlockPos(centerX, context.chunkGenerator()
                .getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()) - 1, centerZ);
        return Optional.of(new GenerationStub(blockPos, pieces -> 
                MeteoritePieces.start(context.structureTemplateManager(), blockPos, pieces, context.random())));
    }
}
