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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Entropica.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS_TAB = CREATIVE_MODE_TABS.register("blocks_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.blocks"))
            .icon(() -> ModItems.MANA_FURNACE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ModBlocks.BLOCKS.getEntries().forEach(blockHolder -> {
                    Item blockItem = blockHolder.get().asItem();
                    if (blockItem != Items.AIR) {
                        output.accept(blockItem);
                    }
                });
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("items_tab", () -> CreativeModeTab.builder()
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
                            item != ModItems.VIS_VALUE_DETECTOR.get()) {

                        output.accept(item);
                    }
                });
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VIS_ITEMS_TAB = CREATIVE_MODE_TABS.register("vis_items_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.vis_items"))
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

                        // Vis Fume Ampoules (Gaseous Material)
                        ItemStack smallFumeAmp = new ItemStack(ModItems.SMALL_VIS_FUME_AMPOULE.get());
                        VisFumeAmpouleItem.setEssenceType(smallFumeAmp, type);
                        output.accept(smallFumeAmp);

                        ItemStack medFumeAmp = new ItemStack(ModItems.MEDIUM_VIS_FUME_AMPOULE.get());
                        VisFumeAmpouleItem.setEssenceType(medFumeAmp, type);
                        output.accept(medFumeAmp);

                        ItemStack largeFumeAmp = new ItemStack(ModItems.LARGE_VIS_FUME_AMPOULE.get());
                        VisFumeAmpouleItem.setEssenceType(largeFumeAmp, type);
                        output.accept(largeFumeAmp);
                    }
                }
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_TAB = CREATIVE_MODE_TABS.register("tools_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.tools"))
            .icon(() -> ModItems.WHISPERWOOD_WAND.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.BASALT_PICKAXE.get());
                output.accept(ModItems.WHISPERWOOD_WAND.get());
                output.accept(ModItems.SHIMMERING_FOCUS.get());
                output.accept(ModItems.AETHERIC_MONOCLE.get());
                output.accept(ModItems.VIS_VALUE_DETECTOR.get());
                output.accept(ModItems.ARCANUM_FOCUS.get());
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEAPONS_TAB = CREATIVE_MODE_TABS.register("weapons_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.entropica.weapons"))
            .icon(() -> ModItems.SOULBOUND_BLADE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.ESSENCE_HARVESTING_BLADE.get());
                output.accept(ModItems.SOULBOUND_BLADE.get());
                output.accept(ModItems.OBLIVION_BLADE.get());
                output.accept(ModItems.TIDAL_TRIDENT.get());
                output.accept(ModItems.VOID_SWORD.get());
            }).build());
}