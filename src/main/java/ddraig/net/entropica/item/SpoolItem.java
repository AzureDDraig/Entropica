package ddraig.net.entropica.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpoolItem extends EntropicaComponentItem {
    private final Supplier<Item> validWire;
    private final Supplier<Item> validFilament;
    private final int maxCapacity = 256;

    public SpoolItem(Properties properties, String tooltipKey, Supplier<Item> validWire, Supplier<Item> validFilament) {
        super(properties, tooltipKey);
        this.validWire = validWire;
        this.validFilament = validFilament;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack spoolStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) {
            // Extract from spool into empty slot
            extractItem(spoolStack, player, new SlotAccess() {
                @Override public ItemStack get() { return slot.getItem(); }
                @Override public boolean set(ItemStack stack) { slot.set(stack); return true; }
            });
            return true;
        } else {
            // Try to insert slot item into spool
            return insertItem(spoolStack, slotStack);
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack spoolStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;

        if (otherStack.isEmpty()) {
            // Extract from spool to cursor
            extractItem(spoolStack, player, access);
            return true;
        } else {
            // Try to insert cursor item into spool
            return insertItem(spoolStack, otherStack);
        }
    }

    private boolean insertItem(ItemStack spoolStack, ItemStack insertStack) {
        if (!insertStack.is(validWire.get()) && !insertStack.is(validFilament.get())) return false;

        CustomData data = spoolStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        String currentItemStr = tag.getString("StoredItem").orElse("");
        int currentCount = tag.getInt("StoredCount").orElse(0);

        String insertItemStr = BuiltInRegistries.ITEM.getKey(insertStack.getItem()).toString();

        // Ensure we don't mix wire and filament in the same spool
        if (currentCount > 0 && !currentItemStr.equals(insertItemStr)) return false;

        int space = maxCapacity - currentCount;
        if (space <= 0) return false;

        int amountToInsert = Math.min(space, insertStack.getCount());
        insertStack.shrink(amountToInsert);

        CustomData.update(DataComponents.CUSTOM_DATA, spoolStack, t -> {
            t.putString("StoredItem", insertItemStr);
            t.putInt("StoredCount", currentCount + amountToInsert);
        });

        return true;
    }

    private void extractItem(ItemStack spoolStack, Player player, SlotAccess access) {
        CustomData data = spoolStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        int currentCount = tag.getInt("StoredCount").orElse(0);
        if (currentCount <= 0) return;

        String currentItemStr = tag.getString("StoredItem").orElse("");
        ResourceLocation rl = ResourceLocation.tryParse(currentItemStr);
        if (rl == null) return;

        Item storedItem = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
        if (storedItem == null) return;

        int extractAmount = Math.min(currentCount, storedItem.getDefaultMaxStackSize());

        ItemStack extracted = new ItemStack(storedItem, extractAmount);
        access.set(extracted);

        int newCount = currentCount - extractAmount;
        CustomData.update(DataComponents.CUSTOM_DATA, spoolStack, t -> {
            if (newCount <= 0) {
                t.remove("StoredItem");
                t.remove("StoredCount");
            } else {
                t.putInt("StoredCount", newCount);
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        int count = tag.getInt("StoredCount").orElse(0);
        if (count > 0) {
            String itemStr = tag.getString("StoredItem").orElse("");
            ResourceLocation rl = ResourceLocation.tryParse(itemStr);
            if (rl != null) {
                Item storedItem = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
                if (storedItem != null) {
                    tooltipComponents.accept(Component.literal("Stored: " + count + " / " + maxCapacity + " ")
                            .append(Component.translatable(storedItem.getDescriptionId()))
                            .withStyle(ChatFormatting.AQUA));
                }
            }
        } else {
            tooltipComponents.accept(Component.literal("Empty").withStyle(ChatFormatting.GRAY));
        }
    }
}