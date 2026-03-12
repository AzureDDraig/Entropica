package ddraig.net.entropica.client.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Entropica.MODID, value = Dist.CLIENT)
public class VisToxicityClientEffects {

    // Trackers for the glitch state
    private static int glitchFramesLeft = 0;
    private static int maxGlitchFrames = 1;
    private static int currentGlitchColor = 0xFFFFFF;

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;

        MobEffectInstance effect = player.getEffect(ModEffects.VIS_TOXICITY);
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

        MobEffectInstance effect = player.getEffect(ModEffects.VIS_TOXICITY);
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

                    // Scan the area for the gas causing the issue
                    currentGlitchColor = findNearestFumeColor(player);
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

        MobEffectInstance effect = player.getEffect(ModEffects.VIS_TOXICITY);
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

    /**
     * Scans a 4-block radius around the player to find the closest machine/pipe with fumes.
     */
    private static int findNearestFumeColor(LocalPlayer player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Default to a sickly pale green if we somehow can't find a pipe nearby
        int closestColor = 0x88FF88;
        double closestDist = Double.MAX_VALUE;

        // Scan a 9x9x9 area around the player
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);

                    if (be instanceof IFumeHandler handler) {
                        VisFumeStack stack = handler.getFumeInTank();
                        if (!stack.isEmpty()) {
                            double dist = pos.distSqr(playerPos);
                            if (dist < closestDist) {
                                closestDist = dist;
                                closestColor = stack.getType().getColorInt();
                            }
                        }
                    }
                }
            }
        }
        return closestColor;
    }
}