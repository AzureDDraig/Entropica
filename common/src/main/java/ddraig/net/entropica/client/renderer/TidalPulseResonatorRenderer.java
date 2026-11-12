package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.TidalPulseResonatorBlock;
import ddraig.net.entropica.block.entity.TidalPulseResonatorBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class TidalPulseResonatorRenderer implements BlockEntityRenderer<TidalPulseResonatorBlockEntity, TidalPulseResonatorRenderer.ResonatorRenderState> {

    private static final ResourceLocation BRASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/beam_splitter_prism_brass.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public TidalPulseResonatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public ResonatorRenderState createRenderState() {
        return new ResonatorRenderState();
    }

    @Override
    public void extractRenderState(TidalPulseResonatorBlockEntity be, ResonatorRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.prongVibration = be.getInterpolatedProngVibration(partialTick);
        state.coreScale = be.getInterpolatedCoreScale(partialTick);
        state.coreSpin = be.getInterpolatedCoreSpin(partialTick);
        state.waveRadius = be.getInterpolatedWaveRadius(partialTick);
        state.waveAlpha = be.getInterpolatedWaveAlpha(partialTick);
        state.facing = be.getBlockState().getValue(TidalPulseResonatorBlock.FACING);
        state.isActive = be.getBlockState().getValue(TidalPulseResonatorBlock.ACTIVE) && !be.getBlockState().getValue(TidalPulseResonatorBlock.POWERED);
    }

    @Override
    public void submit(ResonatorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Align with horizontal facing
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

        // --- 1. 4 Vibrating Tuning Prongs ---
        float vib = state.prongVibration;
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();
            // North-West prong
            renderProng(mat, consumer, -0.28f + vib, -0.28f, light, overlay);
            // North-East prong
            renderProng(mat, consumer, 0.24f - vib, -0.28f, light, overlay);
            // South-East prong
            renderProng(mat, consumer, 0.24f - vib, 0.24f, light, overlay);
            // South-West prong
            renderProng(mat, consumer, -0.28f + vib, 0.24f, light, overlay);
        });

        // --- 2. Floating Graviton Core (Pulsing Cobalt / Void Violet) ---
        poseStack.pushPose();
        poseStack.translate(0.0, 0.15, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.coreSpin));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.coreSpin * 0.5F));
        float s = 0.14F * state.coreScale;

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            // Tidal blue: r = 0.26, g = 0.38, b = 0.93
            renderBox(pose.pose(), consumer, -s, -s, -s, s, s, s, light, overlay, 0.26f, 0.38f, 0.93f, state.isActive ? 0.95f : 0.4f);
        });
        poseStack.popPose();

        // --- 3. Expanding Planar Shockwave Rings (during Slam phase) ---
        if (state.isActive && state.waveAlpha > 0.01F) {
            poseStack.pushPose();
            poseStack.translate(0.0, -0.3, 0.0);
            float wr = state.waveRadius;
            float wa = state.waveAlpha;

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();
                float thickness = 0.03F;
                // Square/octagonal shockwave ring
                renderBox(mat, consumer, -wr, -thickness, -wr, wr, thickness, -wr + 0.08F, light, overlay, 0.45f, 0.1f, 0.9f, wa * 0.8f);
                renderBox(mat, consumer, -wr, -thickness, wr - 0.08F, wr, thickness, wr, light, overlay, 0.45f, 0.1f, 0.9f, wa * 0.8f);
                renderBox(mat, consumer, -wr, -thickness, -wr, -wr + 0.08F, thickness, wr, light, overlay, 0.45f, 0.1f, 0.9f, wa * 0.8f);
                renderBox(mat, consumer, wr - 0.08F, -thickness, -wr, wr, thickness, wr, light, overlay, 0.45f, 0.1f, 0.9f, wa * 0.8f);
            });
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderProng(Matrix4f matrix, VertexConsumer consumer, float x, float z, int light, int overlay) {
        // Vertical tuning fork prong (width 0.04, height 0.45)
        renderBox(matrix, consumer, x, -0.1f, z, x + 0.04f, 0.35f, z + 0.04f, light, overlay, 0.85f, 0.72f, 0.32f, 1.0f);
    }

    private static void renderBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int light, int overlay, float r, float g, float b, float a) {
        // Down
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }

    public static class ResonatorRenderState extends BlockEntityRenderState {
        public float prongVibration;
        public float coreScale;
        public float coreSpin;
        public float waveRadius;
        public float waveAlpha;
        public Direction facing;
        public boolean isActive;
    }
}
