package ddraig.net.entropica.item.weapons;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class DynamicWeaponItem extends Item {

    public DynamicWeaponItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        // Later on, we will read the Item Components (NBT data) from the stack here
        // to dynamically list the modifiers applied to the weapon during Lathe crafting!
        tooltipComponents.accept(Component.translatable("tooltip.entropica.forged_weapon").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            tooltipComponents.accept(Component.literal("A weapon bound to the soul...").withStyle(ChatFormatting.GRAY));
        }
    }
}