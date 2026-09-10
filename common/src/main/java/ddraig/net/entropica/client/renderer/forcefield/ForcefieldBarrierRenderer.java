package ddraig.net.entropica.client.renderer.forcefield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.forcefield.BarrierShapeHandler;
import ddraig.net.entropica.forcefield.BarrierShapeRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedural EntityRenderer for paper-thin forcefield barriers.
 * Submits dynamic double-sided meshes with thin-film interference optics,
 * ripple shockwaves, pluggable shape handlers, Materia color tinting,
 * dormant states, and one-way directional valve chevrons.
 */
public class ForcefieldBarrierRenderer extends EntityRenderer<ForcefieldBarrierEntity, ForcefieldBarrierRenderer.BarrierRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;

    /**
     * Immutable snapshot data carrier for forcefield rendering.
     * Decoupled from live entity simulation per Minecraft 1.21.10 rendering pipeline.
     */
    public static class BarrierRenderState extends EntityRenderState {
        public BarrierShape shape = BarrierShape.PLANAR_QUAD;
        public ApexPredatorTheme theme = ApexPredatorTheme.STANDARD;
        public float width = 4.0F;
        public float height = 4.0F;
        public float radius = 5.0F;
        public float ageTicks;
        public float yRot;
        public float xRot;
        public Vec3 entityWorldPos = Vec3.ZERO;
        public final List<ForcefieldBarrierEntity.RippleImpact> ripples = new ArrayList<>();
        public boolean isActive = true;
        public boolean isOneWay = false;
        @Nullable
        public Integer colorTint = null;
    }

    public ForcefieldBarrierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BarrierRenderState createRenderState() {
        return new BarrierRenderState();
    }

    @Override
    public void extractRenderState(ForcefieldBarrierEntity entity, BarrierRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.shape = entity.getShape();
        state.theme = entity.getPredatorTheme();
        state.width = entity.getWidth();
        state.height = entity.getHeight();
        state.radius = entity.getRadius();
        state.ageTicks = entity.tickCount + partialTick;
        state.yRot = entity.getYRot();
        state.xRot = entity.getXRot();
        state.entityWorldPos = entity.position();

        state.ripples.clear();
        state.ripples.addAll(entity.getActiveRipples());

        state.isActive = entity.isActive();
        state.isOneWay = entity.isOneWay();
        state.colorTint = entity.getColorTint();
    }

    @Override
    public void submit(BarrierRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            BarrierShapeHandler handler = BarrierShapeRegistry.get(state.shape.ordinal());
            if (handler != null) {
                handler.render(matrix, consumer, state, poseStack);
            }

            if (state.isOneWay) {
                renderOneWayGlyphs(poseStack, consumer, state);
            }
        });

        poseStack.popPose();
    }

    /**
     * Renders drifting luminous starlight arrow glyphs in the direction of permitted normal (d . n).
     */
    private void renderOneWayGlyphs(
            PoseStack poseStack,
            VertexConsumer consumer,
            BarrierRenderState state
    ) {
        float halfW = state.width * 0.5F;
        float halfH = state.height * 0.5F;

        int cols = Math.max(2, Math.round(state.width / 2.0F));
        int rows = Math.max(2, Math.round(state.height / 2.0F));

        // Smooth forward drifting cycle along +Z (permitted normal)
        float cycle = (state.ageTicks * 0.035F) % 1.0F;
        float driftZ = (cycle - 0.5F) * 0.35F; // Drifts through the membrane: -0.175m to +0.175m
        float alphaFade = Mth.sin(cycle * (float) Math.PI) * 0.85F;

        if (alphaFade <= 0.05F) return;

        // Celestial cyan starlight (or custom tint)
        float glyphR = 0.45F;
        float glyphG = 0.90F;
        float glyphB = 1.00F;

        if (state.colorTint != null && state.colorTint != 0 && (state.colorTint & 0x00FFFFFF) != 0) {
            glyphR = ((state.colorTint >> 16) & 0xFF) / 255.0F;
            glyphG = ((state.colorTint >> 8) & 0xFF) / 255.0F;
            glyphB = (state.colorTint & 0xFF) / 255.0F;
        }

        ForcefieldShaderHelper.ColorResult glyphColor =
                new ForcefieldShaderHelper.ColorResult(glyphR, glyphG, glyphB, alphaFade);
        Vec3 normal = new Vec3(0, 0, 1);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        Matrix4f rotMatrix = poseStack.last().pose();

        float glyphSize = 0.22F;
        float wingSpan = 0.18F;
        float thickness = 0.05F;

        for (int r = 0; r < rows; r++) {
            float y = -halfH + ((r + 0.5F) / rows) * state.height;
            float stagger = (r % 2 == 1) ? 0.5F : 0.0F;

            for (int c = 0; c < cols; c++) {
                float x = -halfW + ((c + 0.5F + stagger) / (cols + 0.5F)) * state.width;
                if (Math.abs(x) > halfW - 0.25F) continue;

                // Chevron Left Wing
                addVertex(consumer, rotMatrix, x - wingSpan, y - glyphSize, driftZ, glyphColor, normal);
                addVertex(consumer, rotMatrix, x, y, driftZ + 0.05F, glyphColor, normal);
                addVertex(consumer, rotMatrix, x, y + thickness, driftZ + 0.05F, glyphColor, normal);
                addVertex(consumer, rotMatrix, x - wingSpan, y - glyphSize + thickness, driftZ, glyphColor, normal);

                // Chevron Right Wing
                addVertex(consumer, rotMatrix, x, y, driftZ + 0.05F, glyphColor, normal);
                addVertex(consumer, rotMatrix, x + wingSpan, y - glyphSize, driftZ, glyphColor, normal);
                addVertex(consumer, rotMatrix, x + wingSpan, y - glyphSize + thickness, driftZ, glyphColor, normal);
                addVertex(consumer, rotMatrix, x, y + thickness, driftZ + 0.05F, glyphColor, normal);

                // Reverse winding for two-sided visibility
                addVertex(consumer, rotMatrix, x - wingSpan, y - glyphSize + thickness, driftZ, glyphColor, normal.scale(-1));
                addVertex(consumer, rotMatrix, x, y + thickness, driftZ + 0.05F, glyphColor, normal.scale(-1));
                addVertex(consumer, rotMatrix, x, y, driftZ + 0.05F, glyphColor, normal.scale(-1));
                addVertex(consumer, rotMatrix, x - wingSpan, y - glyphSize, driftZ, glyphColor, normal.scale(-1));

                addVertex(consumer, rotMatrix, x, y + thickness, driftZ + 0.05F, glyphColor, normal.scale(-1));
                addVertex(consumer, rotMatrix, x + wingSpan, y - glyphSize + thickness, driftZ, glyphColor, normal.scale(-1));
                addVertex(consumer, rotMatrix, x + wingSpan, y - glyphSize, driftZ, glyphColor, normal.scale(-1));
                addVertex(consumer, rotMatrix, x, y, driftZ + 0.05F, glyphColor, normal.scale(-1));
            }
        }
        poseStack.popPose();
    }

    public static void addVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x, float y, float z,
            ForcefieldShaderHelper.ColorResult color,
            Vec3 normal
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color.r(), color.g(), color.b(), color.a())
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_LIGHT)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }
}
