package com.github.standobyte.jojo.init;

import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.block.MagiciansRedFireBlock;
import com.github.standobyte.jojo.block.MeteoricOreBlock;
import com.github.standobyte.jojo.block.MrPresidentGemBlock;
import com.github.standobyte.jojo.block.PillarmanBossMultiBlock;
import com.github.standobyte.jojo.block.StoneMaskBlock;
import com.github.standobyte.jojo.block.WoodenCoffinBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JojoMod.MOD_ID);
    
    
    public static final RegistryObject<StoneMaskBlock> STONE_MASK = BLOCKS.register("stone_mask", 
            () -> new StoneMaskBlock(Block.Properties.copy(Blocks.STONE).harvestLevel(0).requiresCorrectToolForDrops().noCollission().isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<StoneMaskBlock> AJA_STONE_MASK = BLOCKS.register("aja_stone_mask", 
            () -> new StoneMaskBlock(Block.Properties.copy(Blocks.STONE).harvestLevel(0).requiresCorrectToolForDrops().noCollission().isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<PillarmanBossMultiBlock> SLUMBERING_PILLARMAN = BLOCKS.register("slumbering_pillarman", 
            () -> new PillarmanBossMultiBlock(Block.Properties.copy(Blocks.BEDROCK).isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<LiquidBlock> BOILING_BLOOD = BLOCKS.register("boiling_blood", 
            () -> new LiquidBlock(ModFluids.BOILING_BLOOD, BlockBehaviour.Properties.of()
                    .liquid().pushReaction(PushReaction.DESTROY).mapColor(MapColor.LAVA)
                    .noCollission().randomTicks().strength(100.0F).lightLevel(blockState -> 15).noLootTable()));
    
    public static final RegistryObject<Block> METEORIC_IRON = BLOCKS.register("meteoric_iron", 
           () -> new Block(Block.Properties.copy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops()));
    
    public static final RegistryObject<MeteoricOreBlock> METEORIC_ORE = BLOCKS.register("meteoric_ore", 
           () -> new MeteoricOreBlock(Block.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                    .strength(10.0F, 3.0F).requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK)));
    
    public static final RegistryObject<MagiciansRedFireBlock> MAGICIANS_RED_FIRE = BLOCKS.register("magicians_red_fire", 
            () -> new MagiciansRedFireBlock(Block.Properties.of().mapColor(MapColor.FIRE).replaceable().noCollission().instabreak().lightLevel((blockState) -> {
                return 15;
            }).sound(SoundType.WOOL)));
    
    public static final Map<DyeColor, RegistryObject<WoodenCoffinBlock>> WOODEN_COFFIN_OAK = ModItems.register16colorsBlock("wooden_coffin_oak", 
            color -> new WoodenCoffinBlock(color, Block.Properties.copy(Blocks.OAK_PLANKS)));
    
    public static final RegistryObject<Block> COCO_JUMBO_SHELL = BLOCKS.register("coco_jumbo_shell", 
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<Block> MR_PRESIDENT_EXIT = BLOCKS.register("mr_president_gem", 
            () -> new MrPresidentGemBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                    .strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops()
                    .lightLevel(state -> 15).noLootTable().isValidSpawn((state, reader, pos, entityType) -> false)));
    
}
