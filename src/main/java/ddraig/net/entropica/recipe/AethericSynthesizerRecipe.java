package ddraig.net.entropica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AethericSynthesizerRecipe implements Recipe<RecipeInput> {

    private final List<String> pattern;
    private final Map<String, Ingredient> key;
    private final List<ItemStack> results; // Now supports multiple outputs!

    private final NonNullList<Optional<Ingredient>> gridIngredients;

    public AethericSynthesizerRecipe(List<String> pattern, Map<String, Ingredient> key, List<ItemStack> results) {
        this.pattern = pattern;
        this.key = key;
        this.results = results;

        this.gridIngredients = NonNullList.withSize(25, Optional.empty());
        for (int r = 0; r < 5; r++) {
            String row = r < pattern.size() ? pattern.get(r) : "";
            for (int c = 0; c < 5; c++) {
                char ch = c < row.length() ? row.charAt(c) : ' ';
                if (ch != ' ') {
                    Ingredient ing = key.get(String.valueOf(ch));
                    if (ing != null) {
                        this.gridIngredients.set(r * 5 + c, Optional.of(ing));
                    }
                }
            }
        }
    }

    public List<String> getPattern() { return pattern; }
    public Map<String, Ingredient> getKey() { return key; }
    public NonNullList<Optional<Ingredient>> getGridIngredients() { return gridIngredients; }
    public List<ItemStack> getResults() { return results; }

    @Override
    public boolean matches(RecipeInput input, Level level) { return false; }

    public boolean matchesGrid(SimpleContainer inventory) {
        for (int rotation = 0; rotation < 4; rotation++) {
            if (checkMatchWithRotation(inventory, rotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMatchWithRotation(SimpleContainer inventory, int rotation) {
        // 0. Build the 5x5 expected grid for THIS specific rotation
        List<Optional<Ingredient>> rotatedExpected = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) rotatedExpected.add(Optional.empty());

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                int originalIndex = r * 5 + c;
                int rotatedIndex = getRotatedIndex(r, c, rotation);
                rotatedExpected.set(rotatedIndex, gridIngredients.get(originalIndex));
            }
        }

        // 1. Find the bounding box of the rotated recipe pattern
        int recMinCol = 5, recMaxCol = -1, recMinRow = 5, recMaxRow = -1;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (rotatedExpected.get(r * 5 + c).isPresent()) {
                    if (c < recMinCol) recMinCol = c;
                    if (c > recMaxCol) recMaxCol = c;
                    if (r < recMinRow) recMinRow = r;
                    if (r > recMaxRow) recMaxRow = r;
                }
            }
        }

        // Failsafe: If the recipe is entirely blank, make sure the inventory is also blank
        if (recMaxCol == -1) {
            for (int i = 0; i < 25; i++) {
                if (!inventory.getItem(i).isEmpty()) return false;
            }
            return true;
        }

        int recWidth = recMaxCol - recMinCol + 1;
        int recHeight = recMaxRow - recMinRow + 1;

        // 2. Find the bounding box of the actual items sitting on the table
        int invMinCol = 5, invMaxCol = -1, invMinRow = 5, invMaxRow = -1;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                // Ensure we only check up to slot 24 (ignoring the 4 output slots)
                if (!inventory.getItem(r * 5 + c).isEmpty()) {
                    if (c < invMinCol) invMinCol = c;
                    if (c > invMaxCol) invMaxCol = c;
                    if (r < invMinRow) invMinRow = r;
                    if (r > invMaxRow) invMaxRow = r;
                }
            }
        }

        // If there's nothing on the table but the recipe requires something, it's a fail
        if (invMaxCol == -1) return false;

        int invWidth = invMaxCol - invMinCol + 1;
        int invHeight = invMaxRow - invMinRow + 1;

        // If the shapes don't perfectly match in dimensions, abort early
        if (recWidth != invWidth || recHeight != invHeight) return false;

        // 3. Compare the cropped bounding boxes directly!
        // This allows a 1x3 wire recipe to be placed ANYWHERE on the 5x5 grid!
        for (int r = 0; r < recHeight; r++) {
            for (int c = 0; c < recWidth; c++) {
                int recIndex = (recMinRow + r) * 5 + (recMinCol + c);
                int invIndex = (invMinRow + r) * 5 + (invMinCol + c);

                Optional<Ingredient> expectedIngredient = rotatedExpected.get(recIndex);
                ItemStack stackInSlot = inventory.getItem(invIndex);

                if (expectedIngredient.isPresent()) {
                    if (!expectedIngredient.get().test(stackInSlot)) return false;
                } else {
                    // If the recipe expects an empty space inside its bounds, ensure the slot is actually empty
                    if (!stackInSlot.isEmpty()) return false;
                }
            }
        }

        return true;
    }

    private int getRotatedIndex(int r, int c, int rotation) {
        return switch (rotation) {
            case 0 -> r * 5 + c;
            case 1 -> c * 5 + (4 - r);
            case 2 -> (4 - r) * 5 + (4 - c);
            case 3 -> (4 - c) * 5 + r;
            default -> r * 5 + c;
        };
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0).copy();
    }

    // Removed the @Override annotation! Mojang removed this from the base Recipe interface in 1.21.2+.
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
    }

    @Override public RecipeSerializer<AethericSynthesizerRecipe> getSerializer() { return Serializer.INSTANCE; }
    @Override public RecipeType<AethericSynthesizerRecipe> getType() { return Type.INSTANCE; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }

    // ==========================================
    // HARDCODED FALLBACK RECIPES (1-56)
    // ==========================================
    private static List<AethericSynthesizerRecipe> HARDCODED_RECIPES = null;

    public static List<AethericSynthesizerRecipe> getHardcodedRecipes() {
        if (HARDCODED_RECIPES == null) {
            HARDCODED_RECIPES = new ArrayList<>();

            // Define all component items for clean builder access
            Item smallAmp = ModItems.SMALL_VIS_FUME_AMPOULE.get();
            Item medAmp = ModItems.MEDIUM_VIS_FUME_AMPOULE.get();
            Item largeAmp = ModItems.LARGE_VIS_FUME_AMPOULE.get();
            Item smallBase = ModItems.SMALL_AMPOULE_BASE.get();
            Item medBase = ModItems.MEDIUM_AMPOULE_BASE.get();
            Item largeBase = ModItems.LARGE_AMPOULE_BASE.get();

            Item redstone = Items.REDSTONE;
            Item redstoneBlk = Items.REDSTONE_BLOCK;
            Item iron = Items.IRON_INGOT;
            Item ironNugget = Items.IRON_NUGGET;
            Item gold = Items.GOLD_INGOT;
            Item leather = Items.LEATHER;
            Item glowstone = Items.GLOWSTONE_DUST;
            Item stick = Items.STICK;
            Item planks = Items.OAK_PLANKS; // Can expand to tag checking later

            Item arcIngot = ModItems.ARCANITE_INGOT.get();
            Item viscIngot = ModItems.VISCANITE_INGOT.get();
            Item resIngot = ModItems.RESONITE_INGOT.get();

            Item cArc = ModItems.CHARGED_ARCANITE_INGOT.get();
            Item cVisc = ModItems.CHARGED_VISCANITE_INGOT.get();
            Item cRes = ModItems.CHARGED_RESONITE_INGOT.get();

            Item arcNugget = ModItems.ARCANITE_NUGGET.get();
            Item viscNugget = ModItems.VISCANITE_NUGGET.get();
            Item resNugget = ModItems.RESONITE_NUGGET.get();

            Item arcWire = ModItems.ARCANITE_WIRE.get();
            Item viscWire = ModItems.VISCANITE_WIRE.get();
            Item resWire = ModItems.RESONITE_WIRE.get();

            Item coatArcWire = ModItems.COATED_ARCANITE_WIRE.get();
            Item coatViscWire = ModItems.COATED_VISCANITE_WIRE.get();
            Item coatResWire = ModItems.COATED_RESONITE_WIRE.get();

            Item arcFil = ModItems.ARCANITE_FILAMENT.get();
            Item viscFil = ModItems.VISCANITE_FILAMENT.get();
            Item resFil = ModItems.RESONITE_FILAMENT.get();

            Item cArcFil = ModItems.CHARGED_ARCANITE_FILAMENT.get();
            Item cViscFil = ModItems.CHARGED_VISCANITE_FILAMENT.get();
            Item cResFil = ModItems.CHARGED_RESONITE_FILAMENT.get();

            Item coatArcFil = ModItems.COATED_ARCANITE_FILAMENT.get();
            Item coatViscFil = ModItems.COATED_VISCANITE_FILAMENT.get();
            Item coatResFil = ModItems.COATED_RESONITE_FILAMENT.get();

            Item arcSpool = ModItems.ARCANITE_SPOOL.get();
            Item viscSpool = ModItems.VISCANITE_SPOOL.get();
            Item resSpool = ModItems.RESONITE_SPOOL.get();

            Item arcFrame = ModItems.ARCANITE_FRAME.get();
            Item viscFrame = ModItems.VISCANITE_FRAME.get();
            Item arcPlate = ModItems.ARCANITE_PLATE.get();
            Item viscPlate = ModItems.VISCANITE_PLATE.get();
            Item resPlate = ModItems.RESONITE_PLATE.get();

            Item arcSGear = ModItems.ARCANITE_SMALL_GEAR.get();
            Item viscSGear = ModItems.VISCANITE_SMALL_GEAR.get();
            Item resSGear = ModItems.RESONITE_SMALL_GEAR.get();

            Item arcLGear = ModItems.ARCANITE_LARGE_GEAR.get();
            Item viscLGear = ModItems.VISCANITE_LARGE_GEAR.get();
            Item resLGear = ModItems.RESONITE_LARGE_GEAR.get();

            Item arcGearset = ModItems.ARCANITE_GEARSET.get();
            Item viscGearset = ModItems.VISCANITE_GEARSET.get();
            Item resGearset = ModItems.RESONITE_GEARSET.get();

            // 1-3. Charged Arcanite (Storm Fumes)
            for(int i=0; i<3; i++) {
                Item fA = i==0 ? smallAmp : i==1 ? medAmp : largeAmp;
                Item bA = i==0 ? smallBase : i==1 ? medBase : largeBase;
                int c = i==0 ? 3 : i==1 ? 4 : 6;
                build(HARDCODED_RECIPES, new String[]{"  F  ", " RIR ", " AAA ", " RIR ", "  F  "},
                        new Object[]{'F', getFume(fA, EssenceType.STORM), 'R', redstone, 'I', iron, 'A', arcIngot},
                        new Object[]{cArc, c, bA, 2});
            }

            // 4-12. Charged Viscanite (Magma, Blood, Pyre Fumes)
            EssenceType[] viscFumes = {EssenceType.MAGMA, EssenceType.BLOOD, EssenceType.PYRE};
            for(EssenceType t : viscFumes) {
                for(int i=0; i<3; i++) {
                    Item fA = i==0 ? smallAmp : i==1 ? medAmp : largeAmp;
                    Item bA = i==0 ? smallBase : i==1 ? medBase : largeBase;
                    int c = i==0 ? 3 : i==1 ? 4 : 6;
                    build(HARDCODED_RECIPES, new String[]{"  F  ", " RVR ", " VVV ", " RVR ", "  F  "},
                            new Object[]{'F', getFume(fA, t), 'R', redstoneBlk, 'V', viscIngot},
                            new Object[]{cVisc, c, bA, 2});
                }
            }

            // 13-24. Charged Resonite (Umbral, Void, Null-U, Abyss Fumes)
            EssenceType[] resFumes = {EssenceType.UMBRAL, EssenceType.VOID, EssenceType.NULL_U, EssenceType.ABYSS};
            for(EssenceType t : resFumes) {
                for(int i=0; i<3; i++) {
                    Item fA = i==0 ? smallAmp : i==1 ? medAmp : largeAmp;
                    Item bA = i==0 ? smallBase : i==1 ? medBase : largeBase;
                    int c = i==0 ? 3 : i==1 ? 4 : 6;
                    build(HARDCODED_RECIPES, new String[]{"  F  ", " RGR ", " TTT ", " RGR ", "  F  "},
                            new Object[]{'F', getFume(fA, t), 'R', redstone, 'G', gold, 'T', resIngot},
                            new Object[]{cRes, c, bA, 2});
                }
            }

            // 25-27. Basic Wires
            String[] wirePat = {"     ", " NNN ", "     ", "     ", "     "};
            build(HARDCODED_RECIPES, wirePat, new Object[]{'N', arcNugget}, new Object[]{arcWire, 8});
            build(HARDCODED_RECIPES, wirePat, new Object[]{'N', viscNugget}, new Object[]{viscWire, 8});
            build(HARDCODED_RECIPES, wirePat, new Object[]{'N', resNugget}, new Object[]{resWire, 8});

            // 28-30. Coated Wires
            String[] coatWirePat = {"     ", " LLL ", " WWW ", " LLL ", "     "};
            build(HARDCODED_RECIPES, coatWirePat, new Object[]{'L', leather, 'W', arcWire}, new Object[]{coatArcWire, 8});
            build(HARDCODED_RECIPES, coatWirePat, new Object[]{'L', leather, 'W', viscWire}, new Object[]{coatViscWire, 8});
            build(HARDCODED_RECIPES, coatWirePat, new Object[]{'L', leather, 'W', resWire}, new Object[]{coatResWire, 8});

            // 31-33. Filaments
            String[] filPat = {"  R  ", " N N ", "R   R", " N N ", "  R  "};
            build(HARDCODED_RECIPES, filPat, new Object[]{'N', arcNugget, 'R', redstone}, new Object[]{arcFil, 4});
            build(HARDCODED_RECIPES, filPat, new Object[]{'N', viscNugget, 'R', redstone}, new Object[]{viscFil, 4});
            build(HARDCODED_RECIPES, filPat, new Object[]{'N', resNugget, 'R', redstone}, new Object[]{resFil, 4});

            // 34-36. Charged Filaments
            String[] cFilPat = {"  G  ", "  F  ", "GFEFG", "  F  ", "  G  "};
            build(HARDCODED_RECIPES, cFilPat, new Object[]{'G', glowstone, 'F', arcFil, 'E', getFume(smallAmp, EssenceType.LIGHTNING)}, new Object[]{cArcFil, 4, smallBase, 1});
            build(HARDCODED_RECIPES, cFilPat, new Object[]{'G', glowstone, 'F', viscFil, 'E', getFume(smallAmp, EssenceType.STORM)}, new Object[]{cViscFil, 4, smallBase, 1});
            build(HARDCODED_RECIPES, cFilPat, new Object[]{'G', glowstone, 'F', resFil, 'E', getEssence(ModItems.WEAK_ESSENCE.get(), EssenceType.EARTH)}, new Object[]{cResFil, 4});

            // 37-39. Coated Filaments
            String[] coatFilPat = {" L L ", "L F L", " F F ", "L F L", " L L "};
            build(HARDCODED_RECIPES, coatFilPat, new Object[]{'L', leather, 'F', arcFil}, new Object[]{coatArcFil, 4});
            build(HARDCODED_RECIPES, coatFilPat, new Object[]{'L', leather, 'F', viscFil}, new Object[]{coatViscFil, 4});
            build(HARDCODED_RECIPES, coatFilPat, new Object[]{'L', leather, 'F', resFil}, new Object[]{coatResFil, 4});

            // 40-42. Spools
            String[] spoolPat = {" WSW ", "W   W", "S A S", "W   W", " WSW "};
            build(HARDCODED_RECIPES, spoolPat, new Object[]{'W', planks, 'S', stick, 'A', arcWire}, new Object[]{arcSpool, 1});
            build(HARDCODED_RECIPES, spoolPat, new Object[]{'W', planks, 'S', stick, 'A', viscWire}, new Object[]{viscSpool, 1});
            build(HARDCODED_RECIPES, spoolPat, new Object[]{'W', planks, 'S', stick, 'A', resWire}, new Object[]{resSpool, 1});

            // 43-44. Frames
            String[] framePat = {" AAA ", " A A ", " AAA ", "     ", "     "};
            build(HARDCODED_RECIPES, framePat, new Object[]{'A', arcIngot}, new Object[]{arcFrame, 1});
            build(HARDCODED_RECIPES, framePat, new Object[]{'A', viscIngot}, new Object[]{viscFrame, 1});

            // 45-47. Plates
            String[] platePat = {"     ", " AAA ", " AAA ", "     ", "     "};
            build(HARDCODED_RECIPES, platePat, new Object[]{'A', arcIngot}, new Object[]{arcPlate, 4}); // Updated to yield 4 plates!
            build(HARDCODED_RECIPES, platePat, new Object[]{'A', viscIngot}, new Object[]{viscPlate, 4});
            build(HARDCODED_RECIPES, platePat, new Object[]{'A', resIngot}, new Object[]{resPlate, 4});

            // 48-50. Small Gears
            String[] sGearPat = {"  N  ", " NIN ", "  N  ", "     ", "     "};
            build(HARDCODED_RECIPES, sGearPat, new Object[]{'N', arcNugget, 'I', ironNugget}, new Object[]{arcSGear, 4});
            build(HARDCODED_RECIPES, sGearPat, new Object[]{'N', viscNugget, 'I', ironNugget}, new Object[]{viscSGear, 4});
            build(HARDCODED_RECIPES, sGearPat, new Object[]{'N', resNugget, 'I', ironNugget}, new Object[]{resSGear, 4});

            // 51-53. Large Gears
            String[] lGearPat = {" III ", "I N I", "IN NI", "I N I", " III "};
            build(HARDCODED_RECIPES, lGearPat, new Object[]{'I', arcNugget, 'N', ironNugget}, new Object[]{arcLGear, 1});
            build(HARDCODED_RECIPES, lGearPat, new Object[]{'I', viscNugget, 'N', ironNugget}, new Object[]{viscLGear, 1});
            build(HARDCODED_RECIPES, lGearPat, new Object[]{'I', resNugget, 'N', ironNugget}, new Object[]{resLGear, 1});

            // 54-56. Gearsets
            String[] setPat = {"  S  ", " SLS ", "  S  ", "     ", "     "};
            build(HARDCODED_RECIPES, setPat, new Object[]{'S', arcSGear, 'L', arcLGear}, new Object[]{arcGearset, 1});
            build(HARDCODED_RECIPES, setPat, new Object[]{'S', viscSGear, 'L', viscLGear}, new Object[]{viscGearset, 1});
            build(HARDCODED_RECIPES, setPat, new Object[]{'S', resSGear, 'L', resLGear}, new Object[]{resGearset, 1});
        }
        return HARDCODED_RECIPES;
    }

    // ==========================================
    // BUILDER HELPERS
    // ==========================================
    private static void build(List<AethericSynthesizerRecipe> list, String[] pattern, Object[] keys, Object[] results) {
        Map<String, Ingredient> map = new HashMap<>();
        for (int i = 0; i < keys.length; i += 2) {
            String k = String.valueOf(keys[i]);
            Object v = keys[i+1];
            Ingredient ing = null;
            if (v instanceof Item item) ing = Ingredient.of(item);
            else if (v instanceof ItemStack stack) ing = DataComponentIngredient.of(false, stack);
            else if (v instanceof Ingredient iG) ing = iG;
            map.put(k, ing);
        }
        List<ItemStack> outList = new ArrayList<>();
        for (int i = 0; i < results.length; i += 2) {
            Item item = (Item) results[i];
            int count = (Integer) results[i+1];
            outList.add(new ItemStack(item, count));
        }
        list.add(new AethericSynthesizerRecipe(List.of(pattern), map, outList));
    }

    private static ItemStack getFume(Item ampouleItem, EssenceType type) {
        ItemStack stack = new ItemStack(ampouleItem);
        VisFumeAmpouleItem.setEssenceType(stack, type);
        return stack;
    }

    private static ItemStack getEssence(Item orbItem, EssenceType type) {
        ItemStack stack = new ItemStack(orbItem);
        EssenceItem.setEssenceType(stack, type);
        return stack;
    }

    public static class Type implements RecipeType<AethericSynthesizerRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "aetheric_synthesis";
    }

    public static class Serializer implements RecipeSerializer<AethericSynthesizerRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<AethericSynthesizerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.listOf().fieldOf("pattern").forGetter(AethericSynthesizerRecipe::getPattern),
                Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(AethericSynthesizerRecipe::getKey),
                ItemStack.CODEC.listOf().fieldOf("results").forGetter(AethericSynthesizerRecipe::getResults)
        ).apply(inst, AethericSynthesizerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AethericSynthesizerRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork,
                Serializer::fromNetwork
        );

        @Override public MapCodec<AethericSynthesizerRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, AethericSynthesizerRecipe> streamCodec() { return STREAM_CODEC; }

        private static AethericSynthesizerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int patSize = buffer.readVarInt();
            List<String> pattern = new ArrayList<>();
            for (int i = 0; i < patSize; i++) pattern.add(buffer.readUtf());

            int keySize = buffer.readVarInt();
            Map<String, Ingredient> key = new HashMap<>();
            for (int i = 0; i < keySize; i++) {
                String k = buffer.readUtf();
                Ingredient ing = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                key.put(k, ing);
            }

            int resSize = buffer.readVarInt();
            List<ItemStack> results = new ArrayList<>();
            for (int i = 0; i < resSize; i++) results.add(ItemStack.STREAM_CODEC.decode(buffer));

            return new AethericSynthesizerRecipe(pattern, key, results);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, AethericSynthesizerRecipe recipe) {
            buffer.writeVarInt(recipe.pattern.size());
            for (String s : recipe.pattern) buffer.writeUtf(s);

            buffer.writeVarInt(recipe.key.size());
            for (Map.Entry<String, Ingredient> entry : recipe.key.entrySet()) {
                buffer.writeUtf(entry.getKey());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, entry.getValue());
            }

            buffer.writeVarInt(recipe.results.size());
            for (ItemStack stack : recipe.results) ItemStack.STREAM_CODEC.encode(buffer, stack);
        }
    }
}