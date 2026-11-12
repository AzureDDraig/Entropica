package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Flow Modifiers governing spell multiplicity (Single 1x through Octuple 8x),
 * branching trajectories, leaping chains, satellite orbits, velocity biases, and trigger billing.
 */
public enum SpellFlowModifier implements StringRepresentable {
    SINGLE("single", "Single Cast", 1, 1.0F, 0.0F, false, true),
    DOUBLE_BIFURCATE("double_bifurcate", "Double Bifurcation", 2, 1.0F, 0.0F, false, false),
    TRIPLE_TRIFURCATE("triple_trifurcate", "Triple Trifurcation", 3, 1.0F, 0.0F, false, false),
    QUAD_SPREAD("quad_spread", "Quadruple Spread", 4, 1.0F, 0.0F, false, false),
    QUINTUPLE_ARC("quintuple_arc", "Quintuple Arc", 5, 1.0F, 0.0F, false, false),
    SEXTUPLE_RING("sextuple_ring", "Sextuple Ring", 6, 1.0F, 0.0F, false, false),
    SEPTUPLE_CROWN("septuple_crown", "Septuple Crown", 7, 1.0F, 0.0F, false, false),
    OCTUPLE_DOME("octuple_dome", "Octuple Hemispherical Dome", 8, 1.0F, 0.0F, false, false),

    CHAIN_LEAP("chain_leap", "Leaping Chain", 3, 1.1F, 0.0F, true, false),
    ORBIT_SATELLITE("orbit_satellite", "Satellite Orbit", 4, 0.85F, 0.0F, false, false),
    VOID_WARP("void_warp", "Void-Warp Relay", 2, 1.25F, 0.0F, true, false),
    FAST_DART("fast_dart", "Accelerated Dart", 1, 1.85F, 0.0F, false, false),
    SLOW_HEAVY("slow_heavy", "Deliberate Heavy Drift", 1, 0.45F, 0.40F, false, false),
    DELAYED_BLOOM("delayed_bloom", "Timed Delay Bloom", 2, 0.9F, 0.0F, true, false),
    PROXIMITY_TRIGGER("proximity_trigger", "Proximity Bubble Trigger", 2, 0.8F, 0.0F, true, false),
    CORPSE_BLOOM("corpse_bloom", "Corpse Bloom Contagion", 3, 1.0F, 0.0F, true, false),
    SELF_CAST("self_cast", "Self-Centered Channel", 1, 0.0F, 0.0F, false, true),
    SELF_TIMED("self_timed", "Self-Timed Contingency", 1, 0.0F, 0.0F, true, false);

    public static final Codec<SpellFlowModifier> CODEC = StringRepresentable.fromEnum(SpellFlowModifier::values);
    public static final StreamCodec<FriendlyByteBuf, SpellFlowModifier> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellFlowModifier.class));

    private final String id;
    private final String displayName;
    private final int defaultSubSpellCount;
    private final float velocityMultiplier;
    private final float innateAdvantageBonus;
    private final boolean dynamicTriggerBilling;
    private final boolean starterUnlocked;

    SpellFlowModifier(String id, String displayName, int defaultSubSpellCount,
                      float velocityMultiplier, float innateAdvantageBonus,
                      boolean dynamicTriggerBilling, boolean starterUnlocked) {
        this.id = id;
        this.displayName = displayName;
        this.defaultSubSpellCount = defaultSubSpellCount;
        this.velocityMultiplier = velocityMultiplier;
        this.innateAdvantageBonus = innateAdvantageBonus;
        this.dynamicTriggerBilling = dynamicTriggerBilling;
        this.starterUnlocked = starterUnlocked;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getDefaultSubSpellCount() {
        return this.defaultSubSpellCount;
    }

    public float getVelocityMultiplier() {
        return this.velocityMultiplier;
    }

    /**
     * Innate Materia Advantage score bonus (e.g. +0.40 on Slow Flow per Section 3).
     */
    public float getInnateAdvantageBonus() {
        return this.innateAdvantageBonus;
    }

    /**
     * True if this Flow Modifier uses the 25% Upfront / 75% Dynamic Trigger fuel billing model (Section 3.6).
     */
    public boolean usesDynamicTriggerBilling() {
        return this.dynamicTriggerBilling;
    }

    public boolean isStarterUnlocked() {
        return this.starterUnlocked;
    }

    public String getResearchNodeId() {
        return "spell_flow_" + this.id;
    }
}
