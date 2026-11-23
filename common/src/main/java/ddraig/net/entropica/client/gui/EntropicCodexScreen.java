package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.gui.tab.FusionSimulatorTab;
import ddraig.net.entropica.client.gui.tab.NetworkDiagnosticsTab;
import ddraig.net.entropica.client.gui.widget.CodexRecipeWidget;
import ddraig.net.entropica.client.gui.widget.MultiblockBlueprintWidget;
import ddraig.net.entropica.codex.CodexArticlesBridge;
import ddraig.net.entropica.codex.CodexWikiMetadata;
import ddraig.net.entropica.codex.CodexWikiMetadata.ConstellationChartPage;
import ddraig.net.entropica.codex.CodexWikiMetadata.ConstellationSubGroup;
import ddraig.net.entropica.codex.CodexWikiMetadata.StarPlacement;
import ddraig.net.entropica.codex.CodexWikiMetadata.StarState;
import ddraig.net.entropica.codex.CodexWikiMetadata.WikiArticle;
import ddraig.net.entropica.data.CelestialJournalData;
import ddraig.net.entropica.data.CodexPlayerData;
import ddraig.net.entropica.item.EntropicCodexItem;
import ddraig.net.entropica.network.UpdateCodexStatePayload;
import ddraig.net.entropica.spell.research.SpellResearchManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Overhauled Entropic Codex UI combining:
 *  1. Main Index (Option 1): Center Getting Started + 6-Category Orbital Ring + Top Lore Ribbon
 *  2. Chapter Constellations (Option 5): Concentric Progression Rings with twinkling stars & sub-tabs
 *  3. Modern Wiki Reader (Option 4): Left Collapsible Sub-Folder Sidebar + Two-Page Arcane Tome Spread
 */
public class EntropicCodexScreen extends Screen {

    public enum CodexMode {
        MAIN_INDEX,
        CHAPTER_CONSTELLATION,
        WIKI_READER
    }

    private enum OverlayMode {
        NONE,
        BOOKMARKS,
        TOOLS
    }

    private record NavState(
            CodexMode mode,
            String category,
            int subGroupIndex,
            int chartPageIndex,
            @Nullable String articleId
    ) {
    }

    private record ClickHitbox(int x, int y, int w, int h, Runnable onClick) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private record HoverCardInfo(
            ItemStack icon,
            String title,
            String summary,
            String statusLine,
            int accentColor,
            boolean hideIcon
    ) {
    }

    private static final ResourceLocation BLACK_HOLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/black_hole_bg.png");
    private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final int TOP_BAR_H = 28;

    private final EssenceType highestEssence;
    @Nullable
    private final BlockPos pairedTerminalPos;

    private CodexMode currentMode = CodexMode.MAIN_INDEX;
    private OverlayMode activeOverlay = OverlayMode.NONE;

    private String selectedCategory = CodexWikiMetadata.CAT_GETTING_STARTED;
    private int selectedSubGroupIndex = 0;
    private int selectedChartPageIndex = 0;
    private int constellationTabScrollOffset = 0;

    @Nullable
    private WikiArticle selectedArticle = null;

    // Navigation History (< Back / Forward >)
    private final List<NavState> history = new ArrayList<>();
    private int historyCursor = -1;

    // Scroll States
    private float sidebarScrollOffset = 0f;
    private float maxSidebarScroll = 0f;
    private float leftPageScrollOffset = 0f;
    private float maxLeftPageScroll = 0f;
    private float rightPageScrollOffset = 0f;
    private float maxRightPageScroll = 0f;
    private float searchScrollOffset = 0f;
    private float bookmarkScrollOffset = 0f;

    // Collapsible Sidebar Sub-Folders (stores collapsed folder keys "CAT::Folder")
    private final Set<String> collapsedFolders = new HashSet<>();

    // Top Search Box
    private EditBox searchBox;
    private String lastSearchQuery = "";

    // Interactive Widgets & Tools
    private final CodexRecipeWidget recipeWidget = new CodexRecipeWidget();
    @Nullable
    private MultiblockBlueprintWidget blueprintWidget = null;
    private int lastBlueprintRenderX = -1, lastBlueprintRenderY = -1;
    private final FusionSimulatorTab fusionTab = new FusionSimulatorTab();
    private int lastInlineFusionX = -1, lastInlineFusionY = -1;
    private final NetworkDiagnosticsTab diagnosticsTab;
    private int activeToolsModalTab = 0; // 0 = Fusion Calculator, 1 = Terminal Telemetry

    // Frame-local interactive hitboxes & hover state
    private final List<ClickHitbox> frameHitboxes = new ArrayList<>();
    private final List<ClickHitbox> leftPageLinkHitboxes = new ArrayList<>();
    private final List<ClickHitbox> rightPageLinkHitboxes = new ArrayList<>();
    @Nullable
    private HoverCardInfo activeHoverCard = null;

    public EntropicCodexScreen() {
        this(null, null);
    }

    public EntropicCodexScreen(EssenceType essenceType) {
        this(essenceType, null);
    }

    public EntropicCodexScreen(@Nullable EssenceType essenceType, @Nullable BlockPos pairedTerminalPos) {
        super(Component.translatable("msg.entropica.entropic_codex"));
        this.highestEssence = essenceType;
        BlockPos resolvedPos = pairedTerminalPos;
        if (resolvedPos == null && Minecraft.getInstance().player != null) {
            resolvedPos = EntropicCodexItem.findPairedTerminalPos(Minecraft.getInstance().player);
        }
        this.pairedTerminalPos = resolvedPos;
        this.diagnosticsTab = new NetworkDiagnosticsTab(this.pairedTerminalPos);
    }

    @Override
    protected void init() {
        super.init();
        CodexWikiMetadata.ensureInitialized();

        if (this.minecraft != null && this.minecraft.player != null) {
            CodexPlayerData.CLIENT_DATA.importReadJournalsFromInventory(this.minecraft.player);
        }

        int searchW = Math.min(180, Math.max(96, this.width - 330));
        int rightButtonsW = 196;
        int searchX = Math.max(114, this.width - rightButtonsW - searchW - 8);

        String prevText = (this.searchBox != null) ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.font, searchX, 5, searchW, 18, Component.translatable("msg.entropica.search"));
        this.searchBox.setMaxLength(40);
        this.searchBox.setHint(Component.literal("§8Search wiki & lore..."));
        this.searchBox.setValue(prevText);
        this.addRenderableWidget(this.searchBox);

