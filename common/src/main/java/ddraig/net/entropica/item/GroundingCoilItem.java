package ddraig.net.entropica.item;

import ddraig.net.entropica.block.biome7.StaticChargeHelper;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Grounding Coil — copper-and-fulgurite pocket relic native to 2.7 Static Ice-Plains.
 * Passively absorbs metal-armor Static Charge shocks, cuts incoming lightning damage by 50%,
 * and discharges stored static (+3 electric damage + Slowness II) on your next melee strike.
 */
public class GroundingCoilItem extends Item {

    public GroundingCoilItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static boolean isStoredStatic(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CustomData custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return custom.copyTag().getBoolean(StaticChargeHelper.TAG_STORED_STATIC).orElse(false);
    }

    public static void setStoredStatic(ItemStack stack, boolean stored) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (stored) {
                tag.putBoolean(StaticChargeHelper.TAG_STORED_STATIC, true);
            } else {
                tag.remove(StaticChargeHelper.TAG_STORED_STATIC);
            }
        });
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isStoredStatic(stack) || super.isFoil(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (entity instanceof Player player && player.isAlive()) {
            int staticTicks = StaticChargeHelper.getStaticChargeTicks(player);
            if (staticTicks > 0) {
                setStoredStatic(stack, true);
                StaticChargeHelper.clearStaticCharge(player);
            }
        }
    }

    private static boolean isCoil(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof GroundingCoilItem) {
            return true;
        }
        return ModItems.GROUNDING_COIL.isBound() && stack.is(ModItems.GROUNDING_COIL.get());
    }

    public static boolean hasGroundingCoil(Player player) {
        if (player == null) {
            return false;
        }
        if (isCoil(player.getMainHandItem()) || isCoil(player.getOffhandItem())) {
            return true;
        }
        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            if (isCoil(inv.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Grounding Coil in {@code player}'s hands or inventory, marks it with
     * {@code "StoredStatic": true}, clears static charge buildup, and returns true.
     */
    public static boolean tryAbsorbLightningOrStatic(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack fallbackCoil = ItemStack.EMPTY;

        ItemStack offHand = player.getOffhandItem();
        if (isCoil(offHand)) {
            if (!isStoredStatic(offHand)) {
                setStoredStatic(offHand, true);
                StaticChargeHelper.clearStaticCharge(player);
                return true;
            }
            fallbackCoil = offHand;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (isCoil(mainHand)) {
            if (!isStoredStatic(mainHand)) {
                setStoredStatic(mainHand, true);
                StaticChargeHelper.clearStaticCharge(player);
                return true;
            }
            if (fallbackCoil.isEmpty()) {
                fallbackCoil = mainHand;
            }
        }

        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack slotStack = inv.getItem(i);
            if (isCoil(slotStack)) {
                if (!isStoredStatic(slotStack)) {
                    setStoredStatic(slotStack, true);
                    StaticChargeHelper.clearStaticCharge(player);
                    return true;
                }
                if (fallbackCoil.isEmpty()) {
                    fallbackCoil = slotStack;
                }
            }
        }

        if (!fallbackCoil.isEmpty()) {
            setStoredStatic(fallbackCoil, true);
            StaticChargeHelper.clearStaticCharge(player);
            return true;
        }
        return false;
    }

    /**
     * Consumes {@code "StoredStatic": true} from any Grounding Coil in {@code player}'s
     * hands or inventory, returning true if a stored charge was discharged.
     */
    public static boolean tryConsumeStoredStatic(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack offHand = player.getOffhandItem();
        if (isCoil(offHand) && isStoredStatic(offHand)) {
            setStoredStatic(offHand, false);
            return true;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (isCoil(mainHand) && isStoredStatic(mainHand)) {
            setStoredStatic(mainHand, false);
            return true;
        }

        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack slotStack = inv.getItem(i);
            if (isCoil(slotStack) && isStoredStatic(slotStack)) {
                setStoredStatic(slotStack, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Carry: Absorbs Static Charge shocks and cuts lightning damage by 50%.")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Next Melee Hit: Releases stored static (+3 electric damage & Slowness II).")
                .withStyle(ChatFormatting.GRAY));
        if (isStoredStatic(stack)) {
            tooltipComponents.accept(Component.literal("Charged: Ready to release static on your next hit!")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
