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

public class EntropicCodexScreen extends Screen {

    private static final ResourceLocation BLACK_HOLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/black_hole_bg.png");

    private final EssenceType highestEssence;

    private float panX = 0f;
    private float panY = 0f;
    private float targetPanX = 0f;
    private float targetPanY = 0f;
    private float zoom = 0.45f;
    private float targetZoom = 0.45f;

    private boolean isDragging = false;

    private CodexNode selectedNode = null;
    private boolean isPanelOpen = false;
    private float panelScrollOffset = 0f;

    private EditBox searchBox;

    public EntropicCodexScreen() {
        this(null);
    }

    public EntropicCodexScreen(EssenceType essenceType) {
        super(Component.literal("Entropic Codex"));
        this.highestEssence = essenceType;
    }

    @Override
    protected void init() {
        super.init();

        int searchBoxX = 20;
        int searchBoxY = 175;
        this.searchBox = new EditBox(this.font, searchBoxX, searchBoxY, 150, 18, Component.literal("Search..."));
        this.searchBox.setMaxLength(30);
        this.searchBox.setHint(Component.literal("Search..."));
        this.addRenderableWidget(this.searchBox);

        // Center view on open with comfortable initial zoom showing all 7 Hubs
        this.targetPanX = 0;
        this.targetPanY = 0;
        this.panX = 0;
        this.panY = 0;
        this.targetZoom = 0.45f;
        this.zoom = 0.45f;
    }

    @Override
    public void tick() {
        super.tick();

        // Smooth camera zoom
        zoom = Mth.lerp(0.30f, zoom, targetZoom);

        // Smooth camera glide
        panX = Mth.lerp(0.30f, panX, targetPanX);
        panY = Mth.lerp(0.30f, panY, targetPanY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Dark Base Background Fill across entire screen viewport
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF050711);

        // 2. Render Full-Screen Clamped Background Image
        // Clamped directly to screen edges (0, 0, width, height); stretches dynamically with window size and eliminates all GPU tiling.
        guiGraphics.blit(BLACK_HOLE_TEXTURE, 0, 0, this.width, this.height, 0.0f, 1.0f, 0.0f, 1.0f);

        // 3. Calculate Screen-Space World Origin (0, 0) location
        float screenCenterX = this.width / 2.0f + panX * zoom;
        float screenCenterY = this.height / 2.0f + panY * zoom;

        // 4. Calculate Unprojected World Mouse Coordinates
        float worldMouseX = (float) ((mouseX - screenCenterX) / zoom);
        float worldMouseY = (float) ((mouseY - screenCenterY) / zoom);

        // --- Spatial World Nodes Pose Transformation ---
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(screenCenterX, screenCenterY);
        guiGraphics.pose().scale(zoom, zoom);

        // A. Render Connecting Lines between nodes in World Space
        renderConnectingLinesWorld(guiGraphics, screenCenterX, screenCenterY);

        // B. Render Nodes Graph in World Space
        CodexNode hoveredNode = renderNodesTreeWorld(guiGraphics, worldMouseX, worldMouseY, screenCenterX, screenCenterY);

        guiGraphics.pose().popMatrix();
        // --- End Spatial World Pose ---

        // 5. Render Top Header Banner (ENTROPIC CODEX Title + Red X Close)
        renderHeaderBanner(guiGraphics);

        // 6. Left NAVIGATION Widget
        renderLeftNavigationWidget(guiGraphics, mouseX, mouseY);

        // 7. Render Search Edit Box
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);

        // 8. Right Reading Panel (Strict Scissor Clipping + Dynamic Word Wrapping)
        if (isPanelOpen && selectedNode != null) {
            renderRightSidebarPanel(guiGraphics, mouseX, mouseY);
        }

