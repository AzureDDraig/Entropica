package ddraig.net.entropica.spell.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Modular Delivery Form part binding inside a {@link SubSpellSlot}.
 * Carries its own {@link EssenceType} and independent {@code mutationEnabled} toggle.
 */
public record FormPartBinding(
        SpellDeliveryForm form,
        EssenceType materiaType,
        boolean mutationEnabled,
        String subMode
) {
    public FormPartBinding {
        if (form == null) form = SpellDeliveryForm.PROJECTILE;
        if (materiaType == null) materiaType = EssenceType.REGULAR;
        if (subMode == null || subMode.isBlank()) subMode = "default";

        // Backward-compatible promotion when deserializing legacy "summon" + sub_mode ("sentry"/"decoy")
        if (form == SpellDeliveryForm.SUMMON_AGENT && !"default".equalsIgnoreCase(subMode)) {
            SpellEidolonEntity.TriadArchetype legacyArch = SpellEidolonEntity.TriadArchetype.fromSubMode(subMode);
            if (legacyArch == SpellEidolonEntity.TriadArchetype.SENTRY) {
                form = SpellDeliveryForm.SUMMON_SENTRY;
            } else if (legacyArch == SpellEidolonEntity.TriadArchetype.DECOY) {
                form = SpellDeliveryForm.SUMMON_DECOY;
            }
        }
        if (form.isSummonForm() && "default".equalsIgnoreCase(subMode)) {
            subMode = switch (form) {
                case SUMMON_SENTRY -> "sentry";
                case SUMMON_DECOY -> "decoy";
                default -> "agent";
            };
        }
    }

    public static final Codec<FormPartBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpellDeliveryForm.CODEC.fieldOf("form").forGetter(FormPartBinding::form),
            EssenceType.CODEC.fieldOf("materia_type").forGetter(FormPartBinding::materiaType),
            Codec.BOOL.fieldOf("mutation_enabled").forGetter(FormPartBinding::mutationEnabled),
            Codec.STRING.optionalFieldOf("sub_mode", "default").forGetter(FormPartBinding::subMode)
    ).apply(instance, FormPartBinding::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FormPartBinding> STREAM_CODEC = StreamCodec.composite(
            SpellDeliveryForm.STREAM_CODEC, FormPartBinding::form,
            EssenceType.STREAM_CODEC, FormPartBinding::materiaType,
            ByteBufCodecs.BOOL, FormPartBinding::mutationEnabled,
            ByteBufCodecs.STRING_UTF8, FormPartBinding::subMode,
            FormPartBinding::new
    );

    public static FormPartBinding of(SpellDeliveryForm form, EssenceType materiaType, boolean mutationEnabled) {
        return new FormPartBinding(form, materiaType, mutationEnabled, "default");
    }

    public FormPartBinding withForm(SpellDeliveryForm newForm) {
        String nextSub = (newForm != null && newForm.isSummonForm())
                ? switch (newForm) {
                    case SUMMON_SENTRY -> "sentry";
                    case SUMMON_DECOY -> "decoy";
                    default -> "agent";
                }
                : this.subMode;
        return new FormPartBinding(newForm, this.materiaType, this.mutationEnabled, nextSub);
    }

    public FormPartBinding withMateria(EssenceType newType) {
        return new FormPartBinding(this.form, newType, this.mutationEnabled, this.subMode);
    }

    public FormPartBinding withMutationEnabled(boolean enabled) {
        return new FormPartBinding(this.form, this.materiaType, enabled, this.subMode);
    }

    public SpellEidolonEntity.TriadArchetype resolveSummonArchetype() {
        SpellEidolonEntity.TriadArchetype direct = this.form.getSummonArchetype();
        return direct != null ? direct : SpellEidolonEntity.TriadArchetype.fromSubMode(this.subMode);
    }

    public EssenceType resolveEffectiveMateria(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        if (!materiaSpecificLock && this.materiaType == EssenceType.REGULAR && fallbackCellType != null) {
            return fallbackCellType;
        }
        return this.materiaType;
    }

    public SpellMutationProfile resolveMutation(boolean materiaSpecificLock, EssenceType fallbackCellType) {
        EssenceType effective = resolveEffectiveMateria(materiaSpecificLock, fallbackCellType);
        return SpellMutationRegistry.resolveForm(this.form, effective, this.mutationEnabled);
    }
}
