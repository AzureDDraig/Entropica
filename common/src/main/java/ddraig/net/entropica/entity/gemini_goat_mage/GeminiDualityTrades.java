package ddraig.net.entropica.entity.gemini_goat_mage;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class GeminiDualityTrades {

    public static class DualityTradeOffer {
        public final ItemStack costA;
        public final ItemStack costB;
        public final ItemStack result;

        public DualityTradeOffer(ItemStack costA, ItemStack result) {
            this(costA, ItemStack.EMPTY, result);
        }

        public DualityTradeOffer(ItemStack costA, ItemStack costB, ItemStack result) {
            this.costA = costA;
            this.costB = costB;
            this.result = result;
        }

        public boolean matches(ItemStack inputA, ItemStack inputB) {
            if (!ItemStack.isSameItemSameComponents(inputA, this.costA) || inputA.getCount() < this.costA.getCount()) {
                return false;
            }
            if (this.costB.isEmpty()) {
                return true;
            }
            return ItemStack.isSameItemSameComponents(inputB, this.costB) && inputB.getCount() >= this.costB.getCount();
        }
    }

    public static final List<DualityTradeOffer> TRADES = new ArrayList<>();

    static {
        // --- 1. Radiant-Fire -> Umbral-Water Trades ---
        TRADES.add(new DualityTradeOffer(new ItemStack(Items.BLAZE_POWDER, 4), new ItemStack(Items.PRISMARINE_CRYSTALS, 4)));
        TRADES.add(new DualityTradeOffer(new ItemStack(Items.MAGMA_CREAM, 2), new ItemStack(Items.HEART_OF_THE_SEA, 1)));
        TRADES.add(new DualityTradeOffer(new ItemStack(Items.FIRE_CHARGE, 3), new ItemStack(Items.GLOW_INK_SAC, 6)));

        // --- 2. Umbral-Water -> Radiant-Fire Trades ---
        TRADES.add(new DualityTradeOffer(new ItemStack(Items.PRISMARINE_SHARD, 4), new ItemStack(Items.BLAZE_ROD, 2)));
        TRADES.add(new DualityTradeOffer(new ItemStack(Items.INK_SAC, 8), new ItemStack(Items.GLOWSTONE_DUST, 8)));

        // --- 3. Dual Balanced Offerings (Caprine Equinox Artifacts) ---
        TRADES.add(new DualityTradeOffer(
                new ItemStack(Items.BLAZE_ROD, 4),
                new ItemStack(Items.HEART_OF_THE_SEA, 1),
                new ItemStack(ModItems.EQUINOX_VELVET_CLOTH.get(), 2)
        ));

        TRADES.add(new DualityTradeOffer(
                new ItemStack(Items.NETHER_STAR, 1),
                new ItemStack(Items.NAUTILUS_SHELL, 4),
                new ItemStack(ModItems.GEMINI_FOCUS_HORN.get(), 1)
        ));

        TRADES.add(new DualityTradeOffer(
                new ItemStack(Items.BLAZE_POWDER, 8),
                new ItemStack(Items.PRISMARINE_CRYSTALS, 8),
                new ItemStack(ModItems.EQUINOX_ESSENCE.get(), 1)
        ));
    }
}
