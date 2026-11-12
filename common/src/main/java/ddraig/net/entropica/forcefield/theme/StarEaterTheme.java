package ddraig.net.entropica.forcefield.theme;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.AbstractApexPredatorTheme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The Star Eater: Eclipse Singularity.
 * Pitch-black void core with swirling crimson accretion tendrils and fiery solar coronal rim.
 */
public class StarEaterTheme extends AbstractApexPredatorTheme {

    public StarEaterTheme() {
        super(
                ResourceLocation.fromNamespaceAndPath("entropica", "star_eater"),
                1,
                "The Star Eater",
                "Eclipse Maw: Pitch-black void core with swirling crimson tendrils",
                0xFF9E0000,
                0.45F
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
        float rDist = Mth.sqrt(u * u + v * v);
        float theta = (float) Math.atan2(v, u);

        float spiral = Mth.sin(3.0F * theta - 3.5F * rDist + 2.0F * time);
        float tendril = 0.60F * spiral + 0.40F * Mth.sin(5.0F * u + 5.0F * v + 1.5F * time);
        float fVoid = Mth.clamp(0.80F - rDist, 0.0F, 1.0F);
        float corona = grazingFactor * grazingFactor;

        float r = (0.58F + 0.42F * corona + 0.25F * tendril) * (1.0F - 0.75F * fVoid);
        float g = (0.02F + 0.12F * corona + 0.08F * Math.max(0.0F, tendril)) * (1.0F - 0.90F * fVoid)
                + Math.max(0.0F, grazingFactor - 0.60F) * 0.45F;
        float b = (0.04F + 0.10F * corona) * (1.0F - 0.90F * fVoid);
        float a = 0.35F + 0.55F * grazingFactor + 0.20F * fVoid + 0.30F * rippleIntensity;

        return new ForcefieldShaderHelper.ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
