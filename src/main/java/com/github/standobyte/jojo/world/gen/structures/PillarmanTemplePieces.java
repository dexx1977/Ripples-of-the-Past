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

public class PillarmanTemplePieces {
    private static StructurePieceType PILLARMAN_TEMPLE_PIECES;
    private static final ResourceLocation PIECE_BUILDING = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/building");
    private static final ResourceLocation PIECE_STAIRWAY = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/stairway");
    private static final ResourceLocation PIECE_CORRIDOR = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/corridor");
    private static final ResourceLocation PIECE_BOSSROOM = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/bossroom");
    
    public static void start(StructureTemplateManager templateManager, BlockPos blockPos, List<StructurePiece> pieces, Random random) {
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        pieces.add(new Piece(templateManager, PIECE_BUILDING, blockPos, new BlockPos(-27, 0, -27), rotation));
        pieces.add(new Piece(templateManager, PIECE_STAIRWAY, blockPos, new BlockPos(19, -42, -2), rotation));
        pieces.add(new Piece(templateManager, PIECE_CORRIDOR, blockPos, new BlockPos(61, -43, -3), rotation));
        pieces.add(new Piece(templateManager, PIECE_BOSSROOM, blockPos, new BlockPos(102, -45, -16), rotation));
    }
    
    public static void initPieceType() {
        PILLARMAN_TEMPLE_PIECES = StructurePieceType.setPieceId(Piece::new, JojoMod.MOD_ID + ":PillarmanTemple");
    }

    private static class Piece extends TemplateStructurePiece {
        private final ResourceLocation piece;
        private final Rotation rotation;

        public Piece(StructureTemplateManager templateManager, ResourceLocation piece, BlockPos blockPos, BlockPos pieceOffset, Rotation rotation) {
            super(PILLARMAN_TEMPLE_PIECES, 0);
            this.piece = piece;
            this.templatePosition = blockPos.offset(pieceOffset.rotate(rotation));
            this.rotation = rotation;
            this.setupPiece(templateManager);
        }

        public Piece(StructureTemplateManager templateManager, CompoundTag cnbt) {
           super(PILLARMAN_TEMPLE_PIECES, cnbt);
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
