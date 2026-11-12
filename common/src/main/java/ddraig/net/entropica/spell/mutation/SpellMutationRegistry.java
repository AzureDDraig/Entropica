package ddraig.net.entropica.spell.mutation;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellPartSlotType;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;

import java.util.EnumMap;
import java.util.Map;

/**
 * Resolves elemental Signature Mutations independently for each part of a spell
 * ({@code Flow}, {@code Form}, {@code Primary Core Effect}, and {@code Secondary Core Effect})
 * across all 57+ Materia typings.
 *
 * <p>If a part has {@code mutationEnabled == false}, this registry strictly returns an
 * un-mutated profile so the player can freely mix mutated and non-mutated parts (and
 * distinct Materia types) within the exact same spell.</p>
 */
public final class SpellMutationRegistry {

    private static final Map<EssenceType, String> ELEMENTAL_EPITHETS = new EnumMap<>(EssenceType.class);

    static {
        ELEMENTAL_EPITHETS.put(EssenceType.AIR, "Zephyr Gale");
        ELEMENTAL_EPITHETS.put(EssenceType.WATER, "Riptide Torrent");
        ELEMENTAL_EPITHETS.put(EssenceType.NATURE, "Verdant Briar");
        ELEMENTAL_EPITHETS.put(EssenceType.EARTH, "Seismic Tectonic");
        ELEMENTAL_EPITHETS.put(EssenceType.FROZEN, "Cryo-Frost");
        ELEMENTAL_EPITHETS.put(EssenceType.NETHER, "Hellfire Brimstone");
        ELEMENTAL_EPITHETS.put(EssenceType.RADIANT, "Solar Starlight");
        ELEMENTAL_EPITHETS.put(EssenceType.UMBRAL, "Shadow Eclipse");
        ELEMENTAL_EPITHETS.put(EssenceType.UNDEAD, "Necrotic Ossuary");
        ELEMENTAL_EPITHETS.put(EssenceType.VOID, "Singularity Rift");
        ELEMENTAL_EPITHETS.put(EssenceType.ARID, "Sirocco Haboob");
        ELEMENTAL_EPITHETS.put(EssenceType.LIGHTNING, "Galvanic Tesla");

        ELEMENTAL_EPITHETS.put(EssenceType.MAGMA, "Pyroclastic Caldera");
        ELEMENTAL_EPITHETS.put(EssenceType.STORM, "Tempest Squall");
        ELEMENTAL_EPITHETS.put(EssenceType.GLACIAL, "Absolute-Zero Glacial");
        ELEMENTAL_EPITHETS.put(EssenceType.OVERGROWTH, "Sylvan Bramble");
        ELEMENTAL_EPITHETS.put(EssenceType.DUST, "Abrasive Whiteout");
        ELEMENTAL_EPITHETS.put(EssenceType.SPORE, "Mycelial Spore");
        ELEMENTAL_EPITHETS.put(EssenceType.TAIGA, "Boreal Frost-Resin");
        ELEMENTAL_EPITHETS.put(EssenceType.OASIS, "Miracle Aquifer");

        ELEMENTAL_EPITHETS.put(EssenceType.VITAE, "Cellular Vitality");
        ELEMENTAL_EPITHETS.put(EssenceType.BLOOD, "Sanguine Exsanguination");
        ELEMENTAL_EPITHETS.put(EssenceType.AURA, "Pranic Chakra");
        ELEMENTAL_EPITHETS.put(EssenceType.AMBER, "Fossilizing Resinite");

        ELEMENTAL_EPITHETS.put(EssenceType.ECLIPSE, "Penumbra Coronal");
        ELEMENTAL_EPITHETS.put(EssenceType.BLIGHT, "Cadaverous Rot");
        ELEMENTAL_EPITHETS.put(EssenceType.ASTRAL, "Celestial Pulsar");
        ELEMENTAL_EPITHETS.put(EssenceType.SOULFIRE, "Phantasmal Soulflame");
        ELEMENTAL_EPITHETS.put(EssenceType.VAPOR, "Scalding Steam");
        ELEMENTAL_EPITHETS.put(EssenceType.NULL_R, "White-Hole Antimatter");
        ELEMENTAL_EPITHETS.put(EssenceType.NULL_U, "Black-Hole Oblivion");
        ELEMENTAL_EPITHETS.put(EssenceType.DAWN, "Daybreak Horizon");
        ELEMENTAL_EPITHETS.put(EssenceType.AUREOLE, "Luminous Halo");
        ELEMENTAL_EPITHETS.put(EssenceType.VESPER, "Eventide Twilight");
        ELEMENTAL_EPITHETS.put(EssenceType.ABYSS, "Hadopelagic Trench");
        ELEMENTAL_EPITHETS.put(EssenceType.AEGIS, "Gilded Citadel");
        ELEMENTAL_EPITHETS.put(EssenceType.AURORA, "Borealis Spectrum");
        ELEMENTAL_EPITHETS.put(EssenceType.MIRAGE, "Fata Morgana Phantasm");
        ELEMENTAL_EPITHETS.put(EssenceType.WRAITH, "Incorporeal Specter");
        ELEMENTAL_EPITHETS.put(EssenceType.BARROW, "Catacomb Sepulcher");

        ELEMENTAL_EPITHETS.put(EssenceType.PYRE, "Cremating Wildfire");
        ELEMENTAL_EPITHETS.put(EssenceType.PENUMBRA, "Twilight Event-Lensing");
        ELEMENTAL_EPITHETS.put(EssenceType.RIME, "Hoarfrost Permafrost");
        ELEMENTAL_EPITHETS.put(EssenceType.SPRING, "Vernal Blossom");
        ELEMENTAL_EPITHETS.put(EssenceType.STATIC, "Dielectric Breakdown");
        ELEMENTAL_EPITHETS.put(EssenceType.SYLVAN, "Elderwood Sun-Canopy");
        ELEMENTAL_EPITHETS.put(EssenceType.MIASMA, "Putrescent Quagmire");
        ELEMENTAL_EPITHETS.put(EssenceType.GENESIS, "Primordial Dawn-Tide");
        ELEMENTAL_EPITHETS.put(EssenceType.OBLIVION, "Stygian Extinction");

        ELEMENTAL_EPITHETS.put(EssenceType.CHIMERA, "Discordant Chimeric");
        ELEMENTAL_EPITHETS.put(EssenceType.PRISMATIC, "Refractive Kaleidoscope");

        ELEMENTAL_EPITHETS.put(EssenceType.AETHER, "Quintessential Firmament");
        ELEMENTAL_EPITHETS.put(EssenceType.ENTROPIC, "Thermodynamic Singularity");
        ELEMENTAL_EPITHETS.put(EssenceType.CELESTIAL, "Holy Supernova");

        ELEMENTAL_EPITHETS.put(EssenceType.EMPYREAN, "Yggdrasil World-Tree");
        ELEMENTAL_EPITHETS.put(EssenceType.CATACLYSM, "World-Breaker Caldera");
        ELEMENTAL_EPITHETS.put(EssenceType.TERMINUS, "Zero-Point Cryogenic");

        ELEMENTAL_EPITHETS.put(EssenceType.ESCHATON, "Omega Event-Horizon");
        ELEMENTAL_EPITHETS.put(EssenceType.APOTHEOSIS, "Alpha Genesis");
        ELEMENTAL_EPITHETS.put(EssenceType.ENTROPICA, "Grand Cosmic Equilibrium");

        ELEMENTAL_EPITHETS.put(EssenceType.RADIANT_FIRE, "Solar Prominence");
        ELEMENTAL_EPITHETS.put(EssenceType.UMBRAL_WATER, "Abyssal Cavitation");
        ELEMENTAL_EPITHETS.put(EssenceType.EQUINOX, "Duality Equinox");
    }

