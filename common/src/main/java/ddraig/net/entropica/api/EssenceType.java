package ddraig.net.entropica.api;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The EssenceType enum represents various types of essence, each with associated color cycles
 * and other properties. The enum includes regular essence, foundational base types, elemental and
 * vital fusions, ethereal and paradox fusions, trinity fusions, quaternary fusions, eschaton, singularity,
 * conceptual fragment essences, resonance types, and catalysts.
 *
 * This enum implements the StringRepresentable interface to allow for string-based serialization.
 * The color properties include dynamic color handling and cycling through multiple color stages.
 */
public enum EssenceType implements StringRepresentable {
    // Regular
    REGULAR(new int[][]{{160, 160, 160}}),

    // Base Types
    AIR(new int[][]{{168, 134, 84}}),
    WATER(new int[][]{{34, 92, 124}}),
    NATURE(new int[][]{{26, 89, 35}}),
    EARTH(new int[][]{{119, 50, 20}}),
    FROZEN(new int[][]{{152, 220, 242}}),
    NETHER(new int[][]{{184, 60, 8}}),
    RADIANT(new int[][]{{255, 234, 120}}),
    UMBRAL(new int[][]{{33, 26, 33}}),
    UNDEAD(new int[][]{{77, 100, 83}}),
    VOID(new int[][]{{65, 48, 78}}),
    ARID(new int[][]{{194, 178, 143}}),
    LIGHTNING(new int[][]{{255, 230, 0}}),

    // Elemental Fusions (Tier 1)
    MAGMA(new int[][]{{184, 60, 8}}),
    STORM(new int[][]{{66, 155, 163}}),
    GLACIAL(new int[][]{{152, 220, 242}}),
    OVERGROWTH(new int[][]{{26, 89, 35}}),
    DUST(new int[][]{{194, 178, 143}}),
    SPORE(new int[][]{{26, 89, 35}, {168, 134, 84}}),
    TAIGA(new int[][]{{26, 89, 35}, {152, 220, 242}}),
    OASIS(new int[][]{{26, 89, 35}, {194, 178, 143}}),

    // Vital Fusions (Tier 1 & 2)
    VITAE(new int[][]{{255, 107, 157}}),
    BLOOD(new int[][]{{138, 3, 3}}),
    AURA(new int[][]{{255, 107, 157}, {168, 134, 84}}),
    AMBER(new int[][]{{255, 107, 157}, {119, 50, 20}}),

    // Ethereal & Paradox Fusions (Tier 1 & 2)
    ECLIPSE(new int[][]{{255, 234, 120}, {33, 26, 33}}),
    BLIGHT(new int[][]{{77, 100, 83}, {52, 204, 72}}),
    ASTRAL(new int[][]{{65, 48, 78}, {255, 230, 0}}),
    SOULFIRE(new int[][]{{119, 50, 20}, {77, 100, 83}}),
    VAPOR(new int[][]{{34, 92, 124}, {119, 50, 20}}),
    NULL_R(new int[][]{{255, 234, 120}, {65, 48, 78}}),
    NULL_U(new int[][]{{65, 48, 78}, {33, 26, 33}}),
    DAWN(new int[][]{{255, 234, 120}, {168, 134, 84}}),
    ABYSS(new int[][]{{33, 26, 33}, {34, 92, 124}}),
    AEGIS(new int[][]{{255, 234, 120}, {119, 50, 20}}),
    AURORA(new int[][]{{255, 234, 120}, {152, 220, 242}}),
    MIRAGE(new int[][]{{255, 234, 120}, {194, 178, 143}}),
    WRAITH(new int[][]{{65, 48, 78}, {77, 100, 83}}),
    BARROW(new int[][]{{119, 50, 20}, {77, 100, 83}}),

    // Trinity Fusions (Tier 3)
    PYRE(new int[][]{{119, 50, 20}, {255, 69, 0}}),
    PENUMBRA(new int[][]{{255, 234, 120}, {40, 30, 60}}),
    RIME(new int[][]{{99, 149, 177}, {220, 240, 255}}),
    SPRING(new int[][]{{255, 107, 157}, {100, 200, 255}}),
    STATIC(new int[][]{{255, 230, 0}, {135, 141, 162}}),
    SYLVAN(new int[][]{{26, 89, 35}, {119, 50, 20}, {255, 234, 120}}),
    MIASMA(new int[][]{{26, 89, 35}, {77, 100, 83}, {34, 92, 124}}),
    GENESIS(new int[][]{{255, 107, 157}, {255, 234, 120}, {34, 92, 124}}),
    OBLIVION(new int[][]{{184, 60, 8}, {33, 26, 33}, {77, 100, 83}}),

