package ddraig.net.entropica.forcefield.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.client.renderer.forcefield.ForcefieldBarrierRenderer;
import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.BarrierGeometry;
import ddraig.net.entropica.forcefield.BarrierRaycastHit;
import ddraig.net.entropica.forcefield.BarrierShapeHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Geometric shape handler for circular radial forcefield hoops (Circular Disc).
 */
public class CircularDiscShapeHandler implements BarrierShapeHandler {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("entropica", "circular_disc");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int getOrdinal() {
        return 1;
    }

    @Override
    public String getDisplayName() {
        return "Circular Disc";
    }

    @Override
    public String getDescription() {
        return "Radial circular forcefield hoop";
    }

    @Override
    public BarrierRaycastHit intersect(
            Vec3 center,
            float yRot,
            float xRot,
            float width,
            float height,
            float radius,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius,
            @Nullable ForcefieldBarrierEntity barrier
    ) {
        return BarrierGeometry.intersectCircularDisc(center, yRot, xRot, radius, rayStart, rayEnd, entityRadius);
    }

    @Override
    public AABB computeBoundingBox(
            Vec3 center,
            float yRot,
            float xRot,
            float width,
            float height,
            float radius
    ) {
        float safeRadius = Math.max(0.0F, radius);
        Vec3 n = BarrierGeometry.getNormal(yRot, xRot);
        double dx = safeRadius * Math.sqrt(Math.max(0.0, 1.0 - n.x * n.x));
        double dy = safeRadius * Math.sqrt(Math.max(0.0, 1.0 - n.y * n.y));
        double dz = safeRadius * Math.sqrt(Math.max(0.0, 1.0 - n.z * n.z));
        double margin = 0.5;

        return new AABB(
                center.x - dx - margin, center.y - dy - margin, center.z - dz - margin,
                center.x + dx + margin, center.y + dy + margin, center.z + dz + margin
        );
    }

    @Override
    public void render(
            Matrix4f matrix,
            VertexConsumer consumer,
            ForcefieldBarrierRenderer.BarrierRenderState state,
            PoseStack poseStack
    ) {
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

                ForcefieldShaderHelper.ColorResult c0 = ForcefieldShaderHelper.evaluateColor(x0 / radius, y0 / radius, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c1 = ForcefieldShaderHelper.evaluateColor(x1 / radius, y1 / radius, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c2 = ForcefieldShaderHelper.evaluateColor(x2 / radius, y2 / radius, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c3 = ForcefieldShaderHelper.evaluateColor(x3 / radius, y3 / radius, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);

                // Front face
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y0, 0, c0, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y1, 0, c1, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x2, y2, 0, c2, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x3, y3, 0, c3, normal);

                // Back face
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x3, y3, 0, c3, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x2, y2, 0, c2, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y1, 0, c1, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y0, 0, c0, normal.scale(-1));
            }
        }
        poseStack.popPose();
    }

    @Override
    public void renderPreview(
            PoseStack poseStack,
            VertexConsumer consumer,
            Matrix4f pose,
            float width,
            float height,
            float radius,
            float r,
            float g,
            float b,
            float a
    ) {
        int segs = 24;
        ForcefieldShaderHelper.ColorResult color = new ForcefieldShaderHelper.ColorResult(r, g, b, a);
        Vec3 normal = new Vec3(0, 0, 1);

        for (int i = 0; i < segs; i++) {
            float a0 = 2.0F * (float) Math.PI * i / segs;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / segs;
            float x0 = radius * Mth.cos(a0);
            float y0 = radius * Mth.sin(a0);
            float x1 = radius * Mth.cos(a1);
            float y1 = radius * Mth.sin(a1);

            ForcefieldBarrierRenderer.addVertex(consumer, pose, 0, 0, 0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, y0, 0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, y1, 0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, 0, 0, 0, color, normal);
        }
    }
}
