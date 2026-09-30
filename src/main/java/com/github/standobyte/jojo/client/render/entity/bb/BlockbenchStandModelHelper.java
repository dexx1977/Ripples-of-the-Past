package com.github.standobyte.jojo.client.render.entity.bb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.animnew.INamedModelParts;
import com.github.standobyte.jojo.client.render.entity.pose.XRotationModelRenderer;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.Model;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class BlockbenchStandModelHelper {

    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    /*
     * Allows adding models exported from Blockbench with minimal edits to the model file
     * 
     * Add the .java model file exported from Blockbench to your project, 
     * then create another class for your actual model (which will contain stuff like animations).
     * Call this method from your model's constructor to copy the ModelRenderer values from the Blockbench method.
     * 
     * Example: CrazyDiamondModel2 and CrazyDiamondModelConvertExample.
     * CrazyDiamondModel2 can be used interchangeably with CrazyDiamondModel in CrazyDiamondRenderer.
     *
     */
    public static void fillFromBlockbenchExport(Model bbSourceModel, Model inModModel) {
        Field[] bbModelPartFields = bbSourceModel.getClass().getDeclaredFields();
        Map<String, ModelPart> bbModelParts = new HashMap<>();

        try {
            for (Field bbModelPartField : bbModelPartFields) {
                if (bbModelPartField.getType() == ModelPart.class) {
                    bbModelPartField.setAccessible(true);
                    bbModelParts.put(bbModelPartField.getName(), (ModelPart) bbModelPartField.get(bbSourceModel));
                }
            }
            
            replaceModelParts(inModModel, bbModelParts);
        } catch (Exception e) {
            JojoMod.getLogger().error("Failed to add model parts to {} via Blockbench helper", inModModel.getClass().getName(), e);
        }
        
        ModelPart.setTextureSize(inModModel, ModelPart.textureWidthOf(bbSourceModel), ModelPart.textureHeightOf(bbSourceModel));
    }
    
    public static void replaceModelParts(Model inModModel, Map<String, ModelPart> source) throws IllegalArgumentException, IllegalAccessException {
        Set<Field> declaredModelParts = FieldUtils.getAllFieldsList(inModModel.getClass()).stream()
                .filter(field -> ModelPart.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toCollection(HashSet::new));
        List<ModelPart> editedParts = new ArrayList<>();
        Map<ModelPart, ModelPart> remapParents = new HashMap<>();
        INamedModelParts putNamed = inModModel instanceof INamedModelParts ? (INamedModelParts) inModModel : null;
        
        for (Map.Entry<String, ModelPart> entry : source.entrySet()) {
            String name = entry.getKey();
            ModelPart blockbenchPart = entry.getValue();
            
            Iterator<Field> it = declaredModelParts.iterator();
            boolean foundModelPart = false;
            while (it.hasNext() && !foundModelPart) {
                Field inModPartField = it.next();
                
                if (inModPartField.getName().equals(name)) {
                    boolean xRotJank = false;
                    if (!inModPartField.getType().isAssignableFrom(blockbenchPart.getClass())) {
                        if (inModPartField.getType() == XRotationModelRenderer.class && blockbenchPart.getClass() == ModelPart.class) {
                            xRotJank = true;
                        }
                        else {
                            RuntimeException e = new ClassCastException(blockbenchPart.getClass() + " can't be cast to " + inModPartField.getType());
                            throw e;
                        }
                    }
                    
                    if (xRotJank) {
                        ModelPart jank = jankToKeepAddonsWorkingForNow(blockbenchPart);
                        remapParents.put(blockbenchPart, jank);
                        blockbenchPart = jank;
                    }
                    inModPartField.setAccessible(true);
                    editedParts.add(blockbenchPart);
                    inModPartField.set(inModModel, blockbenchPart);
                    if (putNamed != null) {
                        putNamed.putNamedModelPart(name, blockbenchPart);
                    }
                    
                    it.remove();
                    foundModelPart = true;
                }
            }
        }
        
        for (Field field : declaredModelParts) {
            field.setAccessible(true);
            ModelPart declaredPartNotInGecko = (ModelPart) field.get(inModModel);
            if (declaredPartNotInGecko != null) {
                declaredPartNotInGecko.cubes = new java.util.ArrayList<>(); // baked cuboid lists are immutable in 1.20.1
                declaredPartNotInGecko.children.clear();
            }
        }
        
        for (ModelPart modelPart : editedParts) {
            // 1.20.1 keeps the children in a named map, so the entries are replaced in place
            java.util.Map<String, net.minecraft.client.model.geom.ModelPart> children = modelPart.children;
            if (!children.isEmpty()) {
                remapParents.forEach((oldChild, newChild) -> {
                    children.replaceAll((name, child) -> child == oldChild ? newChild : child);
                });
            }
        }
    }
    
    public static void replaceCubes(Model inModModel, Map<String, ModelPart> source) throws IllegalArgumentException, IllegalAccessException {
        List<Field> inModModelParts = FieldUtils.getAllFieldsList(inModModel.getClass()).stream()
                .filter(field -> ModelPart.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        
        for (Map.Entry<String, ModelPart> entry : source.entrySet()) {
            String name = entry.getKey();
            ModelPart blockbenchPart = entry.getValue();
            
            Iterator<Field> it = inModModelParts.iterator();
            while (it.hasNext()) {
                Field inModPartField = it.next();
                if (inModPartField.getName().equals(name)) {
                    
                    inModPartField.setAccessible(true);
                    ModelPart inModModelPart = (ModelPart) inModPartField.get(inModModel);
                    inModModelPart.cubes = new java.util.ArrayList<>(blockbenchPart.cubes); // baked cuboid lists are immutable in 1.20.1
                    
                    it.remove();
                }
            }
        }
    }
    
    private static XRotationModelRenderer jankToKeepAddonsWorkingForNow(ModelPart modelPart) {
        XRotationModelRenderer deepCopy = new XRotationModelRenderer(256, 256, 0, 0);
        
        deepCopy.x = modelPart.x;
        deepCopy.y = modelPart.y;
        deepCopy.z = modelPart.z;
        deepCopy.xRot = modelPart.xRot;
        deepCopy.yRot = modelPart.yRot;
        deepCopy.zRot = modelPart.zRot;
        deepCopy.mirror = modelPart.mirror;
        deepCopy.visible = modelPart.visible;
        
        deepCopy.cubes.addAll(modelPart.cubes);
        deepCopy.children.putAll(modelPart.children);
        
        return deepCopy;
    }
    
    
    
    public static <M extends Model> void fillFromUnbaked(EntityModelUnbaked model, M inModModel) {
        try {
            replaceModelParts(inModModel, model.getNamedModelParts());
        } catch (Exception e) {
            JojoMod.getLogger().error("Failed to import Geckolib format model as {}", inModModel.getClass().getName(), e);
        }
    }
}