    // Apex Fusions (Tier 4)
    AETHER(new int[][]{{168, 134, 84}, {119, 50, 20}, {34, 92, 124}, {184, 60, 8}}),
    ENTROPIC(new int[][]{{65, 48, 78}, {33, 26, 33}, {77, 100, 83}, {138, 3, 3}}),
    CELESTIAL(new int[][]{{255, 234, 120}, {255, 107, 157}, {26, 89, 35}, {168, 134, 84}}),

    // Eschaton Fusions (Tier 5)
    EMPYREAN(new int[][]{{255, 234, 120}, {255, 107, 157}, {26, 89, 35}}), // Radiant + Vitae + Nature
    CATACLYSM(new int[][]{{65, 48, 78}, {33, 26, 33}, {184, 60, 8}}),      // Void + Umbral + Nether
    TERMINUS(new int[][]{{168, 134, 84}, {152, 220, 242}, {34, 92, 124}}), // Air + Frozen + Water

    // Singularity Fusions (Tier 6)
    ESCHATON(new int[][]{{0, 0, 0}, {33, 26, 33}, {184, 60, 8}, {65, 48, 78}}), // Apocalyptic Dark Cycle
    APOTHEOSIS(new int[][]{{255, 255, 255}, {255, 234, 120}, {255, 107, 157}, {152, 220, 242}}), // Rebirth Light Cycle
    ENTROPICA(new int[][]{}), // True Equilibrium - Empty array triggers isDynamic() for full prismatic shifting!

    // Sub-Materia (Conceptual Fragments)
    ETHER(new int[][]{{168, 134, 84}}),
    RESONANCE(new int[][]{{160, 160, 160}}),
    GRAVITY(new int[][]{{119, 50, 20}}),
    DENSITY(new int[][]{{100, 100, 100}}),
    FLOW(new int[][]{{34, 92, 124}}),
    COHESION(new int[][]{{200, 200, 200}}),
    IGNIS(new int[][]{{184, 60, 8}}),
    FERVOR(new int[][]{{255, 100, 0}}),
    VACUUM(new int[][]{{50, 50, 50}}),
    GROWTH(new int[][]{{26, 89, 35}}),
    PRIMAL(new int[][]{{50, 120, 50}}),
    SILENCE(new int[][]{{33, 26, 33}}),
    OBLIVIATE(new int[][]{{0,0,0}}),
    SHADOW(new int[][]{{10, 10, 20}}),
    CONCEALMENT(new int[][]{{40, 40, 40}}),
    MEMORY(new int[][]{{77, 100, 83}}),
    ECTO(new int[][]{{200, 255, 200}}),
    STASIS(new int[][]{{152, 220, 242}}),
    FROST(new int[][]{{255, 255, 255}}),
    KINETIC(new int[][]{{255, 230, 0}}),
    VOLT(new int[][]{{200, 200, 0}}),
    HEAT(new int[][]{{184, 60, 8}}),
    DESOLATION(new int[][]{{100, 80, 50}}),
    AXIOM(new int[][]{{150, 0, 255}}),
    ECHO(new int[][]{{255, 100, 255}}),

    // Resonance (Co-Fission) Types
    CHRONOS(new int[][]{{168, 134, 84}, {65, 48, 78}}),
    SINGULARITY(new int[][]{{255, 234, 120}, {65, 48, 78}}),
    PHOTON(new int[][]{{255, 234, 120}, {255, 100, 255}}),

    // Catalysts
    CHIMERA(new int[][]{}),
    PRISMATIC(new int[][]{});

    public static final Codec<EssenceType> CODEC = StringRepresentable.fromEnum(EssenceType::values);
    public static final StreamCodec<FriendlyByteBuf, EssenceType> STREAM_CODEC = StreamCodec.of((buf, type) -> buf.writeEnum(type), buf -> buf.readEnum(EssenceType.class));

    private final int[][] colorCycle;

    EssenceType(int[][] colorCycle) {
        this.colorCycle = colorCycle;
    }

    public int getColorInt() {
        return (this.getR() << 16) | (this.getG() << 8) | this.getB();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase();
    }

    public boolean isDynamic() {
        return this.colorCycle.length == 0;
    }

