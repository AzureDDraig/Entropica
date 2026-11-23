package ddraig.net.entropica.item.biome17;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Astral Nautilus Armor — sculpted Engraved Astral Slate and iridescent nacre barding
 * fitted onto tamed Starlight Nautiluses in 2.17 Astral Skerries.
 */
public class AstralNautilusArmorItem extends Item {

    public AstralNautilusArmorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Mount Barding: Right-click a tamed Starlight Nautilus to equip protective Astral Slate shell armor.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Reinforced Shell: Shields your Starlight Nautilus with resilient cosmic nacre plating.")
                .withStyle(ChatFormatting.AQUA));
    }
}
