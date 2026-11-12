package ddraig.net.entropica.spell.engine;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity.TriadArchetype;
import org.jetbrains.annotations.Nullable;

/**
 * Pure registry-free canonical specification table for Summoned Eidolon Triad constructs
 * across all 58 Materia suites (Sections 4.9, 6.1.2 & 6.2–6.59 of the Spellcrafting System Guide).
 */
public final class SpellEidolonSpecs {

    private SpellEidolonSpecs() {
    }

    public static boolean isLifecycleRefreshMateria(@Nullable EssenceType type) {
        if (type == null) return false;
        return switch (type) {
            case PENUMBRA, RIME, SPRING, STATIC, SYLVAN, MIASMA, GENESIS, OBLIVION, CHIMERA,
                    AETHER, ENTROPIC, CELESTIAL, PRISMATIC,
                    EMPYREAN, CATACLYSM, TERMINUS,
                    ESCHATON, APOTHEOSIS, ENTROPICA -> true;
            default -> false;
        };
    }

    public static int resolveCanonicalLifecycleTicks(EssenceType formMateria, TriadArchetype archetype) {
        if (archetype == TriadArchetype.DECOY) {
            return switch (formMateria) {
                case ESCHATON, APOTHEOSIS, ENTROPICA -> 400; // 20.0s Tier 6 Singularity Decoys
                case EMPYREAN, CATACLYSM, TERMINUS -> 320;   // 16.0s Tier 5 Eschaton Decoys
                case AETHER, ENTROPIC, CELESTIAL, PRISMATIC -> 280; // 14.0s Tier 4 Apex & Prismatic Decoys
                default -> 240; // 12.0s Tier 3 Trinity & Chimera Decoys
            };
        }
        return switch (formMateria) {
            case ESCHATON, APOTHEOSIS, ENTROPICA -> 1800; // 90.0s Tier 6 Singularity Lifecycle
            case EMPYREAN, CATACLYSM, TERMINUS -> 1200;   // 60.0s Tier 5 Eschaton Lifecycle
            default -> 900; // 45.0s Tier 3 Trinity, Chimera, Prismatic & Tier 4 Apex Lifecycle
        };
    }

    public static double resolveCanonicalLifecycleBaseHp(EssenceType formMateria, TriadArchetype archetype) {
        return switch (formMateria) {
            case PENUMBRA -> (archetype == TriadArchetype.SENTRY ? 80.0D : 60.0D);
            case RIME, SPRING, STATIC -> (archetype == TriadArchetype.SENTRY ? 85.0D : 65.0D);
            case SYLVAN -> (archetype == TriadArchetype.SENTRY ? 85.0D : 70.0D);
            case MIASMA -> (archetype == TriadArchetype.SENTRY ? 80.0D : 65.0D);
            case GENESIS -> (archetype == TriadArchetype.SENTRY ? 80.0D : 70.0D);
            case OBLIVION, CHIMERA -> (archetype == TriadArchetype.SENTRY ? 85.0D : 75.0D);
            case AETHER, ENTROPIC -> (archetype == TriadArchetype.SENTRY ? 100.0D : 90.0D);
            case CELESTIAL, PRISMATIC -> 100.0D;
            case EMPYREAN, CATACLYSM, TERMINUS -> (archetype == TriadArchetype.SENTRY ? 120.0D : 110.0D);
            case ESCHATON, APOTHEOSIS, ENTROPICA -> (archetype == TriadArchetype.SENTRY ? 150.0D : 140.0D);
            default -> 65.0D;
        };
    }

    public static String resolveCanonicalBlueprintTitle(@Nullable EssenceType type, @Nullable TriadArchetype archetype) {
        TriadArchetype resolvedArch = archetype != null ? archetype : TriadArchetype.AGENT;
        return resolveCanonicalConstructName(type, resolvedArch, true, null);
    }

