package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Procedural pixel-art and geometric renderer for the {@link ArcaneScribingTableScreen}:
 * concentric parchment rings, custom arcane glyphs, pull-out drawer props (wax seals,
 * Materia vials, crystal shards, mutation lever), open Grimoire, and gilded tooltips.
 */
public final class ArcaneScribingGlyphRenderer {

    private ArcaneScribingGlyphRenderer() {
    }

    public static void drawFilledCircle(GuiGraphics g, int cx, int cy, float radius, int argb) {
        int rCeil = Mth.ceil(radius);
        float rSq = radius * radius;
        for (int dy = -rCeil; dy <= rCeil; dy++) {
            float rem = rSq - (dy * dy);
            if (rem < 0.0F) continue;
            int halfW = Mth.floor(Mth.sqrt(rem));
            g.fill(cx - halfW, cy + dy, cx + halfW + 1, cy + dy + 1, argb);
        }
    }

    public static void drawRingBand(GuiGraphics g, int cx, int cy, float innerR, float outerR, int argb) {
        int rOut = Mth.ceil(outerR);
        float outSq = outerR * outerR;
        float inSq = innerR * innerR;
        for (int dy = -rOut; dy <= rOut; dy++) {
            float dySq = dy * dy;
            float remOut = outSq - dySq;
            if (remOut < 0.0F) continue;
            int xOut = Mth.floor(Mth.sqrt(remOut));
            float remIn = inSq - dySq;
            if (remIn <= 0.0F) {
                g.fill(cx - xOut, cy + dy, cx + xOut + 1, cy + dy + 1, argb);
            } else {
                int xIn = Mth.ceil(Mth.sqrt(remIn));
                if (xOut >= xIn) {
                    g.fill(cx - xOut, cy + dy, cx - xIn + 1, cy + dy + 1, argb);
                    g.fill(cx + xIn, cy + dy, cx + xOut + 1, cy + dy + 1, argb);
                }
            }
        }
    }

