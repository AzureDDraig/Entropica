package ddraig.net.entropica.client.gui.tab;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FusionSimulatorTab {

    private int activeSubTab = 0; // 0 = Reaction Yield, 1 = Damage Scaling
    private EssenceType elementA = EssenceType.IGNIS;
    private EssenceType elementB = EssenceType.WATER;
    private int materiaTier = 1;
    private String targetMobAffinity = "Ignis (Fire)";

    private static final EssenceType[] ALL_ESSENCES = {
            EssenceType.VITAE, EssenceType.BLOOD, EssenceType.IGNIS, 
            EssenceType.WATER, EssenceType.AIR, EssenceType.EARTH, 
            EssenceType.UMBRAL, EssenceType.VOID
    };

    private static final String[] AFFINITIES = {
            "Neutral", "Ignis (Fire)", "Aqua (Water)", "Terran (Earth)", "Aer (Air)", "Umbral (Dark)", "Void"
    };

    public void cycleElementA() {
        int idx = (elementA.ordinal() + 1) % ALL_ESSENCES.length;
        elementA = ALL_ESSENCES[idx];
    }

    public void cycleElementB() {
        int idx = (elementB.ordinal() + 1) % ALL_ESSENCES.length;
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
        guiGraphics.fill(x, y, x + width, y + height, 0xEE0B0D18);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF14172B);

        int tabW = 140;
        guiGraphics.fill(x + 10, y + 10, x + 10 + tabW, y + 30, activeSubTab == 0 ? 0xFF00FFCC : 0xFF2A2E47);
        guiGraphics.drawString(Minecraft.getInstance().font, "1. Reaction Yield", x + 25, y + 16, activeSubTab == 0 ? 0xFF000000 : 0xFFFFFFFF);

        guiGraphics.fill(x + 160, y + 10, x + 160 + tabW, y + 30, activeSubTab == 1 ? 0xFF00FFCC : 0xFF2A2E47);
        guiGraphics.drawString(Minecraft.getInstance().font, "2. Damage Scaling", x + 175, y + 16, activeSubTab == 1 ? 0xFF000000 : 0xFFFFFFFF);

        if (activeSubTab == 0) {
            renderReactionYield(guiGraphics, x + 15, y + 45, width - 30);
        } else {
            renderDamageScaling(guiGraphics, x + 15, y + 45, width - 30);
        }
    }

    private void renderReactionYield(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.drawString(Minecraft.getInstance().font, "§b[Sub-Tab 1] Volumetric Reaction & Stability", x, y, 0xFF00FFCC);

        guiGraphics.fill(x, y + 18, x + 150, y + 42, 0xFF222740);
        guiGraphics.drawString(Minecraft.getInstance().font, "Element A: §d" + elementA.name(), x + 10, y + 26, 0xFFFFFFFF);

        guiGraphics.fill(x + 170, y + 18, x + 320, y + 42, 0xFF222740);
        guiGraphics.drawString(Minecraft.getInstance().font, "Element B: §d" + elementB.name(), x + 180, y + 26, 0xFFFFFFFF);

        guiGraphics.fill(x, y + 54, x + 320, y + 78, 0xFF222740);
        guiGraphics.drawString(Minecraft.getInstance().font, "Materia Tier: §eT" + materiaTier + " (Click to Cycle T1-T10)", x + 10, y + 62, 0xFFFFFFFF);

        guiGraphics.fill(x, y + 90, x + width, y + 170, 0xFF1B2038);
        guiGraphics.drawString(Minecraft.getInstance().font, "§aPredicted Reaction Outcome:", x + 10, y + 98, 0xFF00FFCC);

        String outcome = calculateOutcome(elementA, elementB, materiaTier);
        guiGraphics.drawString(Minecraft.getInstance().font, outcome, x + 10, y + 114, 0xFFE0E0FF);

        int inputVol = 100 * materiaTier;
        int outputVol = (int) (inputVol * 1.8 * (0.9 + materiaTier * 0.02));
        guiGraphics.drawString(Minecraft.getInstance().font, "Volumetric Yield: " + inputVol + "mB + " + inputVol + "mB -> " + outputVol + "mB", x + 10, y + 130, 0xFFAAAABB);

        double stability = Math.max(45.0, 99.5 - (materiaTier * 1.5) - (elementA != elementB ? 5.0 : 0.0));
        String stabText = String.format("%.1f%% ", stability) + (stability > 75.0 ? "§a(Safe)" : "§c(High Thermal Stress)");
        guiGraphics.drawString(Minecraft.getInstance().font, "Reaction Stability: " + stabText, x + 10, y + 146, 0xFFFFFFFF);
    }

    private void renderDamageScaling(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.drawString(Minecraft.getInstance().font, "§b[Sub-Tab 2] Additive Resonance Damage Matchup", x, y, 0xFF00FFCC);

        guiGraphics.fill(x, y + 18, x + 150, y + 42, 0xFF222740);
        guiGraphics.drawString(Minecraft.getInstance().font, "Infusion: §d" + elementA.name(), x + 10, y + 26, 0xFFFFFFFF);

        guiGraphics.fill(x + 170, y + 18, x + 320, y + 42, 0xFF222740);
        guiGraphics.drawString(Minecraft.getInstance().font, "Target: §e" + targetMobAffinity, x + 180, y + 26, 0xFFFFFFFF);

        guiGraphics.fill(x, y + 54, x + width, y + 150, 0xFF1B2038);
        guiGraphics.drawString(Minecraft.getInstance().font, "§aAdditive Resonance Output:", x + 10, y + 62, 0xFF00FFCC);

        double resonanceMult = calculateResonance(elementA, targetMobAffinity);
        guiGraphics.drawString(Minecraft.getInstance().font, "Damage Multiplier: §e" + String.format("%.2f", resonanceMult) + "x", x + 10, y + 78, 0xFFFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "Elemental Matchup: " + (resonanceMult > 1.2 ? "§aSUPER EFFECTIVE (+" + (int)((resonanceMult - 1.0)*100) + "%)" : "§7STANDARD RESONANCE"), x + 10, y + 94, 0xFFFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "Shield Floor Bypass: " + (resonanceMult > 1.40 ? "§aENABLED" : "§cDISABLED"), x + 10, y + 110, 0xFFFFFFFF);
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
        return "Fused " + a.name() + "-" + b.name() + " Compound T" + tier;
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
        if (mouseY >= y + 10 && mouseY <= y + 30) {
            if (mouseX >= x + 10 && mouseX <= x + 150) {
                setSubTab(0);
                return true;
            }
            if (mouseX >= x + 160 && mouseX <= x + 300) {
                setSubTab(1);
                return true;
            }
        }

        if (activeSubTab == 0) {
            if (mouseY >= y + 63 && mouseY <= y + 87) {
                if (mouseX >= x + 15 && mouseX <= x + 165) {
                    cycleElementA();
                    return true;
                }
                if (mouseX >= x + 185 && mouseX <= x + 335) {
                    cycleElementB();
                    return true;
                }
            }
            if (mouseY >= y + 99 && mouseY <= y + 123) {
                cycleTier();
                return true;
            }
        } else {
            if (mouseY >= y + 63 && mouseY <= y + 87) {
                if (mouseX >= x + 15 && mouseX <= x + 165) {
                    cycleElementA();
                    return true;
                }
                if (mouseX >= x + 185 && mouseX <= x + 335) {
                    cycleAffinity();
                    return true;
                }
            }
        }
        return false;
    }
}