    public static String resolveCanonicalConstructName(
            @Nullable EssenceType type,
            TriadArchetype archetype,
            boolean mutated,
            @Nullable String chimeraElement
    ) {
        if (!mutated || type == null || type == EssenceType.REGULAR) {
            return "Arcane " + archetype.getDisplayName();
        }
        return switch (type) {
            case AIR -> switch (archetype) {
                case AGENT -> "Zephyr Eidolon";
                case SENTRY -> "Zephyr Updraft Sentry";
                case DECOY -> "Zephyr Cyclone Decoy";
            };
            case WATER -> switch (archetype) {
                case AGENT -> "Aqueous Eidolon";
                case SENTRY -> "Aqueous Orb Sentry";
                case DECOY -> "Aqueous Mirage Decoy";
            };
            case NATURE -> switch (archetype) {
                case AGENT -> "Sylvan Eidolon";
                case SENTRY -> "Sylvan Thorn Sentry";
                case DECOY -> "Sylvan Pollen Effigy";
            };
            case EARTH -> switch (archetype) {
                case AGENT -> "Tectonic Eidolon";
                case SENTRY -> "Tectonic Plinth Sentry";
                case DECOY -> "Granite Effigy Decoy";
            };
            case FROZEN -> switch (archetype) {
                case AGENT -> "Glacial Eidolon";
                case SENTRY -> "Glacial Icicle Sentry";
                case DECOY -> "Glacial Ice Effigy";
            };
            case NETHER -> switch (archetype) {
                case AGENT -> "Infernal Eidolon";
                case SENTRY -> "Infernal Magma Mortar";
                case DECOY -> "Blazing Wicker Effigy";
            };
            case RADIANT -> switch (archetype) {
                case AGENT -> "Solar Seraph";
                case SENTRY -> "Solar Seraph Sentry";
                case DECOY -> "Solar Seraph Decoy";
            };
            case UMBRAL -> switch (archetype) {
                case AGENT -> "Shadow Stalker";
                case SENTRY -> "Umbral Ward Sentry";
                case DECOY -> "Umbral Mirror Decoy";
            };
            case UNDEAD -> switch (archetype) {
                case AGENT -> "Cryptguard Eidolon";
                case SENTRY -> "Cryptguard Ossuary Totem";
                case DECOY -> "Bone-Nova Effigy";
            };
            case VOID -> switch (archetype) {
                case AGENT -> "Void-Stalker";
                case SENTRY -> "Void Gravity Pylon";
                case DECOY -> "Void Singularity Silhouette";
            };
            case ARID -> switch (archetype) {
                case AGENT -> "Dune-Dervish";
                case SENTRY -> "Sandstone Obelisk Sentry";
                case DECOY -> "Heat Mirage Decoy";
            };
            case LIGHTNING -> switch (archetype) {
                case AGENT -> "Volt-Spark Construct";
                case SENTRY -> "Tesla Coil Pylon";
                case DECOY -> "Arc-Discharge Decoy";
            };
            case MAGMA -> switch (archetype) {
                case AGENT -> "Magma-Brute";
                case SENTRY -> "Volcanic Magma Mortar";
                case DECOY -> "Caldera Nova Effigy";
            };
            case STORM -> switch (archetype) {
                case AGENT -> "Tempest-Zephyr";
                case SENTRY -> "Tempest-Squall Spire";
                case DECOY -> "Tempest Nova Effigy";
            };
            case GLACIAL -> switch (archetype) {
                case AGENT -> "Glacial-Bruiser";
                case SENTRY -> "Cryo-Pylon Sentry";
                case DECOY -> "Crystalline Freeze-Nova Effigy";
            };
            case OVERGROWTH -> switch (archetype) {
                case AGENT -> "Sylvan-Goliath";
                case SENTRY -> "Living Root Totem";
                case DECOY -> "Overgrowth Floral Effigy";
            };
            case DUST -> switch (archetype) {
                case AGENT -> "Dust-Stalker";
                case SENTRY -> "Abrasive Dust Pylon";
                case DECOY -> "Whiteout Chalk Silhouette";
            };
            case SPORE -> switch (archetype) {
                case AGENT -> "Spore-Tender";
                case SENTRY -> "Bio-Pollen Spire";
                case DECOY -> "Mushroom Decoy";
            };
            case TAIGA -> switch (archetype) {
                case AGENT -> "Boreal Stalker";
                case SENTRY -> "Frost-Howler Spire";
                case DECOY -> "Phantom Snow Stalker";
            };
            case OASIS -> switch (archetype) {
                case AGENT -> "Oasis-Warden";
                case SENTRY -> "Aquifer Spring Spire";
                case DECOY -> "Mirage Blossom Effigy";
            };
            case VITAE -> switch (archetype) {
                case AGENT -> "Vitality Knight";
                case SENTRY -> "Cardiovascular Spire";
                case DECOY -> "Heart-Husk Effigy";
            };
            case BLOOD -> switch (archetype) {
                case AGENT -> "Blood-Thrall Knight";
                case SENTRY -> "Exsanguination Pylon";
                case DECOY -> "Crimson Effigy";
            };
            case AURA -> switch (archetype) {
                case AGENT -> "Zephyr-Monk";
                case SENTRY -> "Chakra Respiration Spire";
                case DECOY -> "Spirit-Vane Effigy";
            };
            case AMBER -> switch (archetype) {
                case AGENT -> "Amber-Clad Sentinel";
                case SENTRY -> "Resinite Stasis Spire";
                case DECOY -> "Petrified Amber Effigy";
            };
            case ECLIPSE -> switch (archetype) {
                case AGENT -> "Eclipse-Knight";
                case SENTRY -> "Black Sun Corona Spire";
                case DECOY -> "Penumbra Silhouette Effigy";
            };
            case BLIGHT -> switch (archetype) {
                case AGENT -> "Blight-Revenant";
                case SENTRY -> "Necro-Briar Spire";
                case DECOY -> "Cadaverous Effigy";
            };
            case ASTRAL -> switch (archetype) {
                case AGENT -> "Astral-Phase Stalker";
                case SENTRY -> "Pulsar Singularity Spire";
                case DECOY -> "Cosmic Silhouette Effigy";
            };
            case SOULFIRE -> switch (archetype) {
                case AGENT -> "Soul-Harvester Revenant";
                case SENTRY -> "Phantasmal Pyre Spire";
                case DECOY -> "Howling Soul Effigy";
            };
            case VAPOR -> switch (archetype) {
                case AGENT -> "Boiler-Knight";
                case SENTRY -> "Steam-Vent Obelisk";
                case DECOY -> "Condensation Effigy";
            };
            case NULL_R -> switch (archetype) {
                case AGENT -> "White-Hole Paladin";
                case SENTRY -> "Event Horizon Spire";
                case DECOY -> "Antimatter Effigy";
            };
            case NULL_U -> switch (archetype) {
                case AGENT -> "Void-Shadow Stalker";
                case SENTRY -> "Oblivion Singularity Spire";
                case DECOY -> "Abyssal Silhouette Effigy";
            };
            case DAWN -> switch (archetype) {
                case AGENT -> "Dawn-Strider";
                case SENTRY -> "Sol-Zephyr Spire";
                case DECOY -> "Daybreak Mirage Effigy";
            };
            case ABYSS -> switch (archetype) {
                case AGENT -> "Tentacled Kraken-Husk";
                case SENTRY -> "Trench Spire";
                case DECOY -> "Hadopelagic Effigy";
            };
            case AEGIS -> switch (archetype) {
                case AGENT -> "Bulwark-Paladin";
                case SENTRY -> "Citadel Fortress Spire";
                case DECOY -> "Gilded Granite Effigy";
            };
            case AURORA -> switch (archetype) {
                case AGENT -> "Borealis-Archon";
                case SENTRY -> "Prismatic Aurora Spire";
                case DECOY -> "Cryo-Prism Effigy";
            };
            case MIRAGE -> switch (archetype) {
                case AGENT -> "Mirage-Duelist";
                case SENTRY -> "Fata Morgana Obelisk";
                case DECOY -> "Phantom Mirror Effigy";
            };
            case WRAITH -> switch (archetype) {
                case AGENT -> "Wraith-Stalker";
                case SENTRY -> "Banshee Wail Obelisk";
                case DECOY -> "Ectoplasmic Soul-Husk Effigy";
            };
            case BARROW -> switch (archetype) {
                case AGENT -> "Barrow Skeleton";
                case SENTRY -> "Cairn Beacon";
                case DECOY -> "Hollow Bone Effigy";
            };
            case PYRE -> switch (archetype) {
                case AGENT -> "Pyre-Juggernaut";
                case SENTRY -> "Cremation Pyre Spire";
                case DECOY -> "Smoldering Hearth Effigy";
            };
            case PENUMBRA -> switch (archetype) {
                case AGENT -> "Penumbral Void-Bladewarden";
                case SENTRY -> "Twilight Corona Citadel";
                case DECOY -> "Volatile Eclipse Singularity Effigy";
            };
            case RIME -> switch (archetype) {
                case AGENT -> "Glacial Rime-Vanguard";
                case SENTRY -> "Hoarfrost Glacial Citadel Spire";
                case DECOY -> "Brittle Permafrost Effigy";
            };
            case SPRING -> switch (archetype) {
                case AGENT -> "Vernal Bloom-Bladewarden";
                case SENTRY -> "Aquifer Springwater Font Spire";
                case DECOY -> "Blossoming Vernal Nectar Effigy";
            };
            case STATIC -> switch (archetype) {
                case AGENT -> "Galvanic Arc-Vanguard";
                case SENTRY -> "Dielectric Tesla Citadel Spire";
                case DECOY -> "Volatile Dielectric Spark Effigy";
            };
            case SYLVAN -> switch (archetype) {
                case AGENT -> "Ironwood Juggernaut";
                case SENTRY -> "Petrified Monolith Sentry";
                case DECOY -> "Thorn-Bramble Effigy";
            };
            case MIASMA -> switch (archetype) {
                case AGENT -> "Septic Bog-Stalker";
                case SENTRY -> "Cadaverous Geyser Pylon";
                case DECOY -> "Decomposing Bog-Husk";
            };
            case GENESIS -> switch (archetype) {
                case AGENT -> "Solar-Hydro Templar";
                case SENTRY -> "Dawn-Glory Spire";
                case DECOY -> "Amniotic Water-Phantom";
            };
            case OBLIVION -> switch (archetype) {
                case AGENT -> "Stygian Dread-Juggernaut";
                case SENTRY -> "Pyre of Nonexistence Obelisk";
                case DECOY -> "Smoldering Wither-Effigy";
            };
            case CHIMERA -> switch (archetype) {
                case AGENT -> switch (chimeraElement == null ? "" : chimeraElement) {
                    case "AIR" -> "Zephyr-Beast";
                    case "EARTH" -> "Stone-Goliath";
                    case "WATER" -> "Abyssal-Hydra";
                    case "NETHER" -> "Hell-Chimera";
                    default -> "Discordant Chimeric Juggernaut";
                };
                case SENTRY -> "Chromatic Crucible Spire";
                case DECOY -> "Unstable Hybrid Effigy";
            };
            case AETHER -> switch (archetype) {
                case AGENT -> "Firmament Archon-Colossus";
                case SENTRY -> "Apex Firmament Spire of Equilibrium";
                case DECOY -> "Harmonized Quintessence Effigy";
            };
            case ENTROPIC -> switch (archetype) {
                case AGENT -> "Entropic Dread-Titan";
                case SENTRY -> "Apex Event Horizon Monolith";
                case DECOY -> "Smoldering Void-Husk Effigy";
            };
            case CELESTIAL -> switch (archetype) {
                case AGENT -> "Celestial Seraph-Colossus";
                case SENTRY -> "Apex White-Hole Dawn Spire";
                case DECOY -> "Holy Genesis Effigy";
            };
            case PRISMATIC -> switch (archetype) {
                case AGENT -> "Prismatic Valkyrie";
                case SENTRY -> "Refractive Mirror Monolith";
                case DECOY -> "Sacrificial Solar Relic";
            };
            case EMPYREAN -> switch (archetype) {
                case AGENT -> "Yggdrasil Arboreal Juggernaut";
                case SENTRY -> "Heartwood Canopy Obelisk";
                case DECOY -> "Golden Willow Effigy";
            };
            case CATACLYSM -> switch (archetype) {
                case AGENT -> "Stygian Magma-Void Serpent";
                case SENTRY -> "Volcanic Eruption Spire";
                case DECOY -> "Molten Core Beacon";
            };
            case TERMINUS -> switch (archetype) {
                case AGENT -> "Titanic Glacial Elemental";
                case SENTRY -> "Anchored Permafrost Elemental Spire";
                case DECOY -> "Unstable Cryogenic Elemental Effigy";
            };
            case ESCHATON -> switch (archetype) {
                case AGENT -> "Event-Horizon Leviathan";
                case SENTRY -> "Accretion Disk Singularity Spire";
                case DECOY -> "Collapsing Micro-Nova Effigy";
            };
            case APOTHEOSIS -> switch (archetype) {
                case AGENT -> "Celestial Seraph-Titan";
                case SENTRY -> "White-Hole Dawn Obelisk";
                case DECOY -> "Radiant Ascendance Mirage";
            };
            case ENTROPICA -> switch (archetype) {
                case AGENT -> "Avatar of Entropica";
                case SENTRY -> "Altar of the Horizon";
                case DECOY -> "Fractured Paradox Core";
            };
            default -> type.getDisplayName() + " " + archetype.getDisplayName();
        };
    }

