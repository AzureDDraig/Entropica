package ddraig.net.entropica.forcefield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.client.renderer.forcefield.ForcefieldBarrierRenderer;
import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Pluggable handler interface for forcefield barrier geometric primitives.
 * Encapsulates swept-ray intersection, bounding box computation, mesh tessellation, and preview rendering.
 */
public interface BarrierShapeHandler {

    ResourceLocation getId();

    int getOrdinal();

    String getDisplayName();

    String getDescription();

    BarrierRaycastHit intersect(
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
    );

    AABB computeBoundingBox(
            Vec3 center,
            float yRot,
            float xRot,
            float width,
            float height,
            float radius
    );

    void render(
            Matrix4f matrix,
            VertexConsumer consumer,
            ForcefieldBarrierRenderer.BarrierRenderState state,
            PoseStack poseStack
    );

    void renderPreview(
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
    );

    default void applyDefaultDimensions(ForcefieldBarrierEntity barrier) {}
}
