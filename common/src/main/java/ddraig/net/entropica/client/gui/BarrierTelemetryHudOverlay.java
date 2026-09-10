package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.item.FirmamentWeaverItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Creator look-at telemetry HUD overlay for Forcefield Barriers.
 * Renders an analytical celestial telemetry card when the player points the Firmament Weaver
 * at any barrier within 16 meters.
 */
public class BarrierTelemetryHudOverlay {

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.screen != null) return;

        // Only display if player holds Firmament Weaver
        boolean holdsWeaver = player.getMainHandItem().getItem() instanceof FirmamentWeaverItem ||
                player.getOffhandItem().getItem() instanceof FirmamentWeaverItem;
        if (!holdsWeaver) return;

        Vec3 eyePos = player.getEyePosition(partialTick);
        Vec3 lookVec = player.getViewVector(partialTick);
        Vec3 endPos = eyePos.add(lookVec.scale(16.0));

        AABB searchBox = player.getBoundingBox().inflate(16.0);
        List<ForcefieldBarrierEntity> barriers = mc.level.getEntitiesOfClass(ForcefieldBarrierEntity.class, searchBox);
        if (barriers.isEmpty()) return;

        ForcefieldBarrierEntity targeted = null;
        double minDistance = Double.MAX_VALUE;

        // 1. Raycast bounding box clip
        for (ForcefieldBarrierEntity barrier : barriers) {
            AABB aabb = barrier.getBoundingBox().inflate(0.5);
            Optional<Vec3> clip = aabb.clip(eyePos, endPos);
            if (clip.isPresent()) {
                double dist = eyePos.distanceTo(clip.get());
                if (dist < minDistance) {
                    minDistance = dist;
                    targeted = barrier;
                }
            }
        }

        // 2. Fallback: closest barrier within field of view
        if (targeted == null) {
            for (ForcefieldBarrierEntity barrier : barriers) {
                Vec3 toBarrier = barrier.position().subtract(eyePos);
                double projection = toBarrier.dot(lookVec);
                if (projection > 0 && projection <= 16.0) {
                    Vec3 closestPoint = eyePos.add(lookVec.scale(projection));
                    double distToRay = closestPoint.distanceTo(barrier.position());
                    double threshold = Math.max(1.5, Math.max(barrier.getWidth(), barrier.getRadius()) * 0.6);
                    if (distToRay <= threshold && projection < minDistance) {
                        minDistance = projection;
                        targeted = barrier;
                    }
                }
            }
        }

        if (targeted == null) return;

        String title = "§b✦ Firmament Barrier Telemetry";
        List<String> lines = new ArrayList<>();

        // Creator / Owner
        String owner = targeted.getOwnerName();
        lines.add("§7Creator: §f" + (owner.isEmpty() ? "Unclaimed / Natural" : owner));

        // Shape & Dimensions
        String shapeLine = switch (targeted.getShape()) {
            case PLANAR_QUAD -> String.format("§7Shape: §ePlanar Quad §8(%.1fm × %.1fm)", targeted.getWidth(), targeted.getHeight());
            case CIRCULAR_DISC -> String.format("§7Shape: §eCircular Disc §8(r=%.1fm)", targeted.getRadius());
            case HEMISPHERICAL_DOME -> String.format("§7Shape: §eHemispherical Dome §8(r=%.1fm)", targeted.getRadius());
            case SPHERICAL_BUBBLE -> String.format("§7Shape: §eSpherical Bubble §8(r=%.1fm)", targeted.getRadius());
            case CYLINDER -> String.format("§7Shape: §eCylindrical Column §8(r=%.1fm, h=%.1fm)", targeted.getRadius(), targeted.getHeight());
            case CONVEX_POLYGON -> String.format("§7Shape: §eConvex Polygon §8(w=%.1fm, h=%.1fm)", targeted.getWidth(), targeted.getHeight());
        };
        lines.add(shapeLine);

        // Filter Mode
        lines.add("§7Filter: §d" + targeted.getFilterMode().getDisplayName());

        // Flow Directionality
        lines.add("§7Flow: " + (targeted.isOneWay() ? "§6One-Way (Unidirectional)" : "§aBidirectional"));

        // Elasticity
        lines.add(String.format("§7Elasticity: §b%.1fx", targeted.getBounceElasticity()));

        // Redstone Mode
        String redstoneText = switch (targeted.getRedstoneMode()) {
            case 1 -> "§aActive on High Signal";
            case 2 -> "§eInverted (Active on Low)";
            default -> "§7Ignored";
        };
        lines.add("§7Redstone: " + redstoneText);

        // Status
        lines.add("§7Status: " + (targeted.isActive() ? "§aActive" : "§cDeactivated"));

        // Boss encounter status
        if (targeted.isBossEncounter()) {
            lines.add("§c⚠ Apex Boss Bound: Indestructible");
        }

        // Materia color tint
        if (targeted.getColorTint() != null) {
            lines.add(String.format("§7Materia Tint: §d#%06X", targeted.getColorTintRaw() & 0x00FFFFFF));
        }

        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int maxTextWidth = font.width(title);
        for (String line : lines) {
            maxTextWidth = Math.max(maxTextWidth, font.width(line));
        }

        int boxWidth = maxTextWidth + 18;
        int boxHeight = 16 + (lines.size() * 10) + 4;
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = (screenHeight / 2) + 24;

        // Background
        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xDD090D18);

        // Neon Cyan Border
        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFF00E5FF);
        guiGraphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, 0xFF00E5FF);
        guiGraphics.fill(boxX, boxY, boxX + 1, boxY + boxHeight, 0xFF00E5FF);
        guiGraphics.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF00E5FF);

        // Title
        guiGraphics.drawString(font, title, boxX + 8, boxY + 5, 0xFFFFFFFF, false);

        // Lines
        int curY = boxY + 18;
        for (String line : lines) {
            guiGraphics.drawString(font, line, boxX + 8, curY, 0xFFE0E0E0, false);
            curY += 10;
        }
    }
}
