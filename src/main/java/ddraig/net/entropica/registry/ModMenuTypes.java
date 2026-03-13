package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.inventory.SynthesizerUserInterfaceMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    // Create the DeferredRegister for Menu Types
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Entropica.MODID);

    // Register the Synthesizer User Interface Menu
    public static final DeferredHolder<MenuType<?>, MenuType<SynthesizerUserInterfaceMenu>> SYNTHESIZER_USER_INTERFACE_MENU =
            MENU_TYPES.register("synthesizer_user_interface", () -> IMenuTypeExtension.create(SynthesizerUserInterfaceMenu::new));
}