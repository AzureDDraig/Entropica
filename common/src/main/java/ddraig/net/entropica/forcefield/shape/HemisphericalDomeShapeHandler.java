package ddraig.net.entropica.forcefield.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
 * Geometric shape handler for canopy dome forcefields (Hemispherical Dome).
 */
public class HemisphericalDomeShapeHandler implements BarrierShapeHandler {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("entropica", "hemispherical_dome");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int getOrdinal() {
        return 2;
    }

    @Override
    public String getDisplayName() {
        return "Hemispherical Dome";
    }

    @Override
    public String getDescription() {
        return "Canopy dome over an area or machine";
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
        return BarrierGeometry.intersectDome(center, radius, rayStart, rayEnd, entityRadius);
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
        double margin = 0.5;
        double safeRadius = Math.max(0.0, (double) radius);
        return new AABB(
                center.x - safeRadius - margin, center.y - margin, center.z - safeRadius - margin,
                center.x + safeRadius + margin, center.y + safeRadius + margin, center.z + safeRadius + margin
        );
    }

    @Override
    public void render(
            Matrix4f matrix,
            VertexConsumer consumer,
            ForcefieldBarrierRenderer.BarrierRenderState state,
            PoseStack poseStack
    ) {
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

                ForcefieldShaderHelper.ColorResult c0 = ForcefieldShaderHelper.evaluateColor(x0 / radius, z0 / radius, n0, n0, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c1 = ForcefieldShaderHelper.evaluateColor(x1 / radius, z1 / radius, n1, n1, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c2 = ForcefieldShaderHelper.evaluateColor(x2 / radius, z2 / radius, n2, n2, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c3 = ForcefieldShaderHelper.evaluateColor(x3 / radius, z3 / radius, n3, n3, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);

                // Outer surface
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x0, y0, z0, c0, n0);
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x1, y1, z1, c1, n1);
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x2, y1, z2, c2, n2);
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x3, y0, z3, c3, n3);

                // Inner surface
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x3, y0, z3, c3, n3.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x2, y1, z2, c2, n2.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x1, y1, z1, c1, n1.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, matrix, x0, y0, z0, c0, n0.scale(-1));
            }
        }
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
        Vec3 normal = new Vec3(0, 1, 0);

        // Base ring
        for (int i = 0; i < segs; i++) {
            float a0 = 2.0F * (float) Math.PI * i / segs;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / segs;
            float x0 = radius * Mth.cos(a0);
            float z0 = radius * Mth.sin(a0);
            float x1 = radius * Mth.cos(a1);
            float z1 = radius * Mth.sin(a1);

            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, 0, z0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, 0, z1, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, 0.05F, z1, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, 0.05F, z0, color, normal);
        }
    }
}
