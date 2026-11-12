package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.IlluminatedBalustradeBlock;
import ddraig.net.entropica.block.entity.IlluminatedBalustradeBlockEntity;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class IlluminatedBalustradeRenderer implements BlockEntityRenderer<IlluminatedBalustradeBlockEntity, IlluminatedBalustradeRenderer.BalustradeRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public static class BalustradeRenderState extends BlockEntityRenderState {
        public boolean isLit;
        public String starName = "Astral";
        public EssenceType essence = EssenceType.ASTRAL;
        public IlluminatedBalustradeBlock.BalustradeConnection north = IlluminatedBalustradeBlock.BalustradeConnection.NONE;
        public IlluminatedBalustradeBlock.BalustradeConnection east  = IlluminatedBalustradeBlock.BalustradeConnection.NONE;
        public IlluminatedBalustradeBlock.BalustradeConnection south = IlluminatedBalustradeBlock.BalustradeConnection.NONE;
        public IlluminatedBalustradeBlock.BalustradeConnection west  = IlluminatedBalustradeBlock.BalustradeConnection.NONE;
        public float partialTick;
    }

    public IlluminatedBalustradeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public BalustradeRenderState createRenderState() {
        return new BalustradeRenderState();
    }

    @Override
    public void extractRenderState(IlluminatedBalustradeBlockEntity be, BalustradeRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        state.isLit = be.isFluxPowered();
        state.starName = be.getActiveStarName();
        state.essence = be.getActiveEssence();
        state.partialTick = partialTick;

        state.north = blockState.hasProperty(IlluminatedBalustradeBlock.NORTH) ? blockState.getValue(IlluminatedBalustradeBlock.NORTH) : IlluminatedBalustradeBlock.BalustradeConnection.NONE;
        state.east  = blockState.hasProperty(IlluminatedBalustradeBlock.EAST)  ? blockState.getValue(IlluminatedBalustradeBlock.EAST)  : IlluminatedBalustradeBlock.BalustradeConnection.NONE;
        state.south = blockState.hasProperty(IlluminatedBalustradeBlock.SOUTH) ? blockState.getValue(IlluminatedBalustradeBlock.SOUTH) : IlluminatedBalustradeBlock.BalustradeConnection.NONE;
        state.west  = blockState.hasProperty(IlluminatedBalustradeBlock.WEST)  ? blockState.getValue(IlluminatedBalustradeBlock.WEST)  : IlluminatedBalustradeBlock.BalustradeConnection.NONE;
    }

    @Override
    public void submit(BalustradeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (!state.isLit) return;

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double smoothTime = (double) Minecraft.getInstance().level.getGameTime() + (double) state.partialTick;

        EssenceType essence = state.essence != null ? state.essence : RefractiveAstralLensBlockEntity.resolveStarEssence(state.starName);
        int[] rgb = essence.getCurrentRGB(smoothTime);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        float glowR = Mth.lerp(0.5f, r, 1.0f);
        float glowG = Mth.lerp(0.5f, g, 1.0f);
        float glowB = Mth.lerp(0.5f, b, 1.0f);
        float alpha = 0.70f + 0.15f * (float) Math.sin(smoothTime * 0.2);

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();

            // Center post core
            renderBox(mat, consumer, 0.42f, 0.20f, 0.42f, 0.58f, 0.85f, 0.58f, glowR, glowG, glowB, alpha, light, overlay);

            // North connection
            if (state.north == IlluminatedBalustradeBlock.BalustradeConnection.FLAT) {
                renderBox(mat, consumer, 0.45f, 0.25f, 0.0f, 0.55f, 0.80f, 0.42f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.north == IlluminatedBalustradeBlock.BalustradeConnection.UP) {
                renderBox(mat, consumer, 0.45f, 0.31f, 0.25f, 0.55f, 0.94f, 0.38f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.44f, 0.12f, 0.55f, 1.06f, 0.25f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.56f, 0.00f, 0.55f, 1.19f, 0.12f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.north == IlluminatedBalustradeBlock.BalustradeConnection.DOWN) {
                renderBox(mat, consumer, 0.45f, 0.06f, 0.00f, 0.55f, 0.19f, 0.12f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.12f, 0.12f, 0.55f, 0.38f, 0.25f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.19f, 0.25f, 0.55f, 0.56f, 0.38f, glowR, glowG, glowB, alpha, light, overlay);
            }

            // South connection
            if (state.south == IlluminatedBalustradeBlock.BalustradeConnection.FLAT) {
                renderBox(mat, consumer, 0.45f, 0.25f, 0.58f, 0.55f, 0.80f, 1.0f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.south == IlluminatedBalustradeBlock.BalustradeConnection.UP) {
                renderBox(mat, consumer, 0.45f, 0.31f, 0.62f, 0.55f, 0.94f, 0.75f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.44f, 0.75f, 0.55f, 1.06f, 0.88f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.56f, 0.88f, 0.55f, 1.19f, 1.00f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.south == IlluminatedBalustradeBlock.BalustradeConnection.DOWN) {
                renderBox(mat, consumer, 0.45f, 0.06f, 0.88f, 0.55f, 0.19f, 1.00f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.12f, 0.75f, 0.55f, 0.38f, 0.88f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.45f, 0.19f, 0.62f, 0.55f, 0.56f, 0.75f, glowR, glowG, glowB, alpha, light, overlay);
            }

            // West connection
            if (state.west == IlluminatedBalustradeBlock.BalustradeConnection.FLAT) {
                renderBox(mat, consumer, 0.0f, 0.25f, 0.45f, 0.42f, 0.80f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.west == IlluminatedBalustradeBlock.BalustradeConnection.UP) {
                renderBox(mat, consumer, 0.25f, 0.31f, 0.45f, 0.38f, 0.94f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.12f, 0.44f, 0.45f, 0.25f, 1.06f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.00f, 0.56f, 0.45f, 0.12f, 1.19f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.west == IlluminatedBalustradeBlock.BalustradeConnection.DOWN) {
                renderBox(mat, consumer, 0.00f, 0.06f, 0.45f, 0.12f, 0.19f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.12f, 0.12f, 0.45f, 0.25f, 0.38f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.25f, 0.19f, 0.45f, 0.38f, 0.56f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
            }

            // East connection
            if (state.east == IlluminatedBalustradeBlock.BalustradeConnection.FLAT) {
                renderBox(mat, consumer, 0.58f, 0.25f, 0.45f, 1.0f, 0.80f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.east == IlluminatedBalustradeBlock.BalustradeConnection.UP) {
                renderBox(mat, consumer, 0.62f, 0.31f, 0.45f, 0.75f, 0.94f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.75f, 0.44f, 0.45f, 0.88f, 1.06f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.88f, 0.56f, 0.45f, 1.00f, 1.19f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
            } else if (state.east == IlluminatedBalustradeBlock.BalustradeConnection.DOWN) {
                renderBox(mat, consumer, 0.88f, 0.06f, 0.45f, 1.00f, 0.19f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.75f, 0.12f, 0.45f, 0.88f, 0.38f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
                renderBox(mat, consumer, 0.62f, 0.19f, 0.45f, 0.75f, 0.56f, 0.55f, glowR, glowG, glowB, alpha, light, overlay);
            }
        });
    }

    private static void renderBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, int light, int overlay) {
        // Down
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }
}
