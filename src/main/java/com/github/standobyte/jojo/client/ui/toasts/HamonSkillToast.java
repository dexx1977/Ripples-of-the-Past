package com.github.standobyte.jojo.client.ui.toasts;

import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import java.util.List;

import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

@SuppressWarnings("deprecation")
public class HamonSkillToast implements Toast {
    private static final Component NAME = Component.translatable("hamon_skill.toast.title");
    private final Type type;
    private final Component description;
    private final List<AbstractHamonSkill> skills = Lists.newArrayList();
    private long lastChanged;
    private boolean changed;

    private HamonSkillToast(Type type, AbstractHamonSkill skill) {
        this.type = type;
        this.description = Component.translatable("hamon_skill.toast." + type.skillType + "description", 
                Component.keybind("jojo.key.hamon_skills_window").withStyle(ChatFormatting.BOLD));
        this.skills.add(skill);
    }

    @Override
    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastGui, long delta) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        if (changed) {
            lastChanged = delta;
            changed = false;
        }

        if (skills.isEmpty()) {
            return Toast.Visibility.HIDE;
        } else {
            Minecraft mc = toastGui.getMinecraft();
            GuiDraw.bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            GuiDraw.blit(matrixStack, 0, 0, 0, 32, 160, 32);
            GuiDraw.drawString(matrixStack, mc.font, NAME, 30.0F, 7.0F, -11534256);
            GuiDraw.drawString(matrixStack, mc.font, description, 30.0F, 18.0F, -16777216);
            AbstractHamonSkill skill = skills.get((int)(delta / Math.max(1L, 5000L / (long)skills.size()) % (long)skills.size()));
            TextureAtlasSprite textureAtlasSprite = CustomResources.getHamonSkillSprites().getSprite(skill);
            GuiDraw.bind(textureAtlasSprite.atlasLocation());
            GuiDraw.blit(matrixStack, 8, 8, 0, 16, 16, textureAtlasSprite);
            return delta - this.lastChanged >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
    }

    protected void addAction(AbstractHamonSkill skill) {
        if (skills.add(skill)) {
            changed = true;
        }
    }

    public static void addOrUpdate(ToastComponent toastGui, Type type, AbstractHamonSkill skill) {
        HamonSkillToast toast = toastGui.getToast(HamonSkillToast.class, type);
        if (toast == null) {
            toastGui.addToast(new HamonSkillToast(type, skill));
        } else {
            toast.addAction(skill);
        }

    }

    @Override
    public Type getToken() {
        return type;
    }

    public static enum Type {
        STRENGTH("strength."),
        CONTROL("control."),
        TECHNIQUE("technique.");
        
        private final String skillType;
        
        private Type(String skillType) {
            this.skillType = skillType;
        }
    }
}