    private SpellMutationRegistry() {
    }

    public static SpellMutationProfile resolveFlow(SpellFlowModifier flow, EssenceType materiaType, boolean mutationEnabled) {
        String id = flow.getSerializedName();
        if (!mutationEnabled || materiaType == null || materiaType == EssenceType.REGULAR) {
            return SpellMutationProfile.unmutated(SpellPartSlotType.FLOW, id, materiaType);
        }

        MateriaFamilyTier family = MateriaFamilyTier.of(materiaType);
        boolean wallPhase = MateriaFamilyTier.isVoidPhasingLineage(materiaType);
        float velMult = switch (materiaType) {
            case CATACLYSM -> 1.50F;
            case LIGHTNING, STATIC, ASTRAL, AIR, STORM, DAWN -> 1.35F;
            case PRISMATIC, ESCHATON, ENTROPICA -> 1.25F;
            case ECLIPSE, MIRAGE, AURORA -> 1.15F;
            case EARTH, MAGMA, GLACIAL, BARROW, AMBER, AEGIS -> 0.85F;
            default -> 1.0F;
        };
        float rangeMult = (materiaType == EssenceType.STORM || materiaType == EssenceType.LIGHTNING
                || materiaType == EssenceType.ASTRAL || family.getFusionTier() >= 5) ? 1.25F : 1.0F;
        int bonusPierce = materiaType == EssenceType.PRISMATIC ? 0
                : (family.getFusionTier() >= 4 ? 2
                : (materiaType == EssenceType.GLACIAL || materiaType == EssenceType.LIGHTNING
                || materiaType == EssenceType.DAWN || materiaType == EssenceType.AURORA ? 1 : 0));

        MateriaPartMutationHandler handler = SpellMutationHandlerRegistry.resolveActive(materiaType, true);
        String customTitle = handler != null ? handler.resolveFlowTitle(flow) : null;
        String title = (customTitle != null && !customTitle.isBlank())
                ? customTitle
                : (getEpithet(materiaType) + " " + flow.getDisplayName());
        return new SpellMutationProfile(
                SpellPartSlotType.FLOW,
                id,
                materiaType,
                true,
                title,
                wallPhase,
                0.0F,
                0,
                rangeMult,
                1.0F,
                velMult,
                1.0F,
                0.0F,
                bonusPierce,
                false,
                false,
                false,
                false,
                false
        );
    }

