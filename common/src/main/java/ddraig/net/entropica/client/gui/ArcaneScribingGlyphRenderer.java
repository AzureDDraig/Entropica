package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellTriggerType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * High-resolution texture and sub-pixel renderer for {@link ArcaneScribingTableScreen}.
 * Uses supersampled 128x128–512x512 RGBA GUI textures with bilinear filtering, floating-point
 * sub-pixel matrix transforms, smooth rotated ring layers, and crisp scaled typography.
 */
public final class ArcaneScribingGlyphRenderer {

    private static final String BASE = "textures/gui/scribing/";

    public static final ResourceLocation TEX_SCROLL_PARCHMENT = tex("scroll_parchment.png");
    public static final ResourceLocation TEX_WHEEL_BAND_DIST = tex("wheel_band_dist.png");
    public static final ResourceLocation TEX_WHEEL_RIM_DIST = tex("wheel_rim_dist.png");
    public static final ResourceLocation[] TEX_WHEEL_BAND = {
            tex("wheel_band_0.png"), tex("wheel_band_1.png"), tex("wheel_band_2.png"), tex("wheel_band_3.png")
    };
    public static final ResourceLocation[] TEX_WHEEL_RIM = {
            tex("wheel_rim_0.png"), tex("wheel_rim_1.png"), tex("wheel_rim_2.png"), tex("wheel_rim_3.png")
    };
    public static final float INNER_WHEEL_SCALE = 0.845F;
    private static final float INNER_WHEEL_DRAW_SIZE = 128.0F * INNER_WHEEL_SCALE;
    private static final float CORE_MEDALLION_DRAW_SIZE = 64.0F * INNER_WHEEL_SCALE;
    public static final ResourceLocation TEX_WHEEL_CORE_BASE = tex("wheel_core_base.png");
    public static final ResourceLocation TEX_WHEEL_CORE_RIM = tex("wheel_core_rim.png");
    public static final ResourceLocation TEX_WHEEL_ARC_ARROWS = tex("wheel_arc_arrows.png");
    public static final ResourceLocation TEX_WHEEL_SEL_HALO = tex("wheel_selection_halo.png");
    public static final ResourceLocation TEX_CEREMONY_CIRCUIT = tex("ceremony_star_circuit.png");
    public static final ResourceLocation TEX_ALIGNMENT_NEEDLE = tex("alignment_needle.png");

    public static final ResourceLocation TEX_OPEN_GRIMOIRE = tex("open_grimoire.png");
    public static final ResourceLocation TEX_INKWELL_GLASS = tex("inkwell_glass.png");
    public static final ResourceLocation TEX_INKWELL_LIQUID = tex("inkwell_liquid.png");
    public static final ResourceLocation TEX_CRYPTIC_PEDESTAL = tex("cryptic_pedestal.png");
    public static final ResourceLocation TEX_WAX_SEAL_BODY = tex("wax_seal_body.png");
    public static final ResourceLocation[] TEX_WAX_SEAL_ICON = {
            tex("wax_seal_icon_0.png"), tex("wax_seal_icon_1.png"), tex("wax_seal_icon_2.png"),
            tex("wax_seal_icon_3.png"), tex("wax_seal_icon_4.png")
    };
    public static final ResourceLocation TEX_VIAL_GLASS = tex("materia_vial_glass.png");
    public static final ResourceLocation TEX_VIAL_LIQUID = tex("materia_vial_liquid.png");
    public static final ResourceLocation TEX_MATERIA_SHARD = tex("materia_shard.png");
    public static final ResourceLocation TEX_MUTATION_TRACK = tex("mutation_track.png");
    public static final ResourceLocation TEX_MUTATION_CORE = tex("mutation_track_core.png");
    public static final ResourceLocation TEX_MUTATION_PLUNGER = tex("mutation_plunger.png");
    public static final ResourceLocation TEX_QUILL_FEATHER = tex("quill_feather.png");
    public static final ResourceLocation TEX_CORNER_BRACKET = tex("corner_bracket.png");
    public static final ResourceLocation TEX_DRAWER_HANDLE = tex("drawer_handle.png");

