package com.github.standobyte.jojo.client.ui.text;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.util.mod.StoryPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class JojoTextComponentWrapper implements MutableComponent {
    private static final Component[] SPRITE_OFFSET = Util.make(new Component[8], array -> {
        for (int i = 0; i < array.length; i++) {
            array[i] = Component.literal(StringUtils.repeat(" ", (i + 1) * 2));
        }
    });
    private List<Either<ResourceLocation, TextureAtlasSprite>> sprites = new ArrayList<>();
    private final MutableComponent component;
    
    public JojoTextComponentWrapper(MutableComponent component) {
        this.component = component;
    }
    
    
    public JojoTextComponentWrapper setStoryPartSprite(StoryPart storyPart) {
        return addSprite(storyPart != null ? storyPart.getSprite() : null);
    }
    
    public JojoTextComponentWrapper addSprite(ResourceLocation sprite) {
        sprites.add(Either.left(sprite));
        return this;
    }
    
    public JojoTextComponentWrapper addSprite(TextureAtlasSprite sprite) {
        sprites.add(Either.right(sprite));
        return this;
    }
    
    // FIXME fix the icon not rendering if any line from the tooltip is wrapped
    public void tooltipRenderExtra(PoseStack matrixStack, float x, float y) {
        for (Either<ResourceLocation, TextureAtlasSprite> sprite : sprites) {
            float spriteX = x - 1;
            sprite
            .ifLeft(texLocation -> {
                GuiDraw.bind(texLocation);
                BlitFloat.blitFloat(matrixStack, spriteX, y, 0, 0, 8, 8, 8, 8);
            })
            .ifRight(atlasSprite -> {
                GuiDraw.bind(atlasSprite.atlasLocation());
                BlitFloat.blitFloat(matrixStack, spriteX, y, 0, 8, 8, atlasSprite);
            });
            x += 10;
        }
    }
    
    @Override
    public <T> Optional<T> visit(FormattedText.IStyledTextAcceptor<T> pAcceptor, Style pStyle) {
        if (!sprites.isEmpty()) {
            int index = Math.min(sprites.size(), SPRITE_OFFSET.length) - 1;
            SPRITE_OFFSET[index].visit(pAcceptor, pStyle);
        }
        return component.visit(pAcceptor, pStyle);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ITextAcceptor<T> pAcceptor) {
        if (!sprites.isEmpty()) {
            int index = Math.min(sprites.size(), SPRITE_OFFSET.length) - 1;
            SPRITE_OFFSET[index].visit(pAcceptor);
        }
        return component.visit(pAcceptor);
    }
    

    @Override
    public Style getStyle() {
        return component.getStyle();
    }

    @Override
    public String getContents() {
        return component.getContents();
    }

    @Override
    public List<Component> getSiblings() {
        return component.getSiblings();
    }

    @Override
    public MutableComponent plainCopy() {
        return component.plainCopy();
    }

    @Override
    public MutableComponent copy() {
        JojoTextComponentWrapper copy = new JojoTextComponentWrapper(component.copy());
        copy.sprites.addAll(this.sprites);
        return copy;
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return component.getVisualOrderText();
    }

    @Override
    public MutableComponent setStyle(Style pStyle) {
        return component.setStyle(pStyle);
    }

    @Override
    public MutableComponent append(Component pSibling) {
        return component.append(pSibling);
    }

    
    @Override
    public String getString() {
        return component.getString();
    }
    
    @Override
    public String getString(int pMaxLength) {
        return component.getString(pMaxLength);
    }

}
