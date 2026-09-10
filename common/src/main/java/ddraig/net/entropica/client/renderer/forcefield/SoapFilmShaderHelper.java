package ddraig.net.entropica.client.renderer.forcefield;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Optical thin-film interference and wave displacement engine for soap-film forcefield barriers.
 * Generates procedural shifting pastel rainbow iridescence, Fresnel grazing edge glow,
 * concentric impact ripples, and distinct visual signatures for the 6 Apex Predators.
 */
public class SoapFilmShaderHelper {

    public record ColorResult(float r, float g, float b, float a) {}

    /**
     * Computes the iridescent RGBA color for a vertex on the barrier manifold.
     *
     * @param u             Horizontal normalized coordinate [-1.0, 1.0]
     * @param v             Vertical normalized coordinate [-1.0, 1.0]
     * @param normal        Surface normal at the vertex
     * @param viewDir       Direction from vertex to camera
     * @param ageTicks      Total game ticks with partial tick
     * @param theme         Active Apex Predator theme or standard film
     * @param rippleIntensity Additional brightness from active impact ripples
     */
    public static ColorResult evaluateColor(
            float u,
            float v,
            Vec3 normal,
            Vec3 viewDir,
            float ageTicks,
            ApexPredatorTheme theme,
            float rippleIntensity
    ) {
        float cosTheta = (float) Math.abs(normal.dot(viewDir));
        float grazingFactor = 1.0F - cosTheta; // 0.0 perpendicular, 1.0 grazing rim

        float time = ageTicks * 0.04F;
        float wave = Mth.sin(u * 2.5F + time) * 0.25F + Mth.cos(v * 2.5F - time * 0.8F) * 0.25F;

        return switch (theme) {
            case STAR_EATER -> {
                // Eclipse Singularity: Pitch-black void center with deep crimson tendrils and fiery rim
                float tendril = Mth.sin((u * 4.0F) + (v * 4.0F) + (time * 1.5F));
                float r = 0.55F + (0.45F * grazingFactor) + (0.2F * tendril);
                float g = 0.02F + (0.08F * grazingFactor);
                float b = 0.05F + (0.10F * grazingFactor);
                float a = 0.35F + (grazingFactor * 0.55F) + (rippleIntensity * 0.3F);
                yield new ColorResult(r, g, b, Math.min(1.0F, a));
            }
            case VOID_LEVIATHAN -> {
                // Abyssal Rift: Midnight-cyan with bioluminescent azure tidal waves
                float tide = Mth.sin((u * 3.0F) - (time * 2.0F)) * 0.5F + 0.5F;
                float r = 0.05F + (0.15F * grazingFactor);
                float g = 0.45F + (0.35F * tide);
                float b = 0.75F + (0.25F * grazingFactor);
                float a = 0.30F + (grazingFactor * 0.50F) + (tide * 0.15F);
                yield new ColorResult(r, g, b, Math.min(1.0F, a));
            }
            case ENTROPIC_CHIMERA -> {
                // Discordant Chromatic: Tripartite shifting flux (fire, frost, voltaic)
                float cycle = (time * 0.6F) % 3.0F;
                float r, g, b;
                if (cycle < 1.0F) {
                    // Fire
                    float t = cycle;
                    r = 1.0F; g = 0.4F + (0.4F * (1.0F - t)); b = 0.1F;
                } else if (cycle < 2.0F) {
                    // Frost
                    float t = cycle - 1.0F;
                    r = 0.2F + (0.6F * t); g = 0.85F; b = 1.0F;
                } else {
                    // Voltaic
                    float t = cycle - 2.0F;
                    r = 0.75F + (0.25F * (1.0F - t)); g = 0.2F; b = 0.95F;
                }
                float a = 0.28F + (grazingFactor * 0.50F);
                yield new ColorResult(r, g, b, Math.min(1.0F, a));
            }
            case DEFILER_OF_SYMMETRIES -> {
                // Shattered Symmetry: Non-Euclidean geometric tessellation and amethyst refraction
                float facet = ((int)(u * 6.0F) + (int)(v * 6.0F)) % 2 == 0 ? 0.9F : 0.6F;
                float r = 0.65F * facet;
                float g = 0.25F * facet;
                float b = 0.95F;
                float a = 0.32F + (grazingFactor * 0.55F);
                yield new ColorResult(r, g, b, Math.min(1.0F, a));
            }
            case UNMAKER_OF_FORMS -> {
                // Dissolution Sludge: Acidic chartreuse and sulfurous amber with bubbling cells
                float bubble = Mth.sin((u * 8.0F) + time) * Mth.cos((v * 8.0F) - time);
                float r = 0.45F + (0.35F * bubble);
                float g = 0.85F;
                float b = 0.15F + (0.15F * grazingFactor);
                float a = 0.35F + (grazingFactor * 0.45F);
                yield new ColorResult(r, g, b, Math.min(1.0F, a));
            }
            case SILENCER_OF_ECHOES -> {
                // Null Monolith: Smoky obsidian-amethyst that absorbs ambient light
                float r = 0.18F + (0.12F * grazingFactor);
                float g = 0.15F + (0.10F * grazingFactor);
                float b = 0.28F + (0.22F * grazingFactor);
                float a = 0.45F + (grazingFactor * 0.45F);
                yield new ColorResult(r, g, b, Math.min(1.0F, a));
            }
            default -> {
                // STANDARD: True thin-film spectral rainbow interference
                float phase = (grazingFactor * 2.6F) + wave;
                float r = 0.55F + 0.42F * Mth.cos(phase * (float) Math.PI);
                float g = 0.55F + 0.42F * Mth.cos((phase + 0.66F) * (float) Math.PI);
                float b = 0.55F + 0.42F * Mth.cos((phase + 1.33F) * (float) Math.PI);

                // Add brightness from impact ripples
                r = Math.min(1.0F, r + (rippleIntensity * 0.4F));
                g = Math.min(1.0F, g + (rippleIntensity * 0.4F));
                b = Math.min(1.0F, b + (rippleIntensity * 0.4F));

                // Sheer in the middle, luminous glowing rim at grazing angles
                float a = 0.22F + (grazingFactor * 0.58F) + (rippleIntensity * 0.35F);
                yield new ColorResult(r, g, b, Math.min(1.0F, a));
            }
        };
    }

    /**
     * Calculates surface normal displacement and optical brightness from active ripples.
     */
    public static float evaluateRippleOffset(
            Vec3 vertexWorldPos,
            List<ForcefieldBarrierEntity.RippleImpact> ripples,
            long currentTick,
            float partialTick
    ) {
        if (ripples == null || ripples.isEmpty()) return 0.0F;

        float totalDisplacement = 0.0F;

        for (ForcefieldBarrierEntity.RippleImpact rip : ripples) {
            float dt = (float)(currentTick - rip.tick()) + partialTick;
            if (dt < 0.0F || dt > 35.0F) continue;

            double dist = vertexWorldPos.distanceTo(rip.pos());
            float waveRadius = dt * 0.35F; // Wave travel speed
            float distDiff = (float) Math.abs(dist - waveRadius);

            if (distDiff < 1.2F) {
                float decay = (float) Math.exp(-dt * 0.12F) * rip.intensity();
                float wave = Mth.cos(distDiff * (float) Math.PI) * decay;
                totalDisplacement += wave;
            }
        }

        return totalDisplacement;
    }
}
