package ddraig.net.entropica.recipe;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HardcodedRecipes {

    private static List<EidolicLatheRecipe> LATHE_RECIPES = null;
    private static List<AethericSynthesizerRecipe> SYNTHESIZER_RECIPES = null;

    // ==========================================
    // LATHE FALLBACK RECIPES
    // ==========================================
    public static List<EidolicLatheRecipe> getLatheRecipes() {
        if (LATHE_RECIPES == null) {
            LATHE_RECIPES = new ArrayList<>();

            // Build the exact test recipe from the JSON
            List<Ingredient> testModifiers = new ArrayList<>();
            for (int i = 0; i < 4; i++) testModifiers.add(Ingredient.of(Items.DIAMOND));
            for (int i = 0; i < 2; i++) testModifiers.add(Ingredient.of(ModItems.ENTROPIC_ORE_ITEM.get()));
            for (int i = 0; i < 3; i++) testModifiers.add(Ingredient.of(Items.BLAZE_POWDER));
            testModifiers.add(Ingredient.of(Items.ECHO_SHARD));

            LATHE_RECIPES.add(new EidolicLatheRecipe(
                    Ingredient.of(Items.PAPER), // Ethereal Shape
                    Ingredient.of(Items.NETHER_STAR), // Core
                    testModifiers, // 10 Modifiers
                    new ItemStack(ModItems.TEST_EIDOLIC_WEAPON.get()) // Output
            ));
        }
        return LATHE_RECIPES;
    }

    // ==========================================
    // SYNTHESIZER FALLBACK RECIPES (1-56)
    // ==========================================
    public static List<AethericSynthesizerRecipe> getSynthesizerRecipes() {
        if (SYNTHESIZER_RECIPES == null) {
            SYNTHESIZER_RECIPES = new ArrayList<>();

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
                build(SYNTHESIZER_RECIPES, new String[]{"  F  ", " RIR ", " AAA ", " RIR ", "  F  "},
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
                    build(SYNTHESIZER_RECIPES, new String[]{"  F  ", " RVR ", " VVV ", " RVR ", "  F  "},
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
                    build(SYNTHESIZER_RECIPES, new String[]{"  F  ", " RGR ", " TTT ", " RGR ", "  F  "},
                            new Object[]{'F', getFume(fA, t), 'R', redstone, 'G', gold, 'T', resIngot},
                            new Object[]{cRes, c, bA, 2});
                }
            }

            // 25-27. Basic Wires
            String[] wirePat = {"     ", " NNN ", "     ", "     ", "     "};
            build(SYNTHESIZER_RECIPES, wirePat, new Object[]{'N', arcNugget}, new Object[]{arcWire, 8});
            build(SYNTHESIZER_RECIPES, wirePat, new Object[]{'N', viscNugget}, new Object[]{viscWire, 8});
            build(SYNTHESIZER_RECIPES, wirePat, new Object[]{'N', resNugget}, new Object[]{resWire, 8});

            // 28-30. Coated Wires
            String[] coatWirePat = {"     ", " LLL ", " WWW ", " LLL ", "     "};
            build(SYNTHESIZER_RECIPES, coatWirePat, new Object[]{'L', leather, 'W', arcWire}, new Object[]{coatArcWire, 8});
            build(SYNTHESIZER_RECIPES, coatWirePat, new Object[]{'L', leather, 'W', viscWire}, new Object[]{coatViscWire, 8});
            build(SYNTHESIZER_RECIPES, coatWirePat, new Object[]{'L', leather, 'W', resWire}, new Object[]{coatResWire, 8});

            // 31-33. Filaments
            String[] filPat = {"  R  ", " N N ", "R   R", " N N ", "  R  "};
            build(SYNTHESIZER_RECIPES, filPat, new Object[]{'N', arcNugget, 'R', redstone}, new Object[]{arcFil, 4});
            build(SYNTHESIZER_RECIPES, filPat, new Object[]{'N', viscNugget, 'R', redstone}, new Object[]{viscFil, 4});
            build(SYNTHESIZER_RECIPES, filPat, new Object[]{'N', resNugget, 'R', redstone}, new Object[]{resFil, 4});

            // 34-36. Charged Filaments
            String[] cFilPat = {"  G  ", "  F  ", "GFEFG", "  F  ", "  G  "};
            build(SYNTHESIZER_RECIPES, cFilPat, new Object[]{'G', glowstone, 'F', arcFil, 'E', getFume(smallAmp, EssenceType.LIGHTNING)}, new Object[]{cArcFil, 4, smallBase, 1});
            build(SYNTHESIZER_RECIPES, cFilPat, new Object[]{'G', glowstone, 'F', viscFil, 'E', getFume(smallAmp, EssenceType.STORM)}, new Object[]{cViscFil, 4, smallBase, 1});
            build(SYNTHESIZER_RECIPES, cFilPat, new Object[]{'G', glowstone, 'F', resFil, 'E', getEssence(ModItems.WEAK_ESSENCE.get(), EssenceType.EARTH)}, new Object[]{cResFil, 4});

            // 37-39. Coated Filaments
            String[] coatFilPat = {" L L ", "L F L", " F F ", "L F L", " L L "};
            build(SYNTHESIZER_RECIPES, coatFilPat, new Object[]{'L', leather, 'F', arcFil}, new Object[]{coatArcFil, 4});
            build(SYNTHESIZER_RECIPES, coatFilPat, new Object[]{'L', leather, 'F', viscFil}, new Object[]{coatViscFil, 4});
            build(SYNTHESIZER_RECIPES, coatFilPat, new Object[]{'L', leather, 'F', resFil}, new Object[]{coatResFil, 4});

            // 40-42. Spools
            String[] spoolPat = {" WSW ", "W   W", "S A S", "W   W", " WSW "};
            build(SYNTHESIZER_RECIPES, spoolPat, new Object[]{'W', planks, 'S', stick, 'A', arcWire}, new Object[]{arcSpool, 1});
            build(SYNTHESIZER_RECIPES, spoolPat, new Object[]{'W', planks, 'S', stick, 'A', viscWire}, new Object[]{viscSpool, 1});
            build(SYNTHESIZER_RECIPES, spoolPat, new Object[]{'W', planks, 'S', stick, 'A', resWire}, new Object[]{resSpool, 1});

            // 43-44. Frames
            String[] framePat = {" AAA ", " A A ", " AAA ", "     ", "     "};
            build(SYNTHESIZER_RECIPES, framePat, new Object[]{'A', arcIngot}, new Object[]{arcFrame, 1});
            build(SYNTHESIZER_RECIPES, framePat, new Object[]{'A', viscIngot}, new Object[]{viscFrame, 1});

            // 45-47. Plates
            String[] platePat = {"     ", " AAA ", " AAA ", "     ", "     "};
            build(SYNTHESIZER_RECIPES, platePat, new Object[]{'A', arcIngot}, new Object[]{arcPlate, 4}); // Updated to yield 4 plates!
            build(SYNTHESIZER_RECIPES, platePat, new Object[]{'A', viscIngot}, new Object[]{viscPlate, 4});
            build(SYNTHESIZER_RECIPES, platePat, new Object[]{'A', resIngot}, new Object[]{resPlate, 4});

            // 48-50. Small Gears
            String[] sGearPat = {"  N  ", " NIN ", "  N  ", "     ", "     "};
            build(SYNTHESIZER_RECIPES, sGearPat, new Object[]{'N', arcNugget, 'I', ironNugget}, new Object[]{arcSGear, 4});
            build(SYNTHESIZER_RECIPES, sGearPat, new Object[]{'N', viscNugget, 'I', ironNugget}, new Object[]{viscSGear, 4});
            build(SYNTHESIZER_RECIPES, sGearPat, new Object[]{'N', resNugget, 'I', ironNugget}, new Object[]{resSGear, 4});

            // 51-53. Large Gears
            String[] lGearPat = {" III ", "I N I", "IN NI", "I N I", " III "};
            build(SYNTHESIZER_RECIPES, lGearPat, new Object[]{'I', arcNugget, 'N', ironNugget}, new Object[]{arcLGear, 1});
            build(SYNTHESIZER_RECIPES, lGearPat, new Object[]{'I', viscNugget, 'N', ironNugget}, new Object[]{viscLGear, 1});
            build(SYNTHESIZER_RECIPES, lGearPat, new Object[]{'I', resNugget, 'N', ironNugget}, new Object[]{resLGear, 1});

            // 54-56. Gearsets
            String[] setPat = {"  S  ", " SLS ", "  S  ", "     ", "     "};
            build(SYNTHESIZER_RECIPES, setPat, new Object[]{'S', arcSGear, 'L', arcLGear}, new Object[]{arcGearset, 1});
            build(SYNTHESIZER_RECIPES, setPat, new Object[]{'S', viscSGear, 'L', viscLGear}, new Object[]{viscGearset, 1});
            build(SYNTHESIZER_RECIPES, setPat, new Object[]{'S', resSGear, 'L', resLGear}, new Object[]{resGearset, 1});
        }
        return SYNTHESIZER_RECIPES;
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
}