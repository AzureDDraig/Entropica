package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class EssenceAmpouleItem extends Item {
    private final int tier;

    public EssenceAmpouleItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
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
            tooltipComponents.add(Component.literal("§7Type: ").append(Component.literal(type.getFormattedName())));
        }
    }

    public Component getName(ItemStack stack) {
        EssenceType type = getEssenceType(stack);
        if (type != null) {
            String tierPrefix = tier == 1 ? "Small " : tier == 2 ? "Medium " : "Large ";
            return Component.literal(type.getColorCode() + tierPrefix + type.getDisplayName() + " Ampoule");
        }
        return super.getName(stack);
    }
}