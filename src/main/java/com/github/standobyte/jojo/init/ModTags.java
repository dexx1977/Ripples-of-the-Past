package com.github.standobyte.jojo.init;

import net.minecraft.world.damagesource.DamageType;
import com.github.standobyte.jojo.JojoMod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class ModTags {
    /**
     * The damage the 1.16.5 magic flag marked (what DamageSource#isMagic returned).
     *
     * <p>Mojang translated that flag into the witch resistant tag, so the vanilla
     * members are the ones that were flagged in 1.16.5 (magic, indirect magic, thorns)
     * plus sonic boom, which 1.20.1 classifies the same way. It is a mod tag so that a
     * damage type of this mod or of another mod can join the set.</p>
     */
    public static final TagKey<DamageType> MAGIC = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(JojoMod.MOD_ID, "magic"));

    public static final TagKey<EntityType<?>> HAMON_DAMAGE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "hamon_damage"));
    public static final TagKey<EntityType<?>> NO_HAMON_DAMAGE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "no_hamon_damage"));
    public static final TagKey<EntityType<?>> VAMPIRE_CAN_DRAIN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "vampire_can_drain"));
    public static final TagKey<EntityType<?>> VAMPIRE_CANNOT_DRAIN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "vampire_cannot_drain"));
    
    public static void initTags() {}

}
