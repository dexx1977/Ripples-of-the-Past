package com.github.standobyte.jojo.world.gen.structures;

import java.util.List;
import java.util.Random;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class HamonTemplePieces {
    private static StructurePieceType HAMON_TEMPLE_PIECES;

    private static final ResourceLocation PIECE_BUILDING = new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/building");
    private static final ResourceLocation PIECE_PATHWAY = new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/pathway");
    private static final ResourceLocation[] PIECE_ROCK = {
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_1"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_2"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_3"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_4"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_5"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_6"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_7"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_8")};

    public static void start(StructureTemplateManager templateManager, BlockPos blockPos, List<StructurePiece> pieces, Random random) {
        pieces.add(new Piece(templateManager, PIECE_BUILDING, blockPos.offset(new BlockPos(-24, -3, -24)), Rotation.NONE));
        for (Rotation rotation : Rotation.values()) {
            pieces.add(new Piece(templateManager, PIECE_PATHWAY, 
                    blockPos.offset(new BlockPos(-1, -4, -1)).offset(new BlockPos(-22, 0, -1).rotate(rotation)), rotation));
            Rotation randomRotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            randomRotation = Rotation.NONE;
            pieces.add(new Piece(templateManager, PIECE_ROCK[random.nextInt(PIECE_ROCK.length)], 
                    blockPos.offset(new BlockPos(-4, -3, -4).rotate(randomRotation)).offset(new BlockPos(-21, 0, 0).rotate(rotation)), randomRotation));
        }
    }

    public static void initPieceType() {
        HAMON_TEMPLE_PIECES = StructurePieceType.setPieceId(Piece::new, JojoMod.MOD_ID + ":HamonTemple");
    }

    private static class Piece extends TemplateStructurePiece {
        private final ResourceLocation piece;
        private final Rotation rotation;

        public Piece(StructureTemplateManager templateManager, ResourceLocation piece, BlockPos blockPos, Rotation rotation) {
            super(HAMON_TEMPLE_PIECES, 0);
            this.piece = piece;
            this.templatePosition = blockPos;
            this.rotation = rotation;
            this.setupPiece(templateManager);
        }

        public Piece(StructureTemplateManager templateManager, CompoundTag cnbt) {
           super(HAMON_TEMPLE_PIECES, cnbt);
           this.piece = new ResourceLocation(cnbt.getString("Template"));
           this.rotation = Rotation.valueOf(cnbt.getString("Rotation"));
           this.setupPiece(templateManager);
        }

        private void setupPiece(StructureTemplateManager templateManager) {
            StructureTemplate template = templateManager.getOrCreate(piece);
            StructurePlaceSettings placementsettings = new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE);
            setup(template, templatePosition, placementsettings);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag cnbt) {
            super.addAdditionalSaveData(cnbt);
            cnbt.putString("Template", piece.toString());
            cnbt.putString("Rotation", rotation.name());
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor world, Random rand, BoundingBox sbb) {}
    }
}
