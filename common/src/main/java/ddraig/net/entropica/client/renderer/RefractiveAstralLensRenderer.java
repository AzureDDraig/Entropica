package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class RefractiveAstralLensRenderer implements BlockEntityRenderer<RefractiveAstralLensBlockEntity, RefractiveAstralLensRenderer.LensRenderState> {

    private static final ResourceLocation BRASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/stationary_brass_telescope_brass.png");
    private static final ResourceLocation LENS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/stationary_brass_telescope_lens.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public RefractiveAstralLensRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public LensRenderState createRenderState() {
        return new LensRenderState();
    }

    @Override
    public void extractRenderState(RefractiveAstralLensBlockEntity be, LensRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.yaw = be.getInterpolatedYaw(partialTick);
        state.pitch = be.getInterpolatedPitch(partialTick);
        state.isFocused = be.isFocused();
        state.targetName = be.getTargetName();
    }

    @Override
    public void submit(LensRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        // 1. Stationary Base & Gimbal Pillars (Floor mounted)
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            // Heavy Octagonal Base Plate (y = 0.0 to 0.12)
            renderBox(pose.pose(), consumer, 0.20f, 0.0f, 0.20f, 0.80f, 0.12f, 0.80f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Center Pedestal Sleeve (y = 0.12 to 0.22)
            renderBox(pose.pose(), consumer, 0.35f, 0.12f, 0.35f, 0.65f, 0.22f, 0.65f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
        });

        // 2. Azimuth Gimbal Turntable (Rotates around Y axis)
        poseStack.pushPose();
        poseStack.translate(0.5, 0.22, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw + 180.0f));

        // Draw Rotating Azimuth Ring & Twin Gimbal Fork Arms
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            // Lower Turntable Disc
            renderBox(pose.pose(), consumer, -0.18f, 0.0f, -0.18f, 0.18f, 0.06f, 0.18f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Left Fork Arm
            renderBox(pose.pose(), consumer, -0.22f, 0.06f, -0.06f, -0.14f, 0.36f, 0.06f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Right Fork Arm
            renderBox(pose.pose(), consumer, 0.14f, 0.06f, -0.06f, 0.22f, 0.36f, 0.06f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
        });

        // 3. Elevation Pivot & Refractive Optical Lens Ring (Pivots around horizontal X axis)
        poseStack.translate(0.0, 0.28, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));

        // Draw Brass Lens Bezel Frame
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            // Elevation Axis Trunnion Pins
            renderBox(pose.pose(), consumer, -0.22f, -0.03f, -0.03f, 0.22f, 0.03f, 0.03f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Outer Lens Bezel Frame Ring (Square/Octagonal Bezel)
            renderBox(pose.pose(), consumer, -0.28f, 0.25f, -0.04f, 0.28f, 0.29f, 0.04f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1); // Top
            renderBox(pose.pose(), consumer, -0.28f, -0.29f, -0.04f, 0.28f, -0.25f, 0.04f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1); // Bottom
            renderBox(pose.pose(), consumer, -0.29f, -0.25f, -0.04f, -0.25f, 0.25f, 0.04f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1); // Left
            renderBox(pose.pose(), consumer, 0.25f, -0.25f, -0.04f, 0.29f, 0.25f, 0.04f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1); // Right
        });

        // Draw Glowing Refractive Astral Glass Lens (Thick refractive crystal aperture)
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(LENS_TEXTURE), (pose, consumer) -> {
            renderBox(pose.pose(), consumer, -0.25f, -0.25f, -0.02f, 0.25f, 0.25f, 0.02f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 0.85f, 0.95f, 1.0f, 0.92f);
        });

        // 4. If focused on a star, render concentrated starlight beam emission
        if (state.isFocused) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                // Front Starlight Column (Skyward starlight capture)
                renderBox(pose.pose(), consumer, -0.05f, -0.05f, -1.8f, 0.05f, 0.05f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 0.7f, 0.9f, 1.0f, 0.65f);
                // Focused Outgoing Beam (Directed downwards/backwards)
                renderBox(pose.pose(), consumer, -0.03f, -0.03f, 0.0f, 0.03f, 0.03f, 1.2f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1.0f, 0.95f, 0.7f, 0.85f);
            });
        }

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

    public static class LensRenderState extends BlockEntityRenderState {
        public float yaw;
        public float pitch;
        public boolean isFocused;
        public String targetName;
    }
}
