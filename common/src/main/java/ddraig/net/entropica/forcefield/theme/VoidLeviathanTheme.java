package ddraig.net.entropica.forcefield.theme;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.AbstractApexPredatorTheme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The Void Leviathan: Abyssal Rift.
 * Deep midnight-cyan with bioluminescent azure tidal swells and rhythmic breathing pulse.
 */
public class VoidLeviathanTheme extends AbstractApexPredatorTheme {

    public VoidLeviathanTheme() {
        super(
                ResourceLocation.fromNamespaceAndPath("entropica", "void_leviathan"),
                2,
                "The Void Leviathan",
                "Abyssal Rift: Deep midnight-cyan with bioluminescent azure tidal waves",
                0xFF0077B6,
                0.40F
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
        float tide1 = 0.5F * Mth.sin(3.0F * u - 2.0F * time) + 0.5F;
        float tide2 = 0.5F * Mth.sin(4.0F * v + 1.5F * time + Mth.cos(2.0F * u)) + 0.5F;
        float tide = 0.60F * tide1 + 0.40F * tide2;

        float pulse = 0.85F + 0.15F * Mth.sin(0.5F * time);
        float r = (0.04F + 0.15F * grazingFactor) * pulse;
        float g = (0.40F + 0.45F * tide) * pulse + 0.20F * grazingFactor + 0.35F * rippleIntensity;
        float b = 0.72F + 0.28F * grazingFactor + 0.15F * tide + 0.45F * rippleIntensity;
        float a = 0.30F + 0.50F * grazingFactor + 0.18F * tide + 0.30F * rippleIntensity;

        return new ForcefieldShaderHelper.ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
