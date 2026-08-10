package ddraig.net.entropica.codex;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class CodexCategoryRegistry {

    public static class CodexNode {
        public final String id;
        public final String title;
        public final String category;
        public final String summary;
        public final String content;
        public final String prerequisiteId;
        public final int requiredTier;
        public final ItemStack icon;
        public final float orbitRadius;
        public final float orbitSpeed;
        public float currentAngle;
        public final boolean isParentHub;

        public CodexNode(String id, String title, String category, String summary, String content,
                         String prerequisiteId, int requiredTier, ItemStack icon,
                         float orbitRadius, float orbitSpeed, float initialAngle, boolean isParentHub) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.summary = summary;
            this.content = content;
            this.prerequisiteId = prerequisiteId;
            this.requiredTier = requiredTier;
            this.icon = icon;
            this.orbitRadius = orbitRadius;
            this.orbitSpeed = orbitSpeed;
            this.currentAngle = initialAngle;
            this.isParentHub = isParentHub;
        }
    }

    public static final List<CodexNode> ALL_NODES = new ArrayList<>();

    static {
        // --- 7 PARENT CATEGORY HUBS arranged in a 360-degree circle (R = 180) ---
        // Hub 1: GETTING STARTED (Top, -90 deg = 4.712 rad)
        float a_start = (float) (-Math.PI / 2.0);
        // Hub 2: ENVIRONMENT & NATURE (Top-Right, ~-25 deg = 5.85 rad)
        float a_env = (float) (-Math.PI * 0.15);
        // Hub 3: MAGIC (Bottom-Right, ~25 deg = 0.44 rad)
        float a_magic = (float) (Math.PI * 0.15);
        // Hub 4: MULTIBLOCKS (Bottom-Right/Center, ~75 deg = 1.31 rad)
        float a_multi = (float) (Math.PI * 0.42);
        // Hub 5: MACHINERY (Bottom-Left/Center, ~125 deg = 2.18 rad)
        float a_mach = (float) (Math.PI * 0.70);
        // Hub 6: MATERIA (Bottom-Left, ~165 deg = 2.88 rad)
        float a_mat = (float) (Math.PI * 0.92);
        // Hub 7: MATERIALS (Left, 180 deg = PI rad)
        float a_ing = (float) (Math.PI * 1.15);

        // ==========================================
        // 1. GETTING STARTED (Green Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_getting_started", "GETTING STARTED", "GETTING STARTED",
                "Introduction to the principles of Entropica, Materia extraction, and Runic Scribing.",
                "Overview & Description:\n" +
                "Welcome to Entropica. The world is alive with latent elemental Essences and pressure-driven Materia currents.\n\n" +
                "Origin & Obtaining:\n" +
                "Craft your first Arkanist Monocle using Glass Panes and a Rune of Ansuz, or assemble a Crucible to extract raw elemental essences.\n\n" +
                "Crafting Uses:\n" +
                "Unlocks early Crucible extractions, alloy metallurgy, and Runic Scribing engines.\n\n" +
                "Special Properties:\n" +
                "Observing the environment with an Arkanist Monocle reveals active Essence Nodes and atmospheric pressure lines.",
                null, 0, new ItemStack(Items.COMPASS),
                180f, 0.0f, a_start, true
        ));

        ALL_NODES.add(new CodexNode(
                "getting_started_primer", "Technomantic Primer", "GETTING STARTED",
                "Basic principles of Entropica.",
                "Overview & Description:\n" +
                "Technomantics merges physical thermodynamics with ancient Elder Futhark runic symbolism.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted using a standard Book and refined Viscanite dust at a crafting table.\n\n" +
                "Crafting Uses:\n" +
                "Serves as the foundational reference manual for all elemental transmutations.\n\n" +
                "Special Properties:\n" +
                "Resonates in the presence of latent elemental Essences, displaying ambient aura densities.",
                "hub_getting_started", 0, new ItemStack(Items.BOOK),
                260f, 0.0f, a_start, false
        ));

        // ==========================================
        // 2. MATERIALS (Orange Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_materials", "MATERIALS", "MATERIALS",
                "Ores, crystals, and fauna materials of Entropica.",
                "Overview & Description:\n" +
                "An overview of the rich mineral wealth, biological pelts, and crystal formations of Entropica.\n\n" +
                "Origin & Obtaining:\n" +
                "Extracted from mountain strata, geothermal crystal geodes, and indigenous fauna.\n\n" +
                "Crafting Uses:\n" +
                "Form the core components for Arcanite alloys, Viscanite conduits, and Grot insulation.",
                null, 0, new ItemStack(Items.IRON_PICKAXE),
                180f, 0.0f, a_ing, true
        ));

        ALL_NODES.add(new CodexNode(
                "materials_ores", "Ores & Metallurgy", "MATERIALS",
                "Arcanite, Resonite, and Viscanite ore metallurgy.",
                "Overview & Description:\n" +
                "Elemental ores embedded within subterranean stone strata, glowing faintly with latent energetic veins.\n\n" +
                "Origin & Obtaining:\n" +
                "Mined deep underground in mountain biomes and deepslate layers using any pickaxe.\n\n" +
                "Crafting & Synthesis:\n" +
                "Smelt raw Arcanite ore in a furnace, or combine Iron with Viscanite dust in a Crucible.\n\n" +
                "Crafting Uses:\n" +
                "Forged into Arcanite Plating, Vapor-Pneumatic Pipes, and heavy machine frames.\n\n" +
                "Special Properties:\n" +
                "High thermal resistance and exceptional Materia pressure flow conductivity.",
                "hub_materials", 0, new ItemStack(Items.RAW_IRON),
                280f, 0.0f, a_ing - 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_crystals", "Crystals & Geodes", "MATERIALS",
                "Aeterium, Ignisite, Mortisite, and Viscanite crystal structures.",
                "Overview & Description:\n" +
                "Vibrant crystalline formations growing in geothermal veins. Viscanite shimmers with electric blue light, while Resonite vibrates with kinetic warmth.\n\n" +
                "Origin & Obtaining:\n" +
                "Found growing on Budding Crystal blocks inside deep geodes, or harvested from mineral clusters.\n\n" +
                "Crafting Uses:\n" +
                "Used to craft liquid conduits, Resonite Stators, Voltaic Conduits, and Shimmering Foci.\n\n" +
                "Special Properties:\n" +
                "Viscanite conducts liquid Materia without thermal decay; Resonite stabilizes high-torque machinery.",
                "hub_materials", 0, new ItemStack(Items.EMERALD),
                290f, 0.0f, a_ing, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_blessing_shard", "Materia Blessing Shard", "MATERIALS",
                "Resonant crystal fragments harvested from Materia Blessings.",
                "Overview & Description:\n" +
                "Radiant gem fragments chipped from larger Materia Blessing formations. They pulse with vibrant color depending on nearby energy sources.\n\n" +
                "Origin & Obtaining:\n" +
                "Obtained by shattering natural floating Materia Blessing crystals (yields 4-12 shards).\n\n" +
                "Crafting Uses:\n" +
                "Essential catalyst component used in Tier 2 Magic Circle rituals to synthesize Materia Blessing crystals.\n\n" +
                "Special Properties:\n" +
                "Dynamically shifts color to match the dominant EssenceType of nearby Essence Nodes, Entropic Cores, or chunk biomes.",
                "materials_crystals", 1, new ItemStack(ModItems.MATERIA_BLESSING_SHARD.get()),
                370f, 0.0f, a_ing + 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_fauna", "Fauna Materials", "MATERIALS",
                "Grot hides, Veil pelts, and Ovis wool.",
                "Overview & Description:\n" +
                "Tough pelts, dense hides, and wool harvested from indigenous fauna.\n\n" +
                "Origin & Obtaining:\n" +
                "Harvested from Grots, Veil Foxes, and Ovis wandering regional biomes.\n\n" +
                "Crafting Uses:\n" +
                "Provides essential thermal and pressure insulation for high-pressure conduits and armors.\n\n" +
                "Special Properties:\n" +
                "Resistant to high-pressure Materia gas leaks.",
                "hub_materials", 0, new ItemStack(Items.LEATHER),
                280f, 0.0f, a_ing + 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_spectral_dyes", "Spectral Dyes", "MATERIALS",
                "Bioluminescent dyes extracted from flora petals and nectar.",
                "Overview & Description:\n" +
                "Refined alchemical liquid dyes extracted from rare Entropica flora petals and nectar. They retain ambient Materia luminescence.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted by distilling flora petals, pollen, and leaves (Soul-Flame, Aegis Rose, Amber Nectar, Shimmerpetals, Stardust Bell, Fulgurite, Gale-Bloom, Cryo-Static) into Glass Bottles at a crafting bench.\n\n" +
                "Crafting Uses:\n" +
                "Used for staining runic slates, chalks, aesthetic glass, spectral leather armor, and catalyst recipes.\n\n" +
                "Special Properties:\n" +
                "Available in 10 vibrant bioluminescent varieties: Soulfire Blue, Aegis Azure, Golden Amber, Radiant Silver, Glacial Teal, Auroral Violet, Stardust Indigo, Fulgurite Cyan, Zephyr Sand, and Cryo-Static Ice.",

                "hub_materials", 0, new ItemStack(ModItems.SPECTRAL_DYE_SOULFIRE.get()),
                300f, 0.0f, a_ing + 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_glassworking", "Aesthetic Glassworking & Tinting", "MATERIALS",
                "32x32 pre-existing texture tinting, 10 shape variants, dynamic RGB color shifting, and cauldron cleansing.",
                "Overview & Description:\n" +
                "Intricately crafted architectural glass featuring crystal-clear centers with decorative frame bevels, grid lines, and glare highlights.\n\n" +
                "Origin & Obtaining:\n" +
                "Fabricated at a crafting table using Clear Base Glass and elemental dyes, or tinted using diluted Materia pools.\n\n" +
                "Crafting Uses:\n" +
                "Available in 10 shape variants including Full Blocks, Slabs, Vertical Slabs, Quarter Slabs, Stairs, Walls, Panes, Trapdoors, and Doors.\n\n" +
                "Special Properties:\n" +
                "100% transparent centers; can be washed back to Clear Base Glass by right-clicking onto Water Cauldrons.",
                "hub_materials", 0, new ItemStack(Items.GLASS),
                300f, 0.0f, a_ing - 0.35f, false
        ));

        // ==========================================
        // 3. MATERIA (Cyan Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_materia", "MATERIA", "MATERIA",
                "Essence Tiers T1-T6, logistics, and pressure conduits.",
                "Overview & Description:\n" +
                "Materia is the fundamental energetic fluid of Entropica, existing in liquid (Liquida) and gas (Fumus) states.\n\n" +
                "Origin & Obtaining:\n" +
                "Extracted via Crucibles, Materia Pumps, or distilled in Alchemical Stills.\n\n" +
                "Crafting Uses:\n" +
                "Powers technomantic machinery, magic circle rituals, and spellcrafting foci.",
                null, 0, new ItemStack(Items.BEACON),
                180f, 0.0f, a_mat, true
        ));

        ALL_NODES.add(new CodexNode(
                "materia_essences", "Essence Tiers & Ampoules", "MATERIA",
                "Categorization of T1-T6 Essences.",
                "Overview & Description:\n" +
                "Essences are categorized into 6 primary elemental tiers:\n" +
                "- T1: Vitae & Blood\n" +
                "- T2: Ignis, Aqua, Aer, Terra\n" +
                "- T3: Umbral & Void\n\n" +
                "Origin & Obtaining:\n" +
                "Distilled from flora, ores, and mob drops into glass Ampoules.\n\n" +
                "Crafting Uses:\n" +
                "Fed into Granite Ritual Bowls, Catalyst Receptacles, and spell wands.",
                "hub_materia", 1, new ItemStack(Items.BREWING_STAND),
                280f, 0.0f, a_mat - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materia_logistics", "Materia Logistics & Pressure", "MATERIA",
                "Pneumatic pipe dynamics and diverters.",
                "Overview & Description:\n" +
                "Conduits and valves for routing Materia Liquida and Fumus throughout factory networks.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted using Arcanite, Viscanite, Copper, and Iron ingots.\n\n" +
                "Crafting Uses:\n" +
                "Connects Materia Pumps, Furnaces, and Synthesizers into automated networks.\n\n" +
                "Special Properties:\n" +
                "Features pressure relief valves, one-way diverters, and pneumatic gates.",
                "hub_materia", 1, new ItemStack(Items.COPPER_INGOT),
                280f, 0.0f, a_mat + 0.15f, false
        ));

        // ==========================================
        // 4. MACHINERY (Red Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_machinery", "MACHINERY", "MACHINERY",
                "Lathe, Crucible, Scribing Controllers, and Vehicles.",
                "Overview & Description:\n" +
                "Industrial machinery designed to shape alloys, extract essences, and power technomantic devices.\n\n" +
                "Origin & Obtaining:\n" +
                "Assembled using Arcanite frames, gears, pistons, and stators.\n\n" +
                "Crafting Uses:\n" +
                "Produces precision components for advanced magic circles and multiblocks.",
                null, 0, new ItemStack(Items.ANVIL),
                180f, 0.0f, a_mach, true
        ));

        ALL_NODES.add(new CodexNode(
                "workstations_lathe", "Eidolic Lathe & Crucible", "MACHINERY",
                "Precision machinery for turning raw metals.",
                "Overview & Description:\n" +
                "The Eidolic Lathe precision-shapes Arcanite ingots into gears, stators, and fine filaments.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted using Anvils, Viscanite Gears, and Arcanite Plating.\n\n" +
                "Crafting Uses:\n" +
                "Turns raw metal ingots into precision technomantic components.",
                "hub_machinery", 1, new ItemStack(Items.ANVIL),
                280f, 0.0f, a_mach, false
        ));

        // ==========================================
        // 5. MULTIBLOCKS (Yellow Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_multiblocks", "MULTIBLOCKS", "MULTIBLOCKS",
                "Runic Scribing Engine 3x3x3, Focal Pedestal, and Fission Reactor.",
                "Overview & Description:\n" +
                "Large modular structures built in-world to handle high-tier transmutations.\n\n" +
                "Origin & Obtaining:\n" +
                "Assembled block-by-block using core blocks, plating, and structural bricks.",
                null, 0, new ItemStack(Items.CRAFTING_TABLE),
                180f, 0.0f, a_multi, true
        ));

        ALL_NODES.add(new CodexNode(
                "multiblock_scribing_engine", "Runic Scribing Engine (3x3x3)", "MULTIBLOCKS",
                "3x3x3 multiblock for stamping arcane runes.",
                "Overview & Description:\n" +
                "A heavy 3x3x3 multiblock anvil machine featuring a central stamping piston and runic conduits.\n\n" +
                "Origin & Obtaining:\n" +
                "Constructed in-world using a Viscanite Synthesizer base, Arcanite Plating, and Arcane Bricks.\n\n" +
                "Crafting Uses:\n" +
                "Stamps raw slate into Elder Futhark active runes (Fehu, Uruz, Gebo, Eihwaz, etc.).\n\n" +
                "Special Properties:\n" +
                "Requires pressure-driven Materia Fumus to actuate the stamping piston.",
                "hub_multiblocks", 2, new ItemStack(Items.CRAFTING_TABLE),
                280f, 0.0f, a_multi - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "multiblock_fission_reactor", "Materia Fission Reactor", "MULTIBLOCKS",
                "High-tier reactor splitting compound Materia.",
                "Overview & Description:\n" +
                "An advanced containment reactor designed to split complex compound Essences back into base elements.\n\n" +
                "Origin & Obtaining:\n" +
                "Constructed using Orbis Cells, Arcanite Plating, and Heavy Valves.\n\n" +
                "Special Properties:\n" +
                "Generates high kinetic output while processing compound Materia.",
                "hub_multiblocks", 3, new ItemStack(Items.NETHER_STAR),
                280f, 0.0f, a_multi + 0.15f, false
        ));

        // ==========================================
        // 6. ENVIRONMENT & NATURE (Lime Green Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_environment", "ENVIRONMENT & NATURE", "ENVIRONMENT & NATURE",
                "Biomes, Flora, and Fauna of Entropica.",
                "Overview & Description:\n" +
                "Explore the natural ecosystems of Entropica, including floating crystal blessings, unique flora, and mob mutations.",
                null, 0, new ItemStack(Items.OAK_SAPLING),
                180f, 0.0f, a_env, true
        ));

        ALL_NODES.add(new CodexNode(
                "env_biomes", "Biomes & Environments", "ENVIRONMENT & NATURE",
                "Tundra, Spore Forests, and Void Rifts.",
                "Overview & Description:\n" +
                "Regional biomes saturated with unique ambient elemental energies.\n\n" +
                "Origin & Obtaining:\n" +
                "Explored throughout the world (Tundra, Spore Forests, Deserts, Void Rifts).",
                "hub_environment", 0, new ItemStack(Items.GRASS_BLOCK),
                280f, 0.0f, a_env - 0.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_materia_blessing", "Materia Blessing Crystal", "ENVIRONMENT & NATURE",
                "Natural 2x2x3 floating crystal attunement phenomenon.",
                "Overview & Description:\n" +
                "A monolithic floating faceted crystal (2x2x3 structure) that hovers softly and bobs in mid-air, radiating soothing ambient light.\n\n" +
                "Origin & Obtaining:\n" +
                "Found naturally floating in ancient biomes, or synthesized via a Tier 2 Magic Circle.\n\n" +
                "Crafting & Synthesis:\n" +
                "▪ Standard (Eihwaz): 4x Materia Blessing Shards, Rune of Unity (Eihwaz), 200 Materia.\n" +
                "▪ Harmonic Discount (Gebo): 4x Materia Blessing Shards, Rune of Gift (Gebo), 180 Materia (10% off).\n\n" +
                "Crafting Uses:\n" +
                "Shatters into 3-5 Materia Blessing Shards when broken.\n\n" +
                "Special Properties:\n" +
                "▪ Attunes directly to the nearest Essence Node or Entropic Core within 64 blocks, reflecting its colors.\n" +
                "▪ Grants Night Vision & passive healing (+1 HP / 40t) to players within 16 blocks.\n" +
                "▪ Automatically feeds nearby Granite Ritual Bowls with Weak Essence every 100 ticks (8-block range).",
                "env_biomes", 1, new ItemStack(ModBlocks.MATERIA_BLESSING.get()),
                360f, 0.0f, a_env - 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_greater_materia_blessing", "Greater Materia Blessing", "ENVIRONMENT & NATURE",
                "Grand 3x3x4 branching crystal cluster formation.",
                "Overview & Description:\n" +
                "A grand 3x3x4 crystal cluster featuring a towering central gem surrounded by branching satellite crystal spires.\n\n" +
                "Origin & Obtaining:\n" +
                "Rare natural formation in high-energy biomes, or synthesized via a Tier 2 Magic Circle.\n\n" +
                "Crafting & Synthesis:\n" +
                "▪ Standard (Eihwaz): 7x Materia Blessing Shards, Rune of Unity (Eihwaz), 400 Materia.\n" +
                "▪ Harmonic Discount (Gebo): 7x Materia Blessing Shards, Rune of Gift (Gebo), 360 Materia (10% off).\n\n" +
                "Crafting Uses:\n" +
                "Shatters into 6-8 Materia Blessing Shards when broken.\n\n" +
                "Special Properties:\n" +
                "▪ Grants Regeneration I & passive healing (+2 HP / 40t) within 24 blocks.\n" +
                "▪ Rapidly feeds nearby Granite Ritual Bowls with Weak Essence every 60 ticks (12-block range).\n" +
                "▪ Emits Light Level 12.",
                "env_materia_blessing", 2, new ItemStack(ModBlocks.GREATER_MATERIA_BLESSING.get()),
                440f, 0.0f, a_env - 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_flora", "Flora & Vegetation", "ENVIRONMENT & NATURE",
                "Rubber Trees and Viscanite Blossoms.",
                "Overview & Description:\n" +
                "Elemental vegetation including Rubber Trees and glowing Viscanite Blossoms.\n\n" +
                "Origin & Obtaining:\n" +
                "Harvested in forests and meadow biomes.\n\n" +
                "Crafting Uses:\n" +
                "Rubber latex is processed into conduit insulation; Viscanite Blossoms yield raw essences.",
                "hub_environment", 0, new ItemStack(Items.OAK_LEAVES),
                290f, 0.0f, a_env, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_rubber_tree", "Rubber Tree & Latex Taps", "ENVIRONMENT & NATURE",
                "Hevea Entropica trees and raw latex extractions.",
                "Overview & Description:\n" +
                "A dense hardwood tree featuring dark bark laced with vertical amber resin veins. Tapping the bark extracts milky Raw Rubber.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in forest and jungle biomes, or grown from Rubber Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Raw Rubber is processed into thermal and pressure insulation for high-tier Materia conduits, pneumatic pipes, and gaskets.\n\n" +
                "Special Properties:\n" +
                "Rubber leaves feature a small chance to drop Rubber Saplings and Raw Rubber blobs when harvested.",
                "env_flora", 0, new ItemStack(ModBlocks.RUBBER_LOG.get()),
                370f, 0.0f, a_env - 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_shimmerpetal", "Shimmerpetal", "ENVIRONMENT & NATURE",
                "Bioluminescent crystalline flowers emitting ambient Materia light.",
                "Overview & Description:\n" +
                "A radiant, 2-part crystalline lotus flower featuring a green biome-tintable stem and a grayscale energetic blossom head.\n\n" +
                "Origin & Obtaining:\n" +
                "Grows naturally near geothermal crystal geodes and lush forest glades, or harvested with shears.\n\n" +
                "Crafting Uses:\n" +
                "Extracted in Crucibles to produce Weak Viscanite Essence, or used in dye transmutations.\n\n" +
                "Special Properties:\n" +
                "Emits constant Light Level 10 and illuminates surrounding blocks without consuming fuel.",
                "env_flora", 0, new ItemStack(ModBlocks.SHIMMERPETAL.get()),
                370f, 0.0f, a_env + 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_silver_pine", "Silver Pine Wood Set", "ENVIRONMENT & NATURE",
                "Cold alpine trees with pale silver bark and frosted pine needles.",
                "Overview & Description:\n" +
                "An evergreen alpine tree featuring pale silver-blue bark and frosted teal-emerald needles.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Silver-Pine Taiga and cold mountain slopes, or grown from Silver Pine Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Crafted into pale ash floorboard planks, timber frames, and cold-weather architectural decorations.\n\n" +
                "Special Properties:\n" +
                "Stripping its bark reveals smooth silver wood grain with subtle cyan heartwood rings.",
                "env_flora", 0, new ItemStack(ModBlocks.SILVER_PINE_LOG.get()),
                380f, 0.0f, a_env - 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_rimebloom", "Frostbite Blossom (Rimebloom)", "ENVIRONMENT & NATURE",
                "Crystalline 3-petaled ice tulip flowers pulsing ambient cold luminescence.",
                "Overview & Description:\n" +
                "A rare glacial wildflower forming a 3-petaled ice tulip cup with a glowing central crystal core.\n\n" +
                "Origin & Obtaining:\n" +
                "Found growing on Blue-Glacier Shelves and Crystalline Pack-Ice, or harvested with shears.\n\n" +
                "Crafting Uses:\n" +
                "Yields Rimebloom Petals and Glacial Essence when extracted in Crucibles.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 3 and ambient frost mist particles, chilling surrounding air pools.",
                "env_flora", 0, new ItemStack(ModBlocks.RIMEBLOOM.get()),
                380f, 0.0f, a_env + 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_soul_flame_orchid", "Soul-Flame Orchid", "ENVIRONMENT & NATURE",
                "Bone-white stem with flickering soulfire-blue petals emitting floating soul particles.",
                "Overview & Description:\n" +
                "A rare bioluminescent orchid with a pale bone-white stem and flickering soulfire-blue petals radiating ethereal heat.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Soulfire Basalt-Fjords and Grave-Stone Spires biomes.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Soul-Flame Petals and Soulfire Nectar to craft Soulfire Torches, spectral dyes, and fire/soul resistance potions.\n\n" +
                "Special Properties:\n" +
                "Emits ambient Light Level 9 and continuously spawns floating soulfire flame and soul particles.",
                "env_flora", 0, new ItemStack(ModBlocks.SOUL_FLAME_ORCHID.get()),
                380f, 0.0f, a_env + 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_auroral_buttercup", "Auroral Buttercup", "ENVIRONMENT & NATURE",
                "Color-shifting pastel violet/pink cup petals with a glowing radiant gold stamen.",
                "Overview & Description:\n" +
                "A rare tundra wildflower with color-shifting pastel violet and pink cup petals radiating gentle auroral luminescence.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Auroral Tundra, Shimmering Taiga, and Glacial Peaks biomes.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Auroral Petals and Auroral Pollen to craft Auroral Spectral Dye, levitation potions, and technomantic optics.\n\n" +
                "Special Properties:\n" +
                "Emits ambient Light Level 7 and spawns floating color-shifting pastel spectrum sparkles.",
                "env_flora", 0, new ItemStack(ModBlocks.AURORAL_BUTTERCUP.get()),
                380f, 0.0f, a_env + 0.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_stardust_bell", "Stardust Bell", "ENVIRONMENT & NATURE",
                "2-tall celestial luminescent wildflower with drooping star-shaped indigo petals emitting sparkling stardust.",
                "Overview & Description:\n" +
                "A rare 2-tall celestial wildflower with arched branches bearing drooping star-shaped indigo and cobalt bell petals surrounding glowing stardust gold cores.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Astral Ridges, Starfall Valleys, and Luminescent Highlands biomes.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Stardust Bell Petals and Stardust Nectar to craft Shimmering Spectral Dye, levitation potions, and high-tier Materia capacitors.\n\n" +
                "Special Properties:\n" +
                "Emits ambient Light Level 10 and continuously radiates floating celestial blue, gold, and white stardust sparkles from its upper bloom.",
                "env_flora", 0, new ItemStack(ModBlocks.STARDUST_BELL.get()),
                380f, 0.0f, a_env + 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_fulgurite_swamp_bloom", "Fulgurite Swamp-Bloom", "ENVIRONMENT & NATURE",
                "Electric underwater swamp flower planted strictly on mud, emitting cyan and gold sparks.",
                "Overview & Description:\n" +
                "A rare electric swamp bloom with starburst electric cyan petals surrounding a galvanic fulgurite stamen core.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns submerged underwater on Mud, Clay, or Mangrove Mud in Charge-Mire Swamps and Fulgurite Fens.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Fulgurite Petals and Fulgurite Stigmas to craft Glacial/Cyan Spectral Dyes, lightning resistance potions, and Materia galvanic conductors.\n\n" +
                "Special Properties:\n" +
                "Emits ambient Light Level 8 underwater and continuously radiates crackling electric cyan and gold sparks.",
                "env_flora", 0, new ItemStack(ModBlocks.FULGURITE_SWAMP_BLOOM.get()),
                380f, 0.0f, a_env + 0.30f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_gale_bloom_dandelion", "Gale-Bloom Dandelion", "ENVIRONMENT & NATURE",
                "Wind-infused fluffy dandelion emitting floating white spore puffballs and wind bursts on interaction.",
                "Overview & Description:\n" +
                "A wind-infused fluffy dandelion with serrated basal leaves and a radial white spore puffball head that sways with zephyr currents.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally on dirt, grass, clay, and mud in Gale-Windswept Crags and Zephyr Plains.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Gale Spore Puffs and Gale Popped Spores to craft levitation potions, zephyr catalysts, and wind-infused optics.\n\n" +

                "Special Properties:\n" +
                "Emits ambient Light Level 4 and continually releases floating white wind spores. Right-clicking triggers a wind burst sound and spore dispersal.",
                "env_flora", 0, new ItemStack(ModBlocks.GALE_BLOOM_DANDELION.get()),
                380f, 0.0f, a_env + 0.35f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_cryo_static_shrub", "Cryo-Static Shrub", "ENVIRONMENT & NATURE",
                "Glacial frost shrub with a dark wood trunk base and dense electric cyan static leaf canopy.",
                "Overview & Description:\n" +
                "An alpine frost shrub structured with several distinct woody stems reaching up into a dense full-width cyan static leaf canopy.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally on dirt, grass, clay, mud, snow, and packed ice in Glacial Peaks and Frozen Fjords.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Cryo-Static Twigs, Cryo-Static Leaves, and living Cryo-Static Rootlings to craft Glacial Spectral Dye, frost resistance potions, and cryo catalysts.\n\n" +

                "Special Properties:\n" +
                "Emits Light Level 5 and radiates dual Glacial Cyan & Plasma Violet static sparkles. Entities stepping inside receive freezing ticks and static Slowness I stun. Right-clicking triggers an electric static discharge.",

                "env_flora", 0, new ItemStack(ModBlocks.CRYO_STATIC_SHRUB.get()),
                380f, 0.0f, a_env + 0.40f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_vitreous_cactus", "Vitreous Cactus", "ENVIRONMENT & NATURE",
                "A crystal-glass desert cactus with large cyan glass needles and puncture bleeding mechanics.",
                "Overview & Description:\n" +
                "A specialized desert succulent featuring a deep green ribbed body armed with prominent light cyan glass needles.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally on sand, red sand, terracotta, and desert gravel in Glass-Dune Wastes.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Vitreous Needles and Vitreous Cactus Flesh to craft Vitreous Spectral Dye, glass optic lenses, and Bleeding potion brews.\n\n" +
                "Special Properties:\n" +
                "Slimmer 12x16x12 cuboid model. Emits Light Level 6 and electric light cyan glass sparkles. Grows up to 5 blocks tall via random ticking without adjacent solid blocks. Entities colliding with it suffer puncture damage and 4 seconds of custom Bleeding.",


                "env_flora", 0, new ItemStack(ModBlocks.VITREOUS_CACTUS.get()),
                420f, 0.0f, a_env + 0.44f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_barrow_moss", "Barrow Moss", "ENVIRONMENT & NATURE",
                "Ancient glowing tomb moss carpet with Bone Meal spreading and Necrotic Immunity debuff cleansing.",
                "Overview & Description:\n" +
                "An ancient glowing rune moss block and carpet layer that thrives in dark barrows, catacombs, and ancient stone vaults.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally on stone, deepslate, cobblestone, and barrow rune-stones in Barrow Mounds.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Barrow Moss Fiber to craft Barrow Spectral Dye, Barrow Moss Carpets, and Necrotic Cleansing brews.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 7 and tomb emerald spores. Grants undead mobs Strength I & Speed I for 6s. Cleanses Wither, Poison, Weakness on living entities (1 damage trade-off) + 15s regen cooldown.",


                "env_flora", 0, new ItemStack(ModBlocks.BARROW_MOSS.get()),
                460f, 0.0f, a_env + 0.48f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_abyssal_weeproot", "Abyssal Weeproot", "ENVIRONMENT & NATURE",
                "Deep void hanging vine & climbable ladder with downward random ticking growth, Slow Falling void tether, and right-click harvesting.",
                "Overview & Description:\n" +
                "A deep void hanging root vine that grows downward from ceiling stone in subterranean chasms and void trenches.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally hanging from ceiling stone in Abyssal Trench biomes and deep void caverns.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Abyssal Tendrils to craft Abyssal Spectral Dye, Void Tether ropes, and Slow Falling brews.\n\n" +
                "Special Properties:\n" +
                "Functions as a vertical climbable ladder (#minecraft:climbable). Emits Light Level 5 and dark cyan droplet particles. Grants Slow Falling void tether to entities climbing or falling through it and grows downward up to 26 blocks.",
                "env_flora", 0, new ItemStack(ModBlocks.ABYSSAL_WEEPROOT.get()),
                500f, 0.0f, a_env + 0.52f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_blood_root_succulent", "Blood-Root Succulent", "ENVIRONMENT & NATURE",
                "Crimson desert succulent with red sand/terracotta placement, life-drain damage, undead regeneration, and pulp harvesting.",
                "Overview & Description:\n" +
                "A deep crimson desert succulent that thrives on arid red sand, terracotta, and crimson nylium in Sanguine Canyons.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Sanguine Canyons, Red Deserts, and Nether Crimson Forests.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Blood-Root Pulp to craft Sanguine Spectral Dye, Vampiric Elixirs, and Life-Drain catalysts.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 6 and bioluminescent crimson life sparkles. Inflicts 1 life-drain damage on living non-undead entities while nourishing undead mobs with Regeneration.",
                "env_flora", 0, new ItemStack(ModBlocks.BLOOD_ROOT_SUCCULENT.get()),
                540f, 0.0f, a_env + 0.56f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_vitae_orchid", "Vitae Orchid", "ENVIRONMENT & NATURE",
                "Tall, 2-block gold-veined white orchid providing Health Regeneration aura.",
                "Overview & Description:\n" +
                "A majestic 2-tall flower featuring a silky white blossom head with royal purple lips and radiant gold veining.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Vitality Meadows and Sunlit Glades.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Vitae Petals, Vitae Nectar, and Golden Vitae Amber Spectral Dye.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 8 and golden vitality sparkles. Grants Regeneration I to living non-undead entities.",
                "env_flora", 0, new ItemStack(ModBlocks.VITAE_ORCHID.get()),
                580f, 0.0f, a_env + 0.60f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_spore_burst_puffball", "Spore-Burst Puffball", "ENVIRONMENT & NATURE",
                "Bulbous warty yellow-green fungus releasing Nausea I & Poison I spore clouds.",
                "Overview & Description:\n" +
                "A round cream puffball mushroom sitting on soil, covered in bioluminescent green cracks and a top spore pore.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Swamps, Mycelium Fields, and Peat Bogs.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Spore Puffs to craft toxic brewing reagents and spore catalysts.\n\n" +
                "Special Properties:\n" +
                "Explodes on step, right-click, or break, releasing a 6-second Nausea I & Poison I spore cloud.",
                "env_flora", 0, new ItemStack(ModBlocks.SPORE_BURST_PUFFBALL.get()),
                620f, 0.0f, a_env + 0.64f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_fulgurite_reed", "Fulgurite Reed", "ENVIRONMENT & NATURE",
                "Glassy copper-banded 16x16 straight reed crackling with atmospheric electricity and Speed I aura.",
                "Overview & Description:\n" +
                "A straight vertical glassy reed banded in metallic copper rings, conducting electric currents along riverbanks.\n\n" +
                "Origin & Obtaining:\n" +
                "Grows naturally on riverbanks, glass sand, and mud in Fulgurite Swamps.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Fulgurite Stalks to craft electric catalysts and Speed potion brews.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 6 and electric cyan lightning sparks. Contact grants Speed I. Bonemealing at max height (3 blocks) overcharges the reed, shocking all entities within 4 blocks with 3s Paralyzed.",
                "env_flora", 0, new ItemStack(ModBlocks.FULGURITE_REED.get()),
                660f, 0.0f, a_env + 0.68f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_gale_thistle", "Gale-Thistle Bush", "ENVIRONMENT & NATURE",
                "Dense metallic silver spiky bush inflicting prick damage and emitting swirling wind particles.",
                "Overview & Description:\n" +
                "A spiky metallic silver bush with central golden seed heads that sways violently in high winds.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally on rocky cliffs and windswept plateaus.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Gale Seeds to craft wind catalysts and seed projectiles.\n\n" +
                "Special Properties:\n" +
                "Emits swirling wind particles and inflicts prick damage on collision.",
                "env_flora", 0, new ItemStack(ModBlocks.GALE_THISTLE.get()),
                700f, 0.0f, a_env + 0.72f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_mist_veil_marshmallow", "Mist-Veil Marshmallow", "ENVIRONMENT & NATURE",
                "Soft bouncy marshmallow pods on water-floating teal leaves cleansing debuffs and restoring hunger.",
                "Overview & Description:\n" +
                "Fluffy white marshmallow pods growing on wide teal leaves that float on water, mud, and clay.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally floating on water, mud, and clay in Mist-Veil Marshes.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Mist-Veil Marshmallow Pods which restore hunger and cleanse status debuffs.\n\n" +
                "Special Properties:\n" +
                "Acts as a soft bouncy pad resetting fall distance.",
                "env_flora", 0, new ItemStack(ModBlocks.MIST_VEIL_MARSHMALLOW.get()),
                740f, 0.0f, a_env + 0.76f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aegis_spire_orchid", "Aegis-Spire Orchid", "ENVIRONMENT & NATURE",
                "2-tall crystalline orchid with azure-blue layered petal spires granting Resistance I aura.",
                "Overview & Description:\n" +
                "A tall 2-block crystalline orchid featuring layered azure petals with metallic silver rims.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Crystal Crags and Metallic Mountain Peaks.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Aegis-Spire Petals used in Resistance II brewing and protective warding.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 7 and grants Resistance I aura to non-hostile entities within 4 blocks.",
                "env_flora", 0, new ItemStack(ModBlocks.AEGIS_SPIRE_ORCHID.get()),
                780f, 0.0f, a_env + 0.80f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_pyre_sprout", "Pyre-Sprout", "ENVIRONMENT & NATURE",
                "Glowing ember sprout with flickering fire petals; yields Pyre Nectar (Fire Resistance I).",
                "Overview & Description:\n" +
                "A fiery sprout growing on charred volcanic soil, basalt, and ash, flickering with orange sparks.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Basalt Deltas, Volcanic Ash, and Nether Rack.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Pyre Sprout Seeds & Ember Pulp; right-clicking with a Glass Bottle yields Pyre Nectar Potion.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 10 and fire sparks.",
                "env_flora", 0, new ItemStack(ModBlocks.PYRE_SPROUT.get()),
                820f, 0.0f, a_env + 0.84f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aura_drift_sedge", "Aura-Drift Sedge", "ENVIRONMENT & NATURE",
                "Luminescent pink-gold grass blades granting Jump Boost I & Speed I on contact.",
                "Overview & Description:\n" +
                "Tall glowing grass blades shimmering with pink and gold aura drift particles.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Radiant High Meadows and Ether Cliffs.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Aura-Drift Fiber used in Materia weaving and speed charms.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 8; entity contact grants Jump Boost I & Speed I.",
                "env_flora", 0, new ItemStack(ModBlocks.AURA_DRIFT_SEDGE.get()),
                860f, 0.0f, a_env + 0.88f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_spectral_lantern_flower", "Spectral Lantern-Flower", "ENVIRONMENT & NATURE",
                "Bell-shaped cyan soulfire lantern flower emitting Light Level 12 and repelling undead entities.",
                "Overview & Description:\n" +
                "A translucent bell-shaped lantern flower glowing with cyan soulfire inside.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Soul Sand Valleys and Whispering Ruins.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Spectral Lantern Pods used in soul protection brews.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 12; inflicts Slowness & Weakness on undead entities within 6 blocks.",
                "env_flora", 0, new ItemStack(ModBlocks.SPECTRAL_LANTERN_FLOWER.get()),
                900f, 0.0f, a_env + 0.92f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_cinder_spore_mushroom", "Cinder-Spore Mushroom", "ENVIRONMENT & NATURE",
                "Log/wall-attached emerald fungal bracket releasing protective ash-spore clouds.",
                "Overview & Description:\n" +
                "A layered emerald fungal bracket mushroom with bioluminescent spore gills underneath.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally attached to log sides in Nether Warped Forests.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Cinder-Spore Caps used in fire-retardant brewing.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 6; entity contact grants Fire Resistance I.",
                "env_flora", 0, new ItemStack(ModBlocks.CINDER_SPORE_MUSHROOM.get()),
                940f, 0.0f, a_env + 0.96f, false
        ));






        ALL_NODES.add(new CodexNode(
                "env_fauna", "Fauna & Mobs", "ENVIRONMENT & NATURE",
                "Grots, Veil Foxes, and Rime Shepherds.",
                "Overview & Description:\n" +
                "Indigenous creatures adapted to elemental atmospheric pressures.\n\n" +
                "Origin & Obtaining:\n" +
                "Found roaming native biomes or spawned using Fauna Spawn Eggs.",
                "hub_environment", 0, new ItemStack(Items.BONE),
                280f, 0.0f, a_env + 0.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aegis_rose", "Aegis Rose", "ENVIRONMENT & NATURE",
                "A crystalline blue sapphire rose with metallic silver thorns.",
                "Overview & Description:\n" +
                "A radiant rose with crystalline blue sapphire petals and sharp metallic silver thorns growing along its stem.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns in Aegis Meadows and Aegis Orchards. Emits a soft ambient luminescence (Light Level 6).\n\n" +
                "Crafting Uses:\n" +
                "Drops Aegis Rose Petals, used in crafting Aegis-Reinforced Plating, Iron Skin Potions, and protective wards.\n\n" +
                "Special Properties:\n" +
                "Protects nearby soil from corruption and radiates defensive Materia currents.",
                "hub_environment", 1, new ItemStack(ModItems.AEGIS_ROSE_PETALS.get()),
                320f, 0.0f, a_env - 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_amber_nectar_blossom", "Amber Nectar Blossom", "ENVIRONMENT & NATURE",
                "A warm golden cup flower seeping glowing amber nectar.",
                "Overview & Description:\n" +
                "An exotic chalice-shaped flower with warm sienna-amber petals that continuously pools and seeps glowing nectar.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns in Amber Groves and Golden Savannas. Emits ambient golden luminescence (Light Level 5).\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Amber Nectar Cups and viscous Amber Nectar Dewdrops, used in crafting Amber Adhesive sticky seals and alchemical nourishment.\n\n" +
                "Special Properties:\n" +
                "Slowly regenerates glowing nectar dew drops over time.",
                "hub_environment", 1, new ItemStack(ModItems.AMBER_NECTAR_CUP.get()),
                320f, 0.0f, a_env + 0.10f, false
        ));

        // ==========================================
        // 7. MAGIC (Purple Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_magic", "MAGIC", "MAGIC",
                "Magic Circles, Magic Circuits, Spellcrafting, and Astral Signs.",
                "Overview & Description:\n" +
                "Master runic symbolism, chalk footprints, magic circle rituals, and active spellcrafting.\n\n" +
                "Origin & Obtaining:\n" +
                "Unlocked through Runic Scribing and Shimmering Focus crafting.",
                null, 0, new ItemStack(Items.ENCHANTED_BOOK),
                180f, 0.0f, a_magic, true
        ));

        ALL_NODES.add(new CodexNode(
                "magic_rituals", "Runic Rituals & Chalk", "MAGIC",
                "Chalk tiers and Elder Futhark runes.",
                "Overview & Description:\n" +
                "Magic Circles drawn on the ground using 4 Chalk Tiers combined with Elder Futhark active runes.\n\n" +
                "Origin & Obtaining:\n" +
                "Scribed on stone surfaces using Scribed Chalk and Rune Stones.\n\n" +
                "Crafting Uses:\n" +
                "Used for high-tier transmutations, Materia Blessing synthesis, and focus inscription.",
                "hub_magic", 1, new ItemStack(Items.REDSTONE),
                280f, 0.0f, a_magic - 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "magic_spellcrafting", "Spellcrafting", "MAGIC",
                "Wands, foci, and active spellcasting.",
                "Overview & Description:\n" +
                "Channel elemental Materia into active spells using Shimmering Foci.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted using Arcanite Rods, Viscanite Cores, and active Essences.",
                "hub_magic", 1, new ItemStack(Items.BLAZE_ROD),
                280f, 0.0f, a_magic - 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_third_hand", "Arkanist's Third Hand", "MAGIC",
                "Utility summon gathering dropped items.",
                "Overview & Description:\n" +
                "A hovering spectral phantom hand that floats alongside the spellcaster.\n\n" +
                "Origin & Obtaining:\n" +
                "Inscribed into a Shimmering Focus using a Tier 2 Magic Circle.\n\n" +
                "Special Properties:\n" +
                "Autonomously gathers dropped items within 16 blocks, prioritizing items matching your hotbar.",
                "magic_spellcrafting", 2, new ItemStack(Items.SPECTRAL_ARROW),
                380f, 0.0f, a_magic - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_lucky_day", "Arkanist's Lucky Day", "MAGIC",
                "Temporary offhand Looting/Fortune enchant.",
                "Overview & Description:\n" +
                "Enchants held offhand weapons and tools with temporary Fortune and Looting.\n\n" +
                "Origin & Obtaining:\n" +
                "Inscribed into a Shimmering Focus using a Tier 2 Magic Circle.\n\n" +
                "Special Properties:\n" +
                "Provides Looting III and Fortune III for 30 uses before safely dissipating.",
                "magic_spellcrafting", 2, new ItemStack(Items.GOLDEN_PICKAXE),
                380f, 0.0f, a_magic, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_dagger", "Arkanist's Dagger", "MAGIC",
                "Autonomous spectral blade seeking mobs.",
                "Overview & Description:\n" +
                "A glowing ether blade that orbits the caster before darting toward hostiles.\n\n" +
                "Origin & Obtaining:\n" +
                "Inscribed into a Shimmering Focus using a Tier 2 Magic Circle.\n\n" +
                "Special Properties:\n" +
                "Autonomously targets hostile mobs within 16 blocks, dealing elemental damage based on current EssenceType.",
                "magic_spellcrafting", 2, new ItemStack(Items.DIAMOND_SWORD),
                380f, 0.0f, a_magic + 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "magic_astral", "Astral Signs", "MAGIC",
                "Constellation alignments and celestial power.",
                "Overview & Description:\n" +
                "Aligning scribing engines with active celestial constellations.\n\n" +
                "Origin & Obtaining:\n" +
                "Observed through the Arkanist Monocle during clear night skies.",
                "hub_magic", 1, new ItemStack(Items.AMETHYST_SHARD),
                290f, 0.0f, a_magic + 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "magic_alchemy", "Alchemy & Transmutation", "MAGIC",
                "Transmutation, catalysts, and brewing.",
                "Overview & Description:\n" +
                "Transmuting raw elements into rare alloys using catalyst receptacles.\n\n" +
                "Origin & Obtaining:\n" +
                "Performed at Alchemical Stills and Catalyst Receptacles.",
                "hub_magic", 1, new ItemStack(Items.POTION),
                280f, 0.0f, a_magic + 0.25f, false
        ));
    }

    public static CodexNode getNodeById(String id) {
        for (CodexNode node : ALL_NODES) {
            if (node.id.equals(id)) {
                return node;
            }
        }
        return null;
    }
}
