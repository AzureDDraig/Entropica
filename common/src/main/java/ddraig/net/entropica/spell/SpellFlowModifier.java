package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Pure Flow Modifiers governing how carrier payloads travel, branch, chain, orbit, or warp in space.
 *
 * <ul>
 *     <li><b>Spell Multi-Cast Count</b> ({@code Single 1x} through {@code Octuple 8x}) is configured independently
 *         via {@link ddraig.net.entropica.spell.assembly.FlowPartBinding#subSpellCount()} and executes all active
 *         sub-spells at each carrier payload's point of contact.</li>
 *     <li><b>Trigger Logic</b> ({@code On-Entity-Hit}, {@code On-Block-Collision}, {@code Timed Fuse},
 *         {@code On-Proximity}, {@code On-Release}, {@code Corpse Bloom}, {@code Self}, {@code Self-Timed}) is
 *         configured independently on the Outer Trigger Ring via {@link SpellTriggerType}.</li>
 *     <li><b>{@link #VOID_WARP}</b> is a Void Travel Mutation (Guide Section 3.4) that requires Void Materia
 *         or a Void-created Materia fusion on the Flow Ring with {@code mutationEnabled == true}.</li>
 * </ul>
 */
public enum SpellFlowModifier implements StringRepresentable {
    SINGLE("single", "Normal Flow", 1, 1.00F, 1.00F, 0.0F, false, true),
    DOUBLE_BIFURCATE("double_bifurcate", "Bifurcation", 2, 1.85F, 1.00F, 0.0F, false, true),
    TRIPLE_TRIFURCATE("triple_trifurcate", "Trifurcation", 3, 2.65F, 1.00F, 0.0F, false, false),
    OCTUPLE_DOME("octuple_dome", "Omnication Dome", 8, 5.50F, 1.00F, 0.0F, false, false),
    CHAIN_LEAP("chain_leap", "Leaping Chain", 3, 2.25F, 1.10F, 0.0F, true, false),
    ORBIT_SATELLITE("orbit_satellite", "Satellite Orbit", 4, 2.80F, 0.85F, 0.0F, false, false),
    FAST_DART("fast_dart", "Accelerated Fast", 1, 1.15F, 2.35F, 0.0F, false, false),
    SLOW_HEAVY("slow_heavy", "Deliberate Slow", 1, 1.15F, 0.45F, 0.40F, false, false),
    VOID_WARP("void_warp", "Void-Warp Relay", 1, 1.35F, 1.25F, 0.0F, true, true);

    public static final Codec<SpellFlowModifier> CODEC = Codec.STRING.xmap(
            SpellFlowModifier::fromSerializedOrLegacy,
            SpellFlowModifier::getSerializedName
    );
    public static final StreamCodec<FriendlyByteBuf, SpellFlowModifier> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellFlowModifier.class));

    private final String id;
    private final String displayName;
    private final int carrierPayloadCount;
    private final float payloadCostMultiplier;
    private final float velocityMultiplier;
    private final float innateAdvantageBonus;
    private final boolean dynamicTriggerBilling;
    private final boolean starterUnlocked;

    SpellFlowModifier(String id, String displayName, int carrierPayloadCount, float payloadCostMultiplier,
                      float velocityMultiplier, float innateAdvantageBonus,
                      boolean dynamicTriggerBilling, boolean starterUnlocked) {
        this.id = id;
        this.displayName = displayName;
        this.carrierPayloadCount = carrierPayloadCount;
        this.payloadCostMultiplier = payloadCostMultiplier;
        this.velocityMultiplier = velocityMultiplier;
        this.innateAdvantageBonus = innateAdvantageBonus;
        this.dynamicTriggerBilling = dynamicTriggerBilling;
        this.starterUnlocked = starterUnlocked;
    }

    public static SpellFlowModifier fromSerializedOrLegacy(String raw) {
        if (raw == null || raw.isBlank()) {
            return SINGLE;
        }
        String norm = raw.trim().toLowerCase();
        for (SpellFlowModifier val : values()) {
            if (val.id.equals(norm) || val.name().toLowerCase().equals(norm)) {
                return val;
            }
        }
        return switch (norm) {
            case "normal", "direct", "delayed_bloom", "proximity_trigger", "corpse_bloom", "self_cast", "self_timed" -> SINGLE;
            case "quad_spread", "quintuple_arc" -> TRIPLE_TRIFURCATE;
            case "sextuple_ring", "septuple_crown", "omnication_dome", "omnication" -> OCTUPLE_DOME;
            default -> SINGLE;
        };
    }

    public static String getCastCountTitle(int subSpellCount) {
        return switch (Math.clamp(subSpellCount, 1, 8)) {
            case 1 -> "Single Cast (1x)";
            case 2 -> "Double Cast (2x)";
            case 3 -> "Triple Cast (3x)";
            case 4 -> "Quadruple Cast (4x)";
            case 5 -> "Quintuple Cast (5x)";
            case 6 -> "Sextuple Cast (6x)";
            case 7 -> "Septuple Cast (7x)";
            default -> "Octuple Cast (8x)";
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getCarrierPayloadCount() {
        return this.carrierPayloadCount;
    }

    public float getPayloadCostMultiplier() {
        return this.payloadCostMultiplier;
    }

    public int getDefaultSubSpellCount() {
        return 1;
    }

    public float getVelocityMultiplier() {
        return this.velocityMultiplier;
    }

    public float getInnateAdvantageBonus() {
        return this.innateAdvantageBonus;
    }

    public boolean usesDynamicTriggerBilling() {
        return this.dynamicTriggerBilling;
    }

    public boolean requiresVoidTravelMutation() {
        return this == VOID_WARP;
    }

    public boolean isStarterUnlocked() {
        return this.starterUnlocked;
    }

    public String getResearchNodeId() {
        return "spell_flow_" + this.id;
    }
}
