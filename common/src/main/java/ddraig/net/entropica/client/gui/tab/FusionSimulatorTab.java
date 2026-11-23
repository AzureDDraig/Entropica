package ddraig.net.entropica.client.gui.tab;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class FusionSimulatorTab {

    private int activeSubTab = 0; // 0 = Reaction Yield, 1 = Damage Scaling
    private EssenceType elementA = EssenceType.IGNIS;
    private EssenceType elementB = EssenceType.WATER;
    private int materiaTier = 1;
    private String targetMobAffinity = "Ignis (Fire)";
    private int lastRenderWidth = 340;

    private static final EssenceType[] ALL_ESSENCES = {
            EssenceType.VITAE, EssenceType.BLOOD, EssenceType.IGNIS,
            EssenceType.WATER, EssenceType.AIR, EssenceType.EARTH,
            EssenceType.UMBRAL, EssenceType.VOID
    };

    private static final String[] AFFINITIES = {
            "Neutral", "Ignis (Fire)", "Aqua (Water)", "Terran (Earth)", "Aer (Air)", "Umbral (Dark)", "Void"
    };

    public void cycleElementA() {
        int idx = 0;
        for (int i = 0; i < ALL_ESSENCES.length; i++) {
            if (ALL_ESSENCES[i] == elementA) {
                idx = (i + 1) % ALL_ESSENCES.length;
                break;
            }
        }
        elementA = ALL_ESSENCES[idx];
    }

    public void cycleElementB() {
        int idx = 0;
        for (int i = 0; i < ALL_ESSENCES.length; i++) {
            if (ALL_ESSENCES[i] == elementB) {
                idx = (i + 1) % ALL_ESSENCES.length;
                break;
            }
        }
        elementB = ALL_ESSENCES[idx];
    }

    public void cycleTier() {
        materiaTier = (materiaTier % 10) + 1;
    }

    public void cycleAffinity() {
        int idx = 0;
        for (int i = 0; i < AFFINITIES.length; i++) {
            if (AFFINITIES[i].equals(targetMobAffinity)) {
                idx = (i + 1) % AFFINITIES.length;
                break;
            }
        }
        targetMobAffinity = AFFINITIES[idx];
    }

    public void setSubTab(int subTab) {
        this.activeSubTab = subTab;
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        this.lastRenderWidth = Math.max(150, width);
        Font font = Minecraft.getInstance().font;

        guiGraphics.fill(x, y, x + width, y + height, 0xEE0B0D18);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF14172B);

        int tabW = Math.min(130, (width - 24) / 2);
        int tab2X = x + 12 + tabW + 4;

        guiGraphics.fill(x + 8, y + 8, x + 8 + tabW, y + 24, activeSubTab == 0 ? 0xFF00FFCC : 0xFF2A2E47);
        guiGraphics.drawString(font, tabW < 95 ? "1. Yield" : "1. Reaction Yield", x + 12, y + 12, activeSubTab == 0 ? 0xFF000000 : 0xFFFFFFFF, false);

        guiGraphics.fill(tab2X, y + 8, tab2X + tabW, y + 24, activeSubTab == 1 ? 0xFF00FFCC : 0xFF2A2E47);
        guiGraphics.drawString(font, tabW < 95 ? "2. Matchup" : "2. Damage Scaling", tab2X + 4, y + 12, activeSubTab == 1 ? 0xFF000000 : 0xFFFFFFFF, false);

        if (activeSubTab == 0) {
            renderReactionYield(guiGraphics, font, x + 8, y + 32, width - 16);
        } else {
            renderDamageScaling(guiGraphics, font, x + 8, y + 32, width - 16);
        }
    }

    private void renderReactionYield(GuiGraphics g, Font font, int x, int y, int width) {
        g.drawString(font, "§bVolumetric Reaction & Stability", x, y, 0xFF00FFCC, false);

        int halfW = (width - 6) / 2;
        int rightX = x + halfW + 6;

        g.fill(x, y + 13, x + halfW, y + 31, 0xFF222740);
        g.drawString(font, "A: §d" + elementA.name(), x + 5, y + 18, 0xFFFFFFFF, false);

        g.fill(rightX, y + 13, rightX + halfW, y + 31, 0xFF222740);
        g.drawString(font, "B: §d" + elementB.name(), rightX + 5, y + 18, 0xFFFFFFFF, false);

        g.fill(x, y + 35, x + width, y + 53, 0xFF222740);
        g.drawString(font, "Materia Tier: §eT" + materiaTier + " §7(Click)", x + 5, y + 40, 0xFFFFFFFF, false);

        g.fill(x, y + 58, x + width, y + 122, 0xFF1B2038);
        String outcome = calculateOutcome(elementA, elementB, materiaTier);
        g.drawString(font, "§a" + trimToWidth(font, outcome, width - 10), x + 5, y + 64, 0xFFE0E0FF, false);

        int inputVol = 100 * materiaTier;
        int outputVol = (int) (inputVol * 1.8 * (0.9 + materiaTier * 0.02));
        g.drawString(font, "Yield: " + inputVol + "+" + inputVol + " -> " + outputVol + "mB", x + 5, y + 80, 0xFFAAAABB, false);

        double stability = Math.max(45.0, 99.5 - (materiaTier * 1.5) - (elementA != elementB ? 5.0 : 0.0));
        String stabText = String.format("%.1f%% ", stability) + (stability > 75.0 ? "§a(Safe)" : "§c(High Stress)");
        g.drawString(font, "Stability: " + stabText, x + 5, y + 96, 0xFFFFFFFF, false);
    }

    private void renderDamageScaling(GuiGraphics g, Font font, int x, int y, int width) {
        g.drawString(font, "§bResonance Damage Matchup", x, y, 0xFF00FFCC, false);

        int halfW = (width - 6) / 2;
        int rightX = x + halfW + 6;

        g.fill(x, y + 13, x + halfW, y + 31, 0xFF222740);
        g.drawString(font, "Elem: §d" + elementA.name(), x + 5, y + 18, 0xFFFFFFFF, false);

        g.fill(rightX, y + 13, rightX + halfW, y + 31, 0xFF222740);
        g.drawString(font, "Vs: §e" + trimToWidth(font, targetMobAffinity, halfW - 26), rightX + 5, y + 18, 0xFFFFFFFF, false);

        g.fill(x, y + 38, x + width, y + 114, 0xFF1B2038);
        double resonanceMult = calculateResonance(elementA, targetMobAffinity);
        g.drawString(font, "Multiplier: §e" + String.format("%.2f", resonanceMult) + "x", x + 5, y + 46, 0xFFFFFFFF, false);
        g.drawString(font, resonanceMult > 1.2 ? "§aSUPER EFFECTIVE (+" + (int) ((resonanceMult - 1.0) * 100) + "%)" : "§7STANDARD RESONANCE", x + 5, y + 64, 0xFFFFFFFF, false);
        g.drawString(font, "Shield Bypass: " + (resonanceMult > 1.40 ? "§aENABLED" : "§cDISABLED"), x + 5, y + 82, 0xFFFFFFFF, false);
    }

    private static String trimToWidth(Font font, String s, int maxW) {
        if (maxW <= 20 || font.width(s) <= maxW) return s;
        String cur = s;
        while (cur.length() > 4 && font.width(cur + "..") > maxW) {
            cur = cur.substring(0, cur.length() - 1);
        }
        return cur + "..";
    }

    private static String calculateOutcome(EssenceType a, EssenceType b, int tier) {
        if (a == b) return "Condensed " + a.name() + " Materia T" + tier;
        if ((a == EssenceType.IGNIS && b == EssenceType.WATER) || (a == EssenceType.WATER && b == EssenceType.IGNIS)) {
            return "Steam / Vapor Fumes T" + tier;
        }
        if ((a == EssenceType.IGNIS && b == EssenceType.EARTH) || (a == EssenceType.EARTH && b == EssenceType.IGNIS)) {
            return "Magma Slag Materia T" + tier;
        }
        if ((a == EssenceType.VITAE && b == EssenceType.VOID) || (a == EssenceType.VOID && b == EssenceType.VITAE)) {
            return "Necrotic Liquid Materia T" + tier;
        }
        return "Fused " + a.name() + "-" + b.name() + " T" + tier;
    }

    private static double calculateResonance(EssenceType weaponElem, String targetAffinity) {
        if (targetAffinity.contains("Fire") && weaponElem == EssenceType.WATER) return 1.50;
        if (targetAffinity.contains("Water") && weaponElem == EssenceType.IGNIS) return 0.75;
        if (targetAffinity.contains("Dark") && weaponElem == EssenceType.VOID) return 1.75;
        if (targetAffinity.contains("Earth") && weaponElem == EssenceType.AIR) return 1.35;
        if (targetAffinity.contains("Air") && weaponElem == EssenceType.EARTH) return 1.25;
        if (weaponElem == EssenceType.VITAE && targetAffinity.contains("Void")) return 1.60;
        if (weaponElem == EssenceType.BLOOD) return 1.45;
        return 1.00;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y) {
        int tabW = Math.min(130, (lastRenderWidth - 24) / 2);
        int tab2X = x + 12 + tabW + 4;

        if (mouseY >= y + 8 && mouseY <= y + 24) {
            if (mouseX >= x + 8 && mouseX <= x + 8 + tabW) {
                setSubTab(0);
                return true;
            }
            if (mouseX >= tab2X && mouseX <= tab2X + tabW) {
                setSubTab(1);
                return true;
            }
        }

        int innerX = x + 8;
        int innerW = lastRenderWidth - 16;
        int halfW = (innerW - 6) / 2;
        int rightX = innerX + halfW + 6;
        int row1Top = y + 45;

        if (mouseY >= row1Top && mouseY <= row1Top + 18) {
            if (mouseX >= innerX && mouseX <= innerX + halfW) {
                cycleElementA();
                return true;
            }
            if (mouseX >= rightX && mouseX <= rightX + halfW) {
                if (activeSubTab == 0) cycleElementB();
                else cycleAffinity();
                return true;
            }
        }

        if (activeSubTab == 0 && mouseY >= y + 67 && mouseY <= y + 85 && mouseX >= innerX && mouseX <= innerX + innerW) {
            cycleTier();
            return true;
        }

        return false;
    }
}
