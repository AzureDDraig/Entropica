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
                output.accept(ModItems.SPELL_WAND.get());
                output.accept(ModItems.SPELL_STAFF.get());
                output.accept(ModItems.SPELL_GRIMOIRE.get());
                output.accept(ModItems.BLANK_SPELLGEM.get());
                output.accept(ModItems.SPELLGEM_SEED.get());
                output.accept(ModItems.BLANK_PARCHMENT.get());
                output.accept(ModItems.SCRIBING_INK.get());
                output.accept(ModItems.SPELL_PARCHMENT.get());
                output.accept(ModBlocks.ARCANE_SCRIBING_TABLE.get());
                output.accept(ModBlocks.RUNIC_ANVIL.get());
                output.accept(ModBlocks.AUTOCAST_PEDESTAL_STONE.get());
                output.accept(ModBlocks.AUTOCAST_PEDESTAL_MARBLE.get());
                output.accept(ModBlocks.AUTOCAST_PEDESTAL_QUARTZ.get());
                output.accept(ModBlocks.AUTOCAST_PEDESTAL_ASTRAL_SLATE.get());
                output.accept(ModItems.SPIKED_ARMOR_LINING.get());
                output.accept(ModItems.AEGIS_SHIELDING_LINING.get());
                output.accept(ModItems.THERMAL_INSULATED_BLANKET.get());
                output.accept(ModItems.GLACIAL_FOCUS_LENS.get());
                output.accept(ModItems.CAMOUFLAGE_CLOAK.get());
                output.accept(ModItems.AURORAL_WAND.get());
                output.accept(ModItems.CRYO_INSULATED_HELMET.get());
                output.accept(ModItems.CRYO_INSULATED_CHESTPLATE.get());
                output.accept(ModItems.CRYO_INSULATED_LEGGINGS.get());
                output.accept(ModItems.CRYO_INSULATED_BOOTS.get());
                output.accept(ModItems.AEGIS_WARDING_EMBLEM.get());
                output.accept(ModItems.AEGIS_SHIELDING_CHARM.get());
                output.accept(ModItems.SILENT_STRIDERS.get());
                output.accept(ModItems.CRYO_GRENADE.get());
                output.accept(ModItems.VAPOR_SHROUD_CLOAK.get());
                output.accept(ModItems.GROUNDING_COIL.get());
                output.accept(ModItems.BARROW_SLATE_GREAVES.get());
                output.accept(ModItems.RIME_CAP_SPORES.get());
                output.accept(ModItems.PHASE_RECALL_STONE.get());
                output.accept(ModItems.LEVIATHAN_RIFT_BELT.get());
                output.accept(ModItems.NULL_WEAVE_MANTLE.get());
                output.accept(ModItems.QILIN_PRISM_CIRCLET.get());
                output.accept(ModItems.SUN_GLAZED_TALISMAN.get());
                output.accept(ModItems.RADIANT_SNOWBALL.get());
                output.accept(ModItems.VITAE_FERTILIZER.get());
                output.accept(ModItems.STORM_KITE_GLIDER.get());
                output.accept(ModItems.CRYO_TEMPEST_HORN.get());
                output.accept(ModItems.TEMPEST_BOTTLE.get());
                output.accept(ModItems.SPORE_FILTER_MASK.get());
                output.accept(ModItems.CRYO_SPORE_POULTICE.get());
                output.accept(ModItems.GLAZED_SPORE_BOMB.get());
                output.accept(ModItems.MIRAGE_VEIL_GOGGLES.get());
                output.accept(ModItems.MIRAGE_DECOY_PRISM.get());
                output.accept(ModItems.SALAMANDER_WADER_HOOD.get());
                output.accept(ModItems.COPPER_SILT_SIEVE.get());
                output.accept(ModItems.DUST_DEVIL_AMPOULE.get());
                output.accept(ModItems.DUST_CORE_GOLEM_SPAWN_EGG.get());
                output.accept(ModItems.SANGUINE_ICE_LANCET.get());
                output.accept(ModItems.SANGUINE_DRAUGHT_AMPOULE.get());
                output.accept(ModItems.SANGUINE_SEA_ANGEL_SPAWN_EGG.get());
                output.accept(ModItems.STARLIGHT_TUNING_CHISEL.get());
                output.accept(ModItems.STARLIGHT_DRAUGHT_AMPOULE.get());
                output.accept(ModItems.ASTRAL_NAUTILUS_ARMOR.get());
                output.accept(ModItems.STARLIGHT_NAUTILUS_SPAWN_EGG.get());
                output.accept(ddraig.net.entropica.item.SpellgemItem.createInscribedGem(ddraig.net.entropica.spell.assembly.SpellAssembly.createMagicMissileStarter(1)));
                output.accept(ddraig.net.entropica.item.SpellgemItem.createInscribedGem(ddraig.net.entropica.spell.assembly.SpellAssembly.createBasicBreakStarter(1)));
                output.accept(ddraig.net.entropica.item.SpellgemItem.createInscribedGem(ddraig.net.entropica.spell.assembly.SpellAssembly.createBasicWardStarter(1)));
                output.accept(ddraig.net.entropica.item.SpellgemItem.createInscribedGem(ddraig.net.entropica.spell.assembly.SpellAssembly.createBasicHealStarter(1)));
                output.accept(ddraig.net.entropica.item.SpellgemItem.createInscribedGem(ddraig.net.entropica.spell.assembly.SpellAssembly.createBasicWispStarter(1)));
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
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL1.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL2.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL3.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL4.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL5.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL6.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL7.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL8.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL9.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL10.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL11.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL12.get());
                output.accept(ModItems.CELESTIAL_JOURNAL_VOL13.get());
                output.accept(ModItems.MATERIA_FLUX_DISTRIBUTOR_ITEM.get());
                output.accept(ModItems.MORTAR_AND_PESTLE.get());
                output.accept(ModItems.CRYSTAL_SWORD.get());
                output.accept(ModItems.FROST_BITE_DAGGER.get());
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
                output.accept(ModItems.FROST_BITE_DAGGER.get());
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
                output.accept(ModItems.GLACIAL_FROST_CORE_SPAWN_EGG.get());
                output.accept(ModItems.FROST_TUSK_MAMMOT_SPAWN_EGG.get());
                output.accept(ModItems.AURORA_JELLY_SPAWN_EGG.get());
                output.accept(ModItems.CRYO_STALKER_SPAWN_EGG.get());
                output.accept(ModItems.VAPOR_BELLOWS_CRAB_SPAWN_EGG.get());
                output.accept(ModItems.PATINA_OVIS_SPAWN_EGG.get());
                output.accept(ModItems.CRYO_STATIC_HEDGEHOG_SPAWN_EGG.get());

                // Biome 1: Blue-Glacier Shelf & Runic Spire Natural Blocks & Drops
                output.accept(ModItems.AEGIS_CRYSTAL_BLOCK_ITEM.get());
                output.accept(ModItems.AEGIS_CRYSTAL_BUD_ITEM.get());
                output.accept(ModItems.AEGIS_CRYSTAL_CLUSTER_ITEM.get());
                output.accept(ModItems.AEGIS_CRYSTAL_SHARD.get());
                output.accept(ModItems.RUNIC_BLUE_ICE_ITEM.get());
                output.accept(ModItems.RUNED_BASALT_PILLAR_ITEM.get());
                output.accept(ModItems.GLACIAL_HEART_ROOT_ITEM.get());
                output.accept(ModItems.SHIELDING_BRAMBLE_ITEM.get());
                output.accept(ModItems.SHIELDING_THORN_FIBER.get());
                output.accept(ModItems.RIMEBLOOM_PETAL.get());
                output.accept(ModItems.RIMEBLOOM_DYE.get());
                output.accept(ModItems.FROST_CORE_SHARD.get());
                output.accept(ModItems.INSULATED_GLACIAL_WOOL.get());
                output.accept(ModItems.RAW_MAMMOT_MEAT.get());
                output.accept(ModItems.COOKED_MAMMOT_MEAT.get());
                output.accept(ModItems.GLACIAL_FOCUS_LENS.get());

                // Biome 2: Auroral Skerries & Auroral Wellspring Blocks & Items
                output.accept(ModItems.GLIMMER_ICE_ITEM.get());
                output.accept(ModItems.COARSE_GRAVEL_ITEM.get());
                output.accept(ModItems.AURORAL_FEATHER_MOSS_ITEM.get());
                output.accept(ModItems.MIST_VEIL_FERN_ITEM.get());
                output.accept(ModItems.AURORAL_LANTERN_ITEM.get());
                output.accept(ModItems.THERMAL_HEAT_LAMP_ITEM.get());
                output.accept(ModItems.GLOW_GEL_PATHING_ITEM.get());
                output.accept(ModItems.STATIC_REDSTONE_COUPLER_ITEM.get());
                output.accept(ModItems.AURORAL_LENS_BLOCK_ITEM.get());
                output.accept(ModItems.AURORA_DUST.get());
                output.accept(ModItems.GLOWING_AURORAL_FIBER.get());
                output.accept(ModItems.MIST_VEIL_FROND.get());
                output.accept(ModItems.WARM_AURORA_PETAL.get());
                output.accept(ModItems.CAMOUFLAGE_CLOAK.get());
                output.accept(ModItems.AURORAL_GELATIN.get());
                output.accept(ModItems.CHARGED_GLOW_TENTACLE.get());
                output.accept(ModItems.AURORAL_WAND.get());
                output.accept(ModItems.VAPOR_CONDENSATE_CRYSTALS.get());

                // Biome 3: Frost-Bite Floes Blocks & Items
                output.accept(ModItems.ROTTEN_ICE_ITEM.get());
                output.accept(ModItems.BLIGHTED_PACKED_ICE_ITEM.get());
                output.accept(ModItems.NECROTIC_SLIME_PATCH_ITEM.get());
                output.accept(ModItems.NECROTIC_SLIME_BLOCK_ITEM.get());
                output.accept(ModItems.FROZEN_SPRUCE_BRANCH_ITEM.get());
                output.accept(ModItems.CURED_FROST_HIDE_BLOCK_ITEM.get());
                output.accept(ModItems.ROT_POWDER.get());
                output.accept(ModItems.NECROTIC_SLIME.get());
                output.accept(ModItems.NECROTIC_SLIME_BUCKET.get());
                output.accept(ModItems.SEA_SALT_CRYSTALS.get());
                output.accept(ModItems.CRYO_CRYSTALLINE_FANG.get());
                output.accept(ModItems.FROST_VEINED_HIDE.get());
                output.accept(ModItems.CURED_FROST_VEINED_HIDE.get());
                output.accept(ModItems.FROST_BITE_DAGGER.get());
                output.accept(ModItems.CRYO_INSULATED_HELMET.get());
                output.accept(ModItems.CRYO_INSULATED_CHESTPLATE.get());
                output.accept(ModItems.CRYO_INSULATED_LEGGINGS.get());
                output.accept(ModItems.CRYO_INSULATED_BOOTS.get());

                // Biome 4: Crystalline Pack-Ice & Aegis Shielding Monolith Blocks & Items
                output.accept(ModItems.AEGIS_QUARTZ_BLOCK_ITEM.get());
                output.accept(ModItems.AEGIS_QUARTZ_CLUSTER_ITEM.get());
                output.accept(ModItems.BISMUTH_ORE_ITEM.get());
                output.accept(ModItems.BISMUTH_BLOCK_ITEM.get());
                output.accept(ModItems.AEGIS_PLATED_STONE_ITEM.get());
                output.accept(ModItems.BISMUTH_KEY_PEDESTAL_ITEM.get());
                output.accept(ModItems.AEGIS_QUARTZ.get());
                output.accept(ModItems.RAW_BISMUTH.get());
                output.accept(ModItems.BISMUTH_INGOT.get());
                output.accept(ModItems.AEGIS_WARDING_EMBLEM.get());
                output.accept(ModItems.AEGIS_SHIELDING_CHARM.get());

                // Biome 5: Silent Fjord Blocks & Items
                output.accept(ModItems.BASALT_COLUMN_ITEM.get());
                output.accept(ModItems.CHISELED_ECLIPSE_BASALT_ITEM.get());
                output.accept(ModItems.BLUE_FROZEN_MOSS_ITEM.get());
                output.accept(ModItems.BLUE_FROZEN_MOSS_CARPET_ITEM.get());
                output.accept(ModItems.SHADOWED_ECLIPSE_MOSS_ITEM.get());
                output.accept(ModItems.FROZEN_MOSS_CLUMP.get());
                output.accept(ModItems.CRYO_GRENADE.get());
                output.accept(ModItems.SILENT_STRIDERS.get());
                output.accept(ModItems.BASALT_PICKAXE.get());

                // Biome 6: Steaming Skerries & Vaporous Grotto Blocks & Items
                output.accept(ModItems.STEAMING_ICE_ITEM.get());
                output.accept(ModItems.STEAMING_SPRING_STONE_ITEM.get());
                output.accept(ModItems.VAPOR_WEAVER_LICHEN_ITEM.get());
                output.accept(ModItems.STEAM_VENT_ITEM.get());
                output.accept(ModItems.VAPOR_LIFT_MAT_ITEM.get());
                output.accept(ModItems.VAPOR_CONDENSER_ITEM.get());
                output.accept(ModItems.VAPOR_CONDENSER_SPORE.get());
                output.accept(ModItems.VAPOR_SHROUD_CLOAK.get());
                output.accept(ModItems.POROUS_BASALT_SHELL.get());

                // Biome 7: Static Ice-Plains Blocks & Items
                output.accept(ModItems.FULGURITE_ICE_ITEM.get());
                output.accept(ModItems.FULGURITE_PINE_LOG_ITEM.get());
                output.accept(ModItems.FULGURITE_PINE_LEAVES_ITEM.get());
                output.accept(ModItems.STATIC_CONDUCTIVE_MAT_ITEM.get());
                output.accept(ModItems.BIOMASS_CAPACITOR_ITEM.get());
                output.accept(ModItems.STATIC_FROST_POWDER.get());
                output.accept(ModItems.CHARGED_STATIC_NEEDLE.get());
                output.accept(ModItems.STATIC_QUILL.get());
                output.accept(ModItems.GROUNDING_COIL.get());

                // Biome 8: Barrow-Mound Tundra Blocks, Items & Fauna Eggs
                output.accept(ModItems.NECROTIC_SOIL_ITEM.get());
                output.accept(ModItems.BARROW_STONE_BRICKS_ITEM.get());
                output.accept(ModItems.CRACKED_BARROW_BRICKS_ITEM.get());
                output.accept(ModItems.MOSSY_BARROW_BRICKS_ITEM.get());
                output.accept(ModItems.GRAVE_MOSS_ITEM.get());
                output.accept(ModItems.CRYPT_GRAVE_ROOT_ITEM.get());
                output.accept(ModItems.RIME_CAP_MUSHROOM_ITEM.get());
                output.accept(ModItems.RUNED_BRAZIER_ITEM.get());
                output.accept(ModItems.CRYPT_SIPHON_LANTERN_ITEM.get());
                output.accept(ModItems.BARROW_SENTINEL_TOTEM_ITEM.get());
                output.accept(ModItems.RIME_CAP_SPORES.get());
                output.accept(ModItems.CRYPT_GRAVE_FIBERS.get());
                output.accept(ModItems.BLEACHED_BONE_MEAL.get());
                output.accept(ModItems.GRAVE_GLOW_FEATHER.get());
                output.accept(ModItems.BARROW_SKULL_PLATE.get());
                output.accept(ModItems.BARROW_SLATE_GREAVES.get());
                output.accept(ModItems.BARROW_HOUND_SPAWN_EGG.get());
                output.accept(ModItems.BARROW_CRYPT_GLOW_OWL_SPAWN_EGG.get());

                // Biome 9: Null-Fringe Bay Blocks, Items & Fauna Eggs
                output.accept(ModItems.VOID_SAND_ITEM.get());
                output.accept(ModItems.VOID_SAND_TURF_ITEM.get());
                output.accept(ModItems.VOID_STONE_ITEM.get());
                output.accept(ModItems.OBSIDIAN_BRICKS_ITEM.get());
                output.accept(ModItems.VOID_CRYSTAL_BLOCK_ITEM.get());
                output.accept(ModItems.VOID_CRYSTAL_BUD_ITEM.get());
                output.accept(ModItems.VOID_CRYSTAL_CLUSTER_ITEM.get());
                output.accept(ModItems.NULL_FRINGE_TUSSOCK_ITEM.get());
                output.accept(ModItems.SPATIAL_PHASE_ANCHOR_ITEM.get());
                output.accept(ModItems.VOID_CRYSTAL_SHARD.get());
                output.accept(ModItems.VOID_FIBER.get());
                output.accept(ModItems.VOID_WEAVE_FABRIC.get());
                output.accept(ModItems.SERPENT_VOID_SCALE.get());
                output.accept(ModItems.PHASE_RECALL_STONE.get());
                output.accept(ModItems.LEVIATHAN_RIFT_BELT.get());
                output.accept(ModItems.NULL_WEAVE_MANTLE.get());
                output.accept(ModItems.VOID_SEA_SERPENT_SPAWN_EGG.get());
                output.accept(ModItems.VOID_RIFT_SHADE_SPAWN_EGG.get());

                // Biome 10: Sun-Glazed Sanctuary Blocks, Items & Fauna Eggs
                output.accept(ModItems.RADIANT_SNOW_BLOCK_ITEM.get());
                output.accept(ModItems.RADIANT_GLAZE_ICE_ITEM.get());
                output.accept(ModItems.GLAZED_AMBER_LEAVES_ITEM.get());
                output.accept(ModItems.GOLDEN_LICHEN_ITEM.get());
                output.accept(ModItems.VITAE_SPRING_STONE_ITEM.get());
                output.accept(ModItems.QILIN_LIGHT_REFRACTOR_ITEM.get());
                output.accept(ModItems.SUN_GLAZED_LANTERN_ITEM.get());
                output.accept(ModItems.RADIANT_SNOWBALL.get());
                output.accept(ModItems.JADE_QILIN_SCALE.get());
                output.accept(ModItems.LIQUID_VITAE_RESIN.get());
                output.accept(ModItems.VITAE_FERTILIZER.get());
                output.accept(ModItems.QILIN_PRISM_CIRCLET.get());
                output.accept(ModItems.SUN_GLAZED_TALISMAN.get());
                output.accept(ModItems.WEEPING_AMBER_TEAR.get());
                output.accept(ModItems.LUMINOTH_DUST.get());
                output.accept(ModItems.AURORAFOWL_FEATHER.get());
                output.accept(ModItems.RAW_AURORAFOWL.get());
                output.accept(ModItems.COOKED_AURORAFOWL.get());
                output.accept(ModItems.CRYSTALLINE_QILIN_SPAWN_EGG.get());
                output.accept(ModItems.AMBER_WEEPING_STAG_SPAWN_EGG.get());
                output.accept(ModItems.AURORAFOWL_SPAWN_EGG.get());
                output.accept(ModItems.LUMINOTH_SPAWN_EGG.get());

                // Biome 11: Storm-Swept Icebergs Blocks, Items & Fauna Eggs
                output.accept(ModItems.FULGURITE_BLOCK_ITEM.get());
                output.accept(ModItems.ZEPHYR_ICE_ITEM.get());
                output.accept(ModItems.ZEPHYR_GLASS_GRASS_ITEM.get());
                output.accept(ModItems.ZEPHYR_UPDRAFT_GRATE_ITEM.get());
                output.accept(ModItems.SIMURGH_WIND_TURBINE_ITEM.get());
                output.accept(ModItems.ZEPHYR_GLASS_FIBER.get());
                output.accept(ModItems.STORM_KITE_MEMBRANE.get());
                output.accept(ModItems.HYDRA_GLACIAL_HORN.get());
                output.accept(ModItems.STORM_KITE_GLIDER.get());
                output.accept(ModItems.CRYO_TEMPEST_HORN.get());
                output.accept(ModItems.TEMPEST_BOTTLE.get());
                output.accept(ModItems.STORM_KITE_SPAWN_EGG.get());
                output.accept(ModItems.GLACIAL_HYDRA_SPAWN_EGG.get());

                // Biome 12: Spore-Glazed Pack Blocks, Items & Fauna Eggs
                output.accept(ModItems.FROZEN_SPORE_MOSS_ITEM.get());
                output.accept(ModItems.FROZEN_SPORE_MOSS_CARPET_ITEM.get());
                output.accept(ModItems.SPORE_GLAZED_ICE_ITEM.get());
                output.accept(ModItems.SPORE_WOOD_LOG_ITEM.get());
                output.accept(ModItems.SPORE_WOOD_ITEM.get());
                output.accept(ModItems.SPORE_WOOD_PLANKS_ITEM.get());
                output.accept(ModItems.SPORE_WOOD_STAIRS_ITEM.get());
                output.accept(ModItems.SPORE_WOOD_SLAB_ITEM.get());
                output.accept(ModItems.SPORE_WOOD_FENCE_ITEM.get());
                output.accept(ModItems.FROZEN_SPORE_LEAVES_ITEM.get());
                output.accept(ModItems.GLAZE_CAP_MUSHROOM_BLOCK_ITEM.get());
                output.accept(ModItems.SPORE_BELLOWS_ITEM.get());
                output.accept(ModItems.CRYO_SPORE_CENSER_ITEM.get());
                output.accept(ModItems.MYCELIAL_SPORE_CAP.get());
                output.accept(ModItems.SPORE_FILTER_MASK.get());
                output.accept(ModItems.CRYO_SPORE_POULTICE.get());
                output.accept(ModItems.GLAZED_SPORE_BOMB.get());
                output.accept(ModItems.MYCELIAL_SPORE_WEAVER_SPAWN_EGG.get());

                // Biome 13: Mirage Glacier Blocks, Items & Fauna Eggs
                output.accept(ModItems.GLASS_SAND_ITEM.get());
                output.accept(ModItems.GLASS_SANDSTONE_ITEM.get());
                output.accept(ModItems.GLASS_SANDSTONE_STAIRS_ITEM.get());
                output.accept(ModItems.GLASS_SANDSTONE_SLAB_ITEM.get());
                output.accept(ModItems.MIRAGE_ICE_ITEM.get());
                output.accept(ModItems.MIRAGE_PRISM_LANTERN_ITEM.get());
                output.accept(ModItems.SILICA_GLASS_PLATE.get());
                output.accept(ModItems.MIRAGE_VEIL_GOGGLES.get());
                output.accept(ModItems.MIRAGE_DECOY_PRISM.get());
                output.accept(ModItems.SHIMMERING_FOCUS.get());
                output.accept(ModItems.SILICA_GLASS_SCARAB_SPAWN_EGG.get());

                // Biome 14: Oasis Floes Blocks, Items & Fauna Eggs
                output.accept(ModItems.FROST_WILLOW_LEAVES_ITEM.get());
                output.accept(ModItems.FROST_WILLOW_VINES_ITEM.get());
                output.accept(ModItems.AMBER_RESIN_BLOCK_ITEM.get());
                output.accept(ModItems.AMBER_TORCH_ITEM.get());
                output.accept(ModItems.OASIS_DEW_BASIN_ITEM.get());
                output.accept(ModItems.AMBER_NECTAR_BOTTLE.get());
                output.accept(ModItems.AMBER_ADHESIVE.get());
                output.accept(ModItems.WATERPROOF_SALAMANDER_SKIN.get());
                output.accept(ModItems.SALAMANDER_WADER_HOOD.get());
                output.accept(ModItems.MUD_LURKER_SALAMANDER_SPAWN_EGG.get());

                // Biome 15: Dust-Crusted Bergs Blocks & Items
                output.accept(ModItems.DUST_CRUSTED_ICE_ITEM.get());
                output.accept(ModItems.GREY_SAND_ITEM.get());
                output.accept(ModItems.WIND_BLOWN_DUST_SCRUB_ITEM.get());
                output.accept(ModItems.TOUGH_FIBER_ROPE_ITEM.get());
                output.accept(ModItems.WIND_BAFFLE_LANTERN_ITEM.get());
                output.accept(ModItems.DRY_FIBERS.get());
                output.accept(ModItems.DUST_CORE_SHARD.get());
                output.accept(ModItems.COPPER_SILT_SIEVE.get());
                output.accept(ModItems.DUST_DEVIL_AMPOULE.get());
                output.accept(ModItems.DUST_CORE_GOLEM_SPAWN_EGG.get());

                // Biome 16: Blood-Ice Shallows Blocks, Items & Fauna Eggs
                output.accept(ModItems.SANGUINE_ICE_ITEM.get());
                output.accept(ModItems.RED_CLAY_SAND_ITEM.get());
                output.accept(ModItems.SANGUINE_CLAY_BRICKS_ITEM.get());
                output.accept(ModItems.SANGUINE_CLAY_BRICK_STAIRS_ITEM.get());
                output.accept(ModItems.SANGUINE_CLAY_BRICK_SLAB_ITEM.get());
                output.accept(ModItems.RED_ALGAE_ITEM.get());
                output.accept(ModItems.CRIMSON_TIDE_LANTERN_ITEM.get());
                output.accept(ModItems.SANGUINE_DRAUGHT_AMPOULE.get());
                output.accept(ModItems.SANGUINE_ICE_LANCET.get());
                output.accept(ModItems.SANGUINE_HEART_GEL.get());
                output.accept(ModItems.SANGUINE_SEA_ANGEL_SPAWN_EGG.get());

                // Biome 17: Astral Skerries Blocks, Items, Star-Fish & Fauna Eggs
                output.accept(ModItems.STARLIGHT_ICE_ITEM.get());
                output.accept(ModItems.STARDUST_CRYSTAL_BLOCK_ITEM.get());
                output.accept(ModItems.STARDUST_CRYSTAL_CLUSTER_ITEM.get());
                output.accept(ModItems.RIME_STAR_TUSSOCK_ITEM.get());
                output.accept(ModItems.CELESTIAL_ORRERY_LANTERN_ITEM.get());
                output.accept(ModItems.STARDUST_FRAGMENT.get());
                output.accept(ModItems.STARLIGHT_DRAUGHT_AMPOULE.get());
                output.accept(ModItems.STARLIGHT_TUNING_CHISEL.get());
                output.accept(ModItems.ASTRAL_NAUTILUS_NACRE.get());
                output.accept(ModItems.ASTRAL_NAUTILUS_ARMOR.get());
                output.accept(ModItems.NEBULA_DARTER.get());
                output.accept(ModItems.COOKED_NEBULA_DARTER.get());
                output.accept(ModItems.STARWING_FLYING_FISH.get());
                output.accept(ModItems.COOKED_STARWING_FLYING_FISH.get());
                output.accept(ModItems.STARFALL_RAY.get());
                output.accept(ModItems.COOKED_STARFALL_RAY.get());
                output.accept(ModItems.COMET_RIBBONFISH.get());
                output.accept(ModItems.COOKED_COMET_RIBBONFISH.get());
                output.accept(ModItems.ZENITH_MOONFISH.get());
                output.accept(ModItems.COOKED_ZENITH_MOONFISH.get());
                output.accept(ModItems.SUPERNOVA_PUFFER.get());
                output.accept(ModItems.ASTRAL_LANTERNFISH.get());
                output.accept(ModItems.STARLIGHT_NAUTILUS_SPAWN_EGG.get());

                // Fauna Bestiary Pass: Drops & Spawn Eggs
                output.accept(ModItems.HARD_SANDSTONE_SHARD.get());
                output.accept(ModItems.SANDSTONE_GOLEM_SPAWN_EGG.get());
                output.accept(ModItems.KELPIE_WATER_PELT.get());
                output.accept(ModItems.PEAT_MUD_KELPIE_SPAWN_EGG.get());
                output.accept(ModItems.SPHINX_SAND_STONE.get());
                output.accept(ModItems.SILT_SPHINX_SPAWN_EGG.get());
                output.accept(ModItems.SIMURGH_FEATHER.get());
                output.accept(ModItems.ZEPHYR_SIMURGH_SPAWN_EGG.get());
                output.accept(ModItems.SHIMMERING_MIRAGE_AURA.get());
                output.accept(ModItems.MIRAGE_FATA_SPRITE_SPAWN_EGG.get());
                output.accept(ModItems.CHERUFE_BASALT_CRUST.get());
                output.accept(ModItems.MAGMA_CHERUFE_SPAWN_EGG.get());
                output.accept(ModItems.PHENEX_EMBER_FEATHER.get());
                output.accept(ModItems.SOULFIRE_PHENEX_SPAWN_EGG.get());
                output.accept(ModItems.VOID_WEB_SPINDLE.get());
                output.accept(ModItems.VOID_ARACHNE_SPAWN_EGG.get());
                output.accept(ModItems.BARROW_RUNE_STONE.get());
                output.accept(ModItems.ASH_BARROW_GNOME_SPAWN_EGG.get());
                output.accept(ModItems.MAGMA_ORTHRUS_PELT.get());
                output.accept(ModItems.ORTHRUS_MAGMA_BEAST_SPAWN_EGG.get());
                output.accept(ModItems.CINDER_WISP_CORE.get());
                output.accept(ModItems.ASH_CINDER_WISP_SPAWN_EGG.get());
                output.accept(ModItems.HYDRA_SOUL_GLAND.get());
                output.accept(ModItems.SOUL_SAND_HYDRA_SPAWN_EGG.get());
                output.accept(ModItems.OBSIDIAN_GARGOYLE_WING.get());
                output.accept(ModItems.VOID_SHADOW_GARGOYLE_SPAWN_EGG.get());
                output.accept(ModItems.RUNIC_CRYPT_STONE.get());
                output.accept(ModItems.BARROW_WIGHT_GOLEM_SPAWN_EGG.get());
                output.accept(ModItems.SCORPIOID_TAIL_SPIKE.get());
                output.accept(ModItems.OBSIDIAN_SCORPIOID_SPAWN_EGG.get());
                output.accept(ModItems.WYVERN_SCALE.get());
                output.accept(ModItems.BRIMSTONE_WYVERN_SPAWN_EGG.get());
                output.accept(ModItems.OBSIDIAN_CORE.get());
                output.accept(ModItems.OBSIDIAN_GOLEM_SPAWN_EGG.get());
                output.accept(ModItems.BRASS_DJINN_COLLAR.get());
                output.accept(ModItems.SOULFIRE_DJINN_SPAWN_EGG.get());
                output.accept(ModItems.CHIMERA_VENOM_GLAND.get());
                output.accept(ModItems.BRIMSTONE_CHIMERA_SPAWN_EGG.get());
                output.accept(ModItems.WENDIGO_SKULL_PLATE.get());
                output.accept(ModItems.OBSIDIAN_WENDIGO_SPAWN_EGG.get());

                // Entropica Potions (Frost Resistance, Freezing, Decay, Iron Skin, Eclipse Shroud, Dampening, Galvanic Surge, Barrow Ward, Null Buoyancy, & Radiant Vitality)
                parameters.holders().lookup(Registries.POTION).ifPresent(potionLookup -> {
                    for (String potionId : new String[]{"frost_resistance", "freezing", "decay", "iron_skin", "eclipse_shroud", "dampening", "long_dampening", "galvanic_surge", "long_galvanic_surge", "barrow_ward", "long_barrow_ward", "null_buoyancy", "long_null_buoyancy", "radiant_vitality", "long_radiant_vitality", "strong_radiant_vitality", "tempest_stride", "long_tempest_stride", "spore_clarity", "long_spore_clarity", "mirage_phasing", "long_mirage_phasing", "oasis_renewal", "long_oasis_renewal", "zephyr_weight", "long_zephyr_weight", "sanguine_vigor", "long_sanguine_vigor", "starlight_grace", "long_starlight_grace"}) {
                        potionLookup.get(net.minecraft.resources.ResourceKey.create(
                                Registries.POTION,
                                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Entropica.MODID, potionId)
                        )).ifPresent(holder -> {
                            output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION, holder));
                            output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.SPLASH_POTION, holder));
                            output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.LINGERING_POTION, holder));
                            output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.TIPPED_ARROW, holder));
                        });
                    }
                });
            }).build());

    public static final RegistrySupplier<CreativeModeTab> SPELLGEMS_TAB = CREATIVE_MODE_TABS.register("spellgems_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 2)
            .title(Component.translatable("itemGroup.entropica.spellgems"))
            .icon(ddraig.net.entropica.spell.CreativeSpellgemCatalog::createTabIconStack)
            .displayItems((parameters, output) -> {
                // Row 1: Quick-grab casting apparatuses & spellcrafting workstations for immediate Creative testing
                output.accept(ModItems.ARCANUM_FOCUS.get());
                output.accept(ModItems.SPELL_WAND.get());
                output.accept(ModItems.SPELL_STAFF.get());
                output.accept(ModItems.SPELL_GRIMOIRE.get());
                output.accept(ModItems.BLANK_SPELLGEM.get());
                output.accept(ModBlocks.ARCANE_SCRIBING_TABLE.get());
                output.accept(ModBlocks.RUNIC_ANVIL.get());
                output.accept(ModBlocks.AUTOCAST_PEDESTAL_STONE.get());
                output.accept(ModBlocks.AUTOCAST_PEDESTAL_ASTRAL_SLATE.get());

                // Single-Materia-locked Spellgems across all Forms, Delivery Vectors, Triggers & Core Effects (fixed 16m length)
                ddraig.net.entropica.spell.CreativeSpellgemCatalog.populateCreativeTab(output);
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

        if (path.equals("zephyr_glass_grass") || path.equals("glass_sand")) {
            return true;
        }

        if (path.contains("core") || path.contains("glass") || path.contains("machine") || path.contains("pipe")) {
            return false;
        }

        return path.equals("materia_blessing") ||
               path.equals("greater_materia_blessing") ||
               path.endsWith("_ore") ||
               path.contains("aeterium") ||
               path.contains("ignisite") ||
               path.contains("mortisite") ||
               path.equals("aegis_crystal_block") ||
               path.equals("aegis_crystal_bud") ||
               path.equals("aegis_crystal_cluster") ||
               path.equals("glacial_heart_root") ||
               path.equals("shielding_bramble") ||
               path.equals("glimmer_ice") ||
               path.equals("coarse_gravel") ||
               path.equals("mist_veil_fern") ||
               path.equals("rotten_ice") ||
               path.equals("blighted_packed_ice") ||
               path.equals("necrotic_slime_patch") ||
               path.equals("frozen_spruce_branch") ||
               path.equals("aegis_quartz_block") ||
               path.equals("aegis_quartz_cluster") ||
               path.equals("basalt_column") ||
               path.equals("steaming_ice") ||
               path.equals("steaming_spring_stone") ||
               path.equals("vapor_weaver_lichen") ||
               path.equals("steam_vent") ||
               path.equals("fulgurite_ice") ||
               path.equals("fulgurite_pine_log") ||
               path.equals("fulgurite_pine_leaves") ||
               path.equals("necrotic_soil") ||
               path.equals("crypt_grave_root") ||
               path.equals("void_sand") ||
               path.equals("void_sand_turf") ||
               path.equals("void_stone") ||
               path.equals("void_crystal_block") ||
               path.equals("void_crystal_bud") ||
               path.equals("void_crystal_cluster") ||
               path.equals("null_fringe_tussock") ||
               path.equals("radiant_snow_block") ||
               path.equals("radiant_glaze_ice") ||
               path.equals("glazed_amber_leaves") ||
               path.equals("golden_lichen") ||
               path.equals("vitae_spring_stone") ||
               path.equals("fulgurite_block") ||
               path.equals("zephyr_ice") ||
               path.equals("spore_glazed_ice") ||
               path.equals("mirage_ice") ||
               path.equals("spore_wood_log") ||
               path.equals("spore_wood") ||
               path.equals("frozen_spore_leaves") ||
               path.equals("frost_willow_leaves") ||
               path.equals("frost_willow_vines") ||
               path.equals("dust_crusted_ice") ||
               path.equals("grey_sand") ||
               path.equals("wind_blown_dust_scrub") ||
               path.equals("sanguine_ice") ||
               path.equals("red_clay_sand") ||
               path.equals("red_algae") ||
               path.equals("starlight_ice") ||
               path.equals("stardust_crystal_block") ||
               path.equals("stardust_crystal_cluster") ||
               path.equals("rime_star_tussock") ||
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
