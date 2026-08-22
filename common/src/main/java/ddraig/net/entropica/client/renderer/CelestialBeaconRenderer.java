package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.CelestialBeaconControllerBlockEntity;
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

public class CelestialBeaconRenderer implements BlockEntityRenderer<CelestialBeaconControllerBlockEntity, CelestialBeaconRenderer.BeaconRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public CelestialBeaconRenderer(BlockEntityRendererProvider.Context context) {
    }

    public AABB getRenderBoundingBox(CelestialBeaconControllerBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(4.5, 16.0, 4.5);
    }

    @Override
    public BeaconRenderState createRenderState() {
        return new BeaconRenderState();
    }

    @Override
    public void extractRenderState(CelestialBeaconControllerBlockEntity be, BeaconRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isStructureValid = be.isStructureValid();
        state.isStarlightActive = be.isStarlightActive();
        state.timeSec = (float) ((System.nanoTime() / 1_000_000_000.0) % 100000.0);
    }

    @Override
    public void submit(BeaconRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        float timeSec = (float) ((System.nanoTime() / 1_000_000_000.0) % 100000.0);

        if (state.isStructureValid || state.isStarlightActive) {
            float cx = 0.5f;
            float cy = 1.2f;
            float cz = 0.5f;

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();

                // 1. Horizontal Rotating Runic Beacon Rings
                float ringRot1 = timeSec * 1.2f;
                float ringRot2 = -timeSec * 0.9f;
                renderHorizontalRunicRing(mat, consumer, cx, cy + 0.1f, cz, 1.4f, ringRot1, 0.7f, 0.4f, 1.0f, 0.65f, light, overlay);
                renderHorizontalRunicRing(mat, consumer, cx, cy + 0.35f, cz, 1.8f, ringRot2, 0.4f, 0.8f, 1.0f, 0.50f, light, overlay);

                // 2. Central Beacon Pulsating Core
                float pulse = 0.88f + 0.12f * (float) Math.sin(timeSec * 4.0f);
                renderGlowingCore(mat, consumer, cx, cy + 0.2f, cz, 0.35f * pulse, 0.8f, 0.6f, 1.0f, 0.95f, light, overlay);

                // 3. Vertical Cosmic Starlight Beam Shaft if Active
                if (state.isStarlightActive) {
                    renderVerticalBeam(mat, consumer, cx, cy + 1.8f, cz, 48.0f, 0.22f, 0.6f, 0.85f, 1.0f, 0.60f, light, overlay);
                }
            });
        }
    }

    private static void renderHorizontalRunicRing(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float angle, float r, float g, float b, float a, int light, int overlay) {
        int segs = 28;
        float thickness = 0.04f;
        float prevX = 0, prevZ = 0;
        boolean hasPrev = false;

        for (int i = 0; i <= segs; i++) {
            float theta = (float) (2 * Math.PI * i / segs) + angle;
            float curX = cx + (float) Math.cos(theta) * radius;
            float curZ = cz + (float) Math.sin(theta) * radius;

            if (hasPrev) {
                consumer.addVertex(pose, prevX, cy - thickness, prevZ).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(pose, curX, cy - thickness, curZ).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(pose, curX, cy + thickness, curZ).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(pose, prevX, cy + thickness, prevZ).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            }
            prevX = curX;
            prevZ = curZ;
            hasPrev = true;
        }
    }

    private static void renderVerticalBeam(Matrix4f pose, VertexConsumer consumer, float cx, float startY, float cz, float height, float radius, float r, float g, float b, float a, int light, int overlay) {
        float endY = startY + height;
        consumer.addVertex(pose, cx - radius, startY, cz).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx + radius, startY, cz).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx + radius, endY, cz).setColor(r, g, b, a * 0.1f).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx - radius, endY, cz).setColor(r, g, b, a * 0.1f).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        consumer.addVertex(pose, cx, startY, cz - radius).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, cx, startY, cz + radius).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, cx, endY, cz + radius).setColor(r, g, b, a * 0.1f).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, cx, endY, cz - radius).setColor(r, g, b, a * 0.1f).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }

    private static void renderGlowingCore(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(pose, cx - radius, cy, cz - radius).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz - radius).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz + radius).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx - radius, cy, cz + radius).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    public static class BeaconRenderState extends BlockEntityRenderState {
        public boolean isStructureValid = false;
        public boolean isStarlightActive = false;
        public float timeSec = 0.0f;
    }
}
