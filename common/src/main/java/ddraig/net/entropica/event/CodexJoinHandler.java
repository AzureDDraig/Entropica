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

                // Sync Astral Constellation Progress & Connections
                ddraig.net.entropica.astral.PlayerAstralProgress.loadFromPlayer(player);
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    var discovered = new java.util.ArrayList<>(ddraig.net.entropica.astral.PlayerAstralProgress.getDiscovered(player));
                    var charted = new java.util.ArrayList<>(ddraig.net.entropica.astral.PlayerAstralProgress.getChartedConnections(player));
                    dev.architectury.networking.NetworkManager.sendToPlayer(serverPlayer, new ddraig.net.entropica.network.SyncAstralProgressPayload(discovered, charted));

                    // Load & Sync Active Supernovae and Remnants
                    ddraig.net.entropica.astral.SupernovaSavedData.get((net.minecraft.server.level.ServerLevel) serverPlayer.level());
                    ddraig.net.entropica.astral.SupernovaManager.syncToPlayer(serverPlayer);
                }
            }
        });

        dev.architectury.event.events.common.TickEvent.SERVER_LEVEL_POST.register(level -> {
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel && serverLevel.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                ddraig.net.entropica.astral.SupernovaManager.tick(serverLevel);
            }
        });
    }
}
