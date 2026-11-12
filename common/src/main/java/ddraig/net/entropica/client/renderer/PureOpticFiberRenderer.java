package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.PureOpticFiberBlock;
import ddraig.net.entropica.block.entity.PureOpticFiberBlockEntity;
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

public class PureOpticFiberRenderer implements BlockEntityRenderer<PureOpticFiberBlockEntity, PureOpticFiberRenderer.FiberRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public static class FiberRenderState extends BlockEntityRenderState {
        public boolean north;
        public boolean east;
        public boolean south;
        public boolean west;
        public boolean up;
        public boolean down;
        public boolean isPulsing;
        public String activeStarName = "Astral";
    }

    public PureOpticFiberRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public FiberRenderState createRenderState() {
        return new FiberRenderState();
    }

    @Override
    public void extractRenderState(PureOpticFiberBlockEntity be, FiberRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        state.north = blockState.hasProperty(PureOpticFiberBlock.NORTH) && blockState.getValue(PureOpticFiberBlock.NORTH);
        state.east  = blockState.hasProperty(PureOpticFiberBlock.EAST)  && blockState.getValue(PureOpticFiberBlock.EAST);
        state.south = blockState.hasProperty(PureOpticFiberBlock.SOUTH) && blockState.getValue(PureOpticFiberBlock.SOUTH);
        state.west  = blockState.hasProperty(PureOpticFiberBlock.WEST)  && blockState.getValue(PureOpticFiberBlock.WEST);
        state.up    = blockState.hasProperty(PureOpticFiberBlock.UP)    && blockState.getValue(PureOpticFiberBlock.UP);
        state.down  = blockState.hasProperty(PureOpticFiberBlock.DOWN)  && blockState.getValue(PureOpticFiberBlock.DOWN);
        state.isPulsing = be.isPulsing();
        state.activeStarName = be.getActiveStarName();
    }

    @Override
    public void submit(FiberRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;

        EssenceType essence = RefractiveAstralLensBlockEntity.resolveStarEssence(state.activeStarName);
        int[] rgb = essence.getCurrentRGB(gameTime);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        float coreR = Mth.lerp(0.55f, r, 1.0f);
        float coreG = Mth.lerp(0.55f, g, 1.0f);
        float coreB = Mth.lerp(0.55f, b, 1.0f);

        float alpha = state.isPulsing
                ? (0.80f + 0.18f * (float) Math.sin(gameTime * 0.35))
                : (0.28f + 0.08f * (float) Math.sin(gameTime * 0.10));

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();

            // Central Core Node (2x2 pixels centered at 7/16 to 9/16)
            renderBox(mat, consumer, 0.4375f, 0.4375f, 0.4375f, 0.5625f, 0.5625f, 0.5625f, coreR, coreG, coreB, alpha, light, overlay);

            // Cardinal Arms
            if (state.north) {
                renderBox(mat, consumer, 0.4375f, 0.4375f, 0.0f, 0.5625f, 0.5625f, 0.4375f, coreR, coreG, coreB, alpha, light, overlay);
            }
            if (state.south) {
                renderBox(mat, consumer, 0.4375f, 0.4375f, 0.5625f, 0.5625f, 0.5625f, 1.0f, coreR, coreG, coreB, alpha, light, overlay);
            }
            if (state.west) {
                renderBox(mat, consumer, 0.0f, 0.4375f, 0.4375f, 0.4375f, 0.5625f, 0.5625f, coreR, coreG, coreB, alpha, light, overlay);
            }
            if (state.east) {
                renderBox(mat, consumer, 0.5625f, 0.4375f, 0.4375f, 1.0f, 0.5625f, 0.5625f, coreR, coreG, coreB, alpha, light, overlay);
            }
            if (state.down) {
                renderBox(mat, consumer, 0.4375f, 0.0f, 0.4375f, 0.5625f, 0.4375f, 0.5625f, coreR, coreG, coreB, alpha, light, overlay);
            }
            if (state.up) {
                renderBox(mat, consumer, 0.4375f, 0.5625f, 0.4375f, 0.5625f, 1.0f, 0.5625f, coreR, coreG, coreB, alpha, light, overlay);
            }
        });
    }

    private static void renderBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, int light, int overlay) {
        // Down face (y-)
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up face (y+)
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North face (z-)
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South face (z+)
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West face (x-)
        consumer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East face (x+)
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }
}
