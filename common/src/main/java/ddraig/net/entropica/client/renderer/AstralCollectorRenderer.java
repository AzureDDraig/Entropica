package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.AstralCollectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
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
        return new AABB(-30000000.0, -30000000.0, -30000000.0, 30000000.0, 30000000.0, 30000000.0);
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
        state.storedEssence = be.getStoredEssence() != null ? be.getStoredEssence() : EssenceType.ASTRAL;

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

        EssenceType essence = state.storedEssence != null ? state.storedEssence : EssenceType.ASTRAL;
        int[] rgb = essence.getCurrentRGB(gameTime);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;
        float coreR = Mth.lerp(0.5f, r, 1.0f);
        float coreG = Mth.lerp(0.5f, g, 1.0f);
        float coreB = Mth.lerp(0.5f, b, 1.0f);

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
                renderCrossBillboard(pose.pose(), consumer, 0.5f, (float) (1.20 + bob), 0.5f, pulse, coreR, coreG, coreB, 0.75f, light, overlay);
                renderCrossBillboard(pose.pose(), consumer, 0.5f, (float) (1.20 + bob), 0.5f, pulse * 1.5f, r, g, b, 0.40f, light, overlay);

                // 2. Collimated Starlight Beam towards target
                if (state.isBeaming && state.targetPos != null && state.originPos != null) {
                    float sx = 0.5f;
                    float sy = (float) (1.20 + bob);
                    float sz = 0.5f;

                    float tx = (state.targetPos.getX() - state.originPos.getX()) + 0.5f;
                    float ty = (state.targetPos.getY() - state.originPos.getY()) + 1.25f;
                    float tz = (state.targetPos.getZ() - state.originPos.getZ()) + 0.5f;

                    renderBeam(pose.pose(), consumer, sx, sy, sz, tx, ty, tz, coreR, coreG, coreB, 0.85f, 0.05f, light, overlay);
                    renderBeam(pose.pose(), consumer, sx, sy, sz, tx, ty, tz, r, g, b, 0.45f, 0.12f, light, overlay);
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
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001f) return;

        float ux = dx / len;
        float uy = dy / len;
        float uz = dz / len;

        // Orthogonal vector 1
        float px, py, pz;
        if (Math.abs(uy) < 0.95f) {
            px = -uz;
            py = 0;
            pz = ux;
        } else {
            px = 0;
            py = -uz;
            pz = uy;
        }
        float pLen = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (pLen > 0.0001f) {
            px /= pLen;
            py /= pLen;
            pz /= pLen;
        }

        // Orthogonal vector 2 (cross u with p)
        float qx = uy * pz - uz * py;
        float qy = uz * px - ux * pz;
        float qz = ux * py - uy * px;

        int segments = 8;
        for (int i = 0; i < segments; i++) {
            double a1 = (i * 2.0 * Math.PI) / segments;
            double a2 = ((i + 1) * 2.0 * Math.PI) / segments;

            float cos1 = (float) Math.cos(a1) * thickness;
            float sin1 = (float) Math.sin(a1) * thickness;
            float cos2 = (float) Math.cos(a2) * thickness;
            float sin2 = (float) Math.sin(a2) * thickness;

            float ox1 = px * cos1 + qx * sin1;
            float oy1 = py * cos1 + qy * sin1;
            float oz1 = pz * cos1 + qz * sin1;

            float ox2 = px * cos2 + qx * sin2;
            float oy2 = py * cos2 + qy * sin2;
            float oz2 = pz * cos2 + qz * sin2;

            // Outer face
            consumer.addVertex(pose, x1 + ox1, y1 + oy1, z1 + oz1).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(ox1, oy1, oz1);
            consumer.addVertex(pose, x1 + ox2, y1 + oy2, z1 + oz2).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(ox2, oy2, oz2);
            consumer.addVertex(pose, x2 + ox2, y2 + oy2, z2 + oz2).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(ox2, oy2, oz2);
            consumer.addVertex(pose, x2 + ox1, y2 + oy1, z2 + oz1).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(ox1, oy1, oz1);

            // Inner face (reverse winding so visible from inside)
            consumer.addVertex(pose, x2 + ox1, y2 + oy1, z2 + oz1).setColor(r, g, b, a * 0.7f).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(-ox1, -oy1, -oz1);
            consumer.addVertex(pose, x2 + ox2, y2 + oy2, z2 + oz2).setColor(r, g, b, a * 0.7f).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(-ox2, -oy2, -oz2);
            consumer.addVertex(pose, x1 + ox2, y1 + oy2, z1 + oz2).setColor(r, g, b, a * 0.7f).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(-ox2, -oy2, -oz2);
            consumer.addVertex(pose, x1 + ox1, y1 + oy1, z1 + oz1).setColor(r, g, b, a * 0.7f).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(-ox1, -oy1, -oz1);
        }
    }

    public static class CollectorRenderState extends BlockEntityRenderState {
        public boolean isStructureValid = false;
        public boolean isBeaming = false;
        public float rotation = 0.0f;
        public boolean hasCrystal = false;
        public BlockPos originPos = null;
        public BlockPos targetPos = null;
        public EssenceType storedEssence = EssenceType.ASTRAL;
        public final ItemStackRenderState crystalState = new ItemStackRenderState();
    }
}
