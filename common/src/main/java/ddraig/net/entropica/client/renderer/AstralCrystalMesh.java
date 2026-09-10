package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * AstralCrystalMesh — Procedural non-blocky, faceted crystal mesh generator.
 *
 * Emits irregular polygonal faceted crystal geometry (similar to Materia Blessing,
 * but smaller, sharper, and more jagged). Generates:
 * - Stage 1: Small Bud (Single sharp needle spire + basal spur)
 * - Stage 2: Medium Bud (Primary jagged spire + 1 leaning satellite spire)
 * - Stage 3: Large Bud (Dominant jagged spire + 2 satellite spires + basal shard)
 * - Stage 4: Cluster (Central obelisk + 4 tilted satellite spires + 3 basal shards)
 * - Single Gem: Double-terminated floating faceted gemstone + 2 satellite micro-shards
 */
public class AstralCrystalMesh {

    public static final ResourceLocation CRYSTAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/astral_crystal_crystal.png");

    private static final int SIDES = 6;

    // Standard Jagged Spire Profile (Root at y=0, Apex at y=1.0)
    private static final float[] SPIRE_RING_Y = {
            0.00f, 0.08f, 0.22f, 0.40f, 0.60f, 0.76f, 0.90f, 1.00f
    };
    private static final float[] SPIRE_RING_RADIUS = {
            0.00f, 0.14f, 0.18f, 0.17f, 0.14f, 0.10f, 0.05f, 0.00f
    };
    private static final float[] SPIRE_RING_ROT = {
            0.0f, -8.0f, 14.0f, -12.0f, 10.0f, -6.0f, 15.0f, 0.0f
    };

    // Double-Terminated Gemstone Profile (Bottom needle at y=-0.5, Midsection at y=0, Top needle at y=+0.5)
    private static final float[] GEM_RING_Y = {
            -0.50f, -0.38f, -0.20f, 0.00f, 0.20f, 0.38f, 0.50f
    };
    private static final float[] GEM_RING_RADIUS = {
            0.00f, 0.08f, 0.16f, 0.21f, 0.16f, 0.08f, 0.00f
    };
    private static final float[] GEM_RING_ROT = {
            0.0f, -12.0f, 15.0f, -8.0f, 14.0f, -10.0f, 0.0f
    };

    public static RenderType getRenderType() {
        return RenderType.entityCutout(CRYSTAL_TEXTURE);
    }

    // ─────────────────── Cluster / Bud Rendering ───────────────────

    public static void renderCluster(Matrix4f matrix, VertexConsumer consumer, int stage,
                                     int light, int overlay, double time) {
        switch (stage) {
            case 1 -> renderSmallBud(matrix, consumer, light, overlay, time);
            case 2 -> renderMediumBud(matrix, consumer, light, overlay, time);
            case 3 -> renderLargeBud(matrix, consumer, light, overlay, time);
            default -> renderFullCluster(matrix, consumer, light, overlay, time);
        }
    }

    private static void renderFullCluster(Matrix4f mat, VertexConsumer consumer, int light, int overlay, double time) {
        // Central dominant spire
        renderSpireSub(mat, consumer, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, -3.0f, 0.90f, 1.0f, 101, light, overlay, time);

        // 4 Tilted satellite spires leaning outwards in different directions
        renderSpireSub(mat, consumer, -0.15f, 0.0f, 0.12f, 45.0f, 20.0f, 6.0f, 0.62f, 1.0f, 202, light, overlay, time);
        renderSpireSub(mat, consumer, 0.16f, 0.0f, -0.13f, 160.0f, 22.0f, -8.0f, 0.54f, 1.0f, 303, light, overlay, time);
        renderSpireSub(mat, consumer, -0.12f, 0.0f, -0.16f, 245.0f, -16.0f, -18.0f, 0.46f, 1.0f, 404, light, overlay, time);
        renderSpireSub(mat, consumer, 0.14f, 0.0f, 0.15f, 320.0f, -14.0f, 20.0f, 0.40f, 1.0f, 505, light, overlay, time);

        // 3 Basal jagged shards jutting obliquely from bedrock
        renderSpireSub(mat, consumer, -0.20f, 0.0f, 0.02f, 90.0f, 44.0f, 0.0f, 0.28f, 0.9f, 606, light, overlay, time);
        renderSpireSub(mat, consumer, 0.12f, 0.0f, -0.20f, 210.0f, 46.0f, 0.0f, 0.25f, 0.9f, 707, light, overlay, time);
        renderSpireSub(mat, consumer, 0.05f, 0.0f, 0.20f, 340.0f, 40.0f, 0.0f, 0.22f, 0.9f, 808, light, overlay, time);
    }

