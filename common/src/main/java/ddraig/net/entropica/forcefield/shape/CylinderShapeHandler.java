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
 * Geometric shape handler for tubular column forcefields (Cylinder).
 */
public class CylinderShapeHandler implements BarrierShapeHandler {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("entropica", "cylinder");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int getOrdinal() {
        return 4;
    }

    @Override
    public String getDisplayName() {
        return "Cylindrical Column";
    }

    @Override
    public String getDescription() {
        return "Tubular column barrier extending along an axis";
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
        return BarrierGeometry.intersectCylinder(center, radius, height, rayStart, rayEnd, entityRadius);
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
        double safeHeight = Math.max(0.0, (double) height);
        return new AABB(
                center.x - safeRadius - margin, center.y - margin, center.z - safeRadius - margin,
                center.x + safeRadius + margin, center.y + safeHeight + margin, center.z + safeRadius + margin
        );
    }

    @Override
    public void render(
            Matrix4f matrix,
            VertexConsumer consumer,
            ForcefieldBarrierRenderer.BarrierRenderState state,
            PoseStack poseStack
    ) {
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

            ForcefieldShaderHelper.ColorResult c0 = ForcefieldShaderHelper.evaluateColor(x0 / radius, 0, n0, n0, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
            ForcefieldShaderHelper.ColorResult c1 = ForcefieldShaderHelper.evaluateColor(x1 / radius, 0, n1, n1, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);

            // Wall quad
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x0, 0, z0, c0, n0);
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x1, 0, z1, c1, n1);
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x1, height, z1, c1, n1);
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x0, height, z0, c0, n0);

            // Reverse wall quad
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x0, height, z0, c0, n0.scale(-1));
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x1, height, z1, c1, n1.scale(-1));
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x1, 0, z1, c1, n1.scale(-1));
            ForcefieldBarrierRenderer.addVertex(consumer, matrix, x0, 0, z0, c0, n0.scale(-1));
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

        // Bottom ring and top ring
        for (int i = 0; i < segs; i++) {
            float a0 = 2.0F * (float) Math.PI * i / segs;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / segs;
            float x0 = radius * Mth.cos(a0);
            float z0 = radius * Mth.sin(a0);
            float x1 = radius * Mth.cos(a1);
            float z1 = radius * Mth.sin(a1);

            // Bottom
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, 0, z0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, 0, z1, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, 0.05F, z1, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, 0.05F, z0, color, normal);

            // Top
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, height, z0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, height, z1, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, height + 0.05F, z1, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, height + 0.05F, z0, color, normal);
        }
    }
}
