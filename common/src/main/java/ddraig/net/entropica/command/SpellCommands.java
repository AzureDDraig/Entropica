package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.CastingApparatusItem;
import ddraig.net.entropica.item.DynamicVisWeaponItem;
import ddraig.net.entropica.item.SpellParchmentItem;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellPrefix;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.fuel.SpellCostBreakdown;
import ddraig.net.entropica.spell.fuel.SpellFuelCalculator;
import ddraig.net.entropica.spell.mutation.SpellMutationUnlockMode;
import ddraig.net.entropica.spell.research.SpellResearchManager;
import ddraig.net.entropica.spell.test.SpellEngineVerificationSuite;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Brigadier operator commands under {@code /entropica spell ...} for crafting multi-Materia
 * Spellgems/Parchments with independent per-part mutation toggles, socketing apparatuses,
 * inspecting per-part fuel billing, managing Codex spell research, and running the verification suite.
 */
public final class SpellCommands {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREFIX = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SpellPrefix.values()).map(SpellPrefix::getSerializedName), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_FLOW = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SpellFlowModifier.values()).map(SpellFlowModifier::getSerializedName), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_FORM = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SpellDeliveryForm.values()).map(SpellDeliveryForm::getSerializedName), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_EFFECT = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SpellCoreEffect.values()).map(SpellCoreEffect::getSerializedName), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MATERIA = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(EssenceType.values())
                            .filter(t -> !t.isFragment())
                            .map(EssenceType::getSerializedName),
                    builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PARTS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(List.of("flow", "form", "primary", "secondary"), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SOCKET_MODE = (ctx, builder) ->
            SharedSuggestionProvider.suggest(List.of("workstation", "field"), builder);

    private SpellCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entropica")
                .then(Commands.literal("spell")
                        .requires(source -> source.hasPermission(2))

                        // 1. /entropica spell craft <prefix> <tier> <flow> <flowMateria> <flowMutated> <form> <formMateria> <formMutated> <effect> <effectMateria> <effectMutated> [secEffect] [secMateria] [secMutated]
                        .then(Commands.literal("craft")
                                .then(buildAssemblyArgTree(false)))

                        // 2. /entropica spell parchment ...
                        .then(Commands.literal("parchment")
                                .then(buildAssemblyArgTree(true)))

                        // 3. /entropica spell toggle_mutation <part> <enabled> [slotIndex]
                        .then(Commands.literal("toggle_mutation")
                                .then(Commands.argument("part", StringArgumentType.word())
                                        .suggests(SUGGEST_PARTS)
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(ctx -> toggleMutation(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "part"),
                                                        BoolArgumentType.getBool(ctx, "enabled"),
                                                        0))
                                                .then(Commands.argument("slotIndex", IntegerArgumentType.integer(0, 7))
                                                        .executes(ctx -> toggleMutation(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "part"),
                                                                BoolArgumentType.getBool(ctx, "enabled"),
                                                                IntegerArgumentType.getInteger(ctx, "slotIndex")))))))

                        // 4. /entropica spell socket <workstation|field> [slotIndex]
                        .then(Commands.literal("socket")
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests(SUGGEST_SOCKET_MODE)
                                        .executes(ctx -> socketOffhandGem(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "mode"),
                                                -1))
                                        .then(Commands.argument("slotIndex", IntegerArgumentType.integer(0, 8))
                                                .executes(ctx -> socketOffhandGem(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "mode"),
                                                        IntegerArgumentType.getInteger(ctx, "slotIndex"))))))

                        // 5. /entropica spell inspect
                        .then(Commands.literal("inspect")
                                .executes(ctx -> inspectHeldSpell(ctx.getSource())))

                        // 6. /entropica spell research <unlock_all|status|mode>
                        .then(Commands.literal("research")
                                .then(Commands.literal("unlock_all")
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            SpellResearchManager.unlockAllSpellResearch(player);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§6[Spell Research] §aUnlocked all Spell Flow, Form, Core Effect nodes and Materia types for §e"
                                                            + player.getName().getString()), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("status")
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            SpellMutationUnlockMode mode = SpellResearchManager.getActiveUnlockMode();
                                            int unlockedCount = SpellResearchManager.getUnlockedNodes(player).size();
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§6[Spell Research Status] §fPlayer: §e" + player.getName().getString()
                                                            + "\n  §7Active Mutation Config Mode: §b" + mode.getDisplayName() + " (Mode " + mode.getId() + ")"
                                                            + "\n  §7Unlocked Spell Nodes: §a" + unlockedCount), false);
                                            return 1;
                                        })))

                        // 7. /entropica spell verify
                        .then(Commands.literal("verify")
                                .executes(ctx -> runVerificationSuite(ctx.getSource())))
                )
        );
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> buildAssemblyArgTree(boolean asParchment) {
        return Commands.argument("prefix", StringArgumentType.word()).suggests(SUGGEST_PREFIX)
                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10))
                        .then(Commands.argument("flow", StringArgumentType.word()).suggests(SUGGEST_FLOW)
                                .then(Commands.argument("flowMateria", StringArgumentType.word()).suggests(SUGGEST_MATERIA)
                                        .then(Commands.argument("flowMutated", BoolArgumentType.bool())
                                                .then(Commands.argument("form", StringArgumentType.word()).suggests(SUGGEST_FORM)
                                                        .then(Commands.argument("formMateria", StringArgumentType.word()).suggests(SUGGEST_MATERIA)
                                                                .then(Commands.argument("formMutated", BoolArgumentType.bool())
                                                                        .then(Commands.argument("effect", StringArgumentType.word()).suggests(SUGGEST_EFFECT)
                                                                                .then(Commands.argument("effectMateria", StringArgumentType.word()).suggests(SUGGEST_MATERIA)
                                                                                        .then(Commands.argument("effectMutated", BoolArgumentType.bool())
                                                                                                .executes(ctx -> executeCraftSpell(ctx, asParchment, false))
                                                                                                .then(Commands.argument("secEffect", StringArgumentType.word()).suggests(SUGGEST_EFFECT)
                                                                                                        .then(Commands.argument("secMateria", StringArgumentType.word()).suggests(SUGGEST_MATERIA)
                                                                                                                .then(Commands.argument("secMutated", BoolArgumentType.bool())
                                                                                                                        .executes(ctx -> executeCraftSpell(ctx, asParchment, true)))))))))))))));
    }

    private static int executeCraftSpell(CommandContext<CommandSourceStack> ctx, boolean asParchment, boolean withSecondary)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        SpellPrefix prefix = parsePrefix(StringArgumentType.getString(ctx, "prefix"));
        int tier = IntegerArgumentType.getInteger(ctx, "tier");
        SpellFlowModifier flow = parseFlow(StringArgumentType.getString(ctx, "flow"));
        EssenceType flowMat = parseMateria(StringArgumentType.getString(ctx, "flowMateria"));
        boolean flowMut = BoolArgumentType.getBool(ctx, "flowMutated");

        SpellDeliveryForm form = parseForm(StringArgumentType.getString(ctx, "form"));
        EssenceType formMat = parseMateria(StringArgumentType.getString(ctx, "formMateria"));
        boolean formMut = BoolArgumentType.getBool(ctx, "formMutated");

        SpellCoreEffect prim = parseEffect(StringArgumentType.getString(ctx, "effect"));
        EssenceType primMat = parseMateria(StringArgumentType.getString(ctx, "effectMateria"));
        boolean primMut = BoolArgumentType.getBool(ctx, "effectMutated");

        if (prefix == null || flow == null || flowMat == null || form == null || formMat == null || prim == null || primMat == null) {
            source.sendFailure(Component.literal("§cInvalid spell prefix, part node, or Materia type! Use tab-completion."));
            return 0;
        }

        FormPartBinding formBinding = FormPartBinding.of(form, formMat, formMut);
        EffectPartBinding primBinding = EffectPartBinding.of(prim, primMat, primMut);
        SubSpellSlot slot;

        if (withSecondary) {
            SpellCoreEffect sec = parseEffect(StringArgumentType.getString(ctx, "secEffect"));
            EssenceType secMat = parseMateria(StringArgumentType.getString(ctx, "secMateria"));
            boolean secMut = BoolArgumentType.getBool(ctx, "secMutated");
            if (sec == null || secMat == null) {
                source.sendFailure(Component.literal("§cInvalid secondary Core Effect or Materia type!"));
                return 0;
            }
            slot = SubSpellSlot.ofDualVerb(formBinding, primBinding, EffectPartBinding.of(sec, secMat, secMut));
        } else {
            slot = SubSpellSlot.of(formBinding, primBinding);
        }

        boolean hasSpecific = flowMat != EssenceType.REGULAR
                || formMat != EssenceType.REGULAR
                || primMat != EssenceType.REGULAR
                || slot.secondaryEffect().map(s -> s.materiaType() != EssenceType.REGULAR).orElse(false);

        String generatedName = (primMat != EssenceType.REGULAR ? primMat.getDisplayName() + " " : "")
                + prim.getDisplayName() + " " + form.getDisplayName();

        int slotCopies = flow.getDefaultSubSpellCount();
        List<SubSpellSlot> slots = new ArrayList<>(slotCopies);
        for (int i = 0; i < slotCopies; i++) {
            slots.add(slot);
        }

        SpellAssembly assembly = new SpellAssembly(
                generatedName,
                List.of("Crafted via /entropica spell"),
                hasSpecific,
                prefix,
                tier,
                new FlowPartBinding(flow, flowMat, flowMut, slotCopies, flow == SpellFlowModifier.DELAYED_BLOOM ? 3.0F : 0.0F),
                slots,
                SpellFlawProfile.PERFECT,
                0
        );

        ItemStack created = asParchment
                ? SpellParchmentItem.createBlueprint(assembly)
                : SpellgemItem.createInscribedGem(assembly);

        if (!player.getInventory().add(created)) {
            player.drop(created, false);
        }

        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(assembly, null, 0.0F, EssenceType.REGULAR);
        source.sendSuccess(() -> Component.literal("§6[Spellcrafting] §aCreated "
                + (asParchment ? "Spell Parchment" : "Tier " + tier + " Spellgem")
                + ": §b" + assembly.customName()
                + " §7(Cast Cost: §e" + cost.formatTypeCostSummary() + "§7)"), true);
        return 1;
    }

    private static int toggleMutation(CommandSourceStack source, String partName, boolean enabled, int slotIdx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();

        SpellAssembly current = held.get(ModDataComponents.SPELL_ASSEMBLY.get());
        CastingApparatusState appState = held.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        boolean insideApparatus = false;

        if (current == null && appState != null && appState.getActiveSpell().isPresent()) {
            current = appState.getActiveSpell().get();
            insideApparatus = true;
        }

        if (current == null) {
            source.sendFailure(Component.literal("§cHold an Inscribed Spellgem, Spell Parchment, or socketed Casting Apparatus in your Main Hand!"));
            return 0;
        }

        String normalizedPart = partName.toLowerCase(Locale.ROOT);
        FlowPartBinding newFlow = current.flowPart();
        List<SubSpellSlot> newSlots = new ArrayList<>(current.subSpells());

        if ("flow".equals(normalizedPart)) {
            newFlow = newFlow.withMutationEnabled(enabled);
        } else {
            if (slotIdx < 0 || slotIdx >= newSlots.size()) {
                source.sendFailure(Component.literal("§cSub-spell slot index " + slotIdx + " out of bounds (0.." + (newSlots.size() - 1) + ")!"));
                return 0;
            }
            SubSpellSlot targetSlot = newSlots.get(slotIdx);
            switch (normalizedPart) {
                case "form" -> newSlots.set(slotIdx, new SubSpellSlot(
                        targetSlot.formPart().withMutationEnabled(enabled),
                        targetSlot.primaryEffect(),
                        targetSlot.secondaryEffect()));
                case "primary" -> newSlots.set(slotIdx, new SubSpellSlot(
                        targetSlot.formPart(),
                        targetSlot.primaryEffect().withMutationEnabled(enabled),
                        targetSlot.secondaryEffect()));
                case "secondary" -> {
                    if (targetSlot.secondaryEffect().isEmpty()) {
                        source.sendFailure(Component.literal("§cSub-spell slot #" + (slotIdx + 1) + " does not have a Dual-Verb Secondary Effect!"));
                        return 0;
                    }
                    newSlots.set(slotIdx, new SubSpellSlot(
                            targetSlot.formPart(),
                            targetSlot.primaryEffect(),
                            targetSlot.secondaryEffect().map(s -> s.withMutationEnabled(enabled))));
                }
                default -> {
                    source.sendFailure(Component.literal("§cUnknown part '" + partName + "'. Valid: flow, form, primary, secondary."));
                    return 0;
                }
            }
        }

        SpellAssembly updated = new SpellAssembly(
                current.customName(),
                current.customLore(),
                current.materiaSpecific(),
                current.prefix(),
                current.potencyTier(),
                newFlow,
                newSlots,
                current.flawProfile(),
                current.resoniteCritFragments()
        );

        if (insideApparatus) {
            SocketedSpellgem oldSock = appState.getActiveSocket();
            SocketedSpellgem newSock = new SocketedSpellgem(
                    java.util.Optional.of(updated),
                    oldSock.lockoutUntilGameTime(),
                    oldSock.pendingFieldBackfireRoll()
            );
            held.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), appState.withSocket(appState.activeSlotIndex(), newSock));
        } else {
            held.set(ModDataComponents.SPELL_ASSEMBLY.get(), updated);
        }

        source.sendSuccess(() -> Component.literal("§6[Spellcrafting] §fSet §e" + normalizedPart
                + " §fSignature Mutation to " + (enabled ? "§aON" : "§7OFF (Baseline)")
                + " §fon §b" + updated.customName()), true);
        return 1;
    }

    private static int socketOffhandGem(CommandSourceStack source, String modeStr, int explicitSlot)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        if (!SpellgemItem.isInscribed(off)) {
            source.sendFailure(Component.literal("§cHold an Inscribed Spellgem in your Off-Hand to socket!"));
            return 0;
        }
        SpellAssembly spell = off.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (spell == null) return 0;

        CastingApparatusState state = main.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (state == null) {
            if (main.getItem() instanceof CastingApparatusItem appItem) {
                state = CastingApparatusState.emptyFor(appItem.getApparatusType());
            } else if (main.getItem() instanceof DynamicVisWeaponItem) {
                state = CastingApparatusState.emptyFor(ApparatusType.ENGRAVED_TOOL);
            } else {
                source.sendFailure(Component.literal("§cHold a Casting Apparatus (Focus, Wand, Staff, Grimoire) or Dynamic Materia Weapon in your Main Hand!"));
                return 0;
            }
        }

        int slotIdx = explicitSlot >= 0 ? explicitSlot : state.activeSlotIndex();
        int cap = state.apparatusType().getGemCapacity();
        if (slotIdx < 0 || slotIdx >= cap) {
            source.sendFailure(Component.literal("§cSlot index " + slotIdx + " exceeds " + state.apparatusType().getDisplayName() + " capacity (" + cap + ")!"));
            return 0;
        }

        boolean fieldSwap = "field".equalsIgnoreCase(modeStr);
        SocketedSpellgem socketed = fieldSwap
                ? SocketedSpellgem.socketInField(spell, player.level().getGameTime())
                : SocketedSpellgem.socketAtWorkstation(spell);

        SocketedSpellgem prev = state.sockets().get(slotIdx);
        if (!prev.isEmpty() && prev.spell().isPresent()) {
            ItemStack returned = SpellgemItem.createInscribedGem(prev.spell().get());
            if (!player.getInventory().add(returned)) {
                player.drop(returned, false);
            }
        }

        if (!player.isCreative()) {
            off.shrink(1);
        }
        final CastingApparatusState finalState = state;
        main.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), finalState.withSocket(slotIdx, socketed).withActiveSlot(slotIdx));

        source.sendSuccess(() -> Component.literal("§6[Apparatus Socket] §aSocketed §b" + spell.customName()
                + " §ainto §e" + finalState.apparatusType().getDisplayName()
                + " §7[Slot " + (slotIdx + 1) + "/" + cap + "] ("
                + (fieldSwap ? "§cField Swap: 60s Lockout + 45% First-Cast Backfire" : "§aSafe Workstation Swap: 0s Lockout") + "§7)"), true);
        return 1;
    }

    private static int inspectHeldSpell(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();

        SpellAssembly spell = held.get(ModDataComponents.SPELL_ASSEMBLY.get());
        CastingApparatusState appState = held.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        ApparatusType appType = null;
        float matBonus = 0.0F;

        if (spell == null && appState != null && appState.getActiveSpell().isPresent()) {
            spell = appState.getActiveSpell().get();
            appType = appState.apparatusType();
            matBonus = appState.materialEfficiencyBonus();
        }

        if (spell == null) {
            source.sendFailure(Component.literal("§cNo inscribed SpellAssembly found on Main Hand item!"));
            return 0;
        }

        SpellCostBreakdown breakdown = SpellFuelCalculator.calculateCastCost(spell, appType, matBonus, EssenceType.REGULAR);
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Spell Inspection: §b").append(spell.customName())
                .append(" §7(T").append(spell.getEffectiveTier()).append(" ").append(spell.prefix().getDisplayName()).append(") §6===\n");
        sb.append("  §7Materia-Specific Lock (-15% Fuel): ").append(spell.materiaSpecific() ? "§aYES" : "§eNO (Universal)").append('\n');

        for (SpellCostBreakdown.PartCostEntry entry : breakdown.partEntries()) {
            String slotTag = entry.subSpellIndex() < 0 ? "Global" : "Sub #" + (entry.subSpellIndex() + 1);
            String mutTag = entry.mutationEnabled()
                    ? "§dMUTATED §7(" + entry.resolvedMutation().mutationName() + ")"
                    : "§8UNMUTATED (Baseline)";
            sb.append("  §f• [").append(slotTag).append(" ")
                    .append(entry.partSlotType().getDisplayName()).append("] §b")
                    .append(entry.nodeId()).append(" §7| Materia: §e")
                    .append(entry.materiaType().getDisplayName())
                    .append(" §7| ").append(mutTag)
                    .append(" §7| Cost: §6").append(entry.mfumCost()).append(" Mfum\n");
        }
        sb.append("  §aTotal Per-Materia Cost: §e").append(breakdown.formatTypeCostSummary());

        String out = sb.toString();
        source.sendSuccess(() -> Component.literal(out), false);
        return 1;
    }

    private static int runVerificationSuite(CommandSourceStack source) {
        SpellEngineVerificationSuite.VerificationReport report = SpellEngineVerificationSuite.runAllChecks();
        source.sendSuccess(() -> Component.literal("§6=== Spellcrafting Engine Verification Suite ==="), false);
        for (String line : report.lines()) {
            boolean pass = line.startsWith("PASS:");
            source.sendSuccess(() -> Component.literal((pass ? "  §a✔ " : "  §c✘ ") + line), false);
        }
        source.sendSuccess(() -> Component.literal(
                (report.allPassed() ? "§a[ALL PASSED] " : "§c[FAILURES DETECTED] ")
                        + "§fPassed: §a" + report.passed() + " §7| Failed: §c" + report.failed()), false);
        return report.allPassed() ? 1 : 0;
    }

    private static SpellPrefix parsePrefix(String raw) {
        for (SpellPrefix p : SpellPrefix.values()) {
            if (p.getSerializedName().equalsIgnoreCase(raw) || p.name().equalsIgnoreCase(raw)) return p;
        }
        return null;
    }

    private static SpellFlowModifier parseFlow(String raw) {
        for (SpellFlowModifier f : SpellFlowModifier.values()) {
            if (f.getSerializedName().equalsIgnoreCase(raw) || f.name().equalsIgnoreCase(raw)) return f;
        }
        return null;
    }

    private static SpellDeliveryForm parseForm(String raw) {
        if (raw == null) return null;
        for (SpellDeliveryForm f : SpellDeliveryForm.values()) {
            if (f.getSerializedName().equalsIgnoreCase(raw) || f.name().equalsIgnoreCase(raw)) return f;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "summon", "agent" -> SpellDeliveryForm.SUMMON_AGENT;
            case "sentry", "turret" -> SpellDeliveryForm.SUMMON_SENTRY;
            case "decoy", "effigy" -> SpellDeliveryForm.SUMMON_DECOY;
            default -> null;
        };
    }

    private static SpellCoreEffect parseEffect(String raw) {
        for (SpellCoreEffect e : SpellCoreEffect.values()) {
            if (e.getSerializedName().equalsIgnoreCase(raw) || e.name().equalsIgnoreCase(raw)) return e;
        }
        return null;
    }

    private static EssenceType parseMateria(String raw) {
        for (EssenceType t : EssenceType.values()) {
            if (!t.isFragment() && (t.getSerializedName().equalsIgnoreCase(raw) || t.name().equalsIgnoreCase(raw))) {
                return t;
            }
        }
        return null;
    }
}