    private static void renderLargeBud(Matrix4f mat, VertexConsumer consumer, int light, int overlay, double time) {
        // Main spire
        renderSpireSub(mat, consumer, 0.0f, 0.0f, 0.0f, 0.0f, 3.0f, -2.0f, 0.72f, 1.0f, 101, light, overlay, time);

        // 2 Satellite spires
        renderSpireSub(mat, consumer, -0.13f, 0.0f, 0.10f, 55.0f, 22.0f, 4.0f, 0.50f, 1.0f, 202, light, overlay, time);
        renderSpireSub(mat, consumer, 0.14f, 0.0f, -0.11f, 205.0f, 24.0f, -6.0f, 0.42f, 1.0f, 303, light, overlay, time);

        // 1 Basal shard
        renderSpireSub(mat, consumer, -0.10f, 0.0f, -0.13f, 140.0f, 42.0f, 0.0f, 0.24f, 0.9f, 404, light, overlay, time);
    }

    private static void renderMediumBud(Matrix4f mat, VertexConsumer consumer, int light, int overlay, double time) {
        // Main spire
        renderSpireSub(mat, consumer, 0.0f, 0.0f, 0.0f, 0.0f, 3.0f, 3.0f, 0.55f, 1.0f, 101, light, overlay, time);

        // 1 Satellite spire
        renderSpireSub(mat, consumer, -0.10f, 0.0f, 0.08f, 115.0f, 20.0f, 0.0f, 0.38f, 1.0f, 202, light, overlay, time);

        // Basal spur
        renderSpireSub(mat, consumer, 0.10f, 0.0f, -0.06f, 275.0f, 36.0f, 0.0f, 0.20f, 0.9f, 303, light, overlay, time);
    }

    private static void renderSmallBud(Matrix4f mat, VertexConsumer consumer, int light, int overlay, double time) {
        // Single sharp needle spire
        renderSpireSub(mat, consumer, 0.0f, 0.0f, 0.0f, 0.0f, 4.0f, -3.0f, 0.38f, 1.0f, 101, light, overlay, time);

        // Tiny basal spur
        renderSpireSub(mat, consumer, -0.06f, 0.0f, 0.05f, 45.0f, 32.0f, 0.0f, 0.18f, 0.9f, 202, light, overlay, time);
    }

    // ─────────────────── Gemstone Rendering (Item: astral_crystal) ───────────────────

    public static void renderGem(Matrix4f matrix, VertexConsumer consumer, int size,
                                int light, int overlay, double time) {
        Matrix4f gemMat = new Matrix4f(matrix);
        float sizeScale = 0.85f + 0.10f * Math.max(1, Math.min(5, size));
        gemMat.scale(sizeScale, sizeScale, sizeScale);

        // Primary double-terminated floating gem
        renderGeometryFromRings(gemMat, consumer, GEM_RING_Y, GEM_RING_RADIUS, GEM_RING_ROT, 301, light, overlay, time);

        // Two small floating satellite shards
        renderSpireSub(gemMat, consumer, -0.28f, 0.08f, 0.06f, 35.0f, 28.0f, 12.0f, 0.26f, 1.0f, 402, light, overlay, time);
        renderSpireSub(gemMat, consumer, 0.26f, -0.06f, -0.08f, 215.0f, -32.0f, -10.0f, 0.22f, 1.0f, 503, light, overlay, time);
    }

    // ─────────────────── Spire Primitive ───────────────────

