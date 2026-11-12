package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.AethericAutomatorBlockEntity;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AethericAutomatorRenderer implements BlockEntityRenderer<AethericAutomatorBlockEntity, AethericAutomatorRenderer.AutomatorRenderState> {

    private static final ResourceLocation IRON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/iron_block.png");
    private final ItemModelResolver itemModelResolver;

    public AethericAutomatorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public AutomatorRenderState createRenderState() {
        return new AutomatorRenderState();
    }

    @Override
    public void extractRenderState(AethericAutomatorBlockEntity blockEntity, AutomatorRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        renderState.state = blockEntity.state;

        // Ensure smooth interpolation between ticks
        renderState.animationProgress = Math.min(1.0f, (blockEntity.animationTick + partialTick) / 80.0f);

        renderState.hasTarget = blockEntity.linkedSynthesizer != null;
        if (renderState.hasTarget) {
            BlockPos worldPos = blockEntity.getBlockPos();
            BlockPos targetPos = blockEntity.linkedSynthesizer;
            // Calculate the exact 3D distance to the target block
            renderState.targetOffsetX = targetPos.getX() - worldPos.getX();
            renderState.targetOffsetY = targetPos.getY() - worldPos.getY();
            renderState.targetOffsetZ = targetPos.getZ() - worldPos.getZ();

            // Extract the held item to render in the hand if we are mid-pinch
            renderState.hasHeldItem = false;
            if (blockEntity.state == 3 && renderState.animationProgress >= 0.2f) {
                BlockEntity be = blockEntity.getLevel().getBlockEntity(blockEntity.linkedSynthesizer);
                if (be instanceof AethericSynthesizerBlockEntity synth) {
                    ItemStack output = synth.inventory.getItem(25);
                    if (!output.isEmpty()) {
                        renderState.hasHeldItem = true;
                        this.itemModelResolver.updateForTopItem(renderState.heldItemState, output, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong() + 25);
                    }
                }
            } else {
                renderState.heldItemState.clear();
            }
        }
    }

    @Override
    public void submit(AutomatorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        // Render the hand during pushing (1), waiting (2), or pulling (3)
        if (!state.hasTarget || state.state == 0) return;

        int light = 15728880;
        float t = state.animationProgress;
        long time = System.currentTimeMillis();

        // Base coordinates
        float startX = 0.5f;
        float startY = 1.0f; // Top of the upside-down hopper
        float startZ = 0.5f;

        float targetX = state.targetOffsetX + 0.5f;
        float targetZ = state.targetOffsetZ + 0.5f;
        float targetHoverY = state.targetOffsetY + 1.65f; // Safe hovering height over the synthesizer

        // Calculate base rotation to make the arm face the target block
        float dx = targetX - startX;
        float dz = targetZ - startZ;
        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));

        // State trackers
        float outProgress = 0f;
        float arcHeight = 0f;
        float currentY = startY;

        // Animation modifiers
        float indexCurl = 0f;
        float otherCurl = 0f;
        float thumbCurl = 0f;
        float pitch = 0f;
        float yawOffset = 0f; // Controls spinning the hand around

        // ==========================================
        // STATE 1: PUSH & SLAP
        // ==========================================
        if (state.state == 1) {
            if (t <= 0.4f) {
                // Smoothly reach out to the target
                outProgress = map(t, 0.0f, 0.4f, 0.0f, 1.0f);
                arcHeight = (float) Math.sin(outProgress * Math.PI) * 0.8f;
                currentY = startY + (targetHoverY - startY) * outProgress + arcHeight;

                pitch = 15f;
                indexCurl = otherCurl = thumbCurl = map(t, 0.0f, 0.4f, 0.2f, 0.8f); // Curl into dice grab
            }
            else if (t <= 0.6f) {
                // THE SLAP ZONE - Rapid downward curve and blast fingers open
                outProgress = 1.0f;
                float slapIntensity = (float) Math.sin(map(t, 0.4f, 0.6f, 0.0f, 1.0f) * Math.PI);

                currentY = targetHoverY - (slapIntensity * 0.60f); // Dips down perfectly to 1.05f
                pitch = 15f - (15f * slapIntensity); // Snap wrist flat
                indexCurl = otherCurl = thumbCurl = 0.8f - (0.8f * slapIntensity); // Snap fingers open
            }
            else {
                // Rise back up into a hover
                outProgress = 1.0f;

                pitch = map(t, 0.6f, 1.0f, 15f, -10f); // Angle slightly up to watch
                indexCurl = otherCurl = thumbCurl = map(t, 0.6f, 1.0f, 0.8f, 0.2f); // Relax hand

                // Blend a smooth bobbing animation in
                float blend = map(t, 0.6f, 1.0f, 0.0f, 1.0f);
                currentY = targetHoverY + (float) Math.sin(time / 300.0) * 0.05f * blend;
            }
        }
        // ==========================================
        // STATE 2: HOVER & WAIT
        // ==========================================
        else if (state.state == 2) {
            outProgress = 1.0f;
            pitch = -10f;
            indexCurl = otherCurl = thumbCurl = 0.2f;
            // Continuous bobbing while watching the crafting animation
            currentY = targetHoverY + (float) Math.sin(time / 300.0) * 0.05f;
        }
        // ==========================================
        // STATE 3: PULL & PINCH
        // ==========================================
        else if (state.state == 3) {
            if (t <= 0.2f) {
                // Reach down precisely for the item
                outProgress = 1.0f;
                float dipProgress = map(t, 0.0f, 0.2f, 0.0f, 1.0f);

                currentY = targetHoverY - (dipProgress * 0.20f); // Dips down to 1.45f
                pitch = map(dipProgress, 0.0f, 1.0f, -10f, 70f); // Point hand downward

                // Dynamic pinching format
                indexCurl = map(dipProgress, 0.0f, 1.0f, 0.2f, 0.9f);
                thumbCurl = map(dipProgress, 0.0f, 1.0f, 0.2f, 0.7f);
                otherCurl = map(dipProgress, 0.0f, 1.0f, 0.2f, 0.1f);
            }
            else if (t <= 0.4f) {
                // Pinch secured, pull the item back up to hover altitude
                outProgress = 1.0f;
                float riseProgress = map(t, 0.2f, 0.4f, 0.0f, 1.0f);

                currentY = targetHoverY - ((1.0f - riseProgress) * 0.20f); // Returns to 1.65f
                pitch = map(riseProgress, 0.0f, 1.0f, 70f, 30f); // Level out wrist

                indexCurl = 0.9f;
                thumbCurl = 0.7f;
                otherCurl = 0.1f;
            }
            else {
                // Return gracefully to the Automator base
                outProgress = map(t, 0.4f, 1.0f, 1.0f, 0.0f);
                arcHeight = (float) Math.sin(outProgress * Math.PI) * 0.8f;
                currentY = startY + (targetHoverY - startY) * outProgress + arcHeight;

                pitch = 30f;
                indexCurl = 0.9f;
                thumbCurl = 0.7f;
                otherCurl = 0.1f;

                // Smoothly turn 180 degrees to face the automator while returning
                float returnProgress = map(t, 0.4f, 1.0f, 0.0f, 1.0f);
                yawOffset = (float) Math.sin(Math.min(1.0f, returnProgress * 3.0f) * Math.PI / 2) * 180f;
            }
        }

        float currentX = startX + (targetX - startX) * outProgress;
        float currentZ = startZ + (targetZ - startZ) * outProgress;

        // Apply transformations
        poseStack.pushPose();
        poseStack.translate(currentX, currentY, currentZ);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw + yawOffset));

        // Render the articulated hand
        renderHand(poseStack, collector, light, indexCurl, otherCurl, thumbCurl, pitch, state);

        poseStack.popPose();
    }

    // Helper for smooth linear interpolation clipping
    private float map(float val, float inMin, float inMax, float outMin, float outMax) {
        float clampedVal = Math.max(inMin, Math.min(inMax, val));
        return outMin + (outMax - outMin) * ((clampedVal - inMin) / (inMax - inMin));
    }

    private void renderHand(PoseStack poseStack, SubmitNodeCollector collector, int light, float indexCurl, float otherCurl, float thumbCurl, float pitch, AutomatorRenderState state) {
        poseStack.pushPose();

        // 1. Wrist Pitch
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitch));

        // RENDER HELD ITEM (If actively pulling an item out)
        if (state.hasHeldItem) {
            poseStack.pushPose();
            // Moved closer into the palm and slightly upward to sit naturally between the fingers and thumb
            poseStack.translate(0.0f, -0.08f, 0.18f);

            // To match the output item's original lying-flat orientation:
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            poseStack.scale(0.36f, 0.36f, 0.36f); // Match the target Synthesizer scale

            state.heldItemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // 2. The Palm
        poseStack.pushPose();
        poseStack.translate(0, 0, 0.1f); // Shift palm forward slightly from wrist pivot
        poseStack.scale(0.3f, 0.08f, 0.3f);
        collector.submitCustomGeometry(poseStack, RenderType.entitySolid(IRON_TEXTURE), (pose, consumer) -> {
            drawCube(consumer, pose.pose(), 0.6f, 0.5f, 0.7f, 1.0f, light);
        });
        poseStack.popPose();

        // 3. The 3 Main Fingers
        float fingerSpacing = 0.10f;
        for(int i = -1; i <= 1; i++) {
            poseStack.pushPose();
            poseStack.translate(i * fingerSpacing, 0.04f, 0.25f); // Attach to the front edge of the palm

            // i = -1 is the Index Finger (next to the thumb)
            float fingerCurl = (i == -1) ? indexCurl : otherCurl;
            renderFinger(poseStack, collector, light, fingerCurl, 1.0f);

            poseStack.popPose();
        }

        // 4. The Thumb (Opposable!)
        poseStack.pushPose();
        poseStack.translate(-0.15f, 0.0f, 0.05f); // Attach to the side of the palm
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-60f)); // Point it inwards towards the fingers
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-20f)); // Angle it down slightly
        renderFinger(poseStack, collector, light, thumbCurl, 0.65f); // Thumb is shorter!
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderFinger(PoseStack poseStack, SubmitNodeCollector collector, int light, float curl, float lengthScale) {
        poseStack.pushPose();

        // Base Joint Segment
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(curl * 90f)); // Curl downwards
        poseStack.translate(0, 0, 0.1f * lengthScale); // Move to center of this segment

        poseStack.pushPose();
        poseStack.scale(0.06f, 0.06f, 0.2f * lengthScale);
        collector.submitCustomGeometry(poseStack, RenderType.entitySolid(IRON_TEXTURE), (pose, consumer) -> {
            drawCube(consumer, pose.pose(), 0.7f, 0.6f, 0.8f, 1.0f, light); // Slightly lighter tint for joints
        });
        poseStack.popPose();

        // Secondary Joint Segment
        poseStack.translate(0, 0, 0.1f * lengthScale); // Move to tip of the first segment
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(curl * 90f)); // Add additional curl
        poseStack.translate(0, 0, 0.1f * lengthScale); // Center of second segment

        poseStack.pushPose();
        poseStack.scale(0.05f, 0.05f, 0.2f * lengthScale);
        collector.submitCustomGeometry(poseStack, RenderType.entitySolid(IRON_TEXTURE), (pose, consumer) -> {
            drawCube(consumer, pose.pose(), 0.7f, 0.6f, 0.8f, 1.0f, light);
        });
        poseStack.popPose();

        poseStack.popPose();
    }

    private void drawCube(VertexConsumer consumer, Matrix4f matrix, float r, float g, float b, float a, int light) {
        float s = 0.5f;
        int o = OverlayTexture.NO_OVERLAY;

        // Up
        consumer.addVertex(matrix, -s, s, -s).setColor(r, g, b, a).setUv(0, 0).setOverlay(o).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, -s, s, s).setColor(r, g, b, a).setUv(0, 1).setOverlay(o).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, s, s, s).setColor(r, g, b, a).setUv(1, 1).setOverlay(o).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, s, s, -s).setColor(r, g, b, a).setUv(1, 0).setOverlay(o).setLight(light).setNormal(0, 1, 0);
        // Down
        consumer.addVertex(matrix, -s, -s, s).setColor(r, g, b, a).setUv(0, 0).setOverlay(o).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, -s, -s, -s).setColor(r, g, b, a).setUv(0, 1).setOverlay(o).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, s, -s, -s).setColor(r, g, b, a).setUv(1, 1).setOverlay(o).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, s, -s, s).setColor(r, g, b, a).setUv(1, 0).setOverlay(o).setLight(light).setNormal(0, -1, 0);
        // North
        consumer.addVertex(matrix, -s, s, -s).setColor(r, g, b, a).setUv(0, 0).setOverlay(o).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, s, s, -s).setColor(r, g, b, a).setUv(0, 1).setOverlay(o).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, s, -s, -s).setColor(r, g, b, a).setUv(1, 1).setOverlay(o).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(matrix, -s, -s, -s).setColor(r, g, b, a).setUv(1, 0).setOverlay(o).setLight(light).setNormal(0, 0, -1);
        // South
        consumer.addVertex(matrix, s, s, s).setColor(r, g, b, a).setUv(0, 0).setOverlay(o).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, -s, s, s).setColor(r, g, b, a).setUv(0, 1).setOverlay(o).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, -s, -s, s).setColor(r, g, b, a).setUv(1, 1).setOverlay(o).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, s, -s, s).setColor(r, g, b, a).setUv(1, 0).setOverlay(o).setLight(light).setNormal(0, 0, 1);
        // West
        consumer.addVertex(matrix, -s, s, s).setColor(r, g, b, a).setUv(0, 0).setOverlay(o).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, -s, s, -s).setColor(r, g, b, a).setUv(0, 1).setOverlay(o).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, -s, -s, -s).setColor(r, g, b, a).setUv(1, 1).setOverlay(o).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, -s, -s, s).setColor(r, g, b, a).setUv(1, 0).setOverlay(o).setLight(light).setNormal(-1, 0, 0);
        // East
        consumer.addVertex(matrix, s, s, -s).setColor(r, g, b, a).setUv(0, 0).setOverlay(o).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, s, s, s).setColor(r, g, b, a).setUv(0, 1).setOverlay(o).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, s, -s, s).setColor(r, g, b, a).setUv(1, 1).setOverlay(o).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(matrix, s, -s, -s).setColor(r, g, b, a).setUv(1, 0).setOverlay(o).setLight(light).setNormal(1, 0, 0);
    }

    public static class AutomatorRenderState extends BlockEntityRenderState {
        public int state = 0;
        public float animationProgress = 0.0f;
        public boolean hasTarget = false;
        public float targetOffsetX = 0;
        public float targetOffsetY = 0;
        public float targetOffsetZ = 0;
        public boolean hasHeldItem = false;
        public final ItemStackRenderState heldItemState = new ItemStackRenderState();
    }
}