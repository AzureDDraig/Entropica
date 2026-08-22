package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.CelestialArmillaryControllerBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class CelestialArmillaryRenderer implements BlockEntityRenderer<CelestialArmillaryControllerBlockEntity, CelestialArmillaryRenderer.ArmillaryRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public CelestialArmillaryRenderer(BlockEntityRendererProvider.Context context) {
    }

    public AABB getRenderBoundingBox(CelestialArmillaryControllerBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(6.5, 9.0, 6.5);
    }

    @Override
    public ArmillaryRenderState createRenderState() {
        return new ArmillaryRenderState();
    }

    @Override
    public void extractRenderState(CelestialArmillaryControllerBlockEntity be, ArmillaryRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isStructureValid = be.isStructureValid();
        state.isStarlightActive = be.isStarlightActive();
        state.beamCount = be.getBeamCount();
        long gameTime = (be.getLevel() != null) ? be.getLevel().getGameTime() : 0;
        state.timeSec = (gameTime + partialTick) * 0.05f;
    }

    @Override
    public void submit(ArmillaryRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        float timeSec = state.timeSec;

        if (state.isStructureValid || state.isStarlightActive) {
            float cx = 0.5f;
            float cy = 2.25f; // Floating in the center of the 9x9x9 dome
            float cz = 0.5f;

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();

                // 1. Outer & Inner Rotating Refractive Optical Sphere Shells
                renderRefractiveSphere(mat, consumer, cx, cy, cz, 0.72f, timeSec, light, overlay, false);
                renderRefractiveSphere(mat, consumer, cx, cy, cz, 0.55f, timeSec, light, overlay, true);

                // 2. Dual Counter-Rotating Ethereal Gimbal Rings
                renderOrbitalGimbalRing(mat, consumer, cx, cy, cz, 0.88f, timeSec * 0.8f, 0.4f, 0.9f, 1.0f, 0.75f, light, overlay, 1.0f, 0.0f, 0.5f);
                renderOrbitalGimbalRing(mat, consumer, cx, cy, cz, 0.94f, -timeSec * 0.6f, 0.9f, 0.7f, 1.0f, 0.75f, light, overlay, 0.3f, 1.0f, 0.2f);

                // 3. Central Glowing Pulsating Cosmic Singularity inside the Sphere
                float pulse = 0.85f + 0.15f * (float) Math.sin(timeSec * 3.5f);
                renderGlowingCore(mat, consumer, cx, cy, cz, 0.25f * pulse, 0.7f, 0.95f, 1.0f, 0.95f, light, overlay);

                // 4. If Starlight Active: 4 Cardinal Inward Starlight Beams from Layer 9 (Y=+7) Lenses
                if (state.isStarlightActive) {
                    // North beam (Z = -4, Y = +7) -> Lens at (0.5, 7.5625, -3.5)
                    renderBeamLine(mat, consumer, cx, cy, cz, cx, 7.5625f, -3.5f, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);
                    // South beam (Z = +4, Y = +7) -> Lens at (0.5, 7.5625, 4.5)
                    renderBeamLine(mat, consumer, cx, cy, cz, cx, 7.5625f, 4.5f, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);
                    // West beam (X = -4, Y = +7) -> Lens at (-3.5, 7.5625, 0.5)
                    renderBeamLine(mat, consumer, cx, cy, cz, -3.5f, 7.5625f, cz, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);
                    // East beam (X = +4, Y = +7) -> Lens at (4.5, 7.5625, 0.5)
                    renderBeamLine(mat, consumer, cx, cy, cz, 4.5f, 7.5625f, cz, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);

                    // Vertical Concentrated Focus Ray: from Ocular Sphere down into Armillary Controller (Y = 0.75)
                    renderBeamLine(mat, consumer, cx, cy, cz, cx, 0.75f, cz, 0.9f, 0.95f, 1.0f, 0.95f, light, overlay);
                }
            });
        }
    }

    private static void renderRefractiveSphere(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float baseRadius, float timeSec, int light, int overlay, boolean inner) {
        int lats = 12;
        int lons = 18;
        float rotSpeed = inner ? -0.45f : 0.35f;

        for (int i = 0; i < lats; i++) {
            float lat0 = (float) Math.PI * (-0.5f + (float) i / lats);
            float z0 = (float) Math.sin(lat0);
            float zr0 = (float) Math.cos(lat0);

            float lat1 = (float) Math.PI * (-0.5f + (float) (i + 1) / lats);
            float z1 = (float) Math.sin(lat1);
            float zr1 = (float) Math.cos(lat1);

            for (int j = 0; j <= lons; j++) {
                float lon = (float) (2 * Math.PI * j / lons) + timeSec * rotSpeed;
                float x = (float) Math.cos(lon);
                float y = (float) Math.sin(lon);

                float wave0 = 1.0f + 0.04f * (float) Math.sin(4.0f * lon + 3.0f * lat0 + timeSec * 2.0f);
                float wave1 = 1.0f + 0.04f * (float) Math.sin(4.0f * lon + 3.0f * lat1 + timeSec * 2.0f);

                float r0 = baseRadius * wave0;
                float r1 = baseRadius * wave1;

                float vx0 = cx + x * zr0 * r0;
                float vy0 = cy + z0 * r0;
                float vz0 = cz + y * zr0 * r0;

                float vx1 = cx + x * zr1 * r1;
                float vy1 = cy + z1 * r1;
                float vz1 = cz + y * zr1 * r1;

                float nextLon = (float) (2 * Math.PI * (j + 1) / lons) + timeSec * rotSpeed;
                float nx = (float) Math.cos(nextLon);
                float ny = (float) Math.sin(nextLon);

                float nWave0 = 1.0f + 0.04f * (float) Math.sin(4.0f * nextLon + 3.0f * lat0 + timeSec * 2.0f);
                float nWave1 = 1.0f + 0.04f * (float) Math.sin(4.0f * nextLon + 3.0f * lat1 + timeSec * 2.0f);

                float nvx0 = cx + nx * zr0 * (baseRadius * nWave0);
                float nvy0 = cy + z0 * (baseRadius * nWave0);
                float nvz0 = cz + ny * zr0 * (baseRadius * nWave0);

                float nvx1 = cx + nx * zr1 * (baseRadius * nWave1);
                float nvy1 = cy + z1 * (baseRadius * nWave1);
                float nvz1 = cz + ny * zr1 * (baseRadius * nWave1);

                // Subtle ethereal refraction tint (soft white/cyan/violet)
                float r = inner ? 0.95f : 0.80f;
                float g = inner ? 0.85f : 0.92f;
                float b = 1.0f;
                float alpha = inner ? 0.18f : 0.28f;

                consumer.addVertex(pose, vx0, vy0, vz0).setColor(r, g, b, alpha).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(x * zr0, z0, y * zr0);
                consumer.addVertex(pose, nvx0, nvy0, nvz0).setColor(r, g, b, alpha).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(nx * zr0, z0, ny * zr0);
                consumer.addVertex(pose, nvx1, nvy1, nvz1).setColor(r, g, b, alpha).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(nx * zr1, z1, ny * zr1);
                consumer.addVertex(pose, vx1, vy1, vz1).setColor(r, g, b, alpha).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(x * zr1, z1, y * zr1);
            }
        }
    }

    private static void renderOrbitalGimbalRing(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float angle, float r, float g, float b, float a, int light, int overlay, float ax, float ay, float az) {
        int segs = 32;
        float thickness = 0.025f;

        float prevX = 0, prevY = 0, prevZ = 0;
        boolean hasPrev = false;

        for (int i = 0; i <= segs; i++) {
            float theta = (float) (2 * Math.PI * i / segs);
            // Circle in X-Z plane rotated by angle around arbitrary axis
            float x0 = (float) Math.cos(theta) * radius;
            float z0 = (float) Math.sin(theta) * radius;
            float y0 = (float) Math.sin(theta + angle) * 0.3f * radius;

            float curX = cx + x0 * (float) Math.cos(angle) - z0 * (float) Math.sin(angle) * ax;
            float curY = cy + y0 + z0 * ay * 0.4f;
            float curZ = cz + x0 * (float) Math.sin(angle) + z0 * (float) Math.cos(angle) * az;

            if (hasPrev) {
                consumer.addVertex(pose, prevX, prevY - thickness, prevZ).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(pose, curX, curY - thickness, curZ).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(pose, curX, curY + thickness, curZ).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(pose, prevX, prevY + thickness, prevZ).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            }
            prevX = curX;
            prevY = curY;
            prevZ = curZ;
            hasPrev = true;
        }
    }

    private static void renderGlowingCore(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(pose, cx - radius, cy, cz - radius).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz - radius).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz + radius).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx - radius, cy, cz + radius).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        consumer.addVertex(pose, cx - radius, cy - radius, cz).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx + radius, cy - radius, cz).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx + radius, cy + radius, cz).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx - radius, cy + radius, cz).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }

    private static void renderBeamLine(Matrix4f pose, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, int light, int overlay) {
        float thickness = 0.045f;
        consumer.addVertex(pose, x1 - thickness, y1, z1).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x1 + thickness, y1, z1).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x2 + thickness, y2, z2).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x2 - thickness, y2, z2).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    public static class ArmillaryRenderState extends BlockEntityRenderState {
        public boolean isStructureValid = false;
        public boolean isStarlightActive = false;
        public int beamCount = 0;
        public float timeSec = 0.0f;
    }
}
