package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.OpticTransmitterBlock;
import ddraig.net.entropica.block.entity.OpticTransmitterBlockEntity;
import ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class OpticTransmitterRenderer implements BlockEntityRenderer<OpticTransmitterBlockEntity, OpticTransmitterRenderer.TransmitterRenderState> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public OpticTransmitterRenderer(BlockEntityRendererProvider.Context context) {
    }

    public AABB getRenderBoundingBox(OpticTransmitterBlockEntity blockEntity) {
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
    public boolean shouldRender(OpticTransmitterBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    @Override
    public TransmitterRenderState createRenderState() {
        return new TransmitterRenderState();
    }

    @Override
    public void extractRenderState(OpticTransmitterBlockEntity be, TransmitterRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isTransmitting = be.isTransmitting();
        state.beamDistance = be.getBeamDistance();
        state.originPos = be.getBlockPos();
        state.activeStarName = be.getActiveStarName();
        if (be.getBlockState().hasProperty(OpticTransmitterBlock.FACING)) {
            state.facing = be.getBlockState().getValue(OpticTransmitterBlock.FACING);
        } else {
            state.facing = Direction.NORTH;
        }
    }

    @Override
    public void submit(TransmitterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (!state.isTransmitting || state.beamDistance < 0.05f) {
            return;
        }

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        float dist = state.beamDistance;
        Direction facing = state.facing;

        float sx = 0.5f + facing.getStepX() * 0.45f;
        float sy = 0.5f + facing.getStepY() * 0.45f;
        float sz = 0.5f + facing.getStepZ() * 0.45f;

        float tx = sx + facing.getStepX() * dist;
        float ty = sy + facing.getStepY() * dist;
        float tz = sz + facing.getStepZ() * dist;

        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        EssenceType essence = RefractiveAstralLensBlockEntity.resolveStarEssence(state.activeStarName);
        int[] rgb = essence.getCurrentRGB(gameTime);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;
        float coreR = Mth.lerp(0.5f, r, 1.0f);
        float coreG = Mth.lerp(0.5f, g, 1.0f);
        float coreB = Mth.lerp(0.5f, b, 1.0f);

        collector.submitCustomGeometry(poseStack, RenderType.beaconBeam(WHITE_TEXTURE, false), (pose, consumer) -> {
            // Core Starlight Beam
            renderBeam(pose.pose(), consumer, sx, sy, sz, tx, ty, tz, coreR, coreG, coreB, 0.9f, 0.035f, light, overlay);
        });

        collector.submitCustomGeometry(poseStack, RenderType.beaconBeam(WHITE_TEXTURE, true), (pose, consumer) -> {
            // Outer Luminous Aura Shroud
            renderBeam(pose.pose(), consumer, sx, sy, sz, tx, ty, tz, r, g, b, 0.45f, 0.08f, light, overlay);
        });
    }

    private static void renderBeam(Matrix4f matrix, VertexConsumer consumer, float sx, float sy, float sz, float tx, float ty, float tz, float r, float g, float b, float a, float thickness, int light, int overlay) {
        float dx = tx - sx;
        float dy = ty - sy;
        float dz = tz - sz;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001f) return;

        float ux = dx / len;
        float uy = dy / len;
        float uz = dz / len;

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

            consumer.addVertex(matrix, sx + ox1, sy + oy1, sz + oz1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, sx + ox2, sy + oy2, sz + oz2).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, tx + ox2, ty + oy2, tz + oz2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, tx + ox1, ty + oy1, tz + oz1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        }
    }

    public static class TransmitterRenderState extends BlockEntityRenderState {
        public boolean isTransmitting = false;
        public float beamDistance = 0.0f;
        public Direction facing = Direction.NORTH;
        public BlockPos originPos = BlockPos.ZERO;
        public String activeStarName = "";
    }
}
