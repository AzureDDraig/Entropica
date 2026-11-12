package ddraig.net.entropica.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class EquinoxMageArmorItem extends Item {

    private final EquipmentSlot slot;

    public EquinoxMageArmorItem(EquipmentSlot slot, Properties properties) {
        super(properties.stacksTo(1).durability(450).equippable(slot));
        this.slot = slot;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        if (this.slot == EquipmentSlot.HEAD) {
            tooltipComponents.accept(Component.literal("§5[Caprine Wizard Focus]"));
            tooltipComponents.accept(Component.literal("§a+20% Materia Advantage Damage Calculation"));
            tooltipComponents.accept(Component.literal("§7Woven from Equinox Velvet and enchanted by Caprine Sages."));
        } else if (this.slot == EquipmentSlot.CHEST) {
            tooltipComponents.accept(Component.literal("§5[Equinox Duality Robe]"));
            tooltipComponents.accept(Component.literal("§650% Fire & Lava Resistance (Radiant)"));
            tooltipComponents.accept(Component.literal("§b50% Drowning & Frost Resistance (Umbral)"));
            tooltipComponents.accept(Component.literal("§d-20% Materia Spellcasting Cost"));
        }
    }
}
