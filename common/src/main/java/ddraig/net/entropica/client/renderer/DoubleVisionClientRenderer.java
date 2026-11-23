package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Client-side 3D World & 2D HUD rendering system for Biome #13's {@code entropica:double_vision} effect.
 * - Suppressed cleanly whenever the player wears {@code entropica:mirage_veil_goggles} on {@link EquipmentSlot#HEAD}.
 * - 3D World Pass: renders twin laterally-drifting translucent prism/wireframe ghost silhouettes
 *   (-offset in mirage cyan #38BDF8, +offset in sun-amber #FB923C) for up to 90 nearest exposed surface blocks
 *   within 7 blocks ahead of the camera and nearby LivingEntities within 16 blocks.
 * - 2D HUD Pass: renders twin drifting ghost crosshairs, overlapping binocular prism rings, and subtle
 *   mirage heat-haze edge shimmer.
 */
public final class DoubleVisionClientRenderer {

    private static final ResourceLocation DOUBLE_VISION_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "double_vision");
    private static final ResourceLocation MIRAGE_VEIL_GOGGLES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_veil_goggles");

    private static final int MAX_GHOST_BLOCKS = 90;
    private static final Direction[] CARDINAL_DIRS = Direction.values();

    // Mirage Cyan (#38BDF8) & Sun-Amber (#FB923C)
    private static final float CYAN_R = 0.220F;
    private static final float CYAN_G = 0.741F;
    private static final float CYAN_B = 0.973F;

    private static final float AMBER_R = 0.984F;
    private static final float AMBER_G = 0.573F;
    private static final float AMBER_B = 0.235F;

    private record OffsetEntry(int dx, int dy, int dz, float distSq, float invLen) {}

    private static final OffsetEntry[] SORTED_NEAR_OFFSETS = buildSortedOffsets(7);

    private DoubleVisionClientRenderer() {
    }

    private static OffsetEntry[] buildSortedOffsets(int radius) {
        List<OffsetEntry> list = new ArrayList<>();
        int rSqMax = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int dSq = dx * dx + dy * dy + dz * dz;
                    if (dSq == 0 || dSq > rSqMax) {
                        continue;
                    }
                    float invLen = (float) (1.0D / Math.sqrt(dSq));
                    list.add(new OffsetEntry(dx, dy, dz, (float) dSq, invLen));
                }
            }
        }
        list.sort(Comparator.comparingDouble(OffsetEntry::distSq));
        return list.toArray(new OffsetEntry[0]);
    }

    public static boolean isWearingMirageVeilGoggles(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return !head.isEmpty() && MIRAGE_VEIL_GOGGLES_ID.equals(BuiltInRegistries.ITEM.getKey(head.getItem()));
    }

    public static boolean isDoubleVisionActive(Player player) {
        if (player == null || isWearingMirageVeilGoggles(player)) {
            return false;
        }
        Optional<MobEffect> effOpt = BuiltInRegistries.MOB_EFFECT.getOptional(DOUBLE_VISION_ID);
        if (effOpt.isPresent()) {
            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effOpt.get());
            if (player.hasEffect(holder)) {
                return true;
            }
        }
        for (MobEffectInstance active : player.getActiveEffects()) {
            if (active.getEffect().unwrapKey().map(k -> DOUBLE_VISION_ID.equals(k.location())).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 1) 3D World Double-Image Ghosting:
     * Renders twin shifted mirage-cyan (-offset) & sun-amber (+offset) prism wireframes for visible
     * surface blocks ahead of the player and nearby creatures.
     */
    public static void renderWorldDoubleVision(PoseStack poseStack, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || poseStack == null || camera == null) {
            return;
        }
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        Player player = mc.player;
        if (!isDoubleVisionActive(player)) {
            return;
        }

        Level level = mc.level;
        float t = ((level.getGameTime() % 72000L) + partialTick) * 0.05F;

        Vec3 look = player.getViewVector(partialTick);
        Vec3 viewRight = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (viewRight.lengthSqr() < 1.0E-4D) {
            viewRight = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            viewRight = viewRight.normalize();
        }

        double lateralMag = 0.42D + 0.26D * Math.sin(t * 2.1D);
        float offX = (float) (viewRight.x * lateralMag);
        float offY = (float) (0.08D * Math.cos(t * 1.5D));
        float offZ = (float) (viewRight.z * lateralMag);

        Vec3 camPos = camera.getPosition();
        BlockPos eyeBlock = BlockPos.containing(camPos);
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos adjPos = new BlockPos.MutableBlockPos();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

        float lookX = (float) look.x;
        float lookY = (float) look.y;
        float lookZ = (float) look.z;

        int renderedBlocks = 0;
        for (OffsetEntry entry : SORTED_NEAR_OFFSETS) {
            float dot = (entry.dx * lookX + entry.dy * lookY + entry.dz * lookZ) * entry.invLen;
            if (dot <= 0.25F) {
                continue;
            }

            checkPos.set(eyeBlock.getX() + entry.dx, eyeBlock.getY() + entry.dy, eyeBlock.getZ() + entry.dz);
            BlockState state = level.getBlockState(checkPos);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }

            if (!isExposedSurfaceBlock(level, checkPos, adjPos)) {
                continue;
            }

            VoxelShape shape = state.getShape(level, checkPos);
            AABB localBox = shape.isEmpty() ? new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) : shape.bounds();

            float bx0 = (float) (checkPos.getX() + localBox.minX);
            float by0 = (float) (checkPos.getY() + localBox.minY);
            float bz0 = (float) (checkPos.getZ() + localBox.minZ);
            float bx1 = (float) (checkPos.getX() + localBox.maxX);
            float by1 = (float) (checkPos.getY() + localBox.maxY);
            float bz1 = (float) (checkPos.getZ() + localBox.maxZ);

            float distFactor = 1.0F - Mth.clamp(entry.distSq / 49.0F, 0.0F, 1.0F);
            float shimmer = 0.85F + 0.15F * Mth.sin(t * 3.4F + (entry.dx + entry.dz) * 0.45F);
            float alpha = (0.16F + 0.32F * distFactor) * shimmer;
            boolean drawPrismDiagonal = entry.distSq <= 18.0F;

            // Left ghost (-offset) in Mirage Cyan (#38BDF8)
            drawPrismBox(lineConsumer, mat,
                    bx0 - offX, by0 - offY, bz0 - offZ,
                    bx1 - offX, by1 - offY, bz1 - offZ,
                    CYAN_R, CYAN_G, CYAN_B, alpha, drawPrismDiagonal);

            // Right ghost (+offset) in Sun-Amber (#FB923C)
            drawPrismBox(lineConsumer, mat,
                    bx0 + offX, by0 + offY, bz0 + offZ,
                    bx1 + offX, by1 + offY, bz1 + offZ,
                    AMBER_R, AMBER_G, AMBER_B, alpha, drawPrismDiagonal);

            if (++renderedBlocks >= MAX_GHOST_BLOCKS) {
                break;
            }
        }

        // Nearby LivingEntities within 16 blocks split into vivid twin ghost silhouettes
        float entOffX = offX * 1.25F;
        float entOffY = offY * 1.15F;
        float entOffZ = offZ * 1.25F;

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(16.0D),
                e -> e != player && e.isAlive() && !e.isInvisible()
        );
        for (LivingEntity entity : entities) {
            double ex = Mth.lerp(partialTick, entity.xo, entity.getX());
            double ey = Mth.lerp(partialTick, entity.yo, entity.getY());
            double ez = Mth.lerp(partialTick, entity.zo, entity.getZ());

            Vec3 toEnt = new Vec3(ex - camPos.x, (ey + entity.getBbHeight() * 0.5D) - camPos.y, ez - camPos.z);
            double distSq = toEnt.lengthSqr();
            if (distSq < 0.05D || distSq > 256.0D) {
                continue;
            }
            if (toEnt.normalize().dot(look) <= 0.05D) {
                continue;
            }

            AABB bb = entity.getDimensions(entity.getPose()).makeBoundingBox(ex, ey, ez);
            float x0 = (float) bb.minX;
            float y0 = (float) bb.minY;
            float z0 = (float) bb.minZ;
            float x1 = (float) bb.maxX;
            float y1 = (float) bb.maxY;
            float z1 = (float) bb.maxZ;

            float prox = 1.0F - Mth.clamp((float) (distSq / 256.0D), 0.0F, 1.0F);
            float entAlpha = 0.28F + 0.36F * prox;

            drawEntitySilhouette(lineConsumer, mat,
                    x0 - entOffX, y0 - entOffY, z0 - entOffZ,
                    x1 - entOffX, y1 - entOffY, z1 - entOffZ,
                    CYAN_R, CYAN_G, CYAN_B, entAlpha);

            drawEntitySilhouette(lineConsumer, mat,
                    x0 + entOffX, y0 + entOffY, z0 + entOffZ,
                    x1 + entOffX, y1 + entOffY, z1 + entOffZ,
                    AMBER_R, AMBER_G, AMBER_B, entAlpha);
        }

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static boolean isExposedSurfaceBlock(Level level, BlockPos pos, BlockPos.MutableBlockPos adjPos) {
        for (Direction dir : CARDINAL_DIRS) {
            adjPos.setWithOffset(pos, dir);
            BlockState adj = level.getBlockState(adjPos);
            if (adj.isAir() || !adj.isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    private static void drawPrismBox(
            VertexConsumer consumer,
            Matrix4f mat,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float r, float g, float b, float a,
            boolean includeFacetDiagonal
    ) {
        // Bottom 4 edges
        addLine(consumer, mat, x0, y0, z0, x1, y0, z0, r, g, b, a);
        addLine(consumer, mat, x1, y0, z0, x1, y0, z1, r, g, b, a);
        addLine(consumer, mat, x1, y0, z1, x0, y0, z1, r, g, b, a);
        addLine(consumer, mat, x0, y0, z1, x0, y0, z0, r, g, b, a);

        // Top 4 edges
        addLine(consumer, mat, x0, y1, z0, x1, y1, z0, r, g, b, a);
        addLine(consumer, mat, x1, y1, z0, x1, y1, z1, r, g, b, a);
        addLine(consumer, mat, x1, y1, z1, x0, y1, z1, r, g, b, a);
        addLine(consumer, mat, x0, y1, z1, x0, y1, z0, r, g, b, a);

        // Vertical 4 edges
        addLine(consumer, mat, x0, y0, z0, x0, y1, z0, r, g, b, a);
        addLine(consumer, mat, x1, y0, z0, x1, y1, z0, r, g, b, a);
        addLine(consumer, mat, x1, y0, z1, x1, y1, z1, r, g, b, a);
        addLine(consumer, mat, x0, y0, z1, x0, y1, z1, r, g, b, a);

        if (includeFacetDiagonal) {
            addLine(consumer, mat, x0, y1, z0, x1, y1, z1, r, g, b, a * 0.55F);
        }
    }

    private static void drawEntitySilhouette(
            VertexConsumer consumer,
            Matrix4f mat,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float r, float g, float b, float a
    ) {
        drawPrismBox(consumer, mat, x0, y0, z0, x1, y1, z1, r, g, b, a, false);
        // Inner diamond/shoulder prism frame so doubled creatures read clearly as living silhouettes
        float mx = (x0 + x1) * 0.5F;
        float mz = (z0 + z1) * 0.5F;
        float shoulderY = Mth.lerp(0.72F, y0, y1);
        addLine(consumer, mat, x0, shoulderY, mz, mx, y1, mz, r, g, b, a * 0.85F);
        addLine(consumer, mat, x1, shoulderY, mz, mx, y1, mz, r, g, b, a * 0.85F);
        addLine(consumer, mat, mx, shoulderY, z0, mx, y1, mz, r, g, b, a * 0.85F);
        addLine(consumer, mat, mx, shoulderY, z1, mx, y1, mz, r, g, b, a * 0.85F);
        addLine(consumer, mat, mx, y0, mz, mx, y1, mz, r, g, b, a * 0.65F);
    }

    private static void addLine(
            VertexConsumer consumer,
            Matrix4f mat,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0E-5F) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        consumer.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz);
        consumer.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz);
    }

    /**
     * 2) HUD Split Crosshairs & Binocular Mirage Vignette:
     * Draws two drifting offset ghost crosshairs (cyan left, sun-amber right, splitX = 8..16px),
     * soft left/right overlapping binocular prism rings, and subtle heat-haze edge shimmer.
     */
    public static void renderHudDoubleVision(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || guiGraphics == null) {
            return;
        }
        if (mc.options.hideGui || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (!isDoubleVisionActive(mc.player)) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        float cx = width * 0.5F;
        float cy = height * 0.5F;

        float t = ((mc.level.getGameTime() % 72000L) + partialTick) * 0.05F;

        // Drifting binocular split (splitX oscillates smoothly across 8..16px)
        float splitX = 12.0F + 4.0F * Mth.sin(t * 2.1F);
        float splitY = 2.6F * Mth.cos(t * 1.5F);

        // 1. Subtle mirage heat-haze edge shimmer along top, bottom, left, and right screen borders
        renderHeatHazeEdgeShimmer(guiGraphics, width, height, t);

        // 2. Soft overlapping left/right binocular prism rings
        float ringRadius = Math.min(width, height) * 0.34F;
        float ringOffsetX = splitX * 2.25F;
        float ringOffsetY = splitY * 1.5F;
        int ringPulseAlpha = 26 + Math.round(12.0F * (0.5F + 0.5F * Mth.sin(t * 2.6F)));
        int cyanRingColor = (ringPulseAlpha << 24) | 0x38BDF8;
        int amberRingColor = (ringPulseAlpha << 24) | 0xFB923C;

        drawSoftRing(guiGraphics, cx - ringOffsetX, cy - ringOffsetY, ringRadius, cyanRingColor);
        drawSoftRing(guiGraphics, cx + ringOffsetX, cy + ringOffsetY, ringRadius, amberRingColor);

        // Secondary faint inner prism caustic rings
        int innerRingAlpha = Math.max(12, ringPulseAlpha - 10);
        drawSoftRing(guiGraphics, cx - splitX * 1.4F, cy - splitY, ringRadius * 0.48F, (innerRingAlpha << 24) | 0x38BDF8);
        drawSoftRing(guiGraphics, cx + splitX * 1.4F, cy + splitY, ringRadius * 0.48F, (innerRingAlpha << 24) | 0xFB923C);

        // 3. Two drifting offset ghost crosshairs
        int crossAlpha = 148 + Math.round(36.0F * (0.5F + 0.5F * Mth.cos(t * 3.1F)));
        int cyanCrossColor = (crossAlpha << 24) | 0x38BDF8;
        int amberCrossColor = (crossAlpha << 24) | 0xFB923C;

        drawGhostCrosshair(guiGraphics, Math.round(cx - splitX), Math.round(cy - splitY), cyanCrossColor);
        drawGhostCrosshair(guiGraphics, Math.round(cx + splitX), Math.round(cy + splitY), amberCrossColor);
    }

    private static void drawGhostCrosshair(GuiGraphics guiGraphics, int x, int y, int color) {
        // Horizontal arms (4px each side with 1px center aperture)
        guiGraphics.fill(x - 5, y, x - 1, y + 1, color);
        guiGraphics.fill(x + 2, y, x + 6, y + 1, color);
        // Vertical arms
        guiGraphics.fill(x, y - 5, x + 1, y - 1, color);
        guiGraphics.fill(x, y + 2, x + 1, y + 6, color);
    }

    private static void drawSoftRing(GuiGraphics guiGraphics, float cx, float cy, float radius, int color) {
        int segments = 40;
        for (int i = 0; i < segments; i++) {
            float a0 = (2.0F * Mth.PI * i) / segments;
            float a1 = (2.0F * Mth.PI * (i + 1)) / segments;
            float x0 = cx + radius * Mth.cos(a0);
            float y0 = cy + radius * Mth.sin(a0);
            float x1 = cx + radius * Mth.cos(a1);
            float y1 = cy + radius * Mth.sin(a1);
            drawGuiSegment(guiGraphics, x0, y0, x1, y1, color);
        }
    }

    private static void drawGuiSegment(GuiGraphics guiGraphics, float x0, float y0, float x1, float y1, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = Mth.sqrt(dx * dx + dy * dy);
        if (len < 0.5F) {
            return;
        }
        int steps = Math.max(1, Mth.ceil(len * 0.5F));
        float sx = dx / steps;
        float sy = dy / steps;
        for (int s = 0; s <= steps; s++) {
            int px = Math.round(x0 + s * sx);
            int py = Math.round(y0 + s * sy);
            guiGraphics.fill(px, py, px + 2, py + 2, color);
        }
    }

    private static void renderHeatHazeEdgeShimmer(GuiGraphics guiGraphics, int width, int height, float t) {
        int bandDepth = Math.max(12, Math.min(28, height / 14));
        int slices = 16;
        int sliceW = Math.max(1, (width + slices - 1) / slices);

        for (int i = 0; i < slices; i++) {
            int x0 = i * sliceW;
            int x1 = Math.min(width, x0 + sliceW);
            float wave = 0.5F + 0.5F * Mth.sin(t * 2.8F + i * 0.65F);
            int depth = Math.max(4, Math.round(bandDepth * (0.45F + 0.55F * wave)));
            int alpha = 14 + Math.round(18.0F * wave);
            int topColor = (alpha << 24) | ((i % 2 == 0) ? 0x38BDF8 : 0xFB923C);
            int botColor = (alpha << 24) | ((i % 2 == 0) ? 0xFB923C : 0x38BDF8);

            guiGraphics.fill(x0, 0, x1, depth, topColor);
            guiGraphics.fill(x0, height - depth, x1, height, botColor);
        }

        int sideDepth = Math.max(10, Math.min(24, width / 22));
        int sideAlpha = 18 + Math.round(12.0F * (0.5F + 0.5F * Mth.cos(t * 2.3F)));
        guiGraphics.fill(0, 0, sideDepth, height, (sideAlpha << 24) | 0x38BDF8);
        guiGraphics.fill(width - sideDepth, 0, width, height, (sideAlpha << 24) | 0xFB923C);
    }
}
