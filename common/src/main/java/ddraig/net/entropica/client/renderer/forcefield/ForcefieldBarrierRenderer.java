package ddraig.net.entropica.client.renderer.forcefield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import ddraig.net.entropica.forcefield.BarrierShape;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedural EntityRenderer for paper-thin soap-film forcefield barriers.
 * Submits dynamic double-sided meshes for quads, discs, domes, spheres, and cylinders
 * with thin-film interference optics and ripple shockwaves.
 */
public class ForcefieldBarrierRenderer extends EntityRenderer<ForcefieldBarrierEntity, ForcefieldBarrierRenderer.BarrierRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;

    public ForcefieldBarrierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static class BarrierRenderState extends EntityRenderState {
        public BarrierShape shape = BarrierShape.PLANAR_QUAD;
        public ApexPredatorTheme theme = ApexPredatorTheme.STANDARD;
        public float width = 4.0F;
        public float height = 4.0F;
        public float radius = 5.0F;
        public float ageTicks = 0.0F;
        public float yRot = 0.0F;
        public float xRot = 0.0F;
        public Vec3 entityWorldPos = Vec3.ZERO;
        public final List<ForcefieldBarrierEntity.RippleImpact> ripples = new ArrayList<>();
    }

    @Override
    public BarrierRenderState createRenderState() {
        return new BarrierRenderState();
    }

    @Override
    public void extractRenderState(ForcefieldBarrierEntity entity, BarrierRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.shape = entity.getShape();
        state.theme = entity.getPredatorTheme();
        state.width = entity.getWidth();
        state.height = entity.getHeight();
        state.radius = entity.getRadius();
        state.ageTicks = entity.tickCount + partialTick;
        state.yRot = entity.getYRot();
        state.xRot = entity.getXRot();
        state.entityWorldPos = entity.position();

        state.ripples.clear();
        state.ripples.addAll(entity.getActiveRipples());
    }

    @Override
    public void submit(BarrierRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            Vec3 viewDir = new Vec3(0, 0, 1); // default view dir in local space

            switch (state.shape) {
                case PLANAR_QUAD, CONVEX_POLYGON -> renderPlanarQuad(matrix, consumer, state, poseStack);
                case CIRCULAR_DISC -> renderCircularDisc(matrix, consumer, state, poseStack);
                case HEMISPHERICAL_DOME -> renderDome(matrix, consumer, state);
                case SPHERICAL_BUBBLE -> renderSphere(matrix, consumer, state);
                case CYLINDER -> renderCylinder(matrix, consumer, state);
            }
        });

        poseStack.popPose();
    }

    /**
     * Renders a finely subdivided planar quad with animated waves and ripple displacement.
     */
    private void renderPlanarQuad(Matrix4f matrix, VertexConsumer consumer, BarrierRenderState state, PoseStack poseStack) {
        int segsX = 12;
        int segsY = 12;
        float halfW = state.width * 0.5F;
        float halfH = state.height * 0.5F;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        Matrix4f rotMatrix = poseStack.last().pose();

        Vec3 normal = new Vec3(0, 0, 1);
        Vec3 viewDir = new Vec3(0, 0, 1);

        for (int i = 0; i < segsX; i++) {
            float u0 = -1.0F + (2.0F * i / segsX);
            float u1 = -1.0F + (2.0F * (i + 1) / segsX);
            float x0 = u0 * halfW;
            float x1 = u1 * halfW;

            for (int j = 0; j < segsY; j++) {
                float v0 = -1.0F + (2.0F * j / segsY);
                float v1 = -1.0F + (2.0F * (j + 1) / segsY);
                float y0 = v0 * halfH;
                float y1 = v1 * halfH;

                float r0 = SoapFilmShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x0, y0, 0), state.ripples, (long) state.ageTicks, 0);
                float r1 = SoapFilmShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x1, y0, 0), state.ripples, (long) state.ageTicks, 0);
                float r2 = SoapFilmShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x1, y1, 0), state.ripples, (long) state.ageTicks, 0);
                float r3 = SoapFilmShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x0, y1, 0), state.ripples, (long) state.ageTicks, 0);

                SoapFilmShaderHelper.ColorResult c0 = SoapFilmShaderHelper.evaluateColor(u0, v0, normal, viewDir, state.ageTicks, state.theme, Math.abs(r0));
                SoapFilmShaderHelper.ColorResult c1 = SoapFilmShaderHelper.evaluateColor(u1, v0, normal, viewDir, state.ageTicks, state.theme, Math.abs(r1));
                SoapFilmShaderHelper.ColorResult c2 = SoapFilmShaderHelper.evaluateColor(u1, v1, normal, viewDir, state.ageTicks, state.theme, Math.abs(r2));
                SoapFilmShaderHelper.ColorResult c3 = SoapFilmShaderHelper.evaluateColor(u0, v1, normal, viewDir, state.ageTicks, state.theme, Math.abs(r3));

                // Front Face
                addVertex(consumer, rotMatrix, x0, y0, r0, c0, normal);
                addVertex(consumer, rotMatrix, x1, y0, r1, c1, normal);
                addVertex(consumer, rotMatrix, x1, y1, r2, c2, normal);
                addVertex(consumer, rotMatrix, x0, y1, r3, c3, normal);

                // Back Face (Reverse winding)
                addVertex(consumer, rotMatrix, x0, y1, r3, c3, normal.scale(-1));
                addVertex(consumer, rotMatrix, x1, y1, r2, c2, normal.scale(-1));
                addVertex(consumer, rotMatrix, x1, y0, r1, c1, normal.scale(-1));
                addVertex(consumer, rotMatrix, x0, y0, r0, c0, normal.scale(-1));
            }
        }
        poseStack.popPose();
    }

    /**
     * Renders a circular disc soap-film hoop.
     */
    private void renderCircularDisc(Matrix4f matrix, VertexConsumer consumer, BarrierRenderState state, PoseStack poseStack) {
        int radialSegs = 24;
        int ringSegs = 6;
        float radius = state.radius;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        Matrix4f rotMatrix = poseStack.last().pose();

        Vec3 normal = new Vec3(0, 0, 1);
        Vec3 viewDir = new Vec3(0, 0, 1);

        for (int r = 0; r < ringSegs; r++) {
            float fr0 = (float) r / ringSegs;
            float fr1 = (float) (r + 1) / ringSegs;
            float rad0 = fr0 * radius;
            float rad1 = fr1 * radius;

            for (int s = 0; s < radialSegs; s++) {
                float angle0 = 2.0F * (float) Math.PI * s / radialSegs;
                float angle1 = 2.0F * (float) Math.PI * (s + 1) / radialSegs;

                float x0 = rad0 * Mth.cos(angle0);
                float y0 = rad0 * Mth.sin(angle0);
                float x1 = rad1 * Mth.cos(angle0);
                float y1 = rad1 * Mth.sin(angle0);
                float x2 = rad1 * Mth.cos(angle1);
                float y2 = rad1 * Mth.sin(angle1);
                float x3 = rad0 * Mth.cos(angle1);
                float y3 = rad0 * Mth.sin(angle1);

                SoapFilmShaderHelper.ColorResult c0 = SoapFilmShaderHelper.evaluateColor(x0 / radius, y0 / radius, normal, viewDir, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c1 = SoapFilmShaderHelper.evaluateColor(x1 / radius, y1 / radius, normal, viewDir, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c2 = SoapFilmShaderHelper.evaluateColor(x2 / radius, y2 / radius, normal, viewDir, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c3 = SoapFilmShaderHelper.evaluateColor(x3 / radius, y3 / radius, normal, viewDir, state.ageTicks, state.theme, 0);

                // Front face
                addVertex(consumer, rotMatrix, x0, y0, 0, c0, normal);
                addVertex(consumer, rotMatrix, x1, y1, 0, c1, normal);
                addVertex(consumer, rotMatrix, x2, y2, 0, c2, normal);
                addVertex(consumer, rotMatrix, x3, y3, 0, c3, normal);

                // Back face
                addVertex(consumer, rotMatrix, x3, y3, 0, c3, normal.scale(-1));
                addVertex(consumer, rotMatrix, x2, y2, 0, c2, normal.scale(-1));
                addVertex(consumer, rotMatrix, x1, y1, 0, c1, normal.scale(-1));
                addVertex(consumer, rotMatrix, x0, y0, 0, c0, normal.scale(-1));
            }
        }
        poseStack.popPose();
    }

    /**
     * Renders a hemispherical soap bubble canopy dome.
     */
    private void renderDome(Matrix4f matrix, VertexConsumer consumer, BarrierRenderState state) {
        int lats = 12;
        int lons = 24;
        float radius = state.radius;

        for (int i = 0; i < lats; i++) {
            float lat0 = ((float) Math.PI * 0.5F) * (float) i / lats;
            float lat1 = ((float) Math.PI * 0.5F) * (float) (i + 1) / lats;

            float y0 = radius * Mth.sin(lat0);
            float r0 = radius * Mth.cos(lat0);
            float y1 = radius * Mth.sin(lat1);
            float r1 = radius * Mth.cos(lat1);

            for (int j = 0; j < lons; j++) {
                float lon0 = 2.0F * (float) Math.PI * j / lons;
                float lon1 = 2.0F * (float) Math.PI * (j + 1) / lons;

                float x0 = r0 * Mth.cos(lon0); float z0 = r0 * Mth.sin(lon0);
                float x1 = r1 * Mth.cos(lon0); float z1 = r1 * Mth.sin(lon0);
                float x2 = r1 * Mth.cos(lon1); float z2 = r1 * Mth.sin(lon1);
                float x3 = r0 * Mth.cos(lon1); float z3 = r0 * Mth.sin(lon1);

                Vec3 n0 = new Vec3(x0, y0, z0).normalize();
                Vec3 n1 = new Vec3(x1, y1, z1).normalize();
                Vec3 n2 = new Vec3(x2, y1, z2).normalize();
                Vec3 n3 = new Vec3(x3, y0, z3).normalize();

                SoapFilmShaderHelper.ColorResult c0 = SoapFilmShaderHelper.evaluateColor(x0 / radius, z0 / radius, n0, n0, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c1 = SoapFilmShaderHelper.evaluateColor(x1 / radius, z1 / radius, n1, n1, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c2 = SoapFilmShaderHelper.evaluateColor(x2 / radius, z2 / radius, n2, n2, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c3 = SoapFilmShaderHelper.evaluateColor(x3 / radius, z3 / radius, n3, n3, state.ageTicks, state.theme, 0);

                // Outer surface
                addVertex(consumer, matrix, x0, y0, z0, c0, n0);
                addVertex(consumer, matrix, x1, y1, z1, c1, n1);
                addVertex(consumer, matrix, x2, y1, z2, c2, n2);
                addVertex(consumer, matrix, x3, y0, z3, c3, n3);

                // Inner surface
                addVertex(consumer, matrix, x3, y0, z3, c3, n3.scale(-1));
                addVertex(consumer, matrix, x2, y1, z2, c2, n2.scale(-1));
                addVertex(consumer, matrix, x1, y1, z1, c1, n1.scale(-1));
                addVertex(consumer, matrix, x0, y0, z0, c0, n0.scale(-1));
            }
        }
    }

    /**
     * Renders a complete spherical soap bubble forcefield.
     */
    private void renderSphere(Matrix4f matrix, VertexConsumer consumer, BarrierRenderState state) {
        int lats = 16;
        int lons = 24;
        float radius = state.radius;

        for (int i = 0; i < lats; i++) {
            float lat0 = (float) Math.PI * (-0.5F + (float) i / lats);
            float lat1 = (float) Math.PI * (-0.5F + (float) (i + 1) / lats);

            float y0 = radius * Mth.sin(lat0);
            float r0 = radius * Mth.cos(lat0);
            float y1 = radius * Mth.sin(lat1);
            float r1 = radius * Mth.cos(lat1);

            for (int j = 0; j < lons; j++) {
                float lon0 = 2.0F * (float) Math.PI * j / lons;
                float lon1 = 2.0F * (float) Math.PI * (j + 1) / lons;

                float x0 = r0 * Mth.cos(lon0); float z0 = r0 * Mth.sin(lon0);
                float x1 = r1 * Mth.cos(lon0); float z1 = r1 * Mth.sin(lon0);
                float x2 = r1 * Mth.cos(lon1); float z2 = r1 * Mth.sin(lon1);
                float x3 = r0 * Mth.cos(lon1); float z3 = r0 * Mth.sin(lon1);

                Vec3 n0 = new Vec3(x0, y0, z0).normalize();
                Vec3 n1 = new Vec3(x1, y1, z1).normalize();
                Vec3 n2 = new Vec3(x2, y1, z2).normalize();
                Vec3 n3 = new Vec3(x3, y0, z3).normalize();

                SoapFilmShaderHelper.ColorResult c0 = SoapFilmShaderHelper.evaluateColor(x0 / radius, z0 / radius, n0, n0, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c1 = SoapFilmShaderHelper.evaluateColor(x1 / radius, z1 / radius, n1, n1, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c2 = SoapFilmShaderHelper.evaluateColor(x2 / radius, z2 / radius, n2, n2, state.ageTicks, state.theme, 0);
                SoapFilmShaderHelper.ColorResult c3 = SoapFilmShaderHelper.evaluateColor(x3 / radius, z3 / radius, n3, n3, state.ageTicks, state.theme, 0);

                addVertex(consumer, matrix, x0, y0, z0, c0, n0);
                addVertex(consumer, matrix, x1, y1, z1, c1, n1);
                addVertex(consumer, matrix, x2, y1, z2, c2, n2);
                addVertex(consumer, matrix, x3, y0, z3, c3, n3);

                addVertex(consumer, matrix, x3, y0, z3, c3, n3.scale(-1));
                addVertex(consumer, matrix, x2, y1, z2, c2, n2.scale(-1));
                addVertex(consumer, matrix, x1, y1, z1, c1, n1.scale(-1));
                addVertex(consumer, matrix, x0, y0, z0, c0, n0.scale(-1));
            }
        }
    }

    /**
     * Renders a cylindrical column barrier.
     */
    private void renderCylinder(Matrix4f matrix, VertexConsumer consumer, BarrierRenderState state) {
        int segs = 24;
        float radius = state.radius;
        float height = state.height;

        for (int i = 0; i < segs; i++) {
            float angle0 = 2.0F * (float) Math.PI * i / segs;
            float angle1 = 2.0F * (float) Math.PI * (i + 1) / segs;

            float x0 = radius * Mth.cos(angle0); float z0 = radius * Mth.sin(angle0);
            float x1 = radius * Mth.cos(angle1); float z1 = radius * Mth.sin(angle1);

            Vec3 n0 = new Vec3(x0, 0, z0).normalize();
            Vec3 n1 = new Vec3(x1, 0, z1).normalize();

            SoapFilmShaderHelper.ColorResult c0 = SoapFilmShaderHelper.evaluateColor(x0 / radius, 0, n0, n0, state.ageTicks, state.theme, 0);
            SoapFilmShaderHelper.ColorResult c1 = SoapFilmShaderHelper.evaluateColor(x1 / radius, 0, n1, n1, state.ageTicks, state.theme, 0);

            // Wall quad
            addVertex(consumer, matrix, x0, 0, z0, c0, n0);
            addVertex(consumer, matrix, x1, 0, z1, c1, n1);
            addVertex(consumer, matrix, x1, height, z1, c1, n1);
            addVertex(consumer, matrix, x0, height, z0, c0, n0);

            // Reverse wall quad
            addVertex(consumer, matrix, x0, height, z0, c0, n0.scale(-1));
            addVertex(consumer, matrix, x1, height, z1, c1, n1.scale(-1));
            addVertex(consumer, matrix, x1, 0, z1, c1, n1.scale(-1));
            addVertex(consumer, matrix, x0, 0, z0, c0, n0.scale(-1));
        }
    }

    private static void addVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x, float y, float z,
            SoapFilmShaderHelper.ColorResult color,
            Vec3 normal
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color.r(), color.g(), color.b(), color.a())
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_LIGHT)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }
}
