package ddraig.net.entropica.forcefield.theme;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.AbstractApexPredatorTheme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The Unmaker of Forms: Dissolution Sludge.
 * Acidic chartreuse-emerald sludge with decaying cavitation bubbles and caustic amber scald.
 */
public class UnmakerOfFormsTheme extends AbstractApexPredatorTheme {

    public UnmakerOfFormsTheme() {
        super(
                ResourceLocation.fromNamespaceAndPath("entropica", "unmaker_of_forms"),
                5,
                "Unmaker of Forms",
                "Dissolution Sludge: Acidic chartreuse-emerald with decaying bubble cells",
                0xFF55A630,
                0.42F
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

        float cavitation = 0.65F * Mth.sin(8.0F * u + time) * Mth.cos(8.0F * v - 1.1F * time)
                + 0.35F * Mth.sin(14.0F * u - 1.8F * time) * Mth.cos(14.0F * v + 1.5F * time);

        float r = 0.45F + 0.35F * cavitation + 0.15F * grazingFactor + (cavitation > 0.55F ? 0.30F : 0.0F);
        float g = 0.88F + 0.10F * Mth.sin(4.0F * u + time) - (cavitation > 0.55F ? 0.18F : 0.0F);
        float b = 0.14F + 0.18F * grazingFactor + 0.12F * Math.max(0.0F, -cavitation);
        float a = 0.35F + 0.45F * grazingFactor + 0.20F * Math.abs(cavitation) + 0.35F * rippleIntensity;

        return new ForcefieldShaderHelper.ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
