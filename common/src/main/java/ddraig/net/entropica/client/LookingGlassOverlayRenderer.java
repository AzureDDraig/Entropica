package ddraig.net.entropica.client;

import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.item.LookingGlassItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;

public class LookingGlassOverlayRenderer {

    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/looking_glass_overlay.png");

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !ddraig.net.entropica.item.AstrolabeItem.isScoping(player)) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 1. Draw Brass Reticle Overlay (Strict 1:1 Aspect Ratio + Letterbox Black Bars)
        int size = Math.min(screenWidth, screenHeight);
        int x = (screenWidth - size) / 2;
        int y = (screenHeight - size) / 2;

        // Fill surrounding space outside the telescope circular aperture with solid black mask
        if (x > 0) {
            guiGraphics.fill(0, 0, x, screenHeight, 0xFF000000);
            guiGraphics.fill(x + size, 0, screenWidth, screenHeight, 0xFF000000);
        }
        if (y > 0) {
            guiGraphics.fill(0, 0, screenWidth, y, 0xFF000000);
            guiGraphics.fill(0, y + size, screenWidth, screenHeight, 0xFF000000);
        }

        guiGraphics.blit(OVERLAY_TEXTURE, x, y, x + size, y + size, 0.0f, 1.0f, 0.0f, 1.0f);

        // 2. Astronomical Coordinate Readouts
        float yaw = Mth.wrapDegrees(player.getYRot());
        if (yaw < 0) yaw += 360f;
        float pitch = -player.getXRot(); // -90 to +90 (looking straight up = +90)

        String dirName;
        if (yaw >= 337.5 || yaw < 22.5) dirName = "S";
        else if (yaw >= 22.5 && yaw < 67.5) dirName = "SW";
        else if (yaw >= 67.5 && yaw < 112.5) dirName = "W";
        else if (yaw >= 112.5 && yaw < 157.5) dirName = "NW";
        else if (yaw >= 157.5 && yaw < 202.5) dirName = "N";
        else if (yaw >= 202.5 && yaw < 247.5) dirName = "NE";
        else if (yaw >= 247.5 && yaw < 292.5) dirName = "E";
        else dirName = "SE";

        int moonPhase = player.level().getMoonPhase();
        String[] phaseNames = {
                "Full Moon", "Waning Gibbous", "Third Quarter", "Waning Crescent",
                "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous"
        };
        String currentPhase = phaseNames[moonPhase % 8];

        // Header / HUD text in ancient celestial gold
        int textY = 25;
        guiGraphics.drawCenteredString(mc.font, "§6§lCELESTIAL LOOKING GLASS", screenWidth / 2, textY, 0xFFFFD700);
        guiGraphics.drawCenteredString(mc.font, String.format("§eAzimuth: §f%.1f° %s  §e|  Declination: §f%+.1f°  §e|  Moon: §b%s", yaw, dirName, pitch, currentPhase), screenWidth / 2, textY + 12, 0xFFE0E0E0);

        // 3. Highlight Detected Constellation if looking towards the sky (Pitch > -15 deg at night)
        if (pitch > -15.0f && mc.level != null) {
            float skyAngle = mc.level.getTimeOfDay(partialTick);
            boolean isNight = skyAngle > 0.23f && skyAngle < 0.77f;

            if (isNight) {
                Collection<Constellation> allConstellations = ModConstellations.getAllConstellations();
                Constellation closest = null;
                float closestDist = Float.MAX_VALUE;
                for (Constellation c : allConstellations) {
                    float[] appAngles = ddraig.net.entropica.client.gui.SkyLookingGlassScreen.celestialToApparentAngles(
                            c.getCelestialAzimuthRad(), c.getCelestialAltitudeRad(), skyAngle * 360.0f
                    );
                    float dYaw = net.minecraft.util.Mth.wrapDegrees(appAngles[0] - yaw);
                    float dPitch = appAngles[1] - pitch;
                    float dist = (float) Math.hypot(dYaw, dPitch);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = c;
                    }
                }
                if (closest != null && closestDist <= 30.0f) {
                    Constellation activeC = closest;
                    int opticTier = ddraig.net.entropica.astral.PlayerAstralProgress.getInstrumentOpticTier(player);
                    boolean canSelect = ddraig.net.entropica.astral.PlayerAstralProgress.canHardwareObserve(opticTier, activeC.getTier());
                    boolean isDiscovered = ddraig.net.entropica.astral.PlayerAstralProgress.isDiscovered(player, activeC);

                    int cardY = screenHeight - 65;
                    int boxW = 280;
                    int boxH = 45;
                    int bx = screenWidth / 2 - boxW / 2;

                    guiGraphics.fill(bx, cardY, bx + boxW, cardY + boxH, 0xCC0B0E17);
                    // Outline
                    int borderColor = !canSelect ? 0xFF884444 : (isDiscovered ? 0xFFFFD700 : 0xFF00E0FF);
                    guiGraphics.fill(bx, cardY, bx + boxW, cardY + 1, borderColor);
                    guiGraphics.fill(bx, cardY + boxH - 1, bx + boxW, cardY + boxH, borderColor);
                    guiGraphics.fill(bx, cardY, bx + 1, cardY + boxH, borderColor);
                    guiGraphics.fill(bx + boxW - 1, cardY, bx + boxW, cardY + boxH, borderColor);

                    if (!canSelect) {
                        String req = ddraig.net.entropica.astral.PlayerAstralProgress.getRequiredInstrumentName(activeC.getTier());
                        guiGraphics.drawCenteredString(mc.font, "§8✦ Faint Stellar Cluster §8[" + activeC.getTier().getDisplayName() + "] ✦", screenWidth / 2, cardY + 6, 0xFFAAAAAA);
                        guiGraphics.drawCenteredString(mc.font, "§cRequires: §e" + req, screenWidth / 2, cardY + 18, 0xFFFF8080);
                        guiGraphics.drawCenteredString(mc.font, "§8[Optical power insufficient to resolve or chart]", screenWidth / 2, cardY + 29, 0xFF888888);
                    } else if (isDiscovered) {
                        String cTitle = Component.translatable(activeC.getUnlocalizedName()).getString();
                        guiGraphics.drawCenteredString(mc.font, "§6✦ §f" + cTitle + " §7[" + activeC.getTier().getDisplayName() + "] §6✦", screenWidth / 2, cardY + 6, activeC.getTier().getColorHex());
                        guiGraphics.drawCenteredString(mc.font, "§aRitual: §7" + activeC.getRitualEffect(), screenWidth / 2, cardY + 18, 0xFFA0C0D0);
                        guiGraphics.drawCenteredString(mc.font, "§eSpectral Class: §f" + activeC.getPrimarySpectralClass().getTitle() + " (" + activeC.getPrimarySpectralClass().getCode() + ")", screenWidth / 2, cardY + 29, 0xFFFFF0A0);
                    } else {
                        guiGraphics.drawCenteredString(mc.font, "§b✦ §7Uncharted Stellar Cluster §8[" + activeC.getTier().getDisplayName() + "] §b✦", screenWidth / 2, cardY + 6, 0xFFA0C0E0);
                        guiGraphics.drawCenteredString(mc.font, "§eSpectral Resonance: §fClass " + activeC.getPrimarySpectralClass().getTitle() + " (" + activeC.getPrimarySpectralClass().getCode() + ")", screenWidth / 2, cardY + 18, 0xFFFFF0A0);
                        guiGraphics.drawCenteredString(mc.font, "§8[Shift + Right-Click to Chart Pattern]", screenWidth / 2, cardY + 29, 0xFF88A0B0);
                    }
                }
            }
        }
    }
}
