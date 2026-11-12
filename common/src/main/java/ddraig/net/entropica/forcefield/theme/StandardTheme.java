package ddraig.net.entropica.forcefield.theme;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.AbstractApexPredatorTheme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Standard iridescent thin-film rainbow theme.
 * Displays shifting spectral pastel colors and luminous Fresnel edge rim glow.
 */
public class StandardTheme extends AbstractApexPredatorTheme {

    public StandardTheme() {
        super(
                ResourceLocation.fromNamespaceAndPath("entropica", "standard"),
                0,
                "Iridescent Film",
                "Shifting spectral pastel rainbow barrier",
                0xFFE0F7FA,
                0.25F
        );
    }

    @Override
    protected ForcefieldShaderHelper.ColorResult evaluateThemeColor(
            float u, float v,
            Vec3 normal, Vec3 viewDir,
            float grazingFactor,
            float ageTicks,
            float rippleIntensity
    ) {
        float time = ageTicks * 0.04F;
        float wave = Mth.sin(u * 2.5F + time) * 0.25F + Mth.cos(v * 2.5F - time * 0.8F) * 0.25F;

        float phase = (grazingFactor * 2.6F) + wave;
        float r = 0.55F + 0.42F * Mth.cos(phase * (float) Math.PI);
        float g = 0.55F + 0.42F * Mth.cos((phase + 0.66F) * (float) Math.PI);
        float b = 0.55F + 0.42F * Mth.cos((phase + 1.33F) * (float) Math.PI);

        // Add ripple luminescence
        r = Math.min(1.0F, r + (rippleIntensity * 0.40F));
        g = Math.min(1.0F, g + (rippleIntensity * 0.40F));
        b = Math.min(1.0F, b + (rippleIntensity * 0.40F));

        // Sheer in the center, glowing edge at grazing angles
        float a = 0.22F + (grazingFactor * 0.58F) + (rippleIntensity * 0.35F);

        return new ForcefieldShaderHelper.ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
