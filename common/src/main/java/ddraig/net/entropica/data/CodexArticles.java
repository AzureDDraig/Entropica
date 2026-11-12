package ddraig.net.entropica.data;

import java.util.Arrays;
import java.util.List;

/**
 * Knowledge Base & Articles Data for Entropica: The Entropic Codex.
 * Contains detailed specifications for Magic Circles and Magic Circuits.
 */
public class CodexArticles {

    // ==========================================
    // 1. Chalk Tiers Data Specification
    // ==========================================
    public enum ChalkTier {
        REGULAR(1, "Regular Chalk", 0.8f, false, "3x3", "Basic essence transference and ritual activation."),
        CONDUCTIVE(2, "Conductive Chalk", 0.6f, false, "3x3", "Arcanite-infused; supports speed and efficiency runes."),
        RESONANT(3, "Resonant Chalk", 0.5f, false, "5x5", "Viscanite-infused; supports stability and yield runes."),
        EIDOLIC(4, "Eidolic Chalk", 0.4f, false, "5x5", "Eidolite-infused; enables high-tier 5x5 ritual circles with max capacity."),

        CIRCUIT_T1(1, "Basic Chalk Conductor", 1.0f, true, "Line/Grid", "Basic signal and Materia transport line."),
        CIRCUIT_T2(2, "Conductive Wire Chalk", 0.8f, true, "Line/Grid", "High-capacity low-resistance Materia circuit conductor."),
        CIRCUIT_T3(3, "Resonant Filament Chalk", 0.6f, true, "Line/Grid", "Vibration-free kinetic signal & Materia circuit line."),
        CIRCUIT_T4(4, "Eidolic Superconductor Chalk", 0.4f, true, "Line/Grid", "Lossless superconductor circuit for maximum throughput.");

        public final int tier;
        public final String name;
        public final float rotationSpeed; // Speed multiplier (0.8x -> 0.4x)
        public final boolean isCircuit;
        public final String footprint;
        public final String description;

        ChalkTier(int tier, String name, float rotationSpeed, boolean isCircuit, String footprint, String description) {
            this.tier = tier;
            this.name = name;
            this.rotationSpeed = rotationSpeed;
            this.isCircuit = isCircuit;
            this.footprint = footprint;
            this.description = description;
        }
    }

    // ==========================================
    // 2. Magic Circuit 16 Logic Node Types
    // ==========================================
    public enum LogicNodeType {
        DEFAULT("Default", "Standard junction node."),
        INPUT("Input", "Accepts external Materia or signal inputs."),
        SOURCE("Source", "Emits continuous Materia/essence pulse."),
        COLLECTION("Collection", "Gathers excess Materia from surrounding circuit lines."),
        AMPLIFIER("Amplifier", "Boosts signal strength and Materia pressure by +50%."),
        CAPACITOR("Capacitor", "Stores up to 1000mB Materia buffer for pulse discharge."),
        RESONATOR("Resonator", "Harmonizes frequency across circuit channels."),
        DIODE("Diode", "Enforces strict one-way Materia flow."),
        AND_GATE("AND Gate", "Outputs only when both input channels are active."),
        OR_GATE("OR Gate", "Outputs when either input channel is active."),
        NOT_GATE("NOT Gate", "Inverts input signal (Active -> Inactive)."),
        EXTRACTION("Extraction", "Pulls Materia from adjacent block entities."),
        DELAY("Delay", "Applies a configurable tick delay (1-20 ticks) to signal transmission."),
        OUTPUT("Output", "Delivers Materia to ritual bowls or machine ports."),
        ESSENCE_BANK("Essence Bank", "Centralized essence storage junction."),
        RUNE("Rune Slot", "Intermediary slot holding Elder Futhark active runes.");

        public final String displayName;
        public final String function;

        LogicNodeType(String displayName, String function) {
            this.displayName = displayName;
            this.function = function;
        }
    }

    // ==========================================
    // 3. 8-Way Directional Override Modes
    // ==========================================
    public enum DirectionalOverride {
        DEFAULT_AUTO("Default (Auto)", "Automatically connects to valid neighboring chalk nodes."),
        FORCED_CONNECT("Forced Connect", "Forces connection regardless of auto-detection rules."),
        FORCED_DISCONNECT("Forced Disconnect", "Disconnects connection, isolating signal line.");

