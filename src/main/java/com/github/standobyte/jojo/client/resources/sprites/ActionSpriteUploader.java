package com.github.standobyte.jojo.client.resources.sprites;

import java.util.stream.Stream;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import com.github.standobyte.jojo.JojoMod;

@Deprecated
public class ActionSpriteUploader extends TextureAtlasHolder {
    public ActionSpriteUploader(TextureManager textureManager) {
        super(textureManager, new ResourceLocation("textures/atlas/actions.png"), new ResourceLocation(JojoMod.MOD_ID, "actions"));
    }

    public <P extends IPower<P, ?>> TextureAtlasSprite getSprite(Action<P> action, P power) {
        return getSprite(action.getTexture(power));
    }
    
    @Override
    public TextureAtlasSprite getSprite(ResourceLocation texLocation) {
        // 1.20.1 names the sprite by its full path, the old prefix is part of it now
        return super.getSprite(new ResourceLocation(texLocation.getNamespace(), "action/" + texLocation.getPath()));
    }
    
    public static ResourceLocation getIcon(Action<?> action) {
        ResourceLocation key = action.getRegistryName();
        return new ResourceLocation(key.getNamespace(), "textures/action/" + key.getPath() + ".png");
    }
}
