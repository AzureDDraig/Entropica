package ddraig.net.entropica.forcefield;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation of ApexPredatorTheme providing automatic handling of dormant states
 * and Materia color tint blending.
 */
public abstract class AbstractApexPredatorTheme implements ApexPredatorTheme {

    private final ResourceLocation id;
    private final int ordinal;
    private final String displayName;
    private final String description;
    private final int baseColorRgba;
    private final float baseAlpha;

    protected AbstractApexPredatorTheme(
            ResourceLocation id,
            int ordinal,
            String displayName,
            String description,
            int baseColorRgba,
            float baseAlpha
    ) {
        this.id = id;
        this.ordinal = ordinal;
        this.displayName = displayName;
        this.description = description;
        this.baseColorRgba = baseColorRgba;
        this.baseAlpha = baseAlpha;
    }

    @Override public ResourceLocation getId() { return id; }
    @Override public int getOrdinal() { return ordinal; }
    @Override public String getDisplayName() { return displayName; }
    @Override public String getDescription() { return description; }
    @Override public int getBaseColorRgba() { return baseColorRgba; }
    @Override public float getBaseAlpha() { return baseAlpha; }

    @Override
    public ForcefieldShaderHelper.ColorResult evaluateColor(
            float u, float v,
            Vec3 normal, Vec3 viewDir,
            float ageTicks,
            float rippleIntensity,
            @Nullable Integer customColorTint,
            boolean isDormant
    ) {
        float cosTheta = Mth.clamp((float) Math.abs(normal.dot(viewDir)), 0.0F, 1.0F);
        float grazingFactor = 1.0F - cosTheta;

        // Dormant unpowered state: render faint breathing starlight shimmer
        if (isDormant) {
            return ForcefieldShaderHelper.evaluateDormantColor(grazingFactor, ageTicks, customColorTint);
        }

        // Evaluate signature theme optical calculation
        ForcefieldShaderHelper.ColorResult base = evaluateThemeColor(
                u, v, normal, viewDir, grazingFactor, ageTicks, rippleIntensity
        );

        // Blend custom Materia crystal / dye tint if active
        if (customColorTint != null && customColorTint != 0 && (customColorTint & 0x00FFFFFF) != 0) {
            return ForcefieldShaderHelper.blendMateriaTint(base, customColorTint, grazingFactor, rippleIntensity);
        }

        return base;
    }

    /**
     * Concrete optical formula implemented by each theme.
     */
    protected abstract ForcefieldShaderHelper.ColorResult evaluateThemeColor(
            float u, float v,
            Vec3 normal, Vec3 viewDir,
            float grazingFactor,
            float ageTicks,
            float rippleIntensity
    );
}