    public static void drawPixelLine(GuiGraphics g, float x0, float y0, float x1, float y1, int argb) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        int steps = Math.max(1, Mth.ceil(Math.max(Math.abs(dx), Math.abs(dy))));
        float sx = dx / steps;
        float sy = dy / steps;
        float px = x0;
        float py = y0;
        for (int i = 0; i <= steps; i++) {
            int ix = Math.round(px);
            int iy = Math.round(py);
            g.fill(ix, iy, ix + 1, iy + 1, argb);
            px += sx;
            py += sy;
        }
    }

    public static void drawArcWithArrows(GuiGraphics g, int cx, int cy, float radius, float startRad, float endRad, int argb) {
        int steps = Math.max(8, Mth.ceil(Math.abs(endRad - startRad) * radius * 0.85F));
        float prevX = cx + Mth.cos(startRad) * radius;
        float prevY = cy + Mth.sin(startRad) * radius;
        for (int i = 1; i <= steps; i++) {
            float t = (float) i / (float) steps;
            float ang = Mth.lerp(t, startRad, endRad);
            float nx = cx + Mth.cos(ang) * radius;
            float ny = cy + Mth.sin(ang) * radius;
            drawPixelLine(g, prevX, prevY, nx, ny, argb);
            prevX = nx;
            prevY = ny;
        }
        // Arrowheads at both ends showing bidirectional drag rotation
        drawArrowTip(g, cx, cy, radius, endRad, endRad > startRad ? 1.0F : -1.0F, argb);
        drawArrowTip(g, cx, cy, radius, startRad, endRad > startRad ? -1.0F : 1.0F, argb);
    }

    private static void drawArrowTip(GuiGraphics g, int cx, int cy, float radius, float ang, float dirSign, int argb) {
        float tipX = cx + Mth.cos(ang) * radius;
        float tipY = cy + Mth.sin(ang) * radius;
        float tangAngle = ang + dirSign * (Mth.HALF_PI);
        float backAngle1 = tangAngle + 2.55F;
        float backAngle2 = tangAngle - 2.55F;
        drawPixelLine(g, tipX, tipY, tipX + Mth.cos(backAngle1) * 4.2F, tipY + Mth.sin(backAngle1) * 4.2F, argb);
        drawPixelLine(g, tipX, tipY, tipX + Mth.cos(backAngle2) * 4.2F, tipY + Mth.sin(backAngle2) * 4.2F, argb);
    }

    /**
     * Draws the pixel-art open Arcane Grimoire resting on the left side of the desk (State 1, 3, 4).
     */
    public static void drawOpenGrimoire(GuiGraphics g, int x, int y, boolean hovered) {
        int goldTrim = hovered ? 0xFFFCE38A : 0xFFD4AF37;
        int darkWood = 0xFF3E2616;
        // Bookstand & cyan energy bar underneath
        g.fill(x + 6, y + 66, x + 64, y + 71, goldTrim);
        g.fill(x + 6, y + 67, x + 22, y + 70, 0xFF2CE6FF);
        g.fill(x + 48, y + 67, x + 64, y + 70, 0xFF2CE6FF);

        // Leather & Brass Covers (slightly stepped for tilted pixel-art look)
        g.fill(x + 2, y + 6, x + 68, y + 62, darkWood);
        g.fill(x + 1, y + 5, x + 69, y + 8, goldTrim);
        g.fill(x + 1, y + 60, x + 69, y + 63, goldTrim);
        g.fill(x + 1, y + 5, x + 4, y + 63, goldTrim);
        g.fill(x + 66, y + 5, x + 69, y + 63, goldTrim);

        // Left & Right Parchment Pages
        g.fill(x + 5, y + 9, x + 33, y + 59, 0xFFEDE0C4);
        g.fill(x + 37, y + 9, x + 65, y + 59, 0xFFE5D4B3);
        // Page edge shadows
        g.fill(x + 31, y + 9, x + 33, y + 59, 0xFFD2BE96);
        g.fill(x + 37, y + 9, x + 39, y + 59, 0xFFD2BE96);

        // Crimson & Gold Spine Bookmark Ribbon
        g.fill(x + 33, y + 7, x + 37, y + 68, 0xFFA81D34);
        g.fill(x + 34, y + 10, x + 36, y + 65, 0xFF2CE6FF);
        g.fill(x + 33, y + 66, x + 37, y + 70, goldTrim);

        // Left Page Pixel Illumination & Rune Lines (matching State 1)
        g.fill(x + 8, y + 13, x + 19, y + 29, 0xFFE6A817);
        g.fill(x + 10, y + 15, x + 17, y + 27, 0xFFF7E9C8);
        g.fill(x + 13, y + 18, x + 15, y + 24, 0xFF8C1D40);
        g.fill(x + 21, y + 15, x + 29, y + 18, 0xFF4A2545);
        g.fill(x + 21, y + 22, x + 29, y + 25, 0xFF4A2545);
        g.fill(x + 9, y + 34, x + 29, y + 37, 0xFF3A1F38);
        g.fill(x + 9, y + 40, x + 28, y + 43, 0xFF3A1F38);
        // Cyan gem diagram on bottom-left page
        g.fill(x + 9, y + 46, x + 19, y + 55, 0xFF0EA5E9);
        g.fill(x + 11, y + 48, x + 17, y + 53, 0xFF67E8F9);
        g.fill(x + 20, y + 49, x + 29, y + 52, 0xFF3A1F38);

        // Right Page Arcane Crystal Circuit Diagram (matching State 1)
        g.fill(x + 43, y + 15, x + 59, y + 41, 0xFF581C87);
        g.fill(x + 45, y + 17, x + 57, y + 39, 0xFFEADBC0);
        g.fill(x + 47, y + 21, x + 55, y + 35, 0xFF06B6D4);
        g.fill(x + 49, y + 23, x + 53, y + 33, 0xFFA5F3FC);
        // Gold corner nodes
        g.fill(x + 43, y + 15, x + 46, y + 18, 0xFFF59E0B);
        g.fill(x + 56, y + 15, x + 59, y + 18, 0xFFF59E0B);
        g.fill(x + 43, y + 38, x + 46, y + 41, 0xFFF59E0B);
        g.fill(x + 56, y + 38, x + 59, y + 41, 0xFFF59E0B);
        // Pixel teeth runes below diagram
        for (int rx = x + 42; rx <= x + 60; rx += 4) {
            g.fill(rx, y + 46, rx + 2, y + 52, 0xFF6B21A8);
        }
    }

    /**
     * Draws one of the 4 top-shelf pixel glass Materia inkwells with cork stopper.
     */
    public static void drawInkwell(GuiGraphics g, int x, int y, int rgb, boolean focused) {
        int bodyDark = scaleRgb(rgb, 0.52F);
        int bodyMid = 0xFF000000 | rgb;
        int bodyLight = liftRgb(rgb, 80);

        if (focused) {
            g.fill(x - 1, y + 4, x + 17, y + 20, 0xFFFDE047);
        }
        // Cork stopper
        g.fill(x + 4, y, x + 12, y + 6, 0xFFD6B37C);
        g.fill(x + 4, y + 4, x + 12, y + 6, 0xFFA6824C);
        // Glass bottle rim & body
        g.fill(x + 2, y + 5, x + 14, y + 7, bodyLight);
        g.fill(x, y + 7, x + 16, y + 19, bodyDark);
        g.fill(x + 2, y + 8, x + 14, y + 17, bodyMid);
        // Specular glass highlight ("F" shine from the pixel render!)
        g.fill(x + 3, y + 10, x + 5, y + 16, bodyLight);
        g.fill(x + 5, y + 10, x + 10, y + 12, bodyLight);
    }

    /**
     * Draws the top-right arcane Tome Cube pedestal with glowing '?' rune.
     */
    public static void drawCrypticPedestal(GuiGraphics g, Font font, int x, int y, boolean active, boolean hovered) {
        int trim = (active || hovered) ? 0xFFFDE047 : 0xFFD4AF37;
        g.fill(x, y + 14, x + 20, y + 20, trim);
        g.fill(x + 1, y + 16, x + 19, y + 18, 0xFF2CE6FF);
        g.fill(x + 2, y + 1, x + 18, y + 15, 0xFF241438);
        g.fill(x + 2, y + 1, x + 5, y + 15, trim);
        g.fill(x + 15, y + 1, x + 18, y + 15, trim);
        g.fill(x + 2, y + 6, x + 5, y + 9, 0xFF2CE6FF);
        g.fill(x + 15, y + 6, x + 18, y + 9, 0xFF2CE6FF);
        g.drawString(font, "?", x + 8, y + 4, active ? 0xFF67E8F9 : 0xFFFDE047, false);
    }

    /**
     * Draws a circular embossed Wax Seal (Combat=0, Utility=1, Misc=2, Lock=3, Scribe=4).
     */
    public static void drawWaxSeal(GuiGraphics g, int cx, int cy, int radius, int baseRgb, boolean active, boolean hovered, int iconKind) {
        int outerCol = active ? liftRgb(baseRgb, 48) : scaleRgb(baseRgb, hovered ? 0.95F : 0.72F);
        int innerCol = scaleRgb(baseRgb, active ? 0.82F : 0.52F);
        int rimCol = active ? 0xFFFDE68A : (hovered ? 0xFFE5C158 : liftRgb(baseRgb, 24));

        if (active) {
            drawFilledCircle(g, cx, cy, radius + 1.4F, 0xAAFFD700);
        }
        drawFilledCircle(g, cx, cy, radius, outerCol);
        drawRingBand(g, cx, cy, radius - 2.3F, radius - 1.1F, rimCol);
        drawFilledCircle(g, cx, cy, radius - 2.5F, innerCol);

        int glyphCol = active ? 0xFFFFF7CC : liftRgb(baseRgb, 75);
        switch (iconKind) {
            case 0 -> { // Combat: Crossed Blades
                drawPixelLine(g, cx - 3, cy + 3, cx + 3, cy - 3, glyphCol);
                drawPixelLine(g, cx - 3, cy - 3, cx + 3, cy + 3, glyphCol);
            }
            case 1 -> { // Utility: Pick / Compass Cross
                drawPixelLine(g, cx, cy - 3, cx, cy + 3, glyphCol);
                drawPixelLine(g, cx - 3, cy - 1, cx + 3, cy - 1, glyphCol);
            }
            case 2 -> { // Misc: 4-Point Astral Star
                drawPixelLine(g, cx, cy - 4, cx, cy + 4, glyphCol);
                drawPixelLine(g, cx - 4, cy, cx + 4, cy, glyphCol);
                g.fill(cx - 1, cy - 1, cx + 2, cy + 2, glyphCol);
            }
            case 3 -> { // Materia Lock: Padlock / Keyhole
                g.fill(cx - 2, cy - 1, cx + 3, cy + 4, glyphCol);
                g.fill(cx - 1, cy - 3, cx + 2, cy - 1, glyphCol);
            }
            default -> { // Large Golden SCRIBE Seal: Inner Rune Emblem
                drawRingBand(g, cx, cy, radius * 0.42F, radius * 0.56F, glyphCol);
                drawPixelLine(g, cx - 3, cy + 2, cx, cy - 4, glyphCol);
                drawPixelLine(g, cx, cy - 4, cx + 3, cy + 2, glyphCol);
                drawPixelLine(g, cx - 3, cy + 1, cx + 3, cy + 1, glyphCol);
            }
        }
    }

    /**
     * Draws a pixel-art Materia Vial or Faceted Crystal Shard inside the Left Drawer (State 2).
     */
    public static void drawMateriaVialOrShard(GuiGraphics g, int x, int y, int rgb, boolean shardStyle,
                                              boolean selected, boolean hovered, boolean unattuned) {
        if (selected) {
            g.fill(x - 1, y - 1, x + 15, y + 21, 0xFFFDE047);
            g.fill(x, y, x + 14, y + 20, 0xFF2B124C);
        } else if (hovered) {
            g.fill(x - 1, y - 1, x + 15, y + 21, 0x8838BDF8);
        }

        int dark = unattuned ? 0xFF475569 : scaleRgb(rgb, 0.52F);
        int mid = unattuned ? 0xFF94A3B8 : (0xFF000000 | rgb);
        int bright = unattuned ? 0xFFE2E8F0 : liftRgb(rgb, 90);

        if (!shardStyle) {
            // Pixel Potion Vial (cork + glass neck + glowing Materia fill)
            g.fill(x + 5, y + 1, x + 9, y + 4, 0xFFD4A373);
            g.fill(x + 4, y + 4, x + 10, y + 6, 0xFFCBD5E1);
            g.fill(x + 2, y + 6, x + 12, y + 19, dark);
            g.fill(x + 3, y + 8, x + 11, y + 18, mid);
            g.fill(x + 4, y + 9, x + 6, y + 15, bright);
            g.fill(x + 7, y + 12, x + 9, y + 14, bright);
        } else {
            // Faceted Materia Crystal Cluster (matching State 2's vibrant shards!)
            g.fill(x + 6, y + 1, x + 8, y + 4, bright);
            g.fill(x + 4, y + 4, x + 10, y + 9, mid);
            g.fill(x + 2, y + 8, x + 12, y + 15, mid);
            g.fill(x + 4, y + 15, x + 10, y + 19, dark);
            // Left bright facet & right shadow facet
            g.fill(x + 4, y + 5, x + 7, y + 15, bright);
            g.fill(x + 8, y + 7, x + 11, y + 16, dark);
        }
    }

    /**
     * Draws the physical Gilded Brass & Glowing Crystal Mutation Switch (State 2 bottom compartment).
     */
    public static void drawMutationSwitch(GuiGraphics g, Font font, int x, int y, int width,
                                          boolean enabled, boolean canEnable, int crystalRgb, boolean hovered) {
        String label = !canEnable ? "MUTATION: LOCKED" : (enabled ? "MUTATION: ON" : "MUTATION: OFF");
        int labelCol = !canEnable ? 0xFF94A3B8 : (enabled ? 0xFF4ADE80 : 0xFFCBD5E1);
        g.drawString(font, label, x + 22, y, labelCol, false);

        int railY = y + 13;
        int railLeft = x + 10;
        int railRight = x + width - 10;
        int brass = hovered ? 0xFFFCE38A : 0xFFD4AF37;
        int brassDark = 0xFF8C6B1F;

        // Glowing crystal core track
        int trackGlow = !canEnable ? 0xFF334155 : (enabled ? liftRgb(crystalRgb, 65) : 0xFF38BDF8);
        g.fill(railLeft, railY + 2, railRight, railY + 7, brassDark);
        g.fill(railLeft + 4, railY + 3, railRight - 4, railY + 6, trackGlow);

        // Crystal tips on both ends of the switch
        g.fill(railLeft - 4, railY + 3, railLeft, railY + 6, trackGlow);
        g.fill(railRight, railY + 3, railRight + 4, railY + 6, trackGlow);

        // Brass end collars
        g.fill(railLeft + 2, railY, railLeft + 6, railY + 9, brass);
        g.fill(railRight - 6, railY, railRight - 2, railY + 9, brass);

        // Sliding Ornate Cross-Guard Plunger (Left = OFF, Right = ON)
        int knobCenter = enabled ? (railRight - 18) : (railLeft + 18);
        g.fill(knobCenter - 7, railY - 1, knobCenter + 7, railY + 10, brassDark);
        g.fill(knobCenter - 6, railY, knobCenter + 6, railY + 9, brass);
        // Vertical cross-guard fins + crystal tips
        g.fill(knobCenter - 2, railY - 4, knobCenter + 2, railY + 13, brass);
        g.fill(knobCenter - 1, railY - 6, knobCenter + 1, railY - 4, trackGlow);
        g.fill(knobCenter - 1, railY + 13, knobCenter + 1, railY + 15, trackGlow);
        g.fill(knobCenter - 3, railY + 3, knobCenter + 3, railY + 6, trackGlow);
    }

    /**
     * Draws the sweeping golden & white Feather Quill + sparkles during State 4's Scribe flash!
     */
    public static void drawQuillInscribing(GuiGraphics g, int cx, int cy, float progress01) {
        float sweep = Mth.sin(progress01 * Mth.PI * 2.0F) * 14.0F;
        int tipX = Math.round(cx + 4 + sweep * 0.45F);
        int tipY = Math.round(cy + 6 - Math.abs(sweep) * 0.25F);

        // Golden nib & shaft
        drawPixelLine(g, tipX, tipY, tipX + 34, tipY - 22, 0xFFFDE047);
        drawPixelLine(g, tipX + 1, tipY, tipX + 35, tipY - 22, 0xFFD97706);

        // White & cyan feather vane
        for (int i = 8; i <= 32; i += 2) {
            int fx = tipX + i;
            int fy = tipY - Math.round(i * 0.65F);
            g.fill(fx - 3, fy - 5, fx + 3, fy, (i % 6 == 0) ? 0xFF67E8F9 : 0xFFF8FAFC);
            g.fill(fx, fy, fx + 4, fy + 3, 0xFFE2E8F0);
        }

        // Starlight sparkles
        for (int s = 0; s < 6; s++) {
            float ang = progress01 * 8.0F + s * 1.05F;
            int sx = tipX + Math.round(Mth.cos(ang) * (8 + s * 4));
            int sy = tipY - Math.round(Mth.sin(ang) * (6 + s * 3));
            g.fill(sx - 1, sy, sx + 2, sy + 1, 0xFFFFFBEB);
            g.fill(sx, sy - 1, sx + 1, sy + 2, 0xFFFACC15);
        }
    }

    public static void drawFlowSymbol(GuiGraphics g, Font font, int sx, int sy, SpellFlowModifier flow, boolean cryptic, int col) {
        if (cryptic) {
            drawCrypticQuestion(g, font, sx, sy);
            return;
        }
        switch (flow) {
            case SINGLE -> {
                drawPixelLine(g, sx, sy + 3, sx, sy - 3, col);
                drawPixelLine(g, sx, sy - 3, sx - 2, sy - 1, col);
                drawPixelLine(g, sx, sy - 3, sx + 2, sy - 1, col);
            }
            case DOUBLE_BIFURCATE -> {
                drawPixelLine(g, sx, sy + 3, sx, sy, col);
                drawPixelLine(g, sx, sy, sx - 3, sy - 3, col);
                drawPixelLine(g, sx, sy, sx + 3, sy - 3, col);
            }
            case TRIPLE_TRIFURCATE -> {
                for (int dx = -2; dx <= 2; dx += 2) {
                    g.fill(sx + dx, sy - 3, sx + dx + 1, sy + 4, col);
                }
            }
            case QUAD_SPREAD -> {
                drawPixelLine(g, sx - 3, sy, sx + 3, sy, col);
                drawPixelLine(g, sx, sy - 3, sx, sy + 3, col);
            }
            case QUINTUPLE_ARC, SEXTUPLE_RING, SEPTUPLE_CROWN, OCTUPLE_DOME -> {
                drawRingBand(g, sx, sy, 2.0F, 3.4F, col);
                g.fill(sx, sy - 4, sx + 1, sy + 5, col);
            }
            case CHAIN_LEAP -> {
                drawPixelLine(g, sx - 3, sy + 3, sx, sy, col);
                drawPixelLine(g, sx, sy, sx - 1, sy - 1, col);
                drawPixelLine(g, sx - 1, sy - 1, sx + 3, sy - 3, col);
            }
            case ORBIT_SATELLITE -> {
                drawRingBand(g, sx, sy, 2.2F, 3.2F, col);
                g.fill(sx + 2, sy - 3, sx + 4, sy - 1, col);
                g.fill(sx - 3, sy + 1, sx - 1, sy + 3, col);
            }
            case VOID_WARP -> {
                g.fill(sx - 3, sy - 3, sx - 1, sy + 4, col);
                g.fill(sx + 2, sy - 3, sx + 4, sy + 4, col);
                g.fill(sx, sy - 1, sx + 1, sy + 2, col);
            }
            case FAST_DART -> {
                drawPixelLine(g, sx - 3, sy + 2, sx + 3, sy - 2, col);
                drawPixelLine(g, sx - 3, sy, sx + 1, sy - 3, col);
            }
            case SLOW_HEAVY -> {
                g.fill(sx - 3, sy - 2, sx + 4, sy + 3, col);
                g.fill(sx - 1, sy - 3, sx + 2, sy + 4, col);
            }
            default -> {
                drawRingBand(g, sx, sy, 1.5F, 3.0F, col);
                g.fill(sx, sy, sx + 1, sy + 1, col);
            }
        }
    }

    public static void drawFormSymbol(GuiGraphics g, Font font, int sx, int sy, SpellDeliveryForm form, boolean cryptic, int col) {
        if (cryptic) {
            drawCrypticQuestion(g, font, sx, sy);
            return;
        }
        switch (form) {
            case PROJECTILE -> { // Spearhead arrow (State 1 & State 3 12 o'clock)
                drawPixelLine(g, sx - 3, sy + 3, sx + 3, sy - 3, col);
                drawPixelLine(g, sx + 3, sy - 3, sx, sy - 3, col);
                drawPixelLine(g, sx + 3, sy - 3, sx + 3, sy, col);
            }
            case BEAM -> { // Focused ray + tip burst
                drawPixelLine(g, sx - 3, sy + 3, sx + 2, sy - 2, col);
                g.fill(sx + 1, sy - 4, sx + 4, sy - 1, col);
            }
            case RADIAL_BURST -> { // 8-ray starburst
                drawPixelLine(g, sx - 3, sy, sx + 3, sy, col);
                drawPixelLine(g, sx, sy - 3, sx, sy + 3, col);
                drawPixelLine(g, sx - 2, sy - 2, sx + 2, sy + 2, col);
                drawPixelLine(g, sx - 2, sy + 2, sx + 2, sy - 2, col);
            }
            case TOUCH_STRIKE -> { // Kite shield / strike crest
                g.fill(sx - 3, sy - 3, sx + 4, sy - 2, col);
                drawPixelLine(g, sx - 3, sy - 2, sx, sy + 3, col);
                drawPixelLine(g, sx + 3, sy - 2, sx, sy + 3, col);
            }
            case AURA -> {
                drawRingBand(g, sx, sy, 2.2F, 3.5F, col);
                g.fill(sx - 1, sy - 1, sx + 2, sy + 2, col);
            }
            case GLYPH -> {
                drawRingBand(g, sx, sy, 2.5F, 3.6F, col);
                drawPixelLine(g, sx - 2, sy - 2, sx + 2, sy + 2, col);
                drawPixelLine(g, sx - 2, sy + 2, sx + 2, sy - 2, col);
            }
            case CONE -> {
                drawPixelLine(g, sx, sy + 3, sx - 3, sy - 3, col);
                drawPixelLine(g, sx, sy + 3, sx + 3, sy - 3, col);
                drawPixelLine(g, sx - 3, sy - 3, sx + 3, sy - 3, col);
            }
            case SKYFALL -> {
                drawPixelLine(g, sx - 2, sy - 3, sx - 2, sy + 2, col);
                drawPixelLine(g, sx + 2, sy - 3, sx + 2, sy + 2, col);
                g.fill(sx, sy - 1, sx + 1, sy + 4, col);
            }
            default -> { // Eidolon Summons (crowned diamond)
                drawPixelLine(g, sx, sy - 3, sx + 3, sy, col);
                drawPixelLine(g, sx + 3, sy, sx, sy + 3, col);
                drawPixelLine(g, sx, sy + 3, sx - 3, sy, col);
                drawPixelLine(g, sx - 3, sy, sx, sy - 3, col);
                g.fill(sx, sy, sx + 1, sy + 1, col);
            }
        }
    }

    public static void drawEffectSymbol(GuiGraphics g, Font font, int sx, int sy, @Nullable SpellCoreEffect effect, boolean cryptic, int col) {
        if (effect == null) {
            // Empty Dual-Verb core: subtle small dot
            g.fill(sx - 1, sy, sx + 2, sy + 1, 0x888C6B1F);
            return;
        }
        if (cryptic) {
            drawCrypticQuestion(g, font, sx, sy);
            return;
        }
        switch (effect) {
            case HARM -> {
                drawPixelLine(g, sx - 3, sy - 3, sx + 3, sy + 3, col);
                drawPixelLine(g, sx - 3, sy + 3, sx + 3, sy - 3, col);
            }
            case WARD -> {
                g.fill(sx - 2, sy - 3, sx + 3, sy - 2, col);
                g.fill(sx - 3, sy - 2, sx - 2, sy + 1, col);
                g.fill(sx + 2, sy - 2, sx + 3, sy + 1, col);
                drawPixelLine(g, sx - 2, sy + 1, sx, sy + 3, col);
                drawPixelLine(g, sx + 2, sy + 1, sx, sy + 3, col);
            }
            case RESTORATION -> {
                g.fill(sx - 1, sy - 3, sx + 2, sy + 4, col);
                g.fill(sx - 3, sy - 1, sx + 4, sy + 2, col);
            }
            case KINETIC, FLIGHT, GRAPPLE, GRAVITY -> {
                drawPixelLine(g, sx - 3, sy - 2, sx + 2, sy - 2, col);
                drawPixelLine(g, sx - 2, sy, sx + 3, sy, col);
                drawPixelLine(g, sx - 3, sy + 2, sx + 1, sy + 2, col);
            }
            case BLINK -> {
                g.fill(sx - 3, sy - 1, sx - 1, sy + 2, col);
                g.fill(sx + 2, sy - 1, sx + 4, sy + 2, col);
                drawPixelLine(g, sx - 1, sy, sx + 2, sy, col);
            }
            case AFFLICTION -> {
                drawRingBand(g, sx, sy, 1.8F, 3.0F, col);
                g.fill(sx, sy - 1, sx + 1, sy + 2, col);
            }
            default -> { // Transmute, Break, Conjure, Harvest, Sonar, etc.
                drawPixelLine(g, sx, sy - 3, sx - 3, sy + 2, col);
                drawPixelLine(g, sx, sy - 3, sx + 3, sy + 2, col);
                drawPixelLine(g, sx - 3, sy + 2, sx + 3, sy + 2, col);
            }
        }
    }

    private static void drawCrypticQuestion(GuiGraphics g, Font font, int sx, int sy) {
        g.drawString(font, "?", sx - 2, sy - 4, 0xFFE879F9, true);
    }

    /**
     * Renders the double-gilded celestial-navy Spell Decision Tooltip card matching State 3.
     */
    public static void drawGildedTooltipBox(GuiGraphics g, Font font, List<Component> lines,
                                            int mouseX, int mouseY, int screenW, int screenH) {
        if (lines == null || lines.isEmpty()) return;

        int maxTextW = 0;
        for (Component c : lines) {
            maxTextW = Math.max(maxTextW, font.width(c));
        }

        int padX = 7;
        int padY = 6;
        int lineH = 10;
        int boxW = maxTextW + padX * 2;
        int boxH = padY * 2 + lines.size() * lineH + 2;

        int bx = mouseX + 12;
        int by = mouseY - 12;
        if (bx + boxW + 4 > screenW) {
            bx = Math.max(4, mouseX - boxW - 12);
        }
        if (by + boxH + 4 > screenH) {
            by = screenH - boxH - 4;
        }
        if (by < 4) {
            by = 4;
        }

        // Drop shadow
        g.fill(bx + 2, by + 2, bx + boxW + 2, by + boxH + 2, 0x99000000);
        // Outer gilded brass frame
        g.fill(bx, by, bx + boxW, by + boxH, 0xFFD4AF37);
        // Deep celestial navy interior
        g.fill(bx + 1, by + 1, bx + boxW - 1, by + boxH - 1, 0xF60B132B);
        // Inner fine gold filigree line
        g.fill(bx + 2, by + 2, bx + boxW - 2, by + 3, 0xAA9E7B23);
        g.fill(bx + 2, by + boxH - 3, bx + boxW - 2, by + boxH - 2, 0xAA9E7B23);
        g.fill(bx + 2, by + 2, bx + 3, by + boxH - 2, 0xAA9E7B23);
        g.fill(bx + boxW - 3, by + 2, bx + boxW - 2, by + boxH - 2, 0xAA9E7B23);

        // Corner gold rivets (matching State 3's ornate corners!)
        g.fill(bx - 1, by - 1, bx + 2, by + 2, 0xFFFCE38A);
        g.fill(bx + boxW - 2, by - 1, bx + boxW + 1, by + 2, 0xFFFCE38A);
        g.fill(bx - 1, by + boxH - 2, bx + 2, by + boxH + 1, 0xFFFCE38A);
        g.fill(bx + boxW - 2, by + boxH - 2, bx + boxW + 1, by + boxH + 1, 0xFFFCE38A);

        int ty = by + padY;
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), bx + padX, ty, 0xFFFFFFFF, true);
            ty += (i == 0) ? (lineH + 2) : lineH;
        }
    }

    public static int scaleRgb(int rgb, float factor) {
        int r = Mth.clamp(Math.round(((rgb >> 16) & 0xFF) * factor), 0, 255);
        int gr = Mth.clamp(Math.round(((rgb >> 8) & 0xFF) * factor), 0, 255);
        int b = Mth.clamp(Math.round((rgb & 0xFF) * factor), 0, 255);
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }

    public static int liftRgb(int rgb, int boost) {
        int r = Mth.clamp(((rgb >> 16) & 0xFF) + boost, 0, 255);
        int gr = Mth.clamp(((rgb >> 8) & 0xFF) + boost, 0, 255);
        int b = Mth.clamp((rgb & 0xFF) + boost, 0, 255);
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }
}
