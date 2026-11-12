package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.data.CelestialJournalData;
import ddraig.net.entropica.item.CelestialJournalItem;
import ddraig.net.entropica.network.JournalBookmarkPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Interactive dual-page Scholar's Journal reader displaying the complete
 * chapters, epigraphs, equations, quotes, and notes from the 13 Entropica
 * Celestial Lore Journals (Volumes I - XIII).
 */
public class CelestialJournalScreen extends Screen {

    private enum LineKind {
        SPACER,
        PART_BANNER,
        ENTRY_TITLE,
        DIVIDER,
        EPIGRAPH,
        SUBHEADING,
        PARAGRAPH,
        QUOTE,
        BULLET,
        EQUATION_TOP,
        EQUATION_LINE,
        EQUATION_BOTTOM
    }

    private record RenderedLine(LineKind kind, FormattedCharSequence seq, int height) {
    }

    private final ItemStack journalStack;
    private final CelestialJournalData.JournalVolume volume;

    private int leftPos;
    private int topPos;
    private int bookWidth = 396;
    private int bookHeight = 248;

    private int selectedEntryIndex;
    private float indexScrollOffset = 0.0F;
    private int maxIndexScroll = 0;

    private float textScrollOffset = 0.0F;
    private int maxTextScroll = 0;

    private final List<RenderedLine> cachedLines = new ArrayList<>();
    private int cachedWrapWidth = -1;
    private int cachedEntryIndex = -1;

    private static final int PARCHMENT_BG = 0xFFF5ECD8;
    private static final int PARCHMENT_EDGE = 0xFFE5D5B5;
    private static final int PARCHMENT_CARD = 0xFFEDE0C4;
    private static final int INK_DARK = 0xFF2B2016;
    private static final int INK_MUTED = 0xFF6B563E;
    private static final int INK_QUOTE = 0xFF5C381E;
    private static final int INK_HEADING = 0xFF6E3A14;

    public CelestialJournalScreen(ItemStack journalStack, int volumeNumber) {
        super(Component.literal(CelestialJournalData.getVolume(volumeNumber).title()));
        this.journalStack = journalStack;
        this.volume = CelestialJournalData.getVolume(volumeNumber);

        int saved = CelestialJournalItem.getLastEntryIndex(journalStack);
        int count = Math.max(1, this.volume.entries().size());
        this.selectedEntryIndex = Mth.clamp(saved, 0, count - 1);
        recordBookmark(this.selectedEntryIndex);
    }

    public static void open(ItemStack stack, int volumeNumber) {
        Minecraft.getInstance().setScreen(new CelestialJournalScreen(stack, volumeNumber));
    }

