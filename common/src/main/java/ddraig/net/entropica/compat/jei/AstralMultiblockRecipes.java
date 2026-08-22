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
        // 1. 9x9x9 GRAND CIRCULAR ASTRAL OBSERVATORY & CELESTIAL MIRROR POOL
        // ---------------------------------------------------------------------
        List<ItemStack> obsMats = List.of(
                new ItemStack(ModBlocks.ASTRAL_MIRROR_BLOCK.get(), 49),
                new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get(), 28),
                new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get(), 68),
                new ItemStack(ModBlocks.STARLIGHT_PILLAR.get(), 20),
                new ItemStack(ModBlocks.ASTRAL_MARBLE_WALL.get(), 24),
                new ItemStack(ModBlocks.REFRACTIVE_ASTRAL_LENS.get(), 4),
                new ItemStack(ModBlocks.CELESTIAL_ARMILLARY_CONTROLLER.get(), 1)
        );

        List<List<String>> obsLayers = List.of(
                // Layer 1 (Foundation & 7x7 Mirror Pool Floor)
                List.of(
                        "MSSSSSSSM",
                        "SPPPPPPPS",
                        "SPPPPPPPS",
                        "SPPPPPPPS",
                        "SPPPPPPPS",
                        "SPPPPPPPS",
                        "SPPPPPPPS",
                        "SPPPPPPPS",
                        "MSSSSSSSM"
                ),
                // Layer 2 (Lower Ring & Armillary Controller Core)
                List.of(
                        "CMM...MMC",
                        "M.......M",
                        "M.......M",
                        ".........",
                        "....A....",
                        ".........",
                        "M.......M",
                        "M.......M",
                        "CMM...MMC"
                ),
                // Layer 3 (Pillar Shafts & Marble Wall Arches)
                List.of(
                        "CW.....WC",
                        "W.......W",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        "W.......W",
                        "CW.....WC"
                ),
                // Layer 4 (Pillar Shafts & Marble Wall Arches)
                List.of(
                        "CW.....WC",
                        "W.......W",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        "W.......W",
                        "CW.....WC"
                ),
                // Layer 5 (Pillar Shafts & Marble Wall Arches)
                List.of(
                        "CW.....WC",
                        "W.......W",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        "W.......W",
                        "CW.....WC"
                ),
                // Layer 6 (Pillar Capitals)
                List.of(
                        "C.......C",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        "C.......C"
                ),
                // Layer 7 (Arch Dome Ring with 4 Cardinal Open Beam Apertures)
                List.of(
                        ".MMM.MMM.",
                        "M.......M",
                        "M.......M",
                        "M.......M",
                        ".........",
                        "M.......M",
                        "M.......M",
                        "M.......M",
                        ".MMM.MMM."
                ),
                // Layer 8 (Upper Dome Arch Stepped Ring)
                List.of(
                        "..MMMMM..",
                        ".M.....M.",
                        "M.......M",
                        "M.......M",
                        "M.......M",
                        "M.......M",
                        "M.......M",
                        ".M.....M.",
                        "..MMMMM.."
                ),
                // Layer 9 (Grand Sky Aperture & 4 Cardinal Refractive Lenses)
                List.of(
                        "....R....",
                        ".........",
                        ".........",
                        ".........",
                        "R.......R",
                        ".........",
                        ".........",
                        ".........",
                        "....R...."
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
                "9x9x9",
                new ItemStack(ModBlocks.CELESTIAL_ARMILLARY_CONTROLLER.get()),
                obsMats,
                obsLayers,
                obsLegend,
                Component.literal("7x7 Mirror Pool, Rotating Ocular Sphere & 32x Cosmic Magnification")
        ));

        // ---------------------------------------------------------------------
        // 2. 5x5x5 CELESTIAL BEACON SANCTUARY
        // ---------------------------------------------------------------------
        List<ItemStack> beaconMats = List.of(
                new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get(), 14),
                new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get(), 10),
                new ItemStack(ModBlocks.RUNED_ASTRAL_MARBLE.get(), 9),
                new ItemStack(ModBlocks.CHISELED_ASTRAL_MARBLE.get(), 4),
                new ItemStack(ModBlocks.STARLIGHT_PILLAR.get(), 4),
                new ItemStack(ModBlocks.ASTRAL_CRYSTAL_BLOCK.get(), 9),
                new ItemStack(ModBlocks.CELESTIAL_BEACON_CONTROLLER.get(), 1)
        );

        List<List<String>> beaconLayers = List.of(
                // Layer 1 (Stepped Plinth Foundation)
                List.of(
                        "MSSSM",
                        "SMMMS",
                        "SMRMS",
                        "SMMMS",
                        "MSSSM"
                ),
                // Layer 2 (Beacon Controller Core & Runed Marble Channels)
                List.of(
                        "HR.RH",
                        "R...R",
                        "..B..",
                        "R...R",
                        "HR.RH"
                ),
                // Layer 3 (Corner Starlight Pillars)
                List.of(
                        "C...C",
                        ".....",
                        ".....",
                        ".....",
                        "C...C"
                ),
                // Layer 4 (Crystal Focus Brackets)
                List.of(
                        ".K.K.",
                        "K...K",
                        ".....",
                        "K...K",
                        ".K.K."
                ),
                // Layer 5 (Apex Crystal Crown)
                List.of(
                        ".....",
                        ".....",
                        "..K..",
                        ".....",
                        "....."
                )
        );

        Map<Character, ItemStack> beaconLegend = new HashMap<>();
        beaconLegend.put('M', new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get()));
        beaconLegend.put('S', new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()));
        beaconLegend.put('R', new ItemStack(ModBlocks.RUNED_ASTRAL_MARBLE.get()));
        beaconLegend.put('H', new ItemStack(ModBlocks.CHISELED_ASTRAL_MARBLE.get()));
        beaconLegend.put('C', new ItemStack(ModBlocks.STARLIGHT_PILLAR.get()));
        beaconLegend.put('K', new ItemStack(ModBlocks.ASTRAL_CRYSTAL_BLOCK.get()));
        beaconLegend.put('B', new ItemStack(ModBlocks.CELESTIAL_BEACON_CONTROLLER.get()));

        list.add(new AstralMultiblockRecipe(
                Component.literal("Celestial Beacon"),
                "5x5x5",
                new ItemStack(ModBlocks.CELESTIAL_BEACON_CONTROLLER.get()),
                beaconMats,
                beaconLayers,
                beaconLegend,
                Component.literal("Projects Wide-Area Constellation Auras & Singularity Wards")
        ));

        // ---------------------------------------------------------------------
        // 3. 7x7x7 MODULAR ASTRAL ALTAR (Tier 2 Starlight Crafting Altar)
        // ---------------------------------------------------------------------
        List<ItemStack> altarMats = List.of(
                new ItemStack(ModBlocks.ASTRAL_MARBLE_BRICKS.get(), 33),
                new ItemStack(ModBlocks.ENGRAVED_ASTRAL_SLATE.get(), 20),
                new ItemStack(ModBlocks.RESONANCE_PYLON.get(), 16),
                new ItemStack(ModBlocks.ATTUNEMENT_PEDESTAL.get(), 8),
                new ItemStack(ModBlocks.FOCAL_LENS_MOUNT.get(), 1),
                new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get(), 1)
        );

        List<List<String>> altarLayers = List.of(
                // Layer 1 (7x7 Stepped Foundation)
                List.of(
                        "MSSSSSM",
                        "SMMMMMS",
                        "SMMMMMS",
                        "SMMMMMS",
                        "SMMMMMS",
                        "SMMMMMS",
                        "MSSSSSM"
                ),
                // Layer 2 (Altar Core, 8 Pedestals & 4 Corner Pylons)
                List.of(
                        "Y..T..Y",
                        ".T...T.",
                        ".......",
                        "T..A..T",
                        ".......",
                        ".T...T.",
                        "Y..T..Y"
                ),
                // Layer 3 (Pylon Tower Shafts)
                List.of(
                        "Y.....Y",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        "Y.....Y"
                ),
                // Layer 4 (Pylon Tower Shafts)
                List.of(
                        "Y.....Y",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        "Y.....Y"
                ),
                // Layer 5 (Pylon Tower Shafts)
                List.of(
                        "Y.....Y",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        ".......",
                        "Y.....Y"
                ),
                // Layer 6 (Inward Arch Brackets)
                List.of(
                        ".M...M.",
                        "M.....M",
                        ".......",
                        ".......",
                        ".......",
                        "M.....M",
                        ".M...M."
                ),
                // Layer 7 (Overhead Focal Lens Mount Apex)
                List.of(
                        ".......",
                        ".......",
                        ".......",
                        "...F...",
                        ".......",
                        ".......",
                        "......."
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
                "7x7x7",
                new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get()),
                altarMats,
                altarLayers,
                altarLegend,
                Component.literal("Harmonic Resonance Ritual Crafting Grid with 8 Workstations")
        ));

        return list;
    }
}
