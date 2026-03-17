package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.EidolicLatheBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class EidolicFocalPedestalRenderer implements BlockEntityRenderer<EidolicLatheBlockEntity, EidolicFocalPedestalRenderer.LatheRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public EidolicFocalPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    // Completely overrides the distance and frustum checks, forcing the rings to ALWAYS render if the chunk is loaded!
    @Override
    public boolean shouldRender(EidolicLatheBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    @Override
    public LatheRenderState createRenderState() {
        return new LatheRenderState();
    }

    @Override
    public void extractRenderState(EidolicLatheBlockEntity be, LatheRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        state.isFormed = be.isFormed();
        Level level = be.getLevel();

        // FIXED: Added a modulo to the game time before applying the partial tick.
        // This prevents massive numbers from destroying the float's decimal precision, restoring buttery-smooth animations!
        state.time = (level != null ? (level.getGameTime() % 360000L) : 0) + partialTick;

        state.pedestalOffsets.clear();

        if (state.isFormed) {
            BlockPos center = be.getBlockPos();
            for (BlockPos p : be.connectedPedestals) {
                state.pedestalOffsets.add(new Vec3(p.getX() - center.getX(), p.getY() - center.getY(), p.getZ() - center.getZ()));
            }

            // Extract the 4 items from the pedestal's inventory
            int seed = (int) be.getBlockPos().asLong();
            for (int i = 0; i < 4; i++) {
                ItemStack stack = be.inventory.getItem(i);
                state.hasItem[i] = !stack.isEmpty();
                if (!stack.isEmpty() && level != null) {
                    this.itemModelResolver.updateForTopItem(state.items[i], stack, ItemDisplayContext.FIXED, level, null, seed + i);
                } else {
                    state.items[i].clear();
                }
            }
        }
    }

    @Override
    public void submit(LatheRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (!state.isFormed) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        int light = 15728880;
        float time = state.time;

        float r = 0.2f, g = 0.8f, b = 1.0f;
        float a = 0.6f + (float) Math.sin(time * 0.05f) * 0.2f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        // ==========================================
        // 1. LOWER CORE HALO
        // ==========================================
        poseStack.pushPose();
        poseStack.translate(0, 2.2 + Math.sin(time * 0.03) * 0.1, 0);

        // Spin the Torus Clockwise
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 2.0f));
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
            drawTorus(cons, pose.pose(), 0.8f, 0.06f, 32, 12, r, g, b, a, light);
        });
        poseStack.popPose();

        // Spin the Runes Counter-Clockwise
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-time * 2.0f));
        drawTorusRunes(poseStack, bufferSource, this.font, 0.8f, 0.06f, 0.015f, light, time);
        poseStack.popPose();

        poseStack.popPose();

        // ==========================================
        // 2. UPPER CORE HALO
        // ==========================================
        poseStack.pushPose();
        poseStack.translate(0, 2.8 + Math.cos(time * 0.04) * 0.1, 0);

        // Spin the Torus Counter-Clockwise
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-time * 3.0f));
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
            drawTorus(cons, pose.pose(), 0.45f, 0.04f, 32, 12, r, g, b, a, light);
        });
        poseStack.popPose();

        // Spin the Runes Clockwise
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 3.0f));
        drawTorusRunes(poseStack, bufferSource, this.font, 0.45f, 0.04f, 0.01f, light, time);
        poseStack.popPose();

        poseStack.popPose();

        // ==========================================
        // 3. FLOATING INVENTORY ITEMS
        // ==========================================
        float itemScale = 0.4f;
        float hoverY = 1.6f + (float) Math.sin(time * 0.05f) * 0.1f;
        float itemRadius = 0.45f;

        for (int i = 0; i < 4; i++) {
            if (state.hasItem[i]) {
                poseStack.pushPose();

                // Space the 4 items evenly in a circle and spin them around the pedestal
                float angle = (i * 90.0f) + (time * 3.0f);
                float rads = (float) Math.toRadians(angle);
                float xOffset = (float) Math.cos(rads) * itemRadius;
                float zOffset = (float) Math.sin(rads) * itemRadius;

                poseStack.translate(xOffset, hoverY, zOffset);

                // Spin the item on its own axis for a nice magical effect
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 5.0f));
                poseStack.scale(itemScale, itemScale, itemScale);

                state.items[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }
        }

        // ==========================================
        // 4. ALIGNMENT RINGS
        // ==========================================
        for (Vec3 offset : state.pedestalOffsets) {
            poseStack.pushPose();

            double midX = offset.x / 2.0;
            double midY = offset.y / 2.0 + 0.8 + Math.sin(time * 0.05 + offset.x) * 0.05;
            double midZ = offset.z / 2.0;

            poseStack.translate(midX, midY, midZ);

            float yaw = (float) Math.toDegrees(Math.atan2(offset.x, offset.z));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));

            // Spin Torus
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 6.0f));
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
                drawTorus(cons, pose.pose(), 0.175f, 0.015f, 24, 8, r, g, b, a, light);
            });
            poseStack.popPose();

            // Counter-Spin Runes
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-time * 6.0f));
            drawTorusRunes(poseStack, bufferSource, this.font, 0.175f, 0.015f, 0.005f, light, time);
            poseStack.popPose();

            poseStack.popPose();
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    private void drawTorusRunes(PoseStack poseStack, MultiBufferSource bufferSource, Font font, float R, float r, float textScale, int light, float time) {
        String RUNES = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃᛇᛈᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ";
        int len = RUNES.length();

        for (int i = 0; i < len; i++) {
            poseStack.pushPose();

            float angle = (i * 360.0f / len);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-angle));

            // Added the tube radius (r) + a safe gap so the text floats perfectly outside the geometry
            poseStack.translate(R + r + (textScale * 8.0f), 0, 0);

            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
            poseStack.scale(-textScale, -textScale, textScale);

            float alphaPulse = 0.5f + (float) Math.sin(time * 0.1f + i) * 0.5f;
            int alpha = (int) (alphaPulse * 255);
            int color = (alpha << 24) | 0x33CCFF;

            String charStr = String.valueOf(RUNES.charAt(i));
            float width = font.width(charStr);

            font.drawInBatch(charStr, -width / 2.0f, -font.lineHeight / 2.0f, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);

            poseStack.popPose();
        }
    }

    private void drawTorus(VertexConsumer consumer, Matrix4f matrix, float R, float r, int segmentsR, int segmentsTube, float red, float green, float blue, float alpha, int light) {
        for (int i = 0; i < segmentsR; i++) {
            float theta1 = (float) (i * 2 * Math.PI / segmentsR);
            float theta2 = (float) ((i + 1) * 2 * Math.PI / segmentsR);

            float qR, qG, qB;
            if ((i / (segmentsR / 4)) % 2 == 0) {
                qR = 1.0f; qG = 1.0f; qB = 1.0f;
            } else {
                qR = 0.1f; qG = 0.8f; qB = 1.0f;
            }

            for (int j = 0; j < segmentsTube; j++) {
                float phi1 = (float) (j * 2 * Math.PI / segmentsTube);
                float phi2 = (float) ((j + 1) * 2 * Math.PI / segmentsTube);

                addTorusVertex(consumer, matrix, R, r, theta1, phi1, qR, qG, qB, alpha, light);
                addTorusVertex(consumer, matrix, R, r, theta2, phi1, qR, qG, qB, alpha, light);
                addTorusVertex(consumer, matrix, R, r, theta2, phi2, qR, qG, qB, alpha, light);
                addTorusVertex(consumer, matrix, R, r, theta1, phi2, qR, qG, qB, alpha, light);
            }
        }
    }

    private void addTorusVertex(VertexConsumer consumer, Matrix4f matrix, float R, float r, float theta, float phi, float red, float green, float blue, float alpha, int light) {
        float cosTheta = (float) Math.cos(theta);
        float sinTheta = (float) Math.sin(theta);
        float cosPhi = (float) Math.cos(phi);
        float sinPhi = (float) Math.sin(phi);

        float x = (R + r * cosPhi) * cosTheta;
        float y = r * sinPhi;
        float z = (R + r * cosPhi) * sinTheta;

        float nx = cosPhi * cosTheta;
        float ny = sinPhi;
        float nz = cosPhi * sinTheta;

        consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
    }

    public static class LatheRenderState extends BlockEntityRenderState {
        public boolean isFormed;
        public float time;
        public final List<Vec3> pedestalOffsets = new ArrayList<>();
        public final ItemStackRenderState[] items = new ItemStackRenderState[4];
        public final boolean[] hasItem = new boolean[4];

        public LatheRenderState() {
            for (int i = 0; i < 4; i++) {
                items[i] = new ItemStackRenderState();
            }
        }
    }
}