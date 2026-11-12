package ddraig.net.entropica.spell.mutation.handler;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.mutation.element.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Central registry mapping each {@link EssenceType} to its dedicated per-part
 * {@link MateriaPartMutationHandler}.
 *
 * <p>Handlers are registered incrementally one Materia type / family at a time so every
 * Flow, Form, and Core Effect mutation is authentically implemented and verified.</p>
 */
public final class SpellMutationHandlerRegistry {

    private static final Map<EssenceType, MateriaPartMutationHandler> HANDLERS = new EnumMap<>(EssenceType.class);

    static {
        // Primordial Base Materia (#1 through #12 — Sections 6.2 to 6.13)
        register(new AirSpellMutationHandler());       // #1 Air
        register(new WaterSpellMutationHandler());     // #2 Water
        register(new NatureSpellMutationHandler());    // #3 Nature
        register(new EarthSpellMutationHandler());     // #4 Earth
        register(new FrozenSpellMutationHandler());    // #5 Frozen
        register(new NetherSpellMutationHandler());    // #6 Nether
        register(new RadiantSpellMutationHandler());   // #7 Radiant
        register(new UmbralSpellMutationHandler());    // #8 Umbral
        register(new UndeadSpellMutationHandler());    // #9 Undead
        register(new VoidSpellMutationHandler());      // #10 Void
        register(new AridSpellMutationHandler());      // #11 Arid
        register(new LightningSpellMutationHandler()); // #12 Lightning

        // Tier 1 Elemental Fusions (#13 through #20 — Sections 6.14 to 6.21)
        register(new MagmaSpellMutationHandler());      // #13 Magma
        register(new StormSpellMutationHandler());      // #14 Storm
        register(new GlacialSpellMutationHandler());    // #15 Glacial
        register(new OvergrowthSpellMutationHandler()); // #16 Overgrowth
        register(new DustSpellMutationHandler());       // #17 Dust
        register(new SporeSpellMutationHandler());      // #18 Spore
        register(new TaigaSpellMutationHandler());      // #19 Taiga
        register(new OasisSpellMutationHandler());      // #20 Oasis

        // Vital Fusions (#21 through #24 — Sections 6.22 to 6.25)
        register(new VitaeSpellMutationHandler());      // #21 Vitae
        register(new BloodSpellMutationHandler());      // #22 Blood
        register(new AuraSpellMutationHandler());       // #23 Aura
        register(new AmberSpellMutationHandler());      // #24 Amber

        // Ethereal & Paradox Fusions (#25 through #38 — Sections 6.26 to 6.39)
        register(new EclipseSpellMutationHandler());    // #25 Eclipse
        register(new BlightSpellMutationHandler());     // #26 Blight
        register(new AstralSpellMutationHandler());     // #27 Astral
        register(new SoulfireSpellMutationHandler());   // #28 Soulfire
        register(new VaporSpellMutationHandler());      // #29 Vapor
        register(new NullRSpellMutationHandler());      // #30 Null_R
        register(new NullUSpellMutationHandler());      // #31 Null_U
        register(new DawnSpellMutationHandler());       // #32 Dawn
        register(new AbyssSpellMutationHandler());      // #33 Abyss
        register(new AegisSpellMutationHandler());      // #34 Aegis
        register(new AuroraSpellMutationHandler());     // #35 Aurora
        register(new MirageSpellMutationHandler());     // #36 Mirage
        register(new WraithSpellMutationHandler());     // #37 Wraith
        register(new BarrowSpellMutationHandler());     // #38 Barrow

        // Tier 3 Trinity Fusions (#39 through #47 — Sections 6.40 to 6.48) & Chimera Catalyst (Section 6.49)
        register(new PyreSpellMutationHandler());       // #39 Pyre
        register(new PenumbraSpellMutationHandler());   // #40 Penumbra
        register(new RimeSpellMutationHandler());       // #41 Rime
        register(new SpringSpellMutationHandler());     // #42 Spring
        register(new StaticSpellMutationHandler());     // #43 Static
        register(new SylvanSpellMutationHandler());     // #44 Sylvan
        register(new MiasmaSpellMutationHandler());     // #45 Miasma
        register(new GenesisSpellMutationHandler());    // #46 Genesis
        register(new OblivionSpellMutationHandler());   // #47 Oblivion
        register(new ChimeraSpellMutationHandler());    // Sec 6.49 Chimera Catalyst

        // Tier 4 Apex (#48–#50), Prismatic (#51), Tier 5 Eschaton (#52–#54) & Tier 6 Singularity (#55–#57 — Sections 6.50 to 6.59)
        register(new AetherSpellMutationHandler());     // #48 Aether
        register(new EntropicSpellMutationHandler());   // #49 Entropic
        register(new CelestialSpellMutationHandler());  // #50 Celestial
        register(new PrismaticSpellMutationHandler());  // #51 Prismatic
        register(new EmpyreanSpellMutationHandler());   // #52 Empyrean
        register(new CataclysmSpellMutationHandler());  // #53 Cataclysm
        register(new TerminusSpellMutationHandler());   // #54 Terminus
        register(new EschatonSpellMutationHandler());   // #55 Eschaton
        register(new ApotheosisSpellMutationHandler()); // #56 Apotheosis
        register(new EntropicaSpellMutationHandler());  // #57 Entropica
    }

    private SpellMutationHandlerRegistry() {
    }

    public static void register(MateriaPartMutationHandler handler) {
        HANDLERS.put(handler.materiaType(), handler);
    }

    @Nullable
    public static MateriaPartMutationHandler get(@Nullable EssenceType type) {
        if (type == null || type == EssenceType.REGULAR) return null;
        return HANDLERS.get(type);
    }

    /**
     * Resolves the active {@link MateriaPartMutationHandler} ONLY if {@code mutationEnabled} is {@code true}!
     * Guaranteeing {@code null} when {@code mutationEnabled == false} ensures unmutated parts strictly
     * execute clean baseline mechanics even when bound to a specific Materia type.
     */
    @Nullable
    public static MateriaPartMutationHandler resolveActive(@Nullable EssenceType type, boolean mutationEnabled) {
        if (!mutationEnabled || type == null || type == EssenceType.REGULAR) {
            return null;
        }
        return HANDLERS.get(type);
    }

    public static Map<EssenceType, MateriaPartMutationHandler> getAllRegistered() {
        return Collections.unmodifiableMap(HANDLERS);
    }
}