    public static SpellMutationProfile resolveForm(SpellDeliveryForm form, EssenceType materiaType, boolean mutationEnabled) {
        String id = form.getSerializedName();
        if (!mutationEnabled || materiaType == null || materiaType == EssenceType.REGULAR) {
            return SpellMutationProfile.unmutated(SpellPartSlotType.FORM, id, materiaType);
        }

        MateriaFamilyTier family = MateriaFamilyTier.of(materiaType);
        boolean wallPhase = MateriaFamilyTier.isVoidPhasingLineage(materiaType)
                || (materiaType == EssenceType.PRISMATIC && (form == SpellDeliveryForm.BEAM || form == SpellDeliveryForm.CONE));
        float rangeMult = MateriaFamilyTier.isEarthLineage(materiaType) ? 1.25F
                : (family.getFusionTier() >= 5 ? 1.35F : 1.0F);
        float radiusMult = switch (family) {
            case SINGULARITY_FUSION -> 1.50F;
            case ESCHATON_FUSION -> 1.35F;
            case APEX_FUSION -> 1.20F;
            default -> 1.0F;
        };
        float velMult = switch (materiaType) {
            case CATACLYSM -> (form == SpellDeliveryForm.PROJECTILE ? 1.65F : 1.25F);
            case ESCHATON, ENTROPICA -> (form == SpellDeliveryForm.PROJECTILE ? 1.40F : 1.15F);
            case PRISMATIC -> (form == SpellDeliveryForm.PROJECTILE ? 1.30F : 1.0F);
            case LIGHTNING, AIR, ASTRAL, DAWN -> 1.25F;
            case ECLIPSE, MIRAGE, AURORA -> 1.15F;
            case NULL_R, NULL_U -> (form == SpellDeliveryForm.PROJECTILE ? 0.65F : 1.0F);
            default -> 1.0F;
        };
        int pierce = switch (materiaType) {
            case ESCHATON, ENTROPICA -> 12;
            case CATACLYSM, APOTHEOSIS -> 10;
            case EMPYREAN, TERMINUS -> 8;
            case ENTROPIC -> 6;
            case AETHER, CELESTIAL -> 4;
            case PRISMATIC -> 0; // Uses 6-bounce Prismatic Oscillating Light-Disc ricochet!
            default -> switch (family) {
                case SINGULARITY_FUSION -> 10;
                case ESCHATON_FUSION -> 6;
                case APEX_FUSION -> 4;
                default -> switch (materiaType) {
                    case DAWN, AURORA, RIME -> 3;
                    case GLACIAL, TAIGA, BLOOD, AURA, RADIANT, VOID, ASTRAL, WRAITH -> 2;
                    case ECLIPSE, NULL_R, NULL_U -> 1;
                    default -> 0;
                };
            };
        };
        float formArmorBypass = family.getBaseArmorBypass();
        if (form == SpellDeliveryForm.TOUCH_STRIKE && (materiaType == EssenceType.NULL_R
                || materiaType == EssenceType.NULL_U
                || materiaType == EssenceType.WRAITH
                || materiaType == EssenceType.PRISMATIC)) {
            formArmorBypass = Math.max(formArmorBypass, 0.50F);
        } else if (form == SpellDeliveryForm.TOUCH_STRIKE && materiaType == EssenceType.VAPOR) {
            formArmorBypass = Math.max(formArmorBypass, 0.40F);
        } else if (form == SpellDeliveryForm.TOUCH_STRIKE && materiaType == EssenceType.PENUMBRA) {
            formArmorBypass = Math.max(formArmorBypass, 0.25F);
        } else if (form == SpellDeliveryForm.BEAM && materiaType == EssenceType.SOULFIRE) {
            formArmorBypass = Math.max(formArmorBypass, 0.30F);
        }
        int formArmorShred = resolveFormArmorShred(form, materiaType, family);

        String formNoun = switch (form) {
            case PROJECTILE -> (family == MateriaFamilyTier.SINGULARITY_FUSION ? "Singularity Lance" : "Bolt");
            case BEAM -> "Ray";
            case RADIAL_BURST -> "Nova";
            case TOUCH_STRIKE -> "Cleave";
            case AURA -> "Mantle";
            case GLYPH -> "Sigil";
            case CONE -> "Deluge";
            case SKYFALL -> "Cataclysm";
            case SUMMON_AGENT -> "Autonomous Agent";
            case SUMMON_SENTRY -> "Anchored Sentry";
            case SUMMON_DECOY -> "Phantom Decoy";
        };
        MateriaPartMutationHandler handler = SpellMutationHandlerRegistry.resolveActive(materiaType, true);
        String customTitle = handler != null ? handler.resolveFormTitle(form) : null;
        String title = (customTitle != null && !customTitle.isBlank())
                ? customTitle
                : (getEpithet(materiaType) + " " + formNoun);

        return new SpellMutationProfile(
                SpellPartSlotType.FORM,
                id,
                materiaType,
                true,
                title,
                wallPhase,
                formArmorBypass,
                formArmorShred,
                rangeMult,
                radiusMult,
                velMult,
                1.0F,
                0.0F,
                pierce,
                MateriaFamilyTier.isFrozenLineage(materiaType) || materiaType == EssenceType.AMBER,
                MateriaFamilyTier.isNetherSmeltLineage(materiaType) || materiaType == EssenceType.ECLIPSE || materiaType == EssenceType.PRISMATIC,
                false,
                false,
                false
        );
    }

