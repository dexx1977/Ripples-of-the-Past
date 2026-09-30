package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import net.minecraft.client.gui.GuiGraphics;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;
import com.github.standobyte.jojo.capability.entity.LifeformsMetMobs;
import com.github.standobyte.jojo.capability.entity.LifeformsUIState;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ui.screen.ScreenCloseMode;
import com.github.standobyte.jojo.client.ui.screen.WasdAllowingScreen;
import com.github.standobyte.jojo.client.ui.screen.widgets.ImageVanillaButton;
import com.github.standobyte.jojo.client.ui.screen.widgets.RadioButtonsList;
import com.github.standobyte.jojo.client.ui.tooltip.CustomTooltipRender;
import com.github.standobyte.jojo.client.ui.tooltip.ITooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.IconTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.MultiTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.TextTooltipLine;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClAllGELifeformsButtonPacket;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntityTypeToInstance;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.github.standobyte.jojo.util.mc.MCUtil;

public abstract class ChooseLifeformScreen extends WasdAllowingScreen {
    protected LifeformsUIState playerUISettings;
    protected LifeformsMetMobs unlockedMobsData;
    
    // TODO save those on server instead
    private static String savedSearchFilter = "";
    private static FilterMode savedFilterMode = FilterMode.ALL;

    RadioButtonsList<FilterMode> filterList;
    private EditBox searchField;
    private Button clearSearchFieldButton;
    
