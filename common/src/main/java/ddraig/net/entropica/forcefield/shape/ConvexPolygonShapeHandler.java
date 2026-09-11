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
 * Geometric shape handler for arbitrary multi-vertex planar polygon ribbons (Convex Polygon).
 * Supports convex interior raycast tests, shape-accurate bounding boxes, and centroid fan rendering.
 */
public class ConvexPolygonShapeHandler implements BarrierShapeHandler {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("entropica", "convex_polygon");
    private static final int DEFAULT_VERTEX_COUNT = 6;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int getOrdinal() {
        return 5;
    }

    @Override
    public String getDisplayName() {
        return "Convex Polygon";
    }

    @Override
    public String getDescription() {
        return "Arbitrary multi-vertex planar polygon ribbon";
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
        if (width <= 1e-4F || height <= 1e-4F) {
            return BarrierRaycastHit.miss();
        }

        Vec3 normal = BarrierGeometry.getNormal(yRot, xRot);
        Vec3 tangent = BarrierGeometry.getTangent(yRot);
        Vec3 bitangent = normal.cross(tangent).normalize();

        Vec3 rayDir = rayEnd.subtract(rayStart);
        double denom = rayDir.dot(normal);
        double distToPlane = center.subtract(rayStart).dot(normal);

        int n = DEFAULT_VERTEX_COUNT;
        float semiW = width * 0.5F;
        float semiH = height * 0.5F;

        double[] uVerts = new double[n];
        double[] vVerts = new double[n];
        for (int i = 0; i < n; i++) {
            double angle = 2.0 * Math.PI * i / n;
            uVerts[i] = semiW * Math.cos(angle);
            vVerts[i] = semiH * Math.sin(angle);
        }

        if (Math.abs(denom) < 1e-6) {
            return BarrierRaycastHit.miss();
        }

        double t = distToPlane / denom;
        if (t < 0.0 || t > 1.0) {
            return BarrierRaycastHit.miss();
        }

        Vec3 impact = rayStart.add(rayDir.scale(t));
        Vec3 delta = impact.subtract(center);

        double uHit = delta.dot(tangent);
        double vHit = delta.dot(bitangent);

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            double eu = uVerts[next] - uVerts[i];
            double ev = vVerts[next] - vVerts[i];
            double wu = uHit - uVerts[i];
            double wv = vHit - vVerts[i];

            double cross = eu * wv - ev * wu;
            double edgeLen = Math.sqrt(eu * eu + ev * ev);
            double dist = cross / Math.max(1e-6, edgeLen);

            if (dist < -entityRadius) {
                return BarrierRaycastHit.miss();
            }
        }

        boolean fromFront = denom < 0.0;
        return new BarrierRaycastHit(true, t, impact, normal, fromFront);
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
        Vec3 normal = BarrierGeometry.getNormal(yRot, xRot);
        Vec3 tangent = BarrierGeometry.getTangent(yRot);
        Vec3 bitangent = normal.cross(tangent).normalize();

