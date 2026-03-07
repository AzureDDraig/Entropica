package ddraig.net.entropica.event;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "entropica")
public class VisTooltipEventHandler {

    private static final String[] RUNES = {"ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ", "ᚺ", "ᚾ", "ᛁ", "ᛃ", "ᛇ", "ᛈ", "ᛉ", "ᛊ", "ᛏ", "ᛒ", "ᛖ", "ᛗ", "ᛚ", "ᛜ", "ᛞ", "ᛟ"};

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        VisWeaponState visState = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());

        if (visState != null && visState.baseDamage() > 0) {
            List<Component> tooltip = event.getToolTip();
            List<Component> linesToInject = new ArrayList<>();

            // 1. Format the Damage Line with EXACT RGB Values
            String alphaSymbol = visState.isAltered() ? " α" : "";
            String rune = getRuneForType(visState.baseType());

            // Text: " +6.0 ᚠ Soulfire α Damage"
            String damageLineText = " +" + String.format("%.1f", visState.baseDamage()) +
                    " " + rune + " " + visState.baseType().getDisplayName() + alphaSymbol + " Damage";

            // Apply the exact RGB integer from the EssenceType to the Component's style
            Component damageLineComponent = Component.literal(damageLineText)
                    .withStyle(Style.EMPTY.withColor(visState.baseType().getColorInt()));

            linesToInject.add(damageLineComponent);

            // 2. Format the Augment Slots
            int maxSlots = visState.innateSlots();
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                if (entry.getKey().is(ModEnchantments.AUGMENT_SLOT_EXPANSION)) {
                    maxSlots += entry.getIntValue(); // Adds +1 per level limit
                }
            }

            if (maxSlots > 0) {
                for (int i = 0; i < maxSlots; i++) {
                    if (i < visState.slottedFragments().size()) {
                        EssenceType slotted = visState.slottedFragments().get(i);
                        String slotRune = getRuneForType(slotted);

                        String slotText = "  [" + slotRune + "] " + slotted.getDisplayName() + " Augment";
                        Component slotComponent = Component.literal(slotText)
                                .withStyle(Style.EMPTY.withColor(slotted.getColorInt()));

                        linesToInject.add(slotComponent);
                    } else {
                        linesToInject.add(Component.literal("  [ Empty Augment Slot ]").withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }

            // 3. Find the exact insertion point (Immediately after "When in Main Hand:")
            // Because we stripped vanilla Attack Damage, it won't exist in the list anymore!
            int insertIndex = -1;
            for (int i = 0; i < tooltip.size(); i++) {
                if (containsTranslationKey(tooltip.get(i), "item.modifiers.mainhand")) {
                    insertIndex = i + 1; // Insert on the line immediately following the header
                    break;
                }
            }

            // 4. Inject into the Tooltip list
            if (insertIndex != -1) {
                tooltip.addAll(insertIndex, linesToInject);
            } else {
                // Fallback: Append at the bottom if attributes are completely hidden by the player
                tooltip.add(Component.literal(" "));
                tooltip.addAll(linesToInject);
            }
        }
    }

    /**
     * Deep-scans a Component tree to check if it contains a specific Translation key.
     */
    private static boolean containsTranslationKey(Component component, String keyToFind) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            if (translatable.getKey().equals(keyToFind)) {
                return true;
            }
            // Minecraft nests the Attribute name as an *argument* inside the translation block.
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component argComponent) {
                    if (containsTranslationKey(argComponent, keyToFind)) {
                        return true;
                    }
                }
            }
        }

        // Scan siblings
        for (Component sibling : component.getSiblings()) {
            if (containsTranslationKey(sibling, keyToFind)) {
                return true;
            }
        }
        return false;
    }

    private static String getRuneForType(EssenceType type) {
        return RUNES[type.ordinal() % RUNES.length];
    }
}