package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

/**
 * Procedural HUD screen distortion & chromatic edge vignette overlay
 * for gravitational singularities, Zero-G float, and gravitational crush.
 */
public class GravityScreenShimmerRenderer {

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        long time = mc.level.getGameTime();
        float animTick = time + partialTicks;

        // 1. Check Singularity Proximity
        Vec3 singPos = GravityApi.getNearestSingularityPos(mc.level, mc.player.position(), 16.0);
        if (singPos != null) {
            double dist = singPos.distanceTo(mc.player.position());
            float intensity = 1.0f - (float) (dist / 16.0); // 0.0 to 1.0
            if (intensity > 0.0f) {
                renderSingularityVignette(guiGraphics, width, height, animTick, intensity);
                renderAccretionRings(guiGraphics, width, height, animTick, intensity);
            }
        }

        // 2. Check Weightlessness (Zero-G Float)
        MobEffectInstance weightless = mc.player.getEffect(ModEffects.WEIGHTLESSNESS);
        if (weightless != null) {
            float floatPulse = 0.5f + 0.5f * Mth.sin(animTick * 0.1f);
            renderWeightlessGlow(guiGraphics, width, height, floatPulse);
        }

        // 3. Check Gravitational Crush (Heavy compression)
        MobEffectInstance crush = mc.player.getEffect(ModEffects.GRAVITATIONAL_CRUSH);
        if (crush != null) {
            renderCrushVignette(guiGraphics, width, height, animTick);
        }
    }

    private static void renderSingularityVignette(GuiGraphics guiGraphics, int width, int height, float animTick, float intensity) {
        // Deep purple / black event horizon vignette
        int alpha = (int) (Mth.clamp(intensity * 140.0f, 0, 180));
        int colorInner = 0x00100020;
        int colorOuter = (alpha << 24) | 0x150028;

        int borderX = (int) (width * 0.18f * intensity);
        int borderY = (int) (height * 0.18f * intensity);

        // Top border
        guiGraphics.fillGradient(0, 0, width, borderY, colorOuter, colorInner);
        // Bottom border
        guiGraphics.fillGradient(0, height - borderY, width, height, colorInner, colorOuter);
        // Left border
        guiGraphics.fillGradient(0, 0, borderX, height, colorOuter, colorInner);
        // Right border
        guiGraphics.fillGradient(width - borderX, 0, width, height, colorInner, colorOuter);
    }

    private static void renderAccretionRings(GuiGraphics guiGraphics, int width, int height, float animTick, float intensity) {
        // Subtle orbiting accretion particle rings around screen center
        int centerX = width / 2;
        int centerY = height / 2;
        int ringSegments = 16;
        float baseRadius = 60.0f + 30.0f * (1.0f - intensity);

        for (int i = 0; i < ringSegments; i++) {
            float angle = (float) (i * 2 * Math.PI / ringSegments) + animTick * 0.08f;
            float r = baseRadius + 10.0f * Mth.sin(angle * 3.0f + animTick * 0.15f);
            int x = (int) (centerX + r * Math.cos(angle));
            int y = (int) (centerY + r * Math.sin(angle) * 0.6f); // Elliptical inclination

            int pAlpha = (int) (intensity * (40 + 35 * Mth.sin(angle * 2.0f + animTick * 0.2f)));
            int ringColor = (pAlpha << 24) | 0x8A2BE2; // Violet accretion arc
            guiGraphics.fill(x - 1, y - 1, x + 2, y + 2, ringColor);
        }
    }

    private static void renderWeightlessGlow(GuiGraphics guiGraphics, int width, int height, float pulse) {
        int alpha = (int) (20 + 25 * pulse);
        int cyanGlow = (alpha << 24) | 0x00E5FF;
        int clearCyan = 0x0000E5FF;

        int border = 24;
        guiGraphics.fillGradient(0, 0, width, border, cyanGlow, clearCyan);
        guiGraphics.fillGradient(0, height - border, width, height, clearCyan, cyanGlow);
    }

    private static void renderCrushVignette(GuiGraphics guiGraphics, int width, int height, float animTick) {
        float pulse = 0.5f + 0.5f * Mth.sin(animTick * 0.3f);
        int alpha = (int) (50 + 40 * pulse);
        int crushColor = (alpha << 24) | 0x4A0000; // Deep blood crimson
        int clearColor = 0x004A0000;

        int border = 36;
        guiGraphics.fillGradient(0, 0, width, border, crushColor, clearColor);
        guiGraphics.fillGradient(0, height - border, width, height, clearColor, crushColor);
    }
}