        int n = DEFAULT_VERTEX_COUNT;
        float semiW = Math.max(0.0F, width) * 0.5F;
        float semiH = Math.max(0.0F, height) * 0.5F;

        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < n; i++) {
            double angle = 2.0 * Math.PI * i / n;
            double u = semiW * Math.cos(angle);
            double v = semiH * Math.sin(angle);
            Vec3 p = center.add(tangent.scale(u)).add(bitangent.scale(v));

            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }

        double margin = 0.5;
        return new AABB(minX - margin, minY - margin, minZ - margin, maxX + margin, maxY + margin, maxZ + margin);
    }

    @Override
    public void render(
            Matrix4f matrix,
            VertexConsumer consumer,
            ForcefieldBarrierRenderer.BarrierRenderState state,
            PoseStack poseStack
    ) {
        int n = DEFAULT_VERTEX_COUNT;
        int rings = 3;
        float semiW = state.width * 0.5F;
        float semiH = state.height * 0.5F;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        Matrix4f rotMatrix = poseStack.last().pose();

        Vec3 normal = new Vec3(0, 0, 1);
        Vec3 viewDir = new Vec3(0, 0, 1);

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            double a0 = 2.0 * Math.PI * i / n;
            double a1 = 2.0 * Math.PI * next / n;

            float u0 = semiW * (float) Math.cos(a0);
            float v0 = semiH * (float) Math.sin(a0);
            float u1 = semiW * (float) Math.cos(a1);
            float v1 = semiH * (float) Math.sin(a1);

            for (int r = 0; r < rings; r++) {
                float f0 = (float) r / rings;
                float f1 = (float) (r + 1) / rings;

                float x0 = u0 * f0; float y0 = v0 * f0;
                float x1 = u1 * f0; float y1 = v1 * f0;
                float x2 = u1 * f1; float y2 = v1 * f1;
                float x3 = u0 * f1; float y3 = v0 * f1;

                ForcefieldShaderHelper.ColorResult c0 = ForcefieldShaderHelper.evaluateColor(x0 / semiW, y0 / semiH, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c1 = ForcefieldShaderHelper.evaluateColor(x1 / semiW, y1 / semiH, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c2 = ForcefieldShaderHelper.evaluateColor(x2 / semiW, y2 / semiH, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c3 = ForcefieldShaderHelper.evaluateColor(x3 / semiW, y3 / semiH, normal, viewDir, state.ageTicks, state.theme, 0, state.colorTint, !state.isActive);

                // Front Face
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y0, 0, c0, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y1, 0, c1, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x2, y2, 0, c2, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x3, y3, 0, c3, normal);

                // Back Face
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x3, y3, 0, c3, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x2, y2, 0, c2, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y1, 0, c1, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y0, 0, c0, normal.scale(-1));
            }
        }

        // --- Luminous Polygon Perimeter Rim (Eliminates edge-on 1-pixel disappearance) ---
        float rimZ = 0.03F;
        ForcefieldShaderHelper.ColorResult rimColor = ForcefieldShaderHelper.evaluateColor(
                0.9F, 0.9F, normal, viewDir, state.ageTicks, state.theme, 0.4F, state.colorTint, !state.isActive
        );
        rimColor = new ForcefieldShaderHelper.ColorResult(rimColor.r(), rimColor.g(), rimColor.b(), Math.max(0.75F, rimColor.a()));

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            double a0 = 2.0 * Math.PI * i / n;
            double a1 = 2.0 * Math.PI * next / n;

            float px0 = semiW * (float) Math.cos(a0);
            float py0 = semiH * (float) Math.sin(a0);
            float px1 = semiW * (float) Math.cos(a1);
            float py1 = semiH * (float) Math.sin(a1);

            Vec3 edgeDir = new Vec3(px1 - px0, py1 - py0, 0);
            Vec3 rimN = edgeDir.cross(normal).normalize();

            ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, px0, py0, -rimZ, rimColor, rimN);
            ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, px1, py1, -rimZ, rimColor, rimN);
            ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, px1, py1,  rimZ, rimColor, rimN);
            ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, px0, py0,  rimZ, rimColor, rimN);
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
        int n = DEFAULT_VERTEX_COUNT;
        float semiW = width * 0.5F;
        float semiH = height * 0.5F;
        ForcefieldShaderHelper.ColorResult color = new ForcefieldShaderHelper.ColorResult(r, g, b, a);
        Vec3 normal = new Vec3(0, 0, 1);

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            float x0 = semiW * (float) Math.cos(2.0 * Math.PI * i / n);
            float y0 = semiH * (float) Math.sin(2.0 * Math.PI * i / n);
            float x1 = semiW * (float) Math.cos(2.0 * Math.PI * next / n);
            float y1 = semiH * (float) Math.sin(2.0 * Math.PI * next / n);

            ForcefieldBarrierRenderer.addVertex(consumer, pose, 0, 0, 0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x0, y0, 0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, x1, y1, 0, color, normal);
            ForcefieldBarrierRenderer.addVertex(consumer, pose, 0, 0, 0, color, normal);
        }
    }
}
