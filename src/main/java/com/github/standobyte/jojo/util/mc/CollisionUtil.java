package com.github.standobyte.jojo.util.mc;

import java.util.Objects;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

public class CollisionUtil {

    public static Vec3 collide(Entity entity, Vec3 offsetVec) {
        return collide(entity, entity.getBoundingBox(), offsetVec, null);
    }
    
    public static Vec3 collide(Entity entity, AABB collisionBox, Vec3 offsetVec) {
        return collide(entity, collisionBox, offsetVec, null);
    }
    
    public static Vec3 collide(Entity entity, AABB collisionBox, Vec3 offsetVec, @Nullable CollisionContext selectionContext) {
        if (selectionContext == null) selectionContext = CollisionContext.of(entity);
        VoxelShape worldBorder = entity.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> worldBorderCollision = Shapes.joinIsNotEmpty(worldBorder, Shapes.create(collisionBox.deflate(1.0E-7D)), BooleanOp.AND) ? Stream.empty() : Stream.of(worldBorder);
        Stream<VoxelShape> entityCollisions = entity.level.getEntityCollisions(entity, collisionBox.expandTowards(offsetVec), e -> true);
        ReuseableStream<VoxelShape> collisions = new ReuseableStream<>(Stream.concat(entityCollisions, worldBorderCollision));
        Vec3 vector3d = offsetVec.lengthSqr() == 0 ? offsetVec : collideBoundingBoxHeuristically(entity, offsetVec, collisionBox, entity.level, selectionContext, collisions);
        boolean flag = offsetVec.x != vector3d.x;
        boolean flag2 = offsetVec.z != vector3d.z;
        boolean flag3 = entity.onGround() || offsetVec.y != vector3d.y && offsetVec.y < 0.0D;
        if (entity.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
            Vec3 vector3d1 = collideBoundingBoxHeuristically(entity, new Vec3(offsetVec.x, entity.maxUpStep, offsetVec.z), collisionBox, entity.level, selectionContext, collisions);
            Vec3 vector3d2 = collideBoundingBoxHeuristically(entity, new Vec3(0, entity.maxUpStep, 0), collisionBox.expandTowards(offsetVec.x, 0.0D, offsetVec.z), entity.level, selectionContext, collisions);
            if (vector3d2.y < entity.maxUpStep) {
                Vec3 vector3d3 = collideBoundingBoxHeuristically(entity, new Vec3(offsetVec.x, 0.0D, offsetVec.z), collisionBox.move(vector3d2), entity.level, selectionContext, collisions).add(vector3d2);
                if (Entity.getHorizontalDistanceSqr(vector3d3) > Entity.getHorizontalDistanceSqr(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }
            
            if (Entity.getHorizontalDistanceSqr(vector3d1) > Entity.getHorizontalDistanceSqr(vector3d)) {
                return vector3d1.add(collideBoundingBoxHeuristically(entity, new Vec3(0.0D, -vector3d1.y + offsetVec.y, 0.0D), collisionBox.move(vector3d1), entity.level, selectionContext, collisions));
            }
        }
        
        return vector3d;
    }

    public static Vec3 collideBoundingBoxHeuristically(@Nullable Entity pEntity, Vec3 pVec, AABB pCollisionBox, Level pLevel, CollisionContext pContext, ReuseableStream<VoxelShape> pPotentialHits) {
       boolean flag = pVec.x == 0.0D;
       boolean flag1 = pVec.y == 0.0D;
       boolean flag2 = pVec.z == 0.0D;
       if ((!flag || !flag1) && (!flag || !flag2) && (!flag1 || !flag2)) {
          ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(
                  pPotentialHits.getStream(), 
                  StreamSupport.stream(new CustomContextVoxelShapeSpliterator(pLevel, pEntity, pContext, pCollisionBox.expandTowards(pVec)), false)));
          return collideBoundingBoxLegacy(pVec, pCollisionBox, reuseablestream);
       } else {
          return collideBoundingBox(pVec, pCollisionBox, pLevel, pContext, pPotentialHits);
       }
    }



    public static Vec3 collideBoundingBoxLegacy(Vec3 pVec, AABB pCollisionBox, ReuseableStream<VoxelShape> pPotentialHits) {
       double d0 = pVec.x;
       double d1 = pVec.y;
       double d2 = pVec.z;
       if (d1 != 0.0D) {
          d1 = Shapes.collide(Direction.Axis.Y, pCollisionBox, pPotentialHits.getStream(), d1);
          if (d1 != 0.0D) {
             pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
          }
       }

       boolean flag = Math.abs(d0) < Math.abs(d2);
       if (flag && d2 != 0.0D) {
          d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);
          if (d2 != 0.0D) {
             pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
          }
       }

       if (d0 != 0.0D) {
          d0 = Shapes.collide(Direction.Axis.X, pCollisionBox, pPotentialHits.getStream(), d0);
          if (!flag && d0 != 0.0D) {
             pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
          }
       }

       if (!flag && d2 != 0.0D) {
          d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);
       }

