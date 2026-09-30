package com.github.standobyte.jojo.client.ui.screen;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.item.CustomIconItem;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.Alignment;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.HudTextRender;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.PositionConfig;
import com.github.standobyte.jojo.client.ui.screen.widgets.ItemButton;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

public class ClientModSettingsScreen extends OptionsSubScreen {
    protected final ClientModSettings settings;
    protected final ClientModSettings.Settings settingsValues;

    public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings) {
        this(lastScreen, settings, Component.translatable("jojo.options.client.title"));
    }

    public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings, Component title) {
        super(lastScreen, lastScreen.getMinecraft().options, title);
        this.settings = settings;
        this.settingsValues = ClientModSettings.getSettingsReadOnly();
    }

    @Override
    protected void init() {
        addButtons();
    }

    protected void addButtons() {
        int i = 0;
        
        BooleanSetting characterVoiceLines = new BooleanSetting(settings, 
                Component.translatable("jojo.config.client.characterVoiceLines"), 
                Component.translatable("jojo.config.client.characterVoiceLines.tooltip")
                ) {
            @Override public Boolean get() { return settingsValues.characterVoiceLines; }
            @Override public void set(Boolean value) { settingsValues.characterVoiceLines = value; }
        };
        addRenderableWidget(characterVoiceLines.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
        
        BooleanSetting menacingParticles = new BooleanSetting(settings, 
                Component.translatable("jojo.config.client.menacingParticles"), 
                Component.translatable("jojo.config.client.menacingParticles.tooltip")
                ) {
            @Override public Boolean get() { return settingsValues.menacingParticles; }
            @Override public void set(Boolean value) { settingsValues.menacingParticles = value; }
        };
        addRenderableWidget(menacingParticles.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
        
        i += (i % 2 == 1) ? 3 : 2;
        
        addRenderableWidget(Button.builder(Component.translatable("jojo.options.client.hud"), button -> minecraft.setScreen(new HudSettings(this, settings, button.getMessage())))
                        .pos(calcButtonX(i), calcButtonY(i++) + 6)
                        .size(150, 20).build());
        
        addRenderableWidget(Button.builder(Component.translatable("jojo.options.client.stand"), button -> minecraft.setScreen(new StandSettings(this, settings, button.getMessage())))
                        .pos(calcButtonX(i), calcButtonY(i++) + 6)
                        .size(150, 20).build());
        
        addRenderableWidget(Button.builder(Component.translatable("jojo.options.client.hamon"), button -> minecraft.setScreen(new HamonSettings(this, settings, button.getMessage())))
                        .pos(calcButtonX(i), calcButtonY(i++) + 6)
                        .size(150, 20).build());
        
        addRenderableWidget(Button.builder(Component.translatable("jojo.options.client.vampirism"), button -> minecraft.setScreen(new VampirismSettings(this, settings, button.getMessage())))
                        .pos(calcButtonX(i), calcButtonY(i++) + 6)
                        .size(150, 20).build());
        
        addBackButton(CommonComponents.GUI_DONE, i);
    }
    
    protected void addBackButton(Component text, int buttonsAdded) {
        buttonsAdded += 2;
        if (buttonsAdded % 2 == 1) {
            ++buttonsAdded;
        }

        addRenderableWidget(Button.builder(text, button -> minecraft.setScreen(lastScreen))
                        .pos(this.width / 2 - 100, calcButtonY(buttonsAdded))
                        .size(200, 20).build());
    }
    
    
    
    public static class HudSettings extends ClientModSettingsScreen {

        public HudSettings(Screen lastScreen, ClientModSettings settings, Component title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            EnumSetting<PositionConfig> barsPosition = new EnumSetting<PositionConfig>(settings, 
                    Component.translatable("jojo.config.client.barsPosition"), 
                    Component.translatable("jojo.config.client.barsPosition.tooltip"), 
                    PositionConfig.class) {
                @Override public PositionConfig get() { return settingsValues.barsPosition; }
                @Override public void set(PositionConfig value) { settingsValues.barsPosition = value; }
            };
            addRenderableWidget(barsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            
            EnumSetting<PositionConfig> hotbarsPosition = new EnumSetting<PositionConfig>(settings, 
                    Component.translatable("jojo.config.client.hotbarsPosition"), 
                    Component.translatable("jojo.config.client.hotbarsPosition.tooltip"), 
                    PositionConfig.class) {
                @Override public PositionConfig get() { return settingsValues.hotbarsPosition; }
                @Override public void set(PositionConfig value) { settingsValues.hotbarsPosition = value; }
            };
            addRenderableWidget(hotbarsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            
            EnumSetting<HudTextRender> hudNamesRender = new EnumSetting<HudTextRender>(settings, 
                    Component.translatable("jojo.config.client.hudNamesRender"), 
                    Component.translatable("jojo.config.client.hudNamesRender.tooltip"), 
                    HudTextRender.class) {
                @Override public HudTextRender get() { return settingsValues.hudTextRender; }
                @Override public void set(HudTextRender value) { settingsValues.hudTextRender = value; }
            };
            addRenderableWidget(hudNamesRender.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            
            BooleanSetting hudHotbarsFold = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.hudHotbarsFold"), 
                    Component.translatable("jojo.config.client.hudHotbarsFold.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.hudHotbarFold; }
                @Override public void set(Boolean value) { 
                    settingsValues.hudHotbarFold = value;
                    if (minecraft.player != null) {
                        for (PowerClassification power : PowerClassification.values()) {
                            IPower.getPowerOptional(minecraft.player, power).ifPresent(IPower::clUpdateHud);
                        }
                    }
                }
            };
            addRenderableWidget(hudHotbarsFold.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            
            BooleanSetting showLockedSlots = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.showLockedSlots"), 
                    Component.translatable("jojo.config.client.showLockedSlots.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.showLockedSlots; }
                @Override public void set(Boolean value) {
                    settingsValues.showLockedSlots = value;
                    if (minecraft.player != null) {
                        for (PowerClassification power : PowerClassification.values()) {
                            IPower.getPowerOptional(minecraft.player, power).ifPresent(IPower::clUpdateHud);
                        }
                    }
                }
            };
            addRenderableWidget(showLockedSlots.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            addBackButton(CommonComponents.GUI_BACK, i);
        }
        
    }
    
    public static class StandSettings extends ClientModSettingsScreen {

        public StandSettings(Screen lastScreen, ClientModSettings settings, Component title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            BooleanSetting resolveShaders = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.resolveShaders"), 
                    Component.translatable("jojo.config.client.resolveShaders.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.resolveShaders; }
                @Override public void set(Boolean value) { 
                    settingsValues.resolveShaders = value;
                    if (!value) {
                        ShaderEffectApplier.getInstance().stopResolveShader();
                    }
                }
            };
            addRenderableWidget(resolveShaders.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            
            BooleanSetting timeStopAnimation = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.timeStopAnimation"), 
                    Component.translatable("jojo.config.client.timeStopAnimation.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.timeStopAnimation; }
                @Override public void set(Boolean value) { settingsValues.timeStopAnimation = value; }
            };
            addRenderableWidget(timeStopAnimation.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            
            Setting<HumanoidArm> standSide = new EnumSetting<HumanoidArm>(settings, 
                    Component.translatable("jojo.config.client.standSide"), 
                    Component.translatable("jojo.config.client.standSide.tooltip"), 
                    HumanoidArm.class) {
                @Override public HumanoidArm get() { return settingsValues.broadcasted.standSide; }
                @Override public void set(HumanoidArm value) { settingsValues.broadcasted.standSide = value; }
            }
            .prefix("stand_")
            .setBroadcasted();
            addRenderableWidget(standSide.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            
            BooleanSetting standMotionTilt = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.standMotionTilt"), 
                    Component.translatable("jojo.config.client.standMotionTilt.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.standMotionTilt; }
                @Override public void set(Boolean value) { settingsValues.standMotionTilt = value; }
            };
            addRenderableWidget(standMotionTilt.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            BooleanSetting standOutline = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.standOutline"), 
                    Component.translatable("jojo.config.client.standOutline.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.standOutline; }
                @Override public void set(Boolean value) { settingsValues.standOutline = value; }
            };
            addRenderableWidget(standOutline.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            addBackButton(CommonComponents.GUI_BACK, i);
        }
        
    }
    
    public static class HamonSettings extends ClientModSettingsScreen {

        public HamonSettings(Screen lastScreen, ClientModSettings settings, Component title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            BooleanSetting thirdPersonHamonAura = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.thirdPersonHamonAura"), 
                    Component.translatable("jojo.config.client.thirdPersonHamonAura.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.thirdPersonHamonAura; }
                @Override public void set(Boolean value) { 
                    settingsValues.thirdPersonHamonAura = value;
                }
            };
            addRenderableWidget(thirdPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            BooleanSetting firstPersonHamonAura = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.firstPersonHamonAura"), 
                    Component.translatable("jojo.config.client.firstPersonHamonAura.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.firstPersonHamonAura; }
                @Override public void set(Boolean value) { 
                    settingsValues.firstPersonHamonAura = value;
                }
            };
            addRenderableWidget(firstPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            BooleanSetting hamonAuraBlur = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.hamonAuraBlur"), 
                    Component.translatable("jojo.config.client.hamonAuraBlur.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.hamonAuraBlur; }
                @Override public void set(Boolean value) { 
                    settingsValues.hamonAuraBlur = value;
                }
            };
            addRenderableWidget(hamonAuraBlur.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            addBackButton(CommonComponents.GUI_BACK, i);
        }
        
    }
    
    public static class VampirismSettings extends ClientModSettingsScreen {

        public VampirismSettings(Screen lastScreen, ClientModSettings settings, Component title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            BooleanSetting glowingEyes = new BooleanSetting(settings, 
                    Component.translatable("jojo.config.client.vampireGlowingEyes"), 
                    Component.translatable("jojo.config.client.vampireGlowingEyes.tooltip")
                    ) {
                @Override public Boolean get() { return settingsValues.broadcasted.vampireGlowingEyes; }
                @Override public void set(Boolean value) { 
                    settingsValues.broadcasted.vampireGlowingEyes = value;
                }
            };
            addRenderableWidget(glowingEyes.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
            
            addBackButton(CommonComponents.GUI_BACK, i);
        }
        
    }
    
    
    
    protected int calcButtonX(int i) {
        return this.width / 2 - 155 + i % 2 * 160;
    }
    
    protected int calcButtonY(int i) {
        return this.height / 6 - 12 + 24 * (i >> 1);
    }

    @Override
    public void removed() {
        settings.save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        PoseStack pMatrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        GuiDraw.drawCenteredString(pMatrixStack, font, title, width / 2, 15, 0xFFFFFF);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }
    
    
    
    protected static abstract class Setting<T> {
        protected boolean broadcast = false;
        
        public abstract T get();
        public abstract void set(T value);
        
        public Setting<T> setBroadcasted() {
            this.broadcast = true;
            return this;
        }
        
        public abstract Button createButton(int x, int y, int width, int height, Screen screen, int buttonI);
    }
    
    protected static abstract class BooleanSetting extends Setting<Boolean> {
        private final ClientModSettings settings;
        private final Component name;
        private final Component tooltip;
        
        public BooleanSetting(ClientModSettings settings, Component name, @Nullable Component tooltip) {
            this.settings = settings;
            this.name = name;
            this.tooltip = tooltip;
        }
        
        @Override
        public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
            return new ScrollingStringButton(
                    x, y, width, height,
                    CommonComponents.optionStatus(name, get()), 
                    button -> {
                        settings.editSettings(s -> {
                            set(!get());
                            button.setMessage(CommonComponents.optionStatus(name, get()));
                        }, broadcast);
                    },
                    (button, matrixStack, mouseX, mouseY) -> {
                        if (tooltip != null) {
                            GuiDraw.renderToolTip(matrixStack, tooltip, mouseX, mouseY);
                        }
                    })
                    .setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
        }
    }
    
    protected static abstract class EnumSetting<T extends Enum<T>> extends Setting<T> {
        private final ClientModSettings settings;
        private final Component name;
        private final Component tooltip;
        private final Class<T> enumClass;
        private String prefix = "jojo.config.client.option.";
        
        public EnumSetting(ClientModSettings settings, Component name, @Nullable Component tooltip, Class<T> enumClass) {
            this.settings = settings;
            this.name = name;
            this.enumClass = enumClass;
            this.tooltip = tooltip;
        }
        
        public EnumSetting<T> prefix(String prefix) {
            this.prefix += prefix;
            return this;
        }
        
        @Override
        public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
            return new ScrollingStringButton(
                    x, y, width, height,
                    Component.translatable("options.generic_value", name, getValueMessage(get())), 
                    button -> {
                        settings.editSettings(s -> {
                            T[] values = enumClass.getEnumConstants();
                            T val = get();
                            T nextVal = values[(val.ordinal() + 1) % values.length];
                            set(nextVal);
                            button.setMessage(Component.translatable("options.generic_value", name, getValueMessage(nextVal)));
                        }, broadcast);
                    },
                    (button, matrixStack, mouseX, mouseY) -> {
                        if (tooltip != null) {
                            GuiDraw.renderToolTip(matrixStack, tooltip, mouseX, mouseY);
                        }
                    })
                    .setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
        }
        
        private Component getValueMessage(T value) {
            return Component.translatable(prefix + value.name().toLowerCase());
        }
    }
    
    
    
    private static class ScrollingStringButton extends Button {
        private Alignment alignment = Alignment.LEFT;
        
        public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage,
                Button.OnPress pOnPress) {
            super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
        }
        
        public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Button.OnPress pOnPress,
                Tooltip pOnTooltip) {
            super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        }
        
        public ScrollingStringButton setAlignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            GuiDraw.bind(WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            int i = getYImage(isHovered());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            GuiDraw.blit(matrixStack, x, y, 0, 46 + i * 20, width / 2, height);
            GuiDraw.blit(matrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
            renderBg(guiGraphics, mc, mouseX, mouseY);
            int j = getFGColor();
            int textColor = j | Mth.ceil(alpha * 255.0F) << 24;
            
            renderScrollingString(matrixStack, font, getMessage(), 
                    x + 2, y, x + width - 2, y + height, 
                    textColor, isHovered(), alignment);
        }
        
        
        
        protected static void renderScrollingString(PoseStack matrixStack, Font font, Component text, 
                int x0, int y0, int x1, int y1, int color, boolean isHovered, Alignment alignment) {
            int textWidth = font.width(text);
            int y = (y0 + y1 - 9) / 2 + 1;
            int buttonWidth = x1 - x0;
            if (textWidth > buttonWidth) {
                if (isHovered) {
                    switch (alignment) {
                    case LEFT:
                        GuiDraw.drawString(matrixStack, font, text, x0, y, color);
                        break;
                    case RIGHT:
                        ClientUtil.drawRightAlignedString(matrixStack, font, text, x1, y, color);
                        break;
                    }
                }
                else {
                    int scrollMax = textWidth - buttonWidth;
                    double $$12 = (double)Util.getMillis() / 1000.0;
                    double $$13 = Math.max((double)scrollMax * 0.5, 3.0);
                    double $$14 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * $$12 / $$13)) / 2.0 + 0.5;
                    double scrollAmount = Mth.lerp($$14, 0.0, (double)scrollMax);
                    ClientUtil.enableGlScissor(x0, y0, x1 - x0, y1 - y0);
                    font.drawShadow(matrixStack, text, x0 - (int)scrollAmount, y, color);
                    ClientUtil.disableGlScissor();
                }
            } else {
                GuiDraw.drawCenteredString(matrixStack, font, text, (x0 + x1) / 2, y, color);
            }
        }
    }
    
    
    
    
    public static Button addSettingsButton(Screen optionsScreen, List<AbstractWidget> otherModdedButtons) {
        final int minY = optionsScreen.height / 6 + 48 - 6;
        final int maxY = minY + 72;
        
        final int minX1 = 0;
        final int maxX1 = optionsScreen.width / 2 - 155 - 20 - 5;
        final int minX2 = optionsScreen.width / 2 + 160;
        final int maxX2 = optionsScreen.width - 20;
        
        final int minX3 = optionsScreen.width / 2 - 155;
        final int maxX3 = minX3 + 290;
        final int y3 = maxY + 24;
        
        int[] buttonPos = null;
        
        // try placing the button to the right side
        for (int x = minX2; x <= maxX2 && buttonPos == null; x += 25) {
            int y = maxY;
            if (ModList.get().isLoaded("essential")) y -= 24; // for fuck's sake
            for (; y >= minY && buttonPos == null; y -= 24) {
                buttonPos = noOverlapPos(otherModdedButtons, x, y);
            }
        }
        // ...or to the left side
        if (buttonPos == null) {
            for (int x = maxX1; x >= minX1 && buttonPos == null; x -= 25) {
                for (int y = maxY; y >= minY && buttonPos == null; y -= 24) {
                    buttonPos = noOverlapPos(otherModdedButtons, x, y);
                }
            }
        }
        // ...or below the vanilla options
        if (buttonPos == null) {
            for (int x = minX3; x <= maxX3 && buttonPos == null; x += 29) {
                buttonPos = noOverlapPos(otherModdedButtons, x, y3);
            }
        }
        // ...how many new buttons are there?? fuck it, just put it at the "Done" button
        if (buttonPos == null) {
            buttonPos = new int[] { optionsScreen.width / 2 + 110, optionsScreen.height / 6 + 168 };
        }
        
        Component tooltip = Component.translatable("jojo.options.client.title");
        return new ItemButton(buttonPos[0], buttonPos[1], 20, 20, 
                CustomIconItem.makeIconItem(CustomIconItem.CustomModelIcon.MOD_LOGO),
                button -> {
                    optionsScreen.getMinecraft().setScreen(new ClientModSettingsScreen(optionsScreen, ClientModSettings.getInstance()));
                },
                (button, matrixStack, mouseX, mouseY) -> {
                    GuiDraw.renderToolTip(matrixStack, tooltip, mouseX, mouseY);
                },
                tooltip);
    }
    
    @Nullable
    private static int[] noOverlapPos(List<AbstractWidget> buttonsList, int x, int y) {
        int x2 = x + 20;
        int y2 = y + 20;
        return buttonsList.stream().anyMatch(button -> {
            return button.x < x2 && button.x + button.getWidth() > x && button.y < y2 && button.y + button.getHeight() > y;
        }) ? null : new int[] { x, y };
    }
}
