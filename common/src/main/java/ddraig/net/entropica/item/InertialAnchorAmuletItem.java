package ddraig.net.entropica.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class InertialAnchorAmuletItem extends Item {

    public InertialAnchorAmuletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.entropica.inertial_anchor_amulet.desc_1")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.translatable("tooltip.entropica.inertial_anchor_amulet.desc_2")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.entropica.curios_slot.charm")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}