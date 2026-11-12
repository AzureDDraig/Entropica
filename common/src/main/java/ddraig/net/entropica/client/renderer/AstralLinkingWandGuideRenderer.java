package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.item.AstralLinkingWandItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AstralLinkingWandGuideRenderer {

    public static void renderGuide(PoseStack poseStack, Matrix4f modelViewMatrix, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;
        ItemStack held = ItemStack.EMPTY;
        if (player.getMainHandItem().getItem() instanceof AstralLinkingWandItem) {
            held = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof AstralLinkingWandItem) {
            held = player.getOffhandItem();
        }

        if (held.isEmpty()) return;

        CustomData cd = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = cd.copyTag();

        if (!tag.getBoolean(AstralLinkingWandItem.TAG_HAS_SOURCE).orElse(false)) return;

        BlockPos sourcePos = new BlockPos(
                tag.getInt(AstralLinkingWandItem.TAG_SOURCE_X).orElse(0),
                tag.getInt(AstralLinkingWandItem.TAG_SOURCE_Y).orElse(0),
                tag.getInt(AstralLinkingWandItem.TAG_SOURCE_Z).orElse(0)
        );

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
                ? Vec3.atCenterOf(crosshairHit.getBlockPos())
                : crosshairHit.getLocation();

        Vec3 sourceCenter = new Vec3(sourcePos.getX() + 0.5, sourcePos.getY() + 0.5625, sourcePos.getZ() + 0.5);

        // Check if direct line-of-sight between source and target is clear
        BlockHitResult obstructionCheck = mc.level.clip(new ClipContext(
                sourceCenter, targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        boolean isClear = true;
        if (obstructionCheck.getType() == HitResult.Type.BLOCK) {
            BlockPos hitBlock = obstructionCheck.getBlockPos();
            if (!hitBlock.equals(sourcePos) && !hitBlock.equals(crosshairHit.getBlockPos())) {
                isClear = false;
            }
        }

        float r = isClear ? 0.4f : 1.0f;
        float g = isClear ? 0.9f : 0.25f;
        float b = isClear ? 1.0f : 0.25f;
        float a = 0.85f;

        Vec3 camPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        PoseStack.Pose entry = poseStack.last();

        // Render dotted trajectory line
        Vec3 dir = targetPos.subtract(sourceCenter);
        double totalDist = dir.length();
        if (totalDist > 0.1) {
            Vec3 norm = dir.normalize();
            double step = 0.5;
            for (double d = 0; d < totalDist; d += step * 2) {
                double dEnd = Math.min(d + step, totalDist);
                Vec3 p1 = sourceCenter.add(norm.scale(d));
                Vec3 p2 = sourceCenter.add(norm.scale(dEnd));

                lineConsumer.addVertex(entry, (float) p1.x, (float) p1.y, (float) p1.z)
                        .setColor(r, g, b, a)
                        .setNormal(entry, 0, 1, 0);
                lineConsumer.addVertex(entry, (float) p2.x, (float) p2.y, (float) p2.z)
                        .setColor(r, g, b, a)
                        .setNormal(entry, 0, 1, 0);
            }
        }

        // Render target box outline if hovering over a block
        if (crosshairHit.getType() == HitResult.Type.BLOCK) {
            BlockPos targetBlockPos = crosshairHit.getBlockPos();
            float minX = targetBlockPos.getX();
            float minY = targetBlockPos.getY();
            float minZ = targetBlockPos.getZ();
            float maxX = minX + 1.0f;
            float maxY = minY + 1.0f;
            float maxZ = minZ + 1.0f;

            renderBoxOutline(entry, lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a);
        }

        // Render source box outline
        renderBoxOutline(entry, lineConsumer, sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(),
                sourcePos.getX() + 1.0f, sourcePos.getY() + 1.0f, sourcePos.getZ() + 1.0f,
                0.3f, 0.8f, 1.0f, 0.9f);

        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());
    }

    private static void renderBoxOutline(PoseStack.Pose entry, VertexConsumer vc,
                                         float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ,
                                         float r, float g, float b, float a) {
        // Bottom ring
        addLine(entry, vc, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(entry, vc, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(entry, vc, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(entry, vc, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Top ring
        addLine(entry, vc, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(entry, vc, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(entry, vc, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addLine(entry, vc, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // Vertical pillars
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
