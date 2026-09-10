package ddraig.net.entropica.forcefield.theme;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.AbstractApexPredatorTheme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The Defiler of Symmetries: Shattered Symmetry.
 * Non-Euclidean diamond facet lattice with glowing amethyst prismatic seams.
 */
public class DefilerOfSymmetriesTheme extends AbstractApexPredatorTheme {

    public DefilerOfSymmetriesTheme() {
        super(
                ResourceLocation.fromNamespaceAndPath("entropica", "defiler_of_symmetries"),
                4,
                "Defiler of Symmetries",
                "Shattered Symmetry: Razor-thin non-Euclidean geometric tessellation",
                0xFF9D4EDD,
                0.35F
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

        // 30-degree rotation
        float uRot = 0.866F * u - 0.500F * v;
        float vRot = 0.500F * u + 0.866F * v;

        float cx = 6.0F * uRot + 0.40F * time;
        float cy = 6.0F * vRot - 0.25F * time;

        int ix = Mth.floor(cx);
        int iy = Mth.floor(cy);
        float fx = Math.abs(cx - ix - 0.5F);
        float fy = Math.abs(cy - iy - 0.5F);

        float facet = ((ix + iy) % 2 == 0) ? 0.92F : 0.65F;
        boolean isSeam = fx > 0.42F || fy > 0.42F;

        float r = 0.65F * facet + 0.25F * grazingFactor + (isSeam ? 0.35F : 0.0F);
        float g = 0.22F * facet + (isSeam ? 0.45F : 0.0F);
        float b = 0.95F * facet + 0.15F * grazingFactor + (isSeam ? 0.05F : 0.0F);
        float a = 0.32F + 0.55F * grazingFactor + (isSeam ? 0.35F : 0.0F) + 0.30F * rippleIntensity;

        return new ForcefieldShaderHelper.ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
