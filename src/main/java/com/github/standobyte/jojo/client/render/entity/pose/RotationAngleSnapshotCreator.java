package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.Arrays;

import com.github.standobyte.jojo.client.render.entity.pose.anim.RotationAnglesSnapshot;

import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.Entity;

public class RotationAngleSnapshotCreator<T extends Entity> {
    private final ModelPart[] modelRenderers;
    
    public RotationAngleSnapshotCreator(ModelPart... modelRenderers) {
        this.modelRenderers = modelRenderers;
    }
    
    public RotationAnglesSnapshot<T> create() {
        return new RotationAnglesSnapshot<T>(
                Arrays.stream(modelRenderers)
                .map(part -> new RotationAngle(part, part.xRot, part.yRot, part.zRot))
                .toArray(RotationAngle[]::new));
    }

}
