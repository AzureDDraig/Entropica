package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * True Astrophysical Black Hole Gravitational Lensing Engine.
 * Implements 3D Schwarzschild Event Horizon, Photon Sphere & Accretion Halo,
 * Warped Accretion Disk (Direct equatorial ring + Upper & Lower Lensed Einstein Arcs),
 * and Relativistic Doppler Asymmetry.
 */
public class GravitationalLensingRenderer {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;

    /**
     * Renders a full 3D astrophysical black hole with relativistic lensing geometry.
     *
     * @param poseStack   Current pose stack
     * @param collector   SubmitNodeCollector for geometry submission
     * @param radius      Schwarzschild radius (Rs)
     * @param ageTicks    Animation time in game ticks
     * @param light       Packed light coordinates
     * @param overlay     Packed overlay texture coordinates
     */
    public static void renderBlackHole(PoseStack poseStack, SubmitNodeCollector collector, float radius, float ageTicks, int light, int overlay) {
        if (radius <= 0.001F) return;

        poseStack.pushPose();

        // 1. Schwarzschild Event Horizon (Rs)
        // Pitch-black sphere rendered into entitySolid to write depth and occlude geometry behind it
        collector.submitCustomGeometry(poseStack, RenderType.entitySolid(WHITE_TEXTURE), (pose, consumer) -> {
            renderEventHorizonSphere(pose.pose(), consumer, radius, overlay);
        });

        // 2. Photon Sphere & Accretion Halo (~1.5 Rs)
        // Brilliant glowing boundary ring at unstable photon orbit caustic
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            renderPhotonSphereHalo(pose.pose(), consumer, radius, ageTicks, overlay);
        });

        // 3. Warped Accretion Disk with Relativistic Doppler Asymmetry
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            renderWarpedAccretionDisk(poseStack, pose.pose(), consumer, radius, ageTicks, overlay);
        });

        poseStack.popPose();
    }

    /**
     * Renders a pitch-black UV sphere at radius Rs with zero light emission that writes to depth buffer.
     */
    private static void renderEventHorizonSphere(Matrix4f matrix, VertexConsumer consumer, float radius, int overlay) {
        int lats = 16;
        int lons = 24;

        for (int i = 0; i < lats; i++) {
            float lat0 = (float) Math.PI * (-0.5F + (float) i / lats);
            float z0 = radius * Mth.sin(lat0);
            float zr0 = radius * Mth.cos(lat0);

            float lat1 = (float) Math.PI * (-0.5F + (float) (i + 1) / lats);
            float z1 = radius * Mth.sin(lat1);
            float zr1 = radius * Mth.cos(lat1);

            for (int j = 0; j < lons; j++) {
                float lon0 = 2.0F * (float) Math.PI * (float) j / lons;
                float x0 = Mth.cos(lon0);
                float y0 = Mth.sin(lon0);

                float lon1 = 2.0F * (float) Math.PI * (float) (j + 1) / lons;
                float x1 = Mth.cos(lon1);
                float y1 = Mth.sin(lon1);

                // Quad vertices on sphere
                float vx0 = zr0 * x0;
                float vy0 = z0;
                float vz0 = zr0 * y0;

                float vx1 = zr1 * x0;
                float vy1 = z1;
                float vz1 = zr1 * y0;

                float vx2 = zr1 * x1;
                float vy2 = z1;
                float vz2 = zr1 * y1;

                float vx3 = zr0 * x1;
                float vy3 = z0;
                float vz3 = zr0 * y1;

                consumer.addVertex(matrix, vx0, vy0, vz0).setColor(0.0F, 0.0F, 0.0F, 1.0F).setUv(0, 0).setOverlay(overlay).setLight(0).setNormal(vx0 / radius, vy0 / radius, vz0 / radius);
                consumer.addVertex(matrix, vx1, vy1, vz1).setColor(0.0F, 0.0F, 0.0F, 1.0F).setUv(0, 1).setOverlay(overlay).setLight(0).setNormal(vx1 / radius, vy1 / radius, vz1 / radius);
                consumer.addVertex(matrix, vx2, vy2, vz2).setColor(0.0F, 0.0F, 0.0F, 1.0F).setUv(1, 1).setOverlay(overlay).setLight(0).setNormal(vx2 / radius, vy2 / radius, vz2 / radius);
                consumer.addVertex(matrix, vx3, vy3, vz3).setColor(0.0F, 0.0F, 0.0F, 1.0F).setUv(1, 0).setOverlay(overlay).setLight(0).setNormal(vx3 / radius, vy3 / radius, vz3 / radius);
            }
        }
    }

    /**
     * Renders the glowing photon sphere caustic shell and accretion halo rings at ~1.5 Rs.
     */
    private static void renderPhotonSphereHalo(Matrix4f matrix, VertexConsumer consumer, float radius, float ageTicks, int overlay) {
        float rIn = 1.44F * radius;
        float rOut = 1.58F * radius;
        int segments = 36;

        float pulse = 0.90F + 0.08F * Mth.sin(ageTicks * 0.25F);
        float rCol = 0.95F;
        float gCol = 0.98F;
        float bCol = 1.00F;
        float aCol = 0.95F * pulse;

        // 1. Equatorial Photon Ring (XZ plane)
        renderFlatRing(matrix, consumer, rIn, rOut, segments, 0, rCol, gCol, bCol, aCol, overlay);

        // 2. Polar Halos (XY & YZ planes) for authentic 3D spherical boundary luminance
        renderVerticalRingXY(matrix, consumer, rIn, rOut, segments, rCol, gCol, bCol, aCol * 0.65F, overlay);
        renderVerticalRingYZ(matrix, consumer, rIn, rOut, segments, rCol, gCol, bCol, aCol * 0.65F, overlay);
    }

    /**
     * Renders direct tilted equatorial accretion disk, upper lensed Einstein arc,
     * lower lensed Einstein arc, and relativistic Doppler asymmetry.
     */
    private static void renderWarpedAccretionDisk(PoseStack poseStack, Matrix4f baseMatrix, VertexConsumer consumer, float radius, float ageTicks, int overlay) {
        float rIn = 2.0F * radius;
        float rOut = 5.2F * radius;
        int radialBands = 4;
        int sectors = 48;
        float rot = ageTicks * 0.06F;

        // A. Direct Equatorial Disk (tilted at 22°)
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(22.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(ageTicks * 1.5F));
        Matrix4f eqMatrix = poseStack.last().pose();

        for (int band = 0; band < radialBands; band++) {
            float bandR0 = rIn + (rOut - rIn) * ((float) band / radialBands);
            float bandR1 = rIn + (rOut - rIn) * ((float) (band + 1) / radialBands);

            float radFalloff0 = (float) Math.pow(1.0F - ((bandR0 - rIn) / (rOut - rIn)), 0.75);
            float radFalloff1 = (float) Math.pow(1.0F - ((bandR1 - rIn) / (rOut - rIn)), 0.75);

            for (int s = 0; s < sectors; s++) {
                float phi0 = 2.0F * (float) Math.PI * (float) s / sectors;
                float phi1 = 2.0F * (float) Math.PI * (float) (s + 1) / sectors;
                float phiMid = (phi0 + phi1) * 0.5F;

                // Relativistic line-of-sight velocity for Doppler beaming
                float vLos = Mth.sin(phiMid + rot);
                float r, g, b, a;

                if (vLos >= 0.0F) {
                    // Approaching limb: boosted brightness and blueshifted electric cyan
                    float boost = 1.0F + 0.65F * vLos;
                    r = Mth.clamp(0.82F * boost, 0.0F, 1.0F);
                    g = Mth.clamp(0.96F * boost, 0.0F, 1.0F);
                    b = 1.0F;
                    a = Mth.clamp(0.95F * radFalloff0, 0.0F, 1.0F);
                } else {
                    // Receding limb: dimmed and redshifted deep celestial violet / crimson
                    float dim = 1.0F - 0.45F * (-vLos);
                    r = 0.76F * dim;
                    g = 0.16F * dim;
                    b = 0.42F * dim;
                    a = Mth.clamp(0.45F * radFalloff0, 0.0F, 1.0F);
                }

                float x00 = bandR0 * Mth.cos(phi0);
                float z00 = bandR0 * Mth.sin(phi0);
                float x10 = bandR1 * Mth.cos(phi0);
                float z10 = bandR1 * Mth.sin(phi0);
                float x11 = bandR1 * Mth.cos(phi1);
                float z11 = bandR1 * Mth.sin(phi1);
                float x01 = bandR0 * Mth.cos(phi1);
                float z01 = bandR0 * Mth.sin(phi1);

                // Double-sided quad submission
                consumer.addVertex(eqMatrix, x00, 0, z00).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);
                consumer.addVertex(eqMatrix, x10, 0, z10).setColor(r, g, b, a * radFalloff1).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);
                consumer.addVertex(eqMatrix, x11, 0, z11).setColor(r, g, b, a * radFalloff1).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);
                consumer.addVertex(eqMatrix, x01, 0, z01).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);

                // Reverse face
                consumer.addVertex(eqMatrix, x01, 0, z01).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
                consumer.addVertex(eqMatrix, x11, 0, z11).setColor(r, g, b, a * radFalloff1).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
                consumer.addVertex(eqMatrix, x10, 0, z10).setColor(r, g, b, a * radFalloff1).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
                consumer.addVertex(eqMatrix, x00, 0, z00).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
            }
        }
        poseStack.popPose();

        // B. Upper Lensed Einstein Arc (Y > 0)
        // Vertical semi-annular ribbon arched over the top pole representing light from rear disk bent into sightline
        renderEinsteinArc(baseMatrix, consumer, radius, ageTicks, overlay, true);

        // C. Lower Lensed Einstein Arc (Y < 0)
        // Compressed semi-annular ribbon curved beneath bottom pole
        renderEinsteinArc(baseMatrix, consumer, radius, ageTicks, overlay, false);
    }

    /**
     * Renders upper or lower lensed Einstein light arcs with relativistic Doppler asymmetry.
     */
    private static void renderEinsteinArc(Matrix4f matrix, VertexConsumer consumer, float radius, float ageTicks, int overlay, boolean upper) {
        float rIn = 1.85F * radius;
        float rOut = upper ? (4.2F * radius) : (3.5F * radius);
        float yScale = upper ? 0.92F : -0.48F; // Lower arc is vertically compressed by secondary lensing
        float zOffset = -0.06F * radius; // Behind event horizon center
        int arcSectors = 36;
        int radialStrips = 3;

        for (int strip = 0; strip < radialStrips; strip++) {
            float stripR0 = rIn + (rOut - rIn) * ((float) strip / radialStrips);
            float stripR1 = rIn + (rOut - rIn) * ((float) (strip + 1) / radialStrips);

            float radFalloff0 = (float) Math.pow(1.0F - ((stripR0 - rIn) / (rOut - rIn)), 0.80);
            float radFalloff1 = (float) Math.pow(1.0F - ((stripR1 - rIn) / (rOut - rIn)), 0.80);

            for (int s = 0; s < arcSectors; s++) {
                // Azimuth from 0 to PI for semi-annular arch
                float phi0 = (float) Math.PI * (float) s / arcSectors;
                float phi1 = (float) Math.PI * (float) (s + 1) / arcSectors;
                float phiMid = (phi0 + phi1) * 0.5F;

                // Left limb (phi near 0, x > 0) is orbiting toward viewer: blueshifted & boosted
                // Right limb (phi near PI, x < 0) is orbiting away: redshifted & dimmed
                float approachFactor = Mth.cos(phiMid); // 1.0 (left) down to -1.0 (right)

                float r, g, b, a;
                if (approachFactor >= 0.0F) {
                    float boost = 1.0F + 0.60F * approachFactor;
                    r = Mth.clamp(0.85F * boost, 0.0F, 1.0F);
                    g = Mth.clamp(0.96F * boost, 0.0F, 1.0F);
                    b = 1.0F;
                    a = Mth.clamp(0.95F * radFalloff0, 0.0F, 1.0F);
                } else {
                    float dim = 1.0F - 0.40F * (-approachFactor);
                    r = 0.75F * dim;
                    g = 0.16F * dim;
                    b = 0.42F * dim;
                    a = Mth.clamp(0.45F * radFalloff0, 0.0F, 1.0F);
                }

                float x00 = stripR0 * Mth.cos(phi0);
                float y00 = stripR0 * Mth.sin(phi0) * yScale;
                float x10 = stripR1 * Mth.cos(phi0);
                float y10 = stripR1 * Mth.sin(phi0) * yScale;
                float x11 = stripR1 * Mth.cos(phi1);
                float y11 = stripR1 * Mth.sin(phi1) * yScale;
                float x01 = stripR0 * Mth.cos(phi1);
                float y01 = stripR0 * Mth.sin(phi1) * yScale;

                consumer.addVertex(matrix, x00, y00, zOffset).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);
                consumer.addVertex(matrix, x10, y10, zOffset).setColor(r, g, b, a * radFalloff1).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);
                consumer.addVertex(matrix, x11, y11, zOffset).setColor(r, g, b, a * radFalloff1).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);
                consumer.addVertex(matrix, x01, y01, zOffset).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);

                // Reverse face
                consumer.addVertex(matrix, x01, y01, zOffset).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
                consumer.addVertex(matrix, x11, y11, zOffset).setColor(r, g, b, a * radFalloff1).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
                consumer.addVertex(matrix, x10, y10, zOffset).setColor(r, g, b, a * radFalloff1).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
                consumer.addVertex(matrix, x00, y00, zOffset).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
            }
        }
    }

    private static void renderFlatRing(Matrix4f matrix, VertexConsumer consumer, float rIn, float rOut, int segments, float y, float r, float g, float b, float a, int overlay) {
        for (int i = 0; i < segments; i++) {
            float a0 = 2.0F * (float) Math.PI * (float) i / segments;
            float a1 = 2.0F * (float) Math.PI * (float) (i + 1) / segments;

            float x00 = rIn * Mth.cos(a0);
            float z00 = rIn * Mth.sin(a0);
            float x10 = rOut * Mth.cos(a0);
            float z10 = rOut * Mth.sin(a0);
            float x11 = rOut * Mth.cos(a1);
            float z11 = rOut * Mth.sin(a1);
            float x01 = rIn * Mth.cos(a1);
            float z01 = rIn * Mth.sin(a1);

            consumer.addVertex(matrix, x00, y, z00).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x10, y, z10).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x11, y, z11).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x01, y, z01).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 1, 0);

            // Back face
            consumer.addVertex(matrix, x01, y, z01).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
            consumer.addVertex(matrix, x11, y, z11).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
            consumer.addVertex(matrix, x10, y, z10).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
            consumer.addVertex(matrix, x00, y, z00).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, -1, 0);
        }
    }

    private static void renderVerticalRingXY(Matrix4f matrix, VertexConsumer consumer, float rIn, float rOut, int segments, float r, float g, float b, float a, int overlay) {
        for (int i = 0; i < segments; i++) {
            float a0 = 2.0F * (float) Math.PI * (float) i / segments;
            float a1 = 2.0F * (float) Math.PI * (float) (i + 1) / segments;

            float x00 = rIn * Mth.cos(a0);
            float y00 = rIn * Mth.sin(a0);
            float x10 = rOut * Mth.cos(a0);
            float y10 = rOut * Mth.sin(a0);
            float x11 = rOut * Mth.cos(a1);
            float y11 = rOut * Mth.sin(a1);
            float x01 = rIn * Mth.cos(a1);
            float y01 = rIn * Mth.sin(a1);

            consumer.addVertex(matrix, x00, y00, 0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x10, y10, 0).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x11, y11, 0).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x01, y01, 0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, 1);

            // Back face
            consumer.addVertex(matrix, x01, y01, 0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
            consumer.addVertex(matrix, x11, y11, 0).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
            consumer.addVertex(matrix, x10, y10, 0).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
            consumer.addVertex(matrix, x00, y00, 0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(0, 0, -1);
        }
    }

    private static void renderVerticalRingYZ(Matrix4f matrix, VertexConsumer consumer, float rIn, float rOut, int segments, float r, float g, float b, float a, int overlay) {
        for (int i = 0; i < segments; i++) {
            float a0 = 2.0F * (float) Math.PI * (float) i / segments;
            float a1 = 2.0F * (float) Math.PI * (float) (i + 1) / segments;

            float y00 = rIn * Mth.cos(a0);
            float z00 = rIn * Mth.sin(a0);
            float y10 = rOut * Mth.cos(a0);
            float z10 = rOut * Mth.sin(a0);
            float y11 = rOut * Mth.cos(a1);
            float z11 = rOut * Mth.sin(a1);
            float y01 = rIn * Mth.cos(a1);
            float z01 = rIn * Mth.sin(a1);

            consumer.addVertex(matrix, 0, y00, z00).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(1, 0, 0);
            consumer.addVertex(matrix, 0, y10, z10).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(1, 0, 0);
            consumer.addVertex(matrix, 0, y11, z11).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(1, 0, 0);
            consumer.addVertex(matrix, 0, y01, z01).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(1, 0, 0);

            // Back face
            consumer.addVertex(matrix, 0, y01, z01).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(-1, 0, 0);
            consumer.addVertex(matrix, 0, y11, z11).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(-1, 0, 0);
            consumer.addVertex(matrix, 0, y10, z10).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(-1, 0, 0);
            consumer.addVertex(matrix, 0, y00, z00).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_LIGHT).setNormal(-1, 0, 0);
        }
    }
}
