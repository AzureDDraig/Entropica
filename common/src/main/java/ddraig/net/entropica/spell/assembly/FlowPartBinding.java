package ddraig.net.entropica.spell.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Modular Flow Modifier part binding within a {@link SpellAssembly}.
 * Carries its own {@link EssenceType} and independent {@code mutationEnabled} toggle.
 */
public record FlowPartBinding(
        SpellFlowModifier flow,
        EssenceType materiaType,
        boolean mutationEnabled,
        int subSpellCount,
        float delaySeconds
) {
    public FlowPartBinding {
        if (flow == null) flow = SpellFlowModifier.SINGLE;
        if (materiaType == null) materiaType = EssenceType.REGULAR;
        subSpellCount = Math.clamp(subSpellCount, 1, 8);
        delaySeconds = Math.clamp(delaySeconds, 0.0F, 120.0F);
    }

    public static final Codec<FlowPartBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpellFlowModifier.CODEC.fieldOf("flow").forGetter(FlowPartBinding::flow),
            EssenceType.CODEC.fieldOf("materia_type").forGetter(FlowPartBinding::materiaType),
            Codec.BOOL.fieldOf("mutation_enabled").forGetter(FlowPartBinding::mutationEnabled),
            Codec.INT.fieldOf("sub_spell_count").forGetter(FlowPartBinding::subSpellCount),
            Codec.FLOAT.optionalFieldOf("delay_seconds", 0.0F).forGetter(FlowPartBinding::delaySeconds)
    ).apply(instance, FlowPartBinding::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlowPartBinding> STREAM_CODEC = StreamCodec.composite(
            SpellFlowModifier.STREAM_CODEC, FlowPartBinding::flow,
            EssenceType.STREAM_CODEC, FlowPartBinding::materiaType,
            ByteBufCodecs.BOOL, FlowPartBinding::mutationEnabled,
            ByteBufCodecs.VAR_INT, FlowPartBinding::subSpellCount,
            ByteBufCodecs.FLOAT, FlowPartBinding::delaySeconds,
            FlowPartBinding::new
    );

    public static FlowPartBinding basicSingle() {
        return new FlowPartBinding(SpellFlowModifier.SINGLE, EssenceType.REGULAR, false, 1, 0.0F);
    }

    public FlowPartBinding withMateria(EssenceType newType) {
        return new FlowPartBinding(this.flow, newType, this.mutationEnabled, this.subSpellCount, this.delaySeconds);
    }

    public FlowPartBinding withMutationEnabled(boolean enabled) {
        return new FlowPartBinding(this.flow, this.materiaType, enabled, this.subSpellCount, this.delaySeconds);
    }

    public EssenceType resolveEffectiveMateria(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        if (!materiaSpecificLock && this.materiaType == EssenceType.REGULAR && fallbackCellType != null) {
            return fallbackCellType;
        }
        return this.materiaType;
    }

    public SpellMutationProfile resolveMutation(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        EssenceType effective = resolveEffectiveMateria(materiaSpecificLock, fallbackCellType);
        return SpellMutationRegistry.resolveFlow(this.flow, effective, this.mutationEnabled);
    }
}
