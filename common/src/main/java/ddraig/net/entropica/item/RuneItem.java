package ddraig.net.entropica.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class RuneItem extends EntropicaComponentItem {
    private final String unicodeChar;
    private final String runeName;
    private final String runeMeaning;

    public RuneItem(Properties properties, String unicodeChar, String runeName, String runeMeaning) {
        super(properties, "tooltip.entropica.rune");
        this.unicodeChar = unicodeChar;
        this.runeName = runeName;
        this.runeMeaning = runeMeaning;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.literal(unicodeChar + " - " + runeName)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.accept(Component.literal("Meaning: " + runeMeaning)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    public String getUnicodeChar() {
        return unicodeChar;
    }
}