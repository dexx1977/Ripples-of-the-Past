package com.github.standobyte.jojo.client.render.rendertype;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class ModifiedRenderTypeBuffers extends MultiBufferSource.BufferSource {
    private final Map<RenderType, RenderType> renderTypesRemapped;
    
    public static MultiBufferSource.BufferSource create(MultiBufferSource.BufferSource originalBuffers, UnaryOperator<RenderType> renderTypeRemapper) {
        BufferBuilder originalBuilder = ClientReflection.getBuilder(originalBuffers);
        Map<RenderType, BufferBuilder> fixedBuffersOriginal = ClientReflection.getFixedBuffers(originalBuffers);
        
        Map<RenderType, RenderType> renderTypesRemapped = fixedBuffersOriginal.keySet().stream()
                .collect(Collectors.toMap(
                        type -> type, 
                        type -> renderTypeRemapper.apply(type)));
        
        Map<RenderType, BufferBuilder> fixedBuffersRemapped = fixedBuffersOriginal.entrySet().stream()
            .collect(Collectors.toMap(
                    entry -> renderTypesRemapped.get(entry.getKey()), 
                    entry -> entry.getValue()));
        
        return new ModifiedRenderTypeBuffers(originalBuilder, fixedBuffersRemapped, renderTypesRemapped);
    }

    protected ModifiedRenderTypeBuffers(BufferBuilder builder, Map<RenderType, BufferBuilder> fixedBuffers, Map<RenderType, RenderType> renderTypesRemapped) {
        super(builder, fixedBuffers);
        this.renderTypesRemapped = renderTypesRemapped;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return super.getBuffer(renderTypesRemapped.getOrDefault(renderType, renderType));
    }

}
