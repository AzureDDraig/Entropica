package ddraig.net.entropica.item;

import ddraig.net.entropica.block.ScribingControllerBlock;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.SpellTooltipFormatter;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SpellFlawProfile;
import ddraig.net.entropica.spell.fuel.SpellCostBreakdown;
import ddraig.net.entropica.spell.fuel.SpellFuelCalculator;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Spell Parchment blueprint inscribed or duplicated at an Arcane Scribing Table (Section 8.2 & 8.3).
 *
 * <p>Right-clicking a Scribing Controller, Arcane Anvil, or Ritual Bowl while holding a Blank Spellgem
 * in the offhand consumes the parchment and binds the Spellgem to a Potency Tier (scanning nearby placed
 * Orbis Cells, Ritual Bowls, and Materia Vessels within 8 blocks, and rolling a {@link SpellFlawProfile} if under-supplied!).</p>
 */
public class SpellParchmentItem extends Item {

    public SpellParchmentItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createBlueprint(SpellAssembly blueprint) {
        ItemStack stack = new ItemStack(ModItems.SPELL_PARCHMENT.get());
        SpellAssembly unbound = new SpellAssembly(
                blueprint.customName(),
                blueprint.customLore(),
                blueprint.materiaSpecific(),
                blueprint.prefix(),
                blueprint.potencyTier() > 0 ? blueprint.potencyTier() : 1,
                blueprint.flowPart(),
                blueprint.subSpells(),
                SpellFlawProfile.PERFECT,
                blueprint.resoniteCritFragments()
        );
        stack.set(ModDataComponents.SPELL_ASSEMBLY.get(), unbound);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        SpellAssembly blueprint = stack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (blueprint != null) {
            return Component.literal("Spell Parchment: " + blueprint.customName())
                    .withStyle(ChatFormatting.GOLD);
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        BlockState clicked = level.getBlockState(pos);
        boolean validAltar = clicked.getBlock() instanceof ScribingControllerBlock
                || clicked.is(ModBlocks.ARCANE_ANVIL.get())
                || clicked.is(ModBlocks.MARBLE_RITUAL_BOWL.get())
                || clicked.is(ModBlocks.BASALT_RITUAL_BOWL.get())
                || clicked.is(ModBlocks.GRANITE_RITUAL_BOWL.get());
        if (!validAltar) {
            return InteractionResult.PASS;
        }

        ItemStack parchment = context.getItemInHand();
        SpellAssembly blueprint = parchment.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (blueprint == null) {
            return InteractionResult.PASS;
        }

        ItemStack offhand = player.getOffhandItem();
        boolean validBlankGem = (offhand.is(ModItems.BLANK_SPELLGEM.get()))
                || (offhand.is(ModItems.SPELL_GEM.get()) && !SpellgemItem.isInscribed(offhand));
        if (!validBlankGem) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("§eHold a Blank Spellgem in your Off-Hand to infuse this Spell Parchment!"),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        int targetTier = Math.clamp(blueprint.potencyTier() > 0 ? blueprint.potencyTier() : 1, 1, 10);
        SpellCostBreakdown ritualCost = SpellFuelCalculator.calculateMagicCircleRitualCost(blueprint, targetTier);

        SpellFuelService.RitualFuelResult ritualFuel = player.isCreative()
                ? new SpellFuelService.RitualFuelResult(1.0D, true, ritualCost.totalMfumByType())
                : SpellFuelService.consumeMagicCircleRitualFuel(serverLevel, pos, targetTier, ritualCost.totalMfumByType(), false);

        SpellFlawProfile rolledFlaw = ritualFuel.fullySupplied()
                ? SpellFlawProfile.PERFECT
                : SpellFlawProfile.rollUnderSuppliedFlaw(serverLevel.random, blueprint.subSpells().size());

        SpellAssembly boundSpell = blueprint.bindToPotencyTier(targetTier, rolledFlaw);
        ItemStack inscribedGem = SpellgemItem.createInscribedGem(boundSpell);

        if (!player.isCreative()) {
            offhand.shrink(1);
            parchment.shrink(1);
        }
        if (!player.getInventory().add(inscribedGem)) {
            player.drop(inscribedGem, false);
        }

        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
                30, 0.5, 0.5, 0.5, 0.2);
        serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F,
                ritualFuel.fullySupplied() ? 1.2F : 0.65F);

        if (ritualFuel.fullySupplied()) {
            player.displayClientMessage(Component.literal("§a[Ritual Infusion Complete] §fBound pristine Tier "
                    + targetTier + " Spellgem: §b" + boundSpell.customName()), true);
        } else {
            player.displayClientMessage(Component.literal(String.format(
                    "§e[Under-Supplied Ritual (%.0f%% Fuel)] §cBound flawed Tier %d Spellgem!",
                    ritualFuel.suppliedFraction() * 100.0D, targetTier
            )), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.SPELL_ASSEMBLY.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        SpellAssembly blueprint = stack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (blueprint == null) {
            tooltipComponents.accept(Component.literal("Blank vellum ready for Arcane Scribing Table design.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltipComponents.accept(Component.literal("Consumed upon Magic Circle Spellgem binding (Duplicate at Arcane Scribing Table)")
                .withStyle(ChatFormatting.YELLOW));
        SpellTooltipFormatter.appendSpellAssemblyTooltip(blueprint, null, 0.0F, true, tooltipComponents);
    }
}
