package ddraig.net.entropica.spell.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Models permanent Spellgem binding defects caused by activating a Magic Circle ritual
 * while under-supplied with Materia (< 100% required supply, Section 8.4):
 * <ul>
 *     <li>37.5% chance: Unstable Casting only</li>
 *     <li>37.5% chance: Randomly Crippled Spell Part only</li>
 *     <li>25.0% chance: BOTH Unstable Casting AND Randomly Crippled Spell Part!</li>
 * </ul>
 */
public record SpellFlawProfile(
        boolean unstableCasting,
        Optional<CrippledParameter> crippledParameter,
        int crippledSlotIndex
) {
    public enum CrippledParameter implements StringRepresentable {
        RANGE("range", "Range (-50%)"),
        POWER("power", "Potency (-50%)"),
        DURATION("duration", "Duration (-50%)"),
        AREA("area", "Area of Effect (-50%)"),
        SUB_SPELL_SLOT("sub_spell_slot", "Starved Sub-Spell Slot (-65%)");

        public static final Codec<CrippledParameter> CODEC = StringRepresentable.fromEnum(CrippledParameter::values);
        public static final StreamCodec<FriendlyByteBuf, CrippledParameter> STREAM_CODEC =
                StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(CrippledParameter.class));

        private final String id;
        private final String label;

        CrippledParameter(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.id;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public static final float UNSTABLE_BACKFIRE_CHANCE = 0.25F;
    public static final SpellFlawProfile PERFECT = new SpellFlawProfile(false, Optional.empty(), -1);

    public static final Codec<SpellFlawProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("unstable_casting", false).forGetter(SpellFlawProfile::unstableCasting),
            CrippledParameter.CODEC.optionalFieldOf("crippled_parameter").forGetter(SpellFlawProfile::crippledParameter),
            Codec.INT.optionalFieldOf("crippled_slot_index", -1).forGetter(SpellFlawProfile::crippledSlotIndex)
    ).apply(instance, SpellFlawProfile::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellFlawProfile> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SpellFlawProfile::unstableCasting,
            ByteBufCodecs.optional(CrippledParameter.STREAM_CODEC), SpellFlawProfile::crippledParameter,
            ByteBufCodecs.VAR_INT, SpellFlawProfile::crippledSlotIndex,
            SpellFlawProfile::new
    );

    public boolean isFlawed() {
        return this.unstableCasting || this.crippledParameter.isPresent();
    }

    public boolean isCompoundDefect() {
        return this.unstableCasting && this.crippledParameter.isPresent();
    }

    public boolean crippledPotency() {
        return this.crippledParameter.isPresent();
    }

    public float getPotencyMultiplier() {
        return getPotencyMultiplierForSlot(0);
    }

    public float getRangeAndRadiusMultiplier() {
        return Math.min(getRangeMultiplier(), getAreaMultiplier());
    }

    public float getPotencyMultiplierForSlot(int slotIndex) {
        if (this.crippledParameter.isEmpty()) return 1.0F;
        return switch (this.crippledParameter.get()) {
            case POWER -> 0.50F;
            case SUB_SPELL_SLOT -> (this.crippledSlotIndex == slotIndex ? 0.35F : 1.0F);
            default -> 1.0F;
        };
    }

    public float getRangeMultiplier() {
        return this.crippledParameter.filter(p -> p == CrippledParameter.RANGE).isPresent() ? 0.50F : 1.0F;
    }

    public float getAreaMultiplier() {
        return this.crippledParameter.filter(p -> p == CrippledParameter.AREA).isPresent() ? 0.50F : 1.0F;
    }

    public float getDurationMultiplier() {
        return this.crippledParameter.filter(p -> p == CrippledParameter.DURATION).isPresent() ? 0.50F : 1.0F;
    }

    public static SpellFlawProfile rollUnderSuppliedFlaw(RandomSource random, int subSpellCount) {
        float roll = random.nextFloat();
        boolean both = roll < 0.25F;
        boolean unstable = both || (roll < 0.625F);
        boolean crippled = both || (roll >= 0.625F);

        if (!crippled) {
            return new SpellFlawProfile(true, Optional.empty(), -1);
        }

        CrippledParameter[] params = CrippledParameter.values();
        CrippledParameter chosen = params[random.nextInt(params.length)];
        int slotIdx = chosen == CrippledParameter.SUB_SPELL_SLOT
                ? random.nextInt(Math.max(1, subSpellCount))
                : -1;
        return new SpellFlawProfile(unstable, Optional.of(chosen), slotIdx);
    }
}