    public static final ResourceLocation TEX_RUNE_CRYPTIC = tex("runes/rune_cryptic.png");
    public static final ResourceLocation TEX_RUNE_EMPTY_CORE = tex("runes/rune_empty_core.png");

    private static final Map<SpellTriggerType, ResourceLocation> TRIGGER_RUNES = new EnumMap<>(SpellTriggerType.class);
    private static final Map<SpellFlowModifier, ResourceLocation> FLOW_RUNES = new EnumMap<>(SpellFlowModifier.class);
    private static final Map<SpellDeliveryForm, ResourceLocation> FORM_RUNES = new EnumMap<>(SpellDeliveryForm.class);
    private static final Map<SpellCoreEffect, ResourceLocation> EFFECT_RUNES = new EnumMap<>(SpellCoreEffect.class);

    static {
        for (SpellTriggerType t : SpellTriggerType.values()) {
            TRIGGER_RUNES.put(t, tex("runes/trigger_" + t.getSerializedName() + ".png"));
        }
        for (SpellFlowModifier f : SpellFlowModifier.values()) {
            FLOW_RUNES.put(f, tex("runes/flow_" + f.getSerializedName() + ".png"));
        }
        for (SpellDeliveryForm f : SpellDeliveryForm.values()) {
            FORM_RUNES.put(f, tex("runes/form_" + f.getSerializedName() + ".png"));
        }
        for (SpellCoreEffect e : SpellCoreEffect.values()) {
            EFFECT_RUNES.put(e, tex("runes/effect_" + e.getSerializedName() + ".png"));
        }
    }

    private ArcaneScribingGlyphRenderer() {
    }

    private static ResourceLocation tex(String subPath) {
        return ResourceLocation.fromNamespaceAndPath(Entropica.MODID, BASE + subPath);
    }

    // =========================================================================
    // SUB-PIXEL & HIGH-RESOLUTION PRIMITIVES
    // =========================================================================

