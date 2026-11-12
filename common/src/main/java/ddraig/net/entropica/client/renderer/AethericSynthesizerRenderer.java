package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AethericSynthesizerRenderer implements BlockEntityRenderer<AethericSynthesizerBlockEntity, AethericSynthesizerRenderer.SynthesizerRenderState> {

    private final ItemModelResolver itemModelResolver;
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public AethericSynthesizerRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public SynthesizerRenderState createRenderState() {
        return new SynthesizerRenderState();
    }

    @Override
    public void extractRenderState(AethericSynthesizerBlockEntity blockEntity, SynthesizerRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        Level level = blockEntity.getLevel();
        int seed = (int) blockEntity.getBlockPos().asLong();

        for (int i = 0; i < 25; i++) {
            ItemStack stack = blockEntity.inventory.getItem(i);
            renderState.hasItem[i] = !stack.isEmpty();
            if (!stack.isEmpty()) this.itemModelResolver.updateForTopItem(renderState.items[i], stack, ItemDisplayContext.FIXED, level, null, seed + i);
            else renderState.items[i].clear();
        }

        // Output Items (Slots 25-28)
        renderState.activeOutputCount = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack output = blockEntity.inventory.getItem(25 + i);
            renderState.hasOutputItem[i] = !output.isEmpty() && !blockEntity.isOutputBeingGrabbed;
            if (renderState.hasOutputItem[i]) {
                this.itemModelResolver.updateForTopItem(renderState.outputItems[i], output, ItemDisplayContext.FIXED, level, null, seed + 25 + i);
                renderState.activeOutputCount++;
            } else {
                renderState.outputItems[i].clear();
            }
        }

        renderState.isCrafting = blockEntity.isCrafting;
        renderState.craftingProgress = blockEntity.craftingProgress;
        renderState.maxCraftingProgress = blockEntity.maxCraftingProgress;

        renderState.activeCraftingResultCount = 0;
        for (int i = 0; i < 4; i++) {
            if (i < blockEntity.craftingResults.size()) {
                ItemStack res = blockEntity.craftingResults.get(i);
                renderState.hasCraftingResult[i] = !res.isEmpty();
                if (renderState.hasCraftingResult[i]) {
                    this.itemModelResolver.updateForTopItem(renderState.craftingResultItems[i], res, ItemDisplayContext.FIXED, level, null, seed + 99 + i);
                    renderState.activeCraftingResultCount++;
                } else {
                    renderState.craftingResultItems[i].clear();
                }
            } else {
                renderState.hasCraftingResult[i] = false;
                renderState.craftingResultItems[i].clear();
            }
        }
    }

    @Override
    public void submit(SynthesizerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        long time = System.currentTimeMillis();

        float globalHeight = 1.05f;
        float targetHeight = 1.40f;

        for (int i = 0; i < 25; i++) {
            if (state.hasItem[i]) {
                int row = i / 5;
                int col = i % 5;

                float targetXOffset = (col - 2) * 0.18f;
                float targetZOffset = (row - 2) * 0.18f;
                float currentXOffset = targetXOffset;
                float currentZOffset = targetZOffset;
                float currentY = globalHeight;

                if (state.isCrafting && state.maxCraftingProgress > 0) {
                    float progress = (float) state.craftingProgress / state.maxCraftingProgress;
                    if (progress >= 0.5f) continue;
                    float moveProgress = Math.min(1.0f, progress * 2.0f);
                    currentXOffset = targetXOffset * (1.0f - moveProgress);
                    currentZOffset = targetZOffset * (1.0f - moveProgress);
                    currentY = globalHeight + ((targetHeight - globalHeight) * moveProgress);
                }

                float indicatorAlpha = 0.125f + 0.125f * (float) Math.sin(time / 300.0);
                if (state.isCrafting && state.maxCraftingProgress > 0) {
                    float progress = (float) state.craftingProgress / state.maxCraftingProgress;
                    if (progress >= 0.5f) indicatorAlpha = 0.0f;
                    else indicatorAlpha *= (1.0f - (progress * 2.0f));
                }

                if (indicatorAlpha > 0.01f) {
                    poseStack.pushPose();
                    poseStack.translate(0.5f + targetXOffset, 1.005f, 0.5f + targetZOffset);
                    float s = 0.0625f;
                    final float finalAlpha = indicatorAlpha;
                    collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                        Matrix4f matrix = pose.pose();
                        int overlay = OverlayTexture.NO_OVERLAY;
                        consumer.addVertex(matrix, -s, 0, -s).setColor(1.0f, 1.0f, 1.0f, finalAlpha).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                        consumer.addVertex(matrix, -s, 0, s).setColor(1.0f, 1.0f, 1.0f, finalAlpha).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                        consumer.addVertex(matrix, s, 0, s).setColor(1.0f, 1.0f, 1.0f, finalAlpha).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                        consumer.addVertex(matrix, s, 0, -s).setColor(1.0f, 1.0f, 1.0f, finalAlpha).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                    });
                    poseStack.popPose();
                }

                poseStack.pushPose();
                poseStack.translate(0.5f + currentXOffset, currentY, 0.5f + currentZOffset);
                float spinRotation = (time % 4000L) / 4000.0f * 360.0f;
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRotation));
                poseStack.scale(0.12f, 0.12f, 0.12f);
                state.items[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }

        boolean drawSphere = false;
        float sphereScale = 0.0f;
        float sphereAlpha = 0.0f;

        // Dynamic positions based on number of items
        float[][] groupOffsets = {{0, 0}, {-0.15f, 0}, {0.15f, 0}, {0, 0.15f}};

        if (state.isCrafting && state.maxCraftingProgress > 0) {
            float progress = (float) state.craftingProgress / state.maxCraftingProgress;

            if (progress >= 0.5f) {
                float phase2 = (progress - 0.5f) * 2.0f;
                sphereScale = phase2 * 0.18f;
                if (phase2 > 0.8f) sphereAlpha = 0.8f * (1.0f - ((phase2 - 0.8f) * 5.0f));
                else sphereAlpha = 0.8f;
                drawSphere = sphereAlpha > 0.01f;

                if (state.activeCraftingResultCount > 0) {
                    float resultScale = phase2 * (state.activeCraftingResultCount == 1 ? 0.36f : 0.24f);
                    for (int i = 0; i < 4; i++) {
                        if (state.hasCraftingResult[i]) {
                            poseStack.pushPose();

                            float offX = state.activeCraftingResultCount == 1 ? 0 : groupOffsets[i][0];
                            float offZ = state.activeCraftingResultCount == 1 ? 0 : groupOffsets[i][1];
                            poseStack.translate(0.5f + offX, targetHeight, 0.5f + offZ);

                            float spinRotation = (time % 4000L) / 4000.0f * 360.0f;
                            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRotation));
                            poseStack.scale(resultScale, resultScale, resultScale);
                            state.craftingResultItems[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                            poseStack.popPose();
                        }
                    }
                }
            }
        }
        else if (state.activeOutputCount > 0) {
            float resultScale = state.activeOutputCount == 1 ? 0.36f : 0.24f;
            float bobbing = (float) Math.sin(time / 500.0) * 0.05f;

            for (int i = 0; i < 4; i++) {
                if (state.hasOutputItem[i]) {
                    poseStack.pushPose();

                    float offX = state.activeOutputCount == 1 ? 0 : groupOffsets[i][0];
                    float offZ = state.activeOutputCount == 1 ? 0 : groupOffsets[i][1];
                    poseStack.translate(0.5f + offX, targetHeight + bobbing, 0.5f + offZ);

                    float spinRotation = (time % 4000L) / 4000.0f * 360.0f;
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRotation));
                    poseStack.scale(resultScale, resultScale, resultScale);

                    state.outputItems[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
            }
        }

        if (drawSphere) {
            poseStack.pushPose();
            poseStack.translate(0.5f, targetHeight, 0.5f);
            poseStack.scale(sphereScale, sphereScale, sphereScale);
            final float finalSphereAlpha = Math.max(0.0f, Math.min(1.0f, sphereAlpha));
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                drawSphere(consumer, pose.pose(), 1.0f, 1.0f, 1.0f, 1.0f, finalSphereAlpha, light);
            });
            poseStack.popPose();
        }
    }

    private void drawSphere(VertexConsumer consumer, Matrix4f matrix, float radius, float r, float g, float b, float a, int light) {
        int lats = 16;
        int longs = 16;
        int overlay = OverlayTexture.NO_OVERLAY;
        for (int i = 0; i < lats; i++) {
            float lat0 = (float) (Math.PI * (-0.5 + (double) i / lats));
            float z0 = radius * (float) Math.sin(lat0);
            float zr0 = radius * (float) Math.cos(lat0);

            float lat1 = (float) (Math.PI * (-0.5 + (double) (i + 1) / lats));
            float z1 = radius * (float) Math.sin(lat1);
            float zr1 = radius * (float) Math.cos(lat1);

            for (int j = 0; j < longs; j++) {
                float lng = (float) (2 * Math.PI * (double) j / longs);
                float x = (float) Math.cos(lng);
                float y = (float) Math.sin(lng);

                float lng1 = (float) (2 * Math.PI * (double) (j + 1) / longs);
                float x1 = (float) Math.cos(lng1);
                float y1 = (float) Math.sin(lng1);

                consumer.addVertex(matrix, x * zr0, z0, y * zr0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(x, z0/radius, y);
                consumer.addVertex(matrix, x1 * zr0, z0, y1 * zr0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(x1, z0/radius, y1);
                consumer.addVertex(matrix, x1 * zr1, z1, y1 * zr1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(x1, z1/radius, y1);
                consumer.addVertex(matrix, x * zr1, z1, y * zr1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(x, z1/radius, y);
            }
        }
    }

    public static class SynthesizerRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState[] items = new ItemStackRenderState[25];
        public final boolean[] hasItem = new boolean[25];

        public final ItemStackRenderState[] outputItems = new ItemStackRenderState[4];
        public final boolean[] hasOutputItem = new boolean[4];
        public int activeOutputCount = 0;

        public boolean isCrafting = false;
        public int craftingProgress = 0;
        public int maxCraftingProgress = 0;

        public final ItemStackRenderState[] craftingResultItems = new ItemStackRenderState[4];
        public final boolean[] hasCraftingResult = new boolean[4];
        public int activeCraftingResultCount = 0;

        public SynthesizerRenderState() {
            for (int i = 0; i < 25; i++) items[i] = new ItemStackRenderState();
            for (int i = 0; i < 4; i++) {
                outputItems[i] = new ItemStackRenderState();
                craftingResultItems[i] = new ItemStackRenderState();
            }
        }
    }
}