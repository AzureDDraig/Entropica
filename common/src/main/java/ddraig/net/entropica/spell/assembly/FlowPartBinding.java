package ddraig.net.entropica.spell.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellTravelDistance;
import ddraig.net.entropica.spell.SpellTriggerType;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * Modular Flow Modifier, Trigger Logic & Travel Distance binding within a {@link SpellAssembly}.
 * Carries its own {@link EssenceType}, independent {@code mutationEnabled} toggle,
 * {@link SpellTriggerType} activation condition, Multi-Cast {@code subSpellCount},
 * timer {@code delaySeconds}, and Outer Rim {@code travelDistanceMeters} ({@code 0..128m}).
 */
public record FlowPartBinding(
        SpellFlowModifier flow,
        SpellTriggerType trigger,
        EssenceType materiaType,
        boolean mutationEnabled,
        int subSpellCount,
        float delaySeconds,
        int travelDistanceMeters
) {
    public FlowPartBinding {
        if (flow == null) flow = SpellFlowModifier.SINGLE;
        if (trigger == null) trigger = SpellTriggerType.ON_ENTITY_HIT;
        if (materiaType == null) materiaType = EssenceType.REGULAR;
        subSpellCount = Math.clamp(subSpellCount, 1, 8);
        delaySeconds = Math.clamp(delaySeconds, 0.0F, 120.0F);
        travelDistanceMeters = SpellTravelDistance.snapFromMeters(travelDistanceMeters).meters();
    }

    public FlowPartBinding(
            SpellFlowModifier flow,
            SpellTriggerType trigger,
            EssenceType materiaType,
            boolean mutationEnabled,
            int subSpellCount,
            float delaySeconds
    ) {
        this(flow, trigger, materiaType, mutationEnabled, subSpellCount, delaySeconds, SpellTravelDistance.STANDARD_METERS);
    }

    public FlowPartBinding(
            SpellFlowModifier flow,
            EssenceType materiaType,
            boolean mutationEnabled,
            int subSpellCount,
            float delaySeconds
    ) {
        this(flow, SpellTriggerType.ON_ENTITY_HIT, materiaType, mutationEnabled, subSpellCount, delaySeconds, SpellTravelDistance.STANDARD_METERS);
    }

    public static final Codec<FlowPartBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("flow").forGetter(b -> b.flow().getSerializedName()),
            SpellTriggerType.CODEC.optionalFieldOf("trigger").forGetter(b -> Optional.of(b.trigger())),
            EssenceType.CODEC.fieldOf("materia_type").forGetter(FlowPartBinding::materiaType),
            Codec.BOOL.fieldOf("mutation_enabled").forGetter(FlowPartBinding::mutationEnabled),
            Codec.INT.fieldOf("sub_spell_count").forGetter(FlowPartBinding::subSpellCount),
            Codec.FLOAT.optionalFieldOf("delay_seconds", 0.0F).forGetter(FlowPartBinding::delaySeconds),
            Codec.INT.optionalFieldOf("travel_distance", SpellTravelDistance.STANDARD_METERS).forGetter(FlowPartBinding::travelDistanceMeters)
    ).apply(instance, (rawFlow, optTrigger, mat, mut, subCount, delay, travelMeters) -> {
        SpellFlowModifier parsedFlow = SpellFlowModifier.fromSerializedOrLegacy(rawFlow);
        SpellTriggerType parsedTrigger = optTrigger.orElseGet(() -> SpellTriggerType.inferLegacyTriggerFromFlowId(rawFlow));
        return new FlowPartBinding(parsedFlow, parsedTrigger, mat, mut, subCount, delay, travelMeters);
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlowPartBinding> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                SpellFlowModifier.STREAM_CODEC.encode(buf, val.flow());
                SpellTriggerType.STREAM_CODEC.encode(buf, val.trigger());
                EssenceType.STREAM_CODEC.encode(buf, val.materiaType());
                buf.writeBoolean(val.mutationEnabled());
                buf.writeVarInt(val.subSpellCount());
                buf.writeFloat(val.delaySeconds());
                buf.writeVarInt(val.travelDistanceMeters());
            },
            buf -> new FlowPartBinding(
                    SpellFlowModifier.STREAM_CODEC.decode(buf),
                    SpellTriggerType.STREAM_CODEC.decode(buf),
                    EssenceType.STREAM_CODEC.decode(buf),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    buf.readFloat(),
                    buf.readVarInt()
            )
    );

    public static FlowPartBinding basicSingle() {
        return new FlowPartBinding(
                SpellFlowModifier.SINGLE,
                SpellTriggerType.ON_ENTITY_HIT,
                EssenceType.REGULAR,
                false,
                1,
                0.0F,
                SpellTravelDistance.STANDARD_METERS
        );
    }

    public SpellTravelDistance distanceStep() {
        return SpellTravelDistance.snapFromMeters(this.travelDistanceMeters);
    }

    public FlowPartBinding withTravelDistance(int newMeters) {
        return new FlowPartBinding(this.flow, this.trigger, this.materiaType, this.mutationEnabled, this.subSpellCount, this.delaySeconds, newMeters);
    }

    public FlowPartBinding withDistanceStep(SpellTravelDistance step) {
        int m = step != null ? step.meters() : SpellTravelDistance.STANDARD_METERS;
        return withTravelDistance(m);
    }

    public FlowPartBinding withTrigger(SpellTriggerType newTrigger) {
        return new FlowPartBinding(this.flow, newTrigger, this.materiaType, this.mutationEnabled, this.subSpellCount, this.delaySeconds, this.travelDistanceMeters);
    }

    public FlowPartBinding withMateria(EssenceType newType) {
        return new FlowPartBinding(this.flow, this.trigger, newType, this.mutationEnabled, this.subSpellCount, this.delaySeconds, this.travelDistanceMeters);
    }

    public FlowPartBinding withMutationEnabled(boolean enabled) {
        return new FlowPartBinding(this.flow, this.trigger, this.materiaType, enabled, this.subSpellCount, this.delaySeconds, this.travelDistanceMeters);
    }

    public FlowPartBinding withSubSpellCount(int count) {
        return new FlowPartBinding(this.flow, this.trigger, this.materiaType, this.mutationEnabled, count, this.delaySeconds, this.travelDistanceMeters);
    }

    public EssenceType resolveEffectiveMateria(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        if (!materiaSpecificLock && this.materiaType == EssenceType.REGULAR && fallbackCellType != null) {
            return fallbackCellType;
        }
        return this.materiaType;
    }

    /**
     * True only when {@code VOID_WARP} is selected AND the Flow Ring is actively mutated with Void
     * or a Void-created Materia fusion (Guide Section 3.4).
     */
    public boolean isVoidWarpAuthorized(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        if (this.flow != SpellFlowModifier.VOID_WARP || !this.mutationEnabled) {
            return false;
        }
        EssenceType effective = resolveEffectiveMateria(materiaSpecificLock, fallbackCellType);
        return MateriaFamilyTier.hasVoidCreationHeritage(effective);
    }

    public boolean usesDynamicTriggerBilling(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        if (this.flow == SpellFlowModifier.VOID_WARP) {
            return isVoidWarpAuthorized(materiaSpecificLock, fallbackCellType) || this.trigger.usesDynamicTriggerBilling();
        }
        return this.flow.usesDynamicTriggerBilling() || this.trigger.usesDynamicTriggerBilling();
    }

    public SpellMutationProfile resolveMutation(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        EssenceType effective = resolveEffectiveMateria(materiaSpecificLock, fallbackCellType);
        return SpellMutationRegistry.resolveFlow(this.flow, effective, this.mutationEnabled);
    }
}
