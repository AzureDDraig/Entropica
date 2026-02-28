package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EntropicCoreRenderer implements BlockEntityRenderer<EntropicCoreBlockEntity, EntropicCoreRenderer.CoreRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final Font font;

    public EntropicCoreRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public CoreRenderState createRenderState() {
        return new CoreRenderState();
    }

    @Override
    public void extractRenderState(EntropicCoreBlockEntity blockEntity, CoreRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        renderState.isFormed = blockEntity.isFormed();
        renderState.worldPosition = blockEntity.getBlockPos();

        BlockPos activeHatchPos = blockEntity.getActiveRenderPos();
        renderState.shouldRenderUI = false;

        if (renderState.isFormed && activeHatchPos != null) {
            for (Direction dir : Direction.values()) {
                if (renderState.worldPosition.relative(dir).equals(activeHatchPos)) {
                    renderState.shouldRenderUI = true;
                    renderState.hatchFacing = dir;

                    renderState.isActive = blockEntity.isActive();
                    renderState.maxEssence = blockEntity.getMaxEssencePerType();
                    renderState.maxMana = blockEntity.getMaxMana();

                    renderState.essencePool.clear();
                    renderState.manaPool.clear();
                    renderState.essencePool.putAll(blockEntity.getEssencePool());
                    renderState.manaPool.putAll(blockEntity.getManaPool());
                    break;
                }
            }
        }
    }

    @Override
    public void submit(CoreRenderState coreRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!coreRenderState.isFormed || !coreRenderState.shouldRenderUI) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        boolean isLookingAtFurnace = false;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) mc.hitResult).getBlockPos();
            if (hitPos.closerThan(coreRenderState.worldPosition, 3.0)) {
                isLookingAtFurnace = true;
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // Increase offset significantly (1.505f -> 1.51f) to clear the hatch model
        float offset = 1.51f;
        poseStack.translate(coreRenderState.hatchFacing.getStepX() * offset, 0.0f, coreRenderState.hatchFacing.getStepZ() * offset);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - coreRenderState.hatchFacing.toYRot()));

        Matrix4f matrix = poseStack.last().pose();
        float currentY = 0.85f, barHeight = 0.08f, barSpacing = 0.02f, barWidth = 1.2f, startX = -0.6f;

        // LAYER 1: ESSENCE BARS (Deepest)
        VertexConsumer quadConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        float tempY = currentY;
        for (EssenceType type : EssenceType.values()) {
            int amount = coreRenderState.essencePool.getOrDefault(type, 0);
            if (amount > 0) {
                float fillPct = Math.min(1.0f, (float) amount / Math.max(1, coreRenderState.maxEssence));
                int[] rgb = getVibrantColor(type);
                float filledWidth = barWidth * fillPct;
                // Z = 0.001f
                drawQuad(matrix, quadConsumer, startX, startX + filledWidth, tempY, tempY + barHeight, 0.001f, rgb[0], rgb[1], rgb[2], 200, light, overlay);
                if (fillPct < 1.0f) {
                    drawQuad(matrix, quadConsumer, startX + filledWidth, startX + barWidth, tempY, tempY + barHeight, 0.001f, 0, 0, 0, 100, light, overlay);
                }
                tempY += (barHeight + barSpacing);
            }
        }
        bufferSource.endBatch();

        // LAYER 2: MANA CIRCLES (Middle)
        List<EssenceType> activeManaTypes = new ArrayList<>();
        for (EssenceType type : EssenceType.values()) {
            if (coreRenderState.manaPool.getOrDefault(type, 0) > 0) activeManaTypes.add(type);
        }

        if (!activeManaTypes.isEmpty()) {
            float totalInner = 0.40f, totalOuter = 0.75f;
            float ringThickness = (totalOuter - totalInner) / activeManaTypes.size();
            VertexConsumer circleConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));

            for (int i = 0; i < activeManaTypes.size(); i++) {
                EssenceType type = activeManaTypes.get(i);
                float fillPct = Math.min(1.0f, (float) coreRenderState.manaPool.get(type) / Math.max(1, coreRenderState.maxMana));
                float innerR = totalInner + (i * ringThickness);
                float outerR = innerR + ringThickness - 0.01f;
                int[] rgb = getVibrantColor(type);

                int segments = 64;
                float targetAngle = (float) (Math.PI * 2 * fillPct);

                for (int s = 0; s < segments; s++) {
                    float a1 = (float) (s * Math.PI * 2 / segments);

                    // If the start angle of this segment is beyond our fill percentage, stop drawing
                    if (a1 >= targetAngle) break;

                    float a2 = (float) ((s + 1) * Math.PI * 2 / segments);

                    // If this segment overshoots the target, clamp the end angle to the exact target
                    if (a2 > targetAngle) a2 = targetAngle;

                    // Z = 0.005f
                    circleConsumer.addVertex(matrix, (float)Math.sin(a1)*innerR, (float)Math.cos(a1)*innerR, 0.005f).setColor(rgb[0], rgb[1], rgb[2], 180).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                    circleConsumer.addVertex(matrix, (float)Math.sin(a1)*outerR, (float)Math.cos(a1)*outerR, 0.005f).setColor(rgb[0], rgb[1], rgb[2], 180).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                    circleConsumer.addVertex(matrix, (float)Math.sin(a2)*outerR, (float)Math.cos(a2)*outerR, 0.005f).setColor(rgb[0], rgb[1], rgb[2], 180).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                    circleConsumer.addVertex(matrix, (float)Math.sin(a2)*innerR, (float)Math.cos(a2)*innerR, 0.005f).setColor(rgb[0], rgb[1], rgb[2], 180).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                }
            }
        }
        bufferSource.endBatch();

        // LAYER 3: TEXT (Foremost)
        if (isLookingAtFurnace) {
            float textY = currentY;
            for (EssenceType type : EssenceType.values()) {
                int amount = coreRenderState.essencePool.getOrDefault(type, 0);
                if (amount > 0) {
                    poseStack.pushPose();
                    // Z = -0.01f
                    poseStack.translate(0.0f, textY + (barHeight / 2.0f), -0.01f);
                    poseStack.scale(0.007f, -0.007f, 0.007f);

                    String label = type.getDisplayName() + ": " + amount + " / " + coreRenderState.maxEssence;
                    float textWidth = this.font.width(label);
                    this.font.drawInBatch(label, -textWidth / 2f, -this.font.lineHeight / 2f, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);

                    poseStack.popPose();
                    textY += (barHeight + barSpacing);
                }
            }
        }
        bufferSource.endBatch();

        poseStack.popPose();
    }

    private int[] getVibrantColor(EssenceType type) {
        int r = type.getR(), g = type.getG(), b = type.getB();
        if (type.name().equals("CHIMERA") || type.isDynamic()) {
            float t = (System.currentTimeMillis() % 4000L) / 4000.0f * (float) Math.PI * 2;
            r = (int) ((Math.sin(t) * 0.5 + 0.5) * 255);
            g = (int) ((Math.sin(t + 2.094) * 0.5 + 0.5) * 255);
            b = (int) ((Math.sin(t + 4.188) * 0.5 + 0.5) * 255);
        }
        return new int[]{r, g, b};
    }

    private void drawQuad(Matrix4f m, VertexConsumer v, float x1, float x2, float y1, float y2, float z, int r, int g, int b, int a, int l, int o) {
        v.addVertex(m, x1, y1, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(o).setLight(l).setNormal(0, 0, 1);
        v.addVertex(m, x2, y1, z).setColor(r, g, b, a).setUv(1, 0).setOverlay(o).setLight(l).setNormal(0, 0, 1);
        v.addVertex(m, x2, y2, z).setColor(r, g, b, a).setUv(1, 1).setOverlay(o).setLight(l).setNormal(0, 0, 1);
        v.addVertex(m, x1, y2, z).setColor(r, g, b, a).setUv(0, 1).setOverlay(o).setLight(l).setNormal(0, 0, 1);
    }

    public static class CoreRenderState extends BlockEntityRenderState {
        public boolean isFormed, isActive, shouldRenderUI;
        public BlockPos worldPosition;
        public int maxEssence, maxMana;
        public final Map<EssenceType, Integer> essencePool = new EnumMap<>(EssenceType.class);
        public final Map<EssenceType, Integer> manaPool = new EnumMap<>(EssenceType.class);
        public Direction hatchFacing = Direction.NORTH;
    }
}