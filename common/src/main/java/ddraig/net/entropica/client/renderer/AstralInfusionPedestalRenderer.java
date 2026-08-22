package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.AstralInfusionPedestalBlockEntity;
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

public class AstralInfusionPedestalRenderer implements BlockEntityRenderer<AstralInfusionPedestalBlockEntity, AstralInfusionPedestalRenderer.PedestalRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final ItemModelResolver itemModelResolver;

    public AstralInfusionPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public AABB getRenderBoundingBox(AstralInfusionPedestalBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(16.0);
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
    public boolean shouldRender(AstralInfusionPedestalBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(AstralInfusionPedestalBlockEntity be, PedestalRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isIrradiated = be.isIrradiated();
        state.itemRotation = be.getInterpolatedRotation(partialTick);
        state.infusionProgress = be.getInfusionProgress();
        state.hasItem = !be.getHeldItem().isEmpty();

        if (state.hasItem && be.getLevel() != null) {
            int seed = be.getBlockPos().hashCode();
            this.itemModelResolver.updateForTopItem(
                    state.itemRenderState,
                    be.getHeldItem(),
                    ItemDisplayContext.GROUND,
                    be.getLevel(),
                    null,
                    seed
            );
        } else {
            state.itemRenderState.clear();
        }
    }

    @Override
    public void submit(PedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        // 1. If irradiated, render glowing starlight ritual disc in the bowl
        if (state.isIrradiated) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                renderBowlDisc(pose.pose(), consumer, 0.5f, 1.01f, 0.5f, 0.38f, 0.4f, 0.85f, 1.0f, 0.75f, light, overlay);
            });
        }

        // 2. Render Floating Item
        if (state.hasItem) {
            poseStack.pushPose();
            double bob = Math.sin((Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) * 0.05) * 0.03;
            poseStack.translate(0.5, 1.15 + bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.itemRotation));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            state.itemRenderState.submit(poseStack, collector, light, overlay, 0);

            poseStack.popPose();
        }
    }

    private static void renderBowlDisc(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(pose, cx - radius, cy, cz - radius).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz - radius).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx + radius, cy, cz + radius).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, cx - radius, cy, cz + radius).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    public static class PedestalRenderState extends BlockEntityRenderState {
        public boolean isIrradiated = false;
        public float itemRotation = 0.0f;
        public int infusionProgress = 0;
        public boolean hasItem = false;
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    }
}
