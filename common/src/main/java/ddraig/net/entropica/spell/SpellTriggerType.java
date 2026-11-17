package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The 8 canonical Spell Trigger Conditions from Spellcrafting System Guide Section 3.5,
 * selectable on the Outer Trigger Ring of the Arcane Scribing Table independently of
 * {@link SpellFlowModifier} and Multi-Cast count ({@code Single 1x} .. {@code Octuple 8x}).
 */
public enum SpellTriggerType implements StringRepresentable {
    ON_ENTITY_HIT("on_entity_hit", "On-Entity-Hit", 1.00F, false, true),
    ON_BLOCK_COLLISION("on_block_collision", "On-Block-Collision", 1.00F, false, true),
    ON_TIMER("on_timer", "Timed Fuse", 1.20F, true, false),
    ON_PROXIMITY("on_proximity", "On-Proximity", 1.25F, true, false),
    ON_RELEASE("on_release", "On-Release", 1.10F, false, true),
    ON_DEATH("on_death", "Corpse Bloom", 1.45F, true, false),
    SELF("self", "Self", 1.00F, false, true),
    SELF_TIMED("self_timed", "Self-Timed", 1.10F, true, false);

    public static final Codec<SpellTriggerType> CODEC = Codec.STRING.xmap(
            SpellTriggerType::fromSerializedOrLegacy,
            SpellTriggerType::getSerializedName
    );
    public static final StreamCodec<FriendlyByteBuf, SpellTriggerType> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellTriggerType.class));

    private final String id;
    private final String displayName;
    private final float costMultiplier;
    private final boolean dynamicTriggerBilling;
    private final boolean starterUnlocked;

    SpellTriggerType(String id, String displayName, float costMultiplier,
                     boolean dynamicTriggerBilling, boolean starterUnlocked) {
        this.id = id;
        this.displayName = displayName;
        this.costMultiplier = costMultiplier;
        this.dynamicTriggerBilling = dynamicTriggerBilling;
        this.starterUnlocked = starterUnlocked;
    }

    public static SpellTriggerType fromSerializedOrLegacy(String raw) {
        if (raw == null || raw.isBlank()) {
            return ON_ENTITY_HIT;
        }
        String norm = raw.trim().toLowerCase();
        for (SpellTriggerType val : values()) {
            if (val.id.equals(norm) || val.name().toLowerCase().equals(norm)) {
                return val;
            }
        }
        return inferLegacyTriggerFromFlowId(norm);
    }

    /**
     * Migrates legacy spells where trigger modes were previously stored in the {@code flow} field.
     */
    public static SpellTriggerType inferLegacyTriggerFromFlowId(String rawFlowId) {
        if (rawFlowId == null || rawFlowId.isBlank()) {
            return ON_ENTITY_HIT;
        }
        return switch (rawFlowId.trim().toLowerCase()) {
            case "delayed_bloom", "on_timer", "timer" -> ON_TIMER;
            case "proximity_trigger", "on_proximity", "proximity" -> ON_PROXIMITY;
            case "corpse_bloom", "on_death" -> ON_DEATH;
            case "self_cast", "self" -> SELF;
            case "self_timed", "contingency" -> SELF_TIMED;
            case "on_block_collision", "block_hit" -> ON_BLOCK_COLLISION;
            case "on_release", "release" -> ON_RELEASE;
            default -> ON_ENTITY_HIT;
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public float getCostMultiplier() {
        return this.costMultiplier;
    }

    public boolean usesDynamicTriggerBilling() {
        return this.dynamicTriggerBilling;
    }

    public boolean isStarterUnlocked() {
        return this.starterUnlocked;
    }

    public boolean isSelfCentered() {
        return this == SELF || this == SELF_TIMED;
    }

    public boolean hasTimerDial() {
        return this == ON_TIMER || this == SELF_TIMED;
    }

    public String getResearchNodeId() {
        return "spell_trigger_" + this.id;
    }
}
