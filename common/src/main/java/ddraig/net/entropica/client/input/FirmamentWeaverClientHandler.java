package ddraig.net.entropica.client.input;

import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.item.FirmamentWeaverItem;
import ddraig.net.entropica.network.WeaverScrollPayload;
import ddraig.net.entropica.registry.ModItems;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class FirmamentWeaverClientHandler {

    /**
     * Intercepts mouse wheel scrolling when holding the Firmament Weaver while sneaking.
     *
     * @param scrollDelta Vertical scroll delta (+1 for forward/up, -1 for backward/down)
     * @return true if the event was handled and hotbar scrolling must be cancelled; false otherwise.
     */
    public static boolean handleMouseScroll(double scrollDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return false;
        }

        // Must be holding sneak (Shift)
        if (!mc.player.isShiftKeyDown()) {
            return false;
        }

        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();
        InteractionHand hand = null;

        if (mainHand.is(ModItems.FIRMAMENT_WEAVER.get())) {
            hand = InteractionHand.MAIN_HAND;
        } else if (offHand.is(ModItems.FIRMAMENT_WEAVER.get()) && mainHand.isEmpty()) {
            hand = InteractionHand.OFF_HAND;
        }

        if (hand == null) {
            return false;
        }

        ItemStack stack = mc.player.getItemInHand(hand);

        // Do not cycle shapes if Point A of Drag & Snap is actively anchored
        if (FirmamentWeaverItem.hasAnchor(stack)) {
            return false;
        }

        if (Math.abs(scrollDelta) < 1e-3) {
            return false;
        }

        int delta = scrollDelta > 0 ? 1 : -1;

        // 1. Instant Client Prediction
        BarrierShape next = FirmamentWeaverItem.cycleShape(stack, delta);

        // 2. Immediate Client Feedback (Audio & HUD)
        mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.4F);
        mc.player.displayClientMessage(
                Component.literal("§d[Firmament Weaver] §fShape: §b" + next.getDisplayName()),
                true
        );

        // 3. Authoritative C2S Network Packet
        NetworkManager.sendToServer(new WeaverScrollPayload(delta));

        return true;
    }
}
