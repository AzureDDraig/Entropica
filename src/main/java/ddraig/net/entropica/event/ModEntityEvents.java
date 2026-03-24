package ddraig.net.entropica.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

// In NeoForge 1.21+, the 'bus' parameter was removed. It infers the bus automatically!
@EventBusSubscriber(modid = Entropica.MODID)
public class ModEntityEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        // Registers the base max health/speed to prevent the NullPointerException crash
        event.put(ModEntityTypes.GROT.get(), GrotEntity.createAttributes().build());
    }
}