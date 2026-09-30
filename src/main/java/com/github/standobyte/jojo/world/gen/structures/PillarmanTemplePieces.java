package com.github.standobyte.jojo.world.gen.structures;

import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.util.RandomSource;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PillarmanTemplePieces {
    private static final ResourceLocation PIECE_BUILDING = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/building");
    private static final ResourceLocation PIECE_STAIRWAY = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/stairway");
    private static final ResourceLocation PIECE_CORRIDOR = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/corridor");
    private static final ResourceLocation PIECE_BOSSROOM = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/bossroom");
    
    public static void start(StructureTemplateManager templateManager, BlockPos blockPos, StructurePiecesBuilder pieces, RandomSource random) {
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        pieces.addPiece(new Piece(templateManager, PIECE_BUILDING, blockPos, new BlockPos(-27, 0, -27), rotation));
        pieces.addPiece(new Piece(templateManager, PIECE_STAIRWAY, blockPos, new BlockPos(19, -42, -2), rotation));
        pieces.addPiece(new Piece(templateManager, PIECE_CORRIDOR, blockPos, new BlockPos(61, -43, -3), rotation));
        pieces.addPiece(new Piece(templateManager, PIECE_BOSSROOM, blockPos, new BlockPos(102, -45, -16), rotation));
    }
    
    public static final StructurePieceType PIECE_TYPE = (context, tag) -> new Piece(context.structureTemplateManager(), tag);

    private static class Piece extends TemplateStructurePiece {
        private final ResourceLocation piece;
        private final Rotation rotation;

        public Piece(StructureTemplateManager templateManager, ResourceLocation piece, BlockPos blockPos, BlockPos pieceOffset, Rotation rotation) {
            super(PIECE_TYPE, 0, templateManager, piece, piece.getPath(), 
                    new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE), blockPos.offset(pieceOffset.rotate(rotation)));
            this.piece = piece;
            this.rotation = rotation;
        }

        public Piece(StructureTemplateManager templateManager, CompoundTag cnbt) {
           super(PIECE_TYPE, cnbt, templateManager, 
                   template -> new StructurePlaceSettings()
                       .setRotation(Rotation.valueOf(cnbt.getString("Rotation"))).setMirror(Mirror.NONE));
           this.piece = new ResourceLocation(cnbt.getString("Template"));
           this.rotation = Rotation.valueOf(cnbt.getString("Rotation"));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag cnbt) {
            super.addAdditionalSaveData(context, cnbt);
            cnbt.putString("Template", piece.toString());
            cnbt.putString("Rotation", rotation.name());
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor world, RandomSource rand, BoundingBox sbb) {}
    }
}
