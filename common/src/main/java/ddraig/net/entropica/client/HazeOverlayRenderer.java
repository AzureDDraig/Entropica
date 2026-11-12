package ddraig.net.entropica.client;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

public class HazeOverlayRenderer {

    private static final ResourceLocation VIGNETTE = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");

    static {
        ddraig.net.entropica.Entropica.LOGGER.info("HAZE OVERLAY: Starting RenderPipelines field print diagnostic...");
        try {
            Class<?> rpsClass = Class.forName("net.minecraft.client.renderer.RenderPipelines");
            for (java.lang.reflect.Field f : rpsClass.getDeclaredFields()) {
                ddraig.net.entropica.Entropica.LOGGER.info("RENDER_PIPELINES_FIELD: {} -> {}", f.getName(), f.getType().getSimpleName());
            }
        } catch (Exception e) {
            ddraig.net.entropica.Entropica.LOGGER.error("HAZE OVERLAY: Failed to print RenderPipelines fields", e);
        }

        try {
            Class<?> rpClass = Class.forName("com.mojang.blaze3d.pipeline.RenderPipeline");
            for (java.lang.reflect.Field f : rpClass.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals("RenderPipeline")) {
                    ddraig.net.entropica.Entropica.LOGGER.info("RENDER_PIPELINE_FIELD: {} -> {}", f.getName(), f.getType().getSimpleName());
                }
            }
        } catch (Exception e) {
            ddraig.net.entropica.Entropica.LOGGER.error("HAZE OVERLAY: Failed to print RenderPipeline fields", e);
        }
        ddraig.net.entropica.Entropica.LOGGER.info("HAZE OVERLAY: End of RenderPipelines field print diagnostic.");
    }

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Check if the player has the Haze effect
        MobEffectInstance hazeEffect = null;
        for (MobEffectInstance instance : mc.player.getActiveEffects()) {
            if (instance.getEffect().value() == ModEffects.HAZE.get()) {
                hazeEffect = instance;
                break;
            }
        }

        if (hazeEffect == null) return;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        // Get time based on game time + partial ticks for smooth rendering
        float time = (float) (mc.level.getGameTime() % 24000) + partialTicks;

        // Slow, psychedelic color wheel rotation
        float hue = (time * 0.005f) % 1.0f;

        // Temporary basic colored background tint that compiles perfectly
        float centerAlpha = 0.15f + 0.05f * Mth.sin(time * 0.06f);
        float[] rgbCenter = hsvToRgb(hue, 0.8f, 1.0f);
        int centerColorARGB = ((int) (centerAlpha * 255) << 24)
                | ((int) (rgbCenter[0] * 255) << 16)
                | ((int) (rgbCenter[1] * 255) << 8)
                | (int) (rgbCenter[2] * 255);
        guiGraphics.fill(0, 0, width, height, centerColorARGB);
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
        }
        return new float[]{r, g, b};
    }
}
