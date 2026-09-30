package com.github.standobyte.jojo.client.render.entity.animnew;

import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public interface INamedModelParts {
    void putNamedModelPart(String name, ModelPart modelPart);
    ModelPart getModelPart(String name);
}
