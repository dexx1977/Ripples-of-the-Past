package com.github.standobyte.jojo.world.gen.structures;

import net.minecraft.util.RandomSource;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.world.gen.LoadMeFeature;
import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.Mirror;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class MrPresidentRoomFeature extends Feature<NoneFeatureConfiguration> implements LoadMeFeature {
    private final ResourceLocation roomPath = new ResourceLocation(JojoMod.MOD_ID, "mr_president_room");
    private StructureTemplate roomTemplate;

    public MrPresidentRoomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    
    @Override
    public void loadTemplate(StructureTemplateManager templateManager) {
        roomTemplate = templateManager.getOrCreate(roomPath);
    }
    
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        // 1.20.1 hands the feature its world, random and origin through the context
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos blockPos = context.origin();
        if (roomTemplate == null) {
            roomTemplate = context.level().getLevel().getStructureManager().getOrCreate(roomPath);
        }
        if (roomTemplate != null) {
            StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            BlockPos blockpos1 = roomTemplate.getZeroPositionWithTransform(blockPos.offset(0, 0, 0), Mirror.NONE, Rotation.NONE);
            return roomTemplate.placeInWorld(world, blockpos1, blockpos1, settings, random, 2);
        }
        
        return false;
    }

}