        if (this.selectedArticle == null) {
            this.selectedArticle = CodexWikiMetadata.getArticle("hub_getting_started");
            syncArticleWidgets();
        }
        if (this.history.isEmpty()) {
            pushHistoryState();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // =========================================================================
    // NAVIGATION & PROGRESSION STATE HELPERS
    // =========================================================================

    private void navigateToMainIndex() {
        this.activeOverlay = OverlayMode.NONE;
        this.searchBox.setValue("");
        this.searchBox.setFocused(false);
        this.currentMode = CodexMode.MAIN_INDEX;
        pushHistoryState();
        playPageTurnSound();
    }

    private void navigateToConstellation(String category, int subGroupIdx, int pageIdx) {
        this.activeOverlay = OverlayMode.NONE;
        this.searchBox.setValue("");
        this.searchBox.setFocused(false);
        this.selectedCategory = category;
        List<ConstellationSubGroup> groups = CodexWikiMetadata.buildConstellationGroups(category);
        this.selectedSubGroupIndex = groups.isEmpty() ? 0 : Mth.clamp(subGroupIdx, 0, groups.size() - 1);
        int maxPages = groups.isEmpty() ? 1 : groups.get(this.selectedSubGroupIndex).pages().size();
        this.selectedChartPageIndex = Mth.clamp(pageIdx, 0, Math.max(0, maxPages - 1));
        this.constellationTabScrollOffset = Math.max(0, this.selectedSubGroupIndex - 3);
        this.currentMode = CodexMode.CHAPTER_CONSTELLATION;
        pushHistoryState();
        playPageTurnSound();
    }

    private void navigateToWikiArticle(WikiArticle article) {
        if (article == null) return;
        CodexPlayerData data = CodexPlayerData.CLIENT_DATA;
        if (!CodexWikiMetadata.canReadArticle(article, data)) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5F, 0.7F);
            }
            return;
        }

        this.activeOverlay = OverlayMode.NONE;
        this.searchBox.setValue("");
        this.searchBox.setFocused(false);

        this.selectedArticle = article;
        this.selectedCategory = article.category();
        this.currentMode = CodexMode.WIKI_READER;
        this.leftPageScrollOffset = 0f;
        this.rightPageScrollOffset = 0f;

        // Expand this article's sub-folder automatically so it's visible in the sidebar
        this.collapsedFolders.remove(folderKey(article.category(), article.subFolder()));

        // Sync sub-constellation index so "View Constellation" opens to the matching branch/volume!
        syncConstellationGroupToArticle(article);

        // Ignite star in branch if reading in order!
        if (!article.isLoreEntry() && !article.isMobBestiaryEntry()) {
            boolean newlyLit = data.tryIgniteStarOnRead(article.id());
            sendCodexActionToServer(UpdateCodexStatePayload.ACTION_READ_STAR, article.id());
            if (newlyLit && this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.45F, 1.35F);
            }
        }

        syncArticleWidgets();
        pushHistoryState();
        playPageTurnSound();
    }

    private void syncConstellationGroupToArticle(WikiArticle article) {
        List<ConstellationSubGroup> groups = CodexWikiMetadata.buildConstellationGroups(article.category());
        for (int g = 0; g < groups.size(); g++) {
            List<ConstellationChartPage> pages = groups.get(g).pages();
            for (int p = 0; p < pages.size(); p++) {
                for (StarPlacement sp : pages.get(p).stars()) {
                    if (sp.article().id().equals(article.id())) {
                        this.selectedSubGroupIndex = g;
                        this.selectedChartPageIndex = p;
                        return;
                    }
                }
            }
        }
    }

    private void syncArticleWidgets() {
        this.recipeWidget.setArticle(this.selectedArticle);
        this.blueprintWidget = null;
        this.lastBlueprintRenderX = -1;
        this.lastInlineFusionX = -1;

        if (this.selectedArticle != null && !this.selectedArticle.isLoreEntry()) {
            String id = this.selectedArticle.id();
            if (CodexWikiMetadata.CAT_MULTIBLOCKS.equals(this.selectedArticle.category())
                    || "magic_rituals".equals(id)) {
                String bpKey = "magic_rituals".equals(id) ? "Magic Circle" : this.selectedArticle.title();
                this.blueprintWidget = new MultiblockBlueprintWidget(bpKey);
            }
        }
    }

    private void pushHistoryState() {
        NavState state = new NavState(
                this.currentMode,
                this.selectedCategory,
                this.selectedSubGroupIndex,
                this.selectedChartPageIndex,
                this.selectedArticle != null ? this.selectedArticle.id() : null
        );
        if (this.historyCursor >= 0 && this.historyCursor < this.history.size()) {
            if (this.history.get(this.historyCursor).equals(state)) {
                return;
            }
        }
        while (this.history.size() > this.historyCursor + 1) {
            this.history.remove(this.history.size() - 1);
        }
        this.history.add(state);
        if (this.history.size() > 40) {
            this.history.remove(0);
        }
        this.historyCursor = this.history.size() - 1;
    }

    private void stepHistory(int delta) {
        int target = this.historyCursor + delta;
        if (target < 0 || target >= this.history.size()) return;
        this.historyCursor = target;
        NavState s = this.history.get(target);
        this.activeOverlay = OverlayMode.NONE;
        this.currentMode = s.mode();
        this.selectedCategory = s.category();
        this.selectedSubGroupIndex = s.subGroupIndex();
        this.selectedChartPageIndex = s.chartPageIndex();
        if (s.articleId() != null) {
            WikiArticle art = CodexWikiMetadata.getArticle(s.articleId());
            if (art != null) {
                this.selectedArticle = art;
                syncArticleWidgets();
            }
        }
        this.leftPageScrollOffset = 0f;
        this.rightPageScrollOffset = 0f;
        playPageTurnSound();
    }

    private void toggleBookmarkForCurrent() {
        if (this.selectedArticle == null) return;
        CodexPlayerData.CLIENT_DATA.toggleBookmark(this.selectedArticle.id());
        sendCodexActionToServer(UpdateCodexStatePayload.ACTION_TOGGLE_BOOKMARK, this.selectedArticle.id());
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.2F);
        }
    }

    private static void sendCodexActionToServer(int action, String targetId) {
        try {
            NetworkManager.sendToServer(new UpdateCodexStatePayload(action, targetId));
        } catch (Throwable ignored) {
        }
    }

    private void playPageTurnSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8F, 1.05F);
        }
    }

    private static String folderKey(String category, String folder) {
        return category + "::" + folder;
    }

    // =========================================================================
    // MASTER RENDER PIPELINE
    // =========================================================================

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.frameHitboxes.clear();
        this.leftPageLinkHitboxes.clear();
        this.rightPageLinkHitboxes.clear();
        this.activeHoverCard = null;

        float timeSec = (System.currentTimeMillis() % 10_000_000L) * 0.001f;

        // Reset search scroll when search query changes
        String query = this.searchBox != null ? this.searchBox.getValue().trim() : "";
        if (!query.equals(this.lastSearchQuery)) {
            this.lastSearchQuery = query;
            this.searchScrollOffset = 0f;
        }

        // 1. Cosmic Backdrop + Ambient Starfield + Essence Singularity Tint
        renderCosmicBackdrop(g, timeSec);

        // 2. Render Active Screen Stage (unless obscured by full modal, still drawn softly behind)
        switch (this.currentMode) {
            case MAIN_INDEX -> renderMainIndexOption1(g, mouseX, mouseY, timeSec);
            case CHAPTER_CONSTELLATION -> renderConstellationOption5(g, mouseX, mouseY, timeSec);
            case WIKI_READER -> renderWikiReaderOption4(g, mouseX, mouseY, timeSec);
        }

        // 3. Always-Visible Top Navigation Bar + Search Box
        renderTopNavigationBar(g, mouseX, mouseY);
        super.render(g, mouseX, mouseY, partialTick);

        // 4. Active Modal Overlays (Search Results, Bookmarks, or Tools)
        if (!query.isEmpty()) {
            renderSearchResultsOverlay(g, query, mouseX, mouseY);
        } else if (this.activeOverlay == OverlayMode.BOOKMARKS) {
            renderBookmarksOverlay(g, mouseX, mouseY);
        } else if (this.activeOverlay == OverlayMode.TOOLS) {
            renderToolsOverlay(g, mouseX, mouseY);
        }

        // 5. Deferred Tooltips (Hover Cards & Recipe Item Tooltips)
        if (query.isEmpty() && this.activeOverlay == OverlayMode.NONE) {
            if (this.activeHoverCard != null) {
                renderHoverCard(g, mouseX, mouseY, this.activeHoverCard);
            }
            if (this.currentMode == CodexMode.WIKI_READER) {
                this.recipeWidget.renderDeferredTooltip(g, this.font, CodexPlayerData.CLIENT_DATA);
            }
        }
    }

    private void renderCosmicBackdrop(GuiGraphics g, float timeSec) {
        g.fill(0, 0, this.width, this.height, 0xFF050711);
        g.blit(BLACK_HOLE_TEXTURE, 0, 0, this.width, this.height, 0.0f, 1.0f, 0.0f, 1.0f);

        // Dark translucent veil for crisp readability on Constellation & Wiki views
        int veilAlpha = (this.currentMode == CodexMode.MAIN_INDEX) ? 0x38050814 : 0x88050814;
        g.fill(0, 0, this.width, this.height, veilAlpha);

        // Subtle inventory Essence aura tint around center singularity on Main Index
        if (this.currentMode == CodexMode.MAIN_INDEX && this.highestEssence != null) {
            int gemRgb = EntropicCodexItem.getGemColor(this.highestEssence) & 0x00FFFFFF;
            int cx = this.width / 2;
            int cy = (this.height + TOP_BAR_H + 22) / 2;
            g.fill(cx - 46, cy - 46, cx + 46, cy + 46, (0x16 << 24) | gemRgb);
        }

        // Ambient twinkling background stars
        for (int i = 0; i < 42; i++) {
            int sx = ((i * 197 + 31) % Math.max(40, this.width - 16)) + 8;
            int sy = ((i * 113 + 47) % Math.max(40, this.height - TOP_BAR_H - 12)) + TOP_BAR_H + 6;
            float wave = 0.35f + 0.35f * Mth.sin(timeSec * 2.1f + i * 1.7f);
            int alpha = Mth.clamp((int) (wave * 180), 25, 190);
            int size = (i % 5 == 0) ? 2 : 1;
            g.fill(sx, sy, sx + size, sy + size, (alpha << 24) | 0xB8D8FF);
        }
    }

    // =========================================================================
    // TOP NAVIGATION BAR (Always Visible)
    // =========================================================================

    private void renderTopNavigationBar(GuiGraphics g, int mouseX, int mouseY) {
        g.fill(0, 0, this.width, TOP_BAR_H, 0xF2080C1A);
        g.fill(0, TOP_BAR_H - 1, this.width, TOP_BAR_H, 0xFF00D9FF);

        // Left Group: [✦ Index] [<] [>] + Breadcrumb
        boolean isHome = (this.currentMode == CodexMode.MAIN_INDEX && this.activeOverlay == OverlayMode.NONE);
        drawInteractivePill(g, 6, 5, 50, 18, isHome ? 0xFF182C4A : 0xFF10192E, isHome ? 0xFF00E5FF : 0xFF283A5E,
                "✦ Index", mouseX, mouseY, this::navigateToMainIndex);

        boolean canBack = this.historyCursor > 0;
        drawInteractivePill(g, 59, 5, 18, 18, 0xFF0F172A, canBack ? 0xFF38BDF8 : 0xFF1E293B,
                canBack ? "§f<" : "§8<", mouseX, mouseY, () -> {
                    if (canBack) stepHistory(-1);
                });

        boolean canFwd = this.historyCursor >= 0 && this.historyCursor < this.history.size() - 1;
        drawInteractivePill(g, 79, 5, 18, 18, 0xFF0F172A, canFwd ? 0xFF38BDF8 : 0xFF1E293B,
                canFwd ? "§f>" : "§8>", mouseX, mouseY, () -> {
                    if (canFwd) stepHistory(1);
                });

        // Dynamic Breadcrumb Trail
        int bcStartX = 103;
        int bcMaxW = (this.searchBox != null ? this.searchBox.getX() : this.width - 220) - bcStartX - 6;
        if (bcMaxW > 45) {
            String bc = buildBreadcrumbText();
            g.drawString(this.font, trimWithEllipsis(bc, bcMaxW), bcStartX, 10, 0xFF94A3B8, false);
        }

        // Right Quick Buttons: [📖 Lore] [★ Saved] [⚙ Tools] [✕]
        int closeX = this.width - 24;
        int toolsX = closeX - 56;
        int savedX = toolsX - 60;
        int loreX = savedX - 54;

        boolean loreActive = (this.currentMode != CodexMode.MAIN_INDEX && CodexWikiMetadata.CAT_LORE.equals(this.selectedCategory));
        drawInteractivePill(g, loreX, 5, 51, 18, loreActive ? 0xFF3B2810 : 0xFF1F160B, 0xFFF59E0B,
                "§6📖 Lore", mouseX, mouseY, () -> navigateToConstellation(CodexWikiMetadata.CAT_LORE, 0, 0));

        boolean bmActive = (this.activeOverlay == OverlayMode.BOOKMARKS);
        drawInteractivePill(g, savedX, 5, 57, 18, bmActive ? 0xFF2E1A47 : 0xFF17122B, 0xFFC084FC,
                "§d★ Saved", mouseX, mouseY, () -> {
                    this.searchBox.setValue("");
                    this.activeOverlay = (this.activeOverlay == OverlayMode.BOOKMARKS) ? OverlayMode.NONE : OverlayMode.BOOKMARKS;
                });

        boolean toolsActive = (this.activeOverlay == OverlayMode.TOOLS);
        drawInteractivePill(g, toolsX, 5, 53, 18, toolsActive ? 0xFF12353D : 0xFF0D2026, 0xFF2DD4BF,
                "§b⚙ Tools", mouseX, mouseY, () -> {
                    this.searchBox.setValue("");
                    this.activeOverlay = (this.activeOverlay == OverlayMode.TOOLS) ? OverlayMode.NONE : OverlayMode.TOOLS;
                });

        drawInteractivePill(g, closeX, 5, 18, 18, 0xFF7F1D1D, 0xFFF87171, "§f✕", mouseX, mouseY, this::onClose);
    }

    private String buildBreadcrumbText() {
        return switch (this.currentMode) {
            case MAIN_INDEX -> "§bEntropic Codex §8› §7Main Index";
            case CHAPTER_CONSTELLATION -> "§7Index §8› §b" + this.selectedCategory + " §8(Constellation)";
            case WIKI_READER -> {
                String artTitle = this.selectedArticle != null
                        ? CodexWikiMetadata.getDisplayTitle(this.selectedArticle, CodexPlayerData.CLIENT_DATA)
                        : "";
                yield "§7" + this.selectedCategory + " §8› §f" + artTitle;
            }
        };
    }

    // =========================================================================
    // STAGE 1: MAIN INDEX — OPTION 1 (Orbital Ring + Top Lore Ribbon)
    // =========================================================================

    private void renderMainIndexOption1(GuiGraphics g, int mouseX, int mouseY, float timeSec) {
        CodexPlayerData data = CodexPlayerData.CLIENT_DATA;

        // 1. Top Golden Lore Archives Ribbon Banner
        int totalLore = CodexWikiMetadata.getTotalLoreEntryCount();
        int readLore = data.getTotalArchivedLoreCount();
        int discVols = data.getDiscoveredVolumeCount();

        String ribbonText = "📖 LORE ARCHIVES  •  " + readLore + " / " + totalLore + " Chapters Read";
        int ribbonW = Math.min(this.width - 32, Math.max(236, this.font.width(ribbonText) + 36));
        int ribbonH = 24;
        int ribbonX = (this.width - ribbonW) / 2;
        int ribbonY = TOP_BAR_H + 7;

        boolean loreHov = isInside(mouseX, mouseY, ribbonX, ribbonY, ribbonW, ribbonH) && isOverlayClear();
        g.fill(ribbonX, ribbonY, ribbonX + ribbonW, ribbonY + ribbonH, loreHov ? 0xF233230E : 0xEB1D150A);
        drawHollowRect(g, ribbonX, ribbonY, ribbonW, ribbonH, loreHov ? 0xFFFDE047 : 0xFFF59E0B);
        // Ornate corner ticks on ribbon
        g.fill(ribbonX + 2, ribbonY + 2, ribbonX + 6, ribbonY + 3, 0xFFD4AF37);
        g.fill(ribbonX + ribbonW - 6, ribbonY + 2, ribbonX + ribbonW - 2, ribbonY + 3, 0xFFD4AF37);

        int rtW = this.font.width(ribbonText);
        g.drawString(this.font, ribbonText, ribbonX + (ribbonW - rtW) / 2, ribbonY + 8, 0xFFFCD34D, true);

        frameHitboxes.add(new ClickHitbox(ribbonX, ribbonY, ribbonW, ribbonH,
                () -> navigateToConstellation(CodexWikiMetadata.CAT_LORE, 0, 0)));

        if (loreHov) {
            this.activeHoverCard = new HoverCardInfo(
                    CodexWikiMetadata.getJournalVolumeIcon(1),
                    "Lore Archives (Volumes I – XIII)",
                    "Read physical Scholar's Journals found in the world to archive their chapters here.",
                    "§6" + discVols + " / 13 Volumes Found  •  " + readLore + " / " + totalLore + " Stars Lit",
                    0xFFF59E0B,
                    false
            );
        }

        // 2. Orbital Geometry (auto-scaled to fit any window size with zero dragging!)
        int availTop = ribbonY + ribbonH + 10;
        int availH = this.height - availTop - 14;
        int cx = this.width / 2;
        int cy = availTop + availH / 2 - 4;

        float rx = Mth.clamp(this.width * 0.27f, 95f, 185f);
        float ry = Mth.clamp(availH * 0.36f, 56f, 118f);

        // Draw faint celestial orbital ellipse
        drawSegmentedEllipse(g, cx, cy, rx, ry, 64, 0x4400D9FF);

        // Draw 6 radial spokes first (behind center & ring medallions)
        List<String> ringCats = CodexWikiMetadata.RING_CATEGORIES;
        int[][] ringCoords = new int[ringCats.size()][2];
        for (int i = 0; i < ringCats.size(); i++) {
            double angle = -Math.PI / 2.0 + (i * (Math.PI * 2.0 / ringCats.size()));
            int nx = cx + (int) Math.round(Math.cos(angle) * rx);
            int ny = cy + (int) Math.round(Math.sin(angle) * ry);
            ringCoords[i][0] = nx;
            ringCoords[i][1] = ny;

            int catCol = CodexWikiMetadata.getCategoryColor(ringCats.get(i));
            drawSmoothLine(g, cx, cy, nx, ny, (0x38 << 24) | (catCol & 0x00FFFFFF), false);
        }

        // 3. Center Core Star: GETTING STARTED
        String startCat = CodexWikiMetadata.CAT_GETTING_STARTED;
        int startCol = CodexWikiMetadata.getCategoryColor(startCat);
        int startLit = CodexWikiMetadata.countShiningStars(startCat, data);
        int startTotal = CodexWikiMetadata.getArticlesForCategory(startCat).size();
        boolean centerHov = Math.hypot(mouseX - cx, mouseY - cy) <= 24 && isOverlayClear();

        drawHubMedallion(g, cx, cy, 20, startCol, new ItemStack(Items.COMPASS),
                "Getting Started", startLit + "/" + startTotal, centerHov, timeSec);

        frameHitboxes.add(new ClickHitbox(cx - 24, cy - 24, 48, 48,
                () -> navigateToConstellation(startCat, 0, 0)));

        if (centerHov) {
            this.activeHoverCard = new HoverCardInfo(
                    new ItemStack(Items.COMPASS),
                    "Getting Started",
                    CodexWikiMetadata.getCategoryShortDescription(startCat),
                    "§a✦ " + startLit + " / " + startTotal + " Stars Shining  •  Click to open Constellation",
                    startCol,
                    false
            );
        }

        // 4. 6 Surrounding Orbital Category Medallions
        for (int i = 0; i < ringCats.size(); i++) {
            String cat = ringCats.get(i);
            int nx = ringCoords[i][0];
            int ny = ringCoords[i][1];
            int col = CodexWikiMetadata.getCategoryColor(cat);
            WikiArticle hubArt = CodexWikiMetadata.getArticle(CodexWikiMetadata.getCategoryHubId(cat));
            ItemStack icon = hubArt != null ? hubArt.icon() : new ItemStack(Items.NETHER_STAR);

            int lit = CodexWikiMetadata.countShiningStars(cat, data);
            int total = CodexWikiMetadata.getArticlesForCategory(cat).size();
            boolean hov = Math.hypot(mouseX - nx, mouseY - ny) <= 20 && isOverlayClear();

            String shortLabel = CAT_DISPLAY_LABEL(cat);
            drawHubMedallion(g, nx, ny, 16, col, icon, shortLabel, lit + "/" + total, hov, timeSec + i * 0.7f);

            frameHitboxes.add(new ClickHitbox(nx - 21, ny - 21, 42, 42,
                    () -> navigateToConstellation(cat, 0, 0)));

            if (hov) {
                this.activeHoverCard = new HoverCardInfo(
                        icon,
                        cat,
                        CodexWikiMetadata.getCategoryShortDescription(cat),
                        "✦ " + lit + " / " + total + " Stars Shining  •  Click to open Constellation",
                        col,
                        false
                );
            }
        }
    }

    private static String CAT_DISPLAY_LABEL(String cat) {
        if (CodexWikiMetadata.CAT_ENVIRONMENT.equals(cat)) return "Environment";
        return cat.charAt(0) + cat.substring(1).toLowerCase(Locale.ROOT);
    }

    private void drawHubMedallion(GuiGraphics g, int x, int y, int radius, int color, ItemStack icon,
                                  String label, String countStr, boolean hovered, float timeSec) {
        int rgb = color & 0x00FFFFFF;
        float pulse = 1.0f + 0.08f * Mth.sin(timeSec * 3.2f);
        int r = Math.round((hovered ? radius + 3 : radius) * pulse);

        // Soft outer starlight aura
        g.fill(x - r - 4, y - r - 4, x + r + 4, y + r + 4, ((hovered ? 0x44 : 0x22) << 24) | rgb);
        g.fill(x - r - 1, y - r - 1, x + r + 1, y + r + 1, (0xAA << 24) | rgb);

        // Dark celestial core
        g.fill(x - r + 1, y - r + 1, x + r - 1, y + r - 1, 0xF2080C1C);
        drawHollowRect(g, x - r, y - r, r * 2, r * 2, (0xFF << 24) | rgb);

        // 4-point star flare accents on midpoint edges
        g.fill(x - r - 3, y, x + r + 3, y + 1, (0x88 << 24) | rgb);
        g.fill(x, y - r - 3, x + 1, y + r + 3, (0x88 << 24) | rgb);

        g.renderItem(icon, x - 8, y - 8);

        // Crisp label pill below medallion
        int lw = this.font.width(label);
        int labelY = y + r + 4;
        g.fill(x - lw / 2 - 3, labelY - 1, x + lw / 2 + 3, labelY + 9, 0xD8050814);
        g.drawString(this.font, label, x - lw / 2, labelY, hovered ? (0xFF000000 | rgb) : 0xFFF1F5F9, true);

        if (countStr != null && !countStr.isEmpty() && this.height >= 230) {
            int cw = this.font.width(countStr);
            g.drawString(this.font, countStr, x - cw / 2, labelY + 10, 0xFF94A3B8, false);
        }
    }

    // =========================================================================
    // STAGE 2: CHAPTER CONSTELLATIONS — OPTION 5 (Concentric Progression Rings)
    // =========================================================================

    private void renderConstellationOption5(GuiGraphics g, int mouseX, int mouseY, float timeSec) {
        CodexPlayerData data = CodexPlayerData.CLIENT_DATA;
        List<ConstellationSubGroup> groups = CodexWikiMetadata.buildConstellationGroups(this.selectedCategory);
        if (groups.isEmpty()) return;

        this.selectedSubGroupIndex = Mth.clamp(this.selectedSubGroupIndex, 0, groups.size() - 1);
        ConstellationSubGroup activeGroup = groups.get(this.selectedSubGroupIndex);
        List<ConstellationChartPage> pages = activeGroup.pages();
        this.selectedChartPageIndex = Mth.clamp(this.selectedChartPageIndex, 0, pages.size() - 1);
        ConstellationChartPage chartPage = pages.get(this.selectedChartPageIndex);

        // 1. Top Sub-Chapter / Volume Selector Strip + [☰ Open Wiki View]
        int barY = TOP_BAR_H + 5;
        int wikiBtnW = 94;
        int wikiBtnX = this.width - wikiBtnW - 8;

        drawInteractivePill(g, wikiBtnX, barY, wikiBtnW, 18, 0xFF132238, 0xFF38BDF8,
                "§b☰ Wiki Reader", mouseX, mouseY, () -> {
                    WikiArticle target = chartPage.stars().isEmpty()
                            ? CodexWikiMetadata.getArticle(CodexWikiMetadata.getCategoryHubId(this.selectedCategory))
                            : chartPage.stars().get(0).article();
                    if (target != null && CodexWikiMetadata.canReadArticle(target, data)) {
                        navigateToWikiArticle(target);
                    } else {
                        // Open Wiki View on this category even if Volume I Entry 1 is unread!
                        this.selectedArticle = target;
                        this.currentMode = CodexMode.WIKI_READER;
                        syncArticleWidgets();
                        pushHistoryState();
                        playPageTurnSound();
                    }
                });

        // Sub-group tabs on the left of the sub-bar (with < > tab scrolling when 13 Lore Volumes are shown!)
        int tabsMaxRight = wikiBtnX - 8;
        if (pages.size() > 1) {
            tabsMaxRight -= 96; // Reserve space for < Chart 1/N > pager!
        }

        renderConstellationSubTabs(g, groups, 8, barY, tabsMaxRight - 8, mouseX, mouseY, data);

        // Chart Page Switcher (< Chart 1/4 >) if current sub-group spans multiple pages
        if (pages.size() > 1) {
            int pagerX = wikiBtnX - 92;
            String pLbl = "Chart " + (this.selectedChartPageIndex + 1) + "/" + pages.size();
            drawInteractivePill(g, pagerX, barY, 16, 18, 0xFF10182C, 0xFF475569, "<", mouseX, mouseY, () -> {
                this.selectedChartPageIndex = (this.selectedChartPageIndex - 1 + pages.size()) % pages.size();
                playPageTurnSound();
            });
            int pw = this.font.width(pLbl);
            g.drawString(this.font, pLbl, pagerX + 18 + (52 - pw) / 2, barY + 5, 0xFFE2E8F0, false);
            drawInteractivePill(g, pagerX + 72, barY, 16, 18, 0xFF10182C, 0xFF475569, ">", mouseX, mouseY, () -> {
                this.selectedChartPageIndex = (this.selectedChartPageIndex + 1) % pages.size();
                playPageTurnSound();
            });
        }

        // 2. Sky Chart Frame & Concentric Progression Ellipses
        int skyX = 10;
        int skyY = barY + 23;
        int skyW = this.width - 20;
        int skyH = this.height - skyY - 8;

        int accent = activeGroup.accentColor();
        g.fill(skyX, skyY, skyX + skyW, skyY + skyH, 0x66060A18);
        drawHollowRect(g, skyX, skyY, skyW, skyH, (0x66 << 24) | (accent & 0x00FFFFFF));

        int cx = skyX + skyW / 2;
        int cy = skyY + skyH / 2;
        float maxRx = Math.max(60f, skyW * 0.43f);
        float maxRy = Math.max(42f, skyH * 0.41f);

        // Draw the 3 Concentric Progression Rings (Inner = 0.36, Middle = 0.67, Outer = 0.95)
        float[] ringRadii = {0.36f, 0.67f, 0.95f};
        String[] ringLabels = {"I • Core", "II • Mid", "III • Mastery"};
        for (int rIdx = 0; rIdx < ringRadii.length; rIdx++) {
            float rScale = ringRadii[rIdx];
            drawSegmentedEllipse(g, cx, cy, maxRx * rScale, maxRy * rScale, 52, 0x2694A3B8);
            if (skyH >= 160) {
                g.drawString(this.font, "§8" + ringLabels[rIdx], cx + 4, (int) (cy - maxRy * rScale + 2), 0x5594A3B8, false);
            }
        }

        // Precompute screen coordinates of all stars on this chart
        List<StarPlacement> placements = chartPage.stars();
        int[][] screenCoords = new int[placements.size()][2];
        for (int i = 0; i < placements.size(); i++) {
            StarPlacement sp = placements.get(i);
            screenCoords[i][0] = cx + Math.round(sp.normX() * maxRx);
            screenCoords[i][1] = cy + Math.round(sp.normY() * maxRy);
        }

        // 3. Render Constellation Connecting Lines + Animated Traveling Starlight Beads
        for (int i = 0; i < placements.size(); i++) {
            StarPlacement sp = placements.get(i);
            if (sp.connectToId() == null) continue;

            int parentIdx = -1;
            for (int j = 0; j < placements.size(); j++) {
                if (placements.get(j).article().id().equals(sp.connectToId())) {
                    parentIdx = j;
                    break;
                }
            }
            if (parentIdx < 0) continue;

            int x1 = screenCoords[parentIdx][0];
            int y1 = screenCoords[parentIdx][1];
            int x2 = screenCoords[i][0];
            int y2 = screenCoords[i][1];

            StarState sChild = CodexWikiMetadata.getStarState(sp.article(), data);
            StarState sParent = CodexWikiMetadata.getStarState(placements.get(parentIdx).article(), data);
            boolean litBranch = (sChild == StarState.SHINING && sParent == StarState.SHINING);

            int branchRgb = sp.article().branchColor() & 0x00FFFFFF;
            int lineCol = litBranch ? ((0xCC << 24) | branchRgb) : 0x44475569;
            drawSmoothLine(g, x1, y1, x2, y2, lineCol, !litBranch);

            if (litBranch) {
                float prog = (timeSec * 0.55f + i * 0.21f) % 1.0f;
                int bx = Math.round(Mth.lerp(prog, x1, x2));
                int by = Math.round(Mth.lerp(prog, y1, y2));
                g.fill(bx - 2, by - 2, bx + 3, by + 3, (0x88 << 24) | branchRgb);
                g.fill(bx - 1, by - 1, bx + 2, by + 2, 0xFFFFFFFF);
            }
        }

        // 4. Render Twinkling Constellation Stars
        for (int i = 0; i < placements.size(); i++) {
            StarPlacement sp = placements.get(i);
            WikiArticle art = sp.article();
            int sx = screenCoords[i][0];
            int sy = screenCoords[i][1];

            StarState state = CodexWikiMetadata.getStarState(art, data);
            boolean isCenter = (sp.ringIndex() == 0);
            int hitR = isCenter ? 14 : 10;
            boolean hov = Math.hypot(mouseX - sx, mouseY - sy) <= hitR && isOverlayClear();

            drawConstellationStar(g, sx, sy, isCenter, art.branchColor(), state, hov, timeSec + i * 0.63f);

            // Show subtle title next to Center star or Hovered star so sky stays uncluttered!
            if (isCenter || hov) {
                String lbl = CodexWikiMetadata.getDisplayTitle(art, data);
                int lw = this.font.width(lbl);
                int lx = Mth.clamp(sx + 9, skyX + 4, skyX + skyW - lw - 6);
                int ly = sy - 4;
                g.fill(lx - 2, ly - 1, lx + lw + 2, ly + 9, 0xD8050814);
                g.drawString(this.font, lbl, lx, ly, state == StarState.MYSTERY ? 0xFF94A3B8 : 0xFFF8FAFC, false);
            }

            if (CodexWikiMetadata.canReadArticle(art, data)) {
                frameHitboxes.add(new ClickHitbox(sx - hitR, sy - hitR, hitR * 2, hitR * 2,
                        () -> navigateToWikiArticle(art)));
            }

            if (hov) {
                this.activeHoverCard = buildStarHoverCard(art, state, data);
            }
        }

        // Subtle legend hint in bottom-left of sky frame
        if (skyH >= 150) {
            String hint = CodexWikiMetadata.CAT_LORE.equals(this.selectedCategory)
                    ? "§8✦ Shining = Archived Chapter   •   ? = Read physical Scholar's Journal in-world to unlock"
                    : "§8✦ Shining = Read in order   •   ✧ Dim = Click anytime to read   •   ? = Spot creature in-world";
            g.drawString(this.font, trimWithEllipsis(hint, skyW - 16), skyX + 8, skyY + skyH - 12, 0xFF64748B, false);
        }
    }

    private void renderConstellationSubTabs(GuiGraphics g, List<ConstellationSubGroup> groups,
                                            int startX, int y, int availW, int mouseX, int mouseY,
                                            CodexPlayerData data) {
        if (groups.size() <= 1) {
            ConstellationSubGroup only = groups.get(0);
            String title = "✦ " + this.selectedCategory + " Constellation";
            int w = Math.min(availW, this.font.width(title) + 16);
            drawInteractivePill(g, startX, y, w, 18, 0xFF18243E, only.accentColor(), title, mouseX, mouseY, () -> {});
            return;
        }

        int curX = startX;
        boolean needScrollBtns = groups.size() > 5;
        if (needScrollBtns) {
            drawInteractivePill(g, curX, y, 14, 18, 0xFF0F172A, 0xFF475569, "◀", mouseX, mouseY, () -> {
                this.constellationTabScrollOffset = Math.max(0, this.constellationTabScrollOffset - 1);
            });
            curX += 17;
        }

        int endLimit = startX + availW - (needScrollBtns ? 18 : 0);
        this.constellationTabScrollOffset = Mth.clamp(this.constellationTabScrollOffset, 0, Math.max(0, groups.size() - 1));

        for (int i = this.constellationTabScrollOffset; i < groups.size(); i++) {
            ConstellationSubGroup grp = groups.get(i);
            String lbl = grp.tabTitle();
            if (CodexWikiMetadata.CAT_LORE.equals(this.selectedCategory)) {
                int volNum = i + 1;
                int read = data.getArchivedLoreCountForVolume(volNum);
                int tot = CelestialJournalData.getVolume(volNum).entries().size();
                lbl = grp.tabTitle() + " §7(" + read + "/" + tot + ")";
            }
            int tw = this.font.width(lbl) + 12;
            if (curX + tw > endLimit && i > this.constellationTabScrollOffset) {
                break;
            }
            final int idx = i;
            boolean sel = (i == this.selectedSubGroupIndex);
            drawInteractivePill(g, curX, y, tw, 18,
                    sel ? 0xFF1E2944 : 0xFF0D1326,
                    sel ? grp.accentColor() : 0xFF26334D,
                    lbl, mouseX, mouseY, () -> {
                        this.selectedSubGroupIndex = idx;
                        this.selectedChartPageIndex = 0;
                        playPageTurnSound();
                    });
            curX += tw + 3;
        }

        if (needScrollBtns) {
            drawInteractivePill(g, endLimit + 2, y, 14, 18, 0xFF0F172A, 0xFF475569, "▶", mouseX, mouseY, () -> {
                this.constellationTabScrollOffset = Math.min(groups.size() - 1, this.constellationTabScrollOffset + 1);
            });
        }
    }

    private void drawConstellationStar(GuiGraphics g, int x, int y, boolean isCenter, int branchColor,
                                       StarState state, boolean hovered, float timeSec) {
        int rgb = branchColor & 0x00FFFFFF;
        int baseR = isCenter ? 5 : 3;
        if (hovered) baseR += 2;

        if (state == StarState.SHINING) {
            float twinkle = 1.0f + 0.30f * Mth.sin(timeSec * 4.2f);
            int glowR = Math.round((baseR + 4) * twinkle);
            int flareLen = Math.round((baseR + 5) * twinkle);

            // Multi-layered starlight glow
            g.fill(x - glowR, y - glowR, x + glowR + 1, y + glowR + 1, (0x36 << 24) | rgb);
            g.fill(x - baseR - 1, y - baseR - 1, x + baseR + 2, y + baseR + 2, (0xAA << 24) | rgb);

            // 4-point twinkling star flare rays
            g.fill(x - flareLen, y, x + flareLen + 1, y + 1, 0xDDFFFFFF);
            g.fill(x, y - flareLen, x + 1, y + flareLen + 1, 0xDDFFFFFF);

            // Hot white star core
            g.fill(x - baseR + 1, y - baseR + 1, x + baseR, y + baseR, 0xFFFFFFFF);
        } else if (state == StarState.PARTIAL) {
            g.fill(x - baseR - 2, y - baseR - 2, x + baseR + 3, y + baseR + 3, 0x44F59E0B);
            g.fill(x - baseR, y - baseR, x + baseR + 1, y + baseR + 1, 0xFFFBBF24);
        } else if (state == StarState.DIM) {
            int dimCol = hovered ? ((0xEE << 24) | rgb) : 0xFF64748B;
            g.fill(x - baseR - 1, y - baseR - 1, x + baseR + 2, y + baseR + 2, (0x33 << 24) | rgb);
            g.fill(x - baseR, y - baseR, x + baseR + 1, y + baseR + 1, dimCol);
        } else {
            // MYSTERY (???)
            g.fill(x - baseR - 1, y - baseR - 1, x + baseR + 2, y + baseR + 2, 0xAA1E293B);
            drawHollowRect(g, x - baseR - 1, y - baseR - 1, baseR * 2 + 3, baseR * 2 + 3, 0xFF475569);
            g.drawString(this.font, "?", x - 2, y - 3, 0xFF94A3B8, false);
        }
    }

    private HoverCardInfo buildStarHoverCard(WikiArticle art, StarState state, CodexPlayerData data) {
        if (state == StarState.MYSTERY) {
            String hint = art.isLoreEntry()
                    ? "§6🔒 Unread Chapter — Open Scholar's Journal Vol. " + CelestialJournalData.getVolume(art.loreVolume()).roman() + " in-game to archive!"
                    : "§c🔒 Uncharted Creature — Spot this creature in the world to unlock!";
            return new HoverCardInfo(
                    new ItemStack(Items.BOOK),
                    "???",
                    art.isLoreEntry() ? "Faded astral pages awaiting discovery." : "Unknown living creature.",
                    hint,
                    0xFF64748B,
                    true
            );
        }

        if (state == StarState.PARTIAL) {
            return new HoverCardInfo(
                    art.icon(),
                    art.title(),
                    art.summary(),
                    "§e👁 Spotted (Partial) — Click to read overview; defeat/tame for full entry",
                    0xFFF59E0B,
                    false
            );
        }

        if (state == StarState.DIM) {
            String parentTitle = "previous star";
            if (art.prerequisiteId() != null) {
                WikiArticle p = CodexWikiMetadata.getArticle(art.prerequisiteId());
                if (p != null) parentTitle = p.title();
            }
            return new HoverCardInfo(
                    art.icon(),
                    art.title(),
                    art.summary(),
                    "§b✧ Open Wiki (Click to read) §8• §7Read '" + parentTitle + "' first to ignite star",
                    art.branchColor(),
                    false
            );
        }

        return new HoverCardInfo(
                art.icon(),
                art.title(),
                art.summary(),
                "§a✦ Shining Star  •  Click to open in Two-Page Wiki Tome",
                art.branchColor(),
                false
        );
    }

    // =========================================================================
    // STAGE 3: MODERN WIKI READER — OPTION 4 (Left Sidebar + Two-Page Tome)
    // =========================================================================

    private void renderWikiReaderOption4(GuiGraphics g, int mouseX, int mouseY, float timeSec) {
        CodexPlayerData data = CodexPlayerData.CLIENT_DATA;

        int outerPad = 6;
        int bodyY = TOP_BAR_H + 5;
        int bodyH = this.height - bodyY - 6;

        // 1. Left Collapsible Sub-Folder Sidebar
        int sideW = Mth.clamp((int) (this.width * 0.23f), 118, 176);
        int sideX = outerPad;
        renderWikiLeftSidebar(g, sideX, bodyY, sideW, bodyH, mouseX, mouseY, data);

        // 2. Two-Page Arcane Tome Spread (Right ~76% of screen)
        int tomeX = sideX + sideW + 6;
        int tomeW = this.width - tomeX - outerPad;
        renderTwoPageArcaneTome(g, tomeX, bodyY, tomeW, bodyH, mouseX, mouseY, data);
    }

    private void renderWikiLeftSidebar(GuiGraphics g, int x, int y, int w, int h,
                                       int mouseX, int mouseY, CodexPlayerData data) {
        g.fill(x, y, x + w, y + h, 0xF2080B18);
        drawHollowRect(g, x, y, w, h, 0xFF1E293B);

        // Top button: [✦ View Constellation]
        int catCol = CodexWikiMetadata.getCategoryColor(this.selectedCategory);
        drawInteractivePill(g, x + 4, y + 4, w - 8, 17, 0xFF162038, catCol,
                "✦ View Constellation", mouseX, mouseY,
                () -> navigateToConstellation(this.selectedCategory, this.selectedSubGroupIndex, this.selectedChartPageIndex));

        // Chapter Cycle Header (< MAGIC >)
        List<String> allCats = CodexWikiMetadata.ALL_WIKI_CATEGORIES;
        int catIdx = Math.max(0, allCats.indexOf(this.selectedCategory));
        int hdrY = y + 24;

        drawInteractivePill(g, x + 4, hdrY, 14, 15, 0xFF0F172A, 0xFF334155, "◀", mouseX, mouseY, () -> {
            String prevCat = allCats.get((catIdx - 1 + allCats.size()) % allCats.size());
            switchSidebarCategory(prevCat, data);
        });
        drawInteractivePill(g, x + w - 18, hdrY, 14, 15, 0xFF0F172A, 0xFF334155, "▶", mouseX, mouseY, () -> {
            String nextCat = allCats.get((catIdx + 1) % allCats.size());
            switchSidebarCategory(nextCat, data);
        });

        String catShort = CAT_DISPLAY_LABEL(this.selectedCategory).toUpperCase(Locale.ROOT);
        int maxCatW = w - 40;
        String fitCat = trimWithEllipsis(catShort, maxCatW);
        int fcW = this.font.width(fitCat);
        g.drawString(this.font, fitCat, x + (w - fcW) / 2, hdrY + 4, catCol, false);

        g.fill(x + 4, y + 42, x + w - 4, y + 43, 0xFF1E293B);

        // Scrollable Collapsible Sub-Folder Tree
        int listY = y + 45;
        int listH = h - 48;
        Map<String, List<WikiArticle>> folderMap = CodexWikiMetadata.getSubFolderTree(this.selectedCategory);

        // Measure total height
        int totalTreeH = 4;
        for (Map.Entry<String, List<WikiArticle>> entry : folderMap.entrySet()) {
            totalTreeH += 17;
            if (!this.collapsedFolders.contains(folderKey(this.selectedCategory, entry.getKey()))) {
                totalTreeH += entry.getValue().size() * 16;
            }
        }
        this.maxSidebarScroll = Math.max(0, totalTreeH - listH);
        this.sidebarScrollOffset = Mth.clamp(this.sidebarScrollOffset, 0, this.maxSidebarScroll);

        g.enableScissor(x + 1, listY, x + w - 1, listY + listH);
        int drawY = listY + 2 - (int) this.sidebarScrollOffset;

        for (Map.Entry<String, List<WikiArticle>> entry : folderMap.entrySet()) {
            String folder = entry.getKey();
            List<WikiArticle> items = entry.getValue();
            String fKey = folderKey(this.selectedCategory, folder);
            boolean collapsed = this.collapsedFolders.contains(fKey);

            // Render Folder Row
            if (drawY + 16 >= listY && drawY <= listY + listH) {
                boolean fHov = isInside(mouseX, mouseY, x + 3, Math.max(listY, drawY), w - 8, 15)
                        && mouseY >= listY && mouseY < listY + listH && isOverlayClear();
                g.fill(x + 3, drawY, x + w - 5, drawY + 15, fHov ? 0xFF17223B : 0xFF0E1528);
                String arrow = collapsed ? "▶ " : "▼ ";
                String fLabel = arrow + folder + " (" + items.size() + ")";
                g.drawString(this.font, trimWithEllipsis(fLabel, w - 14), x + 6, drawY + 4, 0xFF93C5FD, false);

                final int capturedY = drawY;
                frameHitboxes.add(new ClickHitbox(x + 3, Math.max(listY, capturedY), w - 8, 15, () -> {
                    if (this.collapsedFolders.contains(fKey)) this.collapsedFolders.remove(fKey);
                    else this.collapsedFolders.add(fKey);
                }));
            }
            drawY += 17;

            if (!collapsed) {
                for (WikiArticle art : items) {
                    if (drawY + 15 >= listY && drawY <= listY + listH) {
                        boolean isSel = (this.selectedArticle != null && this.selectedArticle.id().equals(art.id()));
                        boolean iHov = isInside(mouseX, mouseY, x + 5, Math.max(listY, drawY), w - 10, 15)
                                && mouseY >= listY && mouseY < listY + listH && isOverlayClear();

                        if (isSel || iHov) {
                            g.fill(x + 5, drawY, x + w - 5, drawY + 15, isSel ? 0xFF1E3050 : 0xFF141E36);
                        }
                        if (isSel) {
                            g.fill(x + 5, drawY, x + 7, drawY + 15, 0xFF00E5FF);
                        }

                        StarState st = CodexWikiMetadata.getStarState(art, data);
                        boolean saved = data.isBookmarked(art.id());
                        String badge = saved ? "§6★ " : switch (st) {
                            case SHINING -> "§b✦ ";
                            case PARTIAL -> "§e👁 ";
                            case DIM -> "§7✧ ";
                            case MYSTERY -> "§8? ";
                        };
                        String title = CodexWikiMetadata.getDisplayTitle(art, data);
                        int txtCol = isSel ? 0xFF00E5FF : (st == StarState.MYSTERY ? 0xFF64748B : 0xFFE2E8F0);
                        g.drawString(this.font, trimWithEllipsis(badge + title, w - 16), x + 9, drawY + 4, txtCol, false);

                        if (CodexWikiMetadata.canReadArticle(art, data)) {
                            final int rowTop = Math.max(listY, drawY);
                            frameHitboxes.add(new ClickHitbox(x + 5, rowTop, w - 10, 15, () -> navigateToWikiArticle(art)));
                        }
                    }
                    drawY += 16;
                }
            }
        }
        g.disableScissor();

        // Thin scrollbar
        if (this.maxSidebarScroll > 0) {
            drawVerticalScrollbar(g, x + w - 3, listY, listH, this.sidebarScrollOffset, this.maxSidebarScroll, 0xFF00D9FF);
        }
    }

    private void switchSidebarCategory(String newCat, CodexPlayerData data) {
        this.selectedCategory = newCat;
        this.sidebarScrollOffset = 0f;
        for (WikiArticle candidate : CodexWikiMetadata.getArticlesForCategory(newCat)) {
            if (CodexWikiMetadata.canReadArticle(candidate, data)) {
                navigateToWikiArticle(candidate);
                return;
            }
        }
        this.selectedArticle = CodexWikiMetadata.getArticlesForCategory(newCat).stream().findFirst().orElse(null);
        syncArticleWidgets();
        playPageTurnSound();
    }

    // ────────────────── Two-Page Arcane Tome Spread (Option 4) ──────────────────

    private void renderTwoPageArcaneTome(GuiGraphics g, int x, int y, int w, int h,
                                         int mouseX, int mouseY, CodexPlayerData data) {
        boolean isLore = (this.selectedArticle != null && this.selectedArticle.isLoreEntry());
        CelestialJournalData.JournalVolume loreVol = isLore
                ? CelestialJournalData.getVolume(this.selectedArticle.loreVolume())
                : null;

        int coverCol = loreVol != null ? (0xFF000000 | loreVol.guiCoverColor()) : 0xF60B132B;
        int trimCol = loreVol != null ? (0xFF000000 | loreVol.guiTrimColor()) : 0xFFD4AF37;
        int spineCol = loreVol != null ? (0xFF000000 | loreVol.guiAccentColor()) : 0xFF38BDF8;

        // Outer Tome Cover + Metallic Brass Frame & Corner Brackets
        g.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x66000000);
        g.fill(x, y, x + w, y + h, coverCol);
        drawHollowRect(g, x, y, w, h, trimCol);
        drawHollowRect(g, x + 1, y + 1, w - 2, h - 2, (0x88 << 24) | (trimCol & 0x00FFFFFF));

        // Inner Left & Right Arcane Tome Pages
        int pageMargin = 6;
        int spineX = x + w / 2;
        int lpX = x + pageMargin;
        int lpW = (w / 2) - pageMargin - 4;
        int rpX = spineX + 4;
        int rpW = (x + w - pageMargin) - rpX;
        int pY = y + pageMargin;
        int pH = h - pageMargin * 2;

        g.fill(lpX, pY, lpX + lpW, pY + pH, 0xFA0E152A);
        g.fill(rpX, pY, rpX + rpW, pY + pH, 0xFA0E152A);
        drawHollowRect(g, lpX, pY, lpW, pH, 0xFF1E2D4D);
        drawHollowRect(g, rpX, pY, rpW, pH, 0xFF1E2D4D);

        // Glowing Center Binding Spine + Brass Stitches + Hanging Ribbon Tail
        g.fill(spineX - 1, y + 3, spineX + 1, y + h - 3, spineCol);
        for (int sy = pY + 12; sy < pY + pH - 10; sy += 24) {
            g.fill(spineX - 3, sy, spineX + 3, sy + 2, trimCol);
        }
        g.fill(spineX - 2, y + h - 2, spineX + 2, y + h + 5, spineCol);

        if (this.selectedArticle == null || !CodexWikiMetadata.canReadArticle(this.selectedArticle, data)) {
            renderLockedTomePlaceholder(g, lpX, pY, lpW, pH);
            return;
        }

        // Render Left Page (Article Header + Rich Scrollable Text / Journal Lore + Prev/Next)
        renderTomeLeftPage(g, lpX + 6, pY + 6, lpW - 12, pH - 12, mouseX, mouseY, data, trimCol, spineCol);

        // Render Right Page (Recipes + Interactive Blueprints/Tools + Linked Chapters)
        renderTomeRightPage(g, rpX + 6, pY + 6, rpW - 12, pH - 12, mouseX, mouseY, data, trimCol, spineCol);
    }

    private void renderLockedTomePlaceholder(GuiGraphics g, int lpX, int pY, int lpW, int pH) {
        String msg = (this.selectedArticle != null && this.selectedArticle.isLoreEntry())
                ? "Read physical Scholar's Journals in-game to archive chapters here!"
                : "Discover this entry in the world to unlock its pages.";
        var lines = this.font.split(Component.literal("§7" + msg), Math.max(80, lpW - 20));
        int ty = pY + pH / 2 - (lines.size() * 6);
        for (var l : lines) {
            g.drawString(this.font, l, lpX + 12, ty, 0xFF94A3B8, false);
            ty += 12;
        }
    }

    private void renderTomeLeftPage(GuiGraphics g, int x, int y, int w, int h,
                                    int mouseX, int mouseY, CodexPlayerData data,
                                    int trimCol, int accentCol) {
        WikiArticle art = this.selectedArticle;
        if (art == null) return;

        // 1. Header: Icon + Title + Bookmark Button
        g.renderItem(art.icon(), x, y);

        boolean isSaved = data.isBookmarked(art.id());
        int saveBtnW = 48;
        int saveBtnX = x + w - saveBtnW;
        drawInteractivePill(g, saveBtnX, y, saveBtnW, 16,
                isSaved ? 0xFF33240D : 0xFF121B33,
                isSaved ? 0xFFF59E0B : 0xFF334155,
                isSaved ? "§6★ Saved" : "§7☆ Save",
                mouseX, mouseY, this::toggleBookmarkForCurrent);

        int titleMaxW = Math.max(40, saveBtnX - (x + 20) - 4);
        g.drawString(this.font, trimWithEllipsis(art.title(), titleMaxW), x + 20, y + 2, 0xFF00E5FF, true);

        StarState st = CodexWikiMetadata.getStarState(art, data);
        String subMeta = art.subFolder() + (st == StarState.SHINING ? "  §a✦ Lit" : (st == StarState.PARTIAL ? "  §e👁 Partial" : "  §7✧ Open"));
        g.drawString(this.font, trimWithEllipsis(subMeta, titleMaxW + saveBtnW), x + 20, y + 12, 0xFF94A3B8, false);

        g.fill(x, y + 22, x + w, y + 23, 0xFF243456);

        // 2. Scrollable Article Content Viewport (leaving 16px at bottom for < Prev | Next >)
        int viewY = y + 26;
        int footerH = 16;
        int viewH = Math.max(30, h - 26 - footerH);

        g.enableScissor(x, viewY, x + w, viewY + viewH);
        int curY = viewY - (int) this.leftPageScrollOffset;

        if (art.isLoreEntry()) {
            curY = renderLoreJournalEntryBody(g, art, x, curY, w - 6, viewY, viewY + viewH, trimCol, accentCol);
        } else {
            curY = renderStandardArticleBody(g, art, st, x, curY, w - 6, viewY, viewY + viewH, mouseX, mouseY, data);
        }

        g.disableScissor();

        int contentTotalH = (curY + (int) this.leftPageScrollOffset) - viewY;
        this.maxLeftPageScroll = Math.max(0, contentTotalH - viewH + 6);
        this.leftPageScrollOffset = Mth.clamp(this.leftPageScrollOffset, 0, this.maxLeftPageScroll);

        if (this.maxLeftPageScroll > 0) {
            drawVerticalScrollbar(g, x + w - 2, viewY, viewH, this.leftPageScrollOffset, this.maxLeftPageScroll, accentCol);
        }

        // 3. Bottom Prev / Next Readable Entry Steppers
        renderLeftPageFooterNav(g, art, x, y + h - 14, w, mouseX, mouseY, data);
    }

    private int renderStandardArticleBody(GuiGraphics g, WikiArticle art, StarState starState,
                                          int x, int textY, int wrapW, int clipTop, int clipBot,
                                          int mouseX, int mouseY, CodexPlayerData data) {
        // Spell Research Insight Badge for Spellcrafting articles
        if (art.id().startsWith("spell") && this.minecraft != null && this.minecraft.player != null) {
            var mode = SpellResearchManager.getActiveUnlockMode();
            String badge = "§d✦ Spell Research Mode: §f" + mode.getDisplayName();
            if (textY + 14 >= clipTop && textY <= clipBot) {
                g.fill(x, textY, x + wrapW, textY + 13, 0xFF1D1436);
                g.drawString(this.font, trimWithEllipsis(badge, wrapW - 8), x + 4, textY + 3, 0xFFE9D5FF, false);
            }
            textY += 17;
        }

        // Partial Mob Bestiary Notice
        boolean redactLateSections = (art.isMobBestiaryEntry() && starState == StarState.PARTIAL);
        if (redactLateSections) {
            if (textY + 14 >= clipTop && textY <= clipBot) {
                g.fill(x, textY, x + wrapW, textY + 14, 0xFF2B1F0E);
                g.drawString(this.font, trimWithEllipsis("§e👁 Partial Entry — Defeat/tame for full notes", wrapW - 8), x + 4, textY + 3, 0xFFFDE047, false);
            }
            textY += 18;
        }

        // Enrich Magic Circles / Circuits articles with full specifications from CodexArticlesBridge
        String fullText = CodexArticlesBridge.getExpandedContent(art);
        String[] paragraphs = fullText.split("\n");
        int sectionIdx = 0;

        for (String rawPara : paragraphs) {
            String trimmed = rawPara.trim();
            if (trimmed.isEmpty()) {
                textY += 5;
                continue;
            }

            // Markdown / Colon Section Headers
            if (trimmed.startsWith("## ") || trimmed.startsWith("### ") || trimmed.endsWith(":")) {
                sectionIdx++;
                textY += 3;
                String cleanHdr = trimmed.replaceFirst("^#+\\s*", "");
                if (cleanHdr.endsWith(":")) cleanHdr = cleanHdr.substring(0, cleanHdr.length() - 1);

                String iconPrefix = "§b§l✦ ";
                if (cleanHdr.startsWith("Origin") || cleanHdr.contains("Obtaining")) iconPrefix = "§6§l🗺 ";
                else if (cleanHdr.startsWith("Crafting") || cleanHdr.startsWith("Weaving")) iconPrefix = "§e§l🛠 ";
                else if (cleanHdr.startsWith("Special") || cleanHdr.contains("Properties")) iconPrefix = "§d§l✨ ";

                if (textY + 12 >= clipTop && textY <= clipBot) {
                    g.drawString(this.font, trimWithEllipsis(iconPrefix + cleanHdr, wrapW), x, textY, 0xFFFFFFFF, true);
                }
                textY += 13;

                if (redactLateSections && sectionIdx >= 2) {
                    if (textY + 12 >= clipTop && textY <= clipBot) {
                        g.drawString(this.font, "§8🔒 [Complete encounter in-world to reveal]", x + 4, textY, 0xFF64748B, false);
                    }
                    textY += 14;
                }
                continue;
            }

            if (redactLateSections && sectionIdx >= 2) {
                continue;
            }

            // Detect inline [[Wiki Links]] inside paragraph!
            List<WikiArticle> inlineTargets = extractInlineWikiLinks(trimmed, data);
            String renderedPara = replaceWikiLinkBrackets(trimmed);

            var wrapped = this.font.split(Component.literal(renderedPara), Math.max(60, wrapW));
            for (FormattedCharSequence line : wrapped) {
                if (textY + 11 >= clipTop && textY <= clipBot) {
                    g.drawString(this.font, line, x, textY, 0xFFE2E8F0, false);
                }
                textY += 11;
            }

            // Render clickable inline link chips directly beneath paragraphs containing [[...]]
            if (!inlineTargets.isEmpty()) {
                int chipX = x + 2;
                for (WikiArticle target : inlineTargets) {
                    String chipText = "§b↗ " + target.title();
                    int cw = Math.min(wrapW - 4, this.font.width(chipText) + 8);
                    if (chipX + cw > x + wrapW) {
                        chipX = x + 2;
                        textY += 14;
                    }
                    if (textY + 13 >= clipTop && textY <= clipBot) {
                        boolean hov = isInside(mouseX, mouseY, chipX, textY, cw, 12)
                                && mouseY >= clipTop && mouseY <= clipBot && isOverlayClear();
                        g.fill(chipX, textY, chipX + cw, textY + 12, hov ? 0xFF1E3A5F : 0xFF122238);
                        drawHollowRect(g, chipX, textY, cw, 12, 0xFF38BDF8);
                        g.drawString(this.font, trimWithEllipsis(chipText, cw - 6), chipX + 4, textY + 2, 0xFF7DD3FC, false);

                        int hitTop = Math.max(clipTop, textY);
                        int hitBot = Math.min(clipBot, textY + 12);
                        if (hitBot > hitTop) {
                            leftPageLinkHitboxes.add(new ClickHitbox(chipX, hitTop, cw, hitBot - hitTop,
                                    () -> navigateToWikiArticle(target)));
                        }
                    }
                    chipX += cw + 4;
                }
                textY += 15;
            } else {
                textY += 3;
            }
        }

        return textY;
    }

    private int renderLoreJournalEntryBody(GuiGraphics g, WikiArticle art,
                                           int x, int textY, int wrapW, int clipTop, int clipBot,
                                           int trimCol, int accentCol) {
        CelestialJournalData.JournalVolume vol = CelestialJournalData.getVolume(art.loreVolume());
        if (art.loreEntryIndex() < 0 || art.loreEntryIndex() >= vol.entries().size()) return textY;
        CelestialJournalData.JournalEntry entry = vol.entries().get(art.loreEntryIndex());

        // Ceremonial Volume Epigraph on Entry I
        if (art.loreEntryIndex() == 0 && !vol.epigraph().isEmpty()) {
            for (String ep : vol.epigraph()) {
                boolean attr = ep.startsWith("—") || ep.startsWith("--");
                MutableComponent comp = CelestialJournalData.parseStyledMarkdown(ep, attr ? 0xFFFCD34D : 0xFFBAE6FD, !attr);
                for (FormattedCharSequence seq : this.font.split(comp, Math.max(60, wrapW - 8))) {
                    if (textY + 10 >= clipTop && textY <= clipBot) {
                        g.drawString(this.font, seq, x + 4, textY, 0xFFBAE6FD, false);
                    }
                    textY += 10;
                }
            }
            if (textY + 6 >= clipTop && textY <= clipBot) {
                g.fill(x + 8, textY + 3, x + wrapW - 8, textY + 4, trimCol);
            }
            textY += 9;
        }

        if (entry.part() != null && !entry.part().isEmpty()) {
            if (textY + 10 >= clipTop && textY <= clipBot) {
                g.drawString(this.font, trimWithEllipsis("§6§l" + entry.part().toUpperCase(Locale.ROOT), wrapW), x, textY, trimCol, false);
            }
            textY += 12;
        }

        for (CelestialJournalData.EntryBlock block : entry.blocks()) {
            if (block.text() == null || block.text().isEmpty()) continue;
            String type = block.type();

            switch (type) {
                case "subheading" -> {
                    textY += 3;
                    MutableComponent sub = CelestialJournalData.parseStyledMarkdown("**" + block.text() + "**", 0xFFFCD34D, false);
                    for (FormattedCharSequence seq : this.font.split(sub, Math.max(60, wrapW))) {
                        if (textY + 11 >= clipTop && textY <= clipBot) {
                            g.drawString(this.font, seq, x, textY, 0xFFFCD34D, false);
                        }
                        textY += 11;
                    }
                    textY += 2;
                }
                case "equation" -> {
                    textY += 2;
                    var eqLines = this.font.split(Component.literal(block.text()), Math.max(50, wrapW - 10));
                    int eqH = eqLines.size() * 11 + 6;
                    if (textY + eqH >= clipTop && textY <= clipBot) {
                        g.fill(x + 2, textY, x + wrapW - 2, textY + eqH, 0xFF13203B);
                        drawHollowRect(g, x + 2, textY, wrapW - 4, eqH, accentCol);
                    }
                    textY += 3;
                    for (FormattedCharSequence seq : eqLines) {
                        if (textY + 11 >= clipTop && textY <= clipBot) {
                            g.drawString(this.font, seq, x + 6, textY, 0xFF7DD3FC, false);
                        }
                        textY += 11;
                    }
                    textY += 5;
                }
                case "quote" -> {
                    MutableComponent q = CelestialJournalData.parseStyledMarkdown(block.text(), 0xFFD8B4FE, true);
                    for (FormattedCharSequence seq : this.font.split(q, Math.max(50, wrapW - 10))) {
                        if (textY + 10 >= clipTop && textY <= clipBot) {
                            g.fill(x + 1, textY, x + 3, textY + 9, accentCol);
                            g.drawString(this.font, seq, x + 7, textY, 0xFFD8B4FE, false);
                        }
                        textY += 10;
                    }
                    textY += 4;
                }
                case "bullet" -> {
                    MutableComponent b = Component.literal("• ").withStyle(s -> s.withBold(true).withColor(0xFFFCD34D))
                            .append(CelestialJournalData.parseStyledMarkdown(block.text(), 0xFFE2E8F0, false));
                    for (FormattedCharSequence seq : this.font.split(b, Math.max(50, wrapW - 6))) {
                        if (textY + 10 >= clipTop && textY <= clipBot) {
                            g.drawString(this.font, seq, x + 2, textY, 0xFFE2E8F0, false);
                        }
                        textY += 10;
                    }
                    textY += 2;
                }
                default -> {
                    MutableComponent p = CelestialJournalData.parseStyledMarkdown(block.text(), 0xFFE2E8F0, false);
                    for (FormattedCharSequence seq : this.font.split(p, Math.max(50, wrapW))) {
                        if (textY + 10 >= clipTop && textY <= clipBot) {
                            g.drawString(this.font, seq, x, textY, 0xFFE2E8F0, false);
                        }
                        textY += 10;
                    }
                    textY += 4;
                }
            }
        }
        return textY;
    }

    private void renderLeftPageFooterNav(GuiGraphics g, WikiArticle current, int x, int y, int w,
                                         int mouseX, int mouseY, CodexPlayerData data) {
        List<WikiArticle> readableInCat = new ArrayList<>();
        for (WikiArticle a : CodexWikiMetadata.getArticlesForCategory(this.selectedCategory)) {
            if (CodexWikiMetadata.canReadArticle(a, data)) {
                readableInCat.add(a);
            }
        }
        int idx = readableInCat.indexOf(current);
        if (idx < 0 || readableInCat.size() <= 1) return;

        if (idx > 0) {
            WikiArticle prev = readableInCat.get(idx - 1);
            drawInteractivePill(g, x, y, 52, 13, 0xFF10192E, 0xFF334155, "§b← Prev", mouseX, mouseY,
                    () -> navigateToWikiArticle(prev));
        }
        String posStr = (idx + 1) + " / " + readableInCat.size();
        int pw = this.font.width(posStr);
        g.drawString(this.font, posStr, x + (w - pw) / 2, y + 3, 0xFF64748B, false);

        if (idx < readableInCat.size() - 1) {
            WikiArticle next = readableInCat.get(idx + 1);
            drawInteractivePill(g, x + w - 52, y, 52, 13, 0xFF10192E, 0xFF334155, "§bNext →", mouseX, mouseY,
                    () -> navigateToWikiArticle(next));
        }
    }

    // ────────────────── Right Tome Page (Recipes, Tools & Related Links) ──────────────────

    private void renderTomeRightPage(GuiGraphics g, int x, int y, int w, int h,
                                     int mouseX, int mouseY, CodexPlayerData data,
                                     int trimCol, int accentCol) {
        WikiArticle art = this.selectedArticle;
        if (art == null) return;

        this.lastBlueprintRenderX = -1;
        this.lastInlineFusionX = -1;

        g.enableScissor(x, y, x + w, y + h);
        int curY = y - (int) this.rightPageScrollOffset;
        int innerW = Math.max(120, w - 4);

        // 1. Lore Volume Archive Card (when viewing a Scholar's Journal chapter)
        if (art.isLoreEntry()) {
            CelestialJournalData.JournalVolume vol = CelestialJournalData.getVolume(art.loreVolume());
            int archived = data.getArchivedLoreCountForVolume(art.loreVolume());
            int total = vol.entries().size();

            int cardH = 88;
            g.fill(x, curY, x + innerW, curY + cardH, 0xEB10182E);
            drawHollowRect(g, x, curY, innerW, cardH, trimCol);
            g.renderItem(art.icon(), x + 6, curY + 6);
            g.drawString(this.font, trimWithEllipsis("§6✦ Volume " + vol.roman(), innerW - 30), x + 26, curY + 6, trimCol, false);
            g.drawString(this.font, trimWithEllipsis("§f" + vol.title(), innerW - 30), x + 26, curY + 16, 0xFFFFFFFF, false);
            g.drawString(this.font, trimWithEllipsis("§7Author: §e" + vol.author(), innerW - 12), x + 6, curY + 32, 0xFFCBD5E1, false);
            g.drawString(this.font, "§7Archived: §b" + archived + " / " + total + " Chapters", x + 6, curY + 46, 0xFF94A3B8, false);

            int barW = innerW - 14;
            int fillW = total > 0 ? Math.round(((float) archived / total) * barW) : 0;
            g.fill(x + 6, curY + 60, x + 6 + barW, curY + 67, 0xFF1E293B);
            g.fill(x + 6, curY + 60, x + 6 + fillW, curY + 67, accentCol);
            g.drawString(this.font, trimWithEllipsis("§8" + vol.subtitle(), innerW - 12), x + 6, curY + 73, 0xFF64748B, false);

            curY += cardH + 8;
        }

        // 2. Visual Crafting / Synthesizer / Lathe / Ritual Recipe Card
        if (this.recipeWidget.hasRecipes()) {
            this.recipeWidget.render(g, this.font, x, curY, innerW, mouseX, mouseY, data);
            curY += this.recipeWidget.getPreferredHeight() + 8;
        }

        // 3. Context-Aware Interactive Tool Widget
        if (this.blueprintWidget != null) {
            this.lastBlueprintRenderX = x;
            this.lastBlueprintRenderY = curY;
            this.blueprintWidget.renderAdaptive(g, x, curY, innerW, mouseX, mouseY);
            curY += 138;
        } else if ("materia_essences".equals(art.id()) || "hub_materia".equals(art.id())) {
            this.lastInlineFusionX = x;
            this.lastInlineFusionY = curY;
            this.fusionTab.render(g, x, curY, innerW, 158, mouseX, mouseY);
            curY += 164;
        } else if ("materia_logistics".equals(art.id())) {
            this.diagnosticsTab.render(g, x, curY, innerW, 106);
            curY += 112;
        } else if (!art.isLoreEntry() && !this.recipeWidget.hasRecipes()) {
            // Quick Summary / Metadata Infobox Card when an article has no direct crafting recipe
            int infoH = 54;
            g.fill(x, curY, x + innerW, curY + infoH, 0xE60B1124);
            drawHollowRect(g, x, curY, innerW, infoH, 0xFF243456);
            g.drawString(this.font, "§b✦ Quick Reference", x + 6, curY + 6, 0xFF38BDF8, false);
            var sumLines = this.font.split(Component.literal("§7" + art.summary()), Math.max(70, innerW - 12));
            int sy = curY + 19;
            for (int i = 0; i < Math.min(3, sumLines.size()); i++) {
                g.drawString(this.font, sumLines.get(i), x + 6, sy, 0xFFCBD5E1, false);
                sy += 10;
            }
            curY += infoH + 8;
        }

        // 4. Linked & Related Chapters Section (Cross-Category Wiki Navigation!)
        List<WikiArticle> related = CodexWikiMetadata.getRelatedArticles(art, data, 6);
        if (!related.isEmpty()) {
            g.drawString(this.font, "§b🔗 Linked & Related Pages", x + 2, curY + 2, 0xFF38BDF8, false);
            curY += 14;

            for (WikiArticle rel : related) {
                int pillH = 18;
                if (curY + pillH >= y && curY <= y + h) {
                    boolean hov = isInside(mouseX, mouseY, x, curY, innerW, pillH)
                            && mouseY >= y && mouseY <= y + h && isOverlayClear();
                    int relCol = CodexWikiMetadata.getCategoryColor(rel.category());
                    g.fill(x, curY, x + innerW, curY + pillH, hov ? 0xFF192847 : 0xFF0F182D);
                    drawHollowRect(g, x, curY, innerW, pillH, hov ? relCol : 0xFF243456);
                    g.fill(x, curY, x + 3, curY + pillH, relCol);

                    String tag = "[" + CAT_DISPLAY_LABEL(rel.category()) + "]";
                    String label = "→ " + rel.title() + " §8" + tag;
                    g.drawString(this.font, trimWithEllipsis(label, innerW - 12), x + 7, curY + 5,
                            hov ? 0xFF00E5FF : 0xFFE2E8F0, false);

                    int hitTop = Math.max(y, curY);
                    int hitBot = Math.min(y + h, curY + pillH);
                    if (hitBot > hitTop) {
                        rightPageLinkHitboxes.add(new ClickHitbox(x, hitTop, innerW, hitBot - hitTop,
                                () -> navigateToWikiArticle(rel)));
                    }
                }
                curY += pillH + 3;
            }
        }

        g.disableScissor();

        int rightContentH = (curY + (int) this.rightPageScrollOffset) - y;
        this.maxRightPageScroll = Math.max(0, rightContentH - h + 6);
        this.rightPageScrollOffset = Mth.clamp(this.rightPageScrollOffset, 0, this.maxRightPageScroll);

        if (this.maxRightPageScroll > 0) {
            drawVerticalScrollbar(g, x + w - 2, y, h, this.rightPageScrollOffset, this.maxRightPageScroll, accentCol);
        }
    }

    // =========================================================================
    // OVERLAYS: LIVE SEARCH, BOOKMARKS & TECHNOMANTIC TOOLS
    // =========================================================================

    private void renderSearchResultsOverlay(GuiGraphics g, String rawQuery, int mouseX, int mouseY) {
        CodexPlayerData data = CodexPlayerData.CLIENT_DATA;
        String q = rawQuery.toLowerCase(Locale.ROOT);

        List<WikiArticle> matches = new ArrayList<>();
        for (WikiArticle a : CodexWikiMetadata.getAllArticles()) {
            // Never spoil unread Lore chapters or unseen Mobs via search!
            if (!CodexWikiMetadata.canReadArticle(a, data)) continue;
            if (a.title().toLowerCase(Locale.ROOT).contains(q)
                    || a.summary().toLowerCase(Locale.ROOT).contains(q)
                    || a.subFolder().toLowerCase(Locale.ROOT).contains(q)
                    || a.category().toLowerCase(Locale.ROOT).contains(q)
                    || a.content().toLowerCase(Locale.ROOT).contains(q)) {
                matches.add(a);
            }
        }

        int panelW = Math.min(this.width - 24, 420);
        int panelH = Math.min(this.height - TOP_BAR_H - 16, 230);
        int panelX = (this.width - panelW) / 2;
        int panelY = TOP_BAR_H + 4;

        g.fill(0, TOP_BAR_H, this.width, this.height, 0x9903050C);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFA0A1022);
        drawHollowRect(g, panelX, panelY, panelW, panelH, 0xFF00D9FF);

        g.drawString(this.font, "§b🔍 Search Results: §f" + matches.size() + " found", panelX + 10, panelY + 7, 0xFFFFFFFF, false);
        drawInteractivePill(g, panelX + panelW - 46, panelY + 4, 40, 14, 0xFF1E293B, 0xFF64748B, "Clear", mouseX, mouseY, () -> {
            this.searchBox.setValue("");
            this.searchBox.setFocused(false);
        });
        g.fill(panelX + 6, panelY + 21, panelX + panelW - 6, panelY + 22, 0xFF1E293B);

        int listY = panelY + 25;
        int listH = panelH - 30;
        int rowH = 24;
        float maxScroll = Math.max(0, matches.size() * (rowH + 2) - listH);
        this.searchScrollOffset = Mth.clamp(this.searchScrollOffset, 0, maxScroll);

        g.enableScissor(panelX + 4, listY, panelX + panelW - 4, listY + listH);
        int ry = listY - (int) this.searchScrollOffset;
        for (WikiArticle art : matches) {
            if (ry + rowH >= listY && ry <= listY + listH) {
                boolean hov = isInside(mouseX, mouseY, panelX + 6, ry, panelW - 14, rowH)
                        && mouseY >= listY && mouseY <= listY + listH;
                int catCol = CodexWikiMetadata.getCategoryColor(art.category());
                g.fill(panelX + 6, ry, panelX + panelW - 8, ry + rowH, hov ? 0xFF182848 : 0xFF0F172C);
                g.fill(panelX + 6, ry, panelX + 9, ry + rowH, catCol);
                g.renderItem(art.icon(), panelX + 12, ry + 4);

                String line1 = art.title() + "  §8[" + CAT_DISPLAY_LABEL(art.category()) + " › " + art.subFolder() + "]";
                g.drawString(this.font, trimWithEllipsis(line1, panelW - 44), panelX + 32, ry + 3, hov ? 0xFF00E5FF : 0xFFFFFFFF, false);
                g.drawString(this.font, trimWithEllipsis(art.summary(), panelW - 44), panelX + 32, ry + 13, 0xFF94A3B8, false);

                int hitTop = Math.max(listY, ry);
                int hitBot = Math.min(listY + listH, ry + rowH);
                if (hitBot > hitTop) {
                    frameHitboxes.add(new ClickHitbox(panelX + 6, hitTop, panelW - 14, hitBot - hitTop,
                            () -> navigateToWikiArticle(art)));
                }
            }
            ry += rowH + 2;
        }
        g.disableScissor();

        if (maxScroll > 0) {
            drawVerticalScrollbar(g, panelX + panelW - 5, listY, listH, this.searchScrollOffset, maxScroll, 0xFF00D9FF);
        }
    }

    private void renderBookmarksOverlay(GuiGraphics g, int mouseX, int mouseY) {
        CodexPlayerData data = CodexPlayerData.CLIENT_DATA;
        List<WikiArticle> saved = new ArrayList<>();
        for (String id : data.getBookmarkedTopics()) {
            WikiArticle art = CodexWikiMetadata.getArticle(id);
            if (art != null && CodexWikiMetadata.canReadArticle(art, data)) {
                saved.add(art);
            }
        }

        int panelW = Math.min(this.width - 28, 360);
        int panelH = Math.min(this.height - TOP_BAR_H - 20, 210);
        int panelX = (this.width - panelW) / 2;
        int panelY = TOP_BAR_H + 8;

        g.fill(0, TOP_BAR_H, this.width, this.height, 0x9903050C);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFA0C1124);
        drawHollowRect(g, panelX, panelY, panelW, panelH, 0xFFC084FC);

        g.drawString(this.font, "§d★ Bookmarked Pages (" + saved.size() + ")", panelX + 10, panelY + 7, 0xFFFFFFFF, false);
        drawInteractivePill(g, panelX + panelW - 22, panelY + 4, 16, 14, 0xFF2E1A47, 0xFFC084FC, "✕", mouseX, mouseY,
                () -> this.activeOverlay = OverlayMode.NONE);
        g.fill(panelX + 6, panelY + 21, panelX + panelW - 6, panelY + 22, 0xFF1E293B);

        if (saved.isEmpty()) {
            g.drawString(this.font, "§7No pages saved yet. Click '☆ Save' on any wiki page!", panelX + 14, panelY + 42, 0xFF94A3B8, false);
            return;
        }

        int listY = panelY + 26;
        int listH = panelH - 32;
        int rowH = 22;
        float maxScroll = Math.max(0, saved.size() * (rowH + 2) - listH);
        this.bookmarkScrollOffset = Mth.clamp(this.bookmarkScrollOffset, 0, maxScroll);

        g.enableScissor(panelX + 4, listY, panelX + panelW - 4, listY + listH);
        int ry = listY - (int) this.bookmarkScrollOffset;
        for (WikiArticle art : saved) {
            if (ry + rowH >= listY && ry <= listY + listH) {
                boolean hov = isInside(mouseX, mouseY, panelX + 6, ry, panelW - 14, rowH)
                        && mouseY >= listY && mouseY <= listY + listH;
                g.fill(panelX + 6, ry, panelX + panelW - 8, ry + rowH, hov ? 0xFF1D2847 : 0xFF10182E);
                g.renderItem(art.icon(), panelX + 10, ry + 3);
                g.drawString(this.font, trimWithEllipsis("§6★ §f" + art.title() + " §8[" + CAT_DISPLAY_LABEL(art.category()) + "]", panelW - 42),
                        panelX + 30, ry + 7, 0xFFFFFFFF, false);

                int hitTop = Math.max(listY, ry);
                int hitBot = Math.min(listY + listH, ry + rowH);
                if (hitBot > hitTop) {
                    frameHitboxes.add(new ClickHitbox(panelX + 6, hitTop, panelW - 14, hitBot - hitTop,
                            () -> navigateToWikiArticle(art)));
                }
            }
            ry += rowH + 2;
        }
        g.disableScissor();
    }

    private void renderToolsOverlay(GuiGraphics g, int mouseX, int mouseY) {
        int panelW = Math.min(this.width - 24, 380);
        int panelH = Math.min(this.height - TOP_BAR_H - 16, 212);
        int panelX = (this.width - panelW) / 2;
        int panelY = TOP_BAR_H + 8;

        g.fill(0, TOP_BAR_H, this.width, this.height, 0xAA03050C);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFA0A1122);
        drawHollowRect(g, panelX, panelY, panelW, panelH, 0xFF2DD4BF);

        drawInteractivePill(g, panelX + 8, panelY + 5, 132, 16,
                this.activeToolsModalTab == 0 ? 0xFF133A42 : 0xFF0F172A, 0xFF2DD4BF,
                "⚗ Fusion Calculator", mouseX, mouseY, () -> this.activeToolsModalTab = 0);
        drawInteractivePill(g, panelX + 144, panelY + 5, 138, 16,
                this.activeToolsModalTab == 1 ? 0xFF133A42 : 0xFF0F172A, 0xFF2DD4BF,
                "📡 Terminal Telemetry", mouseX, mouseY, () -> this.activeToolsModalTab = 1);
        drawInteractivePill(g, panelX + panelW - 24, panelY + 5, 16, 16, 0xFF7F1D1D, 0xFFF87171, "✕", mouseX, mouseY,
                () -> this.activeOverlay = OverlayMode.NONE);

        int contentX = panelX + 8;
        int contentY = panelY + 26;
        int contentW = panelW - 16;
        int contentH = panelH - 34;

        if (this.activeToolsModalTab == 0) {
            this.fusionTab.render(g, contentX, contentY, contentW, contentH, mouseX, mouseY);
        } else {
            this.diagnosticsTab.render(g, contentX, contentY, contentW, contentH);
        }
    }

    // =========================================================================
    // INPUT EVENTS (Mouse Clicks, Scrolling & Keyboard Shortcuts)
    // =========================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        // Mouse Back / Forward side buttons (buttons 3 & 4)
        if (button == 3) {
            stepHistory(-1);
            return true;
        }
        if (button == 4) {
            stepHistory(1);
            return true;
        }

        // Let EditBox receive focus / clicks first when clicking inside top bar search box
        if (this.searchBox != null && isInside(mx, my, this.searchBox.getX(), this.searchBox.getY(), this.searchBox.getWidth(), this.searchBox.getHeight())) {
            this.activeOverlay = OverlayMode.NONE;
            return super.mouseClicked(event, doubleClick);
        } else if (this.searchBox != null) {
            this.searchBox.setFocused(false);
        }

        // Tools Modal internal clicks
        if (this.activeOverlay == OverlayMode.TOOLS && this.activeToolsModalTab == 0) {
            int panelW = Math.min(this.width - 24, 380);
            int panelX = (this.width - panelW) / 2;
            int panelY = TOP_BAR_H + 8;
            if (this.fusionTab.mouseClicked(mx, my, button, panelX + 8, panelY + 26)) {
                return true;
            }
        }

        // Check registered frame hitboxes (reverse order so top-most overlays win!)
        for (int i = this.frameHitboxes.size() - 1; i >= 0; i--) {
            ClickHitbox box = this.frameHitboxes.get(i);
            if (box.contains(mx, my)) {
                box.onClick().run();
                return true;
            }
        }

        // Wiki Reader interactive elements (Left Page links, Right Page recipes, blueprints, tools & related links)
        if (this.currentMode == CodexMode.WIKI_READER && isOverlayClear()) {
            for (ClickHitbox linkBox : this.leftPageLinkHitboxes) {
                if (linkBox.contains(mx, my)) {
                    linkBox.onClick().run();
                    return true;
                }
            }

            if (this.recipeWidget.hasRecipes()) {
                final boolean[] cycled = {false};
                WikiArticle jumpTarget = this.recipeWidget.handleMouseClicked(mx, my, CodexPlayerData.CLIENT_DATA, () -> cycled[0] = true);
                if (cycled[0]) {
                    playPageTurnSound();
                    return true;
                }
                if (jumpTarget != null) {
                    navigateToWikiArticle(jumpTarget);
                    return true;
                }
            }

            if (this.blueprintWidget != null && this.lastBlueprintRenderX >= 0) {
                if (this.blueprintWidget.mouseClicked(mx, my, this.lastBlueprintRenderX, this.lastBlueprintRenderY)) {
                    return true;
                }
            }

            if (this.lastInlineFusionX >= 0) {
                if (this.fusionTab.mouseClicked(mx, my, button, this.lastInlineFusionX, this.lastInlineFusionY)) {
                    return true;
                }
            }

            for (ClickHitbox relBox : this.rightPageLinkHitboxes) {
                if (relBox.contains(mx, my)) {
                    relBox.onClick().run();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        String query = this.searchBox != null ? this.searchBox.getValue().trim() : "";
        if (!query.isEmpty()) {
            this.searchScrollOffset = Math.max(0f, this.searchScrollOffset - (float) scrollY * 18f);
            return true;
        }
        if (this.activeOverlay == OverlayMode.BOOKMARKS) {
            this.bookmarkScrollOffset = Math.max(0f, this.bookmarkScrollOffset - (float) scrollY * 18f);
            return true;
        }
        if (this.activeOverlay != OverlayMode.NONE) {
            return true;
        }

        if (this.currentMode == CodexMode.CHAPTER_CONSTELLATION) {
            List<ConstellationSubGroup> groups = CodexWikiMetadata.buildConstellationGroups(this.selectedCategory);
            if (!groups.isEmpty()) {
                List<ConstellationChartPage> pages = groups.get(this.selectedSubGroupIndex).pages();
                if (pages.size() > 1) {
                    int delta = scrollY < 0 ? 1 : -1;
                    this.selectedChartPageIndex = (this.selectedChartPageIndex + delta + pages.size()) % pages.size();
                    playPageTurnSound();
                    return true;
                }
            }
        }

        if (this.currentMode == CodexMode.WIKI_READER) {
            int sideW = Mth.clamp((int) (this.width * 0.23f), 118, 176);
            int tomeX = 6 + sideW + 6;
            int spineX = tomeX + (this.width - tomeX - 6) / 2;

            if (mouseX < tomeX) {
                this.sidebarScrollOffset = Mth.clamp(this.sidebarScrollOffset - (float) scrollY * 18f, 0f, this.maxSidebarScroll);
            } else if (mouseX < spineX) {
                this.leftPageScrollOffset = Mth.clamp(this.leftPageScrollOffset - (float) scrollY * 18f, 0f, this.maxLeftPageScroll);
            } else {
                this.rightPageScrollOffset = Mth.clamp(this.rightPageScrollOffset - (float) scrollY * 18f, 0f, this.maxRightPageScroll);
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (this.searchBox != null && !this.searchBox.getValue().isEmpty()) {
                this.searchBox.setValue("");
                this.searchBox.setFocused(false);
                return true;
            }
            if (this.activeOverlay != OverlayMode.NONE) {
                this.activeOverlay = OverlayMode.NONE;
                return true;
            }
        }

        if (this.searchBox != null && this.searchBox.isFocused()) {
            return super.keyPressed(event);
        }

        if (key == GLFW.GLFW_KEY_BACKSPACE && this.historyCursor > 0) {
            stepHistory(-1);
            return true;
        }

        return super.keyPressed(event);
    }

    // =========================================================================
    // LOW-LEVEL DRAWING & TEXT UTILITIES
    // =========================================================================

    private boolean isOverlayClear() {
        String q = this.searchBox != null ? this.searchBox.getValue().trim() : "";
        return q.isEmpty() && this.activeOverlay == OverlayMode.NONE;
    }

    private void drawInteractivePill(GuiGraphics g, int x, int y, int w, int h,
                                     int bg, int border, String text,
                                     int mouseX, int mouseY, Runnable onClick) {
        boolean hov = isInside(mouseX, mouseY, x, y, w, h);
        int fill = hov ? brightenColor(bg, 26) : bg;
        g.fill(x, y, x + w, y + h, fill);
        drawHollowRect(g, x, y, w, h, hov ? 0xFFFFFFFF : border);

        String fit = trimWithEllipsis(text, w - 6);
        int tw = this.font.width(fit);
        g.drawString(this.font, fit, x + (w - tw) / 2, y + (h - 8) / 2, 0xFFF8FAFC, false);

        frameHitboxes.add(new ClickHitbox(x, y, w, h, onClick));
    }

    private void renderHoverCard(GuiGraphics g, int mouseX, int mouseY, HoverCardInfo info) {
        int maxW = Math.min(240, this.width - 20);
        var sumLines = this.font.split(Component.literal(info.summary()), maxW - 14);
        var statLines = this.font.split(Component.literal(info.statusLine()), maxW - 14);

        int cardH = 24 + sumLines.size() * 10 + 4 + statLines.size() * 10 + 6;
        int bx = Mth.clamp(mouseX + 12, 6, Math.max(6, this.width - maxW - 6));
        int by = Mth.clamp(mouseY - 16, TOP_BAR_H + 4, Math.max(TOP_BAR_H + 4, this.height - cardH - 6));

        g.fill(bx, by, bx + maxW, by + cardH, 0xF6080C1C);
        drawHollowRect(g, bx, by, maxW, cardH, (0xFF << 24) | (info.accentColor() & 0x00FFFFFF));

        int titleX = bx + 6;
        if (!info.hideIcon() && !info.icon().isEmpty()) {
            g.renderItem(info.icon(), bx + 5, by + 4);
            titleX = bx + 24;
        }
        g.drawString(this.font, trimWithEllipsis(info.title(), maxW - (titleX - bx) - 6),
                titleX, by + 8, (0xFF << 24) | (info.accentColor() & 0x00FFFFFF), true);

        int ty = by + 23;
        for (var l : sumLines) {
            g.drawString(this.font, l, bx + 7, ty, 0xFFE2E8F0, false);
            ty += 10;
        }
        ty += 3;
        for (var sl : statLines) {
            g.drawString(this.font, sl, bx + 7, ty, 0xFF94A3B8, false);
            ty += 10;
        }
    }

    private static List<WikiArticle> extractInlineWikiLinks(String text, CodexPlayerData data) {
        List<WikiArticle> out = new ArrayList<>();
        Matcher m = WIKI_LINK_PATTERN.matcher(text);
        while (m.find()) {
            WikiArticle resolved = CodexWikiMetadata.resolveWikiLink(m.group(1), data);
            if (resolved != null && !out.contains(resolved)) {
                out.add(resolved);
            }
        }
        return out;
    }

    private static String replaceWikiLinkBrackets(String text) {
        Matcher m = WIKI_LINK_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String raw = m.group(1);
            int pipe = raw.indexOf('|');
            String disp = pipe > 0 ? raw.substring(pipe + 1).trim() : raw.trim();
            m.appendReplacement(sb, Matcher.quoteReplacement("§b§n" + disp + "§r"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String trimWithEllipsis(String text, int maxW) {
        if (maxW <= 12 || this.font.width(text) <= maxW) return text;
        String cur = text;
        while (cur.length() > 3 && this.font.width(cur + "..") > maxW) {
            cur = cur.substring(0, cur.length() - 1);
        }
        return cur + "..";
    }

    private static void drawVerticalScrollbar(GuiGraphics g, int x, int y, int h, float scroll, float maxScroll, int color) {
        g.fill(x, y, x + 2, y + h, 0xFF10172A);
        float ratio = (float) h / (h + maxScroll);
        int thumbH = Math.max(14, Math.round(h * ratio));
        int thumbY = y + Math.round((h - thumbH) * (scroll / maxScroll));
        g.fill(x, thumbY, x + 2, thumbY + thumbH, (0xFF << 24) | (color & 0x00FFFFFF));
    }

    private static void drawSegmentedEllipse(GuiGraphics g, int cx, int cy, float rx, float ry, int segments, int color) {
        int prevX = cx + Math.round(rx);
        int prevY = cy;
        for (int i = 1; i <= segments; i++) {
            double a = (i * Math.PI * 2.0) / segments;
            int nx = cx + (int) Math.round(Math.cos(a) * rx);
            int ny = cy + (int) Math.round(Math.sin(a) * ry);
            if (i % 2 == 0) {
                drawSmoothLine(g, prevX, prevY, nx, ny, color, false);
            }
            prevX = nx;
            prevY = ny;
        }
    }

    private static void drawSmoothLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color, boolean dashed) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;

        int stride = 3;
        for (int i = 0; i <= steps; i += stride) {
            if (dashed && ((i / 6) % 2 == 1)) continue;
            float t = (float) i / steps;
            int px = Math.round(x1 + dx * t);
            int py = Math.round(y1 + dy * t);
            g.fill(px, py, px + 2, py + 2, color);
        }
    }

    private static void drawHollowRect(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int brightenColor(int argb, int delta) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(255, ((argb >>> 16) & 0xFF) + delta);
        int gr = Math.min(255, ((argb >>> 8) & 0xFF) + delta);
        int b = Math.min(255, (argb & 0xFF) + delta);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }
}