        // 9. Tooltip for hovered node
        if (hoveredNode != null && (!isPanelOpen || mouseX < this.width - 340)) {
            Component titleComp = Component.literal("§b" + hoveredNode.title);
            Component sumComp = Component.literal("§7" + hoveredNode.summary);
            int w = Math.max(this.font.width(titleComp), this.font.width(sumComp));
            guiGraphics.fill(mouseX + 8, mouseY - 20, mouseX + 14 + w, mouseY + 10, 0xF0100010);
            guiGraphics.fill(mouseX + 9, mouseY - 19, mouseX + 13 + w, mouseY + 9, 0x505000FF);
            guiGraphics.drawString(this.font, titleComp, mouseX + 10, mouseY - 16, 0xFFFFFFFF, true);
            guiGraphics.drawString(this.font, sumComp, mouseX + 10, mouseY - 4, 0xFFAAAABB, true);
        }
    }

    private void renderConnectingLinesWorld(GuiGraphics guiGraphics, float screenCenterX, float screenCenterY) {
        for (CodexNode node : CodexCategoryRegistry.ALL_NODES) {
            if (node.prerequisiteId != null) {
                CodexNode prereq = CodexCategoryRegistry.getNodeById(node.prerequisiteId);
                if (prereq != null) {
                    int x1 = (int) (Math.cos(node.currentAngle) * node.orbitRadius);
                    int y1 = (int) (Math.sin(node.currentAngle) * node.orbitRadius);

                    int x2 = (int) (Math.cos(prereq.currentAngle) * prereq.orbitRadius);
                    int y2 = (int) (Math.sin(prereq.currentAngle) * prereq.orbitRadius);

                    // Viewport frustum check for lines
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

        // Optimized line segment drawing using 12px strides
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
            int nx = (int) (Math.cos(node.currentAngle) * node.orbitRadius);
            int ny = (int) (Math.sin(node.currentAngle) * node.orbitRadius);

            int catColor = getCategoryColor(node.category);

            if (node.isParentHub) {
                int hubR = 24;
                boolean isHovered = (worldMouseX >= nx - hubR && worldMouseX <= nx + hubR &&
                                     worldMouseY >= ny - hubR && worldMouseY <= ny + hubR);

                if (isHovered) hovered = node;

                // Viewport Frustum Culling
                if (!isNodeInViewport(nx, ny, hubR + 20, screenCenterX, screenCenterY)) {
                    continue;
                }

                // Fast Chamfered Septagonal Fill & Outlines
                drawFilledCircleWorld(guiGraphics, nx, ny, hubR - 4, 0xEE080A14, 0xEE080A14);
                drawSeptagonWorld(guiGraphics, nx, ny, hubR, catColor);
                drawSeptagonWorld(guiGraphics, nx, ny, hubR - 2, catColor);

                // Icon in center of hub septagon
                guiGraphics.renderItem(node.icon, nx - 8, ny - 8);

                // Hub title label centered above/below ring
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

                // Viewport Frustum Culling
                if (!isNodeInViewport(nx, ny, nodeR + 20, screenCenterX, screenCenterY)) {
                    continue;
                }

                // Outer round circular frame & dark inner container
                drawFilledCircleWorld(guiGraphics, nx, ny, nodeR, catColor, 0xF00D0F1D);

                // Sub-node item icon
                guiGraphics.renderItem(node.icon, nx - 8, ny - 8);

                // Sub-node title label to the right
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
        // 1. Single filled chamfered box (2 draw calls instead of hundreds)
        guiGraphics.fill(cx - radius + 3, cy - radius, cx + radius - 3, cy + radius, fillColor);
        guiGraphics.fill(cx - radius, cy - radius + 3, cx + radius, cy + radius - 3, fillColor);

        // 2. Fast 8-point circular outline (8 draw calls instead of 360)
        for (int i = 0; i < 360; i += 45) {
            double rad = Math.toRadians(i);
            int px = cx + (int) (Math.cos(rad) * radius);
            int py = cy + (int) (Math.sin(rad) * radius);
            guiGraphics.fill(px - 1, py - 1, px + 2, py + 2, outlineColor);
        }
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "GETTING STARTED": return 0xFF00FF00; // Bright Green
            case "MATERIALS": return 0xFFFFA500;       // Orange
            case "MATERIA": return 0xFF00FFFF;         // Cyan
            case "MACHINERY": return 0xFFFF3333;       // Red
            case "MULTIBLOCKS": return 0xFFFFFF00;     // Yellow
            case "ENVIRONMENT & NATURE": return 0xFF33FF33; // Lime Green
            case "MAGIC": return 0xFFA020F0;           // Purple
            default: return 0xFF00D9FF;
        }
    }

    private void renderHeaderBanner(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, this.width, 32, 0xF0080B18);
        guiGraphics.fill(0, 31, this.width, 33, 0xFF00D9FF);

        // Header Title Banner: ENTROPIC CODEX
        Component titleComp = Component.literal("ENTROPIC CODEX");
        int titleWidth = this.font.width(titleComp);
        guiGraphics.drawString(this.font, titleComp, this.width / 2 - titleWidth / 2, 10, 0xFF00D9FF, true);

        // Red [X] Close Button
        int closeX = this.width - 24;
        int closeY = 6;
        guiGraphics.fill(closeX, closeY, closeX + 18, closeY + 18, 0xFFCC2222);
        guiGraphics.drawString(this.font, "X", closeX + 5, closeY + 5, 0xFFFFFFFF, true);
    }

    private void renderLeftNavigationWidget(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int widgetX = 12;
        int widgetY = 45;
        int widgetW = 160;
        int widgetH = 115;

        // Navigation Panel Box
        guiGraphics.fill(widgetX, widgetY, widgetX + widgetW, widgetY + widgetH, 0xF00D0F1D);
        guiGraphics.fill(widgetX, widgetY, widgetX + widgetW, widgetY + 2, 0xFF00D9FF);

        guiGraphics.drawString(this.font, "NAVIGATION", widgetX + 10, widgetY + 8, 0xFF00D9FF, true);
        guiGraphics.drawString(this.font, "❖ Pan: Drag mouse", widgetX + 10, widgetY + 28, 0xFFCCCCCC, true);

        String zoomStr = String.format("🔍 Zoom: %.1fx", zoom);
        guiGraphics.drawString(this.font, zoomStr, widgetX + 10, widgetY + 48, 0xFFCCCCCC, true);

        // Clickable Center Button
        int btnX = widgetX + 10;
        int btnY = widgetY + 75;
        int btnW = widgetW - 20;
        int btnH = 22;
        boolean btnHovered = (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH);

        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? 0xFF2A3A5A : 0xFF182238);
        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFF00D9FF);
        guiGraphics.drawString(this.font, "↺ Center View", btnX + 25, btnY + 7, 0xFFFFFFFF, true);
    }

    private void renderRightSidebarPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int panelW = 340;
        int panelX = this.width - panelW;
        int panelY = 32;
        int panelH = this.height - 32;

        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF00A0C16);
        guiGraphics.fill(panelX, panelY, panelX + 2, panelY + panelH, 0xFF00D9FF);

        // Scissor clip reading area
        guiGraphics.enableScissor(panelX + 10, panelY + 10, panelX + panelW - 10, panelY + panelH - 10);

        int textY = panelY + 15 - (int) panelScrollOffset;

        // Title
        guiGraphics.drawString(this.font, selectedNode.title, panelX + 15, textY, 0xFF00D9FF, true);
        textY += 20;

        // Category Tag
        guiGraphics.drawString(this.font, "Category: " + selectedNode.category, panelX + 15, textY, 0xFF8888AA, true);
        textY += 20;

        // Content Paragraphs with Word Wrapping
        int maxWrapWidth = panelW - 40;
        String[] paragraphs = selectedNode.content.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                textY += 10;
                continue;
            }

            var formattedLines = this.font.split(Component.literal(paragraph), maxWrapWidth);
            for (var line : formattedLines) {
                if (textY >= panelY && textY <= panelY + panelH) {
                    guiGraphics.drawString(this.font, line, panelX + 15, textY, 0xFFDDDDDD, true);
                }
                textY += 12;
            }
            textY += 6;
        }

        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        // Red [X] close button
        int closeX = this.width - 24;
        int closeY = 6;
        if (mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18) {
            this.onClose();
            return true;
        }

        // Center View button click
        int btnX = 22;
        int btnY = 120;
        int btnW = 140;
        int btnH = 22;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            this.targetPanX = 0;
            this.targetPanY = 0;
            this.targetZoom = 0.45f;
            return true;
        }

        float screenCenterX = this.width / 2.0f + panX * zoom;
        float screenCenterY = this.height / 2.0f + panY * zoom;
        float worldMouseX = (float) ((mouseX - screenCenterX) / zoom);
        float worldMouseY = (float) ((mouseY - screenCenterY) / zoom);

        for (CodexNode node : CodexCategoryRegistry.ALL_NODES) {
            int nx = (int) (Math.cos(node.currentAngle) * node.orbitRadius);
            int ny = (int) (Math.sin(node.currentAngle) * node.orbitRadius);

            int r = node.isParentHub ? 24 : 14;
            if (worldMouseX >= nx - r && worldMouseX <= nx + r && worldMouseY >= ny - r && worldMouseY <= ny + r) {
                this.selectedNode = node;
                this.isPanelOpen = true;
                this.panelScrollOffset = 0;

                // Glide camera to center directly on clicked node in World Space
                this.targetPanX = -nx;
                this.targetPanY = -ny;
                return true;
            }
        }

        // Handle canvas drag-panning
        if (button == 0 && (mouseX < this.width - 340 || !isPanelOpen)) {
            this.isDragging = true;
            return true;
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
        if (this.isDragging) {
            this.targetPanX += dragX / zoom;
            this.targetPanY += dragY / zoom;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll sidebar panel if mouse is over sidebar
        if (isPanelOpen && mouseX >= this.width - 340) {
            this.panelScrollOffset = Math.max(0, this.panelScrollOffset - (float) scrollY * 16);
            return true;
        }

        // Scroll-wheel canvas zooming
        if (scrollY > 0) {
            this.targetZoom = Math.min(2.5f, this.targetZoom + 0.10f);
        } else if (scrollY < 0) {
            this.targetZoom = Math.max(0.20f, this.targetZoom - 0.10f);
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
