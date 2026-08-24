package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.block.entity.VisFumeVesselControllerBlockEntity;
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
import org.joml.Matrix4f;

public class VisFumeVesselControllerRenderer implements BlockEntityRenderer<VisFumeVesselControllerBlockEntity, VisFumeVesselControllerRenderer.VesselRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public VisFumeVesselControllerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public VesselRenderState createRenderState() {
        return new VesselRenderState();
    }

    @Override
    public void extractRenderState(VisFumeVesselControllerBlockEntity blockEntity, VesselRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        renderState.isFormed = blockEntity.isFormed();
        renderState.maxCapacity = blockEntity.getMaxCapacity();

        VisFumeStack fume = blockEntity.getStoredFume();
        if (renderState.isFormed && renderState.maxCapacity > 0 && !fume.isEmpty()) {
            renderState.storedAmount = fume.getAmount();
            renderState.colorInt = fume.getType().getColorInt();
        } else {
            renderState.storedAmount = 0;
            renderState.colorInt = 0xFFFFFF;
        }
    }

    @Override
    public void submit(VesselRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!renderState.isFormed || renderState.maxCapacity <= 0 || renderState.storedAmount <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));

        float fillRatio = Math.min(1.0f, Math.max(0.0f, (float) renderState.storedAmount / renderState.maxCapacity));

        int r = (renderState.colorInt >> 16) & 0xFF;
        int g = (renderState.colorInt >> 8) & 0xFF;
        int b = renderState.colorInt & 0xFF;
        int a = 200;
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f - dir.toYRot()));

            Matrix4f pose = poseStack.last().pose();

            float z = 0.501f;

            // --- ADJUSTED WIDTH (X) ---
            // Total width increased from 13 to 14 pixels so it accurately touches the opposite edge.
            float startX = -0.5f + (1.0f / 16.0f);
            float totalWidth = 14.0f / 16.0f;
            float endX = startX + (totalWidth * fillRatio);

            // --- ADJUSTED HEIGHT (Y) ---
            // Moved to the 13th and 15th pixels from the bottom (2nd and 3rd from top)
            float startY = -0.5f + (13.0f / 16.0f);
            float endY = -0.5f + (15.0f / 16.0f);

            // Draw the quad counter-clockwise
            builder.addVertex(pose, startX, startY, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            builder.addVertex(pose, endX, startY, z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            builder.addVertex(pose, endX, endY, z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            builder.addVertex(pose, startX, endY, z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    public static class VesselRenderState extends BlockEntityRenderState {
        public boolean isFormed;
        public int maxCapacity;
        public int storedAmount;
        public int colorInt;
    }
}