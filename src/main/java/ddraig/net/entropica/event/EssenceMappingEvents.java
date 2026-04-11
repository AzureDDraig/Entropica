package ddraig.net.entropica.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.ItemEssenceMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Triggers the iterative Essence Graph generation on the server side exactly once when the world loads!
 */
@EventBusSubscriber(modid = Entropica.MODID)
public class EssenceMappingEvents {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // Syncs perfectly with the exact method name in ItemEssenceMap
        ItemEssenceMap.buildEssenceGraph(event.getServer().getRecipeManager().getRecipes(), event.getServer().registryAccess());
    }
}