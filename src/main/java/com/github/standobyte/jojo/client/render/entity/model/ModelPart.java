package com.github.standobyte.jojo.client.render.entity.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Builder-style model part, kept on top of the 1.20.1 model geometry API.
 *
 * <p>Every hand-written model in this mod (and the Blockbench parsers) is built
 * the 1.16.5 way: create a part with {@code new ModelPart(this)}, add cuboids with
 * {@code texOffs(u, v).addBox(...)} and attach children with {@code addChild(...)}.
 * 1.20.1 removed those members in favour of {@link CubeListBuilder},
 * {@link PartDefinition} and baking, which would mean rewriting every model
 * definition and risking geometry drift. Instead this class records the same
 * declarations and bakes them through the 1.20.1 builders, so the models keep
 * their source shape and their exact geometry, UVs and mirrors.</p>
 *
 * <p>It extends the vanilla part, so it can be stored in vanilla model fields and
 * rendered by vanilla code; children are registered in the vanilla children map
 * (rendering recurses through them as usual) while the mod keeps its own naming
 * where it needs it.</p>
 *
 * <p>The texture size is read from the owning model exactly like the old
 * {@code ModelBase#texWidth/texHeight} pair, which the models still assign in
 * their constructors; models that never declare it keep the old 64x64 default.</p>
 */
public class ModelPart extends net.minecraft.client.model.geom.ModelPart {
    private static final String CHILD_NAME_PREFIX = "part";
    private static int childCount = 0;
    private static final Map<Class<?>, Field[]> TEX_SIZE_FIELDS = new ConcurrentHashMap<>();
    private static final int DEFAULT_TEX_SIZE = 64;

    private final CubeListBuilder cubeBuilder = CubeListBuilder.create();
    private int texOffsU;
    private int texOffsV;
    private int texWidth;
    private int texHeight;
    private boolean baked;

    /** Mirrors the old public {@code ModelRenderer#mirror} flag. */
    public boolean mirror;

    public ModelPart(@Nullable Object owner) {
        this(owner, 0, 0);
    }

    /** The 1.16.5 constructor that took the texture size and the texture offsets. */
    public ModelPart(int texWidth, int texHeight, int texOffsX, int texOffsY) {
        this((Object) null, texOffsX, texOffsY);
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    public ModelPart(@Nullable Object owner, int texOffsX, int texOffsY) {
        super(new ArrayList<>(), new LinkedHashMap<>());
        this.texOffsU = texOffsX;
        this.texOffsV = texOffsY;
        int[] size = textureSizeOf(owner);
        this.texWidth = size[0];
        this.texHeight = size[1];
    }

    /**
     * Reads the texture size the owning model declared, like the old
     * {@code ModelRenderer(ModelBase)} constructor did.
     */
    private static int[] textureSizeOf(@Nullable Object owner) {
        int width = DEFAULT_TEX_SIZE;
        int height = DEFAULT_TEX_SIZE;
        if (owner == null) {
            return new int[] { width, height };
        }
        Field[] fields = TEX_SIZE_FIELDS.computeIfAbsent(owner.getClass(), ModelPart::findTexSizeFields);
        if (fields[0] != null) {
            width = readIntField(fields[0], owner, DEFAULT_TEX_SIZE);
        }
        if (fields[1] != null) {
            height = readIntField(fields[1], owner, DEFAULT_TEX_SIZE);
        }
        return new int[] { width, height };
    }

    private static Field[] findTexSizeFields(Class<?> type) {
        Field width = null;
        Field height = null;
        for (Class<?> current = type; current != null && current != Object.class && (width == null || height == null); current = current.getSuperclass()) {
            try {
                if (width == null) {
                    width = current.getDeclaredField("texWidth");
                    width.setAccessible(true);
                }
            }
            catch (NoSuchFieldException ignored) {
            }
            try {
                if (height == null) {
                    height = current.getDeclaredField("texHeight");
                    height.setAccessible(true);
                }
            }
            catch (NoSuchFieldException ignored) {
            }
        }
        return new Field[] { width, height };
    }

    private static int readIntField(Field field, Object owner, int fallback) {
        try {
            return field.getInt(owner);
        }
        catch (IllegalAccessException | IllegalArgumentException e) {
            return fallback;
        }
    }

    public ModelPart texOffs(int u, int v) {
        this.texOffsU = u;
        this.texOffsV = v;
        return this;
    }

    public ModelPart setTexSize(int texWidth, int texHeight) {
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        return this;
    }

    /** Attaches a child to a part of any model, including inherited vanilla parts. */
    public static void addChild(net.minecraft.client.model.geom.ModelPart parent, ModelPart child) {
        parent.children.put("part_" + parent.children.size(), child);
    }

    public ModelPart addChild(ModelPart child) {
        // Vanilla renders children from this map; unnamed as in the old API, so the
        // keys only exist to satisfy the map and preserve insertion order.
        this.children.put("part_" + this.children.size(), child);
        return this;
    }

    public ModelPart addBox(float x, float y, float z, float width, float height, float depth) {
        return addBox(x, y, z, width, height, depth, 0.0F, false);
    }

    public ModelPart addBox(float x, float y, float z, float width, float height, float depth, float inflate) {
        return addBox(x, y, z, width, height, depth, inflate, false);
    }

    public ModelPart addBox(float x, float y, float z, float width, float height, float depth, float inflate, boolean mirror) {
        cubeBuilder.texOffs(this.texOffsU, this.texOffsV);
        cubeBuilder.mirror(mirror || this.mirror);
        cubeBuilder.addBox(x, y, z, width, height, depth, new CubeDeformation(inflate), 1.0F, 1.0F);
        return this;
    }

    /** Alias of {@link #setRotation(float, float, float)}, kept for the ported models. */
    public ModelPart setRotationAngle(float xRot, float yRot, float zRot) {
        setRotation(xRot, yRot, zRot);
        return this;
    }

    /** Copies the pose of another part, as the old model builder allowed. */
    public ModelPart copyFrom(ModelPart other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.xRot = other.xRot;
        this.yRot = other.yRot;
        this.zRot = other.zRot;
        this.visible = other.visible;
        return this;
    }

    private void ensureBaked() {
        if (baked) {
            return;
        }
        baked = true;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition definition = mesh.getRoot().addOrReplaceChild("part", cubeBuilder, PartPose.ZERO);
        net.minecraft.client.model.geom.ModelPart bakedPart = definition.bake(this.texWidth, this.texHeight);
        // Only the cuboids are taken: this instance keeps its own pose fields, so
        // animation code keeps writing x/y/z and xRot/yRot/zRot as before.
        this.cubes.addAll(bakedPart.cubes);
    }

    @Override
    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        ensureBaked();
        super.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    /** The 1.16.5 render signature without explicit colours, used all over the client code. */
    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        render(poseStack, buffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public ModelPart getChild(String name) {
        return (ModelPart) super.getChild(name);
    }

    /** @return a mutable copy of this part's cuboids, for code that edits geometry at runtime. */
    public java.util.List<Cube> cubesMutable() {
        ensureBaked();
        return this.cubes;
    }
}