        public final String name;
        public final String behavior;

        DirectionalOverride(String name, String behavior) {
            this.name = name;
            this.behavior = behavior;
        }

        /**
         * Calculates 8-way directional sector from a 360-degree angle (crouch-click vector).
         * Sectors: North (0°), North-East (45°), East (90°), South-East (135°),
         * South (180°), South-West (225°), West (270°), North-West (315°).
         */
        public static int get8WaySector(double dx, double dz) {
            double angle = Math.toDegrees(Math.atan2(dz, dx));
            if (angle < 0) angle += 360;
            return ((int) Math.round(angle / 45.0)) % 8;
        }
    }

    // ==========================================
    // 4. Elder Futhark Active Runes
    // ==========================================
    public static class ActiveRune {
        public final String symbol;
        public final String name;
        public final String effect;
        public final String description;

        public ActiveRune(String symbol, String name, String effect, String description) {
            this.symbol = symbol;
            this.name = name;
            this.effect = effect;
            this.description = description;
        }
    }

    public static final List<ActiveRune> ACTIVE_RUNES = List.of(
            new ActiveRune("ᚢ", "Uruz", "2x Transmutation Yield", "Doubles output items and liquid Materia produced during ritual completion."),
            new ActiveRune("ᚦ", "Thurisaz", "Instant Finish / Overclock", "Instantly completes ritual transmutation cycle upon sacrifice or activation."),
            new ActiveRune("ᚠ", "Fehu", "Essence Filtering", "Selectively filters desired essence components during extraction."),
            new ActiveRune("ᚨ", "Ansuz", "Frequency Resonance", "Stabilizes volatile Materia reactions, preventing overpressure ruptures."),
            new ActiveRune("ᚱ", "Raidho", "Logistics Speed Boost", "Increases Materia flow velocity along connected circuit lines by 3x."),
            new ActiveRune("ᚲ", "Kenaz", "Thermal Amplification", "Raises reaction temperature for high-tier fusion recipes."),
            new ActiveRune("ᚷ", "Gebo", "Catalytic Harmony", "Reduces essence consumption during transmutations by 50%."),
            new ActiveRune("ᚹ", "Wunjo", "Equilibrium Shielding", "Suppresses negative mutation gas releases during ritual operations."),
            new ActiveRune("ᛉ", "Algiz", "Warding Boundary", "Creates a protective barrier preventing mob interference within 8 blocks.")
    );

    // ==========================================
    // 5. Rich Article Markdown Content
    // ==========================================

    public static final String MAGIC_CIRCLES_ARTICLE =
            "## MAGIC CIRCLES SPECIFICATIONS\n" +
            "**Category**: MAGIC -> RUNIC SYMBOLISM & RITUALS -> Magic Circles\n\n" +
            "Magic Circles are spatial ritual structures constructed on solid surfaces using Scribed Chalk. " +
            "They channel ambient elemental Essences and high-pressure liquid Materia into powerful transmutations.\n\n" +

            "### 1. Chalk Tiers & Perimeter Node Dynamics\n" +
            "- **Tier 1 (Regular Chalk)**: Base tier (`circuit = false`). 3x3 footprint. Perimeter nodes rotate at **0.8x** speed.\n" +
            "- **Tier 2 (Conductive Chalk)**: Arcanite-infused (`circuit = false`). 3x3 footprint. Perimeter nodes rotate at **0.6x** speed.\n" +
            "- **Tier 3 (Resonant Chalk)**: Viscanite-infused (`circuit = false`). 5x5 footprint. Perimeter nodes rotate at **0.5x** speed.\n" +
            "- **Tier 4 (Eidolic Chalk)**: Eidolite-infused (`circuit = false`). 5x5 footprint. Perimeter nodes rotate at **0.4x** speed with max capacity.\n\n" +
            "*Node Mechanics*: 4 perimeter cardinal nodes spin smoothly in 3D around the ritual center. " +
            "Dynamic click redirection allows technomancers to click anywhere along the outer boundary to redirect inputs straight into the central focus.\n\n" +

