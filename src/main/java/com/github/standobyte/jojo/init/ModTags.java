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
     * <p>1.20.1 has no magic flag. Its witch resistant tag happens to contain the
     * damage types the three magic flagged 1.16.5 sources map to (magic, indirect
     * magic, thorns), but it also contains sonic boom, which did not exist in 1.16.5,
     * so that tag is a superset and not an equivalent of the flag. This tag holds
     * exactly the 1.16.5 set, and being a mod tag it also lets a damage type of this
     * mod or of another mod join it.</p>
     */
    public static final TagKey<DamageType> MAGIC = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(JojoMod.MOD_ID, "magic"));

    public static final TagKey<EntityType<?>> HAMON_DAMAGE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "hamon_damage"));
    public static final TagKey<EntityType<?>> NO_HAMON_DAMAGE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "no_hamon_damage"));
    public static final TagKey<EntityType<?>> VAMPIRE_CAN_DRAIN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "vampire_can_drain"));
    public static final TagKey<EntityType<?>> VAMPIRE_CANNOT_DRAIN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JojoMod.MOD_ID, "vampire_cannot_drain"));
    
    public static void initTags() {}

}
