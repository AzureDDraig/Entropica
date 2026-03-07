package ddraig.net.entropica.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record VisWeaponState(EssenceType baseType, String activeProfile, float baseDamage, int innateSlots, List<EssenceType> slottedFragments) {

    public static final VisWeaponState EMPTY = new VisWeaponState(EssenceType.REGULAR, "regular", 0f, 0, List.of());

    public static final Codec<VisWeaponState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EssenceType.CODEC.fieldOf("base_type").forGetter(VisWeaponState::baseType),
            Codec.STRING.fieldOf("active_profile").forGetter(VisWeaponState::activeProfile),
            Codec.FLOAT.fieldOf("base_damage").forGetter(VisWeaponState::baseDamage),
            Codec.INT.fieldOf("innate_slots").forGetter(VisWeaponState::innateSlots),
            EssenceType.CODEC.listOf().fieldOf("slotted_fragments").forGetter(VisWeaponState::slottedFragments)
    ).apply(instance, VisWeaponState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VisWeaponState> STREAM_CODEC = StreamCodec.composite(
            EssenceType.STREAM_CODEC, VisWeaponState::baseType,
            ByteBufCodecs.STRING_UTF8, VisWeaponState::activeProfile,
            ByteBufCodecs.FLOAT, VisWeaponState::baseDamage,
            ByteBufCodecs.VAR_INT, VisWeaponState::innateSlots,
            EssenceType.STREAM_CODEC.apply(ByteBufCodecs.list()), VisWeaponState::slottedFragments,
            VisWeaponState::new
    );

    public boolean isAltered() {
        return !this.baseType.getSerializedName().equalsIgnoreCase(this.activeProfile);
    }

    public static class Builder {
        private final EssenceType baseType;
        private String activeProfile;
        private final float baseDamage;
        private final int innateSlots;
        private final List<EssenceType> fragments = new ArrayList<>();

        public Builder(EssenceType baseType, float baseDamage, int innateSlots) {
            this.baseType = baseType;
            this.activeProfile = baseType.getSerializedName();
            this.baseDamage = baseDamage;
            this.innateSlots = innateSlots;
        }

        public Builder setActiveProfile(String profileId) {
            this.activeProfile = profileId;
            return this;
        }

        public Builder addFragment(EssenceType fragmentType) {
            this.fragments.add(fragmentType);
            return this;
        }

        public VisWeaponState build() {
            return new VisWeaponState(baseType, activeProfile, baseDamage, innateSlots, fragments);
        }
    }
}