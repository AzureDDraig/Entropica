package ddraig.net.entropica;

import ddraig.net.entropica.command.ModCommands;
import ddraig.net.entropica.event.EssenceMappingEvents;
import ddraig.net.entropica.event.MobDropHandler;
import ddraig.net.entropica.event.ModEntityEvents;
import ddraig.net.entropica.network.ModNetwork;
import ddraig.net.entropica.registry.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class Entropica {
    public static final String MODID = "entropica";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LOGGER.info("Entropica: Where Life Fuels Progress - Common Initialization starting.");

        // --- Register the Registries ---
        ModDataComponents.COMPONENTS.register();
        ModEffects.EFFECTS.register();
        ModBlocks.BLOCKS.register();
        ModItems.ITEMS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();
        ModEntityTypes.ENTITY_TYPES.register();
        ModParticles.PARTICLES.register();
        ModRecipes.register();
        ModSounds.SOUNDS.register();
        ModMenuTypes.MENU_TYPES.register();
        ModCreativeTabs.CREATIVE_MODE_TABS.register();

        // --- Register Network Receiver and Commands ---
        ModNetwork.register();
        ModCommands.register();

        // --- Register Common Events ---
        ModEntityEvents.register();
        EssenceMappingEvents.register();
        MobDropHandler.register();
        ddraig.net.entropica.event.ParalyzedEventHandler.register();
        ddraig.net.entropica.event.CodexJoinHandler.register();
        ddraig.net.entropica.event.MobObservationHandler.register();
        ddraig.net.entropica.event.CodexCraftingLockHandler.register();


        LOGGER.info("Entropica: Common Initialization completed.");
    }
}