    @Override
    protected void init() {
        super.init();
        this.bookWidth = Math.min(404, Math.max(320, this.width - 24));
        this.bookHeight = Math.min(252, Math.max(200, this.height - 24));
        this.leftPos = (this.width - this.bookWidth) / 2;
        this.topPos = (this.height - this.bookHeight) / 2;
        this.cachedWrapWidth = -1;
        ensureEntryVisibleInIndex(this.selectedEntryIndex);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void selectEntry(int newIndex, boolean playSound) {
        int count = this.volume.entries().size();
        if (count == 0) return;
        int clamped = Mth.clamp(newIndex, 0, count - 1);
        if (clamped != this.selectedEntryIndex) {
            this.selectedEntryIndex = clamped;
            this.textScrollOffset = 0.0F;
            this.cachedEntryIndex = -1;
            ensureEntryVisibleInIndex(clamped);
            recordBookmark(clamped);
            if (playSound) {
                playPageTurnSound();
            }
        }
    }

    private void recordBookmark(int entryIdx) {
        CelestialJournalItem.updateClientBookmark(this.journalStack, entryIdx);
        try {
            NetworkManager.sendToServer(new JournalBookmarkPayload(entryIdx));
        } catch (Throwable ignored) {
        }
    }

    private void ensureEntryVisibleInIndex(int entryIdx) {
        int listH = getIndexViewportHeight();
        int rowH = 18;
        int itemTop = entryIdx * rowH;
        int itemBottom = itemTop + rowH;
        if (itemTop < this.indexScrollOffset) {
            this.indexScrollOffset = itemTop;
        } else if (itemBottom > this.indexScrollOffset + listH) {
            this.indexScrollOffset = Math.max(0.0F, itemBottom - listH);
        }
    }

    private int getLeftPageX() {
        return this.leftPos + 9;
    }

    private int getLeftPageW() {
        return (this.bookWidth / 2) - 13;
    }

    private int getRightPageX() {
        return this.leftPos + (this.bookWidth / 2) + 4;
    }

    private int getRightPageW() {
        return (this.bookWidth / 2) - 13;
    }

    private int getPageY() {
        return this.topPos + 8;
    }

    private int getPageH() {
        return this.bookHeight - 16;
    }

    private int getIndexViewportY() {
        return getPageY() + 58;
    }

    private int getIndexViewportHeight() {
        return getPageH() - 66;
    }

    private int getReaderViewportY() {
        return getPageY() + 8;
    }

    private int getReaderViewportHeight() {
        return getPageH() - 30;
    }

    private void rebuildWrappedLinesIfNeeded(int wrapWidth) {
        if (this.cachedEntryIndex == this.selectedEntryIndex && this.cachedWrapWidth == wrapWidth) {
            return;
        }
        this.cachedLines.clear();
        this.cachedEntryIndex = this.selectedEntryIndex;
        this.cachedWrapWidth = wrapWidth;

        if (this.volume.entries().isEmpty()) {
            return;
        }

        // Include ceremonial Volume Epigraph at the top of Entry I
        if (this.selectedEntryIndex == 0 && !this.volume.epigraph().isEmpty()) {
            for (String epLine : this.volume.epigraph()) {
                boolean attribution = epLine.startsWith("—") || epLine.startsWith("--");
                MutableComponent comp = CelestialJournalData.parseStyledMarkdown(epLine, attribution ? INK_HEADING : INK_QUOTE, !attribution);
                for (FormattedCharSequence seq : this.font.split(comp, Math.max(60, wrapWidth - 14))) {
                    this.cachedLines.add(new RenderedLine(LineKind.EPIGRAPH, seq, 10));
                }
            }
            this.cachedLines.add(new RenderedLine(LineKind.DIVIDER, FormattedCharSequence.EMPTY, 9));
        }

        CelestialJournalData.JournalEntry entry = this.volume.entries().get(this.selectedEntryIndex);

        // Optional Part Banner (Volumes X - XIII)
        if (entry.part() != null && !entry.part().isEmpty()) {
            MutableComponent partComp = Component.literal(entry.part().toUpperCase())
                    .withStyle(s -> s.withBold(true).withColor(INK_MUTED));
            for (FormattedCharSequence seq : this.font.split(partComp, wrapWidth)) {
                this.cachedLines.add(new RenderedLine(LineKind.PART_BANNER, seq, 10));
            }
            this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 2));
        }

        // Entry Title
        MutableComponent titleComp = CelestialJournalData.parseStyledMarkdown("**" + entry.title() + "**", INK_HEADING, false);
        for (FormattedCharSequence seq : this.font.split(titleComp, wrapWidth)) {
            this.cachedLines.add(new RenderedLine(LineKind.ENTRY_TITLE, seq, 11));
        }
        this.cachedLines.add(new RenderedLine(LineKind.DIVIDER, FormattedCharSequence.EMPTY, 8));

