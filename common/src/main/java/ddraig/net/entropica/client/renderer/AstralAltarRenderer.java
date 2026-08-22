package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.AstralAltarCoreBlockEntity;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AstralAltarRenderer implements BlockEntityRenderer<AstralAltarCoreBlockEntity, AstralAltarRenderer.AltarRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final ItemModelResolver itemModelResolver;

    public AstralAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public AABB getRenderBoundingBox(AstralAltarCoreBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(4.5, 6.5, 4.5);
    }

    @Override
    public AltarRenderState createRenderState() {
        return new AltarRenderState();
    }

    @Override
    public void extractRenderState(AstralAltarCoreBlockEntity be, AltarRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isStructureValid = be.isStructureValid();
        state.isStarlightActive = be.isStarlightActive();
        state.rotation = (be.clientRotation + partialTick * 0.02f) * 57.2958f; // rad to deg

        state.hasHeldItem = !be.getHeldItem().isEmpty();
        if (state.hasHeldItem && be.getLevel() != null) {
            int seed = be.getBlockPos().hashCode();
            this.itemModelResolver.updateForTopItem(state.heldItemState, be.getHeldItem(), ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            state.heldItemState.clear();
        }

        state.hasStarChart = !be.getStarChart().isEmpty();
        if (state.hasStarChart && be.getLevel() != null) {
            int seed = be.getBlockPos().hashCode() + 1;
            this.itemModelResolver.updateForTopItem(state.starChartState, be.getStarChart(), ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            state.starChartState.clear();
        }
    }

    @Override
    public void submit(AltarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        double bob = Math.sin(gameTime * 0.05) * 0.05;

        // 1. Runic / Starlight Circle & Focal Beams on Altar Core Surface when formed
        if (state.isStructureValid) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                float r = state.isStarlightActive ? 0.3f : 0.2f;
                float g = state.isStarlightActive ? 0.8f : 0.5f;
                float b = state.isStarlightActive ? 1.0f : 0.7f;
                float a = state.isStarlightActive ? 0.85f : 0.4f;
                renderCircleDisc(pose.pose(), consumer, 0.5f, 1.01f, 0.5f, 0.45f, r, g, b, a, light, overlay);

                if (state.isStarlightActive) {
                    // Overhead Starlight Focal Ray (From Focal Lens Mount at Y=+5 down to Altar Core)
                    renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, 0.5f, 1.25f, 0.5f, 0.8f, 0.95f, 1.0f, 0.80f, light, overlay);
                }
            });
        }

        // 2. Render Floating Held Item
        if (state.hasHeldItem) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.25 + bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            poseStack.scale(0.6f, 0.6f, 0.6f);
            state.heldItemState.submit(poseStack, collector, light, overlay, 0);
            poseStack.popPose();
        }

        // 3. Render Floating Star Chart
        if (state.hasStarChart) {
            poseStack.pushPose();
            poseStack.translate(0.5, (state.hasHeldItem ? 1.7 : 1.25) - bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.rotation * 0.8f));
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0f));
            poseStack.scale(0.5f, 0.5f, 0.5f);
            state.starChartState.submit(poseStack, collector, light, overlay, 0);
            poseStack.popPose();
        }
    }

    private static void renderCircleDisc(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(pose, cx - radius, cy, cz - radius).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz - radius).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz + radius).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx - radius, cy, cz + radius).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    private static void renderBeamLine(Matrix4f pose, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, int light, int overlay) {
        float thickness = 0.04f;
        consumer.addVertex(pose, x1 - thickness, y1, z1).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x1 + thickness, y1, z1).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x2 + thickness, y2, z2).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x2 - thickness, y2, z2).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    public static class AltarRenderState extends BlockEntityRenderState {
        public boolean isStructureValid = false;
        public boolean isStarlightActive = false;
        public float rotation = 0.0f;
        public boolean hasHeldItem = false;
        public boolean hasStarChart = false;
        public final ItemStackRenderState heldItemState = new ItemStackRenderState();
        public final ItemStackRenderState starChartState = new ItemStackRenderState();
    }
}
