package ddraig.net.entropica;

import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Entropica.MODID)
public class Entropica {
    public static final String MODID = "entropica";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS_TAB = CREATIVE_MODE_TABS.register("blocks_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.blocks"))
            .icon(() -> ModItems.MANA_FURNACE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ModBlocks.BLOCKS.getEntries().forEach(blockHolder -> {
                    Item blockItem = blockHolder.get().asItem();
                    if (blockItem != net.minecraft.world.item.Items.AIR) {
                        output.accept(blockItem);
                    }
                });
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("items_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.items"))
            .icon(() -> ModItems.MEDIUM_CHIMERA_AMPOULE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.MEDIUM_CHIMERA_AMPOULE.get());
                output.accept(ModItems.ARCANUM_FOCUS.get());
                // Also adding the Detector here for general visibility
                output.accept(ModItems.VIS_VALUE_DETECTOR.get());

                ModItems.ITEMS.getEntries().forEach(itemRegistryObject -> {
                    Item item = itemRegistryObject.get();
                    String name = itemRegistryObject.getId().getPath();
                    if (name.contains("essence") || name.contains("ampoule")) {
                        output.accept(item);
                    }
                });
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_TAB = CREATIVE_MODE_TABS.register("tools_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.tools"))
            .icon(() -> ModItems.WHISPERWOOD_WAND.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.BASALT_PICKAXE.get());
                output.accept(ModItems.WHISPERWOOD_WAND.get());
                output.accept(ModItems.SHIMMERING_FOCUS.get());
                // NEW: Added Vis Value Detector to the Tools tab
                output.accept(ModItems.VIS_VALUE_DETECTOR.get());
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEAPONS_TAB = CREATIVE_MODE_TABS.register("weapons_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.weapons"))
            .icon(() -> ModItems.SOULBOUND_BLADE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.SOULBOUND_BLADE.get());
                output.accept(ModItems.OBLIVION_BLADE.get());
                output.accept(ModItems.TIDAL_TRIDENT.get());
            }).build());

    public Entropica(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModRecipes.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, EntropicaConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Entropica: Where Life Fuels Progress setup initialized.");
    }
}