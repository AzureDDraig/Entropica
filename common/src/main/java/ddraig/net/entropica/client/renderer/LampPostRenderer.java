package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.LampPostArm;
import ddraig.net.entropica.block.LampPostBlock;
import ddraig.net.entropica.block.entity.LampPostBlockEntity;
import ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class LampPostRenderer implements BlockEntityRenderer<LampPostBlockEntity, LampPostRenderer.PostRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public static class PostRenderState extends BlockEntityRenderState {
        public boolean isLit = false;
        public String activeStarName = "Astral";
        public LampPostArm northArm = LampPostArm.NONE;
        public LampPostArm eastArm = LampPostArm.NONE;
        public LampPostArm southArm = LampPostArm.NONE;
        public LampPostArm westArm = LampPostArm.NONE;
        public boolean isGlass = false;
    }

    public LampPostRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public PostRenderState createRenderState() {
        return new PostRenderState();
    }

    @Override
    public void extractRenderState(LampPostBlockEntity be, PostRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        boolean blockLit = blockState.hasProperty(LampPostBlock.LIT) && blockState.getValue(LampPostBlock.LIT);
        state.isLit = be.isLit() || blockLit;
        state.activeStarName = be.getActiveStarName();
        if (blockState.hasProperty(LampPostBlock.NORTH_ARM)) state.northArm = blockState.getValue(LampPostBlock.NORTH_ARM);
        if (blockState.hasProperty(LampPostBlock.EAST_ARM))  state.eastArm  = blockState.getValue(LampPostBlock.EAST_ARM);
        if (blockState.hasProperty(LampPostBlock.SOUTH_ARM)) state.southArm = blockState.getValue(LampPostBlock.SOUTH_ARM);
        if (blockState.hasProperty(LampPostBlock.WEST_ARM))  state.westArm  = blockState.getValue(LampPostBlock.WEST_ARM);
        if (blockState.getBlock() instanceof LampPostBlock postBlock) {
            state.isGlass = postBlock.getMaterialVariant() == LampPostBlock.MaterialVariant.GLASS;
        }
    }

    @Override
    public void submit(PostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;

        float r, g, b, a;
        float coreR, coreG, coreB;

        if (state.isLit) {
            EssenceType essence = RefractiveAstralLensBlockEntity.resolveStarEssence(state.activeStarName);
            int[] rgb = essence.getCurrentRGB(gameTime);
            r = rgb[0] / 255.0f;
            g = rgb[1] / 255.0f;
            b = rgb[2] / 255.0f;
            coreR = Mth.lerp(0.6f, r, 1.0f);
            coreG = Mth.lerp(0.6f, g, 1.0f);
            coreB = Mth.lerp(0.6f, b, 1.0f);
            a = 0.85f;
        } else {
            r = 0.5f;
            g = 0.6f;
            b = 0.65f;
            coreR = 0.25f;
            coreG = 0.25f;
            coreB = 0.28f;
            a = 0.25f;
        }

        final float finalR = r;
        final float finalG = g;
        final float finalB = b;
        final float finalA = a;
        final float fCoreR = coreR;
        final float fCoreG = coreG;
        final float fCoreB = coreB;

        // Render Bulbs on active arms using rotated North bulb geometry
        if (state.northArm == LampPostArm.BULB) renderBulbArm(poseStack, collector, 0.0f, finalR, finalG, finalB, finalA, fCoreR, fCoreG, fCoreB, state.isLit, gameTime, light, overlay);
        if (state.eastArm  == LampPostArm.BULB) renderBulbArm(poseStack, collector, 90.0f, finalR, finalG, finalB, finalA, fCoreR, fCoreG, fCoreB, state.isLit, gameTime, light, overlay);
        if (state.southArm == LampPostArm.BULB) renderBulbArm(poseStack, collector, 180.0f, finalR, finalG, finalB, finalA, fCoreR, fCoreG, fCoreB, state.isLit, gameTime, light, overlay);
        if (state.westArm  == LampPostArm.BULB) renderBulbArm(poseStack, collector, 270.0f, finalR, finalG, finalB, finalA, fCoreR, fCoreG, fCoreB, state.isLit, gameTime, light, overlay);

        // Glass Lamp Post Internal Starlight Core
        if (state.isGlass && state.isLit) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                renderBox(pose.pose(), consumer, 0.46875f, 0.0f, 0.46875f, 0.53125f, 1.0f, 0.53125f, fCoreR, fCoreG, fCoreB, 0.9f, light, overlay);
            });
        }
    }

    private static void renderBulbArm(PoseStack poseStack, SubmitNodeCollector collector, float yRot,
                                      float r, float g, float b, float a, float coreR, float coreG, float coreB,
                                      boolean isLit, double gameTime, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (yRot != 0.0f) {
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yRot));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            // Glass envelope inside cage: X:[6.8..9.2], Y:[2.2..7.3], Z:[0.3..2.7]
            renderBox(pose.pose(), consumer, 0.425f, 0.1375f, 0.01875f, 0.575f, 0.45625f, 0.16875f, r, g, b, a, light, overlay);

            // Filament core
            renderBox(pose.pose(), consumer, 0.475f, 0.175f, 0.06875f, 0.525f, 0.41875f, 0.11875f, coreR, coreG, coreB, 1.0f, light, overlay);

            // Soft glow aura nestled safely inside cage ribs (no oversized beam glare box)
            if (isLit) {
                float pulse = (float) Math.sin(gameTime * 0.15) * 0.005f;
                renderBox(pose.pose(), consumer, 0.41f - pulse, 0.125f - pulse, 0.005f - pulse,
                        0.59f + pulse, 0.47f + pulse, 0.18f + pulse,
                        r, g, b, 0.28f, light, overlay);
            }
        });

        poseStack.popPose();
    }

    private static void renderBox(Matrix4f pose, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, int light, int overlay) {
        // Down
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North (z-)
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South (z+)
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West (x-)
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East (x+)
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }
}
