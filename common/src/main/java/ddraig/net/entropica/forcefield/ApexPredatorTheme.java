package ddraig.net.entropica.forcefield;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Visual and energetic theme representing the 6 Apex Predators of the Red Void,
 * as well as the standard iridescent thin-film soap barrier.
 */
public enum ApexPredatorTheme implements StringRepresentable {
    STANDARD("Iridescent Film", "Shifting spectral pastel rainbow soap-film", 0xFFE0F7FA, 0.25F),
    STAR_EATER("The Star Eater", "Eclipse Maw: Pitch-black void core with swirling crimson tendrils", 0xFF9E0000, 0.45F),
    VOID_LEVIATHAN("The Void Leviathan", "Abyssal Rift: Deep midnight-cyan with bioluminescent azure tidal waves", 0xFF0077B6, 0.40F),
    ENTROPIC_CHIMERA("The Entropic Chimera", "Discordant Swarm: Tripartite shifting fire, frost, and voltaic flux", 0xFFFFB703, 0.38F),
    DEFILER_OF_SYMMETRIES("Defiler of Symmetries", "Shattered Symmetry: Razor-thin non-Euclidean geometric tessellation", 0xFF9D4EDD, 0.35F),
    UNMAKER_OF_FORMS("Unmaker of Forms", "Dissolution Sludge: Acidic chartreuse-emerald with decaying bubble cells", 0xFF55A630, 0.42F),
    SILENCER_OF_ECHOES("Silencer of Echoes", "Null Monolith: Smoky obsidian-amethyst that visually absorbs ambient light", 0xFF2B2D42, 0.50F);

    private final String displayName;
    private final String description;
    private final int baseColorRgba;
    private final float baseAlpha;

    ApexPredatorTheme(String displayName, String description, int baseColorRgba, float baseAlpha) {
        this.displayName = displayName;
        this.description = description;
        this.baseColorRgba = baseColorRgba;
        this.baseAlpha = baseAlpha;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getBaseColorRgba() {
        return baseColorRgba;
    }

    public float getBaseAlpha() {
        return baseAlpha;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static ApexPredatorTheme fromOrdinal(int ordinal) {
        ApexPredatorTheme[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return STANDARD;
        }
        return values[ordinal];
    }
}
