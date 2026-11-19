package ddraig.net.entropica.item.biome1;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Glacial Focus Lens — ancient crystalline relic recovered from Frost-Veiled Glacial Runic Spires,
 * resonating with both Glacial and Aegis Materia.
 */
public class GlacialFocusLensItem extends Item {

    public GlacialFocusLensItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Ancient Runic Spire relic attuned to Glacial and Aegis Materia.")
                .withStyle(ChatFormatting.AQUA));
    }
}
