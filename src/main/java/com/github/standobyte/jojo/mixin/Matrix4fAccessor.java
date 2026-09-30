package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import org.joml.Matrix4f;

@Mixin(value = Matrix4f.class, remap = false)
public interface Matrix4fAccessor {

    @Accessor("m00") float getM00();
    @Accessor("m01") float getM01();
    @Accessor("m02") float getM02();
    @Accessor("m03") float getM03();
    @Accessor("m10") float getM10();
    @Accessor("m11") float getM11();
    @Accessor("m12") float getM12();
    @Accessor("m13") float getM13();
    @Accessor("m20") float getM20();
    @Accessor("m21") float getM21();
    @Accessor("m22") float getM22();
    @Accessor("m23") float getM23();
    @Accessor("m30") float getM30();
    @Accessor("m31") float getM31();
    @Accessor("m32") float getM32();
    @Accessor("m33") float getM33();
}
