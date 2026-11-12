package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.RubberLogBlock;
import ddraig.net.entropica.block.entity.RubberLogBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RubberLogBlockEntityRenderer implements BlockEntityRenderer<RubberLogBlockEntity, RubberLogBlockEntityRenderer.RubberLogRenderState> {

    private static final ResourceLocation RESIN_SPOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/rubber_log_resin_spot.png");

    public RubberLogBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public RubberLogRenderState createRenderState() {
        return new RubberLogRenderState();
    }

    @Override
    public void extractRenderState(RubberLogBlockEntity be, RubberLogRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        state.hasResin = blockState.hasProperty(RubberLogBlock.HAS_RESIN) && blockState.getValue(RubberLogBlock.HAS_RESIN);
        state.facing = blockState.hasProperty(RubberLogBlock.RESIN_FACING) ? blockState.getValue(RubberLogBlock.RESIN_FACING) : Direction.NORTH;
        state.spotX = be.getSpotOffsetX();
        state.spotY = be.getSpotOffsetY();
    }

    @Override
    public void submit(RubberLogRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!state.hasResin) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(RESIN_SPOT_TEXTURE));

        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();

        int light = 15728880; // Bright full light for amber resin
        Direction facing = state.facing;

        // Spot Quad Dimensions (0.35 x 0.35 block units)
        float size = 0.35f;
        float rx = Math.max(0.05f, Math.min(0.60f, state.spotX - size * 0.5f));
        float ry = Math.max(0.05f, Math.min(0.60f, state.spotY - size * 0.5f));

        float offset = 1.002f; // Slight offset to prevent z-fighting with log bark

        switch (facing) {
            case NORTH -> renderQuad(pose, consumer, rx + size, ry, -0.002f, rx, ry, -0.002f, rx, ry + size, -0.002f, rx + size, ry + size, -0.002f, Direction.NORTH, light);
            case SOUTH -> renderQuad(pose, consumer, rx, ry, offset, rx + size, ry, offset, rx + size, ry + size, offset, rx, ry + size, offset, Direction.SOUTH, light);
            case WEST  -> renderQuad(pose, consumer, -0.002f, ry, rx, -0.002f, ry, rx + size, -0.002f, ry + size, rx + size, -0.002f, ry + size, rx, Direction.WEST, light);
            case EAST  -> renderQuad(pose, consumer, offset, ry, rx + size, offset, ry, rx, offset, ry + size, rx, offset, ry + size, rx + size, Direction.EAST, light);
            default -> {}
        }

        poseStack.popPose();
    }

    private void renderQuad(PoseStack.Pose pose, VertexConsumer consumer,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4,
                            Direction normal, int light) {
        float nx = normal.getStepX();
        float ny = normal.getStepY();
        float nz = normal.getStepZ();

        consumer.addVertex(pose, x1, y1, z1).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    public static class RubberLogRenderState extends BlockEntityRenderState {
        public boolean hasResin;
        public Direction facing;
        public float spotX;
        public float spotY;
    }
}
