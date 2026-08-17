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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class RefractiveAstralLensRenderer implements BlockEntityRenderer<RefractiveAstralLensBlockEntity, RefractiveAstralLensRenderer.LensRenderState> {

    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/refractive_astral_lens_base.png");
    private static final ResourceLocation BRASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/refractive_astral_lens_brass.png");
    private static final ResourceLocation RING_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/refractive_astral_lens_ring.png");
    private static final ResourceLocation LENS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/refractive_astral_lens.png");
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

        // 1. Group: "base" (Static Pedestal on Floor: [3, 0, 3] to [13, 2, 13], origin: [8, 0, 8])
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BASE_TEXTURE), (pose, consumer) -> {
            // Top face of base pedestal with dedicated marble/base texture
            renderTexturedBox(pose.pose(), consumer, 0.1875f, 0.0f, 0.1875f, 0.8125f, 0.125f, 0.8125f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
        });

        // 2. Group: "azimuth_yoke" (Rotates around Y axis at origin [8, 2, 8] -> [0.5, 0.125, 0.5])
        poseStack.pushPose();
        poseStack.translate(0.5, 0.125, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw + 180.0f));

        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            // Swivel Stem: [6, 2, 6] to [10, 4, 10] -> relative: [-2, 0, -2] to [2, 2, 2]
            renderTexturedBox(pose.pose(), consumer, -0.125f, 0.0f, -0.125f, 0.125f, 0.125f, 0.125f, 0.375f, 0.375f, 0.625f, 0.625f, light, overlay, 1, 1, 1, 1);

            // Yoke Bottom Bridge: [3, 4, 7] to [13, 5, 9] -> relative: [-5, 2, -1] to [5, 3, 1]
            renderTexturedBox(pose.pose(), consumer, -0.3125f, 0.125f, -0.0625f, 0.3125f, 0.1875f, 0.0625f, 0.1875f, 0.6875f, 0.8125f, 0.75f, light, overlay, 1, 1, 1, 1);

            // Yoke Arm Left: [2, 5, 7] to [4, 13, 9] -> relative: [-6, 3, -1] to [-4, 11, 1]
            renderTexturedBox(pose.pose(), consumer, -0.375f, 0.1875f, -0.0625f, -0.25f, 0.6875f, 0.0625f, 0.125f, 0.3125f, 0.25f, 0.8125f, light, overlay, 1, 1, 1, 1);

            // Yoke Arm Right: [12, 5, 7] to [14, 13, 9] -> relative: [4, 3, -1] to [6, 11, 1]
            renderTexturedBox(pose.pose(), consumer, 0.25f, 0.1875f, -0.0625f, 0.375f, 0.6875f, 0.0625f, 0.75f, 0.3125f, 0.875f, 0.8125f, light, overlay, 1, 1, 1, 1);

            // Pivot Thumbscrew Left: [1.25, 8.5, 7.5] to [2, 9.5, 8.5] -> relative: [-6.75, 6.5, -0.5] to [-6, 7.5, 0.5]
            renderTexturedBox(pose.pose(), consumer, -0.421875f, 0.40625f, -0.03125f, -0.375f, 0.46875f, 0.03125f, 0.0f, 0.0f, 0.125f, 0.125f, light, overlay, 1, 1, 1, 1);

            // Pivot Thumbscrew Right: [14, 8.5, 7.5] to [14.75, 9.5, 8.5] -> relative: [6, 6.5, -0.5] to [6.75, 7.5, 0.5]
            renderTexturedBox(pose.pose(), consumer, 0.375f, 0.40625f, -0.03125f, 0.421875f, 0.46875f, 0.03125f, 0.0f, 0.0f, 0.125f, 0.125f, light, overlay, 1, 1, 1, 1);
        });

        // 3. Group: "elevation_lens" (Pivots around horizontal trunnion axis at origin [8, 9, 8] -> relative translation [0.0, 7/16, 0.0])
        poseStack.translate(0.0, 0.4375, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));

        // Render Outer Brass/Ring Lens Bezel
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(RING_TEXTURE), (pose, consumer) -> {
            // Lens Bezel Top Bar: [4, 12, 7.25] to [12, 13, 8.75] -> relative: [-4, 3, -0.75] to [4, 4, 0.75]
            renderTexturedBox(pose.pose(), consumer, -0.25f, 0.1875f, -0.046875f, 0.25f, 0.25f, 0.046875f, 0.25f, 0.0f, 0.75f, 0.0625f, light, overlay, 1, 1, 1, 1);

            // Lens Bezel Bottom Bar: [4, 5, 7.25] to [12, 6, 8.75] -> relative: [-4, -4, -0.75] to [4, -3, 0.75]
            renderTexturedBox(pose.pose(), consumer, -0.25f, -0.25f, -0.046875f, 0.25f, -0.1875f, 0.046875f, 0.25f, 0.125f, 0.75f, 0.1875f, light, overlay, 1, 1, 1, 1);

            // Lens Bezel Left Bar: [4, 6, 7.25] to [5, 12, 8.75] -> relative: [-4, -3, -0.75] to [-3, 3, 0.75]
            renderTexturedBox(pose.pose(), consumer, -0.25f, -0.1875f, -0.046875f, -0.1875f, 0.1875f, 0.046875f, 0.25f, 0.25f, 0.3125f, 0.625f, light, overlay, 1, 1, 1, 1);

            // Lens Bezel Right Bar: [11, 6, 7.25] to [12, 12, 8.75] -> relative: [3, -3, -0.75] to [4, 3, 0.75]
            renderTexturedBox(pose.pose(), consumer, 0.1875f, -0.1875f, -0.046875f, 0.25f, 0.1875f, 0.046875f, 0.6875f, 0.25f, 0.75f, 0.625f, light, overlay, 1, 1, 1, 1);
        });

        // Render Convex Starlight Quartz Lens Disc: [5, 6, 7.5] to [11, 12, 8.5] -> relative: [-3, -3, -0.5] to [3, 3, 0.5]
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(LENS_TEXTURE), (pose, consumer) -> {
            renderTexturedBox(pose.pose(), consumer, -0.1875f, -0.1875f, -0.03125f, 0.1875f, 0.1875f, 0.03125f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 0.9f, 0.95f, 1.0f, 0.92f);
        });

        // 4. Concentrated Starlight Beam Emission (When Focused/Calibrated)
        if (state.isFocused) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                // Skyward Starlight Influx Stream (from the stars into the lens)
                renderTexturedBox(pose.pose(), consumer, -0.04f, -0.04f, -2.5f, 0.04f, 0.04f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 0.75f, 0.92f, 1.0f, 0.75f);
                // Focused Output Beam (refracted down/through the lens)
                renderTexturedBox(pose.pose(), consumer, -0.03f, -0.03f, 0.0f, 0.03f, 0.03f, 1.8f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1.0f, 0.96f, 0.75f, 0.90f);
            });
        }

        poseStack.popPose();
    }

    private static void renderTexturedBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float u0, float v0, float u1, float v1, int light, int overlay, float r, float g, float b, float a) {
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
