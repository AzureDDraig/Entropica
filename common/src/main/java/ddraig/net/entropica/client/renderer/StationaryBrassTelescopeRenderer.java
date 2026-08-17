package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.StationaryBrassTelescopeBlockEntity;
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

public class StationaryBrassTelescopeRenderer implements BlockEntityRenderer<StationaryBrassTelescopeBlockEntity, StationaryBrassTelescopeRenderer.TelescopeRenderState> {

    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/stationary_brass_telescope.png");
    private static final ResourceLocation BRASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/stationary_brass_telescope_brass.png");
    private static final ResourceLocation DIAL_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/stationary_brass_telescope_dial.png");
    private static final ResourceLocation LENS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/stationary_brass_telescope_lens.png");

    public StationaryBrassTelescopeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TelescopeRenderState createRenderState() {
        return new TelescopeRenderState();
    }

    @Override
    public void extractRenderState(StationaryBrassTelescopeBlockEntity be, TelescopeRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.yaw = be.getInterpolatedYaw(partialTick);
        state.pitch = be.getInterpolatedPitch(partialTick);
    }

    @Override
    public void submit(TelescopeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        // 1. Azimuth Turntable (Yaw Rotation)
        poseStack.pushPose();
        poseStack.translate(0.5, 0.52, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw + 180.0f));

        // Draw Azimuth Turntable Hub & Gear Ring
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            renderBox(pose.pose(), consumer, -0.22f, -0.06f, -0.22f, 0.22f, 0.04f, 0.22f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Left & Right Elevation Trunnion Fork Mounts
            renderBox(pose.pose(), consumer, -0.24f, 0.04f, -0.08f, -0.16f, 0.28f, 0.08f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            renderBox(pose.pose(), consumer, 0.16f, 0.04f, -0.08f, 0.24f, 0.28f, 0.08f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
        });

        // Draw Graduated Azimuth Dial
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(DIAL_TEXTURE), (pose, consumer) -> {
            renderBox(pose.pose(), consumer, -0.20f, -0.08f, -0.20f, 0.20f, -0.05f, 0.20f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
        });

        // 2. Elevation Pivot & Brass Optical Telescope Barrel (Pitch Rotation)
        poseStack.translate(0.0, 0.22, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));

        // Draw Brass Optical Tube Barrel
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            // Main Central Brass Tube (Length: 1.1 blocks, centered on pivot)
            renderBox(pose.pose(), consumer, -0.14f, -0.14f, -0.45f, 0.14f, 0.14f, 0.45f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Front Objective Lens Hood / Crown (Wider front)
            renderBox(pose.pose(), consumer, -0.17f, -0.17f, -0.65f, 0.17f, 0.17f, -0.45f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Rear Eyepiece Assembly (Tapered rear)
            renderBox(pose.pose(), consumer, -0.10f, -0.10f, 0.45f, 0.10f, 0.10f, 0.68f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            renderBox(pose.pose(), consumer, -0.13f, -0.13f, 0.68f, 0.13f, 0.13f, 0.76f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Elevation Trunnion Axis Pins (Connecting to fork mount)
            renderBox(pose.pose(), consumer, -0.22f, -0.04f, -0.04f, 0.22f, 0.04f, 0.04f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Side Elevation Vernier Gear Wheel
            renderBox(pose.pose(), consumer, 0.18f, -0.10f, -0.10f, 0.24f, 0.10f, 0.10f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
        });

        // Draw Glowing Astral Objective Lens Aperture
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(LENS_TEXTURE), (pose, consumer) -> {
            renderBox(pose.pose(), consumer, -0.15f, -0.15f, -0.655f, 0.15f, 0.15f, -0.645f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 0.8f, 0.95f, 1.0f, 0.9f);
        });

        poseStack.popPose();
    }

    private static void renderBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float u0, float v0, float u1, float v1, int light, int overlay, float r, float g, float b, float a) {
        // Down face (y-)
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up face (y+)
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North face (z-)
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South face (z+)
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West face (x-)
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East face (x+)
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }

    public static class TelescopeRenderState extends BlockEntityRenderState {
        public float yaw;
        public float pitch;
    }
}
