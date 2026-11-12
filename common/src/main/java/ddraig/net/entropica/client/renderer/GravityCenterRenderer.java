package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.GravityCenterBlockEntity;
import net.minecraft.client.gui.Font;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GravityCenterRenderer implements BlockEntityRenderer<GravityCenterBlockEntity, GravityCenterRenderer.GravityCenterRenderState> {

    private static final ResourceLocation BRASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/block/beam_splitter_prism_brass.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private final Font font;

    public GravityCenterRenderer(BlockEntityRendererProvider.Context context) {
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
    public GravityCenterRenderState createRenderState() {
        return new GravityCenterRenderState();
    }

    @Override
    public void extractRenderState(GravityCenterBlockEntity be, GravityCenterRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.ringX = be.getInterpolatedRingX(partialTick);
        state.ringY = be.getInterpolatedRingY(partialTick);
        state.ringZ = be.getInterpolatedRingZ(partialTick);
        state.coreSpin = be.getInterpolatedCoreSpin(partialTick);
        state.time = (be.getLevel() != null ? (be.getLevel().getGameTime() % 360000L) : 0) + partialTick;

        state.isActive = be.isActive();
        state.isCreative = be.isCreative();
        state.radius = be.getRadius();
        state.fuelAmount = be.getStoredAmount();
        state.safeCapacity = be.getSafeCapacity();
    }

    @Override
    public void submit(GravityCenterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 0x00F000F0; // Emissive celestial light
        int overlay = OverlayTexture.NO_OVERLAY;

        // 1. Outer Planetary Gimbal Ring
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.ringX));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ringY));

        float outerR = 0.32f;
        float ringThick = 0.022f;

        collector.submitCustomGeometry(poseStack, RenderType.entityCutoutNoCull(BRASS_TEXTURE), (pose, consumer) -> {
            renderOctagonRing(pose.pose(), consumer, outerR, ringThick, light, overlay, 0.85f, 0.75f, 0.5f);
        });

        // 2. Inner Polar Planetary Ring
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.ringZ * 1.5f));
        float innerR = 0.22f;
        collector.submitCustomGeometry(poseStack, RenderType.entityCutoutNoCull(BRASS_TEXTURE), (pose, consumer) -> {
            renderOctagonRing(pose.pose(), consumer, innerR, ringThick, light, overlay, 0.95f, 0.85f, 0.4f);
        });
        poseStack.popPose();

        // 3. Central Graviton Core
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.coreSpin));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.coreSpin * 0.7f));

        float pulse = state.isActive ? (0.10f + 0.015f * Mth.sin(state.time * 0.2f)) : 0.07f;
        float r = state.isCreative ? 0.25f : 0.95f;
        float g = state.isCreative ? 0.85f : 0.65f;
        float b = state.isCreative ? 1.0f : 0.25f;
        float a = state.isActive ? 0.95f : 0.4f;

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            renderBox(pose.pose(), consumer, -pulse, -pulse, -pulse, pulse, pulse, pulse, light, overlay, r, g, b, a);
        });
        poseStack.popPose();

        // 4. Floating Holographic Status Label
        if (this.font != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.25, 0.5);
            poseStack.mulPose(cameraRenderState.orientation);
            poseStack.scale(-0.015f, -0.015f, 0.015f);

            String title = state.isCreative
                    ? "§b[Creative Gravity Center: " + (state.isActive ? "§aActive" : "§cOffline") + "§b]"
                    : "§6[Gravity Center: " + (state.isActive ? "§aActive" : "§cNo Fuel") + "§6]";

            String sub = state.isCreative
                    ? "§7Radius: §f" + state.radius + "m"
                    : "§7Radius: §f" + state.radius + "m §8| §e" + state.fuelAmount + "mB";

            float x1 = -this.font.width(title) / 2.0f;
            float x2 = -this.font.width(sub) / 2.0f;

            MultiBufferSource.BufferSource bufferSource = net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource();
            this.font.drawInBatch(title, x1, 0, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x40000000, light);
            this.font.drawInBatch(sub, x2, 10, 0xFFE0E0E0, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x40000000, light);
            bufferSource.endBatch();

            poseStack.popPose();
        }
    }

    private static void renderOctagonRing(Matrix4f matrix, VertexConsumer consumer, float radius, float thickness, int light, int overlay, float red, float green, float blue) {
        int segments = 16;
        float angleStep = (float) (Math.PI * 2.0 / segments);

        for (int i = 0; i < segments; i++) {
            float a1 = i * angleStep;
            float a2 = (i + 1) * angleStep;

            float x1 = Mth.cos(a1) * radius;
            float z1 = Mth.sin(a1) * radius;
            float x2 = Mth.cos(a2) * radius;
            float z2 = Mth.sin(a2) * radius;

            renderBox(matrix, consumer, Math.min(x1, x2) - thickness, -thickness, Math.min(z1, z2) - thickness,
                    Math.max(x1, x2) + thickness, thickness, Math.max(z1, z2) + thickness,
                    light, overlay, red, green, blue, 1.0f);
        }
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

    public static class GravityCenterRenderState extends BlockEntityRenderState {
        public float ringX = 0.0F;
        public float ringY = 0.0F;
        public float ringZ = 0.0F;
        public float coreSpin = 0.0F;
        public float time = 0.0F;
        public boolean isActive = false;
        public boolean isCreative = false;
        public int radius = 16;
        public int fuelAmount = 0;
        public int safeCapacity = 2000;
    }
}
