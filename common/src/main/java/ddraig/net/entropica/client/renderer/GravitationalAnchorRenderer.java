package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.GravitationalAnchorBlock;
import ddraig.net.entropica.block.entity.GravitationalAnchorBlockEntity;
import ddraig.net.entropica.gravity.GravityField;
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

public class GravitationalAnchorRenderer implements BlockEntityRenderer<GravitationalAnchorBlockEntity, GravitationalAnchorRenderer.AnchorRenderState> {

    private static final ResourceLocation BRASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/beam_splitter_prism_brass.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private final net.minecraft.client.gui.Font font;

    public GravitationalAnchorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
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
    public AnchorRenderState createRenderState() {
        return new AnchorRenderState();
    }

    @Override
    public void extractRenderState(GravitationalAnchorBlockEntity be, AnchorRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.gimbalX = be.getInterpolatedGimbalX(partialTick);
        state.gimbalY = be.getInterpolatedGimbalY(partialTick);
        state.coreSpin = be.getInterpolatedCoreSpin(partialTick);
        state.time = (be.getLevel() != null ? (be.getLevel().getGameTime() % 360000L) : 0) + partialTick;

        boolean powered = be.getBlockState().getValue(GravitationalAnchorBlock.POWERED);
        state.isActive = !powered;
        state.mode = be.getBlockState().getValue(GravitationalAnchorBlock.MODE);
        state.modeColor = state.mode.getColor();
        state.ageTicks = state.time;
    }

    @Override
    public void submit(AnchorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // 1. Outer Gimbal Ring (Brass, rotating around X axis)
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(state.gimbalX));
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            renderBox(pose.pose(), consumer, -0.35f, -0.03f, -0.35f, 0.35f, 0.03f, -0.31f, light, overlay, 0.9f, 0.75f, 0.3f, 1.0f);
            renderBox(pose.pose(), consumer, -0.35f, -0.03f, 0.31f, 0.35f, 0.03f, 0.35f, light, overlay, 0.9f, 0.75f, 0.3f, 1.0f);
            renderBox(pose.pose(), consumer, -0.35f, -0.03f, -0.31f, -0.31f, 0.03f, 0.31f, light, overlay, 0.9f, 0.75f, 0.3f, 1.0f);
            renderBox(pose.pose(), consumer, 0.31f, -0.03f, -0.31f, 0.35f, 0.03f, 0.31f, light, overlay, 0.9f, 0.75f, 0.3f, 1.0f);
        });
        poseStack.popPose();

        // 2. Inner Gimbal Ring (Brass, rotating around Y axis)
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.gimbalY));
        collector.submitCustomGeometry(poseStack, RenderType.entityCutout(BRASS_TEXTURE), (pose, consumer) -> {
            renderBox(pose.pose(), consumer, -0.25f, -0.25f, -0.03f, 0.25f, -0.21f, 0.03f, light, overlay, 0.8f, 0.65f, 0.25f, 1.0f);
            renderBox(pose.pose(), consumer, -0.25f, 0.21f, -0.03f, 0.25f, 0.25f, 0.03f, light, overlay, 0.8f, 0.65f, 0.25f, 1.0f);
            renderBox(pose.pose(), consumer, -0.25f, -0.21f, -0.03f, -0.21f, 0.21f, 0.03f, light, overlay, 0.8f, 0.65f, 0.25f, 1.0f);
            renderBox(pose.pose(), consumer, 0.21f, -0.21f, -0.03f, 0.25f, 0.21f, 0.03f, light, overlay, 0.8f, 0.65f, 0.25f, 1.0f);
        });
        poseStack.popPose();

        // 3. Floating Graviton Core (Emissive, mode-colored, pulsating)
        poseStack.pushPose();
        if (state.mode == GravityField.Mode.SINGULARITY) {
            float radius = state.isActive ? 0.32F : 0.18F;
            poseStack.mulPose(Axis.YP.rotationDegrees(state.coreSpin));
            GravitationalLensingRenderer.renderBlackHole(poseStack, collector, radius, state.ageTicks, light, overlay);
        } else {
            float pulse = state.isActive ? (0.12f + 0.02f * Mth.sin(state.time * 0.15f)) : 0.08f;
            poseStack.mulPose(Axis.YP.rotationDegrees(state.coreSpin));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.coreSpin * 0.7F));

            float r = ((state.modeColor >> 16) & 0xFF) / 255.0f;
            float g = ((state.modeColor >> 8) & 0xFF) / 255.0f;
            float b = (state.modeColor & 0xFF) / 255.0f;
            float a = state.isActive ? 0.9F : 0.4F;

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                renderBox(pose.pose(), consumer, -pulse, -pulse, -pulse, pulse, pulse, pulse, light, overlay, r, g, b, a);
            });
        }
        poseStack.popPose();

        // 4. Floating in-world holographic mode label above the anchor
        if (state.mode != null && this.font != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.15, 0.5);
            poseStack.mulPose(cameraRenderState.orientation);
            poseStack.scale(-0.015f, -0.015f, 0.015f);
            String label = "[" + state.mode.getDisplayName() + "]";
            float xOffset = -this.font.width(label) / 2.0f;
            net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource();
            this.font.drawInBatch(label, xOffset, 0, (state.modeColor & 0xFFFFFF) | 0xFF000000, false, poseStack.last().pose(), bufferSource, net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0x40000000, light);
            bufferSource.endBatch();
            poseStack.popPose();
        }
    }

    private static void renderBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int light, int overlay, float r, float g, float b, float a) {
        // Down face (y-)
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up face (y+)
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North face (z-)
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South face (z+)
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West face (x-)
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East face (x+)
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }

    public static class AnchorRenderState extends BlockEntityRenderState {
        public float gimbalX;
        public float gimbalY;
        public float coreSpin;
        public float time;
        public boolean isActive;
        public int modeColor;
        public GravityField.Mode mode;
        public float ageTicks;
    }
}