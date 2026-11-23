package ddraig.net.entropica.codex;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
                "materials_amber_chunk", "Amber Chunk", "MATERIALS",
                "Raw resin crystals harvested by stripping bark from Amber Logs or Amber Wood.",
                "Overview & Description:\n" +
                "Raw crystalline amber resin harvested from Amber-Wood trees.\n\n" +
                "Origin & Obtaining:\n" +
                "Drops in-world when stripping the bark off Amber Logs or Amber Wood using an Axe.\n\n" +
                "Crafting Uses:\n" +
                "Used in alchemical distils, wand foci, arcana book binding, and crystal alchemy.\n\n" +
                "Special Properties:\n" +
                "Contains concentrated golden resin essence.",
                "hub_materials", 0, new ItemStack(ModItems.AMBER_CHUNK.get()),
                300f, 0.0f, a_ing + 0.20f, false
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

        ALL_NODES.add(new CodexNode(
                "machinery_custom_wood_chests", "Elemental Wood Chests", "MACHINERY",
                "Color-coded wooden storage containers crafted from native Entropica wood species.",
                "Overview & Description:\n" +
                "Specially crafted wooden storage chests matching the distinct timber grain and color palette of Entropica's 10 native wood species.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted in a 3x3 crafting grid using 8 matching Planks of any specific wood type (Pyre-Ash Cedar, Abyssal Spore-Cypress, Starlight Aether-Birch, Blood-Root Iron-Oak, Astral-Veil Willow, Void-Blight Mangrove, Amber, Rubber, Silver Pine, or Materia-Echo).\n\n" +
                "Crafting Uses:\n" +
                "Provides 27 slots of item storage per single chest, and 54 slots when two matching chests are placed adjacent to form a Double Chest.\n\n" +
                "Special Properties:\n" +
                "Features unique colored iron latches and wood-grain staining matching each tree family.",
                "hub_machinery", 0, new ItemStack(ModBlocks.PYRE_ASH_CEDAR_CHEST.get()),
                310f, 0.0f, a_mach + 0.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "machinery_custom_wood_barrels", "Elemental Wood Barrels", "MACHINERY",
                "Compact storage barrels crafted from native Entropica wood species with distinct open/closed states.",
                "Overview & Description:\n" +
                "Sturdy wooden storage barrels crafted to match the unique timber grain, reinforcement hoops, and coloration of Entropica's 10 native wood species.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted in a crafting table using 6 Planks and 2 Slabs of any specific wood species (Pyre-Ash Cedar, Abyssal Spore-Cypress, Starlight Aether-Birch, Blood-Root Iron-Oak, Astral-Veil Willow, Void-Blight Mangrove, Amber, Rubber, Silver Pine, or Materia-Echo).\n\n" +
                "Crafting Uses:\n" +
                "Provides 27 slots of compact item storage that can be opened even when solid blocks are placed directly above it.\n\n" +
                "Special Properties:\n" +
                "Features directional placement in all 6 orientations and dynamic open/closed top textures with custom wood-grain banding.",
                "hub_machinery", 0, new ItemStack(ModBlocks.PYRE_ASH_CEDAR_BARREL.get()),
                310f, 0.0f, a_mach + 0.35f, false
        ));

        ALL_NODES.add(new CodexNode(
                "machinery_gravitational_anchor", "Gravitational Anchor", "MACHINERY",
                "Spatial field projector supporting Zero-G, Lunar, Inversion, Singularity, Repulsor, and Tidal modes.",
                "Overview & Description:\n" +
                "The Gravitational Anchor is a celestial field emitter encased in heavy brass plating. It manipulates local spacetime to establish customizable gravitational perimeters for research chambers and orbital habitats.\n\n" +
                "Origin & Obtaining:\n" +
                "Assembled using Arcanite Plating, Brass Ingots, and resonant Astral Crystals.\n\n" +
                "Operational Modes:\n" +
                "Right-clicking cycles between 6 distinct operational modes: Zero-G Chamber, Lunar Pavilion, Graviton Inversion, Singularity Well, Repulsor Dome, and Tidal Pulse.\n\n" +
                "Special Properties:\n" +
                "Displays floating holographic telemetry and can be toggled or locked using redstone signals.",
                "hub_machinery", 0, new ItemStack(ModItems.GRAVITATIONAL_ANCHOR_ITEM.get()),
                360f, 0.0f, a_mach - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "machinery_gravity_center", "Gravity Center Core", "MACHINERY",
                "Omnidirectional point-gravity well powered by Materia Fumus via Vapor Pneumatic Pipes.",
                "Overview & Description:\n" +
                "An advanced celestial gravity core that exerts a true spherical point-gravity field. Entities in its orbit are drawn toward the center, allowing 360-degree walking around floating planetoids and asteroid habitats.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted by infusing a Gravitational Anchor with Astral Crystals and Arcanite Plating.\n\n" +
                "Fuel & Connections:\n" +
                "Connects directly to Vapor Pneumatic Pipes on all six faces, consuming 1 mB of Materia Fumus vapor every 40 ticks.\n\n" +
                "Planetary Inversion:\n" +
                "When travelers cross below the core's equator, their orientation smoothly inverts by 180 degrees, allowing seamless walking on the underside of celestial bodies.",
                "machinery_gravitational_anchor", 0, new ItemStack(ModItems.GRAVITY_CENTER_ITEM.get()),
                440f, 0.0f, a_mach - 0.22f, false
        ));

        ALL_NODES.add(new CodexNode(
                "machinery_tidal_pulse_resonator", "Tidal Pulse Resonator", "MACHINERY",
                "Cyclic celestial resonator pulsing rhythmic gravity tides.",
                "Overview & Description:\n" +
                "A standalone tuning resonator equipped with 4 vibrating brass prongs that project rhythmic gravitational waves across an area.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted with Brass Ingots, Astral Crystals, Arcanite Plating, and a Gravitational Anchor.\n\n" +
                "Respiration Cycle:\n" +
                "Operates on a continuous 4-second cycle: 3 seconds of gentle lunar float followed by a 1-second heavy downward gravity slam.\n\n" +
                "Redstone Control:\n" +
                "Can be toggled by hand or locked into pause using redstone wire signals.",
                "machinery_gravitational_anchor", 0, new ItemStack(ModItems.TIDAL_PULSE_RESONATOR_ITEM.get()),
                440f, 0.0f, a_mach - 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "machinery_graviton_soles", "Graviton Soles & Gear", "MACHINERY",
                "Pneumatic boots and Curios gear for 360-degree surface walking, wall climbing, and fall negation.",
                "Overview & Description:\n" +
                "Engineered footwear equipped with micro-graviton magnets that interface with block surfaces.\n\n" +
                "Wall Traversal:\n" +
                "Walking forward into a wall climbs up at 0.26 m/s, moving backward descends at 0.22 m/s, and crouching locks you in place with zero slipping.\n\n" +
                "Ceiling & Corner Wrapping:\n" +
                "Touching a ceiling inverts gravity with a smooth 180-degree camera roll. Crouching off block ledges smoothly wraps your footing around corners.\n\n" +
                "Curios Support:\n" +
                "Fits seamlessly in the Curios feet accessory slot, completely negating all kinetic fall damage.",
                "hub_machinery", 0, new ItemStack(ModItems.GRAVITON_SOLES.get()),
                360f, 0.0f, a_mach + 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "graviton_bouncepad", "Graviton Bouncepad", "MACHINERY",
                "Omnidirectional kinetic launch pad utilizing localized anti-gravity impulses with fall-damage immunity and redstone scaling.",
                "Overview & Description:\n" +
                "The Graviton Bouncepad is a compact technomantic kinetic propulsion machine encased in heavy Arcanite plating. When stepped upon by players, mobs, or propelled objects, it instantly neutralizes incoming kinetic energy and discharges an upward anti-gravitational shockwave.\n\n" +
                "Origin & Obtaining:\n" +
                "Assembled in a crafting table using a Heavy Weighted Pressure Plate, Slime Block, and Arcanite Ingots.\n\n" +
                "Kinetic Mechanics & Parity:\n" +
                "▪ Omnidirectional Mounting: Can be placed flush against floors, vertical walls, or suspended overhead from ceilings, adapting its launch orientation accordingly.\n" +
                "▪ Gravity-Relative Upward Launch: Evaluates the entity's current local gravity vector (accounting for inverted, lunar, or planetary gravity wells) and launches in the entity's relative upward direction (-g).\n" +
                "▪ Analogue Redstone Scaling: Connecting redstone dust scales the kinetic launch impulse dynamically from level 0 (base impulse) up to level 15 (extreme high-velocity ejection).\n\n" +
                "Special Properties:\n" +
                "Grants complete kinetic fall damage immunity on landing, preventing injury regardless of launch velocity or fall height. Synergizes with Graviton Soles and Forcefield Barriers for advanced traversal courses.",
                "hub_machinery", 0, new ItemStack(ModBlocks.GRAVITON_BOUNCEPAD.get()),
                280f, 0.0f, a_mach - 0.20f, false
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

        ALL_NODES.add(new CodexNode(
                "multiblock_grand_observatory", "Grand Circular Astral Observatory (7x7x6)", "MULTIBLOCKS",
                "Monumental astronomical dome with 5x5 mirror pool and 32x cosmic magnification.",
                "Overview & Description:\n" +
                "A monumental 7x7x6 architectural complex designed for deep-sky cosmic observation, singularity tracking, and large-scale starlight collection.\n\n" +
                "Origin & Obtaining:\n" +
                "Constructed from 25 Astral Mirror Blocks (5x5 pool), 20 Engraved Astral Slate, 16 Astral Marble Bricks, 8 Starlight Pillars, 8 Astral Marble Walls, 4 cardinal Refractive Astral Lenses, and 1 Celestial Armillary Controller.\n\n" +
                "Crafting Uses:\n" +
                "Unlocks the Celestial Armillary Controller (32x cosmic magnification) and feeds multiple high-tier optical conduits.\n\n" +
                "Special Properties:\n" +
                "Can be projected as a 3D translucent in-world ghost template using the Astrolabe.",
                "hub_multiblocks", 3, new ItemStack(ModBlocks.CELESTIAL_ARMILLARY_CONTROLLER.get()),
                360f, 0.0f, a_multi - 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "multiblock_modular_astral_altar", "Modular Astral Altar (7x7x7)", "MULTIBLOCKS",
                "Harmonic resonance ritual crafting grid for starlight transmutation.",
                "Overview & Description:\n" +
                "A 7x7x7 modular ritual platform designed for high-tier starlight item synthesis and constellation infusions.\n\n" +
                "Origin & Obtaining:\n" +
                "Constructed with 33 Astral Marble Bricks, 20 Engraved Astral Slate, 16 Resonance Pylons, 8 Attunement Pedestals, 1 Focal Lens Mount, and 1 central Astral Altar Core.\n\n" +
                "Crafting Uses:\n" +
                "Infuses Resplendent Prisms, Mantles of the Stars, and high-purity Astral Crystal Tools.\n\n" +
                "Special Properties:\n" +
                "Maintains persistent starlight focus during daylight hours if initialized under a clear night sky.",
                "hub_multiblocks", 2, new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get()),
                360f, 0.0f, a_multi + 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "multiblock_astral_collection_altar", "Astral Collection Altar (5x5x3)", "MULTIBLOCKS",
                "Starlight Collector Plinth with socketed Astral Crystal and beam forwarding.",
                "Overview & Description:\n" +
                "A 5x5x3 celestial gathering plinth designed to capture cosmic starlight through an inscribed Astral Crystal and beam collimated energy into Altars or Lenses.\n\n" +
                "Origin & Obtaining:\n" +
                "Built from 8 Chiseled Astral Marble cornerstones, 8 Engraved Astral Slate border slabs, 9 Astral Mirror Blocks (3x3 pool), 1 central Starlight Pillar, and 1 Astral Collector on top.\n\n" +
                "Collector Cannibalization & Chaining:\n" +
                "Collection Altars can be linked directly into other Collection Altars. When chained, the downstream collector cannibalizes the upstream flux, compounding the starlight stream.\n\n" +
                "Special Properties:\n" +
                "Right-click with an Astral Crystal to socket. Use the Astral Linking Wand to configure target coordinates.",
                "multiblock_modular_astral_altar", 2, new ItemStack(ModBlocks.ASTRAL_COLLECTOR.get()),
                440f, 0.0f, a_multi + 0.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "multiblock_master_astral_altar", "Master Astral Altar Network (11x11x7)", "MULTIBLOCKS",
                "Monumental 11x11x7 ritual matrix with 12 pedestals, corner lens receivers, and multi-spectral cascade.",
                "Overview & Description:\n" +
                "The pinnacle of celestial architecture, the 11x11x7 Master Astral Altar integrates 4 tall corner collector towers with an overhead concave lens ring and focal apex.\n\n" +
                "Origin & Obtaining:\n" +
                "Constructed from 65 Astral Marble Bricks, 36 Engraved Astral Slate, 24 Starlight Pillars, 12 Attunement Pedestals, 4 Refractive Astral Lenses, 1 Focal Lens Mount, and 1 central Astral Altar Core.\n\n" +
                "Multi-Beam Spectral Cascade:\n" +
                "Receives multiple incoming starlight beams from surrounding Astral Collection Altars. Overhead apex focuses a prismatic spectrum of colored starlight rays onto the central crystal and pedestals.\n\n" +
                "Special Properties:\n" +
                "Accelerates crystal growth up to 10x and unlocks Master tier celestial transmutations.",
                "multiblock_modular_astral_altar", 3, new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get()),
                440f, 0.0f, a_multi - 0.10f, false
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
                "Regional biomes saturated with unique ambient Materia and elemental essence flows across 6 climatic zones (Polar/Frozen, Cold/Alpine, Temperate, Warm/Wetland, Hot/Arid, and Nether Volcanic Depths).\n\n" +
                "Origin & Obtaining:\n" +
                "Explored naturally throughout the Overworld and Nether.\n\n" +
                "Configuration & Customization:\n" +
                "Entropica features a built-in Multi-Noise Biome Injector that works standalone or alongside TerraBlender (giving way to modded TerraBlender biomes by default, or layering on top if configured), paired with a Sodium/Embeddium/Rubidium & Iris/Oculus-compatible Enhanced Biome Volumetric Mist & Water Optics Renderer (ground-hugging channel mist, Fresnel sky/aurora reflections, and 3-layer sub-surface starlight parallax, controlled via enable_enhanced_biome_fog_and_water, default: true). All 108 biomes, region weights, region scale, TerraBlender compatibility modes, and atmosphere toggles can be customized under Biome Generation Settings (biome_settings / biomeSettings) in the mod config.",
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
                "env_biome_blue_glacier_shelf", "Blue-Glacier Shelf", "ENVIRONMENT & NATURE",
                "Sheer coastal blue-ice cliffs and overhanging shelves saturated with Glacial and Aegis Materia.",
                "Overview & Description:\n" +
                "Biome #1 (entropica:blue_glacier_shelf) forms towering coastal cliffs and horizontal terraces of compressed Blue Ice and Packed Ice rising from Y=63 sea level up to Y=90 under a pale indigo sky (#3F4E8F), thick coastal haze, horizontally drifting snow, and an overhead Glacial Ice Halo.\n\n" +
                "Environmental Hazards & Materia:\n" +
                "▪ Primary Glacial & Secondary Aegis Materia: Favors Glacial (60%), Aegis (30%), and Frozen (10%) Grots alongside Rime-Back Ovis, Glacial Frost-Cores, and Frost-Tusk Mammots.\n" +
                "▪ Ice Shelf Slipperiness & Aegis Heavy Gravity: Smooth Blue Ice (0.989) and Packed Ice (0.98) terraces pair with a +15% faster downward fall speed inside the biome (smoothly blended across borders while keeping fall damage normal).\n\n" +
                "Native Resources & Landmarks:\n" +
                "Home to embedded Aegis Crystal veins, Rimebloom crevasse flowers, ceiling-hanging Glacial Heart-Roots, Shielding Brambles, and the rare Frost-Veiled Glacial Runic Spire.",
                "env_biomes", 1, new ItemStack(ModBlocks.AEGIS_CRYSTAL_CLUSTER.get()),
                360f, 0.0f, a_env - 0.22f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aegis_crystal", "Aegis Crystals & Linings", "ENVIRONMENT & NATURE",
                "Faceted light-blue crystals, defensive linings, shattering barricades, and projectile-sensing plating.",
                "Overview & Description:\n" +
                "Faceted light-blue Aegis Crystal Blocks sprout 2-stage Aegis Crystals—young Aegis Crystal Buds (Stage 1) that mature over time into full Aegis Crystal Clusters (Stage 2)—embedded inside the sheer ice cliffs of the Blue-Glacier Shelf and radiating protective Aegis Materia.\n\n" +
                "Origin & Obtaining:\n" +
                "Mine young Aegis Crystal Buds with an Iron Pickaxe or better for 1 Aegis Crystal Shard (or Silk Touch to replant on Aegis Crystal or Blue Ice), and harvest mature Aegis Crystal Clusters for 2-4 Aegis Crystal Shards (4 shards compact into an Aegis Crystal Block).\n\n" +
                "Crafting & Defensive Gear:\n" +
                "▪ Aegis Shielding Lining: Crafted from Glacial Aegis Plates, Aegis Crystal Shards, and Leather; grants passive Resistance I in your inventory or offhand and shields against Runic Spire hypothermia.\n" +
                "▪ Glacial Barricade: 3-charge ice bulwark crafted from Glacial Aegis Plates and Packed Ice that absorbs incoming projectiles and shatters on its third hit in a 3.5-block frost blast (4.0 cold damage + Slowness III).\n" +
                "▪ Conductive Aegis Plating: Forged from Patina Aegis Plates, Copper, and Redstone; emits a 20-tick strength-15 redstone pulse whenever struck by a projectile.\n" +
                "▪ Runic Blue Ice & Runed Basalt Pillar: Architectural Spire blocks infused with Aegis Crystal Shards.",
                "env_biome_blue_glacier_shelf", 1, new ItemStack(ModItems.AEGIS_CRYSTAL_SHARD.get()),
                440f, 0.0f, a_env - 0.18f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_spire_structure_1", "Frost-Veiled Glacial Runic Spire", "ENVIRONMENT & NATURE",
                "Rare 18x18x45 hexagonal Blue-Ice tower housing the Frost Scriptorium, waterfall Runic Vault, and Aegis Crystal apex.",
                "Overview & Description:\n" +
                "Rising 45 blocks above the Blue-Glacier Shelf, this tapering hexagonal tower of Blue Ice, Packed Ice flying buttresses, Runed Basalt Pillars, and Silver Pine scaffolding is crowned by a floating Aegis Crystal prism.\n\n" +
                "Chambers & Secrets:\n" +
                "▪ Frost Scriptorium (Y+20): Glowing Runic Blue Ice study lined with lecterns, ice benches, and a relic treasure chest.\n" +
                "▪ Waterfall Runic Vault (Y-6): Hidden behind a ground-floor waterfall curtain, featuring Rimebloom-lined Blue Ice floors, a narrow safe Stone Brick walkway, and tripwire/pressure-plate dispensers loaded with Splash Potions of Freezing and Slowness.\n" +
                "▪ Spire Relics & Aegis Apex (Y+42): Chests contain the rare Glacial Focus Lens alongside Rimebloom Petals, Glacial Heart-Roots, Packed Ice, and precious metals beneath the glowing Aegis Crystal summit.\n\n" +
                "Interior Hypothermia:\n" +
                "Unprotected explorers inside the Spire suffer 1 heart of freezing hypothermia every 5 seconds unless carrying Aegis or Cryo frost protection (Cryo-Insulated Armor, Aegis Shielding Lining, or Thermal Insulated Blanket).",
                "env_biome_blue_glacier_shelf", 2, new ItemStack(ModBlocks.AEGIS_CRYSTAL_BLOCK.get()),
                440f, 0.0f, a_env - 0.28f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_auroral_skerries", "2.2 Auroral Skerries", "ENVIRONMENT & NATURE",
                "Steaming polar archipelago of flat black-gravel islets, warm teal channels, Glimmer-Ice shores, and emerald auroras.",
                "Overview & Description:\n" +
                "Biome #2 (entropica:auroral_skerries) scatters low, flat volcanic Coarse Gravel islets (Y=63-65) across warm teal geothermal channels (#0A8E89) bordered by glowing Glimmer-Ice shelves beneath shimmering emerald-green night auroras (#0E6A3B) and rising white steam.\n\n" +
                "Environmental Hazards & Materia:\n" +
                "▪ Primary Aurora & Secondary Vapor Materia: Favors Aurora (55%), Vapor (35%), and Glacial (10%) Grots alongside drifting Aurora Jellies and roaming Frost-Tusk Mammots.\n" +
                "▪ Dense Steam Fog (12 Blocks): Thick geothermal steam cuts visibility down to roughly 12 blocks unless you hold a torch, lantern, or glowing Materia light source (+18 blocks of fog relief).\n\n" +
                "Native Resources & Landmarks:\n" +
                "Home to underwater bubbling steam vents, Glimmer-Ice shorelines, Auroral Feather-Moss carpets, Mist-Veil Ferns, ice-melting Auroral Lily-Pads, and the circular Auroral Wellspring sanctuary.",
                "env_biomes", 1, new ItemStack(ModBlocks.GLIMMER_ICE.get()),
                375f, 0.0f, a_env - 0.48f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_glimmer_ice", "Glimmer-Ice & Coarse Gravel", "ENVIRONMENT & NATURE",
                "Glowing shoreline aurora ice, shimmering Aurora Dust, Auroral Spectral Dye, and dark volcanic Coarse Gravel.",
                "Overview & Description:\n" +
                "Along the steaming water channels of the Auroral Skerries, Aurora Materia crystallizes into translucent Glimmer-Ice (Light Level 10) banded with gold, pink, and emerald light alongside flat islets of gravity-affected basalt-black Coarse Gravel.\n\n" +
                "Origin & Obtaining:\n" +
                "▪ Glimmer-Ice & Aurora Dust: Harvest Glimmer-Ice intact with Silk Touch, or break it normally to collect 2-3 shimmering Aurora Dust. Packed Ice surrounded by 4 Aurora Dust reforms Glimmer-Ice, and 1 Glimmer-Ice breaks down into 2 Aurora Dust.\n" +
                "▪ Coarse Gravel: Dug across skerries islets with a shovel, or mixed from 2 Gravel and 2 Basalt (yields 4 Coarse Gravel).\n\n" +
                "Crafting Uses:\n" +
                "Aurora Dust combines with Glow Ink Sacs or White Dye into 2x Auroral Spectral Dye, and powers the crafting of Auroral Wands and prismatic Auroral Lens Blocks.",
                "env_biome_auroral_skerries", 1, new ItemStack(ModItems.AURORA_DUST.get()),
                465f, 0.0f, a_env - 0.41f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_wellspring_structure_3", "Auroral Wellspring", "ENVIRONMENT & NATURE",
                "24x24x12 concentric quartz sanctuary with debuff-cleansing thermal waters, copper-domed arches, and prismatic Auroral Lens.",
                "Overview & Description:\n" +
                "Anchored at sea level (Y=63) in the Auroral Skerries, this 24x24x12 Smooth Quartz and Quartz Bricks sanctuary forms three concentric water rings—an outer cold channel moat, a Glimmer-Ice and Auroral Lily-Pad middle basin, and a warm bubbling inner thermal spring—crowned by four copper-domed quartz arches.\n\n" +
                "Sanctuary Blocks & Relics:\n" +
                "▪ Auroral Lens Block (Light Level 12): Prismatic crystal suspended above the warm inner thermal spring; chimes and projects an upward aurora beam when struck or right-clicked. Crafted from Aurora Dust, Amethyst/Glass, and rare Vapor Condensate Crystals.\n" +
                "▪ Glimmer-Ice Pedestals: Glowing Glimmer-Ice nodes border the radial quartz walkways around the inner pool.\n" +
                "▪ Sanctuary Chests: Yield Vapor Condensate Crystals, Auroral Spectral Dye, Glowing Auroral Fibers, Prismarine Crystals, and rare Hearts of the Sea.\n\n" +
                "Thermal Spring Blessing & Vapor Clouds:\n" +
                "Bathing in the unfrozen inner thermal pool washes away Slowness, Weakness, Poison, Mining Fatigue, and freezing chill, and stepping out grants Speed II and Jump Boost II for 30 seconds. Every 20 seconds, rising aurora-vapor clouds and arch dispensers bathe nearby visitors in Regeneration I and Invisibility I.",
                "env_biome_auroral_skerries", 2, new ItemStack(ModBlocks.AURORAL_LENS_BLOCK.get()),
                465f, 0.0f, a_env - 0.54f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_frost_bite_floes", "2.3 Frost-Bite Floes", "ENVIRONMENT & NATURE",
                "Rotting green pack-ice floes drifting at sea level on freezing murky swamp water beneath foul blight mist.",
                "Overview & Description:\n" +
                "Biome #3 (entropica:frost_bite_floes) scatters fractured, blocky sheets of Blighted Packed Ice and treacherous Rotten Ice floating at sea level (Y=63) over dark grey swamp water (#3B423D) beneath a sickly yellow-green sky (#7D8C43), olive night haze (#2A3318), and drifting black rot spores.\n\n" +
                "Environmental Hazards & Materia:\n" +
                "▪ Primary Glacial & Secondary Blight Materia: Favors Glacial (50%), Blight (40%), and Frozen (10%) Grots alongside camouflaged Cryo-Stalkers, Slimes, Bogged, Strays, and Drowned.\n" +
                "▪ Rotten Ice Collapse & Poison: Stepping onto Rotten Ice without sneaking or wearing Cryo-Insulated Boots inflicts Poison I and shatters the ice beneath your feet, plunging you into the freezing swamp below.\n" +
                "▪ Freezing Swamp-Water Hypothermia: Wading in the frigid bog water inflicts Slowness and steadily builds freezing frostbite unless protected by Cryo-Insulated Armor.\n\n" +
                "Native Resources & Flora:\n" +
                "Home to half-submerged Frozen Dead Spruce trunks with rime-coated Frozen Spruce Branches, scoopable Necrotic Slime crusts, Rot Powder, and stunted Frostbite Blossoms (Rimebloom) clinging to decaying floes.",
                "env_biomes", 1, new ItemStack(ModBlocks.BLIGHTED_PACKED_ICE.get()),
                395f, 0.0f, a_env - 0.66f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_rotten_ice_slime", "Rotten Ice & Necrotic Slime", "ENVIRONMENT & NATURE",
                "Collapsing blight-mottled floe ice, Rot Powder, Potions of Decay, scoopable Necrotic Slime crusts, and toxic slime blocks.",
                "Overview & Description:\n" +
                "Glacial ice sheets steeped in swamp Blight Materia decay into dense Blighted Packed Ice and brittle Rotten Ice coated in slick Necrotic Slime Patches and tangled Frozen Spruce Branches.\n\n" +
                "Origin & Obtaining:\n" +
                "▪ Rotten Ice & Rot Powder: Harvest Rotten Ice intact with Silk Touch, or break/collapse it for Rot Powder (2 Ice + 2 Rot Powder reforms 2 Rotten Ice; 4 Packed Ice + Rot Powder + Necrotic Slime crafts 4 Blighted Packed Ice).\n" +
                "▪ Necrotic Slime & Slime Buckets: Right-click a Necrotic Slime Patch with an empty Bucket to scoop a Necrotic Slime Bucket (which spreads a new patch onto ice or stone and returns the bucket), shear for the intact patch, or break for 1-2x Necrotic Slime.\n" +
                "▪ Frozen Spruce Branches: Snapped from half-frozen dead spruce trunks with Silk Touch or Shears (otherwise drops 1-2 Sticks).\n\n" +
                "Alchemy & Crafting Uses:\n" +
                "▪ Potion of Decay (entropica:decay): Inflicts Poison I and Weakness I for 45 seconds across regular, splash, lingering, and tipped arrow variants.\n" +
                "▪ Necrotic Slime Block: Compacted from 9 Necrotic Slime (reversible into 9 slime, or 3 in a row for 6 patches); bounces falling entities like slime while poisoning and slowing uninsulated intruders.",
                "env_biome_frost_bite_floes", 1, new ItemStack(ModItems.NECROTIC_SLIME_BUCKET.get()),
                480f, 0.0f, a_env - 0.66f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_icebound_blight_wreck", "Icebound Blight-Wreck", "ENVIRONMENT & NATURE",
                "16x22x14 crushed expedition ship frozen into Blighted Packed Ice with Rotten Ice bilge traps, Glacial/Blight Essences, and early-game gear.",
                "Overview & Description:\n" +
                "Anchored at sea level across Frost-Bite Floes, this 16x22x14 canted wooden vessel lies crushed inside a jagged pressure ridge of Blighted Packed Ice and treacherous Rotten Ice beneath a snapped, rime-crusted mast and hanging Soul Lantern.\n\n" +
                "Hazards & Loot:\n" +
                "▪ Flooded Bilge & Stern Cabin: Watch your step in the Necrotic Slime-coated lower hold where Rotten Ice planks give way into freezing swamp water—tucked under the tilted bow sits a smuggler's Silver Pine Barrel, while dry Silver Pine steps climb to the Captain's Cabin chest.\n" +
                "▪ Biome Essences & Starter Gear: Chests reward Glacial and Blight Materia Essences & Ampoules, Cryo-Insulated Boots or Frost-Bite Daggers, Arcanite Ingots & Nuggets, Small Ampoules, Arcane Clay, and polar survival rations.",
                "env_biome_frost_bite_floes", 2, new ItemStack(ModItems.CRYO_INSULATED_BOOTS.get()),
                565f, 0.0f, a_env - 0.66f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_crystalline_pack_ice", "2.4 Crystalline Pack-Ice", "ENVIRONMENT & NATURE",
                "Peaceful inland polar snowfields of Blue Ice streaks and glowing Glimmer-Ice & Aegis Quartz spires beneath violet night skies.",
                "Overview & Description:\n" +
                "Biome #4 (entropica:crystalline_pack_ice) unfolds across rolling inland polar mountain valleys floored with Snow Blocks, Packed Ice, and exposed Blue Ice bands beneath a lavender-blue sky, violet night skies (#4E2A7A) with brighter stars, and a violet-cyan Glacial Ice Halo.\n\n" +
                "Peaceful Sanctuary & Materia:\n" +
                "▪ Zero Environmental Hazards: Unlike coastal blizzards or rotting swamp floes, Crystalline Pack-Ice is a calm sanctuary biome.\n" +
                "▪ Primary Aegis & Secondary Aurora Materia: Favors Aegis (55%), Aurora (35%), and Glacial (10%) Grots alongside Rime-Back Ovis, drifting Aurora Jellies, Rabbits, Foxes, and rare nocturnal Cryo-Stalkers.\n\n" +
                "Native Spires, Ores & Flora:\n" +
                "Dotted with 6-13 block spires of glowing Glimmer-Ice, Blue Ice, Aegis Quartz Blocks, and Aegis Quartz Clusters, rich underground veins of rainbow Bismuth Ore, snowfield Aegis-Spire Orchids (4-block Resistance I aura), Frostbite Blossoms (Rimebloom), and the rare Aegis Shielding Monolith.",
                "env_biomes", 1, new ItemStack(ModBlocks.AEGIS_QUARTZ_BLOCK.get()),
                415f, 0.0f, a_env - 0.80f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aegis_quartz_bismuth", "Aegis Quartz & Rainbow Bismuth", "ENVIRONMENT & NATURE",
                "Glowing spire-base Aegis Quartz crystals, rainbow hopper-crystal Bismuth metallurgy, and double-obsidian Aegis Plated Stone.",
                "Overview & Description:\n" +
                "Crystalline spires across Crystalline Pack-Ice concentrate Aegis Materia at their roots as luminous Aegis Quartz, while surrounding strata hold stepped rainbow veins of conductive Bismuth Ore.\n\n" +
                "Origin & Obtaining:\n" +
                "▪ Aegis Quartz & Clusters: Mine Aegis Quartz Clusters (Light Level 7) for 2-4x Aegis Quartz (Fortune-boosted; Silk Touch keeps the cluster). 4 Aegis Quartz compact into an Aegis Quartz Block (Light Level 6, reversible).\n" +
                "▪ Bismuth Metallurgy: Mine Bismuth Ore with an Iron Pickaxe+ for Raw Bismuth and smelt or blast into Bismuth Ingots (9 ingots compact into a Block of Bismuth).\n\n" +
                "Defensive Masonry:\n" +
                "▪ Aegis Plated Stone: Forged from 4 Stone Bricks, 4 Bismuth Ingots, and 1 Obsidian into heavy bismuth-riveted masonry boasting double the hardness and blast resistance of Obsidian.",
                "env_biome_crystalline_pack_ice", 1, new ItemStack(ModItems.BISMUTH_INGOT.get()),
                500f, 0.0f, a_env - 0.75f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aegis_shielding_charm_core", "Aegis Shielding Charm & Iron Skin", "ENVIRONMENT & NATURE",
                "Fatal-blow lifesaving Aegis Shielding Charms, Aegis Warding Emblems, and Potions of Iron Skin.",
                "Overview & Description:\n" +
                "Combining snowfield Aegis-Spire Petals, Aegis Quartz, and conductive Bismuth unlocks personal lifesaving wards and heavy defensive alchemy.\n\n" +
                "Crafted Wards & Alchemy:\n" +
                "▪ Aegis Shielding Charm: Woven from 1 Aegis-Spire Petal, 2 Bismuth Ingots, 2 Aegis Quartz, and String. Carried in your inventory or offhand, it shatters to intercept a single fatal blow—restoring 2 hearts, cleansing harmful debuffs, and granting Absorption II and Resistance II.\n" +
                "▪ Potion of Iron Skin (entropica:iron_skin): Defensive Aegis brew granting Resistance II and Absorption I for 90 seconds across regular, splash, lingering, and tipped arrow variants.",
                "env_biome_crystalline_pack_ice", 1, new ItemStack(ModItems.AEGIS_SHIELDING_CHARM.get()),
                500f, 0.0f, a_env - 0.85f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_monolith_structure_2", "Aegis Shielding Monolith", "ENVIRONMENT & NATURE",
                "15x15x22 Obsidian & Bismuth sanctuary with 3 outer pylon towers, a floating Aegis Crystal octahedron, and a Bismuth-keyed vault.",
                "Overview & Description:\n" +
                "Anchored in the snowfields of Crystalline Pack-Ice, this 15x15x22 stepped triangular dais of Obsidian, Crying Obsidian, and Aegis Plated Stone supports three 12-block Bismuth-trimmed outer pylons beaming aurora-cyan sparks toward a floating Aegis Crystal octahedron.\n\n" +
                "Under-Dais Bismuth Vault:\n" +
                "Rear stairs lead into a Conductive Aegis Plating and copper maintenance room where slotting 1 Bismuth Ingot into the Bismuth Key Pedestal powers open the locked Iron Door to treasure chests holding the rare Aegis Warding Emblem (passive Resistance I).",
                "env_biome_crystalline_pack_ice", 2, new ItemStack(ModItems.AEGIS_WARDING_EMBLEM.get()),
                585f, 0.0f, a_env - 0.80f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_silent_fjord", "2.5 Silent Fjord", "ENVIRONMENT & NATURE",
                "Deep dark navy fjord inlets carved between sheer Y=120 hexagonal Basalt Columns under a bruised violet Eclipse sky.",
                "Overview & Description:\n" +
                "Biome #5 (entropica:silent_fjord) carves narrow, deep dark navy inlets (#080E26) between sheer vertical walls of hexagonal Basalt Columns rising up to Y=120 beneath a bruised violet sky (#1B0E3A), 0.42x eclipsed twilight shadows, cold grey fog (#3E4452), silent snowfall, and an overhead Eclipse Corona.\n\n" +
                "Environmental Hazard (Deep Shadows) & Materia:\n" +
                "▪ Primary Eclipse & Secondary Glacial Materia: Favors Eclipse (55%), Glacial (35%), and Frozen (10%) Grots alongside Veil Foxes, camouflaged Cryo-Stalkers, Glow Squids, and shadow-prowling Strays and Drowned.\n" +
                "▪ Deep Shadows Cliff Gloom: Under the dimmed canopy and towering basalt walls (block light <= 5), hostile monsters gain Speed I and resist daylight sun-burn, while unlit travellers lingering in deep cliff shadows (block light <= 2) build creeping frostbite chill unless holding a light or wearing Silent Striders / Cryo-Insulated Boots.\n\n" +
                "Native Landmarks & Flora:\n" +
                "Home to hexagonal Basalt Column cliffs, Chiseled Eclipse Basalt cairns, cushioning Blue Frozen Moss, dark-crevice Shadowed Eclipse Moss, water-freezing Glacial Heart-Roots, and rare cliff-top Frost-Veiled Glacial Runic Spires.",
                "env_biomes", 1, new ItemStack(ModBlocks.BASALT_COLUMN.get()),
                435f, 0.0f, a_env - 0.94f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_basalt_columns_frozen_moss", "Basalt Columns & Blue Frozen Moss", "ENVIRONMENT & NATURE",
                "Hexagonal Basalt Columns, Chiseled Eclipse Basalt, Basalt Pickaxes, and cushioning Bone-Meal-spreadable Blue Frozen Moss.",
                "Overview & Description:\n" +
                "Towering organ-pipe cliffs of hexagonal volcanic Basalt Columns line the shores of Silent Fjord, blanketed in soft cushions of pale Blue Frozen Moss.\n\n" +
                "Masonry & Tools:\n" +
                "▪ Basalt Column & Chiseled Eclipse Basalt: Craft 4 Basalt Columns from 2 Basalt and 2 Smooth Basalt, and carve 4 Chiseled Eclipse Basalt (Light Level 3) around Shadowed Eclipse Moss or Amethyst.\n" +
                "▪ Basalt Pickaxe: Forged from 3 Basalt Columns and 2 Sticks for durable 220-use volcanic mining.\n\n" +
                "Blue Frozen Moss & Carpets:\n" +
                "▪ Cushioning & Silent: Both Blue Frozen Moss and Blue Frozen Moss Carpets (Light Level 2) dampen sculk/footstep vibrations and reduce fall damage by 40%.\n" +
                "▪ Propagation & Clumps: Bone Meal spreads Blue Frozen Moss across adjacent basalt and stone; breaking carpets without Shears yields Frozen Moss Clumps (craftable into Light Blue Dye or 2x2 back into Blue Frozen Moss).",
                "env_biome_silent_fjord", 1, new ItemStack(ModBlocks.BLUE_FROZEN_MOSS.get()),
                525f, 0.0f, a_env - 0.89f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_shadowed_eclipse_moss_striders", "Shadowed Eclipse Moss & Silent Striders", "ENVIRONMENT & NATURE",
                "Night-Vision Shadow Siphon moss carpets, footstep-silencing Silent Striders boots, and Potions of Eclipse Shroud.",
                "Overview & Description:\n" +
                "In dark basalt undercuts (light level <= 4) of Silent Fjord, charcoal-violet Shadowed Eclipse Moss creeps silently over stone, breathing dark twilight motes.\n\n" +
                "Shadow Siphon & Harvesting:\n" +
                "▪ Standing on Shadowed Eclipse Moss silences all footsteps/vibrations and grants Night Vision while slowly draining hunger saturation. Harvest intact with Shears or Silk Touch.\n\n" +
                "Eclipse Stealth Gear & Alchemy:\n" +
                "▪ Silent Striders: Stitched from Leather Boots, 4 Shadowed Eclipse Moss, and 2 Blue Frozen Moss Carpets (+2 Armor, +1 Toughness). Permanently dampens sculk/warden vibrations, cuts monster detection range by 35%, and wards off Fjord Shadow Chill.\n" +
                "▪ Potion of Eclipse Shroud (entropica:eclipse_shroud): Grants Speed I and Night Vision for 3 minutes while halving hostile monster detection range (-50%).",
                "env_biome_silent_fjord", 1, new ItemStack(ModItems.SILENT_STRIDERS.get()),
                525f, 0.0f, a_env - 0.99f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_eclipse_fjord_sanctum", "Eclipse Fjord Watch-Sanctum", "ENVIRONMENT & NATURE",
                "15x17x16 cliffside Basalt Column & Chiseled Eclipse Basalt shrine with a trapped Shadow Crypt, Eclipse/Glacial Essences, and starter gear.",
                "Overview & Description:\n" +
                "Perched on dry basalt cliff terraces in Silent Fjord, this 15x17x16 Nordic-basalt shrine pairs twin 8-block hexagonal Basalt Column watch-obelisks and a vaulted Deepslate Brick nave with a glowing purple Chiseled Eclipse Basalt oculus and ritual altar.\n\n" +
                "Shadow Crypt & Biome Essence Loot:\n" +
                "▪ Sunken Shadow Crypt: An interior stair lined with footstep-silencing Shadowed Eclipse Moss descends into a dark crypt where a tripwire dispenser fires Blindness and Slowness potions ahead of the reliquary chest and Silver Pine Barrel.\n" +
                "▪ Biome Essences & Early-Game Rewards: Rewards Eclipse and Glacial Materia Essences & Ampoules, Silent Striders, Cryo-Grenades, Basalt Pickaxes, Arcanite Ingots & Nuggets, Small Ampoules, and explorer supplies.",
                "env_biome_silent_fjord", 2, new ItemStack(ModBlocks.CHISELED_ECLIPSE_BASALT.get()),
                610f, 0.0f, a_env - 0.94f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_steaming_skerries", "2.6 Steaming Skerries", "ENVIRONMENT & NATURE",
                "Flat Blue-Ice and Steaming-Ice coasts cut by shallow hot-spring geysers beneath a warm yellow-pink sky.",
                "Overview & Description:\n" +
                "Biome #6 (entropica:steaming_skerries) stretches along polar shores where flat plates of Blue Ice, Packed Ice, and fractured Steaming Ice (Y=63) are interwoven with clear shallow hot-spring pools and bubbling Steam Vents beneath a warm yellow-pink sky (#C47B82) and dense rising steam.\n\n" +
                "Atmosphere & Materia:\n" +
                "▪ Primary Vapor & Secondary Glacial Materia: Favors Vapor (60%), Glacial (30%), and Magma (10%) Grots alongside sideways-scuttling Vapor-Bellows Crabs and serpentine Geyser Wiggle-Worms.\n" +
                "▪ Steam Haze & Warm Pools: Thick warm steam reduces sight distance unless carrying a handheld light (+14 blocks relief), while wading in warm pools clears freezing chill.\n\n" +
                "Native Landmarks & Sanctuary:\n" +
                "Home to multi-face Vapor-Weaver Lichen, Mist-Veil Ferns, bubbling geyser pools, and the uncommon Blue-Ice-domed Geothermal Vaporous Grotto.",
                "env_biomes", 1, new ItemStack(ModBlocks.STEAMING_ICE.get()),
                455f, 0.0f, a_env - 1.12f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_vapor_weaver_lichen", "Vapor-Weaver Lichen & Dampening", "ENVIRONMENT & NATURE",
                "Multi-face steam-spreading lichen, knockback steam puffs, Steam Vents, and footstep-silencing Potions of Dampening.",
                "Overview & Description:\n" +
                "Vapor-Weaver Lichen (Light Level 2) clings as a thin multi-face mist-grey and frost-blue web across ice-shelf cracks and volcanic stone, dotted with glowing amber spore specks.\n\n" +
                "Growth & Steam Defense:\n" +
                "▪ Spreads across adjacent rock and ice faces over time when within 2 blocks of a Steam Vent, Steaming Spring Stone, or water.\n" +
                "▪ Right-clicking unleashes a pressurized cloud of hot steam (15s cooldown) that pushes nearby creatures back 2 blocks and blinds hostile monsters in front for 3 seconds.\n\n" +
                "Spores, Vents & Potions:\n" +
                "▪ Breaking without Shears drops 1 Vapor Condenser Spore—used with Cobblestone to craft natural Steam Vent blocks or brewed into Potions of Dampening (entropica:dampening) to silence all player footsteps and vibrations.",
                "env_biome_steaming_skerries", 1, new ItemStack(ModItems.VAPOR_CONDENSER_SPORE.get()),
                545f, 0.0f, a_env - 1.07f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_vapor_bellows_crab_condenser", "Vapor-Bellows Crab & Condensers", "ENVIRONMENT & NATURE",
                "Thermal basalt-carapace crabs, Vapor Lift Mats, and water-producing 3D Thermal Vapor Condensers.",
                "Overview & Description:\n" +
                "Vapor-Bellows Crabs (48 HP) scuttle sideways beside hot springs, venting steam from their porous basalt chimneys. Standing on hot vents charges their shell for +50% damage reduction, while cold/freezing hits make them shed Porous Basalt Shells.\n\n" +
                "Pneumatic & Condenser Gear:\n" +
                "▪ Vapor Lift Mat: Crafted from Geyser Wiggle-Worm Vapor Vent Membranes; negates fall damage and lifts entities upward on a warm steam cushion.\n" +
                "▪ Thermal Vapor Condenser: Stepped 3D alembic vessel crafted from Vapor Condensate Crystals, a Porous Basalt Shell, Copper, and Glass. Placed beside steam blocks, it condenses rising vapor to fill adjacent Cauldrons (or empty Buckets) with clean water, and vents a fire-dousing, monster-blinding mist cloud on Redstone.",
                "env_biome_steaming_skerries", 1, new ItemStack(ModBlocks.VAPOR_CONDENSER.get()),
                545f, 0.0f, a_env - 1.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_grotto_structure_4", "Geothermal Vaporous Grotto", "ENVIRONMENT & NATURE",
                "Uncommon 20x20x18 Blue-Ice-domed thermal bath cavern with copper steam puzzle, Vapor Shroud Cloak, and 12m Steam Vent chimney.",
                "Overview & Description:\n" +
                "Structure 4 (20x20x18) is a pressurized Deepslate Brick and Steaming Spring Stone bathhouse sealed beneath a translucent Blue Ice greenhouse dome, split by an Amber Wood bridge between two steaming thermal pools.\n\n" +
                "Steam Puzzle, Cloak & Chimney Updraft:\n" +
                "▪ Copper Steam Puzzle: Three copper-marked pressure plates guard the central vault—stepping on decoy plates triggers blinding/heated steam traps, while the oxidized copper path reaches the treasure chest holding the Vapor Shroud Cloak (silences footsteps and grants Invisibility while sneaking).\n" +
                "▪ Steam Vent Chimney: Stepping onto the rear Steam Vent shrine launches explorers up the 12-block chimney updraft shaft to an upper ice-ledge alcove.",
                "env_biome_steaming_skerries", 1, new ItemStack(ModItems.VAPOR_SHROUD_CLOAK.get()),
                545f, 0.0f, a_env - 1.23f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_static_ice_plains", "2.7 Static Ice-Plains", "ENVIRONMENT & NATURE",
                "Wind-swept polar snowfields veined with Fulgurite Ice, lightning-split pines, and crackling cloud lightning.",
                "Overview & Description:\n" +
                "Biome #7 (entropica:static_ice_plains) spans flat inland polar snowfields threaded with glowing Fulgurite Ice sheets where blue static sparks leap across the ground beneath a static-charged grey-yellow sky (#6C664C) and intermittent cloud-to-cloud lightning arcs.\n\n" +
                "Static Charge Hazard & Materia:\n" +
                "▪ Primary Lightning & Secondary Glacial Materia: Favors Lightning (55%), Glacial (35%), and Aegis (10%) Grots alongside Cryo-Static Hedgehogs, Rime-Back Ovis, rare Patina Galvanic Ovis, and Glacial Frost-Cores.\n" +
                "▪ Metal-Armor Static Charge: Running for 30 seconds in Iron, Gold, or Chainmail armor builds static electricity and zaps you for 1 heart—unless you carry a Grounding Coil, step onto a Static-Conductive Mat, wear insulated/leather boots, or drink a Potion of Galvanic Surge.",
                "env_biomes", 1, new ItemStack(ModBlocks.FULGURITE_ICE.get()),
                495f, 0.0f, a_env - 1.38f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_fulgurite_ice_surge", "Fulgurite Ice, Pines & Galvanic Surge", "ENVIRONMENT & NATURE",
                "Redstone-emitting Fulgurite Ice, lightning-split Fulgurite Pines, Static Frost Powder, and Potions of Galvanic Surge.",
                "Overview & Description:\n" +
                "Sheets of translucent Fulgurite Ice (Light Level 4) output a steady Level 4 Redstone signal to adjacent wiring while arcing static sparks, and small clumps of bark-less Fulgurite Pine Logs and rime-dusted Fulgurite Pine Leaves dot the plains.\n\n" +
                "Powder & Brewing:\n" +
                "▪ Static Frost Powder: Dropped when breaking Fulgurite Ice without Silk Touch; used to re-form Fulgurite Ice and craft Biomass Capacitors and Grounding Coils.\n" +
                "▪ Potion of Galvanic Surge (entropica:galvanic_surge): Brewed from Static Frost Powder onto an Awkward Potion (extendable to 8m with Redstone); grants Speed I, Haste I, Static Charge shock immunity, and an electric-frost counter-shock when hit in melee.",
                "env_biome_static_ice_plains", 1, new ItemStack(ModItems.STATIC_FROST_POWDER.get()),
                590f, 0.0f, a_env - 1.31f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_hedgehog_capacitors", "Cryo-Static Hedgehog & Capacitors", "ENVIRONMENT & NATURE",
                "Spiky Cryo-Static Hedgehogs, Static-Conductive Mats, and adjustable-delay 3D Biomass Capacitors.",
                "Overview & Description:\n" +
                "Cryo-Static Hedgehogs (20 HP) skitter rapidly across snowfields and hide beneath Cryo-Static Shrubs. When bumped or hurt, they curl into a spiky static ball (+8 Armor, zapping attackers for 1.5 hearts plus freeze and Slowness II) and shed Static Quills.\n\n" +
                "Redstone & Grounding Crafts:\n" +
                "▪ Static-Conductive Mat: 2px pad woven from Static Quills, Copper, and Smooth Basalt; emits a Level 15 Redstone pulse underfoot while safely clearing metal-armor Static Charge.\n" +
                "▪ Biomass Capacitor: Stepped 3D Redstone battery & adjustable timer crafted from sheared Charged Static Needles, Copper, and Static Frost Powder; stores Redstone or lightning charge and fires a Level 15 pulse after 0.5s, 1s, 2s, or 4s (right-click to cycle).",
                "env_biome_static_ice_plains", 1, new ItemStack(ModBlocks.BIOMASS_CAPACITOR.get()),
                590f, 0.0f, a_env - 1.39f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_fulgurite_cryo_relay", "Fulgurite Cryo-Relay & Grounding Coil", "ENVIRONMENT & NATURE",
                "Uncommon 17x17x24 Faraday rotunda, 20m lightning spire, central Biomass Capacitor relay, and pocket Grounding Coil.",
                "Overview & Description:\n" +
                "Surrounded by a Fulgurite Ice moat and 4 copper/basalt Faraday coil pylons, this 17x17x24 sanctuary pairs an open-ribbed rotunda and 20-block storm needle with a trapped Underground Capacitor Vault.\n\n" +
                "Capacitor Relay & Relic:\n" +
                "▪ Storm-Needle Relay: An overhead inverted Lightning Rod channels atmospheric sparks down into the central Biomass Capacitor assembly flanked by Fulgurite Ice.\n" +
                "▪ Grounding Coil & Loot: Chests reward Lightning and Glacial Materia Essences & Ampoules, early-game starter supplies, and the Grounding Coil—a pocket relic that stops Static Charge shocks, cuts lightning damage by 50%, and unleashes stored static (+3 electric damage + Slowness II) on your next melee strike.",
                "env_biome_static_ice_plains", 1, new ItemStack(ModItems.GROUNDING_COIL.get()),
                590f, 0.0f, a_env - 1.47f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_barrow_mound_tundra", "2.8 Barrow-Mound Tundra", "ENVIRONMENT & NATURE",
                "Rolling snow and Necrotic Soil burial hills under bruised-purple skies, prowled by Barrow Hounds and Crypt-Glow Owls.",
                "Overview & Description:\n" +
                "Biome #8 (entropica:barrow_mound_tundra) spans rolling polar hills of Snow Blocks, crop-rejecting Necrotic Soil, and footstep-muffling Grave-Moss beneath an overcast purple sky (#3C2B4C), weathered burial cairns, and dead spruces.\n\n" +
                "Necrotic Cold & Regional Materia:\n" +
                "▪ Primary Barrow & Secondary Glacial Materia: Favors Barrow (55%), Glacial (35%), and Aegis (10%) Grots alongside Rime Shepherds, Barrow Hounds, and Barrow Crypt-Glow Owls.\n" +
                "▪ Necrotic Cold: Under the overcast barrow sky, Undead do not burn in daylight, gain passive Strength I, and chill living targets with brief Slowness on melee hits.",
                "env_biomes", 1, new ItemStack(ModBlocks.BARROW_STONE_BRICKS.get()),
                495f, 0.0f, a_env - 1.54f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_barrow_flora_ward", "Grave-Moss, Rime-Caps & Barrow Ward", "ENVIRONMENT & NATURE",
                "Stealthy Grave-Moss rugs, snaring Crypt-Grave Roots, freezing Rime-Cap Mushrooms, and Potions of Barrow Ward.",
                "Overview & Description:\n" +
                "Necrotic Soil blocks ordinary crops and saplings while allowing Barrow flora to thrive when fertilized with Bone Meal or Bleached Bone Meal:\n" +
                "▪ Grave-Moss: Thin 2px moss carpet (Light Level 1) that muffles footsteps and halves monster detection range (-50%) while moving, but drains hunger if you stand still for over 5 seconds.\n" +
                "▪ Crypt-Grave Root: Skeletal floor roots that snare non-undead trespassers with Slowness IV and Wither I; chop with an Axe for Crypt-Grave Fibers and Bleached Bone Meal.\n" +
                "▪ Rime-Cap Mushroom: Glowing frost mushroom (Light Level 6) that bursts in a 3x3 freezing spore cloud if trampled; drops throwable Rime-Cap Spores that brew Potions of Barrow Ward (Wither immunity, Resistance I, and -15% Undead damage).",
                "env_biome_barrow_mound_tundra", 1, new ItemStack(ModItems.RIME_CAP_SPORES.get()),
                590f, 0.0f, a_env - 1.54f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_barrow_fauna_greaves", "Barrow Hounds, Glow-Owls & Greaves", "ENVIRONMENT & NATURE",
                "Pack-hunting Barrow Hounds, twilight Crypt-Glow Owls, Barrow-Slate Greaves, and 3D tomb wards.",
                "Overview & Description:\n" +
                "▪ Barrow Hound (40 HP): Pack stalkers that gain +20% melee damage near unpurified Barrow masonry, howl to rally packmates, bite with Blindness, and drop Barrow Skull-Plates + 2-4x Average Barrow Essence—used to forge Barrow-Slate Greaves (root-snare & Grave-Moss immunity + -20% Undead damage) and 3D Barrow Sentinel Totems (repels minor Undead within 12 blocks).\n" +
                "▪ Barrow Crypt-Glow Owl (30 HP): Silent neutral flyers gliding between dead spruces and cairns; drop 2-3x Average Barrow Essence and drop/shed Grave-Glow Feathers to craft sculpted 3D Crypt-Siphon Lanterns (stops natural Undead spawns within 16 blocks).",
                "env_biome_barrow_mound_tundra", 1, new ItemStack(ModItems.BARROW_SLATE_GREAVES.get()),
                590f, 0.0f, a_env - 1.62f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_frost_tomb_wardens", "Frost-Tomb of the Aegis Wardens", "ENVIRONMENT & NATURE",
                "26x32x15 burial mound housing a Crypt-Siphon Lantern dais, corner Runed Braziers, and a hidden Warden's Vault.",
                "Overview & Description:\n" +
                "Structure 5 (entropica:frost_tomb_of_the_aegis_wardens) conceals a 3-sarcophagus Antechamber, a Central Crypt illuminated by a Crypt-Siphon Lantern on a raised Barrow Stone dais, and a breakable Cracked Barrow Brick wall hiding the Warden's Vault.\n\n" +
                "Crypt Wards & Vault Loot:\n" +
                "▪ Runed Braziers & Crypt-Siphon Lantern: Lighting the corner Runed Braziers with Flint and Steel bathes the crypt in protective Aegis flame alongside the central Crypt-Siphon Lantern.\n" +
                "▪ Warden's Vault: Breaking through the Cracked Barrow Brick wall behind the central dais reveals sealed burial chests stocked with Barrow and Glacial Materia Essences, Barrow-Slate Greaves, and ancient tomb treasures.",
                "env_biome_barrow_mound_tundra", 1, new ItemStack(ModBlocks.CRYPT_SIPHON_LANTERN.get()),
                590f, 0.0f, a_env - 1.70f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_null_fringe_bay", "2.9 Null-Fringe Bay", "ENVIRONMENT & NATURE",
                "Legendary low-gravity polar coastline of dark purple Void Sand, floating obsidian shards, and Void Sea Serpents.",
                "Overview & Description:\n" +
                "Biome #9 (entropica:null_fringe_bay) is a Legendary frozen coastline where Blue Ice shelves step down into dark purple Void Sand and frosted Void Sand Turf beaches bordering a deep indigo bay (#150E26 sky with overhead Void Rift Fractures, pulsing purple bay rings, and floating obsidian shards).\n\n" +
                "Low-Gravity Anomaly & Regional Materia:\n" +
                "▪ Primary Null-U & Secondary Glacial Materia: Favors Null-U (60%), Glacial (30%), and Void (10%) Grots alongside Void-Rift Shades and deep-water Void Sea Serpents.\n" +
                "▪ Weak Shoreline Gravity: Explorers naturally receive Jump Boost I and gentler 0.80x fall descent across the bay.",
                "env_biomes", 1, new ItemStack(ModBlocks.VOID_SAND.get()),
                495f, 0.0f, a_env - 1.72f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_void_crystals_buoyancy", "Void Sand, Crystals & Null-Buoyancy", "ENVIRONMENT & NATURE",
                "Void Glass smelting, 2-stage Void Crystals, Null-Fringe Tussocks, and Potions of Null-Buoyancy.",
                "Overview & Description:\n" +
                "▪ Void Sand & Void Glass: Gravity-falling dark purple Void Sand smelts directly into Entropica's Void Materia-tinted Void Glass (entropica:void_glass) and supports Void-Stalker Orchids and fall-cushioning Null-Fringe Tussocks (which drop Void Fiber for Void-Weave Fabric).\n" +
                "▪ Void Crystals & Brewing: Void Crystal Blocks sprout 2-stage Void Crystal Buds and Clusters mined for Void Crystal Shards—brewed onto an Awkward Potion to create Potions of Null-Buoyancy (Jump Boost II + Slow Falling).",
                "env_biome_null_fringe_bay", 1, new ItemStack(ModItems.VOID_CRYSTAL_SHARD.get()),
                590f, 0.0f, a_env - 1.78f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_rift_shade_spatial_gear", "Void-Rift Shades & Spatial Gear", "ENVIRONMENT & NATURE",
                "Void-Rift Shades, Spatial Phase Anchors, Phase Recall Stones, Leviathan Rift-Belts, and Null-Weave Mantles.",
                "Overview & Description:\n" +
                "▪ Void-Rift Shade (225 HP): Towering neutral Rift-Lurkers stalking the beaches and floating shards; blink through short rifts in combat, launch targets upward with clawed slashes, and drop Void Crystal Shards and 4-6x Strong Void Essence.\n" +
                "▪ Spatial Gear: Craft sculpted 3D Spatial Phase Anchors and attune a Phase Recall Stone to warp safely back from up to 128 blocks away; forge Leviathan Rift-Belts from Serpent Void Scales for a 7-block forward Rift Dash, and stitch Null-Weave Mantles for automatic Slow Falling, -50% fall damage, and immunity to random teleportation traps.",
                "env_biome_null_fringe_bay", 1, new ItemStack(ModItems.PHASE_RECALL_STONE.get()),
                590f, 0.0f, a_env - 1.86f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_null_fringe_rift_bastion", "Null-Fringe Rift Bastion", "ENVIRONMENT & NATURE",
                "Legendary 24x24x26 bay bastion with a submerged Void Glass vault, floating spiral steps, and a summit Spatial Phase Anchor.",
                "Overview & Description:\n" +
                "Spanning the Void Sand shoreline out into the bay, the Null-Fringe Rift Bastion pairs a dry underwater Void Glass Reliquary Vault with 4 crystal-crowned obsidian pylons, low-gravity floating stepping stones, a floating central Void Crystal focus, and an apex Spatial Phase Anchor.\n\n" +
                "Summit Spatial Phase Anchor & Vault:\n" +
                "▪ Ascend the spiral floating Void Stone steps past the glowing central Void Crystal focus to attune a Phase Recall Stone at the summit Spatial Phase Anchor, or dive into the submerged Void Glass vault for Null-U and Void Materia treasures.",
                "env_biome_null_fringe_bay", 1, new ItemStack(ModBlocks.SPATIAL_PHASE_ANCHOR.get()),
                590f, 0.0f, a_env - 1.94f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_biome_radiant_glaze_plains", "2.10 Radiant Glaze-Plains", "ENVIRONMENT & NATURE",
                "Rare warm-lit snow-glaze oasis with golden grass meadows, unfrozen Vitae Springs, and Crystalline Qilins.",
                "Overview & Description:\n" +
                "Biome #10 (entropica:radiant_glaze_plains) is a Rare polar sanctuary oasis where glowing Radiant Snow Blocks, golden-tinted meadows (#FACC48), and translucent Radiant Glaze-Ice surround warm, unfrozen Vitae Springs beneath an amber sky (#A67E44) and golden mist.\n\n" +
                "Sanctuary Warmth & Regional Materia:\n" +
                "▪ Primary Vitae & Secondary Aurora Materia: Favors Vitae (60%), Aurora (30%), and Glacial (10%) Grots alongside Crystalline Qilins, Amber Weeping Stags, Aurorafowl, and Luminoths.\n" +
                "▪ Zero Cold Hazard: Passively clears frostbite buildup and gently restores health over time.",
                "env_biomes", 1, new ItemStack(ModBlocks.RADIANT_SNOW_BLOCK.get()),
                495f, 0.0f, a_env - 2.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_radiant_springs_amber_oaks", "Vitae Springs, Glazed Oaks & Fertilizer", "ENVIRONMENT & NATURE",
                "Vitae Spring Stone, Glazed Amber Leaves, Golden Lichen, Vitae Fertilizer, and Potions of Radiant Vitality.",
                "Overview & Description:\n" +
                "▪ Vitae Springs & Glazed Oaks: Vitae Spring Stone keeps nearby water unfrozen and sparkling with golden bubbles, while Amber-Oak groves grow Glazed Amber Leaves and shearable Golden Lichen (blended with Bone Meal and Weeping Amber Tears into 3x3 crop-boosting Vitae Fertilizer).\n" +
                "▪ Liquid Vitae-Resin & Brewing: Right-click an adult Amber Weeping Stag with a Glass Bottle to siphon restorative Liquid Vitae-Resin—drinkable directly or brewed onto an Awkward Potion for Potions of Radiant Vitality (Regeneration + Health Boost).",
                "env_biome_radiant_glaze_plains", 1, new ItemStack(ModItems.LIQUID_VITAE_RESIN.get()),
                590f, 0.0f, a_env - 2.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_qilin_radiant_gear", "Crystalline Qilins & Prism Gear", "ENVIRONMENT & NATURE",
                "Crystalline Qilins, Jade Qilin Scales, Qilin Light-Refractors, Qilin Prism Circlets, and Sun-Glazed Talismans.",
                "Overview & Description:\n" +
                "▪ Crystalline Qilin (60 HP): Peaceful emerald-jade Glow Guardians that dazzle attackers with Light-Refraction Pulses (Blindness + Glowing), bless peaceful travellers with Regeneration, drop 2-4x Average Mirage Essence on defeat, and shed Jade Qilin Scales when offered Weeping Amber Tears or Golden Apples.\n" +
                "▪ Optical & Sanctuary Gear: Craft sculpted 3D Qilin Light-Refractors (daylight Redstone 0–15 + monster-revealing pulse), Qilin Prism Circlets (freeze-immune sunlit mending headgear), and reusable Sun-Glazed Talismans (8-block cleansing, healing, and gear-mending pulse).",
                "env_biome_radiant_glaze_plains", 1, new ItemStack(ModItems.QILIN_PRISM_CIRCLET.get()),
                590f, 0.0f, a_env - 2.10f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_radiant_spring_pavilion", "Radiant Spring Pavilion", "ENVIRONMENT & NATURE",
                "Core-free 22x22x18 warm fountain pavilion under a Radiant Glass dome with hidden underground reliquary chests.",
                "Overview & Description:\n" +
                "Crowning flat meadows across the Radiant Glaze-Plains, the Radiant Spring Pavilion frames a bubbling Smooth Quartz, Vitae Spring Stone, and Radiant Glaze-Ice fountain beneath 4 Golden Lichen-draped Amber Log columns and a Radiant Glass skylight dome.\n\n" +
                "Natural Fountain Blessing & Reliquary:\n" +
                "▪ Wading in the pavilion's warm spring water naturally clears Poison, Wither, Slowness, and frostbite while slowly mending worn gear durability, and a side stairwell leads down to two hidden reliquary chests.",
                "env_biome_radiant_glaze_plains", 1, new ItemStack(ModBlocks.QILIN_LIGHT_REFRACTOR.get()),
                590f, 0.0f, a_env - 2.18f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_storm_swept_icebergs", "2.11 Storm-Swept Icebergs", "ENVIRONMENT & NATURE",
                "Wind-carved deep-ocean icebergs, lightning-forged Fulgurite Ice & Fulgurite Blocks, Zephyr Ice bridges, and Zephyr-Glass Grass.",
                "Overview & Description:\n" +
                "Biome #11 (entropica:storm_swept_icebergs) rises out of deep navy polar seas (#1A2B48) beneath dark slate storm clouds (#2D3238), crackling overhead lightning arcs, and horizontal wind-driven spray. Towering, wind-sheared icebergs climb up to Y=107 with Blue Ice keels, Zephyr Ice overhangs, and snow-capped ledges.\n\n" +
                "Storm Lightning & Iceberg Materials:\n" +
                "▪ Primary Storm & Secondary Glacial Materia: Favors Storm (60%), Glacial (30%), and Aurora (10%) Grots.\n" +
                "▪ Storm Peak Strikes: Thunderstorms naturally strike high iceberg peaks, fusing ordinary ice into redstone-charged Fulgurite Ice and turning stone or gravel into dark glassy Fulgurite Blocks.\n" +
                "▪ Zephyr Ice & Zephyr-Glass Grass: Wind-infused Zephyr Ice gives travellers a brisk tailwind stride and freezes adjacent surface water into walkable ice bridges, while snow and Zephyr Ice ledges sprout translucent Zephyr-Glass Grass harvested for flexible Zephyr-Glass Fibers.",
                "env_biomes", 1, resolveBiome11Icon("zephyr_ice", ModBlocks.FULGURITE_ICE.isPresent() ? ModBlocks.FULGURITE_ICE.get().asItem() : Items.BLUE_ICE),
                495f, 0.0f, a_env - 2.34f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_vapor_observatory", "Auroral Vapor Observatory & Wind Tech", "ENVIRONMENT & NATURE",
                "Core-free Structure 6 dome observatory, Zephyr Updraft Grates, Simurgh Wind Turbines, gliders, horns, and Tempest Stride.",
                "Overview & Description:\n" +
                "Anchored atop iceberg crowns or floating Blue Ice keels, Structure 6 (Auroral Vapor Observatory) is a 22x22x32 core-free outpost featuring a Deepslate & Fulgurite engine room, a panoramic Silver Pine chart gallery with heated Auroral Lily-Pad planters, and a Cut-Copper & Zephyr-Ice dome housing a giant angled copper Sky-Telescope and apex Lightning Rod mast.\n\n" +
                "Wind Machinery, Relics & Alchemy:\n" +
                "▪ Zephyr Updraft Grate & Night Clarity: A central floor grate channels a vertical column of wind straight up the 3x3 shaft to the upper telescope deck, where clear night air above Y=74 grants Night Vision and Slow Falling.\n" +
                "▪ Simurgh Wind Turbine: Corner-mounted copper wind rotors spin faster at high elevation and during storms to output clean Redstone power.\n" +
                "▪ Observatory Gear & Brewing: Chests and Zephyr-Glass Fiber / Simurgh Feather crafting unlock the Storm-Kite Glider, gale-blasting Cryo-Tempest Horn, dash-boosting Tempest in a Bottle, and Potions of Tempest Stride (wind speed, higher leaps, and cushioned landings).",
                "env_storm_swept_icebergs", 1, resolveBiome11Icon("storm_kite_glider", ModBlocks.AURORAL_LENS_BLOCK.isPresent() ? ModBlocks.AURORAL_LENS_BLOCK.get().asItem() : Items.SPYGLASS),
                590f, 0.0f, a_env - 2.28f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_tempest_iceberg_fauna", "Tempest Iceberg Apex Fauna", "ENVIRONMENT & NATURE",
                "Swooping electric Storm Kites, frost-breathing Glacial Hydras, and weather-heralding Zephyr Simurghs.",
                "Overview & Description:\n" +
                "Three powerful creatures rule the sky currents and frozen ledges of Storm-Swept Icebergs:\n" +
                "▪ Storm Kite (210 HP): High-altitude hostile sky-rays that circle in storm clouds before diving down at high speed—delivering an extra lightning jolt whenever rain or thunder rages.\n" +
                "▪ Glacial Hydra (480 HP): Armored ice-shelf apex predators that slowly mend their health while standing on ice or snow and unleash cones of freezing breath alongside frost-slowing bites.\n" +
                "▪ Zephyr Simurgh (105 HP): Neutral copper-plumed wind raptors perching on high iceberg ledges. They screech ahead of approaching storms to warn travellers, bless friendly visitors who offer Copper Ingots or Rabbit with Speed and Slow Falling, and shed prized Simurgh Feathers.",
                "env_storm_swept_icebergs", 1, new ItemStack(ModItems.SIMURGH_FEATHER.isPresent() ? ModItems.SIMURGH_FEATHER.get() : Items.FEATHER),
                590f, 0.0f, a_env - 2.40f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_spore_glazed_pack", "2.12 Spore-Glazed Pack", "ENVIRONMENT & NATURE",
                "Sheltered polar bay pack-ice cushioned in Frozen Spore Moss, Spore-Glazed Ice, Giant Glaze-Cap Mushrooms, and Spore-Wood.",
                "Overview & Description:\n" +
                "Biome #12 (entropica:spore_glazed_pack) lines sheltered polar bays and coastal lagoon channels beneath a pale yellow-green sky (#6B7C50) and drifting golden spore mist. Thick pack-ice rafts of Blue Ice and glowing Spore-Glazed Ice carry spongy cushions of Frozen Spore Moss, crooked Spore-Wood trees, and towering Giant Frozen Mushrooms with pale mushroom-stem trunks and golden Frozen Glaze-Cap domes.\n\n" +
                "Spore & Glacial Materia Ecology:\n" +
                "▪ Primary Spore & Secondary Glacial Materia: Favors Spore (60%), Glacial (30%), and Overgrowth (10%) Grots.\n" +
                "▪ Frozen Spore Moss Hazard: Walking across Frozen Spore Moss without sneaking, wearing a Spore-Filter Mask, drinking Spore Clarity, or standing near a Cryo-Spore Censer stirs up a brief dizzying Nausea cloud.\n" +
                "▪ Spongy Spore-Wood & Mushroom Caps: Spore-Wood Logs craft into all-bark Spore-Wood or Spore-Wood Planks (stairs, slabs, fences, and 3 Sticks per 2 planks due to its soft fungal grain), while Frozen Glaze-Cap Blocks cushion 40% of fall damage.",
                "env_biomes", 1, resolveBiome11Icon("frozen_spore_moss", Items.MOSS_BLOCK),
                495f, 0.0f, a_env - 2.52f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_glazed_mycelial_conservatory", "Glazed Mycelial Conservatory & Spore Tech", "ENVIRONMENT & NATURE",
                "Core-free Spore-Wood & Spore-Glass biodome, Cryo-Spore Censers, Spore Bellows, Filter Masks, Poultices, and Spore Bombs.",
                "Overview & Description:\n" +
                "Anchored along pack-ice bays beside a Giant Frozen Mushroom, the Glazed Mycelial Conservatory (20x20x18) arches all-bark Spore-Wood ribs, Frozen Glaze-Cap panels, and green-tinted Spore Glass skylights over cultivated beds of Spore-Bearing Pitcher-Plumps and Spore-Burst Puffballs—with zero Shrine Cores or Anchors.\n\n" +
                "Air-Purifying Contraptions & Spore Alchemy:\n" +
                "▪ Cryo-Spore Censer: A copper and Spore Glass lantern (Light 13) that clears Nausea and Poison across a 12-block radius so bases and greenhouses stay clear-headed.\n" +
                "▪ Spore Bellows: Right-click or power with Redstone to puff a 5-block golden spore jet that boosts crop and moss growth while pushing back hostile monsters.\n" +
                "▪ Survival Gear & Brewing: Craft the Spore-Filter Mask (full Nausea immunity and halved Poison duration), Cryo-Spore Poultice (instant Poison/Nausea/frostbite cure + Regeneration II), throwable Glazed Spore-Pod Bombs, and Potions of Spore Clarity.",
                "env_spore_glazed_pack", 1, resolveBiome11Icon("cryo_spore_censer", Items.LANTERN),
                590f, 0.0f, a_env - 2.48f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_mycelial_spore_weaver", "Mycelial Spore-Weaver & Bay Fauna", "ENVIRONMENT & NATURE",
                "Shearable 40 HP fungal-wood quadrupeds, buoyant 24 HP Spore Drifters, and mossy 60 HP Overgrowth Ovis.",
                "Overview & Description:\n" +
                "Three symbiotic Spore-attuned creatures inhabit the cushioned floes of the Spore-Glazed Pack:\n" +
                "▪ Mycelial Spore-Weaver (40 HP): Gentle gnarled root-bark quadrupeds crowned with a glowing golden mushroom cap. Use Shears to harvest 1-2 Mycelial Spore Caps (which regrow faster as they graze Frozen Spore Moss); if attacked, they puff a defensive Nausea and Slowness spore cloud.\n" +
                "▪ Spore Drifter (24 HP): Buoyant glowing fungal floaters that drift above the pack-ice, seeding mushrooms and Spore-Burst Puffballs onto moss and snow while dropping bottled Living Spores.\n" +
                "▪ Overgrowth Ovis (60 HP): Sturdy moss-plated wild sheep sheared for Mossy Aegis Plates used in Spore-Filter Masks.",
                "env_spore_glazed_pack", 1, resolveBiome11Icon("mycelial_spore_cap", Items.RED_MUSHROOM),
                590f, 0.0f, a_env - 2.58f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_mirage_glacier", "2.13 Mirage Glacier", "ENVIRONMENT & NATURE",
                "Sun-baked desert-glacier arches of seamless Mirage Ice bordered by reddish Glass Sand dunes, Canyon-Strata Glass Sandstone, and Vitreous Cactus.",
                "Overview & Description:\n" +
                "Biome #13 (entropica:mirage_glacier) forms a rare inland desert-glacier borderland beneath a burnt terracotta sky (#A8592E) and shimmering amber dust haze. Sweeping arches of seamless Mirage Ice and Blue Ice rise out of reddish-amber Glass Sand dunes and layered Glass Sandstone cliffs dotted with crystal-needled Vitreous Cacti and glowing Shimmerpetals.\n\n" +
                "Mirage & Glacial Materia Hazards:\n" +
                "▪ Primary Mirage & Secondary Glacial Materia: Favors Mirage (60%), Glacial (30%), and Arid (10%) Grots.\n" +
                "▪ Phasic Mirage Ice & Immediate Suffocation: Mirage Ice smelts directly into Mirage Glass, toggles phase with Redstone, and phases open underfoot after a moment unless you sneak, wear Mirage-Veil Goggles, or drink Mirage Phasing. Getting your head trapped inside solid Mirage Ice deals immediate suffocation damage and triggers Double Vision!\n" +
                "▪ Double Vision & Midday Sun Glare: Unprotected explorers caught in Mirage Ice, Scarab solar flashes, or bright midday glacier glare suffer Double Vision—splitting crosshairs and ghosting nearby blocks and creatures into twin cyan and amber mirages until cured by Mirage-Veil Goggles or a Mirage Prism Lantern.",
                "env_biomes", 1, resolveBiome11Icon("mirage_ice", Items.BLUE_ICE),
                495f, 0.0f, a_env - 2.70f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_fata_morgana_sanctuary", "Fata Morgana Sanctuary & Mirage Optics", "ENVIRONMENT & NATURE",
                "Core-free Mirage Ice & Mirage Glass arch-sanctuary, Mirage Prism Lanterns, Mirage-Veil Goggles, Decoy Prisms, and Shimmering Focus.",
                "Overview & Description:\n" +
                "Bridging reddish Glass Sand dunes across a sunken mirror pool, the Fata Morgana Mirage Arch-Sanctuary (22x22x20) weaves seamless Mirage Ice, Mirage Glass, and Canyon-Strata Glass Sandstone into a soaring twin-ribbed arch—built with zero Shrine Cores or Anchors.\n\n" +
                "Refractive Lanterns, Optical Gear & Brewing:\n" +
                "▪ Mirage Prism Lantern: A sculpted bronze and Glass Sandstone prism lantern (Light 15) that clears Double Vision, Blindness, and Darkness across 16 blocks while cloaking sneaking players within 8 blocks.\n" +
                "▪ Mirage-Veil Goggles: Polarized Silica Glass Plate headgear granting full immunity to Double Vision, Blindness, and Darkness, solid footing across Mirage Ice without sneaking, and outlines on nearby invisible creatures.\n" +
                "▪ Mirage Decoy Prism, Shimmering Focus & Potions: Craft throwable Mirage Decoy Prisms (using Vitreous Needles + Shimmering Mirage Aura) to spawn aggro-drawing Fata afterimages and dazzle foes with Double Vision, channel the upgraded Shimmering Focus to blink backward out of danger, or brew Shimmering Mirage Auras into Potions of Mirage Phasing.",
                "env_mirage_glacier", 1, resolveBiome11Icon("mirage_prism_lantern", Items.LANTERN),
                590f, 0.0f, a_env - 2.66f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_silica_glass_scarab", "Silica Glass-Scarab & Mirage Fata-Sprites", "ENVIRONMENT & NATURE",
                "30 HP solar-focusing Silica Glass-Scarabs that shed Silica Glass Plates alongside 24 HP mirror-twin Mirage Fata-Sprites.",
                "Overview & Description:\n" +
                "Two refractive Mirage-attuned creatures thrive along the sun-baked ice and silica dunes of the Mirage Glacier:\n" +
                "▪ Silica Glass-Scarab (30 HP, 6 Armor): Neutral desert-glacier beetles clad in clay-amber chitin and translucent quartz wing-panes. Feeding Glass Sand, Sand, Quartz, or Amethyst Shards coaxes them to peacefully shed a Silica Glass Plate; if attacked, they angle their quartz wing-panes to flash attackers with fire damage and Double Vision.\n" +
                "▪ Mirage Fata-Sprite (24 HP): Hovering sun-stone and neon-cyan sylphs that focus daytime sunbeams onto optical collectors, condense Shimmering Mirage Auras, and split off mirror afterimage twins when struck.",
                "env_mirage_glacier", 1, resolveBiome11Icon("silica_glass_plate", Items.QUARTZ),
                590f, 0.0f, a_env - 2.76f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_oasis_floes", "2.14 Oasis Floes", "ENVIRONMENT & NATURE",
                "Warm unfrozen Amber Springs lined in Vitae Spring Stone, nestled within snowy Silver Pine woods and Frost-Draped Amber-Willows.",
                "Overview & Description:\n" +
                "Biome #14 (entropica:oasis_floes) carves warm-water thermal basins inside frozen Silver Pine valleys beneath a warm orange-yellow sky (#B57A3C) and clear humid mist. Steaming Amber Springs lined with Vitae Spring Stone and green grass margins stay completely ice-free year-round, shaded by resin-veined Amber-Wood weeping willows draped in Frost-Draped Willow Leaves and full-block Frost-Draped Willow Vines.\n\n" +
                "Oasis & Glacial Materia Ecology:\n" +
                "▪ Primary Oasis & Secondary Glacial Materia: Favors Oasis (60%), Glacial (30%), and Vapor (10%) Grots.\n" +
                "▪ Peaceful Thermal Sanctuary: Zero cold hazards—wading in the warm Amber Springs clears freezing chill and gently restores health while Auroral Lily-Pads and Amber Nectar Blossoms flourish along the grass banks.\n" +
                "▪ Amber Resin, Torches & Adhesive: Stripping Amber-Wood Logs yields Amber Chunks to craft fall-cushioning Blocks of Amber Resin, snow-melting Amber Torches (Light 14), and sticky Amber Adhesive (used in Sticky Pistons, Leads, and waxing Copper).",
                "env_biomes", 1, resolveBiome11Icon("frost_willow_leaves", Items.OAK_LEAVES),
                495f, 0.0f, a_env - 2.88f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_amber_willow_bathhouse", "Amber-Willow Bathhouse & Oasis Dew-Basin", "ENVIRONMENT & NATURE",
                "Core-free Amber-Wood & Oasis Glass thermal bathhouse, sculpted Oasis Dew-Basins, Bottled Amber Nectar, and Oasis Renewal.",
                "Overview & Description:\n" +
                "Nestled beside a weeping Amber-Willow, the Amber-Willow Thermal Bathhouse (22x22x18) shelters an unfrozen Vitae Spring Stone bath and Amber-Wood footbridge under Silver Pine eaves and an Oasis Glass skylight—built with zero Shrine Cores or Anchors.\n\n" +
                "Thermal Basins, Nectar Ampoules & Gear:\n" +
                "▪ Oasis Dew-Basin: A sculpted 3D Vitae Spring Stone, Amber-Wood, and Copper font (Light 12-14) that clears freezing chill, Poison, and Hunger across 10 blocks, keeps Farmland hydrated up to 6 blocks away in icy biomes, and steeps Amber Chunks + Glass Bottles into Bottled Amber Nectar.\n" +
                "▪ Bottled Amber Nectar & Oasis Renewal: Collected from Amber Nectar Blossoms or steeped in an Oasis Dew-Basin—drinkable for 4 hunger, frostbite relief, and 30s Absorption I, or brewed into Potions of Oasis Renewal.\n" +
                "▪ Salamander Wader Hood: Waterproof diving and thermal headgear granting +50% underwater breath duration and full polar-water chill immunity.",
                "env_oasis_floes", 1, resolveBiome11Icon("oasis_dew_basin", Items.CAULDRON),
                590f, 0.0f, a_env - 2.84f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_mud_lurker_salamander", "Mud-Lurker Salamander & Oasis Fauna", "ENVIRONMENT & NATURE",
                "36 HP amphibious Mud-Lurker Salamanders (Overworld Thermal-Mud & Nether Magma variants), 96 HP Amber-Weeping Stags, and 48 HP Vapor-Bellows Crabs.",
                "Overview & Description:\n" +
                "Warm spring amphibians and resin-antlered grazers gather around the steaming basins of Oasis Floes:\n" +
                "▪ Mud-Lurker Salamander (36 HP, 4 Armor): Neutral 41-cube amphibians in two regional variants—Overworld Thermal-Mud (basalt-pebble hide with burnt-orange geothermal dorsal plates, healing 1 HP/s in water or mud) and Nether Magma & Soul-Fire (fire-immune lava swimmers with cyan soul-fire dorsal plates). Offering Slimeballs, Kelp, Tropical Fish, Magma Cream, or Amber Chunks coaxes them to shed Waterproof Salamander Skin.\n" +
                "▪ Amber-Weeping Stag & Vapor-Bellows Crab: Crystalline-antlered stags (siphoned with a Glass Bottle for Liquid Vitae-Resin) and steam-venting crabs share the warm grass margins.",
                "env_oasis_floes", 1, resolveBiome11Icon("waterproof_salamander_skin", Items.SLIME_BALL),
                590f, 0.0f, a_env - 2.94f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_dust_crusted_bergs", "2.15 Dust-Crusted Bergs", "ENVIRONMENT & NATURE",
                "Flat-topped polar table-icebergs blanketed in weathered Grey Sand, Dust-Crusted Ice, and dry Wind-Blown Dust Scrub.",
                "Overview & Description:\n" +
                "Biome #15 (entropica:dust_crusted_bergs) forms dry, wind-scoured coastal table-icebergs beneath a dusty reddish-gold sky (#8A6B4E) and drifting grey silt haze. Massive flat-topped ice islands built from Blue Ice, Packed Ice, and Dust-Crusted Ice sit covered in sheets of weathered Grey Sand and Coarse Gravel without trees, snow layers, or grass.\n\n" +
                "Dust & Glacial Materia Ecology:\n" +
                "▪ Primary Dust & Secondary Glacial Materia: Favors Dust (60%), Glacial (30%), and Arid (10%) Grots.\n" +
                "▪ Dust-Crusted Ice & Grey Sand: Breaking Dust-Crusted Ice without Silk Touch crumbles the rime into 1-2x Grey Sand. Grey Sand smelts straight into Entropica's Dust Glass (entropica:dust_glass) and sifts into nuggets, Quartz, and rare Dust-Core Shards with a Copper Silt Sieve.\n" +
                "▪ Wind-Blown Dust Scrub, Dry Fibers & Tough Fiber Rope: Stepping through dry scrub rustles loudly and puffs 3s of Blindness unless your eyes are shielded, while storm gusts can roll scrub downwind. Harvesting yields coal-grade Dry Fibers (80s furnace fuel) woven into diamond-plaited Tough Fiber Rope—a free-hanging climbable line that pays out downward up to 32 blocks when right-clicked with more rope.",
                "env_biomes", 1, resolveBiome11Icon("dust_crusted_ice", Items.PACKED_ICE),
                495f, 0.0f, a_env - 3.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_silt_bound_berg_watch_pylon", "Berg Watch-Pylon, Wind-Baffle Lantern & Sieve", "ENVIRONMENT & NATURE",
                "Core-free table-berg shelter with cliff boarding ropes, brass-louvered Wind-Baffle Lanterns, Copper Silt Sieves, and Dust-Devil Ampoules.",
                "Overview & Description:\n" +
                "Perched atop a flat table-iceberg, the Silt-Bound Berg Watch-Pylon (20x20x22) combines exterior Tough Fiber Rope boarding lines down the sheer ice cliffs, an octagonal Mud Brick and Tuff Brick windbreak shelter with Dust Glass storm slits, a central rope shaft, and four Cut Copper wind-vane sails—built with zero Shrine Cores or Anchors.\n\n" +
                "Storm Beacons, Prospecting & Ampoules:\n" +
                "▪ Wind-Baffle Lantern: A sculpted 3D brass-louvered Copper, Tuff Brick, and Dust Glass beacon (Light 14) that clears dust Blindness and Darkness across 20 blocks while granting Zephyr Weight (-55% fall damage, +1 block jump boost, and dust-blindness immunity).\n" +
                "▪ Copper Silt Sieve: A clean riveted copper pan woven with Tough Fiber Rope (128 durability) that sifts Grey Sand, Sand, Red Sand, and Gravel into Flint, Clay, Iron/Gold/Copper nuggets, Quartz, Emeralds, and Dust-Core Shards.\n" +
                "▪ Dust-Devil Ampoule & Zephyr Weight: Corked Medium Ampoule holding a layered dust-gale vortex—uncork to ride an updraft with 8s of Zephyr Weight while knocking back and blinding nearby foes.",
                "env_dust_crusted_bergs", 1, resolveBiome11Icon("wind_baffle_lantern", Items.LANTERN),
                590f, 0.0f, a_env - 2.99f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_dust_core_golem", "Dust-Core Golem & Silt Sentinels", "ENVIRONMENT & NATURE",
                "60 HP hovering Dust-Core Golems (Polar Ash-Berg & Red Canyon variants) alongside rare Sandstone Golems.",
                "Overview & Description:\n" +
                "Elemental silt sentinels patrol the windswept plateaus of Dust-Crusted Bergs:\n" +
                "▪ Dust-Core Golem (60 HP): Neutral hovering redstone-silt cores orbited by 3 spinning stone shards in two regional variants—Polar Ash-Berg (frosted basalt-ash shards matching Grey Sand and Dust-Crusted Ice) and Red Canyon Silt (chiseled red sandstone shards). Provoking one triggers a 6.0-damage blinding dust blast; injured golems absorb nearby loose sand to heal (+6 HP), and hand-feeding sand at full health can coax them to compact and shed a Dust-Core Shard (which compacts 3x3 patches of loose sand into Sandstone or Tuff and brews Potions of Zephyr Weight).\n" +
                "▪ Sandstone Golem: Rare heavy 4-limbed sentinels occasionally share the widest sandy berg plateaus.",
                "env_dust_crusted_bergs", 1, resolveBiome11Icon("dust_core_shard", Items.REDSTONE),
                590f, 0.0f, a_env - 3.06f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_blood_ice_shallows", "2.16 Blood-Ice Shallows", "ENVIRONMENT & NATURE",
                "Shallow crimson-glaze polar bays with Sanguine Ice floes, Red Clay Sand seabeds, edible Red Algae mats, and Sanguine Lilies.",
                "Overview & Description:\n" +
                "Biome #16 (entropica:blood_ice_shallows) forms sheltered polar coastal bays beneath a dark rose twilight sky (#6A242B) and warm crimson sea mist (#8C353D) with zero passive cold hazard. Translucent shelves of Sanguine Ice float across shallow iron-crimson waters carpeted in Red Clay Sand, Red Algae mats, and floating Sanguine Lilies.\n\n" +
                "Blood & Glacial Materia Ecology:\n" +
                "▪ Primary Blood & Secondary Glacial Materia: Favors Blood (60%), Glacial (30%), and Vitae (10%) Grots.\n" +
                "▪ Sanguine Ice & Red Clay Sand: Sanguine Ice glows faintly (Light 3), drifts crimson mist upward, and chimes softly when walked on while injured; breaking it without Silk Touch or a Sanguine Ice-Lancet melts it into water. Red Clay Sand lines the bay floor, packs 2x2 into Sanguine Clay Bricks (with Stairs & Slabs), and smelts directly into Blood Glass (entropica:blood_glass).\n" +
                "▪ Red Algae: Coastal crimson dulse mats thriving in water or on Sanguine Ice. Directly edible (3 hunger, halves active Poison & Wither durations, and grants 6s Regeneration I), substitutes for Slimeballs in Sticky Pistons, Leads, and Magma Cream, and crafts Red Dye.",
                "env_biomes", 1, resolveBiome11Icon("sanguine_ice", Items.PACKED_ICE),
                495f, 0.0f, a_env - 3.16f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_crimson_tide_sanctum", "Crimson Tide Sanctum, Tide Lantern & Ice-Lancet", "ENVIRONMENT & NATURE",
                "Half-submerged Blood Glass & Sanguine Clay Brick tide-sanctum with Crimson Tide Lanterns, Sanguine Ice-Lancets, and Sanguine Draught Ampoules.",
                "Overview & Description:\n" +
                "Anchored at sea level in Blood-Ice Shallows, the Crimson Tide Sanctum (20x20x18) opens four tide-archways into a sunken crimson basin blooming with Sanguine Lilies and Red Algae beneath a Blood Glass oculus dome—built with zero Shrine Cores or Anchors.\n\n" +
                "Coastal Beacons, Blades & Ampoules:\n" +
                "▪ Crimson Tide Lantern: A sculpted 3D Deepslate Tile, Cut Copper, and Blood Glass coastal beacon (Light 15, waterloggable) that clears polar chill, Weakness, and Mining Fatigue across 16 blocks while granting Regeneration I to explorers in water or on Sanguine Ice.\n" +
                "▪ Sanguine Ice-Lancet: A fast crimson harvesting blade (192 durability) that automatically Silk-Touches all Ice blocks without needing Silk Touch, chills hit foes with Slowness I, and siphons 1.5 HP per melee strike.\n" +
                "▪ Sanguine Draught Ampoule & Sanguine Vigor: Uncorking restores 3 Hearts, clears Weakness & Wither, and grants 45s of Sanguine Vigor (+2 Max Hearts, freeze immunity, and melee life-steal).",
                "env_blood_ice_shallows", 1, resolveBiome11Icon("crimson_tide_lantern", Items.LANTERN),
                590f, 0.0f, a_env - 3.13f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_sanguine_sea_angel", "Sanguine Sea-Angel & Blood-Frost Darts", "ENVIRONMENT & NATURE",
                "36 HP hostile amphibious sea-angels that swim in bays, skim low over Sanguine Ice, spit Blood-Frost Darts, and drop Sanguine Heart-Gel.",
                "Overview & Description:\n" +
                "Shoals of predatory Sanguine Sea-Angels (36 HP, Polar Blood-Shallows & Abyssal Garnet variants) haunt the crimson bays:\n" +
                "▪ Amphibious Bay & Ice-Shelf Hunter: Translucent pteropods with a pulsing crimson heart visible inside their bell. They swim gracefully through bay water and skim low over Sanguine Ice shelves without ever beaching, chiming like glass when calm before splitting their head cowl open with a sharp snap and rhythmic heartbeat when hunting.\n" +
                "▪ Blood-Frost Darts & Siphon Lunge: Fires chilling Blood-Frost Darts at mid-range (4.0 damage + Slowness I + freeze buildup) and lunges headfirst up close to siphon 2.5 HP.\n" +
                "▪ Sanguine Heart-Gel: Dropped (1-2x) alongside Average Blood Essence; brews Potions of Sanguine Vigor and crafts Crimson Tide Lanterns, Sanguine Ice-Lancets, and Sanguine Draught Ampoules.",
                "env_blood_ice_shallows", 1, resolveBiome11Icon("sanguine_heart_gel", Items.MAGMA_CREAM),
                590f, 0.0f, a_env - 3.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_astral_skerries", "2.17 Astral Skerries", "ENVIRONMENT & NATURE",
                "Barren Starlight Ice & Astral Impactite island chains with 12-28 block ice needles, Stardust Spikes, and starlight-parallax waters.",
                "Overview & Description:\n" +
                "Biome #17 (entropica:astral_skerries) forms rare polar island chains beneath a deep starlight-indigo sky (#1C1A38), glowing purple volumetric mist (#32245C), drifting stardust motes, and starlight-parallax channels (#1B2854)—with zero trees, leaves, grass blocks, or environmental hazards.\n\n" +
                "Astral, Glacial & Aurora Materia Ecology:\n" +
                "▪ Primary Astral & Secondary Glacial Materia: Favors Astral (60%), Glacial (30%), and Aurora (10%) Grots alongside Aurora Jellies, Luminoths, and night Glacial Frost-Cores.\n" +
                "▪ Tall Spires & Stardust Crystals: Slender 12-28 block Starlight Ice Pillars and 10-20 block Stardust Spikes (built of Stardust Crystal Blocks and directional Stardust Crystal Clusters) soar from rocky Astral Impactite shelves. Mining clusters yields 2-4 Stardust Fragments (Fortune scaling).\n" +
                "▪ Rime-Star Tussock: Frost-silver coastal sedge thriving directly on ice and stone; brushing through it eases frost buildup and puffs stardust that outlines invisible creatures.",
                "env_biomes", 1, resolveBiome11Icon("starlight_ice", Items.BLUE_ICE),
                505f, 0.0f, a_env - 3.34f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_stardust_skerry_orrery", "Stardust Skerry Orrery, Orrery Lantern & Tuning Chisel", "ENVIRONMENT & NATURE",
                "Core-free Astral Glass & Mirror observatory with Celestial Orrery Lanterns, Starlight Tuning Chisels, and Starlight Draught Ampoules.",
                "Overview & Description:\n" +
                "Rising from a Starlight Ice and Astral Impactite skerry ringed by natural Stardust Spikes, the Stardust Skerry Orrery (20x20x22) aligns a sunken Astral Mirror floor dish directly beneath an open sighting slit in its Astral Glass and Astral Marble dome—built with zero Life-Force Cores or Anchors.\n\n" +
                "Observatory Beacons, Tools & Tonics:\n" +
                "▪ Celestial Orrery Lantern: A sculpted 3D Engraved Astral Slate and Astral Glass beacon (Light 15, standing or hanging, waterloggable) that clears Darkness and Blindness across 18 blocks, grants Starlight Grace, outlines invisible foes, and accelerates nearby budding Astral crystals at night.\n" +
                "▪ Starlight Tuning Chisel: A 256-durability Resonite-and-Stardust tool that self-repairs under night starlight, harvests crystal clusters for +1 bonus shard (or intact cluster while sneaking), cleanly harvests Starlight Ice, and chimes on Right-Click to ping nearby geodes and invisible creatures.\n" +
                "▪ Starlight Draught Ampoule & Starlight Grace: Restores 2 Hearts, clears Darkness, Blindness, Slowness, and frost buildup, and grants 60s of Starlight Grace (flicker-free Night Vision, +0.5 step height, -60% fall damage, and +12% movement speed; also brewed from Awkward Potion + Stardust Fragment).",
                "env_astral_skerries", 1, resolveBiome11Icon("celestial_orrery_lantern", Items.LANTERN),
                600f, 0.0f, a_env - 3.30f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_starlight_nautilus", "Starlight Nautilus, Barding & Astral Star-Fishing", "ENVIRONMENT & NATURE",
                "36 HP tameable 3D-flying & swimming Nautilus mount fed by 7 biome-exclusive cosmic fish species, supporting Saddles and Mount Armors.",
                "Overview & Description:\n" +
                "Pods of peaceful Starlight Nautiluses (36 HP) glide through the starry air and swim icy channels using the base Nautilus silhouette and jet-propulsion animations, clad in coiled Starlight spiral chambers and Engraved Astral Slate rims.\n\n" +
                "Biome-Only Star-Fishing, Saddle Flight & Barding:\n" +
                "▪ 7 Astral Skerries Star Fish: Fishing exclusively in Astral Skerries catches Nebula Darters, Starwing Flying-Fish, Starfall Rays, Comet Ribbonfish, Zenith Moonfish, Supernova Puffers, and rare nighttime Astral Lanternfish. Every raw fish tames, heals, breeds, or buffs the Starlight Nautilus (while 5 species cook into Starlight-infused meals).\n" +
                "▪ Saddle 3D Flight & Star-Jet Dash: Equip a Saddle on a tamed Starlight Nautilus for full 3D air flight and underwater swimming (with rider Water Breathing underwater and a chargeable Spacebar Star-Jet Dash).\n" +
                "▪ Mount Armors & Shears: Supports Leather, Copper, Iron, Gold, Diamond, and Netherite mount armors plus craftable Astral Slate Nautilus Armor (+12 Armor, +3.5 Toughness, +35% Knockback Resistance, and night shell regen). Right-click with Shears to remove armor first, then the saddle.",
                "env_astral_skerries", 1, resolveBiome11Icon("astral_nautilus_armor", Items.NAUTILUS_SHELL),
                600f, 0.0f, a_env - 3.38f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_flora", "Elemental Flora & Trees", "ENVIRONMENT & NATURE",
                "Materia-attuned wildflowers, hanging roots, defensive brambles, fungal shelf-caps, and elemental timber.",
                "Overview & Description:\n" +
                "Across Entropica's six climatic zones, plant life adapts directly to regional Materia streams—producing luminous petals, reactive defensive thorns, alchemical nectars, and specialized timber.\n\n" +
                "Origin & Obtaining:\n" +
                "Harvested carefully with Shears or Glass Bottles across Overworld, Cave, and Nether biomes for Crucible Materia extraction, brewing, dyes, and protective linings.",
                "hub_environment", 0, new ItemStack(ModBlocks.RIMEBLOOM.get()),
                280f, 0.0f, a_env, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_glacial_heart_root", "Glacial Heart-Root & Cryo-Grenades", "ENVIRONMENT & NATURE",
                "Water-freezing hanging roots harvested carefully with Shears or Axes and crafted into throwable Cryo-Grenades.",
                "Overview & Description:\n" +
                "Dense, deep-blue hanging root tendrils saturated with pure Glacial Materia that cling beneath Blue-Glacier Shelf overhangs, Silent Fjord basalt cliffs, and Runic Spire archways—gradually freezing open surface water directly below into ice and chilling anything that brushes against them (Slowness IV + frostbite).\n\n" +
                "Harvesting Hazard & Drops:\n" +
                "▪ Shears, Silk Touch, or Axe: Safely harvests Glacial Heart-Roots (breaking bare-handed shatters the sub-zero tendrils for 3 hearts of freezing damage!).\n\n" +
                "Throwable Cryo-Grenades:\n" +
                "▪ Crafted from Glacial Heart-Root / Frost-Core reagents, Snowballs, Frozen Moss Clumps, and Gunpowder (yields 4x Cryo-Grenades). Right-click to hurl a cryo-sphere that freezes nearby surface water into Ice, douses flames, and blasts foes within 3.5 blocks with frost damage, Slowness III, and deep freeze buildup.",
                "env_flora", 0, new ItemStack(ModItems.CRYO_GRENADE.get()),
                420f, 0.0f, a_env + 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_shielding_bramble", "Shielding Bramble & Thorned Wire", "ENVIRONMENT & NATURE",
                "Blast-resistant metallic silver shelf vines sheared for Spiked Armor Linings and defensive Thorned Wire.",
                "Overview & Description:\n" +
                "A rigid, geometric silver bramble infused with Aegis Materia that crawls along exposed frozen coastal ledges, shrugging off explosions and pricking careless intruders.\n\n" +
                "Origin & Obtaining:\n" +
                "Grows naturally along Blue-Glacier Shelf edges (Light Level 1, high blast resistance). Harvest with Shears to collect 1-2 Shielding Thorn-Fibers.\n\n" +
                "Crafting Uses:\n" +
                "▪ Thorned Wire: Woven from 3 Shielding Thorn-Fibers and 2 Iron Nuggets into blast-resistant defensive wire coils that inflict Slowness II and 1.5 piercing damage on monsters moving through them.\n" +
                "▪ Spiked Armor Lining: Stitched from 4 Shielding Thorn-Fibers and Leather; deals automatic 2.0 thorns retaliation damage back to melee attackers while carried.",
                "env_flora", 1, new ItemStack(ModBlocks.SHIELDING_BRAMBLE.get()),
                420f, 0.0f, a_env - 0.04f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_auroral_feather_moss", "Auroral Feather-Moss & Auroral Lanterns", "ENVIRONMENT & NATURE",
                "Color-shifting bioluminescent moss carpets and Light Level 15 monster-warding Auroral Lanterns.",
                "Overview & Description:\n" +
                "Feathery bioluminescent moss carpets (Light Level 9) that drape Coarse Gravel islets and Glimmer-Ice banks across the Auroral Skerries, shimmering in emerald, magenta, and cyan Aurora Materia hues.\n\n" +
                "Origin & Obtaining:\n" +
                "Harvest intact with Shears, or break by hand to gather 1-2x Glowing Auroral Fibers.\n\n" +
                "Crafting Uses:\n" +
                "▪ Auroral Lantern (Light Level 15): Framed from 8 Iron Nuggets around a Glowing Auroral Fiber. Casts full aurora radiance (standing or hanging) and establishes a 16-block warding aura that blocks natural hostile monster spawns.",
                "env_flora", 1, new ItemStack(ModBlocks.AURORAL_FEATHER_MOSS.get()),
                495f, 0.0f, a_env - 0.14f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_mist_veil_fern", "Mist-Veil Fern & Camouflage Cloak", "ENVIRONMENT & NATURE",
                "Silver-fronded geothermal ferns with warm peach hearts woven into stealth-granting Camouflage Cloaks.",
                "Overview & Description:\n" +
                "Translucent silver ferns (Light Level 4) with a warm glowing peach heart that flourish amid rising geothermal steam vents on Coarse Gravel, Basalt, and Glimmer-Ice.\n\n" +
                "Origin & Obtaining:\n" +
                "Harvest intact with Shears (also displayable in Flower Pots), or break normally for 1x Mist-Veil Frond.\n\n" +
                "Crafting Uses:\n" +
                "▪ Camouflage Cloak: Woven from 4 Mist-Veil Fronds, 2 Leather, and String. When worn on the chest or held in the offhand, it cuts hostile monster detection range in half (-50%) and wraps you in warm mist Invisibility while sneaking.",
                "env_flora", 1, new ItemStack(ModBlocks.MIST_VEIL_FERN.get()),
                475f, 0.0f, a_env + 0.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_auroral_lily_pad", "Auroral Lily-Pad & Thermal Heat Lamp", "ENVIRONMENT & NATURE",
                "Warm glowing water lilies that keep channels ice-free and fuel 10-block snow-melting Thermal Heat Lamps.",
                "Overview & Description:\n" +
                "Floating geothermal water lilies (Light Level 11) native to the steaming channels of the Auroral Skerries that gently warm the water beneath them, melting surface ice within a 3-block radius.\n\n" +
                "Origin & Obtaining:\n" +
                "Collect intact with Shears, or harvest without Shears for 1x Warm Aurora Petal.\n\n" +
                "Crafting Uses:\n" +
                "▪ Thermal Heat Lamp (Light Level 14): Built from Copper Ingots, Glass, Redstone, and a Warm Aurora Petal. Passively (and on right-click thermal pulses) melts snow layers and surface ice into water across a 10-block radius while clearing freezing chill on nearby players.",
                "env_flora", 1, new ItemStack(ModBlocks.AURORAL_LILY_PAD.get()),
                475f, 0.0f, a_env + 0.14f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_spore_cannon", "Spore-Cannon Puffball", "ENVIRONMENT & NATURE",
                "Turret puffball launching toxic spore projectiles.",
                "Overview & Description:\n" +
                "A crimson-purple puffball stalk with a glowing crater orifice that targets non-Nether entities.\n\n" +
                "Special Properties:\n" +
                "Scans an 8-block radius and launches Toxic Spore Projectiles dealing 2.0 HP damage, Poison II, and Nausea.",
                "env_flora", 1, new ItemStack(ModBlocks.SPORE_CANNON_PUFFBALL.get()),
                370f, 0.0f, a_env + 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_pyre_thorn_launcher", "Pyre-Thorn Launcher", "ENVIRONMENT & NATURE",
                "Prickly magma stalk firing rapid ignited thorns.",
                "Overview & Description:\n" +
                "A lava-veined stalk topped with a magma pod that fires 3-round burst volleys of Ignited Magma Thorns.\n\n" +
                "Special Properties:\n" +
                "Scans a 10-block radius, dealing 1.5 HP piercing damage per thorn and setting targets on fire for 3s.",
                "env_flora", 1, new ItemStack(ModBlocks.PYRE_THORN_LAUNCHER.get()),
                370f, 0.0f, a_env + 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_spore_pitcher_plump", "Spore-Bearing Pitcher-Plump", "ENVIRONMENT & NATURE",
                "Lime-green pitcher plant producing acidic spore nectar.",
                "Overview & Description:\n" +
                "Bulbous acidic pitcher plant that squirts acid fluid on contact, damaging armor durability and inflicting Poison II & Nausea.\n\n" +
                "Crafting & Harvesting:\n" +
                "Right-click with a Glass Bottle to harvest Acidic Spore Nectar.",
                "env_flora", 1, new ItemStack(ModBlocks.SPORE_BEARING_PITCHER_PLUMP.get()),
                370f, 0.0f, a_env + 0.35f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_blood_tendril_bramble", "Blood-Tendril Bramble", "ENVIRONMENT & NATURE",
                "Sinister thorn vine cluster inflicting Sanguine Bleeding.",
                "Overview & Description:\n" +
                "Crimson-black thorn vine cluster that ensnares entities with Slowness III, deals 1.0 HP prick damage, and inflicts Sanguine Bleeding.",
                "env_flora", 1, new ItemStack(ModBlocks.BLOOD_TENDRIL_BRAMBLE.get()),
                370f, 0.0f, a_env + 0.45f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_soot_veil_blight_cap", "Soot-Veil Blight-Cap", "ENVIRONMENT & NATURE",
                "Volatile ash mushroom erupting in soot clouds.",
                "Overview & Description:\n" +
                "Glowing violet-purple ash mushroom that explodes into a 3-block soot spore cloud when touched or broken without Shears.",
                "env_flora", 1, new ItemStack(ModBlocks.SOOT_VEIL_BLIGHT_CAP.get()),
                370f, 0.0f, a_env - 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_magma_grip_tendrils", "Magma-Grip Tendrils", "ENVIRONMENT & NATURE",
                "Fiery-orange magma lichen trapping feet.",
                "Overview & Description:\n" +
                "Fiery magma lichen vines that trap feet with Slowness IV, ignite entities for 5s, and deal thermal fire damage.",
                "env_flora", 1, new ItemStack(ModBlocks.MAGMA_GRIP_TENDRILS.get()),
                370f, 0.0f, a_env - 0.35f, false
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
                "Bioluminescent crystalline flowers emitting ambient Materia light and dynamic essence tinting.",
                "Overview & Description:\n" +
                "A radiant 2-part crystalline lotus flower featuring a natural green stem and a glowing blossom head attuned to ambient Materia streams.\n\n" +
                "Origin & Obtaining:\n" +
                "Grows naturally near geothermal crystal geodes and lush forest glades, or harvested with shears.\n\n" +
                "Crafting Uses:\n" +
                "Extracted in Crucibles to produce Weak Viscanite Essence, or used in dye transmutations.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 10 and dynamically tints its blossom head according to the ambient Materia / essence stream of the surrounding biome (Nether, Void, Frozen, Water, Arid, Undead, Air, or dynamic spectrum shimmer).",
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
                "env_amber_wood", "Amber-Wood Wood Set", "ENVIRONMENT & NATURE",
                "Resinous trees with glowing amber resin seams and honeyed orange planks.",
                "Overview & Description:\n" +
                "A warm arboreal species featuring dark chestnut bark coated in glowing golden amber resin seams.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Amber Glades and Amber Pine-Vales, or grown from Amber Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Crafted into sticky golden amber planks, log posts, stripped timber frames, and redstone adhesive recipes.\n\n" +
                "Special Properties:\n" +
                "Logs emit glowing golden resin seams (`#F59E0B`, `#FDE047`) and concentric honey rings.",
                "env_flora", 0, new ItemStack(ModBlocks.AMBER_LOG.get()),
                380f, 0.0f, a_env - 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_pyre_ash_cedar", "Pyre-Ash Cedar Wood Set", "ENVIRONMENT & NATURE",
                "Volcanic tree species with charred dark spruce bark and glowing cinder-orange interior wood.",
                "Overview & Description:\n" +
                "A volcanic tree species native to basalt deltas and pyre slopes in Entropica. Its charred bark shelters a blazing cinder-orange interior that resists extreme heat.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in basalt deltas and volcanic pyre slopes, or grown from Pyre-Ash Cedar Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Yields charred timber frames, volcanic planks, stairs, slabs, vertical slabs, and fences.",
                "env_flora", 0, new ItemStack(ModBlocks.PYRE_ASH_CEDAR_LOG.get()),
                390f, 0.0f, a_env - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_abyssal_spore_cypress", "Abyssal Spore-Cypress Wood Set", "ENVIRONMENT & NATURE",
                "Subterranean fungal-tree species with dark indigo bark lined with bioluminescent cyan spore nodes.",
                "Overview & Description:\n" +
                "A subterranean fungal-tree species thriving in deep abyssal mycelial caverns. Its dark indigo bark is lined with bioluminescent cyan spore gills.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in abyssal mycelial caverns, or grown from Abyssal Spore-Cypress Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Yields bioluminescent cyan spore planks, timber frames, stairs, slabs, vertical slabs, and fences.",
                "env_flora", 0, new ItemStack(ModBlocks.ABYSSAL_SPORE_CYPRESS_LOG.get()),
                390f, 0.0f, a_env - 0.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_starlight_aether_birch", "Starlight Aether-Birch Wood Set", "ENVIRONMENT & NATURE",
                "Ethereal celestial birch species with shimmering white-gold bark and dark golden-amber growth rings.",
                "Overview & Description:\n" +
                "An ethereal celestial birch species indigenous to high-altitude Veil glades. Its shimmering white-gold bark conducts ambient Materia streams.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Veil glades and starlight ridges, or grown from Starlight Aether-Birch Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Yields white-gold celestial planks, amber ring timber frames, stairs, slabs, vertical slabs, and fences.",
                "env_flora", 0, new ItemStack(ModBlocks.STARLIGHT_AETHER_BIRCH_LOG.get()),
                390f, 0.0f, a_env - 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_blood_root_iron_oak", "Blood-Root Iron-Oak Wood Set", "ENVIRONMENT & NATURE",
                "Sinister iron-hard tree species growing in deadlands with crimson-black bark and ironwood heartwood.",
                "Overview & Description:\n" +
                "A sinister, iron-hard tree species growing in deadlands and blood-soaked soils. Its twisted crimson-black bark bleeds thick vital sap and forms ironwood timber frames.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in deadlands and blood-soaked soils, or grown from Blood-Root Iron-Oak Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Yields heavy ironwood planks, blood-root timber frames, stairs, slabs, vertical slabs, and fences.",
                "env_flora", 0, new ItemStack(ModBlocks.BLOOD_ROOT_IRON_OAK_LOG.get()),
                390f, 0.0f, a_env - 0.30f, false
        ));


        ALL_NODES.add(new CodexNode(
                "env_rimebloom", "Frostbite Blossom (Rimebloom)", "ENVIRONMENT & NATURE",
                "Crystalline 3-petaled ice tulip flowers pulsing ambient cold luminescence across glacial shelves and decaying floes.",
                "Overview & Description:\n" +
                "A rare glacial wildflower forming a 3-petaled ice tulip cup with a glowing central crystal core.\n\n" +
                "Origin & Obtaining:\n" +
                "Found growing in Blue-Glacier Shelf crevasses, Crystalline Pack-Ice, and as stunted clusters atop the decaying ice packs of Frost-Bite Floes. Harvest intact with Shears.\n\n" +
                "Crafting Uses:\n" +
                "Harvested without Shears for Rimebloom Petals—used to craft 2x Rimebloom Dye (convertible to Light Blue Dye), brew Potions of Frost Resistance, and extract Glacial Materia in Crucibles.\n\n" +
                "Special Properties:\n" +
                "Thrives directly on Blue Ice, Packed Ice, Ice, Snow Blocks, Rotten Ice, and Blighted Packed Ice; emits Light Level 3 and ambient frost mist particles that slow intruders.",
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
                "Low charcoal-wood polar shrub covered in glowing cyan static-ice needles that shear for Charged Static Needles.",
                "Overview & Description:\n" +
                "A low, gnarled shrub of charcoal-black wood bristling with fine needles of glowing cyan static ice that crackle across wind-swept polar plains.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally on Snow, Packed Ice, Blue Ice, Fulgurite Ice, and copper-rich stone in Static Ice-Plains, and spreads onto nearby snow/ice when lightning strikes within 6 blocks.\n\n" +
                "Crafting Uses:\n" +
                "Sheared for 1-2 Charged Static Needles to build adjustable-delay Biomass Capacitors, or harvested for Cryo-Static Twigs and Leaves for Cryo-Static Spectral Dye.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 5 and static sparkles. Stepping inside inflicts chill, Slowness I, and a light 0.5-heart shock (unless grounded) while charging your next melee swing for +2 bonus lightning damage; breaking without Shears or Silk Touch discharges for 2 hearts of shock.",
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
                "Crystalline azure-and-silver orchid blooming on Crystalline Pack-Ice snowfields and granting a 4-block Resistance I aura.",
                "Overview & Description:\n" +
                "A crystalline orchid featuring layered azure petals with metallic silver rims, occurring in both standard and 2-block tall spires.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally across snowfields and ice in Crystalline Pack-Ice, as well as Crystal Crags and Metallic Mountain Peaks.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Aegis-Spire Petals used to craft lifesaving Aegis Shielding Charms, brew Potions of Iron Skin, and extract Aegis Materia.\n\n" +
                "Special Properties:\n" +
                "Thrives directly on Snow Blocks, Packed Ice, Blue Ice, Glimmer-Ice, and Aegis Quartz; emits Light Level 7 and grants a passive 4-block Resistance I aura to players and friendly creatures.",
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
                "Emits Light Level 6 and green spore particles.",
                "env_flora", 0, new ItemStack(ModBlocks.CINDER_SPORE_MUSHROOM.get()),
                940f, 0.0f, a_env + 0.96f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_cinder_grip_lichen", "Cinder-Grip Lichen", "ENVIRONMENT & NATURE",
                "Volcanic ember-veined crust lichen providing speed boosts and thermal embers.",
                "Overview & Description:\n" +
                "A multi-directional crust lichen pulsing with volcanic orange ember veins.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally across basalt deltas, netherrack, and blackstone.\n\n" +
                "Crafting Uses:\n" +
                "Sheared for Cinder Lichen Flakes used in speed & fire resistance brews.\n\n" +
                "Special Properties:\n" +
                "Grants Speed I for 3s; burns bare feet for 0.5 fire damage.",
                "env_flora", 0, new ItemStack(ModItems.CINDER_GRIP_LICHEN_ITEM.get()),
                980f, 0.0f, a_env + 1.00f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_soot_shroud_fungi", "Soot-Shroud Fungi", "ENVIRONMENT & NATURE",
                "Jet-black parasol mushroom with glowing sulfur spots and ash cloud spores.",
                "Overview & Description:\n" +
                "A dark violet-grey parasol mushroom with glowing sulfur-yellow spots and ember gills.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally on netherrack, soul soil, and basalt.\n\n" +
                "Crafting Uses:\n" +
                "Sheared for Soot-Shroud Caps used as gunpowder substitute and brewing.\n\n" +
                "Special Properties:\n" +
                "Releases sulfuric ash clouds inflicting Nausea I and Blindness I.",
                "env_flora", 0, new ItemStack(ModBlocks.SOOT_SHROUD_FUNGI.get()),
                1020f, 0.0f, a_env + 1.04f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_pyrocyst_algae", "Pyrocyst Algae", "ENVIRONMENT & NATURE",
                "Fire-proof thermal carpet algae filled with bioluminescent fluid vesicles.",
                "Overview & Description:\n" +
                "A bioluminescent crimson thermal algae carpet studded with glowing fluid bladders.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns on magma blocks and nether stone near lava lakes.\n\n" +
                "Crafting Uses:\n" +
                "Right-click harvested for Pyrocyst Vesicles (5-min regrowth cooldown).\n\n" +
                "Special Properties:\n" +
                "100% fire-proof; extinguishes burning players & grants Fire Resistance I for 10s.",
                "env_flora", 0, new ItemStack(ModBlocks.PYROCYST_ALGAE.get()),
                1060f, 0.0f, a_env + 1.08f, false
        ));

        // 9 Fungal Shelf-Caps Nodes
        ALL_NODES.add(new CodexNode(
                "env_barrow_fungal_shelf_cap", "Barrow Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Tomb Emerald bracket mushroom extending from wall faces. Grants Resistance I on contact.",
                "Overview:\nTomb Emerald bracket shelf mushroom attached to wall faces.\n\nProperties:\nGrants Resistance I for 4s on contact. Sheared for Barrow Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.BARROW_FUNGAL_SHELF_CAP.get()), 1080f, 0.0f, a_env + 1.12f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_spore_fungal_shelf_cap", "Spore Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Overgrowth spore-green shelf cap sprinkled with gold spore dust. Grants Regeneration I on contact.",
                "Overview:\nLush overgrowth spore-green bracket shelf mushroom.\n\nProperties:\nGrants Regeneration I for 4s on contact. Sheared for Spore Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.SPORE_FUNGAL_SHELF_CAP.get()), 1100f, 0.0f, a_env + 1.16f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_blight_fungal_shelf_cap", "Blight Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Necrotic grey-green shelf cap with toxic green vein networks. Inflicts Nausea & Poison on contact.",
                "Overview:\nNecrotic grey-green bracket mushroom laced with toxic green veins.\n\nProperties:\nInflicts Nausea & Poison on contact. Sheared for Blight Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.BLIGHT_FUNGAL_SHELF_CAP.get()), 1120f, 0.0f, a_env + 1.20f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_frost_fungal_shelf_cap", "Frost Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Glacial ice-blue bracket shelf emitting frost mist. Inflicts Slowness I on contact.",
                "Overview:\nGlacial ice-blue bracket shelf mushroom.\n\nProperties:\nInflicts Slowness I for 4s on contact. Sheared for Frost Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.FROST_FUNGAL_SHELF_CAP.get()), 1140f, 0.0f, a_env + 1.24f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_cinder_fungal_shelf_cap", "Cinder Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Volcanic ember-orange bracket shelf. Extinguishes fire and grants Fire Resistance I on contact.",
                "Overview:\nVolcanic ember-orange bracket shelf mushroom.\n\nProperties:\nExtinguishes fire & grants Fire Resistance I for 6s. Sheared for Cinder Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.CINDER_FUNGAL_SHELF_CAP.get()), 1160f, 0.0f, a_env + 1.28f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_astral_fungal_shelf_cap", "Astral Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Stardust indigo bracket shelf. Grants Jump Boost II on contact.",
                "Overview:\nStardust indigo-violet bracket shelf mushroom.\n\nProperties:\nGrants Jump Boost II for 4s on contact. Sheared for Astral Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.ASTRAL_FUNGAL_SHELF_CAP.get()), 1180f, 0.0f, a_env + 1.32f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_dawn_fungal_shelf_cap", "Dawn Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Sunburst gold bracket shelf emitting Light Level 12. Cleanses debuffs on contact.",
                "Overview:\nSunburst gold bracket shelf mushroom emitting Light Level 12.\n\nProperties:\nCleanses debuffs & grants Glowing for 6s on contact. Sheared for Dawn Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.DAWN_FUNGAL_SHELF_CAP.get()), 1200f, 0.0f, a_env + 1.36f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_sanguine_fungal_shelf_cap", "Sanguine Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Crimson blood bracket shelf. Drains 0.5 heart on contact but grants Strength I.",
                "Overview:\nDeep crimson red velvety bracket shelf mushroom.\n\nProperties:\nDrains 0.5 heart on contact but grants Strength I for 4s. Sheared for Sanguine Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.SANGUINE_FUNGAL_SHELF_CAP.get()), 1220f, 0.0f, a_env + 1.40f, false
        ));
        ALL_NODES.add(new CodexNode(
                "env_static_fungal_shelf_cap", "Static Fungal Shelf-Cap", "ENVIRONMENT & NATURE",
                "Electric cyan bracket shelf emitting sparks. Delivers a 0.5 lightning zap and grants Speed II.",
                "Overview:\nElectric cyan bracket shelf mushroom emitting sparks.\n\nProperties:\nDelivers a 0.5 lightning zap & grants Speed II for 3s. Sheared for Static Fungal Caps.",
                "env_flora", 0, new ItemStack(ModBlocks.STATIC_FUNGAL_SHELF_CAP.get()), 1240f, 0.0f, a_env + 1.44f, false
        ));



        
        ALL_NODES.add(new CodexNode(
                "env_materia_echo_tree", "Materia-Echo Tree & Echo Fruit", "ENVIRONMENT & NATURE",
                "Ghostly ash-wood tree bearing climate-attuned Echo Fruits hanging beneath leaves.",
                "Overview & Description:\n" +
                "The Materia-Echo Tree is an ethereal pale ash-wood species that bears hanging Echo Fruits beneath its leaves.\n\n" +
                "Origin & Obtaining:\n" +
                "Grown from Materia-Echo Saplings or harvested in essence-rich biomes.\n\n" +
                "Special Properties:\n" +
                "Hanging Echo Fruits attune to local biome EssenceTypes. Consuming 2+ Echo Fruits within 60s causes Materia Toxicity.",
                "hub_environment", 2, new ItemStack(ModBlocks.MATERIA_ECHO_LOG.get()),
                300f, 0.0f, a_env - 0.55f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_astral_veil_willow", "Astral-Veil Willow Tree", "ENVIRONMENT & NATURE",
                "Mystical Netherin wood species with starlight bark furrows, indigo foliage, and 16x16 weeping tendril vines.",
                "Overview & Description:\n" +
                "The Astral-Veil Willow is a majestic Nether tree whose bark glows softly with Light Level 3 ambient Materia energy.\n\n" +
                "Origin & Obtaining:\n" +
                "Found in celestial Nether groves or cultivated from Astral-Veil Willow Saplings using Bone Meal.\n\n" +
                "Crafting Uses:\n" +
                "Yields Astral-Veil Willow Logs, Stripped Logs, 1:1 Oak-style Planks, Slabs, Stairs, Fences, and weeping Vines.",
                "hub_environment", 2, new ItemStack(ModBlocks.ASTRAL_VEIL_WILLOW_LOG.get()),
                300f, 0.0f, a_env - 0.45f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_void_blight_mangrove", "Void-Blight Mangrove Tree", "ENVIRONMENT & NATURE",
                "Abyssal Nether stilt wood species with void-magenta bark, stilt roots, and bioluminescent fruit pods.",
                "Overview & Description:\n" +
                "The Void-Blight Mangrove grows in deep Nether chasms, supported by stilt roots and producing hanging Void-Blight Pods.\n\n" +
                "Origin & Obtaining:\n" +
                "Harvested in abyssal Nether chasms or grown from Void-Blight Mangrove Saplings.\n\n" +
                "Crafting Uses:\n" +
                "Yields Void-Blight Mangrove Logs, Stripped Logs, 1:1 Oak-style Planks, Roots, and consumable Void-Blight Pods.",
                "hub_environment", 2, new ItemStack(ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get()),
                300f, 0.0f, a_env - 0.50f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_necrotic_rose_of_jericho", "Necrotic Rose-of-Jericho", "ENVIRONMENT & NATURE",
                "Two-state dry root ball that unfurls into a decay-inflicting rose (Wither I).",
                "Overview & Description:\n" +
                "A two-state desert flower that remains a closed gray root ball until walked over, unfurling into a dark decay rose.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Tomb Dunes, Blighted Dunes, and Frost-Tomb Barrows.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Necrotic Rose Petals used in Decay Potion brewing.\n\n" +
                "Special Properties:\n" +
                "Unfurls on entity contact and inflicts Wither I.",
                "env_flora", 0, new ItemStack(ModBlocks.NECROTIC_ROSE_OF_JERICHO.get()),
                980f, 0.0f, a_env + 1.00f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_void_stalker_orchid", "Void-Stalker Orchid", "ENVIRONMENT & NATURE",
                "Black petals framing a rotating purple void orb with short-range entity teleportation.",
                "Overview & Description:\n" +
                "An alien orchid with three black petals surrounding a hovering purple void orb.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Void-Fringe Mangroves and Void Rift Caverns.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Void-Stalker Petals used in teleportation catalysts.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 6 and portal particles; entity contact teleports entities.",
                "env_flora", 0, new ItemStack(ModBlocks.VOID_STALKER_ORCHID.get()),
                1020f, 0.0f, a_env + 1.04f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_stardust_aloe", "Stardust Aloe", "ENVIRONMENT & NATURE",
                "Succulent rosette with glowing teal ridges emitting celestial stardust sparkles.",
                "Overview & Description:\n" +
                "A fleshy desert succulent featuring indigo leaves laced with glowing teal stardust ridges.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Astral Dunes and Red-Sand Arches.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Stardust Aloe Leaves used in Stardust Gel extractions.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 6 and stardust sparkles.",
                "env_flora", 0, new ItemStack(ModBlocks.STARDUST_ALOE.get()),
                1060f, 0.0f, a_env + 1.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_sanguine_lily", "Sanguine Lily", "ENVIRONMENT & NATURE",
                "Crimson star water lily pad providing Regeneration I aura to injured entities.",
                "Overview & Description:\n" +
                "A large crimson star water lily floating on water with a bubbling pool of blood liquid.\n\n" +
                "Origin & Obtaining:\n" +
                "Spawns naturally in Crimson Orchards and Blood-Mire Swamps.\n\n" +
                "Crafting Uses:\n" +
                "Harvested for Sanguine Lily Pads used in health restoration filters.\n\n" +
                "Special Properties:\n" +
                "Emits Light Level 5 and grants Regeneration I to injured entities.",
                "env_flora", 0, new ItemStack(ModBlocks.SANGUINE_LILY.get()),
                1100f, 0.0f, a_env + 1.12f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_fauna", "Fauna & Mobs", "ENVIRONMENT & NATURE",
                "Grots, Veil Foxes, Aurorafowl, Cryo-Stalkers, and Ovis species.",
                "Overview & Description:\n" +
                "Indigenous creatures adapted to elemental atmospheric pressures and regional Materia currents.\n\n" +
                "Origin & Obtaining:\n" +
                "Found roaming native biomes or spawned using Fauna Spawn Eggs.",
                "hub_environment", 0, new ItemStack(Items.BONE),
                280f, 0.0f, a_env + 0.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aurorafowl", "Aurorafowl", "ENVIRONMENT & NATURE",
                "Ethereal sky-skimming avian with a 7-block biplane delta wingspan gliding along atmospheric Materia currents.",
                "Overview & Description:\n" +
                "The Aurorafowl is an ethereal wading avian native to Arctic peaks and Aurora Skies. Features biplane delta wings with radiant cyan-magenta membranes.\n\n" +
                "Behavior & Mechanics:\n" +
                "Effortlessly catches thermal updrafts, glides across fjords, and performs bipedal crane steps on ground. Can be sheared for Celestial Plumage.\n\n" +
                "Harvest & Drops:\n" +
                "Drops Aurorafowl Feathers, Raw Aurorafowl meat, and 2-3x Average Aurora Essence.",
                "env_fauna", 1, new ItemStack(ModItems.AURORAFOWL_FEATHER.get()),
                360f, 0.0f, a_env + 0.12f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_geyser_wiggle_worm", "Geyser Wiggle-Worm", "ENVIRONMENT & NATURE",
                "10-segment geothermal serpentine leviathan residing in magma pools and steaming basalt crevices.",
                "Overview & Description:\n" +
                "A massive volcanic worm with an incandescent 16-cube magma collar ring and articulated lower jaw.\n\n" +
                "Behavior & Mechanics:\n" +
                "Swims through molten lava and vents pressurized high-temperature steam plumes that generate powerful physical updrafts.\n\n" +
                "Harvest & Drops:\n" +
                "Shearing or harvesting during venting yields Vapor Vent Membranes; drops Sulfur Clods and 2-4x Average Vapor Essence upon defeat.",
                "env_fauna", 1, new ItemStack(ModItems.VAPOR_VENT_MEMBRANE.get()),
                380f, 0.0f, a_env + 0.18f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_cryo_stalker", "Cryo-Stalker", "ENVIRONMENT & NATURE",
                "Glacial apex predator with 90% snow/ice stealth, 8-block pounce leaps, rear tail-swipes, and bright-light/fire weakness.",
                "Overview & Description:\n" +
                "A digitigrade quadrupedal hunter native to Frost-Bite Floes and polar ice packs, sculpted with glassy black-ice armor and glowing cyan Glacial Materia veins.\n\n" +
                "Behavior, Stealth & Weaknesses:\n" +
                "▪ 90% Snow/Ice Camouflage: Turns ~90% translucent with silent footsteps (gliding over Rotten Ice without collapsing it) whenever stalking on or beside snow or ice in dim light (block light <= 10).\n" +
                "▪ Fire & Bright-Light Weakness: Igniting a Cryo-Stalker or luring it into bright torches/lanterns (block light > 10) instantly strips its stealth and inflicts a -30% movement speed penalty.\n" +
                "▪ 8-Block Pounce, Freezing Bite & Rear Tail-Swipe: Lunges up to 8 blocks to deliver a 4-second freezing hypothermia bite (dealing bonus frost damage to chilled targets), and whips attackers striking from behind with a knockback Rear Tail-Swipe.\n\n" +
                "Harvest & Drops:\n" +
                "Yields Cryo-Crystalline Fangs (for forging the Frost-Bite Dagger), Frost-Veined Hides (for curing into Cryo-Insulated Armor), and 1-2x Average Glacial Essence + 1-2x Average Vapor Essence.",
                "env_fauna", 1, new ItemStack(ModItems.CRYO_CRYSTALLINE_FANG.get()),
                400f, 0.0f, a_env + 0.24f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_cryo_stalker_gear", "Frost-Bite Dagger & Cryo-Insulated Armor", "ENVIRONMENT & NATURE",
                "Swift freezing fang dagger, Sea Salt hide curing, and 4-piece Cryo-Insulated expedition armor.",
                "Overview & Description:\n" +
                "Materials hunted from Cryo-Stalkers across Frost-Bite Floes can be preserved with Sea Salt Crystals and forged into fast frostbite weaponry and full-body polar hazard protection.\n\n" +
                "Hide Curing & Crafting Recipes:\n" +
                "▪ Sea Salt Crystals: Boiled from a Water Bucket in a furnace, cooked from Kelp on a campfire, or mixed from a Water Bucket (or Ice) and Kelp.\n" +
                "▪ Cured Frost-Veined Hide & Storage Block: Cure 1 Frost-Veined Hide with 2 Sea Salt Crystals (4 cured hides compact into a Cured Frost-Hide Block and unpack back into 4 hides).\n" +
                "▪ Frost-Bite Dagger: Forged diagonally from 2 Cryo-Crystalline Fangs, 1 Necrotic Slime, and 1 Stick. Strikes rapidly (-1.8 attack speed) and inflicts deep freeze buildup, Slowness III (-45% speed), and Mining Fatigue II for 3 seconds.\n\n" +
                "4-Piece Cryo-Insulated Armor (Helmet, Chestplate, Leggings & Boots):\n" +
                "Crafted in standard armor patterns from Cured Frost-Veined Hide (+18 total Armor, +6.0 total Toughness). Wearing any piece clears freeze buildup (and wards off Runic Spire chill); Cryo-Insulated Boots walk safely across Rotten Ice without collapsing it; and the full 4-piece suit cleanses Frost-Bite Floes swamp-water Slowness and Rotten Ice Poison.",
                "env_cryo_stalker", 2, new ItemStack(ModItems.FROST_BITE_DAGGER.get()),
                555f, 0.0f, a_env + 0.22f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_ovis_species", "Ovis Species & Aegis Plates", "ENVIRONMENT & NATURE",
                "Mountain-climbing horned quadrupeds with frontal aegis bracing and ramming charges.",
                "Overview & Description:\n" +
                "Heavy alpine grazers featuring 14-segment spiral horns. Encompasses Rime-Back Ovis, Overgrowth Ovis, and Patina Galvanic Ovis variants.\n\n" +
                "Behavior & Mechanics:\n" +
                "Climbs sheer scree cliffs, braces defensively against frontal damage (-80%), and sheds Aegis Plates when charging into stone walls.\n\n" +
                "Harvest & Drops:\n" +
                "Spawns naturally across Blue-Glacier Shelves. Yields Glacial Aegis Plates, Patina Aegis Plates, or Mossy Aegis Plates alongside 1-2x Average Aegis Essence and 1-2x Average Glacial/Static/Overgrowth Essence.",
                "env_fauna", 1, new ItemStack(ModItems.GLACIAL_AEGIS_PLATE.get()),
                420f, 0.0f, a_env + 0.30f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_void_sea_serpent", "Void Sea Serpent", "ENVIRONMENT & NATURE",
                "16-segment abyssal leviathan with a grand dorsal sail, pectoral fins, and sonic shockwave roars.",
                "Overview & Description:\n" +
                "An enormous 16-segment articulated sea serpent swimming through deep abyssal oceanic trenches and void-permeated depths.\n\n" +
                "Behavior & Mechanics:\n" +
                "Uses an undulating 16-segment Catmull-Rom swimming animation, executes biting lunges that inflict Blindness and Slowness, and unleashes sonic void shockwaves that fling attackers away.\n\n" +
                "Harvest & Drops:\n" +
                "Drops Serpent Void Scales and 5-8x Strong Void Essence, utilized in crafting Void-attuned maritime armor and abyssal diving gear.",
                "env_fauna", 1, new ItemStack(ModItems.SERPENT_VOID_SCALE.get()),
                440f, 0.0f, a_env + 0.36f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_glacial_hydra", "Glacial Hydra", "ENVIRONMENT & NATURE",
                "Multi-tined cryo-beast that breathes sub-zero frost and regenerates rapidly atop ice floes.",
                "Overview & Description:\n" +
                "A massive glacial beast possessing layered crystalline antlers, throat fluff, and frost-encrusted armor.\n\n" +
                "Behavior & Mechanics:\n" +
                "Roams frozen oceans and glacial taigas. Channels intense Cryo breath that deals freezing damage and slows targets, while rapidly healing whenever standing upon ice, packed ice, blue ice, or snow.\n\n" +
                "Harvest & Drops:\n" +
                "Drops Hydra Glacial Horns and 5-8x Strong Glacial Essence, essential for forging Cryo-infusion matrices and absolute-zero weaponry.",
                "env_fauna", 1, new ItemStack(ModItems.HYDRA_GLACIAL_HORN.get()),
                460f, 0.0f, a_env + 0.42f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_luminoth", "Luminoth", "ENVIRONMENT & NATURE",
                "Bioluminescent moth drawn to light sources, shedding shimmering solar dust and granting night vision.",
                "Overview & Description:\n" +
                "A gentle, winged moth with glowing luminescent wings that flutters gracefully through dusk forests and subterranean caverns.\n\n" +
                "Behavior & Mechanics:\n" +
                "Attracted to torches, lanterns, and Materia nodes. Emits soft floating motes and confers ambient Night Vision to nearby travelers.\n\n" +
                "Harvest & Drops:\n" +
                "Yields Luminoth Dust plus 1-2x Average Radiant Essence and 1x Average Aurora Essence.",
                "env_fauna", 1, new ItemStack(ModItems.LUMINOTH_DUST.get()),
                480f, 0.0f, a_env + 0.48f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_amber_weeping_stag", "Amber Weeping Stag", "ENVIRONMENT & NATURE",
                "Majestic cervid with weeping crystalline antlers, existing in 3 regional variants (Amber, Frost, Solar).",
                "Overview & Description:\n" +
                "An ancient horned stag with elaborate multi-tined crystal antlers that weep elemental essence dew.\n\n" +
                "Variants & Behavior:\n" +
                "▪ Amber: Native to temperate groves, weeps Vitae nectar.\n" +
                "▪ Frost: Native to sub-zero tundras, immune to freezing, weeps Cryo snow crystals.\n" +
                "▪ Solar: Native to hot savannas and sunlit peaks, weeps Radiant sparks.\n\n" +
                "Harvest & Drops:\n" +
                "Yields Weeping Amber Tears alongside 2x Strong Vitae Essence and 1-2x Strong Overgrowth Essence.",
                "env_fauna", 1, new ItemStack(ModItems.WEEPING_AMBER_TEAR.get()),
                500f, 0.0f, a_env + 0.54f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_storm_kite", "Storm Kite", "ENVIRONMENT & NATURE",
                "Wide-winged aerial predator that rides thunderhead thermals and swoops down with static shock strikes.",
                "Overview & Description:\n" +
                "A majestic ray-like atmospheric predator possessing 6 segmented wing joints, cephalic fins, and an electric glowing underbelly.\n\n" +
                "Behavior & Mechanics:\n" +
                "Soars at high altitudes. Upon spotting prey, executes dramatic high-speed dive attacks. During thunderstorms, its attacks channel direct lightning discharges.\n\n" +
                "Harvest & Drops:\n" +
                "Drops Storm Kite Membranes and 4-6x Strong Storm Essence, utilized in crafting Aer-attuned gliding wings and galvanic capacitors.",
                "env_fauna", 1, new ItemStack(ModItems.STORM_KITE_MEMBRANE.get()),
                520f, 0.0f, a_env + 0.60f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_glacial_frost_core", "Glacial Frost-Core", "ENVIRONMENT & NATURE",
                "Hovering 40 HP crystalline diamond construct with 4 orbiting ice satellites and a 5-block Freezing Frost Aura.",
                "Overview & Description:\n" +
                "A silent, hovering elemental construct native to the Blue-Glacier Shelf, formed from a vertical rotating sapphire-cyan diamond core surrounded by 4 orbiting satellite ice cubes.\n\n" +
                "Behavior & Mechanics:\n" +
                "Glides ~2 blocks above frozen shelves, phases effortlessly through snow layers, and remains peaceful until provoked—unleashing a 5-block Freezing Frost Aura (Slowness II for 5s + 3.0 frost damage).\n\n" +
                "Harvest & Crafting Uses:\n" +
                "Periodically sheds Frost-Core Shards when undisturbed and drops 1x Frost-Core Shard, 1-2x Packed Ice, and 2-4x Average Glacial Essence upon defeat. Used to build the automated Glacial Ice-Maker (which turns water buckets or adjacent water sources into Packed Ice) and brew Potions of Freezing.",
                "env_fauna", 1, new ItemStack(ModItems.FROST_CORE_SHARD.get()),
                540f, 0.0f, a_env + 0.66f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_frost_tusk_mammot", "Frost-Tusk Mammot", "ENVIRONMENT & NATURE",
                "135 HP elephant-sized woolly glacial mammoth with sweeping blue-ice tusks, peaceful snow kneeling, bull-charges, and upward tusk tosses.",
                "Overview & Description:\n" +
                "A massive 135 HP snowy-cream-to-taupe woolly mammoth roaming the Blue-Glacier Shelf and the gravel islets of the Auroral Skerries, crowned with shaggy frost bangs, a 5-segment curled trunk, sturdy 3-toenail elephant feet, and twin sweeping glacier-blue ice tusks.\n\n" +
                "Behavior & Mechanics:\n" +
                "Plows aside snow layers ahead of its feet as it walks and periodically kneels onto its front knees to rest peacefully on the snow. Neutral unless provoked—waking up immediately to stampede-charge attackers with lowered tusks and deliver an upward-launching Tusk Sweep (10.0 damage + 4-block upward toss).\n\n" +
                "Shearing, Food & Gear:\n" +
                "▪ Shearing (1-day regrowth): Yields 1-2x Insulated Glacial Wool, woven with String into the Thermal Insulated Blanket (clears freezing buildup and protects against Runic Spire hypothermia).\n" +
                "▪ Defeat Drops: Yields 3-4x Raw Mammot Meat (Cooked Mammot Meat if slain by fire), 2-3x Insulated Glacial Wool, and 3-5x Strong Glacial Essence.",
                "env_fauna", 1, new ItemStack(ModItems.INSULATED_GLACIAL_WOOL.get()),
                560f, 0.0f, a_env + 0.72f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_aurora_jelly", "Aurora Jelly, Glow-Gel & Auroral Wand", "ENVIRONMENT & NATURE",
                "40 HP buoyant magnetic glow-drifter that pulls nearby loot to Aegis-holding players and powers Glow-Gel, Auroral Wands, and Redstone Couplers.",
                "Overview & Description:\n" +
                "A passive 40 HP (20 hearts) translucent dome jellyfish native to the Auroral Skerries, Crystalline Pack-Ice, and Blood-Ice Shallows, modeled with a 4-tier neon-green, cyan, and indigo stepped bell dome housing twin spinning mint-emerald magnetic dipole cores, an inner oral manubrium, and 7 flowing two-segment tendrils.\n\n" +
                "Magnetism & Defense:\n" +
                "▪ Aegis Magnetism: Drifts gently toward players holding an Aegis-Spire Petal or Aegis Crystal Shard, fanning its tendrils in a magnetic halo to pull dropped items within 6 blocks smoothly toward the player.\n" +
                "▪ Bioluminescent Flash & Sting: Delivers a 2.0-damage electric tentacle sting if its tentacles are directly touched, and if hurt, releases a 3-second blinding flash before darting rapidly away.\n\n" +
                "Drops & Crafted Gear:\n" +
                "▪ Defeat Drops: Yields 1x Auroral Gelatin, 1-2x Charged Glow-Tentacles, 2-4x Average Aurora Essence, and 2x Average Aegis Essence (all scaling with Looting).\n" +
                "▪ Auroral Gelatin -> Glow-Gel Pathing: Bouncy bioluminescent path blocks compounded with Coarse Gravel that light up underfoot (Light Level 13) and grant a swift movement speed boost.\n" +
                "▪ Charged Glow-Tentacle -> Auroral Wand & Static Redstone Coupler: Crafts the ranged Auroral Wand (fires a 32-block cold aurora ray that places Light Level 14 Auroral Mote Lights in air or water without melting ice) and directional Static Redstone Coupler relays (collects side/back signals and outputs strength 15 forward).",
                "env_fauna", 1, new ItemStack(ModItems.CHARGED_GLOW_TENTACLE.get()),
                500f, 0.0f, a_env + 0.28f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_sandstone_golem", "Sandstone Golem", "ENVIRONMENT & NATURE",
                "135 HP hovering sandstone sentinel with Pale & Red Canyon variants, a 3-block blinding sandstorm slam, and magnetic sand-vortex self-repair.",
                "Overview & Description:\n" +
                "A faceless 135 HP (67.5 hearts, 8 Armor) desert sentinel powered by a fractured crimson redstone heart suspended inside an open sandstone chest archway, flanked by 4 magnetically orbiting hammer-arm and column-leg assemblies. Naturally adapts into Pale Desert Sandstone or Red Canyon Sandstone variants matching local sands and canyons.\n\n" +
                "Behavior, Combat & Sand-Vortex Repair:\n" +
                "▪ Neutral Ruin Guardian: Peaceful toward travelers unless provoked, while actively defending desert ruins against undead and raiders.\n" +
                "▪ Sandstorm Cyclone & Double-Hammer Slam: Spins its floating sandstone limbs in a blinding cyclone before slamming both war-hammers down for 10.0 damage and 3 seconds of Blindness across a 3-block radius.\n" +
                "▪ Magnetic Compass-Vortex Repair: When damaged near Sand or Sandstone—or when right-clicked with Sand or Sandstone by hand—unlocks all 4 floating limbs into an orbiting compass vortex to vacuum up loose sand, restore health, and attune its Pale or Red masonry.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Feeding Sand or Sandstone by hand coaxes the golem to shed a Hard Sandstone Shard on cooldown, and defeating one drops 1x Hard Sandstone Shard, 2-4x Sandstone blocks, and 3-5x Strong Dust Essence (Large Dust Materia piece; attuned to Dust, Earth, and Arid Materia).",
                "env_fauna", 1, new ItemStack(ModItems.HARD_SANDSTONE_SHARD.get()),
                580f, 0.0f, a_env + 0.78f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_peat_mud_kelpie", "Peat-Mud Kelpie", "ENVIRONMENT & NATURE",
                "40 HP amphibious swamp water-spirit with Peat-Mud & Mangrove variants, storm water-walking, and a blinding mud-splash tail slap.",
                "Overview & Description:\n" +
                "A passive 40 HP (20 hearts, 2 Armor) equine swamp water-spirit native to Deep Swamp Trenches and Mangrove Fens, appearing in two natural 64x64 variants: Peat-Mud & Portal-Teal (slick peat hide, bog-kelp mane, glowing portal-teal hooves) and Mangrove Tannin & Wisp-Gold (reddish mangrove-bark hide, olive fen-reeds mane, glowing marsh-lantern amber hooves).\n\n" +
                "Behavior, Water-Walking & Defense:\n" +
                "▪ Channel Swimmer & Storm Water-Walker: Glides swiftly through deep swamp channels, trots smoothly over mud without sinking, and walks right atop water blocks during rain, storms, and mirage weather while trailing glowing spirit droplets.\n" +
                "▪ Mud-Splash Tail Slap: If startled or hurt, whips around to slap mud and water across a 3.5-block radius, inflicting 2 seconds of Blindness before darting away.\n\n" +
                "Marsh Offering & Materia Affinity:\n" +
                "Offering Kelp, Seagrass, Mangrove Propagules, or Lily Pads heals the Kelpie, grants 15 seconds of Water Breathing, and coaxes it to peacefully shed a Kelpie Water-Pelt on cooldown. Defeating one yields 1x Kelpie Water-Pelt, 0-2x Kelp, and 2-4x Average Abyss Essence (Medium Abyss Materia piece; attuned to Abyss, Water, and Umbral Materia).",
                "env_fauna", 1, new ItemStack(ModItems.KELPIE_WATER_PELT.get()),
                600f, 0.0f, a_env + 0.84f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_silt_sphinx", "Silt-Sphinx", "ENVIRONMENT & NATURE",
                "120 HP couchant stone sphinx monument with Gilded Pharaoh variant, ancient chest & rune-seal guardianship, and a blinding winged paw slam.",
                "Overview & Description:\n" +
                "A 120 HP (60 hearts, 8 Armor) living canyon sphinx native to Dusty Canyons and Petrified Wood ruins, built with a chiseled pharaoh face, royal nemes headdress, uraeus cobra crest, feathered stone wings, and lion limbs in two 64x64 variants: Red Canyon Silt-Stone Monolith and Gilded Pharaoh Gold & Lapis.\n\n" +
                "Monument Lifecycle, Chest Chambers & Combat:\n" +
                "▪ Couchant Statue & Awakening: Reclines motionless as a petrified monument (-50% projectile damage) until disturbed—shuddering awake over 2.2 seconds to prowl on four stone lion legs, and reclining smoothly back into its monument pose once intruders leave.\n" +
                "▪ Ancient Chest & Rune Seal Rule: Guards nearby treasure chests, awakening immediately if an intruder opens a chest without solving local runes. Powering a local rune seal (lit Runed Brazier, Autocast Pedestal, Redstone Lamp, or powered Lectern / Chiseled Bookshelf / Chiseled Sandstone) keeps the Sphinx peacefully dormant while chests are opened.\n" +
                "▪ Winged Double-Paw Slam: Rears up, flares its feathered stone wings, and slams both front paws down for 8.0 damage plus 3 seconds of Blindness in a 3.5-block silt cloud.\n\n" +
                "Royal Tribute & Materia Affinity:\n" +
                "Offering a Gold Ingot or Lapis Lazuli to a rune-appeased dormant Silt-Sphinx gilds it into the Pharaoh variant and coaxes it to shed a Sphinx Sand-Stone on cooldown. Defeating one yields 1x Sphinx Sand-Stone, 1-2x Chiseled Sandstone, and 3-4x Strong Barrow Essence (Large Barrow Materia piece; attuned to Barrow, Earth, and Arid Materia).",
                "env_fauna", 1, new ItemStack(ModItems.SPHINX_SAND_STONE.get()),
                620f, 0.0f, a_env + 0.90f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_zephyr_simurgh", "Zephyr-Simurgh", "ENVIRONMENT & NATURE",
                "105 HP legendary sky-raptor with a full takeoff/soar/airbrake cycle, 6-block gale wing-clap shockwave, and thunderstorm-sentinel tailwind screech.",
                "Overview & Description:\n" +
                "A neutral 105 HP (52.5 hearts, 4 Armor) sky-raptor native to Silica Dunes and Windy Ridges, clad in layered sky-blue plumage, bronze armor plates, a 3-plume glowing copper crown crest, giant 3-jointed 10-blade feathered wings, and 3 trailing copper tail streamers. Completely immune to both fall damage and lightning strikes.\n\n" +
                "Flight Lifecycle, Gale Clap & Weather Sentinel:\n" +
                "▪ Ridge Launch & Thermal Soaring: Launches off rocky ridges with an updraft wing-thrust, rides high canyon thermals, flares its slotted wingtips in an eagle airbrake landing, and hops agilely on bronze talons when perched.\n" +
                "▪ 6-Block Gale-Force Windstorm Clap: If provoked on the ground or in mid-air, snaps both giant copper-tipped wings violently forward to deal 8.0 damage (+3.0 static shock in thunderstorms) and hurl attackers 6 blocks backward.\n" +
                "▪ Weather-Sentinel Sky-Screech: Calls out to the sky whenever rain or thunderstorms gather (or when offered Rabbit, Copper Ingots, or Phantom Membranes), granting nearby peaceful travelers 20s of Slow Falling + 15s of Speed I tailwind.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sheds Simurgh Feathers during storms or when offered Rabbit/Copper/Membrane tribute on cooldown, and drops 1-2x Simurgh Feathers and 3-4x Strong Storm Essence (Large Storm Materia piece) upon defeat (attuned to Storm, Air, and Lightning Materia).",
                "env_fauna", 1, new ItemStack(ModItems.SIMURGH_FEATHER.get()),
                640f, 0.0f, a_env + 0.96f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_mirage_fata_sprite", "Mirage Fata-Sprite", "ENVIRONMENT & NATURE",
                "24 HP hovering sun-stone & neon-cyan desert sylph with duplicate mirror-afterimage twins, parabolic solar dish collector focusing, and mirage evaporation.",
                "Overview & Description:\n" +
                "A passive 24 HP (12 hearts) crystalline desert sylph native to Red Silt-Wastes and Void-Charged Flats, built from an incandescent sun-stone core (Light Level 12-15) recessed inside a 12-bar glowing neon-cyan prismatic mirage frame and orbited by 4 double-sided beveled quartz mirror shards.\n\n" +
                "Solar Dish Focusing & Mirror Afterimages:\n" +
                "▪ Parabolic Solar Reflector: Under bright daytime skies (or when offered Quartz, Amethyst Shards, Glass Panes, or Glowstone Dust), pitches its 4 mirror-shards 90° upright into a solar dish—focusing sunbeams directly onto nearby solar/optical collectors (such as Astral Collectors, Qilin Light-Refractors, and Daylight Detectors), highlighting nearby monsters with Glowing, and warming nearby peaceful travelers with 15s of Fire Resistance.\n" +
                "▪ Duplicate Mirror Afterimage: If struck, shears its neon-cyan mirage frame off its sun-stone core to spawn a duplicate Mirage Fata-Sprite afterimage twin (without the afterimage ability or drops) that mirrors the main sprite's flight, heading, and solar-dish poses to pull away hostile monster aggro before evaporating into shimmering motes.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Offering Quartz, Amethyst Shards, Glass Panes, or Glowstone Dust coaxes the Sprite to peacefully condense a Shimmering Mirage Aura on cooldown, and defeating one yields 1x Shimmering Mirage Aura, 0-2x Nether Quartz, and 2-3x Average Mirage Essence (Medium Mirage Materia piece; attuned to Mirage, Radiant, and Arid Materia).",
                "env_fauna", 1, new ItemStack(ModItems.SHIMMERING_MIRAGE_AURA.get()),
                660f, 0.0f, a_env + 1.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_magma_cherufe", "Magma Cherufe", "ENVIRONMENT & NATURE",
                "105 HP cliff-climbing volcanic basalt lizard with an 8-damage molten jaw clamp, igniting 5-segment tail whip, and lava regeneration.",
                "Overview & Description:\n" +
                "A hostile 105 HP (52.5 hearts, 8 Armor) reptilian lava beast native to Magma Caverns and Basalt Delves, clad in polygonal dark-basalt dorsal and flank plates, 6 glowing molten magma vents, a broad crocodilian skull with 11 jagged ivory fangs, 4 sprawling 3-clawed legs, and a heavy 5-segment basalt tail.\n\n" +
                "Cliff Scaling, Lava Bathing & Combat:\n" +
                "▪ Basalt Cliff Climber & Lava Regenerator: Crawls across volcanic rock and effortlessly climbs vertical basalt cliffs near lava rivers. Completely immune to fire and lava, regenerating 2 HP (1 heart) per second whenever submerged in lava.\n" +
                "▪ Molten Jaw Clamp & Tearing Shake: Coils back roaring wide and lunges forward to clamp prey in its interlocking fangs for 8.0 damage while briefly pinning them.\n" +
                "▪ Igniting Basalt Tail Whip: Spins to lash flanking or close-range foes across a sweeping arc with all 5 basalt tail segments, dealing 4.0 fire damage and igniting targets for 5 seconds.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Thermally shocking its hot armor with Snowballs, Splash Water Bottles, or Ice cracks loose a fire-resistant Cherufe Basalt Crust on cooldown, and defeating one yields 1x Cherufe Basalt Crust, 2-4x Basalt blocks, and 3-4x Strong Magma Essence (Large Magma Materia piece; attuned to Magma, Nether, and Earth Materia).",
                "env_fauna", 1, new ItemStack(ModItems.CHERUFE_BASALT_CRUST.get()),
                680f, 0.0f, a_env + 1.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_soulfire_phenex", "Soulfire Phenex", "ENVIRONMENT & NATURE",
                "44 HP skeletal bird-of-prey with a blazing hollow-ribcage soulfire wisp, 3-block Soul Wither breath cone, and soul-storm flight surge.",
                "Overview & Description:\n" +
                "A hostile 44 HP (22 hearts, 4 Armor) demonic skeletal bird-of-prey native to Soulfire Fjords and Ash Canyons, built from bleached ivory bone with twin V-crown horns, a hooked raptor beak, 3-jointed fanned bone wings, a 5-feather skeletal tail fan, and a see-through 360-degree hollow ribcage housing a blazing cyan-blue soulfire wisp.\n\n" +
                "Flight Lifecycle, Soulfire Breath & Storm Surge:\n" +
                "▪ Ledge Launch & Cavern Soaring: Hops agilely on skeletal talons when perched, launches off ledges in an explosive burst of blue flame, soars through cavern vaults trailing cyan-blue soulfire particles, and flares its bone wings and tail fan to airbrake onto perches.\n" +
                "▪ 3-Block Soulfire Breath Cone: Breathes blue soulfire in a 3-block cone either in a diving aerial swoop or a grounded predatory lunge, dealing 6.0 damage, igniting prey, and afflicting targets with Soul Wither.\n" +
                "▪ Soul Sand Storm Surge & Death Gutter-Out: Flies significantly faster during soul sand storms and in windy nether/soul/ash biomes. Upon defeat, its ribcage soulfire wisp flares brightly once and gutters out completely inside the collapsing bleached skeleton.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Offering Soul Sand, Soul Soil, Soul Torches, or Blaze Powder stokes its ribcage wisp to shed a fire-resistant Phenex Ember-Feather on cooldown, and defeating one drops 1-2x Phenex Ember-Feathers and 2-4x Average Soulfire Essence (Medium Soulfire Materia piece; attuned to Soulfire, Nether, and Undead Materia).",
                "env_fauna", 1, new ItemStack(ModItems.PHENEX_EMBER_FEATHER.get()),
                580f, 0.0f, a_env + 1.14f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_void_arachne", "Void Arachne", "ENVIRONMENT & NATURE",
                "40 HP wall-climbing Void spider-priestess with 3-second rooting silk nets, 6-damage venomous obsidian fangs, and portal-slit blink dodges.",
                "Overview & Description:\n" +
                "A hostile 40 HP (20 hearts, 4 Armor) mythological spider-humanoid weaver native to Rift Flats and Basalt Delves, uniting an 8-legged Nether-obsidian arachnid body and jointed front fangs with a midnight-indigo robed Void Priestess upper torso, flared wizard bell-sleeves, and six glowing magenta slit eyes.\n\n" +
                "Wall Skittering, Silk Nets & Portal Blinking:\n" +
                "▪ Wall Climber & Ruin Weaver: Skitters across cavern floors, climbs vertical walls effortlessly, ignores cobwebs, and nests near obsidian and Nether portal ruins while weaving Void silk between her hands.\n" +
                "▪ Rooting Void Silk Net & Fang Clamp: Hurls a taut Void silk net that snares prey in place for 3 seconds, then lunges or blinks in to clamp with her venomous obsidian fangs for 6.0 damage plus Poison.\n" +
                "▪ Dimensional Portal Blink: Compresses into a thin vertical portal slit (blink_out) and tears back open nearby (blink_in) to dodge incoming projectiles and counter-attacks, unless anchored by a nearby Spatial Phase Anchor.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Offering String, Ender Pearls, or Chorus Fruit barters for a fire-resistant Void Web Spindle on cooldown, and defeating one drops 1-3x Void Web Spindles and 2-4x Average Void Essence (Medium Void Materia piece; attuned to Void, Nether, and Umbral Materia).",
                "env_fauna", 1, new ItemStack(ModItems.VOID_WEB_SPINDLE.get()),
                600f, 0.0f, a_env + 1.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_ash_barrow_gnome", "Ash Barrow-Gnome", "ENVIRONMENT & NATURE",
                "30 HP squat slate canyon spirit with above-ground stone-hat cairn disguise, gravel-to-bone-dust grinding, and a 5-damage shovel-claw swipe.",
                "Overview & Description:\n" +
                "A neutral 30 HP (15 hearts, 6 Armor) squat humanoid slate spirit native to Ash Canyons and Skeleton Canyons, featuring a braided stone beard, heavy three-clawed excavator shovel pincers, a glowing purple Barrow Rune-Stone recessed into its back, and a wide 12x12 ash-dusted tiered stone slab hat.\n\n" +
                "Cairn Disguise, Gravel Grinding & Defense:\n" +
                "▪ Stone-Hat Cairn Disguise: Waddles slowly across canyon floors and tucks its arms and legs beneath its wide stone hat like a slate gravel cairn (halving incoming damage while tucked), strictly staying above ground without burrowing into solid blocks.\n" +
                "▪ Gravel-to-Bone-Dust Grinding: Scoops up nearby Gravel or Coarse Gravel (or Gravel handed/tossed to it by travelers) and grinds it beneath its stone beard into 1-2x Bone Dust (Bleached Bone Meal).\n" +
                "▪ Excavator Shovel Swipe: If provoked, pops out of its cairn disguise and swipes with its heavy stone shovel-claw arm for 5.0 damage and strong knockback.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Feeding Gravel, Coarse Gravel, or Flint by hand coaxes the Gnome to grind out bone dust and shed a Barrow Rune-Stone on cooldown, while defeating one yields 1x Barrow Rune-Stone, 1-2x Gravel blocks, and 2-3x Average Barrow Essence (Medium Barrow Materia piece; attuned to Barrow, Earth, and Undead Materia).",
                "env_fauna", 1, new ItemStack(ModItems.BARROW_RUNE_STONE.get()),
                620f, 0.0f, a_env + 1.26f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_orthrus_magma_beast", "Orthrus Magma-Beast", "ENVIRONMENT & NATURE",
                "50 HP dire two-headed magma hound with bounding gallop charges, an 8-fire-damage double-bite pounce, Left-Right snap combos, and magma-block speed.",
                "Overview & Description:\n" +
                "A hostile 50 HP (25 hearts, 6 Armor) mount-scale two-headed magma hound native to Magma Caverns and Ash Canyons, built from volcanic charcoal-basalt split by a blazing X-shaped molten chest nexus and crowned with two diverging horned wolf skulls and a flame-shard brush tail.\n\n" +
                "Bounding Charge, Twin Jaws & Magma Speed:\n" +
                "▪ Trot, Bounding Gallop & Magma Surge: Trots across volcanic stone and breaks into a ferocious bounding gallop when charging prey. Completely immune to fire and lava, regenerating Speed II and health whenever treading on Magma Blocks or wading in lava.\n" +
                "▪ Igniting Double-Bite Leap & Snap Combo: Coils low and pounces forward to slam both horned jaws shut for 8.0 fire damage and 5s ignition, or lashes out in close quarters with an alternating Left-then-Right twin-jaw snap combo.\n" +
                "▪ Subterranean Twin Howl: Throws both heads skyward in a fiery subterranean howl that rallies nearby Orthrus packmates with bounding speed.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Offering Magma Cream or Cooked Meats appeases both heads briefly to shed a fireproof Magma Orthrus Pelt on cooldown, and defeating one drops 1-2x Magma Orthrus Pelts and 2-4x Average Magma Essence (Medium Magma Materia piece; attuned to Magma, Nether, and Earth Materia).",
                "env_fauna", 1, new ItemStack(ModItems.MAGMA_ORTHRUS_PELT.get()),
                640f, 0.0f, a_env + 1.32f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_ash_cinder_wisp", "Ash-Cinder Wisp", "ENVIRONMENT & NATURE",
                "20 HP passive magma-core Will-o'-Wisp orbited by charcoal flakes that dances around torches and lanterns and puffs 2s blinding ash when disturbed.",
                "Overview & Description:\n" +
                "A passive 20 HP (10 hearts) hovering Will-o'-Wisp native to Soulfire Fjords and Ash Canyons. Its cracked spherical magma core glows warmly at Light Level 13-15 while three striated charcoal-and-ember flakes and three diamond cinder sparks counter-orbit in midair.\n\n" +
                "Light Curiosity & Ash Puff Defense:\n" +
                "▪ Magma & Fissure Drift: Floats peacefully above magma pools, lava seams, and ash fissures, completely immune to fire, lava, and fall damage.\n" +
                "▪ Torch & Lantern Hover Dance: Curiously drawn to placed or handheld Torches, Lanterns, and Braziers, spiraling around them in a moth-like flutter dance.\n" +
                "▪ Disturbed Ash Puff Spray: Never deals damage, but if struck or rushed without a calming light, flings its charcoal flakes wide to puff a cloud of ash that blinds disturbers for 2 seconds while drifting away.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Stoking a wisp with Coal, Charcoal, or Blaze Powder heals its spark, clears Darkness/Blindness, grants brief Fire Resistance, and sheds a Cinder Wisp Core on cooldown, while defeating one drops 1x Cinder Wisp Core and 2-3x Average Magma Essence (Medium Magma Materia piece; attuned to Magma, Nether, and Radiant Materia).",
                "env_fauna", 1, new ItemStack(ModItems.CINDER_WISP_CORE.get()),
                660f, 0.0f, a_env + 1.38f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_soul_sand_hydra", "Soul-Sand Hydra", "ENVIRONMENT & NATURE",
                "105 HP three-headed soul-sand draconic Hydra with soul-sand submerge ambushes, cascading 10-damage Wither bites, sweeping soulfire breath, and 3-way spell volleys.",
                "Overview & Description:\n" +
                "A hostile 105 HP (52.5 hearts, 8 Armor) four-legged draconic Hydra native to Soulfire Fjords and Ash Canyons, sculpted from spiral-carved soul-sand blocks with a 6-segment barbed tail and three horned S-curved dragon heads glowing with cold blue soulfire.\n\n" +
                "Soul-Sand Ambush & Triple-Head Combat:\n" +
                "▪ Soul-Sand Submerge Ambush: Glides effortlessly across Soul Sand without slowing, crouching low into soft soul sand to hide before erupting upward in a blinding sand-and-soulfire lunge when prey approaches.\n" +
                "▪ Cascading Triple-Head Wither Bite: Snaps Left, Right, and Center jaws in rapid sequence for 3 + 3 + 4 = 10.0 total damage and Wither.\n" +
                "▪ Sweeping Soulfire Breath & Volley: Sweeps all three maws in a cold-blue soulfire breath cone or charges its throat glands to hurl a 3-way volley of Soulfire Orbs.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Soul Sand, Soul Soil, or a Ghast Tear to an unaggroed Hydra coaxes it to shed a Hydra Soul-Gland on cooldown, while defeating one drops 1x Hydra Soul-Gland, 1-3x Soul Sand, and 3-4x Strong Soulfire Essence (Large Soulfire Materia piece; attuned to Soulfire, Nether, and Undead Materia).",
                "env_fauna", 1, new ItemStack(ModItems.HYDRA_SOUL_GLAND.get()),
                680f, 0.0f, a_env + 1.44f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_void_shadow_gargoyle", "Void Shadow-Gargoyle", "ENVIRONMENT & NATURE",
                "40 HP crying-obsidian cathedral sentinel that perches on obsidian, dives rapidly, screeches for 4 shock damage, and blinks through purple Void slits.",
                "Overview & Description:\n" +
                "A hostile 40 HP (20 hearts, 8 Armor) cathedral-style Rift Sentinel native to Rift Flats and Basalt Delves, carved from crying obsidian veined with glowing portal-purple streaks and winged with neon-magenta scalloped bat membranes.\n\n" +
                "Obsidian Perching, Screech Dive & Void Blink:\n" +
                "▪ Obsidian Perch & Rapid Dive: Seeks out elevated Obsidian, Crying Obsidian, and Void Stone blocks to perch on with cloaked wings, launching into rapid diving flight when prey approaches.\n" +
                "▪ Shockwave Screech & Blink Behind: Unleashes a piercing 4.0-damage shock screech into a diving claw-and-talon swoop, then blinks directly behind its target.\n" +
                "▪ Damage Rift Blink: Whenever struck (outside a Spatial Phase Anchor field), spirals into a vertical purple Void portal slit (blink_out) and erupts from a new angle (blink_in).\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Crying Obsidian or an Ender Pearl to an unaggroed perched sentinel sheds an Obsidian Gargoyle Wing on cooldown, while defeating one drops 1x Obsidian Gargoyle Wing and 2-4x Average Void Essence (Medium Void Materia piece; attuned to Void, Earth, and Umbral Materia).",
                "env_fauna", 1, new ItemStack(ModItems.OBSIDIAN_GARGOYLE_WING.get()),
                580f, 0.0f, a_env + 1.50f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_barrow_wight_golem", "Barrow Wight Golem", "ENVIRONMENT & NATURE",
                "120 HP crypt-stone sentinel disguised as a compact stone block until approached within 5 blocks, wielding a 12-damage column slam and 10-damage soulfire burst.",
                "Overview & Description:\n" +
                "A hostile 120 HP (60 hearts, 10 Armor) Crypt Sentinel native to Soulfire Fjords and Ash Canyons, built from chiseled dark crypt-stone masonry wrapped in glowing blue lichen around an open chest vault housing a spinning cyan soul-stone core.\n\n" +
                "Crypt-Stone Block Disguise & Sentinel Strikes:\n" +
                "▪ 5-Block Crypt-Stone Block Ambush: Folds its head, legs, and runic column arms tightly inward to rest as a sealed crypt-stone block (dormant) until an explorer enters its 5-block range, then roars awake (awaken).\n" +
                "▪ Crushing Double-Fist Column Slam: Swings both massive stone column fists overhead and smashes the ground for 12.0 damage and heavy knockback.\n" +
                "▪ Open-Chest Soulfire Shockwave: Compresses its spinning chest core and flings its arms wide to unleash a 10.0-damage radial soulfire shockwave.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Stone Bricks or Soul Sand to an unaggroed sentinel repairs its masonry and sheds a Runic Crypt Stone on cooldown, while defeating one drops 1x Runic Crypt Stone, 2-4x Stone Bricks, and 3-4x Strong Soulfire Essence (Large Soulfire Materia piece; attuned to Soulfire, Barrow, and Earth Materia).",
                "env_fauna", 1, new ItemStack(ModItems.RUNIC_CRYPT_STONE.get()),
                600f, 0.0f, a_env + 1.56f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_obsidian_scorpioid", "Obsidian Scorpioid", "ENVIRONMENT & NATURE",
                "60 HP basalt-climbing crying-obsidian scorpion with 6-damage pincer clamps, 4-fire-damage igniting magma tail stings, and full lava immunity.",
                "Overview & Description:\n" +
                "A hostile 60 HP (30 hearts, 10 Armor) Lava Beast native to Magma Caverns and Basalt Delves, built from chipped crying-obsidian plates veined with violet fissures, six columnar-basalt legs, massive serrated pincer claws, and an arched 6-segment molten magma stinger.\n\n" +
                "Basalt Wall Scaling, Pincer Clamp & Molten Sting:\n" +
                "▪ Basalt Climbing & Lava Immunity: Crawls in an alternating hexapod tripod gait, scales vertical basalt and obsidian cliffs effortlessly, and wades unharmed through lava pools.\n" +
                "▪ Serrated Pincer Clamp: Sweeps both pedipalps wide and slams its serrated pincer jaws shut for 6.0 damage, briefly clamping prey in place.\n" +
                "▪ Overhead Magma Tail Sting: Whips its 6-segment tail overhead like a spear to drive its white-hot magma stinger forward for 4.0 fire damage and 5s ignition.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Magma Cream or Crying Obsidian to an unaggroed Scorpioid sheds a Scorpioid Tail-Spike on cooldown, while defeating one drops 1x Scorpioid Tail-Spike and 2-4x Average Magma Essence (Medium Magma Materia piece; attuned to Magma, Void, and Nether Materia).",
                "env_fauna", 1, new ItemStack(ModItems.SCORPIOID_TAIL_SPIKE.get()),
                620f, 0.0f, a_env + 1.62f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_brimstone_wyvern", "Brimstone Wyvern", "ENVIRONMENT & NATURE",
                "135 HP lava-swimming and cavern-flying volcanic drake with an 8-damage bite, contact-exploding brimstone fireballs, and magma fire-recharging.",
                "Overview & Description:\n" +
                "A hostile 135 HP (67.5 hearts, 8 Armor) Lava Drake native to Magma Caverns and Ash Canyons, built from chiseled dark basalt scales crowned with glowing yellow brimstone dorsal crystals, blue-slate bat wings, digitigrade talons, and a 5-spike brimstone tail-paddle.\n\n" +
                "Lava Swimming, Cavern Flight & Magma Furnace:\n" +
                "▪ Lava Swimmer & Cavern Flyer: Glides swiftly through underground lava pools in a serpentine S-wave (swim), stalks on digitigrade talons (walk), and launches from ledges (takeoff -> fly -> landing) to patrol high cavern vaults.\n" +
                "▪ Magma Jaw Bite & Explosive Fireballs: Snaps its serrated jaws for 8.0 damage on land or in diving swoops, and hurls sulfur-yellow brimstone fireballs that explode on contact.\n" +
                "▪ Magma Fire Recharge: Completely immune to fire, lava, and fall damage. Swimming in lava or standing on Magma Blocks recharges its internal furnace, boosting its fire damage and fireball blast radius.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Magma Cream, Blaze Powder, or a Lava Bucket to an unaggroed Wyvern recharges its furnace and sheds a fireproof Wyvern Scale on cooldown, while defeating one drops 1x Wyvern Scale and 3-5x Strong Magma Essence (Large Magma Materia piece; attuned to Magma, Nether, and Air Materia).",
                "env_fauna", 1, new ItemStack(ModItems.WYVERN_SCALE.get()),
                640f, 0.0f, a_env + 1.68f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_obsidian_golem", "Obsidian Golem", "ENVIRONMENT & NATURE",
                "180 HP four-armed Nether sentinel colossus with a 15-damage quad-fist slam, 12-damage chest portal wave, and 4-arm projectile-deflecting shield wall.",
                "Overview & Description:\n" +
                "A hostile 180 HP (90 hearts, 12 Armor) four-armed Nether Sentinel native to Rift Flats and Basalt Delves, built from abyssal obsidian and stepped deepslate clamps veined with crying-violet fissures around a hollow 12x12x12 obsidian chest cage housing a swirling purple Nether-portal singularity core.\n\n" +
                "Portal Ruin Guarding, Quad Slam & 4-Arm Shield Wall:\n" +
                "▪ Crushing Quad-Fist Column Slam: Raises all four heavy crying-obsidian arms overhead and smashes the ground for 15.0 damage and heavy knockback.\n" +
                "▪ Open-Chest Spatial Portal Wave: Compresses its chest singularity and flings all four arms into a wide X-spread to release a 12.0-damage radial spatial shockwave.\n" +
                "▪ 4-Arm Projectile Shield Wall: Snaps all four forearms across its hollow chest cage (shield_block) to completely block and ricochet incoming arrows and projectiles.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Crying Obsidian or Obsidian to an unaggroed sentinel mends its masonry and sheds a fireproof Obsidian Core on cooldown, while defeating one drops 1x Obsidian Core, 2-4x Obsidian blocks, and 4-6x Strong Void Essence (Large Void Materia piece; attuned to Void, Nether, and Earth Materia).",
                "env_fauna", 1, new ItemStack(ModItems.OBSIDIAN_CORE.get()),
                660f, 0.0f, a_env + 1.74f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_soulfire_djinn", "Soulfire Djinn", "ENVIRONMENT & NATURE",
                "50 HP hovering cobalt soulfire efreet with high-speed soulfire-wake gliding, cupped-palm Soul Wither fireballs, and non-solid fence/iron-bar phasing.",
                "Overview & Description:\n" +
                "A hostile 50 HP (25 hearts, 4 Armor) legless Desert Efreet native to Soulfire Fjords and Silica Dunes, built from cobalt-blue soulfire blocks bound in an 8-segment rune-etched antique brass locking collar and bronze wrist cuffs, trailing flickering soulfire flames cupped in both hands.\n\n" +
                "Aerial Desert Gliding, Cupped-Palm Fireballs & Bar Phasing:\n" +
                "▪ High-Speed Soulfire Glide: Hovers low over dunes and fjords (walk) or launches into a forward-pitched aerial glide (glide_takeoff -> glide -> glide_landing), trailing blue soulfire embers.\n" +
                "▪ Cupped-Palm Soulfire Fireball: Cups both hands together in front of its chest to conjure a blazing soulfire orb, then thrusts both palms forward to hurl it for 6.0 damage plus Soul Wither.\n" +
                "▪ Non-Solid Block Phasing: Compresses its smoke-and-flame ribs and vortex tail laterally (phase_glide) to slip straight through fences, iron bars, glass panes, grates, and walls, while solid blocks still bar its passage.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Soul Sand, Soul Soil, Gold Ingots, or Copper Ingots to an unaggroed Djinn calms its bindings and sheds a Brass Djinn Collar on cooldown, while defeating one drops 1x Brass Djinn Collar and 2-4x Average Soulfire Essence (Medium Soulfire Materia piece; attuned to Soulfire, Arid, and Nether Materia).",
                "env_fauna", 1, new ItemStack(ModItems.BRASS_DJINN_COLLAR.get()),
                680f, 0.0f, a_env + 1.80f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_brimstone_chimera", "Brimstone Chimera", "ENVIRONMENT & NATURE",
                "135 HP three-headed volcanic beast (lion, shoulder goat, and viper tail) with bounding gallops, 12-damage triple strikes, and twin-maw magma breath.",
                "Overview & Description:\n" +
                "A hostile 135 HP (67.5 hearts, 8 Armor) three-headed Lava Beast native to Magma Caverns and Ash Canyons, featuring a tawny-golden lion body with mahogany-russet mane, an ashy-slate basalt-horned goat head on its right shoulder, and an overhead 7-segment olive viper snake tail.\n\n" +
                "Bounding Gallop, Triple Strike & Twin-Maw Magma Breath:\n" +
                "▪ Fast Prowl & Bounding Gallop: Patrols near lava lakes in a feline prowl (walk) and charges prey in a bounding quadrupedal gallop (run).\n" +
                "▪ 12-Damage Triple-Head & Claw Assault: Rears up to slash with its foreclaws (4.0 damage) before its lion, goat, and overhead viper tail strike together (8.0 fire + necrotic venom damage, 12.0 total).\n" +
                "▪ Sweeping Magma-Ember Breath & Lava Recharge: Immune to fire and lava; wading in lava pools recharges its twin molten lion and goat maws so they sweep wide torrents of magma-ember fire (12.0 total damage).\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Magma Cream, Blaze Powder, or raw meat to an unaggroed Chimera sates all three heads and sheds a Chimera Venom-Gland on cooldown, while defeating one drops 1x Chimera Venom-Gland and 3-5x Strong Magma Essence (Large Magma Materia piece; attuned to Magma, Chimera, and Blight Materia).",
                "env_fauna", 1, new ItemStack(ModItems.CHIMERA_VENOM_GLAND.get()),
                580f, 0.0f, a_env + 1.86f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_obsidian_wendigo", "Obsidian Wendigo", "ENVIRONMENT & NATURE",
                "105 HP towering skeletal forest fiend that turns completely invisible below Light Level 4, climbs tree trunks, rakes for 8 damage, and freezes prey with Slowness V.",
                "Overview & Description:\n" +
                "A hostile 105 HP (52.5 hearts, 6 Armor) towering skeletal Forest Fiend native to Blighted Copse and Petrified Wood, crowned with a weathered bleached deer skull and wide-swept dark obsidian-indigo antlers etched with glowing void-purple and neon-magenta fear runes.\n\n" +
                "Shadow Invisibility, Trunk Climbing & Terror Howl:\n" +
                "▪ Shadow Stalking (< Light 4): Turns completely invisible whenever local light drops below Level 4, creeping in a hunched marionette stalk (walk_start -> walk -> walk_stop), bursting into a feral sprint (run_start -> run -> run_stop), and scrambling hand-over-hand up tall tree trunks (climb_start -> climb -> climb_stop).\n" +
                "▪ 8-Damage Obsidian Claw Rake: Twists its spine to rip and cross-rake targets with its long skeletal bone claws for 8.0 damage.\n" +
                "▪ Bone-Chilling Terror Howl: Clutches its skull and arches backward in a convulsing howl (screech) that freezes nearby targets in terror with Slowness V for 2 seconds.\n\n" +
                "Harvest & Materia Affinity:\n" +
                "Sneaking to offer Bone, Bone Blocks, Rotten Flesh, or Obsidian to an unaggroed Wendigo sheds a Wendigo Skull-Plate on cooldown, while defeating one drops 1x Wendigo Skull-Plate and 3-4x Strong Barrow Essence (Large Barrow Materia piece; attuned to Barrow, Void, and Undead Materia).",
                "env_fauna", 1, new ItemStack(ModItems.WENDIGO_SKULL_PLATE.get()),
                600f, 0.0f, a_env + 1.92f, false
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
                "spell_modular_architecture", "Modular Spell Assembly", "MAGIC",
                "5-Ring Concentric Spell Circle, 8 Trigger Conditions, 9 Pure Flow Modifiers, Point-of-Contact Multi-Cast, Forms, and Dual-Verb Effects.",
                "Overview & Description:\n" +
                "Every custom incantation in Entropica combines an Intent Prefix (Combat, Utility, or Misc), an independent Cast Count from Single Cast (1x) up to Octuple Cast (8x), an Outer Rim Travel Distance stop (0m, 8m, 16m, 24m, 32m, 48m, 64m, 96m, or 128m), a Trigger Condition (8 triggers: On-Entity-Hit, On-Block-Collision, Timed Fuse, On-Proximity, On-Release, Corpse Bloom, Self, or Self-Timed), one of the 9 pure Flow Modifiers (Normal, Bifurcation, Trifurcation, Omnication Dome, Leaping Chain, Satellite Orbit, Accelerated Fast, Deliberate Slow, or Void-Warp Relay), and 1 to 8 Sub-Spell Slots each containing a Delivery Form (40% of slot fuel), a Primary Core Effect (40% of slot fuel), and an optional Dual-Verb Secondary Core Effect (+100% slot fuel).\n\n" +
                "Travel Distance, Trigger Logic, Travel Mutations & Point-of-Contact Multi-Cast:\n" +
                "▪ Outer Rim Travel Distance Circle (0m to 128m): Spins along the outermost astrolabe rim of the Arcane Scribing Table across 9 stops—0m (0.75x Materia cost, instant point-blank discharge), 8m (0.85x), 16m Standard (1.00x default), 24m (1.15x), 32m (1.30x), 48m (1.60x), 64m (1.95x), 96m (2.60x), and 128m Horizon Reach (3.40x). Governs projectile/carrier flight range, Beam reach, Blink/Position Swap distance, Void-Warp reach, and ranged Glyph/Skyfall/Summon placement without inflating AoE blast size (Shift + Right-Click resets to 16m).\n" +
                "▪ Trigger Logic Ring: Pairs freely with any Flow Modifier—including On-Entity-Hit (with a 75% potency splash fallback if a shot hits terrain), On-Block-Collision (100% power on floors/walls), Timed Fuse (0.5s-3.0s mid-air split or surface detonation), On-Proximity (3m sensor), On-Release (hold-and-release compression up to +15% power), Corpse Bloom (3s death-mark), Self, and Self-Timed (30s/60s/120s).\n" +
                "▪ Void-Warp Travel Mutation Gate: Selecting Void-Warp Relay on the Flow Ring requires Void Materia or a Materia created with Void (Astral, Null-R, Null-U, Wraith, Penumbra, Abyss, Oblivion, Entropic, Cataclysm, Eschaton, Entropica, or Singularity) infused into the Flow Ring with Mutation: ACTIVE.\n" +
                "▪ Point-of-Contact Multi-Cast (I-VIII) & 25%/75% Billing: Every payload launched by your Flow Modifier casts all 1 to 8 slotted spells right at that payload's hit spot. Dynamic triggers and relays pay 25% upfront and 75% on trigger, refunding the 25% deposit back into your Orbis Cell after 5 seconds if a shot misses completely.\n" +
                "▪ Held + Quick-Tap Beams, Held Stream Cones & Next-Hit Touch Priming: Beam spells channel continuously from your hand toward your crosshair while holding Right-Click (billing in Materia/second, ticking 5x/sec with clean multi-ray timers and a 3.0s Burst Finisher on Tier 1-2 beams or a 5.0s-only Cataclysmic Finisher on Tier 3-6 & Catalyst beams) OR fire a clean single ray if quick-tapped and released early; Cone spells support both Instant 60° Fan blasts and continuous Held Stream flamethrower/deluge channeling. Touch/Strike spells prime your weapon or hand for 30 seconds (sheathable for free via Sneak + Cast, reserving the +15% Spellblade potency bonus strictly for Engraved Weapons/Tools, and discharging cleanly on both mob and block hits with 50% sweeping cleave splash); Mobile and Sneak-Anchored Auras pulse 2x/sec across all 63 Materia suites (up to 4 concurrent Auras with +25% escalating fuel drain); and omni-surface Floor/Wall/Ceiling Glyphs support Single-Use or Repeating Sentry (3 pulses, 2s re-arm, 60% potency), Tier 5+ permanent duration, and 50% multi-Materia disarm refunds.",
                "magic_spellcrafting", 1, new ItemStack(ModItems.SPELL_PARCHMENT.get()),
                460f, 0.0f, a_magic - 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_multi_materia_mutations", "Multi-Materia & Signature Mutations", "MAGIC",
                "Per-part Materia assignment across all 63 Materia suites, full Defender Lineage Advantage, and independent Signature Mutation switches.",
                "Overview & Description:\n" +
                "Every individual part of a spell—Flow, Form, Primary Effect, and Secondary Effect—can be bound to any of the 63 registered Materia suites (#1 Air through #57 Entropica, Chimera, Caprine Secrets Radiant-Fire, Umbral-Water & Equinox, plus Aureole & Vesper) and has an independent Mutation ON/OFF switch.\n\n" +
                "Crafting, Advantage & Casting:\n" +
                "▪ Mutation ON: Activates that Materia type's unique Signature Mutation on that specific part—including unlocking Void-Warp Relay on the Flow Ring when Void or any Void-created Materia (Astral, Null-R, Null-U, Wraith, Penumbra, Abyss, Oblivion, Entropic, Cataclysm, Eschaton, Entropica, or Singularity) is infused, and sustaining persistent protective rings on Satellite Orbit.\n" +
                "▪ Mutation OFF: Keeps clean baseline behavior while still using that Materia type for visual color, combat advantage, and fuel cost.\n" +
                "▪ Full Defender Lineage Advantage: Combat Damage and Restoration evaluate the defender's full parent-tree Materia lineage (+0.5x per point uncapped vs mobs, +1 both ways on mutual volatile clashes, 4.0x Tier 5 Divine Override with Terminus weak to Undead, and Tier 6 0.0x standard-element immunity / 5.0x Mirror Clash & Entropica Singularity supremacy).\n" +
                "▪ Materia-Specific Lock: Binding specific Materia types grants a 15% fuel efficiency discount across the spell.",
                "spell_modular_architecture", 2, new ItemStack(ModItems.SPELL_GEM.get()),
                550f, 0.0f, a_magic - 0.06f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_primordial_base_mutations", "Primordial Base Spell Mutations", "MAGIC",
                "Signature Flow, Form, and Core Effect mutations for all 12 Primordial Base Materia types.",
                "Overview & Description:\n" +
                "Enabling a part's Mutation switch while bound to any of the 12 Primordial Base Materia types transforms how that specific spell part behaves without overriding other parts of the spell.\n\n" +
                "Primordial Twelve Lineup:\n" +
                "▪ Air, Water, Nature & Earth: Corkscrew-weaving 3D Turbulence Spirals (+1 vs Earth, -1 vs Void, deflecting incoming hostile projectiles), +35% Aerodynamic Slipstream Flows (excluding Deliberate Slow), +35% Zephyr Gale Orbits (2.5m wind-shear pulses) & 1.5s lingering Sonic Leap Chain vacuum bridges, 8-pierce Jetstream Propulsion wind tunnels & surface-grounded vertical jetpacks, spherical Gust Shields (+30% speed, 50% projectile DR & Air affinity), primary-target 8m Concussive Uppercuts, +20%-speed Slipstream Shields (0.8s sprint bowling for 28% Materia damage, mobile or Sneak-anchored), 1.5s/step-off circular 3m Geyser Mine Air Trampolines (launching all entity types 14m up while cushioning friendly landings), exact 60° Howling Gales (extinguishing fires/campfires/candles across the full cone & deflecting incoming physical projectiles), 9s-45s scaling 6m-11m Tornado Vortices, golden-buff (#A88654) wind-blade / 6m updraft-gust / 12m-taunt non-damaging-gust Zephyr Eidolons, departure Vacuum Implosion blinks (sucking foes, items & projectiles inward with 1.2s Slowness II), zero-drop cobweb/leaf-clearing & root/mud-drying Aeration & Dehydration, and immediate milk-proof -40% stamina/attack-speed Hypoxia Suffocation Hexes; arcing 3D Hydro-Cannon Slugs (+1 vs Nether & mutual +1 clash vs Arid, -1 vs Frozen & Nature, impact-only circular 3m splash with 30% Harm splash, knockback, fire douse & farmland hydration), +0.6 Slow Viscous Fluid Mass (vs burning or true Nether/Fire/Magma foes) & soaking Riptide Undertow Chains, cobweb/fire/weed-slicing 8-pierce High-Pressure Hydro-Cutters (2x horizontal & vertical knockback on pre-soaked/swimming foes), uncapped circular Tidal Novas (cooling source Lava -> Obsidian & flowing Lava/Magma -> Cobblestone + clearing surface flora), primary-target 1.0s-stunning Crashing Breakers (+50% combat bonus vs burning or true Nether/Fire foes), 100% fire/lava-immune & 40% all-explosion-dampening Oceanic Shrouds (mobile or Sneak-anchored), circular 3s Whirlpool Geysers, exact 60° 12m+ Deluge Torrents (deflecting incoming physical projectiles & hydrating/dousing the full cone), 8s-36s scaling 8m-16m Monsoon Cloudbursts (healing friendly aquatic creatures & drowning true Nether/Fire mobs), deep-azure (#225C7C) tendril / soaking-orb + passive fire-soil-care / first-hit 3.5s-blinding-pop Aqueous Eidolons, 2m/3m-splashing open-water Fluid Dissolution steps, cross-namespace copper-cleaning & dirt/grass->clay / sponge / concrete / magma->cobble Aqueous Elutriation, and immediate fire-quenching milk-proof 2x-Lightning/3x-Frozen Waterlogged Drowning Hexes; spiked 3D Thorned Briar Seeds (+1 vs Water & Earth, -1 vs Nether, Frozen & Umbral, Nature-advantage puncture & non-boss 2.0s vine root), hard-5m CHAIN_LEAP-only Entangling Briar Chains, ally-safe 8-pierce crop-growing & ally-mending Verdant Growth Rays (zero friendly-fire harm/hex on injured allies & peaceful wildlife), circular 6m+ Bramble Bloom Explosions (flinging foes back first, rooting non-bosses for 1.5s after 0.4s & carpeting the full blast in briars), primary-target 8s-Poison-II Toughness-shredding & Undead-corroding Poison Ivy Slashes (light 2.5s Poison I cleave splash), walking-blooming +30% true-foliage-speed Sylvan Mantles (tall grass/flowers while walking, clear-sky sunlight regen, unprovoked wildlife calm & Nature affinity, mobile or Sneak-anchored), circular 4m+ 3D wooden root-cage Overgrowth Snares, exact 60° Toxic Pollen Cones (with active 4.5s Poison II + Nausea lingering clouds), 6s-30s scaling 6m-14m Spore Tempests, lush-green (#1A5923) lunging-briar / 1.0s-root+Poison-I Thorn-Dart / 35%-thorns spore-bursting Sylvan Eidolons, Tier 1-10 canopy/log-phasing Tree-Stepping (boosted range near living foliage & 30s Flowering Azalea departure bush), living-Oak-Wood & 6-sapling Botanical Synthesis, and immediate 0-damage milk-proof 20%/35%/50%-Toughness-shredding Silencing Spore Hexes; terrain-hugging 3D Quake Spires (+1 vs Void, -1 vs Air, temporary 4s Pointed Dripstone spike & 2-2.5 armor shred), Slow-Flow-only +0.4 Dense Tectonic Mass (3.0+0.35*tier armor shred for 8s, with 1.0x unpenalized non-Slow Earth flows), metal-only Magnetite Tractor Beams (pulling metallic items, raw/metallic ores & metal-armored foes + Tier 2+ EntropicaConfig whitelist/blacklist exposed ore mining), 12-pillar Stone Ring Rampart bunkers (around caster or downrange impact center with 4.45m anti-clip push), pure Earth-converting primary-target Seismic Impacts (armor shred & knockdown), exact 0.50x-Void / 1.50x-Air +2-Toughness / 50%-KB-resist Earth Defensive Attunements (mobile or Sneak-anchored), circular underfoot-erupting 4-armor-shredding Tectonic Impalement Spikes, exact 60° 6m-push 1.5s-Blindness loose-dirt/sand/gravel-only Sand & Gravel Flurries, 6.0s-13.2s scaling circular Meteorite Shrapnel Barrages, fortified (#773214, +25% HP, +8 Armor, +2 Toughness) armor-shredding Tectonic Eidolons (melee-only taunt & death shrapnel exclusive to Decoy), Tier 1-10 wall-phasing Burrow Steps (+25% range boost through/onto stone & soil), 4-sand/gravel->1-sandstone & Mossy-Cobble/Cobbled-Deepslate/Red-Sand Lithic Compaction, and immediate 0-damage milk-proof 5/7.5/10-armor-shredding exact 2.0x-Earth Sundering Hexes.\n" +
                "▪ Frozen, Nether, Radiant & Umbral: Aerodynamic zero-drop 3D Glacial Ice-Shards (+1 vs Water, -1 vs Nether & Undead, exact non-compounded 60% slow for 3s & Water->Packed Ice / Lava->Obsidian freeze), Slow-Flow-only +0.4 Permafrost Creep (vs Water & permanent Blue Ice water freezing), progressive-slow Absolute-Zero Cryo-Rays (3.0s dry / 1.0s-1.5s wet freeze-stasis, 1.0s boss stasis cap & walkable Ice/Obsidian bridges), circular 6m Frost Nova Bursts (exact 2.0s real temporary Ice block world encasement on grounded foes, exact 60% airborne slow & surface water/lava freeze), 1.5s real temporary Ice block world-encasing Flash-Freeze Stasis strikes, exact -30% attack-speed Glacial Spikes Retaliation wards (zero Mining Fatigue compounding, mobile or Sneak-anchored), temporary non-destructive 3x3x3 collapsing Glacial Pitfalls (exact 50% slow + frost chill, cleanly restoring original terrain blocks afterward), 10m Hoarfrost Flash-Freeze cones (freezing source & flowing Water/Lava into Packed/Blue Ice & Obsidian bridges + permanent natural-melting Snow layers & exact 50% slow), full-radius tier-scaled 3D pseudoblock Colossal Glacial Monolith spires (leaving permanent Blue/Packed/Regular Ice monuments), cyan-frost (#98DCF2) 3-hit-freezing Agent / exact-60%-slow icicle Sentry / destruction-only spherical-4m-shatter Decoy Glacial Eidolons, 3x3 water-freezing Frostwalker Stasis blinks (destruction-only shatter statue decoy), universal breakable-block Silk Touch & Permafrost Transmutes (1 Snow Block / 8 Snow layers / 4 Snowballs -> Packed Ice & Lava Bucket -> Obsidian + empty Bucket), and immediate 0-damage milk-proof exact-60%-slow sprint-locking +50%/+75%/+100%-blunt-shatter Permafrost Stasis Hexes; molten 3D Netherfire Comets (+1 vs Frozen, -1 vs Water, all-furnace-recipe item smelting, water/ice melting & ground/low-air temporary fire), CHAIN_LEAP-only 6s crossing-hostile Arcing Fire-Whip Chains, Orbis-Cell-only Nether Tendril Tethers (recharging Nether Materia fuel proportional strictly to actual damage dealt, zero player HP heal, lighting campfires/candles & melting water/ice), hostile-targeted circular 6m Hellfire Ring Shockwaves, weapon-synergy Cauterizing Cleave & Auto-Smelt strikes (6.0s unquenchable Netherfire on primary & cleave targets, cooking meat & pre-smelting held raw ores across full mob/player inventories strictly on kill), exact 0.50x-Frozen / 1.50x-Water lava-swimming Immolation Shrouds (100% fire/lava/magma immunity, 5s melee ignition & ice/snow melting, mobile or Sneak-anchored), contained non-spilling 4s 3x3 real-Lava Magma Puddle Traps (exact 65% slow, permanently solidifying into leg-trapping Obsidian), hostile-targeted recoiling Combustion Backdraft cones, 6.0s-13.2s tier-scaled circular Brimstone Meteor Rains (water vaporization & item auto-smelting), dashing-claw / ballistic magma-mortar / wicker-effigy Infernal Eidolons (#B83C08), 5s 3x3 departure-fire-pool & circular 3.5m arrival-blast Thermal Pop blinks, virtual Fortune I-IV ore-smelting & plank/log-charring Thermal Pyrolysis (sand->glass, clay->bricks, water steam & TNT priming), and immediate milk-proof underwater-burning regen-locking 30%/45%/60%-armor-melting Hellfire Sear Hexes (cleansed strictly via Water/Restoration magic or caster defeat); zero-pierce spherical-16m Homing Dawn Stars (#FFE278, +1 vs Undead, -1 vs Umbral, banking onto the lowest-HP Undead within a 16m sphere & detonating on first impact), CHAIN_LEAP-only 5s anti-stealth Prismatic Starlight Chains (stripping Invisibility & Umbral shrouds), Tier 1+ Blue-Ice-melting bloom-rendered Solar Thermal Lances (melting all Ice/Packed/Blue/Frosted Ice & Snow, igniting Phantoms, smiting Undead, dealing 65% Radiant heat to living hostiles & stripping Invisibility without chunk light updates), circular 6m+ Enemy-blinding Supernova Flashbangs (full Undead smite, 55% living-hostile damage & 4s Blindness + Weakness on all hostile Enemy mobs), weapon-synergy +50%-Undead Holy Smite & Daylight Ignition strikes (7s radiant fire on primary & cleave Undead, disintegrating drops into Starlight Flakes strictly on killing blows), exact 0.50x-Undead / 1.50x-Umbral Enderman-calming Pacification Shrouds (pacifying neutral mobs & refreshing allies in Sneak-Anchored fields), circular 3.0s 12-beam Single/Repeating Sunburst Confinement Sigils (exact 3.0s Blindness, full Undead smite & 45% living-hostile damage), exact 60° Cleansing Mist Cones (ally heal, front friendly debuff cleanse, 55% caster self-heal/cleanse wash & Undead sear), tier-scaled 3D Celestial Quartz pseudoblock Daylight Megalith Pillars (2s drop with touchdown Blindness/Slowness, 3 circular Holy Shockwaves & permanent Celestial Quartz monuments), solar-gold (#FFE278) Solar Seraph Eidolons (zero summon self-heal; flying light-arrow Agent, 7.5m-sphere projectile-reflecting Sentry & 45%-damage-absorbing circular-8m heal/cleanse-pulsing Decoy with zero death smite nova), Enemy/PvP-blinding 1-debuff Luminous Cleanse blinks, 2.5s channeled Holy Purification (Rotten Flesh -> Leather, Zombie Villager cure, curse removal, Soul Sand -> Sand, Netherrack -> Quartz Block, Mycelium/Podzol/Nylium -> Grass Block, Sculk -> Moss Block, Tinted -> Glass, Crying -> Obsidian, Poisonous Potato -> Potato, Spider Eye -> Glow Berries & Wither Rose -> Starlight Flakes, with zero invisible Light blocks on Break), and immediate milk-proof 4/6/8-armor-melting Glowing regen-locking Sun-Scorched Smite Hexes (searing Undead & living hostiles, cleansed via Restoration/Shadow magic or caster defeat); silent zero-drop piercing 3D Shadow Snare Missiles (+1 vs Radiant, -1 vs Void, stealth assassination with zero pack alerts & 2s physical floor shadow-tether), invisible-thread i-frame-bypassing 25%-echo Silent Shadow Chains, -30%-all-attack-draining & silencing Abyssal Shadow Lances (rigid or 45° tether), silent 6m aggro-wiping Smoke Veil Shockwaves (4s particle-free Invisibility), weapon-stacking 2s-cloaking Umbral Shroud strikes, 100%-GameEvent-vibration-silent Sculk/Warden-immune -60%-detection Shrouds of Silence & Stealth (mobile or anchored), Single/Repeating 8m aggro-wiping Void-Blind Mines (causing disoriented wandering), exact -50%-speed Shadow Dart Fans, cave-adaptive 6s 16m Black Rains of Obfuscation, silent ceiling-stalking / 16m-spawn-suppressing / live-gear-copying Shadow Stalker Eidolons (#211A21), shadow-to-shadow zero-particle Shadow-Steps, batch-discounted heat-free Nocturnal Darkening, and milk-immune +25%-hostile-damage Sensory Void Hexes.\n" +
                "▪ Undead & Void: Shattering 3D Necrotic Bone-Shards & Skull Missiles (+1 vs Frozen, -1 vs Radiant, shattering into bone shrapnel & 4s Wither I on mobs, terrain, or max-range airbursts), +0.4 Slow Creeping Tomb-Dust grave fog (exact -50% wake & impact movement speed), flexible 45°-leash Siphon Life shadow tethers (draining 25% dealt damage into both caster HP & Orbis Cell Undead Materia fuel), weapon-stacking 30%-lifesteal Vampiric Touch bone-claw strikes, 6m Corpse Explosion / Bone Nova ribcage eruptions (4s Wither II & knockback), every-tick undead-befriending Necrotic Shrouds (0.5x Frozen, 1.5x Radiant, mobile or anchored, & 1 HP/s living-hostile drain within 3m), Single/Repeating invisible Corpse Bloom Mines (Wither II skull blast converting slain mobs into 3-5 Bone Meal), jitter-free 2s fear-fleeing Screaming Skull Swarms, cave-adaptive 6s 12m shield-piercing Spectral Soul Swarms (exact -40% speed sap on living foes), health-siphoning / living-seeking skull-totem / 6m-Bone-Nova-effigy Cryptguard Eidolons (#4D6453), 3s lingering departure ribcage-rooting Grave-Phasing blinks, batch-discounted fossil-stone & bone-block Necrotic Fossilization, hunger-to-health Metabolic Transfusion, regen-locking death-spreading Entangling Miasma reactions, and -30%-toughness regen-locking Cadaverous Wither-Blight Hexes (+150% 32m Necrotic Resonance undead spawn-lure on players); slow-drifting thin-obstacle-phasing 3D Singularity Rift Orbs (#41304E, +1 vs Air, -1 vs Earth, collapsing into a 4m black-hole implosion on entities, solid walls, or max range), solid-wall-phasing Void-Warp Phasing Flows (micro-wormhole chains & homing bifurcations), stone-piercing 45°-locking Gravitational Tractor Beams (reeling foes inward or holding them weightlessly suspended in mid-air), 6m Singularity Implosions, weapon-stacking armor-piercing Spatial Destabilization Strikes (10s Void toxicity random warps), 0.5x-Air / 1.5x-Earth Zero-G Levitation Auras (high-float jumps & fall-immune glide, mobile or anchored), Single/Repeating 6m 5s Null-Zone Silencing Mines, buff- & ward-stripping Null-Matter Dispel Sprays, cave-adaptive 6s 12m Singularity Vortex Rifts (lifting mobs skyward before dropping them for vertical fall damage), short-range-phasing claw / 10m gravity-pulsing void-dart pylon / 5m micro-singularity silhouette Void-Stalker Eidolons (#41304E), persistent 5s two-way 3D Wormhole Transit portals, batch-discounted Abyssal Disintegration (plus Void-lineage Direct-to-Inventory mining/harvesting/logistics & Omni-Sponge bucket filling), simultaneous-shock Gravitational Collapse (Void + Lightning) mergers, stationary 3D black-hole Gravity Wells, and milk-immune 10-20s rubber-banding +50%-Void Spacetime Anchoring Curses (2x Void-Stalker & Enderman Spacetime Rift Beacon on players).\n" +
                "▪ Arid & Lightning: Swirling 3D Dust Devil Missiles (#C2B28F, mutually volatile +1/-1 clash vs Water, launching struck mobs 3 blocks backward with a swirling sand trail), 1.5s-blinding item-sweeping Sirocco Leaping Chains, fire-dousing & repelling Sirocco Sandstreams, 6m Haboob Sandstorm Novas (3s Blindness, fire dousing & drying water pools into packed sand or dry mud), weapon-stacking Desiccating Mummification Strikes (4s Slowness II & Hunger, turning slain mob drops into cured meat & sand), -50%-mob-detection arrow-deflecting Sandstorm Veils (forming temporary dry sandstone crusts over water, mobile or Sneak-Cast anchored), Single/Repeating 4m hunger-draining Desiccation Siphon Sigils, 60° grass-scouring & bark-stripping/log-petrifying Sandblast Blunderbuss Waves, cave-adaptive 6s 12m Haboob Sandstorm Downpours (30% Slowness, Blindness, water drying & leaf-to-stick breaking), sandblade / sandstone-obelisk / exploding heat-mirage Dune-Dervish Eidolons (#C2B28F), mirage-decoy Mirage Steps, batch-discounted Petrofaction & Desiccation, 16-hunger +2-Toughness Mineral Cauterization, and milk-immune 2x-fire regen-locking Desiccation Mummification Hexes (25% armor bypass & +30% knockback Desiccated Fragility on players); near-instant zero-drop 3D Ball-Lightning Plasma Bolts (#FFE600, +1 vs Earth & Water, -1 vs Void & Water, +100% Conductive Overcharge & 8-target double-range arcs on wet foes, 1.0s stun & 3m wet/metal-armor spark discharge), 1->2->4 exponential Supercharged Forked Chains (+50% 18m jump across wet mobs or water surfaces), blinding stagger & water-pool-conducting Continuous Tesla Arcs, 6m 1.5s movement-locking Static EMP Novas (momentary level-15 redstone & battery pulse), weapon-stacking 1.0s-stunning +45% 15s attack-speed Overcharge Discharge Strikes, +30%-sprint 0.5s-retaliation Static Shrouds (Living Lightning Rod absorbing natural lightning strikes into 100% full Orbis Cell Materia fuel, mobile or anchored), Single/Repeating 4-victim 2.0s-paralyzing Tesla Arc Mines (powering redstone wire for 4.0s), +50% rain/water-extended 60° Forked Arc Fans, cave-adaptive 15s 12m Thunderstorm Skyfall Bolt Barrages, spark-trailing / rapid-homing Ball-Lightning Tesla-coil / contact-shocking Volt-Spark Eidolons (#FFE600), line-shocking Thundersteps, batch-discounted Galvanic Electrolysis & Smelting (copper oxidation/wax control, sand->glass & remote Machine Re-Charging), Magnetic Railguns, and milk-immune -40% move/attack-speed shield-disabling Galvanic Overcharge Hexes.",
                "spell_multi_materia_mutations", 3, new ItemStack(ModItems.SPELL_GRIMOIRE.get()),
                640f, 0.0f, a_magic - 0.04f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_elemental_fusion_mutations", "Elemental Fusion Spell Mutations", "MAGIC",
                "Signature Flow, Form, and Core Effect mutations for Magma, Storm, Glacial, Overgrowth, Dust, Spore, Taiga, and Oasis.",
                "Overview & Description:\n" +
                "Tier 1 Elemental Fusion Materia types (#13-#20) blend two primordial streams into compound spell mutations when a part's Mutation switch is toggled ON.\n\n" +
                "Elemental Fusion Lineup:\n" +
                "▪ Magma (#13 — Earth + Nether): Arcing 3D Molten Magma Boulders (+1 vs Frozen & Nature, -1 vs Air & Water, 10% armor bypass, 6s burn & temporary 1-block magma crust), volcanic-pop Pyroclastic Split Flows, 1x1 stone/dirt-boring & ore-smelting Pyroclastic Lavastreams (unquenchable fire & ice/snow melting), 6m contained-lava Caldera Shockwave Novas, weapon-stacking -30%-toughness auto-smelting Volcanic Slag Cleaves, +4-Armor +50%-knockback-resist fire/lava-immune Igneous Armor Mantles (-10% speed, 4m snow/ice melt & water steam, mobile or anchored), 3s 8m-launching 3x3-lava Volcanic Vent Geyser Traps, 60° obsidian/cobblestone-quenching Pyroclastic Surges, cave-ready 6s 12m Volcanic Meteorite Bombardments, ground-shaking / ballistic-mortar / caldera-nova Magma-Brute Eidolons (#B83C08), lava-pool & shockwave Caldera Steps, batch-discounted Igneous Forging, underwater-burning -40%-toughness -30%-speed Molten Slag Encasement Hexes, and 12m-airborne Volcanic Eruption Blasts.\n" +
                "▪ Storm (#14 — Air + Water): 3D Squall Tempest Slugs (#429BA3, +1 vs Earth & Glacial, -1 vs Nature, 10% armor bypass, 5m squall knockback, soaking & fire snuffing), rain-boosted (+50% reach) vacuum-pulling Squall-Leap Chains, ground-recoiling Gale-Cutter Torrent Beams, 6m 8m-knockback projectile-deflecting Tempest Nova Bursts, weapon-stacking arrow-deflecting Squall Cleaves (6m back & 4m airborne), every-tick smooth glider Squall-Rider Shrouds (2.0x flight speed in rain/thunder, fall immunity, mobile or anchored), 3s 4m ground-pinning Atmospheric Downburst Traps, 60° 8m-push cobweb-washing & farmland-hydrating Deluge Squall Cones, cave-ready 8s 16m Tempest Monsoon Downpours (pushing foes to the rim & grounding flyers), dashing / slug-spire / tempest-nova Tempest-Zephyr Eidolons (#429BA3), vacuum-splash +40%-speed Squall-Steps, batch-discounted Aero-Hydraulic Erosion (Dirt -> Mud -> Clay -> Gravel -> Sand & Lava -> Obsidian), land-drowning -40%-attack-speed 2x-Lightning Waterlogged Hypoxia Hexes, and 14m (2x vs wet) Hurricane Force Pushes.\n" +
                "▪ Glacial (#15 — Water + Frozen): Heavy 2-pierce 3D Glacial Ice-Lances (#98DCF2, Strong vs Arid, Weak vs Nature & Undead, 10% armor bypass, 60% Slowness for 3s & Packed Ice water flash-freezing), +0.4-advantage Blue-Ice-waking Permafrost Creep flows, 3s-channeling 2s-solid-freezing Absolute-Zero Cryo-Torrent Beams (3-wide Packed Ice bridges & Lava -> Obsidian), 6m 4s-Blinding 80%-Slowing fire-snuffing Hoarfrost Blizzard Shockwaves, weapon-stacking -30%-toughness Slowness-IV Brittle Frost Strikes, every-tick Powder-Snow-walking Frost-Walker Permafrost Cryo-Shrouds (40% Slowness & 0.5s freeze stutter on melee attackers, mobile or anchored), disarmable Single/Repeating 3x3 Powder Snow Pitfall Traps (restoring floor cleanly), 60° -30%-toughness 1.5s-ground-flattening Blizzard Shrapnel Fans, cave-ready 2s-telegraph Glacial Monolith Cataclysms (2s grounded freeze shockwave & permanent Ice/Packed Ice/Blue Ice spire monuments), 3-hit solid-freezing / rapid-icicle / 4m-freeze-nova Glacial-Bruiser Eidolons (#98DCF2), gear-mirroring strike-shattering Ice Statue Blinks, batch-discounted Cryo-Solidification & Silk Touch harvesting, sprint/jump-freezing -50%-speed -40%-toughness Absolute-Zero Cryo-Hexes, and 2s 100%-invulnerable rapidly-healing 3D Cryo-Cocoon Mending shells.\n" +
                "▪ Overgrowth (#16 — Nature + Earth): 3D earthen-thorned Verdant Briar-Spike Seeds (#1A5923, +1 vs Water & Earth, -1 vs Nether, Frozen, Umbral & Air, 10% armor bypass, 2.5s 3D bramble cage root & moss/tall-grass spread), 5m-tethering 1.5s-rooting Entangling Briar Chains, -60%-speed root-dragging & crop/sapling-accelerating Verdant Growth Tether Rays, 6m knockback & 2.5s-rooting Bramble Rampart Blooms (erecting mossy stone/root spires & wildflower carpets), weapon-stacking ground-flattening Botanical Ground Slams, 2s-stillness +8-Armor 1-HP/s Earth-Root Bastions (mobile or anchored), disarmable Single/Repeating 4m 4s Strangler Briar Cage Mines, 60° knockback/1.5s-rooting & crop-harvesting/replanting Bramble Flurry & Phyto-Waves, cave-ready 8s 16m Phyto-Canopy Downpours (maturing saplings into trees & healing constructs), ground-smashing / crop-pulsing root-totem / 4m-snare Sylvan-Goliath Eidolons (#1A5923), 24m+ foliage-phasing Tree-Stepping blinks, batch-discounted Botanical Synthesis, silencing -35%-toughness movement-bramble Strangling Phyto-Bramble Hexes, and 2x-sunlight Photosynthesis Mending.\n" +
                "▪ Dust (#17 — Air + Earth): 3D compacted Choking Dust-Pods (#C2B28F, +1 vs Water, Void & Vapor, -1 vs Water & Void mutually volatile clash, 10% armor bypass, 2.5s Blindness, 2m torch/fire snuff & smooth-stone polishing), aggro-clearing torch-snuffing Abrasive Sandblaster Chains (1.5s Blindness), pushback stone-polishing Abrasive Sandblasting Jets, 6m 4s-Blinding aggro-wiping Whiteout Powder Novas, weapon-stacking 4s-silencing suffocating Choking Smother Strikes, -60%-mob-detection arrow-deflecting fire-clearing Obscuring Chalk Veils (3m torch snuff, mobile or anchored), disarmable Single/Repeating 2-deep 4s suffocating Choking Powder Pitfalls (restoring floor cleanly), 60° 3s-Blinding copper-scraping/wax-stripping/mud-drying/stone-polishing Abrasive Sandblasting Deluges, cave-ready 15s 14m aggro-wiping Whiteout Dust Storm Downpours, dashing Armor-Eroding / pressurized-slug pylon / 5m-Whiteout-Nova Dust-Stalker Eidolons (#C2B28F), gear-mirroring zero-vibration Chalk-Steps, batch-discounted Sedimentary Abrasion & Polishing, milk-immune blinding silencing Armor-Eroding Choking Powder Smother Hexes, and 10m Pneumatic Dust-Blasts inflicting Armor Erosion (-5% Armor & Toughness per level up to Level 20 / -100%).\n" +
                "▪ Spore (#18 — Nature + Air): Zero-gravity 3D Aerodynamic Fungal Puffballs (#1A5923 / #A88654, +1 vs Water & Earth, -1 vs Nether, Frozen, Umbral & Void, 10% armor bypass, 3m Poison II + Nausea spore cloud & mushroom sprouting), toxic-shield spiraling & outward-flinging Drifting Spore-Pollen Orbit Flows, 3s-channeling Mycelial Inoculation Streams (Dirt/Grass -> Mycelium & accelerating small mushrooms into Huge Mushrooms), 6m wind-knockback 4s-Poison-II/Nausea Fungal Bloom Novas, weapon-stacking -40%-speed chain-infectious death-popping Mycelial Infestation Strikes, Poison/Nausea-immune +30%-Mycelium-speed retaliating Mycelial Spore-Cloud Mantles (mushroom trail underfoot, mobile or anchored), disarmable Single/Repeating 4s immobilizing & silencing Silencing Mycelium Webs, 60° 5s-Poison-II/Nausea crop-accelerating Billowing Windborne Spore Blasts, cave-ready 15s 14m Choking Fungal Fog Downpours (blinding & 50%-slowing non-arthropods while decaying organic drops into compost), crop-tending Spore-Tender / growth-pulsing Bio-Pollen Spire / calming & nauseating Mushroom Decoy Symbiotic Fungal Eidolons (#1A5923 / #A88654), 24m+ silent underground Hyphal Network Blinks, bidirectional Nether Spore Transmutations, milk-immune allegiance-confusing poison-coughing Hallucinatory Mycelial Hexes, and Absorption + Resistance chitinous Spore Carapace Restorations.\n" +
                "▪ Taiga (#19 — Nature + Frozen): Zero-drop 2-pierce 3D Hardened Frost-Resin Spikes (#1A5923 / #98DCF2, +1 vs Water & Nature, -1 vs Nether, Umbral & Undead, 10% armor bypass, pinning the 2nd pierced foe in sticky freezing sap for 3s), impact/max-range Permafrost Fork Splits, speed- & toughness-siphoning Glacial Amber Leeching Beams (leaving frozen resin trails), 6m knockback 4s-Slowness-III/Freezing Boreal Blizzard Novas (snow & sweet berry bushes), weapon-stacking +30%-speed 10s-Frost-Walker Heat-Siphon Conifer Touches, 50%-arrow-deflecting instant-fire-snuffing cold-biome-regenerating Winter Solstice Shrouds (mobile or anchored), disarmable Single/Repeating 4s-pinning Slowness-IV Frosted Conifer Briar Traps (sweet berry bushes & powder snow), 60° -60%-speed +50%-shatter-vulnerability Frozen Sap Splatter Cones, tier-scaled cave-safe 6s Falling Glacial Timber Cataclysms (3D falling/standing Spruce-Log & Packed/Blue-Ice Pseudoblock barricades that solidify into real blocks upon completion), leaping Boreal Stalker / +25%-attack-speed cold-resist Frost-Howler Spire / blinding Phantom Snow Stalker Eidolons (#1A5923 / #98DCF2), 14m+ decoy-bush Boreal Pine-Phasing Blinks, batch-discounted Boreal Timber & Podzol Transmutes (+ Silk Touch Break), milk-immune anti-sprint +50%-shatter Petrifying Frost-Sap Hexes, and Regeneration-II 30s-cold-immune Evergreen Resilience Restorations.\n" +
                "▪ Oasis (#20 — Nature + Arid): 20%-lifesteal 3D Miracle Desert Bloom Seeds (#1A5923 / #C2B28F, +1 vs Water, Earth & Dust, -1 vs Nether & Umbral, 10% armor bypass, converting sand into grass or moist farmland & blooming desert flowers), 3-blossom healing-pollen Succulent Bloom Splits, 15%-lifesteal soil-hydrating & crop/cactus/sugarcane-accelerating Subterranean Aquifer Streams, 6m 4-block-knockback ally-healing fire-snuffing Miracle Oasis Bloom Novas, dual-mode Desert Blessing Touches (2.5s hostile pacification or ally healing + debuff/hex purification), -20%-physical-damage thorn-retaliating Succulent Thorn Carapaces (mobile or anchored), disarmable Single/Repeating 8m-launching Aquifer Geyser Mines (hydrating farmland), 60° 4s-pacifying crop/cactus/sugarcane-accelerating Desert Greening Mists, tier-scaled cave-safe 12s 16m+ Miracle Desert Downpours (healing players/pets, harming hostiles & steadily turning sand into grass), 20%-ally-healing Oasis-Warden / hydrating Aquifer Spring Spire / 5m-pacifying Mirage Blossom Effigy Eidolons (#1A5923 / #C2B28F), 14m+ flower-decoy Mirage-Spring Blinks, batch-discounted Desert Greening Transmutes (Sand -> Grass/Farmland, Dead Bush -> Flowers, Coarse Dirt -> Podzol), milk-immune hunger-draining -35%-attack-speed sprint/jump-jolting Dehydrating Sunstroke Hexes, and purifying hunger-refilling Miracle Spring Mending Restorations.",
                "spell_primordial_base_mutations", 4, new ItemStack(ModItems.WEEPING_AMBER_TEAR.get()),
                730f, 0.0f, a_magic - 0.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_vital_fusion_mutations", "Vital Fusion Spell Mutations", "MAGIC",
                "Signature Flow, Form, and Core Effect mutations for Vitae, Blood, Aura, and Amber Materia.",
                "Overview & Description:\n" +
                "Vital Fusion Materia types (#21-#24) channel living essence, hemomancy, spiritual breath, and prehistoric resin into specialized combat and support spell parts.\n\n" +
                "Vital Fusion Lineup:\n" +
                "▪ Vitae (#21 — Vital Fusion): Buff-siphoning 3D Spiraling Vitality Helices (#FF6B9D, +1 vs Undead, -1 vs Nether, Frozen & Umbral, 15% armor bypass), lowest-HP ally/hostile-seeking Cellular Mitosis Split Flows, 25%-lifesteal ally-cleansing & animal-breeding Cardiovascular Life-Tethers, 6m Speed-II / Jump-Boost-II (8s) Cellular Overcharge Blooms (dropping collectible healing life-essence orbs), ally-healing-on-hit Cellular Overcharge Strikes (5s), 2x-natural-regen -50%-exhaustion +20%-sprint Cellular Overdrive Shrouds (mobile or anchored), disarmable Single/Repeating -35%-speed death-healing Cellular Overcharge Mines (5s), 60° 35%-miss 3m-knockback ally-cleansing (+20% speed) Cellular Rejuvenation Sprays, tier-scaled cave-safe 12s 16m+ Font of Vitality Downpours (healing players/pets, clearing ailments every 2s & sprouting roses/orchids), caster-healing & Regen-pulsing Vitality Knight / 6m heartbeat-cleansing Cardiovascular Spire / 5m 6-heart Heart-Husk Effigy Eidolons (#FF6B9D), 14m+ 1s-stunning Cardiovascular Shock Blinks, Zombie-Villager-curing & Rotten-Flesh/Dead-Bush Vitality Synthesis Transmutes, and milk-immune regen-blocking -40%-attack-damage self-recoiling Hyper-Metabolic Arrhythmia Hexes.\n" +
                "▪ Blood (#22 — Vital Fusion): Zero-drop 2-pierce 30%-lifesteal 3D Sanguine Coagulation Needles (#8A0303, +1 vs Frozen, Nature & Spring [or Undead under Vitae dominance], -1 vs Radiant, Nether & Umbral, 15% armor bypass, inflicting 4s movement/attack Hemorrhage), 20%-lifesteal 2m-pulling Sanguine Capillary Chains, 35%-lifesteal -45%-speed Sanguine Exsanguination Tethers (leaving slowing blood puddles), 6m 3s-blinding arrow-melting Boiling Blood Mist Detonations (+2 Absorption hearts per enemy hit), weapon-stacking 40%-lifesteal 6s-Hemorrhage Vampiric Exsanguination Strikes (+2 hunger shanks & Speed II 5s on kill), +6-Armor / +2-Toughness Coagulated Crimson Carapaces (mobile or anchored, slowly consuming saturation), disarmable Single/Repeating 3.5s-rooting 30%-lifesteal Sanguine Spike Snares, 60° -25%-Armor-Toughness 3m-knockback 20%-lifesteal Sanguine Shrapnel Deluges, tier-scaled cave-safe 12s 16m+ Sanguine Deluge Downpours (stacking up to 50% Slowness, 15% lifesteal & +30% caster sprint speed on blood slicks), 25%-lifesteal Blood-Thrall Knight / twin-tether player-healing Exsanguination Pylon / 5m blinding +4-Absorption-heart Crimson Effigy Eidolons (#8A0303), 16m+ mob-shoving Sanguine Dash Blinks, Flesh Grafting Transmutes (Leather & Rotten Flesh -> Raw Porkchops), and milk-immune coagulation-blocking 3s-siphoning Sanguine Exsanguination Hexes (+100% 32m Scent of Hemorrhage predator spawn-lure).\n" +
                "▪ Aura (#23 — Vital Fusion): Zero-drop 2-pierce arrow-deflecting vortex-dragging 3D Spiritual Gale Lances (#FF6B9D / #A88654, +1 vs Undead & Wraith [or Earth under Air dominance], -1 vs Umbral, Void & Nether, 15% armor bypass), 3m-airborne-launching 1.5s-silencing 15%-fuel-siphoning Pranic Slipstream Chains, 20%-fuel-siphoning hostile-repelling downward-jetpack Pranic Jetstream Tethers (granting allies Slow Falling & +30% speed), 6m inward-pulling 2s-silencing item-vacuuming Spiritual Vacuum Implosions, weapon-stacking 4s-silencing 30%-fuel-siphoning Chakra Disruptor Palms, -20%-Materia-fuel drowning/suffocation-immune saturation-regenerating Chakra Respiration Shrouds (mobile or anchored), disarmable Single/Repeating 5s-silencing -25%-attack-damage Chakra Disruption Glyphs, 60° 8m-knockback arrow/fireball-deflecting fire-snuffing 2.5s-silencing ally-cleansing (+25% speed) Pranic Gale Deluges, tier-scaled cave-safe 6s Spiritual Gale Barrages (3m updraft torpedo geysers launching foes 10m upward for heavy fall damage), glider-swift 20%-fuel-siphoning Zephyr-Monk / 8m -20%-fuel Slow-Falling Chakra Respiration Spire / 5m 3s-silencing vacuum Spirit-Vane Effigy Eidolons (#FF6B9D / #A88654), 14m+ zero-vibration vacuum-pop Slow-Falling Pranic Slipstream Blinks, Aerated Purifying Transmutes (clearing potion clouds & cobwebs, drying sponges & purifying water/potions), and milk-immune silencing suffocation-ticking 2x-knockback Chakra Severance Hexes.\n" +
                "▪ Amber (#24 — Vital Fusion): 3m-rooting item-preserving 3D Amber Stasis Bubble Orbs (#FF6B9D / #773214 / #F29B2E, +1 vs Void & Barrow [or Undead under Vitae dominance], -1 vs Nether, Frozen & Umbral, 15% armor bypass, Silk Touch inheritance), +50%-duration 10%/s-escalating-slow (up to 50%) Resinite Viscous Linger Flows, 3s-contact -> 2.5s-Amber-Stasis fire/lava-item-guarding Silk-Touch Fossilizing Stasis Torrents, 6m 4m-knockback 3s-rooting (+20% ally speed / -40% hostile slow) flora-petrifying Amber Crystallization Novas, weapon-stacking -60%-speed -40%-attack-speed effigy-shattering Fossilizing Stasis Strikes, +8-Armor / +3-Toughness / 50%-KB-res zero-armor-wear Petrified Amber Bastion Mantles (mobile or anchored, 40% 3s melee slow splash), disarmable fire/lava/despawn-proof Amber Preservation Vault Glyphs, 60° 3m-knockback -60%-speed -30%-Toughness jump-locking Silk-Touch Molten Resin Deluges, tier-scaled cave-safe 10s blinding monument-encasing Prehistoric Resinite Monsoons, -50%-speed -30%-Toughness Amber-Clad Sentinel / 8m +4-Armor item-vault Resinite Stasis Spire / 5m 3s-rooting Petrified Amber Effigy Eidolons (#FF6B9D / #773214), 14m+ +4-Armor 10-HP-chrysalis Amber Chrysalis Blinks, Petrification & Fossilization Transmutes (logs -> Amber Wood, flora -> Amber Nectar Blossoms & Silk Touch fragile harvesting), and milk-immune -70%-speed sprint/jump-locking -40%-Toughness buff-purging Petrifying Amber Stasis Hexes.",
                "spell_elemental_fusion_mutations", 5, new ItemStack(ModItems.AMBER_NECTAR_CUP.get()),
                820f, 0.0f, a_magic - 0.01f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_ethereal_paradox_mutations", "Ethereal & Paradox Spell Mutations", "MAGIC",
                "Signature Flow, Form, and Core Effect mutations for Eclipse through Barrow (#25-#38), Aureole, Vesper, and Caprine Secrets (Radiant-Fire, Umbral-Water, Equinox).",
                "Overview & Description:\n" +
                "Ethereal & Paradox Fusion Materia types (#25-#38 plus Aureole & Vesper) and the Caprine Secret Fusions (Radiant-Fire, Umbral-Water & Equinox) combine opposing or celestial Materia streams into reality-bending spell parts whenever a part's Mutation switch is toggled ON.\n\n" +
                "Ethereal, Paradox & Caprine Secret Lineup:\n" +
                "▪ Eclipse (#25 — Ethereal & Paradox Fusion): Shield/cover-curving back-striking 2s-silencing 3D Penumbra Crescent Scythes (#FFEA78 / #211A21, +1 vs Radiant & Nature [or Undead under Radiant dominance], -1 vs Void, 1-pierce, 20% Harm lifesteal, innate Auto-Smelt), 3-bolt obstacle-curving Coronal Flare Bifurcation Split Flows, 2m-aggro-clamping -1-armor/s (up to -4) Umbral Inversion Jets, 6m 4m-knockback 4s-Blindness/Glowing torch-snuffing aggro-resetting Total Eclipse Novas, weapon-stacking -40%-melee-damage (6s) buff-siphoning Umbral Siphon Palms, +6-Armor Blindness/Darkness-immune celestial-fire-retaliating Solar Corona Bastions (mobile or anchored), disarmable Single/Repeating 4m-black-hole 3s-rooting buff-purging 4s-blinding torch-snuffing Total Eclipse Singularity Mines, 60° 6m-knockback 4s-Blindness/Glowing -3-armor (6s) torch/campfire-snuffing Coronal Flare Deluges, tier-scaled cave-safe 10s buff- & armor-enchantment-stripping silencing Penumbral Shadow Monsoons, shield-piercing Eclipse-Knight / 8m blinding Black Sun Corona Spire / 5m pulling-blinding Penumbra Silhouette Effigy Eidolons (#FFEA78 / #211A21), 16m mob-shoving celestial-fire Coronal Flare Dashes, +20%-darkness-yield Umbral Smelting Transmutes, and milk-immune Blindness/Glowing -3-armor mob-regen-locking Coronal Blind-Rot Hexes.\n" +
                "▪ Blight (#26 — Ethereal & Paradox Fusion): 2.5s-rooting death-contagious 3m-Poison-II-cloud 3D Contagious Necro-Spore Pods (#4D6453 / #34CC48, +1 vs Frozen [or Water & Earth under Nature dominance], -1 vs Radiant, Nether, Frozen & Umbral), 1.5s-rooting -1-armor death-leaping (2 hostiles within 3s) Cadaverous Contagion Chains, inward-pulling -50%-speed 3s-Blight-Bloom (-3 armor & 20% channeled heal) Necrotic Bramble Tether Beams, 6m 2.5s-rooting 4s-Poison-II Wither-Rose-wilting Cadaverous Briar Novas, weapon-stacking 4s-Poison-II -40%-speed -2-armor corpse-snaring (3s) Necrotic Thorn Gouges, +6-Armor Poison/Hunger-immune undead-neutral 30%-thorns Cadaverous Briar-Carapaces (mobile or anchored), disarmable Single/Repeating 4m 3.5s-rooting -3-armor Wither-Rose-blooming Strangling Rot-Briar Snares, 60° 3m-knockback 4s-Poison-II -40%-speed -2-armor bark-decaying flora-wilting Virulent Necro-Spore Flurries, cave-safe 14m+ 10s Poison-II -50%-speed meat-rotting thorn-rooting Cadaverous Rot Tempests, Poison-II armor-shredding Blight-Revenant / 8m -30%-speed 2-pierce Necro-Briar Spire / 5m 3s-rooting Wither-Rose Cadaverous Effigy Eidolons (#4D6453 / #34CC48), 14m+ 3s-rooting Poison-II Cadaverous Root-Steps, +25%-Bone-Meal-yield Necrotic Decomposition Transmutes, and milk-immune regen-blocking -40%-Toughness contagious Virulent Wither-Blight Hexes (Virulent Blood-Tasting monster empowerment).\n" +
                "▪ Astral (#27 — Ethereal & Paradox Fusion): Arcing 2-pierce 5m-airborne-launching 3s-redstone-powering 3D Celestial Comet Missiles (#41304E / #FFE600, runtime Void+Lightning Astral Materia advantage), wall-phasing micro-wormhole 1->2->4 forked 1s-stunning inward-pulling Supercharged Void-Warp Chains, mid-air lifting & pulling Pulsar Tractor Rays (3s contact -> 2s-stunning 4s-redstone Stellar Supernova Flash), 6m 8m-knockback 4m-airborne 3s-silencing Supernova Gravitational Waves, weapon-stacking 1s-stunning 2-arc 4m-zero-g-launching (+40% attack speed 10s) Cosmic Overcharge Strikes, high-jump Slow-Falling +30%-sprint Lightning/Void-fuel-absorbing Cosmic Starlight Shrouds (mobile or anchored, 0.5s melee stun + 3m knockback), disarmable Single/Repeating 4m 3m-airborne-suspending (3s) 5s-redstone Cosmic Vortex Singularity Mines, 60° 1s-stunning 4m-knockback buff-stripping 3s-redstone Pulsar Arc Sprays, cave-safe 14m+ 10s 0.5s-stunning zero-g-lifting & slamming Celestial Meteor-Storm Cataclysms, obstacle-phasing Astral-Phase Stalker / 8m pulling redstone-powering homing-orb Pulsar Singularity Spire / 5m EMP-imploding Cosmic Silhouette Effigy Eidolons (#41304E / #FFE600), 18m Slow-Falling Astral Comet Dashes, Cosmic Disintegration Transmutes (cobble & dirt -> Astral Impactite void slag & Redstone Dust), and milk-immune -70%-speed sprint/jump-locking mob-colliding Gravitational Singularity Tether Hexes (Cosmic Meteoric Attraction).\n" +
                "▪ Soulfire (#28 — Ethereal & Paradox Fusion): Homing 3m-soulfire-burst 20%-lifesteal 5s-underwater-burning 3D Screaming Soul Skulls (#1DF0E5 / #0D6469, +1 vs Frozen & Nature, -1 vs Water & Radiant, 15% Harm lifesteal), 4s-igniting 15%-lifesteal death-soulbursting (3s across all chained peers) Phantasmal Soul-Chains, 30%-armor/toughness-bypassing 20%-HP-&-fuel-siphoning Phantasmal Soul-Torch Jets (3s contact -> Soul-Sand/Bone-Meal-dissolving Soul Immolation Burst), 6m 4m-knockback 5s-underwater-soulfire 15%-lifesteal Sand->Soul-Sand Phantasmal Soul Novas, weapon-stacking 5s-underwater-soulfire 25%-lifesteal 3m-death-soulbursting Phantasmal Soul Cleaves, fire/lava-immune +40%-Soul-Speed Nether-mob-pacifying Phantasmal Soulfire Shrouds (mobile or anchored, 20% melee siphon), disarmable Single/Repeating 4m 6m-launching 6s-igniting 2.5s-landing-root 20%-lifesteal 3x3-Soul-Sand Soulflame Pyre Traps, 60° 4m-pushback shield-ignoring 6s-underwater-soulfire ice-melting Sand->Soul-Sand 15%-lifesteal Phantasmal Soul-Torch Waves, cave-safe 14m+ 10s 15%-lifesteal ice-melting Soul-Sand/Soil Phantasmal Soulfire Tempests, health-siphoning Soul-Harvester Revenant / 8m projectile-burning homing-skull Phantasmal Pyre Spire / 5m Soul-Nova Howling Soul Effigy Eidolons (#1DF0E5 / #0D6469), 12m +30%-ally-speed-puddle (4s) Spectral Rift Blinks, Nether Ash Transmutes (stone -> Netherrack, basalt -> Magma Block & sand -> Soul Sand), and milk-immune hunger-draining Slowness-II/Weakness-II underwater-burning Phantasmal Soul-Burn Hexes (Soul-Flame Beacon +30% monster empowerment).\n" +
                "▪ Vapor (#29 — Ethereal & Paradox Fusion): 3m-blinding (3s) fire-dousing 4m-thermal-updraft 3D Scalding Steam-Burst Geyser Orbs (#E0F7FA / #30A9B0, runtime Water+Nether Vapor Materia advantage), 3-way arcing secondary-blinding Hydro-Steam Split Flows, blinding 3m-pushback packed-ice-melting downward-jetpack Scalding Steam Jetstreams (3s contact -> 6m-airborne Convective Hydro-Burst), 6m 8m-airborne 6m-knockback glass-shattering Scalding Pressure Blasts, weapon-stacking 40%-armor-bypassing fire-dousing steam-cloud-dissolving Superheated Hydro-Gouges, rain/water-invisible +30%-swim/sprint Mist-Walker Shrouds (mobile or anchored), disarmable Single/Repeating 3s-erupting 10m-launching 4s-blinding 4m-torch-snuffing Scalding Geyser Pitfall Traps, 60° 6m-pushback 4s-blinding torch/fire-snuffing packed-ice-melting caster-recoil Scalding Steam-Blast Deluges, cave-safe 14m+ 12s armor-shredding (-1 per 2s up to -6) farmland-hydrating Thermal Condensation Storms, steam-punching Boiler-Knight / 8m blinding-slowing Steam-Vent Obelisk / 5m blinding-splashing Condensation Effigy Eidolons (#E0F7FA / #30A9B0), 16m invisible fog-trail Mist-Slip Blinks, Cloud Condensation Transmutes (air -> Water & mud -> Clay), and milk-immune -2-armor/3s (up to -10) -30%-speed +50%-Lightning-shock Scalding Dissolution Hexes.\n" +
                "▪ Null_R (#30 — Ethereal & Paradox Fusion): Wall-phasing 1-pierce slow-drifting 5m-inward-collapsing 1.5s-stunning 3s-silencing 3D White Hole Singularity Orbs (#FFFFFF / #41304E, +1 vs Air & Undead, -1 vs Earth & Umbral), projectile-absorbing melee-lifting Singularity Orbit Flows, zero-g pulling Event Horizon Tractor Beams (3s contact -> buff-purging 4s-blinding Annihilation Flash), 6m 4m-inward-pull -> 6m-knockback 4s-blinding buff-stripping aggro-resetting White Hole Antimatter Novas, weapon-stacking 50%-armor-bypassing buff-purging 2s-blinding 3-warp-in-6s Event Horizon Palms, high-jump zero-g-glide Blindness/Darkness-immune level-15-light 50%-arrow-deflecting 4m-repel Event Horizon Bastions (mobile or anchored), disarmable Single/Repeating 4m 3m-airborne-suspending (3s) buff-stripping 4s-blinding White Hole Singularity Traps, 60° 6m-pushback 4s-blinding buff-stripping +50%-undead-smite level-15-light Blinding Antimatter Flare Cones, cave-safe 14m+ 10s 3m-zero-g-lifting buff-stripping undead-smiting & slamming White Hole Singularity Tempests, obstacle-phasing buff-purging undead-smiting White-Hole Paladin / 8m pulling-blinding piercing-lance Event Horizon Spire / 5m White-Hole-Nova Antimatter Effigy Eidolons (#FFFFFF / #41304E), 16m mob-shoving aggro-dropping Antimatter Phase Dashes, Null-Slag Dissolution Transmutes (stone & gravel -> Astral Impactite void slag & Glowstone Dust), and milk-immune -2-armor/3s (up to -12) shield-disabling buff-suppressing Antimatter Annihilation Hexes.\n" +
                "▪ Null_U (#31 — Ethereal & Paradox Fusion): Wall-phasing 1-pierce slow-drifting 4m-black-hole-collapsing 3s-blinding aggro-wiping 3D Black Hole Singularity Orbs (#110B17 / #41304E, +1 vs Air & Radiant, -1 vs Earth), arrow-absorbing melee-pulling Abyssal Singularity Orbit Flows, silencing -1-armor/s (up to -4) pulling Void-Umbral Disintegration Streams (3s contact -> 4.5m-inward-pulling 4s-blinding Event Horizon Implosion), completely silent 6m pull-then-blast 4s-blinding aggro-wiping Abyssal Gravity Shockwaves (granting 4s caster Invisibility & silence), weapon-stacking 50%-armor-bypassing 2.5s-shadow-rooting 3-warp-in-6s 25%-lifesteal Spacetime Oblivion Palms, zero-vibration Sculk/Warden-immune -70%-mob-detection high-jump zero-g-glide 50%-arrow-deflecting 3s-blind/silence-retaliating Abyssal Zero-Acoustic Veils (mobile or anchored), disarmable Single/Repeating 4m 3s-floor-pinning 4s-blinding aggro-wiping Shadow-Snare Singularity Mines, 60° 6m-pushback 4s-blinding aggro-wiping -3-armor (6s) torch/fire-snuffing Void-Umbral Disintegration Waves, cave-safe 14m+ 10s inward-pulling 3m-zero-g-lifting blinding & slamming Black Hole Oblivion Tempests, wall-phasing silent 50%-armor-piercing Void-Shadow Stalker / 8m silencing pulling-dart Oblivion Singularity Spire / 5m Abyssal-Gravity-Shockwave Abyssal Silhouette Effigy Eidolons (#110B17 / #41304E), 16m silent departure-singularity Abyssal Event Horizon Blinks, Abyssal Dark Matter Transmutes (cobble, stone & ores -> Astral Impactite dark matter slag & Blackstone), and milk-immune Blindness/Silence -40%-speed regen-locking Sensory Blackout Hexes (cleansed by Radiant/Restoration magic or eating Mushroom Stew).\n" +
                "▪ Dawn (#32 — Ethereal & Paradox Fusion): Zero-g straight-flying 3-pierce slipstream-pushing 5s-Glowing undead-igniting 3D Solar Horizon Javelins (#FFEA78 / #A88654, +1 vs Undead & Earth, -1 vs Umbral & Void), Slow-Falling arrow-deflecting melee-hurling Convective Horizon Orbit Flows, blinding staggering 20%-fuel-siphoning Pranic Solar Lances (3s contact -> 6m-knockback 4s-blinding Sunrise Flash), 6m 8m-knockback 3m-airborne 4s-Blindness/Glowing 2x-undead-smiting gas/cobweb-clearing Sunrise Horizon Novas (no dynamic light blocks), weapon-stacking 3m-frontal-sweep arrow-deflecting 4m-knockback (+30% sprint & attack speed 5s) Aero-Solar Cleaves, high-jump Slow-Falling Blindness/Darkness-immune 40%-arrow-deflecting 3m-gale/2s-blind-retaliating Dawn-Bringer Solar Shrouds (mobile or anchored), disarmable Single/Repeating dual-purpose Pranic Sunrise Wells (4-heart heal & debuff cleanse for allies OR 8m gale blast vs hostiles), 60° 5m-pushback 6s-undead-solar-fire 6s-Glowing Solar Flare Breaths, cave-safe 14m+ 10s 4m-airborne-launching undead-smiting Darkness/Blindness-stripping 1-HP/s-ally-healing Sunrise Daybreak Tempests, glider-fast blinding Dawn-Strider / 8m Slow-Falling-&-Speed piercing-javelin Sol-Zephyr Spire / 6m Sunrise-Horizon-Nova Daybreak Mirage Effigy Eidolons (#FFEA78 / #A88654), 14m silent 1-debuff-cleansing +30%-sprint (5s) Solar Zephyr Blinks, Daybreak Awakening Transmutes (Zombie Villager -> Villager, blackstone/deepslate/dark-oak -> sandstone/terracotta/birch & Rotten Flesh -> Leather), and milk-immune Blindness/Glowing -3-armor undead-burning Solar Blind-Sear Hexes.\n" +
                "▪ Abyss (#33 — Ethereal & Paradox Fusion): 3m-vortex-imploding 3s-Blindness/Slowness-II fire-dousing 3D Abyssal Depth Charge Orbs (#211A21 / #225C7C, +1 vs Radiant, Nether & Nature, -1 vs Void & Frozen), fire-dousing melee-absorbing (-40% speed & knockback) Abyssal Maelstrom Orbit Flows, -60%-speed silencing Hydrostatic Trench Torrent Beams (3s contact -> 4m-inward-pulling -3-armor Benthic Implosion), silent 6m 6m-knockback 4s-Blindness/Slowness-III aggro-wiping target-drenching fire-extinguishing Benthic Inversion Shockwaves, weapon-stacking 5m-knockback 4s-land-drowning aggro-dropping Abyssal Drowning Cleaves, Water-Breathing +60%-swim Sculk/Warden-immune -60%-mob-detection ink-blinding Hadopelagic Trench Shrouds (mobile or anchored), disarmable Single/Repeating 4m 3.5s-pinning land-drowning blinding Benthic Maelstrom Pitfalls, 60° 6m-knockback 4s-Blindness/Slowness-II aggro-wiping drenching torch/fire-snuffing Abyssal Ink-Blast Deluges, 2s-telegraphed 3D-monolith 10m-tidal-wave black-water Crushing Trench Monoliths, tentacle-lashing Tentacled Kraken-Husk / 8m whirlpool Trench Spire / 5m silencing-ink-cloud Hadopelagic Effigy Eidolons (#211A21 / #225C7C), 14m zero-sound zero-particle liquid/shadow aggro-dropping Benthic Trench Slips, Hadopelagic Erosion & Darkening Transmutes (sand -> Clay, gravel -> Cobblestone, lava -> Crying Obsidian, Glowstone/Sea Lantern -> Dark Prismarine & smooth stone -> cracked bricks), and milk-immune land-drowning -40%-speed silencing 2x-Lightning Crushing Benthic Drowning Hexes (Hadopelagic Helplessness 2x aquatic monster damage & +50% swim speed).\n" +
                "▪ Aegis (#34 — Ethereal & Paradox Fusion): 5m-knockback 2.5s-rooting temporary-rampart-raising 3D Gilded Bastion Shield-Slugs (#FFEA78 / #773214, +1 vs Void & Undead, -1 vs Umbral, Air & Nature), +4-Armor arrow-deflecting melee-battering Gilded Bastion Orbit Flows, enemy-repelling -1-armor/s Citadel Bastion Tether Beams (ally link -> +8 Armor & 50% damage reduction), 6m 8m-knockback +50%-undead-smite spike-raising (+4 ally Armor & 50% KB resist) Gilded Bulwark Rampart Novas, weapon-stacking 6m-knockback 1.5s-stunning +50%-undead-smite -3-armor (+4 caster Armor 5s) Gilded Shield-Bash Strikes, +10-Armor +4-Toughness 75%-KB-resist Blindness/Darkness-immune -25%-physical-damage 40%-thorns Gilded Bastion Citadel Mantles (mobile or anchored, -10% speed), disarmable Single/Repeating 4m 4s-caging -4-armor projectile-blocking Citadel Rampart Vault Mines, 60° 6m-knockback 1s-stunning -3-armor +50%-undead-smite projectile-deflecting Gilded Granite Shrapnel Deluges, 2s-telegraphed 3D-megalith 14m-shockwave monument-raising Gilded Citadel Megalith Cataclysms, stunning armor-shredding Bulwark-Paladin / 8m +6-Armor Citadel Fortress Spire / 6m Bulwark-Rampart-Nova Gilded Granite Effigy Eidolons (#FFEA78 / #773214), 12m 15-HP-tower-shield-decoy (+6 Armor & 50% KB resist 4s) Bulwark Phalanx Steps, curse-cleansing Gilded Petrofaction Transmutes (cobble/deepslate -> Polished Granite/Calcite/Quartz, dirt/gravel -> Clay, Rotten Flesh/Bone -> Bone Block & flowers -> Aegis Rose), and milk-immune -40%-Toughness -40%-speed jump-locking +35%-blunt-damage Petrifying Citadel Sundering Hexes (Fractured Bastion +40% armor-ignoring mob strikes).\n" +
                "▪ Aurora (#35 — Ethereal & Paradox Fusion): Zero-drop straight-flying 3-pierce shard-refracting 2s-freezing 5s-Glowing 3D Prismatic Aurora Ribbon-Lances (#FFEA78 / #98DCF2, +1 vs Undead, Nether, Earth & Nature, -1 vs Void), item-pulling fire-quenching Slowness-II Glacial Aurora Spiral Flows, Slowness-IV-stacking Glowing Borealis Prismatic Spectrum Rays (3s contact -> 1.5s-stunning Prismatic Flashbang Shatter), 6m 6m-knockback 2s-stasis-freezing 4s-Blindness/Glowing +50%-undead-smiting Borealis Prismatic Curtain Novas, weapon-stacking 1.5s-solid-freezing +50%-undead-smiting 6s-Glowing -2-armor-shattering Prismatic Aurora Polar Cleaves, +4-Armor Frost-Walker freeze/powder-snow-immune Blindness/Darkness-immune level-12-light Borealis Prismatic Veil Mantles (mobile or anchored, melee Blindness + 2s Frostbite retaliation), disarmable Single/Repeating projectile-reflecting 3s-ice-rooting Cryo-Refractive Mirror Traps, 60° 5m-knockback 2s-freezing 3s-Glowing/Blindness +50%-undead-smiting frosted-ice Borealis Prismatic Fan Deluges, 6s 5-meteor 3D-blue-ice-comet -2-armor Cryo-Solar Comet Barrages, ribbon-lance Borealis-Archon / 8m Frost-Walker & projectile-deflecting Prismatic Aurora Spire / 6m Borealis-Curtain-Nova Cryo-Prism Effigy Eidolons (#FFEA78 / #98DCF2), 14m 2s-frozen-hologram Frost-Walker +30%-sprint (4s) Borealis Prism-Flash Steps, flow-free Polar Cryo-Refraction Transmutes (water -> Packed/Blue Ice, lava -> Obsidian/Crying Obsidian, glass -> Tinted Glass & flowers -> Auroral Buttercup), and milk-immune -50%-attack-speed -40%-speed Glowing frostbite Sub-Zero Polar Stasis Hexes.\n" +
                "▪ Mirage (#36 — Ethereal & Paradox Fusion): 2-decoy-splitting 3s-blinding 3s-traitor-scrambling shifting-sand 3D Shimmering Mirage Sand-Darts (#FFEA78 / #C2B28F, +1 vs Water, Void, Blight & Undead, -1 vs Umbral & Nature), 25%-projectile-deflecting level-12-light melee-blinding Mirror Mirage Spiral Flows, 50%-damage-redirecting blinding Phantasm Mirage Tether Beams (3s contact -> 1.5s-stunning mob-taunting Sensory Overload Flash), 6m 6m-knockback 4s-Blindness/Nausea aggro-wiping 3-clone (5s) Shimmering Fata Morgana Shockwaves, weapon-stacking 3s-air-swinging 5s-friend-foe-inverting caster-hiding Phantasmal Paranoia & Traitor Strikes, 3m-displaced 50%-dodge phantom-double Spatial Displacement Glamour Shrouds (mobile or anchored), disarmable Single/Repeating 12m-mob-luring 3m-quicksand (3.5s) False Oasis Lure Glyphs, 60° 4s-Blindness/Nausea 3s-traitor-swinging Hallucinatory Mirage Shimmer Deluges, 10s 8m+ panic-dodging suffocating Mirage Sand-Storm Tempests, 2-afterimage paranoia-striking Mirage-Duelist / 12m illusion-luring Fata Morgana Obelisk / blinding-traitor-bursting Phantom Mirror Effigy Eidolons (#FFEA78 / #C2B28F), 14m 3s-running-clone 2.5s-invisible Fata Morgana Displacement Steps, Hallucinatory Glazing Transmutes (stone/cobble -> Sandstone, deepslate -> Red Sand, lava -> Glass, terracotta -> Sand & Rotten Flesh -> dried jerky), and milk-immune 50%-miss -40%-Toughness swerving caster-hiding Delusional Phantasm Paranoia Hexes (Paranoid Guard-Fumble +35% mob crit & 40% shield fumble).\n" +
                "▪ Wraith (#37 — Ethereal & Paradox Fusion): Wall-phasing homing 2-pierce 3s-Wither/Slowness-II 2-heart-leeching 3D Spectral Wraith-Skull Missiles (#41304E / #4D6453, +1 vs Air, Umbral, Aura & Frozen, -1 vs Radiant & Earth, 20% Harm lifesteal), +4-Armor 20%-physical-to-hunger Incorporeal Soul-Shield Flows, wall-phasing 1.5-heart/s-leeching -50%-speed Incorporeal Soul-Siphon Tether Beams (3s contact -> soul-husk-tearing 1.5s-stunning Soul Severance Detonation), 6m 3s-intangible-disarming -3-armor summon-healing Incorporeal Ghost-Storm Shockwaves, weapon-stacking shield/toughness-bypassing 2-heart-stealing -3-armor 1.5s-stunning Spectral Phasing Palm-Gouges, +8-Armor 100%-KB-immune 25%-magic-reflecting suffocation/wither-immune Sculk/Warden-silent Specter Ectoplasm Armors (mobile or anchored), disarmable Single/Repeating 4s-intangible-stasis life-siphoning Incorporeal Grave-Grip Pitfall Glyphs, 60° 6m-knockback shield-bypassing 3.5s-Blindness/Wither-II -3-armor 3s-silencing Banshee Wail Ectoplasm Deluges, 12m+ swirling 3D-skull Wither-II/Slowness-III Spectral Rift Skull-Vortex Cataclysms, wall-phasing siphoning Wraith-Stalker / 8m silencing -40%-speed Banshee Wail Obelisk / 6m Ghost-Storm-Nova Ectoplasmic Soul-Husk Effigy Eidolons (#41304E / #4D6453), 12m silent aggro-dropping 2s-speed/intangible Grave-Slip Blinks, Ghostly Ash Transmutes (dirt/grass -> Soul Soil, leaves/foliage -> Cobweb, cobble -> Mossy Cobblestone & Bone Block -> Soul Sand), and milk-immune -50%-damage -40%-speed regen-locking life-transferring Incorporeal Soul-Hollowing Hexes.\n" +
                "▪ Barrow (#38 — Ethereal & Paradox Fusion): 6m-knockback 2.5s-mud-rooting burial-earth 3D Tombstone Ossuary Boulder-Slugs (#773214 / #4D6453, +1 vs Frozen, Amber & Void, -1 vs Radiant, Air & Nature), -25%-physical bone-shrapnel Ossuary Bone-Shield Orbit Flows, downward-dragging -60%-speed -1-armor/s Barrow-Tomb Necrotic Tether Beams (3s contact -> 2s stone-sarcophagus Sepulchral Entombment), 6m Wither/Poison-II 5s-blinding undead/summon-healing (+4 caster Armor) Catacomb Miasma Eruptions, weapon-stacking 6m-knockback 1.5s-stunning 3s-Slowness-IV -3-armor (+3 caster Armor 5s) Barrow-Cairn Tombstone Slams, -40%-mob-detection 4m-Wither/Slowness poison/wither-immune Night-Vision Catacomb Miasma Shrouds (mobile or anchored), disarmable Single/Repeating -3-armor 6m-airborne-launching Cairn Collapse Mines, 60° 6m-knockback -3-armor 3.5s-Blindness/Slowness-III torch/fire-smothering coarse-dirt/gravel Ossuary Bone-Grit & Tomb-Dust Deluges, 6s 10m+ 6-megalith 3D-pseudoblock -2-armor Megalithic Ossuary Cataclysm Downpours, shield-&-flail taunting/stunning Barrow Skeleton / 8m -40%-speed +4-summon-Armor Cairn Beacon / 5m bone-shrapnel Hollow Bone Effigy Eidolons (#773214 / #4D6453), 14m taunting-burial-mound (+4 Armor & 50% KB resist 4s) Subterranean Crypt-Tunnel Steps, Megalithic Grave Transmutes (dirt/grass/mud -> Coarse Dirt/Mossy Cobblestone/Cracked Stone Bricks, Rotten Flesh/raw meat -> Bone Block, cobble -> Mossy Cobblestone & wood -> petrified wood), and milk-immune -40%-move/attack-speed jump-locking -40%-Toughness +35%-physical/blunt Petrifying Sepulchral Entombment Hexes (Catacomb Awakening +100% armored skeleton spawn-lure).\n" +
                "▪ Aureole & Vesper (Ethereal Atmospheric Fusions): Shimmering Aureole (#FFF59D / #81D4FA, Radiant + Air, +1 vs Undead, Earth & Void, -1 vs Umbral: stealth-revealing Luminous Halo Chakrams, arrow-deflecting Luminous Halo Orbit Flows, 3.0s-bursting Aureole Zenith Rays, ally-cleansing Solar Halo Sanctification Novas, Slow-Falling Crowned Halo Arc-Slashes & Aureole Nimbus Crowns, dual-purpose Sanctified Halo Ward-Sigils, Refractive Halo Gales, 10s Zenith Aureole Star-Rains, Aureole Halo-Vanguard / Celestial Crown Spire / Luminous Nimbus Effigy Eidolons, Halo Ascent Steps, 2.5s-channeled Sanctified Halo Transmutes & milk-immune Aureole Halo Brand Hexes) and whispering Vesper (#5C6BC0 / #2A3B4C, Umbral + Air, +1 vs Radiant, Water & Earth, -1 vs Void: silent aggro-dropping Eventide Dusk Wind-Blades, footstep-cloaking Eventide Vesper Orbit & Whispering Dusk Chain Flows, 3.0s-pulsing Vesper Twilight Gale Tethers, Invisibility-granting Nocturne Whisper Shockwaves, Sculk-silent Eventide Zephyr-Scythes & Cloaks of the Evening Star, Vesper Hush-Wind Traps, Twilight Gale Shears, 10s Vespera Comet Dusk-Tempests, Eventide Vesper-Wraith / Nocturne Gale Obelisk / Dusk-Whisper Silhouette Eidolons, Eventide Whisper-Steps, 2.5s-channeled Eventide Dusk Weathering Transmutes & milk-immune Vesper Nocturne Silence Hexes).\n" +
                "▪ Radiant-Fire, Umbral-Water & Equinox (Caprine Secret Fusions): Purging Radiant-Fire (#FFEA78 / #FF3C00, Radiant + Nether, +1 vs Undead, Frozen & Nature, -1 vs Umbral & Water: Radiant Fire Lances, Solar Prominence Jetstreams, Helios Corona Eruptions, -3-armor Caprine Sunfire Cleaves, Solar-Prominence Seraph / Helios Pyre Obelisk / Incandescent Corona Effigy Eidolons & 2.5x Steam-Cavitation shockwaves against drenched foes), pressurized Umbral-Water (#211A21 / #14508C, Umbral + Water, +1 vs Radiant, Nature & Nether, -1 vs Void & Frozen: inward-pulling Umbral Water Vortex Orbs, +0.4 Slow Pressurized Cavitation Undertow Flows, Abyssal Cavitation Torrents, -3-armor Cavitation Undertow Cleaves, Cavitation Trench-Stalker / Umbral Undertow Spire / Submerged Shadow Effigy Eidolons & Abyssal Drench priming), and Apex Equinox (#FFEA78 / #FF5000 / #225C7C, Radiant + Nether + Umbral + Water, +1 vs Undead, Frozen, Nature, Nether & Radiant, -1 vs Void & Umbral: twin-polarity Equinox Phase-Inversion Lance-Orbs, Gemini Duality Convergence Beams, Equinox Steam-Cavitation Novas, -4-armor Gemini Scepter Duality Cleaves, Gemini Equinox Archon / Phase-Inversion Crucible Spire / Twin-Polarity Mirage Effigy Eidolons & 2.5s-channeled Phase-Inversion Transmutes).",
                "spell_vital_fusion_mutations", 6, new ItemStack(Items.END_CRYSTAL),
                910f, 0.0f, a_magic + 0.00f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_trinity_and_catalyst_mutations", "Trinity & Catalyst Spell Mutations", "MAGIC",
                "Signature Flow, Form, and Core Effect mutations for Pyre through Oblivion (#39-#47) and Chimera.",
                "Overview & Description:\n" +
                "Tier 3 Trinity Fusions (#39-#47) and the failed-fusion Chimera Catalyst unleash high-tier sustained beams (5.0s detonation thresholds), 2.5-second held Right-Click channeled Transmutations, 18-second cataclysmic Skyfall storms, and 45-second refreshing Eidolon constructs (including Pyre #39 at 70/85 HP).\n\n" +
                "Trinity & Catalyst Lineup:\n" +
                "▪ Pyre (#39 — Tier 3 Trinity Fusion): 3.5m-firestorm 5s-wildfire 5m-knockback ice/snow-vaporizing 3D Combustion Hearth Orbs (#773214 / #FF4500, +1 vs Void, Frozen, Nature, Glacial & Rime, -1 vs Air), +3-Armor arrow-melting 4s-attacker-burning dart-launching Cremating Wildfire Orbit Flows, projectile-disintegrating -1-armor/1.5s Cremating Wildfire Jetstream Beams (3s contact -> 4.5m 1.5s-stunning 6s-igniting Cataclysmic Flashover), 6m+ 7m-knockback 6s-wildfire plant-charring (+25% caster melee 8s) Raging Wildfire Pyre Novas, weapon-stacking 6s-wildfire -3-armor 5m-knockback ember-arc Cremating Timber-Pyre Cleaves, +8-Armor +2-Toughness Fire/Lava/Freezing-immune ice-melting Pyre Hearth Hearthstone Shrouds (mobile or anchored, -10% speed), disarmable Single/Repeating 4m 4s-pitfall -3-armor jump-locking Cremation Pyre-Pitfall Glyphs, ice-melting -1-armor/2s meat-cooking sand->Glass Combustion Hearth Flame-Throwers, 10s 8m+ 8-meteor 3D-pseudoblock -2-armor fiery-crater Cremating Pyre Cataclysm Downpours, 45s refreshing 70-HP wildfire-cleaving Pyre-Juggernaut / 85-HP 8m +4-summon-Armor Cremation Pyre Spire / 6m Wildfire-Nova Smoldering Hearth Effigy Eidolons (#773214 / #FF4500), 14m 3s-burning-decoy Fire-Immune +30%-sprint (4s) Wildfire Flashover Steps, 2.5s-channeled Pyre Ash-Charcoal Transmutes (logs -> Coal Block +20% Charcoal, sand -> Glass, raw food -> cooked, dirt/grass -> Podzol/Rooted Dirt, cobble -> Smooth Stone/Magma Block & saplings/flowers -> Pyre-Ash Cedar/Pyre-Sprout), and milk-immune underwater-burning -40%-Toughness sprint/regen-locking ember-showering Unquenchable Wildfire Cremation Hexes.\n" +
                "▪ Penumbra (#40 — Tier 3 Trinity Fusion): Wall-phasing 3.5s-blinding 3m-pulling 3D Penumbral Twilight Eclipse-Bolts (#FFEA78 / #281E3C, +1 vs Air, Radiant, Nature, Celestial & Aegis [or Undead under Radiant dominance], -1 vs Earth, Void & Undead), 4-sphere +5-Armor +2-Toughness Blindness/Darkness-immune arrow-dissolving melee/spell-hit dart-launching Penumbral Corona-Umbra Binary Orbit Flows, Blindness/Darkness-oscillating Penumbral Event-Lensing Rays (5.0s contact -> 4m-pulling Penumbral Convergence Flash), zero-light-block 6m+ 6m-knockback 4s-Blindness/Glowing undead-smiting (+20% ally Twilight Shimmer dodge 6s) Twilight Corona Novas, weapon-stacking armor-bypassing 3s-blinding -35%-slow-wake (4s) Penumbral Eclipse Cleaves, +6-Armor +2-Toughness Blindness/Darkness-immune 25%-projectile-glancing 30%-attacker-dazzle Penumbral Corona Mantles (mobile or anchored), disarmable Single/Repeating 5m 4s-pulling Penumbral Twilight Trap Glyphs, 60° projectile-vaporizing 3m-knockback 25s-AI-disrupting Penumbral Corona-Twilight Fan Deluges, 18s+8s 12m+ 5m-pulling -5-armor 6s-blinding -60%-speed Penumbral Cataclysmic Eclipse Tempests, 45s refreshing 60-HP twin-scythe Penumbral Void-Bladewarden / 80-HP twin-lance +6-Armor +3-Toughness debuff-cleansing Twilight Corona Citadel / 10m 8s-silencing Volatile Eclipse Singularity Effigy Eidolons (#FFEA78 / #281E3C), 16m 2s-imploding-decoy +30%-sprint +25%-dodge (5s) Penumbral Horizon-Phase Steps, curse-purging Penumbral Eclipse Transmutes (Obsidian/Crying Obsidian <-> End Stone/Sculk, Glowstone <-> Sea Lantern/Shroomlight & Rotten Flesh -> Penumbra Essence), and milk-immune 50%-miss AI-crippling -35%-Toughness regen-locking Penumbral Contrast-Collapse Hexes.\n" +
                "▪ Rime (#41 — Tier 3 Trinity Fusion): 3-pierce 3s-rooting 3.5m-shattering (-50% speed 4s) 3D Hoarfrost Rime-Spike Javelins (#6395B1 / #DCF0FF, +1 vs Earth, Nature, Magma & Overgrowth [plus Nether & Arid under Water dominance], -1 vs Void & Undead), 4-icicle +5-Armor +2-Toughness Freeze/Powder-Snow/Slowness-immune arrow-shattering -30%-attacker-chill melee/spell-hit 2s-rooting-dart Crystalline Hoarfrost Spiral Flows, Slowness-IV-stacking Glacial Absolute-Zero Jetstream Beams (5.0s contact -> 2.5s solid-freezing Deep Freeze Shatter), zero-light-block 6m+ 6m-knockback 3s-flash-freezing (+4 ally Armor & 50% fire resist 8s Hoarfrost Carapace) Flash-Freeze Rime Frost-Novas, weapon-stacking -3-armor 2.5s-solid-freezing 5m-knockback 2s-rooting-wake Permafrost Rime-Cleaves, +7-Armor +3-Toughness Freeze/Powder-Snow/Slowness/Fire-immune Packed-Ice-underfoot -35%-attack-speed Hoarfrost Glacial Aegis Mantles (mobile or anchored), disarmable Single/Repeating 4m 4s-chasm 3.5s-rooting -3-armor jump-locking Flash-Freeze Rime Pitfall Glyphs, 60° fire/torch-quenching water-freezing 3m-knockback Slowness-IV -50%-attack-speed 25s-AI-disrupting Sub-Zero Blizzarding Rime Cones, 18s+8s 12m+ 10-stalactite 3D-pseudoblock -5-armor 6s-freezing -60%-speed Glacial Rime Cataclysmic Blizzards, 45s refreshing 65-HP 35%-armor-bypassing 2.5s-freezing Glacial Rime-Vanguard / 85-HP 16m-piercing 8m-root-pulsing +6-Armor +3-Toughness Fire/Freeze-immune Hoarfrost Glacial Citadel Spire / 10m 3.5s-freezing -4-armor Brittle Permafrost Effigy Eidolons (#6395B1 / #DCF0FF), 18m packed/blue-ice Frost-Walker +30%-speed (6s) Permafrost Glacial Slides, Glacial Cryo-Crystallization Transmutes (water -> Packed/Blue Ice, lava -> Obsidian/Crying Obsidian, dirt/grass -> Permafrost, mud -> Clay, sand -> Quartz/Calcite & air vapor -> water/ice), and milk-immune enchantment-suppressing -30%-Toughness 0.5s/3s-stunning Sub-Zero Cryo-Numbness Hexes.\n" +
                "▪ Spring (#42 — Tier 3 Trinity Fusion): Ally-mending (3 hearts + Regen II 4s) / Undead-smiting (+50% damage & 3s root) 5m-knockback vernal-flora 3D Vernal Blossom Aquifer-Orbs (#FF6B9D / #64C8FF, +1 vs Undead, Water, Earth, Soulfire & Blight [plus Nether & Arid under Water dominance], -1 vs Umbral), 4-droplet +5-Armor +2-Toughness Drowning/Poison/Wither-immune projectile-to-1-HP-mist melee-siphoning melee/spell-hit 2s-rooting/ally-healing Vernal Blossom-Aquifer Spiral Flows, 3-HP/s-siphoning Slowness-III / ally-cleansing (+30% attack speed) Photosynthetic Life-Tethers (5.0s contact -> 8m-knockback +4-Absorption-Hearts Vernal Bloom Supercharge), zero-light-block 6m+ 7m-knockback Undead-smiting crop-maturing (+20% speed, +3 Armor & Regen II 8s Vernal Vigor) Vernal Rebirth Blossom-Novas, weapon-stacking +50%-Undead-smite -3-Undead-armor 5m-knockback 2.5s-rooting/2-heart-healing Vernal Blossom-Cleaves (or ally Touch of Spring debuff cleanse + 3 Absorption Hearts), +6-Armor +2-Toughness Drowning/Poison/Wither/Fire-immune Water-Breathing +30%-swim 1-heart/3s-regen 5m-ally-healing/cleansing Vernal Fountainhead Mantles (mobile or anchored), disarmable Single/Repeating dual-purpose Bubbling Vernal Aquifer Well Glyphs (ally 5-heart heal + cleanse + Regen II 6s OR hostile 4m 4s Geyser Chasm -3 armor & Undead holy-water drowning), 60° ally-healing (1.5 hearts/s) 3m-knockback -50%-attack-speed 25s-AI-disrupting Vernal Rejuvenation Torrent Cones, 18s+8s 12m+ -5-armor Undead-smiting 1-heart/s-healing hunger-filling Regen-II/Resistance-I crop-maturing Vernal Miracle Downpours, 45s refreshing 65-HP 35%-armor-bypassing 2-heart-pollen-healing Vernal Bloom-Bladewarden / 85-HP 16m-piercing 8m-3-heart-healing/cleansing +6-Armor +3-Toughness Water-Breathing Aquifer Springwater Font Spire / 10m 8m-knockback -4-armor full-ally-restore Regen-III Blossoming Vernal Nectar Effigy Eidolons (#FF6B9D / #64C8FF), 14m aggro-shedding 4s-Undead-undetectable Petal-Warp Blinks, Vernal Purification & Greening Transmutes (Netherrack/Soul Sand/Sculk -> Grass/Moss/Farmland, 6m Farmland hydration, Zombie Villager curing, Rotten Flesh -> Leather/Beef/Pork, crop maturation & fluid/potion purification), and milk-immune 1s/4s-stunning -50%-speed -40%-attack-speed -35%-Toughness Undead-smiting 1-heart-dew-orb-dropping Uncontrolled Vernal Overgrowth Hexes.\n" +
                "▪ Static (#43 — Tier 3 Trinity Fusion): 3m-in-flight-zapping armor-magnetizing 5m-EM-burst 5m-knockback -3-armor 3D Dielectric Ball-Lightning Mortars (#FFE600 / #878DA2, +1 vs Earth, Water, Void, Glacial & Vapor, -1 vs Nature [plus Void under Air dominance]), 4-sphere double-helix +5-Armor +2-Toughness Lightning/Fall/Slowness-immune metallic-arrow-vaporizing 1s-attacker-paralyzing melee/spell-hit 2s-rooting 3-target-arcing Galvanic Tesla Orbit Flows, secondary-arcing -1-armor/1.5s item-magnetizing Dielectric Breakdown Arc-Stream Beams (5.0s contact -> 4.5m 3.0s-paralyzing Catastrophic Dielectric Flashover), zero-light-block 6m+ creeper-disabling aggro-clearing 2.5s-paralyzing Ground-Discharge EMP Shockwaves, weapon-stacking creeper-defusing 8m-knockback Dielectric Palm Overloads, +7-Armor +3-Toughness Lightning/Fall/Slowness/Kinetic/Thorns-immune +25%-sprint/attack-speed arrow-vaporizing 1.5s-paralyzing Dielectric Faraday Shrouds (mobile or anchored), disarmable Single/Repeating 8m 5-hostile-tethering -3-armor shared-shock Galvanic Arc-Snare Sigils, 60° arrow-melting 3m-knockback weapon-magnetizing -50%-attack-speed 25s-AI-disrupting High-Voltage Static Arc-Fan Deluges, 18s+8s 12m+ downward-pinning -5-armor 3s-paralyzing creeper-disabling -60%-speed sprint-shocking Galvanic Tesla Cataclysmic Tempests, 45s refreshing 65-HP twin-khopesh dash-striking 35%-armor-bypassing 2s-paralyzing Galvanic Arc-Vanguard / 85-HP 16m-lightning-beam 8m-EMP-pulsing +6-Armor +3-Toughness Lightning/Fall-immune Dielectric Tesla Citadel Spire / 10m 8m-knockback -4-armor 4s-paralyzing Volatile Dielectric Spark Effigy Eidolons (#FFE600 / #878DA2), 14m silent aggro-shedding spark-trail Ionized Slipstream Steps, Dielectric Electrolysis & Vitrification Transmutes (raw iron/copper ores & items -> refined ingots +25% bonus yield, sand -> Glass/Tinted Glass, cobble/gravel -> Deepslate, machinery/battery recharging & water -> dry air), and milk-immune 3s-interrupting -50%-speed -40%-attack-speed -35%-Toughness 4m-secondary-arcing Galvanic Nerve-Spasm Hexes.\n" +
                "▪ Sylvan (#44 — Tier 3 Trinity Fusion): +50%-Undead-smiting 3s-rooting -3-armor 4s-briar-cluster (-50% speed) 3D Elderwood Sun-Bough Javelins (#1A5923 / #773214 / #FFEA78, +1 vs Water, Earth, Undead, Barrow & Wraith [or Void, Undead, Barrow & Wraith under Earth dominance], -1 vs Nether & Air), 4-prism +20%-spell-damage +5-Armor +2-Toughness Poison/Wither-immune sunlight-Materia-regenerating 1.5s-attacker-rooting melee/spell-hit 2s-rooting-dart Photosynthetic Sun-Canopy Orbit Flows, 2m/s-pulling Undead-combusting 1-heart/s-ally-healing Canopy Solar-Root Tether Beams (5.0s contact -> 8m-knockback 3.0s-root-cage Primeval Arbor Overgrowth), zero-light-block 6m+ 7m-knockback Undead-smiting 3s-rooting crop-maturing (+6 Armor, +2 Toughness & KB resist 8s Ironwood Barkskin) Primeval Sylvan Canopy Novas, weapon-stacking +50%-Undead-smite -3-armor 6m-knockback 2.5s-rooting 2s-root/ally-Regen-I-arc Elderwood Canopy-Cleaves, +7-Armor +3-Toughness Poison/Wither/Blindness-immune 12-hit 50%-damage-to-Materia-fuel Primeval Photosynthetic Aegises (mobile or anchored), disarmable Single/Repeating 3m-proximity 5s-blinding airborne-launching -3-armor Canopy Solar-Mines, 60° ally-Poison/Wither-purging arbor-flame -50%-attack-speed 25s-AI-disrupting Canopy Solar-Flare Breaths, 16s+8s 10m+ 8-boulder 3D-pseudoblock & 0.4s-seed airborne-launching -5-armor Undead-smiting -60%-speed Sun-Canopy Orbital Meteor Barrages, 45s refreshing 70-HP 35%-armor-bypassing 2.5s-rooting Ironwood Juggernaut / 85-HP 16m-boulder 8m-root-wave +6-Armor +3-Toughness KB-resistant Petrified Monolith Sentry / 35%-melee-reflecting 10m -4-armor 4s-root-cage Thorn-Bramble Effigy Eidolons (#1A5923 / #773214 / #FFEA78), 16m upward/forward 6s-Slow-Falling/fall-immune aggro-shedding Canopy Solar-Glide Steps, Petrified Amber-Bark Fusion Transmutes (logs -> Amber-Wood, clay -> floral Terracotta, 6m crop/sapling maturation & Netherrack/Soul Sand -> Moss/Ferns), and milk-immune 1s/3.5s-stunning -60%-speed -40%-attack-speed -35%-Toughness +50%-Undead 1-heart-golden-acorn Petrifying Arbor-Root Entombment Hexes.\n" +
                "▪ Miasma (#45 — Tier 3 Trinity Fusion): Airborne-knocking -3-armor 4s-blinding-cloud 3D Putrid Swampland Gas-Pods (#1A5923 / #4D6453 / #225C7C, +1 vs Frozen, Earth, Oasis & Taiga [or Nether, Arid, Frozen, Oasis & Taiga under Water dominance], -1 vs Umbral, Radiant & Nether), 4-target 6m 60%-damage Slowness-II -25%-Toughness (5s) Virulent Contagion-Chain Flows, 1.5m/s-dragging 1-heart/1.5s-draining Decomposing Quagmire Siphon Rays (5.0s contact -> downward-dragging 4.0s-root Wither-II Swamp Quagmire Collapse), zero-light-block 6m+ 7m-knockback -3-armor aggro-resetting (10s Poison/Wither/Hunger Septic Immunity) Putrescent Swamp-Blight Novas, weapon-stacking -3-armor 6s-Poison-II 3m-sludge-puddle (2.5s root & +25% incoming damage) Septic Quagmire Palm-Gouges, +7-Armor +3-Toughness Poison/Wither/Hunger/Drowning-immune 35%-melee-reflecting (-2 attacker armor) water/mud-solidifying Putrescent Mire-Carapaces (mobile or anchored), disarmable Single/Repeating 8m-launching buff-purging +50%-fall-vulnerability (6s) Pressurized Septic Gas-Geysers, projectile-repelling 2-hunger-bar-draining 25s-AI-disrupting 3s-interrupting Choking Bog-Gas Fans, 18s+8s 12m+ 8-boulder 3D-pseudoblock flat-knocking -5-armor -60%-speed poisoning Cataclysmic Miasmic Bog-Downpours, 45s refreshing 65-HP mud-ambushing 2-heart-siphoning 3s-blinding Septic Bog-Stalker / 80-HP 3s 6m airborne-launching -2-armor buff-purging Cadaverous Geyser Pylon / 12m-taunt 10m -3-armor 4s-blinding Decomposing Bog-Husk Eidolons (#1A5923 / #4D6453 / #225C7C), 14m enemy- & fence/bar-phasing 3s-coughing aggro-shedding Putrid Miasma Phase-Shifts, 2.5s-channeled Virulent Quagmire & Peat Fusion Transmutes (dirt/grass/mud -> 3x-mushroom/wart double-harvest Bog Peat, water -> Poison-II-bottling Algae Swamp Water & rotten flesh/spider eyes -> +25% slime balls & bone meal), and milk-immune -25%-Max-HP regen-locking -35%-Toughness 3.5s-bile-vomiting Virulent Septic Pestilence Hexes (+120% slime/witch/arthropod Pestilence Pheromone Trail).\n" +
                "▪ Genesis (#46 — Tier 3 Trinity Fusion): +50%-Undead/Oblivion/Wraith-smiting (-3 armor) 4m-amniotic-splash (3-heart Regen-II ally mend & 2s radiant-water foe root) 3D Primordial Dawn-Tear Globules (#FF6B9D / #FFEA78 / #225C7C, +1 vs Undead, Oblivion & Wraith [or Nether, Arid, Oblivion & Wraith under Water dominance], -1 vs Umbral), 4-lotus projectile-dissolving 1-heart/s +20%-speed ally-mending 5m-knockback 3s-blinding melee/spell-hit-dart Dawn-Tide Lotus Orbit Flows, Undead/Oblivion/Wraith-smiting 2-heart/s + saturation ally-healing Primordial Genesis Creation Rays (5.0s contact -> 8m-knockback -5-armor Cataclysmic Rebirth Pulse + 15s +6-Absorption-Heart Speed-II Aegis of the First Dawn), zero-light-block 6m+ 8m-knockback -3-armor debuff-curing (5s 1-heart/s +4-Armor +2-Toughness Aura of Inception) Dawn-Tide Rebirth Novas, weapon-stacking +50%-smite -3-armor 6m-knockback 3m-Amniotic-Waters-of-Origin (2-heart + Saturation ally mend / 2.5s root & +25% vulnerability) Primordial Inception Palm-Strikes, +7-Armor +3-Toughness Blindness/Darkness/Wither/Poison/Drowning-immune (+50% swim) 40%-damage-absorbing (Regen II + Genesis Materia fuel) 4m-knockback 3s-blinding Amniotic Genesis Shrouds (mobile or anchored), disarmable Single/Repeating dual-purpose Well of Creation & Cleansing Geysers (ally step -> 6-heart curse-curing +4-Absorption-Heart 30s Fountain of Inception; hostile step -> 10m-launching buff-purging -3-armor +50%-fall-vulnerability Cataclysmic Cleansing Geyser), debuff/curse-purging 2-heart/s + hunger ally-mending +50%-smiting (-1 armor/s up to -4) 25s-AI-disrupting -50%-speed Dawn-Tide Rejuvenation Deluges, 18s+8s 12m+ 8-boulder 3D-pseudoblock flat-knocking -5-armor +1-heart/s-ally-healing -60%-speed Cataclysmic Waters of Creation Downpours, 45s refreshing 70-HP 35%-armor-bypassing 3s-blinding Solar-Hydro Templar / 80-HP 3-target 16m golden-water-beam (-2 armor/hit) +6-Armor +3-Toughness Dawn-Glory Spire / 14m-taunt 10m -4-armor 4s-blinding 6-heart-ally-healing Amniotic Water-Phantom Eidolons (#FF6B9D / #FFEA78 / #225C7C), 18m knock-aside 4s-blinding smiting aggro-shedding Dawn-Glory Solar Slipstreams, 2.5s-channeled Primordial Creation & Genesis Rebirth Fusion Transmutes (instant Zombie-Villager cure, Netherrack/Soul Sand/Basalt -> Grass/Clay/Flowering Azalea, Water -> debuff-curing 2x-crop Sacred Dawn Water & gear curse purge), and milk-immune -25%-Max-HP heal-blocking -35%-Toughness attack-staggering 1-heart/3.5s-true-damage opponent-spark-healing Hyper-Cellular Genesis Destabilization Hexes.\n" +
                "▪ Oblivion (#47 — Tier 3 Trinity Fusion): Underwater-burning 5m-shattering -3-armor 5s-Wither-II + Blindness 3D Stygian Hellfire Annihilation Skulls (#B83C08 / #211A21 / #4D6453, +1 vs Radiant, Nature, Frozen, Genesis & Aura, -1 vs Water & Void), 4-target 6m 60%-damage 4s-ash-fire -30%-Toughness (5s) Entropic Ash-Chain Flows, projectile-vaporizing -1-armor/s 1.5-heart/s-draining Stygian Extinction Erasure Beams (5.0s contact -> 6m 8m-knockback -5-armor Wither-III + Darkness Extinction Flashover), zero-light-block 6m+ 8m-knockback -4-armor 5s-Blindness/Wither-II (5s projectile-vaporizing torch-snuffing -70%-detection 1-heart/s-draining Oblivion Void-Shroud) Stygian Extinction Novas, weapon-stacking -4-armor 4s-shield-disabling 6s-Stygian-Conflagration (1-heart/s burn + hunger drain + healing lock) Stygian Extinction Palm-Cleaves, +8-Armor +3-Toughness Fire/Lava/Wither/Blindness/Darkness-immune -15%-speed 40%-melee-reflecting (-2 attacker armor) lava/water-solidifying Stygian Extinction Armors (mobile or anchored), disarmable Single/Repeating 4m-suction -> 8m-launching buff-purging -4-armor +50%-fall-vulnerability (6s) Oblivion Singularity Mines, 60° projectile-vaporizing 1.5-hunger-draining -1-armor/s (up to -4) 25s-AI-disrupting Crematorium Shadow-Breath Cones, 18s+8s 12m+ 8-meteor 3D-pseudoblock flat-knocking -5-armor -60%-speed Cataclysmic Stygian Meteor Downpours, 45s refreshing 75-HP 35%-armor-bypassing 6m-knockback 5s-stygian-fire Stygian Dread-Juggernaut / 85-HP 16m-skull-mortar 8m -1-armor/s 1-heart/s Extinction-Aura Pyre of Nonexistence Obelisk / 14m-taunt 35%-melee-reflecting 10m -4-armor 5s-Wither-II/Blindness Smoldering Wither-Effigy Eidolons (#B83C08 / #211A21 / #4D6453), 16m phasing 4s-Wither-II/Blindness aggro-shedding Fire/Fall-immune (5s) Abyssal Hell-Rift Steps, 2.5s-channeled Stygian Desolation & Ash-Slag Smelting Transmutes (logs -> Coal Block +25% Charcoal, sand/dirt/grass -> Soul Sand/Soul Soil/Basalt, water -> Obsidian +25%, cobble/stone -> Blackstone/Magma Block & +30% furnace speed), and milk-immune -30%-Max-HP heal-blocking -40%-Toughness -35%-attack 1.5-heart/3.5s-underwater-hellfire Stygian Oblivion-Extinction Hexes (+120% Blaze/Phantom/Wither-Skeleton Stygian Extinction Scent).\n" +
                "▪ Chimera (Dynamic Base-4 Catalyst): Dynamic Air/Earth/Water/Nether roulette spells (+1 vs Undead, Blight, Soulfire & Blood, -1 vs Umbral), 5.0s Discordant Meltdown beams, 45s refreshing 75-HP Chimeric Juggernaut / 85-HP Chromatic Crucible Spire / Unstable Hybrid Effigy Eidolons, 2.5s-channeled Chimeric Catalytic Metamorphosis Transmutes, and milk-immune Discordant Chimeric Mutation Hexes.",
                "spell_ethereal_paradox_mutations", 7, new ItemStack(Items.NETHER_STAR),
                1000f, 0.0f, a_magic + 0.01f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_apex_and_prismatic_mutations", "Apex & Prismatic Spell Mutations", "MAGIC",
                "Signature Flow, Form, and Core Effect mutations for Aether, Entropic, Celestial, and Prismatic (#48-#51).",
                "Overview & Description:\n" +
                "Tier 4 Apex Fusions (#48-#50) and the refined Prismatic Catalyst (#51) unlock 50% armor-bypassing strikes, 20-second cataclysmic Skyfalls, 2.5-second held Right-Click channeled area Transmutations, and 45-second refreshing 90-100 HP Eidolon Triads.\n\n" +
                "Apex & Prismatic Lineup:\n" +
                "▪ Aether (#48 — Tier 4 Apex Fusion): Gravity-immune wall-phasing 4-pierce 7m-detonating 4-homing-spike (-5 armor & 5s shield disable) 3D Apex Celestial Meteorites (#A88654 / #773214 / #225C7C / #B83C08, +1 vs Earth, Nether, Void, Magma, Dust & Overgrowth, -1 vs Void, Nature, Frozen & Arid), 4-orb wall-phasing projectile-vaporizing homing-dart (-2 armor) Quintessential Quad-Harmonic Orbit Flows, wall-phasing -2-armor/s zero-g Quintessential Firmament Rays (5.0s contact -> 12m-knockback -6-armor buff-purging 5s-Levitation/Stun Cataclysmic Firmament Supernova), zero-light-block 10m+ 10m-airborne-launching -5-armor (12s +10-Armor +5-Toughness fall/drowning/suffocation-immune 3D-flight Transcendent Firmament Aegis) Quintessential Firmament Resonance Novas, weapon-stacking 50%-armor-bypassing -5-armor 5s-shield-disabling 6m-splash 10m-airborne 5m-vacuum-fissure Quintessential Firmament Sunders, +10-Armor +5-Toughness fall/drowning/suffocation-immune -80%-detection 4m-projectile-vaporizing 6m-anti-gravity 3D-flight Transcendent Firmament Quintessence Shrouds (mobile or anchored), disarmable Single/Repeating 4m-trigger -> 8m-pillar 12m-upward-launching -5-armor buff-purging 5s-Levitation/Weakness-III -> hyper-gravity ground-slam Quintessential Firmament Inversion Vortex Sigils, wall-penetrating -5-armor projectile-vaporizing 8m-launching 25s-AI-disrupting -50%-speed Quintessential Firmament Deluges, 20s+10s 14m+ 8-spire 3D-pseudoblock flat-knocking -6-armor (-70% speed, zero-g & 1.5 hearts/s crushing) Cataclysmic Firmament Skyfalls, 45s refreshing 90-HP 50%-armor-bypassing -3-armor 8m-airborne Firmament Archon-Colossus / 100-HP 18m wall-phasing mortar (-2 armor) 10m -1-armor/s ally-cleansing Apex Firmament Spire of Equilibrium / 16m-taunt 40%-damage-reflecting 12m -5-armor 5s-suspending Harmonized Quintessence Effigy Eidolons (#A88654 / #773214 / #225C7C / #B83C08), 20m wall-phasing 6m-departure-vacuum 8m-arrival-knockback (-3 armor) 8s-3D-flight/fall-immune aggro-shedding Firmament Ascension Steps, 2.5s-channeled Quintessential Firmament Genesis Transmutes (Stone/Deepslate/Netherrack/End Stone -> Calcite/Quartz/Amethyst/Gilded Blackstone, curse purge + 35% +1 enchant tier, mob drops -> +25% Aether Distillate + Amethyst Crystals & air -> Purified Aetheric Spring Water), and milk-immune -80%-crawl jump-locking -50%-Toughness 50%-weapon-recoil 1.5-heart/3s-crushing Apex Gravitational Collapse Hexes (+50% aerial/charge mob Positional Crushing Vulnerability).\n" +
                "▪ Entropic (#49 — Tier 4 Apex Fusion): Wall-phasing 6-pierce 50%-armor-bypassing 7m-Thermodynamic-Singularity-Well (-5 armor, 6s Wither III + Hemorrhage & 30% lifesteal) 3D Entropic Singularity Skull-Orbs (#41304E / #211A21 / #4D6453 / #8A0303, +1 vs Air, Radiant, Nature, Frozen, Celestial, Aegis & Aurora, -1 vs Earth, Void, Radiant, Nether & Umbral), 5-target 8m wall-phasing 70%-damage -3-armor heal-blocking health-siphoning Abyssal Sanguine-Void Chain Flows, wall-phasing -2-armor/s 1-heart/s-siphoning arrow-disintegrating Entropic Oblivion-Siphon Rays (5.0s contact -> 12m-knockback -6-armor buff-purging 5s-Blindness/Wither-III/Slowness-IV Apex Event Horizon Cataclysm), zero-light-block 10m+ vacuum-imploding -> 10m-blasting -5-armor (12s +10-Armor +5-Toughness Wither/Poison/Blindness-immune 40%-reflecting 25%-lifestealing Apex Entropic Shroud) Entropic Singularity Implosion Novas, weapon-stacking 50%-armor-bypassing -5-armor 5s-shield-disabling 35%-lifestealing 5s-rot-fissure (-60% speed & Wither II) Entropic Singularity Cleaves, 6m-torch-snuffing -80%-detection +10-Armor +5-Toughness Wither/Poison/Blindness/Darkness-immune 4m-arrow/trident-vaporizing 6m 1.5-heart/s-draining Abyssal Event Horizon Mantles (mobile or anchored), disarmable Single/Repeating 4m-trigger -> 8m-vacuum 12m-upward-launching -5-armor buff-purging 5s-Wither-III/Hemorrhage/Blindness -> ground-slam Entropic Singularity Collapse Sigils, wall-penetrating -5-armor wood-incinerating water-boiling 25s-Thermodynamic-Rot-Frenzy Entropic Oblivion-Flame Deluges, 20s+10s 14m+ 8-monolith 3D-pseudoblock flat-knocking -6-armor (-70% speed, Wither III & 2.0 hearts/s life-drain) Cataclysmic Entropic Skyfalls, 45s refreshing 90-HP 50%-armor-bypassing -3-armor 35%-siphoning 8m-knockback Entropic Dread-Titan / 100-HP 18m wall-phasing singularity-mortar (-2 armor) 10m 1.5-heart/s -1-armor/s blinding Apex Event Horizon Monolith / 16m-taunt 40%-damage-reflecting 12m -5-armor 5s-Wither-III/Blindness Smoldering Void-Husk Effigy Eidolons (#41304E / #211A21 / #4D6453 / #8A0303), 20m wall-phasing 8m-departure-black-hole (-4 armor & 4s Blindness/Wither-II) 8s-invulnerable aggro-shedding Event Horizon Phase-Slips, 2.5s-channeled Apex Thermodynamic Dissolution & Corruption Transmutes (Stone/Cobble -> Blackstone/Deepslate/Crying Obsidian, Dirt/Grass -> Soul Sand/Sculk, plants -> Wither Roses, mob drops -> +25% Blood/Void Essence & damaged metal gear -> +35% ingots/nuggets + slag), and milk-immune -30%-Max-HP heal-blocking -50%-Toughness 1.5-heart/3s-leeching bone-spasm Apex Thermodynamic Decay & Exsanguination Hexes (32m +200% void-horror Apex Heat-Death Influx).\n" +
                "▪ Celestial (#50 — Tier 4 Apex Fusion): Wall-phasing shield-piercing 4-pierce 50%-armor-bypassing 3.5x-Undead/Demonic/Abyssal-smiting 6m-Holy-White-Hole-Singularity (-5 armor, 8s Blindness & party health siphon) 3D Holy Solar Singularity Darts (#FFEA78 / #FF6B9D / #1A5923 / #A88654, +1 vs Undead, Water, Earth, Oblivion, Wraith & Barrow, -1 vs Umbral & Void), 4-orb projectile-to-healing-starlight 50%-bypassing blinding-dart 4m 1-heart/3s-healing Holy White-Hole Star-Orbit Flows, wall-penetrating -2-armor/s blinding ally-healing fuel-refilling Holy Pranic Fusion Beams (5.0s contact -> -6-armor 12s-Blindness holy-vortex -> 10m-knockback Celestial-Lily-blooming Cataclysmic Supernova Collapse), zero-light-block 10m+ 8m-knockback projectile/necrotic-summon-vaporizing -5-armor (15s +10-Armor +5-Toughness Wither/Poison/Blindness-immune 40%-holy-retaliation Celestial Ascendance Shroud) Holy Supernova Dawn-Shockwaves, weapon-stacking 50%-armor-bypassing -5-armor 5s-shield-disabling 35%-party-health/fuel-leeching 6m-knockback Holy-Blindness/Brand Holy Supernova Cleaves, zero-light-block +10-Armor +5-Toughness Wither/Poison/Darkness/Blindness-immune -80%-detection 4m-projectile-to-healing-starlight white-hole-retaliating Holy Celestial Supernova Shrouds (mobile or anchored), disarmable Single/Repeating dual-purpose Holy White-Hole Sigils (ally step -> instant 10-heart heal + Regen III + cleanse; 4m hostile step -> 10m-vortex 12m-airborne-launch -5-armor 10s-Blindness -> ground slam), wall-penetrating ally-cleansing 12s-Regen-III 50%-armor-bypassing -5-armor 25s-Divine-Stupefaction Holy Dawn-Deluge Cones, 20s+10s 14m+ 8-spire 3D-pseudoblock 3.5x-Undead/Abyssal -6-armor flat-knocking (10s inward-pulling holy-fire & ally health/hunger/fuel Consecrated White-Hole Singularity Zone) Cataclysmic Supernova Starfalls, 45s refreshing 100-HP 50%-armor-bypassing -3-armor undead-smiting 35%-party-healing 8m-knockback Celestial Seraph-Colossus / 100-HP 18m wall-phasing white-hole-mortar (-2 armor) 10m Genesis-Field (-2 armor/s & 6m healing/smiting waves) Apex White-Hole Dawn Spire / 16m-taunt 40%-holy-fire-reflecting 10m -6-armor 8s-Blindness full-party-healing Holy Genesis Effigy Eidolons (#FFEA78 / #FF6B9D / #1A5923 / #A88654), 20m wall-phasing aggro-shedding 6s-3D-flight/Slow-Falling Holy-Dawn-Decoy (6s Blindness + flashbang if struck) Celestial Supernova Ascendance Steps, 2.5s-channeled Celestial Genesis & Consecration Transmutes (max-discount Zombie Villager cure, Netherrack/Soul Sand/Sculk -> Calcite/White Terracotta/Flowering Azalea Moss, Rotten Flesh/Spider Eyes -> +25% Golden Carrots/Glistering Melons & curse purge into +1 divine blessing), and milk-immune 2x-Undead/Demonic/Abyssal holy-burn heal/buff-blocking 50%-fumble -40%-Toughness Divine Genesis Brand / Holy Supernova Overload Hexes.\n" +
                "▪ Prismatic (#51 — Refined Catalyst): Zero-drop 6-ricochet projectile-refracting -4-armor/bounce 3D Prismatic Oscillating Light-Discs (Full HSB Spectrum / #72F6FF / #FFFFFF, +1 vs Undead, Blight, Soulfire & Blood, -1 vs Umbral, blinding 5m rainbow nova + 5-heart wounded-party heal on final bounce), 4-lens projectile/magic-refracting (+20% ally attack speed 4m) seeking-rainbow-dart Kaleidoscope Orbit Flows, wall-phasing 3-way-splitting (1.5 hearts/s siphon & -2 armor/s) Bifurcated Rainbow Spectrum Rays (5.0s contact -> -5-armor 4.0s-paralyzing party-absorption-shielding Prismatic Meltdown), zero-light-block 10m+ hazard/cloud-purging -5-armor (8-heart party heal & cleanse) Rainbow Bloom Shockwaves, weapon-stacking 50%-armor-bypassing -5-armor 4s-disarming (-65% attack) 4s-Blindness 10s-Glowing Prismatic Crystalline Palm-Shatters, zero-light-block Fire/Lava/Drowning-immune Slow-Falling-gliding 5m-Undead/Blight-burning Chromatic Dispersion Auras (mobile or anchored), disarmable Single/Repeating 4m-trigger -> 5s stained-glass-encasement weapon-dropping -4-armor Refractive Prism-Vault Glyphs, wall-penetrating ally-cleansing (+30% speed 12s) projectile-deflecting -5-armor 25s-AI-disrupting Chromatic Prismatic Deluge Cones, 20s+10s 14m+ 8-spire foliage/glass-phasing 3D-pseudoblock (permanently materializing Sea Lantern/Amethyst/Quartz/Calcite spires) -6-armor knocking-down Chromatic Star-Showers (10s +100%-ally-attack-speed Jump-Boost-III corrupt-burning Crystalline Resonance Field), 45s refreshing 100-HP flying piercing-javelin Prismatic Valkyrie / 100-HP 12m 100%-projectile/magic-reflecting Refractive Mirror Monolith / 16m-taunt 10m-blinding 15s-complete-party-invulnerability Sacrificial Solar Relic Eidolons, 18m wall-phasing aggro-shedding 2s-invisible flashbang-Prismatic-Mirror-Decoy Refractive Phase-Steps, 2.5s-channeled 8m fuel-free 1x-yield Prismatic Ore Metamorphosis Transmutes (instant ore->ingot smelting + 10% chance to transmute into a random ore up to Diamond), and milk-immune 50%-fumble -40%-Toughness buff-inverting 2x-Undead/Demonic/Blight/Blood-burning Refractive Dyschromia / Chromatic Overload Hexes.",
                "spell_trinity_and_catalyst_mutations", 8, new ItemStack(Items.AMETHYST_CLUSTER),
                1090f, 0.0f, a_magic + 0.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_eschaton_and_singularity_mutations", "Eschaton & Singularity Spells", "MAGIC",
                "Tier 5 Eschaton (#52-#54), Tier 6 Singularity (#55-#57), bottled Transmute Elixirs, and +5 enchantment elevation.",
                "Overview & Description:\n" +
                "The pinnacle of Spellcrafting across Empyrean, Cataclysm, and Terminus (Tier 5 Eschaton: 60% armor bypass, 24s Skyfalls, 60s / 110-120 HP Eidolons) and Eschaton, Apotheosis, and Entropica (Tier 6 Singularity: 75% armor bypass, 30s Skyfalls, 90s / 140-150 HP Eidolons).\n\n" +
                "Eschaton, Singularity & Bottled Elixirs:\n" +
                "▪ Empyrean (#52 — Tier 5 Eschaton Fusion): Wall-phasing 8-pierce 60%-armor-bypassing 8m-Siphoning-Solar-Root-Web (3.5s briar root, -6 armor & 35% damage->party absorption) 3D World-Tree Siphoning Seed-Pods (#FFEA78 / #FF6B9D / #1A5923, +1 vs Undead, Water, Earth, Nether, Void, Oblivion, Wraith, Barrow & Blight, -1 vs Umbral), 4-pod wall-phasing projectile/spell-vaporizing seeking-tendril (2.0s root, -3 armor & party absorption siphon) World-Tree Siphoning Root-Web Orbit Flows, wall-phasing -2-armor/s aerial-grounding party-health/saturation-siphoning Sustained Root-Lance Sunbeams (5.0s channel -> 12m-knockback -7-armor buff-purging 5.0s-root 10m-azalea-blooming 4-heart-ally-healing Canopy Cataclysm), zero-light-block 12m+ 10m-knockback +60%-Undead/Dark-smiting -6-armor 4.0s-rooting World-Tree Heartwood Bastion Novas (16s +12-Armor +6-Toughness Poison/Wither/Drowning/Hunger-immune party-absorption Aegis of the Infinite Life), weapon-stacking 60%-armor-bypassing -6-armor 6.0s-shield-disabling 4.0s-rooting 8m-tendril-siphoning Siphoning World-Tree Root-Cleaves, +12-Armor +6-Toughness Suffocation/Drowning/Poison/Wither/Hunger-immune -85%-detection 5m-arrow/trident-petal-vaporizing 6m 2.0-hearts/s-true-damage->party-absorption Living Titan Bark & Canopy Mantles (mobile or anchored), disarmable Single/Repeating dual-purpose World-Tree Siphoning Root-Vault Sigils (ally step -> 10-heart heal + curse cure + 45s +8-Absorption-Hearts; 5m hostile step -> 8m 5.0s golden-titan-root-cage -6-armor buff-purging 40%-health->golden-healing-dewdrops), wall-penetrating -6-armor projectile-vaporizing 3.0s-rooting 30s-AI-disrupting (-60% speed) Solar-Verdant Spore-Web Deluges, 24s+12s 16m+ 8-megalith 3D-pseudoblock (permanently materializing Materia-Echo Heartwood, Moss, Flowering Azalea & Sacred Loam) -7-armor dark-smiting Cataclysmic World-Tree Canopy Skyfalls (12s -70%-speed 4s-periodic-root +2-hearts/s Saturation +6-Armor World-Tree Consecrated Soil), 60s refreshing 110-HP 60%-armor-bypassing -4-armor 3.0s-rooting 40%-absorption-siphoning Yggdrasil Arboreal Juggernaut / 120-HP 20m seeking-root-seed-mortar (-3 armor) 16m Sanctuary-Field (+8 Armor, +4 Toughness & 10-HP absorption) Heartwood Canopy Obelisk / 16s 16m-taunt 45%-melee-reflecting 14m true-damage (-6 armor, 5.0s root & 8-heart ally heal) Golden Willow Effigy Eidolons (#FFEA78 / #FF6B9D / #1A5923), 22m ceiling-phasing 6m-departure-root-blast (3.0s root) 10s-3D-flight/Slow-Falling ground-dewdrop aggro-shedding Solar Slipstream Ascents, 2.5s-channeled World-Tree Genesis & Vitality Consecration Transmutes (Stone/Deepslate/Netherrack -> Sacred World-Tree Heartwood/Flowering Azalea Moss/Sacred Loam, curse purge + 50% +1 enchant tier, max-discount Zombie Villager cure & 3m standing water -> bottled Dew of the World-Tree), and milk-immune 2x-Undead/Demonic/Abyssal true-holy-burn heal-blocking 50%-fumble -50%-Toughness Radiant Empyrean Brand Hexes (32m +120% Empyrean Starlight Beacon).\n" +
                "▪ Cataclysm (#53 — Tier 5 Eschaton Fusion): Ultra-high-velocity wall-penetrating 10-pierce 60%-armor-bypassing 3D Catastrophic Hellfire Railgun Slugs (#41304E / #211A21 / #B83C08, +1 vs Air, Radiant, Nature, Frozen, Water, Celestial, Aegis, Aurora & Genesis, -1 vs Vitae, triggering a flat-knocking -35%-Toughness unquenchable-burn volcanic magma eruption on every entity penetrated), 4-core wall-phasing projectile-crushing seeking-railgun-spike (-40% Toughness & true fire) Singularity Orbit Flows, wall-phasing -2-armor/s arrow-disintegrating core-pulling Tectonic Magma Fissure Rays (5.0s channel -> 14m-knockback -7-armor buff-purging Cataclysmic Seismic Eruption & 12m -70%-speed underwater-burning molten caldera), zero-light-block 12m+ 12m-knockback -6-armor 6.0s-shield-disabling molten-obsidian-crack World-Breaker Seismic Novas, weapon-stacking 60%-armor-bypassing -6-armor 6.0s-shield-disabling 10m-inward-pulling (2.0 hearts/s) Tectonic Calamity Sunders, 6m-torch-snuffing +12-Armor +6-Toughness Fire/Lava/Wither/Blindness-immune -85%-detection 5m-arrow/trident-slag-vaporizing 6m 2.0-hearts/s-true-fire-gravity-well Mantles of the World-Breaker (mobile or anchored), disarmable Single/Repeating 5m-trigger -> 8m-caldera 14m-upward-launching -6-armor buff-purging 6.0s-Wither-III/Stygian-Conflagration -> violent ground-slam Abyssal Calamity Caldera Sigils, wall-penetrating -6-armor projectile-vaporizing wood-incinerating water-boiling 30s-Tectonic-Vertigo-Panic (-60% speed & blind terror) Cataclysmic Magma-Ash Deluges, 24s+12s 16m+ 8-meteorite 3D-pseudoblock (permanently materializing Obsidian, Crying Obsidian, Blackstone & Magma Blocks) -7-armor flat-knocking Cataclysmic World-Breaker Skyfalls (12s -70%-speed Wither-III underwater-true-burning Stygian Magma Slag), 60s refreshing 110-HP wall-phasing 60%-armor-bypassing -4-armor 12m-jaw-pulling Stygian Magma-Void Serpent / 120-HP 20m wall-penetrating railgun-magma-spike (-3 armor) 16m Gravitational-Caldera-Field (-1 armor/s & 1.5 hearts/s burn) Volcanic Eruption Spire / 16s 16m-taunt 45%-melee-reflecting 14m true-damage (-6 armor & singularity pull) Molten Core Beacon Eidolons (#41304E / #211A21 / #B83C08), 100%-silent 22m wall-phasing 6m-departure-micro-singularity (arrow-crushing, inward-pulling & 2.5s-stunning) 8.0s-Fire/Fall-immune aggro-shedding Gravitational Void-Warp Collapses, 2.5s-channeled 12m Tectonic Subduction & Magma Vitrification Transmutes (Stone/Deepslate/Cobble -> Obsidian/Crying Obsidian/Blackstone, raw/mineral ores -> +35% bonus yield + nuggets, Sand/Red Sand -> Glass & damaged equipment/tools/metal items -> raw Void Slag + base ingots), and milk-immune underwater-burning -70%-speed jump-locking -50%-Toughness 50%-weapon-recoil Abyssal World-Breaker Singularity Hexes (32m +150% Abyssal World-Breaker Beacon).\n" +
                "▪ Terminus (#54 — Tier 5 Eschaton Fusion): Wall-phasing 8-pierce 60%-armor-bypassing 8m-Zero-Point-Glacial-Stasis-Field (3.5s solid ice stasis, -6 armor & slippery Blue Ice ground flash-freeze) 3D Absolute-Zero Permafrost Lances (#A88654 / #98DCF2 / #225C7C, +1 vs Nether, Arid, Earth, Water, Magma, Pyre, Blight, Soulfire & Miasma, -1 vs Undead), 4-blade wall-phasing projectile-shattering seeking-ice-shard (-40% Toughness & -40% attack speed) Cryogenic Polar Vortex Orbit Flows, wall-phasing -2-armor/s aerial-grounding fire-dousing Sustained Cryogenic Vortex Rays (5.0s channel -> 12m-knockback -7-armor buff-purging 5.0s-solid-blue-ice Zero-Point Atmospheric Collapse), zero-light-block 12m+ 10m-knockback -6-armor water->Packed-Ice lava->Obsidian 4.0s-ice-encasing Absolute-Zero Cryo-Novas (16s +12-Armor +6-Toughness Freeze/Drowning/Fire-immune +40%-ice-speed Glacial Mantle of Terminus), weapon-stacking 60%-armor-bypassing -6-armor 6.0s-shield-disabling 4.0s-ice-encasing 8m-frost-nova (2.5s surrounding freeze) Absolute-Zero Glacier Cleaves, 6m-fire/torch-snuffing +12-Armor +6-Toughness Fire/Lava/Drowning/Freeze/Wither-immune -85%-detection 5m-arrow/trident-hail-shattering 50%-damage-absorbing 2.0s-melee-ice-encasing (-60% attack speed) Zero-Point Kinetic Stasis Shields (mobile or anchored), disarmable Single/Repeating 5m-trigger -> 8m-cryo-vortex 12m-gale-geyser apex-flash-freeze -> catastrophic crash-impact (-6 armor, buff purge & 4.0s blue-ice encasement) Permafrost Glacial Abyss Sigils, wall-penetrating -6-armor projectile-deflecting water->Packed-Ice 30s-Cryogenic-Molecular-Hypothermia (AI disruption & -60% speed) Cryogenic Blizzard Deluges, 24s+12s 16m+ 8-monolith 3D-pseudoblock (permanently materializing Blue Ice, Packed Ice, Runic Blue Ice & Zephyr Ice) suspended-animation -7-armor Continuous Cryo-Stasis Hailstorms (12s mob-freezing +6-Armor Frost-Walker Absolute Zero Permafrost), 60s refreshing 110-HP wall-phasing 60%-armor-bypassing -4-armor 3.0s-ice-encasing Titanic Glacial Elemental / 120-HP 20m wall-penetrating cryogenic-lance (-3 armor) 16m Polar-Freeze-Field (-50% speed & continuous water freezing) Anchored Permafrost Elemental Spire / 16s 16m-taunt 45%-melee-reflecting 14m true-damage (-6 armor & 5.0s solid ice encasement) Unstable Cryogenic Elemental Effigy Eidolons (#A88654 / #98DCF2 / #225C7C), 22m wall-phasing 6m-departure-frost-nova (3.0s ice root) 10s-Frost-Walker/Fall-immune/+40%-ice-speed aggro-shedding Glacial Slipstream Flash-Slides, 2.5s-channeled 12m Zero-Point Crystallization & Permafrost Synthesis Transmutes (water/rainfall -> Blue Ice, lava -> Obsidian, stone/deepslate -> Permafrost/Packed Ice & 3m standing water -> bottled Glacial Frost Elixirs), and milk-immune true-frostbite -80%-speed -60%-attack-speed -50%-Toughness jump-disabling Absolute Zero Molecular Cessation Hexes (+40% monster crushing damage & melee Freezing Absolute Zero Vulnerability).\n" +
                "▪ Eschaton (#55 — Tier 6 Singularity Fusion): Ultra-dense wall-phasing 12-pierce 75%-armor-bypassing wake-dragging 10m-Omega-Event-Horizon (inward vacuum, loose-obstacle->fuel vaporization, -8 armor, true void collapse & raw Eschaton Materia droplets) 3D Black-Hole Railgun Darts (#0B0813 / #7012A5 / #D4C2FC, +1 vs all lower-tier elements, -1 vs Apotheosis), 4-core wall-phasing 100%-projectile->fuel-vaporizing seeking-mini-black-hole (-50% Toughness & true void damage) Event Horizon Orbit Flows, wall-phasing -3-armor/s block->fuel-vaporizing Dimensional Void-Fissure Beams (5.0s channel -> 16m-knockback -8-armor buff-purging Omega Event Horizon Implosion & 6.0s 14m inward gravitational tear), zero-light-block 16m+ 14m-flat-knockback -8-armor projectile->fuel Cosmic Event-Horizon Novas (20.0s +14-Armor +8-Toughness debuff/fall/void-immune 8m-vacuum Mantle of the Omega Singularity), weapon-stacking shield/wall-cleaving 75%-armor-bypassing -8-armor 8.0s-shield-disabling buff-severing 4.0s-stasis-locking 8m-arc-pulling Reality-Cleaving Scythes, 10m-light-snuffing +14-Armor +8-Toughness Fire/Lava/Drowning/Freeze/Wither/Debuff/Fall/Void-immune -90%-detection 6m-projectile->fuel 8m 3.0-hearts/s-true-damage Mantles of the Omega Singularity (mobile or anchored), disarmable Single/Repeating 6m-trigger -> 6.0s unbreakable-gravity-tether (-8 armor, buff purge & 12m projectile-into-victim magnet) Singularity Anchor Sigils, wall-penetrating -8-armor projectile->fuel non-bedrock-tunnel-carving 35.0s-Omega-Spacetime-Disorientation (AI disruption, -75% speed & helpless spinning) Accretion Singularity Torrents, 30s+15s 20m+ 8-singularity 3D-pseudoblock (permanently materializing Crying Obsidian, Astral Impactite, Obsidian & Chiseled Eclipse Basalt) zero-g-stasis/flat-pulverizing -8-armor block->fuel Dimensional Inversion Starfalls (15s -80%-speed inward-dragging true-damage Collapsed Reality Slag), 90s refreshing 140-HP wall-phasing 75%-armor-bypassing -5-armor 14m-jaw/matter-pulling Event-Horizon Leviathan / 150-HP 24m wall-penetrating singularity-lance (-4 armor) 20m Gravitational-Accretion-Field (-1.5 armor/s & projectile/hostile pull) Accretion Disk Singularity Spire / 20s 20m-taunt 50%-damage-reflecting 20m reality-tear (block->fuel, massive true damage, -8 armor & singularity pull) Collapsing Micro-Nova Effigy Eidolons (#0B0813 / #7012A5 / #D4C2FC), 100%-silent 24m wall-phasing furthest-enemy-swapping (4.0s stasis bubble & buff purge) cloaked 4.0s-invulnerable aggro-shedding Spacetime Fold Slips, 2.5s-channeled 16m Dimensional Event Horizon Transmutation & Accretion Compression (non-bedrock->fuel, Stone/Deepslate/Netherrack -> Crying Obsidian/Blackstone/Void Slag, Cobble/Stone/Gravel -> condensed raw mineral blocks, mob drops -> pure Eschaton essence, curse purge + up to 2 enchants +2, and 4m standing water -> 50/50 bottled Gravitational Stasis Elixirs or Brewing-Stand Essence of the Omega Horizon), and milk-immune true-void-collapse -85%-speed jump/teleport-blocking -60%-Toughness 60%-weapon-recoil 6m-contagious Omega Event Horizon Dissolution Hexes (36m +200% Cosmic Reality Tear).\n" +
                "▪ Apotheosis (#56 — Tier 6 Singularity Fusion): Wall-phasing 10-hostile-bouncing 75%-armor-bypassing blinding party-absorption-siphoning 10m-White-Hole-Genesis-Nova (-8 armor, smites hostiles, radiant essence orbs & 100% full ally heal) 3D Seeking Crowns of Dawn (#FFFFFF / #FFE484 / #72F6FF, +1 vs all lower-tier elements, +2 smite vs dark/void/nether/undead, -1 vs Eschaton), 4-starcore wall-phasing 100%-projectile->radiant-essence/healing-dew seeking-holy-star-dart (-50% Toughness & true holy fire) White-Hole Star-Orbit Flows, wall-phasing -3-armor/s undead-smiting ally-healing Sustained Holy White-Hole Rays (5.0s channel -> 16m-knockback -8-armor buff-purging Cataclysmic Genesis Bloom & 14m flora-blooming 6-heart/Saturation sanctuary), zero-light-block 16m+ 14m-flat-knockback -8-armor 2x-Undead/Demonic/Void Supernova Genesis Novas (party debuff purge, 12-heart ally heal & 20.0s Mantle of the Alpha Singularity), weapon-stacking wall-cleaving 75%-armor-bypassing -8-armor 8.0s-shield-disabling dark-smiting 4.0s-starlight-briar-rooting 10m-golden-crown-siphoning Ascendant Solar Genesis Cleaves, Level-15-light +14-Armor +8-Toughness Fire/Lava/Drowning/Freeze/Wither/Blindness/Void-immune -90%-detection 6m-projectile->petal/radiant-droplet 3D-flight/Slow-Falling 8m (+3 hearts/s + Saturation for allies; 3.0 hearts/s true holy burn to dark/void/nether hostiles) Mantles of the Alpha Singularity (mobile or anchored), disarmable Single/Repeating 6m-trigger White-Hole Genesis Sanctuary Sigils (ally step -> full HP + curse cure + +12 Absorption hearts + 60.0s Regeneration IV; hostile step -> 12m 6.0s golden hard-light pillar cage, -8 armor, buff strip, dark smite & 50% total HP -> radiant essence orbs), wall-penetrating -8-armor blinding undead-igniting sand->Glass flower/crop-blooming netherrack->Moss wither-foliage-purging 35.0s-Divine-Reverence (AI disruption & -75% speed) Prismatic Solar Fans, 30s+15s 20m+ 8-megalith 3D-pseudoblock (permanently materializing Quartz Pillar, Sea Lantern, Calcite & Moss) flat-knocking -8-armor dark-smiting Cataclysmic White-Hole Genesis Starfalls (15s 3.0s-periodic-root -80%-speed radiant-orb +3-hearts/s Saturation Regeneration-IV +8-Armor Consecrated Genesis Ground), 90s refreshing 140-HP 6-winged wall-phasing 75%-armor-bypassing -5-armor holy-fire 25%-ally-healing Celestial Seraph-Titan / 150-HP 24m wall-penetrating holy-supernova-lance (-4 armor) 20m Sanctuary-Field (+10 Armor, +6 Toughness & 15-HP absorption) White-Hole Dawn Obelisk / 20s 20m-taunt 50%-damage-reflecting 20m supernova (massive true holy damage, -8 armor, 12-heart ally heal & radiant essence) Radiant Ascendance Mirage Eidolons (#FFFFFF / #FFE484 / #72F6FF), 24m wall/ceiling-phasing 8m-departure-flashbang (6.0s Blindness + 3.0s golden briar root) 12.0s-3D-flight/Slow-Falling/Fall&Void-immune ground-dewdrop aggro-shedding Ascendant Solar Genesis Steps, 2.5s-channeled 16m Alpha Genesis Transmutation & Universal Consecration (Netherrack/Soul Sand/Sculk/Deepslate -> Calcite/Flowering Azalea Moss/Lush Loam/Amethyst/Quartz, brick repair, cursed blocks -> Radiant Raw Essence & droplets, curse purge + up to 2 enchants +2, max-discount Zombie Villager cure & 4m standing water -> 90s bottled Nectar of the Alpha Genesis), and milk-immune 2x-Undead/Demonic/Abyssal true-holy-burn heal-blocking 60%-fumble -60%-Toughness dark-magic-backfire 6m-contagious Divine Genesis Brand Hexes (+150% monster attack speed & 100% crit Blinding Overexposure).\n" +
                "▪ Entropica (#57 — Tier 6 Singularity Fusion): Wall-phasing 12-pierce 75%-armor-bypassing trajectory-pulling 10m-creation/dissolution-nova (-8 armor, knocks mobs flat & spawns collectible Essence Orbs) 3D Singularity Equilibrium Lances (#E0E5EC / #1A153B / #FAD02C, \"§b\", +1 vs EVERYTHING including Eschaton & Apotheosis, 0 weaknesses), 4-core wall-phasing End-portal-parallax 100%-projectile->fuel seeking-balance-dart (-50% Toughness & true damage) Cosmic Parallax Singularity Orbit Flows, wall-phasing -3-armor/s beam-center-pulling Twin Helix Singularity Streams (5.0s channel -> 16m-knockback -8-armor buff-purging Singularity Annihilation Blast), zero-light-block 16m+ 16m-flat-knockback -8-armor Cosmic Equilibrium Novas (party debuff purge, 12-heart ally heal & 20.0s Mantle of Entropica), weapon-stacking wall-cleaving 75%-armor-bypassing -8-armor 8.0s-shield-disabling 50%-damage->party-absorption 8m-mini-vortex Blades of the Grand Paradox, zero-light-block +14-Armor +8-Toughness Fire/Lava/Drowning/Freeze/Wither/Blindness/Void-immune -90%-detection 3D-flight/Slow-Falling 6m-projectile->fuel 8m (3.0 hearts/s true damage to hostiles & +3 hearts/s ally heal) Mantles of Entropica (mobile or anchored), disarmable omni-surface Single/Repeating Singularity Horizon Mandalas (hostile trigger -> 8m 6.0s stasis cage, -8 armor, buff strip & projectile-into-target magnet; ally step -> full HP & hunger + curse cure + +12 Absorption hearts + 3D flight), wall-penetrating -8-armor water->Blue-Ice sand->Glass fire-extinguishing 5.0s-stunning 35.0s-Cosmic-Equilibrium-Stupor (AI disruption & -75% speed) Thermodynamic Heat-Death Waves, 30s+15s 20m+ crimson-void-red-parallax 8-lance/starcore 3D-pseudoblock (permanently materializing Runed Astral Marble, Quartz Pillars, Calcite & Aeterium Crystal Blocks) -8-armor flat-knocking periodic-zero-g-stasis +3-hearts/s-ally-healing Cataclysmic Singularity Equilibrium Starfalls (15s -80%-speed party-Materia-fuel-regenerating Equilibrium Ground), 90s refreshing 140-HP wall-phasing 75%-armor-bypassing -5-armor 14m-aura-pulling alternating-void/radiant-orb Avatar of Entropica / 150-HP 24m wall-penetrating balance-ray (-4 armor) 20m Equilibrium-Field (2 hearts/s true damage & +2 hearts/s ally heal) Altar of the Horizon / 20s 20m-taunt 50%-damage-reflecting 20m reality-rupture (inward pull, 5.0s stun, -8 armor & massive true damage) Fractured Paradox Core Eidolons (#E0E5EC / #1A153B / #FAD02C), 24m wall/ceiling-phasing 4.0s-departure-parallax-decoy (taunt/pull -> stasis nova) 12.0s-3D-flight/Slow-Falling 4.0s-invulnerable aggro-shedding Parallax Event-Horizon Folds, 2.5s-channeled 16m Grand Cosmic Equilibrium Transmutation (strictly no fluids/air; cobble/dirt/gravel -> collectible Essence Orbs, stone/deepslate/netherrack -> Iridescent Primordial Runestone/Calcite/Harmonic Crystal Blocks, curse purge & +5 permanent tiers to ALL worn & dropped enchantments), and milk-immune true-damage -85%-speed heal-blocking -60%-Toughness 60%-weapon-recoil 6m-contagious Grand Equilibrium Paradox Hexes (dual 36m +200% Spawn-Lure Beacon + +100% monster attack speed / +50% crit Cosmic Paradox Distortion).",
                "spell_apex_and_prismatic_mutations", 9, new ItemStack(Items.DRAGON_BREATH),
                1180f, 0.0f, a_magic + 0.03f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_eidolon_summon_triad", "Eidolon Summon Forms", "MAGIC",
                "Three separate Summon creation forms (Autonomous Agent, Anchored Sentry, Phantom Decoy — up to 8 concurrent at Tiers 8-10) across all 63 Materia suites.",
                "Overview & Description:\n" +
                "During spell creation, Autonomous Agent (summon_agent), Anchored Sentry (summon_sentry), and Phantom Decoy (summon_decoy) are three separate Delivery Form choices—you pick one summon type per spell to manifest across all 63 registered Materia suites (including Caprine Secrets Radiant-Fire, Umbral-Water & Equinox plus Aureole & Vesper): Autonomous Agents (mobile companions up to 140 HP Avatar of Entropica, Event-Horizon Leviathan, Celestial Seraph-Titan, and flying Prismatic Valkyries), Anchored Sentries (stationary turrets & field spires up to 150 HP, including 16-block Umbral/Eclipse/Null_U/Penumbra dark-spawn blocking, Overgrowth/Spore/Oasis/Spring/Sylvan crop growth, Vitae heartbeat cleansing, Aura -20% spell fuel spires, Prismatic 12-block 100% projectile/magic reflect domes, and 20-block Singularity fields), or Phantom Decoys (taunt effigies with 4m-20m death bursts, including Prismatic's 15s party invulnerability burst). Concurrent active summons scale from 1 (Tier 1) up to 8 simultaneously at Tiers 8-10, and Tier 3-6 & Catalyst constructs (starting at Pyre #39 at 70/85 HP) run automated 45s / 60s / 90s Lifecycle Refresh Engines.\n\n" +
                "15s Orbis Cell Upkeep & Field Controls:\n" +
                "▪ 15-Second Orbis Cell Upkeep & Auto-Heal: Initial casting covers the first 15 seconds; every 15 seconds thereafter, each active Eidolon drains Materia from your equipped Orbis Cell first -> inventory Orbis Cells at 25% cast cost when healthy (50%+ HP) or 100% full cast cost + 100% full HP restore when below 50% HP.\n" +
                "▪ Orbis Cell Healing: Sneak + Right-Click an owned Eidolon with a matching Orbis Cell to repair its health directly.\n" +
                "▪ Zero-Cost Safe Dismissal: Hold Sneak while casting a Summon spell to safely dismiss active Eidolons matching that spell's chosen summon type at 0 Materia cost.",
                "spell_primordial_base_mutations", 3, new ItemStack(Items.TOTEM_OF_UNDYING),
                730f, 0.0f, a_magic - 0.07f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_wards_hexes_and_verbs", "Wards, Hexes & Core Verbs", "MAGIC",
                "Shield gates, Milk-proof Level I-III boss-stacking hexes, kinetic modes, and conjured bridges.",
                "Overview & Description:\n" +
                "All 19 Core Effect verbs scale across Tiers 1-10 across Combat, Utility, and Miscellaneous disciplines.\n\n" +
                "Key Mechanics:\n" +
                "▪ Arcane Ward & Aegis Shells (1.5x Base Damage): Remains continuously visible as a shimmering 3D barrier dome with rotating runic hoops and orbiting hexagonal plates for its full active duration, flares brightly in opacity with directional impact ripples whenever it absorbs a hit, intercepts combat hits only, blocks all overflow damage on the hit that shatters the shield, and converts 25% of matching-element hits back into Materia fuel.\n" +
                "▪ Persistent Hexes (Milk-Proof & Boss Level I-III Stacking): Cannot be cleared by drinking Milk (which strips normal potion effects while preserving active Materia Hexes), last indefinitely on standard foes (stacking Levels I-III), stack from Level I up to Level III on Bosses during their 30-second curse window (stamping a 10-second per-Hex immunity window only after the 30s Hex ends or is cleansed), cleanly remove Max-HP cuts on overwrite/cleanse, spread strictly Level I on death only during the initial window or while actively channeled, dispel if the caster dies, and require dedicated Cleanse/Purify sub-runes or opposing Materia to cure.\n" +
                "▪ Harm, Kinetic, Grapple, Blink, Restoration & Conjure: Tier 4+ Harm pierces physical armor and ruptures overkill targets into Raw Essence; Kinetic supports Push, Pull, Launch, Slam, and rocket-jumps; Grapple reels foes or zip-lines the caster to walls and ledges; Blink steps instantly up to 16+ blocks along your crosshair (wall-phasing at Tier 4+ or Void infusion, snapping safely onto solid non-lava ground or refunding 100% Materia fuel if blocked, converting freefall into forward speed, staying silent by default, and right-click toggling between Self-Blink and Position Swap); Restoration heals allies/Eidolons or harms Undead via unified Defender Lineage Advantage; Conjure dials light (7-15) or forms 15s stepping bridges.",
                "spell_modular_architecture", 2, new ItemStack(Items.SHIELD),
                550f, 0.0f, a_magic - 0.01f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_arcane_flight_and_utility_verbs", "Arcane Flight, Materia Wings & Field Utility", "MAGIC",
                "Timed 3D flight with Materia-colored Arcane Wings, plus 2.5s Held Transmute, Break, Harvest, Logistics, Sonar, Atmosphere, Acoustic, Diagnostic, and Gravity.",
                "Overview & Description:\n" +
                "Utility and Miscellaneous Core Effects empower exploration, farming, weather control, sensory scanning, and true 3D aerial flight.\n\n" +
                "Arcane Flight & 3D Materia Wings:\n" +
                "▪ Casting Arcane Flight (in any Delivery Form or Flow) grants timed 3D flight (15s at Tier 1 up to 60s+ at Tier 10), an immediate upward wing-stroke launch, and a gentle Slow Falling cushion when flight ends.\n" +
                "▪ Articulated 3D glowing Arcane Wings unfurl from the caster's back, dynamically colored by whichever of the 63 Materia types powers the spell—including two-tone feather tips on dual-Materia spells and live shifting spectrums for Prismatic, Chimera, Aurora, and Entropica!\n\n" +
                "Field Utility & Sensory Verbs:\n" +
                "▪ Break, Harvest & Construct: Cleanly excavate stone/ores without misrouting into Transmute (never breaking chests/machines), harvest & replant crops/berries/wart/cane/melons, or place offhand/conjured blocks.\n" +
                "▪ 2.5s Held Transmute & Logistics: Hold Right-Click continuously for 2.5 seconds to transform placed world blocks or dropped item stacks (with Autocast Pedestals charging the 2.5s channel automatically in-zone and 30% bulk fuel discounts), vacuum nearby items & XP orbs, or route items between linked chests on Autocast Pedestals.\n" +
                "▪ Sonar, Diagnostic, Atmosphere, Acoustic & Gravity: Highlight nearby creatures & ping valuable ore veins, inspect exact entity HP/Wards or block crop/honey/redstone states, shift weather between Clear Skies, Rain, and Thunderstorms, emit sonic shockwaves or stealth hush veils, and deploy Lunar, Zero-G, Grav-Lift, Repulsor, or Singularity gravity domes.",
                "spell_wards_hexes_and_verbs", 3, new ItemStack(Items.ELYTRA),
                640f, 0.0f, a_magic - 0.005f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_gems_and_ritual_infusion", "Spellgems & Ritual Infusion", "MAGIC",
                "Reusable Spell Parchments, Tier 1-10 binding, and flawed ritual risks.",
                "Overview & Description:\n" +
                "Designs scribed onto a Spell Parchment act as reusable blueprints that are never consumed. Hold a Blank Spellgem in your off-hand and use the Parchment on a Scribing Controller, Arcane Anvil, or Ritual Bowl to infuse an Inscribed Spellgem at Potency Tiers 1 through 10.\n\n" +
                "Special Properties:\n" +
                "Ritual infusion draws 10x a single cast's Materia cost from placed Orbis Cells within 8 blocks. Under-supplying the circle still completes the gem, but permanently inflicts Unstable Casting (37.5%), a Crippled Spell Part (37.5%), or both flaws (25%).",
                "spell_modular_architecture", 2, new ItemStack(ModItems.BLANK_SPELLGEM.get()),
                550f, 0.0f, a_magic - 0.12f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_casting_apparatuses", "Casting Apparatuses & Socketing", "MAGIC",
                "Arcanum Focus, Wand, Staff, Grimoire, and safe vs. field gem swaps.",
                "Overview & Description:\n" +
                "Raw Spellgems cannot be cast bare-handed. Socket them into an Engraved Weapon (1 slot), Arcanum Focus (1 slot), Arcanist Wand (2 slots, off-hand capable), Arcanist Staff (4 slots, -20% channeled drain), or Spellweaver Grimoire (9 slots). Hold Sneak + Scroll Wheel to cycle active slots instantly.\n\n" +
                "Workstation vs. Field Socketing:\n" +
                "▪ Safe Workstation Swap (Sneak + Right-Click Runic Anvil, Arcane Anvil, or Scribing Table): 0s lockout and 0% backfire.\n" +
                "▪ Emergency Field Swap (Right-Click in inventory): Triggers a 60s lockout and a 45% backfire risk on the first cast (bypassed automatically in Creative Mode, which also provides a full single-Materia Spellgem testing catalog in the Entropica: Spellgems Creative Tab).",
                "magic_spellcrafting", 1, new ItemStack(ModItems.ARCANUM_FOCUS.get()),
                460f, 0.0f, a_magic - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "spell_ten_stage_fuel_economy", "10-Stage Materia Spell Fueling", "MAGIC",
                "Multi-Materia Orbis Cell billing, fractional credits, and cross-tier rules.",
                "Overview & Description:\n" +
                "Spells draw exact per-part Materia costs from Orbis Cells, Ampoules, and Raw Essences in your inventory across all 10 Materia refinement stages (Raw Essence through Liminalia), storing leftover sub-unit fractions losslessly.\n\n" +
                "Cross-Tier Fueling:\n" +
                "▪ Higher-tier fuel powering a lower-tier spell: 0% penalty (exact conversion).\n" +
                "▪ Lower-tier fuel powering a higher-tier spell: +25% cost markup per tier jumped.",
                "spell_casting_apparatuses", 2, new ItemStack(ModBlocks.ORBIS_CELL.get()),
                550f, 0.0f, a_magic - 0.18f, false
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

        
        // ==========================================
        // ASTRAL MATERIA SUB-BRANCH (Expanding magic_astral)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "astral_instruments_and_charts", "Looking Glass & Celestial Instruments", "MAGIC",
                "Celestial observation, constellation charting, and vertex alignment.",
                "Overview & Description:\n" +
                "The night sky is not empty void—it is a cosmic vault leaking pure Materia through ancient stellar tears. Using a Looking Glass and Blank Star Chart, arkanists map constellations across the 8 lunar phases.\n\n" +
                "Origin & Obtaining:\n" +
                "Craft a Looking Glass with Starlight Birch timber and Quartz Lenses. Hold a Blank Star Chart at night while looking at the stars to trace vertices.\n\n" +
                "Crafting Uses:\n" +
                "Completed Star Charts are placed into the Astral Altar Core and Celestial Armillary to focus constellation powers.\n\n" +
                "Special Properties:\n" +
                "Constellation vertices have a 12px magnetic snap zone when tracing.",
                "magic_astral", 1, new ItemStack(ModItems.LOOKING_GLASS.get()),
                390f, 0.0f, a_magic + 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_astrolabe_and_holograms", "Astrolabe & Holographic Projections", "MAGIC",
                "Precision astronomical navigation, 3-slot memory bank, and 3D in-world ghost blueprint projection.",
                "Overview & Description:\n" +
                "The Astrolabe is a precision handheld coordinate dial providing up to 8x magnification and fine 15° vernier degree markings for celestial tracking.\n\n" +
                "Origin & Obtaining:\n" +
                "Forged with Brass Ingots, Quartz Lenses, and a Resonite gear mechanism.\n\n" +
                "Multi-Slot Memory Bank & Horizon Waypoints:\n" +
                "▪ Shift + Right-Click in air: Cycles through 3 independent memory slots ([Slot 1..3]) with distance readouts in meters.\n" +
                "▪ Right-Click on ground: Anchors or clears the holographic blueprint at the target location.\n" +
                "▪ Right-Click in air: Opens the sky looking glass or cycles blueprint layer slices.\n" +
                "▪ Waypoint Beacon: Anchored blueprints project a 32m vertical starlight beacon pillar visible from afar across the horizon.\n\n" +
                "Special Properties:\n" +
                "Translucent ghost templates provide live color-coded validation (cyan = missing, green = correct, red = invalid).",
                "astral_instruments_and_charts", 1, new ItemStack(ModItems.ASTROLABE.get()),
                470f, 0.0f, a_magic + 0.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_celestial_atlas", "Celestial Atlas & Compendium", "MAGIC",
                "Grand astronomical compendium storing up to 24 discovered Star Charts with live sky monitoring.",
                "Overview & Description:\n" +
                "The Celestial Atlas is an illustrated astronomical tome that binds and stores completed Star Charts into a unified celestial compendium.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted by binding an Astrolabe with a Book and Gold Ingots.\n\n" +
                "Crafting & Utility:\n" +
                "▪ Shift + Right-Click: Automatically scans your inventory and absorbs all completed Star Charts into the atlas.\n" +
                "▪ Right-Click: Opens the dual-page interactive compendium browser showing all 24 constellations categorized across 4 Tiers.\n\n" +
                "Special Properties:\n" +
                "Displays full astrological lore, lunar phase visibility, base flux rates, and real-time horizon status (zenith altitude vs. minutes until rising).",
                "astral_instruments_and_charts", 2, new ItemStack(ModItems.CELESTIAL_ATLAS.get()),
                470f, 0.0f, a_magic + 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_pantheon_constellations", "The 50 Constellations of Entropica", "MAGIC",
                "Five tiers of celestial stars, spectral classes, calibrated inverse flux rates, and coordinate conflicts.",
                "Overview & Description:\n" +
                "Entropica's cosmos is organized into 50 distinct constellations across 5 celestial tiers (Fundamental, Advanced, Master, Mythic, Transcendent):\n" +
                "▪ Fundamental (T1 - 16 Stars): Vespa Aculeus (Air), Lucerna Radialis (Radiant), Serpens Veneni (Umbral), Arbor Vitae (Vitae), Scutum Aegis (Aegis), Athanor Ignis (Magma), Gladius Ignis (Pyre), Penna Aetheris (Aether), Falx Aetheria (Sky Scythe), Cervus Vitae (Sylvan Hart), Noctua Lucis (Luminous Owl), Aranea Tenebrae (Web of Night), Calcar Vulcanis (Forge Hearth), Arcus Sidereus (Starlight Bow), Taurus Petrae (Stone Bull), Iris Spectralis (Prismatic Veil).\n" +
                "▪ Advanced (T2 - 16 Stars): Mineralis Geodae (Earth), Glacies Crystalline (Glacial), Ulteria Viatoris (Aurora), Resina Succini (Amber), Sanguis Chalybis (Blood), Fulgur Tonitrus (Lightning), Horologium Chroni (Astral), Bootes Pastoralis (Sylvan), Felis Succina (Amber Lynx), Draco Pyrotis (Pyre Drake), Ceraunius Sagitta (Thunder Arrow), Serpens Glacialis (Rime Viper), Speculum Abyssi (Abyssal Mirror), Pallium Tenebrae (Shroud of Dusk), Anulus Aeternus (Gyroscopic Ring), Sol Invictus (Unconquered Sun).\n" +
                "▪ Master (T3 - 8 Stars): Vorago Blighti (Blight), Velum Umbraticum (Penumbra), Echo Primordialis (Entropic), Corona Solaris (Celestial), Charybdis Profunda (Abyssal Maelstrom), Terranox Primus (World Impactite), Vorator Stellarum (Star Devourer), Aetherion Aeternus (Celestial Weave).\n" +
                "▪ Mythic (T4 - 4 Stars): Leviathan (Abyss), Yggdrasil (Genesis), Ouroboros (Aether), Azathoth (Oblivion).\n" +
                "▪ Transcendent (T5 - 6 Stars): Astrape Primordialis, Aegis Ecliptica, Ignis Eschaton, Aether Aeternitas, Umbra Genesis, Nihil Coronatum.\n\n" +
                "Stargazing Pinning & Coordinate Conflicts:\n" +
                "Right-clicking a Completed Star Chart pins that constellation to the night sky for 10 minutes, enhancing star size (+80%) and radiant beam lines. Simultaneously attuning conflicting shared-coordinate constellations causes destructive harmonic rejection.",
                "magic_astral", 1, new ItemStack(ModItems.STAR_CHART_COMPLETED.get()),
                390f, 0.0f, a_magic + 0.12f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_materia_flux_distributor", "Materia-Flux Distributor & Wireless Charging", "MAGIC",
                "Subterranean pneumatic siphoning and wireless Materia-Flux distribution to Inscribed Crystals.",
                "Overview & Description:\n" +
                "The Materia-Flux Distributor wirelessly channels stored Materia Fumus into Inscribed Astral Crystals within a large spherical radius.\n\n" +
                "Origin & Obtaining:\n" +
                "Assembled using Astral Brass plates, an Attuned Quartz disc, and a pneumatic manifold port. Connects directly to Materia Fumus pipes.\n\n" +
                "Vertical Siphoning Physics:\n" +
                "When placed directly underneath an Astral Collector, the distributor automatically draws stored Materia downward into its internal buffer.\n\n" +
                "Tier Upgrades & Distribution Range:\n" +
                "▪ Tier 1: 32-block radius | 4,000 mb buffer | 10 Materia/s charging\n" +
                "▪ Tier 2: 64-block radius | 8,000 mb buffer | 25 Materia/s charging\n" +
                "▪ Tier 3: 96-block radius | 16,000 mb buffer | 60 Materia/s charging\n\n" +
                "Right-clicking the block with Astral Crystals or upgrade plates upgrades the tier up to Tier 3.",
                "astral_collector_materia_reservoir", 2, new ItemStack(ModBlocks.MATERIA_FLUX_DISTRIBUTOR.get()),
                720f, 0.0f, a_magic + 0.06f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_altar_and_observatory", "Astral Altar & Observatory Structure", "MAGIC",
                "Multiblock structures for channeling starlight into transmutations and advanced crafting.",
                "Overview & Description:\n" +
                "The Astral Observatory and Astral Altar are multiblock ritual structures designed to capture and focus cosmic energy.\n\n" +
                "Origin & Obtaining:\n" +
                "Constructed using Astral Marble, Chiseled Astral Marble, Sooty Marble, and an Altar Core.\n\n" +
                "Crafting Uses:\n" +
                "Enables high-tier essence infusions, celestial weapon crafting, and cosmic ritual channeling.\n\n" +
                "Special Properties:\n" +
                "Requires direct line-of-sight to the night sky; enhanced during full moons and solar alignments.",
                "astral_instruments_and_charts", 2, new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get()),
                480f, 0.0f, a_magic + 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_mirror_reflection", "Astral Mirror & 6-Way Reflections", "MAGIC",
                "Connected multi-directional mercury mirrors with real-time celestial sky and constellation rendering.",
                "Overview & Description:\n" +
                "The Astral Mirror Block is a specialized planar reflective surface that mirrors the real-time diurnal firmament, nebulae, stars, and charted constellations.\n\n" +
                "6-Way Multi-Directional CTM:\n" +
                "Features complete 6-way connected texturing (UP, DOWN, NORTH, EAST, SOUTH, WEST). Interior brass borders disappear seamlessly when adjacent mirror blocks are connected horizontally or vertically.\n\n" +
                "Multi-Surface Sky Reflection:\n" +
                "Renders celestial starlight reflections across any exposed surface: horizontal floor pools, vertical mirror walls, ceilings, and concave parabolic observation dishes.\n\n" +
                "Interaction & Observatories:\n" +
                "Forms the central 5x5 starlight reflective basin for the Grand Astral Observatory. Right-clicking an active mirror pool beneath a valid Celestial Armillary opens the Sky Looking Glass directly.",
                "astral_altar_and_observatory", 2, new ItemStack(ModBlocks.ASTRAL_MIRROR_BLOCK.get()),
                560f, 0.0f, a_magic - 0.06f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_optics_and_logistics", "Pure Optic Conduits & Diagnostic Ping", "MAGIC",
                "Lossless optical transmission, 8-block quick-dyeing, sponge cleanse, and wand diagnostic ping.",
                "Overview & Description:\n" +
                "Pure Optic Fiber cables transmit concentrated starlight flux underground and through solid blocks with only 1% attenuation per 16 blocks.\n\n" +
                "Origin & Obtaining:\n" +
                "Drawn from high-purity fused quartz encased in a solid vulcanized matte black rubber jacket (zero brass) with 2x2 transparent end apertures.\n\n" +
                "Quick-Dyeing, Cleansing & Diagnostic Tracing:\n" +
                "▪ Sneak + Right-Click with Dye: Propagates color across up to 8 contiguous connected fibers in 1 click.\n" +
                "▪ Sponge / Water Cleanse: De-colors fibers back to universal white (up to 8 contiguous if sneaking).\n" +
                "▪ Linking Wand Ping: Right-clicking with the Astral Linking Wand runs a BFS cable trace, pulsing end-rod particles and displaying fiber, amplifier, and receiver counts in the actionbar.\n\n" +
                "Special Properties:\n" +
                "Adjacent fibers of different colors never cross-connect, allowing dense parallel cable bundles.",
                "astral_altar_and_observatory", 2, new ItemStack(ModBlocks.PURE_OPTIC_FIBER.get()),
                560f, 0.0f, a_magic + 0.04f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_optic_receiver_and_transmitter", "Wireless Optic Receiver & Transmitter", "MAGIC",
                "Straight-line wireless optical interfaces connecting open air starlight beams to pure optic fiber cable runs.",
                "Overview & Description:\n" +
                "The Optic Receiver and Optic Transmitter are modular silver pipe components that bridge aerial optical beams and physical optic fiber networks.\n\n" +
                "Optic Receiver:\n" +
                "Equipped with a directional quartz sensor aperture on its facing side, the Optic Receiver captures straight-line beams from Refractive Lenses, Secondary Lenses, or Transmitters, and injects the starlight stream into all connected Pure Optic Fiber cables.\n\n" +
                "Optic Transmitter:\n" +
                "Draws starlight from connected fiber runs and collimates it into a straight-line laser firing out of its nozzle up to 32 blocks forward. The beam hits pedestals, collectors, prisms, or receivers, while inflicting Materia toxicity on any entities in its path.\n\n" +
                "Pipe Geometry:\n" +
                "Both units utilize Entropica's modular pipe model with an all-around silver finish, seamlessly connecting to optic fiber cables.",
                "astral_optics_and_logistics", 2, new ItemStack(ModBlocks.OPTIC_TRANSMITTER.get()),
                600f, 0.0f, a_magic + 0.03f, false
        ));

        ALL_NODES.add(new CodexNode(
                "caged_optic_bulb", "Caged Optic Bulb", "MAGIC",
                "Directional filament lamp illuminated and tinted dynamically by active Materia flux through optic fibers.",
                "Overview & Description:\n" +
                "The Caged Optic Bulb is a compact, industrial caged lamp that connects directly to Pure Optic Fiber cable networks.\n\n" +
                "Dynamic Chromatic Illumination:\n" +
                "Whenever pulses of starlight or Materia flow through the fiber line, the caged bulb illuminates at maximum light level (15) and dynamically tints its inner filament and glowing envelope to the exact hue of the active Materia type.\n\n" +
                "Omnidirectional Mounting:\n" +
                "Can be attached to floors, ceilings, walls, or directly onto optic fiber cable runs to serve as an instant visual network activity monitor and decorative atmospheric lighting.",
                "astral_optics_and_logistics", 2, new ItemStack(ModBlocks.CAGED_OPTIC_BULB.get()),
                640f, 0.0f, a_magic + 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "glowpeg", "Glowpeg", "MAGIC",
                "Architectural fence-sized glowing rod providing pure, warm glowstone luminescence across floors, walls, and ceilings.",
                "Overview & Description:\n" +
                "The Glowpeg is an architectural light rod sized to match a fence post, capped with polished brass plates and filled with radiant, concentrated glowstone.\n\n" +
                "Crafting:\n" +
                "Fashioned from a central wooden stick flanked by 2 pinches of glowstone dust, yielding 1 Glowpeg.\n\n" +
                "Architectural Mounting:\n" +
                "Designed to stay strictly vertical across all placements for an authentic classical aesthetic:\n" +
                "▪ Floor: Stands upright atop blocks, pillars, and pedestals.\n" +
                "▪ Wall & Pillar: Mounts vertically against vertical surfaces with brass standoff brackets.\n" +
                "▪ Ceiling & Chains: Suspends vertically from ceilings, aligning seamlessly with hanging chains.\n\n" +
                "Luminescence:\n" +
                "Maintains a constant, rich warm-yellow glowstone illumination at Light Level 15, fully waterloggable.",
                "astral_optics_and_logistics", 2, new ItemStack(ModBlocks.GLOWPEG.get()),
                640f, 0.0f, a_magic + 0.055f, false
        ));

        ALL_NODES.add(new CodexNode(
                "soul_glowpeg", "Soul Glowpeg", "MAGIC",
                "Ethereal light blue soul fire variant of the architectural Glowpeg, casting calm azure light.",
                "Overview & Description:\n" +
                "The Soul Glowpeg combines the fence-sized architectural rod design of the Glowpeg with the supernatural radiance of soul fire.\n\n" +
                "Crafting & Transmutation:\n" +
                "▪ Crafted using Glowstone Dust, a wooden Stick, and Soul Sand or Soul Soil.\n" +
                "▪ Alternatively, any standard Glowpeg can be transmuted with a block of Soul Sand or Soul Soil in a crafting grid.\n\n" +
                "Supernatural Properties:\n" +
                "▪ Radiates a gentle, eerie cyan illumination at Light Level 10, preventing Piglin agitation.\n" +
                "▪ Emits soft soul flame particles when active.\n" +
                "▪ Retains strictly vertical alignment across floor, wall, and ceiling mounts.",
                "glowpeg", 2, new ItemStack(ModBlocks.SOUL_GLOWPEG.get()),
                660f, 0.0f, a_magic + 0.06f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_lamp_posts", "Lamp Posts & Street Illumination", "MAGIC",
                "Modular 1-to-3 block tall tapered lamp posts conducting starlight flux to cardinal arm bulbs.",
                "Overview & Description:\n" +
                "Lamp Posts provide elegant, architectural vertical starlight transmission and municipal illumination across 4 distinct material variants: Iron, Brass, Steel, and Essence Glass.\n\n" +
                "Zero-Gap Baseplate:\n" +
                "Each lamp post base features a full 16x16x1 bottom plate that perfectly covers and conceals underlying optic fiber conduits with zero exposed gaps.\n\n" +
                "Dynamic Vertical Tapering:\n" +
                "Lamp posts seamlessly adapt their model across 1, 2, or 3 blocks tall (enforcing a hard cap at 3 high), gracefully tapering from a broad 10-pixel pedestal up to a slender 4-pixel neck and crown.\n\n" +
                "Interactive Cardinal Arms & Bulbs:\n" +
                "▪ Right-click with an empty hand to toggle ornamental support arms extending North, East, South, or West.\n" +
                "▪ Right-click an arm with a Caged Optic Bulb to snap and hang the bulb directly from the arm bracket.\n" +
                "▪ Sneak right-click to safely detach bulbs or remove arms.\n\n" +
                "Integrated Flux Conduction:\n" +
                "Lamp posts conduct starlight flux from underground optic fiber cables straight up into all attached arm bulbs, brightly illuminating at light level 15 and dynamically tinting to the flowing Materia color.",
                "astral_optics_and_logistics", 2, new ItemStack(ModBlocks.IRON_LAMP_POST.get()),
                680f, 0.0f, a_magic + 0.07f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_flux_cosmetics", "Celestial Architecture & Wireless Flux Fixtures", "MAGIC",
                "Decorative and functional fixtures powered wirelessly by the Materia-Flux Distributor or directly via pure optic fiber.",
                "Overview & Description:\n" +
                "Beyond utilitarian conduits and municipal street lamps, celestial architects can craft ornate architectural fixtures that harness ambient Materia Flux wirelessly or via direct cable routing.\n\n" +
                "Resonance Starlight Fountain & Birdbath:\n" +
                "▪ Carved from Astral Marble with an internal Essence Glass basin.\n" +
                "▪ When powered, erupts with a bubbling starlight geyser and animated starlight pool surface.\n" +
                "▪ Emits a soothing celestial aura within 8 blocks that clears harmful status effects (Poison, Wither, Slowness, Weakness) and bestows gentle health regeneration.\n\n" +
                "Luminous Garden Arbor & Trellis Archways:\n" +
                "▪ Fine brass lattice work wrapped in star-blossomed wisteria vines.\n" +
                "▪ When powered, glows softly and bathes nearby crops, saplings, and herbs in astral photosynthetic light, accelerating their growth cycles.\n\n" +
                "Attuned Display Vitrine & Bell Jar:\n" +
                "▪ A velvet-cushioned brass plinth protected beneath a crystal glass dome.\n" +
                "▪ Right-click with any item to display it inside.\n" +
                "▪ When powered, projects a collimated vertical starlight spotlight beam that suspends the item mid-air in continuous, graceful rotation.\n\n" +
                "Celestial Grandfather Chronometer:\n" +
                "▪ Tall, ornate astronomical timepiece with swinging brass pendulum and dual celestial dials.\n" +
                "▪ The upper dial tracks diurnal solar and nocturnal stellar movements, while the lower dial tracks real-time lunar phases.\n" +
                "▪ When flux-powered, celestial constellations glow and a deep, resonant celestial bell chimes across the estate at the turn of every in-game hour.\n\n" +
                "Illuminated Balustrades & Glass Railings:\n" +
                "▪ Frosted starlight glass panes framed in polished metal balusters.\n" +
                "▪ Connects seamlessly into straight railings, corner posts, and junctions.\n" +
                "▪ Edge-lit with vibrant starlight tinting to the frequency of flowing Materia.\n\n" +
                "Wireless Flux Distribution:\n" +
                "All 5 fixtures possess internal resonant starlight receivers. They require no cables when placed within range (up to 16 blocks) of an active Materia-Flux Distributor, or they can be placed directly on Pure Optic Fiber cables.",
                "astral_lamp_posts", 2, new ItemStack(ModBlocks.RESONANCE_STARLIGHT_FOUNTAIN.get()),
                720f, 0.0f, a_magic + 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "starlight_cloth_and_carpets", "Starlight Cloth & Celestial Carpets", "MAGIC",
                "Frictionless celestial silk woven from starlight threads, enabling high-speed gliding transit runners.",
                "Overview & Description:\n" +
                "By weaving delicate Starlight Silk with reinforced Astral Crystal Thread, celestial artificers produce Starlight Cloth—a fabric imbued with the low-friction properties of deep astral space.\n\n" +
                "Starlight Cloth:\n" +
                "▪ A shimmering textile used in astral apparel, ritual sails, and celestial apparatus.\n" +
                "▪ Woven from Starlight Silk and Astral Crystal Thread.\n\n" +
                "Starlight Carpet:\n" +
                "▪ Crafted from 2 Starlight Cloth bolts, producing 3 decorative carpet tiles patterned with midnight constellations.\n" +
                "▪ Frictionless Glide: Features ultra-low surface friction (comparable to Blue Ice), allowing players and mobs to slide effortlessly across vast distances.\n" +
                "▪ Kinetic Acceleration: Walking or running along the carpet imparts a passive +35% movement speed boost, turning corridors into high-speed transit runways.\n" +
                "▪ Emits soft ambient starlight (Light Level 8) and leaves sparkling starlight footprints when traversed.\n\n" +
                "Reversible Weaving:\n" +
                "3 Starlight Carpets can be unraveled back into 2 Starlight Cloth bolts at a crafting table.",
                "astral_flux_cosmetics", 2, new ItemStack(ModBlocks.STARLIGHT_CARPET.get()),
                750f, 0.0f, a_magic + 0.085f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_optical_relays_and_hazards", "Optical Relays & Radiation Hazards", "MAGIC",
                "Gimbaled refractive lenses, 4-way beam splitters, comparator automation, and occlusion hazards.",
                "Overview & Description:\n" +
                "Refractive Astral Lenses, Secondary Astral Lenses, and Beam Splitter Prisms direct, split, and relay collimated starlight beams across open air.\n\n" +
                "Origin & Obtaining:\n" +
                "Assembled using polished Starlight Quartz discs, brass friction gimbals, and Astral Marble plinths.\n\n" +
                "Automation & Occlusion Alerts:\n" +
                "▪ Redstone Comparators: Output signal strength 15 when actively focusing or splitting starlight (receiver ports output 0..15 scaled to signal quality).\n" +
                "▪ Occlusion Sizzle & Sparks: Entities stepping into active starlight beams trigger high-pitched audio sizzles and electric spark particles while incurring Materia Toxicity.\n\n" +
                "Special Properties:\n" +
                "Lenses feature full 3D friction gimbals with first-person alignment and wand auto-calibration.",
                "astral_optics_and_logistics", 2, new ItemStack(ModBlocks.REFRACTIVE_ASTRAL_LENS.get()),
                640f, 0.0f, a_magic + 0.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_beam_interconnection", "Starlight Beam Routing & Multiblock Networks", "MAGIC",
                "Inter-connecting multiblocks via collimated starlight beams, altar amplification, and collector cannibalization.",
                "Overview & Description:\n" +
                "Entropica's celestial architecture allows large-scale multiblock installations to connect wirelessly across space via focused starlight beams.\n\n" +
                "Multiblock-to-Multiblock Inter-Connection:\n" +
                "Astral Collection Altars automatically seek out or can be manually linked using the Astral Linking Wand to beam concentrated cosmic flux directly into Master Astral Altar corner receiver lenses.\n\n" +
                "Collector Cannibalization & Cascading:\n" +
                "Collection Altars can also target adjacent Collection Altars. When linked in sequence, the downstream altar cannibalizes the incoming starlight stream, compounding its power before beaming it forward.\n\n" +
                "Cycle Safety:\n" +
                "The optical network includes cycle detection algorithms that prevent closed loops or harmonic infinite feedback.",
                "astral_optical_relays_and_hazards", 2, new ItemStack(ModItems.ASTRAL_LINKING_WAND.get()),
                720f, 0.0f, a_magic + 0.04f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_secondary_lens", "Secondary Astral Lens & Gimbal Aiming", "MAGIC",
                "Terrestrial starlight beam redirection and real-time first-person in-world aiming calibration.",
                "Overview & Description:\n" +
                "The Secondary Astral Lens is a freestanding optical workstation designed for complex horizontal and vertical beam routing around obstacles and between base facilities.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted using Astral Brass ingots, an Astral Marble plinth, and a polished Starlight Quartz disc.\n\n" +
                "Aiming & Calibration:\n" +
                "Right-clicking the lens temporarily shifts player camera perspective directly to the lens gimbal head in first-person mode. Moving the mouse smoothly pans Azimuth (0°-360°) and tilts Pitch (-85° to +85°) with live distance and target HUD readouts.\n\n" +
                "Astral Linking Wand Auto-Alignment:\n" +
                "Shift-clicking a Secondary Lens with an Astral Linking Wand then clicking any target block automatically calculates the exact azimuth and pitch angles to lock the beam onto the destination.\n\n" +
                "Center-Terminating Beam Physics:\n" +
                "Primary and upstream lenses detect the Secondary Lens and terminate incoming beams cleanly at the central quartz disc, eliminating beam overshoot.",
                "astral_optical_relays_and_hazards", 3, new ItemStack(ModBlocks.SECONDARY_ASTRAL_LENS.get()),
                720f, 0.0f, a_magic + 0.01f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_calcite_transmutation", "Calcite Starlight Transmutation", "MAGIC",
                "Transmuting Calcite into Medium Essence Orbs via concentrated optical starlight beams.",
                "Overview & Description:\n" +
                "Direct continuous irradiation of Calcite using a focused starlight beam from a Refractive Astral Lens or Secondary Astral Lens triggers deep mineral transmutation.\n\n" +
                "In-World Transmutation Process:\n" +
                "Exposing a placed Calcite block or dropped Calcite item entity directly to a starlight beam for 5.0 seconds (100 ticks) transfigures the calcite matrix into a Medium Essence Orb.\n\n" +
                "Essence Attribution:\n" +
                "The resulting Medium Essence Orb inherits the exact elemental Materia essence of the locked celestial star, planet, comet, or supernova remnant (e.g. Vitae from Sylva, Singularity from Stellar Black Holes, Lightning from Pulsars).\n\n" +
                "Audio & Visual Resonance:\n" +
                "During charging, the calcite emanates enchanting starlight glyphs, erupting into a resonant amethyst chime and totem particle flash upon successful transfiguration.",
                "astral_optical_relays_and_hazards", 2, new ItemStack(Items.CALCITE),
                720f, 0.0f, a_magic + 0.05f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_beam_splitter_prism", "Beam Splitter Prism & Orthogonal Splitting", "MAGIC",
                "Incident-angle 90° dual orthogonal starlight splitting turntable.",
                "Overview & Description:\n" +
                "The Beam Splitter Prism divides an incoming collimated starlight beam into two orthogonal streams.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted from 1 Astral Crystal Block, 4 Astral Brass Ingots, and 4 Refractive Astral Lenses.\n\n" +
                "Splitting Physics:\n" +
                "Dynamically calculates the incoming beam incident angle from the source position. Projects two distinct output beams at exactly 90° left and 90° right relative to the incoming trajectory.\n\n" +
                "Special Properties:\n" +
                "Each split beam carries full starlight potency to downstream receivers, with independent entity collision and Materia Toxicity raycasting.",
                "astral_optical_relays_and_hazards", 3, new ItemStack(ModBlocks.BEAM_SPLITTER_PRISM.get()),
                720f, 0.0f, a_magic + 0.03f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_infusion_pedestal", "Astral Infusion Pedestal & Automation", "MAGIC",
                "Dry starlight transmutations, elemental flora swapping, hopper input, and downward auto-ejection.",
                "Overview & Description:\n" +
                "A solid marble plinth that focuses overhead starlight beams onto held items for dry transmutation rituals without liquid pools.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted from an Astral Pedestal, 1 Astral Crystal Block, and Gold Ingots.\n\n" +
                "Automated Factory Integration:\n" +
                "▪ Hopper & Pipe Input: Accepts input items via hoppers or pneumatic pipes into its 1-item container when empty and idle.\n" +
                "▪ Downward Auto-Ejection: Automatically pushes finished transmutation products into containers or hoppers directly beneath it.\n\n" +
                "Elemental Flora Swapping:\n" +
                "Irradiating standard vanilla flowers converts them into elemental variants based on the focused constellation:\n" +
                "▪ Ignis / Pyre / Athanor → Soul-Flame Orchid\n" +
                "▪ Vitae / Arbor / Sylvan → Vitae Orchid\n" +
                "▪ Scutum / Aegis → Aegis-Spire Orchid\n" +
                "▪ Serpens / Vorago / Abyss → Void-Stalker Orchid\n\n" +
                "Substrate Metamorphism:\n" +
                "Continuous irradiation transmutes the soil directly beneath into Sooty Marble, Engraved Astral Slate, or Astral Marble.",
                "astral_altar_and_observatory", 2, new ItemStack(ModBlocks.ASTRAL_INFUSION_PEDESTAL.get()),
                640f, 0.0f, a_magic - 0.02f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_collector_materia_reservoir", "Astral Collector & Condensation", "MAGIC",
                "Starlight condensation basin and single-essence Materia storage reservoir.",
                "Overview & Description:\n" +
                "The Astral Collector condenses concentrated starlight beam radiation into pure liquid Materia, holding up to 2,000 units of single-essence Materia in its internal reservoir.\n\n" +
                "Origin & Obtaining:\n" +
                "Sculpted from Astral Marble Bricks, Brass Trim, and an attuned Starlight Crystal receptor node.\n\n" +
                "Crafting Uses:\n" +
                "Accumulates Materia at 8 Materia/s (configurable) under concentrated optical starlight irradiation.\n\n" +
                "Special Properties:\n" +
                "Automatically purges previously stored Materia if an incompatible essence type is focused into the collector.",
                "astral_optics_and_logistics", 2, new ItemStack(ModBlocks.ASTRAL_COLLECTOR.get()),
                640f, 0.0f, a_magic + 0.06f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_crystals_and_impactite", "Astral Crystals & Impactite", "MAGIC",
                "Dual-stat growing crystals, self-repairing tools, and extraterrestrial impactites.",
                "Overview & Description:\n" +
                "Astral Crystals possess unique dual statistics: Size (1 to 5) and Purity (10% to 100%). Mined from rare deep-slate Celestial Geodes formed of shock-vitrified Astral Impactite.\n\n" +
                "Origin & Obtaining:\n" +
                "Budding Astral Impactite sprouts crystal clusters over time. Mined clusters drop raw Astral Crystals.\n\n" +
                "Crafting Uses:\n" +
                "Forges self-repairing Crystal Swords, Pickaxes, Axes, and Shovels that mend under the night sky.\n\n" +
                "Special Properties:\n" +
                "When broken, size-5 crystal tools enter a drained dormant state rather than vanishing, regaining full durability when bathed in starlight.",
                "astral_altar_and_observatory", 2, new ItemStack(ModItems.ASTRAL_CRYSTAL.get()),
                560f, 0.0f, a_magic + 0.12f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_celestial_beacons", "Celestial Beacons & Singularity Wards", "MAGIC",
                "Radial passive constellation fields, mob suppression, and forcefield domes.",
                "Overview & Description:\n" +
                "A 3x3x3 multiblock beacon topped with an attuned Astral Crystal projecting planetary-scale astrological fields.\n\n" +
                "Origin & Obtaining:\n" +
                "Built using Astral Marble, corner Resonance Pylons, a Celestial Beacon Controller, and tuned crystal apex.\n\n" +
                "Crafting Uses:\n" +
                "Projects Lucerna mob suppression (up to 128 blocks), Mineralis automatic stone transmutation, and Scutum forcefield domes.\n\n" +
                "Special Properties:\n" +
                "Beacon radius scales dynamically based on crystal Size: Radius = 32 + (Size - 1) * 24 blocks.",
                "astral_crystals_and_impactite", 3, new ItemStack(ModBlocks.CELESTIAL_BEACON_CONTROLLER.get()),
                640f, 0.0f, a_magic + 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_supernovae_and_remnants", "Stellar Supernovae & Metamorphosis", "MAGIC",
                "Core-collapse supernovae, Figure-8 bipolar nebulae, and exotic stellar remnants.",
                "Overview & Description:\n" +
                "Isolated stars outside the sacred constellation lattice occasionally undergo thermal instability and cataclysmic core collapse. The resulting detonation produces an expanding Figure-8 Bipolar Hourglass Nebula of ionized gas.\n\n" +
                "Origin & Obtaining:\n" +
                "Observed across the nocturnal firmament via the Looking Glass or Telescope. Over thousands of ticks, the precursor instability ignites into a blinding flash before settling into a persistent remnant.\n\n" +
                "Crafting Uses:\n" +
                "Aiming a Refractive Astral Lens at a supernova remnant channels mutated high-order essences: STATIC, LIGHTNING, PHOTON, DENSITY, ENTROPICA, and PYRE.\n\n" +
                "Special Properties:\n" +
                "Collapsed cores evolve into Pulsars, Magnetars, White Dwarfs, Strange Quark Stars, or Collapsars.",
                "astral_instruments_and_charts", 2, new ItemStack(ModItems.LOOKING_GLASS.get()),
                580f, 0.0f, a_magic - 0.18f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_black_holes_and_singularities", "Stellar Black Holes & Singularities", "MAGIC",
                "Gravitational singularities, swirling accretion disks, and void essence transmutation.",
                "Overview & Description:\n" +
                "When high-mass stars undergo complete gravitational collapse, they form Stellar Black Holes: infinite density singularities enveloped in luminous spinning accretion discs and relativistic jets.\n\n" +
                "Origin & Obtaining:\n" +
                "Born from Class V stellar collapse events. Located by optical coordinate telemetry in the Looking Glass.\n\n" +
                "Crafting Uses:\n" +
                "Calibrating Astral Lenses to a Black Hole channels pure SINGULARITY, VOID, ESCHATON, NULL, ABYSS, OBLIVION, and CHRONOS essences into downstream machinery.\n\n" +
                "Special Properties:\n" +
                "Channels highest-tier Materia toxicity and extreme transmutation pressure into connected infusers and generators.",
                "astral_supernovae_and_remnants", 3, new ItemStack(ModBlocks.REFRACTIVE_ASTRAL_LENS.get()),
                660f, 0.0f, a_magic - 0.22f, false
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

        ALL_NODES.add(new CodexNode(
                "caprine_duality_arcana", "Caprine Duality Arcana", "MAGIC",
                "Esoteric secrets of Radiant Fire and Umbral Waters mastered by the Gemini Goat Mage.",
                "Overview & Description:\n" +
                "The Caprine Sages discovered that opposing elemental extremes—blinding Radiant Fire and deep Umbral Waters—do not annihilate when harmonized through an Equinox focal matrix. Instead, they unlock phase-inversion alchemy and steam-cavitation shockwaves.\n\n" +
                "Origin & Obtaining:\n" +
                "Unlocked by presenting an Entropic Codex or astronomical instrument to an elusive Gemini Goat Mage wandering the alpine summits.\n\n" +
                "Crafting & Transmutation:\n" +
                "Unlocks the Gemini Scepter of Equinox, Equinox Velvet Cloth, Equinox Robes & Floppy Wizard Hat, and lossless Radiant-Fire <-> Umbral-Water alchemy.\n\n" +
                "Special Properties:\n" +
                "The Floppy Caprine Wizard Hat provides a +20% bonus in the Materia Advantage damage calculation.",
                "hub_magic", 2, new ItemStack(ModItems.GEMINI_FOCUS_HORN.get()),
                340f, 0.0f, a_magic + 0.35f, false
        ));

        ALL_NODES.add(new CodexNode(
                "firmament_weaver", "Firmament Weaver & Forcefield Barriers", "MAGIC",
                "Celestial tool for weaving customizable iridescent forcefield barriers, elastic deflectors, and directional one-way valves.",
                "Overview & Description:\n" +
                "The Firmament Weaver is an advanced celestial focus wand used to manipulate localized spacetime fabrics into paper-thin, non-block forcefield barriers. Rather than occupying bulky block voxels, firmament membranes coexist freely in world-space, allowing blocks and machinery to pass through them while presenting an impenetrable barrier to unauthorized entities and high-velocity projectiles.\n\n" +
                "Origin & Obtaining:\n" +
                "Crafted at a crafting table using an Astral Crystal, Materia Crystals, and Astral Silver Rods.\n\n" +
                "Weaving & Barrier Operations:\n" +
                "▪ Two-Point Drag & Snap: Right-click Point A to set an anchor beacon, then right-click Point B across an opening to automatically calculate dimensions and span a planar barrier flush with the frame.\n" +
                "▪ 0.5m Edge-Fusing: Placing adjacent barriers within 0.5 blocks automatically snaps border vertices together seamlessly, eliminating visual seams and z-fighting.\n" +
                "▪ Configuration GUI: Sneak + Right-Click an existing barrier while holding the Weaver to open the Celestial Configuration Screen, modifying shape, filter modes, dimensions, elasticity, redstone response, and whitelist access.\n" +
                "▪ Holographic Preview: Holding the Weaver projects a real-time, shimmering wireframe outline showing exact in-world placement before commitment.\n" +
                "▪ Quick Dispel: Left-click a placed barrier as creator to instantly dissipate it into sparkling starlight bubbles.\n\n" +
                "Special Properties & Physics:\n" +
                "Features Continuous Swept Collision Detection (CCD) preventing projectile tunneling, side-of-approach elastic reflection, drifting starlight arrow directional valves, redstone & Materia switchability, dye/crystal color tinting, and Apex Predator boss arena protection.",
                "hub_magic", 1, new ItemStack(ModItems.FIRMAMENT_WEAVER.get()),
                280f, 0.0f, a_magic + 0.16f, false));

        ALL_NODES.add(new CodexNode(
                "spellcrafting_workstations", "Spellcrafting Workstations & Apparatuses", "MAGIC",
                "Arcane Scribing Table 6-Ring Spell Circle (0m-128m Distance Rim), Runic Anvil, Research Bench study, and 4 Casting Apparatuses.",
                "Overview & Description:\n" +
                "Physical spellcrafting revolves around dedicated arcane workstations and multi-socketed Casting Apparatuses.\n\n" +
                "Workstations & Tools:\n" +
                "▪ Arcane Scribing Table: 2-block drafting desk with pull-out side drawers and a 6-part Concentric Rotating Spell Circle (Outer Rim Travel Distance Circle [0m/8m/16m/24m/32m/48m/64m/96m/128m] -> Trigger Logic Ring -> Flow Ring -> Delivery Form Ring -> Primary Effect Ring -> Center Dual-Verb Core) that emits a light carved-stone grinding & notch-click sound as each ring turns. Drag out the Left Drawer to pick Combat/Utility/Misc wax seals, set Single (I) to Octuple (VIII) Multi-Cast tabs (left-click to select/expand, right-click or scroll wheel to shrink), spin or click the Outer Rim Distance Circle to set exact spell travel reach (Shift + Right-Click to snap back to 16m), right-click the Trigger Ring or Flow pill to tune Timed Fuse (0.5s-3.0s) or Self-Timed (30s/60s/120s) intervals, infuse colored Materia vials (including Void or Void-created Materia with Mutation: ACTIVE to unlock Void-Warp on the Flow Ring), and flip the Mutation lever; spin all 6 zones under the golden 12 o'clock needle while reading Spell Decision Tooltips; and slide open the Right Supply Drawer to load Blank Parchment and Arcane Scribing Ink, press the golden SCRIBE wax seal, copy blueprints, scrub Spellgems, or safely swap sockets.\n" +
                "▪ Runic Anvil: 2-block metallurgic anvil for 100% safe, zero-lockout Spellgem socketing/extraction, weapon & tool spell socket engraving, and Alloy Efficiency Tempering (+5% to +30% fuel discount). Load the two parts onto the anvil to conjure a 3D Magic Circle between them and smash them together with an Ethereal Hammer, leaving the finished item floating above the anvil until picked up.\n" +
                "▪ Research Bench: Finalizes Observed spell nodes and Signature Mutations over 5 seconds using Scribing Ink, Blank Parchment, and a matching Materia sample.\n" +
                "▪ Casting Apparatuses: Arcanum Focus (1 gem), Arcanist Wand (2 gems), Arcanist Staff (4 gems), and Spellweaver Grimoire (9 gems), each displaying live Materia-tinted crystal gems in 3D and supporting hold-and-release compression casting for On-Release triggers.",
                "spell_modular_architecture", 2, new ItemStack(ModItems.ARCANE_SCRIBING_TABLE_ITEM.get()),
                550f, 0.0f, a_magic - 0.14f, false
        ));

        ALL_NODES.add(new CodexNode(
                "autocast_pedestals", "Autocast Pedestals & Blank Spellgems", "MAGIC",
                "Starlight-condensed & water-grown Blank Spellgems, plus automated Stone, Marble, Quartz, and Astral Slate spell turrets.",
                "Overview & Description:\n" +
                "Blank Spellgems serve as the crystalline vessel for every bound spell, while Autocast Pedestals automate continuous utility and defensive spellcasting.\n\n" +
                "Obtaining Blank Spellgems:\n" +
                "▪ Pathway A (Starlight Condensation): Focus Starlight onto Glass or Glass Panes at an Astral Collector, Astral Infusion Pedestal, or Resonance Starlight Fountain.\n" +
                "▪ Pathway B (Crystal Seed Growth): Craft a Spellgem Crystal Seed (Amethyst Shard + 4 Redstone + 4 Quartz) and submerge the dropped seed in water for 10 minutes.\n\n" +
                "Autocast Pedestals:\n" +
                "Available in Stone, Marble, Quartz, and Astral Slate. Mount an Inscribed Spellgem or Apparatus, supply an Orbis Cell (internally or placed adjacent), and configure the 1.0s–60.0s Cast Timer, -90°..+90° Aim Pitch, and Redstone mode. Sneak-right-click a chest with any Casting Apparatus and then sneak-right-click the pedestal to link harvested item routing!",
                "spellcrafting_workstations", 3, new ItemStack(ModItems.AUTOCAST_PEDESTAL_ASTRAL_SLATE_ITEM.get()),
                640f, 0.0f, a_magic - 0.16f, false
        ));

        ALL_NODES.add(new CodexNode(
                "astral_scholar_journals", "Scholar's Journals (Volumes I–XIII)", "MAGIC",
                "All 13 personal field journals, memoirs, and chronicles of the Astral Spire scholars with 233 readable entries.",
                "Overview & Description:\n" +
                "Across thirteen leather-bound volumes and 233 chapters, the great scholars of Mount Aetheria and the Grand Observatorium—Archon Valerius, Master Oron, Lady Vespera, Ronald of Oakhaven, Master Artificer Kaelen, High Alchemist Justinian, Brother Cadmus, Archon Aurelius, High Astrologer Gerald, and High Astrologer Vaelin—recorded their discoveries of stellar Materia fountains, all 50 constellations, optical engineering, supernovae, the Red Void wars, and the Starlight Covenant.\n\n" +
                "Origin & Obtaining:\n" +
                "▪ Survival Crafting: Bind a Book with Scribing Ink (or an Ink Sac) and two thematic reagents matching each volume.\n" +
                "▪ Exploration & Combat: Found in Stronghold Libraries, Ancient Cities, Woodland Mansions, Temples, Dungeons, Bastions, End Cities, and Trial Vaults, or dropped by Gemini Goat Mages, Evokers, Witches, Cosmic Denizens, and major Bosses.\n\n" +
                "Reading & Shelving:\n" +
                "Right-click any volume to open the dual-page Scholar's Journal reader with an interactive chapter directory, persistent ribbon bookmarks, read-entry tracking, and Chiseled Bookshelf storage support.",
                "astral_celestial_atlas", 2, new ItemStack(ModItems.CELESTIAL_JOURNAL_VOL1.get()),
                560f, 0.0f, a_magic + 0.13f, false
        
        ));
    }


    private static ItemStack resolveBiome11Icon(String itemPath, Item fallback) {
        var itemOpt = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("entropica", itemPath));
        if (itemOpt.isPresent() && itemOpt.get() != Items.AIR) {
            return new ItemStack(itemOpt.get());
        }
        return new ItemStack(fallback);
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
