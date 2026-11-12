package ddraig.net.entropica.spell.apparatus;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The 5 canonical Casting Apparatus classes and their fixed Spellgem capacities & modifiers
 * (Section 8.5.2):
 * <ul>
 *     <li>Engraved Weapon / Tool = 1 Gem (+15% Touch/Strike & Utility synergy)</li>
 *     <li>Arcanum Focus = 1 Gem (1.0x balanced baseline)</li>
 *     <li>Wand = 2 Gems (-25% cooldown, rapid projectile draw)</li>
 *     <li>Staff = 4 Gems (+20% Range & Potency, -25% Beam/Aura continuous fuel drain)</li>
 *     <li>Grimoire = 9 Gems (-15% base Materia fuel cost discount)</li>
 * </ul>
 */
public enum ApparatusType implements StringRepresentable {
    ENGRAVED_TOOL("engraved_tool", "Engraved Weapon / Tool", 1, 1.00F, 1.00F, 1.00F, 1.00F, 1.00F, false),
    ARCANUM_FOCUS("arcanum_focus", "Arcanum Focus", 1, 1.00F, 1.00F, 1.00F, 1.00F, 1.00F, true),
    WAND("wand", "Wand", 2, 0.75F, 1.00F, 1.00F, 1.00F, 1.00F, true),
    STAFF("staff", "Staff", 4, 1.00F, 1.00F, 0.75F, 1.20F, 1.20F, false),
    GRIMOIRE("grimoire", "Grimoire", 9, 1.00F, 0.85F, 0.85F, 1.00F, 1.00F, true);

    public static final Codec<ApparatusType> CODEC = StringRepresentable.fromEnum(ApparatusType::values);
    public static final StreamCodec<FriendlyByteBuf, ApparatusType> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(ApparatusType.class));

    private final String id;
    private final String displayName;
    private final int gemCapacity;
    private final float cooldownMultiplier;
    private final float baseFuelMultiplier;
    private final float channeledFuelMultiplier;
    private final float rangeMultiplier;
    private final float potencyMultiplier;
    private final boolean allowsOffhandCast;

    ApparatusType(String id, String displayName, int gemCapacity,
                  float cooldownMultiplier, float baseFuelMultiplier, float channeledFuelMultiplier,
                  float rangeMultiplier, float potencyMultiplier, boolean allowsOffhandCast) {
        this.id = id;
        this.displayName = displayName;
        this.gemCapacity = gemCapacity;
        this.cooldownMultiplier = cooldownMultiplier;
        this.baseFuelMultiplier = baseFuelMultiplier;
        this.channeledFuelMultiplier = channeledFuelMultiplier;
        this.rangeMultiplier = rangeMultiplier;
        this.potencyMultiplier = potencyMultiplier;
        this.allowsOffhandCast = allowsOffhandCast;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getGemCapacity() {
        return this.gemCapacity;
    }

    public float getCooldownMultiplier() {
        return this.cooldownMultiplier;
    }

    public float getBaseFuelMultiplier() {
        return this.baseFuelMultiplier;
    }

    public float getChanneledFuelMultiplier() {
        return this.channeledFuelMultiplier;
    }

    public float getRangeMultiplier() {
        return this.rangeMultiplier;
    }

    public float getPotencyMultiplier() {
        return this.potencyMultiplier;
    }

    public boolean allowsOffhandCast() {
        return this.allowsOffhandCast;
    }

    public boolean isAllowsOffhand() {
        return this.allowsOffhandCast;
    }
}