    private static void renderSpireSub(Matrix4f baseMatrix, VertexConsumer consumer,
                                       float tx, float ty, float tz,
                                       float yawDeg, float tiltXDeg, float tiltZDeg,
                                       float scale, float heightAspect, int seed,
                                       int light, int overlay, double time) {
        Matrix4f spireMat = new Matrix4f(baseMatrix);
        spireMat.translate(tx, ty, tz);

        if (yawDeg != 0.0f) spireMat.rotateY((float) Math.toRadians(yawDeg));
        if (tiltXDeg != 0.0f) spireMat.rotateX((float) Math.toRadians(tiltXDeg));
        if (tiltZDeg != 0.0f) spireMat.rotateZ((float) Math.toRadians(tiltZDeg));

        spireMat.scale(scale, scale * heightAspect, scale);

        renderGeometryFromRings(spireMat, consumer, SPIRE_RING_Y, SPIRE_RING_RADIUS, SPIRE_RING_ROT, seed, light, overlay, time);
    }

    // ─────────────────── Ring-Based Polyhedral Emission ───────────────────

    private static void renderGeometryFromRings(Matrix4f matrix, VertexConsumer consumer,
                                                float[] ringY, float[] ringRadius, float[] ringRot,
                                                int seed, int light, int overlay, double time) {
        int ringCount = ringY.length;

        for (int ring = 0; ring < ringCount - 1; ring++) {
            float y0 = ringY[ring];
            float y1 = ringY[ring + 1];
            float rad0 = ringRadius[ring];
            float rad1 = ringRadius[ring + 1];
            float rotOff0 = ringRot[ring];
            float rotOff1 = ringRot[ring + 1];

            // Height-based subtle brightness gradient for crystal depth
            float heightFactor = 0.85f + 0.15f * ((y0 - ringY[0]) / Math.max(0.001f, ringY[ringCount - 1] - ringY[0]));

            for (int side = 0; side < SIDES; side++) {
                float angle0 = (float) (2.0 * Math.PI * side / SIDES);
                float angle1 = (float) (2.0 * Math.PI * (side + 1) / SIDES);

                float a0_bot = angle0 + (float) Math.toRadians(rotOff0);
                float a1_bot = angle1 + (float) Math.toRadians(rotOff0);
                float a0_top = angle0 + (float) Math.toRadians(rotOff1);
                float a1_top = angle1 + (float) Math.toRadians(rotOff1);

                // Per-vertex micro-displacement using simplex noise for natural jagged faceting
                float noise0 = simplexNoise2D(side * 2.1f + ring * 0.9f + seed, (float) (time * 0.003)) * 0.018f;
                float noise1 = simplexNoise2D((side + 1) * 2.1f + ring * 0.9f + seed, (float) (time * 0.003)) * 0.018f;
                float noise2 = simplexNoise2D((side + 1) * 2.1f + (ring + 1) * 0.9f + seed, (float) (time * 0.003)) * 0.018f;
                float noise3 = simplexNoise2D(side * 2.1f + (ring + 1) * 0.9f + seed, (float) (time * 0.003)) * 0.018f;

                // Bottom-left
                float x0 = (float) Math.cos(a0_bot) * (rad0 + noise0);
                float z0 = (float) Math.sin(a0_bot) * (rad0 + noise0);
                // Bottom-right
                float x1 = (float) Math.cos(a1_bot) * (rad1 + noise1);
                float z1 = (float) Math.sin(a1_bot) * (rad1 + noise1);
                // Top-right
                float x2 = (float) Math.cos(a1_top) * (rad1 + noise2);
                float z2 = (float) Math.sin(a1_top) * (rad1 + noise2);
                // Top-left
                float x3 = (float) Math.cos(a0_top) * (rad1 + noise3);
                float z3 = (float) Math.sin(a0_top) * (rad1 + noise3);

                // Compute exact face normal
                Vector3f normal = computeFaceNormal(x0, y0, z0, x1, y0, z1, x2, y1, z2);

                // Directional diffuse shading simulation + per-facet sparkle hash
                float diffuse = Math.max(0.65f, Math.min(1.0f, normal.y() * 0.20f + 0.80f));
                float facetSparkle = 0.85f + 0.15f * hashFloat(side * 4.3f + seed, ring * 3.1f, 88);
                float shade = heightFactor * diffuse * facetSparkle;

                // Rotated UV mapping per face
                int uvRot = ((int) (hashFloat(side * 5.7f + seed, ring * 7.9f, 44) * 4)) % 4;
                float[][] uvs = getRotatedUVs(uvRot);

                if (rad0 < 0.001f) {
                    // Bottom tip triangle fan
                    emitVertex(matrix, consumer, 0.0f, y0, 0.0f, 0.5f, 0.5f, shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, x2, y1, z2, uvs[2][0], uvs[2][1], shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, x3, y1, z3, uvs[3][0], uvs[3][1], shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, x3, y1, z3, uvs[3][0], uvs[3][1], shade, shade, shade, light, overlay, normal);
                } else if (rad1 < 0.001f) {
                    // Top tip triangle fan
                    emitVertex(matrix, consumer, x0, y0, z0, uvs[0][0], uvs[0][1], shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, x1, y0, z1, uvs[1][0], uvs[1][1], shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, 0.0f, y1, 0.0f, 0.5f, 0.5f, shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, 0.0f, y1, 0.0f, 0.5f, 0.5f, shade, shade, shade, light, overlay, normal);
                } else {
                    // Regular quad facet
                    emitVertex(matrix, consumer, x0, y0, z0, uvs[0][0], uvs[0][1], shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, x1, y0, z1, uvs[1][0], uvs[1][1], shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, x2, y1, z2, uvs[2][0], uvs[2][1], shade, shade, shade, light, overlay, normal);
                    emitVertex(matrix, consumer, x3, y1, z3, uvs[3][0], uvs[3][1], shade, shade, shade, light, overlay, normal);
                }
            }
        }
    }

