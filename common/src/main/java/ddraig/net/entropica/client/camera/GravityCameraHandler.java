package ddraig.net.entropica.client.camera;

import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

/**
 * Handles client-side camera roll (smooth 180-degree flip in inverted gravity)
 * and gravitational singularity slant / optical lensing.
 */
public class GravityCameraHandler {

    private static float prevRoll = 0.0f;
    private static float currentRoll = 0.0f;
    private static float rollProgress = 0.0f; // 0.0 (normal) to 1.0 (inverted)

    private static float prevSingularityRoll = 0.0f;
    private static float currentSingularityRoll = 0.0f;

    private static float prevWallRoll = 0.0f;
    private static float currentWallRoll = 0.0f;

    public static void clientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            prevRoll = currentRoll = rollProgress = 0.0f;
            prevSingularityRoll = currentSingularityRoll = 0.0f;
            prevWallRoll = currentWallRoll = 0.0f;
            return;
        }

        // 1. Inverted Gravity Check
        boolean inverted = GravityApi.isInverted(mc.player);
        if (!inverted) {
            MobEffectInstance eff = mc.player.getEffect(ModEffects.WEIGHTLESSNESS);
            if (eff != null && eff.getAmplifier() >= 2) {
                inverted = true;
            }
        }

        // 10-tick smooth cubic transition (0.1 per tick)
        float targetProgress = inverted ? 1.0f : 0.0f;
        if (rollProgress < targetProgress) {
            rollProgress = Math.min(targetProgress, rollProgress + 0.1f);
        } else if (rollProgress > targetProgress) {
            rollProgress = Math.max(targetProgress, rollProgress - 0.1f);
        }

        // Cubic smoothstep: 3*t^2 - 2*t^3 for 180-degree flip
        float smoothT = rollProgress * rollProgress * (3.0f - 2.0f * rollProgress);
        float baseInversionRoll = smoothT * 180.0f;

        // 2. Gravitational Singularity Slant via View-Right Dot Product (Camera Roll only, 15°-25°)
        Vec3 singPos = GravityApi.getNearestSingularityPos(mc.level, mc.player.position(), 20.0);
        float targetSingularityRoll = 0.0f;

        if (singPos != null) {
            Vec3 eyePos = mc.player.getEyePosition();
            Vec3 toSing = singPos.subtract(eyePos);
            double dist = toSing.length();

            if (dist > 0.5 && dist < 20.0) {
                float factor = 1.0f - (float) (dist / 20.0); // 0.0 to 1.0

                // Calculate player view-right vector
                Vec3 look = mc.player.getViewVector(1.0f);
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 viewRight = look.cross(up).normalize();

                if (viewRight.lengthSqr() < 1e-4) {
                    viewRight = new Vec3(1, 0, 0);
                }

                // Dot product between normalized direction to singularity and viewRight
                double dot = toSing.normalize().dot(viewRight);

                // Strictly camera roll (Z-axis rotation, 15°-25°) without modifying pitch or yaw
                targetSingularityRoll = (float) Mth.clamp(dot * 25.0 * factor, -25.0, 25.0);
            }
        }

        prevSingularityRoll = currentSingularityRoll;
        currentSingularityRoll = Mth.lerp(0.2f, currentSingularityRoll, targetSingularityRoll);

        // 3. Wall Running Roll Transition (Graviton Soles on Walls)
        net.minecraft.core.Direction wallDir = ddraig.net.entropica.client.gravity.GravitonSolesClientHandler.getCurrentWallDir();
        float targetWallRoll = 0.0f;
        if (wallDir != null && !inverted) {
            Vec3 look = mc.player.getViewVector(1.0f);
            Vec3 viewRight = look.cross(new Vec3(0, 1, 0)).normalize();
            if (viewRight.lengthSqr() < 1e-4) {
                viewRight = new Vec3(1, 0, 0);
            }
            Vec3 toWall = new Vec3(wallDir.getStepX(), 0, wallDir.getStepZ());
            double rightDot = toWall.dot(viewRight);
            // If wall is on the player's right, roll -90°; if on left, roll +90°
            targetWallRoll = (float) Mth.clamp(rightDot * -90.0, -90.0, 90.0);
        }

        prevWallRoll = currentWallRoll;
        currentWallRoll = Mth.lerp(0.18f, currentWallRoll, targetWallRoll);

        // Combine inversion 180° flip with singularity roll slant and wall roll
        prevRoll = currentRoll;
        currentRoll = baseInversionRoll + currentSingularityRoll + currentWallRoll;
    }

    public static float getRoll(float partialTick) {
        return Mth.lerp(partialTick, prevRoll, currentRoll);
    }

    public static float getYawSlant(float partialTick) {
        return 0.0f; // Preserved for binary parity; pitch/yaw modification disabled
    }

    public static float getPitchSlant(float partialTick) {
        return 0.0f; // Preserved for binary parity; pitch/yaw modification disabled
    }
}
