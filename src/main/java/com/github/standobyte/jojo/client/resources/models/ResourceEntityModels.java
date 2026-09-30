package com.github.standobyte.jojo.client.resources.models;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;
import com.github.standobyte.jojo.client.resources.models.StandModelOverrides.CustomModelPrepared;

import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;

public class ResourceEntityModels {
    static final Map<ResourceLocation, Consumer<EntityModelUnbaked>> resourceListeners = new HashMap<>();
    /** The models of the last reload, for renderers that are only created later. */
    private static final Map<ResourceLocation, CustomModelPrepared> preparedModels = new HashMap<>();
    
    
    static void loadEntityModel(ResourceLocation listenerId, CustomModelPrepared readJson) {
        preparedModels.put(listenerId, readJson);
        notifyListener(listenerId, readJson);
    }
    
    private static void notifyListener(ResourceLocation modelId, CustomModelPrepared readJson) {
        Consumer<EntityModelUnbaked> listener = resourceListeners.get(modelId);
        if (listener == null) {
            return;
        }
        try {
            listener.accept(readJson.createModel(modelId));
        }
        catch (Exception e) {
            JojoMod.getLogger().error("Failed to load model {}", modelId, e);
        }
    }
    
    public static <M extends Model> void addModelLoader(ResourceLocation modelPath, Supplier<M> makeNewModel, Consumer<M> applyModel) {
        addListener(modelPath, parsedModel -> {
            try {
                M newModel = makeNewModel.get();
                BlockbenchStandModelHelper.replaceModelParts(newModel, parsedModel.getNamedModelParts());
                applyModel.accept(newModel);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                JojoMod.getLogger().error("Failed to load model {}", modelPath, e);
            }
        });
    }
    
    public static void addListener(ResourceLocation id, Consumer<EntityModelUnbaked> onLoad) {
        resourceListeners.put(id, onLoad);
        // item renderers are constructed lazily, possibly after the resource reload
        // that parsed the model, so the stored model is handed over right away
        CustomModelPrepared prepared = preparedModels.get(id);
        if (prepared != null) {
            notifyListener(id, prepared);
        }
    }
}
