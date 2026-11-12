package ddraig.net.entropica.spell.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellPartSlotType;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Modular Core Effect (or Secondary Dual-Verb Core Effect) part binding inside a {@link SubSpellSlot}.
 * Carries its own {@link EssenceType} and independent {@code mutationEnabled} toggle.
 */
public record EffectPartBinding(
        SpellCoreEffect effect,
        EssenceType materiaType,
        boolean mutationEnabled,
        String subMode,
        int dialValue
) {
    public EffectPartBinding {
        if (effect == null) effect = SpellCoreEffect.HARM;
        if (materiaType == null) materiaType = EssenceType.REGULAR;
        if (subMode == null || subMode.isBlank()) subMode = "default";
    }

    public static final Codec<EffectPartBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpellCoreEffect.CODEC.fieldOf("effect").forGetter(EffectPartBinding::effect),
            EssenceType.CODEC.fieldOf("materia_type").forGetter(EffectPartBinding::materiaType),
            Codec.BOOL.fieldOf("mutation_enabled").forGetter(EffectPartBinding::mutationEnabled),
            Codec.STRING.optionalFieldOf("sub_mode", "default").forGetter(EffectPartBinding::subMode),
            Codec.INT.optionalFieldOf("dial_value", 15).forGetter(EffectPartBinding::dialValue)
    ).apply(instance, EffectPartBinding::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectPartBinding> STREAM_CODEC = StreamCodec.composite(
            SpellCoreEffect.STREAM_CODEC, EffectPartBinding::effect,
            EssenceType.STREAM_CODEC, EffectPartBinding::materiaType,
            ByteBufCodecs.BOOL, EffectPartBinding::mutationEnabled,
            ByteBufCodecs.STRING_UTF8, EffectPartBinding::subMode,
            ByteBufCodecs.VAR_INT, EffectPartBinding::dialValue,
            EffectPartBinding::new
    );

    public static EffectPartBinding of(SpellCoreEffect effect, EssenceType materiaType, boolean mutationEnabled) {
        return new EffectPartBinding(effect, materiaType, mutationEnabled, "default", 15);
    }

    public EffectPartBinding withMateria(EssenceType newType) {
        return new EffectPartBinding(this.effect, newType, this.mutationEnabled, this.subMode, this.dialValue);
    }

    public EffectPartBinding withMutationEnabled(boolean enabled) {
        return new EffectPartBinding(this.effect, this.materiaType, enabled, this.subMode, this.dialValue);
    }

    public EssenceType resolveEffectiveMateria(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        if (!materiaSpecificLock && this.materiaType == EssenceType.REGULAR && fallbackCellType != null) {
            return fallbackCellType;
        }
        return this.materiaType;
    }

    public SpellMutationProfile resolveMutation(SpellPartSlotType slotType,
                                                boolean materiaSpecificLock,
                                                EssenceType fallbackCellType) {
        EssenceType effective = resolveEffectiveMateria(materiaSpecificLock, fallbackCellType);
        return SpellMutationRegistry.resolveEffect(slotType, this.effect, effective, this.mutationEnabled);
    }
}
