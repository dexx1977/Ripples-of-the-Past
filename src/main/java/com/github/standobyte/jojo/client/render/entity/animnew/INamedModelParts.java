package com.github.standobyte.jojo.client.render.entity.animnew;

import net.minecraft.client.model.geom.ModelPart;

public interface INamedModelParts {
    void putNamedModelPart(String name, ModelPart modelPart);
    ModelPart getModelPart(String name);
}
