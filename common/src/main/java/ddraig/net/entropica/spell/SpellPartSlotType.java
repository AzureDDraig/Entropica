package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies each independent modular part within a Spell Assembly.
 * Every SpellPartSlotType carries its own assigned {@link ddraig.net.entropica.api.EssenceType},
 * its own independent {@code mutationEnabled} toggle, and contributes its proportional share
 * of Materia fuel cost in that assigned Materia type.
 */
public enum SpellPartSlotType implements StringRepresentable {
    /**
     * Governs cast multiplicity (1x-8x), branching, chaining, orbiting, velocity, and triggers.
     * Contributes 20% of each sub-spell's base tier cost in the Flow part's Materia type.
     */
    FLOW("flow", "Flow", 0.20D),

    /**
     * Governs the spatial delivery vector (Projectile, Beam, Burst, Strike, Aura, Glyph, Cone, Skyfall, Summon).
     * Contributes 40% of its sub-spell's base tier cost in the Form part's Materia type.
     */
    FORM("form", "Form", 0.40D),

    /**
     * Governs the primary action verb (Harm, Kinetic, Ward, Blink, Transmute, Affliction, Restoration, Conjure, etc.).
     * Contributes 40% of its sub-spell's base tier cost in the Core Effect part's Materia type.
     * Notice: 20% (Flow) + 40% (Form) + 40% (Core Effect) = 100% (exact 1.0x Base Cast Cost per sub-spell).
     */
    CORE_EFFECT("core_effect", "Core Effect", 0.40D),

    /**
     * Optional Dual-Verb Hybrid Core Effect slot on a sub-spell.
     * Per Section 5 Dual-Verb Hybridization rules, slotting a second Core Effect verb scales that sub-spell's
     * total fuel cost to 2.0x (+100% base cost billed in the Secondary Effect part's own Materia type).
     */
    SECONDARY_EFFECT("secondary_effect", "Secondary Effect", 1.00D);

    public static final Codec<SpellPartSlotType> CODEC = StringRepresentable.fromEnum(SpellPartSlotType::values);
    public static final StreamCodec<FriendlyByteBuf, SpellPartSlotType> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellPartSlotType.class));

    private final String id;
    private final String displayName;
    private final double baseCostWeight;

    SpellPartSlotType(String id, String displayName, double baseCostWeight) {
        this.id = id;
        this.displayName = displayName;
        this.baseCostWeight = baseCostWeight;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public double getBaseCostWeight() {
        return this.baseCostWeight;
    }
}
