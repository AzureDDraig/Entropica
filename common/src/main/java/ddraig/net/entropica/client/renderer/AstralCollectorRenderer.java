package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.AstralCollectorBlockEntity;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AstralCollectorRenderer implements BlockEntityRenderer<AstralCollectorBlockEntity, AstralCollectorRenderer.CollectorRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final ItemModelResolver itemModelResolver;

    public AstralCollectorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public AABB getRenderBoundingBox(AstralCollectorBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(64.0, 64.0, 64.0);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(AstralCollectorBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    @Override
    public CollectorRenderState createRenderState() {
        return new CollectorRenderState();
    }

    @Override
    public void extractRenderState(AstralCollectorBlockEntity be, CollectorRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isStructureValid = be.isStructureValid();
        state.isBeaming = be.isBeaming();
        state.rotation = (be.clientRotation + partialTick * 0.03f) * 57.2958f;
        state.hasCrystal = !be.getSocketedCrystal().isEmpty();
        state.originPos = be.getBlockPos();
        state.targetPos = be.getTargetPos();

        if (state.hasCrystal && be.getLevel() != null) {
            int seed = be.getBlockPos().hashCode();
            this.itemModelResolver.updateForTopItem(state.crystalState, be.getSocketedCrystal(), ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            state.crystalState.clear();
        }
    }

    @Override
    public void submit(CollectorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        double bob = Math.sin(gameTime * 0.06) * 0.04;

        // 1. Render Floating Socketed Crystal
        if (state.hasCrystal) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.20 + bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            poseStack.scale(0.85f, 0.85f, 0.85f);
            state.crystalState.submit(poseStack, collector, light, overlay, 0);
            poseStack.popPose();

            // Render Pulsating Starburst Glow Billboards
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                float pulse = 0.35f + (float) Math.sin(gameTime * 0.1) * 0.08f;
                // Core Starburst Flare Cross
                renderCrossBillboard(pose.pose(), consumer, 0.5f, (float) (1.20 + bob), 0.5f, pulse, 0.4f, 0.7f, 1.0f, 0.75f, light, overlay);
                renderCrossBillboard(pose.pose(), consumer, 0.5f, (float) (1.20 + bob), 0.5f, pulse * 1.5f, 0.6f, 0.3f, 0.9f, 0.40f, light, overlay);

                // 2. Collimated Starlight Beam towards target
                if (state.isBeaming && state.targetPos != null && state.originPos != null) {
                    float sx = 0.5f;
                    float sy = (float) (1.20 + bob);
                    float sz = 0.5f;

                    float tx = (state.targetPos.getX() - state.originPos.getX()) + 0.5f;
                    float ty = (state.targetPos.getY() - state.originPos.getY()) + 1.25f;
                    float tz = (state.targetPos.getZ() - state.originPos.getZ()) + 0.5f;

                    renderBeam(pose.pose(), consumer, sx, sy, sz, tx, ty, tz, 0.7f, 0.9f, 1.0f, 0.85f, 0.05f, light, overlay);
                    renderBeam(pose.pose(), consumer, sx, sy, sz, tx, ty, tz, 0.4f, 0.7f, 1.0f, 0.45f, 0.12f, light, overlay);
                }
            });
        }
    }

    private static void renderCrossBillboard(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float size, float r, float g, float b, float a, int light, int overlay) {
        // Horizontal Quad
        consumer.addVertex(pose, cx - size, cy, cz - size).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + size, cy, cz - size).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + size, cy, cz + size).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx - size, cy, cz + size).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // Vertical Quad XY
        consumer.addVertex(pose, cx - size, cy - size, cz).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx + size, cy - size, cz).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx + size, cy + size, cz).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, cx - size, cy + size, cz).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }

    private static void renderBeam(Matrix4f pose, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, float thickness, int light, int overlay) {
        consumer.addVertex(pose, x1 - thickness, y1, z1).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x1 + thickness, y1, z1).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x2 + thickness, y2, z2).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x2 - thickness, y2, z2).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    public static class CollectorRenderState extends BlockEntityRenderState {
        public boolean isStructureValid = false;
        public boolean isBeaming = false;
        public float rotation = 0.0f;
        public boolean hasCrystal = false;
        public BlockPos originPos = null;
        public BlockPos targetPos = null;
        public final ItemStackRenderState crystalState = new ItemStackRenderState();
    }
}
