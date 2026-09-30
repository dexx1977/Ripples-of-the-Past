package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import net.minecraft.client.gui.GuiGraphics;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen.widgets.TextButton;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public abstract class LifeformsList<V> extends ObjectSelectionList<LifeformsList.LifeformsListEntry> {
    private static final Set<String> COLLAPSED_MOD_NAMES = new HashSet<>();
    
    protected ChooseLifeformListScreen screen;
    
    private List<V> allValues = new ArrayList<>();
    private Map<String, List<LifeformEntry>> allVisibleEntries = new HashMap<>();
    
    public LifeformsList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight, ChooseLifeformListScreen screen) {
        super(mc, width, height, y0, y1, itemHeight, Button.DEFAULT_NARRATION);
        this.screen = screen;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }
    
    public void setAllLegalValues(Collection<V> allValues) {
        this.allValues.clear();
        this.allValues.addAll(allValues);
    }
    
    public void update(Collection<V> lifeformValues) {
        update(lifeformValues.stream());
    }
    
    public void update(Stream<V> lifeformValues) {
        update(lifeformValues.collect(Collectors.groupingBy(this::getModName)));
    }
    
    private void update(Map<String, List<V>> byModName) {
        clearEntries();
        allVisibleEntries.clear();
//        maxWidth = -1;
        
        byModName.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey, ChooseLifeformScreen.MOD_NAMES_ORDER)).forEach(modEntry -> {
            String modName = modEntry.getKey();
            
            ModCategoryEntry category = new ModCategoryEntry(modName);
            category.isExpanded = !COLLAPSED_MOD_NAMES.contains(modName);
            category.addExpandButton(new ModCategoryEntry.ExpandCollapseButton(
                    -1, -1, 10, 10, screen, button -> {
                        if (category.isExpanded) {
                            for (LifeformEntry entryToHide : allVisibleEntries.get(modName)) {
                                removeEntry(entryToHide);
                            }
                            setScrollAmount(getScrollAmount());
                            COLLAPSED_MOD_NAMES.add(modName);
                            category.isExpanded = false;
                        }
                        else {
                            int index = children().indexOf(category);
                            if (index > -1) {
                                children().addAll(index + 1, allVisibleEntries.get(modName));
                            }
                            COLLAPSED_MOD_NAMES.remove(modName);
                            category.isExpanded = true;
                        }
                    }, () -> category.isExpanded));
            addEntry(category);
//            maxWidth = Math.max(maxWidth, minecraft.font.width(Component.literal(modName)));
            
            List<LifeformEntry> entriesWithHidden = new ArrayList<>();
            allVisibleEntries.put(modName, entriesWithHidden);
            
            List<V> values = modEntry.getValue();
            values.stream().sorted(Comparator.comparing(
                    ((Function<V, Component>) (this::getValueName))
                    .andThen(Component::getString), String::compareTo)).forEach(entryVal -> {
                        
                Component name = getValueName(entryVal);
                
                LifeformEntry entry = makeLifeformEntry(entryVal, name);
                if (isNew(entryVal)) {
                    entry.unseenEntry = true;
                }
                
                TextButton textButton = new TextButton(-1, -1, name, 
                        button -> {
                            select(entryVal);
                            screen.onClose();
                        }, 
                        (button, matrixStack, mouseX, mouseY) -> {
                            renderHoveredTooltip(matrixStack, entryVal, mouseX, mouseY);
                        },
                        minecraft.font) {
                    
                    @Override
                    public Component makeText() {
                        Component text = super.makeText();
                        if (entry.unseenEntry) {
                            text = Component.translatable("gold_experience.lifeform_unseen", text).withStyle(ChatFormatting.AQUA);
                        }
                        return text;
                    }
                };
                textButton.setHeight(itemHeight);
                LifeformEntry.FavoriteButton favoritesButton = new LifeformEntry.FavoriteButton(-1, -1, 9, 9, 
                        b -> {
                            LifeformEntry.FavoriteButton button = (LifeformEntry.FavoriteButton) b;
                            if (button.isFavorited) {
                                removeFavorite(entryVal);
                                button.isFavorited = false;
                            }
                            else {
                                addFavorite(entryVal);
                                button.isFavorited = true;
                            }
                        }, 
                        screen);
                favoritesButton.isFavorited = isInFavorites(entryVal);
                entry.addButtons(textButton, favoritesButton);

//                ITextComponent widthCheck = name;
//                if (entry.unseenEntry) {
//                    widthCheck = Component.translatable("gold_experience.lifeform_unseen", name);
//                }
//                maxWidth = Math.max(maxWidth, minecraft.font.width(widthCheck));
                
                entriesWithHidden.add(entry);
                if (category.isExpanded) {
                    addEntry(entry);
                }
            });
        });
        