       return new Vec3(d0, d1, d2);
    }
     
     public static class CustomContextVoxelShapeSpliterator extends AbstractSpliterator<VoxelShape> {
         @Nullable
         private final Entity source;
         private final AABB box;
         private final CollisionContext context;
         private final Cursor3D cursor;
         private final BlockPos.MutableBlockPos pos;
         private final VoxelShape entityShape;
         private final CollisionGetter collisionGetter;
         private boolean needsBorderCheck;
         private final BiPredicate<BlockState, BlockPos> predicate;

         public CustomContextVoxelShapeSpliterator(CollisionGetter pGetter, @Nullable Entity pEntity, CollisionContext pContext, AABB pCollisionBox) {
            this(pGetter, pEntity, pContext, pCollisionBox, (p_241459_0_, p_241459_1_) -> {
               return true;
            });
         }

         public CustomContextVoxelShapeSpliterator(CollisionGetter pCollisionGetter, @Nullable Entity pSource, CollisionContext pContext, AABB pBox, BiPredicate<BlockState, BlockPos> pPredicate) {
            super(Long.MAX_VALUE, 1280);
            this.context = pContext; // this line is the only difference from the vanilla VoxelShapeSpliterator, and we need the custom ISelectionContext for things like BarrierBlockWallClimbMixin#changeCollisionShape
//            this.context = pSource == null ? ISelectionContext.empty() : ISelectionContext.of(pSource);
            this.pos = new BlockPos.MutableBlockPos();
            this.entityShape = Shapes.create(pBox);
            this.collisionGetter = pCollisionGetter;
            this.needsBorderCheck = pSource != null;
            this.source = pSource;
            this.box = pBox;
            this.predicate = pPredicate;
            int i = Mth.floor(pBox.minX - 1.0E-7D) - 1;
            int j = Mth.floor(pBox.maxX + 1.0E-7D) + 1;
            int k = Mth.floor(pBox.minY - 1.0E-7D) - 1;
            int l = Mth.floor(pBox.maxY + 1.0E-7D) + 1;
            int i1 = Mth.floor(pBox.minZ - 1.0E-7D) - 1;
            int j1 = Mth.floor(pBox.maxZ + 1.0E-7D) + 1;
            this.cursor = new Cursor3D(i, k, i1, j, l, j1);
         }

         public boolean tryAdvance(Consumer<? super VoxelShape> p_tryAdvance_1_) {
            return this.needsBorderCheck && this.worldBorderCheck(p_tryAdvance_1_) || this.collisionCheck(p_tryAdvance_1_);
         }

         boolean collisionCheck(Consumer<? super VoxelShape> pConsumer) {
            while(true) {
               if (this.cursor.advance()) {
                  int i = this.cursor.nextX();
                  int j = this.cursor.nextY();
                  int k = this.cursor.nextZ();
                  int l = this.cursor.getNextType();
                  if (l == 3) {
                     continue;
                  }

                  BlockGetter iblockreader = this.getChunk(i, k);
                  if (iblockreader == null) {
                     continue;
                  }

                  this.pos.set(i, j, k);
                  BlockState blockstate = iblockreader.getBlockState(this.pos);
                  if (!this.predicate.test(blockstate, this.pos) || l == 1 && !blockstate.hasLargeCollisionShape() || l == 2 && !blockstate.is(Blocks.MOVING_PISTON)) {
                     continue;
                  }

                  VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
                  if (voxelshape == Shapes.block()) {
                     if (!this.box.intersects((double)i, (double)j, (double)k, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D)) {
                        continue;
                     }

                     pConsumer.accept(voxelshape.move((double)i, (double)j, (double)k));
                     return true;
                  }

                  VoxelShape voxelshape1 = voxelshape.move((double)i, (double)j, (double)k);
                  if (!Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND)) {
                     continue;
                  }

                  pConsumer.accept(voxelshape1);
                  return true;
               }

               return false;
            }
         }

         @Nullable
         private BlockGetter getChunk(int pX, int pZ) {
            int i = pX >> 4;
            int j = pZ >> 4;
            return this.collisionGetter.getChunkForCollisions(i, j);
         }

         boolean worldBorderCheck(Consumer<? super VoxelShape> pConsumer) {
            Objects.requireNonNull(this.source);
            this.needsBorderCheck = false;
            WorldBorder worldborder = this.collisionGetter.getWorldBorder();
            AABB axisalignedbb = this.source.getBoundingBox();
            if (!isBoxFullyWithinWorldBorder(worldborder, axisalignedbb)) {
               VoxelShape voxelshape = worldborder.getCollisionShape();
               if (!isOutsideBorder(voxelshape, axisalignedbb) && isCloseToBorder(voxelshape, axisalignedbb)) {
                  pConsumer.accept(voxelshape);
                  return true;
               }
            }

            return false;
         }

         private static boolean isCloseToBorder(VoxelShape pShape, AABB pCollisionBox) {
            return Shapes.joinIsNotEmpty(pShape, Shapes.create(pCollisionBox.inflate(1.0E-7D)), BooleanOp.AND);
         }

         private static boolean isOutsideBorder(VoxelShape pShape, AABB pCollisionBox) {
            return Shapes.joinIsNotEmpty(pShape, Shapes.create(pCollisionBox.deflate(1.0E-7D)), BooleanOp.AND);
         }

         public static boolean isBoxFullyWithinWorldBorder(WorldBorder pBorder, AABB pCollisionBox) {
            double d0 = (double)Mth.floor(pBorder.getMinX());
            double d1 = (double)Mth.floor(pBorder.getMinZ());
            double d2 = (double)Mth.ceil(pBorder.getMaxX());
            double d3 = (double)Mth.ceil(pBorder.getMaxZ());
            return pCollisionBox.minX > d0 && pCollisionBox.minX < d2 && pCollisionBox.minZ > d1 && pCollisionBox.minZ < d3 && pCollisionBox.maxX > d0 && pCollisionBox.maxX < d2 && pCollisionBox.maxZ > d1 && pCollisionBox.maxZ < d3;
         }
      }

     
     public static Vec3 collideBoundingBox(Vec3 pVec, AABB pCollisionBox, LevelReader pLevel, CollisionContext pSelectionContext, ReuseableStream<VoxelShape> pPotentialHits) {
        double d0 = pVec.x;
        double d1 = pVec.y;
        double d2 = pVec.z;
        if (d1 != 0.0D) {
           d1 = Shapes.collide(Direction.Axis.Y, pCollisionBox, pLevel, d1, pSelectionContext, pPotentialHits.getStream());
           if (d1 != 0.0D) {
              pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
           }
        }

        boolean flag = Math.abs(d0) < Math.abs(d2);
        if (flag && d2 != 0.0D) {
           d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());
           if (d2 != 0.0D) {
              pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
           }
        }

        if (d0 != 0.0D) {
           d0 = Shapes.collide(Direction.Axis.X, pCollisionBox, pLevel, d0, pSelectionContext, pPotentialHits.getStream());
           if (!flag && d0 != 0.0D) {
              pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
           }
        }

        if (!flag && d2 != 0.0D) {
           d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());
        }

        return new Vec3(d0, d1, d2);
     }
    
}
