package ddraig.net.entropica.client;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LensOverlayRenderer {

    public static boolean isWearingOrHoldingLens(Player player) {
        if (player == null) return false;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        if (head.is(ModItems.ARKANIST_MONOCLE.get()) ||
            head.is(ModItems.AETHERIC_MONOCLE.get()) ||
            head.is(ModItems.PROPAGATION_LENS.get()) ||
            head.is(ModItems.AETHERIC_LENS.get()) ||
            head.is(ModItems.VITAE_LENS.get()) ||
            head.is(ModItems.MATERIA_LENS.get())) {
            return true;
        }
        
        if (main.is(ModItems.ARKANIST_MONOCLE.get()) ||
            main.is(ModItems.AETHERIC_MONOCLE.get()) ||
            main.is(ModItems.PROPAGATION_LENS.get()) ||
            main.is(ModItems.AETHERIC_LENS.get()) ||
            main.is(ModItems.VITAE_LENS.get()) ||
            main.is(ModItems.MATERIA_LENS.get())) {
            return true;
        }

        if (off.is(ModItems.ARKANIST_MONOCLE.get()) ||
            off.is(ModItems.AETHERIC_MONOCLE.get()) ||
            off.is(ModItems.PROPAGATION_LENS.get()) ||
            off.is(ModItems.AETHERIC_LENS.get()) ||
            off.is(ModItems.VITAE_LENS.get()) ||
            off.is(ModItems.MATERIA_LENS.get())) {
            return true;
        }

        return false;
    }

    public static boolean hasLensActive(Player player, net.minecraft.world.item.Item lensItem) {
        if (player == null) return false;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.is(lensItem)) return true;
        if (player.getMainHandItem().is(lensItem) || player.getOffhandItem().is(lensItem)) return true;
        
        if (head.is(ModItems.ARKANIST_MONOCLE.get())) {
            ItemStack activeLens = ddraig.net.entropica.item.ArkanistMonocleItem.getActiveLens(player);
            if (!activeLens.isEmpty() && activeLens.is(lensItem)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAethericVisionActive(Player player) {
        if (player == null) return false;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.is(ModItems.AETHERIC_MONOCLE.get())) return true;
        if (player.getMainHandItem().is(ModItems.AETHERIC_MONOCLE.get()) || player.getOffhandItem().is(ModItems.AETHERIC_MONOCLE.get())) return true;
        return hasLensActive(player, ModItems.AETHERIC_LENS.get());
    }

    public static boolean isPropagationVisionActive(Player player) {
        return hasLensActive(player, ModItems.PROPAGATION_LENS.get());
    }

    public static boolean isVitaeVisionActive(Player player) {
        return hasLensActive(player, ModItems.VITAE_LENS.get());
    }

    public static boolean isMateriaVisionActive(Player player) {
        return hasLensActive(player, ModItems.MATERIA_LENS.get());
    }

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!isWearingOrHoldingLens(mc.player)) return;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        float time = (mc.level.getGameTime() + partialTicks) * 0.05f;

        // Render shifting soap-film/oil-slick iridescence in vertical gradient strips
        int strips = 8;
        int stripWidth = width / strips;
        for (int i = 0; i < strips; i++) {
            int xStart = i * stripWidth;
            int xEnd = (i == strips - 1) ? width : (i + 1) * stripWidth;

            // Vignette effect: stronger iridescence near edges, very subtle in the center
            float distFromCenterLeft = Math.abs((xStart + stripWidth / 2f) - width / 2f) / (width / 2f);
            float baseAlpha = 0.06f + 0.12f * distFromCenterLeft;

            int colorTop = getSoapFilmColor(xStart, 0, time, baseAlpha);
            int colorBottom = getSoapFilmColor(xStart, height, time, baseAlpha + 0.05f);

            guiGraphics.fillGradient(xStart, 0, xEnd, height, colorTop, colorBottom);
        }
    }

    private static int getSoapFilmColor(float x, float y, float time, float alpha) {
        float r = 0.5f + 0.5f * (float) Math.sin(time + x * 0.004f + y * 0.002f);
        float g = 0.5f + 0.5f * (float) Math.sin(time * 1.2f + x * 0.002f - y * 0.003f + 2.0f);
        float b = 0.5f + 0.5f * (float) Math.sin(time * 0.8f - x * 0.003f + y * 0.002f + 4.0f);

        return ((int) (alpha * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
