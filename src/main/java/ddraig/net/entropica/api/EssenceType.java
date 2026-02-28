package ddraig.net.entropica.api;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

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

    // Elemental Fusions
    MAGMA(new int[][]{{184, 60, 8}}),
    STORM(new int[][]{{66, 155, 163}}),
    GLACIAL(new int[][]{{152, 220, 242}}),
    OVERGROWTH(new int[][]{{26, 89, 35}}),
    DUST(new int[][]{{194, 178, 143}}),

    // Vital Fusions
    VITAE(new int[][]{{255, 107, 157}}),
    BLOOD(new int[][]{{138, 3, 3}}),

    // Ethereal & Paradox Fusions
    ECLIPSE(new int[][]{{255, 234, 120}, {33, 26, 33}}),
    BLIGHT(new int[][]{{77, 100, 83}, {52, 204, 72}}),
    ASTRAL(new int[][]{{65, 48, 78}, {255, 230, 0}}),
    SOULFIRE(new int[][]{{119, 50, 20}, {77, 100, 83}}),
    VAPOR(new int[][]{{34, 92, 124}, {119, 50, 20}}),
    NULL(new int[][]{{255, 234, 120}, {65, 48, 78}}),

    // Trinity Fusions
    PYRE(new int[][]{{119, 50, 20}, {255, 69, 0}}),
    PENUMBRA(new int[][]{{255, 234, 120}, {40, 30, 60}}),
    RIME(new int[][]{{99, 149, 177}, {220, 240, 255}}),
    SPRING(new int[][]{{255, 107, 157}, {100, 200, 255}}),
    STATIC(new int[][]{{255, 230, 0}, {135, 141, 162}}),

    // Quaternary Fusions
    AETHER(new int[][]{{168, 134, 84}, {119, 50, 20}, {34, 92, 124}, {184, 60, 8}}),
    ENTROPIC(new int[][]{{65, 48, 78}, {33, 26, 33}, {77, 100, 83}, {138, 3, 3}}),
    CELESTIAL(new int[][]{{255, 234, 120}, {255, 107, 157}, {26, 89, 35}, {168, 134, 84}}),

    // Sub-Mana (Conceptual Fragments)
    ETHER(new int[][]{{168, 134, 84}}),
    RESONANCE(new int[][]{{160, 160, 160}}),
    GRAVITY(new int[][]{{119, 50, 20}}),
    DENSITY(new int[][]{{100, 100, 100}}),
    FLOW(new int[][]{{34, 92, 124}}),
    COHESION(new int[][]{{200, 200, 200}}),
    IGNIS(new int[][]{{184, 60, 8}}),
    FERVOR(new int[][]{{255, 100, 0}}),
    OBLIVION(new int[][]{{0, 0, 0}}),
    VACUUM(new int[][]{{50, 50, 50}}),
    GROWTH(new int[][]{{26, 89, 35}}),
    PRIMAL(new int[][]{{50, 120, 50}}),
    SILENCE(new int[][]{{33, 26, 33}}),
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
        if (isDynamic()) return new int[]{255, 255, 255};
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
            case REGULAR, VITAE, SPRING -> "§d";
            // Yellows
            case AIR, STATIC, LIGHTNING, ETHER, KINETIC, VOLT -> "§e";
            // Aquas / Cyans
            case WATER, FROZEN, GLACIAL, STORM, FLOW, STASIS -> "§b";
            // Greens
            case NATURE, OVERGROWTH, GROWTH, MEMORY, ECTO -> "§a";
            // Dark Greens
            case UNDEAD, BLIGHT, SOULFIRE, PRIMAL -> "§2";
            // Golds / Oranges
            case EARTH, ARID, DUST, GRAVITY, FERVOR, HEAT -> "§6";
            // Reds
            case NETHER, MAGMA, BLOOD, PYRE, IGNIS -> "§c";
            // Whites / Light Grays
            case RADIANT, CELESTIAL, COHESION, FROST -> "§f";
            // Dark Grays / Blacks
            case UMBRAL, VOID, ENTROPIC, ECLIPSE, DENSITY, OBLIVION, VACUUM, SILENCE, SHADOW, CONCEALMENT, DESOLATION -> "§8";
            // Purples
            case ASTRAL, PENUMBRA, AETHER, AXIOM, ECHO, CHRONOS, SINGULARITY, PHOTON -> "§5";
            default -> "§r";
        };
    }

    public String getFormattedName() {
        return getColorCode() + getDisplayName();
    }
}