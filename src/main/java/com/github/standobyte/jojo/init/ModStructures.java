package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.world.gen.ConfiguredFeatureSupplier;
import com.github.standobyte.jojo.world.gen.structures.HamonTemplePieces;
import com.github.standobyte.jojo.world.gen.structures.HamonTempleStructure;
import com.github.standobyte.jojo.world.gen.structures.MeteoritePieces;
import com.github.standobyte.jojo.world.gen.structures.MeteoriteStructure;
import com.github.standobyte.jojo.world.gen.structures.MrPresidentRoomFeature;
import com.github.standobyte.jojo.world.gen.structures.PillarmanTemplePieces;
import com.github.standobyte.jojo.world.gen.structures.PillarmanTempleStructure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * The mod's worldgen entries.
 *
 * <p>1.20.1 makes structures data driven: the classes below are only the structure
 * *types*, while the biome set, generation step, terrain adaptation, the piece
 * placement (spacing, separation, salt) and the configured feature live in
 * {@code data/jojo/worldgen}. What used to be
 * {@code setupMapSpacingAndLand}/{@code registerConfiguredStructure} - including the
 * reflection into the old dimension settings - is therefore expressed as data, and
 * the config flags that switched a structure off are checked when the structure
 * looks for a generation point.</p>
 */
@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModStructures {
    /** The data driven structures, which the map trades point at. */
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> METEORITE = 
            net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(JojoMod.MOD_ID, "meteorite"));
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> HAMON_TEMPLE = 
            net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(JojoMod.MOD_ID, "hamon_temple"));
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> PILLARMAN_TEMPLE = 
            net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple"));

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, JojoMod.MOD_ID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, JojoMod.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, JojoMod.MOD_ID);

    public static final RegistryObject<StructureType<HamonTempleStructure>> HAMON_TEMPLE_TYPE = 
            STRUCTURE_TYPES.register("hamon_temple", () -> () -> HamonTempleStructure.CODEC);
    public static final RegistryObject<StructureType<MeteoriteStructure>> METEORITE_TYPE = 
            STRUCTURE_TYPES.register("meteorite", () -> () -> MeteoriteStructure.CODEC);
    public static final RegistryObject<StructureType<PillarmanTempleStructure>> PILLARMAN_TEMPLE_TYPE = 
            STRUCTURE_TYPES.register("pillarman_temple", () -> () -> PillarmanTempleStructure.CODEC);

    public static final RegistryObject<StructurePieceType> HAMON_TEMPLE_PIECE = 
            STRUCTURE_PIECES.register("hamon_temple", () -> HamonTemplePieces.PIECE_TYPE);
    public static final RegistryObject<StructurePieceType> METEORITE_PIECE = 
            STRUCTURE_PIECES.register("meteorite", () -> MeteoritePieces.PIECE_TYPE);
    public static final RegistryObject<StructurePieceType> PILLARMAN_TEMPLE_PIECE = 
            STRUCTURE_PIECES.register("pillarman_temple", () -> PillarmanTemplePieces.PIECE_TYPE);

    public static final RegistryObject<MrPresidentRoomFeature> MR_PRESIDENT_ROOM = FEATURES.register("mr_president_room", 
            () -> (new MrPresidentRoomFeature(NoneFeatureConfiguration.CODEC)));

    /** Kept so the configured feature can still be built where it is needed. */
    public static final ConfiguredFeatureSupplier<?, ?> CONFIGURED_MR_PRESIDENT_ROOM = 
            new ConfiguredFeatureSupplier<>(MR_PRESIDENT_ROOM, FeatureConfiguration.NONE);
}
