package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.MateriaBlessingBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * MateriaBlessingRenderer — Procedural code-rendered floating faceted crystal.
 *
 * Renders a single large monolithic crystal with flat faceted planes,
 * matching the visual style of a natural quartz/gemstone formation.
 * Uses two texture passes:
 * 1. Crystal body (solid, tinted with EssenceType RGB)
 * 2. Crack overlay (cutout, tinted darker)
 *
 * This renderer is used for BOTH Lesser and Greater variants,
 * with scale and texture determined by the isGreater() flag.
 */
public class MateriaBlessingRenderer implements BlockEntityRenderer<MateriaBlessingBlockEntity, MateriaBlessingRenderer.BlessingRenderState> {

    // Textures for Lesser variant
    private static final ResourceLocation LESSER_CRYSTAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/lesser_materia_blessing_crystal.png");
    private static final ResourceLocation LESSER_CRACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/lesser_materia_blessing_cracks.png");

    // Textures for Greater variant
    private static final ResourceLocation GREATER_CRYSTAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/greater_materia_blessing_crystal.png");
    private static final ResourceLocation GREATER_CRACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/greater_materia_blessing_cracks.png");

    private static final int FULL_BRIGHT = 15728880;
    private static final int OVERLAY = OverlayTexture.NO_OVERLAY;

    // Crystal geometry — a single large faceted crystal silhouette
    // Defined as vertical cross-section "rings" that form the crystal shape
    // Each ring: {y-height, radius, number-of-sides, rotation-offset-degrees}
    // The crystal tapers from a rough bottom point → wide mid-section → sharp apex

    // Vertices are computed from these rings to form quads between adjacent rings
    private static final int CRYSTAL_SIDES = 6; // hexagonal cross-section

    // Y-positions and radii defining the crystal silhouette (bottom to top)
    // Crystal is ~2.4 blocks tall for Lesser, scaled up for Greater
    private static final float[] RING_Y =     {0.0f,  0.15f, 0.4f,  0.6f,  0.85f, 1.0f,  1.3f,  1.6f,  1.9f,  2.15f, 2.35f, 2.4f};
    private static final float[] RING_RADIUS = {0.0f,  0.12f, 0.28f, 0.42f, 0.52f, 0.55f, 0.50f, 0.42f, 0.32f, 0.20f, 0.08f, 0.0f};
    // Per-ring rotation jitter (degrees) for asymmetry
    private static final float[] RING_ROT_OFFSET = {0, 5, -3, 7, -2, 4, -6, 3, -4, 8, -5, 0};

    // Slight tilt angle for natural look
    private static final float TILT_X = 5.0f; // degrees
    private static final float TILT_Z = 3.0f; // degrees

    public MateriaBlessingRenderer(BlockEntityRendererProvider.Context context) {
    }

    // ─────────────────── Render State ───────────────────

    public static class BlessingRenderState extends BlockEntityRenderState {
        public EssenceType ambientAffinity = EssenceType.REGULAR;
        public float rotationAngle = 0f;
        public float bobPhase = 0f;
        public long gameTime = 0L;
        public boolean isGreater = false;
    }

    @Override
    public BlessingRenderState createRenderState() {
        return new BlessingRenderState();
    }

    @Override
    public void extractRenderState(MateriaBlessingBlockEntity be, BlessingRenderState state, float partialTick,
                                    Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.ambientAffinity = be.getAmbientAffinity();
        float rotSpeed = be.isGreater() ? 0.6f : 0.5f;
        state.rotationAngle = be.getRotationAngle() + partialTick * rotSpeed;
        state.bobPhase = be.getBobPhase() + partialTick;
        state.gameTime = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
        state.isGreater = be.isGreater();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    // ─────────────────── Submit (Render) ───────────────────

    @Override
    public void submit(BlessingRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Select textures based on variant
        ResourceLocation crystalTex = state.isGreater ? GREATER_CRYSTAL_TEXTURE : LESSER_CRYSTAL_TEXTURE;
        ResourceLocation crackTex = state.isGreater ? GREATER_CRACK_TEXTURE : LESSER_CRACK_TEXTURE;

        // Get EssenceType RGB color
        EssenceType affinity = state.ambientAffinity;
        double timeD = state.gameTime + state.bobPhase;
        int[] rgb = affinity.getCurrentRGB(timeD);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        // Bobbing offset
        float bobOffset = (float) Math.sin(state.bobPhase * 0.05) * 0.15f;

        // Scale: Greater is 1.5× larger
        float scale = state.isGreater ? 1.5f : 1.0f;

        // Center offset: center of the multiblock footprint
        float centerX = state.isGreater ? 1.5f : 1.0f;
        float centerZ = state.isGreater ? 1.5f : 1.0f;

        poseStack.pushPose();

        // Position at center of multiblock, slightly above ground
        poseStack.translate(centerX, 0.3 + bobOffset, centerZ);

        // Apply continuous Y-axis rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotationAngle));

        // Apply slight tilt for natural look
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT_X));
        poseStack.mulPose(Axis.ZP.rotationDegrees(TILT_Z));

