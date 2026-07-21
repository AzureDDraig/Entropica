package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.config.EntropicaConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class OrbisCellItem extends BlockItem {

    public OrbisCellItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public int getMaxMateria() {
        // Assuming config keeps the original internal variable name, but logically returning Materia
        return EntropicaConfig.ORBIS_CELL_MAX_MATERIA.get();
    }

    // --- HELPER METHODS FOR DATA COMPONENTS ---

    @Nullable
    public static EssenceType getStoredMateriaType(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        String typeStr = customData.copyTag().getString("MateriaType").orElse("");
        if (!typeStr.isEmpty()) {
            try {
                return EssenceType.valueOf(typeStr);
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    public static int getStoredMateriaAmount(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getInt("StoredMateria2").orElse(0);
    }

    // --- DYNAMIC TOOLTIP ---

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        int currentCellMateria = getStoredMateriaAmount(stack);
        EssenceType type = getStoredMateriaType(stack);
        int maxMateria = getMaxMateria();

        if (currentCellMateria > 0 && type != null) {
            long time = System.currentTimeMillis() / 50;
            int[] rgb = type.getCurrentRGB(time);
            int hexColor = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];

            Style essenceStyle = Style.EMPTY.withColor(hexColor);

            tooltipComponents.accept(Component.literal("Stored Materia (").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(type.getDisplayName()).withStyle(essenceStyle))
                    .append(Component.literal("): ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(currentCellMateria + " / " + maxMateria).withStyle(essenceStyle)));

            // Text Progress Bar
            int barSegments = 20;
            int filledSegments = (int) Math.round(((double) currentCellMateria / maxMateria) * barSegments);
            filledSegments = Math.clamp(filledSegments, 0, barSegments);
            int emptySegments = barSegments - filledSegments;

            Component progressBar = Component.literal("[")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("|".repeat(filledSegments)).withStyle(essenceStyle))
                    .append(Component.literal("|".repeat(emptySegments)).withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));

            tooltipComponents.accept(progressBar);
        } else {
            // Empty State
            tooltipComponents.accept(Component.literal("Stored Materia (Empty): 0 / " + maxMateria).withStyle(ChatFormatting.DARK_GRAY));
            Component emptyBar = Component.literal("[" + "|".repeat(20) + "]").withStyle(ChatFormatting.DARK_GRAY);
            tooltipComponents.accept(emptyBar);
        }
    }
}