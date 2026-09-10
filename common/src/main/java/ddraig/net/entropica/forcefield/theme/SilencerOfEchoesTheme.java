package ddraig.net.entropica.forcefield.theme;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.AbstractApexPredatorTheme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The Silencer of Echoes: Null Monolith.
 * Smoky obsidian-amethyst monolithic slab that visually absorbs ambient light with muffled ripple response.
 */
public class SilencerOfEchoesTheme extends AbstractApexPredatorTheme {

    public SilencerOfEchoesTheme() {
        super(
                ResourceLocation.fromNamespaceAndPath("entropica", "silencer_of_echoes"),
                6,
                "Silencer of Echoes",
                "Null Monolith: Smoky obsidian-amethyst that visually absorbs ambient light",
                0xFF2B2D42,
                0.50F
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
        float time = ageTicks * 0.015F; // Ultra-slow monolithic drift
        float smoke = 0.08F * Mth.sin(1.5F * u + time) * Mth.cos(1.5F * v - 0.7F * time);

        float r = 0.16F + 0.12F * grazingFactor + smoke + 0.12F * rippleIntensity;
        float g = 0.14F + 0.10F * grazingFactor + smoke + 0.10F * rippleIntensity;
        float b = 0.26F + 0.24F * grazingFactor + 1.5F * smoke + 0.20F * rippleIntensity;
        float a = 0.50F + 0.45F * grazingFactor + 0.15F * rippleIntensity;

        return new ForcefieldShaderHelper.ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