    public static void blitRect(GuiGraphics g, ResourceLocation texture, int texW, int texH,
                                float x, float y, float drawW, float drawH, int argb) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(drawW / (float) texW, drawH / (float) texH);
        g.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0F, 0.0F, texW, texH, texW, texH, argb);
        g.pose().popMatrix();
    }

    public static void blitCentered(GuiGraphics g, ResourceLocation texture, int texW, int texH,
                                    float cx, float cy, float drawW, float drawH, float rotRad, int argb) {
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        if (rotRad != 0.0F) {
            g.pose().rotate(rotRad);
        }
        g.pose().scale(drawW / (float) texW, drawH / (float) texH);
        g.blit(RenderPipelines.GUI_TEXTURED, texture, -texW / 2, -texH / 2, 0.0F, 0.0F, texW, texH, texW, texH, argb);
        g.pose().popMatrix();
    }

    /**
     * Sub-pixel rectangle fill at 0.25 GUI-unit (4x) precision for razor-sharp borders and hairlines.
     */
    public static void fillHiRes(GuiGraphics g, float x0, float y0, float x1, float y1, int argb) {
        int ix0 = Math.round(x0 * 4.0F);
        int iy0 = Math.round(y0 * 4.0F);
        int ix1 = Math.max(ix0 + 1, Math.round(x1 * 4.0F));
        int iy1 = Math.max(iy0 + 1, Math.round(y1 * 4.0F));
        g.pose().pushMatrix();
        g.pose().scale(0.25F, 0.25F);
        g.fill(ix0, iy0, ix1, iy1, argb);
        g.pose().popMatrix();
    }

    public static void drawBeveledBox(GuiGraphics g, float x, float y, float w, float h,
                                      int fillArgb, int outerRimArgb, int innerHighlightArgb) {
        fillHiRes(g, x, y, x + w, y + h, outerRimArgb);
        fillHiRes(g, x + 0.5F, y + 0.5F, x + w - 0.5F, y + h - 0.5F, fillArgb);
        fillHiRes(g, x + 0.75F, y + 0.75F, x + w - 0.75F, y + 1.25F, innerHighlightArgb);
    }

    public static void drawRadialTick(GuiGraphics g, float cx, float cy, float angleRad,
                                      float innerR, float outerR, float thickness, int argb) {
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().rotate(angleRad);
        g.pose().scale(0.25F, 0.25F);
        int x0 = Math.round(innerR * 4.0F);
        int x1 = Math.max(x0 + 1, Math.round(outerR * 4.0F));
        int hy = Math.max(1, Math.round(thickness * 2.0F));
        g.fill(x0, -hy, x1, hy, argb);
        g.pose().popMatrix();
    }

    public static void drawScaledString(GuiGraphics g, Font font, String text,
                                        float x, float y, float scale, int argb, boolean shadow) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.drawString(font, text, 0, 0, argb, shadow);
        g.pose().popMatrix();
    }

    public static void drawScaledComponent(GuiGraphics g, Font font, Component text,
                                           float x, float y, float scale, int argb, boolean shadow) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.drawString(font, text, 0, 0, argb, shadow);
        g.pose().popMatrix();
    }

    public static void drawScaledCenteredString(GuiGraphics g, Font font, String text,
                                                float cx, float y, float scale, int argb, boolean shadow) {
        float halfW = (font.width(text) * scale) * 0.5F;
        drawScaledString(g, font, text, cx - halfW, y, scale, argb, shadow);
    }

    /**
     * Trims a string dynamically to fit a pixel width at the given font scale without premature '...' clipping.
     */
    public static String fitTextToWidth(Font font, String text, float maxGuiWidth, float scale) {
        if (text == null || text.isEmpty()) return "";
        int maxUnscaled = Math.max(8, Mth.floor(maxGuiWidth / scale));
        if (font.width(text) <= maxUnscaled) {
            return text;
        }
        String ell = "…";
        int target = Math.max(4, maxUnscaled - font.width(ell));
        return font.plainSubstrByWidth(text, target) + ell;
    }

    // =========================================================================
    // HIGH-RES CONCENTRIC WHEEL & SCROLL RENDERERS
    // =========================================================================

    public static void drawParchmentScroll(GuiGraphics g, float x, float y, float w, float h) {
        blitRect(g, TEX_SCROLL_PARCHMENT, 512, 448, x, y, w, h, 0xFFFFFFFF);
    }

    public static void drawOuterDistanceBand(GuiGraphics g, float cx, float cy, float rotRad,
                                             boolean hovered, boolean ceremony) {
        int bandTint = ceremony ? 0xFFFFF3C4 : (hovered ? 0xFFE8FAFF : 0xFFD4ECFA);
        int rimCol = ceremony ? 0xFFFEF08A : (hovered ? 0xFF7DF4FF : 0xFFE5BF55);
        blitCentered(g, TEX_WHEEL_BAND_DIST, 512, 512, cx, cy, 128.0F, 128.0F, rotRad, bandTint);
        blitCentered(g, TEX_WHEEL_RIM_DIST, 512, 512, cx, cy, 128.0F, 128.0F, rotRad, rimCol);
    }

    public static void drawDistanceStopBadge(GuiGraphics g, Font font, float sx, float sy,
                                             String label, boolean alignedAtTop, boolean hovered) {
        float textScale = alignedAtTop ? 0.60F : 0.52F;
        float textW = font.width(label) * textScale;
        float boxW = Math.max(10.5F, textW + 2.8F);
        float boxH = alignedAtTop ? 6.6F : 5.8F;
        float bx = sx - boxW * 0.5F;
        float by = sy - boxH * 0.5F;

        if (alignedAtTop) {
            blitCentered(g, TEX_WHEEL_SEL_HALO, 128, 128, sx, sy, boxW + 3.2F, boxH + 4.4F, 0.0F, 0xCC7DF4FF);
            drawBeveledBox(g, bx, by, boxW, boxH, 0xEE0D1F33, 0xFFFDE047, 0xAAFFFBEB);
        } else if (hovered) {
            drawBeveledBox(g, bx, by, boxW, boxH, 0xCC122840, 0xCC7DF4FF, 0x55FFFFFF);
        } else {
            drawBeveledBox(g, bx, by, boxW, boxH, 0xB80B1829, 0x99C79F3E, 0x33FFFFFF);
        }

        int textCol = alignedAtTop ? 0xFFFEF08A : (hovered ? 0xFFE0F7FA : 0xFFDbeafe);
        drawScaledCenteredString(g, font, label, sx, sy - (4.0F * textScale) + 0.35F, textScale, textCol, true);
    }

    public static void drawWheelBand(GuiGraphics g, int ringIndex, float cx, float cy, float rotRad,
                                     int matRgb, boolean focused, boolean hovered, boolean ceremony) {
        int bandTint;
        int rimCol;
        if (ringIndex == 0) {
            // Trigger Logic Ring: crisp astrological silver-violet band so Trigger Logic reads distinctly from Materia rings
            bandTint = ceremony ? 0xFFFFF3C4 : (hovered ? 0xFFF5ECFF : 0xFFE3D6F2);
            rimCol = ceremony ? 0xFFFEF08A : (hovered ? 0xFF7DF4FF : 0xFFE2B842);
        } else {
            bandTint = ceremony ? 0xFFFFF3C4 : (hovered ? 0xFFFFE8C2 : 0xFFEAD5B0);
            rimCol = ceremony ? 0xFFFEF08A : ((focused || hovered) ? (0xFF000000 | liftRgb(matRgb, 28)) : 0xFFD4AF37);
        }
        blitCentered(g, TEX_WHEEL_BAND[ringIndex], 512, 512, cx, cy, INNER_WHEEL_DRAW_SIZE, INNER_WHEEL_DRAW_SIZE, rotRad, bandTint);
        blitCentered(g, TEX_WHEEL_RIM[ringIndex], 512, 512, cx, cy, INNER_WHEEL_DRAW_SIZE, INNER_WHEEL_DRAW_SIZE, 0.0F, rimCol);
    }

    public static void drawWheelCoreMedallion(GuiGraphics g, float cx, float cy,
                                              int dualRgb, boolean hasDualEffect,
                                              boolean focused, boolean hovered, boolean ceremony) {
        int coreTint = hasDualEffect ? scaleRgb(dualRgb, 0.55F) : 0xFF6B4F28;
        int rimTint = (ceremony || focused || hovered) ? (0xFF000000 | liftRgb(dualRgb, 35)) : 0xFFD4AF37;
        blitCentered(g, TEX_WHEEL_CORE_BASE, 256, 256, cx, cy, CORE_MEDALLION_DRAW_SIZE, CORE_MEDALLION_DRAW_SIZE, 0.0F, coreTint);
        blitCentered(g, TEX_WHEEL_CORE_RIM, 256, 256, cx, cy, CORE_MEDALLION_DRAW_SIZE, CORE_MEDALLION_DRAW_SIZE, 0.0F, rimTint);
    }

    public static void drawArcWithArrows(GuiGraphics g, float cx, float cy, float radius, float rotRad, int argb) {
        // wheel_arc_arrows.png has its arc at r = 222 / 256 of half-size -> scale = radius / (222 / 512 * size)
        float drawSize = (radius / 222.0F) * 512.0F;
        blitCentered(g, TEX_WHEEL_ARC_ARROWS, 256, 256, cx, cy, drawSize, drawSize, rotRad, argb);
    }

    public static void drawCeremonyStarCircuit(GuiGraphics g, float cx, float cy, float rotRad) {
        blitCentered(g, TEX_CEREMONY_CIRCUIT, 512, 512, cx, cy, 128.0F, 128.0F, rotRad, 0xEEFFFFFF);
    }

    public static void drawSelectionHalo(GuiGraphics g, float sx, float sy, float size, boolean primarySelected) {
        int tint = primarySelected ? 0xFFFFFFFF : 0xAA94A3B8;
        blitCentered(g, TEX_WHEEL_SEL_HALO, 128, 128, sx, sy, size, size, 0.0F, tint);
    }

    public static void drawAlignmentNeedle(GuiGraphics g, float cx, float topY, boolean ceremony) {
        int tint = ceremony ? 0xFFFFFFFF : 0xFFFFF8DC;
        blitRect(g, TEX_ALIGNMENT_NEEDLE, 128, 256, cx - 9.0F, topY - 3.0F, 18.0F, 50.0F, tint);
    }

    // =========================================================================
    // HIGH-RES 128x128 SPELL RUNE SYMBOLS
    // =========================================================================

    public static void drawTriggerSymbol(GuiGraphics g, float sx, float sy, float size,
                                         SpellTriggerType trigger, boolean cryptic, int col) {
        ResourceLocation rl = cryptic ? TEX_RUNE_CRYPTIC : TRIGGER_RUNES.getOrDefault(trigger, TEX_RUNE_CRYPTIC);
        drawCrispRune(g, rl, sx, sy, size, cryptic ? 0xFFF0ABFC : col);
    }

    public static void drawFlowSymbol(GuiGraphics g, float sx, float sy, float size,
                                      SpellFlowModifier flow, boolean cryptic, int col) {
        ResourceLocation rl = cryptic ? TEX_RUNE_CRYPTIC : FLOW_RUNES.getOrDefault(flow, TEX_RUNE_CRYPTIC);
        drawCrispRune(g, rl, sx, sy, size, cryptic ? 0xFFF0ABFC : col);
    }

    public static void drawFormSymbol(GuiGraphics g, float sx, float sy, float size,
                                      SpellDeliveryForm form, boolean cryptic, int col) {
        ResourceLocation rl = cryptic ? TEX_RUNE_CRYPTIC : FORM_RUNES.getOrDefault(form, TEX_RUNE_CRYPTIC);
        drawCrispRune(g, rl, sx, sy, size, cryptic ? 0xFFF0ABFC : col);
    }

    public static void drawEffectSymbol(GuiGraphics g, float sx, float sy, float size,
                                        @Nullable SpellCoreEffect effect, boolean cryptic, int col) {
        if (effect == null) {
            blitCentered(g, TEX_RUNE_EMPTY_CORE, 128, 128, sx, sy, size * 0.9F, size * 0.9F, 0.0F, 0x99D4AF37);
            return;
        }
        ResourceLocation rl = cryptic ? TEX_RUNE_CRYPTIC : EFFECT_RUNES.getOrDefault(effect, TEX_RUNE_CRYPTIC);
        drawCrispRune(g, rl, sx, sy, size, cryptic ? 0xFFF0ABFC : col);
    }

    private static void drawCrispRune(GuiGraphics g, ResourceLocation runeTex, float sx, float sy, float size, int col) {
        // Subtle 0.35-unit drop shadow so bright runes remain razor-sharp over warm gold/parchment
        blitCentered(g, runeTex, 128, 128, sx + 0.35F, sy + 0.35F, size, size, 0.0F, 0x99090514);
        blitCentered(g, runeTex, 128, 128, sx, sy, size, size, 0.0F, 0xFF000000 | col);
    }

    // =========================================================================
    // HIGH-RES TABLETOP PROPS, SEALS, VIALS & CONTROLS
    // =========================================================================

    public static void drawCornerBracket(GuiGraphics g, float x, float y, float size, boolean left, boolean top) {
        float rot = 0.0F;
        if (!left && top) rot = Mth.HALF_PI;
        else if (!left && !top) rot = Mth.PI;
        else if (left && !top) rot = -Mth.HALF_PI;
        blitCentered(g, TEX_CORNER_BRACKET, 128, 128, x + size * 0.5F, y + size * 0.5F, size, size, rot, 0xFFFFFFFF);
    }

    public static void drawDrawerHandle(GuiGraphics g, float x, float y, float w, float h, boolean hovered) {
        int tint = hovered ? 0xFFFFFFFF : 0xFFEAD8B1;
        blitRect(g, TEX_DRAWER_HANDLE, 64, 192, x, y, w, h, tint);
    }

    public static void drawOpenGrimoire(GuiGraphics g, int x, int y, boolean hovered) {
        int tint = hovered ? 0xFFFFFFFF : 0xFFF5EBDC;
        blitRect(g, TEX_OPEN_GRIMOIRE, 256, 256, x, y + 1, 70.0F, 70.0F, tint);
    }

    public static void drawInkwell(GuiGraphics g, int x, int y, int rgb, boolean focused) {
        if (focused) {
            fillHiRes(g, x - 1.0F, y + 4.5F, x + 17.0F, y + 20.0F, 0x66FDE047);
            drawBeveledBox(g, x - 1.25F, y + 4.25F, 18.5F, 16.0F, 0x22FDE047, 0xCCFDE047, 0x88FFFFFF);
        }
        int liquidCol = 0xFF000000 | liftRgb(rgb, focused ? 35 : 10);
        blitRect(g, TEX_INKWELL_LIQUID, 128, 128, x - 1.0F, y, 18.0F, 20.0F, liquidCol);
        blitRect(g, TEX_INKWELL_GLASS, 128, 128, x - 1.0F, y, 18.0F, 20.0F, 0xFFFFFFFF);
    }

    public static void drawCrypticPedestal(GuiGraphics g, int x, int y, boolean active, boolean hovered) {
        int tint = (active || hovered) ? 0xFFFFFFFF : 0xFFEAD8B1;
        if (active) {
            fillHiRes(g, x - 0.5F, y + 0.5F, x + 20.5F, y + 20.5F, 0x4467E8F9);
        }
        blitRect(g, TEX_CRYPTIC_PEDESTAL, 128, 128, x, y, 20.0F, 20.0F, tint);
    }

    public static void drawWaxSeal(GuiGraphics g, float cx, float cy, float radius,
                                   int baseRgb, boolean active, boolean hovered, int iconKind) {
        float d = radius * 2.15F;
        if (active) {
            blitCentered(g, TEX_WHEEL_SEL_HALO, 128, 128, cx, cy, d + 3.2F, d + 3.2F, 0.0F, 0xDDFFD700);
        }
        int bodyCol = active ? liftRgb(baseRgb, 42) : (hovered ? liftRgb(baseRgb, 18) : scaleRgb(baseRgb, 0.85F));
        int iconCol = active ? 0xFFFFFBEB : liftRgb(baseRgb, 95);
        int idx = Math.clamp(iconKind, 0, TEX_WAX_SEAL_ICON.length - 1);

        blitCentered(g, TEX_WAX_SEAL_BODY, 128, 128, cx + 0.4F, cy + 0.5F, d, d, 0.0F, 0x66000000);
        blitCentered(g, TEX_WAX_SEAL_BODY, 128, 128, cx, cy, d, d, 0.0F, bodyCol);
        blitCentered(g, TEX_WAX_SEAL_ICON[idx], 128, 128, cx, cy, d * 0.88F, d * 0.88F, 0.0F, iconCol);
    }

    public static void drawMateriaVialOrShard(GuiGraphics g, int x, int y, int rgb, boolean shardStyle,
                                              boolean selected, boolean hovered, boolean unattuned) {
        if (selected) {
            drawBeveledBox(g, x - 1.0F, y - 0.5F, 16.0F, 21.0F, 0xCC2B124C, 0xFFFDE047, 0xAAFFFBEB);
        } else if (hovered) {
            drawBeveledBox(g, x - 1.0F, y - 0.5F, 16.0F, 21.0F, 0x661E1B4B, 0xAA67E8F9, 0x55FFFFFF);
        }

        int tint = unattuned ? 0xFFCBD5E1 : (0xFF000000 | liftRgb(rgb, selected ? 28 : 8));
        if (!shardStyle) {
            blitRect(g, TEX_VIAL_LIQUID, 128, 128, x - 1.0F, y, 16.0F, 20.0F, tint);
            blitRect(g, TEX_VIAL_GLASS, 128, 128, x - 1.0F, y, 16.0F, 20.0F, 0xFFFFFFFF);
        } else {
            blitRect(g, TEX_MATERIA_SHARD, 128, 128, x - 0.5F, y + 0.5F, 16.0F, 20.0F, 0x55000000);
            blitRect(g, TEX_MATERIA_SHARD, 128, 128, x - 1.0F, y, 16.0F, 20.0F, tint);
        }
    }

    public static void drawMutationSwitch(GuiGraphics g, Font font, int x, int y, int width,
                                          boolean enabled, boolean canEnable, int crystalRgb, boolean hovered) {
        String label = !canEnable ? "MUTATION: LOCKED" : (enabled ? "MUTATION: ACTIVE" : "MUTATION: OFF");
        int labelCol = !canEnable ? 0xFF94A3B8 : (enabled ? 0xFF4ADE80 : 0xFFE2E8F0);
        drawScaledCenteredString(g, font, label, x + width * 0.5F, y + 1.0F, 0.72F, labelCol, true);

        float trackX = x + 4.0F;
        float trackY = y + 9.0F;
        float trackW = width - 8.0F;
        float trackH = 16.0F;

        int coreCol = !canEnable ? 0xFF475569 : (enabled ? liftRgb(crystalRgb, 55) : 0xFF38BDF8);
        blitRect(g, TEX_MUTATION_CORE, 256, 64, trackX, trackY, trackW, trackH, coreCol);
        blitRect(g, TEX_MUTATION_TRACK, 256, 64, trackX, trackY, trackW, trackH, hovered ? 0xFFFFFFFF : 0xFFEAD8B1);

        float knobCx = enabled ? (trackX + trackW - 18.0F) : (trackX + 18.0F);
        float knobCy = trackY + trackH * 0.5F;
        blitCentered(g, TEX_MUTATION_PLUNGER, 128, 128, knobCx + 0.5F, knobCy + 0.5F, 16.0F, 16.0F, 0.0F, 0x66000000);
        blitCentered(g, TEX_MUTATION_PLUNGER, 128, 128, knobCx, knobCy, 16.0F, 16.0F, 0.0F, hovered ? 0xFFFFFFFF : 0xFFF5E6C4);
    }

    public static void drawQuillInscribing(GuiGraphics g, float cx, float cy, float progress01) {
        float sweep = Mth.sin(progress01 * Mth.PI * 2.0F) * 12.0F;
        float qx = cx + 14.0F + sweep * 0.45F;
        float qy = cy - 8.0F - Math.abs(sweep) * 0.25F;
        float tilt = Mth.sin(progress01 * Mth.PI * 4.0F) * 0.14F;
        blitCentered(g, TEX_QUILL_FEATHER, 128, 128, qx, qy, 38.0F, 38.0F, tilt, 0xFFFFFFFF);

        for (int s = 0; s < 6; s++) {
            float ang = progress01 * 8.0F + s * 1.05F;
            float sx = cx + Mth.cos(ang) * (8.0F + s * 4.2F);
            float sy = cy - Mth.sin(ang) * (6.0F + s * 3.2F);
            blitCentered(g, TEX_RUNE_EMPTY_CORE, 128, 128, sx, sy, 4.5F, 4.5F, ang, 0xEEFEF08A);
        }
    }

    /**
     * High-resolution double-gilded Spell Decision Tooltip card matching State 3's crisp styling.
     */
    public static void drawGildedTooltipBox(GuiGraphics g, Font font, List<Component> lines,
                                            int mouseX, int mouseY, int screenW, int screenH) {
        if (lines == null || lines.isEmpty()) return;

        float textScale = 0.84F;
        float titleScale = 0.92F;

        float maxTextW = 0.0F;
        for (int i = 0; i < lines.size(); i++) {
            float sc = (i == 0) ? titleScale : textScale;
            maxTextW = Math.max(maxTextW, font.width(lines.get(i)) * sc);
        }

        float padX = 7.0F;
        float padY = 5.5F;
        float lineH = 8.8F;
        float boxW = maxTextW + padX * 2.0F;
        float boxH = padY * 2.0F + lines.size() * lineH + 2.5F;

        float bx = mouseX + 12.0F;
        float by = mouseY - 12.0F;
        if (bx + boxW + 4.0F > screenW) {
            bx = Math.max(4.0F, mouseX - boxW - 12.0F);
        }
        if (by + boxH + 4.0F > screenH) {
            by = screenH - boxH - 4.0F;
        }
        if (by < 4.0F) {
            by = 4.0F;
        }

        // Soft drop shadow
        fillHiRes(g, bx + 1.75F, by + 1.75F, bx + boxW + 1.75F, by + boxH + 1.75F, 0xAA000000);
        // Outer gilded brass frame (0.75-unit hairline!)
        fillHiRes(g, bx, by, bx + boxW, by + boxH, 0xFFD4AF37);
        // Deep celestial navy interior
        fillHiRes(g, bx + 0.75F, by + 0.75F, bx + boxW - 0.75F, by + boxH - 0.75F, 0xF6091126);
        // Inner fine gold filigree hairline
        fillHiRes(g, bx + 1.75F, by + 1.75F, bx + boxW - 1.75F, by + 2.25F, 0xBBAA8428);
        fillHiRes(g, bx + 1.75F, by + boxH - 2.25F, bx + boxW - 1.75F, by + boxH - 1.75F, 0xBBAA8428);
        fillHiRes(g, bx + 1.75F, by + 1.75F, bx + 2.25F, by + boxH - 1.75F, 0xBBAA8428);
        fillHiRes(g, bx + boxW - 2.25F, by + 1.75F, bx + boxW - 1.75F, by + boxH - 1.75F, 0xBBAA8428);

        // Crisp 4-corner gilded rivets
        fillHiRes(g, bx - 0.5F, by - 0.5F, bx + 1.5F, by + 1.5F, 0xFFFDE68A);
        fillHiRes(g, bx + boxW - 1.5F, by - 0.5F, bx + boxW + 0.5F, by + 1.5F, 0xFFFDE68A);
        fillHiRes(g, bx - 0.5F, by + boxH - 1.5F, bx + 1.5F, by + boxH + 0.5F, 0xFFFDE68A);
        fillHiRes(g, bx + boxW - 1.5F, by + boxH - 1.5F, bx + boxW + 0.5F, by + boxH + 0.5F, 0xFFFDE68A);

        // Subtle gold divider beneath title
        fillHiRes(g, bx + padX, by + padY + 10.0F, bx + boxW - padX, by + padY + 10.5F, 0x66D4AF37);

        float ty = by + padY;
        for (int i = 0; i < lines.size(); i++) {
            float sc = (i == 0) ? titleScale : textScale;
            drawScaledComponent(g, font, lines.get(i), bx + padX, ty, sc, 0xFFFFFFFF, true);
            ty += (i == 0) ? (lineH + 3.0F) : lineH;
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
