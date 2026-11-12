package ddraig.net.entropica.spell.research;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.OrbisCellItem;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import ddraig.net.entropica.spell.mutation.SpellMutationUnlockMode;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Manages the Entropic Codex Two-Step Spell Research Discovery Loop
 * ({@code Cryptic -> Observed -> Unlocked}) and the 3-Mode Signature Mutation
 * Unlock System ({@code PER_COMBO}, {@code AUTO_BY_NODE}, {@code BY_FAMILY_TIER}).
 */
public final class SpellResearchManager {

    public static final String TAG_OBSERVED_NODES = "ObservedSpellNodes";
    public static final String TAG_UNLOCKED_NODES = "UnlockedSpellNodes";
    public static final String TAG_OBSERVED_MUTATIONS = "ObservedSpellMutations";
    public static final String TAG_UNLOCKED_MUTATIONS = "UnlockedSpellMutations";
    public static final String TAG_KNOWN_MATERIA = "KnownSpellMateriaTypes";

    private static final Set<String> STARTER_NODES = Set.of(
            nodeId(SpellFlowModifier.SINGLE),
            nodeId(SpellFlowModifier.DOUBLE_BIFURCATE),
            nodeId(SpellFlowModifier.SELF_CAST),
            nodeId(SpellDeliveryForm.PROJECTILE),
            nodeId(SpellDeliveryForm.TOUCH_STRIKE),
            nodeId(SpellDeliveryForm.RADIAL_BURST),
            nodeId(SpellCoreEffect.HARM),
            nodeId(SpellCoreEffect.KINETIC),
            nodeId(SpellCoreEffect.WARD),
            nodeId(SpellCoreEffect.BREAK),
            nodeId(SpellCoreEffect.CONJURE)
    );

    private SpellResearchManager() {
    }

    public static String nodeId(SpellFlowModifier flow) {
        return "flow:" + flow.getSerializedName();
    }

    public static String nodeId(SpellDeliveryForm form) {
        return "form:" + form.getSerializedName();
    }

    public static String nodeId(SpellCoreEffect effect) {
        return "effect:" + effect.getSerializedName();
    }

    public static String comboId(String nodeId, EssenceType type) {
        return nodeId + "@" + type.getSerializedName();
    }

    public static String familyTierId(String nodeId, MateriaFamilyTier familyTier) {
        return nodeId + "#tier_" + familyTier.getNumericTier();
    }

    public static SpellMutationUnlockMode getActiveUnlockMode() {
        int raw = 2;
        try {
            Integer cfg = EntropicaConfig.SPELL_MUTATION_UNLOCK_MODE.get();
            if (cfg != null) raw = cfg;
        } catch (Exception ignored) {
        }
        return SpellMutationUnlockMode.fromId(raw);
    }

    public static Set<String> getStarterNodes() {
        return STARTER_NODES;
    }

    public static boolean isNodeUnlocked(Player player, String nodeId) {
        if (player.isCreative() || STARTER_NODES.contains(nodeId)) {
            return true;
        }
        return readStringSet(EntityHelper.getPersistentData(player), TAG_UNLOCKED_NODES).contains(nodeId);
    }

    public static boolean isNodeObserved(Player player, String nodeId) {
        if (isNodeUnlocked(player, nodeId)) {
            return true;
        }
        return readStringSet(EntityHelper.getPersistentData(player), TAG_OBSERVED_NODES).contains(nodeId);
    }

    public static boolean isMateriaKnown(Player player, EssenceType type) {
        if (type == null || type == EssenceType.REGULAR || player.isCreative()) {
            return true;
        }
        if (MateriaFamilyTier.classify(type) == MateriaFamilyTier.PRIMORDIAL_BASE) {
            return true;
        }
        Set<String> known = readStringSet(EntityHelper.getPersistentData(player), TAG_KNOWN_MATERIA);
        return known.contains(type.getSerializedName());
    }

    /**
     * Evaluates whether a player can inscribe a specific part's Signature Mutation on a new spell
     * according to the server's configured {@link SpellMutationUnlockMode}.
     * Note: Unmutated parts ({@code mutationEnabled == false}) never require mutation research!
     */
    public static boolean isMutationUnlocked(Player player, String nodeId, EssenceType type, boolean mutationEnabled) {
        if (!mutationEnabled || type == null || type == EssenceType.REGULAR || player.isCreative()) {
            return true;
        }
        if (!isNodeUnlocked(player, nodeId)) {
            return false;
        }

        SpellMutationUnlockMode mode = getActiveUnlockMode();
        CompoundTag data = EntityHelper.getPersistentData(player);
        Set<String> unlockedMutations = readStringSet(data, TAG_UNLOCKED_MUTATIONS);

        return switch (mode) {
            case AUTO_BY_NODE -> isMateriaKnown(player, type);
            case PER_COMBO -> unlockedMutations.contains(comboId(nodeId, type));
            case BY_FAMILY_TIER -> unlockedMutations.contains(familyTierId(nodeId, MateriaFamilyTier.classify(type)))
                    || unlockedMutations.contains(comboId(nodeId, type));
        };
    }

