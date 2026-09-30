package com.github.standobyte.jojo.client.ui.actionshud.hotbar;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@SuppressWarnings("deprecation")
public class HotbarRenderer {
    public static final ResourceLocation HOTBAR_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/overlay_hotbar.png");
    
    public static final int EDGE_EXTRA_WIDTH = 15;
    
    public static void renderHotbar(PoseStack matrixStack, Minecraft mc, int x, int y, int slots, float alpha) {
        if (slots <= 0) return;
        GuiDraw.bind(HOTBAR_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        
        HotbarTexPosition texPos = HotbarTexPosition.getHotbarFromSlotsCount(slots);
        GuiDraw.blit(matrixStack, 
                x - EDGE_EXTRA_WIDTH + 1, y - EDGE_EXTRA_WIDTH + 1, 
                texPos.texX - EDGE_EXTRA_WIDTH, texPos.texY - EDGE_EXTRA_WIDTH, 
                slots * 20 + EDGE_EXTRA_WIDTH * 2, 20 + EDGE_EXTRA_WIDTH * 2, 
                512, 512);
        
    }
    
    public static void renderHotbar(PoseStack matrixStack, Minecraft mc, float x, float y, int slots, float alpha) {
        GuiDraw.bind(HOTBAR_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        
        HotbarTexPosition texPos = HotbarTexPosition.getHotbarFromSlotsCount(slots);
        GuiDraw.blit(matrixStack, 
                (int) x - EDGE_EXTRA_WIDTH + 1, (int) y - EDGE_EXTRA_WIDTH + 1, 
                texPos.texX - EDGE_EXTRA_WIDTH, texPos.texY - EDGE_EXTRA_WIDTH, 
                slots * 20 + EDGE_EXTRA_WIDTH * 2, 20 + EDGE_EXTRA_WIDTH * 2, 
                512, 512);
    }
    
    public static void renderFoldingHotbar(PoseStack matrixStack, Minecraft mc, float x, float y, HotbarFold hotbarFold, float alpha) {
        int slotsCount = hotbarFold.getSlotsCount();
        if (slotsCount <= 0) return;
        GuiDraw.bind(HOTBAR_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        
        HotbarTexPosition texPos = HotbarTexPosition.getHotbarFromSlotsCount(slotsCount);
        hotbarFold.renderSlots(slot -> {
            float width = slot.getFrameRenderedWidth();
            if (width > 0) {
                BlitFloat.blitFloat(matrixStack, 
                        x + slot.getFrameRenderedLeftEdge() - EDGE_EXTRA_WIDTH + 1, y - EDGE_EXTRA_WIDTH + 1, 
                        texPos.texX + slot.getFrameRenderedTexX() - EDGE_EXTRA_WIDTH, texPos.texY - EDGE_EXTRA_WIDTH, 
                        width, 20 + EDGE_EXTRA_WIDTH * 2, 
                        512, 512);
            }
        });
    }
    
    private static enum HotbarTexPosition {
        _1(405, 65),
        _2(385, 115),
        _3(365, 165),
        _4(345, 215),
        _5(325, 265),
        _6(305, 315),
        _7(285, 365),
        _8(265, 415),
        _9(245, 465),
        _10(15, 465),
        _11(15, 415),
        _12(15, 365),
        _13(15, 315),
        _14(15, 265),
        _15(15, 215),
        _16(15, 165),
        _17(15, 115),
        _18(15, 65),
        _19(15, 15);
        
        private final int texX;
        private final int texY;
        
        private HotbarTexPosition(int texX, int texY) {
            this.texX = texX;
            this.texY = texY;
        }
        
        static HotbarTexPosition getHotbarFromSlotsCount(int slots) {
            HotbarTexPosition[] values = values();
            return values[Mth.clamp(slots - 1, 0, values.length - 1)];
        }
    }
    
    public static void renderSlotSelection(PoseStack matrixStack, Minecraft mc, float slotX, float slotY, float hotbarAlpha, boolean greenSelection) {
        GuiDraw.bind(HOTBAR_LOCATION);
        if (greenSelection) {
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, hotbarAlpha);
        }
        else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hotbarAlpha);
        }
        BlitFloat.blitFloat(matrixStack, slotX - 28, slotY - 28, 440, 0, 72, 72, 512, 512);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hotbarAlpha);
    }
}