    private static int resolveFormArmorShred(SpellDeliveryForm form, EssenceType materiaType, MateriaFamilyTier family) {
        if (materiaType == EssenceType.PRISMATIC) {
            return switch (form) {
                case BEAM -> 2;
                case PROJECTILE, GLYPH, AURA, SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> 4;
                case RADIAL_BURST, TOUCH_STRIKE, CONE -> 5;
                case SKYFALL -> 6;
            };
        }
        return switch (family) {
            case APEX_FUSION -> switch (form) {
                case BEAM -> 2;
                case SKYFALL -> 6;
                default -> 5;
            };
            case ESCHATON_FUSION -> switch (form) {
                case BEAM -> 2;
                case SKYFALL -> 7;
                default -> 6;
            };
            case SINGULARITY_FUSION -> (form == SpellDeliveryForm.BEAM ? 3 : 8);
            default -> family.getBaseArmorShred();
        };
    }

    public static SpellMutationProfile resolveEffect(SpellPartSlotType slotType, SpellCoreEffect effect,
                                                     EssenceType materiaType, boolean mutationEnabled) {
        String id = effect.getSerializedName();
        if (!mutationEnabled || materiaType == null || materiaType == EssenceType.REGULAR) {
            return SpellMutationProfile.unmutated(slotType, id, materiaType);
        }

        MateriaFamilyTier family = MateriaFamilyTier.of(materiaType);
        boolean wallPhase = (MateriaFamilyTier.isVoidPhasingLineage(materiaType) || materiaType == EssenceType.PRISMATIC)
                && (effect == SpellCoreEffect.BLINK || family.hasInnateWallPhasing());
        float lifesteal = switch (materiaType) {
            case ENTROPICA -> 0.50F;
            case CELESTIAL, EMPYREAN -> 0.35F;
            case BLOOD, ENTROPIC -> 0.30F;
            case APOTHEOSIS -> 0.25F;
            case UNDEAD, WRAITH, ECLIPSE -> 0.20F;
            case VITAE, SOULFIRE -> 0.15F;
            default -> 0.0F;
        };
        float potencyMult = switch (family) {
            case SINGULARITY_FUSION -> 1.30F;
            case ESCHATON_FUSION -> 1.20F;
            case APEX_FUSION -> 1.15F;
            case TRINITY_FUSION, CATALYST -> 1.10F;
            default -> 1.05F;
        };

        boolean isHex = effect == SpellCoreEffect.AFFLICTION;
        boolean silkTouch = (MateriaFamilyTier.isFrozenLineage(materiaType) || materiaType == EssenceType.AMBER)
                && (effect == SpellCoreEffect.BREAK || effect == SpellCoreEffect.TRANSMUTE);
        boolean autoSmelt = (MateriaFamilyTier.isNetherSmeltLineage(materiaType) || materiaType == EssenceType.ECLIPSE || materiaType == EssenceType.PRISMATIC)
                && (effect == SpellCoreEffect.BREAK || effect == SpellCoreEffect.TRANSMUTE);
        float rangeMult = (MateriaFamilyTier.isEarthLineage(materiaType) && effect == SpellCoreEffect.CONSTRUCT) ? 1.50F : 1.0F;

        String verbSuffix = switch (effect.getRootFamily()) {
            case HARM -> "Strike";
            case KINETIC -> "Impulse";
            case WARD -> "Bastion";
            case BLINK -> "Phase-Step";
            case TRANSMUTE -> "Metamorphosis";
            case AFFLICTION -> "Persistent Hex";
            case RESTORATION -> "Mending";
            case CONJURE -> "Manifestation";
        };
        MateriaPartMutationHandler handler = SpellMutationHandlerRegistry.resolveActive(materiaType, true);
        String customTitle = handler != null ? handler.resolveEffectTitle(effect) : null;
        String title = (customTitle != null && !customTitle.isBlank())
                ? customTitle
                : (getEpithet(materiaType) + " " + verbSuffix);

        return new SpellMutationProfile(
                slotType,
                id,
                materiaType,
                true,
                title,
                wallPhase,
                family.getBaseArmorBypass(),
                family.getBaseArmorShred(),
                rangeMult,
                1.0F,
                1.0F,
                potencyMult,
                lifesteal,
                0,
                silkTouch,
                autoSmelt,
                isHex,
                isHex && MateriaFamilyTier.isSpawnLureHexElement(materiaType),
                isHex && MateriaFamilyTier.isMonsterEmpowermentHexElement(materiaType)
        );
    }

    public static String getEpithet(EssenceType type) {
        if (type == null || type == EssenceType.REGULAR) return "Unattuned";
        return ELEMENTAL_EPITHETS.getOrDefault(type, type.getDisplayName());
    }

    public static String getComboDiscoveryKey(SpellPartSlotType slotType, String nodeId, EssenceType materiaType) {
        return slotType.getSerializedName() + ":" + nodeId + ":" + materiaType.getSerializedName();
    }
}
