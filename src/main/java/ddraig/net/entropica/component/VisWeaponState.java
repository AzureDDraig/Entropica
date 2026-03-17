package ddraig.net.entropica.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record VisWeaponState(
        EssenceType baseType,
        String activeProfile,
        String materialTier,
        float baseDamage,
        float bonusSpeed,
        int innateSlots,
        List<EssenceType> slottedFragments,
        String activeSpell,
        boolean isAutonomous,
        boolean hasInitializedName
) {

    public static final VisWeaponState EMPTY = new VisWeaponState(EssenceType.REGULAR, "regular", "None", 0f, 0f, 0, List.of(), "none", false, false);

    public static final Codec<VisWeaponState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EssenceType.CODEC.fieldOf("base_type").forGetter(VisWeaponState::baseType),
            Codec.STRING.fieldOf("active_profile").forGetter(VisWeaponState::activeProfile),
            Codec.STRING.fieldOf("material_tier").forGetter(VisWeaponState::materialTier),
            Codec.FLOAT.fieldOf("base_damage").forGetter(VisWeaponState::baseDamage),
            Codec.FLOAT.fieldOf("bonus_speed").forGetter(VisWeaponState::bonusSpeed),
            Codec.INT.fieldOf("innate_slots").forGetter(VisWeaponState::innateSlots),
            EssenceType.CODEC.listOf().fieldOf("slotted_fragments").forGetter(VisWeaponState::slottedFragments),
            Codec.STRING.fieldOf("active_spell").forGetter(VisWeaponState::activeSpell),
            Codec.BOOL.fieldOf("is_autonomous").forGetter(VisWeaponState::isAutonomous),
            Codec.BOOL.fieldOf("has_initialized_name").forGetter(VisWeaponState::hasInitializedName)
    ).apply(instance, VisWeaponState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VisWeaponState> STREAM_CODEC = StreamCodec.of(
            (buf, state) -> {
                EssenceType.STREAM_CODEC.encode(buf, state.baseType());
                ByteBufCodecs.STRING_UTF8.encode(buf, state.activeProfile());
                ByteBufCodecs.STRING_UTF8.encode(buf, state.materialTier());
                ByteBufCodecs.FLOAT.encode(buf, state.baseDamage());
                ByteBufCodecs.FLOAT.encode(buf, state.bonusSpeed());
                ByteBufCodecs.VAR_INT.encode(buf, state.innateSlots());
                EssenceType.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, state.slottedFragments());
                ByteBufCodecs.STRING_UTF8.encode(buf, state.activeSpell());
                ByteBufCodecs.BOOL.encode(buf, state.isAutonomous());
                ByteBufCodecs.BOOL.encode(buf, state.hasInitializedName());
            },
            buf -> new VisWeaponState(
                    EssenceType.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    EssenceType.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    public boolean isAltered() {
        return !this.baseType.getSerializedName().equalsIgnoreCase(this.activeProfile);
    }

    public VisWeaponState withInitializedName(boolean named) {
        return new VisWeaponState(baseType, activeProfile, materialTier, baseDamage, bonusSpeed, innateSlots, slottedFragments, activeSpell, isAutonomous, named);
    }

    public static class Builder {
        private final EssenceType baseType;
        private String activeProfile;
        private String materialTier = "Arcanite";
        private final float baseDamage;
        private float bonusSpeed = 0f;
        private final int innateSlots;
        private final List<EssenceType> fragments = new ArrayList<>();
        private String activeSpell = "none";
        private boolean isAutonomous = false;
        private boolean hasInitializedName = false;

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

        public Builder material(String materialTier) {
            this.materialTier = materialTier;
            return this;
        }

        public Builder speed(float bonusSpeed) {
            this.bonusSpeed = bonusSpeed;
            return this;
        }

        public Builder spell(String activeSpell) {
            this.activeSpell = activeSpell;
            return this;
        }

        public Builder autonomous(boolean isAutonomous) {
            this.isAutonomous = isAutonomous;
            return this;
        }

        public Builder named(boolean hasInitializedName) {
            this.hasInitializedName = hasInitializedName;
            return this;
        }

        public Builder addFragment(EssenceType fragmentType) {
            this.fragments.add(fragmentType);
            return this;
        }

        public VisWeaponState build() {
            return new VisWeaponState(baseType, activeProfile, materialTier, baseDamage, bonusSpeed, innateSlots, fragments, activeSpell, isAutonomous, hasInitializedName);
        }
    }
}