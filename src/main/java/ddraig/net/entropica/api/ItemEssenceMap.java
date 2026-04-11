package ddraig.net.entropica.api;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.item.EssenceItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ItemEssenceMap {

    public record EssenceValue(EssenceType type, float amount) {}

    // The O(1) Master Cache that holds the generated Essence Graph!
    private static final Map<Item, List<EssenceValue>> CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, List<EssenceValue>> MANUAL_REGISTRY = new ConcurrentHashMap<>();

    private static ItemStack[] ALL_ITEMS_CACHE = null;

    public static void register(Item item, EssenceValue... values) {
        MANUAL_REGISTRY.put(item, List.of(values));
    }

    /**
     * O(1) Instant Lookup. The graph is already built by the time the player joins the game!
     */
    public static List<EssenceValue> getEssenceFor(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof EssenceItem) {
            return List.of();
        }

        Item item = stack.getItem();
        if (MANUAL_REGISTRY.containsKey(item)) return MANUAL_REGISTRY.get(item);
        if (CACHE.containsKey(item)) return CACHE.get(item);

        // Ultimate Fallback if an item is completely uncraftable and unmapped
        return List.of(new EssenceValue(EssenceType.EARTH, 0.03125f));
    }

    /**
     * The Iterative Essence Graph Generator.
     * Runs exactly once on Server Start to perfectly map the entire game!
     */
    public static void buildEssenceGraph(Iterable<RecipeHolder<?>> recipes, net.minecraft.core.HolderLookup.Provider registries) {
        CACHE.clear();

        // 1. SEED THE GRAPH: Load base materials into the Cache
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            List<EssenceValue> base = getBaseMaterialEssence(stack);

            if (!base.isEmpty()) {
                CACHE.put(item, base);
            }
        }

        // 2. ITERATIVE CASCADE: Keep scanning recipes until the entire progression tree is mapped!
        boolean changed = true;
        int iterations = 0;

        while (changed && iterations < 100) { // Max 100 deep crafting steps to prevent infinite loop crashes
            changed = false;
            iterations++;

            for (RecipeHolder<?> holder : recipes) {
                Object recipe = holder.value();

                // Deep scan for the result item
                ItemStack result = extractResultSafe(recipe, registries);

                // If we already calculated this item's value, skip it
                if (result == null || result.isEmpty() || CACHE.containsKey(result.getItem())) continue;

                List<EssenceValue> recipeCost = new ArrayList<>();
                boolean canCraft = true;
                boolean hasIngredients = false;

                // Deep scan for ingredients hidden in patterns, lists, or fields
                for (Ingredient ingredient : extractIngredientsSafe(recipe)) {
                    if (ingredient == null || ingredient.isEmpty()) continue;
                    hasIngredients = true;

                    ItemStack rep = getKnownMatchingItemSafe(ingredient);

                    // If we don't know the essence value of an ingredient yet, we can't calculate this recipe!
                    if (rep.isEmpty() || !CACHE.containsKey(rep.getItem())) {
                        canCraft = false;
                        break;
                    }

                    // Add the ingredient's essence to the total recipe cost
                    recipeCost = combineEssences(recipeCost, CACHE.get(rep.getItem()));
                }

                // If all ingredients were known, we successfully calculated the result!
                if (canCraft && hasIngredients && !recipeCost.isEmpty()) {
                    int count = result.getCount();
                    if (count > 1) {
                        List<EssenceValue> divided = new ArrayList<>();
                        for (EssenceValue ev : recipeCost) {
                            divided.add(new EssenceValue(ev.type(), ev.amount() / count));
                        }
                        CACHE.put(result.getItem(), divided);
                    } else {
                        CACHE.put(result.getItem(), recipeCost);
                    }
                    changed = true; // We discovered a new item! Run the loop again!
                }
            }
        }

        Entropica.LOGGER.info("Entropica Essence Graph Generation Complete! Mapped " + CACHE.size() + " items in " + iterations + " iterations.");
    }

    // ==========================================
    // DEEP REFLECTION SCANNERS (Completely ignores missing API methods)
    // ==========================================

    private static ItemStack extractResultSafe(Object recipe, net.minecraft.core.HolderLookup.Provider registries) {
        // 1. Check direct methods (The polite way)
        try {
            for (java.lang.reflect.Method m : recipe.getClass().getMethods()) {
                if (m.getReturnType() == ItemStack.class) {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == net.minecraft.core.HolderLookup.Provider.class) {
                        ItemStack res = (ItemStack) m.invoke(recipe, registries);
                        if (res != null && !res.isEmpty()) return res;
                    } else if (m.getParameterCount() == 0) {
                        String name = m.getName().toLowerCase();
                        if (name.contains("result") || name.contains("output")) {
                            ItemStack res = (ItemStack) m.invoke(recipe);
                            if (res != null && !res.isEmpty()) return res;
                        }
                    }
                }
            }
        } catch (Exception e) {}

        // 2. Scan internal memory fields (The forceful way)
        try {
            Class<?> current = recipe.getClass();
            while (current != null && current != Object.class) {
                for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                    if (f.getType() == ItemStack.class) {
                        f.setAccessible(true);
                        ItemStack res = (ItemStack) f.get(recipe);
                        if (res != null && !res.isEmpty()) return res;
                    }
                }
                current = current.getSuperclass();
            }
        } catch (Exception e) {}

        return ItemStack.EMPTY;
    }

    private static Iterable<Ingredient> extractIngredientsSafe(Object recipe) {
        List<Ingredient> found = new ArrayList<>();
        findIngredientsRecursive(recipe, found, 0, new HashSet<>());
        return found;
    }

    private static void findIngredientsRecursive(Object obj, List<Ingredient> found, int depth, Set<Integer> visited) {
        // Stop going too deep to prevent lag, 4 levels is enough to crack RecipePatterns and Lists
        if (obj == null || depth > 4) return;

        int hash = System.identityHashCode(obj);
        if (!visited.add(hash)) return; // Prevent circular references

        if (obj instanceof Ingredient ing) {
            try {
                if (!ing.isEmpty() && !ing.test(ItemStack.EMPTY)) {
                    found.add(ing);
                }
            } catch (Exception e) {}
            return;
        }

        if (obj instanceof Iterable<?> iter) {
            for (Object o : iter) findIngredientsRecursive(o, found, depth + 1, visited);
            return;
        }

        if (obj.getClass().isArray()) {
            if (!obj.getClass().getComponentType().isPrimitive()) {
                for (Object o : (Object[]) obj) findIngredientsRecursive(o, found, depth + 1, visited);
            }
            return;
        }

        // Do not scan core Java classes to save memory
        String className = obj.getClass().getName();
        if (className.startsWith("java.") || className.startsWith("jdk.")) return;

        // Recursively crack open all fields looking for ingredients
        Class<?> current = obj.getClass();
        while (current != null && current != Object.class) {
            for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                try {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    findIngredientsRecursive(val, found, depth + 1, visited);
                } catch (Exception e) {}
            }
            current = current.getSuperclass();
        }
    }

    private static ItemStack getKnownMatchingItemSafe(Ingredient ingredient) {
        if (ALL_ITEMS_CACHE == null) {
            ALL_ITEMS_CACHE = BuiltInRegistries.ITEM.stream().map(ItemStack::new).toArray(ItemStack[]::new);
        }

        ItemStack firstMatch = ItemStack.EMPTY;

        for (ItemStack testStack : ALL_ITEMS_CACHE) {
            try {
                if (ingredient.test(testStack)) {
                    if (firstMatch.isEmpty()) firstMatch = testStack; // Save the first valid item as a fallback

                    // Crucial: Prioritize items that are already mapped in the Cache!
                    // This prevents a generic tag like "c:ingots" from testing against an unmapped modded item and failing!
                    if (CACHE.containsKey(testStack.getItem())) {
                        return testStack;
                    }
                }
            } catch (Exception e) {}
        }

        return firstMatch;
    }

    private static List<EssenceValue> combineEssences(List<EssenceValue> a, List<EssenceValue> b) {
        Map<EssenceType, Float> map = new EnumMap<>(EssenceType.class);
        for (EssenceValue ev : a) map.put(ev.type(), map.getOrDefault(ev.type(), 0f) + ev.amount());
        for (EssenceValue ev : b) map.put(ev.type(), map.getOrDefault(ev.type(), 0f) + ev.amount());

        List<EssenceValue> result = new ArrayList<>();
        for (Map.Entry<EssenceType, Float> entry : map.entrySet()) {
            result.add(new EssenceValue(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    // ==========================================
    // BASE MATERIAL HARDCODES (The Seed Data)
    // ==========================================

    private static List<EssenceValue> getBaseMaterialEssence(ItemStack stack) {
        Item item = stack.getItem();
        List<EssenceValue> list = new ArrayList<>();

        // 1. Specific Vanilla Base Items (Placed FIRST to explicitly override generic tags!)
        if (item == Items.LAPIS_LAZULI) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.EARTH, 0.25f));
        if (item == Items.REDSTONE) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f), new EssenceValue(EssenceType.LIGHTNING, 0.25f));
        if (item == Items.QUARTZ) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f), new EssenceValue(EssenceType.NETHER, 0.25f));

        if (item == Items.ROTTEN_FLESH || item == Items.BONE) return List.of(new EssenceValue(EssenceType.UNDEAD, 0.25f));
        if (item == Items.SPIDER_EYE || item == Items.STRING) return List.of(new EssenceValue(EssenceType.UMBRAL, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));
        if (item == Items.GUNPOWDER) return List.of(new EssenceValue(EssenceType.DUST, 0.25f), new EssenceValue(EssenceType.PYRE, 0.25f));
        if (item == Items.SLIME_BALL) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));
        if (item == Items.ENDER_PEARL) return List.of(new EssenceValue(EssenceType.VOID, 1.0f));
        if (item == Items.GHAST_TEAR) return List.of(new EssenceValue(EssenceType.UMBRAL, 1.0f), new EssenceValue(EssenceType.NETHER, 1.0f));
        if (item == Items.BLAZE_ROD) return List.of(new EssenceValue(EssenceType.PYRE, 1.0f));
        if (item == Items.PHANTOM_MEMBRANE) return List.of(new EssenceValue(EssenceType.UMBRAL, 0.5f), new EssenceValue(EssenceType.AIR, 0.5f));
        if (item == Items.FEATHER) return List.of(new EssenceValue(EssenceType.AIR, 0.25f));
        if (item == Items.BREEZE_ROD) return List.of(new EssenceValue(EssenceType.AIR, 1.0f), new EssenceValue(EssenceType.STORM, 1.0f));

        if (item == Items.GLOW_INK_SAC) return List.of(new EssenceValue(EssenceType.RADIANT, 0.25f), new EssenceValue(EssenceType.WATER, 0.25f));
        if (item == Items.INK_SAC) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.UMBRAL, 0.25f));
        if (item == Items.LEATHER || item == Items.BEEF || item == Items.PORKCHOP || item == Items.MUTTON || item == Items.CHICKEN) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));

        if (stack.is(ItemTags.LOGS)) return List.of(new EssenceValue(EssenceType.NATURE, 1.0f));
        if (item == Items.COAL) return List.of(new EssenceValue(EssenceType.PYRE, 0.5f), new EssenceValue(EssenceType.EARTH, 0.25f));

        if (item == Items.NETHERRACK || item == Items.BLACKSTONE || item == Items.BASALT) return List.of(new EssenceValue(EssenceType.NETHER, 0.25f), new EssenceValue(EssenceType.DUST, 0.1f));
        if (item == Items.MAGMA_BLOCK) return List.of(new EssenceValue(EssenceType.MAGMA, 1.0f));
        if (item == Items.MAGMA_CREAM) return List.of(new EssenceValue(EssenceType.MAGMA, 0.5f), new EssenceValue(EssenceType.PYRE, 0.25f));
        if (item == Items.OBSIDIAN || item == Items.CRYING_OBSIDIAN) return List.of(new EssenceValue(EssenceType.VOID, 0.5f), new EssenceValue(EssenceType.MAGMA, 0.5f));
        if (item == Items.END_STONE) return List.of(new EssenceValue(EssenceType.VOID, 0.25f), new EssenceValue(EssenceType.EARTH, 0.25f));

        if (item == Items.COBBLESTONE || item == Items.STONE || item == Items.DEEPSLATE) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f));
        if (stack.is(ItemTags.SAND) || stack.is(ItemTags.DIRT) || item == Items.GRAVEL) return List.of(new EssenceValue(EssenceType.DUST, 0.25f));

        if (item == Items.ICE || item == Items.PACKED_ICE || item == Items.BLUE_ICE) return List.of(new EssenceValue(EssenceType.FROZEN, 1.0f));
        if (item == Items.KELP || item == Items.SEAGRASS || item == Items.SUGAR_CANE) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.NATURE, 0.125f));

        if (item == Items.PRISMARINE_SHARD) return List.of(new EssenceValue(EssenceType.WATER, 0.25f));
        if (item == Items.PRISMARINE_CRYSTALS) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.RADIANT, 0.25f));
        if (item == Items.PRISMARINE || item == Items.PRISMARINE_BRICKS || item == Items.DARK_PRISMARINE) return List.of(new EssenceValue(EssenceType.WATER, 1.0f));
        if (item == Items.SEA_LANTERN) return List.of(new EssenceValue(EssenceType.WATER, 1.0f), new EssenceValue(EssenceType.RADIANT, 1.0f));
        if (item == Items.COD || item == Items.SALMON || item == Items.PUFFERFISH || item == Items.TROPICAL_FISH) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));

        // 2. Dynamic Base Tags
        boolean matchedTag = false;
        for (var tagKey : stack.getTags().toList()) {
            String path = tagKey.location().getPath().toLowerCase();

            if (path.contains("ingots")) {
                list.add(new EssenceValue(EssenceType.EARTH, 0.5f));
                list.add(new EssenceValue(EssenceType.PYRE, 0.5f));
                matchedTag = true; break;
            } else if (path.contains("raw_materials")) {
                list.add(new EssenceValue(EssenceType.EARTH, 1.0f));
                matchedTag = true; break;
            } else if (path.contains("gems")) {
                list.add(new EssenceValue(EssenceType.EARTH, 2.0f));
                list.add(new EssenceValue(EssenceType.RADIANT, 1.0f));
                matchedTag = true; break;
            } else if (path.contains("dusts")) {
                list.add(new EssenceValue(EssenceType.DUST, 0.25f));
                list.add(new EssenceValue(EssenceType.LIGHTNING, 0.25f));
                matchedTag = true; break;
            }
        }
        if (matchedTag) return list;

        return List.of();
    }
}