    private static void emitVertex(Matrix4f matrix, VertexConsumer consumer,
                                   float x, float y, float z,
                                   float u, float v,
                                   float r, float g, float b,
                                   int light, int overlay,
                                   Vector3f normal) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, 1.0f)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(normal.x(), normal.y(), normal.z());
    }

    private static Vector3f computeFaceNormal(float x0, float y0, float z0,
                                             float x1, float y1, float z1,
                                             float x2, float y2, float z2) {
        float ux = x1 - x0, uy = y1 - y0, uz = z1 - z0;
        float vx = x2 - x0, vy = y2 - y0, vz = z2 - z0;
        Vector3f normal = new Vector3f(
                uy * vz - uz * vy,
                uz * vx - ux * vz,
                ux * vy - uy * vx
        );
        normal.normalize();
        return normal;
    }

    private static float[][] getRotatedUVs(int rotation) {
        return switch (rotation) {
            case 1 -> new float[][]{{1, 0}, {1, 1}, {0, 1}, {0, 0}};
            case 2 -> new float[][]{{1, 1}, {0, 1}, {0, 0}, {1, 0}};
            case 3 -> new float[][]{{0, 1}, {0, 0}, {1, 0}, {1, 1}};
            default -> new float[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        };
    }

    private static float simplexNoise2D(float x, float y) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        float fx = x - ix;
        float fy = y - iy;

        float sx = fx * fx * (3 - 2 * fx);
        float sy = fy * fy * (3 - 2 * fy);

        float n00 = hashFloat(ix, iy, 2);
        float n10 = hashFloat(ix + 1, iy, 2);
        float n01 = hashFloat(ix, iy + 1, 2);
        float n11 = hashFloat(ix + 1, iy + 1, 2);

        float nx0 = n00 + (n10 - n00) * sx;
        float nx1 = n01 + (n11 - n01) * sx;
        return (nx0 + (nx1 - nx0) * sy) * 2.0f - 1.0f;
    }

    private static float hashFloat(float x, float y, int seed) {
        int h = Float.floatToIntBits(x * 127.1f + y * 311.7f + seed * 73.7f);
        h = (h ^ (h >>> 16)) * 0x45d9f3b;
        h = (h ^ (h >>> 16)) * 0x45d9f3b;
        h = h ^ (h >>> 16);
        return (h & 0x7FFFFFFF) / (float) Integer.MAX_VALUE;
    }
}
