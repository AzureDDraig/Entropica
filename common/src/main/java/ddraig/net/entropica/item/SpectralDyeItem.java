package ddraig.net.entropica.item;

import ddraig.net.entropica.api.SpectralDyeApi;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class SpectralDyeItem extends Item {
    private final String dyeTypeId;

    public SpectralDyeItem(Properties properties, String dyeTypeId) {
        super(properties);
        this.dyeTypeId = dyeTypeId;
    }

    public String getDyeTypeId() {
        return dyeTypeId;
    }

    public SpectralDyeApi.DyeColor getDyeColor() {
        return SpectralDyeApi.getColor(dyeTypeId);
    }

    public int getColor() {
        return SpectralDyeApi.getRgb(dyeTypeId);
    }

    @Override
    public Component getName(ItemStack stack) {
        String langKey = "item.entropica.spectral_dye_" + dyeTypeId;
        if (Component.translatable(langKey).getString().equals(langKey)) {
            return Component.literal(getDyeColor().displayName() + " Spectral Dye");
        }
        return Component.translatable(langKey);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipConsumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipConsumer, tooltipFlag);
        String tooltipKey = "tooltip.entropica.spectral_dye_" + dyeTypeId;
        if (!Component.translatable(tooltipKey).getString().equals(tooltipKey)) {
            tooltipConsumer.accept(Component.translatable(tooltipKey));
        } else {
            tooltipConsumer.accept(Component.literal("Refined bioluminescent dye (" + getDyeColor().hexCode() + ")"));
        }
    }
}
