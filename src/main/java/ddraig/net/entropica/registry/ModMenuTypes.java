package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.inventory.SynthesizerUserInterfaceMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    // Create the DeferredRegister for Menu Types
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Entropica.MODID, Registries.MENU);

    // Register the Synthesizer User Interface Menu using MenuRegistry.ofExtended
    public static final RegistrySupplier<MenuType<SynthesizerUserInterfaceMenu>> SYNTHESIZER_USER_INTERFACE_MENU =
            MENU_TYPES.register("synthesizer_user_interface", () -> MenuRegistry.ofExtended(SynthesizerUserInterfaceMenu::new));
}