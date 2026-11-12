package ddraig.net.entropica.client.input;

import ddraig.net.entropica.item.CastingApparatusItem;
import ddraig.net.entropica.network.SpellApparatusScrollPayload;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Intercepts Sneak + Mouse Scroll Wheel when holding a multi-slot {@link CastingApparatusItem}
 * (Wand = 2, Staff = 4, Grimoire = 9) to cycle active Spellgem slots instantly with 0s lockout.
 */
public final class SpellApparatusClientHandler {

    private SpellApparatusClientHandler() {
    }

    public static boolean handleMouseScroll(double scrollDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || !mc.player.isShiftKeyDown()) {
            return false;
        }
        if (Math.abs(scrollDelta) < 1e-3) {
            return false;
        }

        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();
        InteractionHand hand = null;

        if (mainHand.getItem() instanceof CastingApparatusItem c && c.getApparatusType().getGemCapacity() > 1) {
            hand = InteractionHand.MAIN_HAND;
        } else if (offHand.getItem() instanceof CastingApparatusItem c
                && c.getApparatusType().getGemCapacity() > 1
                && c.getApparatusType().allowsOffhandCast()
                && mainHand.isEmpty()) {
            hand = InteractionHand.OFF_HAND;
        }

        if (hand == null) {
            return false;
        }

        int delta = scrollDelta > 0 ? -1 : 1;
        ItemStack stack = mc.player.getItemInHand(hand);

        // Client prediction + HUD feedback
        CastingApparatusState nextState = CastingApparatusItem.cycleSlot(stack, delta);
        SocketedSpellgem active = nextState.getActiveSocket();
        String spellName = active.spell().map(s -> "§b" + s.customName() + " §7(T" + s.getEffectiveTier() + ")")
                .orElse("§8(Empty Socket)");

        mc.player.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.65F, 1.45F);
        mc.player.displayClientMessage(
                Component.literal("§d[" + nextState.apparatusType().getDisplayName() + "] §7Slot §e["
                        + (nextState.activeSlotIndex() + 1) + "/" + nextState.apparatusType().getGemCapacity()
                        + "]§7: " + spellName),
                true
        );

        NetworkManager.sendToServer(new SpellApparatusScrollPayload(delta));
        return true;
    }
}
