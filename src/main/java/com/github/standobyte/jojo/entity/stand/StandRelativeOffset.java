package com.github.standobyte.jojo.entity.stand;

import java.util.Optional;

import com.github.standobyte.jojo.capability.entity.player.PlayerClientBroadcastedSettings;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class StandRelativeOffset {
    private final double left;
    private final double forward;
    private final boolean doYOffset;
    public final double y;
    private final boolean useXRot;
    public boolean canInvertSide;
//    private final float yRotOffset;
    
    public static StandRelativeOffset noYOffset(double left, double forward) {
        return new StandRelativeOffset(left, 0, forward, false, false, true);
    }
    
    public static StandRelativeOffset withYOffset(double left, double y, double forward) {
        return new StandRelativeOffset(left, y, forward, true, false, true);
    }
    
    public static StandRelativeOffset withXRot(double left, double forward) {
        return new StandRelativeOffset(left, 0, forward, false, true, true);
    }
    
    public StandRelativeOffset copy() {
        return new StandRelativeOffset(this.left, this.y, this.forward, this.doYOffset, this.useXRot, this.canInvertSide);
    }
    
    public StandRelativeOffset copyScale(double leftScale, double yScale, double forwardScale) {
        return new StandRelativeOffset(this.left * leftScale, this.y * yScale, this.forward * forwardScale, this.doYOffset, this.useXRot, this.canInvertSide);
    }
    
    private StandRelativeOffset(double left, double y, double forward, boolean doYOffset, boolean useXRot, boolean canInvertSide) {
        this.left = left;
        this.forward = forward;
        this.doYOffset = doYOffset;
        this.y = y;
        this.useXRot = useXRot;
        this.canInvertSide = canInvertSide;
    }
    
    @Deprecated
    public Vec3 getAbsoluteVec(float yRot, float xRot, StandEntity standEntity, LivingEntity user, double yDefault) {
        return getAbsoluteVec(yRot, xRot, standEntity, user, yDefault, Optional.empty());
    }
    
    public Vec3 getAbsoluteVec(float yRot, float xRot, StandEntity standEntity, LivingEntity user, double yDefault, 
            Optional<PlayerClientBroadcastedSettings> userSettings) {
        double yOffset = 0;
        if (standEntity.isArmsOnlyMode() && user.getPose() != Pose.STANDING) {
            yOffset = (user.getDimensions(user.getPose()).height - user.getDimensions(Pose.STANDING).height) * 0.85F;
        }
        Vec3 vec;
        double left = this.left;
        
        boolean invertSide = canInvertSide && userSettings.map(settings -> settings.standSide == HumanoidArm.LEFT).orElse(false);
        if (invertSide) {
            left = -left;
        }
        
        if (useXRot) {
            vec = new Vec3(left, 0, forward).xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        }
        else {
            vec = new Vec3(left, (doYOffset ? y : yDefault) + yOffset, forward).yRot(-yRot * MathUtil.DEG_TO_RAD);
        }
        
        float standWidth = user.getBbWidth();
        float defaultWidth = user.getType().getWidth();
        float standHeight = user.getBbHeight();
        float defaultHeight = user.getType().getHeight();
        if (standWidth != defaultWidth || standHeight != defaultHeight) {
            double widthRatio = standWidth / defaultWidth;
            double heightRatio = standHeight / defaultHeight;
            vec = new Vec3(vec.x * widthRatio, vec.y * heightRatio, vec.z * widthRatio);
        }
        return vec;
    }

    @Deprecated
    public Vec3 toRelativeVec() {
        return new Vec3(left, y, forward);
    }

    @Deprecated
    public StandRelativeOffset withRelativeVec(Vec3 vec) {
        return new StandRelativeOffset(vec.x, vec.y, vec.z, this.doYOffset, this.useXRot, this.canInvertSide);
    }
    
    @Deprecated
    public double getLeft() {
        return left;
    }

    @Deprecated
    public double getForward() {
        return forward;
    }
    
    
    public StandRelativeOffset applyYOffset(double y) {
        if (this.doYOffset) {
            return this;
        }
        return new StandRelativeOffset(this.left, y, this.forward, true, this.useXRot, this.canInvertSide);
    }
    
    public StandRelativeOffset makeSnapshot(double yDefault, float xRot) {
        if (doYOffset && !useXRot) {
            return this;
        }
        
        double x = left;
        double y = doYOffset ? this.y : yDefault;
        double z = forward;
        if (useXRot) {
            Vec3 vec = new Vec3(x, 0, z).xRot(-xRot * MathUtil.DEG_TO_RAD);
            x = vec.x;
            y = vec.y;
            z = vec.z;
        }
        return new StandRelativeOffset(x, y, z, true, false, this.canInvertSide);
    }
    
    public StandRelativeOffset lerp(StandRelativeOffset prev, double lerp, double yDefault, float xRot) {
        if (prev.canInvertSide != this.canInvertSide) {
            return this;
        }
        StandRelativeOffset offset0 = prev.makeSnapshot(yDefault, xRot);
        StandRelativeOffset offset1 = this.makeSnapshot(yDefault, xRot);
        
        return new StandRelativeOffset(
                Mth.lerp(lerp, offset0.left,    offset1.left),
                Mth.lerp(lerp, offset0.y,       offset1.y),
                Mth.lerp(lerp, offset0.forward, offset1.forward),
                true, false, canInvertSide);
    }
    

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeDouble(left);
        buf.writeDouble(y);
        buf.writeDouble(forward);
        buf.writeBoolean(doYOffset);
        buf.writeBoolean(useXRot);
        buf.writeBoolean(canInvertSide);
    }
    
    public static StandRelativeOffset readFromBuf(FriendlyByteBuf buf) {
        StandRelativeOffset offset = new StandRelativeOffset(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
        return offset;
    }
}
