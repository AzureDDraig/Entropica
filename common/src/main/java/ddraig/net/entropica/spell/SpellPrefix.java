package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The four canonical Prefix Disciplines of Entropica's Spellcrafting Engine.
 * Governs legal targeting rules, Zero-Harm guarantees, container protection, and friendly fire.
 */
public enum SpellPrefix implements StringRepresentable {
    COMBAT("combat", "Combat", true, false, true, false),
    UTILITY("utility", "Utility", false, true, true, false),
    MISC("misc", "Misc", false, false, true, false),
    UNIQUE("unique", "Unique", true, true, true, true);

    public static final Codec<SpellPrefix> CODEC = StringRepresentable.fromEnum(SpellPrefix::values);
    public static final StreamCodec<FriendlyByteBuf, SpellPrefix> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellPrefix.class));

    private final String id;
    private final String displayName;
    private final boolean allowsEntityDamage;
    private final boolean allowsBlockModification;
    private final boolean protectsContainers;
    private final boolean immutableRelic;

    SpellPrefix(String id, String displayName, boolean allowsEntityDamage,
                boolean allowsBlockModification, boolean protectsContainers, boolean immutableRelic) {
        this.id = id;
        this.displayName = displayName;
        this.allowsEntityDamage = allowsEntityDamage;
        this.allowsBlockModification = allowsBlockModification;
        this.protectsContainers = protectsContainers;
        this.immutableRelic = immutableRelic;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * True for Combat and Unique; strictly false for Utility (Zero-Harm rule) and Misc.
     */
    public boolean allowsEntityDamage() {
        return this.allowsEntityDamage;
    }

    public boolean enforcesZeroEntityHarm() {
        return !this.allowsEntityDamage;
    }

    public boolean allowsBlockModification() {
        return this.allowsBlockModification;
    }

    /**
     * Enforces Chest/Container Immunity on Transmutation and Excavation spells.
     */
    public boolean protectsContainers() {
        return this.protectsContainers;
    }

    /**
     * Unique spells cannot be inspected, edited, duplicated, or wiped back into Blank Spellgems.
     */
    public boolean isImmutableRelic() {
        return this.immutableRelic;
    }
}
