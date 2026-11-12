package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.block.PureOpticFiberBlock;
import ddraig.net.entropica.block.entity.*;
import ddraig.net.entropica.item.AstrolabeItem;
import ddraig.net.entropica.item.LookingGlassItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class OpticalInspectionHudOverlay {

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.screen != null) return;

        // Only display if player is holding a Looking Glass or Astrolabe
        boolean holdsInspector = player.getMainHandItem().getItem() instanceof LookingGlassItem ||
                player.getOffhandItem().getItem() instanceof LookingGlassItem ||
                player.getMainHandItem().getItem() instanceof AstrolabeItem ||
                player.getOffhandItem().getItem() instanceof AstrolabeItem;

        if (!holdsInspector) return;

        Vec3 eyePos = player.getEyePosition(partialTick);
        Vec3 lookVec = player.getViewVector(partialTick);
        Vec3 endPos = eyePos.add(lookVec.scale(16.0));

        BlockHitResult hit = mc.level.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos targetPos = hit.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(targetPos);
        BlockState state = mc.level.getBlockState(targetPos);

        String title = null;
        List<String> lines = new ArrayList<>();

        if (be instanceof RefractiveAstralLensBlockEntity lens) {
            title = "§b✦ Refractive Astral Lens";
            lines.add(lens.isBeamActive() ? "§a● Focusing Starlight Beam" : "§7○ Standby (No Star Focused)");
            if (lens.isBeamActive()) {
                lines.add("§7Locked Star: §e" + lens.getActiveStarName());
                lines.add(String.format("§7Azimuth/Pitch: §f%.1f° / %.1f°", lens.getYaw(), lens.getPitch()));
                lines.add(String.format("§7Beam Distance: §f%.1fm", lens.getBeamDistance()));
            }
        } else if (be instanceof SecondaryAstralLensBlockEntity secondary) {
            title = "§b✦ Secondary Astral Lens";
            lines.add(secondary.isReceiving() ? "§a● Relaying (" + secondary.getActiveStarName() + ")" : "§e○ Awaiting Incoming Beam");
            lines.add(String.format("§7Target Aim: §f%.1f° / %.1f°", secondary.getYaw(), secondary.getPitch()));
            lines.add(String.format("§7Beam Reach: §f%.1fm", secondary.getBeamDistance()));
        } else if (be instanceof AstralCollectorBlockEntity collector) {
            title = "§b✦ Astral Collector";
            lines.add(collector.isIrradiated() ? "§a● Condensing Active Starlight" : "§7○ Dormant (No Beam)");
            lines.add(String.format("§7Stored Materia: §d%d / 2000 mB", collector.getStoredMateria()));
            if (collector.getStoredEssence() != null) {
                lines.add("§7Essence: §e" + collector.getStoredEssence().name());
            }
        } else if (be instanceof BeamSplitterPrismBlockEntity prism) {
            title = "§b✦ Beam Splitter Prism";
            lines.add(prism.isReceiving() ? "§a● Splitting Dual Orthogonal (90°)" : "§7○ Awaiting Starlight");
        } else if (be instanceof OpticalReceiverPortBlockEntity receiver) {
            title = "§b✦ Optical Receiver Port";
            lines.add(receiver.isEmitting() ? "§a● Emitting Fiber Flux (" + (int)(receiver.getSignalQuality() * 100) + "%)" : "§7○ Inactive");
        } else if (be instanceof OpticalTransmitterPortBlockEntity) {
            title = "§b✦ Optical Transmitter Port";
            lines.add("§7Injects open-air beam into fiber");
        } else if (be instanceof OpticalBoosterAmplifierBlockEntity) {
            title = "§b✦ Optical Booster Amplifier";
            lines.add("§a● Restoring 100% Starlight Signal");
        } else if (state.getBlock() instanceof PureOpticFiberBlock) {
            title = "§b✦ Pure Optic Fiber";
            lines.add("§7High-Purity Fused Quartz Conduit");
            lines.add("§8Attenuation: 1% per 16 blocks");
        }

        if (title == null) return;

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

        // Header Title
        guiGraphics.drawString(font, title, boxX + 8, boxY + 5, 0xFFFFFFFF, false);

        // Content lines
        int curY = boxY + 18;
        for (String line : lines) {
            guiGraphics.drawString(font, line, boxX + 8, curY, 0xFFE0E0E0, false);
            curY += 10;
        }
    }
}
