package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.EidolicShadowEntity;
import ddraig.net.entropica.item.EidolonPathmarkerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Entropica.MODID, value = Dist.CLIENT)
public class EidolicPathRenderer {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    // FIXED: Subscribed to the specific .AfterEntities subclass required by NeoForge 1.21.10!
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterEntities event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean holdsMarker = mainHand.getItem() instanceof EidolonPathmarkerItem || offHand.getItem() instanceof EidolonPathmarkerItem;

        // Only draw the holographic paths if the player is holding the Pathmarker tool!
        if (!holdsMarker) return;

        PoseStack poseStack = event.getPoseStack();

        // Safely pull the Camera directly from the GameRenderer
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
        // Shift matrix to 0,0,0 world origin for absolute coordinate rendering
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WHITE_TEXTURE));

        // 1. Draw EXISTING Shadows' Paths (Cyan color)
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EidolicShadowEntity shadow) {
                List<BlockPos> waypoints = shadow.getWaypoints();
                renderPath(poseStack, consumer, waypoints, 0.0f, 1.0f, 1.0f, 0.4f);
            }
        }

        // 2. Draw the CURRENT Path being built (Purple/Pink color)
        ItemStack marker = mainHand.getItem() instanceof EidolonPathmarkerItem ? mainHand : offHand;
        CustomData data = marker.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        int size = tag.getInt("PathSize").orElse(0);

        if (size > 0) {
            List<BlockPos> currentPath = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                currentPath.add(BlockPos.of(tag.getLong("Path_" + i).orElse(0L)));
            }
            renderPath(poseStack, consumer, currentPath, 0.8f, 0.2f, 1.0f, 0.8f);
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static void renderPath(PoseStack poseStack, VertexConsumer consumer, List<BlockPos> path, float r, float g, float b, float a) {
        if (path.isEmpty()) return;

        int light = 15728880; // Max Light (Glow in the dark)

        // Draw small glowing cubes at each recorded Block Node
        for (BlockPos pos : path) {
            poseStack.pushPose();
            poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            // Subtle rotation pulse for the nodes
            float time = (System.currentTimeMillis() % 360000L) / 20.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(time));

            drawCube(poseStack, consumer, 0.2f, 0.2f, 0.2f, 0, 0, 0, (int)(r*255), (int)(g*255), (int)(b*255), (int)(a*255), light);
            poseStack.popPose();
        }

        // Draw connecting glowing beams between the nodes
        for (int i = 0; i < path.size() - 1; i++) {
            Vec3 p1 = new Vec3(path.get(i).getX() + 0.5, path.get(i).getY() + 0.5, path.get(i).getZ() + 0.5);
            Vec3 p2 = new Vec3(path.get(i+1).getX() + 0.5, path.get(i+1).getY() + 0.5, path.get(i+1).getZ() + 0.5);
            drawBeam(consumer, poseStack.last().pose(), p1, p2, 0.05f, r, g, b, a, light);
        }
    }

    private static void drawBeam(VertexConsumer consumer, Matrix4f matrix, Vec3 start, Vec3 end, float width, float r, float g, float b, float a, int light) {
        Vec3 dir = end.subtract(start);
        if (dir.lengthSqr() < 0.0001) return;
        dir = dir.normalize();

        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dir.y) > 0.9) up = new Vec3(1, 0, 0);

        Vec3 right = dir.cross(up).normalize();
        Vec3 upOrth = right.cross(dir).normalize();

        Vec3[] offsets = { right.scale(width), upOrth.scale(width), right.scale(-width), upOrth.scale(-width) };

        for (int i = 0; i < 4; i++) {
            Vec3 o1 = offsets[i];
            Vec3 o2 = offsets[(i + 1) % 4];

            consumer.addVertex(matrix, (float)(start.x + o1.x), (float)(start.y + o1.y), (float)(start.z + o1.z)).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
            consumer.addVertex(matrix, (float)(end.x + o1.x), (float)(end.y + o1.y), (float)(end.z + o1.z)).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
            consumer.addVertex(matrix, (float)(end.x + o2.x), (float)(end.y + o2.y), (float)(end.z + o2.z)).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
            consumer.addVertex(matrix, (float)(start.x + o2.x), (float)(start.y + o2.y), (float)(start.z + o2.z)).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
        }
    }

    private static void drawCube(PoseStack poseStack, VertexConsumer consumer, float width, float height, float depth, float offsetX, float offsetY, float offsetZ, int r, int g, int b, int a, int light) {
        Matrix4f pose = poseStack.last().pose();
        float hw = width / 2f;
        float hd = depth / 2f;

        float x0 = offsetX - hw, x1 = offsetX + hw;
        float y0 = offsetY - height/2f, y1 = offsetY + height/2f;
        float z0 = offsetZ - hd, z1 = offsetZ + hd;

        drawQuad(consumer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a, light);
        drawQuad(consumer, pose, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a, light);
        drawQuad(consumer, pose, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a, light);
        drawQuad(consumer, pose, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a, light);
        drawQuad(consumer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a, light);
        drawQuad(consumer, pose, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a, light);
    }

    private static void drawQuad(VertexConsumer consumer, Matrix4f pose, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a, int light) {
        consumer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }
}