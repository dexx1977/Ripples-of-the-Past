package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModPaintings {
    public static final DeferredRegister<PaintingVariant> PAINTINGS = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, JojoMod.MOD_ID);
    
    public static final RegistryObject<PaintingVariant> MONA_LISA = PAINTINGS.register("mona_lisa", 
            () -> new PaintingVariant(32, 48));
    
    public static final RegistryObject<PaintingVariant> MONA_LISA_HANDS = PAINTINGS.register("hands", 
            () -> new PaintingVariant(16, 16));

}
