package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Canonical Delivery Forms / Spatial Shapes in Entropica's Spellcrafting Engine.
 * All forms carry a uniform 1.0x base fuel cost and 1.0x base tier potency (Section 7.4).
 *
 * <p>The three Eidolon Summon archetypes ({@link #SUMMON_AGENT}, {@link #SUMMON_SENTRY},
 * and {@link #SUMMON_DECOY}) are distinct selectable Delivery Form types during spell creation,
 * so the player chooses one summon type per spell.</p>
 */
public enum SpellDeliveryForm implements StringRepresentable {
    PROJECTILE("projectile", "Projectile", false, true),
    BEAM("beam", "Continuous Beam", true, true),
    RADIAL_BURST("radial_burst", "Radial Burst", false, false),
    TOUCH_STRIKE("touch_strike", "Touch / Strike", false, false),
    AURA("aura", "Aura / Self Shroud", true, true),
    GLYPH("glyph", "Glyph / Magic Circle", false, false),
    CONE("cone", "Cone / Spray", false, false),
    SKYFALL("skyfall", "Rain / Skyfall", false, false),
    SUMMON_AGENT("summon_agent", "Summon: Autonomous Agent", false, false),
    SUMMON_SENTRY("summon_sentry", "Summon: Anchored Sentry", false, false),
    SUMMON_DECOY("summon_decoy", "Summon: Phantom Decoy", false, false);

    public static final Codec<SpellDeliveryForm> CODEC = Codec.STRING.comapFlatMap(
            raw -> {
                if ("summon".equalsIgnoreCase(raw)) {
                    return DataResult.success(SUMMON_AGENT);
                }
                for (SpellDeliveryForm f : values()) {
                    if (f.id.equalsIgnoreCase(raw) || f.name().equalsIgnoreCase(raw)) {
                        return DataResult.success(f);
                    }
                }
                return DataResult.error(() -> "Unknown SpellDeliveryForm: " + raw);
            },
            SpellDeliveryForm::getSerializedName
    );
    public static final StreamCodec<FriendlyByteBuf, SpellDeliveryForm> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellDeliveryForm.class));

    private static final int[] BEAM_REACH_BY_TIER = {8, 12, 16, 20, 24, 28, 32, 36, 42, 48};
    private static final int[] BURST_DIAMETER_BY_TIER = {5, 9, 13, 17, 23, 29, 35, 41, 47, 53};
    private static final int[] CONSTRUCTION_SPAN_BY_TIER = {5, 9, 13, 17, 21, 25, 29, 33, 37, 41};

    private final String id;
    private final String displayName;
    private final boolean channeled;
    private final boolean starterUnlocked;

    SpellDeliveryForm(String id, String displayName, boolean channeled, boolean starterUnlocked) {
        this.id = id;
        this.displayName = displayName;
        this.channeled = channeled;
        this.starterUnlocked = starterUnlocked;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * True for Continuous Beams and Auras (eligible for the Staff's -25% continuous fuel drain bonus).
     */
    public boolean isChanneled() {
        return this.channeled;
    }

    public boolean isStarterUnlocked() {
        return this.starterUnlocked;
    }

    /**
     * True if this Delivery Form summons an Eidolon construct ({@link #SUMMON_AGENT},
     * {@link #SUMMON_SENTRY}, or {@link #SUMMON_DECOY}).
     */
    public boolean isSummonForm() {
        return this == SUMMON_AGENT || this == SUMMON_SENTRY || this == SUMMON_DECOY;
    }

    @Nullable
    public SpellEidolonEntity.TriadArchetype getSummonArchetype() {
        return switch (this) {
            case SUMMON_AGENT -> SpellEidolonEntity.TriadArchetype.AGENT;
            case SUMMON_SENTRY -> SpellEidolonEntity.TriadArchetype.SENTRY;
            case SUMMON_DECOY -> SpellEidolonEntity.TriadArchetype.DECOY;
            default -> null;
        };
    }

    public static SpellDeliveryForm fromSummonArchetype(SpellEidolonEntity.TriadArchetype archetype) {
        return switch (archetype) {
            case SENTRY -> SUMMON_SENTRY;
            case DECOY -> SUMMON_DECOY;
            case AGENT -> SUMMON_AGENT;
        };
    }

    public String getResearchNodeId() {
        return "spell_form_" + this.id;
    }

    public static int getBeamReachBlocks(int potencyTier, boolean earthLineageBonus) {
        int idx = Math.clamp(potencyTier, 1, 10) - 1;
        int base = BEAM_REACH_BY_TIER[idx];
        return earthLineageBonus ? (int) Math.round(base * 1.5D) : base;
    }

    public static int getBurstOrGlyphDiameter(int potencyTier) {
        int idx = Math.clamp(potencyTier, 1, 10) - 1;
        return BURST_DIAMETER_BY_TIER[idx];
    }

    public static double getCombatRadiusBlocks(SpellDeliveryForm form, int potencyTier) {
        int clamped = Math.clamp(potencyTier, 1, 10);
        return switch (form) {
            case RADIAL_BURST -> 3.0D + (clamped * 1.4D);
            case AURA -> 3.0D + (clamped * 0.7D);
            case GLYPH -> 2.5D + (clamped * 0.65D);
            case CONE -> 4.0D + (clamped * 1.2D);
            case SKYFALL -> 5.0D + (clamped * 1.6D);
            case TOUCH_STRIKE -> 3.5D + (clamped * 0.35D);
            case PROJECTILE -> 1.5D + (clamped * 0.25D);
            case BEAM -> getBeamReachBlocks(clamped, false);
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> 8.0D + (clamped * 1.5D);
        };
    }

    public static int getConstructionSpanBlocks(int potencyTier, boolean earthLineageBonus) {
        int idx = Math.clamp(potencyTier, 1, 10) - 1;
        int base = CONSTRUCTION_SPAN_BY_TIER[idx];
        return earthLineageBonus ? (int) Math.round(base * 1.5D) : base;
    }
}
