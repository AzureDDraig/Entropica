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

        SpellAssembly boundForPreview = spell.isBoundToTier() ? spell : spell.bindToPotencyTier(1, flaw);
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(
                boundForPreview,
                apparatusType,
                materialBonus,
                EssenceType.REGULAR
        );

        // Cast Count (Single..Octuple at Contact) + Flow Modifier + Trigger Condition header
        int castCount = Math.clamp(Math.max(spell.subSpells().size(), spell.flowPart().subSpellCount()), 1, 8);
        SpellFlowModifier flow = spell.flowPart().flow();
        SpellTriggerType trigger = spell.flowPart().trigger();
        String castTargetText = trigger.isSelfCentered()
                ? "on Self"
                : (castCount > 1 ? "at Hit Spot" : "");
        String castTitle = SpellFlowModifier.getCastCountTitle(castCount) + (castTargetText.isEmpty() ? "" : " " + castTargetText);
        String payloadInfo = flow.getCarrierPayloadCount() > 1
                ? " (" + flow.getCarrierPayloadCount() + " Payloads)"
                : "";
        String triggerLabel = trigger.getDisplayName();
        if (trigger == SpellTriggerType.ON_TIMER) {
            float sec = spell.flowPart().delaySeconds() >= 0.5F ? spell.flowPart().delaySeconds() : 1.5F;
            triggerLabel += String.format(" (%.1fs)", sec);
        } else if (trigger == SpellTriggerType.SELF_TIMED) {
            float sec = spell.flowPart().delaySeconds() >= 5.0F ? spell.flowPart().delaySeconds() : 30.0F;
            triggerLabel += String.format(" (%.0fs)", sec);
        }
        SpellTravelDistance distStep = spell.flowPart().distanceStep();
        out.accept(Component.literal("Cast: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(castTitle).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" • Flow: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(flow.getDisplayName() + payloadInfo).withStyle(ChatFormatting.AQUA)))
                .append(Component.literal(" • Trigger: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(triggerLabel).withStyle(ChatFormatting.YELLOW)))
                .append(Component.literal(" • Travel: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(distStep.shortLabel()).withStyle(ChatFormatting.GREEN))));

        // Expected Power, Reach/Span & Cooldown summary
        appendPowerAndCooldownSummary(boundForPreview, apparatusType, out);

        out.accept(Component.literal(cost.channeledPerSecond() ? "Channeled Drain: " : "Total Cast Fuel: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(cost.formatTypeCostSummary()).withStyle(ChatFormatting.WHITE)));

        if (!detailedParts) {
            return;
        }

        String unit = MateriaStageTable.getStageUnitCode(cost.potencyTier()) + (cost.channeledPerSecond() ? "/s" : "");
        for (SpellCostBreakdown.PartCostEntry entry : cost.partEntries()) {
            String slotPrefix = entry.subSpellIndex() < 0
                    ? "  Flow: "
                    : "  #" + (entry.subSpellIndex() + 1) + " " + entry.partSlotType().getDisplayName() + ": ";

            String displayNode = entry.partSlotType() == SpellPartSlotType.FLOW
                    ? SpellFlowModifier.fromSerializedOrLegacy(entry.nodeId()).getDisplayName()
                    : formatNodeName(entry.nodeId());
            MutableComponent line = Component.literal(slotPrefix).withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(displayNode).withStyle(ChatFormatting.GRAY))
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

            if (entry.mutationEnabled() && mut.mutated() && mut.mutationDescription() != null && !mut.mutationDescription().isBlank()) {
                out.accept(Component.literal("    ↳ " + mut.mutationDescription()).withStyle(ChatFormatting.GRAY));
            }
        }

        for (String loreLine : spell.customLore()) {
            out.accept(Component.literal("  \"" + loreLine + "\"").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }

    private static void appendPowerAndCooldownSummary(
            SpellAssembly spell,
            @Nullable ApparatusType apparatusType,
            Consumer<Component> out
    ) {
        if (spell.subSpells().isEmpty()) {
            return;
        }
        int tier = spell.getEffectiveTier();
        float appPotency = apparatusType != null ? apparatusType.getPotencyMultiplier() : 1.0F;
        float appCooldown = apparatusType != null ? apparatusType.getCooldownMultiplier() : 1.0F;
        float flawPotency = spell.flawProfile().getPotencyMultiplier();

        SubSpellSlot first = spell.subSpells().getFirst();
        EffectPartBinding prim = first.primaryEffect();
        EssenceType primMat = prim.materiaType();
        SpellMutationProfile primMut = prim.resolveMutation(SpellPartSlotType.CORE_EFFECT, true, EssenceType.REGULAR);
        float mult = (primMut.mutated() ? primMut.potencyMultiplier() : 1.0F) * appPotency * flawPotency;
        if (first.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE && appPotency == 1.0F) {
            mult *= 1.15F;
        }

        SpellTravelDistance distStep = spell.flowPart().distanceStep();
        float appRange = apparatusType != null ? apparatusType.getRangeMultiplier() : 1.0F;
        float flawRange = spell.flawProfile().getRangeAndRadiusMultiplier();
        boolean channeledBeam = spell.isChanneledHoldBeam();
        boolean pureTouch = spell.isPureTouchStrikeSpell();
        String metric = switch (prim.effect().getRootFamily()) {
            case HARM -> channeledBeam
                    ? String.format("Power: %.1f HP/s", MateriaStageTable.getBaseCombatDamageHp(tier) * mult * 1.25F)
                    : String.format("Power: %.1f HP", MateriaStageTable.getBaseCombatDamageHp(tier) * mult);
            case WARD -> String.format("Ward Shield: %.0f HP", MateriaStageTable.getBaseWardShieldHp(tier) * mult);
            case RESTORATION -> channeledBeam
                    ? String.format("Heal: %.1f HP/s", MateriaStageTable.getBaseRestorationHealHp(tier) * mult * 1.25F)
                    : String.format("Heal: %.1f HP", MateriaStageTable.getBaseRestorationHealHp(tier) * mult);
            case BLINK -> prim.subMode().equalsIgnoreCase("swap") || prim.subMode().equalsIgnoreCase("transposition")
                    ? String.format("Blink: %s Position Swap", distStep.shortLabel())
                    : (distStep.isPointBlankZero()
                    ? "Blink: 0m In-Place Step"
                    : String.format("Blink: %.1fm %s",
                            (tier <= 3 ? 16.0F : 16.0F + (tier - 3) * 2.5F) * distStep.getDistanceScaleFromStandard() * appRange * flawRange,
                            tier >= 4 ? "Phase-Step" : "Instant Step"));
            default -> "Shape: " + first.formPart().form().getDisplayName();
        };

        SpellDeliveryForm fm = first.formPart().form();
        SpellMutationProfile formMut = first.formPart().resolveMutation(true, EssenceType.REGULAR);
        boolean earthReach = first.formPart().mutationEnabled()
                && ddraig.net.entropica.spell.mutation.MateriaFamilyTier.isEarthLineage(first.formPart().materiaType());
        String spanMetric = switch (fm) {
            case BEAM -> String.format("Reach: %.1fm",
                    distStep.resolveBaseBeamReachBlocks(tier, spell.prefix() == SpellPrefix.COMBAT, earthReach)
                            * (formMut.mutated() ? formMut.rangeMultiplier() : 1.0F) * appRange * flawRange);
            case PROJECTILE -> distStep.isPointBlankZero()
                    ? "Range: 0m Point-Blank"
                    : String.format("Range: %.0fm", distStep.meters() * appRange * flawRange);
            case TOUCH_STRIKE -> "Next Melee Hit (30s Prime)";
            case RADIAL_BURST, AURA, GLYPH, CONE, SKYFALL ->
                    String.format("Span: %.1fm", SpellDeliveryForm.getCombatRadiusBlocks(fm, tier));
            default -> null;
        };

        float cdSec = Math.max(4, Math.round(spell.getBaseCooldownTicks() * appCooldown)) / 20.0F;
        String cadenceText = channeledBeam
                ? " • Hold R-Click (Channeled)"
                : (pureTouch ? String.format(" • Prime CD: %.1fs", cdSec) : String.format(" • Cooldown: %.1fs", cdSec));
        String summary = metric + (spanMetric != null ? " • " + spanMetric : "") + cadenceText;
        out.accept(Component.literal(summary).withStyle(ChatFormatting.YELLOW));

        EssenceType matchupType = primMat != EssenceType.REGULAR
                ? primMat
                : (first.formPart().materiaType() != EssenceType.REGULAR
                ? first.formPart().materiaType()
                : spell.flowPart().materiaType());
        if (matchupType != null && matchupType != EssenceType.REGULAR) {
            out.accept(Component.literal("Matchup (" + matchupType.getDisplayName() + "): ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ddraig.net.entropica.combat.VisInteractionManager.getInstance().formatMatchupSummary(matchupType))
                            .withStyle(ChatFormatting.AQUA)));
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
