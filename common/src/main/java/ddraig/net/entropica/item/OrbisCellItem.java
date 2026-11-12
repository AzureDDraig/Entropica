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

    public int getMaxVis() {
        Block block = this.getBlock();
        if (block instanceof ddraig.net.entropica.block.OrbisCellBlock) return EntropicaConfig.ORBIS_CELL_MAX_MATERIA.get();
        if (block instanceof ddraig.net.entropica.block.SublimatedOrbisCellBlock) return 100000;
        if (block instanceof ddraig.net.entropica.block.PneumaticCalixBlock) return 250000;
        if (block instanceof ddraig.net.entropica.block.VoltaicCalixBlock) return 500000;
        if (block instanceof ddraig.net.entropica.block.MatrixCalixBlock) return 1000000;
        if (block instanceof ddraig.net.entropica.block.ThecaCellBlock) return 2500000;
        if (block instanceof ddraig.net.entropica.block.VasCellBlock) return 5000000;
        if (block instanceof ddraig.net.entropica.block.MonadCoreBlock) return 10000000;
        if (block instanceof ddraig.net.entropica.block.AthanorCoreBlock) return 25000000;
        return EntropicaConfig.ORBIS_CELL_MAX_MATERIA.get();
    }

    public static int getTier(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof ddraig.net.entropica.block.OrbisCellBlock) return 2;
            if (block instanceof ddraig.net.entropica.block.SublimatedOrbisCellBlock) return 3;
            if (block instanceof ddraig.net.entropica.block.PneumaticCalixBlock) return 4;
            if (block instanceof ddraig.net.entropica.block.VoltaicCalixBlock) return 5;
            if (block instanceof ddraig.net.entropica.block.MatrixCalixBlock) return 6;
            if (block instanceof ddraig.net.entropica.block.ThecaCellBlock) return 7;
            if (block instanceof ddraig.net.entropica.block.VasCellBlock) return 8;
            if (block instanceof ddraig.net.entropica.block.MonadCoreBlock) return 9;
            if (block instanceof ddraig.net.entropica.block.AthanorCoreBlock) return 10;
        }
        return 2;
    }

    public static int getMaxMateria(ItemStack stack) {
        int tier = getTier(stack);
        return switch (tier) {
            case 3 -> 100000;
            case 4 -> 250000;
            case 5 -> 500000;
            case 6 -> 1000000;
            case 7 -> 2500000;
            case 8 -> 5000000;
            case 9 -> 10000000;
            case 10 -> 25000000;
            default -> EntropicaConfig.ORBIS_CELL_MAX_MATERIA.get();
        };
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
        int tier = getTier(stack);
        return customData.copyTag().getInt("StoredMateria" + tier).orElse(0);
    }

    public static void setStoredMateriaAmount(ItemStack stack, int amount) {
        net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int tier = getTier(stack);
        tag.putInt("StoredMateria" + tier, amount);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // --- DYNAMIC TOOLTIP ---

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        int currentCellMateria = getStoredMateriaAmount(stack);
        EssenceType type = getStoredMateriaType(stack);
        int maxMateria = getMaxMateria(stack);
        int tier = getTier(stack);

        if (currentCellMateria > 0 && type != null) {
            long time = System.currentTimeMillis() / 50;
            int[] rgb = type.getCurrentRGB(time);
            int hexColor = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];

            Style essenceStyle = Style.EMPTY.withColor(hexColor);

            tooltipComponents.accept(Component.translatable("msg.entropica.stored_materia").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(type.getDisplayName()).withStyle(essenceStyle))
                    .append(Component.literal(") - Tier " + tier + ": ").withStyle(ChatFormatting.GRAY))
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
            tooltipComponents.accept(Component.literal("Stored Materia (Empty) - Tier " + tier + ": 0 / " + maxMateria).withStyle(ChatFormatting.DARK_GRAY));
            Component emptyBar = Component.literal("[" + "|".repeat(20) + "]").withStyle(ChatFormatting.DARK_GRAY);
            tooltipComponents.accept(emptyBar);
        }
    }
}