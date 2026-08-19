package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.OpticalReceiverPortBlockEntity;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class OpticalReceiverPortRenderer implements BlockEntityRenderer<OpticalReceiverPortBlockEntity, OpticalReceiverPortRenderer.ReceiverRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public OpticalReceiverPortRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ReceiverRenderState createRenderState() {
        return new ReceiverRenderState();
    }

    @Override
    public void extractRenderState(OpticalReceiverPortBlockEntity be, ReceiverRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isEmitting = be.isEmitting();
        state.beamDistance = be.getBeamDistance();
        if (be.getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)) {
            state.facing = be.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        } else {
            state.facing = Direction.NORTH;
        }
    }

    @Override
    public void submit(ReceiverRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        if (state.isEmitting && state.beamDistance > 0.1f) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            float yRot = switch (state.facing) {
                case SOUTH -> 180.0f;
                case WEST -> 90.0f;
                case EAST -> 270.0f;
                default -> 0.0f;
            };
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                renderBeamQuad(pose.pose(), consumer, state.beamDistance, 0.05f, 0.45f, 0.85f, 1.0f, 0.8f, light, overlay);
            });

            poseStack.popPose();
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

    public static class ReceiverRenderState extends BlockEntityRenderState {
        public boolean isEmitting = false;
        public float beamDistance = 0.0f;
        public Direction facing = Direction.NORTH;
    }
}
