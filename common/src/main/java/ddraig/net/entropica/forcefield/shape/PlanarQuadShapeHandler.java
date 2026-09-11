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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Geometric shape handler for rectangular planar forcefield sheets (Planar Quad).
 */
public class PlanarQuadShapeHandler implements BarrierShapeHandler {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("entropica", "planar_quad");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int getOrdinal() {
        return 0;
    }

    @Override
    public String getDisplayName() {
        return "Planar Quad";
    }

    @Override
    public String getDescription() {
        return "Flat rectangular wall, floor, or angled pane";
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
        return BarrierGeometry.intersectPlanarQuad(center, yRot, xRot, width, height, rayStart, rayEnd, entityRadius);
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

        float halfW = width * 0.5F;
        float halfH = height * 0.5F;

        Vec3[] corners = new Vec3[]{
                center.add(tangent.scale(halfW)).add(bitangent.scale(halfH)),
                center.add(tangent.scale(-halfW)).add(bitangent.scale(halfH)),
                center.add(tangent.scale(halfW)).add(bitangent.scale(-halfH)),
                center.add(tangent.scale(-halfW)).add(bitangent.scale(-halfH))
        };

        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (Vec3 c : corners) {
            minX = Math.min(minX, c.x);
            minY = Math.min(minY, c.y);
            minZ = Math.min(minZ, c.z);
            maxX = Math.max(maxX, c.x);
            maxY = Math.max(maxY, c.y);
            maxZ = Math.max(maxZ, c.z);
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

                float r0 = ForcefieldShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x0, y0, 0), state.ripples, (long) state.ageTicks, 0);
                float r1 = ForcefieldShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x1, y0, 0), state.ripples, (long) state.ageTicks, 0);
                float r2 = ForcefieldShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x1, y1, 0), state.ripples, (long) state.ageTicks, 0);
                float r3 = ForcefieldShaderHelper.evaluateRippleOffset(state.entityWorldPos.add(x0, y1, 0), state.ripples, (long) state.ageTicks, 0);

                ForcefieldShaderHelper.ColorResult c0 = ForcefieldShaderHelper.evaluateColor(u0, v0, normal, viewDir, state.ageTicks, state.theme, Math.abs(r0), state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c1 = ForcefieldShaderHelper.evaluateColor(u1, v0, normal, viewDir, state.ageTicks, state.theme, Math.abs(r1), state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c2 = ForcefieldShaderHelper.evaluateColor(u1, v1, normal, viewDir, state.ageTicks, state.theme, Math.abs(r2), state.colorTint, !state.isActive);
                ForcefieldShaderHelper.ColorResult c3 = ForcefieldShaderHelper.evaluateColor(u0, v1, normal, viewDir, state.ageTicks, state.theme, Math.abs(r3), state.colorTint, !state.isActive);

                // Front Face
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y0, r0, c0, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y0, r1, c1, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y1, r2, c2, normal);
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y1, r3, c3, normal);

                // Back Face (Reverse winding)
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y1, r3, c3, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y1, r2, c2, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x1, y0, r1, c1, normal.scale(-1));
                ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, x0, y0, r0, c0, normal.scale(-1));
            }
        }

        // --- Luminous Perimeter Edge Rim (Eliminates edge-on 1-pixel disappearance) ---
        float rimZ = 0.03F; // 0.06m total bevel depth along normal
        ForcefieldShaderHelper.ColorResult rimColor = ForcefieldShaderHelper.evaluateColor(
                0.9F, 0.9F, normal, viewDir, state.ageTicks, state.theme, 0.4F, state.colorTint, !state.isActive
        );
        rimColor = new ForcefieldShaderHelper.ColorResult(rimColor.r(), rimColor.g(), rimColor.b(), Math.max(0.75F, rimColor.a()));

        Vec3 topN = new Vec3(0, 1, 0);
        Vec3 botN = new Vec3(0, -1, 0);
        Vec3 leftN = new Vec3(-1, 0, 0);
        Vec3 rightN = new Vec3(1, 0, 0);

        // Top Edge Rim
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW, halfH, -rimZ, rimColor, topN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix,  halfW, halfH, -rimZ, rimColor, topN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix,  halfW, halfH,  rimZ, rimColor, topN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW, halfH,  rimZ, rimColor, topN);

        // Bottom Edge Rim
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW, -halfH,  rimZ, rimColor, botN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix,  halfW, -halfH,  rimZ, rimColor, botN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix,  halfW, -halfH, -rimZ, rimColor, botN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW, -halfH, -rimZ, rimColor, botN);

        // Left Edge Rim
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW, -halfH, -rimZ, rimColor, leftN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW,  halfH, -rimZ, rimColor, leftN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW,  halfH,  rimZ, rimColor, leftN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, -halfW, -halfH,  rimZ, rimColor, leftN);

        // Right Edge Rim
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, halfW, -halfH,  rimZ, rimColor, rightN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, halfW,  halfH,  rimZ, rimColor, rightN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, halfW,  halfH, -rimZ, rimColor, rightN);
        ForcefieldBarrierRenderer.addVertex(consumer, rotMatrix, halfW, -halfH, -rimZ, rimColor, rightN);

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
        float halfW = width * 0.5F;
        float halfH = height * 0.5F;
        ForcefieldShaderHelper.ColorResult color = new ForcefieldShaderHelper.ColorResult(r, g, b, a);
        Vec3 normal = new Vec3(0, 0, 1);

        ForcefieldBarrierRenderer.addVertex(consumer, pose, -halfW, -halfH, 0, color, normal);
        ForcefieldBarrierRenderer.addVertex(consumer, pose, halfW, -halfH, 0, color, normal);
        ForcefieldBarrierRenderer.addVertex(consumer, pose, halfW, halfH, 0, color, normal);
        ForcefieldBarrierRenderer.addVertex(consumer, pose, -halfW, halfH, 0, color, normal);
    }
}
