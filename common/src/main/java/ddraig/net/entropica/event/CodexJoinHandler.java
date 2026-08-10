package ddraig.net.entropica.event;

import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.util.EntityHelper;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CodexJoinHandler {

    public static void register() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player != null && !player.level().isClientSide()) {
                CompoundTag tag = EntityHelper.getPersistentData(player);
                boolean hasReceived = tag.getBoolean("hasReceivedCodex").orElse(false) || tag.getBoolean("HasReceivedCodex").orElse(false);

                if (!hasReceived) {
                    player.getInventory().add(new ItemStack(ModItems.ENTROPIC_CODEX.get()));
                    tag.putBoolean("hasReceivedCodex", true);
                    tag.putBoolean("HasReceivedCodex", true);
                    player.displayClientMessage(Component.translatable("msg.entropica.you_received_the_entropic_codex_right"), false);
                }
            }
        });
    }
}