        // Content Blocks
        for (CelestialJournalData.EntryBlock block : entry.blocks()) {
            String type = block.type();
            String text = block.text();
            if (text == null || text.isEmpty()) continue;

            switch (type) {
                case "subheading" -> {
                    this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 3));
                    MutableComponent sub = CelestialJournalData.parseStyledMarkdown("**" + text + "**", INK_HEADING, false);
                    for (FormattedCharSequence seq : this.font.split(sub, wrapWidth)) {
                        this.cachedLines.add(new RenderedLine(LineKind.SUBHEADING, seq, 11));
                    }
                    this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 2));
                }
                case "equation" -> {
                    this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 2));
                    this.cachedLines.add(new RenderedLine(LineKind.EQUATION_TOP, FormattedCharSequence.EMPTY, 4));
                    MutableComponent eq = Component.literal(text).withStyle(s -> s.withBold(true).withColor(0xFF3E240E));
                    for (FormattedCharSequence seq : this.font.split(eq, Math.max(60, wrapWidth - 14))) {
                        this.cachedLines.add(new RenderedLine(LineKind.EQUATION_LINE, seq, 11));
                    }
                    this.cachedLines.add(new RenderedLine(LineKind.EQUATION_BOTTOM, FormattedCharSequence.EMPTY, 4));
                    this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 4));
                }
                case "quote" -> {
                    MutableComponent q = CelestialJournalData.parseStyledMarkdown(text, INK_QUOTE, true);
                    for (FormattedCharSequence seq : this.font.split(q, Math.max(60, wrapWidth - 12))) {
                        this.cachedLines.add(new RenderedLine(LineKind.QUOTE, seq, 10));
                    }
                    this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 4));
                }
                case "bullet" -> {
                    MutableComponent b = Component.literal("• ").withStyle(s -> s.withBold(true).withColor(INK_HEADING))
                            .append(CelestialJournalData.parseStyledMarkdown(text, INK_DARK, false));
                    List<FormattedCharSequence> split = this.font.split(b, Math.max(60, wrapWidth - 8));
                    for (FormattedCharSequence seq : split) {
                        this.cachedLines.add(new RenderedLine(LineKind.BULLET, seq, 10));
                    }
                    this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 2));
                }
                default -> {
                    MutableComponent p = CelestialJournalData.parseStyledMarkdown(text, INK_DARK, false);
                    for (FormattedCharSequence seq : this.font.split(p, wrapWidth)) {
                        this.cachedLines.add(new RenderedLine(LineKind.PARAGRAPH, seq, 10));
                    }
                    this.cachedLines.add(new RenderedLine(LineKind.SPACER, FormattedCharSequence.EMPTY, 5));
                }
            }
        }

        int totalContentH = 0;
        for (RenderedLine line : this.cachedLines) {
            totalContentH += line.height();
        }
        this.maxTextScroll = Math.max(0, totalContentH - getReaderViewportHeight() + 8);
        this.textScrollOffset = Mth.clamp(this.textScrollOffset, 0.0F, this.maxTextScroll);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;
        int w = this.bookWidth;
        int h = this.bookHeight;

        int coverCol = this.volume.guiCoverColor() | 0xFF000000;
        int trimCol = this.volume.guiTrimColor() | 0xFF000000;
        int accentCol = this.volume.guiAccentColor() | 0xFF000000;

        // 1. Outer Drop Shadow & Leather Journal Cover
        g.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x66000000);
        g.fill(x, y, x + w, y + h, coverCol);

        // Metallic Outer Trim & Corner Brackets
        g.fill(x, y, x + w, y + 2, trimCol);
        g.fill(x, y + h - 2, x + w, y + h, trimCol);
        g.fill(x, y, x + 2, y + h, trimCol);
        g.fill(x + w - 2, y, x + w, y + h, trimCol);
        drawCornerBracket(g, x + 2, y + 2, 8, true, true, trimCol);
        drawCornerBracket(g, x + w - 10, y + 2, 8, false, true, trimCol);
        drawCornerBracket(g, x + 2, y + h - 10, 8, true, false, trimCol);
        drawCornerBracket(g, x + w - 10, y + h - 10, 8, false, false, trimCol);

        // 2. Dual Warm Parchment Spread
        int lpX = getLeftPageX();
        int lpW = getLeftPageW();
        int rpX = getRightPageX();
        int rpW = getRightPageW();
        int pY = getPageY();
        int pH = getPageH();

        // Page stack edge & parchment sheets
        g.fill(lpX - 2, pY + 2, lpX + lpW, pY + pH + 2, 0xFFC7B28E);
        g.fill(rpX, pY + 2, rpX + rpW + 2, pY + pH + 2, 0xFFC7B28E);
        g.fill(lpX, pY, lpX + lpW, pY + pH, PARCHMENT_BG);
        g.fill(rpX, pY, rpX + rpW, pY + pH, PARCHMENT_BG);

        // Inner page margin borders
        drawHollowRect(g, lpX + 3, pY + 3, lpW - 6, pH - 6, PARCHMENT_EDGE);
        drawHollowRect(g, rpX + 3, pY + 3, rpW - 6, pH - 6, PARCHMENT_EDGE);

        // Center stitched binding spine crease & hanging silk ribbon bookmark
        int spineX = x + (w / 2);
        g.fill(spineX - 4, pY, spineX + 4, pY + pH, 0xFFD9C7A3);
        g.fill(spineX - 1, y + 2, spineX + 1, y + h - 2, coverCol);
        for (int stitchY = pY + 14; stitchY < pY + pH - 12; stitchY += 22) {
            g.fill(spineX - 3, stitchY, spineX + 3, stitchY + 2, trimCol);
        }
        // Hanging ribbon tail below spine
        g.fill(spineX - 3, y + h - 2, spineX + 2, y + h + 8, accentCol);
        g.fill(spineX - 3, y + h + 8, spineX - 1, y + h + 11, accentCol);
        g.fill(spineX, y + h + 8, spineX + 2, y + h + 11, accentCol);

        // 3. Render Left Page (Title Banner + Interactive Chapter Directory)
        renderLeftIndexPage(g, lpX, pY, lpW, pH, mouseX, mouseY, coverCol, trimCol, accentCol);

        // 4. Render Right Page (Scrollable Rich Chapter Reader + Bottom Nav)
        renderRightReaderPage(g, rpX, pY, rpW, pH, mouseX, mouseY, coverCol, trimCol, accentCol);
    }

    private void renderLeftIndexPage(GuiGraphics g, int lpX, int pY, int lpW, int pH,
                                     int mouseX, int mouseY, int coverCol, int trimCol, int accentCol) {
        int innerX = lpX + 8;
        int innerW = lpW - 16;

        // Volume Header Plaque
        g.fill(innerX, pY + 7, innerX + innerW, pY + 49, coverCol);
        drawHollowRect(g, innerX, pY + 7, innerW, 42, trimCol);

        String volBadge = "✦ VOLUME " + this.volume.roman() + " ✦";
        int badgeW = this.font.width(volBadge);
        g.drawString(this.font, volBadge, innerX + (innerW - badgeW) / 2, pY + 11, trimCol, false);

        // Trimmed Short Title
        String shortT = this.font.plainSubstrByWidth(this.volume.shortTitle(), innerW - 8);
        int stW = this.font.width(shortT);
        g.drawString(this.font, shortT, innerX + (innerW - stW) / 2, pY + 22, 0xFFFDF7E7, false);

        // Author line
        String authClean = CelestialJournalData.stripMarkdownMarkers(this.volume.author());
        String authTrimmed = this.font.plainSubstrByWidth(authClean, innerW - 8);
        int auW = this.font.width(authTrimmed);
        g.drawString(this.font, authTrimmed, innerX + (innerW - auW) / 2, pY + 34, 0xFFD2C5B0, false);

        // Section Label
        Set<Integer> readSet = CelestialJournalItem.getReadEntries(this.journalStack);
        int totalEntries = this.volume.entries().size();
        String tocHeader = "Journal Entries (" + Math.min(readSet.size(), totalEntries) + "/" + totalEntries + ")";
        g.drawString(this.font, tocHeader, innerX + 2, pY + 51, INK_MUTED, false);

        // Scrollable Chapter Directory
        int listX = innerX;
        int listY = getIndexViewportY() + 4;
        int listW = innerW - 6;
        int listH = getIndexViewportHeight() - 4;
        int rowH = 18;

        this.maxIndexScroll = Math.max(0, totalEntries * rowH - listH);
        this.indexScrollOffset = Mth.clamp(this.indexScrollOffset, 0.0F, this.maxIndexScroll);

        g.enableScissor(listX, listY, listX + listW, listY + listH);
        for (int i = 0; i < totalEntries; i++) {
            int rowY = Math.round(listY + i * rowH - this.indexScrollOffset);
            if (rowY + rowH < listY || rowY > listY + listH) continue;

            boolean selected = (i == this.selectedEntryIndex);
            boolean hovered = mouseX >= listX && mouseX <= listX + listW
                    && mouseY >= Math.max(listY, rowY) && mouseY <= Math.min(listY + listH, rowY + rowH - 2);
            boolean isRead = readSet.contains(i);

            int bg = selected ? coverCol : (hovered ? 0xFFE6D5B4 : PARCHMENT_CARD);
            g.fill(listX, rowY, listX + listW, rowY + rowH - 2, bg);
            if (selected) {
                g.fill(listX, rowY, listX + 3, rowY + rowH - 2, accentCol);
            } else {
                g.fill(listX, rowY + rowH - 3, listX + listW, rowY + rowH - 2, PARCHMENT_EDGE);
            }

            String rawTitle = CelestialJournalData.stripMarkdownMarkers(this.volume.entries().get(i).title());
            String prefix = isRead ? "✦ " : "• ";
            int prefixCol = selected ? accentCol : (isRead ? 0xFFB57C18 : INK_MUTED);
            g.drawString(this.font, prefix, listX + 5, rowY + 4, prefixCol, false);

            int pfxW = this.font.width(prefix);
            String clippedTitle = this.font.plainSubstrByWidth(rawTitle, listW - pfxW - 9);
            int textCol = selected ? 0xFFFDF7E7 : INK_DARK;
            g.drawString(this.font, clippedTitle, listX + 5 + pfxW, rowY + 4, textCol, false);
        }
        g.disableScissor();

        // Left Scrollbar
        if (this.maxIndexScroll > 0) {
            int sbX = innerX + innerW - 4;
            g.fill(sbX, listY, sbX + 3, listY + listH, PARCHMENT_EDGE);
            int thumbH = Math.max(14, Math.round((float) listH * listH / (totalEntries * rowH)));
            int thumbY = listY + Math.round((this.indexScrollOffset / this.maxIndexScroll) * (listH - thumbH));
            g.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, coverCol);
        }
    }

    private void renderRightReaderPage(GuiGraphics g, int rpX, int pY, int rpW, int pH,
                                       int mouseX, int mouseY, int coverCol, int trimCol, int accentCol) {
        int innerX = rpX + 9;
        int innerW = rpW - 18;
        int viewY = getReaderViewportY();
        int viewH = getReaderViewportHeight();
        int textWrapW = innerW - 8;

        rebuildWrappedLinesIfNeeded(textWrapW);

        g.enableScissor(innerX - 2, viewY, innerX + innerW - 4, viewY + viewH);
        int cursorY = Math.round(viewY - this.textScrollOffset);

        for (RenderedLine line : this.cachedLines) {
            int lh = line.height();
            if (cursorY + lh >= viewY - 8 && cursorY <= viewY + viewH + 8) {
                switch (line.kind()) {
                    case PART_BANNER -> g.drawString(this.font, line.seq(), innerX, cursorY, INK_MUTED, false);
                    case ENTRY_TITLE -> g.drawString(this.font, line.seq(), innerX, cursorY, INK_HEADING, false);
                    case DIVIDER -> {
                        int midY = cursorY + 3;
                        g.fill(innerX, midY, innerX + textWrapW, midY + 1, 0xFFC5A875);
                        int cx = innerX + textWrapW / 2;
                        g.fill(cx - 3, midY - 1, cx + 3, midY + 2, trimCol);
                    }
                    case EPIGRAPH -> {
                        g.fill(innerX, cursorY - 1, innerX + textWrapW, cursorY + lh, PARCHMENT_CARD);
                        g.fill(innerX, cursorY - 1, innerX + 2, cursorY + lh, trimCol);
                        g.drawString(this.font, line.seq(), innerX + 6, cursorY, INK_QUOTE, false);
                    }
                    case SUBHEADING -> g.drawString(this.font, line.seq(), innerX, cursorY, INK_HEADING, false);
                    case QUOTE -> {
                        g.fill(innerX + 1, cursorY - 1, innerX + 3, cursorY + lh, 0xFFB8893A);
                        g.drawString(this.font, line.seq(), innerX + 8, cursorY, INK_QUOTE, false);
                    }
                    case BULLET -> g.drawString(this.font, line.seq(), innerX + 3, cursorY, INK_DARK, false);
                    case EQUATION_TOP -> {
                        g.fill(innerX + 2, cursorY + 1, innerX + textWrapW - 2, cursorY + lh, 0xFFEAD9BA);
                        g.fill(innerX + 2, cursorY + 1, innerX + textWrapW - 2, cursorY + 2, 0xFFB89450);
                    }
                    case EQUATION_LINE -> {
                        g.fill(innerX + 2, cursorY, innerX + textWrapW - 2, cursorY + lh, 0xFFEAD9BA);
                        g.fill(innerX + 2, cursorY, innerX + 3, cursorY + lh, 0xFFB89450);
                        g.fill(innerX + textWrapW - 3, cursorY, innerX + textWrapW - 2, cursorY + lh, 0xFFB89450);
                        int eqW = this.font.width(line.seq());
                        g.drawString(this.font, line.seq(), innerX + (textWrapW - eqW) / 2, cursorY + 1, 0xFF3E240E, false);
                    }
                    case EQUATION_BOTTOM -> {
                        g.fill(innerX + 2, cursorY, innerX + textWrapW - 2, cursorY + lh - 1, 0xFFEAD9BA);
                        g.fill(innerX + 2, cursorY + lh - 2, innerX + textWrapW - 2, cursorY + lh - 1, 0xFFB89450);
                    }
                    case PARAGRAPH -> g.drawString(this.font, line.seq(), innerX, cursorY, INK_DARK, false);
                    case SPACER -> {
                    }
                }
            }
            cursorY += lh;
        }
        g.disableScissor();

        // Right Page Scrollbar
        if (this.maxTextScroll > 0) {
            int sbX = innerX + innerW - 3;
            g.fill(sbX, viewY, sbX + 3, viewY + viewH, PARCHMENT_EDGE);
            int totalH = viewH + this.maxTextScroll;
            int thumbH = Math.max(16, Math.round((float) viewH * viewH / totalH));
            int thumbY = viewY + Math.round((this.textScrollOffset / this.maxTextScroll) * (viewH - thumbH));
            g.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, coverCol);
        }

        // Bottom Navigation Bar: [< Prev]   Entry X / N   [Next >]
        int navY = pY + pH - 18;
        int totalEntries = Math.max(1, this.volume.entries().size());
        boolean canPrev = this.selectedEntryIndex > 0;
        boolean canNext = this.selectedEntryIndex < totalEntries - 1;

        int btnW = 44;
        int btnH = 13;
        int prevX = innerX;
        int nextX = innerX + innerW - btnW;

        boolean hoverPrev = canPrev && mouseX >= prevX && mouseX <= prevX + btnW && mouseY >= navY && mouseY <= navY + btnH;
        boolean hoverNext = canNext && mouseX >= nextX && mouseX <= nextX + btnW && mouseY >= navY && mouseY <= navY + btnH;

        drawNavButton(g, prevX, navY, btnW, btnH, "◀ Prev", canPrev, hoverPrev, coverCol, trimCol);
        drawNavButton(g, nextX, navY, btnW, btnH, "Next ▶", canNext, hoverNext, coverCol, trimCol);

        String pageIndicator = "Entry " + (this.selectedEntryIndex + 1) + " / " + totalEntries;
        int piW = this.font.width(pageIndicator);
        g.drawString(this.font, pageIndicator, innerX + (innerW - piW) / 2, navY + 3, INK_MUTED, false);
    }

    private void drawNavButton(GuiGraphics g, int bx, int by, int bw, int bh, String label,
                               boolean enabled, boolean hovered, int coverCol, int trimCol) {
        int bg = !enabled ? PARCHMENT_EDGE : (hovered ? coverCol : PARCHMENT_CARD);
        int border = !enabled ? 0xFFD4C4A6 : (hovered ? trimCol : 0xFFB89E72);
        int fg = !enabled ? 0xFFA59378 : (hovered ? 0xFFFDF7E7 : INK_DARK);
        g.fill(bx, by, bx + bw, by + bh, bg);
        drawHollowRect(g, bx, by, bw, bh, border);
        int lw = this.font.width(label);
        g.drawString(this.font, label, bx + (bw - lw) / 2, by + 3, fg, false);
    }

    private static void drawHollowRect(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void drawCornerBracket(GuiGraphics g, int x, int y, int size, boolean left, boolean top, int color) {
        int hy = top ? y : y + size - 2;
        int vx = left ? x : x + size - 2;
        g.fill(x, hy, x + size, hy + 2, color);
        g.fill(vx, y, vx + 2, y + size, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        // 1. Left Page Chapter Directory Clicks
        int lpX = getLeftPageX();
        int lpW = getLeftPageW();
        int listX = lpX + 8;
        int listW = lpW - 22;
        int listY = getIndexViewportY() + 4;
        int listH = getIndexViewportHeight() - 4;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int clickedIdx = (int) Math.floor((mouseY - listY + this.indexScrollOffset) / 18.0);
            if (clickedIdx >= 0 && clickedIdx < this.volume.entries().size()) {
                selectEntry(clickedIdx, true);
                return true;
            }
        }

        // 2. Right Page Prev / Next Buttons
        int rpX = getRightPageX();
        int rpW = getRightPageW();
        int innerX = rpX + 9;
        int innerW = rpW - 18;
        int navY = getPageY() + getPageH() - 18;
        int btnW = 44;
        int btnH = 13;

        if (mouseY >= navY && mouseY <= navY + btnH) {
            if (this.selectedEntryIndex > 0 && mouseX >= innerX && mouseX <= innerX + btnW) {
                selectEntry(this.selectedEntryIndex - 1, true);
                return true;
            }
            int nextX = innerX + innerW - btnW;
            if (this.selectedEntryIndex < this.volume.entries().size() - 1 && mouseX >= nextX && mouseX <= nextX + btnW) {
                selectEntry(this.selectedEntryIndex + 1, true);
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int midX = this.leftPos + (this.bookWidth / 2);
        if (mouseX >= this.leftPos && mouseX < midX && this.maxIndexScroll > 0) {
            this.indexScrollOffset = Mth.clamp(this.indexScrollOffset - (float) (scrollY * 18.0), 0.0F, this.maxIndexScroll);
            return true;
        }
        if (mouseX >= midX && mouseX <= this.leftPos + this.bookWidth && this.maxTextScroll > 0) {
            this.textScrollOffset = Mth.clamp(this.textScrollOffset - (float) (scrollY * 18.0), 0.0F, this.maxTextScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) {
            if (this.selectedEntryIndex > 0) {
                selectEntry(this.selectedEntryIndex - 1, true);
                return true;
            }
        } else if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) {
            if (this.selectedEntryIndex < this.volume.entries().size() - 1) {
                selectEntry(this.selectedEntryIndex + 1, true);
                return true;
            }
        } else if (key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_S) {
            if (this.maxTextScroll > 0) {
                this.textScrollOffset = Mth.clamp(this.textScrollOffset + 20.0F, 0.0F, this.maxTextScroll);
                return true;
            }
        } else if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_W) {
            if (this.maxTextScroll > 0) {
                this.textScrollOffset = Mth.clamp(this.textScrollOffset - 20.0F, 0.0F, this.maxTextScroll);
                return true;
            }
        } else if (key == GLFW.GLFW_KEY_PAGE_DOWN) {
            if (this.maxTextScroll > 0) {
                this.textScrollOffset = Mth.clamp(this.textScrollOffset + 120.0F, 0.0F, this.maxTextScroll);
                return true;
            }
        } else if (key == GLFW.GLFW_KEY_PAGE_UP) {
            if (this.maxTextScroll > 0) {
                this.textScrollOffset = Mth.clamp(this.textScrollOffset - 120.0F, 0.0F, this.maxTextScroll);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    private void playPageTurnSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.85F, 1.0F + (this.minecraft.level != null ? (this.minecraft.level.random.nextFloat() - 0.5F) * 0.15F : 0.0F));
        }
    }
}
