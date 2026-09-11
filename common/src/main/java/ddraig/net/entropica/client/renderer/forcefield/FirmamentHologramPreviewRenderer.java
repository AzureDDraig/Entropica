package ddraig.net.entropica.client.renderer.forcefield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.forcefield.BarrierFieldManager;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.forcefield.SnapResult;
import ddraig.net.entropica.item.FirmamentWeaverItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Holographic in-world wireframe preview renderer for Firmament Weaver barrier placement.
 * Renders real-time placement geometries, anchor point beacon, dynamic guide lines,
 * and snap alignment feedback (Cyan = normal, Emerald = snapped, Crimson = obstructed).
 */
public class FirmamentHologramPreviewRenderer {

    public static void renderPreview(PoseStack poseStack, Matrix4f modelViewMatrix, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;
        ItemStack held = ItemStack.EMPTY;
        if (player.getMainHandItem().getItem() instanceof FirmamentWeaverItem) {
            held = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof FirmamentWeaverItem) {
            held = player.getOffhandItem();
        }

        if (held.isEmpty()) return;

        Vec3 anchorPos = FirmamentWeaverItem.getAnchor(held).orElse(null);
        boolean hasAnchor = anchorPos != null;
        BarrierShape shape = FirmamentWeaverItem.getShape(held);

        Vec3 eyePos = player.getEyePosition(partialTick);
        Vec3 lookVec = player.getViewVector(partialTick);
        Vec3 targetRayEnd = eyePos.add(lookVec.scale(32.0));

        BlockHitResult crosshairHit = mc.level.clip(new ClipContext(
                eyePos, targetRayEnd,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        Vec3 targetPos = crosshairHit.getType() == HitResult.Type.BLOCK
                ? crosshairHit.getLocation()
                : targetRayEnd;

        Vec3 center;
        float yaw;
        float pitch = 0.0F;
        float width = 4.0F;
        float height = 4.0F;
        float radius = 3.0F;
        boolean obstructed = false;
        boolean isSnapped = false;
        SnapResult snap;

        if (hasAnchor && anchorPos != null) {
            // If player has moved beyond maximum allowed span (32m), do not render preview
            if (player.position().distanceTo(anchorPos) > 32.0) {
                return;
            }

            // Only stretch the preview wireframe when actually aiming at a block surface
            boolean aimingAtSurface = crosshairHit.getType() == HitResult.Type.BLOCK;
            if (!aimingAtSurface) {
                // Aiming into empty sky: render only the Point A beacon and a subtle guide line towards player
                Vec3 camPos = camera.getPosition();
                MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());

                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                PoseStack.Pose worldEntry = poseStack.last();

                float bSize = 0.25F;
                renderBoxOutline(worldEntry, vc,
                        (float) (anchorPos.x - bSize), (float) (anchorPos.y - bSize), (float) (anchorPos.z - bSize),
                        (float) (anchorPos.x + bSize), (float) (anchorPos.y + bSize), (float) (anchorPos.z + bSize),
                        1.0F, 0.85F, 0.20F, 0.90F);

                poseStack.popPose();
                bufferSource.endBatch(RenderType.lines());
                return;
            }

            Vec3 span = targetPos.subtract(anchorPos);
            double horizDist = span.horizontalDistance();
            double vertDist = Math.abs(span.y);
            double totalDist = span.length();

            if (totalDist < 1.0 || totalDist > 32.0) {
                obstructed = true;
            }

            if (horizDist > 0.05) {
                center = anchorPos.add(span.scale(0.5));
                yaw = (float) (Math.atan2(span.z, span.x) * 180.0 / Math.PI);
                width = (float) Math.min(32.0, Math.max(1.0, horizDist));
                height = (float) Math.min(32.0, Math.max(1.0, vertDist));
            } else {
                center = anchorPos.add(0, span.y * 0.5, 0);
                yaw = player.getYRot();
                width = 4.0F;
                height = (float) Math.min(32.0, Math.max(1.0, vertDist));
            }
            radius = (float) Math.min(16.0, Math.max(0.5, totalDist * 0.5));

            snap = BarrierFieldManager.findSnapAlignment(mc.level, center, yaw, pitch, width, height, shape, 0.5);
            if (snap.isSnapped()) {
                isSnapped = true;
                center = snap.snappedPos();
                yaw = snap.snappedYaw();
                pitch = snap.snappedPitch();
            }
        } else {
            // Free placement: only render if aiming at a block or crouching
            if (crosshairHit.getType() != HitResult.Type.BLOCK && !player.isShiftKeyDown()) {
                return;
            }

            snap = BarrierFieldManager.findSnapAlignment(mc.level, targetPos, player.getYRot(), 0.0F, width, height, shape, 0.5);
            if (snap.isSnapped()) {
                isSnapped = true;
                center = snap.snappedPos();
                yaw = snap.snappedYaw();
                pitch = snap.snappedPitch();
            } else {
                center = targetPos;
                yaw = player.getYRot();
                pitch = 0.0F;
            }
        }

        // Color coding: Snapped = Emerald, Obstructed = Crimson, Normal = Starlight Cyan
        float r, g, b, a;
        if (isSnapped) {
            r = 0.20F; g = 1.00F; b = 0.50F; a = 0.75F; // Emerald Green
        } else if (obstructed) {
            r = 1.00F; g = 0.20F; b = 0.20F; a = 0.65F; // Crimson Warning
        } else {
            r = 0.30F; g = 0.85F; b = 1.00F; a = 0.55F; // Starlight Cyan
        }

        Vec3 camPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        PoseStack.Pose worldEntry = poseStack.last();

        // If Point A is active, render Anchor beacon & connecting stretch guide line
        if (hasAnchor && anchorPos != null) {
            float bSize = 0.25F;
            renderBoxOutline(worldEntry, vc,
                    (float) (anchorPos.x - bSize), (float) (anchorPos.y - bSize), (float) (anchorPos.z - bSize),
                    (float) (anchorPos.x + bSize), (float) (anchorPos.y + bSize), (float) (anchorPos.z + bSize),
                    1.0F, 0.85F, 0.20F, 0.90F);

            addLine(worldEntry, vc,
                    (float) anchorPos.x, (float) anchorPos.y, (float) anchorPos.z,
                    (float) targetPos.x, (float) targetPos.y, (float) targetPos.z,
                    r, g, b, 0.85F);
        }

        // Render shape wireframe at placement center
        poseStack.pushPose();
        poseStack.translate(center.x, center.y, center.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        PoseStack.Pose shapeEntry = poseStack.last();

        switch (shape) {
            case PLANAR_QUAD -> renderPlanarQuad(shapeEntry, vc, width, height, r, g, b, a);
            case CIRCULAR_DISC -> renderCircularDisc(shapeEntry, vc, radius, r, g, b, a);
            case HEMISPHERICAL_DOME -> renderHemisphericalDome(shapeEntry, vc, radius, r, g, b, a);
            case SPHERICAL_BUBBLE -> renderSphericalBubble(shapeEntry, vc, radius, r, g, b, a);
            case CYLINDER -> renderCylinder(shapeEntry, vc, radius, height, r, g, b, a);
            case CONVEX_POLYGON -> renderConvexPolygon(shapeEntry, vc, width, height, r, g, b, a);
        }

        poseStack.popPose();
        poseStack.popPose();

        bufferSource.endBatch(RenderType.lines());
    }

    private static void renderPlanarQuad(PoseStack.Pose entry, VertexConsumer vc, float width, float height, float r, float g, float b, float a) {
        float halfW = width * 0.5F;
        float halfH = height * 0.5F;

        // Perimeter
        addLine(entry, vc, -halfW, -halfH, 0,  halfW, -halfH, 0, r, g, b, a);
        addLine(entry, vc,  halfW, -halfH, 0,  halfW,  halfH, 0, r, g, b, a);
        addLine(entry, vc,  halfW,  halfH, 0, -halfW,  halfH, 0, r, g, b, a);
        addLine(entry, vc, -halfW,  halfH, 0, -halfW, -halfH, 0, r, g, b, a);

        // Sub-grid cross braces
        addLine(entry, vc, -halfW, 0, 0,  halfW, 0, 0, r, g, b, a * 0.6F);
        addLine(entry, vc, 0, -halfH, 0, 0,  halfH, 0, r, g, b, a * 0.6F);
        addLine(entry, vc, -halfW, -halfH, 0,  halfW,  halfH, 0, r, g, b, a * 0.4F);
        addLine(entry, vc, -halfW,  halfH, 0,  halfW, -halfH, 0, r, g, b, a * 0.4F);
    }

    private static void renderCircularDisc(PoseStack.Pose entry, VertexConsumer vc, float radius, float r, float g, float b, float a) {
        int segs = 32;
        for (int i = 0; i < segs; i++) {
            float a0 = 2.0F * (float) Math.PI * i / segs;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / segs;
            float x0 = radius * Mth.cos(a0);
            float y0 = radius * Mth.sin(a0);
            float x1 = radius * Mth.cos(a1);
            float y1 = radius * Mth.sin(a1);

            // Outer hoop
            addLine(entry, vc, x0, y0, 0, x1, y1, 0, r, g, b, a);
            // Inner concentric hoop
            addLine(entry, vc, x0 * 0.5F, y0 * 0.5F, 0, x1 * 0.5F, y1 * 0.5F, 0, r, g, b, a * 0.5F);
        }
        // Radial cross spokes
        addLine(entry, vc, -radius, 0, 0, radius, 0, 0, r, g, b, a * 0.6F);
        addLine(entry, vc, 0, -radius, 0, 0, radius, 0, r, g, b, a * 0.6F);
    }

    private static void renderHemisphericalDome(PoseStack.Pose entry, VertexConsumer vc, float radius, float r, float g, float b, float a) {
        int segs = 32;
        float rMid = radius * 0.7071F;
        float yMid = radius * 0.7071F;

        for (int i = 0; i < segs; i++) {
            float a0 = 2.0F * (float) Math.PI * i / segs;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / segs;

            // Base circle (y = 0)
            addLine(entry, vc, radius * Mth.cos(a0), 0, radius * Mth.sin(a0),
                    radius * Mth.cos(a1), 0, radius * Mth.sin(a1), r, g, b, a);

            // 45-degree elevation ring
            addLine(entry, vc, rMid * Mth.cos(a0), yMid, rMid * Mth.sin(a0),
                    rMid * Mth.cos(a1), yMid, rMid * Mth.sin(a1), r, g, b, a * 0.6F);
        }

        // 8 Vertical dome ribs
        int ribs = 8;
        int arcSteps = 16;
        for (int rib = 0; rib < ribs; rib++) {
            float angle = 2.0F * (float) Math.PI * rib / ribs;
            float cosA = Mth.cos(angle);
            float sinA = Mth.sin(angle);

            for (int s = 0; s < arcSteps; s++) {
                float lat0 = ((float) Math.PI * 0.5F) * (float) s / arcSteps;
                float lat1 = ((float) Math.PI * 0.5F) * (float) (s + 1) / arcSteps;

                float x0 = radius * Mth.cos(lat0) * cosA;
                float z0 = radius * Mth.cos(lat0) * sinA;
                float y0 = radius * Mth.sin(lat0);

                float x1 = radius * Mth.cos(lat1) * cosA;
                float z1 = radius * Mth.cos(lat1) * sinA;
                float y1 = radius * Mth.sin(lat1);

                addLine(entry, vc, x0, y0, z0, x1, y1, z1, r, g, b, a * 0.75F);
            }
        }
    }

    private static void renderSphericalBubble(PoseStack.Pose entry, VertexConsumer vc, float radius, float r, float g, float b, float a) {
        int segs = 32;
        float rLat = radius * 0.866F;
        float yLat = radius * 0.5F;

        for (int i = 0; i < segs; i++) {
            float a0 = 2.0F * (float) Math.PI * i / segs;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / segs;
            float c0 = radius * Mth.cos(a0);
            float s0 = radius * Mth.sin(a0);
            float c1 = radius * Mth.cos(a1);
            float s1 = radius * Mth.sin(a1);

            // Equator (XZ)
            addLine(entry, vc, c0, 0, s0, c1, 0, s1, r, g, b, a);
            // Prime meridian (XY)
            addLine(entry, vc, c0, s0, 0, c1, s1, 0, r, g, b, a);
            // Transverse meridian (YZ)
            addLine(entry, vc, 0, c0, s0, 0, c1, s1, r, g, b, a);

            // North and South latitude rings
            addLine(entry, vc, rLat * Mth.cos(a0),  yLat, rLat * Mth.sin(a0),
                    rLat * Mth.cos(a1),  yLat, rLat * Mth.sin(a1), r, g, b, a * 0.5F);
            addLine(entry, vc, rLat * Mth.cos(a0), -yLat, rLat * Mth.sin(a0),
                    rLat * Mth.cos(a1), -yLat, rLat * Mth.sin(a1), r, g, b, a * 0.5F);
        }
    }

    private static void renderCylinder(PoseStack.Pose entry, VertexConsumer vc, float radius, float height, float r, float g, float b, float a) {
        float halfH = height * 0.5F;
        int segs = 24;

        for (int i = 0; i < segs; i++) {
            float a0 = 2.0F * (float) Math.PI * i / segs;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / segs;
            float x0 = radius * Mth.cos(a0);
            float z0 = radius * Mth.sin(a0);
            float x1 = radius * Mth.cos(a1);
            float z1 = radius * Mth.sin(a1);

            // Bottom and top rings
            addLine(entry, vc, x0, -halfH, z0, x1, -halfH, z1, r, g, b, a);
            addLine(entry, vc, x0,  halfH, z0, x1,  halfH, z1, r, g, b, a);

            // Mid belt
            addLine(entry, vc, x0, 0, z0, x1, 0, z1, r, g, b, a * 0.4F);

            // Vertical ribs
            if (i % 2 == 0) {
                addLine(entry, vc, x0, -halfH, z0, x0, halfH, z0, r, g, b, a * 0.7F);
            }
        }
    }

    private static void renderConvexPolygon(PoseStack.Pose entry, VertexConsumer vc, float width, float height, float r, float g, float b, float a) {
        float halfW = width * 0.5F;
        float halfH = height * 0.5F;
        int sides = 6;

        for (int i = 0; i < sides; i++) {
            float a0 = 2.0F * (float) Math.PI * i / sides;
            float a1 = 2.0F * (float) Math.PI * (i + 1) / sides;
            float x0 = halfW * Mth.cos(a0);
            float z0 = halfW * Mth.sin(a0);
            float x1 = halfW * Mth.cos(a1);
            float z1 = halfW * Mth.sin(a1);

            // Bottom and top polygon rims
            addLine(entry, vc, x0, -halfH, z0, x1, -halfH, z1, r, g, b, a);
            addLine(entry, vc, x0,  halfH, z0, x1,  halfH, z1, r, g, b, a);

            // Corner pillars
            addLine(entry, vc, x0, -halfH, z0, x0,  halfH, z0, r, g, b, a);

            // Center spokes
            addLine(entry, vc, 0, -halfH, 0, x0, -halfH, z0, r, g, b, a * 0.4F);
            addLine(entry, vc, 0,  halfH, 0, x0,  halfH, z0, r, g, b, a * 0.4F);
        }
    }

    private static void renderBoxOutline(PoseStack.Pose entry, VertexConsumer vc,
                                         float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ,
                                         float r, float g, float b, float a) {
        // Bottom
        addLine(entry, vc, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(entry, vc, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(entry, vc, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(entry, vc, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Top
        addLine(entry, vc, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(entry, vc, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(entry, vc, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addLine(entry, vc, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // Pillars
        addLine(entry, vc, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        addLine(entry, vc, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(entry, vc, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(entry, vc, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void addLine(PoseStack.Pose entry, VertexConsumer vc,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float r, float g, float b, float a) {
        vc.addVertex(entry, x1, y1, z1).setColor(r, g, b, a).setNormal(entry, 0, 1, 0);
        vc.addVertex(entry, x2, y2, z2).setColor(r, g, b, a).setNormal(entry, 0, 1, 0);
    }
}
