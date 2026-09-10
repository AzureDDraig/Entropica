package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity;
import ddraig.net.entropica.block.entity.ResonanceStarlightFountainBlockEntity;
import net.minecraft.client.Minecraft;
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

public class ResonanceStarlightFountainRenderer implements BlockEntityRenderer<ResonanceStarlightFountainBlockEntity, ResonanceStarlightFountainRenderer.FountainRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public static class FountainRenderState extends BlockEntityRenderState {
        public boolean isLit;
        public String starName = "Astral";
        public EssenceType essence = EssenceType.ASTRAL;
        public int animTicks;
        public float partialTick;
    }

    public ResonanceStarlightFountainRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public FountainRenderState createRenderState() {
        return new FountainRenderState();
    }

    @Override
    public void extractRenderState(ResonanceStarlightFountainBlockEntity be, FountainRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isLit = be.isFluxPowered();
        state.starName = be.getActiveStarName();
        state.essence = be.getActiveEssence();
        state.animTicks = be.getAnimationTicks();
        state.partialTick = partialTick;
    }

    @Override
    public void submit(FountainRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double smoothTime = (double) state.animTicks + (double) state.partialTick;

        EssenceType essence = state.essence != null ? state.essence : RefractiveAstralLensBlockEntity.resolveStarEssence(state.starName);
        int[] rgb = essence.getCurrentRGB(smoothTime);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        // Render Starlight Pool Surface & Inner Surging Plume Core in order(1)
        collector.order(1).submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();

            // Pool surface at Y = 0.73 with gentle bobbing inside the marble rim (rim is 0.875, floor is 0.6875)
            float poolY = 0.73f + 0.015f * (float) Math.sin(smoothTime * 0.08);
            float poolAlpha = state.isLit ? 0.80f : 0.40f;

            renderQuadHorizontal(mat, consumer, 0.20f, 0.20f, 0.80f, 0.80f, poolY, r, g, b, poolAlpha, light, overlay);

            // If lit, render the continuous vertical bubbling starlight geyser / fountain plume core
            if (state.isLit) {
                float time = (float) smoothTime * 0.10f;
                float height = 0.85f + 0.08f * (float) Math.sin(time * 2.0f);
                float plumeR = Mth.lerp(0.35f, r, 1.0f);
                float plumeG = Mth.lerp(0.35f, g, 1.0f);
                float plumeB = Mth.lerp(0.35f, b, 1.0f);

                renderPlumeCore(mat, consumer, 0.5f, poolY, 0.5f, height, plumeR, plumeG, plumeB, light, overlay, smoothTime);
            }
        });

        // If lit, render the Outer Cascading Starlight Spray Veil in order(2) so it blends over background BERs and plume
        if (state.isLit) {
            float poolY = 0.73f + 0.015f * (float) Math.sin(smoothTime * 0.08);
            float time = (float) smoothTime * 0.10f;
            float height = 0.85f + 0.08f * (float) Math.sin(time * 2.0f);
            float plumeR = Mth.lerp(0.35f, r, 1.0f);
            float plumeG = Mth.lerp(0.35f, g, 1.0f);
            float plumeB = Mth.lerp(0.35f, b, 1.0f);

            collector.order(2).submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                renderSprayVeil(pose.pose(), consumer, 0.5f, poolY, 0.5f, height, plumeR, plumeG, plumeB, light, overlay, smoothTime);
            });
        }
    }

    private static void renderQuadHorizontal(Matrix4f mat, VertexConsumer consumer, float minX, float minZ, float maxX, float maxZ, float y, float r, float g, float b, float a, int light, int overlay) {
        // Top face
        consumer.addVertex(mat, minX, y, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, maxX, y, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, maxX, y, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, minX, y, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // Bottom face (in case viewed from underneath)
        consumer.addVertex(mat, minX, y, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(mat, maxX, y, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(mat, maxX, y, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(mat, minX, y, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
    }

    private static void renderPlumeCore(Matrix4f mat, VertexConsumer consumer, float cx, float baseY, float cz, float height, float r, float g, float b, int light, int overlay, double time) {
        int segments = 12;

        // Inner Surging Plume Core: 5 lofted rings forming a smooth fountain silhouette
        float[] ringY = {
            baseY,
            baseY + height * 0.35f,
            baseY + height * 0.70f,
            baseY + height * 0.94f,
            baseY + height
        };
        float pulse = (float) Math.sin(time * 0.15);
        float[] ringR = {
            0.06f,
            0.075f + 0.005f * pulse,
            0.100f + 0.010f * pulse,
            0.150f + 0.015f * pulse,
            0.020f
        };
        float[] ringAlpha = {
            0.90f,
            0.85f,
            0.80f,
            0.70f,
            0.55f
        };

        for (int band = 0; band < 4; band++) {
            float yLow = ringY[band];
            float yHigh = ringY[band + 1];
            float rLow = ringR[band];
            float rHigh = ringR[band + 1];
            float aLow = ringAlpha[band];
            float aHigh = ringAlpha[band + 1];

            for (int i = 0; i < segments; i++) {
                float a1 = (float) (i * 2 * Math.PI / segments);
                float a2 = (float) ((i + 1) * 2 * Math.PI / segments);

                float cos1 = (float) Math.cos(a1);
                float sin1 = (float) Math.sin(a1);
                float cos2 = (float) Math.cos(a2);
                float sin2 = (float) Math.sin(a2);

                float x1Low = cx + cos1 * rLow;
                float z1Low = cz + sin1 * rLow;
                float x2Low = cx + cos2 * rLow;
                float z2Low = cz + sin2 * rLow;

                float x1High = cx + cos1 * rHigh;
                float z1High = cz + sin1 * rHigh;
                float x2High = cx + cos2 * rHigh;
                float z2High = cz + sin2 * rHigh;

                // Outward quad
                consumer.addVertex(mat, x1Low, yLow, z1Low).setColor(r, g, b, aLow).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(cos1, 0, sin1);
                consumer.addVertex(mat, x2Low, yLow, z2Low).setColor(r, g, b, aLow).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(cos2, 0, sin2);
                consumer.addVertex(mat, x2High, yHigh, z2High).setColor(r, g, b, aHigh).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(cos2, 0, sin2);
                consumer.addVertex(mat, x1High, yHigh, z1High).setColor(r, g, b, aHigh).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(cos1, 0, sin1);

                // Inward quad
                consumer.addVertex(mat, x1High, yHigh, z1High).setColor(r, g, b, aHigh).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-cos1, 0, -sin1);
                consumer.addVertex(mat, x2High, yHigh, z2High).setColor(r, g, b, aHigh).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-cos2, 0, -sin2);
                consumer.addVertex(mat, x2Low, yLow, z2Low).setColor(r, g, b, aLow).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-cos2, 0, -sin2);
                consumer.addVertex(mat, x1Low, yLow, z1Low).setColor(r, g, b, aLow).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-cos1, 0, -sin1);
            }
        }
    }

    private static void renderSprayVeil(Matrix4f mat, VertexConsumer consumer, float cx, float baseY, float cz, float height, float r, float g, float b, int light, int overlay, double time) {
        int segments = 12;
        float pulse = (float) Math.sin(time * 0.15);

        // Outer Cascading Starlight Spray Veil: cascading smoothly down from crest
        float[] skirtY = {
            baseY + height * 0.94f,
            baseY + height * 0.60f,
            baseY + height * 0.20f
        };
        float[] skirtR = {
            0.150f + 0.015f * pulse,
            0.220f + 0.020f * (float) Math.sin(time * 0.12 + 1.0),
            0.280f + 0.025f * (float) Math.sin(time * 0.10 + 2.0)
        };
        float[] skirtAlpha = {
            0.60f,
            0.35f,
            0.15f
        };

        for (int band = 0; band < 2; band++) {
            float yTop = skirtY[band];
            float yBot = skirtY[band + 1];
            float rTop = skirtR[band];
            float rBot = skirtR[band + 1];
            float aTop = skirtAlpha[band];
            float aBot = skirtAlpha[band + 1];

            for (int i = 0; i < segments; i++) {
                float a1 = (float) (i * 2 * Math.PI / segments);
                float a2 = (float) ((i + 1) * 2 * Math.PI / segments);

                float cos1 = (float) Math.cos(a1);
                float sin1 = (float) Math.sin(a1);
                float cos2 = (float) Math.cos(a2);
                float sin2 = (float) Math.sin(a2);

                float x1Top = cx + cos1 * rTop;
                float z1Top = cz + sin1 * rTop;
                float x2Top = cx + cos2 * rTop;
                float z2Top = cz + sin2 * rTop;

                float x1Bot = cx + cos1 * rBot;
                float z1Bot = cz + sin1 * rBot;
                float x2Bot = cx + cos2 * rBot;
                float z2Bot = cz + sin2 * rBot;

                // Outward
                consumer.addVertex(mat, x1Bot, yBot, z1Bot).setColor(r, g, b, aBot).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(cos1, 0, sin1);
                consumer.addVertex(mat, x2Bot, yBot, z2Bot).setColor(r, g, b, aBot).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(cos2, 0, sin2);
                consumer.addVertex(mat, x2Top, yTop, z2Top).setColor(r, g, b, aTop).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(cos2, 0, sin2);
                consumer.addVertex(mat, x1Top, yTop, z1Top).setColor(r, g, b, aTop).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(cos1, 0, sin1);

                // Inward
                consumer.addVertex(mat, x1Top, yTop, z1Top).setColor(r, g, b, aTop).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-cos1, 0, -sin1);
                consumer.addVertex(mat, x2Top, yTop, z2Top).setColor(r, g, b, aTop).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-cos2, 0, -sin2);
                consumer.addVertex(mat, x2Bot, yBot, z2Bot).setColor(r, g, b, aBot).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-cos2, 0, -sin2);
                consumer.addVertex(mat, x1Bot, yBot, z1Bot).setColor(r, g, b, aBot).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-cos1, 0, -sin1);
            }
        }
    }
}

