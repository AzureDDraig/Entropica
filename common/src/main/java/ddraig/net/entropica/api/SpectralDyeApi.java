package ddraig.net.entropica.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SpectralDyeApi {
    public record DyeColor(String id, int rgb, String displayName, String hexCode) {
        public int r() { return (rgb >> 16) & 0xFF; }
        public int g() { return (rgb >> 8) & 0xFF; }
        public int b() { return rgb & 0xFF; }
    }

    private static final Map<String, DyeColor> DYE_REGISTRY = new LinkedHashMap<>();

    public static final DyeColor SOULFIRE    = register("soulfire",    0x3B82F6, "Soulfire Blue", "#3B82F6");
    public static final DyeColor AEGIS       = register("aegis",       0x1D4ED8, "Aegis Azure",  "#1D4ED8");
    public static final DyeColor AMBER       = register("amber",       0xF59E0B, "Golden Amber", "#F59E0B");
    public static final DyeColor SHIMMER     = register("shimmer",     0xE2E8F0, "Radiant Silver", "#E2E8F0");
    public static final DyeColor FROST       = register("frost",       0x14B8A6, "Glacial Teal",  "#14B8A6");
    public static final DyeColor AURORAL     = register("auroral",     0xE087EC, "Auroral Violet", "#E087EC");
    public static final DyeColor STARDUST    = register("stardust",    0x818CF8, "Stardust Indigo", "#818CF8");
    public static final DyeColor FULGURITE   = register("fulgurite",   0x06B6D4, "Fulgurite Cyan",  "#06B6D4");
    public static final DyeColor GALE        = register("gale",        0xFDE68A, "Zephyr Sand",     "#FDE68A");
    public static final DyeColor CRYO_STATIC = register("cryo_static", 0x38BDF8, "Cryo-Static Ice", "#38BDF8");
    public static final DyeColor VITREOUS    = register("vitreous",    0x06B6D4, "Vitreous Cyan",   "#06B6D4");
    public static final DyeColor BARROW      = register("barrow",      0x10B981, "Tomb Emerald",    "#10B981");
    public static final DyeColor ABYSSAL     = register("abyssal",     0x0284C7, "Abyssal Navy",    "#0284C7");
    public static final DyeColor SANGUINE    = register("sanguine",    0x8A0303, "Sanguine Crimson", "#8A0303");
        public static final DyeColor SPORE = register("spore", 0x22C55E, "Spore Green", "#22C55E");
    public static final DyeColor BLIGHT = register("blight", 0x34CC48, "Blight Toxic Green", "#34CC48");
    public static final DyeColor ASTRAL = register("astral", 0x6366F1, "Astral Stardust Indigo", "#6366F1");
    public static final DyeColor DAWN = register("dawn", 0xF59E0B, "Dawn Sunburst Gold", "#F59E0B");
    public static final DyeColor STATIC = register("static", 0x38BDF8, "Static Electric Cyan", "#38BDF8");
public static final DyeColor VITAE       = register("vitae",       0xFF6B9D, "Vitae Pink",       "#FF6B9D");
    public static final DyeColor CINDER      = register("cinder",      0xF97316, "Cinder Ember",     "#F97316");
    public static final DyeColor SOOT        = register("soot",        0x312E81, "Soot Ash",         "#312E81");
    public static final DyeColor PYROCYST    = register("pyrocyst",    0xEF4444, "Pyrocyst Crimson", "#EF4444");









    public static DyeColor register(String id, int rgb, String displayName, String hexCode) {
        String key = id.toLowerCase();
        DyeColor color = new DyeColor(key, rgb, displayName, hexCode);
        DYE_REGISTRY.put(key, color);
        return color;
    }

    public static DyeColor getColor(String id) {
        if (id == null) return SHIMMER;
        return DYE_REGISTRY.getOrDefault(id.toLowerCase(), SHIMMER);
    }

    public static int getRgb(String id) {
        DyeColor color = getColor(id);
        return color != null ? color.rgb() : 0xFFFFFF;
    }

    public static Map<String, DyeColor> getAllDyes() {
        return Collections.unmodifiableMap(DYE_REGISTRY);
    }
}
