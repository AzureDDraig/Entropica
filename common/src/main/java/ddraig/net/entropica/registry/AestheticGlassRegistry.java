package ddraig.net.entropica.registry;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.QuarterSlabBlock;
import ddraig.net.entropica.block.HorizontalPaneBlock;
import ddraig.net.entropica.block.VerticalQuarterSlabBlock;
import ddraig.net.entropica.block.VerticalSlabBlock;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

import java.util.*;

public class AestheticGlassRegistry {

    public static class GlassColorDefinition {
        private final String id;
        private final boolean isStatic;
        private final int staticColorRGB;
        private final EssenceType essenceType;

        public GlassColorDefinition(String id, int staticColorRGB) {
            this.id = id;
            this.isStatic = true;
            this.staticColorRGB = staticColorRGB;
            this.essenceType = null;
        }

        public GlassColorDefinition(String id, EssenceType essenceType) {
            this.id = id;
            this.isStatic = false;
            this.staticColorRGB = 0xFFFFFFFF;
            this.essenceType = essenceType;
        }

        public String getId() { return id; }
        public boolean isStatic() { return isStatic; }
        public int getStaticColorRGB() { return staticColorRGB; }
        public EssenceType getEssenceType() { return essenceType; }

        public int getColorRGB() {
            if (isStatic) {
                return staticColorRGB;
            }
            if (essenceType != null) {
                long millis = net.minecraft.Util.getMillis();
                if (essenceType.isDynamic()) {
                    return hsbToRgb((float) ((millis % 4000L) / 4000.0), 0.75f, 1.0f);
                }
                int[] rgb = essenceType.getCurrentRGB(millis / 50.0);
                return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            }
            return 0xFFFFFFFF;
        }
    }

