package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.CagedOpticBulbBlock;
import ddraig.net.entropica.block.entity.CagedOpticBulbBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class CagedOpticBulbRenderer implements BlockEntityRenderer<CagedOpticBulbBlockEntity, CagedOpticBulbRenderer.BulbRenderState> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public CagedOpticBulbRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BulbRenderState createRenderState() {
        return new BulbRenderState();
    }

    @Override
    public void extractRenderState(CagedOpticBulbBlockEntity be, BulbRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        boolean blockLit = blockState.hasProperty(CagedOpticBulbBlock.LIT) && blockState.getValue(CagedOpticBulbBlock.LIT);
        state.isLit = be.isLit() || blockLit;
        state.activeStarName = be.getActiveStarName();
        if (blockState.hasProperty(CagedOpticBulbBlock.FACING)) {
            state.facing = blockState.getValue(CagedOpticBulbBlock.FACING);
        } else {
            state.facing = Direction.UP;
        }
    }

    @Override
    public void submit(BulbRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;

        poseStack.pushPose();
        // Center rotation
        poseStack.translate(0.5, 0.5, 0.5);
        applyFacingRotation(poseStack, state.facing);
        poseStack.translate(-0.5, -0.5, -0.5);

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

        // 1. Render Inner Glass Bulb (Translucent Emissive)
        // Inside cage: X: [6.5..9.5] (0.40625..0.59375), Y: [4.8..9.2] (0.30..0.575), Z: [6.5..9.5] (0.40625..0.59375)
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            // Glass envelope
            renderBox(pose.pose(), consumer, 0.40625f, 0.30f, 0.40625f, 0.59375f, 0.575f, 0.59375f, finalR, finalG, finalB, finalA, light, overlay);

            // Filament Core Stem
            renderBox(pose.pose(), consumer, 0.475f, 0.325f, 0.475f, 0.525f, 0.55f, 0.525f, fCoreR, fCoreG, fCoreB, 1.0f, light, overlay);

            // 2. Render Soft Inner Luminous Glow when lit (nestled comfortably inside cage)
            if (state.isLit) {
                float aura = 0.015f + (float) Math.sin(gameTime * 0.15) * 0.005f;
                renderBox(pose.pose(), consumer, 0.40625f - aura, 0.30f - aura, 0.40625f - aura,
                        0.59375f + aura, 0.575f + aura, 0.59375f + aura,
                        finalR, finalG, finalB, 0.28f, light, overlay);
            }
        });

        poseStack.popPose();
    }

    private static void applyFacingRotation(PoseStack poseStack, Direction facing) {
        net.minecraft.client.resources.model.BlockModelRotation rot = switch (facing) {
            case DOWN -> net.minecraft.client.resources.model.BlockModelRotation.X180_Y0;
            case UP -> net.minecraft.client.resources.model.BlockModelRotation.X0_Y0;
            case NORTH -> net.minecraft.client.resources.model.BlockModelRotation.X90_Y0;
            case SOUTH -> net.minecraft.client.resources.model.BlockModelRotation.X90_Y180;
            case WEST -> net.minecraft.client.resources.model.BlockModelRotation.X90_Y270;
            case EAST -> net.minecraft.client.resources.model.BlockModelRotation.X90_Y90;
        };
        poseStack.mulPose(rot.transformation().getLeftRotation());
    }

    private static void renderBox(Matrix4f pose, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, int light, int overlay) {
        // Down (y-)
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);

        // Up (y+)
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

        // North (z-)
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);

        // South (z+)
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);

        // West (x-)
        consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);

        // East (x+)
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
    }

    public static class BulbRenderState extends BlockEntityRenderState {
        public boolean isLit = false;
        public String activeStarName = "";
        public Direction facing = Direction.UP;
    }
}
