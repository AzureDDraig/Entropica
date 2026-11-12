package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.AttunedDisplayVitrineBlockEntity;
import ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AttunedDisplayVitrineRenderer implements BlockEntityRenderer<AttunedDisplayVitrineBlockEntity, AttunedDisplayVitrineRenderer.VitrineRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final ItemModelResolver itemModelResolver;

    public static class VitrineRenderState extends BlockEntityRenderState {
        public boolean isLit;
        public String starName = "Astral";
        public EssenceType essence = EssenceType.ASTRAL;
        public boolean hasItem;
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public int animTicks;
    }

    public AttunedDisplayVitrineRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public VitrineRenderState createRenderState() {
        return new VitrineRenderState();
    }

    @Override
    public void extractRenderState(AttunedDisplayVitrineBlockEntity be, VitrineRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isLit = be.isFluxPowered();
        state.starName = be.getActiveStarName();
        state.essence = be.getActiveEssence();
        state.hasItem = !be.getDisplayedItem().isEmpty();
        state.animTicks = be.getAnimationTicks();

        if (state.hasItem && be.getLevel() != null) {
            int seed = be.getBlockPos().hashCode();
            this.itemModelResolver.updateForTopItem(
                    state.itemRenderState,
                    be.getDisplayedItem(),
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
    public void submit(VitrineRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : state.animTicks;

        EssenceType essence = state.essence != null ? state.essence : RefractiveAstralLensBlockEntity.resolveStarEssence(state.starName);
        int[] rgb = essence.getCurrentRGB(gameTime);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        // 1. If lit, render collimated upward starlight spotlight beam from pedestal base
        if (state.isLit) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();
                float beamR = Mth.lerp(0.4f, r, 1.0f);
                float beamG = Mth.lerp(0.4f, g, 1.0f);
                float beamB = Mth.lerp(0.4f, b, 1.0f);
                float alpha = 0.45f + 0.12f * (float) Math.sin(gameTime * 0.2);

                renderCylinderBeam(mat, consumer, 0.5f, 0.25f, 0.5f, 0.20f, 0.75f, beamR, beamG, beamB, alpha, light, overlay);
            });
        }

        // 2. Render floating rotating artifact in center of bell jar
        if (state.hasItem) {
            poseStack.pushPose();
            double bob = Math.sin(gameTime * 0.08) * 0.04;
            poseStack.translate(0.5, 0.55 + bob, 0.5);
            float rot = (float) (gameTime * 1.5) % 360.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rot));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            state.itemRenderState.submit(poseStack, collector, light, overlay, 0);

            poseStack.popPose();
        }
    }

    private static void renderCylinderBeam(Matrix4f mat, VertexConsumer consumer, float cx, float baseY, float cz, float radius, float height, float r, float g, float b, float a, int light, int overlay) {
        int segments = 8;
        float topY = baseY + height;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1 = cx + (float) Math.cos(angle1) * radius;
            float z1 = cz + (float) Math.sin(angle1) * radius;
            float x2 = cx + (float) Math.cos(angle2) * radius;
            float z2 = cz + (float) Math.sin(angle2) * radius;

            consumer.addVertex(mat, x1, baseY, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(mat, x2, baseY, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(mat, x2, topY, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(mat, x1, topY, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        }
    }
}
