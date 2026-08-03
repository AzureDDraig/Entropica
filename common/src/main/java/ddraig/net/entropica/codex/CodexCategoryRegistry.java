package ddraig.net.entropica.codex;

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
                "Welcome to Entropica. The world is saturated with latent elemental Essences and pressure-driven Materia.\n\n" +
                "To begin your journey, inspect resources using the [[Arkanist Monocle]] or construct a basic [[Crucible]] to extract raw essences from native flora and mineral ores.\n\n" +
                "Read [[MATERIALS]] to forge your first Arcanite alloys.",
                null, 0, new ItemStack(Items.COMPASS),
                180f, 0.0f, a_start, true
        ));

        ALL_NODES.add(new CodexNode(
                "getting_started_primer", "Technomantic Primer", "GETTING STARTED",
                "Basic principes of Entropica.",
                "Technomantics merges physical thermodynamics with ancient runic symbolism.\n\n" +
                "Extract raw essences from regional ores and flora using a Crucible or Eidolic Lathe.",
                "hub_getting_started", 0, new ItemStack(Items.BOOK),
                260f, 0.0f, a_start, false
        ));

        // ==========================================
        // 2. MATERIALS (Orange Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_materials", "MATERIALS", "MATERIALS",
                "Ores, crystals, and fauna materials of Entropica.",
                "Explore the mineral wealth and biological materials of Entropica, including Arcanite, Viscanite, and Grot Hides.",
                null, 0, new ItemStack(Items.IRON_PICKAXE),
                180f, 0.0f, a_ing, true
        ));

        ALL_NODES.add(new CodexNode(
                "materials_ores", "Ores", "MATERIALS",
                "Arcanite, Resonite, and Viscanite ore metallurgy.",
                "Arcanite is the foundational metal alloy of Entropica, forged by combining Iron with refined Viscanite dust.\n\n" +
                "It features high durability, excellent Materia conductivity, and forms the structural framework for Vapor-Pneumatic Pipes.",
                "hub_materials", 0, new ItemStack(Items.RAW_IRON),
                280f, 0.0f, a_ing - 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_crystals", "Crystals", "MATERIALS",
                "Eidolite and Prismatic Fragment crystal structures.",
                "Viscanite crystals vibrate at high frequency, conducting liquid Materia without thermal decay.\n\n" +
                "Resonite acts as a kinetic stabilizer, utilized in high-torque Resonite Stators.",
                "hub_materials", 0, new ItemStack(Items.EMERALD),
                290f, 0.0f, a_ing, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_fauna", "Fauna Materials", "MATERIALS",
                "Grot hides, Veil pelts, and Ovis wool.",
                "Biological materials harvested from indigenous mobs provide insulation for high-pressure conduits.",
                "hub_materials", 0, new ItemStack(Items.LEATHER),
                280f, 0.0f, a_ing + 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materials_glassworking", "Aesthetic Glassworking & Tinting", "MATERIALS",
                "32x32 pre-existing texture tinting, 10 shape variants, dynamic RGB color shifting, and cauldron cleansing.",
                "Glassworking in Entropica directly tints native 32x32 pixel art textures using dynamic BlockColors and ItemColors handlers.\n\n" +
                "A strict 75%+ Alpha filter ensures tinting applies only to border frames, bevels, grid lines, and glare highlights (Alpha >= 191), leaving interior glass center spaces 100% transparent and clear.\n\n" +
                "Every glass family is fabricated in 10 distinct shape variants: Full Blocks, Slabs, Vertical Slabs, 1/4 Slabs (Quarter Slabs), Stairs & Corner Stairs, Walls & Corner Walls, Connected Panes, Horizontal Panes, Trapdoors, and Doors.\n\n" +
                "All variants drop cleanly when mined with any pickaxe. Right-clicking dyed or Essence glass items onto Water Cauldrons or Diluted Materia pools washes them back to Clear Base Glass.\n\n" +
                "Vertical Slabs feature 5 placement directions and are fully compatible with KleeSlabs (`#c:vertical_slabs`, `#kleeslabs:vertical_slabs`).",
                "hub_materials", 0, new ItemStack(Items.GLASS),
                300f, 0.0f, a_ing - 0.35f, false
        ));

        // ==========================================
        // 3. MATERIA (Cyan Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_materia", "MATERIA", "MATERIA",
                "Essence Tiers T1-T6, logistics, and pressure conduits.",
                "Materia exists as liquid (Liquida) and gas (Fumus) in pressure conduits.",
                null, 0, new ItemStack(Items.BEACON),
                180f, 0.0f, a_mat, true
        ));

        ALL_NODES.add(new CodexNode(
                "materia_essences", "Essence Tiers & Ampoules", "MATERIA",
                "Categorization of T1-T6 Essences.",
                "Essences exist across 6 primary tiers:\n- T1: Vitae & Blood\n- T2: Ignis, Aqua, Aer, Terra\n- T3: Umbral & Void",
                "hub_materia", 1, new ItemStack(Items.BREWING_STAND),
                280f, 0.0f, a_mat - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "materia_logistics", "Materia Logistics & Pressure", "MATERIA",
                "Pneumatic pipe dynamics and diverters.",
                "Transport Materia Liquida through conduits safely without exceeding maximum pressure thresholds.",
                "hub_materia", 1, new ItemStack(Items.COPPER_INGOT),
                280f, 0.0f, a_mat + 0.15f, false
        ));

        // ==========================================
        // 4. MACHINERY (Red Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_machinery", "MACHINERY", "MACHINERY",
                "Lathe, Crucible, Scribing Controllers, and Vehicles.",
                "Construct precision machinery for forging and powering technomantic devices.",
                null, 0, new ItemStack(Items.ANVIL),
                180f, 0.0f, a_mach, true
        ));

        ALL_NODES.add(new CodexNode(
                "workstations_lathe", "Eidolic Lathe & Crucible", "MACHINERY",
                "Precision machinery for turning raw metals.",
                "The Eidolic Lathe shapes Arcanite ingots into precision components.",
                "hub_machinery", 1, new ItemStack(Items.ANVIL),
                280f, 0.0f, a_mach, false
        ));

        // ==========================================
        // 5. MULTIBLOCKS (Yellow Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_multiblocks", "MULTIBLOCKS", "MULTIBLOCKS",
                "Runic Scribing Engine 3x3x3, Focal Pedestal, and Fission Reactor.",
                "Construct powerful multiblock structures for advanced synthesis.",
                null, 0, new ItemStack(Items.CRAFTING_TABLE),
                180f, 0.0f, a_multi, true
        ));

        ALL_NODES.add(new CodexNode(
                "multiblock_scribing_engine", "Runic Scribing Engine (3x3x3)", "MULTIBLOCKS",
                "3x3x3 multiblock for stamping arcane runes.",
                "The Runic Scribing Engine is built with a Viscanite Synthesizer base.",
                "hub_multiblocks", 2, new ItemStack(Items.CRAFTING_TABLE),
                280f, 0.0f, a_multi - 0.15f, false
        ));

        ALL_NODES.add(new CodexNode(
                "multiblock_fission_reactor", "Materia Fission Reactor", "MULTIBLOCKS",
                "High-tier reactor splitting compound Materia.",
                "Splits compound Materia back into parent elements.",
                "hub_multiblocks", 3, new ItemStack(Items.NETHER_STAR),
                280f, 0.0f, a_multi + 0.15f, false
        ));

        // ==========================================
        // 6. ENVIRONMENT & NATURE (Lime Green Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_environment", "ENVIRONMENT & NATURE", "ENVIRONMENT & NATURE",
                "Biomes, Flora, and Fauna of Entropica.",
                "Discover native biomes, rare flora, and elemental mob mutations.",
                null, 0, new ItemStack(Items.OAK_SAPLING),
                180f, 0.0f, a_env, true
        ));

        ALL_NODES.add(new CodexNode(
                "env_biomes", "Biomes", "ENVIRONMENT & NATURE",
                "Tundra, Spore Forests, and Void Rifts.",
                "Explore unique biomes saturated with ambient elemental energy.",
                "hub_environment", 0, new ItemStack(Items.GRASS_BLOCK),
                280f, 0.0f, a_env - 0.20f, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_flora", "Flora", "ENVIRONMENT & NATURE",
                "Rubber Trees and Viscanite Blossoms.",
                "Harvest rubber latex and viscanite blossoms for conduit insulation.",
                "hub_environment", 0, new ItemStack(Items.OAK_LEAVES),
                290f, 0.0f, a_env, false
        ));

        ALL_NODES.add(new CodexNode(
                "env_fauna", "Fauna", "ENVIRONMENT & NATURE",
                "Grots, Veil Foxes, and Rime Shepherds.",
                "Observe indigenous fauna for drops and research unlocks.",
                "hub_environment", 0, new ItemStack(Items.BONE),
                280f, 0.0f, a_env + 0.20f, false
        ));

        // ==========================================
        // 7. MAGIC (Purple Ring)
        // ==========================================
        ALL_NODES.add(new CodexNode(
                "hub_magic", "MAGIC", "MAGIC",
                "Magic Circles, Magic Circuits, Spellcrafting, and Astral Signs.",
                "Master runic symbolism, chalk footprints, and spellcrafting.",
                null, 0, new ItemStack(Items.ENCHANTED_BOOK),
                180f, 0.0f, a_magic, true
        ));

        ALL_NODES.add(new CodexNode(
                "magic_rituals", "Runic Rituals", "MAGIC",
                "Chalk tiers and Elder Futhark runes.",
                "Magic Circles are drawn using 4 Chalk Tiers with Elder Futhark active runes.",
                "hub_magic", 1, new ItemStack(Items.REDSTONE),
                280f, 0.0f, a_magic - 0.25f, false
        ));

        ALL_NODES.add(new CodexNode(
                "magic_spellcrafting", "Spellcrafting", "MAGIC",
                "Wands, foci, and active spellcasting.",
                "Channel elemental Materia into active spells using Shimmering Foci.",
                "hub_magic", 1, new ItemStack(Items.BLAZE_ROD),
                290f, 0.0f, a_magic - 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "magic_astral", "Astral Signs", "MAGIC",
                "Constellation alignments and celestial power.",
                "Align your scribing engine with active celestial constellations.",
                "hub_magic", 1, new ItemStack(Items.AMETHYST_SHARD),
                290f, 0.0f, a_magic + 0.08f, false
        ));

        ALL_NODES.add(new CodexNode(
                "magic_alchemy", "Alchemy", "MAGIC",
                "Transmutation, catalysts, and brewing.",
                "Transmute raw elements into rare alloys using catalyst receptacles.",
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
