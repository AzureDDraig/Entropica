package ddraig.net.entropica.spell.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * Represents one Sub-Spell Payload Slot (Slots 1..8) within a {@link SpellAssembly}.
 *
 * <p>Each part inside the slot—the {@link FormPartBinding}, the primary {@link EffectPartBinding},
 * and the optional Dual-Verb secondary {@link EffectPartBinding}—can be assigned a completely
 * different {@link ddraig.net.entropica.api.EssenceType} and have its Signature Mutation toggled
 * ON or OFF independently.</p>
 */
public record SubSpellSlot(
        FormPartBinding formPart,
        EffectPartBinding primaryEffect,
        Optional<EffectPartBinding> secondaryEffect
) {
    public SubSpellSlot {
        if (formPart == null) throw new IllegalArgumentException("formPart cannot be null");
        if (primaryEffect == null) throw new IllegalArgumentException("primaryEffect cannot be null");
        if (secondaryEffect == null) secondaryEffect = Optional.empty();
    }

    public static final Codec<SubSpellSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FormPartBinding.CODEC.fieldOf("form_part").forGetter(SubSpellSlot::formPart),
            EffectPartBinding.CODEC.fieldOf("primary_effect").forGetter(SubSpellSlot::primaryEffect),
            EffectPartBinding.CODEC.optionalFieldOf("secondary_effect").forGetter(SubSpellSlot::secondaryEffect)
    ).apply(instance, SubSpellSlot::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubSpellSlot> STREAM_CODEC = StreamCodec.composite(
            FormPartBinding.STREAM_CODEC, SubSpellSlot::formPart,
            EffectPartBinding.STREAM_CODEC, SubSpellSlot::primaryEffect,
            ByteBufCodecs.optional(EffectPartBinding.STREAM_CODEC), SubSpellSlot::secondaryEffect,
            SubSpellSlot::new
    );

    public static SubSpellSlot of(FormPartBinding formPart, EffectPartBinding primaryEffect) {
        return new SubSpellSlot(formPart, primaryEffect, Optional.empty());
    }

    public static SubSpellSlot ofDualVerb(FormPartBinding formPart, EffectPartBinding primaryEffect, EffectPartBinding secondaryEffect) {
        return new SubSpellSlot(formPart, primaryEffect, Optional.of(secondaryEffect));
    }

    public boolean isDualVerb() {
        return this.secondaryEffect.isPresent();
    }
}
