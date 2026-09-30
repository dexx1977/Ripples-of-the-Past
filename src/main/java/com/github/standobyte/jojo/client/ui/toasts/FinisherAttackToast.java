package com.github.standobyte.jojo.client.ui.toasts;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;

public class FinisherAttackToast extends ActionToast {
    
    protected FinisherAttackToast(ResourceLocation actionIcon, ResourceLocation powerTypeIcon) {
        super(SpecialToastType.FINISHER_HEAVY_ATTACK, actionIcon, powerTypeIcon);
    }
    
    private static final int FINISHER_BAR_TIME = 2000;
    @Override
    protected void renderIcon(PoseStack matrixStack, ToastComponent toastGui, int timeMs) {
        if (timeMs > TIME_MS - FINISHER_BAR_TIME) {
            toastGui.getMinecraft().getTextureManager().bind(ActionsOverlayGui.OVERLAY_LOCATION);
            GuiDraw.blit(matrixStack, 7, 7, 132, 216, 18, 18);
        }
        else {
            int actionRotationTime = timeMs * TIME_MS / (TIME_MS - FINISHER_BAR_TIME);
            super.renderIcon(matrixStack, toastGui, actionRotationTime);
        }
    }
    
    public static enum SpecialToastType implements IActionToastType {
        FINISHER_HEAVY_ATTACK("stand.attack.heavy_finisher");
        
        private final String name;
        
        private SpecialToastType(String name) {
            this.name = name;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public Toast createToast(ResourceLocation actionIcon, ResourceLocation powerTypeIcon) {
            return new FinisherAttackToast(actionIcon, powerTypeIcon);
        }
    }
}
