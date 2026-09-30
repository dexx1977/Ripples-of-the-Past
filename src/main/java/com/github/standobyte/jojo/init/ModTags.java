package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class ModTags {
    public static final TagKey<EntityType<?>> HAMON_DAMAGE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "hamon_damage"));
    public static final TagKey<EntityType<?>> NO_HAMON_DAMAGE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "no_hamon_damage"));
    public static final TagKey<EntityType<?>> VAMPIRE_CAN_DRAIN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "vampire_can_drain"));
    public static final TagKey<EntityType<?>> VAMPIRE_CANNOT_DRAIN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "vampire_cannot_drain"));
    
    public static void initTags() {}

}
