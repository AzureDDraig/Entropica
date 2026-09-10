package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Entropica.MODID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> BLOCKS_TAB = CREATIVE_MODE_TABS.register("blocks_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.entropica.blocks"))
            .icon(() -> ModItems.MATERIA_FURNACE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ModBlocks.BLOCKS.getEntries().forEach(blockHolder -> {
                    if (blockHolder.isBound()) {
                        Item blockItem = blockHolder.get().asItem();
                        if (blockItem != Items.AIR && !isLogisticsItem(blockItem) && !isAestheticItem(blockItem) && !isWorldItem(blockItem)) {
                            output.accept(blockItem);
                        }
                    }
                });
            }).build());

    public static final RegistrySupplier<CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("items_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
            .title(Component.translatable("itemGroup.entropica.items"))
            .icon(() -> ModItems.ARCANUM_FOCUS.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().forEach(itemRegistryObject -> {
                    if (itemRegistryObject.isBound()) {
                        Item item = itemRegistryObject.get();

                        if (!(item instanceof net.minecraft.world.item.BlockItem) &&
                                !(item instanceof EssenceItem) &&
                                !(item instanceof EssenceAmpouleItem) &&
                                !(item instanceof VisFumeAmpouleItem) &&
                                !(item instanceof ddraig.net.entropica.item.ChalkItem) &&
                                !(item instanceof ddraig.net.entropica.item.SpectralDyeItem) &&
                                !itemRegistryObject.getId().getPath().contains("ampoule_base") &&
                                item != ModItems.SOULBOUND_BLADE.get() &&
                                item != ModItems.OBLIVION_BLADE.get() &&
                                item != ModItems.TIDAL_TRIDENT.get() &&
                                item != ModItems.VOID_SWORD.get() &&
                                item != ModItems.ESSENCE_HARVESTING_BLADE.get() &&
                                item != ModItems.BASALT_PICKAXE.get() &&
                                item != ModItems.WHISPERWOOD_WAND.get() &&
                                item != ModItems.SHIMMERING_FOCUS.get() &&
                                item != ModItems.AETHERIC_MONOCLE.get() &&
                                item != ModItems.MATERIA_VALUE_DETECTOR.get()) {

                            output.accept(item);
                        }
                    }
                });
            }).build());

    public static final RegistrySupplier<CreativeModeTab> MATERIA_ITEMS_TAB = CREATIVE_MODE_TABS.register("materia_items_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
            .title(Component.translatable("itemGroup.entropica.materia_items"))
            .icon(() -> {
                ItemStack iconStack = new ItemStack(ModItems.LARGE_ESSENCE_AMPOULE.get());
                EssenceAmpouleItem.setEssenceType(iconStack, EssenceType.CHIMERA);
                return iconStack;
            })
            .displayItems((parameters, output) -> {
                output.accept(ModItems.SMALL_AMPOULE_BASE.get());
                output.accept(ModItems.MEDIUM_AMPOULE_BASE.get());
                output.accept(ModItems.LARGE_AMPOULE_BASE.get());

                for (EssenceType type : EssenceType.values()) {
                    if (type.isFragment()) {
                        ItemStack fragmentOrb = new ItemStack(ModItems.FRAGMENT_ESSENCE.get());
                        EssenceItem.setEssenceType(fragmentOrb, type);
                        output.accept(fragmentOrb);
                    } else {
                        ItemStack weakOrb = new ItemStack(ModItems.WEAK_ESSENCE.get());
                        EssenceItem.setEssenceType(weakOrb, type);
                        output.accept(weakOrb);

                        ItemStack avgOrb = new ItemStack(ModItems.AVERAGE_ESSENCE.get());
                        EssenceItem.setEssenceType(avgOrb, type);
                        output.accept(avgOrb);

                        ItemStack strongOrb = new ItemStack(ModItems.STRONG_ESSENCE.get());
                        EssenceItem.setEssenceType(strongOrb, type);
                        output.accept(strongOrb);

                        ItemStack smallEssenceAmp = new ItemStack(ModItems.SMALL_ESSENCE_AMPOULE.get());
                        EssenceAmpouleItem.setEssenceType(smallEssenceAmp, type);
                        output.accept(smallEssenceAmp);

                        ItemStack medEssenceAmp = new ItemStack(ModItems.MEDIUM_ESSENCE_AMPOULE.get());
                        EssenceAmpouleItem.setEssenceType(medEssenceAmp, type);
                        output.accept(medEssenceAmp);

                        ItemStack largeEssenceAmp = new ItemStack(ModItems.LARGE_ESSENCE_AMPOULE.get());
                        EssenceAmpouleItem.setEssenceType(largeEssenceAmp, type);
                        output.accept(largeEssenceAmp);

                        ItemStack smallFumeAmp = new ItemStack(ModItems.SMALL_MATERIA_FUMUS_AMPOULE.get());
                        VisFumeAmpouleItem.setEssenceType(smallFumeAmp, type);
                        output.accept(smallFumeAmp);

                        ItemStack medFumeAmp = new ItemStack(ModItems.MEDIUM_MATERIA_FUMUS_AMPOULE.get());
                        VisFumeAmpouleItem.setEssenceType(medFumeAmp, type);
                        output.accept(medFumeAmp);

                        ItemStack largeFumeAmp = new ItemStack(ModItems.LARGE_MATERIA_FUMUS_AMPOULE.get());
                        VisFumeAmpouleItem.setEssenceType(largeFumeAmp, type);
                        output.accept(largeFumeAmp);
                    }
                }
            }).build());

    public static final RegistrySupplier<CreativeModeTab> TOOLS_TAB = CREATIVE_MODE_TABS.register("tools_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
            .title(Component.translatable("itemGroup.entropica.tools"))
            .icon(() -> ModItems.WHISPERWOOD_WAND.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.BASALT_PICKAXE.get());
                output.accept(ModItems.RUBBER_TAP.get());
                output.accept(ModItems.WHISPERWOOD_WAND.get());
                output.accept(ModItems.SHIMMERING_FOCUS.get());
                output.accept(ModItems.AETHERIC_MONOCLE.get());
                output.accept(ModItems.PROPAGATION_LENS.get());
                output.accept(ModItems.AETHERIC_LENS.get());
                output.accept(ModItems.VITAE_LENS.get());
                output.accept(ModItems.MATERIA_LENS.get());
                output.accept(ModItems.ARKANIST_MONOCLE.get());
                output.accept(ModItems.MATERIA_VALUE_DETECTOR.get());
                output.accept(ModItems.ARCANUM_FOCUS.get());
                output.accept(ModItems.CHALK.get());
                output.accept(ModItems.DULL_CHALK.get());
                output.accept(ModItems.CONDUCTIVE_CHALK.get());
                output.accept(ModItems.RESONANT_CHALK.get());
                output.accept(ModItems.EIDOLIC_CHALK.get());
                output.accept(ModItems.ADVANCED_CHALK.get());
                output.accept(ModItems.ADVANCED_DULL_CHALK.get());
                output.accept(ModItems.ADVANCED_CONDUCTIVE_CHALK.get());
                output.accept(ModItems.ADVANCED_EIDOLIC_CHALK.get());
                // Astral Materia Instruments, Tools & Baubles
                output.accept(ModItems.LOOKING_GLASS.get());
                output.accept(ModItems.ASTROLABE.get());
                output.accept(ModItems.DRAFTING_COMPASS.get());
                output.accept(ModItems.STAR_CHART_BLANK.get());
                output.accept(ModItems.STAR_CHART_COMPLETED.get());
                output.accept(ModItems.ASTRAL_LINKING_WAND.get());
                output.accept(ModItems.CELESTIAL_ATLAS.get());
                output.accept(ModItems.MATERIA_FLUX_DISTRIBUTOR_ITEM.get());
                output.accept(ModItems.MORTAR_AND_PESTLE.get());
                output.accept(ModItems.CRYSTAL_SWORD.get());
                output.accept(ModItems.CRYSTAL_PICKAXE.get());
                output.accept(ModItems.CRYSTAL_AXE.get());
                output.accept(ModItems.CRYSTAL_SHOVEL.get());
                output.accept(ModItems.CRYSTAL_HOE.get());
                output.accept(ModItems.DRAINED_CRYSTAL_SWORD.get());
                output.accept(ModItems.DRAINED_CRYSTAL_PICKAXE.get());
                output.accept(ModItems.DRAINED_CRYSTAL_AXE.get());
                output.accept(ModItems.DRAINED_CRYSTAL_SHOVEL.get());
                output.accept(ModItems.DRAINED_CRYSTAL_HOE.get());
                output.accept(ModItems.DRAINED_CRYSTAL_TOOL.get());
                output.accept(ModItems.RESPLENDENT_PRISM.get());
                output.accept(ModItems.MANTLE_OF_THE_STARS.get());
                output.accept(ModItems.GRAVITON_WAND.get());
                output.accept(ModItems.GRAVITON_SOLES.get());
                output.accept(ModItems.INERTIAL_ANCHOR_AMULET.get());
                output.accept(ModItems.SINGULARITY_GRENADE.get());
                output.accept(ModItems.FIRMAMENT_WEAVER.get());
                output.accept(ModBlocks.GRAVITON_BOUNCEPAD.get());
            }).build());

    public static final RegistrySupplier<CreativeModeTab> WEAPON_CRAFTING_TAB = CREATIVE_MODE_TABS.register("weapon_crafting_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
            .title(Component.translatable("itemGroup.entropica.weapon_crafting"))
            .icon(() -> ModItems.ORBIS_ACCEPTOR.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get());
                output.accept(ModBlocks.ATTUNEMENT_PEDESTAL.get());
                output.accept(ModBlocks.MORPHIC_LOOM.get());
                output.accept(ModBlocks.CRUCIBLE.get());
                output.accept(ModBlocks.MARBLE_RITUAL_BOWL.get());
                output.accept(ModBlocks.BASALT_RITUAL_BOWL.get());
                output.accept(ModBlocks.GRANITE_RITUAL_BOWL.get());
                output.accept(ModItems.SPELL_GEM.get());

                output.accept(ModItems.ARCANITE_WEAPON_CORE.get());
                output.accept(ModItems.CHARGED_ARCANITE_WEAPON_CORE.get());
                output.accept(ModItems.ANCIENT_ARCANITE_WEAPON_CORE.get());

                output.accept(ModItems.VISCANITE_WEAPON_CORE.get());
                output.accept(ModItems.CHARGED_VISCANITE_WEAPON_CORE.get());
                output.accept(ModItems.ANCIENT_VISCANITE_WEAPON_CORE.get());

                output.accept(ModItems.RESONITE_WEAPON_CORE.get());
                output.accept(ModItems.CHARGED_RESONITE_WEAPON_CORE.get());
                output.accept(ModItems.ANCIENT_RESONITE_WEAPON_CORE.get());

                output.accept(ModItems.ARCANITE_TOOL_CORE.get());
                output.accept(ModItems.CHARGED_ARCANITE_TOOL_CORE.get());
                output.accept(ModItems.ANCIENT_ARCANITE_TOOL_CORE.get());

                output.accept(ModItems.VISCANITE_TOOL_CORE.get());
                output.accept(ModItems.CHARGED_VISCANITE_TOOL_CORE.get());
                output.accept(ModItems.ANCIENT_VISCANITE_TOOL_CORE.get());

                output.accept(ModItems.RESONITE_TOOL_CORE.get());
                output.accept(ModItems.CHARGED_RESONITE_TOOL_CORE.get());
                output.accept(ModItems.ANCIENT_RESONITE_TOOL_CORE.get());

                output.accept(ModItems.EIDOLITE_CORE.get());
                output.accept(ModItems.CHARGED_EIDOLITE_CORE.get());
                output.accept(ModItems.ANCIENT_EIDOLITE_CORE.get());

                output.accept(ModItems.GLADIUS_SHAPE_CONCEPT.get());
                output.accept(ModItems.LONGSWORD_SHAPE_CONCEPT.get());
                output.accept(ModItems.SHORTSWORD_SHAPE_CONCEPT.get());
                output.accept(ModItems.AKRAFENA_SHAPE_CONCEPT.get());

                output.accept(ModItems.HAND_AXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.WAR_AXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.POLE_AXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.HALBERD_SHAPE_CONCEPT.get());
                output.accept(ModItems.BEARD_AXE_SHAPE_CONCEPT.get());

                output.accept(ModItems.SHORTBOW_SHAPE_CONCEPT.get());
                output.accept(ModItems.LONGBOW_SHAPE_CONCEPT.get());
                output.accept(ModItems.CROSSBOW_SHAPE_CONCEPT.get());
                output.accept(ModItems.REPEATER_SHAPE_CONCEPT.get());
                output.accept(ModItems.WAR_BOW_SHAPE_CONCEPT.get());

                output.accept(ModItems.PICKAXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.SHOVEL_SHAPE_CONCEPT.get());
                output.accept(ModItems.ADZE_SHAPE_CONCEPT.get());
                output.accept(ModItems.PAXEL_SHAPE_CONCEPT.get());

                output.accept(ModItems.SPEAR_SHAPE_CONCEPT.get());
                output.accept(ModItems.MACE_SHAPE_CONCEPT.get());
                output.accept(ModItems.MORNING_STAR_SHAPE_CONCEPT.get());
                output.accept(ModItems.WARHAMMER_SHAPE_CONCEPT.get());
            }).build());

    public static final RegistrySupplier<CreativeModeTab> WEAPONS_TAB = CREATIVE_MODE_TABS.register("weapons_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 5)
            .title(Component.translatable("itemGroup.entropica.weapons"))
            .icon(() -> ModItems.SOULBOUND_BLADE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.ESSENCE_HARVESTING_BLADE.get());
                output.accept(ModItems.SOULBOUND_BLADE.get());
                output.accept(ModItems.OBLIVION_BLADE.get());
                output.accept(ModItems.TIDAL_TRIDENT.get());
                output.accept(ModItems.VOID_SWORD.get());
                output.accept(ModItems.CRYSTAL_SWORD.get());
            }).build());

    public static final RegistrySupplier<CreativeModeTab> LOGISTICS_TAB = CREATIVE_MODE_TABS.register("logistics_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 6)
            .title(Component.translatable("itemGroup.entropica.logistics"))
            .icon(() -> ModItems.VAPOR_PNEUMATIC_PIPE_COPPER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ModBlocks.BLOCKS.getEntries().forEach(blockHolder -> {
                    Item blockItem = blockHolder.get().asItem();
                    if (blockItem != Items.AIR && isLogisticsItem(blockItem)) {
                        output.accept(blockItem);
                    }
                });
            }).build());

    private static boolean isLogisticsItem(Item item) {
        if (item == null) return false;
        net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        if (key == null) return false;
        String path = key.getPath();
        return path.contains("pipe") ||
               path.contains("pipeline") ||
               path.contains("conduit") ||
               path.contains("valve") ||
               path.contains("diverter") ||
               path.contains("port") ||
               path.contains("pump") ||
               path.contains("generator") ||
               path.contains("coupling") ||
               path.contains("distributor") ||
               path.contains("agitator") ||
               path.contains("bulb") ||
               path.contains("lamp_post") ||
               path.contains("fountain") ||
               path.contains("arbor") ||
               path.contains("vitrine") ||
               path.contains("clock") ||
               path.contains("balustrade") ||
               path.contains("optic");
    }

    public static final RegistrySupplier<CreativeModeTab> AESTHETICA_TAB = CREATIVE_MODE_TABS.register("aesthetica_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0)
            .title(Component.translatable("itemGroup.entropica.aesthetica"))
            .icon(() -> ddraig.net.entropica.registry.AestheticGlassRegistry.ESSENCE_ENRICHED_GLASS.get().asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ddraig.net.entropica.registry.AestheticGlassRegistry.ALL_GLASS_ITEMS.forEach(itemSup -> output.accept(itemSup.get()));
            }).build());

    public static final RegistrySupplier<CreativeModeTab> WORLD_TAB = CREATIVE_MODE_TABS.register("world_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 1)
            .title(Component.translatable("itemGroup.entropica.world"))
            .icon(() -> ModItems.GREATER_MATERIA_BLESSING_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // Materia Blessings
                output.accept(ModItems.MATERIA_BLESSING_ITEM.get());
                output.accept(ModItems.GREATER_MATERIA_BLESSING_ITEM.get());

                // Ores
                output.accept(ModBlocks.ENTROPIC_ORE.get());
                output.accept(ModItems.VORPALITE_ORE_ITEM.get());
                output.accept(ModItems.SORROWSTONE_ORE_ITEM.get());
                output.accept(ModItems.UMBRALITE_ORE_ITEM.get());

                // Aeterium Crystals & Buds
                output.accept(ModItems.AETERIUM_CRYSTAL_BLOCK_ITEM.get());
                output.accept(ModItems.BUDDING_AETERIUM_ITEM.get());
                output.accept(ModItems.AETERIUM_CLUSTER_ITEM.get());
                output.accept(ModItems.LARGE_AETERIUM_BUD_ITEM.get());
                output.accept(ModItems.MEDIUM_AETERIUM_BUD_ITEM.get());
                output.accept(ModItems.SMALL_AETERIUM_BUD_ITEM.get());

                // Ignisite Crystals & Buds
                output.accept(ModItems.IGNISITE_CRYSTAL_BLOCK_ITEM.get());
                output.accept(ModItems.BUDDING_IGNISITE_ITEM.get());
                output.accept(ModItems.IGNISITE_CLUSTER_ITEM.get());
                output.accept(ModItems.LARGE_IGNISITE_BUD_ITEM.get());
                output.accept(ModItems.MEDIUM_IGNISITE_BUD_ITEM.get());
                output.accept(ModItems.SMALL_IGNISITE_BUD_ITEM.get());

                // Mortisite Crystals & Buds
                output.accept(ModItems.MORTISITE_CRYSTAL_BLOCK_ITEM.get());
                output.accept(ModItems.BUDDING_MORTISITE_ITEM.get());
                output.accept(ModItems.MORTISITE_CLUSTER_ITEM.get());
                output.accept(ModItems.LARGE_MORTISITE_BUD_ITEM.get());
                output.accept(ModItems.MEDIUM_MORTISITE_BUD_ITEM.get());
                output.accept(ModItems.SMALL_MORTISITE_BUD_ITEM.get());

                // Flora & Trees
                output.accept(ModItems.RUBBER_LOG_ITEM.get());
                output.accept(ModItems.RUBBER_WOOD_ITEM.get());
                output.accept(ModItems.STRIPPED_RUBBER_LOG_ITEM.get());
                output.accept(ModItems.STRIPPED_RUBBER_WOOD_ITEM.get());
                output.accept(ModItems.RUBBER_PLANKS_ITEM.get());
                output.accept(ModItems.RUBBER_LEAVES_ITEM.get());
                output.accept(ModItems.RUBBER_SAPLING_ITEM.get());
                output.accept(ModItems.RAW_RUBBER.get());
                output.accept(ModItems.SHIMMERPETAL_ITEM.get());
                output.accept(ModItems.SILVER_PINE_LOG_ITEM.get());
                output.accept(ModItems.SILVER_PINE_WOOD_ITEM.get());
                output.accept(ModItems.STRIPPED_SILVER_PINE_LOG_ITEM.get());
                output.accept(ModItems.STRIPPED_SILVER_PINE_WOOD_ITEM.get());
                output.accept(ModItems.SILVER_PINE_PLANKS_ITEM.get());
                output.accept(ModItems.SILVER_PINE_LEAVES_ITEM.get());
                output.accept(ModItems.SILVER_PINE_SAPLING_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_WOOD_ITEM.get());
                output.accept(ModItems.STRIPPED_ASTRAL_VEIL_WILLOW_WOOD_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_STAIRS_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_SLAB_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_VERTICAL_SLAB_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_FENCE_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_FENCE_GATE_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_BUTTON_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_PRESSURE_PLATE_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_CHEST_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_WOOD_ITEM.get());
                output.accept(ModItems.STRIPPED_VOID_BLIGHT_MANGROVE_WOOD_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_STAIRS_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_SLAB_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_VERTICAL_SLAB_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_FENCE_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_FENCE_GATE_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_BUTTON_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_PRESSURE_PLATE_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_CHEST_ITEM.get());
                output.accept(ModItems.RUBBER_STAIRS_ITEM.get());
                output.accept(ModItems.RUBBER_SLAB_ITEM.get());
                output.accept(ModItems.RUBBER_VERTICAL_SLAB_ITEM.get());
                output.accept(ModItems.RUBBER_FENCE_ITEM.get());
                output.accept(ModItems.RUBBER_FENCE_GATE_ITEM.get());
                output.accept(ModItems.RUBBER_BUTTON_ITEM.get());
                output.accept(ModItems.RUBBER_PRESSURE_PLATE_ITEM.get());
                output.accept(ModItems.RUBBER_CHEST_ITEM.get());
                output.accept(ModItems.SILVER_PINE_STAIRS_ITEM.get());
                output.accept(ModItems.SILVER_PINE_SLAB_ITEM.get());
                output.accept(ModItems.SILVER_PINE_VERTICAL_SLAB_ITEM.get());
                output.accept(ModItems.SILVER_PINE_FENCE_ITEM.get());
                output.accept(ModItems.SILVER_PINE_FENCE_GATE_ITEM.get());
                output.accept(ModItems.SILVER_PINE_BUTTON_ITEM.get());
                output.accept(ModItems.SILVER_PINE_PRESSURE_PLATE_ITEM.get());
                output.accept(ModItems.SILVER_PINE_CHEST_ITEM.get());
                output.accept(ModItems.AMBER_STAIRS_ITEM.get());
                output.accept(ModItems.AMBER_SLAB_ITEM.get());
                output.accept(ModItems.AMBER_VERTICAL_SLAB_ITEM.get());
                output.accept(ModItems.AMBER_FENCE_ITEM.get());
                output.accept(ModItems.AMBER_FENCE_GATE_ITEM.get());
                output.accept(ModItems.AMBER_BUTTON_ITEM.get());
                output.accept(ModItems.AMBER_PRESSURE_PLATE_ITEM.get());
                output.accept(ModItems.AMBER_CHEST_ITEM.get());

                
                // Astral-Veil Willow Set
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_LOG_ITEM.get());
                output.accept(ModItems.STRIPPED_ASTRAL_VEIL_WILLOW_LOG_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_PLANKS_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_LEAVES_ITEM.get());
                output.accept(ModItems.ASTRAL_VEIL_WILLOW_SAPLING_ITEM.get());

                // Void-Blight Mangrove Set
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_LOG_ITEM.get());
                output.accept(ModItems.STRIPPED_VOID_BLIGHT_MANGROVE_LOG_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_PLANKS_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_LEAVES_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_ROOT_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_MANGROVE_SAPLING_ITEM.get());
                output.accept(ModItems.VOID_BLIGHT_POD.get());

                // Nether Flora Blocks
                output.accept(ModItems.SPORE_CANNON_PUFFBALL_ITEM.get());
                output.accept(ModItems.PYRE_THORN_LAUNCHER_ITEM.get());
                output.accept(ModItems.SPORE_BEARING_PITCHER_PLUMP_ITEM.get());
                output.accept(ModItems.BLOOD_TENDRIL_BRAMBLE_ITEM.get());
                output.accept(ModItems.SOOT_VEIL_BLIGHT_CAP_ITEM.get());
                output.accept(ModItems.MAGMA_GRIP_TENDRILS_ITEM.get());

                // Amber-Wood Set
                output.accept(ModItems.AMBER_LOG_ITEM.get());
                output.accept(ModItems.AMBER_WOOD_ITEM.get());
                output.accept(ModItems.STRIPPED_AMBER_LOG_ITEM.get());
                output.accept(ModItems.STRIPPED_AMBER_WOOD_ITEM.get());
                output.accept(ModItems.AMBER_PLANKS_ITEM.get());
                output.accept(ModItems.AMBER_LEAVES_ITEM.get());
                output.accept(ModItems.AMBER_SAPLING_ITEM.get());
                output.accept(ModItems.AMBER_CHUNK.get());


                output.accept(ModItems.RIMEBLOOM_ITEM.get());

                output.accept(ModItems.AEGIS_ROSE_ITEM.get());
                output.accept(ModItems.AEGIS_ROSE_PETALS.get());
                output.accept(ModItems.AMBER_NECTAR_BLOSSOM_ITEM.get());
                output.accept(ModItems.AMBER_NECTAR_CUP.get());
                output.accept(ModItems.AMBER_NECTAR.get());
                output.accept(ModItems.SOUL_FLAME_ORCHID_ITEM.get());
                output.accept(ModItems.SOUL_FLAME_PETAL.get());
                output.accept(ModItems.SOULFIRE_NECTAR.get());
                output.accept(ModItems.AURORAL_BUTTERCUP_ITEM.get());
                output.accept(ModItems.AURORAL_PETAL.get());
                output.accept(ModItems.AURORAL_POLLEN.get());
                output.accept(ModItems.STARDUST_BELL_ITEM.get());
                output.accept(ModItems.STARDUST_BELL_PETAL.get());
                output.accept(ModItems.STARDUST_NECTAR.get());
                output.accept(ModItems.FULGURITE_SWAMP_BLOOM_ITEM.get());
                output.accept(ModItems.FULGURITE_PETAL.get());
                output.accept(ModItems.FULGURITE_STIGMA.get());
                output.accept(ModItems.GALE_BLOOM_DANDELION_ITEM.get());
                output.accept(ModItems.GALE_SPORE_PUFF.get());
                output.accept(ModItems.GALE_POPPED_SPORE.get());
                output.accept(ModItems.CRYO_STATIC_SHRUB_ITEM.get());
                output.accept(ModItems.CRYO_STATIC_TWIG.get());
                output.accept(ModItems.CRYO_STATIC_LEAF.get());
                output.accept(ModItems.CRYO_STATIC_ROOTLING.get());
                output.accept(ModItems.VITREOUS_CACTUS_ITEM.get());
                output.accept(ModItems.VITREOUS_NEEDLE.get());
                output.accept(ModItems.VITREOUS_CACTUS_FLESH.get());
                output.accept(ModItems.BARROW_MOSS_ITEM.get());
                output.accept(ModItems.BARROW_MOSS_CARPET_ITEM.get());
                output.accept(ModItems.BARROW_MOSS_FIBER.get());
                output.accept(ModItems.ABYSSAL_WEEPROOT_ITEM.get());
                output.accept(ModItems.ABYSSAL_TENDRIL.get());
                output.accept(ModItems.BLOOD_ROOT_SUCCULENT_ITEM.get());
                output.accept(ModItems.BLOOD_ROOT_PULP.get());

                // Round 2 Flora Additions
                output.accept(ModItems.VITAE_ORCHID_ITEM.get());
                output.accept(ModItems.VITAE_PETAL.get());
                output.accept(ModItems.SPORE_BURST_PUFFBALL_ITEM.get());
                output.accept(ModItems.SPORE_PUFF.get());
                output.accept(ModItems.FULGURITE_REED_ITEM.get());
                output.accept(ModItems.GALE_THISTLE_ITEM.get());
                output.accept(ModItems.GALE_SEED.get());
                output.accept(ModItems.MIST_VEIL_MARSHMALLOW_ITEM.get());
                output.accept(ModItems.MIST_VEIL_MARSHMALLOW_POD.get());

                // Round 3 Flora Additions
                output.accept(ModItems.AEGIS_SPIRE_ORCHID_ITEM.get());
                output.accept(ModItems.TALL_AEGIS_SPIRE_ORCHID_ITEM.get());
                output.accept(ModItems.AEGIS_SPIRE_PETAL.get());
                output.accept(ModItems.PYRE_SPROUT_ITEM.get());
                output.accept(ModItems.PYRE_SPROUT_SEED.get());
                output.accept(ModItems.EMBER_PULP.get());
                output.accept(ModItems.AURA_DRIFT_SEDGE_ITEM.get());
                output.accept(ModItems.AURA_DRIFT_FIBER.get());
                output.accept(ModItems.SPECTRAL_LANTERN_FLOWER_ITEM.get());
                output.accept(ModItems.SPECTRAL_LANTERN_POD.get());
                output.accept(ModItems.CINDER_SPORE_MUSHROOM_ITEM.get());
                output.accept(ModItems.CINDER_SPORE_CAP.get());

                // Round 4 Flora Additions
                output.accept(ModItems.NECROTIC_ROSE_OF_JERICHO_ITEM.get());
                output.accept(ModItems.NECROTIC_ROSE_PETAL.get());
                output.accept(ModItems.VOID_STALKER_ORCHID_ITEM.get());
                output.accept(ModItems.VOID_STALKER_PETAL.get());
                output.accept(ModItems.STARDUST_ALOE_ITEM.get());
                output.accept(ModItems.STARDUST_ALOE_LEAF.get());
                output.accept(ModItems.SANGUINE_LILY_ITEM.get());
                output.accept(ModItems.SANGUINE_LILY_PAD.get());
                output.accept(ModItems.AURORAL_LILY_PAD_ITEM.get());
                output.accept(ModItems.CINDER_GRIP_LICHEN_ITEM.get());
                output.accept(ModItems.CINDER_LICHEN_FLAKES.get());
                output.accept(ModItems.SOOT_SHROUD_FUNGI_ITEM.get());
                output.accept(ModItems.SOOT_SHROUD_CAP.get());
                output.accept(ModItems.PYROCYST_ALGAE_ITEM.get());
                output.accept(ModItems.PYROCYST_VESICLE.get());

                output.accept(ModItems.BARROW_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.BARROW_FUNGAL_CAP.get());
                output.accept(ModItems.SPORE_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.SPORE_FUNGAL_CAP.get());
                output.accept(ModItems.BLIGHT_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.BLIGHT_FUNGAL_CAP.get());
                output.accept(ModItems.FROST_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.FROST_FUNGAL_CAP.get());
                output.accept(ModItems.CINDER_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.CINDER_FUNGAL_CAP.get());
                output.accept(ModItems.ASTRAL_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.ASTRAL_FUNGAL_CAP.get());
                output.accept(ModItems.DAWN_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.DAWN_FUNGAL_CAP.get());
                output.accept(ModItems.SANGUINE_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.SANGUINE_FUNGAL_CAP.get());
                output.accept(ModItems.STATIC_FUNGAL_SHELF_CAP_ITEM.get());
                output.accept(ModItems.STATIC_FUNGAL_CAP.get());

                // 4 Tree Families Wood Suites
                                output.accept(ModItems.ECHO_FRUIT.get());
                output.accept(ModItems.MATERIA_ECHO_BARK.get());
                output.accept(ModItems.MATERIA_ECHO_LOG.get());
                output.accept(ModItems.STRIPPED_MATERIA_ECHO_LOG.get());
                output.accept(ModItems.MATERIA_ECHO_WOOD.get());
                output.accept(ModItems.STRIPPED_MATERIA_ECHO_WOOD.get());
                output.accept(ModItems.MATERIA_ECHO_PLANKS.get());
                output.accept(ModItems.MATERIA_ECHO_LEAVES.get());
                output.accept(ModItems.MATERIA_ECHO_SAPLING.get());
                output.accept(ModItems.MATERIA_ECHO_STAIRS.get());
                output.accept(ModItems.MATERIA_ECHO_SLAB.get());
                output.accept(ModItems.MATERIA_ECHO_VERTICAL_SLAB.get());
                output.accept(ModItems.MATERIA_ECHO_FENCE.get());
                output.accept(ModItems.MATERIA_ECHO_FENCE_GATE.get());
                output.accept(ModItems.MATERIA_ECHO_BUTTON.get());
                output.accept(ModItems.MATERIA_ECHO_PRESSURE_PLATE.get());
                output.accept(ModItems.MATERIA_ECHO_CHEST_ITEM.get());
                output.accept(ModItems.CINDER_ASH_FLAKES.get());
                output.accept(ModItems.BIOLUMINESCENT_SPORE_POD.get());
                output.accept(ModItems.AETHERIC_BIRCH_BARK.get());
                output.accept(ModItems.STARLIGHT_FLAKES.get());
                output.accept(ModItems.IRON_ROOT_SAP.get());
                output.accept(ModItems.ASTRAL_WILLOW_FIBRE.get());
                output.accept(ModItems.BLIGHTED_BARK_FLAKES.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_LOG.get());
                output.accept(ModItems.STRIPPED_PYRE_ASH_CEDAR_LOG.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_WOOD.get());
                output.accept(ModItems.STRIPPED_PYRE_ASH_CEDAR_WOOD.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_PLANKS.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_LEAVES.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_SAPLING.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_STAIRS.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_SLAB.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_VERTICAL_SLAB.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_FENCE.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_FENCE_GATE.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_BUTTON.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_PRESSURE_PLATE.get());
                output.accept(ModItems.PYRE_ASH_CEDAR_CHEST_ITEM.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_LOG.get());
                output.accept(ModItems.STRIPPED_ABYSSAL_SPORE_CYPRESS_LOG.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_WOOD.get());
                output.accept(ModItems.STRIPPED_ABYSSAL_SPORE_CYPRESS_WOOD.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_PLANKS.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_LEAVES.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_SAPLING.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_STAIRS.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_SLAB.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_VERTICAL_SLAB.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_FENCE.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_FENCE_GATE.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_BUTTON.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_PRESSURE_PLATE.get());
                output.accept(ModItems.ABYSSAL_SPORE_CYPRESS_CHEST_ITEM.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_LOG.get());
                output.accept(ModItems.STRIPPED_STARLIGHT_AETHER_BIRCH_LOG.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_WOOD.get());
                output.accept(ModItems.STRIPPED_STARLIGHT_AETHER_BIRCH_WOOD.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_PLANKS.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_LEAVES.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_SAPLING.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_STAIRS.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_SLAB.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_VERTICAL_SLAB.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_FENCE.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_FENCE_GATE.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_BUTTON.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_PRESSURE_PLATE.get());
                output.accept(ModItems.STARLIGHT_AETHER_BIRCH_CHEST_ITEM.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_LOG.get());
                output.accept(ModItems.STRIPPED_BLOOD_ROOT_IRON_OAK_LOG.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_WOOD.get());
                output.accept(ModItems.STRIPPED_BLOOD_ROOT_IRON_OAK_WOOD.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_PLANKS.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_LEAVES.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_SAPLING.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_STAIRS.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_SLAB.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_VERTICAL_SLAB.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_FENCE.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_FENCE_GATE.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_BUTTON.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_PRESSURE_PLATE.get());
                output.accept(ModItems.BLOOD_ROOT_IRON_OAK_CHEST_ITEM.get());




                // Dynamic Spectral Dye Category
                for (var dyeSupplier : ModItems.SPECTRAL_DYES.values()) {
                    output.accept(dyeSupplier.get());
                }

                // Indigenous Fauna Spawn Eggs
                output.accept(ModItems.GROT_SPAWN_EGG.get());
                output.accept(ModItems.VEIL_FOX_SPAWN_EGG.get());
                output.accept(ModItems.ASHEN_STALKER_SPAWN_EGG.get());
                output.accept(ModItems.SPORE_DRIFTER_SPAWN_EGG.get());
                output.accept(ModItems.RIME_BACK_OVIS_SPAWN_EGG.get());
                output.accept(ModItems.OVERGROWTH_OVIS_SPAWN_EGG.get());
                output.accept(ModItems.RIME_SHEPHERD_SPAWN_EGG.get());
                output.accept(ModItems.BLOOM_CRAWLER_SPAWN_EGG.get());
            }).build());

    private static boolean isAestheticItem(Item item) {
        if (item == null) return false;
        return ddraig.net.entropica.registry.AestheticGlassRegistry.ALL_GLASS_ITEMS.stream().anyMatch(sup -> sup != null && sup.get() == item);
    }

    private static boolean isWorldItem(Item item) {
        if (item == null) return false;
        net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        if (key == null) return false;
        String path = key.getPath();

        if (path.contains("core") || path.contains("glass") || path.contains("machine") || path.contains("pipe")) {
            return false;
        }

        return path.equals("materia_blessing") ||
               path.equals("greater_materia_blessing") ||
               path.endsWith("_ore") ||
               path.contains("aeterium") ||
               path.contains("ignisite") ||
               path.contains("mortisite") ||
               path.contains("rubber") ||
               path.contains("silver_pine") ||
               path.contains("amber") ||
               path.contains("rose") ||
               path.contains("lily") ||
               path.contains("orchid") ||
               path.contains("buttercup") ||
               path.contains("bell") ||
               path.contains("bloom") ||
               path.contains("shrub") ||
               path.contains("cactus") ||
               path.contains("moss") ||
               path.contains("weeproot") ||
               path.contains("succulent") ||
               path.contains("puffball") ||
               path.contains("reed") ||
               path.contains("thistle") ||
               path.contains("marshmallow") ||
               path.contains("sprout") ||
               path.contains("sedge") ||
               path.contains("lantern") ||
               path.contains("mushroom") ||
               path.contains("aloe") ||
               path.contains("shimmerpetal") ||
               path.contains("blossom") ||
               path.contains("sapling") ||
               path.contains("chest") ||
               path.contains("spawn_egg");
    }


}
