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
                    Item blockItem = blockHolder.get().asItem();
                    if (blockItem != Items.AIR && !isLogisticsItem(blockItem)) {
                        output.accept(blockItem);
                    }
                });
            }).build());

    public static final RegistrySupplier<CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("items_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
            .title(Component.translatable("itemGroup.entropica.items"))
            .icon(() -> ModItems.ARCANUM_FOCUS.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().forEach(itemRegistryObject -> {
                    Item item = itemRegistryObject.get();

                    // Filter out items that have dedicated tabs or are dynamic variants
                    if (!(item instanceof net.minecraft.world.item.BlockItem) &&
                            !(item instanceof EssenceItem) &&
                            !(item instanceof EssenceAmpouleItem) &&
                            !(item instanceof VisFumeAmpouleItem) &&
                            !(item instanceof ddraig.net.entropica.item.ChalkItem) &&
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
                // 1. Add Empty Base Ampoules First
                output.accept(ModItems.SMALL_AMPOULE_BASE.get());
                output.accept(ModItems.MEDIUM_AMPOULE_BASE.get());
                output.accept(ModItems.LARGE_AMPOULE_BASE.get());

                // 2. Dynamically Generate Every Variation
                for (EssenceType type : EssenceType.values()) {

                    if (type.isFragment()) {
                        // FRAGMENTS: Only generate the Tier 0 Fragment Orb, NO Ampoules!
                        ItemStack fragmentOrb = new ItemStack(ModItems.FRAGMENT_ESSENCE.get());
                        EssenceItem.setEssenceType(fragmentOrb, type);
                        output.accept(fragmentOrb);
                    } else {
                        // STANDARD ESSENCES: Generate all 3 tiers of Orbs and all 3 types of Ampoules

                        // Orbs
                        ItemStack weakOrb = new ItemStack(ModItems.WEAK_ESSENCE.get());
                        EssenceItem.setEssenceType(weakOrb, type);
                        output.accept(weakOrb);

                        ItemStack avgOrb = new ItemStack(ModItems.AVERAGE_ESSENCE.get());
                        EssenceItem.setEssenceType(avgOrb, type);
                        output.accept(avgOrb);

                        ItemStack strongOrb = new ItemStack(ModItems.STRONG_ESSENCE.get());
                        EssenceItem.setEssenceType(strongOrb, type);
                        output.accept(strongOrb);

                        // Essence Ampoules (Physical Material)
                        ItemStack smallEssenceAmp = new ItemStack(ModItems.SMALL_ESSENCE_AMPOULE.get());
                        EssenceAmpouleItem.setEssenceType(smallEssenceAmp, type);
                        output.accept(smallEssenceAmp);

                        ItemStack medEssenceAmp = new ItemStack(ModItems.MEDIUM_ESSENCE_AMPOULE.get());
                        EssenceAmpouleItem.setEssenceType(medEssenceAmp, type);
                        output.accept(medEssenceAmp);

                        ItemStack largeEssenceAmp = new ItemStack(ModItems.LARGE_ESSENCE_AMPOULE.get());
                        EssenceAmpouleItem.setEssenceType(largeEssenceAmp, type);
                        output.accept(largeEssenceAmp);

                        // Materia Fume Ampoules (Gaseous Material)
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
                output.accept(ModItems.WHISPERWOOD_WAND.get());
                output.accept(ModItems.SHIMMERING_FOCUS.get());
                output.accept(ModItems.AETHERIC_MONOCLE.get());
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
                output.accept(ModItems.ADVANCED_RESONANT_CHALK.get());
                output.accept(ModItems.ADVANCED_EIDOLIC_CHALK.get());
            }).build());

    public static final RegistrySupplier<CreativeModeTab> WEAPON_CRAFTING_TAB = CREATIVE_MODE_TABS.register("weapon_crafting_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
            .title(Component.translatable("itemGroup.entropica.weapon_crafting"))
            .icon(() -> ModItems.ORBIS_ACCEPTOR.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // Blocks
                output.accept(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get());
                output.accept(ModBlocks.ATTUNEMENT_PEDESTAL.get());
                output.accept(ModBlocks.MORPHIC_LOOM.get());
                output.accept(ModBlocks.CRUCIBLE.get());
                output.accept(ModBlocks.MARBLE_RITUAL_BOWL.get()); // Ritual Bowls
                output.accept(ModBlocks.BASALT_RITUAL_BOWL.get());
                output.accept(ModItems.SPELL_GEM.get());

                // Weapon Cores
                output.accept(ModItems.ARCANITE_WEAPON_CORE.get());
                output.accept(ModItems.CHARGED_ARCANITE_WEAPON_CORE.get());
                output.accept(ModItems.ANCIENT_ARCANITE_WEAPON_CORE.get());

                output.accept(ModItems.VISCANITE_WEAPON_CORE.get());
                output.accept(ModItems.CHARGED_VISCANITE_WEAPON_CORE.get());
                output.accept(ModItems.ANCIENT_VISCANITE_WEAPON_CORE.get());

                output.accept(ModItems.RESONITE_WEAPON_CORE.get());
                output.accept(ModItems.CHARGED_RESONITE_WEAPON_CORE.get());
                output.accept(ModItems.ANCIENT_RESONITE_WEAPON_CORE.get());

                // Tool Cores
                output.accept(ModItems.ARCANITE_TOOL_CORE.get());
                output.accept(ModItems.CHARGED_ARCANITE_TOOL_CORE.get());
                output.accept(ModItems.ANCIENT_ARCANITE_TOOL_CORE.get());

                output.accept(ModItems.VISCANITE_TOOL_CORE.get());
                output.accept(ModItems.CHARGED_VISCANITE_TOOL_CORE.get());
                output.accept(ModItems.ANCIENT_VISCANITE_TOOL_CORE.get());

                output.accept(ModItems.RESONITE_TOOL_CORE.get());
                output.accept(ModItems.CHARGED_RESONITE_TOOL_CORE.get());
                output.accept(ModItems.ANCIENT_RESONITE_TOOL_CORE.get());

                // Eidolite Cores
                output.accept(ModItems.EIDOLITE_CORE.get());
                output.accept(ModItems.CHARGED_EIDOLITE_CORE.get());
                output.accept(ModItems.ANCIENT_EIDOLITE_CORE.get());

                // Shape Concepts (Swords)
                output.accept(ModItems.GLADIUS_SHAPE_CONCEPT.get());
                output.accept(ModItems.LONGSWORD_SHAPE_CONCEPT.get());
                output.accept(ModItems.SHORTSWORD_SHAPE_CONCEPT.get());
                output.accept(ModItems.AKRAFENA_SHAPE_CONCEPT.get());

                // Shape Concepts (Axes)
                output.accept(ModItems.HAND_AXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.WAR_AXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.POLE_AXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.HALBERD_SHAPE_CONCEPT.get());
                output.accept(ModItems.BEARD_AXE_SHAPE_CONCEPT.get());

                // Shape Concepts (Bows)
                output.accept(ModItems.SHORTBOW_SHAPE_CONCEPT.get());
                output.accept(ModItems.LONGBOW_SHAPE_CONCEPT.get());
                output.accept(ModItems.CROSSBOW_SHAPE_CONCEPT.get());
                output.accept(ModItems.REPEATER_SHAPE_CONCEPT.get());
                output.accept(ModItems.WAR_BOW_SHAPE_CONCEPT.get());

                // Shape Concepts (Tools)
                output.accept(ModItems.PICKAXE_SHAPE_CONCEPT.get());
                output.accept(ModItems.SHOVEL_SHAPE_CONCEPT.get());
                output.accept(ModItems.ADZE_SHAPE_CONCEPT.get());
                output.accept(ModItems.PAXEL_SHAPE_CONCEPT.get());

                // Shape Concepts (Others)
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
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        return path.contains("pipe") ||
               path.contains("pipeline") ||
               path.contains("conduit") ||
               path.contains("valve") ||
               path.contains("diverter") ||
               path.contains("port") ||
               path.contains("pump") ||
               path.contains("generator") ||
               path.contains("coupling") ||
               path.contains("agitator");
    }
}