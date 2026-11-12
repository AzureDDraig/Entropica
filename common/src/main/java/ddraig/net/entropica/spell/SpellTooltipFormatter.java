package ddraig.net.entropica.spell;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellCostBreakdown;
import ddraig.net.entropica.spell.fuel.SpellFuelCalculator;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Shared tooltip renderer for Inscribed Spellgems, Spell Parchments, and Casting Apparatuses,
 * displaying every part's independent Materia type, Mutation ON/OFF state, and per-type fuel cost.
 */
public final class SpellTooltipFormatter {

    private SpellTooltipFormatter() {
    }

    public static void appendSpellAssemblyTooltip(
            SpellAssembly spell,
            @Nullable ApparatusType apparatusType,
            float materialBonus,
            boolean detailedParts,
            Consumer<Component> out
    ) {
        // Header: Prefix + Tier + Binding Mode
        String tierText = spell.isBoundToTier()
                ? "Tier " + spell.getEffectiveTier() + " (" + MateriaStageTable.getStageUnitCode(spell.getEffectiveTier()) + ")"
                : "Unbound Blueprint";
        String modeText = spell.materiaSpecific() ? "Materia-Locked (-15% Cost)" : "Dynamic Fuel";

        out.accept(Component.literal("[" + spell.prefix().getDisplayName() + "] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(tierText + " • " + modeText).withStyle(ChatFormatting.AQUA)));

        // Flaw profile warning if under-supplied during ritual binding
        SpellFlawProfile flaw = spell.flawProfile();
        if (flaw.isCompoundDefect()) {
            out.accept(Component.literal("⚠ Flawed Gem: Compound Defect (25% Backfire & -50% Potency/Range)")
                    .withStyle(ChatFormatting.RED));
        } else if (flaw.unstableCasting()) {
            out.accept(Component.literal("⚠ Flawed Gem: Unstable Casting (25% Backfire/Fizzle)")
                    .withStyle(ChatFormatting.RED));
        } else if (flaw.crippledPotency()) {
            out.accept(Component.literal("⚠ Flawed Gem: Crippled Potency (-50% Damage & Span)")
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (spell.getCriticalStrikeChance() > 0.0F) {
            out.accept(Component.literal(String.format("✦ Resonite Resonance: %.0f%% Spell Crit (+75%% Dmg)",
                    spell.getCriticalStrikeChance() * 100.0F)).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(
                spell.isBoundToTier() ? spell : spell.bindToPotencyTier(1, flaw),
                apparatusType,
                materialBonus,
                EssenceType.REGULAR
        );

        out.accept(Component.literal("Total Cast Fuel: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(cost.formatTypeCostSummary()).withStyle(ChatFormatting.WHITE)));

        if (!detailedParts) {
            return;
        }

        String unit = MateriaStageTable.getStageUnitCode(cost.potencyTier());
        for (SpellCostBreakdown.PartCostEntry entry : cost.partEntries()) {
            String slotPrefix = entry.subSpellIndex() < 0
                    ? "  Flow: "
                    : "  #" + (entry.subSpellIndex() + 1) + " " + entry.partSlotType().getDisplayName() + ": ";

            MutableComponent line = Component.literal(slotPrefix).withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(formatNodeName(entry.nodeId())).withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
                    .append(coloredMateria(entry.materiaType()))
                    .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));

            SpellMutationProfile mut = entry.resolvedMutation();
            if (entry.mutationEnabled() && mut.mutated()) {
                line.append(Component.literal("Mutated: " + mut.mutationTitle()).withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                line.append(Component.literal("Mutation OFF").withStyle(ChatFormatting.DARK_GRAY));
            }

            String num = Math.abs(entry.nativeTierUnits() - Math.rint(entry.nativeTierUnits())) < 1e-4
                    ? String.valueOf(Math.round(entry.nativeTierUnits()))
                    : String.format("%.2f", entry.nativeTierUnits());
            line.append(Component.literal(" | " + num + " " + unit + "]").withStyle(ChatFormatting.GRAY));
            out.accept(line);
        }

        for (String loreLine : spell.customLore()) {
            out.accept(Component.literal("  \"" + loreLine + "\"").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }

    private static Component coloredMateria(EssenceType type) {
        if (type == null || type == EssenceType.REGULAR) {
            return Component.literal("Any Materia").withStyle(ChatFormatting.WHITE);
        }
        return Component.literal(type.getDisplayName()).withStyle(Style.EMPTY.withColor(type.getTextColorInt()));
    }

    private static String formatNodeName(String raw) {
        if (raw == null || raw.isEmpty()) return "Unknown";
        String[] parts = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    /**
     * Appends Engraved Spellgem socket and Alloy Tempering tooltip lines onto vanilla/modded weapons & tools
     * carrying {@code ModDataComponents.CASTING_APPARATUS_STATE}.
     */
    public static void appendEngravedEquipmentTooltip(net.minecraft.world.item.ItemStack stack, java.util.List<Component> lines) {
        if (stack.isEmpty()
                || stack.getItem() instanceof ddraig.net.entropica.item.CastingApparatusItem
                || stack.getItem() instanceof ddraig.net.entropica.item.DynamicVisWeaponItem) {
            return;
        }
        ddraig.net.entropica.spell.apparatus.CastingApparatusState state =
                stack.get(ddraig.net.entropica.registry.ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (state == null) {
            return;
        }
        lines.add(Component.empty());
        if (state.materialEfficiencyBonus() > 0.001F) {
            lines.add(Component.literal(String.format("✦ Alloy Tempered: -%.0f%% Materia Cast Cost",
                    state.materialEfficiencyBonus() * 100.0F)).withStyle(ChatFormatting.GOLD));
        }
        ddraig.net.entropica.spell.apparatus.SocketedSpellgem active = state.getActiveSocket();
        if (active.isEmpty() || active.spell().isEmpty()) {
            lines.add(Component.literal("✦ Runic Socket: [Empty — Socket Spellgem at Runic Anvil]")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            SpellAssembly spell = active.spell().get();
            lines.add(Component.literal("✦ Engraved Spell: " + spell.customName() + " (Right-Click to Cast)")
                    .withStyle(ChatFormatting.AQUA));
            appendSpellAssemblyTooltip(spell, state.apparatusType(), state.materialEfficiencyBonus(), false, lines::add);
        }
    }
}
