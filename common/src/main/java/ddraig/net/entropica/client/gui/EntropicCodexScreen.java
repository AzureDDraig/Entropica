package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.codex.CodexCategoryRegistry;
import ddraig.net.entropica.codex.CodexCategoryRegistry.CodexNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntropicCodexScreen extends Screen {

    public enum CodexMode {
        SPATIAL_GRID,
        BOOK_CATEGORY,
        BOOK_INDEX
    }

    private static final ResourceLocation BLACK_HOLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/black_hole_bg.png");

    private final EssenceType highestEssence;

    private CodexMode currentMode = CodexMode.SPATIAL_GRID;
    private String selectedCategory = "GETTING STARTED";

    private float panX = 0f;
    private float panY = 0f;
    private float targetPanX = 0f;
    private float targetPanY = 0f;
    private float zoom = 0.45f;
    private float targetZoom = 0.45f;

    private boolean isDragging = false;

    private CodexNode selectedNode = null;
    private float panelScrollOffset = 0f;
    private float categoryScrollOffset = 0f;
    private float indexScrollOffset = 0f;

    private EditBox searchBox;
    private final Map<String, float[]> nodePositions = new HashMap<>();

    private final String[] categories = {
            "GETTING STARTED", "MATERIALS", "MATERIA",
            "MACHINERY", "MULTIBLOCKS", "ENVIRONMENT & NATURE", "MAGIC"
    };
    private final String[] hubIds = {
            "hub_getting_started", "hub_materials", "hub_materia",
            "hub_machinery", "hub_multiblocks", "hub_environment", "hub_magic"
    };

    public EntropicCodexScreen() {
        this(null);
    }

    public EntropicCodexScreen(EssenceType essenceType) {
        super(Component.translatable("msg.entropica.entropic_codex"));
        this.highestEssence = essenceType;
    }

    @Override
    protected void init() {
        super.init();

        // Calculate non-overlapping node positions for Spatial Grid mode
        calculateNodePositions();

        int searchBoxX = 165;
        int searchBoxY = 48;
        this.searchBox = new EditBox(this.font, searchBoxX, searchBoxY, 220, 18, Component.translatable("msg.entropica.search"));
        this.searchBox.setMaxLength(30);
        this.searchBox.setHint(Component.translatable("msg.entropica.search_codex_entries"));
        this.searchBox.setVisible(false);
        this.addRenderableWidget(this.searchBox);

        // Default selected node to first hub if none selected
        if (selectedNode == null) {
            selectedNode = CodexCategoryRegistry.getNodeById("hub_getting_started");
        }

        // Center view on open with comfortable initial zoom showing all 7 Hubs
        this.targetPanX = 0;
        this.targetPanY = 0;
        this.panX = 0;
        this.panY = 0;
        this.targetZoom = 0.45f;
        this.zoom = 0.45f;
    }

    private void calculateNodePositions() {
        nodePositions.clear();

        for (CodexNode node : CodexCategoryRegistry.ALL_NODES) {
            float x = (float) (Math.cos(node.currentAngle) * node.orbitRadius);
            float y = (float) (Math.sin(node.currentAngle) * node.orbitRadius);
            nodePositions.put(node.id, new float[]{x, y});
        }

        int iterations = 35;
        for (int iter = 0; iter < iterations; iter++) {
            for (int i = 0; i < CodexCategoryRegistry.ALL_NODES.size(); i++) {
                CodexNode nodeA = CodexCategoryRegistry.ALL_NODES.get(i);
                float[] posA = nodePositions.get(nodeA.id);

                for (int j = i + 1; j < CodexCategoryRegistry.ALL_NODES.size(); j++) {
                    CodexNode nodeB = CodexCategoryRegistry.ALL_NODES.get(j);
                    float[] posB = nodePositions.get(nodeB.id);

                    float dx = posB[0] - posA[0];
                    float dy = posB[1] - posA[1];
                    float distSq = dx * dx + dy * dy;

                    float minDist = (nodeA.isParentHub || nodeB.isParentHub) ? 95.0f : 75.0f;

                    if (distSq < minDist * minDist) {
                        float dist = (float) Math.sqrt(distSq);
                        if (dist < 0.001f) {
                            dx = 1.0f;
                            dy = 0.0f;
                            dist = 1.0f;
                        }
                        float overlap = 0.5f * (minDist - dist);
                        float pushX = (dx / dist) * overlap;
                        float pushY = (dy / dist) * overlap;

                        if (!nodeA.isParentHub) {
                            posA[0] -= pushX;
                            posA[1] -= pushY;
                        }
                        if (!nodeB.isParentHub) {
                            posB[0] += pushX;
                            posB[1] += pushY;
                        }
                    }
                }
            }
        }
    }

    private float[] getNodeWorldPos(CodexNode node) {
        return nodePositions.getOrDefault(node.id, new float[]{
                (float) (Math.cos(node.currentAngle) * node.orbitRadius),
                (float) (Math.sin(node.currentAngle) * node.orbitRadius)
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (currentMode == CodexMode.SPATIAL_GRID) {
            zoom = Mth.lerp(0.30f, zoom, targetZoom);
            panX = Mth.lerp(0.30f, panX, targetPanX);
            panY = Mth.lerp(0.30f, panY, targetPanY);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Dark Base Background Fill
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF050711);

        // 2. Render Full-Screen Clamped Background Image
        guiGraphics.blit(BLACK_HOLE_TEXTURE, 0, 0, this.width, this.height, 0.0f, 1.0f, 0.0f, 1.0f);

        // Update search box visibility
        this.searchBox.setVisible(currentMode == CodexMode.BOOK_INDEX);

        // 3. Render Mode Specific Content
        if (currentMode == CodexMode.SPATIAL_GRID) {
            renderSpatialGridMode(guiGraphics, mouseX, mouseY);
        } else if (currentMode == CodexMode.BOOK_CATEGORY) {
            renderBookCategoryMode(guiGraphics, mouseX, mouseY);
        } else if (currentMode == CodexMode.BOOK_INDEX) {
            renderBookIndexMode(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 4. Render Top Header Banner (ENTROPIC CODEX Title + Red X Close)
        renderHeaderBanner(guiGraphics);

        // 5. Render Screen Edge Category & Special Tabs
        renderEdgeCategoryTabs(guiGraphics, mouseX, mouseY);
    }

    // ────────────────── Mode 1: Spatial Grid Mode ──────────────────

    private void renderSpatialGridMode(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        float screenCenterX = this.width / 2.0f + panX * zoom;
        float screenCenterY = this.height / 2.0f + panY * zoom;

        float worldMouseX = (float) ((mouseX - screenCenterX) / zoom);
        float worldMouseY = (float) ((mouseY - screenCenterY) / zoom);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(screenCenterX, screenCenterY);
        guiGraphics.pose().scale(zoom, zoom);

        renderConnectingLinesWorld(guiGraphics, screenCenterX, screenCenterY);
        CodexNode hoveredNode = renderNodesTreeWorld(guiGraphics, worldMouseX, worldMouseY, screenCenterX, screenCenterY);

        guiGraphics.pose().popMatrix();

        // Tooltip for hovered node on spatial grid
        if (hoveredNode != null) {
            Component titleComp = Component.literal("§b" + hoveredNode.title);
            Component sumComp = Component.literal("§7" + hoveredNode.summary);
            int w = Math.max(this.font.width(titleComp), this.font.width(sumComp));
            guiGraphics.fill(mouseX + 8, mouseY - 20, mouseX + 14 + w, mouseY + 10, 0xF0100010);
            guiGraphics.fill(mouseX + 9, mouseY - 19, mouseX + 13 + w, mouseY + 9, 0x505000FF);
            guiGraphics.drawString(this.font, titleComp, mouseX + 10, mouseY - 16, 0xFFFFFFFF, true);
            guiGraphics.drawString(this.font, sumComp, mouseX + 10, mouseY - 4, 0xFFAAAABB, true);
        }
    }

    // ────────────────── Mode 2: Book Category Mode ──────────────────

    private void renderBookCategoryMode(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int bookX = 155;
        int bookY = 36;
        int bookW = this.width - 165;
        int bookH = this.height - 44;

        // Main Book Container Frame
        guiGraphics.fill(bookX, bookY, bookX + bookW, bookY + bookH, 0xF00A0C18);
        guiGraphics.fill(bookX, bookY, bookX + bookW, bookY + 2, getCategoryColor(selectedCategory));

        // Left Chapter / Article Selector Sidebar (Width: 160px)
        int sideW = 160;
        guiGraphics.fill(bookX, bookY, bookX + sideW, bookY + bookH, 0xF0070912);
        guiGraphics.fill(bookX + sideW - 1, bookY, bookX + sideW, bookY + bookH, 0xFF1C2438);

        // Sidebar Header
        guiGraphics.fill(bookX, bookY, bookX + sideW, bookY + 26, 0xF00D1122);
        guiGraphics.drawString(this.font, selectedCategory, bookX + 8, bookY + 8, getCategoryColor(selectedCategory), true);

        // List nodes belonging to this category
        List<CodexNode> catNodes = new ArrayList<>();
        for (CodexNode n : CodexCategoryRegistry.ALL_NODES) {
            if (n.category.equals(selectedCategory)) {
                catNodes.add(n);
            }
        }

        int itemH = 22;
        int itemSpacing = 2;
        int listY = bookY + 28;
        int listH = bookH - 30;
        int totalListH = catNodes.size() * (itemH + itemSpacing) + 6;
        float maxScroll = Math.max(0, totalListH - listH);
        this.categoryScrollOffset = Mth.clamp(this.categoryScrollOffset, 0, maxScroll);

        // Scissor clip the category entry list viewport
        guiGraphics.enableScissor(bookX, listY, bookX + sideW, bookY + bookH);

        int itemY = listY + 2 - (int) categoryScrollOffset;
        int listRightPad = (totalListH > listH) ? 6 : 4;

        for (CodexNode node : catNodes) {
            if (itemY + itemH >= listY && itemY <= listY + listH + 4) {
                boolean isSel = (selectedNode != null && selectedNode.id.equals(node.id));
                boolean isHov = (mouseX >= bookX + 4 && mouseX <= bookX + sideW - listRightPad && mouseY >= itemY && mouseY <= itemY + itemH && mouseY >= listY && mouseY <= listY + listH);

                int bgCol = isSel ? 0xFF1E2D4A : (isHov ? 0xFF141F33 : 0x00000000);
                if (bgCol != 0) {
                    guiGraphics.fill(bookX + 4, itemY, bookX + sideW - listRightPad, itemY + itemH, bgCol);
                }
                if (isSel) {
                    guiGraphics.fill(bookX + 4, itemY, bookX + 7, itemY + itemH, 0xFF00D9FF);
                }

                guiGraphics.renderItem(node.icon, bookX + 8, itemY + 3);

                // Format & truncate title text if overflowing sidebar width
                String titleStr = node.title;
                int maxTextW = sideW - listRightPad - 32;
                if (this.font.width(titleStr) > maxTextW) {
                    while (titleStr.length() > 3 && this.font.width(titleStr + "...") > maxTextW) {
                        titleStr = titleStr.substring(0, titleStr.length() - 1);
                    }
                    titleStr = titleStr + "...";
                }
                guiGraphics.drawString(this.font, titleStr, bookX + 28, itemY + 7, isSel ? 0xFF00D9FF : (isHov ? 0xFFFFFFFF : 0xFFCCCCCC), true);
            }

            itemY += itemH + itemSpacing;
        }

        guiGraphics.disableScissor();

        // Render scrollbar on the right border of the sidebar when entries exceed height
        if (totalListH > listH) {
            int scrollbarX = bookX + sideW - 4;
            int scrollbarW = 3;
            guiGraphics.fill(scrollbarX, listY, scrollbarX + scrollbarW, listY + listH, 0xFF101422);

            float viewRatio = (float) listH / totalListH;
            int thumbH = Math.max(16, (int) (listH * viewRatio));
            float scrollProgress = (maxScroll > 0) ? (categoryScrollOffset / maxScroll) : 0f;
            int thumbY = listY + (int) ((listH - thumbH) * scrollProgress);

            guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarW, thumbY + thumbH, 0xFF00D9FF);
        }

        // Right Main Reading Page Area
        if (selectedNode != null) {
            int pageX = bookX + sideW + 12;
            int pageY = bookY + 12;
            int pageW = bookW - sideW - 24;
            int pageH = bookH - 24;

            // Header Banner for current article
            guiGraphics.renderItem(selectedNode.icon, pageX, pageY);
            guiGraphics.drawString(this.font, selectedNode.title, pageX + 22, pageY + 4, 0xFF00D9FF, true);

            // Article Sub-header / Category tag
            guiGraphics.drawString(this.font, "§7Category: §b" + selectedNode.category + " §8| §7Type: " + (selectedNode.isParentHub ? "Hub Overview" : "Research Article"), pageX + 22, pageY + 16, 0xFF8888AA, true);

            guiGraphics.fill(pageX, pageY + 28, pageX + pageW, pageY + 29, 0xFF1C2438);

            // Scissor clip reading area for smooth text scrolling
            int contentY = pageY + 36;
            guiGraphics.enableScissor(pageX, contentY, pageX + pageW, pageY + pageH);

            int textY = contentY - (int) panelScrollOffset;
            String[] paragraphs = selectedNode.content.split("\n");

            for (String paragraph : paragraphs) {
                String trimmed = paragraph.trim();
                if (trimmed.isEmpty()) {
                    textY += 6;
                    continue;
                }

                if (trimmed.endsWith(":")) {
                    textY += 4;
                    String headerText = "§b§l✦ " + trimmed.substring(0, trimmed.length() - 1);
                    if (trimmed.startsWith("Overview")) headerText = "§b§l✦ " + trimmed.substring(0, trimmed.length() - 1);
                    else if (trimmed.startsWith("Origin")) headerText = "§6§l🗺 " + trimmed.substring(0, trimmed.length() - 1);
                    else if (trimmed.startsWith("Crafting &")) headerText = "§e§l🛠 " + trimmed.substring(0, trimmed.length() - 1);
                    else if (trimmed.startsWith("Crafting Uses")) headerText = "§a§l⚙ " + trimmed.substring(0, trimmed.length() - 1);
                    else if (trimmed.startsWith("Special")) headerText = "§d§l✨ " + trimmed.substring(0, trimmed.length() - 1);

                    if (textY >= contentY - 12 && textY <= pageY + pageH) {
                        guiGraphics.drawString(this.font, headerText, pageX, textY, 0xFFFFFFFF, true);
                    }
                    textY += 14;
                    continue;
                }

                var formattedLines = this.font.split(Component.literal(paragraph), pageW - 10);
                for (var line : formattedLines) {
                    if (textY >= contentY - 12 && textY <= pageY + pageH) {
                        guiGraphics.drawString(this.font, line, pageX, textY, 0xFFDDDDDD, true);
                    }
                    textY += 12;
                }
                textY += 4;
            }

            guiGraphics.disableScissor();
        }
    }

    // ────────────────── Mode 3: Book Index Mode ──────────────────

    private void renderBookIndexMode(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int bookX = 155;
        int bookY = 36;
        int bookW = this.width - 165;
        int bookH = this.height - 44;

        // Main Index Frame Container
        guiGraphics.fill(bookX, bookY, bookX + bookW, bookY + bookH, 0xF00A0C18);
        guiGraphics.fill(bookX, bookY, bookX + bookW, bookY + 2, 0xFF00D9FF);

        // Top Search Header
        guiGraphics.drawString(this.font, "CODEX MASTER INDEX & SEARCH", bookX + 12, bookY + 10, 0xFF00D9FF, true);

        // Render Search Edit Box
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);

        // Filter nodes based on search text
        String query = this.searchBox.getValue().trim().toLowerCase();
        List<CodexNode> filtered = new ArrayList<>();
        for (CodexNode n : CodexCategoryRegistry.ALL_NODES) {
            if (query.isEmpty() ||
                n.title.toLowerCase().contains(query) ||
                n.summary.toLowerCase().contains(query) ||
                n.category.toLowerCase().contains(query) ||
                n.content.toLowerCase().contains(query)) {
                filtered.add(n);
            }
        }

        // Count display
        guiGraphics.drawString(this.font, "§7Entries: §b" + filtered.size() + " / " + CodexCategoryRegistry.ALL_NODES.size(), bookX + 400, bookY + 14, 0xFF8888AA, true);
        guiGraphics.fill(bookX + 12, bookY + 36, bookX + bookW - 12, bookY + 37, 0xFF1C2438);

        // Index List View with Scissor Clipping
        int listX = bookX + 12;
        int listY = bookY + 44;
        int listW = bookW - 24;
        int listH = bookH - 52;

        guiGraphics.enableScissor(listX, listY, listX + listW, listY + listH);

        int entryY = listY - (int) indexScrollOffset;
        int entryH = 28;

        for (CodexNode node : filtered) {
            boolean isHovered = (mouseX >= listX && mouseX <= listX + listW && mouseY >= entryY && mouseY <= entryY + entryH);

            if (entryY + entryH >= listY && entryY <= listY + listH) {
                // Background strip
                guiGraphics.fill(listX, entryY, listX + listW, entryY + entryH, isHovered ? 0xFF18243B : 0xF00E1222);
                // Category color accent bar
                guiGraphics.fill(listX, entryY, listX + 3, entryY + entryH, getCategoryColor(node.category));

                // Icon
                guiGraphics.renderItem(node.icon, listX + 8, entryY + 6);

                // Title
                guiGraphics.drawString(this.font, node.title, listX + 30, entryY + 4, isHovered ? 0xFF00D9FF : 0xFFFFFFFF, true);

                // Category Tag
                guiGraphics.drawString(this.font, "[" + node.category + "]", listX + 220, entryY + 4, getCategoryColor(node.category), true);

                // Summary snippet
                String snippet = node.summary;
                if (snippet.length() > 55) snippet = snippet.substring(0, 52) + "...";
                guiGraphics.drawString(this.font, snippet, listX + 30, entryY + 16, 0xFFAAAABB, true);
            }

            entryY += entryH + 4;
        }

        guiGraphics.disableScissor();
    }

    // ────────────────── Shared Edge Category Tabs ──────────────────

    private void renderEdgeCategoryTabs(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startY = 36;
        int tabHeight = 22;
        int tabSpacing = 2;

        // 1. TOP TAB 0: SPATIAL GRID
        int tab0Y = startY;
        boolean isTopHovered = (mouseX >= 4 && mouseX <= 145 && mouseY >= tab0Y && mouseY <= tab0Y + tabHeight);
        boolean isTopActive = (currentMode == CodexMode.SPATIAL_GRID);

        int tab0W = (isTopHovered || isTopActive) ? 145 : 28;
        guiGraphics.fill(4, tab0Y, 4 + tab0W, tab0Y + tabHeight, (isTopHovered || isTopActive) ? 0xF01C2B47 : 0xF0080E1C);
        guiGraphics.fill(4, tab0Y, 7, tab0Y + tabHeight, 0xFF00D9FF);
        guiGraphics.renderItem(new ItemStack(Items.NETHER_STAR), 9, tab0Y + 3);

        if (isTopHovered || isTopActive) {
            guiGraphics.drawString(this.font, "Spatial Grid", 30, tab0Y + 7, isTopActive ? 0xFF00D9FF : 0xFFCCCCCC, true);
        }

        // 2. CATEGORY TABS 1..7
        for (int i = 0; i < categories.length; i++) {
            String cat = categories[i];
            String hubId = hubIds[i];
            CodexNode hubNode = CodexCategoryRegistry.getNodeById(hubId);

            int tabY = startY + (i + 1) * (tabHeight + tabSpacing);
            int catColor = getCategoryColor(cat);

            boolean isHovered = (mouseX >= 4 && mouseX <= 145 && mouseY >= tabY && mouseY <= tabY + tabHeight);
            boolean isActive = (currentMode == CodexMode.BOOK_CATEGORY && cat.equals(selectedCategory));

            int tabW = (isHovered || isActive) ? 145 : 28;

            guiGraphics.fill(4, tabY, 4 + tabW, tabY + tabHeight, (isHovered || isActive) ? 0xF0141A2E : 0xF0080B18);
            guiGraphics.fill(4, tabY, 7, tabY + tabHeight, catColor);

            if (hubNode != null) {
                guiGraphics.renderItem(hubNode.icon, 9, tabY + 3);
            }

            if (isHovered || isActive) {
                Component titleComp = Component.literal(cat);
                guiGraphics.drawString(this.font, titleComp, 30, tabY + 7, isActive ? 0xFF00D9FF : 0xFFCCCCCC, true);
            }
        }

        // 3. BOTTOM TAB 8: INDEX & SEARCH
        int tabBotY = startY + (categories.length + 1) * (tabHeight + tabSpacing);
        boolean isBotHovered = (mouseX >= 4 && mouseX <= 145 && mouseY >= tabBotY && mouseY <= tabBotY + tabHeight);
        boolean isBotActive = (currentMode == CodexMode.BOOK_INDEX);

        int tabBotW = (isBotHovered || isBotActive) ? 145 : 28;
        guiGraphics.fill(4, tabBotY, 4 + tabBotW, tabBotY + tabHeight, (isBotHovered || isBotActive) ? 0xF02E1C3B : 0xF0120B1C);
        guiGraphics.fill(4, tabBotY, 7, tabBotY + tabHeight, 0xFFA020F0);
        guiGraphics.renderItem(new ItemStack(Items.KNOWLEDGE_BOOK), 9, tabBotY + 3);

        if (isBotHovered || isBotActive) {
            guiGraphics.drawString(this.font, "Index & Search", 30, tabBotY + 7, isBotActive ? 0xFFA020F0 : 0xFFCCCCCC, true);
        }
    }

    private void renderConnectingLinesWorld(GuiGraphics guiGraphics, float screenCenterX, float screenCenterY) {
        for (CodexNode node : CodexCategoryRegistry.ALL_NODES) {
            if (node.prerequisiteId != null) {
                CodexNode prereq = CodexCategoryRegistry.getNodeById(node.prerequisiteId);
                if (prereq != null) {
                    float[] pos1 = getNodeWorldPos(node);
                    float[] pos2 = getNodeWorldPos(prereq);

                    int x1 = (int) pos1[0];
                    int y1 = (int) pos1[1];
                    int x2 = (int) pos2[0];
                    int y2 = (int) pos2[1];

                    if (isNodeInViewport(x1, y1, 50, screenCenterX, screenCenterY) ||
                        isNodeInViewport(x2, y2, 50, screenCenterX, screenCenterY)) {
                        int lineColor = getCategoryColor(node.category);
                        drawLineWorld(guiGraphics, x2, y2, x1, y1, lineColor);
                    }
                }
            }
        }
    }

    private boolean isNodeInViewport(int worldX, int worldY, int radius, float screenCenterX, float screenCenterY) {
        float screenX = screenCenterX + worldX * zoom;
        float screenY = screenCenterY + worldY * zoom;
        float r = radius * zoom;

        return (screenX + r >= -50 && screenX - r <= this.width + 50 &&
                screenY + r >= -50 && screenY - r <= this.height + 50);
    }

    private void drawLineWorld(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        if (dx == 0 && dy == 0) return;

        int steps = Math.max(dx, dy);
        int stride = 12;

        float xInc = (float) (x2 - x1) / steps;
        float yInc = (float) (y2 - y1) / steps;

        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i += stride) {
            int nx = (int) (x + xInc * Math.min(stride, steps - i));
            int ny = (int) (y + yInc * Math.min(stride, steps - i));

            int minX = Math.min((int) x, nx) - 1;
            int minY = Math.min((int) y, ny) - 1;
            int maxX = Math.max((int) x, nx) + 1;
            int maxY = Math.max((int) y, ny) + 1;

            guiGraphics.fill(minX, minY, maxX, maxY, color);
            x = nx;
            y = ny;
        }
    }

    private CodexNode renderNodesTreeWorld(GuiGraphics guiGraphics, float worldMouseX, float worldMouseY, float screenCenterX, float screenCenterY) {
        CodexNode hovered = null;

        for (CodexNode node : CodexCategoryRegistry.ALL_NODES) {
            float[] pos = getNodeWorldPos(node);
            int nx = (int) pos[0];
            int ny = (int) pos[1];

            int catColor = getCategoryColor(node.category);

            if (node.isParentHub) {
                int hubR = 24;
                boolean isHovered = (worldMouseX >= nx - hubR && worldMouseX <= nx + hubR &&
                                     worldMouseY >= ny - hubR && worldMouseY <= ny + hubR);

                if (isHovered) hovered = node;

                if (!isNodeInViewport(nx, ny, hubR + 20, screenCenterX, screenCenterY)) {
                    continue;
                }

                drawFilledCircleWorld(guiGraphics, nx, ny, hubR - 4, 0xEE080A14, 0xEE080A14);
                drawSeptagonWorld(guiGraphics, nx, ny, hubR, catColor);
                drawSeptagonWorld(guiGraphics, nx, ny, hubR - 2, catColor);

                guiGraphics.renderItem(node.icon, nx - 8, ny - 8);

                String labelStr = node.title;
                int lw = this.font.width(labelStr);
                int labelY = ny > 0 ? ny + hubR + 4 : ny - hubR - 12;
                guiGraphics.fill(nx - lw / 2 - 4, labelY - 2, nx + lw / 2 + 4, labelY + 10, 0xD0050711);
                guiGraphics.drawString(this.font, labelStr, nx - lw / 2, labelY, 0xFFFFFFFF, true);

            } else {
                int nodeR = 15;
                boolean isHovered = (worldMouseX >= nx - nodeR && worldMouseX <= nx + nodeR &&
                                     worldMouseY >= ny - nodeR && worldMouseY <= ny + nodeR);

                if (isHovered) hovered = node;

                if (!isNodeInViewport(nx, ny, nodeR + 20, screenCenterX, screenCenterY)) {
                    continue;
                }

                drawFilledCircleWorld(guiGraphics, nx, ny, nodeR, catColor, 0xF00D0F1D);
                guiGraphics.renderItem(node.icon, nx - 8, ny - 8);

                String subTitle = node.title;
                int labelX = nx + nodeR + 6;
                int labelY = ny - 4;
                int lw = this.font.width(subTitle);
                guiGraphics.fill(labelX - 2, labelY - 2, labelX + lw + 2, labelY + 10, 0xD0050711);
                guiGraphics.drawString(this.font, subTitle, labelX, labelY, 0xFFE0E0E0, true);
            }
        }

        return hovered;
    }

    private void drawSeptagonWorld(GuiGraphics guiGraphics, int cx, int cy, int radius, int color) {
        int sides = 7;
        for (int i = 0; i < sides; i++) {
            double a1 = Math.toRadians(i * (360.0 / sides) - 90);
            double a2 = Math.toRadians((i + 1) * (360.0 / sides) - 90);

            int x1 = cx + (int) (Math.cos(a1) * radius);
            int y1 = cy + (int) (Math.sin(a1) * radius);

            int x2 = cx + (int) (Math.cos(a2) * radius);
            int y2 = cy + (int) (Math.sin(a2) * radius);

            drawLineWorld(guiGraphics, x1, y1, x2, y2, color);
        }
    }

    private void drawFilledCircleWorld(GuiGraphics guiGraphics, int cx, int cy, int radius, int outlineColor, int fillColor) {
        guiGraphics.fill(cx - radius + 3, cy - radius, cx + radius - 3, cy + radius, fillColor);
        guiGraphics.fill(cx - radius, cy - radius + 3, cx + radius, cy + radius - 3, fillColor);

        for (int i = 0; i < 360; i += 45) {
            double rad = Math.toRadians(i);
            int px = cx + (int) (Math.cos(rad) * radius);
            int py = cy + (int) (Math.sin(rad) * radius);
            guiGraphics.fill(px - 1, py - 1, px + 2, py + 2, outlineColor);
        }
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "GETTING STARTED": return 0xFF00FF00;
            case "MATERIALS": return 0xFFFFA500;
            case "MATERIA": return 0xFF00FFFF;
            case "MACHINERY": return 0xFFFF3333;
            case "MULTIBLOCKS": return 0xFFFFFF00;
            case "ENVIRONMENT & NATURE": return 0xFF33FF33;
            case "MAGIC": return 0xFFA020F0;
            default: return 0xFF00D9FF;
        }
    }

    private void renderHeaderBanner(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, this.width, 32, 0xF0080B18);
        guiGraphics.fill(0, 31, this.width, 33, 0xFF00D9FF);

        // Header Title Banner: ENTROPIC CODEX
        Component titleComp = Component.translatable("msg.entropica.entropic_codex");
        int titleWidth = this.font.width(titleComp);
        guiGraphics.drawString(this.font, titleComp, this.width / 2 - titleWidth / 2, 10, 0xFF00D9FF, true);

        // Red [X] Close Button
        int closeX = this.width - 24;
        int closeY = 6;
        guiGraphics.fill(closeX, closeY, closeX + 18, closeY + 18, 0xFFCC2222);
        guiGraphics.drawString(this.font, "X", closeX + 5, closeY + 5, 0xFFFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        // 1. Red [X] close button
        int closeX = this.width - 24;
        int closeY = 6;
        if (mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18) {
            this.onClose();
            return true;
        }

        // 2. Edge Category & Special Tabs click handling
        int startY = 36;
        int tabHeight = 22;
        int tabSpacing = 2;

        // Top Tab 0: Spatial Grid
        int tab0Y = startY;
        if (mouseX >= 4 && mouseX <= 145 && mouseY >= tab0Y && mouseY <= tab0Y + tabHeight) {
            this.currentMode = CodexMode.SPATIAL_GRID;
            return true;
        }

        // Category Tabs 1..7
        for (int i = 0; i < categories.length; i++) {
            int tabY = startY + (i + 1) * (tabHeight + tabSpacing);
            if (mouseX >= 4 && mouseX <= 145 && mouseY >= tabY && mouseY <= tabY + tabHeight) {
                this.currentMode = CodexMode.BOOK_CATEGORY;
                this.selectedCategory = categories[i];
                CodexNode hubNode = CodexCategoryRegistry.getNodeById(hubIds[i]);
                if (hubNode != null) {
                    this.selectedNode = hubNode;
                }
                this.categoryScrollOffset = 0;
                this.panelScrollOffset = 0;
                return true;
            }
        }

        // Bottom Tab 8: Index & Search
        int tabBotY = startY + (categories.length + 1) * (tabHeight + tabSpacing);
        if (mouseX >= 4 && mouseX <= 145 && mouseY >= tabBotY && mouseY <= tabBotY + tabHeight) {
            this.currentMode = CodexMode.BOOK_INDEX;
            this.searchBox.setFocused(true);
            return true;
        }

        // 3. Book Category Mode Clicks
        if (currentMode == CodexMode.BOOK_CATEGORY) {
            int bookX = 155;
            int bookY = 36;
            int sideW = 160;
            int bookH = this.height - 44;
            int listY = bookY + 28;
            int listH = bookH - 30;

            int itemY = listY + 2 - (int) categoryScrollOffset;
            int itemH = 22;
            int itemSpacing = 2;

            List<CodexNode> catNodes = new ArrayList<>();
            for (CodexNode n : CodexCategoryRegistry.ALL_NODES) {
                if (n.category.equals(selectedCategory)) {
                    catNodes.add(n);
                }
            }

            if (mouseX >= bookX + 4 && mouseX <= bookX + sideW - 4 && mouseY >= listY && mouseY <= listY + listH) {
                for (CodexNode node : catNodes) {
                    if (mouseY >= itemY && mouseY <= itemY + itemH) {
                        this.selectedNode = node;
                        this.panelScrollOffset = 0;
                        return true;
                    }
                    itemY += itemH + itemSpacing;
                }
            }
        }

        // 4. Book Index Mode Clicks
        if (currentMode == CodexMode.BOOK_INDEX) {
            int bookX = 155;
            int bookY = 36;
            int bookW = this.width - 165;
            int bookH = this.height - 44;

            int listX = bookX + 12;
            int listY = bookY + 44;
            int listW = bookW - 24;
            int listH = bookH - 52;

            String query = this.searchBox.getValue().trim().toLowerCase();
            List<CodexNode> filtered = new ArrayList<>();
            for (CodexNode n : CodexCategoryRegistry.ALL_NODES) {
                if (query.isEmpty() ||
                    n.title.toLowerCase().contains(query) ||
                    n.summary.toLowerCase().contains(query) ||
                    n.category.toLowerCase().contains(query) ||
                    n.content.toLowerCase().contains(query)) {
                    filtered.add(n);
                }
            }

            int entryY = listY - (int) indexScrollOffset;
            int entryH = 28;

            for (CodexNode node : filtered) {
                if (mouseX >= listX && mouseX <= listX + listW && mouseY >= entryY && mouseY <= entryY + entryH) {
                    this.selectedNode = node;
                    this.selectedCategory = node.category;
                    this.currentMode = CodexMode.BOOK_CATEGORY;
                    this.panelScrollOffset = 0;

                    // Auto scroll category list to show this node
                    List<CodexNode> catNodes = new ArrayList<>();
                    for (CodexNode n : CodexCategoryRegistry.ALL_NODES) {
                        if (n.category.equals(node.category)) catNodes.add(n);
                    }
                    int nodeIdx = catNodes.indexOf(node);
                    if (nodeIdx >= 0) {
                        int listHeight = (this.height - 44) - 30;
                        int targetY = nodeIdx * 24;
                        int totalListH = catNodes.size() * 24 + 6;
                        float maxScroll = Math.max(0, totalListH - listHeight);
                        this.categoryScrollOffset = Mth.clamp(targetY - listHeight / 3, 0, maxScroll);
                    }

                    return true;
                }
                entryY += entryH + 4;
            }
        }

        // 5. Spatial Grid Mode Node Clicks & Panning
        if (currentMode == CodexMode.SPATIAL_GRID) {
            float screenCenterX = this.width / 2.0f + panX * zoom;
            float screenCenterY = this.height / 2.0f + panY * zoom;
            float worldMouseX = (float) ((mouseX - screenCenterX) / zoom);
            float worldMouseY = (float) ((mouseY - screenCenterY) / zoom);

            for (CodexNode node : CodexCategoryRegistry.ALL_NODES) {
                float[] pos = getNodeWorldPos(node);
                int nx = (int) pos[0];
                int ny = (int) pos[1];

                int r = node.isParentHub ? 24 : 14;
                if (worldMouseX >= nx - r && worldMouseX <= nx + r && worldMouseY >= ny - r && worldMouseY <= ny + r) {
                    this.selectedNode = node;
                    this.selectedCategory = node.category;
                    this.currentMode = CodexMode.BOOK_CATEGORY;
                    this.panelScrollOffset = 0;

                    // Auto scroll category list to show this node
                    List<CodexNode> catNodes = new ArrayList<>();
                    for (CodexNode n : CodexCategoryRegistry.ALL_NODES) {
                        if (n.category.equals(node.category)) catNodes.add(n);
                    }
                    int nodeIdx = catNodes.indexOf(node);
                    if (nodeIdx >= 0) {
                        int listHeight = (this.height - 44) - 30;
                        int targetY = nodeIdx * 24;
                        int totalListH = catNodes.size() * 24 + 6;
                        float maxScroll = Math.max(0, totalListH - listHeight);
                        this.categoryScrollOffset = Mth.clamp(targetY - listHeight / 3, 0, maxScroll);
                    }

                    return true;
                }
            }

            if (button == 0) {
                this.isDragging = true;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.isDragging = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.isDragging && currentMode == CodexMode.SPATIAL_GRID) {
            this.targetPanX += dragX / zoom;
            this.targetPanY += dragY / zoom;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (currentMode == CodexMode.BOOK_CATEGORY) {
            int bookX = 155;
            int sideW = 160;
            if (mouseX >= bookX && mouseX <= bookX + sideW) {
                // Scroll the category entry list on the left
                List<CodexNode> catNodes = new ArrayList<>();
                for (CodexNode n : CodexCategoryRegistry.ALL_NODES) {
                    if (n.category.equals(selectedCategory)) {
                        catNodes.add(n);
                    }
                }
                int listH = (this.height - 44) - 30;
                int totalListH = catNodes.size() * (22 + 2) + 6;
                float maxScroll = Math.max(0, totalListH - listH);
                this.categoryScrollOffset = Mth.clamp(this.categoryScrollOffset - (float) scrollY * 18, 0, maxScroll);
            } else {
                // Scroll the right article reader
                this.panelScrollOffset = Math.max(0, this.panelScrollOffset - (float) scrollY * 16);
            }
            return true;
        }

        if (currentMode == CodexMode.BOOK_INDEX) {
            this.indexScrollOffset = Math.max(0, this.indexScrollOffset - (float) scrollY * 16);
            return true;
        }

        if (currentMode == CodexMode.SPATIAL_GRID) {
            if (scrollY > 0) {
                this.targetZoom = Math.min(2.5f, this.targetZoom + 0.10f);
            } else if (scrollY < 0) {
                this.targetZoom = Math.max(0.20f, this.targetZoom - 0.10f);
            }
            return true;
        }

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
