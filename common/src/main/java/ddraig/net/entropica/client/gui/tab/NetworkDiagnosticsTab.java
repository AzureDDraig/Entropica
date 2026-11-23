package ddraig.net.entropica.client.gui.tab;

import ddraig.net.entropica.api.pressure.IPressureHandler;
import ddraig.net.entropica.block.entity.VaporPneumaticPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

public class NetworkDiagnosticsTab {

    private final BlockPos pairedPos;

    public NetworkDiagnosticsTab(BlockPos pairedPos) {
        this.pairedPos = pairedPos;
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.fill(x, y, x + width, y + height, 0xEE0B0D18);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF14172B);

        guiGraphics.drawString(font, "§b📡 Terminal Telemetry", x + 10, y + 8, 0xFF00FFCC, false);

        Level level = Minecraft.getInstance().level;

        if (pairedPos == null) {
            guiGraphics.fill(x + 8, y + 24, x + width - 8, y + height - 8, 0xFF222740);
            guiGraphics.drawString(font, "§c[UNPAIRED TERMINAL]", x + 14, y + 32, 0xFFFF5555, false);
            var lines = font.split(net.minecraft.network.chat.Component.literal(
                    "Right-click a Materia Terminal block in-world with your Codex to pair remote pipe telemetry."
            ), Math.max(80, width - 28));
            int ly = y + 48;
            for (var line : lines) {
                if (ly + 10 <= y + height - 8) {
                    guiGraphics.drawString(font, line, x + 14, ly, 0xFFCBD5E1, false);
                }
                ly += 11;
            }
        } else if (level == null || !level.hasChunkAt(pairedPos)) {
            guiGraphics.fill(x + 8, y + 24, x + width - 8, y + height - 8, 0xFF222740);
            guiGraphics.drawString(font, "§e[OFFLINE / UNLOADED]", x + 14, y + 32, 0xFFFFAA00, false);
            guiGraphics.drawString(font, "Pos: " + pairedPos.toShortString(), x + 14, y + 48, 0xFFE0E0FF, false);
        } else {
            guiGraphics.fill(x + 8, y + 24, x + width - 8, y + height - 8, 0xFF1B2038);
            guiGraphics.drawString(font, "§aPaired: " + pairedPos.toShortString(), x + 12, y + 30, 0xFF00FFCC, false);

            BlockEntity targetBE = level.getBlockEntity(pairedPos);
            float pressure = 0.0f;
            if (targetBE instanceof IPressureHandler handler) {
                pressure = handler.getPressure();
            }

            float currentBar = pressure * 10.0f;
            String statusText = pressure > 1.5f ? "DANGER" : (pressure > 1.0f ? "HIGH" : "NORMAL");
            int statusColor = pressure > 1.5f ? 0xFFFF3333 : (pressure > 1.0f ? 0xFFFFAA00 : 0xFF55FF55);

            int maxBarW = Math.max(60, width - 28);
            int filledW = Math.min(maxBarW, (int) (Math.min(1.0f, pressure) * maxBarW));

            guiGraphics.drawString(font, String.format(Locale.ROOT, "Conduit: %.1f / 10 Bar (%s)", currentBar, statusText), x + 12, y + 46, statusColor, false);
            guiGraphics.fill(x + 12, y + 58, x + 12 + maxBarW, y + 66, 0xFF2A2E47);
            guiGraphics.fill(x + 12, y + 58, x + 12 + filledW, y + 66, statusColor);

            BlockPos leakPos = findActiveNetworkLeak(level, pairedPos, 32);
            if (leakPos != null) {
                guiGraphics.fill(x + 12, y + 74, x + 12 + maxBarW, y + 82, 0xFFFF3333);
                guiGraphics.drawString(font, "§cLeak @ " + leakPos.toShortString(), x + 12, y + 88, 0xFFFF5555, false);
            } else {
                guiGraphics.fill(x + 12, y + 74, x + 12 + maxBarW, y + 82, 0xFF00FFCC);
                guiGraphics.drawString(font, "§aNetwork Stable — No Leaks", x + 12, y + 88, 0xFF55FF55, false);
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
