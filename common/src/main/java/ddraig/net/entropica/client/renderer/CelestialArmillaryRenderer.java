package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.block.entity.CelestialArmillaryControllerBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class CelestialArmillaryRenderer implements BlockEntityRenderer<CelestialArmillaryControllerBlockEntity, CelestialArmillaryRenderer.ArmillaryRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public CelestialArmillaryRenderer(BlockEntityRendererProvider.Context context) {
    }

    public AABB getRenderBoundingBox(CelestialArmillaryControllerBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(4.5);
    }

    @Override
    public ArmillaryRenderState createRenderState() {
        return new ArmillaryRenderState();
    }

    @Override
    public void extractRenderState(CelestialArmillaryControllerBlockEntity be, ArmillaryRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isStructureValid = be.isStructureValid();
        state.isStarlightActive = be.isStarlightActive();
        state.beamCount = be.getBeamCount();
    }

    @Override
    public void submit(ArmillaryRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        if (state.isStarlightActive) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                // Central Glowing Celestial Singularity
                renderGlowingCore(pose.pose(), consumer, 0.5f, 0.75f, 0.5f, 0.22f, 0.4f, 0.85f, 1.0f, 0.95f, light, overlay);

                // 4 Cardinal Starlight Beam Convergence Lines into Center (Exact optical focal vectors)
                float cx = 0.5f;
                float cy = 0.75f;
                float cz = 0.5f;

                // North beam (Z = -3, Y = +4)
                renderBeamLine(pose.pose(), consumer, cx, cy, cz, cx, 4.5625f, -2.5f, 0.3f, 0.9f, 1.0f, 0.85f, light, overlay);
                // South beam (Z = +3, Y = +4)
                renderBeamLine(pose.pose(), consumer, cx, cy, cz, cx, 4.5625f, 3.5f, 0.3f, 0.9f, 1.0f, 0.85f, light, overlay);
                // West beam (X = -3, Y = +4)
                renderBeamLine(pose.pose(), consumer, cx, cy, cz, -2.5f, 4.5625f, cz, 0.3f, 0.9f, 1.0f, 0.85f, light, overlay);
                // East beam (X = +3, Y = +4)
                renderBeamLine(pose.pose(), consumer, cx, cy, cz, 3.5f, 4.5625f, cz, 0.3f, 0.9f, 1.0f, 0.85f, light, overlay);
            });
        }
    }

    private static void renderGlowingCore(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float r, float g, float b, float a, int light, int overlay) {
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

    public static class ArmillaryRenderState extends BlockEntityRenderState {
        public boolean isStructureValid = false;
        public boolean isStarlightActive = false;
        public int beamCount = 0;
    }
}
