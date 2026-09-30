package com.github.standobyte.jojo.client.ui.render;

import com.github.standobyte.jojo.client.ui.render.AbstractGui;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * Drawing helpers for the mod's screens, HUD and toasts.
 *
 * <p>1.16.5 drew GUIs with static {@code AbstractGui} helpers and by binding a
 * texture, and passed a {@link PoseStack} around. 1.20.1 replaced that with
 * {@link GuiGraphics}, which owns the pose stack, the vertex buffers and the
 * texture per draw call. Rewriting every screen and HUD renderer to thread a
 * {@code GuiGraphics} through its own method signatures would touch a large amount
 * of drawing code, so the current {@code GuiGraphics} is published here once per
 * render entry point (screen, toast, HUD overlay) and these helpers forward to it.
 * GUI drawing is single threaded and always happens inside such an entry point,
 * which is what makes this safe; the passed {@link PoseStack} is the one owned by
 * that {@code GuiGraphics}.</p>
 */
public final class GuiDraw {
    /** As in 1.16.5: the vanilla widget texture. */
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");

    @Nullable
    private static GuiGraphics graphics;
    @Nullable
    private static ResourceLocation texture;

    private GuiDraw() {}

    /** Called by every render entry point before it draws. */
    public static void setGraphics(GuiGraphics guiGraphics) {
        graphics = guiGraphics;
    }

    @Nullable
    public static GuiGraphics graphics() {
        return graphics;
    }

    public static PoseStack pose() {
        return graphics.pose();
    }

    /** Records the texture the following blits use, and binds it like 1.16.5 did. */
    public static void bind(ResourceLocation texture) {
        GuiDraw.texture = texture;
        GuiDraw.bind(texture);
    }

    public static ResourceLocation boundTexture() {
        return texture;
    }

    public static void fill(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y2, color);
    }

    public static void fill(PoseStack poseStack, int x1, int y1, int x2, int y2, int z, int color) {
        graphics.fill(x1, y1, x2, y2, z, color);
    }

    public static void blit(PoseStack poseStack, int x, int y, int u, int v, int width, int height) {
        graphics.blit(texture, x, y, u, v, width, height);
    }

    /** 1.16.5 took the source texture size as well. */
    public static void blit(PoseStack poseStack, int x, int y, int u, int v, int width, int height, int texWidth, int texHeight) {
        graphics.blit(texture, x, y, 0, (float) u, (float) v, width, height, texWidth, texHeight);
    }

    /** 1.16.5 had a z offset in front of the source coordinates. */
    public static void blit(PoseStack poseStack, int x, int y, int blitOffset, int u, int v, int width, int height, int texWidth, int texHeight) {
        graphics.blit(texture, x, y, blitOffset, (float) u, (float) v, width, height, texWidth, texHeight);
    }

    /** The 1.16.5 form that took float source offsets. */
    public static void blit(PoseStack poseStack, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        graphics.blit(texture, x, y, 0, u, v, width, height, texWidth, texHeight);
    }

    /** The 1.16.5 form that blitted an atlas sprite. */
    public static void blit(PoseStack poseStack, int x, int y, int blitOffset, int width, int height, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
        graphics.blit(x, y, blitOffset, width, height, sprite);
    }

    /**
     * The 1.16.5 form with separate destination and source sizes. 1.20.1 draws a
     * source-sized region, and every call in this mod passes matching sizes, so the
     * destination size is used for both.
     */
    public static void blit(PoseStack poseStack, int x, int y, int width, int height, float u, float v,
            int uWidth, int vHeight, int texWidth, int texHeight) {
        graphics.blit(texture, x, y, 0, u, v, width, height, texWidth, texHeight);
    }

    public static void drawString(PoseStack poseStack, Font font, String text, float x, float y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    public static void drawString(PoseStack poseStack, Font font, Component text, float x, float y, int color) {
        graphics.drawString(font, text, (int) x, (int) y, color, true);
    }

    public static void drawString(PoseStack poseStack, Font font, FormattedCharSequence text, float x, float y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    public static void drawCenteredString(PoseStack poseStack, Font font, Component text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, Font font, String text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, color);
    }

    public static void renderTooltip(PoseStack poseStack, Font font, List<? extends FormattedCharSequence> lines, int mouseX, int mouseY) {
        graphics.renderTooltip(font, lines, mouseX, mouseY);
    }

    public static void renderTooltip(PoseStack poseStack, Font font, Component text, int mouseX, int mouseY) {
        graphics.renderTooltip(font, text, mouseX, mouseY);
    }

    public static void renderTooltip(PoseStack poseStack, Font font, ItemStack stack, int mouseX, int mouseY) {
        graphics.renderTooltip(font, stack, mouseX, mouseY);
    }

    /** The mod's 1.16.5 helpers used the vanilla Screen methods with this shape. */
    public static void renderToolTip(PoseStack poseStack, Component text, int mouseX, int mouseY) {
        graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, text, mouseX, mouseY);
    }

    public static void renderToolTip(PoseStack poseStack, List<? extends FormattedCharSequence> lines, int mouseX, int mouseY) {
        graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, lines, mouseX, mouseY);
    }

    public static void renderToolTipWrapped(PoseStack poseStack, List<? extends FormattedText> lines, int mouseX, int mouseY) {
        graphics.renderComponentTooltip(net.minecraft.client.Minecraft.getInstance().font, lines, mouseX, mouseY, ItemStack.EMPTY);
    }

    public static void renderTooltipWrapped(PoseStack poseStack, Font font, List<? extends FormattedText> lines, int mouseX, int mouseY) {
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY, ItemStack.EMPTY);
    }

    public static void pushMatrix() {
        graphics.pose().pushPose();
    }

    public static void popMatrix() {
        graphics.pose().popPose();
    }

    public static void enableScissor(int x1, int y1, int x2, int y2) {
        graphics.enableScissor(x1, y1, x2, y2);
    }

    public static void disableScissor() {
        graphics.disableScissor();
    }
}
