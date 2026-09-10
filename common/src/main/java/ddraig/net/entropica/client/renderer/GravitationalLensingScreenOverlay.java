package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.entity.projectile.SingularityGrenadeEntity;
import ddraig.net.entropica.gravity.GravityApi;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Screen-space gravitational lensing optical distortion HUD overlay.
 * Mathematically projects 3D singularity coordinates into 2D GUI screen space
 * via inverse camera rotation quaternion and perspective division.
 * Renders an unlit central event horizon void, 3 chromatic aberration caustic rings,
 * and a dynamic swirling radial refraction field.
 */
public class GravitationalLensingScreenOverlay {

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        render(guiGraphics, deltaTracker != null ? deltaTracker.getGameTimeDeltaTicks() : 0.0F);
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;

        // 1. World-to-Screen Singularity Discovery (within 24 blocks)
        Vec3 targetPos = GravityApi.getNearestSingularityPos(mc.level, mc.player.position(), 24.0);
        if (targetPos == null) {
            List<SingularityGrenadeEntity> grenades = mc.level.getEntitiesOfClass(
                SingularityGrenadeEntity.class,
                mc.player.getBoundingBox().inflate(24.0)
            );
            double closestDistSq = Double.MAX_VALUE;
            for (SingularityGrenadeEntity grenade : grenades) {
                if (grenade.isNoGravity() || grenade.getDeltaMovement().lengthSqr() < 1e-4) {
                    double dSq = grenade.distanceToSqr(mc.player);
                    if (dSq < closestDistSq) {
                        closestDistSq = dSq;
                        targetPos = grenade.position();
                    }
                }
            }
        }

        if (targetPos == null) return;

        // 2. Camera Offset Vector
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        Vec3 D = targetPos.subtract(camPos);
        double dist = D.length();
        if (dist < 0.05 || dist > 24.0) return;

        // 3. Inverse Camera Rotation Transformation (camera view space)
        Vector3f viewPos = new Vector3f((float) D.x, (float) D.y, (float) D.z);
        Quaternionf invRot = new Quaternionf(camera.rotation()).conjugate();
        invRot.transform(viewPos);

        float zCam = -viewPos.z;
        if (zCam <= 0.1F) {
            return; // Behind camera
        }

        // 4. Perspective Division to NDC
        int guiWidth = guiGraphics.guiWidth();
        int guiHeight = guiGraphics.guiHeight();
        float tanHalf = (float) Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
        if (tanHalf <= 1e-4F || Float.isNaN(tanHalf)) return;

        float aspect = (float) guiWidth / (float) guiHeight;
        float ndcX = viewPos.x / (zCam * aspect * tanHalf);
        float ndcY = viewPos.y / (zCam * tanHalf);

        // Frustum culling (allow 1.6 margin so edges refract when black hole is slightly off-screen)
        if (Float.isNaN(ndcX) || Float.isNaN(ndcY) || ndcX < -1.6F || ndcX > 1.6F || ndcY < -1.6F || ndcY > 1.6F) {
            return;
        }

        // 5. Map NDC to GUI Screen Coordinates
        float screenX = (guiWidth / 2.0F) * (1.0F + ndcX);
        float screenY = (guiHeight / 2.0F) * (1.0F - ndcY);

        // 6. Proximity & Apparent Screen Radius
        float proximity = 1.0F - Mth.clamp((float) (dist / 24.0), 0.0F, 1.0F);
        float intensity = proximity * proximity; // Smooth non-linear falloff
        float angularRadius = (0.55F * (guiHeight / 2.0F)) / (zCam * tanHalf);
        float baseRadius = Mth.clamp(angularRadius, 14.0F, guiHeight * 0.65F);

        float time = (mc.level.getGameTime() % 72000L) + partialTick;

        // 7. Event Horizon Shadow Disc (Pitch-black central void)
        int shadowAlpha = (int) (220 * intensity);
        if (shadowAlpha > 0) {
            int shadowColor = (shadowAlpha << 24);
            fillCircle(guiGraphics, screenX, screenY, baseRadius * 0.62F, shadowColor);
        }

        // 8. Dynamic Radial Refraction Field (Warped spiraling starlight rays)
        renderRadialRefractionField(guiGraphics, screenX, screenY, baseRadius, intensity, time);

        // 9. Three Chromatic Aberration Concentric Rings:
        // - Outer Cyan / Electric Blue (0x6000E5FF) at 1.04x
        // - Central White Caustic (0xC0FFFFFF) at 1.00x
        // - Inner Magenta / Crimson Redshift (0x60F72585) at 0.96x
        int cyanAlpha = (int) (0x60 * intensity);
        if (cyanAlpha > 0) {
            int cyanColor = (cyanAlpha << 24) | 0x00E5FF;
            drawRing(guiGraphics, screenX, screenY, baseRadius * 1.04F, 2.0F, cyanColor);
        }

        int whiteAlpha = (int) (0xC0 * intensity);
        if (whiteAlpha > 0) {
            int whiteColor = (whiteAlpha << 24) | 0xFFFFFF;
            drawRing(guiGraphics, screenX, screenY, baseRadius * 1.00F, 2.5F, whiteColor);
        }

