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

        // 1. Grid Items
        for (int i = 0; i < 25; i++) {
            ItemStack stack = blockEntity.inventory.getItem(i);
            renderState.hasItem[i] = !stack.isEmpty();

            if (!stack.isEmpty()) {
                this.itemModelResolver.updateForTopItem(renderState.items[i], stack, ItemDisplayContext.FIXED, level, null, seed + i);
            } else {
                renderState.items[i].clear();
            }
        }

        // 2. Output Item (Changed to FIXED to match inputs)
        ItemStack output = blockEntity.inventory.getItem(25);
        // Do not render the item here if the Automator's mechanical hand has grabbed it!
        renderState.hasOutputItem = !output.isEmpty() && !blockEntity.isOutputBeingGrabbed;
        if (renderState.hasOutputItem) {
            this.itemModelResolver.updateForTopItem(renderState.outputItem, output, ItemDisplayContext.FIXED, level, null, seed + 25);
        } else {
            renderState.outputItem.clear();
        }

        // 3. Crafting State Data
        renderState.isCrafting = blockEntity.isCrafting;
        renderState.craftingProgress = blockEntity.craftingProgress;
        renderState.maxCraftingProgress = blockEntity.maxCraftingProgress;

        renderState.hasCraftingResult = !blockEntity.craftingResult.isEmpty();
        if (renderState.hasCraftingResult) {
            this.itemModelResolver.updateForTopItem(renderState.craftingResultItem, blockEntity.craftingResult, ItemDisplayContext.FIXED, level, null, seed + 99);
        } else {
            renderState.craftingResultItem.clear();
        }
    }

    @Override
    public void submit(SynthesizerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        long time = System.currentTimeMillis();

        float globalHeight = 1.05f;
        float targetHeight = 1.40f; // Raised target output height

        // RENDER 25 INPUT ITEMS
        for (int i = 0; i < 25; i++) {
            if (state.hasItem[i]) {
                int row = i / 5;
                int col = i % 5;

                float targetXOffset = (col - 2) * 0.18f;
                float targetZOffset = (row - 2) * 0.18f;

                float currentXOffset = targetXOffset;
                float currentZOffset = targetZOffset;
                float currentY = globalHeight;

                // Animate items sliding into the center AND rising up to the output height
                if (state.isCrafting && state.maxCraftingProgress > 0) {
                    float progress = (float) state.craftingProgress / state.maxCraftingProgress;

                    // Stop rendering individual items once the glowing sphere takes over
                    if (progress >= 0.5f) continue;

                    float moveProgress = Math.min(1.0f, progress * 2.0f);
                    currentXOffset = targetXOffset * (1.0f - moveProgress);
                    currentZOffset = targetZOffset * (1.0f - moveProgress);
                    currentY = globalHeight + ((targetHeight - globalHeight) * moveProgress);
                }

                // --- NEW: INDICATOR SQUARE ---
                // Pulse opacity gently, synchronous across the entire grid (max 25%)
                float indicatorAlpha = 0.125f + 0.125f * (float) Math.sin(time / 300.0);

                // Fade out the indicator along with the items during crafting
                if (state.isCrafting && state.maxCraftingProgress > 0) {
                    float progress = (float) state.craftingProgress / state.maxCraftingProgress;
                    if (progress >= 0.5f) {
                        indicatorAlpha = 0.0f;
                    } else {
                        indicatorAlpha *= (1.0f - (progress * 2.0f));
                    }
                }

                if (indicatorAlpha > 0.01f) {
                    poseStack.pushPose();
                    // 1.005f puts it just barely above the solid block to prevent Z-fighting
                    poseStack.translate(0.5f + targetXOffset, 1.005f, 0.5f + targetZOffset);

                    float s = 0.0625f; // 2x2 pixels (half of 0.125f)
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
                // -----------------------------

                poseStack.pushPose();
                poseStack.translate(0.5f + currentXOffset, currentY, 0.5f + currentZOffset);

                // Rotates them to spin like a coin on a table (Y), then pitches them down to lie flat (X)
                float spinRotation = (time % 4000L) / 4000.0f * 360.0f;
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRotation));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));

                poseStack.scale(0.12f, 0.12f, 0.12f);

                state.items[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }
        }

        // RENDER CRAFTING ANIMATION SPHERE & NEW ITEM FADE IN
        boolean drawSphere = false;
        float sphereScale = 0.0f;
        float sphereAlpha = 0.0f;

        if (state.isCrafting && state.maxCraftingProgress > 0) {
            float progress = (float) state.craftingProgress / state.maxCraftingProgress;

            if (progress >= 0.5f) {
                float phase2 = (progress - 0.5f) * 2.0f; // 0.0 to 1.0 mapping for the last half

                sphereScale = phase2 * 0.18f; // Much smaller sphere to encase the 0.36f item

                // Handle the fade out in the last 20% of the animation (phase2 > 0.8f)
                if (phase2 > 0.8f) {
                    sphereAlpha = 0.8f * (1.0f - ((phase2 - 0.8f) * 5.0f));
                } else {
                    sphereAlpha = 0.8f; // Constant high opacity to hide the internal swap initially
                }

                drawSphere = sphereAlpha > 0.01f;

                // Draw crafting result scaling up from the inside
                if (state.hasCraftingResult) {
                    poseStack.pushPose();
                    poseStack.translate(0.5f, targetHeight, 0.5f); // Fixed height, no rising

                    float spinRotation = (time % 4000L) / 4000.0f * 360.0f;
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRotation));
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90)); // Spin flat

                    float resultScale = phase2 * 0.36f; // 3x the input item size (0.12f * 3)
                    poseStack.scale(resultScale, resultScale, resultScale);

                    state.craftingResultItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
            }
        }
        // If finished crafting and the item is sitting there, JUST render the output item
        else if (state.hasOutputItem) {
            poseStack.pushPose();
            float bobbing = (float) Math.sin(time / 500.0) * 0.05f;
            poseStack.translate(0.5f, targetHeight + bobbing, 0.5f);

            float spinRotation = (time % 4000L) / 4000.0f * 360.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRotation));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90)); // Spin flat

            poseStack.scale(0.36f, 0.36f, 0.36f); // 3x the input item size

            state.outputItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Draw the encapsulating sphere ONLY during the crafting animation
        if (drawSphere) {
            poseStack.pushPose();
            poseStack.translate(0.5f, targetHeight, 0.5f); // Fixed target height
            poseStack.scale(sphereScale, sphereScale, sphereScale);

            // Java requires variables used in lambdas to be effectively final
            final float finalSphereAlpha = Math.max(0.0f, Math.min(1.0f, sphereAlpha));

            // Use an emissive transparent material to make it glow brightly
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                drawSphere(consumer, pose.pose(), 1.0f, 1.0f, 1.0f, 1.0f, finalSphereAlpha, light);
            });

            poseStack.popPose();
        }
    }

    // Generates a 16x16 UV Sphere programmatically using quads
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

                // Note: We swap Y and Z in addVertex because Minecraft's UP axis is Y, not Z
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

        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public boolean hasOutputItem = false;

        public boolean isCrafting = false;
        public int craftingProgress = 0;
        public int maxCraftingProgress = 0;

        public final ItemStackRenderState craftingResultItem = new ItemStackRenderState();
        public boolean hasCraftingResult = false;

        public SynthesizerRenderState() {
            for (int i = 0; i < 25; i++) {
                items[i] = new ItemStackRenderState();
            }
        }
    }
}