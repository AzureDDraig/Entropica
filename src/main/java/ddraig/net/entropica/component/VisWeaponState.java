package ddraig.net.entropica.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record VisWeaponState(
        EssenceType baseType,
        String activeProfile,
        String materialTier,
        String coreType,
        float baseDamage,
        float bonusSpeed,
        float absoluteSpeed,
        int innateSlots,
        List<EssenceType> slottedFragments,
        String activeSpell,
        boolean isAutonomous,
        boolean hasInitializedName,
        boolean hasOrbisSlot,
        ItemStack orbisCell
) {

    public static final VisWeaponState EMPTY = new VisWeaponState(EssenceType.REGULAR, "regular", "None", "None", 0f, 0f, 1.0f, 0, List.of(), "none", false, false, false, ItemStack.EMPTY);

    public static final Codec<VisWeaponState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EssenceType.CODEC.fieldOf("base_type").forGetter(VisWeaponState::baseType),
            Codec.STRING.fieldOf("active_profile").forGetter(VisWeaponState::activeProfile),
            Codec.STRING.fieldOf("material_tier").forGetter(VisWeaponState::materialTier),
            Codec.STRING.fieldOf("core_type").forGetter(VisWeaponState::coreType),
            Codec.FLOAT.fieldOf("base_damage").forGetter(VisWeaponState::baseDamage),
            Codec.FLOAT.fieldOf("bonus_speed").forGetter(VisWeaponState::bonusSpeed),
            Codec.FLOAT.fieldOf("absolute_speed").forGetter(VisWeaponState::absoluteSpeed),
            Codec.INT.fieldOf("innate_slots").forGetter(VisWeaponState::innateSlots),
            EssenceType.CODEC.listOf().fieldOf("slotted_fragments").forGetter(VisWeaponState::slottedFragments),
            Codec.STRING.fieldOf("active_spell").forGetter(VisWeaponState::activeSpell),
            Codec.BOOL.fieldOf("is_autonomous").forGetter(VisWeaponState::isAutonomous),
            Codec.BOOL.fieldOf("has_initialized_name").forGetter(VisWeaponState::hasInitializedName),
            Codec.BOOL.fieldOf("has_orbis_slot").forGetter(VisWeaponState::hasOrbisSlot),
            ItemStack.OPTIONAL_CODEC.fieldOf("orbis_cell").forGetter(VisWeaponState::orbisCell)
    ).apply(instance, VisWeaponState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VisWeaponState> STREAM_CODEC = StreamCodec.of(
            (buf, state) -> {
                EssenceType.STREAM_CODEC.encode(buf, state.baseType());
                ByteBufCodecs.STRING_UTF8.encode(buf, state.activeProfile());
                ByteBufCodecs.STRING_UTF8.encode(buf, state.materialTier());
                ByteBufCodecs.STRING_UTF8.encode(buf, state.coreType());
                ByteBufCodecs.FLOAT.encode(buf, state.baseDamage());
                ByteBufCodecs.FLOAT.encode(buf, state.bonusSpeed());
                ByteBufCodecs.FLOAT.encode(buf, state.absoluteSpeed());
                ByteBufCodecs.VAR_INT.encode(buf, state.innateSlots());
                EssenceType.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, state.slottedFragments());
                ByteBufCodecs.STRING_UTF8.encode(buf, state.activeSpell());
                ByteBufCodecs.BOOL.encode(buf, state.isAutonomous());
                ByteBufCodecs.BOOL.encode(buf, state.hasInitializedName());
                ByteBufCodecs.BOOL.encode(buf, state.hasOrbisSlot());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, state.orbisCell());
            },
            buf -> new VisWeaponState(
                    EssenceType.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    EssenceType.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)
            )
    );

    public boolean isAltered() {
        return !this.baseType.getSerializedName().equalsIgnoreCase(this.activeProfile);
    }

    public VisWeaponState withInitializedName(boolean named) {
        return new VisWeaponState(baseType, activeProfile, materialTier, coreType, baseDamage, bonusSpeed, absoluteSpeed, innateSlots, slottedFragments, activeSpell, isAutonomous, named, hasOrbisSlot, orbisCell);
    }

    public VisWeaponState withOrbisCell(ItemStack newCell) {
        return new VisWeaponState(baseType, activeProfile, materialTier, coreType, baseDamage, bonusSpeed, absoluteSpeed, innateSlots, slottedFragments, activeSpell, isAutonomous, hasInitializedName, hasOrbisSlot, newCell);
    }

    public static class Builder {
        private final EssenceType baseType;
        private String activeProfile;
        private String materialTier = "Arcanite";
        private String coreType = "Base Arcanite Core";
        private final float baseDamage;
        private float bonusSpeed = 0f;
        private float absoluteSpeed = 1.0f;
        private final int innateSlots;
        private final List<EssenceType> fragments = new ArrayList<>();
        private String activeSpell = "none";
        private boolean isAutonomous = false;
        private boolean hasInitializedName = false;
        private boolean hasOrbisSlot = false;
        private ItemStack orbisCell = ItemStack.EMPTY;

        public Builder(EssenceType baseType, float baseDamage, int innateSlots) {
            this.baseType = baseType;
            this.activeProfile = baseType.getSerializedName();
            this.baseDamage = baseDamage;
            this.innateSlots = innateSlots;
        }

        public Builder setActiveProfile(String profileId) { this.activeProfile = profileId; return this; }
        public Builder material(String materialTier) { this.materialTier = materialTier; return this; }
        public Builder coreType(String coreType) { this.coreType = coreType; return this; }
        public Builder speed(float bonusSpeed) { this.bonusSpeed = bonusSpeed; return this; }
        public Builder absoluteSpeed(float absoluteSpeed) { this.absoluteSpeed = absoluteSpeed; return this; }
        public Builder spell(String activeSpell) { this.activeSpell = activeSpell; return this; }
        public Builder autonomous(boolean isAutonomous) { this.isAutonomous = isAutonomous; return this; }
        public Builder named(boolean hasInitializedName) { this.hasInitializedName = hasInitializedName; return this; }
        public Builder hasOrbisSlot(boolean hasOrbisSlot) { this.hasOrbisSlot = hasOrbisSlot; return this; }
        public Builder orbisCell(ItemStack orbisCell) { this.orbisCell = orbisCell; return this; }
        public Builder addFragment(EssenceType fragmentType) { this.fragments.add(fragmentType); return this; }

        public VisWeaponState build() {
            return new VisWeaponState(baseType, activeProfile, materialTier, coreType, baseDamage, bonusSpeed, absoluteSpeed, innateSlots, fragments, activeSpell, isAutonomous, hasInitializedName, hasOrbisSlot, orbisCell);
        }
    }
}