        int magentaAlpha = (int) (0x60 * intensity);
        if (magentaAlpha > 0) {
            int magentaColor = (magentaAlpha << 24) | 0xF72585;
            drawRing(guiGraphics, screenX, screenY, baseRadius * 0.96F, 2.0F, magentaColor);
        }

        // 10. Secondary Lensing Caustic Ripples
        int rippleAlpha = (int) (0x25 * intensity);
        if (rippleAlpha > 0) {
            drawRing(guiGraphics, screenX, screenY, baseRadius * 1.45F, 1.5F, (rippleAlpha << 24) | 0x00E5FF);
            drawRing(guiGraphics, screenX, screenY, baseRadius * 1.85F, 1.2F, (rippleAlpha << 24) | 0xF72585);
        }
    }

    /**
     * Renders swirling radial refraction rays spiraling out from the photon ring.
     */
    private static void renderRadialRefractionField(GuiGraphics guiGraphics, float cx, float cy, float baseRadius, float intensity, float time) {
        int numRays = 24;
        float rStart = baseRadius * 1.06F;
        float rEnd = baseRadius * 2.20F;
        int steps = 12;

        for (int i = 0; i < numRays; i++) {
            float baseAngle = (i * 2.0F * (float) Math.PI / numRays) + time * 0.03F;

            for (int s = 0; s < steps; s++) {
                float t = (float) s / steps;
                float r = Mth.lerp(t, rStart, rEnd);

                // Gravitational light bending logarithmic swirl
                float swirlAngle = baseAngle + 0.35F * (float) Math.log(r / baseRadius) + 0.04F * Mth.sin(time * 0.2F + i);
                float px = cx + r * Mth.cos(swirlAngle);
                float py = cy + r * Mth.sin(swirlAngle);

                // Check screen bounds
                if (px < -10 || px > guiGraphics.guiWidth() + 10 || py < -10 || py > guiGraphics.guiHeight() + 10) {
                    continue;
                }

                // Shimmering alpha falloff
                float rayPulse = 0.6F + 0.4F * Mth.sin(time * 0.25F + i * 1.5F + s * 0.4F);
                float alphaFactor = (1.0F - t) * intensity * rayPulse;
                int pAlpha = (int) (alphaFactor * 110.0F);

                if (pAlpha > 2) {
                    // Alternate between electric cyan and celestial purple
                    int colorRgb = (i % 2 == 0) ? 0x00E5FF : 0x8A2BE2;
                    int color = (pAlpha << 24) | colorRgb;
                    guiGraphics.fill((int) (px - 1), (int) (py - 1), (int) (px + 2), (int) (py + 2), color);
                }
            }
        }
    }

    /**
     * Renders a filled circle for the event horizon shadow void.
     */
    private static void fillCircle(GuiGraphics guiGraphics, float cx, float cy, float radius, int color) {
        int rInt = (int) Math.ceil(radius);
        float rSq = radius * radius;
        int minGuiY = 0;
        int maxGuiY = guiGraphics.guiHeight();
        int minGuiX = 0;
        int maxGuiX = guiGraphics.guiWidth();

        for (int dy = -rInt; dy <= rInt; dy++) {
            int y = (int) (cy + dy);
            if (y < minGuiY || y >= maxGuiY) continue;

            float dx = (float) Math.sqrt(Math.max(0.0F, rSq - dy * dy));
            int x1 = (int) Math.max(minGuiX, cx - dx);
            int x2 = (int) Math.min(maxGuiX, cx + dx + 1);

            if (x2 > x1) {
                guiGraphics.fill(x1, y, x2, y + 1, color);
            }
        }
    }

    /**
     * Renders an anti-aliased / smooth polygon ring outline around (cx, cy).
     */
    private static void drawRing(GuiGraphics guiGraphics, float cx, float cy, float radius, float thickness, int color) {
        int segments = 48;
        float halfT = Math.max(1.0F, thickness * 0.5F);

        for (int i = 0; i < segments; i++) {
            float a0 = 2.0F * (float) Math.PI * (float) i / segments;
            float a1 = 2.0F * (float) Math.PI * (float) (i + 1) / segments;

            float x0 = cx + radius * Mth.cos(a0);
            float y0 = cy + radius * Mth.sin(a0);
            float x1 = cx + radius * Mth.cos(a1);
            float y1 = cy + radius * Mth.sin(a1);

            drawThickLine(guiGraphics, x0, y0, x1, y1, halfT, color);
        }
    }

    private static void drawThickLine(GuiGraphics guiGraphics, float x0, float y0, float x1, float y1, float halfThickness, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001F) return;

        int steps = (int) Math.ceil(len);
        float stepX = dx / steps;
        float stepY = dy / steps;
        int h = (int) Math.ceil(halfThickness);

        for (int s = 0; s <= steps; s++) {
            int px = (int) Math.round(x0 + s * stepX);
            int py = (int) Math.round(y0 + s * stepY);
            guiGraphics.fill(px - h, py - h, px + h + 1, py + h + 1, color);
        }
    }
}
