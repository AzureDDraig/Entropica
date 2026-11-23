package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.registry.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Biome-exclusive Star-Fishing loot helper for Biome #17 (2.17 Astral Skerries).
 * Replaces vanilla fish catches in Astral Skerries waters with weighted rolls across
 * the 7 cosmic Star Fish species while leaving junk/treasure catches untouched.
 */
public final class AstralSkerriesFishingHelper {

    private record WeightedCatch(RegistrySupplier<? extends Item> item, int weight) {}

    private AstralSkerriesFishingHelper() {
    }

    public static void replaceAstralSkerriesCatches(
            FishingHook hook,
            LootParams lootParams,
            ObjectArrayList<ItemStack> items
    ) {
        if (hook == null || items == null || items.isEmpty()) {
            return;
        }
        Level level = hook.level();
        RandomSource random = level.getRandom();
        int luck = Math.max(0, Mth.floor(lootParams.getLuck()));
        boolean isNight = level.isDarkOutside();
        boolean openSky = level.canSeeSky(hook.blockPosition().above()) || level.canSeeSky(hook.blockPosition());

        for (int i = 0; i < items.size(); i++) {
            ItemStack current = items.get(i);
            if (isFishCatch(current)) {
                Item chosen = rollAstralStarFish(random, luck, isNight, openSky);
                if (chosen != null) {
                    items.set(i, new ItemStack(chosen, Math.max(1, current.getCount())));
                }
            }
        }
    }

    private static boolean isFishCatch(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.is(Items.COD)
                || stack.is(Items.SALMON)
                || stack.is(Items.TROPICAL_FISH)
                || stack.is(Items.PUFFERFISH)
                || stack.is(ItemTags.FISHES);
    }

    private static Item rollAstralStarFish(RandomSource random, int luck, boolean isNight, boolean openSky) {
        List<WeightedCatch> pool = new ArrayList<>(7);
        addIfBound(pool, ModItems.NEBULA_DARTER, 32);
        addIfBound(pool, ModItems.STARWING_FLYING_FISH, 18);
        addIfBound(pool, ModItems.COMET_RIBBONFISH, 16);
        addIfBound(pool, ModItems.STARFALL_RAY, 14);
        addIfBound(pool, ModItems.ZENITH_MOONFISH, isNight ? 24 : 12);
        addIfBound(pool, ModItems.SUPERNOVA_PUFFER, 10 + 3 * luck);
        addIfBound(pool, ModItems.ASTRAL_LANTERNFISH, (isNight && openSky) ? (14 + 3 * luck) : 4);

        int totalWeight = 0;
        for (WeightedCatch entry : pool) {
            totalWeight += entry.weight();
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (WeightedCatch entry : pool) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.item().get();
            }
        }
        return pool.get(pool.size() - 1).item().get();
    }

    private static void addIfBound(List<WeightedCatch> pool, RegistrySupplier<? extends Item> supplier, int weight) {
        if (weight > 0 && supplier != null && supplier.isBound()) {
            pool.add(new WeightedCatch(supplier, weight));
        }
    }
}
