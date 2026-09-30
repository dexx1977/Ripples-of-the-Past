package com.github.standobyte.jojo.client.resources.sprites;

import java.util.stream.Stream;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import com.github.standobyte.jojo.JojoMod;

public class HamonSkillSpriteUploader extends TextureAtlasHolder {
    public HamonSkillSpriteUploader(TextureManager textureManager) {
        super(textureManager, new ResourceLocation("textures/atlas/hamon_skills.png"), new ResourceLocation(JojoMod.MOD_ID, "hamon_skills"));
    }

    public TextureAtlasSprite getSprite(AbstractHamonSkill skill) {
        // 1.20.1 names the sprite by its full path, the old prefix is part of it now
        return this.getSprite(new ResourceLocation(skill.getRegistryName().getNamespace(), "hamon/" + skill.getRegistryName().getPath()));
    }
}
