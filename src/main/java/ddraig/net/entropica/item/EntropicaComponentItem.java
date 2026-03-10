package ddraig.net.entropica.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class EntropicaComponentItem extends Item {
    private final String tooltipKey;

    public EntropicaComponentItem(Properties properties, String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (this.tooltipKey != null && !this.tooltipKey.isEmpty()) {
            // We use .accept() instead of .add() for the new Consumer interface
            tooltipComponents.accept(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}