    public static void openWindowOnClick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            ViewMode type = ClientModSettings.getSettingsReadOnly().viewModeGE;
            if (type == null) {
                type = ViewMode.GRID;
                ClientModSettings.edit(s -> s.viewModeGE = ViewMode.GRID);
            }
            Screen screen;
            switch (type) {
            case GRID:
                screen = new ChooseLifeformGridScreen(InputHandler.lastActionKey);
                mc.setScreen(screen);
                break;
            case LIST:
                screen = new ChooseLifeformListScreen(InputHandler.lastActionKey);
                mc.setScreen(screen);
                break;
            }
        }
    }
    
    public enum ViewMode {
        GRID,
        LIST
    }
    
    public enum FilterMode {
        ALL(Component.translatable("jojo.ui.lifeform_ui_mode.all")),
        FAVORITES(Component.translatable("jojo.ui.lifeform_ui_mode.favs")),
        NEW(Component.translatable("jojo.ui.lifeform_ui_mode.new"));
        
        public final Component uiName;
        private FilterMode(Component uiName) {
            this.uiName = uiName;
        }
    }
    
    
    
    public ChooseLifeformScreen(KeyMapping keyHeld) {
        super(Component.empty());
        this.keyHeld = keyHeld;
    }
    
    
    @Override
    protected void init() {
        super.init();
        PlayerUtilCap playerData = minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve().get();
        playerUISettings = playerData.getGELifeformsUIState();
        unlockedMobsData = playerData.getMetMobs();
    }
    
    protected void addSearchField() {
        searchField = new EditBox(minecraft.font, width - 101, height - 76, 84, 20, 
                searchField, Component.translatable("jojo.ge_lifeform.search_field"));
        searchField.setResponder(this::filterEntriesRaw);
        addWidget(searchField);
        
        addRenderableWidget(clearSearchFieldButton = new ImageButton(width - 12, height - 70, 8, 7, 40, 112, 8, LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    searchField.setValue("");
                }));
        clearSearchFieldButton.visible = searchField.visible;
        searchField.setValue(savedSearchFilter);
    }
    
    protected abstract void refreshEntityTypes();
    
    protected void addCommonWidgets(ViewMode currentMode) {
        Minecraft mc = getMinecraft();
        Button gridModeButton = new ImageVanillaButton(
                width - 101, height - 48, 20, 20, 48, 68, 
                LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    ClientModSettings.edit(s -> s.viewModeGE = ViewMode.GRID);
                    mc.setScreen(new ChooseLifeformGridScreen(null));
                },
                (matrixStack, button, mouseX, mouseY) -> {
                    // TODO tooltip
                },
                Component.empty());
        
        Button listModeButton = new ImageVanillaButton(
                width - 76, height - 48, 20, 20, 68, 68, 
                LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    ClientModSettings.edit(s -> s.viewModeGE = ViewMode.LIST);
                    mc.setScreen(new ChooseLifeformListScreen(null));
                },
                (matrixStack, button, mouseX, mouseY) -> {
                    // TODO tooltip
                },
                Component.empty());
        
        switch (currentMode) {
        case GRID:
            gridModeButton.active = false;
            break;
        case LIST:
            listModeButton.active = false;
            break;
        }
        
        addRenderableWidget(listModeButton);
        addRenderableWidget(gridModeButton);
        
        filterList = new RadioButtonsList<>(savedFilterMode, val -> {
            savedFilterMode = val;
            onFilterRadioButton();
        });
        int x = width - 101;
        int y = height - 95;
        for (int i = FilterMode.values().length - 1; i >= 0; i--) {
            FilterMode mode = FilterMode.values()[i];
            filterList.addRenderableWidget(x, y, mode.uiName, mode);
            y -= 16;
        }
        addWidget(filterList);
        
        Button unlockAllButton = Button.builder(Component.translatable("jojo.ge_lifeform.unlock_all"), button -> {
                    GoldExperienceChooseLifeform.unlockAllEntityTypes(mc.player);
                    PacketManager.sendToServer(new ClAllGELifeformsButtonPacket());
                    refreshEntityTypes();
                })
                        .pos(width - 101, height - 24)
                        .size(95, 20).build();
        unlockAllButton.visible = mc.player.abilities.instabuild;
        addRenderableWidget(unlockAllButton);
    }
    
    public static final ResourceLocation LIFEFORM_CHOOSE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/lifeform_choose.png");
    
    public static final Comparator<String> MOD_NAMES_ORDER = (name1, name2) -> {
        boolean mc1 = "Minecraft".equals(name1);
        boolean mc2 = "Minecraft".equals(name2);
        if (mc1 ^ mc2) {
            return mc1 ? -1 : 1;
        }
        return name1.compareTo(name2);
    };
    
    public static final Comparator<EntityType<?>> ENTITY_MOD_NAME_COMPARE = Comparator.comparing(
            type -> ModInteractionUtil.getModName(MCUtil.id(type)),
            MOD_NAMES_ORDER);
    public static final Comparator<EntityType<?>> ENTITY_NAME_COMPARE = Comparator.comparing(t -> t.getDescription().getString());
    
    protected void filterEntriesRaw(String field) {
        savedSearchFilter = field;
        
        Predicate<EntityType<?>> filter;
        if (field.isEmpty()) {
            filter = null;
        }
        else {
            String[] words = field.split(" ");
            @Nullable Predicate<String> modNameFilter = null;
            StringBuilder nameFilter = new StringBuilder();
            boolean maybeTypingKeyword = field.charAt(field.length() - 1) != ' ';
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                word = word.toLowerCase();
                if (word.startsWith("mod:")) {
                    String modSearch = word.substring("mod:".length());
                    Predicate<String> nextFilter = mod -> mod.contains(modSearch);
                    modNameFilter = modNameFilter == null ? nextFilter : modNameFilter.or(nextFilter);
                }
                else {
                    boolean isTypingKeyword = maybeTypingKeyword && i == words.length - 1 && "mod".startsWith(word);
                    if (!isTypingKeyword) {
                        if (nameFilter.length() > 0) {
                            nameFilter.append(' ');
                        }
                        nameFilter.append(word);
                    }
                }
            }
            Predicate<String> finalModFilter = modNameFilter;
            filter = entityType -> 
                    (entityType.getDescription().getString().toLowerCase().contains(nameFilter) || MCUtil.id(entityType).getPath().contains(nameFilter)) && 
                    (finalModFilter == null || finalModFilter.test(ModInteractionUtil.getModName(MCUtil.id(entityType)).toLowerCase()));
        }
        searchBarFilter(filter);
    }
    
    protected abstract void searchBarFilter(@Nullable Predicate<EntityType<?>> filter);
    protected abstract void onFilterRadioButton();
    
    @Override
    public void setFocused(@Nullable GuiEventListener pListener) {
        if (pListener == null || pListener == searchField) {
            doSetFocused(pListener);
        }
        else {
            GuiEventListener focused = getFocused();
            if (searchField != null && focused == searchField) {
                searchField.setFocused(true);
            }
        }
    }
