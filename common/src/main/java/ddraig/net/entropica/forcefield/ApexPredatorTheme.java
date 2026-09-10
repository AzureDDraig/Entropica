package ddraig.net.entropica.forcefield;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;

/**
 * Pluggable theme definition representing the 6 Apex Predators of Astral Materia
 * and standard thin-film forcefield barriers.
 */
public interface ApexPredatorTheme extends StringRepresentable {

    ResourceLocation getId();
    int getOrdinal();
    String getDisplayName();
    String getDescription();
    int getBaseColorRgba();
    float getBaseAlpha();

    @Override
    default String getSerializedName() {
        return getId().getPath();
    }

    default int ordinal() {
        return getOrdinal();
    }

    default String name() {
        return getId().getPath().toUpperCase(Locale.ROOT);
    }

    /**
     * Evaluates the procedural RGBA color at a specific vertex or surface coordinate.
     *
     * @param u Horizontal normalized coordinate [-1.0, 1.0]
     * @param v Vertical normalized coordinate [-1.0, 1.0]
     * @param normal Surface normal at the vertex
     * @param viewDir Normalized vector from vertex toward camera
     * @param ageTicks Total entity age ticks with partial tick
     * @param rippleIntensity Local impact ripple brightness [0.0, 1.0]
     * @param customColorTint Packed RGB/ARGB custom Materia crystal or dye color (null if none)
     * @param isDormant Whether the barrier is currently unpowered/inactive
     * @return Evaluated ColorResult (r, g, b, a)
     */
    ForcefieldShaderHelper.ColorResult evaluateColor(
            float u,
            float v,
            Vec3 normal,
            Vec3 viewDir,
            float ageTicks,
            float rippleIntensity,
            @Nullable Integer customColorTint,
            boolean isDormant
    );

    // --- Backward Compatibility Delegates ---
    ApexPredatorTheme STANDARD = ApexPredatorThemeRegistry.STANDARD;
    ApexPredatorTheme STAR_EATER = ApexPredatorThemeRegistry.STAR_EATER;
    ApexPredatorTheme VOID_LEVIATHAN = ApexPredatorThemeRegistry.VOID_LEVIATHAN;
    ApexPredatorTheme ENTROPIC_CHIMERA = ApexPredatorThemeRegistry.ENTROPIC_CHIMERA;
    ApexPredatorTheme DEFILER_OF_SYMMETRIES = ApexPredatorThemeRegistry.DEFILER_OF_SYMMETRIES;
    ApexPredatorTheme UNMAKER_OF_FORMS = ApexPredatorThemeRegistry.UNMAKER_OF_FORMS;
    ApexPredatorTheme SILENCER_OF_ECHOES = ApexPredatorThemeRegistry.SILENCER_OF_ECHOES;

    static ApexPredatorTheme fromOrdinal(int ordinal) {
        return ApexPredatorThemeRegistry.fromOrdinal(ordinal);
    }

    static ApexPredatorTheme valueOf(String name) {
        return ApexPredatorThemeRegistry.fromName(name);
    }

    static ApexPredatorTheme[] values() {
        Collection<ApexPredatorTheme> all = ApexPredatorThemeRegistry.getAll();
        return all.toArray(new ApexPredatorTheme[0]);
    }
}
