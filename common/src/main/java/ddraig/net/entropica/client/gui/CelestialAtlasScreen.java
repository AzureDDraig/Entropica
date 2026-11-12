package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ConstellationConnection;
import ddraig.net.entropica.astral.ConstellationStar;
import ddraig.net.entropica.astral.ConstellationTier;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.item.CelestialAtlasItem;
import ddraig.net.entropica.network.ReinscribeStarChartPayload;
import ddraig.net.entropica.registry.ModItems;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CelestialAtlasScreen extends Screen {

    public enum ViewMode {
        INDEX,
        DETAIL
    }

    private final ItemStack atlasStack;
    private final Set<String> discoveredIds;
    private ViewMode viewMode = ViewMode.INDEX;
    private ConstellationTier selectedTier = null; // null = All
    private Constellation selectedConstellation = null;

    private int leftPos;
    private int topPos;
    private final int imageWidth = 340;
    private final int imageHeight = 220;

    // Index Pagination
    private int indexPage = 0;
    private final int itemsPerPage = 8;

    // Lore Box Scrolling
    private float loreScrollOffset = 0.0f;
    private int maxLoreScroll = 0;

    public CelestialAtlasScreen(ItemStack atlasStack) {
        super(Component.literal("Celestial Atlas"));
        this.atlasStack = atlasStack;
        this.discoveredIds = CelestialAtlasItem.getDiscoveredConstellationIds(atlasStack);

        List<Constellation> all = new ArrayList<>(ModConstellations.getAllConstellations());
        if (!all.isEmpty()) {
            this.selectedConstellation = all.get(0);
        }
    }

    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(new CelestialAtlasScreen(stack));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    private List<Constellation> getFilteredConstellations() {
        List<Constellation> list = new ArrayList<>();
        for (Constellation c : ModConstellations.getAllConstellations()) {
            if (selectedTier == null || c.getTier() == selectedTier) {
                list.add(c);
            }
        }
        return list;
    }

    private boolean isConstellationDiscovered(Constellation c) {
        if (c == null) return false;
        if (discoveredIds.contains(c.getId().toString()) || discoveredIds.contains(c.getId().getPath())) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && ddraig.net.entropica.astral.PlayerAstralProgress.isDiscovered(mc.player, c);
    }

    private boolean hasBlankStarChartInInventory() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (mc.player.isCreative()) return true;

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(ModItems.STAR_CHART_BLANK.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // 1. Draw Dual-Page Open Book Body (Deep Celestial Navy Leather & Astral Brass)
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF40B132B);

        // Ornate Brass Outer Frame & Center Cyan Spine
        guiGraphics.fill(x, y, x + imageWidth, y + 2, 0xFFD4AF37); // Top Brass
        guiGraphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFFD4AF37); // Bottom Brass
        guiGraphics.fill(x, y, x + 2, y + imageHeight, 0xFFD4AF37); // Left Brass
        guiGraphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFFD4AF37); // Right Brass
        guiGraphics.fill(x + (imageWidth / 2) - 1, y + 2, x + (imageWidth / 2) + 1, y + imageHeight - 2, 0xFF38BDF8); // Center Spine

        if (this.viewMode == ViewMode.INDEX) {
            renderIndexView(guiGraphics, mouseX, mouseY, x, y);
        } else {
            renderDetailView(guiGraphics, mouseX, mouseY, x, y);
        }
    }

    // =========================================================================
    // INDEX VIEW: Directory of Discovered Constellations
    // =========================================================================
    private void drawCenteredNoShadow(GuiGraphics guiGraphics, String text, int cx, int cy, int color) {
        int w = this.font.width(text);
        guiGraphics.drawString(this.font, text, cx - (w / 2), cy, color, false);
    }

    private void renderIndexView(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        long totalDiscovered = ModConstellations.getAllConstellations().stream().filter(this::isConstellationDiscovered).count();
        int totalRegistry = ModConstellations.getAllConstellations().size();
        // Left Page: Header & Tier Filter Tabs
        guiGraphics.drawString(font, "§6§lCELESTIAL ATLAS", x + 14, y + 10, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, "§7Discovered: §f" + totalDiscovered + " / " + totalRegistry, x + 14, y + 22, 0xFFCCCCCC, false);

        // Tier Filter Tabs: [ALL] [T1] [T2] [T3] [T4] [T5]
        int tabX = x + 14;
        int tabY = y + 36;
        int tabW = 21;
        int tabH = 14;

        // ALL Tab
        boolean isAllSel = (selectedTier == null);
        boolean allHover = mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH;
        guiGraphics.fill(tabX, tabY, tabX + tabW, tabY + tabH, isAllSel ? 0xFF00E5FF : (allHover ? 0xFF2A374A : 0xFF1E293B));
        if (isAllSel) {
            guiGraphics.fill(tabX, tabY, tabX + tabW, tabY + 1, 0xFFFFD700);
            guiGraphics.fill(tabX, tabY + tabH - 1, tabX + tabW, tabY + tabH, 0xFFFFD700);
        }
        drawCenteredNoShadow(guiGraphics, "ALL", tabX + (tabW / 2), tabY + 3, isAllSel ? 0xFF000000 : (allHover ? 0xFF00E5FF : 0xFFCBD5E1));
        tabX += tabW + 3;

        ConstellationTier[] tiers = ConstellationTier.values();
        for (int i = 0; i < tiers.length; i++) {
            ConstellationTier t = tiers[i];
            boolean isSel = (t == selectedTier);
            boolean hover = mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH;
            guiGraphics.fill(tabX, tabY, tabX + tabW, tabY + tabH, isSel ? 0xFF00E5FF : (hover ? 0xFF2A374A : 0xFF1E293B));
            if (isSel) {
                guiGraphics.fill(tabX, tabY, tabX + tabW, tabY + 1, 0xFFFFD700);
                guiGraphics.fill(tabX, tabY + tabH - 1, tabX + tabW, tabY + tabH, 0xFFFFD700);
            }
            drawCenteredNoShadow(guiGraphics, "T" + (i + 1), tabX + (tabW / 2), tabY + 3, isSel ? 0xFF000000 : (hover ? 0xFF00E5FF : 0xFFCBD5E1));
            tabX += tabW + 3;
        }

        // Left Page: Constellation Directory List
        List<Constellation> filtered = getFilteredConstellations();
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / itemsPerPage));
        if (indexPage >= totalPages) indexPage = totalPages - 1;

        int startIndex = indexPage * itemsPerPage;
        int endIndex = Math.min(filtered.size(), startIndex + itemsPerPage);

        int entryY = y + 56;
        for (int i = startIndex; i < endIndex; i++) {
            Constellation c = filtered.get(i);
            boolean isDiscovered = isConstellationDiscovered(c);
            int entryW = 140;
            int entryH = 16;
            boolean hover = mouseX >= x + 14 && mouseX <= x + 14 + entryW && mouseY >= entryY && mouseY <= entryY + entryH;

            if (isDiscovered) {
                if (hover) {
                    guiGraphics.fill(x + 14, entryY, x + 14 + entryW, entryY + entryH, 0x2638BDF8);
                    guiGraphics.fill(x + 14, entryY, x + 16, entryY + entryH, 0xFF38BDF8);
                }
                String essCode = c.getEssenceType().getColorCode();
                String title = Component.translatable(c.getUnlocalizedName()).getString();
                if (title.length() > 17) title = title.substring(0, 15) + "..";
                guiGraphics.drawString(font, essCode + "✦ §f" + title, x + 18, entryY + 4, hover ? 0xFFFFFFFF : 0xFFE2E8F0, false);
            } else {
                guiGraphics.drawString(font, "§8✦ ?????", x + 18, entryY + 4, 0xFF555555, false);
            }

            entryY += 18;
        }

        // Left Page Bottom: Pagination [ < Page X/Y > ]
        if (totalPages > 1) {
            int pageBarY = y + imageHeight - 18;
            guiGraphics.drawString(font, "§7< Prev", x + 18, pageBarY, indexPage > 0 ? 0xFF00E5FF : 0xFF555555, false);
            drawCenteredNoShadow(guiGraphics, "§f" + (indexPage + 1) + " / " + totalPages, x + 85, pageBarY, 0xFFCCCCCC);
            guiGraphics.drawString(font, "Next >", x + 125, pageBarY, indexPage < totalPages - 1 ? 0xFF00E5FF : 0xFF555555, false);
        }

        // Right Page: Compendium Overview / Instructions
        int rX = x + (imageWidth / 2) + 14;
        int rY = y + 10;

        guiGraphics.drawString(font, "§6§lASTRONOMICAL COMPENDIUM", rX, rY, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, "§8The " + totalRegistry + " Celestial Firmaments", rX, rY + 12, 0xFFAAAAAA, false);

        rY += 30;
        guiGraphics.drawString(font, "§e✦ Reading the Firmament:", rX, rY, 0xFFFFFFFF, false);
        rY += 12;

        String[] introLines = new String[]{
                "Click on any §adiscovered constellation§7 in the directory to open its detailed astronomical sheet.",
                "",
                "Each sheet reveals the constellation's §6antique sepia star map§7, astrological powers, lunar phase alignments, and cosmological treatise lore.",
                "",
                "You can also §bre-inscribe physical star charts§7 on demand using Blank Star Charts without having to re-trace the stars."
        };

        for (String paragraph : introLines) {
            if (paragraph.isEmpty()) {
                rY += 6;
                continue;
            }
            List<FormattedCharSequence> split = font.split(Component.literal("§7" + paragraph), 140);
            for (FormattedCharSequence line : split) {
                guiGraphics.drawString(font, line, rX, rY, 0xFFB0BEC5, false);
                rY += 10;
            }
        }
    }

    // =========================================================================
    // DETAIL VIEW: Sepia Star Map + Reinscribe Button + Scrollable Lore Box
    // =========================================================================
    private void renderDetailView(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (selectedConstellation == null) return;

        boolean isDiscovered = isConstellationDiscovered(selectedConstellation);
        String essCode = selectedConstellation.getEssenceType().getColorCode();
        String title = Component.translatable(selectedConstellation.getUnlocalizedName()).getString();

        // ---------------------------------------------------------------------
        // LEFT PAGE: Back Button + Sepia Star Map + Reinscribe Button
        // ---------------------------------------------------------------------
        int lX = x + 14;
        int lY = y + 10;

        // 1. Back Button: [← Back to Index]
        int backBtnW = 60;
        int backBtnH = 14;
        boolean backHover = mouseX >= lX && mouseX <= lX + backBtnW && mouseY >= lY && mouseY <= lY + backBtnH;
        guiGraphics.fill(lX, lY, lX + backBtnW, lY + backBtnH, backHover ? 0xFF2A374A : 0xFF1E293B);
        drawCenteredNoShadow(guiGraphics, "§e← Index", lX + (backBtnW / 2), lY + 3, backHover ? 0xFF00E5FF : 0xFFFFFFFF);

        // Constellation Title Header
        guiGraphics.drawString(font, essCode + "✦ §l" + title, lX + 66, lY + 3, 0xFFFFFFFF, false);

        // 2. Antique Sepia Constellation Star Map Box
        int mapX = x + 16;
        int mapY = y + 28;
        int mapW = 138;
        int mapH = 120;

        // Sepia Parchment Background & Ink Borders
        guiGraphics.fill(mapX, mapY, mapX + mapW, mapY + mapH, 0xFF2A2016); // Aged Sepia Background
        guiGraphics.fill(mapX, mapY, mapX + mapW, mapY + 1, 0xFF8B6B48);     // Top Ink Line
        guiGraphics.fill(mapX, mapY + mapH - 1, mapX + mapW, mapY + mapH, 0xFF8B6B48); // Bottom Ink Line
        guiGraphics.fill(mapX, mapY, mapX + 1, mapY + mapH, 0xFF8B6B48);     // Left Ink Line
        guiGraphics.fill(mapX + mapW - 1, mapY, mapX + mapW, mapY + mapH, 0xFF8B6B48); // Right Ink Line

        if (isDiscovered) {
            // Render the constellation in authentic Sepia tones!
            renderSepiaConstellation(guiGraphics, mapX, mapY, mapW, mapH, selectedConstellation);
        } else {
            drawCenteredNoShadow(guiGraphics, "§8[UNDISCOVERED]", mapX + (mapW / 2), mapY + (mapH / 2) - 8, 0xFF888888);
            drawCenteredNoShadow(guiGraphics, "§7Trace at night to reveal", mapX + (mapW / 2), mapY + (mapH / 2) + 4, 0xFF666666);
        }

        // 3. "Reinscribe Star Chart" Clickable Button
        int reinBtnX = x + 16;
        int reinBtnY = y + 154;
        int reinBtnW = 138;
        int reinBtnH = 20;

        boolean canReinscribe = isDiscovered;
        boolean reinHover = canReinscribe && mouseX >= reinBtnX && mouseX <= reinBtnX + reinBtnW && mouseY >= reinBtnY && mouseY <= reinBtnY + reinBtnH;
        int reinBg = canReinscribe ? (reinHover ? 0xFF00E5FF : 0xFF1E293B) : 0xFF151D2A;
        int reinTextCol = canReinscribe ? (reinHover ? 0xFF000000 : 0xFFFFD700) : 0xFF555555;

        guiGraphics.fill(reinBtnX, reinBtnY, reinBtnX + reinBtnW, reinBtnY + reinBtnH, reinBg);
        guiGraphics.fill(reinBtnX, reinBtnY, reinBtnX + reinBtnW, reinBtnY + 1, 0xFFD4AF37);
        guiGraphics.fill(reinBtnX, reinBtnY + reinBtnH - 1, reinBtnX + reinBtnW, reinBtnY + reinBtnH, 0xFFD4AF37);
        drawCenteredNoShadow(guiGraphics, "§6✦ §lReinscribe Star Chart §6✦", reinBtnX + (reinBtnW / 2), reinBtnY + 6, reinTextCol);

        // Helper status under the reinscribe button
        boolean hasBlank = hasBlankStarChartInInventory();
        int statusY = reinBtnY + 24;
        if (!isDiscovered) {
            drawCenteredNoShadow(guiGraphics, "§8Undiscovered Constellation", reinBtnX + (reinBtnW / 2), statusY, 0xFF666666);
        } else if (hasBlank) {
            drawCenteredNoShadow(guiGraphics, "§a✓ 1 Blank Star Chart Ready", reinBtnX + (reinBtnW / 2), statusY, 0xFF80FF80);
        } else {
            drawCenteredNoShadow(guiGraphics, "§cRequires 1 Blank Star Chart", reinBtnX + (reinBtnW / 2), statusY, 0xFFFF6666);
        }

        // ---------------------------------------------------------------------
        // RIGHT PAGE: Astronomical Metrics + Scrollable Lore Box
        // ---------------------------------------------------------------------
        int rX = x + (imageWidth / 2) + 14;
        int rY = y + 10;

        // Top Metrics
        guiGraphics.drawString(font, "§eTier: §f" + selectedConstellation.getTier().name(), rX, rY, 0xFFFFFFFF, false);
        rY += 11;
        guiGraphics.drawString(font, "§7Essence: " + essCode + selectedConstellation.getEssenceType().name(), rX, rY, 0xFFDDDDDD, false);
        rY += 11;
        guiGraphics.drawString(font, "§7Spectral Class: §f" + selectedConstellation.getPrimarySpectralClass().name(), rX, rY, 0xFFDDDDDD, false);
        rY += 11;
        guiGraphics.drawString(font, "§7Base Flux Rate: §b" + (int) selectedConstellation.getFluxRate() + " flux/t", rX, rY, 0xFFDDDDDD, false);
        rY += 14;

        // Scrollable Lore Box Container
        int loreBoxX = rX;
        int loreBoxY = rY;
        int loreBoxW = 142;
        int loreBoxH = imageHeight - (loreBoxY - y) - 14;

        // Frame
        guiGraphics.fill(loreBoxX, loreBoxY, loreBoxX + loreBoxW, loreBoxY + loreBoxH, 0x44000000);
        guiGraphics.fill(loreBoxX, loreBoxY, loreBoxX + loreBoxW, loreBoxY + 1, 0xFF38BDF8);

        // Compile multi-line formatted lore text
        List<FormattedCharSequence> formattedLines = new ArrayList<>();
        if (isDiscovered) {
            // Check for shared-star conflict pairs
            if (!selectedConstellation.getConflictingConstellations().isEmpty()) {
                StringBuilder conflictNames = new StringBuilder();
                for (ResourceLocation confId : selectedConstellation.getConflictingConstellations()) {
                    ModConstellations.getById(confId).ifPresent(other -> {
                        if (!conflictNames.isEmpty()) conflictNames.append(", ");
                        conflictNames.append(Component.translatable(other.getUnlocalizedName()).getString());
                    });
                }
                if (!conflictNames.isEmpty()) {
                    addLoreSection(formattedLines, "§c§l✦ Celestial Conflict:", "Shares star resonance with §e" + conflictNames + "§7. Harmonic interference prevents channeling both simultaneously in altar cores or beacons.", loreBoxW - 14);
                }
            }

            addLoreSection(formattedLines, "§b§lAstrological Ritual:", selectedConstellation.getRitualEffect(), loreBoxW - 14);
            addLoreSection(formattedLines, "§e§lLunar Alignments:", "Visible during " + selectedConstellation.getLunarPhasesDescription() + ".", loreBoxW - 14);
            addLoreSection(formattedLines, "§6§lCosmological Treatise:", "An immutable celestial stellar tear continuously channeling pure Materia into physical reality. When attuned to the Astral Altar or Celestial Beacon, its harmonics permeate local space-time.", loreBoxW - 14);

            boolean isDaytime = Minecraft.getInstance().level != null && (Minecraft.getInstance().level.getDayTime() % 24000L < 12000L);
            String skyStatus = isDaytime ? "Obscured by daylight scattering (focus persists)." : "High celestial zenith position.";
            addLoreSection(formattedLines, "§a§lSky Status:", skyStatus, loreBoxW - 14);
        } else {
            addLoreSection(formattedLines, "§8§lUndiscovered Constellation", "This constellation has not yet been cataloged. Locate it in the night sky using a Looking Glass or Astrolabe and trace its star vertices onto a Blank Star Chart.", loreBoxW - 14);
        }

        int totalContentHeight = formattedLines.size() * 10;
        this.maxLoreScroll = Math.max(0, totalContentHeight - loreBoxH + 10);
        this.loreScrollOffset = Mth.clamp(this.loreScrollOffset, 0, maxLoreScroll);

        // Render Scissor-Clipped Lore Content
        guiGraphics.enableScissor(loreBoxX, loreBoxY + 2, loreBoxX + loreBoxW, loreBoxY + loreBoxH - 2);
        int lineY = loreBoxY + 4 - (int) loreScrollOffset;
        for (FormattedCharSequence line : formattedLines) {
            if (lineY + 10 >= loreBoxY && lineY <= loreBoxY + loreBoxH) {
                guiGraphics.drawString(font, line, loreBoxX + 4, lineY, 0xFFE2E8F0, false);
            }
            lineY += 10;
        }
        guiGraphics.disableScissor();

        // Draw Scrollbar Thumb if content exceeds lore box
        if (maxLoreScroll > 0) {
            int scrollTrackH = loreBoxH - 4;
            int thumbH = Math.max(12, (int) ((float) loreBoxH / totalContentHeight * scrollTrackH));
            int thumbY = loreBoxY + 2 + (int) ((loreScrollOffset / maxLoreScroll) * (scrollTrackH - thumbH));
            guiGraphics.fill(loreBoxX + loreBoxW - 5, loreBoxY + 2, loreBoxX + loreBoxW - 2, loreBoxY + loreBoxH - 2, 0x44000000);
            guiGraphics.fill(loreBoxX + loreBoxW - 5, thumbY, loreBoxX + loreBoxW - 2, thumbY + thumbH, 0xFF00E5FF);
        }
    }

    private void addLoreSection(List<FormattedCharSequence> list, String header, String body, int wrapWidth) {
        list.addAll(font.split(Component.literal(header), wrapWidth));
        list.addAll(font.split(Component.literal("§7" + body), wrapWidth));
        list.add(FormattedCharSequence.EMPTY);
    }

    // =========================================================================
    // SEPIA CONSTELLATION RENDERER
    // =========================================================================
    private void renderSepiaConstellation(GuiGraphics guiGraphics, int x, int y, int w, int h, Constellation c) {
        List<ConstellationStar> stars = c.getStars();
        if (stars.isEmpty()) return;

        // Find bounding box of normalized star coordinates (0..100)
        float minX = 100, maxX = 0, minY = 100, maxY = 0;
        for (ConstellationStar s : stars) {
            minX = Math.min(minX, s.x());
            maxX = Math.max(maxX, s.x());
            minY = Math.min(minY, s.y());
            maxY = Math.max(maxY, s.y());
        }

        float spanX = Math.max(20.0f, maxX - minX);
        float spanY = Math.max(20.0f, maxY - minY);
        float pad = 18.0f;
        float drawW = w - (pad * 2);
        float drawH = h - (pad * 2);

        // Precompute screen positions for each star node
        float[] starSX = new float[stars.size()];
        float[] starSY = new float[stars.size()];
        for (int i = 0; i < stars.size(); i++) {
            ConstellationStar s = stars.get(i);
            starSX[i] = x + pad + ((s.x() - minX) / spanX) * drawW;
            starSY[i] = y + pad + ((s.y() - minY) / spanY) * drawH;
        }

        // 1. Draw Sepia Connecting Ink Lines
        int sepiaLineColor = 0xFF8B6B48;     // Rich Sepia Ink
        int sepiaLineHighlight = 0xFFD4AF37; // Warm Golden Ink Accent
        for (ConstellationConnection conn : c.getConnections()) {
            if (conn.fromIndex() < stars.size() && conn.toIndex() < stars.size()) {
                int x1 = (int) starSX[conn.fromIndex()];
                int y1 = (int) starSY[conn.fromIndex()];
                int x2 = (int) starSX[conn.toIndex()];
                int y2 = (int) starSY[conn.toIndex()];
                drawSepiaLine(guiGraphics, x1, y1, x2, y2, sepiaLineColor);
            }
        }

        // 2. Draw Sepia Star Nodes & Subtle Glows
        for (int i = 0; i < stars.size(); i++) {
            ConstellationStar s = stars.get(i);
            int sx = (int) starSX[i];
            int sy = (int) starSY[i];
            int r = Math.max(2, Math.round(s.brightness() * 1.5f));

            boolean isShared = c.getSharedStarIndices().contains(i);
            if (isShared) {
                // Antique double-circle glyph for shared resonance nodes
                guiGraphics.fill(sx - r - 3, sy - r - 3, sx + r + 4, sy + r + 4, 0x44E11D48);
                guiGraphics.fill(sx - r - 2, sy - r - 2, sx + r + 3, sy - r - 1, 0xFFE11D48);
                guiGraphics.fill(sx - r - 2, sy + r + 2, sx + r + 3, sy + r + 3, 0xFFE11D48);
                guiGraphics.fill(sx - r - 2, sy - r - 2, sx - r - 1, sy + r + 3, 0xFFE11D48);
                guiGraphics.fill(sx + r + 2, sy - r - 2, sx + r + 3, sy + r + 3, 0xFFE11D48);
            } else {
                // Outer warm sepia halo
                guiGraphics.fill(sx - r - 1, sy - r - 1, sx + r + 2, sy + r + 2, 0x44D4AF37);
            }

            // Diamond Star Core
            guiGraphics.fill(sx - r, sy, sx + r + 1, sy + 1, 0xFFFFF2A3);
            guiGraphics.fill(sx, sy - r, sx + 1, sy + r + 1, 0xFFFFF2A3);
            guiGraphics.fill(sx - 1, sy - 1, sx + 2, sy + 2, isShared ? 0xFFFFA07A : 0xFFE5B842);
        }
    }

    private void drawSepiaLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    // =========================================================================
    // INPUT HANDLING & INTERACTIONS
    // =========================================================================
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int x = this.leftPos;
        int y = this.topPos;

        if (this.viewMode == ViewMode.INDEX) {
            // Check Tier Tab clicks
            int tabX = x + 14;
            int tabY = y + 36;
            int tabW = 21;
            int tabH = 14;

            // ALL Tab
            if (mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
                this.selectedTier = null;
                this.indexPage = 0;
                playPageTurnSound();
                return true;
            }
            tabX += tabW + 3;

            ConstellationTier[] tiers = ConstellationTier.values();
            for (ConstellationTier t : tiers) {
                if (mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
                    this.selectedTier = t;
                    this.indexPage = 0;
                    playPageTurnSound();
                    return true;
                }
                tabX += tabW + 3;
            }

            // Check Constellation List clicks
            List<Constellation> filtered = getFilteredConstellations();
            int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / itemsPerPage));
            int startIndex = indexPage * itemsPerPage;
            int endIndex = Math.min(filtered.size(), startIndex + itemsPerPage);

            int entryY = y + 56;
            for (int i = startIndex; i < endIndex; i++) {
                Constellation c = filtered.get(i);
                int entryW = 140;
                int entryH = 16;
                if (mouseX >= x + 14 && mouseX <= x + 14 + entryW && mouseY >= entryY && mouseY <= entryY + entryH) {
                    if (isConstellationDiscovered(c)) {
                        this.selectedConstellation = c;
                        this.viewMode = ViewMode.DETAIL;
                        this.loreScrollOffset = 0.0f;
                        playPageTurnSound();
                        return true;
                    }
                }
                entryY += 18;
            }

            // Check Pagination clicks
            if (totalPages > 1) {
                int pageBarY = y + imageHeight - 18;
                // Prev
                if (indexPage > 0 && mouseX >= x + 18 && mouseX <= x + 55 && mouseY >= pageBarY && mouseY <= pageBarY + 12) {
                    this.indexPage--;
                    playButtonClickSound();
                    return true;
                }
                // Next
                if (indexPage < totalPages - 1 && mouseX >= x + 125 && mouseX <= x + 160 && mouseY >= pageBarY && mouseY <= pageBarY + 12) {
                    this.indexPage++;
                    playButtonClickSound();
                    return true;
                }
            }
        } else {
            // DETAIL VIEW INTERACTIONS
            int lX = x + 14;
            int lY = y + 10;

            // 1. Back Button [← Index]
            int backBtnW = 60;
            int backBtnH = 14;
            if (mouseX >= lX && mouseX <= lX + backBtnW && mouseY >= lY && mouseY <= lY + backBtnH) {
                this.viewMode = ViewMode.INDEX;
                playPageTurnSound();
                return true;
            }

            // 2. Reinscribe Button [Reinscribe Star Chart]
            int reinBtnX = x + 16;
            int reinBtnY = y + 154;
            int reinBtnW = 138;
            int reinBtnH = 20;
            if (selectedConstellation != null && isConstellationDiscovered(selectedConstellation)) {
                if (mouseX >= reinBtnX && mouseX <= reinBtnX + reinBtnW && mouseY >= reinBtnY && mouseY <= reinBtnY + reinBtnH) {
                    NetworkManager.sendToServer(new ReinscribeStarChartPayload(selectedConstellation.getId()));
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.viewMode == ViewMode.DETAIL && maxLoreScroll > 0) {
            int rX = leftPos + (imageWidth / 2) + 14;
            int rY = topPos + 10 + 44;
            int loreBoxW = 142;
            int loreBoxH = imageHeight - (rY - topPos) - 14;

            if (mouseX >= rX && mouseX <= rX + loreBoxW && mouseY >= rY && mouseY <= rY + loreBoxH) {
                this.loreScrollOffset -= (float) (scrollY * 16.0);
                this.loreScrollOffset = Mth.clamp(this.loreScrollOffset, 0, maxLoreScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void playPageTurnSound() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8f, 1.2f);
        }
    }

    private void playButtonClickSound() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1.4f);
        }
    }
}
