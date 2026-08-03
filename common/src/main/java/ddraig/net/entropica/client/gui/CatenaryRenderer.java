package ddraig.net.entropica.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class CatenaryRenderer {

    /**
     * Renders a glowing catenary arch curve between parent node (x1, y1) and child node (x2, y2)
     * with animated energy particle pulses.
     */
    public static void renderCatenaryCurve(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, float animationTicks, boolean isLocked) {
        int segments = 16;
        float dist = Mth.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        if (dist < 1.0f) return;

        float sag = Math.min(35.0f, dist * 0.15f);

        int alpha = isLocked ? 0x44000000 : 0xAA000000;
        int baseColor = (color & 0x00FFFFFF) | alpha;

        float prevX = x1;
        float prevY = y1;

        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments;
            float currX = Mth.lerp(t, (float) x1, (float) x2);
            float linearY = Mth.lerp(t, (float) y1, (float) y2);
            float catenaryOffset = sag * (1.0f - (2.0f * t - 1.0f) * (2.0f * t - 1.0f));
            float currY = linearY + catenaryOffset;

            int minX = (int) Math.min(prevX, currX);
            int maxX = (int) Math.max(prevX, currX) + 1;
            int minY = (int) Math.min(prevY, currY);
            int maxY = (int) Math.max(prevY, currY) + 1;

            guiGraphics.fill(minX, minY, maxX, maxY, baseColor);

            prevX = currX;
            prevY = currY;
        }

        // Animated particle energy pulse flowing along catenary arch curves toward child nodes
        if (!isLocked) {
            float pulseT = (animationTicks * 0.35f) % 1.0f;
            float px = Mth.lerp(pulseT, (float) x1, (float) x2);
            float py = Mth.lerp(pulseT, (float) y1, (float) y2) + sag * (1.0f - (2.0f * pulseT - 1.0f) * (2.0f * pulseT - 1.0f));

            int pulseColor = (color & 0x00FFFFFF) | 0xFF000000;
            guiGraphics.fill((int) px - 2, (int) py - 2, (int) px + 3, (int) py + 3, pulseColor);
            guiGraphics.fill((int) px - 1, (int) py - 1, (int) px + 2, (int) py + 2, 0xFFFFFFFF);
        }
    }
}
