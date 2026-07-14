package ddraig.net.entropica.event;

import ddraig.net.entropica.api.ItemEssenceMap;
import dev.architectury.event.events.common.LifecycleEvent;

public class EssenceMappingEvents {
    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(server -> {
            ItemEssenceMap.buildEssenceGraph(server.getRecipeManager().getRecipes(), server.registryAccess());
        });
    }
}