    /**
     * Checks whether a player has unlocked all parts (and any enabled part mutations) required
     * to scribe {@code spell} from scratch at an Arcane Scribing Table.
     */
    public static boolean canPlayerScribeSpell(Player player, SpellAssembly spell) {
        if (player.isCreative()) return true;
        discoverMateriaFromInventory(player);

        FlowPartBinding flow = spell.flowPart();
        String flowNode = nodeId(flow.flow());
        if (!isNodeUnlocked(player, flowNode)
                || !isMutationUnlocked(player, flowNode, flow.materiaType(), flow.mutationEnabled())) {
            return false;
        }

        for (SubSpellSlot slot : spell.subSpells()) {
            FormPartBinding form = slot.formPart();
            String formNode = nodeId(form.form());
            if (!isNodeUnlocked(player, formNode)
                    || !isMutationUnlocked(player, formNode, form.materiaType(), form.mutationEnabled())) {
                return false;
            }

            EffectPartBinding primary = slot.primaryEffect();
            String primaryNode = nodeId(primary.effect());
            if (!isNodeUnlocked(player, primaryNode)
                    || !isMutationUnlocked(player, primaryNode, primary.materiaType(), primary.mutationEnabled())) {
                return false;
            }

            if (slot.secondaryEffect().isPresent()) {
                EffectPartBinding sec = slot.secondaryEffect().get();
                String secNode = nodeId(sec.effect());
                if (!isNodeUnlocked(player, secNode)
                        || !isMutationUnlocked(player, secNode, sec.materiaType(), sec.mutationEnabled())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Called whenever a player casts a Spellgem (including looted dungeon Trial Spellgems!).
     * Casting an unresearched part or mutated element-node pair 1 time promotes it from
     * {@code Cryptic} to {@code Observed} so it can be studied at the Research Bench.
     */
    public static void onSpellCast(Player player, SpellAssembly spell, @Nullable EssenceType dynamicFallback) {
        if (player == null || player.level().isClientSide()) return;

        discoverMateriaFromInventory(player);

        FlowPartBinding flow = spell.flowPart();
        EssenceType flowType = flow.resolveEffectiveMateria(spell.materiaSpecific(), dynamicFallback);
        observePartOnCast(player, nodeId(flow.flow()), flow.flow().getDisplayName(), flowType, flow.mutationEnabled());

        for (SubSpellSlot slot : spell.subSpells()) {
            FormPartBinding form = slot.formPart();
            EssenceType formType = form.resolveEffectiveMateria(spell.materiaSpecific(), dynamicFallback);
            observePartOnCast(player, nodeId(form.form()), form.form().getDisplayName(), formType, form.mutationEnabled());

            EffectPartBinding prim = slot.primaryEffect();
            EssenceType primType = prim.resolveEffectiveMateria(spell.materiaSpecific(), dynamicFallback);
            observePartOnCast(player, nodeId(prim.effect()), prim.effect().getDisplayName(), primType, prim.mutationEnabled());

            if (slot.secondaryEffect().isPresent()) {
                EffectPartBinding sec = slot.secondaryEffect().get();
                EssenceType secType = sec.resolveEffectiveMateria(spell.materiaSpecific(), dynamicFallback);
                observePartOnCast(player, nodeId(sec.effect()), sec.effect().getDisplayName(), secType, sec.mutationEnabled());
            }
        }
    }

    private static void observePartOnCast(Player player, String nodeId, String partLabel,
                                          EssenceType type, boolean mutationEnabled) {
        if (type != null && type != EssenceType.REGULAR) {
            markMateriaKnown(player, type);
        }

        if (!isNodeObserved(player, nodeId)) {
            observeNode(player, nodeId);
            player.displayClientMessage(
                    Component.literal("[Codex] Trial Spell Observed: ")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
                            .append(Component.literal(partLabel).withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" (Ready for Research Bench)").withStyle(ChatFormatting.GRAY)),
                    true
            );
        }

        if (mutationEnabled && type != null && type != EssenceType.REGULAR
                && !isMutationUnlocked(player, nodeId, type, true)) {
            SpellMutationUnlockMode mode = getActiveUnlockMode();
            CompoundTag tag = EntityHelper.getPersistentData(player);
            Set<String> observed = readStringSet(tag, TAG_OBSERVED_MUTATIONS);
            String key = (mode == SpellMutationUnlockMode.BY_FAMILY_TIER)
                    ? familyTierId(nodeId, MateriaFamilyTier.classify(type))
                    : comboId(nodeId, type);
            if (observed.add(key)) {
                writeStringSet(tag, TAG_OBSERVED_MUTATIONS, observed);
                player.displayClientMessage(
                        Component.literal("[Codex] Mutation Observed: ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.literal(type.getDisplayName() + " + " + partLabel).withStyle(ChatFormatting.GOLD)),
                        true
                );
            }
        }
    }

    public static void discoverMateriaFromInventory(Player player) {
        if (player == null) return;
        Inventory inv = player.getInventory();
        CompoundTag data = EntityHelper.getPersistentData(player);
        Set<String> known = readStringSet(data, TAG_KNOWN_MATERIA);
        boolean changed = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            EssenceType found = null;
            if (s.getItem() instanceof OrbisCellItem) {
                found = SpellFuelService.readOrbisCellType(s);
            } else if (s.getItem() instanceof EssenceAmpouleItem) {
                found = EssenceAmpouleItem.getEssenceType(s);
            } else if (s.getItem() instanceof EssenceItem) {
                found = EssenceItem.getEssenceType(s);
            }
            if (found != null && found != EssenceType.REGULAR) {
                changed |= known.add(found.getSerializedName());
            }
        }
        if (changed) {
            writeStringSet(data, TAG_KNOWN_MATERIA, known);
        }
    }

    public static void markMateriaKnown(Player player, EssenceType type) {
        if (type == null || type == EssenceType.REGULAR) return;
        CompoundTag tag = EntityHelper.getPersistentData(player);
        Set<String> set = readStringSet(tag, TAG_KNOWN_MATERIA);
        if (set.add(type.getSerializedName())) {
            writeStringSet(tag, TAG_KNOWN_MATERIA, set);
        }
    }

    public static void observeNode(Player player, String nodeId) {
        CompoundTag tag = EntityHelper.getPersistentData(player);
        Set<String> set = readStringSet(tag, TAG_OBSERVED_NODES);
        if (set.add(nodeId)) {
            writeStringSet(tag, TAG_OBSERVED_NODES, set);
        }
    }

    public static void unlockNode(Player player, String nodeId) {
        CompoundTag tag = EntityHelper.getPersistentData(player);
        Set<String> obs = readStringSet(tag, TAG_OBSERVED_NODES);
        obs.add(nodeId);
        writeStringSet(tag, TAG_OBSERVED_NODES, obs);

        Set<String> unl = readStringSet(tag, TAG_UNLOCKED_NODES);
        unl.add(nodeId);
        writeStringSet(tag, TAG_UNLOCKED_NODES, unl);
    }

    public static void unlockMutationCombo(Player player, String nodeId, EssenceType type) {
        markMateriaKnown(player, type);
        unlockNode(player, nodeId);
        CompoundTag tag = EntityHelper.getPersistentData(player);
        Set<String> unl = readStringSet(tag, TAG_UNLOCKED_MUTATIONS);
        unl.add(comboId(nodeId, type));
        unl.add(familyTierId(nodeId, MateriaFamilyTier.classify(type)));
        writeStringSet(tag, TAG_UNLOCKED_MUTATIONS, unl);
    }

    public static void unlockAllSpellResearch(Player player) {
        for (SpellFlowModifier f : SpellFlowModifier.values()) unlockNode(player, nodeId(f));
        for (SpellDeliveryForm f : SpellDeliveryForm.values()) unlockNode(player, nodeId(f));
        for (SpellCoreEffect e : SpellCoreEffect.values()) unlockNode(player, nodeId(e));
        for (EssenceType t : EssenceType.values()) {
            if (!t.isFragment() && t != EssenceType.REGULAR) {
                markMateriaKnown(player, t);
            }
        }
    }

    public record PendingStudyTarget(
            boolean isMutation,
            String targetKey,
            String displayLabel,
            @Nullable EssenceType requiredMateriaSample
    ) {
    }

    /**
     * Returns all currently {@code Observed} (but not yet {@code Unlocked}) spell nodes and
     * element-node mutations awaiting finalization at a {@code ResearchBenchBlock} (Section 3.8).
     */
    public static java.util.List<PendingStudyTarget> getPendingObservedTargets(Player player) {
        java.util.List<PendingStudyTarget> pending = new java.util.ArrayList<>();
        if (player == null) return pending;

        CompoundTag tag = EntityHelper.getPersistentData(player);
        Set<String> observedNodes = readStringSet(tag, TAG_OBSERVED_NODES);
        Set<String> unlockedNodes = readStringSet(tag, TAG_UNLOCKED_NODES);

        for (String node : observedNodes) {
            if (!STARTER_NODES.contains(node) && !unlockedNodes.contains(node)) {
                pending.add(new PendingStudyTarget(false, node, formatNodeDisplayName(node), null));
            }
        }

        Set<String> observedMut = readStringSet(tag, TAG_OBSERVED_MUTATIONS);
        Set<String> unlockedMut = readStringSet(tag, TAG_UNLOCKED_MUTATIONS);
        for (String mutKey : observedMut) {
            if (!unlockedMut.contains(mutKey)) {
                EssenceType reqType = parseComboMateria(mutKey);
                pending.add(new PendingStudyTarget(true, mutKey, formatMutationDisplayName(mutKey), reqType));
            }
        }
        return pending;
    }

    /**
     * Finalizes a studied {@link PendingStudyTarget} from {@code Observed -> Unlocked}.
     */
    public static void finalizeObservedTarget(Player player, PendingStudyTarget target) {
        if (player == null || target == null) return;
        CompoundTag tag = EntityHelper.getPersistentData(player);
        if (!target.isMutation()) {
            unlockNode(player, target.targetKey());
        } else {
            String key = target.targetKey();
            int atIdx = key.indexOf('@');
            int hashIdx = key.indexOf('#');
            if (atIdx > 0) {
                unlockNode(player, key.substring(0, atIdx));
            } else if (hashIdx > 0) {
                unlockNode(player, key.substring(0, hashIdx));
            }
            if (target.requiredMateriaSample() != null) {
                markMateriaKnown(player, target.requiredMateriaSample());
            }
            Set<String> unl = readStringSet(tag, TAG_UNLOCKED_MUTATIONS);
            unl.add(key);
            writeStringSet(tag, TAG_UNLOCKED_MUTATIONS, unl);
        }
    }

    @Nullable
    public static PendingStudyTarget finalizeNextObservedTarget(Player player) {
        java.util.List<PendingStudyTarget> pending = getPendingObservedTargets(player);
        if (pending.isEmpty()) return null;
        PendingStudyTarget next = pending.getFirst();
        finalizeObservedTarget(player, next);
        return next;
    }

    @Nullable
    private static EssenceType parseComboMateria(String mutKey) {
        int at = mutKey.indexOf('@');
        if (at < 0 || at + 1 >= mutKey.length()) return null;
        String serialized = mutKey.substring(at + 1);
        for (EssenceType t : EssenceType.values()) {
            if (t.getSerializedName().equalsIgnoreCase(serialized) || t.name().equalsIgnoreCase(serialized)) {
                return t;
            }
        }
        return null;
    }

    public static String formatNodeDisplayName(String nodeId) {
        if (nodeId.startsWith("flow:")) {
            String id = nodeId.substring(5);
            for (SpellFlowModifier f : SpellFlowModifier.values()) {
                if (f.getSerializedName().equals(id)) return "Flow: " + f.getDisplayName();
            }
        } else if (nodeId.startsWith("form:")) {
            String id = nodeId.substring(5);
            for (SpellDeliveryForm f : SpellDeliveryForm.values()) {
                if (f.getSerializedName().equals(id)) return "Form: " + f.getDisplayName();
            }
        } else if (nodeId.startsWith("effect:")) {
            String id = nodeId.substring(7);
            for (SpellCoreEffect e : SpellCoreEffect.values()) {
                if (e.getSerializedName().equals(id)) return "Effect: " + e.getDisplayName();
            }
        }
        return nodeId;
    }

    private static String formatMutationDisplayName(String mutKey) {
        int at = mutKey.indexOf('@');
        if (at > 0) {
            String nodePart = formatNodeDisplayName(mutKey.substring(0, at));
            EssenceType t = parseComboMateria(mutKey);
            return (t != null ? t.getDisplayName() : mutKey.substring(at + 1)) + " Mutation (" + nodePart + ")";
        }
        int hash = mutKey.indexOf('#');
        if (hash > 0) {
            String nodePart = formatNodeDisplayName(mutKey.substring(0, hash));
            return mutKey.substring(hash + 1).toUpperCase() + " Family Mutation (" + nodePart + ")";
        }
        return mutKey;
    }

    public static Set<String> getUnlockedNodes(Player player) {
        Set<String> out = new LinkedHashSet<>(STARTER_NODES);
        out.addAll(readStringSet(EntityHelper.getPersistentData(player), TAG_UNLOCKED_NODES));
        return Collections.unmodifiableSet(out);
    }

    private static Set<String> readStringSet(CompoundTag tag, String key) {
        Set<String> set = new LinkedHashSet<>();
        tag.getList(key).ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                String val = list.getString(i).orElse("");
                if (!val.isEmpty()) set.add(val);
            }
        });
        return set;
    }

    private static void writeStringSet(CompoundTag tag, String key, Set<String> values) {
        ListTag list = new ListTag();
        for (String v : values) {
            list.add(StringTag.valueOf(v));
        }
        tag.put(key, list);
    }
}