//    
//    private void filterEntries(String field) {
//        boolean emptyQuery = field == null || field.isEmpty();
//        Predicate<EntityType<?>> filter = emptyQuery ? null : 
//            entityType -> {
//                String searchLC = field.toLowerCase();
//                return entityType.getDescription().getString().toLowerCase().contains(searchLC)
//                || ModInteractionUtil.getModName(entityType.getRegistryName()).toLowerCase().contains(searchLC)
//                || entityType.getRegistryName().toString().contains(searchLC);
//            };
//        entityIconsGrid.setFilter(GeneralUtil.mapPredicate(filter, widget -> widget.entityType));
//        entityIconsGrid.setShowHidden(!emptyQuery);
//    }
//    
//    
//    
    private int ticksKeyHeld = 0;
    private final KeyMapping keyHeld;
    private ScreenCloseMode mode = ScreenCloseMode.CLICK;
    private boolean holdsButton = true;
    
    @Override
    public void tick() {
        super.tick();
        if (holdsButton) {
            if (!isKeyBeingHeld()) {
                holdsButton = false;
            }
            else if (++ticksKeyHeld == 5) {
                mode = ScreenCloseMode.HOLD;
            }
        }
        if (searchField != null) {
            searchField.tick();
        }
    }
    
    private boolean isKeyBeingHeld() {
        if (keyHeld == null) return false;
        
        long window = minecraft.getWindow().getWindow();
        int value = keyHeld.getKey().getValue();
        int state = value < 8 ? GLFW.glfwGetMouseButton(window, value) : GLFW.glfwGetKey(window, value);
        return state == 1;
    }
    
    
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
//        chosenLifeformCache = getEntriesUiData(minecraft.player).map(
//                entityData -> entityData.getGEChosenLifeformType()).orElse(null);
        if (searchField != null) {
            searchField.render(com.github.standobyte.jojo.client.ui.render.GuiDraw.graphics(), mouseX, mouseY, partialTicks);
        }
        if (filterList != null) {
            filterList.render(com.github.standobyte.jojo.client.ui.render.GuiDraw.graphics(), mouseX, mouseY, partialTicks);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        if (!holdsButton && mode == ScreenCloseMode.HOLD) {
            chooseHoveredAndClose();
        }
    }
    
    protected void chooseHoveredAndClose() {
    }
    
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.0");
    protected void renderHoveredTooltip(PoseStack matrixStack, EntitySubtype<?> entityType, int mouseX, int mouseY) {
        List<ITooltipLine> entityTypeInfo = makeHoveredTooltip(entityType);
        entityTypeInfo.stream().map(line -> line.getWidth(font)).max(Comparator.naturalOrder()).ifPresent(tooltipWidth -> {
            CustomTooltipRender.renderWrappedToolTip(matrixStack, entityTypeInfo, mouseX, mouseY, font);
        });
    }
    
    protected List<ITooltipLine> makeHoveredTooltip(EntitySubtype<?> entityType) {
        List<ITooltipLine> entityTypeInfo = new ArrayList<>();
        
        entityTypeInfo.add(new TextTooltipLine(entityType.getDescription()));
        
        entityTypeInfo.add(new TextTooltipLine(Component.literal(ModInteractionUtil.getModName(entityType.getId()))
                .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
        
        Entity entity = EntityTypeToInstance.getEntityInstance(entityType, minecraft.level);
        String width = SIZE_FORMAT.format(entity.getBbWidth());
        String height = SIZE_FORMAT.format(entity.getBbHeight());
        double strength = GoldExperienceCreateLifeform.getAttackStrength(entity);
        int creationTicks = GoldExperienceCreateLifeform.getTicksToCreate(minecraft.player, ClientUtil.getStandPowerClCached(), entity, unlockedMobsData);
        boolean isMobNativeToArea = unlockedMobsData.isMobNativeToPlayerPos(minecraft.level, entity, minecraft.player);
        String creationSecs = String.format("%.2f", (float) creationTicks / 20F);
        
        entityTypeInfo.add(new MultiTooltipLine(
                new IconTooltipLine(IconTooltipLine.Icon.VOLUME),
                new TextTooltipLine(Component.translatable("gold_experience.lifeform_size", width, height, width))));
        if (strength > 0) {
            entityTypeInfo.add(new MultiTooltipLine(
                    new IconTooltipLine(IconTooltipLine.Icon.STRENGTH),
                    new TextTooltipLine(Component.literal(String.format("%.1f", strength)))));
        }
        entityTypeInfo.add(new MultiTooltipLine(
                new IconTooltipLine(IconTooltipLine.Icon.TIME),
                new TextTooltipLine(Component.translatable("gold_experience.lifeform_time", creationSecs)
                        .withStyle(isMobNativeToArea ? ChatFormatting.GREEN : ChatFormatting.WHITE))));

//        entityTypeInfo.add(new TextTooltipLine(Component.literal(String.valueOf(GoldExperienceCreateLifeform.getVolume(entity)))));
        
        return entityTypeInfo;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (super.mouseClicked(mouseX, mouseY, buttonId)) {
            return true;
        }
        
        if (closeOnSecondClick(buttonId)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (closeOnSecondClick(pKeyCode)) { // FIXME causes the window to open again
            return true;
        }
        
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
    
    private boolean closeOnSecondClick(int keyPressedCode) {
        if (mode == ScreenCloseMode.CLICK && keyHeld != null && keyPressedCode == keyHeld.getKey().getValue()) {
            keyHeld.setDown(false);
            onClose();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onClose() {
        super.onClose();
        playerUISettings.clearGENewMobs();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public boolean acceptsKeyInput() {
        return searchField == null || !searchField.canConsumeInput();
    }
    
}