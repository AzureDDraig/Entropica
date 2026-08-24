package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.ViscanitePistonPressBlockEntity;
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

public class ViscanitePistonPressRenderer implements BlockEntityRenderer<ViscanitePistonPressBlockEntity, ViscanitePistonPressRenderer.PressRenderState> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public ViscanitePistonPressRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public PressRenderState createRenderState() {
        return new PressRenderState();
    }

    @Override
    public void extractRenderState(ViscanitePistonPressBlockEntity be, PressRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, renderState, crumblingOverlay);
        renderState.isActive = be.isActive();
        renderState.animationTick = be.getAnimationTick();
    }

    @Override
    public void submit(PressRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(WHITE_TEXTURE));

        int light = 15728880;

        // Base frame colors (dark iron)
        float rFrame = 0.25f, gFrame = 0.25f, bFrame = 0.25f;
        // Piston shaft / head colors (bright steel / viscanite purple)
        float rShaft = 0.5f, gShaft = 0.5f, bShaft = 0.5f;
        float rHead = 0.4f, gHead = 0.2f, bHead = 0.6f; // Viscanite purple

        poseStack.pushPose();

        // 1. Render static frame cylinder (top block space)
        renderCuboid(poseStack, consumer, 0.2f, 0.5f, 0.2f, 0.8f, 1.0f, 0.8f, rFrame, gFrame, bFrame, 1.0f, light);

        // 2. Compute dynamic Y movement offset
        float yOffset = 0.0f;
        if (renderState.isActive) {
            int tick = renderState.animationTick;
            if (tick >= 0 && tick <= 10) {
                float progress = tick / 10.0f;
                yOffset = -progress * 0.8f;
            } else if (tick > 10 && tick <= 20) {
                float progress = (20 - tick) / 10.0f;
                yOffset = -progress * 0.8f;
            }
        }

        // Translate the moving piston head assembly downwards based on active progress
        poseStack.pushPose();
        poseStack.translate(0, yOffset, 0);

        // Render moving shaft (long rod from center to bottom)
        renderCuboid(poseStack, consumer, 0.45f, 0.0f, 0.45f, 0.55f, 0.8f, 0.55f, rShaft, gShaft, bShaft, 1.0f, light);

        // Render flat stamping head (bottom base plate)
        renderCuboid(poseStack, consumer, 0.1f, -0.15f, 0.1f, 0.9f, 0.0f, 0.9f, rHead, gHead, bHead, 1.0f, light);

        poseStack.popPose();
        poseStack.popPose();
    }

    private void renderCuboid(PoseStack poseStack, VertexConsumer consumer,
                              float minX, float minY, float minZ,
                              float maxX, float maxY, float maxZ,
                              float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        renderQuad(pose, consumer, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, Direction.DOWN,  r, g, b, a, light);
        renderQuad(pose, consumer, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, Direction.UP,    r, g, b, a, light);
        renderQuad(pose, consumer, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, Direction.NORTH, r, g, b, a, light);
        renderQuad(pose, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, Direction.SOUTH, r, g, b, a, light);
        renderQuad(pose, consumer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, Direction.WEST,  r, g, b, a, light);
        renderQuad(pose, consumer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, Direction.EAST,  r, g, b, a, light);
    }

    private void renderQuad(PoseStack.Pose pose, VertexConsumer consumer,
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

    public static class PressRenderState extends BlockEntityRenderState {
        public boolean isActive;
        public int animationTick;
    }
}
