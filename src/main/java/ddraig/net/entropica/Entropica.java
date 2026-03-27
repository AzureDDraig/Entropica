package ddraig.net.entropica;

import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.event.ModEntityEvents;
import ddraig.net.entropica.network.ModNetwork;
import ddraig.net.entropica.registry.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Entropica.MODID)
public class Entropica {
    public static final String MODID = "entropica";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Entropica(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // --- Register the Network Packets ---
        modEventBus.addListener(ModNetwork::register);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus); // <-- Added Effects Registry here!
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        // --- Register Menu Types ---
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        // --- Register the new Creative Tabs Class ---
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, EntropicaConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Entropica: Where Life Fuels Progress setup initialized.");
    }
}