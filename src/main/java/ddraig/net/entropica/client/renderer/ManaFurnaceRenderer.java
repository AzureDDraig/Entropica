package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.ManaFurnaceBlockEntity;
import ddraig.net.entropica.config.EntropicaConfig;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ManaFurnaceRenderer implements BlockEntityRenderer<ManaFurnaceBlockEntity, ManaFurnaceRenderer.FurnaceRenderState> {

    // Using the default white texture so we can freely color the bars
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public ManaFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FurnaceRenderState createRenderState() {
        return new FurnaceRenderState();
    }

    @Override
    public void extractRenderState(ManaFurnaceBlockEntity blockEntity, FurnaceRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        renderState.manaPercent = blockEntity.getManaPercent();
        renderState.essencePercent = blockEntity.getEssencePercent();

        // UPDATED: Now uses MANA_FURNACE_MAX_ESSENCE from the config
        renderState.currentEssence = (int) Math.round(renderState.essencePercent * EntropicaConfig.MANA_FURNACE_MAX_ESSENCE.get());
        renderState.isActive = blockEntity.isActive();

        // It is actively burning if it is active, has fuel, and the mana isn't full
        renderState.isBurning = renderState.isActive && renderState.currentEssence > 0 && renderState.manaPercent < 1.0f;

        // Safely extract the block's facing direction. Defaults to NORTH if the property is missing.
        renderState.facing = blockEntity.getBlockState().getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).orElse(Direction.NORTH);
    }

    @Override
    public void submit(FurnaceRenderState furnaceRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        // Since we are drawing raw geometry and not a baked model, we get the buffer straight from Minecraft's main render buffers
        VertexConsumer vertexConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();

        // Move to center, rotate based ONLY on the block's front facing direction, move back
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-furnaceRenderState.facing.toYRot()));
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        Matrix4f matrix = poseStack.last().pose();

        // --- 1. VERTICAL ESSENCE BAR (Right Side) ---
        float startX = 0.83f, endX = 0.91f;
        float startY = 0.2f;
        float maxEndY = 0.8f; // 0.2f + 0.6f (Represents 100% capacity)

        // Draw a semi-transparent black background quad to show the total capacity
        // Rendered at Z: 1.0005f to sit just behind the active essence bar
        drawQuad(matrix, vertexConsumer, startX, endX, startY, maxEndY, 1.0005f, 0, 0, 0, 100, light, overlay);

        // Draw the active essence inside the background
        if (furnaceRenderState.essencePercent > 0) {
            float endY = 0.2f + (0.6f * furnaceRenderState.essencePercent);

            // Determine Essence Bar Color
            int barR, barG, barB;

            if (furnaceRenderState.currentEssence < 10) {
                barR = 255; barG = 50; barB = 50; // Red (Below Minimum)
            } else if (!furnaceRenderState.isActive) {
                barR = 255; barG = 255; barB = 255; // White (Enough Essence, Not Active)
            } else if (furnaceRenderState.isBurning) {
                barR = 255; barG = 150; barB = 0; // Orange (Active and Burning)
            } else {
                barR = 150; barG = 50; barB = 255; // Purple (Active but Not Actively Burning)
            }

            // Rendered slightly in front of the background quad (Z: 1.001f)
            drawQuad(matrix, vertexConsumer, startX, endX, startY, endY, 1.001f, barR, barG, barB, 200, light, overlay);
        }

        // --- 2. RADIAL MANA CIRCLE (Dead Center) ---
        if (furnaceRenderState.manaPercent > 0) {
            float centerX = 0.5f, centerY = 0.5f;
            float innerRadius = 0.10f, outerRadius = 0.20f;
            float z = 1.002f; // Rendered slightly in front to prevent Z-fighting

            int segments = 32;
            int activeSegments = (int) Math.ceil(segments * furnaceRenderState.manaPercent);

            for (int s = 0; s < activeSegments; s++) {
                float angle1 = (float) (s * Math.PI * 2 / segments);
                float angle2 = (float) ((s + 1) * Math.PI * 2 / segments);

                float x1_out = centerX + (float) Math.sin(angle1) * outerRadius;
                float y1_out = centerY + (float) Math.cos(angle1) * outerRadius;
                float x1_in = centerX + (float) Math.sin(angle1) * innerRadius;
                float y1_in = centerY + (float) Math.cos(angle1) * innerRadius;

                float x2_out = centerX + (float) Math.sin(angle2) * outerRadius;
                float y2_out = centerY + (float) Math.cos(angle2) * outerRadius;
                float x2_in = centerX + (float) Math.sin(angle2) * innerRadius;
                float y2_in = centerY + (float) Math.cos(angle2) * innerRadius;

                vertexConsumer.addVertex(matrix, x1_in, y1_in, z).setColor(50, 150, 255, 200).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                vertexConsumer.addVertex(matrix, x1_out, y1_out, z).setColor(50, 150, 255, 200).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                vertexConsumer.addVertex(matrix, x2_out, y2_out, z).setColor(50, 150, 255, 200).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                vertexConsumer.addVertex(matrix, x2_in, y2_in, z).setColor(50, 150, 255, 200).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            }
        }

        poseStack.popPose();
    }

    private void drawQuad(Matrix4f matrix, VertexConsumer vertexConsumer, float minX, float maxX, float minY, float maxY, float z, int r, int g, int b, int a, int light, int overlay) {
        vertexConsumer.addVertex(matrix, minX, minY, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, maxX, minY, z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, maxX, maxY, z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, minX, maxY, z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }

    public static class FurnaceRenderState extends BlockEntityRenderState {
        public float manaPercent;
        public float essencePercent;
        public int currentEssence;
        public boolean isActive;
        public boolean isBurning;
        public Direction facing = Direction.NORTH;
    }
}