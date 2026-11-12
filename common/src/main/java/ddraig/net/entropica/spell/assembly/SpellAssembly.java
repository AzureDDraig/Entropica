package ddraig.net.entropica.spell.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellPrefix;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * The complete modular Spell Assembly data container stored on Inscribed Spellgems
 * (with locked {@code potencyTier >= 1}) and Spell Parchments ({@code potencyTier == 0} until ritual binding).
 *
 * <p>Supports multi-Materia composition across every individual part ({@code Flow}, {@code Form},
 * {@code Primary Effect}, and {@code Secondary Effect}) and per-part {@code mutationEnabled} toggles.</p>
 */
public record SpellAssembly(
        String customName,
        List<String> customLore,
        boolean materiaSpecific,
        SpellPrefix prefix,
        int potencyTier,
        FlowPartBinding flowPart,
        List<SubSpellSlot> subSpells,
        SpellFlawProfile flawProfile,
        int resoniteCritFragments
) {
    public SpellAssembly {
        if (customName == null || customName.isBlank()) customName = "Unnamed Incantation";
        customLore = customLore == null ? List.of() : List.copyOf(customLore);
        if (prefix == null) prefix = SpellPrefix.COMBAT;
        potencyTier = Math.clamp(potencyTier, 0, 10);
        if (flowPart == null) flowPart = FlowPartBinding.basicSingle();
        if (subSpells == null || subSpells.isEmpty()) {
            subSpells = List.of(SubSpellSlot.of(
                    FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.REGULAR, false),
                    EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.REGULAR, false)
            ));
        } else {
            List<SubSpellSlot> bounded = subSpells.size() > 8 ? subSpells.subList(0, 8) : subSpells;
            // Enforce single chosen summon type per spell: Autonomous Agent, Anchored Sentry, and Phantom Decoy
            // are three separate creation forms, and a single spell may only choose one summon type.
            SpellDeliveryForm chosenSummonForm = null;
            boolean needsSummonNormalization = false;
            for (SubSpellSlot slot : bounded) {
                SpellDeliveryForm f = slot.formPart().form();
                if (f.isSummonForm()) {
                    if (chosenSummonForm == null) {
                        chosenSummonForm = f;
                    } else if (f != chosenSummonForm) {
                        needsSummonNormalization = true;
                    }
                }
            }
            if (needsSummonNormalization && chosenSummonForm != null) {
                List<SubSpellSlot> normalized = new ArrayList<>(bounded.size());
                for (SubSpellSlot slot : bounded) {
                    if (slot.formPart().form().isSummonForm() && slot.formPart().form() != chosenSummonForm) {
                        normalized.add(new SubSpellSlot(
                                slot.formPart().withForm(chosenSummonForm),
                                slot.primaryEffect(),
                                slot.secondaryEffect()
                        ));
                    } else {
                        normalized.add(slot);
                    }
                }
                subSpells = List.copyOf(normalized);
            } else {
                subSpells = List.copyOf(bounded);
            }
        }
        if (flawProfile == null) flawProfile = SpellFlawProfile.PERFECT;
        resoniteCritFragments = Math.clamp(resoniteCritFragments, 0, 5);
    }

    public static final Codec<SpellAssembly> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("custom_name").forGetter(SpellAssembly::customName),
            Codec.STRING.listOf().optionalFieldOf("custom_lore", List.of()).forGetter(SpellAssembly::customLore),
            Codec.BOOL.fieldOf("materia_specific").forGetter(SpellAssembly::materiaSpecific),
            SpellPrefix.CODEC.fieldOf("prefix").forGetter(SpellAssembly::prefix),
            Codec.INT.fieldOf("potency_tier").forGetter(SpellAssembly::potencyTier),
            FlowPartBinding.CODEC.fieldOf("flow_part").forGetter(SpellAssembly::flowPart),
            SubSpellSlot.CODEC.listOf().fieldOf("sub_spells").forGetter(SpellAssembly::subSpells),
            SpellFlawProfile.CODEC.optionalFieldOf("flaw_profile", SpellFlawProfile.PERFECT).forGetter(SpellAssembly::flawProfile),
            Codec.INT.optionalFieldOf("resonite_crit_fragments", 0).forGetter(SpellAssembly::resoniteCritFragments)
    ).apply(instance, SpellAssembly::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellAssembly> STREAM_CODEC = StreamCodec.of(
            (buf, assembly) -> {
                buf.writeUtf(assembly.customName());
                buf.writeVarInt(assembly.customLore().size());
                for (String line : assembly.customLore()) {
                    buf.writeUtf(line);
                }
                buf.writeBoolean(assembly.materiaSpecific());
                SpellPrefix.STREAM_CODEC.encode(buf, assembly.prefix());
                buf.writeVarInt(assembly.potencyTier());
                FlowPartBinding.STREAM_CODEC.encode(buf, assembly.flowPart());
                buf.writeVarInt(assembly.subSpells().size());
                for (SubSpellSlot slot : assembly.subSpells()) {
                    SubSpellSlot.STREAM_CODEC.encode(buf, slot);
                }
                SpellFlawProfile.STREAM_CODEC.encode(buf, assembly.flawProfile());
                buf.writeVarInt(assembly.resoniteCritFragments());
            },
            buf -> {
                String name = buf.readUtf();
                int loreCount = buf.readVarInt();
                List<String> lore = new ArrayList<>(loreCount);
                for (int i = 0; i < loreCount; i++) {
                    lore.add(buf.readUtf());
                }
                boolean specific = buf.readBoolean();
                SpellPrefix prefix = SpellPrefix.STREAM_CODEC.decode(buf);
                int tier = buf.readVarInt();
                FlowPartBinding flow = FlowPartBinding.STREAM_CODEC.decode(buf);
                int slotCount = buf.readVarInt();
                List<SubSpellSlot> slots = new ArrayList<>(slotCount);
                for (int i = 0; i < slotCount; i++) {
                    slots.add(SubSpellSlot.STREAM_CODEC.decode(buf));
                }
                SpellFlawProfile flaw = SpellFlawProfile.STREAM_CODEC.decode(buf);
                int critFrags = buf.readVarInt();
                return new SpellAssembly(name, lore, specific, prefix, tier, flow, slots, flaw, critFrags);
            }
    );

    public boolean isBoundToTier() {
        return this.potencyTier >= 1;
    }

    public int getEffectiveTier() {
        return Math.clamp(this.potencyTier, 1, 10);
    }

    /**
     * Returns the single chosen Summon Delivery Form ({@link SpellDeliveryForm#SUMMON_AGENT},
     * {@link SpellDeliveryForm#SUMMON_SENTRY}, or {@link SpellDeliveryForm#SUMMON_DECOY}) for this spell,
     * or {@link Optional#empty()} if this spell does not summon an Eidolon construct.
     */
    public Optional<SpellDeliveryForm> getChosenSummonForm() {
        for (SubSpellSlot slot : this.subSpells) {
            if (slot.formPart().form().isSummonForm()) {
                return Optional.of(slot.formPart().form());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns true if any sub-spell integrates a Ward / Aegis component, exempting the caster,
     * tamed pets, and allies from Combat splash friendly fire (Section 2.1.4).
     */
    public boolean hasWardFriendlyFireSafeguard() {
        for (SubSpellSlot slot : this.subSpells) {
            if (slot.primaryEffect().effect() == SpellCoreEffect.WARD) return true;
            if (slot.secondaryEffect().map(e -> e.effect() == SpellCoreEffect.WARD).orElse(false)) return true;
        }
        return false;
    }

    /**
     * Collects all distinct Materia types assigned across every part of this spell assembly
     * in deterministic insertion order.
     */
    public Set<EssenceType> getDistinctMateriaTypes() {
        Set<EssenceType> types = new LinkedHashSet<>();
        types.add(this.flowPart.materiaType());
        for (SubSpellSlot slot : this.subSpells) {
            types.add(slot.formPart().materiaType());
            types.add(slot.primaryEffect().materiaType());
            slot.secondaryEffect().ifPresent(sec -> types.add(sec.materiaType()));
        }
        return Collections.unmodifiableSet(types);
    }

    public float getCriticalStrikeChance() {
        if (this.resoniteCritFragments <= 0) return 0.0F;
        return Math.min(0.25F, this.resoniteCritFragments * 0.05F);
    }

    public int getBaseCooldownTicks() {
        int base = 20 + (this.subSpells.size() - 1) * 6;
        if (this.flowPart.flow() == SpellFlowModifier.FAST_DART) {
            base = Math.max(8, (int) (base * 0.75F));
        }
        return base;
    }

    public SpellAssembly bindToPotencyTier(int lockedTier, SpellFlawProfile rolledFlaw) {
        return new SpellAssembly(
                this.customName,
                this.customLore,
                this.materiaSpecific,
                this.prefix,
                Math.clamp(lockedTier, 1, 10),
                this.flowPart,
                this.subSpells,
                rolledFlaw == null ? SpellFlawProfile.PERFECT : rolledFlaw,
                this.resoniteCritFragments
        );
    }

    // --- Default Starter Spells (Section 8.6.1) ---

    public static SpellAssembly createMagicMissileStarter(int tier) {
        return new Builder("Magic Missile", SpellPrefix.COMBAT)
                .materiaSpecific(false)
                .potencyTier(tier)
                .flow(SpellFlowModifier.SINGLE, EssenceType.REGULAR, false, 1)
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.REGULAR, false),
                        EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.REGULAR, false)
                ))
                .build();
    }

    public static SpellAssembly createBasicBreakStarter(int tier) {
        return new Builder("Basic Break", SpellPrefix.UTILITY)
                .materiaSpecific(false)
                .potencyTier(tier)
                .flow(SpellFlowModifier.SINGLE, EssenceType.REGULAR, false, 1)
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.BEAM, EssenceType.REGULAR, false),
                        EffectPartBinding.of(SpellCoreEffect.BREAK, EssenceType.REGULAR, false)
                ))
                .build();
    }

    public static SpellAssembly createBasicWispStarter(int tier) {
        return new Builder("Basic Wisp", SpellPrefix.MISC)
                .materiaSpecific(false)
                .potencyTier(tier)
                .flow(SpellFlowModifier.SELF_CAST, EssenceType.REGULAR, false, 1)
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.AURA, EssenceType.REGULAR, false),
                        EffectPartBinding.of(SpellCoreEffect.CONJURE, EssenceType.REGULAR, false)
                ))
                .build();
    }

    public static SpellAssembly createBasicWardStarter(int tier) {
        return new Builder("Basic Ward", SpellPrefix.COMBAT)
                .materiaSpecific(false)
                .potencyTier(tier)
                .flow(SpellFlowModifier.SELF_CAST, EssenceType.REGULAR, false, 1)
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.AURA, EssenceType.REGULAR, false),
                        EffectPartBinding.of(SpellCoreEffect.WARD, EssenceType.REGULAR, false)
                ))
                .build();
    }

    public static SpellAssembly createBasicHealStarter(int tier) {
        return new Builder("Basic Heal", SpellPrefix.COMBAT)
                .materiaSpecific(false)
                .potencyTier(tier)
                .flow(SpellFlowModifier.SELF_CAST, EssenceType.REGULAR, false, 1)
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.AURA, EssenceType.REGULAR, false),
                        EffectPartBinding.of(SpellCoreEffect.RESTORATION, EssenceType.REGULAR, false)
                ))
                .build();
    }

    public static final class Builder {
        private final String name;
        private final SpellPrefix prefix;
        private final List<String> lore = new ArrayList<>();
        private boolean materiaSpecific = true;
        private int potencyTier = 1;
        private FlowPartBinding flowPart = FlowPartBinding.basicSingle();
        private final List<SubSpellSlot> subSpells = new ArrayList<>();
        private SpellFlawProfile flawProfile = SpellFlawProfile.PERFECT;
        private int resoniteCritFragments = 0;

        public Builder(String name, SpellPrefix prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        public Builder addLore(String line) {
            this.lore.add(line);
            return this;
        }

        public Builder materiaSpecific(boolean specific) {
            this.materiaSpecific = specific;
            return this;
        }

        public Builder potencyTier(int tier) {
            this.potencyTier = tier;
            return this;
        }

        public Builder flow(SpellFlowModifier flow, EssenceType materiaType, boolean mutationEnabled, int subSpellCount) {
            this.flowPart = new FlowPartBinding(flow, materiaType, mutationEnabled, subSpellCount, 0.0F);
            return this;
        }

        public Builder flowBinding(FlowPartBinding binding) {
            this.flowPart = binding;
            return this;
        }

        public Builder addSubSpell(SubSpellSlot slot) {
            this.subSpells.add(slot);
            return this;
        }

        public Builder flaw(SpellFlawProfile flaw) {
            this.flawProfile = flaw;
            return this;
        }

        public Builder resoniteCritFragments(int count) {
            this.resoniteCritFragments = count;
            return this;
        }

        public SpellAssembly build() {
            return new SpellAssembly(
                    this.name,
                    this.lore,
                    this.materiaSpecific,
                    this.prefix,
                    this.potencyTier,
                    this.flowPart,
                    this.subSpells,
                    this.flawProfile,
                    this.resoniteCritFragments
            );
        }
    }
}
