package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.BeamSplitterPrismBlockEntity;
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

public class BeamSplitterPrismRenderer implements BlockEntityRenderer<BeamSplitterPrismBlockEntity, BeamSplitterPrismRenderer.PrismRenderState> {

    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/beam_splitter_prism_base.png");
    private static final ResourceLocation BRASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/beam_splitter_prism_brass.png");
    private static final ResourceLocation CRYSTAL_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/beam_splitter_prism_crystal.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public BeamSplitterPrismRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public PrismRenderState createRenderState() {
        return new PrismRenderState();
    }

    @Override
    public void extractRenderState(BeamSplitterPrismBlockEntity be, PrismRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isReceiving = be.isReceiving();
        state.rotationAngle = be.getInterpolatedRotation(partialTick);
        state.activeStarName = be.getActiveStarName();
        state.leftSplitYaw = be.getLeftSplitYaw();
        state.rightSplitYaw = be.getRightSplitYaw();
        state.leftRayDistance = be.getLeftRayDistance();
        state.rightRayDistance = be.getRightRayDistance();
    }

    @Override
    public void submit(PrismRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        // 1. Group: "base" (Static Plinth: [1, 0, 1] to [15, 2.5, 15])
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BASE_TEXTURE), (pose, consumer) -> {
            renderTexturedBox(pose.pose(), consumer, 0.0625f, 0.0f, 0.0625f, 0.9375f, 0.15625f, 0.9375f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 1, 1, 1, 1);
            // Middle stepped bevel: [2.5, 2.5, 2.5] to [13.5, 4.0, 13.5]
            renderTexturedBox(pose.pose(), consumer, 0.15625f, 0.15625f, 0.15625f, 0.84375f, 0.25f, 0.84375f, 0.125f, 0.125f, 0.875f, 0.875f, light, overlay, 1, 1, 1, 1);
        });

        // 2. Group: "turntable_prism" (Rotates around Y-axis at center [8, 4, 8])
        poseStack.pushPose();
        poseStack.translate(0.5, 0.25, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotationAngle));

        // Brass 4-Pillar Vise Mounts
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            // NW Pillar: [-0.25, 0, -0.25] to [-0.1875, 0.5, -0.1875]
            renderTexturedBox(pose.pose(), consumer, -0.28f, 0.0f, -0.28f, -0.20f, 0.55f, -0.20f, 0.0f, 0.0f, 0.25f, 1.0f, light, overlay, 1, 1, 1, 1);
            // NE Pillar: [0.1875, 0, -0.25] to [0.25, 0.5, -0.1875]
            renderTexturedBox(pose.pose(), consumer, 0.20f, 0.0f, -0.28f, 0.28f, 0.55f, -0.20f, 0.0f, 0.0f, 0.25f, 1.0f, light, overlay, 1, 1, 1, 1);
            // SW Pillar: [-0.25, 0, 0.1875] to [-0.1875, 0.5, 0.25]
            renderTexturedBox(pose.pose(), consumer, -0.28f, 0.0f, 0.20f, -0.20f, 0.55f, 0.28f, 0.0f, 0.0f, 0.25f, 1.0f, light, overlay, 1, 1, 1, 1);
            // SE Pillar: [0.1875, 0, 0.1875] to [0.25, 0.5, 0.25]
            renderTexturedBox(pose.pose(), consumer, 0.20f, 0.0f, 0.20f, 0.28f, 0.55f, 0.28f, 0.0f, 0.0f, 0.25f, 1.0f, light, overlay, 1, 1, 1, 1);
        });

        // 45-degree Refractive Prism Crystal Core
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(CRYSTAL_TEXTURE), (pose, consumer) -> {
            renderTexturedBox(pose.pose(), consumer, -0.1875f, 0.0625f, -0.1875f, 0.1875f, 0.5f, 0.1875f, 0.0f, 0.0f, 1.0f, 1.0f, light, overlay, 0.6f, 0.9f, 1.0f, 0.85f);
        });

        poseStack.popPose();

        // 3. Render the Two 90-Degree Orthogonal Split Beams if active
        if (state.isReceiving) {
            float[] beamAngles = new float[]{state.leftSplitYaw, state.rightSplitYaw};
            float[] beamDists = new float[]{state.leftRayDistance, state.rightRayDistance};

            for (int i = 0; i < 2; i++) {
                float angle = beamAngles[i];
                float dist = beamDists[i] > 0.0f ? beamDists[i] : 16.0f;

                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(-angle + 180.0f));

                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                    renderBeamQuad(pose.pose(), consumer, dist, 0.06f, 0.5f, 0.9f, 1.0f, 0.75f, light, overlay);
                });

                poseStack.popPose();
            }
        }
    }

    private static void renderBeamQuad(Matrix4f pose, VertexConsumer consumer, float length, float radius, float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(pose, -radius, 0.0f, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, radius, 0.0f, 0.0f).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, radius, 0.0f, -length).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, -radius, 0.0f, -length).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        consumer.addVertex(pose, 0.0f, -radius, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, 0.0f, radius, 0.0f).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, 0.0f, radius, -length).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, 0.0f, -radius, -length).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }

    private static void renderTexturedBox(Matrix4f pose, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float u0, float v0, float u1, float v1, int light, int overlay, float r, float g, float b, float a) {
        // Down Face (y = minY)
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up Face (y = maxY)
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North Face (z = minZ)
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South Face (z = maxZ)
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West Face (x = minX)
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East Face (x = maxX)
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }

    public static class PrismRenderState extends BlockEntityRenderState {
        public boolean isReceiving;
        public float rotationAngle;
        public String activeStarName;
        public float leftSplitYaw;
        public float rightSplitYaw;
        public float leftRayDistance;
        public float rightRayDistance;
    }
}