            "### 2. Blood Sacrifice via Sacrificial Knife\n" +
            "- **Tool**: [[Sacrificial Knife]] (`entropica:sacrificial_knife`).\n" +
            "- **Mechanic**: Slaying or sacrificing a living entity near an active output node doubles ritual transmutation speed for **30 seconds**.\n" +
            "- **Overclock & Survival**: Self-inflicted magic damage yields direct blood essence. If player perishes during sacrifice, a Materia-Infused Zombie spawns with Strength I and Resistance II.\n\n" +

            "### 3. Elder Futhark Active Runes\n" +
            "Slot Elder Futhark runes into ritual perimeter slots to alter transmutation parameters:\n" +
            "- **ᚢ Uruz**: Grants **2x Transmutation Yield** (doubles output count & fluid volumes).\n" +
            "- **ᚦ Thurisaz**: Grants **Instant Finish** / Overclock (completes ritual cycle in 1 tick).\n" +
            "- **ᛉ Algiz**: Wards the ritual boundary, pushing aggressive mobs back.\n" +
            "- **ᚱ Raidho**: Triples circuit transfer velocity into input pedestals.";

    public static final String MAGIC_CIRCUITS_ARTICLE =
            "## MAGIC CIRCUITS SPECIFICATIONS\n" +
            "**Category**: MAGIC -> RUNIC SYMBOLISM & RITUALS -> Magic Circuits\n\n" +
            "Magic Circuits function as precision electrical conductors for liquid Materia, pneumatic fumes, and Runic signals (`circuit = true`). " +
            "They interconnect workstations, multiblock structures, and magic circle output nodes.\n\n" +

            "### 1. Advanced Chalk Conductors\n" +
            "- **Tier 1 (Basic Conductor)**: Base signal & Materia transport wire.\n" +
            "- **Tier 2 (Conductive Wire Chalk)**: High-capacity low-resistance Materia circuit conductor.\n" +
            "- **Tier 3 (Resonant Filament Chalk)**: Vibration-free kinetic signal & Materia line.\n" +
            "- **Tier 4 (Eidolic Superconductor Chalk)**: Lossless superconductor circuit for maximum throughput.\n\n" +

            "### 2. 16 Logic Node Types\n" +
            "1. **DEFAULT**: Standard junction node.\n" +
            "2. **INPUT**: Accepts external Materia or signal inputs.\n" +
            "3. **SOURCE**: Emits continuous Materia/essence pulse.\n" +
            "4. **COLLECTION**: Gathers excess Materia from surrounding circuit lines.\n" +
            "5. **AMPLIFIER**: Boosts signal strength and Materia pressure by +50%.\n" +
            "6. **CAPACITOR**: Stores up to 1000mB Materia buffer for pulse discharge.\n" +
            "7. **RESONATOR**: Harmonizes frequency across circuit channels.\n" +
            "8. **DIODE**: Enforces strict one-way Materia flow.\n" +
            "9. **AND_GATE**: Outputs only when both input channels are active.\n" +
            "10. **OR_GATE**: Outputs when either input channel is active.\n" +
            "11. **NOT_GATE**: Inverts input signal (Active -> Inactive).\n" +
            "12. **EXTRACTION**: Pulls Materia from adjacent block entities.\n" +
            "13. **DELAY**: Applies configurable tick delay (1-20 ticks) to signal transmission.\n" +
            "14. **OUTPUT**: Delivers Materia to ritual bowls or machine ports.\n" +
            "15. **ESSENCE_BANK**: Centralized essence storage junction.\n" +
            "16. **RUNE**: Intermediary slot holding Elder Futhark active runes.\n\n" +

            "### 3. 8-Way Directional Overrides\n" +
            "Crouch-clicking a circuit node within a 45° sector toggles connection modes:\n" +
            "- **Default (Auto)**: Automatically connects to valid neighboring chalk nodes.\n" +
            "- **Forced Connect**: Forces connection regardless of auto-detection rules.\n" +
            "- **Forced Disconnect**: Disconnects connection, isolating signal line.\n\n" +

            "### 4. Auto Outer Shell Drawing\n" +
            "Magic Circle output nodes automatically draw a protective outer shell around connected circuit paths, preventing leakage and backflow.";
}
