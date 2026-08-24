package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.VoidRiftBlock;
import ddraig.net.entropica.block.entity.VoidRiftBlockEntity;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class VoidRiftRenderer implements BlockEntityRenderer<VoidRiftBlockEntity, VoidRiftRenderer.VoidRiftRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final ItemModelResolver itemModelResolver;

    public static final int MAX_RENDER_ITEMS = 64;

    public VoidRiftRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class RiftItemState {
        public final ItemStackRenderState renderState = new ItemStackRenderState();
        public float progress = 0;
        public boolean fromGround = false;
        public Vec3 startOffset = Vec3.ZERO;
        public boolean hasInvTarget = false;
        public List<Vec3> smartPath = null;
    }

    public static class VoidRiftRenderState extends BlockEntityRenderState {
        public final RiftItemState[] itemStates = new RiftItemState[MAX_RENDER_ITEMS];
        public int itemCount = 0;
        public boolean isInput = true;

        public VoidRiftRenderState() {
            for (int i = 0; i < MAX_RENDER_ITEMS; i++) {
                itemStates[i] = new RiftItemState();
            }
        }
    }

    @Override
    public VoidRiftRenderState createRenderState() {
        return new VoidRiftRenderState();
    }

    @Override
    public void extractRenderState(VoidRiftBlockEntity be, VoidRiftRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        state.itemCount = Math.min(MAX_RENDER_ITEMS, be.transitItems.size());

        if (be.getLevel() != null) {
            for (int i = 0; i < state.itemCount; i++) {
                VoidRiftBlockEntity.TransitItem transitItem = be.transitItems.get(i);
                RiftItemState itemState = state.itemStates[i];

                this.itemModelResolver.updateForTopItem(itemState.renderState, transitItem.stack, ItemDisplayContext.GROUND, be.getLevel(), null, 0);

                // Uses the specific item's dynamically calculated travel time!
                itemState.progress = (transitItem.tick + partialTick) / (float) transitItem.maxTicks;
                if (itemState.progress > 1.0f) itemState.progress = 1.0f;

                itemState.fromGround = transitItem.fromGround;
                itemState.startOffset = new Vec3(transitItem.startX, transitItem.startY, transitItem.startZ);
                itemState.hasInvTarget = transitItem.hasInvTarget;

                if (transitItem.hasInvTarget) {
                    BlockPos invPos = be.getBlockPos().offset((int)transitItem.invOffsetX, (int)transitItem.invOffsetY, (int)transitItem.invOffsetZ);
                    itemState.smartPath = be.getPathToInventory(invPos);
                }
            }
        }

        state.isInput = be.getBlockState().getValue(VoidRiftBlock.IS_INPUT);

        if (be.getLevel() != null && be.getLevel().random.nextInt(3) == 0) {
            double px = be.getBlockPos().getX() + 0.5 + (be.getLevel().random.nextDouble() - 0.5) * 0.5;
            double py = be.getBlockPos().getY() + 0.5 + (be.getLevel().random.nextDouble() - 0.5) * 0.5;
            double pz = be.getBlockPos().getZ() + 0.5 + (be.getLevel().random.nextDouble() - 0.5) * 0.5;

            if (state.isInput) {
                be.getLevel().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0, 0);
            } else {
                be.getLevel().addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, 0, 0);
            }
        }
    }

    @Override
    public void submit(VoidRiftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        float time = (System.currentTimeMillis() % 360000L) / 50.0f;

        poseStack.pushPose();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 3.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 2.0f));

        if (state.isInput) {
            collector.submitCustomGeometry(poseStack, RenderType.entityCutoutNoCull(WHITE_TEXTURE), (pose, cons) -> {
                drawSphere(cons, pose.pose(), 0.15f, 0.0f, 0.0f, 0.0f, 1.0f, light);
            });
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucent(WHITE_TEXTURE), (pose, cons) -> {
                drawFlatRing(cons, pose.pose(), 0.3f, 0.02f, 32, 0.4f, 0.0f, 0.6f, 0.8f, light);
            });
        } else {
            collector.submitCustomGeometry(poseStack, RenderType.entityCutoutNoCull(WHITE_TEXTURE), (pose, cons) -> {
                drawSphere(cons, pose.pose(), 0.15f, 1.0f, 1.0f, 1.0f, 1.0f, light);
            });
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucent(WHITE_TEXTURE), (pose, cons) -> {
                drawFlatRing(cons, pose.pose(), 0.3f, 0.02f, 32, 1.0f, 0.9f, 0.0f, 0.8f, light);
            });
        }
        poseStack.popPose();

        Vec3 riftCenter = new Vec3(0.5, 0.5, 0.5);

        for (int i = 0; i < state.itemCount; i++) {
            RiftItemState itemState = state.itemStates[i];
            float p = itemState.progress;

            Vec3 currentPos;
            float scale = 1.0f;
            float spinX = 0;
            float spinZ = 0;
            float spinY = p * 360f;

            if (itemState.fromGround) {
                Vec3 groundStart = itemState.startOffset;
                Vec3 groundHover = groundStart.add(0, 0.5, 0);

                Vec3 diff = riftCenter.subtract(groundStart);
                Vec3 dir = diff.lengthSqr() < 0.001 ? new Vec3(0, 1, 0) : diff.normalize();

                Vec3 mid = groundHover.add(riftCenter).scale(0.5);
                Vec3 controlPoint = Math.abs(dir.y) > 0.7
                        ? new Vec3(mid.x + 1.0, mid.y, mid.z + 1.0)
                        : new Vec3(mid.x, Math.max(groundHover.y, riftCenter.y) + 1.5, mid.z);

                if (p < 0.25f) {
                    float t = p / 0.25f;
                    currentPos = groundStart.lerp(groundHover, t);
                } else {
                    float t = (p - 0.25f) / 0.75f;
                    Vec3 baseCurve = quadraticBezier(groundHover, controlPoint, riftCenter, t);

                    float swirlT = Math.max(0.0f, (t - 0.5f) / 0.5f);
                    float radius = (float)Math.sin(swirlT * Math.PI) * 0.4f;
                    float angle = swirlT * (float)Math.PI * 8.0f;
                    currentPos = baseCurve.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);

                    scale = 1.0f - t;
                    spinX = t * 720f; spinZ = t * 1080f;
                }
            } else if (itemState.hasInvTarget && itemState.smartPath != null && !itemState.smartPath.isEmpty()) {
                float mappedT = state.isInput ? (1.0f - p) : p;
                Vec3 basePos = getPointOnPath(itemState.smartPath, mappedT);

                float swirlT = 0;
                if (state.isInput && p > 0.5f) {
                    swirlT = (p - 0.5f) / 0.5f;
                    scale = 1.0f - swirlT;
                } else if (!state.isInput && p < 0.5f) {
                    swirlT = (0.5f - p) / 0.5f;
                    scale = 1.0f - swirlT;
                }

                if (swirlT > 0) {
                    float radius = (float)Math.sin(swirlT * Math.PI) * 0.4f;
                    float angle = swirlT * (float)Math.PI * 8.0f;
                    basePos = basePos.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                    spinX = swirlT * 720f; spinZ = swirlT * 1080f;
                }

                currentPos = basePos;
            } else {
                currentPos = riftCenter;
                scale = 0;
            }

            poseStack.pushPose();
            poseStack.translate(currentPos.x, currentPos.y, currentPos.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(spinY));
            if (spinX != 0 || spinZ != 0) {
                poseStack.mulPose(Axis.XP.rotationDegrees(spinX));
                poseStack.mulPose(Axis.ZP.rotationDegrees(spinZ));
            }
            poseStack.scale(scale, scale, scale);

            itemState.renderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private Vec3 getPointOnPath(List<Vec3> path, float t) {
        if (path == null || path.isEmpty()) return new Vec3(0.5, 0.5, 0.5);
        if (path.size() == 1) return path.get(0);
        if (t <= 0) return path.get(0);
        if (t >= 1) return path.get(path.size() - 1);

        float scaledT = t * (path.size() - 1);
        int index = (int) scaledT;
        float localT = scaledT - index;

        return path.get(index).lerp(path.get(index + 1), localT);
    }

    private Vec3 quadraticBezier(Vec3 p0, Vec3 p1, Vec3 p2, float t) {
        float u = 1.0f - t;
        float tt = t * t;
        float uu = u * u;
        return p0.scale(uu).add(p1.scale(2 * u * t)).add(p2.scale(tt));
    }

    private void drawSphere(VertexConsumer consumer, Matrix4f matrix, float radius, float r, float g, float b, float a, int light) {
        int lats = 12;
        int longs = 12;
        int overlay = OverlayTexture.NO_OVERLAY;
        for (int i = 0; i < lats; i++) {
            float lat0 = (float) (Math.PI * (-0.5 + (double) i / lats));
            float z0 = radius * (float) Math.sin(lat0);
            float zr0 = radius * (float) Math.cos(lat0);

            float lat1 = (float) (Math.PI * (-0.5 + (double) (i + 1) / lats));
            float z1 = radius * (float) Math.sin(lat1);
            float zr1 = radius * (float) Math.cos(lat1);

            for (int j = 0; j < longs; j++) {
                float lng = (float) (2 * Math.PI * (double) j / longs);
                float x = (float) Math.cos(lng);
                float y = (float) Math.sin(lng);

                float lng1 = (float) (2 * Math.PI * (double) (j + 1) / longs);
                float x1 = (float) Math.cos(lng1);
                float y1 = (float) Math.sin(lng1);

                consumer.addVertex(matrix, x * zr0, z0, y * zr0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(x, z0/radius, y);
                consumer.addVertex(matrix, x1 * zr0, z0, y1 * zr0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(x1, z0/radius, y1);
                consumer.addVertex(matrix, x1 * zr1, z1, y1 * zr1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(x1, z1/radius, y1);
                consumer.addVertex(matrix, x * zr1, z1, y * zr1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(x, z1/radius, y);
            }
        }
    }

    private void drawFlatRing(VertexConsumer consumer, Matrix4f matrix, float radius, float width, int segments, float r, float g, float b, float a, int light) {
        float halfWidth = width / 2.0f;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float c1 = (float) Math.cos(angle1);
            float s1 = (float) Math.sin(angle1);
            float c2 = (float) Math.cos(angle2);
            float s2 = (float) Math.sin(angle2);

            float x1_in = c1 * (radius - halfWidth);
            float z1_in = s1 * (radius - halfWidth);
            float x1_out = c1 * (radius + halfWidth);
            float z1_out = s1 * (radius + halfWidth);

            float x2_in = c2 * (radius - halfWidth);
            float z2_in = s2 * (radius - halfWidth);
            float x2_out = c2 * (radius + halfWidth);
            float z2_out = s2 * (radius + halfWidth);

            consumer.addVertex(matrix, x1_out, 0, z1_out).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x1_in, 0, z1_in).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x2_in, 0, z2_in).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x2_out, 0, z2_out).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        }
    }
}