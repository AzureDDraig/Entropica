package ddraig.net.entropica.client.gui.tab;

import ddraig.net.entropica.api.pressure.IPressureHandler;
import ddraig.net.entropica.block.entity.VaporPneumaticPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class NetworkDiagnosticsTab {

    private final BlockPos pairedPos;

    public NetworkDiagnosticsTab(BlockPos pairedPos) {
        this.pairedPos = pairedPos;
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xEE0B0D18);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF14172B);

        guiGraphics.drawString(Minecraft.getInstance().font, "§b[Network Diagnostics] Remote Telemetry Hub", x + 15, y + 15, 0xFF00FFCC);

        Level level = Minecraft.getInstance().level;

        if (pairedPos == null) {
            guiGraphics.fill(x + 15, y + 40, x + width - 15, y + 120, 0xFF222740);
            guiGraphics.drawString(Minecraft.getInstance().font, "§c[UNPAIRED FACILITY]", x + 25, y + 50, 0xFFFF5555);
            guiGraphics.drawString(Minecraft.getInstance().font, "Right-click any Materia Terminal block in the world with your Codex", x + 25, y + 70, 0xFFE0E0FF);
            guiGraphics.drawString(Minecraft.getInstance().font, "to pair remote pipe telemetry and leak alerts.", x + 25, y + 84, 0xFFAAAABB);
        } else if (level == null || !level.hasChunkAt(pairedPos)) {
            guiGraphics.fill(x + 15, y + 40, x + width - 15, y + 120, 0xFF222740);
            guiGraphics.drawString(Minecraft.getInstance().font, "§e[OFFLINE / CHUNK UNLOADED]", x + 25, y + 50, 0xFFFFAA00);
            guiGraphics.drawString(Minecraft.getInstance().font, "Target position: " + pairedPos.toShortString(), x + 25, y + 70, 0xFFE0E0FF);
        } else {
            guiGraphics.fill(x + 15, y + 40, x + width - 15, y + 180, 0xFF1B2038);
            guiGraphics.drawString(Minecraft.getInstance().font, "§aPaired Facility Location: " + pairedPos.toShortString(), x + 25, y + 50, 0xFF00FFCC);
            guiGraphics.drawString(Minecraft.getInstance().font, "Scanning Radius: 32 Blocks", x + 25, y + 66, 0xFFFFFFFF);

            // Fetch live target BlockEntity
            BlockEntity targetBE = level.getBlockEntity(pairedPos);
            float pressure = 0.0f;
            if (targetBE instanceof IPressureHandler handler) {
                pressure = handler.getPressure();
            }

            float currentBar = pressure * 10.0f;
            String statusText = pressure > 1.5f ? "(OVERPRESSURE / DANGER)" : (pressure > 1.0f ? "(HIGH PRESSURE)" : "(NORMAL)");
            int statusColor = pressure > 1.5f ? 0xFFFF3333 : (pressure > 1.0f ? 0xFFFFAA00 : 0xFF55FF55);
            int barWidth = Math.min(225, (int) (Math.min(1.0f, pressure) * 225));

            // Network 1 Status (Live Conduit Pressure)
            guiGraphics.drawString(Minecraft.getInstance().font, "Network #1 (Vapor-Pneumatic Conduit):", x + 25, y + 90, 0xFFE0E0FF);
            guiGraphics.fill(x + 25, y + 102, x + 250, y + 112, 0xFF2A2E47);
            guiGraphics.fill(x + 25, y + 102, x + 25 + barWidth, y + 112, statusColor);
            guiGraphics.drawString(Minecraft.getInstance().font, String.format(Locale.ROOT, "%.1f / 10.0 Bar %s", currentBar, statusText), x + 255, y + 103, statusColor);

            // Network 2 Status (Dynamic Breach & Leak Scanning)
            guiGraphics.drawString(Minecraft.getInstance().font, "Network #2 (Materia Fumus Gas Line):", x + 25, y + 125, 0xFFE0E0FF);
            guiGraphics.fill(x + 25, y + 137, x + 250, y + 147, 0xFF2A2E47);

            BlockPos leakPos = findActiveNetworkLeak(level, pairedPos, 32);
            if (leakPos != null) {
                guiGraphics.fill(x + 25, y + 137, x + 240, y + 147, 0xFFFF3333); // Overpressure alert bar
                guiGraphics.drawString(Minecraft.getInstance().font, "§c[LEAK BREACH DETECTED at X:" + leakPos.getX() + " Y:" + leakPos.getY() + " Z:" + leakPos.getZ() + "]", x + 25, y + 155, 0xFFFF5555);
            } else {
                guiGraphics.fill(x + 25, y + 137, x + 250, y + 147, 0xFF00FFCC);
                guiGraphics.drawString(Minecraft.getInstance().font, "§a[NETWORK STABLE - NO LEAKS DETECTED]", x + 25, y + 155, 0xFF55FF55);
            }
        }
    }

    private BlockPos findActiveNetworkLeak(Level level, BlockPos startPos, int radius) {
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() < 100) {
            BlockPos current = queue.poll();
            if (current.distManhattan(startPos) > radius) continue;

            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof IPressureHandler handler) {
                if (handler.getPressure() > 1.0f) {
                    return current;
                }
                if (be instanceof VaporPneumaticPipeBlockEntity pipe) {
                    if (pipe.isBeingOverpowered()) {
                        return current;
                    }
                }
            }

            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && level.getBlockEntity(neighbor) instanceof IPressureHandler) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }
}
