package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstralMultiblockRecipes {

    public static List<AstralMultiblockRecipe> createRecipes() {
        List<AstralMultiblockRecipe> list = new ArrayList<>();

        // ---------------------------------------------------------------------
        // 1. 7x7x6 GRAND CIRCULAR ASTRAL OBSERVATORY & CELESTIAL MIRROR POOL
        // ---------------------------------------------------------------------
        List<ItemStack> obsMats = List.of(
                new ItemStack(ModBlocks.ASTRAL_MIRROR_BLOCK.get(), 25),
                new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get(), 20),
                new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get(), 16),
                new ItemStack(ModBlocks.STARLIGHT_PILLAR.get(), 8),
                new ItemStack(ModBlocks.ASTRAL_MARBLE_WALL.get(), 8),
                new ItemStack(ModBlocks.REFRACTIVE_ASTRAL_LENS.get(), 4),
                new ItemStack(ModBlocks.CELESTIAL_ARMILLARY_CONTROLLER.get(), 1)
        );

        List<List<String>> obsLayers = List.of(
                // Layer 1 (Foundation & 5x5 Mirror Pool Floor)
                List.of(
                        "MSSSSSM",
                        "SPPPPPS",
                        "SPPPPPS",
                        "SPPPPPS",
                        "SPPPPPS",
                        "SPPPPPS",
                        "MSSSSSM"
                ),
                // Layer 2 (Lower Ring & Armillary Controller Core)
                List.of(
                        "CM...MC",
                        "M.....M",
                        ".......",
                        "...A...",
                        ".......",
                        "M.....M",
                        "CM...MC"
                ),
                // Layer 3 (Pillar Shafts & Marble Walls)
                List.of(
                        "CW...WC",
                        "W.....W",
                        ".......",
                        ".......",
                        ".......",
                        "W.....W",
                        "CW...WC"
                ),
                // Layer 4 (Pillar Capitals)
                List.of(
                        "C.....C",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        "C.....C"
                ),
                // Layer 5 (Arch Dome Ring with 4 Cardinal Open Beam Apertures)
                List.of(
                        ".MM.MM.",
                        "M.....M",
                        "M.....M",
                        ".......",
                        "M.....M",
                        "M.....M",
                        ".MM.MM."
                ),
                // Layer 6 (Sky Aperture & 4 Cardinal Refractive Lenses)
                List.of(
                        "...R...",
                        ".......",
                        ".......",
                        "R.....R",
                        ".......",
                        ".......",
                        "...R..."
                )
        );

        Map<Character, ItemStack> obsLegend = new HashMap<>();
        obsLegend.put('P', new ItemStack(ModBlocks.ASTRAL_MIRROR_BLOCK.get()));
        obsLegend.put('M', new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get()));
        obsLegend.put('S', new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()));
        obsLegend.put('C', new ItemStack(ModBlocks.STARLIGHT_PILLAR.get()));
        obsLegend.put('W', new ItemStack(ModBlocks.ASTRAL_MARBLE_WALL.get()));
        obsLegend.put('A', new ItemStack(ModBlocks.CELESTIAL_ARMILLARY_CONTROLLER.get()));
        obsLegend.put('R', new ItemStack(ModBlocks.REFRACTIVE_ASTRAL_LENS.get()));

        list.add(new AstralMultiblockRecipe(
                Component.literal("Astral Observatory"),
                "7x6x7",
                new ItemStack(ModBlocks.CELESTIAL_ARMILLARY_CONTROLLER.get()),
                obsMats,
                obsLayers,
                obsLegend,
                Component.literal("5x5 Mirror Pool & 32x Cosmic Magnification")
        ));

        // ---------------------------------------------------------------------
        // 2. 3x3x3 CELESTIAL BEACON SANCTUARY
        // ---------------------------------------------------------------------
        List<ItemStack> beaconMats = List.of(
                new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get(), 12),
                new ItemStack(ModBlocks.RUNED_ASTRAL_MARBLE.get(), 8),
                new ItemStack(ModBlocks.CHISELED_ASTRAL_MARBLE.get(), 4),
                new ItemStack(ModBlocks.ASTRAL_CRYSTAL_BLOCK.get(), 2),
                new ItemStack(ModBlocks.CELESTIAL_BEACON_CONTROLLER.get(), 1)
        );

        List<List<String>> beaconLayers = List.of(
                // Layer 1 (Plinth Foundation)
                List.of(
                        "MMM",
                        "MRM",
                        "MMM"
                ),
                // Layer 2 (Beacon Controller Core & Runed Marble)
                List.of(
                        "HRH",
                        "RBR",
                        "HRH"
                ),
                // Layer 3 (Focus Crystals & Chiseled Apex)
                List.of(
                        "...",
                        ".K.",
                        "..."
                )
        );

        Map<Character, ItemStack> beaconLegend = new HashMap<>();
        beaconLegend.put('M', new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get()));
        beaconLegend.put('R', new ItemStack(ModBlocks.RUNED_ASTRAL_MARBLE.get()));
        beaconLegend.put('H', new ItemStack(ModBlocks.CHISELED_ASTRAL_MARBLE.get()));
        beaconLegend.put('K', new ItemStack(ModBlocks.ASTRAL_CRYSTAL_BLOCK.get()));
        beaconLegend.put('B', new ItemStack(ModBlocks.CELESTIAL_BEACON_CONTROLLER.get()));

        list.add(new AstralMultiblockRecipe(
                Component.literal("Celestial Beacon"),
                "3x3x3",
                new ItemStack(ModBlocks.CELESTIAL_BEACON_CONTROLLER.get()),
                beaconMats,
                beaconLayers,
                beaconLegend,
                Component.literal("Projects Constellation Auras & Wards")
        ));

        // ---------------------------------------------------------------------
        // 3. 5x5x3 MODULAR ASTRAL ALTAR (Tier 2 Starlight Crafting Altar)
        // ---------------------------------------------------------------------
        List<ItemStack> altarMats = List.of(
                new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get(), 12),
                new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get(), 4),
                new ItemStack(ModBlocks.RESONANCE_PYLON.get(), 4),
                new ItemStack(ModBlocks.ATTUNEMENT_PEDESTAL.get(), 4),
                new ItemStack(ModBlocks.FOCAL_LENS_MOUNT.get(), 1),
                new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get(), 1)
        );

        List<List<String>> altarLayers = List.of(
                // Layer 1 (Foundation Tiles)
                List.of(
                        "M.S.M",
                        ".MMM.",
                        "SMMMS",
                        ".MMM.",
                        "M.S.M"
                ),
                // Layer 2 (Altar Core, Pedestals & Pylons)
                List.of(
                        "Y.T.Y",
                        ".....",
                        "T.A.T",
                        ".....",
                        "Y.T.Y"
                ),
                // Layer 3 (Overhead Focal Lens Mount)
                List.of(
                        ".....",
                        ".....",
                        "..F..",
                        ".....",
                        "....."
                )
        );

        Map<Character, ItemStack> altarLegend = new HashMap<>();
        altarLegend.put('M', new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get()));
        altarLegend.put('S', new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()));
        altarLegend.put('Y', new ItemStack(ModBlocks.RESONANCE_PYLON.get()));
        altarLegend.put('T', new ItemStack(ModBlocks.ATTUNEMENT_PEDESTAL.get()));
        altarLegend.put('A', new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get()));
        altarLegend.put('F', new ItemStack(ModBlocks.FOCAL_LENS_MOUNT.get()));

        list.add(new AstralMultiblockRecipe(
                Component.literal("Modular Astral Altar"),
                "5x3x5",
                new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get()),
                altarMats,
                altarLayers,
                altarLegend,
                Component.literal("Harmonic Resonance Ritual Crafting Grid")
        ));

        return list;
    }
}
