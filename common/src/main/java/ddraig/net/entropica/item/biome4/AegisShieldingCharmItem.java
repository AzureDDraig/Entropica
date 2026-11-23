package ddraig.net.entropica.item.biome4;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Aegis Shielding Charm — fragile crystalline relic recovered from the Aegis Shielding Monolith.
 * Shatters upon taking a fatal blow anywhere in hands or inventory, restoring 2 hearts and
 * wrapping the bearer in Absorption II and Resistance II.
 */
public class AegisShieldingCharmItem extends Item {

    public AegisShieldingCharmItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    /**
     * Searches {@code player}'s mainhand, offhand, and inventory for an Aegis Shielding Charm,
     * consuming one if found.
     */
    public static boolean tryConsumeCharm(Player player) {
        if (player == null || !ModItems.AEGIS_SHIELDING_CHARM.isBound()) {
            return false;
        }
        Item charm = ModItems.AEGIS_SHIELDING_CHARM.get();

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(charm)) {
            mainHand.shrink(1);
            return true;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(charm)) {
            offHand.shrink(1);
            return true;
        }

        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack slotStack = inv.getItem(i);
            if (slotStack.is(charm)) {
                slotStack.shrink(1);
                return true;
            }
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Shatters to absorb one fatal blow and grant Absorption II + Resistance II.")
                .withStyle(ChatFormatting.AQUA));
    }
}
