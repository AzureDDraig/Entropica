package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
        return new AABB(blockEntity.getBlockPos()).inflate(32.0, 32.0, 32.0);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(CelestialArmillaryControllerBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
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
        state.timeSec = (float) ((System.nanoTime() / 1_000_000_000.0) % 100000.0);
    }

    @Override
    public void submit(ArmillaryRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        float timeSec = (float) ((System.nanoTime() / 1_000_000_000.0) % 100000.0);

        if (state.isStructureValid || state.isStarlightActive) {
            float cx = 0.5f;
            float cy = 2.5f; // Floating in the center of the 9x9x9 dome
            float cz = 0.5f;

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f basePose = pose.pose();

                // 1. Large Outer Rotating Refractive Optical Sphere Shell (Smooth Matrix4f rotation from basePose)
                Matrix4f outerSphereMat = new Matrix4f(basePose)
                        .translate(cx, cy, cz)
                        .rotateY((float) Math.toRadians(timeSec * 22.0f))
                        .rotateX((float) Math.toRadians((float) Math.sin(timeSec * 0.45f) * 12.0f));
                renderUnitSphere(outerSphereMat, consumer, 1.35f, 0.80f, 0.92f, 1.0f, 0.28f, light, overlay);

                // 2. Inner Counter-Rotating Refractive Sphere Shell
                Matrix4f innerSphereMat = new Matrix4f(basePose)
                        .translate(cx, cy, cz)
                        .rotateY((float) Math.toRadians(-timeSec * 32.0f))
                        .rotateZ((float) Math.toRadians((float) Math.cos(timeSec * 0.55f) * 15.0f));
                renderUnitSphere(innerSphereMat, consumer, 1.05f, 0.95f, 0.85f, 1.0f, 0.20f, light, overlay);

                // 3. Gimbal Ring 1 (Smooth continuous orbital rotation)
                Matrix4f ring1Mat = new Matrix4f(basePose)
                        .translate(cx, cy, cz)
                        .rotateX((float) Math.toRadians(35.0f))
                        .rotateY((float) Math.toRadians(timeSec * 28.0f))
                        .rotateZ((float) Math.toRadians((float) Math.sin(timeSec * 0.7f) * 18.0f));
                renderFlatRing(ring1Mat, consumer, 1.62f, 0.035f, 0.4f, 0.9f, 1.0f, 0.80f, light, overlay);

                // 4. Gimbal Ring 2 (Counter-orbital rotation)
                Matrix4f ring2Mat = new Matrix4f(basePose)
                        .translate(cx, cy, cz)
                        .rotateZ((float) Math.toRadians(55.0f))
                        .rotateY((float) Math.toRadians(-timeSec * 24.0f))
                        .rotateX((float) Math.toRadians((float) Math.cos(timeSec * 0.6f) * 22.0f));
                renderFlatRing(ring2Mat, consumer, 1.78f, 0.035f, 0.9f, 0.7f, 1.0f, 0.80f, light, overlay);

                // 5. Central Glowing Pulsating Cosmic Singularity inside the Sphere
                float pulse = 0.85f + 0.15f * (float) Math.sin(timeSec * 3.5f);
                Matrix4f coreMat = new Matrix4f(basePose)
                        .translate(cx, cy, cz)
                        .rotateY((float) Math.toRadians(timeSec * 50.0f))
                        .rotateX((float) Math.toRadians(timeSec * 35.0f));
                renderGlowingCore(coreMat, consumer, 0, 0, 0, 0.42f * pulse, 0.7f, 0.95f, 1.0f, 0.95f, light, overlay);

                // 6. If Starlight Active: 4 Cardinal Inward Starlight Beams from Layer 9 (Y=+7) Lenses
                if (state.isStarlightActive) {
                    // North beam (Z = -4, Y = +7) -> Lens at (0.5, 7.5625, -3.5)
                    renderBeamLine(basePose, consumer, cx, cy, cz, cx, 7.5625f, -3.5f, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);
                    // South beam (Z = +4, Y = +7) -> Lens at (0.5, 7.5625, 4.5)
                    renderBeamLine(basePose, consumer, cx, cy, cz, cx, 7.5625f, 4.5f, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);
                    // West beam (X = -4, Y = +7) -> Lens at (-3.5, 7.5625, 0.5)
                    renderBeamLine(basePose, consumer, cx, cy, cz, -3.5f, 7.5625f, cz, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);
                    // East beam (X = +4, Y = +7) -> Lens at (4.5, 7.5625, 0.5)
                    renderBeamLine(basePose, consumer, cx, cy, cz, 4.5f, 7.5625f, cz, 0.4f, 0.95f, 1.0f, 0.88f, light, overlay);

                    // Vertical Concentrated Focus Ray: from Ocular Sphere down into Armillary Controller (Y = 0.75)
                    renderBeamLine(basePose, consumer, cx, cy, cz, cx, 0.75f, cz, 0.9f, 0.95f, 1.0f, 0.95f, light, overlay);
                }
            });
        }
    }

    private static void renderUnitSphere(Matrix4f pose, VertexConsumer consumer, float radius, float r, float g, float b, float alpha, int light, int overlay) {
        int lats = 16;
        int lons = 24;

        for (int i = 0; i < lats; i++) {
            float lat0 = (float) Math.PI * (-0.5f + (float) i / lats);
            float z0 = (float) Math.sin(lat0);
            float zr0 = (float) Math.cos(lat0);

            float lat1 = (float) Math.PI * (-0.5f + (float) (i + 1) / lats);
            float z1 = (float) Math.sin(lat1);
            float zr1 = (float) Math.cos(lat1);

            for (int j = 0; j < lons; j++) {
                float lon0 = (float) (2 * Math.PI * j / lons);
                float lon1 = (float) (2 * Math.PI * (j + 1) / lons);

                float cos0 = (float) Math.cos(lon0);
                float sin0 = (float) Math.sin(lon0);
                float cos1 = (float) Math.cos(lon1);
                float sin1 = (float) Math.sin(lon1);

                float x00 = cos0 * zr0 * radius;
                float y00 = z0 * radius;
                float z00 = sin0 * zr0 * radius;

                float x10 = cos0 * zr1 * radius;
                float y10 = z1 * radius;
                float z10 = sin0 * zr1 * radius;

                float x01 = cos1 * zr0 * radius;
                float y01 = z0 * radius;
                float z01 = sin1 * zr0 * radius;

                float x11 = cos1 * zr1 * radius;
                float y11 = z1 * radius;
                float z11 = sin1 * zr1 * radius;

                consumer.addVertex(pose, x00, y00, z00).setColor(r, g, b, alpha).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(x00 / radius, y00 / radius, z00 / radius);
                consumer.addVertex(pose, x01, y01, z01).setColor(r, g, b, alpha).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(x01 / radius, y01 / radius, z01 / radius);
                consumer.addVertex(pose, x11, y11, z11).setColor(r, g, b, alpha).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(x11 / radius, y11 / radius, z11 / radius);
                consumer.addVertex(pose, x10, y10, z10).setColor(r, g, b, alpha).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(x10 / radius, y10 / radius, z10 / radius);
            }
        }
    }

    private static void renderFlatRing(Matrix4f pose, VertexConsumer consumer, float radius, float thickness, float r, float g, float b, float a, int light, int overlay) {
        int segs = 36;
        for (int i = 0; i < segs; i++) {
            float theta0 = (float) (2 * Math.PI * i / segs);
            float theta1 = (float) (2 * Math.PI * (i + 1) / segs);

            float cos0 = (float) Math.cos(theta0);
            float sin0 = (float) Math.sin(theta0);
            float cos1 = (float) Math.cos(theta1);
            float sin1 = (float) Math.sin(theta1);

            float rIn = radius - thickness;
            float rOut = radius + thickness;

            // Top ring quad
            consumer.addVertex(pose, cos0 * rIn, 0.015f, sin0 * rIn).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, cos1 * rIn, 0.015f, sin1 * rIn).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, cos1 * rOut, 0.015f, sin1 * rOut).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, cos0 * rOut, 0.015f, sin0 * rOut).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

            // Bottom ring quad
            consumer.addVertex(pose, cos0 * rOut, -0.015f, sin0 * rOut).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
            consumer.addVertex(pose, cos1 * rOut, -0.015f, sin1 * rOut).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
            consumer.addVertex(pose, cos1 * rIn, -0.015f, sin1 * rIn).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
            consumer.addVertex(pose, cos0 * rIn, -0.015f, sin0 * rIn).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
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
