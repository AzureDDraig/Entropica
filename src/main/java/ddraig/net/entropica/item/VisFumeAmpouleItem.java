package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.function.Supplier;

public class VisFumeAmpouleItem extends Item {
    private final int capacity;
    private final Supplier<Item> baseItem;

    public VisFumeAmpouleItem(Properties properties, int capacity, Supplier<Item> baseItem) {
        super(properties);
        this.capacity = capacity;
        this.baseItem = baseItem;
    }

    public int getCapacity() {
        return capacity;
    }

    public Item getBaseItem() {
        return baseItem.get();
    }

    public static void setEssenceType(ItemStack stack, EssenceType type) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString("EssenceType", type.name());
        });
    }

    public static EssenceType getEssenceType(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.copyTag().contains("EssenceType")) {
            try {
                return EssenceType.valueOf(data.copyTag().getString("EssenceType").orElse(""));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        EssenceType type = getEssenceType(stack);
        if (type != null) {
            tooltipComponents.add(Component.literal("Type: " + type.getDisplayName())
                    .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(type.getTextColorInt())));
            tooltipComponents.add(Component.literal("§7Contains: §b" + capacity + " Mana"));
        } else {
            tooltipComponents.add(Component.literal("Corrupted Mana")
                    .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xFF5555)));
        }
    }

    public Component getName(ItemStack stack) {
        EssenceType type = getEssenceType(stack);
        if (type != null) {
            return Component.literal(type.getDisplayName() + " Ampoule")
                    .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(type.getTextColorInt()));
        }
        return super.getName(stack);
    }
}