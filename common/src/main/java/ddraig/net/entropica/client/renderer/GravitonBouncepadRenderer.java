package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.GravitonBouncepadBlock;
import ddraig.net.entropica.block.entity.GravitonBouncepadBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GravitonBouncepadRenderer implements BlockEntityRenderer<GravitonBouncepadBlockEntity, GravitonBouncepadRenderer.BouncepadRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;

    public GravitonBouncepadRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static class BouncepadRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.UP;
        public boolean isPowered = false;
        public float compression = 0.0F;
        public float time = 0.0F;
    }

    @Override
    public BouncepadRenderState createRenderState() {
        return new BouncepadRenderState();
    }

    @Override
    public void extractRenderState(GravitonBouncepadBlockEntity be, BouncepadRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.facing = be.getBlockState().getValue(GravitonBouncepadBlock.FACING);
        state.isPowered = be.getBlockState().getValue(GravitonBouncepadBlock.POWERED);
        state.compression = be.getCompression(partialTick);
        state.time = (be.getLevel() != null ? (be.getLevel().getGameTime() % 360000L) : 0) + partialTick;
    }

    @Override
    public void submit(BouncepadRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();

        // Translate to block center
        poseStack.translate(0.5, 0.5, 0.5);

        // Rotate based on facing direction
        switch (state.facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            default -> {} // UP
        }

        // Piston compression offset: moves downward by up to 0.15 blocks when compressed
        float yOffset = -0.12F - (state.compression * 0.14F);
        poseStack.translate(0.0, yOffset, 0.0);

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();

            // Membrane dimensions: 12x12 pixels centered at local origin
            float halfSize = 0.38F;
            float pulse = Mth.sin(state.time * 0.15F) * 0.08F;

            // Color: Celestial cyan (normal) or bright gold (redstone powered)
            float r = state.isPowered ? 1.0F : 0.28F + pulse;
            float g = state.isPowered ? 0.85F : 0.78F + pulse;
            float b = state.isPowered ? 0.3F : 0.98F;
            float a = 0.75F - (state.compression * 0.25F);

            // Top membrane surface
            addQuad(consumer, matrix, -halfSize, 0.0F, -halfSize, halfSize, 0.0F, halfSize, r, g, b, a);
        });

        poseStack.popPose();
    }

    private static void addQuad(
            VertexConsumer consumer, Matrix4f matrix,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float r, float g, float b, float a
    ) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, 1, 0);

        // Reverse side for double-sided visibility
        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0, -1, 0);
    }
}