    public int[] getCurrentRGB(long gameTime) {
        if (isDynamic()) {
            int rgb = ddraig.net.entropica.registry.AestheticGlassRegistry.hsbToRgb((float) (((gameTime * 50) % 4000L) / 4000.0), 0.75f, 1.0f);
            return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
        }
        if (colorCycle.length == 1) return colorCycle[0];

        float time = (gameTime % 40) / 40.0f;
        int stages = colorCycle.length;
        float scaledTime = time * stages;
        int index1 = (int) scaledTime;
        int index2 = (index1 + 1) % stages;
        float blend = scaledTime - index1;

        int[] c1 = colorCycle[index1];
        int[] c2 = colorCycle[index2];

        return new int[]{
                (int) (c1[0] + (c2[0] - c1[0]) * blend),
                (int) (c1[1] + (c2[1] - c1[1]) * blend),
                (int) (c1[2] + (c2[2] - c1[2]) * blend)
        };
    }

    public int[] getCurrentRGB(double gameTime) {
        if (isDynamic()) {
            int rgb = ddraig.net.entropica.registry.AestheticGlassRegistry.hsbToRgb((float) (((gameTime * 50.0) % 4000.0) / 4000.0), 0.75f, 1.0f);
            return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
        }
        if (colorCycle.length == 1) return colorCycle[0];

        double time = (gameTime % 40.0) / 40.0;
        int stages = colorCycle.length;
        double scaledTime = time * stages;
        int index1 = (int) scaledTime;
        int index2 = (index1 + 1) % stages;
        double blend = scaledTime - index1;

        int[] c1 = colorCycle[index1];
        int[] c2 = colorCycle[index2];

        return new int[]{
                (int) (c1[0] + (c2[0] - c1[0]) * blend),
                (int) (c1[1] + (c2[1] - c1[1]) * blend),
                (int) (c1[2] + (c2[2] - c1[2]) * blend)
        };
    }

    public int[][] getColorCycle() { return colorCycle; }

    public int getR() { return colorCycle.length > 0 ? colorCycle[0][0] : 255; }
    public int getG() { return colorCycle.length > 0 ? colorCycle[0][1] : 255; }
    public int getB() { return colorCycle.length > 0 ? colorCycle[0][2] : 255; }

    public String getDisplayName() {
        String name = this.name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String getColorCode() {
        return switch (this) {
            // Pinks / Magentas
            case REGULAR, VITAE, SPRING, AURA, GENESIS -> "§d";
            // Yellows
            case AIR, STATIC, LIGHTNING, ETHER, KINETIC, VOLT, DAWN, EMPYREAN -> "§e";
            // Aquas / Cyans
            case WATER, FROZEN, GLACIAL, STORM, FLOW, STASIS, TAIGA, AURORA, TERMINUS -> "§b";
            // Greens
            case NATURE, OVERGROWTH, GROWTH, MEMORY, ECTO, SPORE, OASIS, SYLVAN -> "§a";
            // Dark Greens
            case UNDEAD, BLIGHT, SOULFIRE, PRIMAL, BARROW, MIASMA -> "§2";
            // Golds / Oranges
            case EARTH, ARID, DUST, GRAVITY, FERVOR, HEAT, AEGIS, MIRAGE, AMBER -> "§6";
            // Reds
            case NETHER, MAGMA, BLOOD, PYRE, IGNIS, CATACLYSM -> "§c";
            // Whites / Light Grays
            case RADIANT, CELESTIAL, COHESION, FROST, APOTHEOSIS -> "§f";
            // Dark Grays / Blacks
            case UMBRAL, VOID, ENTROPIC, ECLIPSE, DENSITY, OBLIVION, VACUUM, SILENCE, SHADOW, CONCEALMENT, DESOLATION, ABYSS, WRAITH, OBLIVIATE, ESCHATON -> "§8";
            // Purples
            case ASTRAL, PENUMBRA, AETHER, AXIOM, ECHO, CHRONOS, SINGULARITY, PHOTON, ENTROPICA -> "§5";
            default -> "§r";
        };
    }

    public String getFormattedName() {
        return getColorCode() + getDisplayName();
    }
    public boolean isFragment() {
        // Captures all conceptual fragments from ETHER through PHOTON
        return this.ordinal() >= ETHER.ordinal() && this.ordinal() <= PHOTON.ordinal();
    }

    /**
     * Calculates the perceived brightness (luminance) of the color.
     * If the color is too dark to be readable against a standard Minecraft tooltip background,
     * it safely boosts the brightness while preserving the original hue/tint.
     */
    public int getTextColorInt() {
        int r = getR();
        int g = getG();
        int b = getB();

        // Standard perceived luminance formula
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);

        // 85 is roughly the brightness of standard Minecraft Dark Gray (§8).
        // Anything lower than this is extremely hard to read on a tooltip.
        if (luminance < 85) {
            int add = (int) (85 - luminance);
            r = Math.min(255, r + add);
            g = Math.min(255, g + add);
            b = Math.min(255, b + add);
        }

        return (r << 16) | (g << 8) | b;
    }
}