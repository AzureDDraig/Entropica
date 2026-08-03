package ddraig.net.entropica.neoforge.client.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Entropica.MODID, value = Dist.CLIENT)
public class MateriaToxicityClientEffects {

    // Trackers for the glitch state
    private static int glitchFramesLeft = 0;
    private static int maxGlitchFrames = 3;
    private static int currentGlitchColor = 0xFFFFFF;

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;

        MobEffectInstance effect = player.getEffect(ModEffects.MATERIA_TOXICITY);
        if (effect != null) {
            float intensity = 1.0f + effect.getAmplifier();
            long time = player.level().getGameTime();
            float tick = time + (float) event.getPartialTick();

            // Slow sine wave that pulses the FOV in and out
            float fovOffset = (float) (Math.sin(tick / 20.0) * 10.0 * intensity);
            event.setFOV(event.getFOV() + fovOffset);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.isPaused()) return;

        MobEffectInstance effect = player.getEffect(ModEffects.MATERIA_TOXICITY);
        if (effect != null) {
            float intensity = 1.0f + effect.getAmplifier();
            long time = player.level().getGameTime();
            float tick = time + (float) event.getPartialTick();

            // 1. Sea-sick Camera Roll
            float roll = (float) Math.sin(tick / 40.0) * 5.0f * intensity;
            event.setRoll(event.getRoll() + roll);

            // 2. The Glitch Effect Manager
            if (glitchFramesLeft <= 0) {
                // Tripled glitch frequency! (~1.5% chance per frame at level 1)
                if (player.level().random.nextFloat() < 0.015f * intensity) {
                    // Randomize how long the glitch lasts (5 to 14 frames)
                    glitchFramesLeft = player.level().random.nextInt(10) + 5;
                    maxGlitchFrames = glitchFramesLeft;

                    // DIRECT READ: Pull the synced toxicity source directly from the player!
                    String typeName = ModAttachments.getToxicitySource(player);
                    try {
                        if (!typeName.equals("UNKNOWN")) {
                            EssenceType type = EssenceType.valueOf(typeName);
                            currentGlitchColor = type.getColorInt();
                        } else {
                            currentGlitchColor = 0x88FF88; // Default pale green fallback
                        }
                    } catch (IllegalArgumentException e) {
                        currentGlitchColor = 0x88FF88;
                    }
                }
            }

            // Apply the aggressive camera snapping if we are actively glitching
            if (glitchFramesLeft > 0) {
                glitchFramesLeft--;

                // Wilder, more aggressive camera snapping
                float glitchYaw = (player.level().random.nextFloat() - 0.5f) * 8.0f * intensity;
                float glitchPitch = (player.level().random.nextFloat() - 0.5f) * 8.0f * intensity;

                event.setYaw(event.getYaw() + glitchYaw);
                event.setPitch(event.getPitch() + glitchPitch);
            }

        } else {
            // Instantly clear the glitch if the effect wears off or is cured
            glitchFramesLeft = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || glitchFramesLeft <= 0) return;

        MobEffectInstance effect = player.getEffect(ModEffects.MATERIA_TOXICITY);
        if (effect != null) {
            // Calculate alpha so the color fades out smoothly as the glitch finishes
            float progress = (float) glitchFramesLeft / maxGlitchFrames;
            int alpha = (int) (progress * 130); // Max ~50% opacity

            if (alpha > 0) {
                int screenWidth = event.getGuiGraphics().guiWidth();
                int screenHeight = event.getGuiGraphics().guiHeight();

                // Combine the calculated alpha with the dynamic block color
                int argb = (alpha << 24) | (currentGlitchColor & 0xFFFFFF);

                // Draw a full-screen colored rectangle over the UI/World
                event.getGuiGraphics().fill(0, 0, screenWidth, screenHeight, argb);
            }
        }
    }
}