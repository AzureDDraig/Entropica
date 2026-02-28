package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.config.EntropicaConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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

    public int getMaxMana() {
        return EntropicaConfig.ORBIS_CELL_MAX_MANA.get();
    }

    // --- HELPER METHODS FOR DATA COMPONENTS ---

    @Nullable
    public static EssenceType getStoredManaType(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        String typeStr = customData.copyTag().getString("ManaType").orElse("");
        if (!typeStr.isEmpty()) {
            try {
                return EssenceType.valueOf(typeStr);
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    public static int getStoredManaAmount(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getInt("StoredMana").orElse(0);
    }

    // --- DYNAMIC TOOLTIP ---

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        int currentCellMana = getStoredManaAmount(stack);
        EssenceType type = getStoredManaType(stack);
        int maxMana = getMaxMana();

        if (currentCellMana > 0 && type != null) {
            // Get the precise RGB color from your Enum
            // We use a static time of 0 for the tooltip so it doesn't flicker wildly unless it's a dynamic type
            long time = System.currentTimeMillis() / 50;
            int[] rgb = type.getCurrentRGB(time);
            int hexColor = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];

            // Create a custom style using that Hex value
            Style essenceStyle = Style.EMPTY.withColor(hexColor);

            tooltipComponents.accept(Component.literal("Stored Mana (").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(type.getDisplayName()).withStyle(essenceStyle))
                    .append(Component.literal("): ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(currentCellMana + " / " + maxMana).withStyle(essenceStyle)));

            // Text Progress Bar
            int barSegments = 20;
            int filledSegments = (int) Math.round(((double) currentCellMana / maxMana) * barSegments);
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
            tooltipComponents.accept(Component.literal("Stored Mana (Empty): 0 / " + maxMana).withStyle(ChatFormatting.DARK_GRAY));
            Component emptyBar = Component.literal("[" + "|".repeat(20) + "]").withStyle(ChatFormatting.DARK_GRAY);
            tooltipComponents.accept(emptyBar);
        }
    }
}