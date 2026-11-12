package ddraig.net.entropica.client.renderer.forcefield;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Optical thin-film interference and wave displacement engine for forcefield barriers.
 * Generates procedural shifting pastel rainbow iridescence, Fresnel grazing edge glow,
 * concentric impact ripples, Materia color tint blending, dormant states,
 * and distinct visual signatures for the 6 Apex Predators of Astral Materia.
 */
public class ForcefieldShaderHelper {

    public record ColorResult(float r, float g, float b, float a) {}

    /**
     * Overload for evaluating color with default active state and no custom tint.
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
        return evaluateColor(u, v, normal, viewDir, ageTicks, theme, rippleIntensity, null, false);
    }

    /**
     * Computes the iridescent RGBA color for a vertex on the barrier manifold,
     * delegating to the pluggable ApexPredatorTheme architecture.
     *
     * @param u                Horizontal normalized coordinate [-1.0, 1.0]
     * @param v                Vertical normalized coordinate [-1.0, 1.0]
     * @param normal           Surface normal at the vertex
     * @param viewDir          Direction from vertex to camera
     * @param ageTicks         Total game ticks with partial tick
     * @param theme            Active Apex Predator theme or standard film
     * @param rippleIntensity  Additional brightness from active impact ripples
     * @param customColorTint  Custom packed RGB/ARGB Materia or dye color tint (null if none)
     * @param isDormant        Whether the barrier is unpowered/dormant
     */
    public static ColorResult evaluateColor(
            float u,
            float v,
            Vec3 normal,
            Vec3 viewDir,
            float ageTicks,
            ApexPredatorTheme theme,
            float rippleIntensity,
            @Nullable Integer customColorTint,
            boolean isDormant
    ) {
        if (theme == null) {
            theme = ApexPredatorTheme.STANDARD;
        }
        ColorResult res = theme.evaluateColor(u, v, normal, viewDir, ageTicks, rippleIntensity, customColorTint, isDormant);
        return new ColorResult(
                Mth.clamp(res.r(), 0.0F, 1.0F),
                Mth.clamp(res.g(), 0.0F, 1.0F),
                Mth.clamp(res.b(), 0.0F, 1.0F),
                Mth.clamp(res.a(), 0.0F, 1.0F)
        );
    }

    /**
     * Dual-stage hybrid Materia color tint blending model:
     * 1. Luminance-preserving chromatic overlay (65% custom dye/crystal tint, 35% base theme).
     * 2. Screen-blend sheen highlight preserving iridescent rim glow and ripple luminescence.
     */
    public static ColorResult blendMateriaTint(
            ColorResult base,
            int tintRgb,
            float grazingFactor,
            float rippleIntensity
    ) {
        float tintR = ((tintRgb >> 16) & 0xFF) / 255.0F;
        float tintG = ((tintRgb >> 8) & 0xFF) / 255.0F;
        float tintB = (tintRgb & 0xFF) / 255.0F;

        // 1. Theme luminance calculation
        float baseLum = 0.299F * base.r() + 0.587F * base.g() + 0.114F * base.b();

        // 2. Chromatic overlay (65% tint dominant, modulated by base luminance)
        float wTint = 0.65F;
        float mod = 0.55F + 0.45F * baseLum;
        float chromaR = (1.0F - wTint) * base.r() + wTint * (tintR * mod);
        float chromaG = (1.0F - wTint) * base.g() + wTint * (tintG * mod);
        float chromaB = (1.0F - wTint) * base.b() + wTint * (tintB * mod);

        // 3. Screen-blend sheen highlight: preserve iridescent rim and white-hot ripples
        float sheen = grazingFactor * 0.45F + rippleIntensity * 0.50F;
        float finalR = 1.0F - (1.0F - chromaR) * (1.0F - sheen * base.r());
        float finalG = 1.0F - (1.0F - chromaG) * (1.0F - sheen * base.g());
        float finalB = 1.0F - (1.0F - chromaB) * (1.0F - sheen * base.b());

        // 4. Alpha tuning
        float tintLum = 0.299F * tintR + 0.587F * tintG + 0.114F * tintB;
        float finalA = Mth.clamp(base.a() * (0.85F + 0.25F * tintLum), 0.18F, 1.0F);

        return new ColorResult(
                Mth.clamp(finalR, 0.0F, 1.0F),
                Mth.clamp(finalG, 0.0F, 1.0F),
                Mth.clamp(finalB, 0.0F, 1.0F),
                finalA
        );
    }

    /**
     * Ethereal breathing starlight shimmer for dormant (unpowered) barriers.
     */
    public static ColorResult evaluateDormantColor(
            float grazingFactor,
            float ageTicks,
            @Nullable Integer customColorTint
    ) {
        float clampedGrazing = Mth.clamp(grazingFactor, 0.0F, 1.0F);

        // 15-second breathing cycle (~300 ticks)
        float breathe = 0.5F + 0.5F * Mth.sin(ageTicks * 0.02F);

        // Ultra-low alpha: 3.5% center, 10% grazing edge
        float a = (0.035F + 0.065F * clampedGrazing) * (0.75F + 0.25F * breathe);

        // Pale starlight silver-lavender
        float r = 0.70F + 0.15F * clampedGrazing;
        float g = 0.82F + 0.12F * clampedGrazing;
        float b = 0.95F + 0.05F * clampedGrazing;

        if (customColorTint != null && customColorTint != 0 && (customColorTint & 0x00FFFFFF) != 0) {
            float tintR = ((customColorTint >> 16) & 0xFF) / 255.0F;
            float tintG = ((customColorTint >> 8) & 0xFF) / 255.0F;
            float tintB = (customColorTint & 0xFF) / 255.0F;
            r = 0.40F * r + 0.60F * tintR;
            g = 0.40F * g + 0.60F * tintG;
            b = 0.40F * b + 0.60F * tintB;
        }

        return new ColorResult(
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
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
