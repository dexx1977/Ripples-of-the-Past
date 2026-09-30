package com.github.standobyte.jojo.client.render.entity.bb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.model.geom.ModelPart;

public class EntityModelUnbaked {
    private final Map<String, ModelPart> modelParts = new HashMap<>();
    public final int texWidth;
    public final int texHeight;
    
    public EntityModelUnbaked(int texWidth, int texHeight) {
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }
    
    private final Map<ModelPart, String> orphanage = new HashMap<>();
    public void addModelPart(String name, ModelPart modelPart, @Nullable String parentName) {
        modelParts.put(name, modelPart);
        if (parentName != null) {
            if (parentName.equals(name)) throw new IllegalArgumentException();
            
            ModelPart parent = modelParts.get(parentName);
            if (parent != null) {
                parent.addChild(modelPart);
            }
            else {
                orphanage.put(modelPart, parentName);
            }
        }
        
        if (!orphanage.isEmpty()) {
            Iterator<Map.Entry<ModelPart, String>> orphanIter = orphanage.entrySet().iterator();
            while (orphanIter.hasNext()) {
                Map.Entry<ModelPart, String> orphan = orphanIter.next();
                if (orphan.getValue().equals(name)) {
                    modelPart.addChild(orphan.getKey());
                    orphanIter.remove();
                }
            }
        }
    }
    
    public Map<String, ModelPart> getNamedModelParts() {
        return modelParts;
    }

}