    public static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> { r = (int) (brightness * 255.0f + 0.5f); g = (int) (t * 255.0f + 0.5f); b = (int) (p * 255.0f + 0.5f); }
                case 1 -> { r = (int) (q * 255.0f + 0.5f); g = (int) (brightness * 255.0f + 0.5f); b = (int) (p * 255.0f + 0.5f); }
                case 2 -> { r = (int) (p * 255.0f + 0.5f); g = (int) (brightness * 255.0f + 0.5f); b = (int) (t * 255.0f + 0.5f); }
                case 3 -> { r = (int) (p * 255.0f + 0.5f); g = (int) (q * 255.0f + 0.5f); b = (int) (brightness * 255.0f + 0.5f); }
                case 4 -> { r = (int) (t * 255.0f + 0.5f); g = (int) (p * 255.0f + 0.5f); b = (int) (brightness * 255.0f + 0.5f); }
                case 5 -> { r = (int) (brightness * 255.0f + 0.5f); g = (int) (p * 255.0f + 0.5f); b = (int) (q * 255.0f + 0.5f); }
            }
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int getEssenceLightLevel(EssenceType type) {
        if (type == null) return 0;
        return switch (type) {
            case RADIANT, APOTHEOSIS, CELESTIAL, EMPYREAN, PHOTON -> 15;
            case IGNIS, VOLT, LIGHTNING, PYRE, AURORA, DAWN, STATIC, KINETIC, FERVOR, HEAT -> 10;
            case FROZEN, GLACIAL, ASTRAL, AETHER, SOULFIRE, PRISMATIC, ENTROPICA, CHIMERA, GENESIS, SINGULARITY, RIME, AURA -> 7;
            default -> 0;
        };
    }

    public static final List<RegistrySupplier<Block>> ALL_GLASS_BLOCKS = new ArrayList<>();
    public static final List<RegistrySupplier<Item>> ALL_GLASS_ITEMS = new ArrayList<>();
    public static final Map<Block, GlassColorDefinition> BLOCK_COLOR_MAP = new IdentityHashMap<>();
    public static final Map<Item, GlassColorDefinition> ITEM_COLOR_MAP = new IdentityHashMap<>();
    public static final Map<Item, Integer> ITEM_TO_INDEX_MAP = new IdentityHashMap<>();
    public static final Map<String, Integer> FAMILY_BASE_NAME_TO_INDEX = new HashMap<>();

    public static RegistrySupplier<Block> ESSENCE_ENRICHED_GLASS;
    public static RegistrySupplier<Block> MATERIA_FUMUS_STRENGTHENED_GLASS;
    public static RegistrySupplier<Block> MATERIA_LIQUIDA_ENRICHED_GLASS;
    public static RegistrySupplier<Block> FRAGMENT_LATTICE_GLASS;

    public static final int SHAPES_PER_FAMILY = 9;

    public static void init() {
        if (!ALL_GLASS_BLOCKS.isEmpty()) return;

        List<GlassFamilySpec> specs = createFamilySpecs();
        for (GlassFamilySpec spec : specs) {
            registerFamily(spec);
        }
    }

    public record GlassFamilySpec(String baseName, GlassColorDefinition colorDef, int lightEmission, float hardness) {}

    private static List<GlassFamilySpec> createFamilySpecs() {
        List<GlassFamilySpec> specs = new ArrayList<>();

        specs.add(new GlassFamilySpec("essence_enriched_glass", new GlassColorDefinition("essence_enriched_glass", 0xFFFFFFFF), 7, 1.5f));
        specs.add(new GlassFamilySpec("materia_fumus_strengthened_glass", new GlassColorDefinition("materia_fumus_strengthened_glass", 0xFFFFFFFF), 0, 2.0f));
        specs.add(new GlassFamilySpec("materia_liquida_enriched_glass", new GlassColorDefinition("materia_liquida_enriched_glass", 0xFFFFFFFF), 0, 2.5f));
        specs.add(new GlassFamilySpec("fragment_lattice_glass", new GlassColorDefinition("fragment_lattice_glass", 0xFFFFFFFF), 0, 3.5f));

        specs.add(new GlassFamilySpec("clear", new GlassColorDefinition("clear", 0xFFFFFFFF), 0, 0.3f));

        Map<String, Integer> vanillaDyes = Map.ofEntries(
                Map.entry("white", 0xFFF9FFFE), Map.entry("orange", 0xFFF9801D),
                Map.entry("magenta", 0xFFC969E0), Map.entry("light_blue", 0xFF47B4E5),
                Map.entry("yellow", 0xFFFED83D), Map.entry("lime", 0xFF80C71F),
                Map.entry("pink", 0xFFF38BAA), Map.entry("gray", 0xFF474F52),
                Map.entry("light_gray", 0xFF9D9D97), Map.entry("cyan", 0xFF169C9C),
                Map.entry("purple", 0xFF8932B8), Map.entry("blue", 0xFF3C44AA),
                Map.entry("brown", 0xFF835432), Map.entry("green", 0xFF5E7C16),
                Map.entry("red", 0xFFB02E26), Map.entry("black", 0xFF1D1D21)
        );
        for (var entry : vanillaDyes.entrySet()) {
            specs.add(new GlassFamilySpec(entry.getKey(), new GlassColorDefinition(entry.getKey(), entry.getValue()), 0, 0.3f));
        }

        Map<String, Integer> dyenamicsColors = Map.ofEntries(
                Map.entry("maroon", 0xFF660000), Map.entry("rose", 0xFFFF6699),
                Map.entry("coral", 0xFFFF7F50), Map.entry("ginger", 0xFFB05E26),
                Map.entry("tan", 0xFFD2B48C), Map.entry("beige", 0xFFF5F5DC),
                Map.entry("amber", 0xFFFFBF00), Map.entry("olive", 0xFF808000),
                Map.entry("mint", 0xFF98FF98), Map.entry("teal", 0xFF008080),
                Map.entry("aquamarine", 0xFF7FFFD4), Map.entry("navy", 0xFF000080),
                Map.entry("lavender", 0xFFE6E6FA), Map.entry("plum", 0xFFDDA0DD),
                Map.entry("magenta_pink", 0xFFFF0099), Map.entry("peach", 0xFFFFDAB9),
                Map.entry("fluorescent", 0xFFCCFF00), Map.entry("ruby", 0xFFE0115F),
                Map.entry("garnet", 0xFF73020C), Map.entry("fuchsia", 0xFFFF00FF),
                Map.entry("persimmon", 0xFFEC5800), Map.entry("cherenkov", 0xFF00D2FF),
                Map.entry("honey", 0xFFEB9605), Map.entry("ice", 0xFFA5F2F3),
                Map.entry("spring_green", 0xFF00FF7F), Map.entry("bubblegum", 0xFFFFC0CB),
                Map.entry("shadow", 0xFF2B2B2B), Map.entry("wine", 0xFF722F37)
        );
        for (var entry : dyenamicsColors.entrySet()) {
            specs.add(new GlassFamilySpec(entry.getKey(), new GlassColorDefinition(entry.getKey(), entry.getValue()), 0, 0.3f));
        }

        Set<String> existingNames = new HashSet<>(dyenamicsColors.keySet());
        existingNames.addAll(vanillaDyes.keySet());
        existingNames.add("clear");

        for (EssenceType type : EssenceType.values()) {
            String name = type.name().toLowerCase();
            if (existingNames.contains(name)) {
                name = "essence_" + name;
            }
            specs.add(new GlassFamilySpec(name, new GlassColorDefinition(name, type), getEssenceLightLevel(type), 0.3f));
        }

        return specs;
    }

    public static final BlockSetType GLASS_BLOCK_SET_TYPE = new BlockSetType("glass");

    private static void registerFamily(GlassFamilySpec spec) {
        String base = spec.baseName();
        boolean endsWithGlass = base.endsWith("_glass");

        String blockName = endsWithGlass ? base : base + "_glass";
        String paneName = endsWithGlass ? base + "_pane" : base + "_glass_pane";
        String doorName = endsWithGlass ? base + "_door" : base + "_glass_door";
        String trapdoorName = endsWithGlass ? base + "_trapdoor" : base + "_glass_trapdoor";
        String slabName = endsWithGlass ? base + "_slab" : base + "_glass_slab";
        String vSlabName = endsWithGlass ? base + "_vertical_slab" : base + "_glass_vertical_slab";
        String qSlabName = endsWithGlass ? base + "_quarter_slab" : base + "_glass_quarter_slab";
        String stairsName = endsWithGlass ? base + "_stairs" : base + "_glass_stairs";
        String hPaneName = endsWithGlass ? base + "_horizontal_pane" : base + "_glass_horizontal_pane";

        // 1. Block
        RegistrySupplier<Block> blockSup = ModBlocks.BLOCKS.register(blockName, loc ->
                new ddraig.net.entropica.block.AestheticGlassBlock(createProperties(blockName, spec.lightEmission(), spec.hardness())));
        // 2. Pane
        RegistrySupplier<Block> paneSup = ModBlocks.BLOCKS.register(paneName, loc ->
                new ddraig.net.entropica.block.AestheticGlassPaneBlock(createProperties(paneName, spec.lightEmission(), spec.hardness())));
        // 3. Door
        RegistrySupplier<Block> doorSup = ModBlocks.BLOCKS.register(doorName, loc ->
                new ddraig.net.entropica.block.AestheticGlassDoorBlock(GLASS_BLOCK_SET_TYPE, createProperties(doorName, spec.lightEmission(), spec.hardness())));
        // 4. Trapdoor
        RegistrySupplier<Block> trapdoorSup = ModBlocks.BLOCKS.register(trapdoorName, loc ->
                new ddraig.net.entropica.block.AestheticGlassTrapdoorBlock(GLASS_BLOCK_SET_TYPE, createProperties(trapdoorName, spec.lightEmission(), spec.hardness())));
        // 5. Slab
        RegistrySupplier<Block> slabSup = ModBlocks.BLOCKS.register(slabName, loc ->
                new ddraig.net.entropica.block.AestheticGlassSlabBlock(createProperties(slabName, spec.lightEmission(), spec.hardness())));
        // 6. Vertical Slab
        RegistrySupplier<Block> vSlabSup = ModBlocks.BLOCKS.register(vSlabName, loc ->
                new VerticalSlabBlock(createProperties(vSlabName, spec.lightEmission(), spec.hardness())));
        // 7. Quarter Slab
        RegistrySupplier<Block> qSlabSup = ModBlocks.BLOCKS.register(qSlabName, loc ->
                new QuarterSlabBlock(createProperties(qSlabName, spec.lightEmission(), spec.hardness())));
        // 8. Stairs
        RegistrySupplier<Block> stairsSup = ModBlocks.BLOCKS.register(stairsName, loc ->
                new ddraig.net.entropica.block.AestheticGlassStairBlock(blockSup.get().defaultBlockState(), createProperties(stairsName, spec.lightEmission(), spec.hardness())));
        // 9. Horizontal Pane
        RegistrySupplier<Block> hPaneSup = ModBlocks.BLOCKS.register(hPaneName, loc ->
                new HorizontalPaneBlock(createProperties(hPaneName, spec.lightEmission(), spec.hardness())));
        List<RegistrySupplier<Block>> familyBlocks = List.of(blockSup, paneSup, doorSup, trapdoorSup, slabSup, vSlabSup, qSlabSup, stairsSup, hPaneSup);
        List<String> familyNames = List.of(blockName, paneName, doorName, trapdoorName, slabName, vSlabName, qSlabName, stairsName, hPaneName);

        for (int i = 0; i < familyBlocks.size(); i++) {
            RegistrySupplier<Block> bSup = familyBlocks.get(i);
            String name = familyNames.get(i);
            ALL_GLASS_BLOCKS.add(bSup);

            RegistrySupplier<Item> itemSup;
            if (name.equals("essence_enriched_glass")) {
                itemSup = (RegistrySupplier<Item>) (Object) ModItems.ESSENCE_ENRICHED_GLASS_ITEM;
            } else if (name.equals("materia_fumus_strengthened_glass")) {
                itemSup = (RegistrySupplier<Item>) (Object) ModItems.MATERIA_FUMUS_STRENGTHENED_GLASS_ITEM;
            } else if (name.equals("materia_liquida_enriched_glass")) {
                itemSup = (RegistrySupplier<Item>) (Object) ModItems.MATERIA_LIQUIDA_ENRICHED_GLASS_ITEM;
            } else if (name.equals("fragment_lattice_glass")) {
                itemSup = (RegistrySupplier<Item>) (Object) ModItems.FRAGMENT_LATTICE_GLASS_ITEM;
            } else {
                itemSup = ModItems.ITEMS.registerItem(name, props -> new BlockItem(bSup.get(), props));
            }
            ALL_GLASS_ITEMS.add(itemSup);
        }

        if (blockName.equals("essence_enriched_glass")) ESSENCE_ENRICHED_GLASS = blockSup;
        else if (blockName.equals("materia_fumus_strengthened_glass")) MATERIA_FUMUS_STRENGTHENED_GLASS = blockSup;
        else if (blockName.equals("materia_liquida_enriched_glass")) MATERIA_LIQUIDA_ENRICHED_GLASS = blockSup;
        else if (blockName.equals("fragment_lattice_glass")) FRAGMENT_LATTICE_GLASS = blockSup;
    }

    private static BlockBehaviour.Properties createProperties(String id, int lightEmission, float hardness) {
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", id)))
                .mapColor(MapColor.NONE)
                .destroyTime(hardness)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> lightEmission)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isRedstoneConductor((state, getter, pos) -> false)
                .isSuffocating((state, getter, pos) -> false)
                .isViewBlocking((state, getter, pos) -> false);
    }

    public static void populateColorMaps() {
        if (!BLOCK_COLOR_MAP.isEmpty()) return;
        List<GlassFamilySpec> specs = createFamilySpecs();
        int specIndex = 0;
        for (int i = 0; i < ALL_GLASS_BLOCKS.size() && specIndex < specs.size(); i += SHAPES_PER_FAMILY) {
            GlassFamilySpec spec = specs.get(specIndex);
            FAMILY_BASE_NAME_TO_INDEX.put(spec.baseName().toLowerCase(Locale.ROOT), specIndex);
            specIndex++;
            for (int j = 0; j < SHAPES_PER_FAMILY && (i + j) < ALL_GLASS_BLOCKS.size(); j++) {
                Block block = ALL_GLASS_BLOCKS.get(i + j).get();
                Item item = ALL_GLASS_ITEMS.get(i + j).get();
                BLOCK_COLOR_MAP.put(block, spec.colorDef());
                ITEM_COLOR_MAP.put(item, spec.colorDef());
                ITEM_TO_INDEX_MAP.put(item, i + j);
            }
        }
    }

    public static boolean isGlassBlock(Block block) {
        populateColorMaps();
        return BLOCK_COLOR_MAP.containsKey(block);
    }

    public static boolean isMatchingGlassBlock(Block block, Block adjacentBlock) {
        return isGlassBlock(block) && isGlassBlock(adjacentBlock);
    }

    public static int getGlassColor(Block block) {
        populateColorMaps();
        GlassColorDefinition def = BLOCK_COLOR_MAP.get(block);
        return def != null ? def.getColorRGB() : 0xFFFFFFFF;
    }

    public static int getGlassColor(Item item) {
        populateColorMaps();
        GlassColorDefinition def = ITEM_COLOR_MAP.get(item);
        return def != null ? def.getColorRGB() : 0xFFFFFFFF;
    }

    public static int getShapeIndex(Item item) {
        populateColorMaps();
        Integer globalIndex = ITEM_TO_INDEX_MAP.get(item);
        return globalIndex != null ? globalIndex % SHAPES_PER_FAMILY : -1;
    }

    public static int getFamilyIndex(Item item) {
        populateColorMaps();
        Integer globalIndex = ITEM_TO_INDEX_MAP.get(item);
        return globalIndex != null ? globalIndex / SHAPES_PER_FAMILY : -1;
    }

    public static Item getClearGlassItem(int shapeIndex) {
        populateColorMaps();
        Integer familyIndex = FAMILY_BASE_NAME_TO_INDEX.get("clear");
        if (familyIndex != null && shapeIndex >= 0 && shapeIndex < SHAPES_PER_FAMILY) {
            return ALL_GLASS_ITEMS.get(familyIndex * SHAPES_PER_FAMILY + shapeIndex).get();
        }
        return null;
    }

    public static Item getGlassItem(String familyBaseName, int shapeIndex) {
        if (familyBaseName == null || shapeIndex < 0 || shapeIndex >= SHAPES_PER_FAMILY) return null;
        populateColorMaps();
        Integer familyIndex = FAMILY_BASE_NAME_TO_INDEX.get(familyBaseName.toLowerCase(Locale.ROOT));
        if (familyIndex != null) {
            return ALL_GLASS_ITEMS.get(familyIndex * SHAPES_PER_FAMILY + shapeIndex).get();
        }
        return null;
    }

    public static boolean hasFamily(String familyBaseName) {
        if (familyBaseName == null) return false;
        populateColorMaps();
        return FAMILY_BASE_NAME_TO_INDEX.containsKey(familyBaseName.toLowerCase(Locale.ROOT));
    }
}
