package com.github.standobyte.jojo.client.ui.hud;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers the mod's HUD overlays.
 *
 * <p>1.16.5 hooked RenderGameOverlayEvent and checked the element type inside each
 * handler; 1.20.1 asks mods to register named overlays relative to the vanilla ones.
 * The overlays keep the same layering as the old element checks: the losing-vision
 * effect sits above the helmet overlay, the stand arrow experience bar and the
 * carried turtle slot replace their vanilla counterparts position-wise, and the
 * multi-line overlay message sits above the subtitles. Handlers that used to
 * *cancel* a vanilla element (food/air/health/experience) live in
 * ClientEventHandler as RenderGuiOverlayEvent.Pre listeners.</p>
 */
@Mod.EventBusSubscriber(modid = JojoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class JojoHudOverlays {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HELMET.id(), "jojo_losing_vision",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ClientEventHandler.getInstance().renderLosingVisionOverlay(guiGraphics, partialTick);
                });
        event.registerAboveAll("jojo_ge_detector_data",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ClientEventHandler.getInstance().renderGEDetectorDataOverlay(guiGraphics);
                });
        event.registerAbove(VanillaGuiOverlay.SUBTITLES.id(), "jojo_multiline_message",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ClientEventHandler.getInstance().renderMultiLineOverlayMessage(guiGraphics, partialTick);
                });
        event.registerAbove(VanillaGuiOverlay.HELMET.id(), "jojo_out_of_breath_vignette",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ActionsOverlayGui.getInstance().renderOutOfBreathVignetteOverlay(guiGraphics, partialTick);
                });
        // the action HUD drew its bars at the start of the vanilla overlay pass and
        // its text near the end, which the below/above anchors reproduce
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "jojo_actions_hud",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ActionsOverlayGui.getInstance().render(guiGraphics, partialTick, false);
                });
        event.registerAboveAll("jojo_actions_hud_text",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ActionsOverlayGui.getInstance().render(guiGraphics, partialTick, true);
                });
        event.registerAboveAll("jojo_actions_hud_post",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ActionsOverlayGui.getInstance().renderPost(guiGraphics, partialTick);
                });
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "jojo_carried_turtle_slot",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    GuiDraw.setGraphics(guiGraphics);
                    ClientEventHandler.getInstance().renderCarriedCocoJumboSlot(guiGraphics);
                });
    }
}
