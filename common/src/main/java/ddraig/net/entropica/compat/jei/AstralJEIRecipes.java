package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.item.AstralCrystalItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class AstralJEIRecipes {

    public static List<AstralAltarRecipe> createAltarRecipes() {
        List<AstralAltarRecipe> list = new ArrayList<>();

        // 1. Resplendent Prism
        List<ItemStack> prismReagents = List.of(
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(ModBlocks.ESSENCE_ENRICHED_GLASS.get()),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(ModBlocks.ESSENCE_ENRICHED_GLASS.get()),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(ModBlocks.ESSENCE_ENRICHED_GLASS.get()),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(ModBlocks.ESSENCE_ENRICHED_GLASS.get())
        );
        list.add(new AstralAltarRecipe(
                Component.literal("Resplendent Prism"),
                new ItemStack(ModItems.ASTRAL_CRYSTAL.get()),
                prismReagents,
                new ItemStack(ModItems.RESPLENDENT_PRISM.get()),
                "Combines an Astral Crystal with 4 Gold and 4 Glass reagents on the surrounding pedestals."
        ));

        // 2. Mantle of the Stars
        List<ItemStack> mantleReagents = List.of(
                new ItemStack(ModItems.STARLIGHT_SILK.get()),
                new ItemStack(ModItems.STARLIGHT_SILK.get()),
                new ItemStack(ModItems.STARLIGHT_SILK.get()),
                new ItemStack(ModItems.STARLIGHT_SILK.get()),
                new ItemStack(ModItems.STARLIGHT_SILK.get()),
                new ItemStack(ModItems.ASTRAL_CRYSTAL_THREAD.get()),
                new ItemStack(ModItems.ASTRAL_CRYSTAL_THREAD.get()),
                new ItemStack(ModBlocks.RUNED_ASTRAL_MARBLE.get())
        );
        list.add(new AstralAltarRecipe(
                Component.literal("Mantle of the Stars"),
                new ItemStack(ModItems.RESPLENDENT_PRISM.get()),
                mantleReagents,
                new ItemStack(ModItems.MANTLE_OF_THE_STARS.get()),
                "Requires a Resplendent Prism in the core, 5 Starlight Silk, 2 Crystal Threads, and 1 Runed Marble."
        ));

        // 3. Tool Restorations
        list.add(new AstralAltarRecipe(
                Component.literal("Restore Crystal Pickaxe"),
                new ItemStack(ModItems.DRAINED_CRYSTAL_PICKAXE.get()),
                List.of(),
                new ItemStack(ModItems.CRYSTAL_PICKAXE.get()),
                "Absorbs open night starlight on the altar to restore full crystalline durability."
        ));
        list.add(new AstralAltarRecipe(
                Component.literal("Restore Crystal Sword"),
                new ItemStack(ModItems.DRAINED_CRYSTAL_SWORD.get()),
                List.of(),
                new ItemStack(ModItems.CRYSTAL_SWORD.get()),
                "Absorbs open night starlight on the altar to restore full crystalline durability."
        ));
        list.add(new AstralAltarRecipe(
                Component.literal("Restore Crystal Axe"),
                new ItemStack(ModItems.DRAINED_CRYSTAL_AXE.get()),
                List.of(),
                new ItemStack(ModItems.CRYSTAL_AXE.get()),
                "Absorbs open night starlight on the altar to restore full crystalline durability."
        ));

        return list;
    }

    public static List<AstralOpticalRecipe> createOpticalRecipes() {
        List<AstralOpticalRecipe> list = new ArrayList<>();

        // 1. Crystal Inscriptions
        addCrystalInscription(list, "gladius_ignis", "Gladius Ignis");
        addCrystalInscription(list, "armatura_adamant", "Armatura");
        addCrystalInscription(list, "sagitta_aeris", "Sagitta");
        addCrystalInscription(list, "lux_radiosa", "Lux");
        addCrystalInscription(list, "serpens_vorago", "Serpens");
        addCrystalInscription(list, "arbor_vitae", "Arbor Vitae");

        // 2. Optical Flora Swapping
        list.add(new AstralOpticalRecipe(
                Component.literal("Soul-Flame Orchid"),
                new ItemStack(Items.POPPY),
                "Gladius Ignis",
                new ItemStack(ModBlocks.SOUL_FLAME_ORCHID.get()),
                new ItemStack(ModBlocks.SOOTY_MARBLE.get())
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Rimebloom"),
                new ItemStack(Items.AZURE_BLUET),
                "Leviathan Profundi",
                new ItemStack(ModBlocks.RIMEBLOOM.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Stardust Bell"),
                new ItemStack(Items.ALLIUM),
                "Penna Aetheris",
                new ItemStack(ModBlocks.STARDUST_BELL.get()),
                new ItemStack(ModBlocks.ASTRAL_MARBLE.get())
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Fulgurite Swamp-Bloom"),
                new ItemStack(Items.DANDELION),
                "Fulgur Tonitrus",
                new ItemStack(ModBlocks.FULGURITE_SWAMP_BLOOM.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Aegis Spire Orchid"),
                new ItemStack(Items.BLUE_ORCHID),
                "Scutum Aegis",
                new ItemStack(ModBlocks.AEGIS_SPIRE_ORCHID.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Void Stalker Orchid"),
                new ItemStack(Items.CORNFLOWER),
                "Serpens",
                new ItemStack(ModBlocks.VOID_STALKER_ORCHID.get()),
                new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get())
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Vitae Orchid"),
                new ItemStack(Items.OXEYE_DAISY),
                "Starlight Beam",
                new ItemStack(ModBlocks.VITAE_ORCHID.get()),
                ItemStack.EMPTY
        ));

        // 3. Material Transmutations
        list.add(new AstralOpticalRecipe(
                Component.literal("Astral Mirror Block"),
                new ItemStack(Items.GLASS),
                "Starlight Beam",
                new ItemStack(ModBlocks.ASTRAL_MIRROR_BLOCK.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Arcanite Ingot"),
                new ItemStack(Items.IRON_INGOT),
                "Starlight Beam",
                new ItemStack(ModItems.ARCANITE_INGOT.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Viscanite Ingot"),
                new ItemStack(Items.GOLD_INGOT),
                "Starlight Beam",
                new ItemStack(ModItems.VISCANITE_INGOT.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Sooty Marble"),
                new ItemStack(Items.OBSIDIAN),
                "Starlight Beam",
                new ItemStack(ModBlocks.SOOTY_MARBLE.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Starlight Birch Sapling"),
                new ItemStack(Items.BIRCH_SAPLING),
                "Starlight Beam",
                new ItemStack(ModBlocks.STARLIGHT_AETHER_BIRCH_SAPLING.get()),
                ItemStack.EMPTY
        ));
        list.add(new AstralOpticalRecipe(
                Component.literal("Starlight Birch Log"),
                new ItemStack(Items.BIRCH_LOG),
                "Starlight Beam",
                new ItemStack(ModBlocks.STARLIGHT_AETHER_BIRCH_LOG.get()),
                ItemStack.EMPTY
        ));

        return list;
    }

    private static void addCrystalInscription(List<AstralOpticalRecipe> list, String constellationPath, String label) {
        ItemStack rawCrystal = new ItemStack(ModItems.ASTRAL_CRYSTAL.get());
        ItemStack inscribed = rawCrystal.copy();
        AstralCrystalItem.setRitual(inscribed, ResourceLocation.fromNamespaceAndPath("entropica", constellationPath));
        list.add(new AstralOpticalRecipe(
                Component.literal("Inscribe: " + label),
                rawCrystal,
                label,
                inscribed,
                ItemStack.EMPTY
        ));
    }
}
