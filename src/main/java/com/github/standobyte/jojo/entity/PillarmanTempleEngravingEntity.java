package com.github.standobyte.jojo.entity;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class PillarmanTempleEngravingEntity extends HangingEntity implements IEntityAdditionalSpawnData {
    private int textureId;

    public PillarmanTempleEngravingEntity(Level world, BlockPos pos, Direction facing, int textureId) {
        super(ModEntityTypes.PILLARMAN_TEMPLE_ENGRAVING.get(), world, pos);
        this.textureId = textureId;
        setDirection(facing);
    }

    public PillarmanTempleEngravingEntity(EntityType<? extends PillarmanTempleEngravingEntity> type, Level world) {
        super(type, world);
    }

    public int getTextureId() {
        return textureId;
    }
    
    @Override
    public boolean survives() {
        return true;
    }

    @Override
    protected void recalculateBoundingBox() {
        if (this.direction != null) {
            double xBlock = (double)pos.getX();
            double yBlock = (double)pos.getY() + 0.5D;
            double zBlock = (double)pos.getZ();

//            xBlock += (Math.abs(direction.getStepX() - 1)) * 0.5D;
//            zBlock += (Math.abs(direction.getStepZ() - 1)) * 0.5D;
            xBlock -= (Math.abs(direction.getStepX()) - 1) * 0.5D;
            zBlock -= (Math.abs(direction.getStepZ()) - 1) * 0.5D;
            
            setPosRaw(xBlock, yBlock, zBlock);

            double xSize = getWidth();
            double ySize = getHeight();
            double zSize = getWidth();
            if (direction.getAxis() == Direction.Axis.Z) {
                zSize = 0;
            } else {
                xSize = 0;
            }

            xSize /= 32.0D;
            ySize /= 32.0D;
            zSize /= 32.0D;
            this.setBoundingBox(new AABB(
                    xBlock - xSize, yBlock - ySize, zBlock - zSize, 
                    xBlock + xSize, yBlock + ySize, zBlock + zSize));
        }
    }

    @Override
    public int getWidth() {
        return 48;
    }

    @Override
    public int getHeight() {
        return 48;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {}

    @Override
    public void playPlacementSound() {}

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        this.setPos(x, y, z);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int posRotationIncrements, boolean teleport) {
        BlockPos blockPos = pos.offset(x - this.getX(), y - this.getY(), z - this.getZ());
        this.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putByte("Facing", (byte) direction.get2DDataValue());
        compound.putInt("TexId", textureId);
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.textureId = compound.getInt("TexId");
        this.direction = Direction.from2DDataValue(compound.getByte("Facing"));
        super.readAdditionalSaveData(compound);
        setDirection(direction);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(textureId);
        buffer.writeBlockPos(pos);
        buffer.writeEnum(direction);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.textureId = additionalData.readVarInt();
        this.pos = additionalData.readBlockPos();
        this.direction = additionalData.readEnum(Direction.class);
        setDirection(direction);
    }
}
