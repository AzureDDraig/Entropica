package ddraig.net.entropica.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * The 8 canonical Root Core Effect Families (plus Utility & Misc discipline action verbs)
 * in Entropica's Spellcrafting Engine.
 */
public enum SpellCoreEffect implements StringRepresentable {
    // --- 8 Root Core Effect Families ---
    HARM("harm", "Harm", RootFamily.HARM, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UNIQUE), true),
    KINETIC("kinetic", "Kinetic", RootFamily.KINETIC, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    WARD("ward", "Ward / Aegis", RootFamily.WARD, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UTILITY, SpellPrefix.UNIQUE), true),
    BLINK("blink", "Blink / Step", RootFamily.BLINK, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    TRANSMUTE("transmute", "Transmute", RootFamily.TRANSMUTE, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    AFFLICTION("affliction", "Affliction / Hex", RootFamily.AFFLICTION, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UNIQUE), false),
    RESTORATION("restoration", "Restoration", RootFamily.RESTORATION, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UTILITY, SpellPrefix.UNIQUE), true),
    CONJURE("conjure", "Conjure / Light", RootFamily.CONJURE, EnumSet.of(SpellPrefix.COMBAT, SpellPrefix.UTILITY, SpellPrefix.MISC, SpellPrefix.UNIQUE), true),

    // --- Specialized Utility & Misc Sub-Verbs ---
    BREAK("break", "Break / Excavate", RootFamily.TRANSMUTE, EnumSet.of(SpellPrefix.UTILITY, SpellPrefix.UNIQUE), true),
    HARVEST("harvest", "Agrarian Harvest", RootFamily.TRANSMUTE, EnumSet.of(SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    CONSTRUCT("construct", "Arcane Construction", RootFamily.CONJURE, EnumSet.of(SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    LOGISTICS("logistics", "Logistics & Retrieval", RootFamily.KINETIC, EnumSet.of(SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    FLIGHT("flight", "Arcane Flight & Jumps", RootFamily.KINETIC, EnumSet.of(SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    GRAPPLE("grapple", "Grapple & Zipline", RootFamily.KINETIC, EnumSet.of(SpellPrefix.UTILITY, SpellPrefix.UNIQUE), false),
    SONAR("sonar", "Starlight Pathfinding Sonar", RootFamily.CONJURE, EnumSet.of(SpellPrefix.MISC, SpellPrefix.UNIQUE), false),
    ATMOSPHERE("atmosphere", "Celestial & Weather Control", RootFamily.CONJURE, EnumSet.of(SpellPrefix.MISC, SpellPrefix.UNIQUE), false),
    ACOUSTIC("acoustic", "Auditory Lure & Silence", RootFamily.CONJURE, EnumSet.of(SpellPrefix.MISC, SpellPrefix.UNIQUE), false),
    DIAGNOSTIC("diagnostic", "Diagnostic Inspector", RootFamily.CONJURE, EnumSet.of(SpellPrefix.MISC, SpellPrefix.UNIQUE), false),
    GRAVITY("gravity", "Gravity Manipulation", RootFamily.KINETIC, EnumSet.of(SpellPrefix.MISC, SpellPrefix.UNIQUE), false);

    public enum RootFamily {
        HARM, KINETIC, WARD, BLINK, TRANSMUTE, AFFLICTION, RESTORATION, CONJURE
    }

    public static final Codec<SpellCoreEffect> CODEC = StringRepresentable.fromEnum(SpellCoreEffect::values);
    public static final StreamCodec<FriendlyByteBuf, SpellCoreEffect> STREAM_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SpellCoreEffect.class));

    private final String id;
    private final String displayName;
    private final RootFamily rootFamily;
    private final Set<SpellPrefix> allowedPrefixes;
    private final boolean starterUnlocked;

    SpellCoreEffect(String id, String displayName, RootFamily rootFamily,
                    Set<SpellPrefix> allowedPrefixes, boolean starterUnlocked) {
        this.id = id;
        this.displayName = displayName;
        this.rootFamily = rootFamily;
        this.allowedPrefixes = allowedPrefixes;
        this.starterUnlocked = starterUnlocked;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public RootFamily getRootFamily() {
        return this.rootFamily;
    }

    public boolean isAllowedForPrefix(SpellPrefix prefix) {
        return this.allowedPrefixes.contains(prefix);
    }

    public boolean isStarterUnlocked() {
        return this.starterUnlocked;
    }

    public String getResearchNodeId() {
        return "spell_effect_" + this.id;
    }
}
