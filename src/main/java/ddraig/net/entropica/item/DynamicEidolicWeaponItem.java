package ddraig.net.entropica.item;

import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class DynamicEidolicWeaponItem extends Item {

    public DynamicEidolicWeaponItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        tooltipComponents.accept(Component.translatable("tooltip.entropica.dynamic_weapon_desc").withStyle(ChatFormatting.DARK_PURPLE));

        // Read and display the Vis Weapon State
        VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());
        if (state != null) {
            tooltipComponents.accept(Component.empty()); // Spacer
            tooltipComponents.accept(Component.literal("Vis Element: " + state.baseType().getDisplayName())
                    .withStyle(Style.EMPTY.withColor(state.baseType().getColorInt())));

            if (state.isAltered()) {
                tooltipComponents.accept(Component.literal("Active Profile: " + state.activeProfile())
                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            }

            tooltipComponents.accept(Component.literal("Base Vis Damage: " + state.baseDamage())
                    .withStyle(ChatFormatting.DARK_GREEN));

            tooltipComponents.accept(Component.literal("Augment Slots: " + state.slottedFragments().size() + " / " + state.innateSlots())
                    .withStyle(ChatFormatting.GOLD));
        }

        // (We will re-enable EssenceCombatStats here later once we register the ModDataComponent for it!)
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Makes it permanently glow like an enchanted item for the cool factor!
        return true;
    }
}