//        int leftPos = this.x0;
//        updateSize(maxWidth + 54, this.height, y0, y1);
//        setLeftPos(leftPos);
    }

    protected abstract String getModName(V lifeformType);
    protected abstract Component getValueName(V lifeformType);
    protected abstract LifeformEntry makeLifeformEntry(V lifeformType, Component name);
    protected abstract void select(V lifeformType);
    protected abstract void addFavorite(V lifeformType);
    protected abstract void removeFavorite(V lifeformType);
    protected abstract boolean isInFavorites(V lifeformType);
    protected abstract boolean isNew(V lifeformType);
    protected abstract void renderHoveredTooltip(PoseStack matrixStack, V lifeformType, int mouseX, int mouseY);
    
    private Predicate<V> searchBarFilter = null;
    public void setSearchBarFilter(@Nullable Predicate<V> filter) {
        this.searchBarFilter = filter;
        doFilter();
        setScrollAmount(getScrollAmount());
    }
    
    public void updateRadioButtonFilter() {
        doFilter();
        setScrollAmount(getScrollAmount());
    }
    
    protected void doFilter() {
        Predicate<V> filter = searchBarFilter != null ? searchBarFilter : v -> true;
        switch (screen.filterList.getSelectedValue()) {
        case FAVORITES:
            filter = filter.and(this::isInFavorites);
            break;
        case NEW:
            filter = filter.and(this::isNew);
            break;
        default:
            break;
        }
        
        update(allValues.stream().filter(filter));
    }
    
    @Override
    public int getRowWidth() {
        return width;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return x1 - 6;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        PoseStack pMatrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
        // Ctrl + C, Ctrl + V
        this.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
//        if (this.renderBackground) {
//            GuiDraw.bind(GuiDraw.BACKGROUND_LOCATION);
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            float f = 32.0F;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            tessellator.end();
//        }

        int j1 = this.getRowLeft();
        int k = this.y0 + 4 - (int)this.getScrollAmount();
//        if (this.renderHeader) {
//            this.renderHeader(pMatrixStack, j1, k, tessellator);
//        }

//        if (this.renderTopAndBottom) {
//            GuiDraw.bind(GuiDraw.BACKGROUND_LOCATION);
//            RenderSystem.enableDepthTest();
//            RenderSystem.depthFunc(519);
//            float f1 = 32.0F;
//            int l = -100;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.vertex((double)this.x0, (double)this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
//            tessellator.end();
//            RenderSystem.depthFunc(515);
//            RenderSystem.disableDepthTest();
//            RenderSystem.enableBlend();
//            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
//            RenderSystem.disableAlphaTest();
//            RenderSystem.shadeModel(7425);
//            RenderSystem.disableTexture();
//            int i1 = 4;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            tessellator.end();
//        }

        int k1 = this.getMaxScroll();
        if (k1 > 0) {
            int scrollBarAlpha = 127;
            int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            l1 = Mth.clamp(l1, 32, this.y1 - this.y0 - 8);
            int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
            if (i2 < this.y0) {
                i2 = this.y0;
            }
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)(i2 + l1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)i2, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)(i2 + l1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)i2, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            tessellator.end();

            RenderSystem.disableBlend();
        }

        // moved it lower to render tooltips on top of the scroll bar
        this.renderList(pMatrixStack, j1, k, pMouseX, pMouseY, pPartialTicks);

        this.renderDecorations(pMatrixStack, pMouseX, pMouseY);
        RenderSystem.disableBlend();
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.setScrollAmount(this.getScrollAmount() - delta * itemHeight);
        return true;
    }
    
    protected abstract static class LifeformsListEntry extends ContainerObjectSelectionList.Entry<LifeformsListEntry> {}
    
    public static class ModCategoryEntry extends LifeformsListEntry {
        private final String modName;
        private AbstractWidget expandButton;
        private boolean isExpanded = true;
        private List<AbstractWidget> buttons = Collections.emptyList();
        
        public ModCategoryEntry(String modName) {
            this.modName = modName;
        }
        
        private void addExpandButton(AbstractWidget expandButton) {
            this.expandButton = expandButton;
            this.buttons = ImmutableList.of(expandButton);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return buttons;
        }

        @Override
        public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight,
                int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            Font font = Minecraft.getInstance().font;
            Component name = Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
            GuiDraw.drawString(pMatrixStack, font, name, pLeft + 43, pTop + 1, 0xFFFFFF);
            
            if (expandButton != null) {
                expandButton.x = pLeft + 29;
                expandButton.y = pTop;
                expandButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            }
        }
        
        
        private static class ExpandCollapseButton extends Button {
            private Supplier<Boolean> isExpanded;
            
            public ExpandCollapseButton(int pX, int pY, int pWidth, int pHeight, Screen screen, 
                    Button.OnPress pOnPress, Supplier<Boolean> isExpanded) {
                super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, 
                        (button, matrixStack, mouseX, mouseY) -> {
                            Component text = isExpanded.get() ? Component.translatable("jojo.ui.list_collapse") : Component.translatable("jojo.ui.list_expand");
                            com.github.standobyte.jojo.client.ui.render.GuiDraw.renderToolTip(matrixStack, text, mouseX, mouseY);
                        });
                this.isExpanded = isExpanded;
            }
            
            @Override
            public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack matrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
                Minecraft minecraft = Minecraft.getInstance();
                GuiDraw.bind(ChooseLifeformListScreen.LIFEFORM_CHOOSE_LOCATION);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
                
                int texX = isHovered ? 118 : 108;
                int texY = isExpanded.get() ? 20 : 30;
                
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                GuiDraw.blit(matrixStack, x, y, texX, texY, width, height, 128, 128);
            }
        }
    }
    
    public static class LifeformEntry extends LifeformsListEntry {
        protected final Component valueName;
        private AbstractWidget lifeformButton;
        private AbstractWidget favoriteButton;
        private List<AbstractWidget> buttons = Collections.emptyList();
        private boolean unseenEntry = false;
        
        public LifeformEntry(Component valueName) {
            this.valueName = valueName;
        }
        
        void addButtons(AbstractWidget lifeformButton, AbstractWidget favoriteButton) {
            this.lifeformButton = lifeformButton;
            this.favoriteButton = favoriteButton;
            this.buttons = ImmutableList.of(lifeformButton, favoriteButton);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return buttons;
        }

        @Override
        public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight,
                int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            if (lifeformButton != null) {
                lifeformButton.x = pLeft + 25;
                lifeformButton.y = pTop + 1;
                lifeformButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            }
            if (favoriteButton != null) {
                favoriteButton.x = pLeft + 2;
                favoriteButton.y = pTop + 1;
                favoriteButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            }
        }
        
        
        private static class FavoriteButton extends Button {
            private boolean isFavorited;

            public FavoriteButton(int pX, int pY, int pWidth, int pHeight, 
                    Button.OnPress pOnPress, Screen screen) {
                super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, 
                        (button, matrixStack, mouseX, mouseY) -> {
                            Component text = ((FavoriteButton) button).isFavorited ? Component.translatable("jojo.ui.favorite_remove") : Component.translatable("jojo.ui.favorite");
                            com.github.standobyte.jojo.client.ui.render.GuiDraw.renderToolTip(matrixStack, text, mouseX, mouseY);
                        });
            }

            @Override
            public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        PoseStack pMatrixStack = guiGraphics.pose();
        GuiDraw.setGraphics(guiGraphics);
                if (!(isFavorited || isHovered())) return;
                
                Minecraft mc = Minecraft.getInstance();
                GuiDraw.bind(ChooseLifeformListScreen.LIFEFORM_CHOOSE_LOCATION);
                int texY = isFavorited ? 9 : 0;
                RenderSystem.enableDepthTest();
                GuiDraw.blit(pMatrixStack, x, y, 119, texY, width, height, 128, 128);
            }
        }
        
    }
}
