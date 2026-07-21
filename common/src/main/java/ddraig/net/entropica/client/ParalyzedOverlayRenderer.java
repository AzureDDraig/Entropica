package ddraig.net.entropica.client;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;

public class ParalyzedOverlayRenderer {

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Check if the player has the Paralyzed effect
        MobEffectInstance paralyzedEffect = null;
        for (MobEffectInstance instance : mc.player.getActiveEffects()) {
            if (instance.getEffect().value() == ModEffects.PARALYZED.get()) {
                paralyzedEffect = instance;
                break;
            }
        }

        if (paralyzedEffect == null) return;

        // Visual overlay is only shown if in first person
        if (!mc.options.getCameraType().isFirstPerson()) return;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        java.util.Random rand = new java.util.Random();
        long time = mc.level.getGameTime();
        
        // Seed based on time to create a flickering, procedural animation
        rand.setSeed(time / 2 * 31L);

        int purpleColor = 0xFFD03BFF; // Electric Purple

        if (rand.nextFloat() < 0.85f) {
            int count = 6 + rand.nextInt(7);
            for (int i = 0; i < count; i++) {
                int edge = rand.nextInt(4);
                float startX = 0, startY = 0;
                float endX = 0, endY = 0;

                switch (edge) {
                    case 0 -> { // Top
                        startX = rand.nextFloat() * width;
                        startY = 0;
                        endX = startX + (rand.nextFloat() - 0.5f) * 60;
                        endY = rand.nextFloat() * 40 + 15;
                    }
                    case 1 -> { // Bottom
                        startX = rand.nextFloat() * width;
                        startY = height;
                        endX = startX + (rand.nextFloat() - 0.5f) * 60;
                        endY = height - (rand.nextFloat() * 40 + 15);
                    }
                    case 2 -> { // Left
                        startX = 0;
                        startY = rand.nextFloat() * height;
                        endX = rand.nextFloat() * 40 + 15;
                        endY = startY + (rand.nextFloat() - 0.5f) * 60;
                    }
                    case 3 -> { // Right
                        startX = width;
                        startY = rand.nextFloat() * height;
                        endX = width - (rand.nextFloat() * 40 + 15);
                        endY = startY + (rand.nextFloat() - 0.5f) * 60;
                    }
                }

                drawLightningBolt(guiGraphics, startX, startY, endX, endY, rand, purpleColor);
            }
        }
    }

    private static void drawLine(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;

        int steps = Math.max(1, (int) len);
        float stepX = dx / steps;
        float stepY = dy / steps;

        for (int i = 0; i <= steps; i++) {
            float cx = x1 + stepX * i;
            float cy = y1 + stepY * i;
            guiGraphics.fill((int) (cx - 1), (int) (cy - 1), (int) (cx + 1), (int) (cy + 1), color);
        }
    }

    private static void drawLightningBolt(GuiGraphics guiGraphics, float startX, float startY, float endX, float endY, java.util.Random rand, int color) {
        float curX = startX;
        float curY = startY;

        float dx = endX - startX;
        float dy = endY - startY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        int segments = Math.max(2, (int) (dist / 8));

        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments;
            float targetX = startX + dx * t;
            float targetY = startY + dy * t;

            float jitterX = 0;
            float jitterY = 0;
            if (i < segments) {
                float perpX = -dy / dist;
                float perpY = dx / dist;
                float jVal = (rand.nextFloat() - 0.5f) * 16.0f;
                jitterX = perpX * jVal;
                jitterY = perpY * jVal;
            }

            float nextX = targetX + jitterX;
            float nextY = targetY + jitterY;

            drawLine(guiGraphics, curX, curY, nextX, nextY, color);

            curX = nextX;
            curY = nextY;
        }
    }
}
