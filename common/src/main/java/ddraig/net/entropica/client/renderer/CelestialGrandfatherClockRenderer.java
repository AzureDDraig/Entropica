package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.CelestialGrandfatherClockBlock;
import ddraig.net.entropica.block.entity.CelestialGrandfatherClockBlockEntity;
import ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class CelestialGrandfatherClockRenderer implements BlockEntityRenderer<CelestialGrandfatherClockBlockEntity, CelestialGrandfatherClockRenderer.ClockRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public static class ClockRenderState extends BlockEntityRenderState {
        public boolean isLit;
        public String starName = "Astral";
        public EssenceType essence = EssenceType.ASTRAL;
        public Direction facing = Direction.NORTH;
        public DoubleBlockHalf half = DoubleBlockHalf.LOWER;
        public long dayTime = 0;
        public int animTicks = 0;
        public float partialTick = 0;
    }

    public CelestialGrandfatherClockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public ClockRenderState createRenderState() {
        return new ClockRenderState();
    }

    @Override
    public void extractRenderState(CelestialGrandfatherClockBlockEntity be, ClockRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        state.isLit = be.isFluxPowered();
        state.starName = be.getActiveStarName();
        state.essence = be.getActiveEssence();
        state.animTicks = be.getAnimationTicks();
        state.partialTick = partialTick;

        if (blockState.hasProperty(CelestialGrandfatherClockBlock.FACING)) {
            state.facing = blockState.getValue(CelestialGrandfatherClockBlock.FACING);
        }
        if (blockState.hasProperty(CelestialGrandfatherClockBlock.HALF)) {
            state.half = blockState.getValue(CelestialGrandfatherClockBlock.HALF);
        }
        if (be.getLevel() != null) {
            state.dayTime = be.getLevel().getDayTime();
        }
    }

    @Override
    public void submit(ClockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double smoothTime = (double) state.animTicks + (double) state.partialTick;

        EssenceType essence = state.essence != null ? state.essence : RefractiveAstralLensBlockEntity.resolveStarEssence(state.starName);
        int[] rgb = essence.getCurrentRGB(smoothTime);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        float glowR = Mth.lerp(0.5f, r, 1.0f);
        float glowG = Mth.lerp(0.5f, g, 1.0f);
        float glowB = Mth.lerp(0.5f, b, 1.0f);

        poseStack.pushPose();

        // Orient by block facing
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);

        if (state.half == DoubleBlockHalf.LOWER) {
            // 1. Lower Half: Swinging Brass Pendulum and Hanging Brass Weights inside glass cabinet cavity
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();

                // Hanging Brass Weights flanking the center (at Z=0.50, X=0.38 and X=0.62)
                float weightZ = 0.50f;
                // Left Weight & Chain
                renderQuadXY(mat, consumer, 0.37f, 0.70f, 0.39f, 0.95f, weightZ, 0.75f, 0.60f, 0.25f, 1.0f, light, overlay);
                renderQuadXY(mat, consumer, 0.35f, 0.40f, 0.41f, 0.70f, weightZ, 0.88f, 0.72f, 0.28f, 1.0f, light, overlay);

                // Right Weight & Chain
                renderQuadXY(mat, consumer, 0.61f, 0.78f, 0.63f, 0.95f, weightZ, 0.75f, 0.60f, 0.25f, 1.0f, light, overlay);
                renderQuadXY(mat, consumer, 0.59f, 0.48f, 0.65f, 0.78f, weightZ, 0.88f, 0.72f, 0.28f, 1.0f, light, overlay);

                // Pendulum pivot is at (0.5, 0.95, 0.45) in front of the back weights
                float swingAngle = 15.0f * (float) Math.sin(smoothTime * 0.12);
                float rad = (float) Math.toRadians(swingAngle);
                float armLength = 0.48f;

                float pivotX = 0.50f;
                float pivotY = 0.95f;
                float pivotZ = 0.44f;

                float bobX = pivotX + (float) Math.sin(rad) * armLength;
                float bobY = pivotY - (float) Math.cos(rad) * armLength;
                float bobZ = pivotZ;

                // Slender pendulum shaft
                consumer.addVertex(mat, pivotX - 0.012f, pivotY, pivotZ).setColor(0.90f, 0.75f, 0.30f, 1.0f).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                consumer.addVertex(mat, pivotX + 0.012f, pivotY, pivotZ).setColor(0.90f, 0.75f, 0.30f, 1.0f).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                consumer.addVertex(mat, bobX + 0.012f, bobY, bobZ).setColor(0.90f, 0.75f, 0.30f, 1.0f).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                consumer.addVertex(mat, bobX - 0.012f, bobY, bobZ).setColor(0.90f, 0.75f, 0.30f, 1.0f).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

                // Pendulum Bob (brass disc with glowing starlight gem core)
                float bobRadius = 0.055f;
                renderQuadXY(mat, consumer, bobX - bobRadius, bobY - bobRadius, bobX + bobRadius, bobY + bobRadius, bobZ - 0.005f, 0.92f, 0.78f, 0.32f, 1.0f, light, overlay);

                if (state.isLit) {
                    float gemSize = 0.025f;
                    renderQuadXY(mat, consumer, bobX - gemSize, bobY - gemSize, bobX + gemSize, bobY + gemSize, bobZ - 0.010f, glowR, glowG, glowB, 0.95f, light, overlay);
                }
            });
        } else {
            // 2. Upper Half: Celestial Dial & Starlight Clock Hands
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();

                // Dial center aligned precisely on the dial plate (X=0.50, Y=0.375, Z=0.170)
                float dialX = 0.50f;
                float dialY = 0.375f;
                float dialZ = 0.170f;

                // Time calculations
                long time = state.dayTime % 24000;
                float hours = (time / 1000.0f + 6.0f) % 24.0f;
                float hourAngle = (hours / 12.0f) * 360.0f;
                float minuteAngle = ((time % 1000) / 1000.0f) * 360.0f;

                // Hour Hand (length 0.12)
                renderHand(mat, consumer, dialX, dialY, dialZ, hourAngle, 0.12f, 0.022f, 0.92f, 0.78f, 0.30f, light, overlay);

                // Minute Hand (length 0.18, slender)
                renderHand(mat, consumer, dialX, dialY, dialZ - 0.005f, minuteAngle, 0.18f, 0.014f, glowR, glowG, glowB, light, overlay);

                // Glowing Starlight Dial Outer Ring when powered
                if (state.isLit) {
                    renderDialRing(mat, consumer, dialX, dialY, dialZ - 0.002f, 0.20f, glowR, glowG, glowB, 0.85f, light, overlay);
                }
            });
        }

        poseStack.popPose();
    }

    private static void renderQuadXY(Matrix4f mat, VertexConsumer consumer, float minX, float minY, float maxX, float maxY, float z, float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(mat, minX, minY, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(mat, maxX, minY, z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(mat, maxX, maxY, z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(mat, minX, maxY, z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }

    private static void renderHand(Matrix4f mat, VertexConsumer consumer, float cx, float cy, float cz, float angleDeg, float length, float width, float r, float g, float b, int light, int overlay) {
        float rad = (float) Math.toRadians(angleDeg);
        float sin = (float) Math.sin(rad);
        float cos = (float) Math.cos(rad);

        float tipX = cx + sin * length;
        float tipY = cy + cos * length;

        float perpX = -cos * (width / 2.0f);
        float perpY = sin * (width / 2.0f);

        consumer.addVertex(mat, cx - perpX, cy - perpY, cz).setColor(r, g, b, 1.0f).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(mat, cx + perpX, cy + perpY, cz).setColor(r, g, b, 1.0f).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(mat, tipX, tipY, cz).setColor(r, g, b, 1.0f).setUv(0.5f, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(mat, cx - perpX, cy - perpY, cz).setColor(r, g, b, 1.0f).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }

    private static void renderDialRing(Matrix4f mat, VertexConsumer consumer, float cx, float cy, float cz, float radius, float r, float g, float b, float a, int light, int overlay) {
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * 2 * Math.PI / segments);
            float a2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1 = cx + (float) Math.cos(a1) * radius;
            float y1 = cy + (float) Math.sin(a1) * radius;
            float x2 = cx + (float) Math.cos(a2) * radius;
            float y2 = cy + (float) Math.sin(a2) * radius;

            consumer.addVertex(mat, x1, y1, cz).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            consumer.addVertex(mat, x2, y2, cz).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            consumer.addVertex(mat, x2, y2 + 0.015f, cz).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            consumer.addVertex(mat, x1, y1 + 0.015f, cz).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        }
    }
}
