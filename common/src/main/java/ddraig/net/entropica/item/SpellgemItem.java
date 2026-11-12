package ddraig.net.entropica.item;

import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.SpellTooltipFormatter;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Physical Spellgem item.
 *
 * <p>Can exist as a Blank Spellgem (no {@link SpellAssembly} component) or an Inscribed Spellgem
 * bound to a locked Potency Tier (T1-T10). Per Section 8.5.1, raw Spellgems can NEVER be cast
 * bare-handed—they must be socketed into a Casting Apparatus or Engraved Weapon/Tool.</p>
 */
public class SpellgemItem extends Item {

    public SpellgemItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    public static ItemStack createInscribedGem(SpellAssembly assembly) {
        ItemStack stack = new ItemStack(ModItems.SPELL_GEM.get());
        stack.set(ModDataComponents.SPELL_ASSEMBLY.get(), assembly);
        return stack;
    }

    public static boolean isInscribed(ItemStack stack) {
        SpellAssembly spell = stack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        return spell != null && spell.isBoundToTier();
    }

    public static java.util.Optional<SpellAssembly> getSpell(ItemStack stack) {
        SpellAssembly spell = stack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        return (spell != null && spell.isBoundToTier()) ? java.util.Optional.of(spell) : java.util.Optional.empty();
    }

    @Override
    public Component getName(ItemStack stack) {
        SpellAssembly spell = stack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (spell != null && spell.isBoundToTier()) {
            return Component.literal("Spellgem: " + spell.customName())
                    .withStyle(ChatFormatting.AQUA);
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isInscribed(stack)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("§cRaw Spellgems cannot be cast bare-handed! §7Socket into an Arcanum Focus, Wand, Staff, Grimoire, or Engraved Weapon."),
                        true
                );
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 0.75F);
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isInscribed(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        SpellAssembly spell = stack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (spell == null || !spell.isBoundToTier()) {
            tooltipComponents.accept(Component.literal("Blank crystalline spell core ready for Magic Circle infusion.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        SpellTooltipFormatter.appendSpellAssemblyTooltip(spell, null, 0.0F, true, tooltipComponents);
        tooltipComponents.accept(Component.literal("Right-click onto a Casting Apparatus to socket.")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
