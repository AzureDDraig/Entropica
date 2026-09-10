package ddraig.net.entropica.forcefield.theme;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.AbstractApexPredatorTheme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The Entropic Chimera: Discordant Chromatic Swarm.
 * Tripartite thermodynamic flux cycling Fire, Frost, and Voltaic states with micro-jitter lightning arcs.
 */
public class EntropicChimeraTheme extends AbstractApexPredatorTheme {

    public EntropicChimeraTheme() {
        super(
                ResourceLocation.fromNamespaceAndPath("entropica", "entropic_chimera"),
                3,
                "The Entropic Chimera",
                "Discordant Swarm: Tripartite shifting fire, frost, and voltaic flux",
                0xFFFFB703,
                0.38F
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
        float cycle = Math.abs((time * 0.70F + 0.5F * Mth.sin(3.0F * u) + 0.5F * Mth.cos(3.0F * v)) % 3.0F);

        float r, g, b;
        if (cycle < 1.0F) {
            // Fire Phase
            float t = cycle;
            r = 1.0F;
            g = 0.40F + 0.40F * (1.0F - t);
            b = 0.05F + 0.10F * Mth.sin(t * (float) Math.PI);
        } else if (cycle < 2.0F) {
            // Frost Phase
            float t = cycle - 1.0F;
            r = 0.15F + 0.50F * t;
            g = 0.85F;
            b = 1.0F;
        } else {
            // Voltaic Phase
            float t = cycle - 2.0F;
            r = 0.80F + 0.20F * (1.0F - t);
            g = 0.15F + 0.20F * t;
            b = 0.95F;
        }

        // High frequency micro-jitter lightning discharge
        float jitter = Mth.sin(22.0F * u + 35.0F * time) * Mth.sin(22.0F * v - 28.0F * time);
        if (jitter > 0.80F) {
            r = 1.0F;
            g = 1.0F;
            b = 1.0F;
        }

        float a = 0.28F + 0.52F * grazingFactor + 0.20F * Math.abs(jitter) + 0.35F * rippleIntensity;

        return new ForcefieldShaderHelper.ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
