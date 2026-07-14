package ddraig.net.entropica.registry.neoforge;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper; // <-- The correct 1.21.10 Transfer Rework Import

@EventBusSubscriber(modid = Entropica.MODID)
public class ModCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        // Binds the new Transfer Rework Item capability to our Receptacle's SimpleContainer buffer
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.ESSENCE_RECEPTACLE_BE.get(),
                (blockEntity, side) -> VanillaContainerWrapper.of(blockEntity.getContainer())
        );

    }
}