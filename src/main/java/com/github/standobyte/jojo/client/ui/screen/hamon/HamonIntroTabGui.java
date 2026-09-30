package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_HEIGHT;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_THIN_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_UPPER_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_WIDTH;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.actionshud.hotbar.HotbarRenderer;
import com.github.standobyte.jojo.client.ui.screen.widgets.HideScreenPartToggleBox;
import com.github.standobyte.jojo.client.ui.screen.widgets.HideScreenPartToggleBox.Direction;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class HamonIntroTabGui extends HamonTabGui {
    private final MutableComponent aboutName;
    private final List<FormattedCharSequence> aboutText;
    private final MutableComponent breathName;
    private final List<FormattedCharSequence> breathTextBar;
    private final List<FormattedCharSequence> breathTextEnergy;
    private final List<FormattedCharSequence> breathTextAbility;
    private final List<FormattedCharSequence> breathTextStabilityTitle;
    private final List<FormattedCharSequence> breathTextStability;
    private final List<FormattedCharSequence> breathTextStability2;
    private final List<FormattedCharSequence> statsTransitionText;
    private int y1;
    private int y2;
    private int y3;
    private int bar2RenderTime = -1;
    private int bar3RenderTime = -1;
    private HideScreenPartToggleBox breathStabilityInfoToggle;
    
    HamonIntroTabGui(Minecraft minecraft, HamonScreen screen, String title) {
        super(minecraft, screen, title, -1, 1);
        int textWidth = WINDOW_WIDTH - 30;
        aboutName = Component.translatable("hamon.intro.about.name");
        aboutText = minecraft.font.split(Component.translatable("hamon.intro.about.text"), textWidth);
        breathName = Component.translatable("hamon.intro.breath.name");
        breathTextBar = minecraft.font.split(Component.translatable("hamon.intro.breath.text1", 
                Component.translatable("hamon.intro.breath.text1.underlined").withStyle(ChatFormatting.UNDERLINE)), textWidth);
        breathTextEnergy = minecraft.font.split(Component.translatable("hamon.intro.breath.text2", 
                Component.translatable("hamon.intro.breath.text2.underlined").withStyle(ChatFormatting.UNDERLINE)), textWidth);
        breathTextAbility = minecraft.font.split(Component.translatable("hamon.intro.breath.text3", 
                Component.translatable("hamon.intro.breath.text3.underlined").withStyle(ChatFormatting.UNDERLINE)), textWidth);
        breathTextStabilityTitle = minecraft.font.split(Component.translatable("hamon.intro.breath.stability_hidden")
                .withStyle(ChatFormatting.ITALIC), textWidth);
        breathTextStability = minecraft.font.split(Component.translatable("hamon.intro.breath.text4", 
                Component.translatable("hamon.intro.breath.text4.underlined").withStyle(ChatFormatting.UNDERLINE)), textWidth);
        breathTextStability2 = minecraft.font.split(Component.translatable("hamon.intro.breath.text5"), textWidth);
        statsTransitionText = minecraft.font.split(Component.translatable("hamon.intro.stats_transition"), textWidth);
    }
    
    @Override
    public void addButtons() {
        addButton(breathStabilityInfoToggle = new HideScreenPartToggleBox(
                screen.windowPosX() + 13, -1, Direction.DOWN, screen));
    }
    
    @Override
    protected void drawText(PoseStack matrixStack) {
        int textX = intScrollX + 5;
        int textY = intScrollY + 6;
        drawString(matrixStack, minecraft.font, aboutName, textX - 3, textY, 0xFFFFFF);
        
        textY += 2;
        for (int i = 0; i < aboutText.size(); i++) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, aboutText.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        
        textY += 15;
        drawString(matrixStack, minecraft.font, breathName, textX - 3, textY, 0xFFFFFF);
        
        textY += 2;
        for (FormattedCharSequence line : breathTextBar) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, line, (float) textX, (float) textY, 0xFFFFFF);
        }
        y1 = textY + 11;
        
        textY += 24;
        for (FormattedCharSequence line : breathTextEnergy) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, line, (float) textX, (float) textY, 0xFFFFFF);
        }
        
        for (FormattedCharSequence line : breathTextAbility) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, line, (float) textX, (float) textY, 0xFFFFFF);
        }
        y2 = textY + 36;
        
        textY += 49;
        for (FormattedCharSequence line : breathTextStabilityTitle) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, line, (float) textX + 14, (float) textY, 0xFFFFFF);
        }
        breathStabilityInfoToggle.getWidgetExtension().setY(screen.windowPosY() + textY + 15 - intScrollY);
        if (breathStabilityInfoToggle.getState()) {
            textY += 3;
            for (FormattedCharSequence line : breathTextStability) {
                textY += minecraft.font.lineHeight;
                minecraft.font.draw(matrixStack, line, (float) textX, (float) textY, 0xFFFFFF);
            }
            y3 = textY + 12;
            
            textY += 14;
            for (FormattedCharSequence line : breathTextStability2) {
                textY += minecraft.font.lineHeight;
                minecraft.font.draw(matrixStack, line, (float) textX, (float) textY, 0xFFFFFF);
            }
        }

        textY += 15;
        for (FormattedCharSequence line : statsTransitionText) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, line, (float) textX, (float) textY, 0xFFFFFF);
        }
        
        setMaxY(textY + 15 - intScrollY);
    }
    
    @Override
    protected void drawActualContents(HamonScreen screen, PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        minecraft.textureManager.bind(ActionsOverlayGui.OVERLAY_LOCATION);
        float ticks = screen.tickCount + partialTick;
        int x = intScrollX + 5;
        
        // empty energy bar
        renderEnergyBar(matrixStack, x, y1, 1, 0);
        
        // charging energy bar
        boolean bar2InView = y2 > -7 && y2 < 199;
        if (bar2RenderTime < 0 && bar2InView) {
            bar2RenderTime = screen.tickCount;
        }
        if (bar2RenderTime >= 0) {
            float barTicks = (ticks - bar2RenderTime) % 100;
            renderEnergyBar(matrixStack, x, y2, 1, Mth.clamp((barTicks - 20F) / 60F, 0F, 1F));
        }
        
        if (breathStabilityInfoToggle.getState()) {
            // energy bar with lower breath stability charging not as fast
            boolean bar3InView = y3 > -7 && y3 < 199;
            if (bar3RenderTime < 0 && bar3InView) {
                bar3RenderTime = screen.tickCount;
            }
            if (bar3RenderTime >= 0) {
                float barTicks = (ticks - bar3RenderTime) % 750;
                float fillStab = 0.4F + 0.6F * barTicks / 720;
                float fillEnergy = Mth.clamp((barTicks - 20) / 60, 0, fillStab);
                renderEnergyBar(matrixStack, x, y3, fillStab, fillEnergy);
            }
        }
        
        renderHamonBreathIcon(matrixStack, intScrollX + 98, y2 - 21);
        
        RenderSystem.disableBlend();
    }
    
    @SuppressWarnings("deprecation")
    private void renderEnergyBar(PoseStack matrixStack, int x, int y, float fillStab, float fillEnergy) {
        float[] hamonRGB = ClientUtil.rgb(HamonPowerType.COLOR);
        blit(matrixStack, x, y, 0, 128, 202, 8);
        RenderSystem.color4f(hamonRGB[0], hamonRGB[1], hamonRGB[2], 0.4F);
        blit(matrixStack, x + 1, y + 1, 1, 161, (int) (200 * fillStab), 6);
        
        RenderSystem.color4f(hamonRGB[0], hamonRGB[1], hamonRGB[2], 1.0F);
        blit(matrixStack, x + 1, y + 1, 1, 161, (int) (200 * fillEnergy), 6);
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(matrixStack, x + 1, y + 1, 0, 145, 200, 6);
    }
    
    private void renderHamonBreathIcon(PoseStack matrixStack, int x, int y) {
        minecraft.getTextureManager().bind(ActionsOverlayGui.OVERLAY_LOCATION);
        blit(matrixStack, x - 15, y - 1, 236, 128, 9, 16);
        HotbarRenderer.renderHotbar(matrixStack, minecraft, x - 3, y - 3, 1, 1);
        blit(matrixStack, x - 17, y - 17, 390, 50, 50, 50, 512, 512);
        ResourceLocation actionIcon = ModHamonActions.HAMON_BREATH.get().getIconTexture(null);
        minecraft.getTextureManager().bind(actionIcon);
        blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
        HotbarRenderer.renderSlotSelection(matrixStack, minecraft, x, y, 1, false);
    }

    @Override
    void drawIcon(PoseStack matrixStack, int windowX, int windowY, ItemRenderer itemRenderer) {
        minecraft.getTextureManager().bind(ModPowers.HAMON.get().getIconTexture(null));
        int x = tabPositioning.getIconX(windowX, index, WINDOW_WIDTH);
        int y = tabPositioning.getIconY(windowY, index, WINDOW_HEIGHT);
        blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
    }
    
    @Override
    void drawToolTips(PoseStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        if (breathStabilityInfoToggle.visible && breathStabilityInfoToggle.isMouseOver(
                mouseX + screen.windowPosX() + WINDOW_THIN_BORDER, 
                mouseY + screen.windowPosY() + WINDOW_UPPER_BORDER)) {
            breathStabilityInfoToggle.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
    
    
    
    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton, boolean mouseInsideWindow) {
        return false;
    }
    
    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return false;
    }
    
    @Override
    void updateTab() {}
}
