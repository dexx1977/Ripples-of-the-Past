package com.github.standobyte.jojo.util.mc.damage;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * The mod's damage types.
 *
 * <p>1.20.1 moved the properties of a damage source (armour/magic bypass, scaling,
 * death message) into the data-driven {@link DamageType} registry, and
 * {@link DamageSource} is now built from a {@link Holder} of one. The ids and the
 * death message ids are the ones this mod used before, so existing translation
 * keys and any saved data that refers to them still line up; the type files live in
 * {@code data/jojo/damage_type}.</p>
 */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> ULTRAVIOLET = key("ultraviolet");
    public static final ResourceKey<DamageType> COLD = key("cold");
    public static final ResourceKey<DamageType> HAMON = key("hamon");
    public static final ResourceKey<DamageType> PILLAR_MAN_ABSORPTION = key("pillar_man_absorption");
    public static final ResourceKey<DamageType> STAND_VIRUS = key("stand_virus");
    public static final ResourceKey<DamageType> STAND_VIRUS_METEORITE = key("stand_virus_meteorite");
    public static final ResourceKey<DamageType> SUFFOCATION = key("suffocation");
    public static final ResourceKey<DamageType> EYE_OF_ENDER_SHARDS = key("eye_of_ender_shards");
    public static final ResourceKey<DamageType> STONE_MASK = key("stone_mask");
    public static final ResourceKey<DamageType> HEALTH_LINK = key("health_link");
    /** The old on fire source with the explosion flag, in both the fire and explosion tags. */
    public static final ResourceKey<DamageType> ON_FIRE_EXPLOSION = key("on_fire_explosion");

    private ModDamageTypes() {}

    public static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(JojoMod.MOD_ID, name));
    }

    /** Resolves a damage type from the registry the level was loaded with. */
    public static Holder<DamageType> holder(Level level, ResourceKey<DamageType> type) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
    }

    @Nullable
    public static Holder<DamageType> holder(@Nullable Entity entity, String msgId) {
        return entity != null ? holder(entity.level(), key(msgId)) : null;
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(holder(level, type));
    }

    public static DamageSource source(Entity entity, ResourceKey<DamageType> type) {
        return new DamageSource(holder(entity.level(), type), entity);
    }

    public static DamageSource source(Entity entity, String msgId) {
        return source(entity, key(msgId));
    }

    public static DamageSource source(Entity directEntity, Entity causingEntity, ResourceKey<DamageType> type) {
        return new DamageSource(holder(directEntity.level(), type), directEntity, causingEntity);
    }

    public static DamageSource source(Entity directEntity, Entity causingEntity, String msgId) {
        return source(directEntity, causingEntity, key(msgId));
    }
}