    public static boolean isPureAuraOrFieldSentry(EssenceType mat) {
        return switch (mat) {
            case AIR, RADIANT, UMBRAL, SPORE, TAIGA, BLOOD, AURA, VAPOR,
                    ABYSS, AURORA, MIRAGE, WRAITH, BARROW, PYRE, MIASMA, GENESIS, PRISMATIC -> true;
            default -> false;
        };
    }

    public static double resolveCanonicalDeathBurstRadius(EssenceType mat) {
        return switch (mat) {
            case ESCHATON, APOTHEOSIS, ENTROPICA -> 20.0D;
            case EMPYREAN, CATACLYSM, TERMINUS -> 14.0D;
            case AETHER, ENTROPIC -> 12.0D;
            case PENUMBRA, RIME, SPRING, STATIC, SYLVAN, MIASMA, GENESIS, OBLIVION, CHIMERA, CELESTIAL, PRISMATIC -> 10.0D;
            case UNDEAD, MAGMA, STORM, DAWN, AEGIS, AURORA, WRAITH, PYRE -> 6.0D;
            case DUST, OASIS, VITAE, BLOOD, AURA, AMBER, ECLIPSE, BLIGHT, ASTRAL, SOULFIRE, NULL_R, NULL_U -> 5.0D;
            default -> 4.0D;
        };
    }
}
