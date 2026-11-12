package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.ScribingControllerBlock;
import ddraig.net.entropica.block.entity.ScribingControllerBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ScribingControllerRenderer implements BlockEntityRenderer<ScribingControllerBlockEntity, ScribingControllerRenderer.ControllerRenderState> {
    private static final ResourceLocation GAS_ANIM_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/misc/fume_gas_anim.png");

    public ScribingControllerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public ControllerRenderState createRenderState() {
        return new ControllerRenderState();
    }

    @Override
    public void extractRenderState(ScribingControllerBlockEntity be, ControllerRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, renderState, crumblingOverlay);
        renderState.isFormed = be.isFormed();
        renderState.facing = be.getBlockState().getValue(ScribingControllerBlock.FACING);
        renderState.hasMateria = !be.getStoredMateria().isEmpty();
        if (renderState.hasMateria) {
            renderState.colorInt = be.getStoredMateria().getType().getColorInt();
            renderState.fillRatio = (float) be.getStoredMateria().getAmount() / be.getSafeCapacity();
        }
        renderState.time = (be.getLevel() != null ? be.getLevel().getGameTime() : 0) + partialTick;
    }

    @Override
    public void submit(ControllerRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!renderState.isFormed || !renderState.hasMateria) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(GAS_ANIM_TEXTURE));

        int colorInt = renderState.colorInt;
        float r = ((colorInt >> 16) & 0xFF) / 255.0f;
        float g = ((colorInt >> 8)  & 0xFF) / 255.0f;
        float b = (colorInt & 0xFF)          / 255.0f;
        float alpha = 0.2f + (0.5f * Math.min(1.0f, renderState.fillRatio));
        int light = 15728880;

        poseStack.pushPose();

        // Translate to the middle layer cavity (1 block behind the controller)
        Direction facing = renderState.facing;
        Direction back = facing.getOpposite();
        poseStack.translate(back.getStepX(), back.getStepY(), back.getStepZ());

        // Center rotation point inside the cavity
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // Multiple overlapping swirling gas sheets
        int layers = 3;
        for (int i = 0; i < layers; i++) {
            poseStack.pushPose();
            
            // Differentiate rotation direction and speed per layer
            float speedMultiplier = 1.0f + (i * 0.5f);
            float angle = (renderState.time * 2.0f * speedMultiplier * (i % 2 == 0 ? 1 : -1)) % 360.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle * 0.2f));

            // Dynamic scale pulsing
            float pulseScale = 0.7f + 0.15f * (float) Math.sin(renderState.time * 0.1f + i);
            poseStack.scale(pulseScale, pulseScale, pulseScale);

            // Render a rotating gas cube
            renderCube(poseStack, consumer, r, g, b, alpha, light);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderCube(PoseStack poseStack, VertexConsumer consumer, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        float size = 0.35f;

        // Draw 6 faces of the gas cloud cube
        renderFace(pose, consumer, -size, -size, size, size, -size, size, size, size, size, -size, size, size, Direction.SOUTH, r, g, b, a, light);
        renderFace(pose, consumer, size, -size, -size, -size, -size, -size, -size, size, -size, size, size, -size, Direction.NORTH, r, g, b, a, light);
        renderFace(pose, consumer, -size, size, size, size, size, size, size, size, -size, -size, size, -size, Direction.UP, r, g, b, a, light);
        renderFace(pose, consumer, -size, -size, -size, size, -size, -size, size, -size, size, -size, -size, size, Direction.DOWN, r, g, b, a, light);
        renderFace(pose, consumer, -size, -size, -size, -size, -size, size, -size, size, size, -size, size, -size, Direction.WEST, r, g, b, a, light);
        renderFace(pose, consumer, size, -size, size, size, -size, -size, size, size, -size, size, size, size, Direction.EAST, r, g, b, a, light);
    }

    private void renderFace(PoseStack.Pose pose, VertexConsumer consumer,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float x3, float y3, float z3, float x4, float y4, float z4,
                            Direction normal, float r, float g, float b, float a, int light) {
        float nx = normal.getStepX();
        float ny = normal.getStepY();
        float nz = normal.getStepZ();

        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    public static class ControllerRenderState extends BlockEntityRenderState {
        public boolean isFormed;
        public Direction facing;
        public boolean hasMateria;
        public int colorInt;
        public float fillRatio;
        public float time;
    }
}
