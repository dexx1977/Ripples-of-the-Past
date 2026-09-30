package com.github.standobyte.jojo.client.render;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.render.entity.bb.MeshVerticesHelper;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.model.Model;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import org.joml.Vector3f;

public class MeshModelBox extends ModelPart.Cube {
    
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private MeshModelBox(Builder builder) {
        super(0, 0, 
                builder.minX, 
                builder.minY, 
                builder.minZ, 
                builder.maxX - builder.minX, 
                builder.maxY - builder.minY, 
                builder.maxZ - builder.minZ, 
                0, 0, 0, 
                false, 1, 1);
        
        ModelPart.Polygon[] quads = builder.quads.toArray(new ModelPart.Polygon[0]);
        this.polygons = quads;
    }
    
    // a convenience method for chainable calls
    public void addCube(ModelPart modelRenderer) {
        modelRenderer.cubes.add(this);
    }
    
    
    
    public static class Builder {
        private final boolean livingEntityRenderHacks;
        private final MeshFaceBuilder faceBuilder;
        private float minX;
        private float minY;
        private float minZ;
        private float maxX;
        private float maxY;
        private float maxZ;
        private final List<ModelPart.Polygon> quads = new ArrayList<>();
        
        public Builder(boolean livingEntityRenderHacks, float texWidth, float texHeight) {
            this.livingEntityRenderHacks = livingEntityRenderHacks;
            faceBuilder = new MeshFaceBuilder(this, texWidth, texHeight);
        }
        
        public Builder(boolean livingEntityRenderHacks, Model model) {
            this(livingEntityRenderHacks, model.texWidth, model.texHeight);
        }
        
        public MeshFaceBuilder startFace(Direction lightingDir) {
            if (livingEntityRenderHacks) {
                lightingDir = lightingDir.getAxis() == Axis.Z ? lightingDir : lightingDir.getOpposite();
            }
            return faceBuilder.withState(lightingDir, null, false, false);
        }
        
        public MeshFaceBuilder startFace(Vector3f faceNormal) {
            return faceBuilder.withState(null, faceNormal, false, false);
        }
        
        public MeshFaceBuilder startFaceCalcNormal() {
            return faceBuilder.withState(null, null, true, false);
        }
        
        public MeshFaceBuilder startFaceCalcNormal(boolean invertVec) {
            return faceBuilder.withState(null, null, true, invertVec);
        }
        
        
        public MeshModelBox buildCube() {
            MeshModelBox cube = new MeshModelBox(this);
            return cube;
        }
        
        
        public class MeshFaceBuilder {
            private final MeshModelBox.Builder boxBuilder;
            private final float texWidth;
            private final float texHeight;
            
            private Direction direction;
            private Vector3f faceNormal;
            private boolean calcNormalFromVertices;
            private boolean invertCalcNormal;
            
            private List<ModelPart.Vertex> vertices = new ArrayList<>();
            
            private MeshFaceBuilder(MeshModelBox.Builder boxBuilder, float texWidth, float texHeight) {
                this.boxBuilder = boxBuilder;
                this.texWidth = texWidth;
                this.texHeight = texHeight;
            }
            
            public MeshFaceBuilder withVertex(double x, double y, double z, double texU, double texV) {
                if (boxBuilder.livingEntityRenderHacks) {
                    x = -x;
                    y = -y;
                }
                float xF = (float) x;
                float yF = (float) y;
                float zF = (float) z;
                boxBuilder.minX = Math.min(boxBuilder.minX, xF);
                boxBuilder.minY = Math.min(boxBuilder.minY, yF);
                boxBuilder.minZ = Math.min(boxBuilder.minZ, zF);
                boxBuilder.maxX = Math.max(boxBuilder.maxX, xF);
                boxBuilder.maxY = Math.max(boxBuilder.maxY, yF);
                boxBuilder.maxZ = Math.max(boxBuilder.maxZ, zF);
                
                ModelPart.Vertex vertex = new ModelPart.Vertex(
                        xF, yF, zF, (float) texU / texWidth, (float) texV / texHeight);
                vertices.add(vertex);
                return this;
            }
            
            private MeshFaceBuilder withState(Direction direction, Vector3f faceNormal, 
                    boolean calcNormalFromVertices, boolean invertCalcNormal) {
                this.direction = direction;
                this.faceNormal = faceNormal;
                this.calcNormalFromVertices = calcNormalFromVertices;
                this.invertCalcNormal = invertCalcNormal;
                return this;
            }
            
            private static final int MAX_VERTICES = 4;
            public MeshModelBox.Builder createFace() {
                if (vertices.size() > 2) {
                    ModelPart.Vertex[] verticesDummy = new ModelPart.Vertex[] {
                            new ModelPart.Vertex(0, 0, 0, 0, 0),
                            new ModelPart.Vertex(0, 0, 0, 0, 0),
                            new ModelPart.Vertex(0, 0, 0, 0, 0),
                            new ModelPart.Vertex(0, 0, 0, 0, 0)
                    };
                    ModelPart.Polygon quad = new ModelPart.Polygon(verticesDummy, 
                            0, 0, 0, 0, 1, 1, false, direction != null ? direction : Direction.UP);
                    
                    ModelPart.Vertex[] verticesArr = vertices.toArray(new ModelPart.Vertex[MAX_VERTICES]);
                    if (this.vertices.size() < MAX_VERTICES) {
                        Vertex lastVertex = verticesArr[this.vertices.size() - 1];
                        for (int i = this.vertices.size(); i < verticesArr.length; i++) {
                            verticesArr[i] = lastVertex;
                        }
                    }
                    else {
                        MeshVerticesHelper.sortVertices(verticesArr);
                    }
                    ClientReflection.setVertices(quad, verticesArr);
                    
                    if (calcNormalFromVertices) {
                        Vector3f pos0 = verticesArr[0].pos.copy();
                        Vector3f vec1 = verticesArr[1].pos.copy();
                        Vector3f vec2 = verticesArr[2].pos.copy();
                        vec1.sub(pos0);
                        vec2.sub(pos0);
                        vec1.cross(vec2);
                        vec1.normalize();
                        if (invertCalcNormal) {
                            vec1.mul(-1);
                        }
                        ClientReflection.setNormal(quad, vec1);
                    }
                    else if (faceNormal != null) {
                        ClientReflection.setNormal(quad, faceNormal);
                    }
                    
                    boxBuilder.quads.add(quad);
                }
                
                vertices.clear();
                return boxBuilder;
            }
        }
    }
}