        // Apply scale
        poseStack.scale(scale, scale, scale);

        Matrix4f matrix = poseStack.last().pose();

        // Pass 1: Solid crystal body
        VertexConsumer crystalConsumer = bufferSource.getBuffer(RenderType.entitySolid(crystalTex));
        renderCrystalBody(matrix, crystalConsumer, r, g, b, timeD);

        // Pass 2: Crack overlay (cutout, darker tint)
        VertexConsumer crackConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(crackTex));
        float darkR = r * 0.65f;
        float darkG = g * 0.65f;
        float darkB = b * 0.65f;
        renderCrystalBody(matrix, crackConsumer, darkR, darkG, darkB, timeD);

        // If Greater variant, render branching satellite crystal spires around the base
        if (state.isGreater) {
            renderSatelliteSpire(poseStack, bufferSource, crystalTex, crackTex, r, g, b, darkR, darkG, darkB, timeD, -0.45f, 0.15f, 0.35f, 0.55f, 18.0f, 12.0f);
            renderSatelliteSpire(poseStack, bufferSource, crystalTex, crackTex, r, g, b, darkR, darkG, darkB, timeD, 0.40f, 0.12f, -0.40f, 0.48f, -15.0f, -16.0f);
            renderSatelliteSpire(poseStack, bufferSource, crystalTex, crackTex, r, g, b, darkR, darkG, darkB, timeD, -0.30f, 0.10f, -0.45f, 0.38f, -20.0f, 10.0f);
        }

        poseStack.popPose();
    }

    private void renderSatelliteSpire(PoseStack poseStack, MultiBufferSource bufferSource,
                                      ResourceLocation crystalTex, ResourceLocation crackTex,
                                      float r, float g, float b, float darkR, float darkG, float darkB,
                                      double timeD, float offsetX, float offsetY, float offsetZ,
                                      float spireScale, float tiltX, float tiltZ) {
        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, offsetZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(tiltX));
        poseStack.mulPose(Axis.ZP.rotationDegrees(tiltZ));
        poseStack.scale(spireScale, spireScale, spireScale);

        Matrix4f spireMatrix = poseStack.last().pose();
        VertexConsumer crystalConsumer = bufferSource.getBuffer(RenderType.entitySolid(crystalTex));
        renderCrystalBody(spireMatrix, crystalConsumer, r, g, b, timeD + offsetX * 10.0);

        VertexConsumer crackConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(crackTex));
        renderCrystalBody(spireMatrix, crackConsumer, darkR, darkG, darkB, timeD + offsetX * 10.0);

        poseStack.popPose();
    }

    // ─────────────────── Crystal Body Rendering ───────────────────

    /**
     * Renders the single faceted crystal shape using ring-based geometry.
     * Each pair of adjacent rings forms a band of quads (or triangles at tips).
     */
    private void renderCrystalBody(Matrix4f matrix, VertexConsumer consumer,
                                    float r, float g, float b, double time) {
        int ringCount = RING_Y.length;

        for (int ring = 0; ring < ringCount - 1; ring++) {
            float y0 = RING_Y[ring];
            float y1 = RING_Y[ring + 1];
            float rad0 = RING_RADIUS[ring];
            float rad1 = RING_RADIUS[ring + 1];
            float rotOff0 = RING_ROT_OFFSET[ring];
            float rotOff1 = RING_ROT_OFFSET[ring + 1];

            // Height-based brightness variation for depth
            float heightFactor = 0.7f + 0.3f * (y0 / RING_Y[ringCount - 1]);

            for (int side = 0; side < CRYSTAL_SIDES; side++) {
                float angle0 = (float) (2.0 * Math.PI * side / CRYSTAL_SIDES);
                float angle1 = (float) (2.0 * Math.PI * (side + 1) / CRYSTAL_SIDES);

                // Apply per-ring rotation offset for asymmetry
                float a0_bot = angle0 + (float) Math.toRadians(rotOff0);
                float a1_bot = angle1 + (float) Math.toRadians(rotOff0);
                float a0_top = angle0 + (float) Math.toRadians(rotOff1);
                float a1_top = angle1 + (float) Math.toRadians(rotOff1);

                // Apply subtle noise displacement per vertex
                float noise0 = simplexNoise2D(side * 1.7f + ring * 0.5f, (float) (time * 0.005)) * 0.015f;
                float noise1 = simplexNoise2D((side + 1) * 1.7f + ring * 0.5f, (float) (time * 0.005)) * 0.015f;
                float noise2 = simplexNoise2D((side + 1) * 1.7f + (ring + 1) * 0.5f, (float) (time * 0.005)) * 0.015f;
                float noise3 = simplexNoise2D(side * 1.7f + (ring + 1) * 0.5f, (float) (time * 0.005)) * 0.015f;

                // Bottom-left
                float x0 = (float) Math.cos(a0_bot) * (rad0 + noise0);
                float z0 = (float) Math.sin(a0_bot) * (rad0 + noise0);
                // Bottom-right
                float x1 = (float) Math.cos(a1_bot) * (rad0 + noise1);
                float z1 = (float) Math.sin(a1_bot) * (rad0 + noise1);
                // Top-right
                float x2 = (float) Math.cos(a1_top) * (rad1 + noise2);
                float z2 = (float) Math.sin(a1_top) * (rad1 + noise2);
                // Top-left
                float x3 = (float) Math.cos(a0_top) * (rad1 + noise3);
                float z3 = (float) Math.sin(a0_top) * (rad1 + noise3);

                // Per-face brightness variation (iridescent effect)
                float faceVariation = 0.85f + 0.15f * hashFloat(side * 3.1f, ring * 2.7f, 42);
                float faceR = r * heightFactor * faceVariation;
                float faceG = g * heightFactor * faceVariation;
                float faceB = b * heightFactor * faceVariation;

                // Compute face normal
                Vector3f normal = computeFaceNormal(x0, y0, z0, x1, y0, z1, x2, y1, z2);

                // Deterministic UV rotation per face — hash to select one of 4 UV orientations
                int uvRot = ((int) (hashFloat(side * 7.3f, ring * 11.1f, 99) * 4)) % 4;
                float[][] uvCoords = getRotatedUVs(uvRot);

                if (rad0 < 0.001f) {
                    // Bottom tip — triangle fan (degenerate quad)
                    emitVertex(matrix, consumer, 0, y0, 0, 0.5f, 0.5f, faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, x2, y1, z2, uvCoords[2][0], uvCoords[2][1], faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, x3, y1, z3, uvCoords[3][0], uvCoords[3][1], faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, x3, y1, z3, uvCoords[3][0], uvCoords[3][1], faceR, faceG, faceB, normal); // degenerate
                } else if (rad1 < 0.001f) {
                    // Top tip — triangle fan (degenerate quad)
                    emitVertex(matrix, consumer, x0, y0, z0, uvCoords[0][0], uvCoords[0][1], faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, x1, y0, z1, uvCoords[1][0], uvCoords[1][1], faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, 0, y1, 0, 0.5f, 0.5f, faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, 0, y1, 0, 0.5f, 0.5f, faceR, faceG, faceB, normal); // degenerate
                } else {
                    // Regular quad
                    emitVertex(matrix, consumer, x0, y0, z0, uvCoords[0][0], uvCoords[0][1], faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, x1, y0, z1, uvCoords[1][0], uvCoords[1][1], faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, x2, y1, z2, uvCoords[2][0], uvCoords[2][1], faceR, faceG, faceB, normal);
                    emitVertex(matrix, consumer, x3, y1, z3, uvCoords[3][0], uvCoords[3][1], faceR, faceG, faceB, normal);
                }
            }
        }
    }

    // ─────────────────── UV Rotation ───────────────────

    /**
     * Returns UV coordinates rotated by 0/90/180/270 degrees for per-face randomization.
     */
    private float[][] getRotatedUVs(int rotation) {
        return switch (rotation) {
            case 1 -> new float[][]{{1, 0}, {1, 1}, {0, 1}, {0, 0}};    // 90°
            case 2 -> new float[][]{{1, 1}, {0, 1}, {0, 0}, {1, 0}};    // 180°
            case 3 -> new float[][]{{0, 1}, {0, 0}, {1, 0}, {1, 1}};    // 270°
            default -> new float[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}};   // 0°
        };
    }

    // ─────────────────── Vertex Emission ───────────────────

    private void emitVertex(Matrix4f matrix, VertexConsumer consumer,
                             float x, float y, float z,
                             float u, float v,
                             float r, float g, float b,
                             Vector3f normal) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, 1.0f)
                .setUv(u, v)
                .setOverlay(OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(normal.x(), normal.y(), normal.z());
    }

    // ─────────────────── Simplex Noise (Simplified 2D) ───────────────────

    private float simplexNoise2D(float x, float y) {
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

    // ─────────────────── Hash Function ───────────────────

    private float hashFloat(float x, float y, int seed) {
        int h = Float.floatToIntBits(x * 127.1f + y * 311.7f + seed * 73.7f);
        h = (h ^ (h >>> 16)) * 0x45d9f3b;
        h = (h ^ (h >>> 16)) * 0x45d9f3b;
        h = h ^ (h >>> 16);
        return (h & 0x7FFFFFFF) / (float) Integer.MAX_VALUE;
    }

    // ─────────────────── Normal Computation ───────────────────

    private Vector3f computeFaceNormal(float x0, float y0, float z0,
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
}
