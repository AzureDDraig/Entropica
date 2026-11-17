package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Canonical 9-stop Outer Rim Travel Distance Circle on the Arcane Scribing Table:
 * {@code 0m -> 8m -> 16m -> 24m -> 32m -> 48m -> 64m -> 96m -> 128m}.
 *
 * <p>Governs spell travel & targeting reach across Projectiles, Flow Carriers, Beams,
 * Blink / Position Swap, Void-Warp Relays, Grapple Tethers, and Glyph / Skyfall / Summon placement,
 * while applying a balanced Materia fuel cost multiplier relative to the {@code 16m} baseline.</p>
 */
public enum SpellTravelDistance implements StringRepresentable {
    DIST_0(0, "0m", "Point-Blank Origin (0m)", 0.75D,
            "Discharges immediately at caster origin (0m travel) with -25% Materia cost."),
    DIST_8(8, "8m", "Close Reach (8m)", 0.85D,
            "Compact 8-block travel cap (0.5x standard reach) with -15% Materia cost."),
    DIST_16(16, "16m", "Standard Reach (16m)", 1.00D,
            "Canonical 16-block baseline spell travel distance at 1.0x Materia cost."),
    DIST_24(24, "24m", "Extended Reach (24m)", 1.15D,
            "Extends spell travel & targeting reach to 24 blocks (1.5x standard, 1.15x fuel)."),
    DIST_32(32, "32m", "Mid-Long Reach (32m)", 1.30D,
            "Doubles standard spell travel distance to 32 blocks (2.0x standard, 1.30x fuel)."),
    DIST_48(48, "48m", "Far Reach (48m)", 1.60D,
            "Triples standard spell travel distance to 48 blocks (3.0x standard, 1.60x fuel)."),
    DIST_64(64, "64m", "Sniper Reach (64m)", 1.95D,
            "Quadruples standard spell travel distance to 64 blocks (4.0x standard, 1.95x fuel)."),
    DIST_96(96, "96m", "Artillery Span (96m)", 2.60D,
            "Long-range 96-block spell projection across valleys (6.0x standard, 2.60x fuel)."),
    DIST_128(128, "128m", "Horizon Span (128m)", 3.40D,
            "Maximum 128-block horizon-spanning spell travel distance (8.0x standard, 3.40x fuel).");

    public static final int STANDARD_METERS = 16;

    public static final Codec<SpellTravelDistance> CODEC =
            StringRepresentable.fromEnum(SpellTravelDistance::values);

    public static final StreamCodec<FriendlyByteBuf, SpellTravelDistance> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellTravelDistance.class));

    private final int meters;
    private final String shortLabel;
    private final String displayName;
    private final double costMultiplier;
    private final String summaryDescription;

    SpellTravelDistance(int meters, String shortLabel, String displayName, double costMultiplier, String summaryDescription) {
        this.meters = meters;
        this.shortLabel = shortLabel;
        this.displayName = displayName;
        this.costMultiplier = costMultiplier;
        this.summaryDescription = summaryDescription;
    }

    public int meters() {
        return this.meters;
    }

    public String shortLabel() {
        return this.shortLabel;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public double getCostMultiplier() {
        return this.costMultiplier;
    }

    public String getSummaryDescription() {
        return this.summaryDescription;
    }

    /**
     * Multiplier relative to the {@code 16m} standard travel distance:
     * {@code 0.0F} at {@code 0m}, {@code 0.5F} at {@code 8m}, {@code 1.0F} at {@code 16m}, ..., {@code 8.0F} at {@code 128m}.
     */
    public float getDistanceScaleFromStandard() {
        return this.meters <= 0 ? 0.0F : (this.meters / (float) STANDARD_METERS);
    }

    public boolean isPointBlankZero() {
        return this.meters <= 0;
    }

    /**
     * Resolves Beam reach (in blocks) for the given Potency Tier and this Distance Circle stop,
     * before Apparatus and Form Mutation multipliers.
     */
    public double resolveBaseBeamReachBlocks(int potencyTier, boolean combatPrefix, boolean earthLineageBonus) {
        if (this.meters <= 0) {
            return 1.75D;
        }
        int tierBase = combatPrefix
                ? SpellDeliveryForm.getCombatBeamReachBlocks(potencyTier, earthLineageBonus)
                : SpellDeliveryForm.getBeamReachBlocks(potencyTier, earthLineageBonus);
        return Math.min(160.0D, tierBase * (this.meters / (double) STANDARD_METERS));
    }

    /**
     * Snaps any raw integer block distance to the nearest canonical stop on the 9-stop Distance Circle.
     */
    public static SpellTravelDistance snapFromMeters(int rawMeters) {
        SpellTravelDistance best = DIST_16;
        int bestDiff = Integer.MAX_VALUE;
        for (SpellTravelDistance step : values()) {
            int diff = Math.abs(step.meters - rawMeters);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = step;
            }
        }
        return best;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.shortLabel;
    }
}
