package com.github.standobyte.jojo.util.general;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class PlaneRectangle {
    private static final Random RANDOM = new Random();
    public final Vec3 pLD;
    public final Vec3 pLU;
    public final Vec3 pRU;
    public final Vec3 pRD;
    public final Vec3 center;
    public final float width;
    public final float height;
    public final Vec3 normalVec;
    
    public static PlaneRectangle create(Vec3 center, float xRot, float yRot, float width, float height) {
        Vec3 offset = new Vec3(width / 2, height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        Vec3 offset2 = new Vec3(width / 2, -height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        
        Vec3 pointLeftDown = center.add(offset);
        Vec3 pointLeftUp = center.add(offset2);
        Vec3 pointRightUp = center.add(offset.reverse());
        Vec3 pointRightDown = pointRightUp.add(pointLeftDown.subtract(pointLeftUp));
        
        return new PlaneRectangle(pointLeftDown, pointLeftUp, pointRightUp, pointRightDown, center, width, height);
    }
    
    private PlaneRectangle(Vec3 pointLeftDown, Vec3 pointLeftUp, Vec3 pointRightUp, Vec3 pointRightDown,
            Vec3 center, float width, float height) {
        this.pLD = pointLeftDown;
        this.pLU = pointLeftUp;
        this.pRU = pointRightUp;
        this.pRD = pointRightDown;
//        this.center = pointLeftDown.add(pointLeftUp.subtract(pointLeftDown).scale(0.5)).add(pointRightDown.subtract(pointLeftDown).scale(0.5));
        this.center = center;
        this.width = width;
        this.height = height;
        this.normalVec = pointRightUp.subtract(pointLeftDown).cross(pointLeftUp.subtract(pointRightDown)).normalize();
    }
    
//    public PlaneRectangle scale(double scale) {
//        return scale(scale, scale);
//    }
//    
//    public PlaneRectangle scale(double scaleX, double scaleY) {
//        Vector3d right = pRD.subtract(pLD).scale(scaleX * 0.5);
//        Vector3d up = pLU.subtract(pLD).scale(scaleY * 0.5);
//        return clockwisePoints(
//                center.add(right.reverse()).add(up.reverse()),
//                center.add(right.reverse()).add(up),
//                center.add(right).add(up));
//    }
    
    public Vec3 getUniformRandomPos() {
        return pLD
                .add(pRD.subtract(pLD).scale(RANDOM.nextDouble()))
                .add(pLU.subtract(pLD).scale(RANDOM.nextDouble()));
    }
    
    
    /**
     * @return The intersection point of the projectile's movement trajectory with this plane
     */
    @Nullable
    public Vec3 projectileIsPassing(Entity projectile) {
        Vec3 deltaMov = projectile.getDeltaMovement();
        Vec3 posCur = projectile.position();
        Vec3 posNext = posCur.add(deltaMov);
        double normalProjCur = posCur.subtract(center).dot(normalVec);
        double normalProjNext = posNext.subtract(center).dot(normalVec);
        if (normalProjCur > 0 && // current position is in front of the shield
                normalProjNext <= 0 /* next position would be behind the shield */ ) {
            double penetrationRatio = normalProjCur / (normalProjCur - normalProjNext);
            Vec3 intersectionPoint = posCur.add(deltaMov.scale(penetrationRatio));
            
            Vec3 centerToIntersectionVec = intersectionPoint.subtract(center);
            Vec3 horizontalShieldVec = pRD.subtract(pLD);
            Vec3 verticalShieldVec = pLU.subtract(pLD);
            boolean intersectsInShieldBounds = 
                    Math.abs(centerToIntersectionVec.dot(horizontalShieldVec)) <= width * width / 2 && // comparing the projections length with extra steps
                    Math.abs(centerToIntersectionVec.dot(verticalShieldVec)) <= height * height / 2;
            
            if (intersectsInShieldBounds) {
                return intersectionPoint;
            }
        }
        
        return null;
    }
    
}
