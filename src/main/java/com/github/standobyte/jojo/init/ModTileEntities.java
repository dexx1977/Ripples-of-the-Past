package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.tileentity.PillarmanBossTileEntity;
import com.github.standobyte.jojo.tileentity.StoneMaskTileEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, JojoMod.MOD_ID);
    
    public static final RegistryObject<BlockEntityType<StoneMaskTileEntity>> STONE_MASK = TILE_ENTITIES.register("stone_mask", 
            () -> BlockEntityType.Builder.of(StoneMaskTileEntity::new, ModBlocks.STONE_MASK.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<PillarmanBossTileEntity>> SLUMBERING_PILLARMAN = TILE_ENTITIES.register("slumbering_pillarman", 
            () -> BlockEntityType.Builder.of(PillarmanBossTileEntity::new, ModBlocks.SLUMBERING_PILLARMAN.get()).build(null));
}
