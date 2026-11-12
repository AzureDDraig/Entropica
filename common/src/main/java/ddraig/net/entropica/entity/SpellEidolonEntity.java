package ddraig.net.entropica.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Modular Combat & Tactical Eidolon Construct summoned via one of the three distinct
 * Summon Delivery Forms ({@code SUMMON_AGENT}, {@code SUMMON_SENTRY}, or {@code SUMMON_DECOY})
 * chosen during spell creation (Spellcrafting System Guide Section 4.9 & Sections 6.2–6.59).
 *
 * <p>Supports the three distinct Summon Creation Types (one chosen per spell):</p>
 * <ul>
 *     <li><b>{@link TriadArchetype#AGENT}</b> ({@code summon_agent}): Mobile companion pursuing hostiles with elemental melee/dash/trail attacks.</li>
 *     <li><b>{@link TriadArchetype#SENTRY}</b> ({@code summon_sentry}): Stationary turret firing elemental projectiles, pulses, and suppression fields.</li>
 *     <li><b>{@link TriadArchetype#DECOY}</b> ({@code summon_decoy}): High-threat taunt effigy with elemental hit retaliation and death-burst detonation.</li>
 * </ul>
 * <p>Strictly honors {@code slot.formPart().mutationEnabled()} (unmutated summons operate as pure arcane constructs
 * without elemental side-effects), supports <b>Sneak + Right-Click</b> in-field Orbis Cell healing, and
 * <b>Sneak-Cast</b> zero-fuel safe dismissal.</p>
 */
public class SpellEidolonEntity extends PathfinderMob {

    public enum TriadArchetype {
        AGENT("Autonomous Agent", "ᚫ"),
        SENTRY("Anchored Sentry", "ᛉ"),
        DECOY("Phantom Decoy", "ᚦ");

        private final String displayName;
        private final String runeGlyph;

        TriadArchetype(String displayName, String runeGlyph) {
            this.displayName = displayName;
            this.runeGlyph = runeGlyph;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getRuneGlyph() {
            return this.runeGlyph;
        }

        public static TriadArchetype fromSubMode(@Nullable String subMode) {
            if (subMode == null) return AGENT;
            String norm = subMode.trim().toLowerCase(Locale.ROOT);
            if (norm.contains("sentry") || norm.contains("turret") || norm.contains("obelisk")) {
                return SENTRY;
            }
            if (norm.contains("decoy") || norm.contains("effigy") || norm.contains("phantom") || norm.contains("totem")) {
                return DECOY;
            }
            return AGENT;
        }
    }

    public static final EntityDataAccessor<String> DATA_MATERIA =
            SynchedEntityData.defineId(SpellEidolonEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> DATA_TINT_RGB =
            SynchedEntityData.defineId(SpellEidolonEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> DATA_ARCHETYPE =
            SynchedEntityData.defineId(SpellEidolonEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> DATA_MUTATED =
            SynchedEntityData.defineId(SpellEidolonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<ItemStack> DATA_DISPLAY_ITEM =
            SynchedEntityData.defineId(SpellEidolonEntity.class, EntityDataSerializers.ITEM_STACK);

    @Nullable
    private UUID summonerUuid;
    private SpellAssembly spell = SpellAssembly.createMagicMissileStarter(1);
    private int slotIndex = 0;
    @Nullable
    private EssenceType fallbackMateria = EssenceType.REGULAR;
    private float apparatusPotency = 1.0F;
    private int maxLifetimeTicks = 600;
    private int ageTicks = 0;
    private int actionCooldown = 0;
    private int meleeHitCounter = 0;
    @Nullable
    private UUID lastMeleeTargetUuid = null;
    private int consecutiveTargetHits = 0;
    private Vec3 anchorPos = Vec3.ZERO;
    private boolean deathBurstTriggered = false;
    private boolean shortLivedIllusion = false;
    @Nullable
    private Vec3 illusionRunVelocity = null;

    // Client stride interpolation for EidolicShadowModel
    public float walkDistAnim = 0.0F;
    public float prevWalkDistAnim = 0.0F;
    private Vec3 lastClientPos = Vec3.ZERO;

    public SpellEidolonEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MATERIA, EssenceType.REGULAR.name());
        builder.define(DATA_TINT_RGB, 0x88CCFF);
        builder.define(DATA_ARCHETYPE, TriadArchetype.AGENT.name());
        builder.define(DATA_MUTATED, false);
        builder.define(DATA_DISPLAY_ITEM, ItemStack.EMPTY);
    }

    public void configureConstruct(
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            int slotIndex,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            Vec3 spawnPos
    ) {
        this.summonerUuid = caster != null ? caster.getUUID() : null;
        this.spell = spell;
        this.slotIndex = Math.clamp(slotIndex, 0, spell.subSpells().size() - 1);
        this.fallbackMateria = fallbackMateria;
        this.apparatusPotency = apparatusPotency;
        this.anchorPos = spawnPos;

        SubSpellSlot slot = spell.subSpells().get(this.slotIndex);
        EssenceType formMateria = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        boolean mutated = slot.formPart().mutationEnabled() && formMateria != EssenceType.REGULAR;
        TriadArchetype archetype = slot.formPart().resolveSummonArchetype();
        int tier = spell.getEffectiveTier();

        this.entityData.set(DATA_MATERIA, formMateria.name());
        this.entityData.set(DATA_TINT_RGB, resolveCanonicalTint(formMateria, mutated));
        this.entityData.set(DATA_ARCHETYPE, archetype.name());
        this.entityData.set(DATA_MUTATED, mutated);

        this.maxLifetimeTicks = 400 + (tier * 80); // 20s at T1 up to 60s at T10

        double baseHp = (18.0D + tier * 8.0D) * spell.flawProfile().getPotencyMultiplier();
        double armor = 2.0D + tier;
        double kbRes = archetype == TriadArchetype.SENTRY ? 1.0D : 0.35D;

        if (archetype == TriadArchetype.DECOY) {
            baseHp *= 1.45D;
        }
        String chimeraElement = "";
        if (mutated && formMateria == EssenceType.CHIMERA) {
            chimeraElement = switch (this.random.nextInt(4)) {
                case 0 -> "AIR";
                case 1 -> "EARTH";
                case 2 -> "WATER";
                default -> "NETHER";
            };
            ddraig.net.entropica.util.EntityHelper.getPersistentData(this).putString("SpellChimeraSummonElement", chimeraElement);
        }
        boolean trinityOrCatalystLifecycle = isLifecycleRefreshMateria(formMateria) && mutated;
        if (trinityOrCatalystLifecycle) {
            this.maxLifetimeTicks = ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalLifecycleTicks(formMateria, archetype);
            if (archetype == TriadArchetype.DECOY) {
                double minDecoyHp = switch (formMateria) {
                    case ESCHATON, APOTHEOSIS, ENTROPICA -> 90.0D;
                    case EMPYREAN, CATACLYSM, TERMINUS -> 70.0D;
                    case AETHER, ENTROPIC, CELESTIAL, PRISMATIC -> 60.0D;
                    default -> 45.0D;
                };
                baseHp = Math.max(baseHp, minDecoyHp);
            } else {
                double canonicalHp = resolveCanonicalLifecycleBaseHp(formMateria, archetype);
                baseHp = canonicalHp * spell.flawProfile().getPotencyMultiplier();
                armor += switch (formMateria) {
                    case ESCHATON, APOTHEOSIS, ENTROPICA -> 16.0D;
                    case EMPYREAN, CATACLYSM, TERMINUS -> 12.0D;
                    case AETHER, ENTROPIC, CELESTIAL, PRISMATIC -> 10.0D;
                    default -> 8.0D;
                };
            }
        }
        if (mutated && (formMateria == EssenceType.EARTH
                || formMateria == EssenceType.MAGMA
                || formMateria == EssenceType.GLACIAL
                || formMateria == EssenceType.OVERGROWTH
                || formMateria == EssenceType.AMBER
                || formMateria == EssenceType.ABYSS
                || formMateria == EssenceType.AEGIS
                || formMateria == EssenceType.BARROW
                || formMateria == EssenceType.PYRE
                || formMateria == EssenceType.RIME
                || formMateria == EssenceType.SYLVAN
                || formMateria == EssenceType.OBLIVION
                || formMateria == EssenceType.AETHER
                || formMateria == EssenceType.ENTROPIC
                || formMateria == EssenceType.EMPYREAN
                || formMateria == EssenceType.CATACLYSM
                || formMateria == EssenceType.TERMINUS
                || formMateria == EssenceType.ESCHATON
                || (formMateria == EssenceType.CHIMERA && "EARTH".equals(chimeraElement)))) {
            if (!trinityOrCatalystLifecycle || archetype == TriadArchetype.DECOY) {
                baseHp *= (archetype == TriadArchetype.DECOY ? 1.75D : 1.25D);
            }
            armor += (formMateria == EssenceType.AEGIS ? 12.0D : 8.0D);
            kbRes = Math.max(kbRes, 0.85D);
        } else if (mutated && (formMateria == EssenceType.AIR
                || formMateria == EssenceType.ARID
                || formMateria == EssenceType.LIGHTNING
                || formMateria == EssenceType.STORM
                || formMateria == EssenceType.DUST
                || formMateria == EssenceType.TAIGA
                || formMateria == EssenceType.AURA
                || formMateria == EssenceType.ECLIPSE
                || formMateria == EssenceType.ASTRAL
                || formMateria == EssenceType.NULL_R
                || formMateria == EssenceType.NULL_U
                || formMateria == EssenceType.DAWN
                || formMateria == EssenceType.AURORA
                || formMateria == EssenceType.MIRAGE
                || formMateria == EssenceType.WRAITH
                || formMateria == EssenceType.PENUMBRA
                || formMateria == EssenceType.SPRING
                || formMateria == EssenceType.STATIC
                || formMateria == EssenceType.GENESIS
                || formMateria == EssenceType.CELESTIAL
                || formMateria == EssenceType.PRISMATIC
                || formMateria == EssenceType.APOTHEOSIS
                || formMateria == EssenceType.ENTROPICA
                || (formMateria == EssenceType.CHIMERA && ("AIR".equals(chimeraElement) || "WATER".equals(chimeraElement))))) {
            AttributeInstance spd = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (spd != null) spd.setBaseValue(0.42D);
        }

        // Section 6.32 Void-Shadow Stalker executes completely silent strikes and footsteps!
        this.setSilent(mutated && formMateria == EssenceType.NULL_U && archetype == TriadArchetype.AGENT);

        if (mutated && (formMateria == EssenceType.UMBRAL || formMateria == EssenceType.MIRAGE)
                && archetype == TriadArchetype.DECOY && caster != null) {
            // Umbral Phantom Decoy & Mirage Phantom Mirror Effigy visually copy caster's held weapons & armor!
            this.entityData.set(DATA_DISPLAY_ITEM, caster.getMainHandItem().copy());
            this.setItemSlot(EquipmentSlot.OFFHAND, caster.getOffhandItem().copy());
            this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
            for (EquipmentSlot eq : EquipmentSlot.values()) {
                if (eq.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    this.setItemSlot(eq, caster.getItemBySlot(eq).copy());
                    this.setDropChance(eq, 0.0F);
                }
            }
        }

        AttributeInstance maxHpAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHpAttr != null) maxHpAttr.setBaseValue(baseHp);
        AttributeInstance armorAttr = this.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) armorAttr.setBaseValue(armor);
        AttributeInstance kbAttr = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) kbAttr.setBaseValue(kbRes);

        this.setHealth((float) baseHp);
        this.setCustomName(Component.literal(resolveCanonicalConstructName(formMateria, archetype, mutated, chimeraElement)));
    }

    /**
     * Spawns a transient tactical decoy effigy (such as Aegis Blink's 15-HP golden tower-shield,
     * Aurora Blink's 2.0s frozen hologram, Mirage Burst's 5.0s illusory clones, or Mirage Blink's 3.0s runner clone)
     * without counting against or evicting the caster's primary Eidolon summon cap.
     */
    @Nullable
    public UUID getOwnerUuid() {
        return this.summonerUuid;
    }

    public static SpellEidolonEntity spawnShortLivedDecoy(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 pos,
            EssenceType materia,
            float maxHp,
            int lifetimeTicks,
            String customTitle,
            @Nullable Vec3 runVelocity
    ) {
        SpellEidolonEntity decoy = new SpellEidolonEntity(ModEntityTypes.SPELL_EIDOLON.get(), level);
        decoy.setPos(pos.x, pos.y, pos.z);
        decoy.summonerUuid = caster != null ? caster.getUUID() : null;
        decoy.anchorPos = pos;
        decoy.maxLifetimeTicks = Math.max(10, lifetimeTicks);
        decoy.shortLivedIllusion = true;
        decoy.illusionRunVelocity = runVelocity;
        decoy.entityData.set(DATA_MATERIA, materia.name());
        decoy.entityData.set(DATA_TINT_RGB, resolveCanonicalTint(materia, true));
        decoy.entityData.set(DATA_ARCHETYPE, TriadArchetype.DECOY.name());
        decoy.entityData.set(DATA_MUTATED, true);

        if (caster != null && (materia == EssenceType.MIRAGE || materia == EssenceType.AURORA || materia == EssenceType.UMBRAL)) {
            decoy.entityData.set(DATA_DISPLAY_ITEM, caster.getMainHandItem().copy());
            decoy.setItemSlot(EquipmentSlot.OFFHAND, caster.getOffhandItem().copy());
            decoy.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
            for (EquipmentSlot eq : EquipmentSlot.values()) {
                if (eq.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    decoy.setItemSlot(eq, caster.getItemBySlot(eq).copy());
                    decoy.setDropChance(eq, 0.0F);
                }
            }
        }
        AttributeInstance maxHpAttr = decoy.getAttribute(Attributes.MAX_HEALTH);
        if (maxHpAttr != null) maxHpAttr.setBaseValue(Math.max(2.0D, maxHp));
        AttributeInstance kbAttr = decoy.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) kbAttr.setBaseValue(runVelocity == null ? 0.90D : 0.35D);
        decoy.setHealth(Math.max(2.0F, maxHp));
        decoy.setCustomName(Component.literal(customTitle));
        if (caster != null) {
            decoy.setYRot(caster.getYRot());
            decoy.setYHeadRot(caster.getYHeadRot());
        }
        level.addFreshEntity(decoy);
        return decoy;
    }

    public static boolean isLifecycleRefreshMateria(@Nullable EssenceType type) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.isLifecycleRefreshMateria(type);
    }

    public static double resolveCanonicalLifecycleBaseHp(EssenceType formMateria, TriadArchetype archetype) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(formMateria, archetype);
    }

    public static String resolveCanonicalConstructName(
            @Nullable EssenceType type,
            TriadArchetype archetype,
            boolean mutated,
            @Nullable String chimeraElement
    ) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalConstructName(type, archetype, mutated, chimeraElement);
    }

    /**
     * Lifecycle Refresh Engine (Sections 6.41–6.49): Re-casting Summon when at cap refreshes an existing
     * matching Agent or Sentry's 45.0s timer and restores 50% of its maximum health instead of evicting it!
     */
    public static boolean tryRefreshLifecycleConstruct(
            ServerLevel level,
            LivingEntity caster,
            EssenceType materia,
            TriadArchetype archetype,
            int tier,
            Vec3 spawnPos
    ) {
        int cap = Math.min(4, 1 + (tier / 3));
        List<SpellEidolonEntity> owned = level.getEntitiesOfClass(
                SpellEidolonEntity.class,
                caster.getBoundingBox().inflate(64.0D),
                e -> e.isAlive() && !e.shortLivedIllusion && e.isOwnedBy(caster)
        );
        if (owned.size() < cap) {
            return false; // Room remains under summon cap to spawn an additional construct!
        }
        for (SpellEidolonEntity candidate : owned) {
            if (candidate.isMutated()
                    && candidate.getEffectiveMateria() == materia
                    && candidate.getArchetype() == archetype) {
                candidate.ageTicks = 0;
                float restored = candidate.getMaxHealth() * 0.50F;
                SpellCombatReactionEngine.notifyMagicalHeal(candidate, restored);
                candidate.heal(restored);
                if (archetype == TriadArchetype.SENTRY) {
                    candidate.anchorPos = spawnPos;
                    candidate.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);
                }
                level.sendParticles(ParticleTypes.ENCHANT, candidate.getX(), candidate.getY(1.1D), candidate.getZ(),
                        22, 0.4D, 0.5D, 0.4D, 0.08D);
                level.playSound(null, candidate.blockPosition(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 0.85F, 1.35F);
                return true;
            }
        }
        return false;
    }

    public static int resolveCanonicalTint(EssenceType type, boolean mutated) {
        if (!mutated || type == null || type == EssenceType.REGULAR) {
            return 0x88CCFF;
        }
        return switch (type) {
            case AIR -> 0xA88654;
            case WATER -> 0x225C7C;
            case NATURE -> 0x1A5923;
            case EARTH -> 0x773214;
            case FROZEN -> 0x98DCF2;
            case NETHER -> 0xB83C08;
            case RADIANT -> 0xFFE278;
            case UMBRAL -> 0x211A21;
            case UNDEAD -> 0x4D6453;
            case VOID -> 0x41304E;
            case ARID -> 0xC2B28F;
            case LIGHTNING -> 0xFFE600;
            case MAGMA -> 0xD4480E;
            case STORM -> 0x429BA3;
            case GLACIAL -> 0x98DCF2;
            case OVERGROWTH -> 0x1A5923;
            case DUST -> 0xC2B28F;
            case SPORE -> 0x6E8B3D;
            case TAIGA -> 0x2E6F5E;
            case OASIS -> 0x2EA87E;
            case VITAE -> 0xFF6B9D;
            case BLOOD -> 0x8A0303;
            case AURA -> 0xD8B4F8;
            case AMBER -> 0xD98218;
            case ECLIPSE -> 0x90824C;
            case BLIGHT -> 0x40984D;
            case ASTRAL -> 0xA08B27;
            case SOULFIRE -> 0x1DF0E5;
            case VAPOR -> 0xA8E6EA;
            case NULL_R -> 0xEFE8FA;
            case NULL_U -> 0x291D33;
            case DAWN -> 0xFFD266;
            case ABYSS -> 0x213B4E;
            case AEGIS -> 0xE2A83C;
            case AURORA -> 0xBCE3B5;
            case MIRAGE -> 0xE0CE84;
            case WRAITH -> 0x41304E;
            case BARROW -> 0x773214;
            case PYRE -> 0xFF4500;
            case PENUMBRA -> 0x281E3C;
            case RIME -> 0x6395B1;
            case SPRING -> 0xFF6B9D;
            case STATIC -> 0xFFE600;
            case SYLVAN -> 0x1A5923;
            case MIASMA -> 0x225C7C;
            case GENESIS -> 0xFFEA78;
            case OBLIVION -> 0xB83C08;
            case CHIMERA -> 0xFF55FF;
            case AETHER -> 0xA88654;
            case ENTROPIC -> 0x41304E;
            case CELESTIAL -> 0xFFEA78;
            case PRISMATIC -> 0xFFFFFF;
            case EMPYREAN -> 0xFFEA78;
            case CATACLYSM -> 0xB83C08;
            case TERMINUS -> 0x98DCF2;
            case ESCHATON -> 0x7012A5;
            case APOTHEOSIS -> 0xFFE484;
            case ENTROPICA -> 0xFAD02C;
            default -> type.getColorInt();
        };
    }

    /**
     * Enforces active summon cap per caster (Section 4.9) so older constructs cleanly dissipate when exceeded.
     */
    public static void enforceSummonCap(ServerLevel level, LivingEntity caster, int tier) {
        int cap = Math.min(4, 1 + (tier / 3));
        List<SpellEidolonEntity> existing = level.getEntitiesOfClass(
                SpellEidolonEntity.class,
                caster.getBoundingBox().inflate(64.0D),
                e -> e.isAlive() && !e.shortLivedIllusion && e.isOwnedBy(caster)
        );
        existing.sort(Comparator.comparingInt((SpellEidolonEntity e) -> e.ageTicks).reversed());
        while (existing.size() >= cap && !existing.isEmpty()) {
            SpellEidolonEntity oldest = existing.remove(0);
            oldest.safeDismiss(level);
        }
    }

    /**
     * Section 4.9 Safe Dismissal: Sneak-casting a Summon spell dismisses active Eidolons at 0 Materia fuel cost!
     * Prioritizes dismissing constructs matching the spell's chosen summon type ({@code preferredArchetype}),
     * falling back to all owned Eidolons if none of that archetype are currently active.
     *
     * @return number of Eidolons safely dismissed.
     */
    public static int dismissCasterEidolons(ServerLevel level, LivingEntity caster) {
        return dismissCasterEidolonsByArchetype(level, caster, null);
    }

    public static int dismissCasterEidolonsByArchetype(
            ServerLevel level,
            LivingEntity caster,
            @Nullable TriadArchetype preferredArchetype
    ) {
        List<SpellEidolonEntity> owned = level.getEntitiesOfClass(
                SpellEidolonEntity.class,
                caster.getBoundingBox().inflate(64.0D),
                e -> e.isAlive() && e.isOwnedBy(caster)
        );
        List<SpellEidolonEntity> toDismiss = preferredArchetype == null
                ? owned
                : owned.stream().filter(e -> e.getArchetype() == preferredArchetype).toList();
        if (toDismiss.isEmpty()) {
            toDismiss = owned;
        }
        for (SpellEidolonEntity e : toDismiss) {
            e.safeDismiss(level);
        }
        if (!toDismiss.isEmpty() && caster instanceof Player player) {
            String roleLabel = preferredArchetype != null ? preferredArchetype.getDisplayName() : "Eidolon";
            player.displayClientMessage(
                    Component.literal("§b[Eidolon Summon] §7Safely dismissed " + toDismiss.size() + " active "
                            + roleLabel + "(s) (0 Materia cost)."),
                    true
            );
        }
        return toDismiss.size();
    }

    /**
     * Checks whether any mutated Umbral Anchored Sentry is suppressing natural monster spawns at {@code pos} (16-block radius).
     */
    public static boolean isUmbralSentrySuppressingSpawns(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(16.0D);
        return !level.getEntitiesOfClass(
                SpellEidolonEntity.class,
                box,
                e -> e.isAlive()
                        && e.isMutated()
                        && e.getEffectiveMateria() == EssenceType.UMBRAL
                        && e.getArchetype() == TriadArchetype.SENTRY
        ).isEmpty();
    }

    public void safeDismiss(ServerLevel level) {
        this.deathBurstTriggered = true; // Safe dismissal does not detonate offensive death bursts
        level.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY(0.9D), this.getZ(), 20, 0.35D, 0.5D, 0.35D, 0.08D);
        level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.35F);
        this.discard();
    }

    public boolean isOwnedBy(@Nullable Entity candidate) {
        return candidate != null && this.summonerUuid != null && this.summonerUuid.equals(candidate.getUUID());
    }

    @Nullable
    public LivingEntity resolveSummoner(ServerLevel level) {
        if (this.summonerUuid == null) return null;
        Entity e = level.getEntity(this.summonerUuid);
        return e instanceof LivingEntity l ? l : null;
    }

    public boolean isFriendlyTo(@Nullable Entity other) {
        if (other == null) return false;
        if (isOwnedBy(other)) return true;
        if (other instanceof SpellEidolonEntity otherEidolon
                && this.summonerUuid != null
                && this.summonerUuid.equals(otherEidolon.summonerUuid)) {
            return true;
        }
        if (this.level() instanceof ServerLevel srv) {
            LivingEntity owner = resolveSummoner(srv);
            if (owner != null && other.isAlliedTo(owner)) return true;
        }
        return super.isAlliedTo(other);
    }

    public EssenceType getEffectiveMateria() {
        try {
            return EssenceType.valueOf(this.entityData.get(DATA_MATERIA));
        } catch (IllegalArgumentException ignored) {
            return EssenceType.REGULAR;
        }
    }

    public int getTintRgb() {
        return this.entityData.get(DATA_TINT_RGB);
    }

    public TriadArchetype getArchetype() {
        try {
            return TriadArchetype.valueOf(this.entityData.get(DATA_ARCHETYPE));
        } catch (IllegalArgumentException ignored) {
            return TriadArchetype.AGENT;
        }
    }

    public boolean isMutated() {
        return this.entityData.get(DATA_MUTATED);
    }

    public ItemStack getDisplayItem() {
        return this.entityData.get(DATA_DISPLAY_ITEM);
    }

    @Override
    public boolean fireImmune() {
        return (isMutated() && (getEffectiveMateria() == EssenceType.NETHER
                || getEffectiveMateria() == EssenceType.MAGMA
                || getEffectiveMateria() == EssenceType.ECLIPSE
                || getEffectiveMateria() == EssenceType.SOULFIRE
                || getEffectiveMateria() == EssenceType.VAPOR
                || getEffectiveMateria() == EssenceType.AEGIS
                || getEffectiveMateria() == EssenceType.PYRE
                || getEffectiveMateria() == EssenceType.RIME
                || getEffectiveMateria() == EssenceType.SPRING
                || getEffectiveMateria() == EssenceType.OBLIVION
                || getEffectiveMateria() == EssenceType.CHIMERA
                || getEffectiveMateria() == EssenceType.AETHER
                || getEffectiveMateria() == EssenceType.ENTROPIC
                || getEffectiveMateria() == EssenceType.CELESTIAL
                || getEffectiveMateria() == EssenceType.PRISMATIC
                || getEffectiveMateria() == EssenceType.EMPYREAN
                || getEffectiveMateria() == EssenceType.CATACLYSM
                || getEffectiveMateria() == EssenceType.TERMINUS
                || getEffectiveMateria() == EssenceType.ESCHATON
                || getEffectiveMateria() == EssenceType.APOTHEOSIS
                || getEffectiveMateria() == EssenceType.ENTROPICA))
                || super.fireImmune();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Section 4.9 In-Field Healing: Sneak + Right-Click an injured Eidolon drains Orbis Cell fuel to mend it!
        if (player.isShiftKeyDown() && isOwnedBy(player) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel srv) {
                int tier = this.spell.getEffectiveTier();
                EssenceType reqType = getEffectiveMateria();
                long healCostMfum = 20L * MateriaStageTable.getMfumPerNativeUnit(Math.max(1, tier - 1));
                SpellFuelService.FuelCheckResult res = SpellFuelService.consumePlayerSpellFuel(
                        player,
                        tier,
                        Map.of(reqType, Math.max(16L, healCostMfum / 8L)),
                        false
                );
                if (res.success()) {
                    float restored = this.getMaxHealth() * 0.35F;
                    this.heal(restored);
                    srv.sendParticles(ParticleTypes.HEART, this.getX(), this.getY(1.2D), this.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.04D);
                    srv.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 0.85F, 1.4F);
                    player.displayClientMessage(Component.literal(String.format(
                            "§a[Eidolon Mended] §f%.1f / %.1f HP", this.getHealth(), this.getMaxHealth()
                    )), true);
                } else {
                    player.displayClientMessage(Component.literal(
                            "§cInsufficient " + reqType.getDisplayName() + " Materia to mend Eidolon!"
                    ), true);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();

        this.prevWalkDistAnim = this.walkDistAnim;
        if (this.lastClientPos != Vec3.ZERO) {
            double dx = this.getX() - this.lastClientPos.x;
            double dz = this.getZ() - this.lastClientPos.z;
            this.walkDistAnim += (float) Math.sqrt(dx * dx + dz * dz) * 4.0F;
        }
        this.lastClientPos = this.position();

        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        this.ageTicks++;
        if (this.actionCooldown > 0) {
            this.actionCooldown--;
        }

        if (isMutated() && (getEffectiveMateria() == EssenceType.CHIMERA || getEffectiveMateria() == EssenceType.PRISMATIC) && this.ageTicks % 6 == 0) {
            int[] rainbow = getEffectiveMateria() == EssenceType.PRISMATIC
                    ? new int[]{0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0xAA55FF, 0xFFFFFF}
                    : new int[]{0xA88654, 0x773214, 0x225C7C, 0xB83C08, 0xFF55FF, 0x55FFFF};
            this.entityData.set(DATA_TINT_RGB, rainbow[(this.ageTicks / 6) % rainbow.length]);
        }

        if (this.ageTicks >= this.maxLifetimeTicks) {
            if (!this.shortLivedIllusion
                    && isMutated()
                    && getArchetype() != TriadArchetype.DECOY
                    && isLifecycleRefreshMateria(getEffectiveMateria())) {
                LivingEntity owner = resolveSummoner(serverLevel);
                if (owner instanceof Player player && player.isAlive()) {
                    int tier = this.spell.getEffectiveTier();
                    long upkeepMfum = Math.max(8L, MateriaStageTable.getBaseMfumCastCost(tier) / 6L);
                    SpellFuelService.FuelCheckResult fed = SpellFuelService.consumePlayerSpellFuel(
                            player,
                            tier,
                            Map.of(getEffectiveMateria(), upkeepMfum),
                            false
                    );
                    if (fed.success()) {
                        this.ageTicks = 0;
                        this.heal(this.getMaxHealth() * 0.25F);
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY(1.1D), this.getZ(),
                                14, 0.35D, 0.45D, 0.35D, 0.08D);
                        return;
                    }
                }
            }
            if (getArchetype() == TriadArchetype.DECOY) {
                triggerElementalDeathBurst(serverLevel);
            }
            this.discard();
            return;
        }

        TriadArchetype archetype = getArchetype();
        switch (archetype) {
            case AGENT -> tickAutonomousAgent(serverLevel);
            case SENTRY -> tickAnchoredSentry(serverLevel);
            case DECOY -> tickPhantomDecoy(serverLevel);
        }
    }

    private void tickAutonomousAgent(ServerLevel level) {
        LivingEntity owner = resolveSummoner(level);
        EssenceType mat = getEffectiveMateria();
        boolean mutated = isMutated();
        int tier = this.spell.getEffectiveTier();

        // Dawn-Strider, Astral, Null_R, Null_U, Aura Zephyr-Monk, Borealis-Archon, Storm Glider, & Apex/Singularity Seraph/Archon Agents float gently
        if (mutated && (mat == EssenceType.AURA || mat == EssenceType.STORM || mat == EssenceType.AURORA
                || mat == EssenceType.DAWN || mat == EssenceType.ASTRAL || mat == EssenceType.NULL_R || mat == EssenceType.NULL_U
                || mat == EssenceType.AETHER || mat == EssenceType.CELESTIAL || mat == EssenceType.PRISMATIC
                || mat == EssenceType.APOTHEOSIS || mat == EssenceType.ENTROPICA)
                && !this.onGround() && this.getDeltaMovement().y < -0.08D) {
            Vec3 v = this.getDeltaMovement();
            this.setDeltaMovement(v.x, -0.08D, v.z);
            this.resetFallDistance();
        }

        // Aurora Borealis-Archon freezes water underfoot into frosted ice; Mirage-Duelist leaves 2 phantom afterimages with every step!
        if (mutated && mat == EssenceType.AURORA && this.ageTicks % 6 == 0) {
            BlockPos below = this.blockPosition().below();
            for (BlockPos p : BlockPos.betweenClosed(below.offset(-1, 0, -1), below.offset(1, 0, 1))) {
                if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                    level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
                }
            }
        }
        if (mutated && mat == EssenceType.MIRAGE && this.ageTicks % 7 == 0 && this.getDeltaMovement().horizontalDistanceSqr() > 0.003D) {
            Vec3 fwd = new Vec3(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z).normalize();
            Vec3 side = new Vec3(-fwd.z, 0.0D, fwd.x).scale(0.65D);
            Vec3 leftPhantom = this.position().subtract(fwd.scale(0.75D)).add(side);
            Vec3 rightPhantom = this.position().subtract(fwd.scale(1.15D)).subtract(side);
            for (Vec3 phantomPos : new Vec3[]{leftPhantom, rightPhantom}) {
                level.sendParticles(ParticleTypes.END_ROD, phantomPos.x, phantomPos.y + 0.9D, phantomPos.z, 5, 0.16D, 0.45D, 0.16D, 0.01D);
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        phantomPos.x, phantomPos.y + 0.8D, phantomPos.z, 5, 0.2D, 0.4D, 0.2D, 0.02D);
            }
        }

        // Lightning Volt-Spark Agent leaves a lingering electrified floor-spark trail behind as it runs!
        if (mutated && mat == EssenceType.LIGHTNING && this.ageTicks % 8 == 0
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.004D) {
            Vec3 trailPos = this.position();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, trailPos.x, trailPos.y + 0.3D, trailPos.z, 5, 0.2D, 0.15D, 0.2D, 0.06D);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    owner, trailPos, 1.25D, 50, level.getGameTime(), null, null, EssenceType.LIGHTNING, 1.0F,
                    (lvl, z, el) -> {
                        if (el % 10 == 0) {
                            lvl.sendParticles(ParticleTypes.ELECTRIC_SPARK, z.center.x, z.center.y + 0.2D, z.center.z,
                                    4, 0.35D, 0.1D, 0.35D, 0.04D);
                            LivingEntity sum = z.resolveCaster(lvl);
                            for (LivingEntity foe : lvl.getEntitiesOfClass(LivingEntity.class,
                                    new AABB(z.center.x - 1.25D, z.center.y - 0.5D, z.center.z - 1.25D,
                                            z.center.x + 1.25D, z.center.y + 1.5D, z.center.z + 1.25D))) {
                                if (!foe.isAlive() || foe == sum || foe instanceof SpellEidolonEntity
                                        || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sum, true)) {
                                    continue;
                                }
                                foe.hurtServer(lvl, lvl.damageSources().lightningBolt(), 1.5F + tier * 0.45F);
                            }
                        }
                        return el < z.maxDurationTicks;
                    }
            ));
        }

        // Magma-Brute Agent leaves a burning magma trail & lava drips while striding!
        if (mutated && mat == EssenceType.MAGMA && this.ageTicks % 10 == 0
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.003D) {
            BlockPos foot = this.blockPosition();
            if (level.getBlockState(foot).isAir() && level.getBlockState(foot.below()).isSolidRender() && level.random.nextFloat() < 0.45F) {
                level.setBlockAndUpdate(foot, Blocks.FIRE.defaultBlockState());
            }
            BlockPos ground = foot.below();
            BlockState orig = level.getBlockState(ground);
            if (orig.isSolidRender() && !orig.is(Blocks.MAGMA_BLOCK)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, ground, orig)
                    && level.random.nextFloat() < 0.50F) {
                level.setBlockAndUpdate(ground, Blocks.MAGMA_BLOCK.defaultBlockState());
                BlockPos saved = ground.immutable();
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        owner, Vec3.atCenterOf(saved), 1.0D, 80, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                        (lvl, z, el) -> {
                            if (el >= z.maxDurationTicks) {
                                if (lvl.getBlockState(saved).is(Blocks.MAGMA_BLOCK)) lvl.setBlockAndUpdate(saved, orig);
                                return false;
                            }
                            return true;
                        }
                ));
            }
            level.sendParticles(ParticleTypes.DRIPPING_LAVA, this.getX(), this.getY(0.5D), this.getZ(), 3, 0.2D, 0.1D, 0.2D, 0.01D);
        }

        // Glacial Cryo-Stalker Agent freezes water underfoot into walkable frosted ice!
        if (mutated && (mat == EssenceType.GLACIAL || mat == EssenceType.FROZEN) && this.ageTicks % 6 == 0) {
            BlockPos below = this.blockPosition().below();
            for (BlockPos p : BlockPos.betweenClosed(below.offset(-1, 0, -1), below.offset(1, 0, 1))) {
                if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                    level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
                }
            }
        }

        // Overgrowth Bramble-Warden & Barrow Skeleton Agents taunt adjacent enemies onto themselves!
        if (mutated && (mat == EssenceType.OVERGROWTH || mat == EssenceType.BARROW) && this.ageTicks % 20 == 0) {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(4.0D))) {
                if (!mob.isAlive() || this.isFriendlyTo(mob)) continue;
                if (mob instanceof Monster || mob.getTarget() == owner) {
                    mob.setTarget(this);
                }
            }
        }

        // Vitae Vitality Knight pulses passive Regeneration to adjacent allies!
        if (mutated && mat == EssenceType.VITAE && this.ageTicks % 30 == 0) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, true, true));
                }
            }
        }

        // Section 6.19 Spore-Tender Agent: tends farms by walking to, harvesting, and replanting mature crops,
        // only engaging hostiles within a 6-block self-defense perimeter with spore puffs!
        if (mutated && mat == EssenceType.SPORE) {
            LivingEntity closeThreat = findNearestHostile(level, 6.0D);
            if (closeThreat == null) {
                BlockPos center = this.blockPosition();
                BlockPos nearestMature = null;
                double bestDistSq = Double.MAX_VALUE;
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-8, -1, -8), center.offset(8, 1, 8))) {
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof CropBlock crop && crop.isMaxAge(bs)) {
                        double dSq = p.distToCenterSqr(this.position());
                        if (dSq < bestDistSq) {
                            bestDistSq = dSq;
                            nearestMature = p.immutable();
                        }
                    }
                }
                if (nearestMature != null) {
                    if (bestDistSq <= 6.25D && this.ageTicks % 20 == 0) {
                        BlockState bs = level.getBlockState(nearestMature);
                        if (bs.getBlock() instanceof CropBlock crop && crop.isMaxAge(bs)) {
                            Block.dropResources(bs, level, nearestMature, null, owner != null ? owner : this, ItemStack.EMPTY);
                            level.setBlock(nearestMature, crop.getStateForAge(0), Block.UPDATE_ALL);
                            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                    nearestMature.getX() + 0.5D, nearestMature.getY() + 0.5D, nearestMature.getZ() + 0.5D,
                                    6, 0.25D, 0.25D, 0.25D, 0.02D);
                        }
                    } else {
                        this.getNavigation().moveTo(nearestMature.getX() + 0.5D, nearestMature.getY(), nearestMature.getZ() + 0.5D, 1.15D);
                    }
                    return;
                }
            }
        }

        double engageRange = (mutated && mat == EssenceType.SPORE) ? 6.0D : 16.0D;
        LivingEntity target = findNearestHostile(level, engageRange);
        if (target == null) {
            if (mutated && (mat == EssenceType.RADIANT || mat == EssenceType.PRISMATIC) && owner != null) {
                // Solar Seraph & Prismatic Valkyrie hover gracefully above their summoner while patrolling!
                double desiredY = owner.getY() + 2.6D;
                double dy = MthClamp(desiredY - this.getY(), -0.25D, 0.25D);
                Vec3 toOwner = owner.position().subtract(this.position());
                Vec3 horiz = new Vec3(toOwner.x, 0.0D, toOwner.z);
                Vec3 hVel = horiz.lengthSqr() > 16.0D ? horiz.normalize().scale(0.28D) : Vec3.ZERO;
                this.setDeltaMovement(hVel.x, dy, hVel.z);
                this.resetFallDistance();
                return;
            }
            if (owner != null && this.distanceToSqr(owner) > 25.0D) {
                this.getNavigation().moveTo(owner, 1.2D);
            }
            return;
        }

        double distSq = this.distanceToSqr(target);

        // Section 6.8 Radiant Solar Seraph & Section 6.53 Prismatic Valkyrie: fly airborne and rain piercing light arrows/javelins down on foes!
        if (mutated && (mat == EssenceType.RADIANT || mat == EssenceType.PRISMATIC)) {
            this.getNavigation().stop();
            double desiredY = target.getY() + 3.0D;
            BlockPos aboveSelf = this.blockPosition().above(2);
            if (!level.getBlockState(aboveSelf).isAir()) {
                desiredY = Math.min(desiredY, aboveSelf.getY() - 1.1D);
            }
            double dy = MthClamp((desiredY - this.getY()) * 0.22D, -0.25D, 0.28D);
            Vec3 flatToTarget = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            double flatDistSq = flatToTarget.lengthSqr();
            Vec3 horizVel = Vec3.ZERO;
            if (flatDistSq > 49.0D) {
                horizVel = flatToTarget.normalize().scale(0.32D);
            } else if (flatDistSq < 12.25D && flatDistSq > 0.01D) {
                horizVel = flatToTarget.normalize().scale(-0.20D);
            }
            this.setDeltaMovement(horizVel.x, dy, horizVel.z);
            this.resetFallDistance();
            Vec3 aim = target.getEyePosition().subtract(this.getEyePosition());
            this.setYRot((float) (Math.atan2(aim.z, aim.x) * (180.0D / Math.PI)) - 90.0F);
            this.yHeadRot = this.getYRot();
            if (this.ageTicks % 8 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.6D), this.getZ(), 2, 0.25D, 0.15D, 0.25D, 0.01D);
            }
            if (this.actionCooldown <= 0) {
                fireConstructBolt(level, owner, target);
                this.actionCooldown = mat == EssenceType.PRISMATIC ? 14 : 20;
            }
            return;
        }

        // Cataclysm Serpent, Eschaton Leviathan & Avatar of Entropica drag distant enemies inward;
        // Avatar of Entropica also hurls alternating Void / Radiant orbs while closing for melee cleaves!
        if (mutated && (mat == EssenceType.CATACLYSM || mat == EssenceType.ESCHATON || mat == EssenceType.ENTROPICA)
                && this.ageTicks % 20 == 0) {
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                Vec3 pull = this.position().subtract(foe.position()).normalize().scale(0.55D);
                foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.12D, pull.z));
                foe.hasImpulse = true;
            }
            if (mat == EssenceType.ESCHATON) {
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    Vec3 pull = this.position().subtract(item.position()).normalize().scale(0.35D);
                    item.setDeltaMovement(item.getDeltaMovement().add(pull));
                    item.hasImpulse = true;
                }
            }
        }
        if (mutated && mat == EssenceType.ENTROPICA && distSq > 9.0D && distSq <= 144.0D && this.ageTicks % 24 == 0) {
            Vec3 aimDir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
            EssenceType orbAspect = ((this.ageTicks / 24) % 2 == 0) ? EssenceType.VOID : EssenceType.RADIANT;
            spawnSingleConstructBolt(level, owner, orbAspect, aimDir, Vec3.ZERO);
        }

        // Section 6.36 Aurora Borealis-Archon: glides across terrain firing piercing freezing ribbon-lances!
        if (mutated && mat == EssenceType.AURORA) {
            if (distSq > 36.0D) {
                this.getNavigation().moveTo(target, 1.35D);
            } else {
                this.getNavigation().stop();
            }
            Vec3 aim = target.getEyePosition().subtract(this.getEyePosition());
            this.setYRot((float) (Math.atan2(aim.z, aim.x) * (180.0D / Math.PI)) - 90.0F);
            this.yHeadRot = this.getYRot();
            if (this.actionCooldown <= 0) {
                fireConstructBolt(level, owner, target);
                this.actionCooldown = 22;
            }
            return;
        }

        // Section 6.9 Umbral Shadow Stalker: stalks targets from indoor ceilings and pounces downward for stealth ambushes!
        if (mutated && mat == EssenceType.UMBRAL) {
            int ceilingY = findIndoorCeilingY(level, this.blockPosition(), 9);
            if (ceilingY > 0) {
                this.getNavigation().stop();
                Vec3 flat = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
                double flatDistSq = flat.lengthSqr();
                if (flatDistSq <= 7.5D && this.getY() >= target.getY() + 1.4D && this.actionCooldown <= 0) {
                    // Ceiling drop-ambush strike!
                    Vec3 pounce = target.position().subtract(this.position()).normalize().scale(0.95D);
                    this.setDeltaMovement(pounce.x, Math.min(-0.45D, pounce.y), pounce.z);
                    this.hasImpulse = true;
                    this.resetFallDistance();
                    this.actionCooldown = 22;
                    this.meleeHitCounter++;
                    target.invulnerableTime = 0;
                    target.hurtServer(level, level.damageSources().magic(), 2.5F + tier * 0.65F);
                    performAgentMeleeStrike(level, owner, target, mat, true, tier);
                    level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.9D), target.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.04D);
                    return;
                }
                double clingY = ceilingY - 1.35D;
                double dy = MthClamp((clingY - this.getY()) * 0.30D, -0.25D, 0.32D);
                Vec3 horizVel = flatDistSq > 0.25D ? flat.normalize().scale(0.34D) : Vec3.ZERO;
                this.setDeltaMovement(horizVel.x, dy, horizVel.z);
                this.resetFallDistance();
                if (this.ageTicks % 8 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(1.1D), this.getZ(), 3, 0.2D, 0.1D, 0.2D, 0.01D);
                }
                return;
            }
        }

        // Section 6.49 Chimera Hell-Chimera ("NETHER"): breathes short-range immolation flames every 2.0s!
        if (mutated && mat == EssenceType.CHIMERA && distSq <= 25.0D && this.ageTicks % 40 == 0
                && "NETHER".equals(ddraig.net.entropica.util.EntityHelper.getPersistentData(this)
                .getString("SpellChimeraSummonElement").orElse("NETHER"))) {
            Vec3 breathDir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
            for (double d = 0.8D; d <= 4.5D; d += 0.75D) {
                Vec3 p = this.getEyePosition().add(breathDir.scale(d));
                level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 5, 0.25D, 0.2D, 0.25D, 0.03D);
            }
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
                if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                if (foe.position().subtract(this.position()).normalize().dot(breathDir) >= 0.45D) {
                    foe.igniteForSeconds(5);
                    foe.hurtServer(level, level.damageSources().onFire(), 2.5F + tier * 0.65F);
                }
            }
        }

        // Abyss Tentacled Kraken-Husk lashes mid-range enemies with grasping ink-tentacles to drag them closer!
        if (mutated && mat == EssenceType.ABYSS && distSq > 9.0D && distSq <= 64.0D && this.ageTicks % 30 == 0) {
            Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.85D);
            target.setDeltaMovement(pull.x, 0.28D, pull.z);
            target.hasImpulse = true;
            level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.8D), target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.04D);
        }

        // Mid-range elemental lunges & dashes (Nature briar lunge, Taiga frost pounce, Nether/Storm/Static/Genesis/Chimera-Air dashes)
        if (mutated && distSq > 6.25D && distSq <= 42.0D && this.onGround() && this.ageTicks % 32 == 0) {
            boolean isChimeraAir = mat == EssenceType.CHIMERA
                    && "AIR".equals(ddraig.net.entropica.util.EntityHelper.getPersistentData(this)
                    .getString("SpellChimeraSummonElement").orElse(""));
            if (mat == EssenceType.NATURE || mat == EssenceType.TAIGA || mat == EssenceType.NETHER
                    || mat == EssenceType.STORM || mat == EssenceType.STATIC || mat == EssenceType.GENESIS || isChimeraAir) {
                Vec3 leap = target.position().subtract(this.position()).normalize();
                double dashSpeed = (mat == EssenceType.STATIC || mat == EssenceType.STORM || isChimeraAir) ? 1.05D : 0.88D;
                this.setDeltaMovement(leap.x * dashSpeed, 0.42D, leap.z * dashSpeed);
                this.hasImpulse = true;
                var dashParticle = switch (mat) {
                    case NATURE -> ParticleTypes.HAPPY_VILLAGER;
                    case TAIGA -> ParticleTypes.SNOWFLAKE;
                    case NETHER -> ParticleTypes.FLAME;
                    case STATIC -> ParticleTypes.ELECTRIC_SPARK;
                    case GENESIS -> ParticleTypes.SPLASH;
                    default -> ParticleTypes.CLOUD;
                };
                level.sendParticles(dashParticle, this.getX(), this.getY(0.5D), this.getZ(), 10, 0.3D, 0.2D, 0.3D, 0.05D);
            }
        }

        // Void, Umbral (outdoors), Astral, Null_R, Null_U, Wraith, Penumbra & Miasma Agents phase-step / submerge behind distant targets
        if (mutated && (mat == EssenceType.VOID || mat == EssenceType.UMBRAL
                || mat == EssenceType.ASTRAL || mat == EssenceType.NULL_R || mat == EssenceType.NULL_U
                || mat == EssenceType.WRAITH || mat == EssenceType.PENUMBRA || mat == EssenceType.MIASMA)
                && distSq > 16.0D && this.ageTicks % 40 == 0) {
            if (mat == EssenceType.MIASMA) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                        this.getX(), this.getY(0.2D), this.getZ(), 14, 0.35D, 0.2D, 0.35D, 0.05D);
            }
            Vec3 behind = target.position().subtract(target.getLookAngle().scale(1.2D));
            this.teleportTo(behind.x, target.getY(), behind.z);
            if (mat == EssenceType.MIASMA) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                        this.getX(), this.getY(0.2D), this.getZ(), 14, 0.35D, 0.2D, 0.35D, 0.05D);
            }
            level.sendParticles(mat == EssenceType.MIASMA ? ParticleTypes.SPORE_BLOSSOM_AIR
                            : (mat == EssenceType.UMBRAL ? ParticleTypes.SMOKE : ParticleTypes.REVERSE_PORTAL),
                    this.getX(), this.getY(0.8D), this.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.05D);
        } else {
            boolean swiftPursuit = mutated && (mat == EssenceType.AIR || mat == EssenceType.ARID
                    || mat == EssenceType.NETHER || mat == EssenceType.LIGHTNING
                    || mat == EssenceType.STORM || mat == EssenceType.DUST
                    || mat == EssenceType.TAIGA || mat == EssenceType.AURA
                    || mat == EssenceType.DAWN || mat == EssenceType.MIRAGE || mat == EssenceType.ECLIPSE
                    || mat == EssenceType.WRAITH || mat == EssenceType.PENUMBRA
                    || mat == EssenceType.SPRING || mat == EssenceType.STATIC || mat == EssenceType.GENESIS
                    || mat == EssenceType.CELESTIAL || mat == EssenceType.APOTHEOSIS || mat == EssenceType.ENTROPICA);
            this.getNavigation().moveTo(target, swiftPursuit ? 1.45D : 1.2D);
            // True wall-phasing step-through for Astral, Null_R, Null_U, Wraith Stalkers & Tier 4–6 Apex/Eschaton/Singularity Agents!
            if (mutated && (mat == EssenceType.WRAITH || mat == EssenceType.ASTRAL
                    || mat == EssenceType.NULL_R || mat == EssenceType.NULL_U
                    || mat == EssenceType.AETHER || mat == EssenceType.ENTROPIC || mat == EssenceType.CELESTIAL
                    || mat == EssenceType.EMPYREAN || mat == EssenceType.CATACLYSM || mat == EssenceType.TERMINUS
                    || mat == EssenceType.ESCHATON || mat == EssenceType.APOTHEOSIS || mat == EssenceType.ENTROPICA)
                    && (this.horizontalCollision || !this.hasLineOfSight(target)) && this.ageTicks % 10 == 0) {
                Vec3 stepDir = target.position().subtract(this.position());
                Vec3 horiz = new Vec3(stepDir.x, 0.0D, stepDir.z);
                if (horiz.lengthSqr() > 0.01D) {
                    Vec3 norm = horiz.normalize();
                    BlockPos aheadBlock = BlockPos.containing(this.position().add(norm.scale(1.0D)));
                    BlockState aheadState = level.getBlockState(aheadBlock);
                    if (!SpellExecutionEngine.isProtectedBlockOrContainer(level, aheadBlock, aheadState)
                            && aheadState.getDestroySpeed(level, aheadBlock) >= 0.0F) {
                        for (double d = 1.5D; d <= 3.5D; d += 0.75D) {
                            Vec3 probe = this.position().add(norm.scale(d));
                            BlockPos pPos = BlockPos.containing(probe);
                            if (level.getBlockState(pPos).isAir() && level.getBlockState(pPos.above()).isAir()) {
                                this.teleportTo(probe.x, probe.y, probe.z);
                                level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.8D), this.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.02D);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (distSq <= 6.25D && this.actionCooldown <= 0) {
            boolean rapidCadence = mutated && (mat == EssenceType.AIR || mat == EssenceType.ARID
                    || mat == EssenceType.STORM || mat == EssenceType.DUST || mat == EssenceType.TAIGA
                    || mat == EssenceType.AURA || mat == EssenceType.PENUMBRA
                    || mat == EssenceType.SPRING || mat == EssenceType.STATIC
                    || (mat == EssenceType.CHIMERA && "WATER".equals(
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(this)
                            .getString("SpellChimeraSummonElement").orElse(""))));
            this.actionCooldown = rapidCadence ? 13 : 20;
            this.meleeHitCounter++;
            if (target.getUUID().equals(this.lastMeleeTargetUuid)) {
                this.consecutiveTargetHits++;
            } else {
                this.lastMeleeTargetUuid = target.getUUID();
                this.consecutiveTargetHits = 1;
            }
            performAgentMeleeStrike(level, owner, target, mat, mutated, tier);
        }
    }

    private static double MthClamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int findIndoorCeilingY(ServerLevel level, BlockPos pos, int maxCheckHeight) {
        for (int dy = 3; dy <= maxCheckHeight; dy++) {
            BlockPos check = pos.above(dy);
            if (level.getBlockState(check).isSolidRender() && level.getBlockState(check.below()).isAir()) {
                return check.getY();
            }
        }
        return -1;
    }

    private void performAgentMeleeStrike(
            ServerLevel level,
            @Nullable LivingEntity owner,
            LivingEntity target,
            EssenceType mat,
            boolean mutated,
            int tier
    ) {
        Vec3 dir = target.position().subtract(this.position()).normalize();
        SubSpellSlot slot = this.spell.subSpells().get(this.slotIndex);
        SpellExecutionEngine.deliverSubSpellOnEntity(level, owner != null ? owner : this, target, dir,
                this.spell, slot, this.fallbackMateria, this.apparatusPotency * 0.75F);

        if (!mutated) {
            return;
        }

        switch (mat) {
            case AIR -> {
                target.setDeltaMovement(dir.x * 0.65D, 0.55D, dir.z * 0.65D);
                target.hasImpulse = true;
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.7D), target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
            }
            case WATER -> {
                SpellLingeringZoneManager.applySoaked(target, 140);
                target.setDeltaMovement(dir.x * 0.8D, 0.28D, dir.z * 0.8D);
                target.hasImpulse = true;
            }
            case NATURE -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
            }
            case EARTH -> {
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 120L);
                target.setDeltaMovement(dir.x * 0.5D, 0.52D, dir.z * 0.5D);
                target.hasImpulse = true;
            }
            case FROZEN, GLACIAL -> {
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 40);
                // 3 consecutive hits on the same target freeze them solid!
                if (this.consecutiveTargetHits >= 3 && this.consecutiveTargetHits % 3 == 0) {
                    target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                    level.playSound(null, target.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.9F, 1.35F);
                }
            }
            case NETHER -> target.igniteForSeconds(5);
            case UMBRAL -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            case UNDEAD -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.5F);
                }
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.20F, this.apparatusPotency * 0.75F);
                }
            }
            case VOID -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 2.0F + tier * 0.7F);
            }
            case ARID -> {
                // Dune-Dervish kicks up blinding sand-dust clouds on rapid sandblade strikes!
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        target.getX(), target.getY(0.8D), target.getZ(), 14, 1.1D, 0.45D, 1.1D, 0.04D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.5D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0));
                }
            }
            case LIGHTNING -> target.addEffect(new MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 14, 0));
            case MAGMA -> {
                target.igniteForSeconds(6);
                target.setDeltaMovement(dir.x * 0.55D, 0.42D, dir.z * 0.55D);
                target.hasImpulse = true;
                level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.6D), target.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.04D);
            }
            case STORM -> {
                SpellLingeringZoneManager.applySoaked(target, 160);
                target.setDeltaMovement(dir.x * 0.95D, 0.42D, dir.z * 0.95D);
                target.hasImpulse = true;
            }
            case OVERGROWTH -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
            }
            case DUST -> {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                var erosionHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ARMOR_EROSION.get());
                MobEffectInstance cur = target.getEffect(erosionHolder);
                int nextAmp = cur == null ? 0 : Math.min(19, cur.getAmplifier() + 1);
                target.addEffect(new MobEffectInstance(erosionHolder, 120, nextAmp));
            }
            case SPORE -> {
                // Spore-Tender defensive spore puff cloud!
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(0.8D), this.getZ(),
                        16, 1.2D, 0.5D, 1.2D, 0.04D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
                }
            }
            case TAIGA -> {
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 40, target.getTicksFrozen() + 50));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
            }
            case OASIS -> {
                // Oasis Mirage-Runner Agent heals nearby allies for 20% of its melee damage dealt!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTierScaledLifesteal(level, ally, tier, 0.20F, this.apparatusPotency * 0.75F);
                    }
                }
            }
            case VITAE -> {
                // Vitae Vitality Knight heals its caster for 25% of melee damage dealt!
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.25F, this.apparatusPotency * 0.75F);
                }
            }
            case BLOOD -> {
                // Blood-Thrall Knight inflicts Bleeding + Hemorrhage and heals caster for 25% of melee damage dealt!
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 80, 0));
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, level.getGameTime() + 80L);
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.25F, this.apparatusPotency * 0.75F);
                }
            }
            case AURA -> {
                // Aura Zephyr-Monk Spirit delivers aerial juggle strikes and restores 20% Materia fuel per hit!
                target.setDeltaMovement(dir.x * 0.45D, 0.68D, dir.z * 0.45D);
                target.hasImpulse = true;
                if (owner instanceof Player player) {
                    SpellCombatReactionEngine.siphonCasterMateriaFuel(player, EssenceType.AURA, tier, 0.20F);
                }
            }
            case AMBER -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.30D, 80L);
            }
            case ECLIPSE -> {
                // Eclipse-Knight shield-piercing strikes!
                if (target.isBlocking()) {
                    if (target instanceof Player bp && !bp.getUseItem().isEmpty()) {
                        bp.getCooldowns().addCooldown(bp.getUseItem(), 60);
                    }
                    target.stopUsingItem();
                }
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 2.5F + tier * 0.75F);
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.0D, 0.0D, 100L);
            }
            case BLIGHT -> {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.0D, 0.0D, 100L);
            }
            case ASTRAL -> {
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 15, 0));
                target.setDeltaMovement(dir.x * 0.4D, 0.65D, dir.z * 0.4D);
                target.hasImpulse = true;
            }
            case SOULFIRE -> {
                target.igniteForSeconds(5);
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 100L);
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.5F);
                }
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.20F, this.apparatusPotency * 0.75F);
                }
            }
            case VAPOR -> {
                target.setDeltaMovement(dir.x * 0.95D, 0.38D, dir.z * 0.95D);
                target.hasImpulse = true;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                SpellLingeringZoneManager.applySoaked(target, 120);
            }
            case NULL_R -> {
                for (var eff : new java.util.ArrayList<>(target.getActiveEffects())) {
                    if (eff.getEffect().value().isBeneficial()) target.removeEffect(eff.getEffect());
                }
                if (target.getType().is(EntityTypeTags.UNDEAD)) {
                    target.hurtServer(level, level.damageSources().magic(), 3.0F + tier * 0.9F);
                }
            }
            case NULL_U -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 2.5F + tier * 0.7F);
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            }
            case DAWN -> {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.7D), target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.2D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                    foe.setDeltaMovement(dir.x * 0.75D, 0.35D, dir.z * 0.75D);
                    foe.hasImpulse = true;
                }
            }
            case ABYSS -> {
                SpellLingeringZoneManager.applySoaked(target, 160);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.55D);
                target.setDeltaMovement(pull.x, 0.18D, pull.z);
                target.hasImpulse = true;
                level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.7D), target.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.03D);
            }
            case AEGIS -> {
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 120L);
            }
            case AURORA -> {
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 40);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            }
            case MIRAGE -> {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                SpellLingeringZoneManager.applyMirageTraitorFrenzy(target, 80);
                var tData = ddraig.net.entropica.util.EntityHelper.getPersistentData(target);
                tData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_UNTIL, level.getGameTime() + 80L);
                if (owner != null) {
                    tData.putString(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_CASTER_UUID, owner.getUUID().toString());
                }
                target.swing(InteractionHand.MAIN_HAND, true);
            }
            case WRAITH -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.8F);
                }
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.25F, this.apparatusPotency * 0.75F);
                }
            }
            case BARROW -> {
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
                if (target instanceof Mob mob) {
                    mob.setTarget(this);
                }
            }
            case PYRE -> {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.7D), target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.2D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.igniteForSeconds(5);
                    if (foe != target) {
                        foe.hurtServer(level, level.damageSources().onFire(), 2.0F + tier * 0.5F);
                    }
                }
            }
            case PENUMBRA -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 2.5F + tier * 0.75F); // 35% armor-bypassing slice
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.45D);
                target.setDeltaMovement(pull.x, 0.15D, pull.z);
                target.hasImpulse = true;
            }
            case RIME -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 2.5F + tier * 0.75F);
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 50);
            }
            case SPRING -> {
                target.invulnerableTime = 0;
                float bonus = target.getType().is(EntityTypeTags.UNDEAD) ? (4.0F + tier) : (2.5F + tier * 0.75F);
                target.hurtServer(level, level.damageSources().magic(), bonus);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, 4.0F);
                        ally.heal(4.0F); // 2 hearts healing pollen splash!
                    }
                }
            }
            case STATIC -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().lightningBolt(), 2.5F + tier * 0.75F);
                if (this.meleeHitCounter % 2 == 0) {
                    target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                }
                int arcs = 0;
                for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.5D))) {
                    if (adj == target || adj == this || adj == owner || !adj.isAlive() || this.isFriendlyTo(adj)) continue;
                    adj.hurtServer(level, level.damageSources().lightningBolt(), 2.0F + tier * 0.5F);
                    if (++arcs >= 2) break;
                }
            }
            case SYLVAN -> {
                target.invulnerableTime = 0;
                float bonus = target.getType().is(EntityTypeTags.UNDEAD) ? (4.0F + tier) : (2.5F + tier * 0.75F);
                target.hurtServer(level, level.damageSources().magic(), bonus);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
            }
            case MIASMA -> {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                if (this.getHealth() < this.getMaxHealth() - 0.5F) {
                    this.heal(4.0F);
                } else if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.notifyMagicalHeal(owner, 4.0F);
                    owner.heal(4.0F);
                }
            }
            case GENESIS -> {
                target.invulnerableTime = 0;
                boolean smiteTarget = target.getType().is(EntityTypeTags.UNDEAD)
                        || (target instanceof SpellEidolonEntity se
                        && (se.getEffectiveMateria() == EssenceType.WRAITH || se.getEffectiveMateria() == EssenceType.OBLIVION))
                        || ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L) > level.getGameTime()
                        || ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .getLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL).orElse(0L) > level.getGameTime();
                float bonus = smiteTarget ? (4.5F + tier) : (2.5F + tier * 0.75F);
                target.hurtServer(level, level.damageSources().magic(), bonus);
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
            case OBLIVION -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 3.0F + tier * 0.85F);
                target.setDeltaMovement(dir.x * 1.45D, 0.38D, dir.z * 1.45D);
                target.hasImpulse = true;
                target.igniteForSeconds(5);
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
            }
            case CHIMERA -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 2.8F + tier * 0.8F);
                target.setDeltaMovement(dir.x * 1.45D, 0.40D, dir.z * 1.45D);
                target.hasImpulse = true;
                String rolled = ddraig.net.entropica.util.EntityHelper.getPersistentData(this)
                        .getString("SpellChimeraSummonElement").orElse("NETHER");
                switch (rolled) {
                    case "AIR" -> {
                        target.setDeltaMovement(dir.x * 1.65D, 0.65D, dir.z * 1.65D);
                        target.hasImpulse = true;
                    }
                    case "EARTH" -> {
                        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 120L);
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                        for (double d = 1.0D; d <= 4.0D; d += 1.0D) {
                            Vec3 fPos = this.position().add(dir.x * d, 0.1D, dir.z * d);
                            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                                    fPos.x, fPos.y, fPos.z, 8, 0.25D, 0.15D, 0.25D, 0.08D);
                            for (LivingEntity lineFoe : level.getEntitiesOfClass(LivingEntity.class,
                                    new AABB(fPos.x - 1.0D, fPos.y - 0.5D, fPos.z - 1.0D, fPos.x + 1.0D, fPos.y + 1.8D, fPos.z + 1.0D))) {
                                if (lineFoe == target || lineFoe == this || lineFoe == owner || !lineFoe.isAlive() || this.isFriendlyTo(lineFoe)) continue;
                                lineFoe.hurtServer(level, level.damageSources().magic(), 2.5F + tier * 0.65F);
                                SpellCombatReactionEngine.applyTimedAttributeShred(lineFoe, level, 2.0D, 0.0D, 100L);
                            }
                        }
                    }
                    case "WATER" -> {
                        SpellLingeringZoneManager.applySoaked(target, 140);
                        this.heal(2.5F);
                        if (owner != null && owner.isAlive()) {
                            SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.20F, this.apparatusPotency * 0.75F);
                        }
                    }
                    default -> target.igniteForSeconds(5);
                }
            }
            case AETHER -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 3.5F + tier * 0.9F);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                target.setDeltaMovement(dir.x * 0.45D, 1.18D, dir.z * 0.45D); // 8m airborne zero-g float!
                target.hasImpulse = true;
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 45, 0));
            }
            case ENTROPIC -> {
                target.invulnerableTime = 0;
                float hitDmg = 3.5F + tier * 0.9F;
                target.hurtServer(level, level.damageSources().magic(), hitDmg);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                target.setDeltaMovement(dir.x * 1.85D, 0.40D, dir.z * 1.85D); // 8m knockback!
                target.hasImpulse = true;
                this.heal(hitDmg * 0.35F);
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.35F, this.apparatusPotency * 0.75F);
                }
            }
            case CELESTIAL -> {
                target.invulnerableTime = 0;
                float smiteMult = target.getType().is(EntityTypeTags.UNDEAD) ? 1.75F : 1.0F;
                float hitDmg = (3.5F + tier * 0.9F) * smiteMult;
                target.hurtServer(level, level.damageSources().magic(), hitDmg);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                target.setDeltaMovement(dir.x * 1.85D, 0.40D, dir.z * 1.85D);
                target.hasImpulse = true;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, hitDmg * 0.35F);
                        ally.heal(hitDmg * 0.35F);
                    }
                }
            }
            case PRISMATIC -> {
                target.invulnerableTime = 0;
                float smiteMult = target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                target.hurtServer(level, level.damageSources().magic(), (3.2F + tier * 0.85F) * smiteMult);
            }
            case EMPYREAN -> {
                target.invulnerableTime = 0;
                float hitDmg = 4.5F + tier * 1.05F;
                target.hurtServer(level, level.damageSources().magic(), hitDmg);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D, 0.0D, 160L);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                SpellLingeringZoneManager.grantPartyAbsorption(level, owner != null ? owner : this, hitDmg * 0.40F, 24.0F, 12.0D);
            }
            case CATACLYSM -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 4.5F + tier * 1.05F);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D, 0.0D, 160L);
                target.igniteForSeconds(6);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position()).normalize().scale(0.65D);
                    foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                    foe.hasImpulse = true;
                }
            }
            case TERMINUS -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().magic(), 4.5F + tier * 1.05F);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D, 0.0D, 160L);
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 60);
            }
            case ESCHATON -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().fellOutOfWorld(), 6.0F + tier * 1.25F);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D, 0.0D, 180L);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position()).normalize().scale(0.75D);
                    foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                    foe.hasImpulse = true;
                }
            }
            case APOTHEOSIS -> {
                target.invulnerableTime = 0;
                float smite = target.getType().is(EntityTypeTags.UNDEAD) ? 2.0F : 1.0F;
                float hitDmg = (6.0F + tier * 1.25F) * smite;
                target.hurtServer(level, level.damageSources().magic(), hitDmg);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D, 0.0D, 180L);
                target.igniteForSeconds(5);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, hitDmg * 0.25F);
                        ally.heal(hitDmg * 0.25F);
                    }
                }
            }
            case ENTROPICA -> {
                target.invulnerableTime = 0;
                target.hurtServer(level, level.damageSources().fellOutOfWorld(), 6.5F + tier * 1.3F);
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D, 0.0D, 180L);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position()).normalize().scale(0.70D);
                    foe.setDeltaMovement(pull.x, 0.16D, pull.z);
                    foe.hasImpulse = true;
                }
            }
            default -> {
            }
        }
    }

    private void tickAnchoredSentry(ServerLevel level) {
        // Keep anchored firmly at spawn coordinates
        if (this.anchorPos != Vec3.ZERO && this.position().distanceToSqr(this.anchorPos) > 0.25D) {
            this.teleportTo(this.anchorPos.x, this.anchorPos.y, this.anchorPos.z);
        }
        this.setDeltaMovement(Vec3.ZERO);

        LivingEntity owner = resolveSummoner(level);
        EssenceType mat = getEffectiveMateria();
        boolean mutated = isMutated();
        int tier = this.spell.getEffectiveTier();

        if (mutated) {
            // Element-specific passive sentry field behaviors
            if (mat == EssenceType.AIR && this.ageTicks % 15 == 0) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.setDeltaMovement(foe.getDeltaMovement().x, 0.85D, foe.getDeltaMovement().z);
                    foe.hasImpulse = true;
                    level.sendParticles(ParticleTypes.GUST, foe.getX(), foe.getY(0.2D), foe.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.02D);
                }
            } else if ((mat == EssenceType.WATER || mat == EssenceType.STORM) && this.ageTicks % 20 == 0) {
                // Storm Tempest-Squall Sentry extinguishes fires in a 6-block radius (Water in 3-block radius)!
                int fireRad = (mat == EssenceType.STORM) ? 6 : 3;
                BlockPos c = this.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-fireRad, -1, -fireRad), c.offset(fireRad, 2, fireRad))) {
                    if (level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                        level.removeBlock(p, false);
                    }
                }
            } else if (mat == EssenceType.GLACIAL && this.ageTicks % 20 == 0) {
                // Glacial Cryo-Sentry radiates a 4-block chilling field!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, true));
                    foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 40, foe.getTicksFrozen() + 30));
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(0.8D), this.getZ(), 6, 1.8D, 0.4D, 1.8D, 0.01D);
            } else if ((mat == EssenceType.OVERGROWTH || mat == EssenceType.SPORE) && this.ageTicks % 35 == 0) {
                int r = 6;
                BlockPos c = this.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-r, -1, -r), c.offset(r, 1, r))) {
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof BonemealableBlock b && b.isValidBonemealTarget(level, p, bs) && level.random.nextFloat() < 0.25F) {
                        b.performBonemeal(level, level.random, p.immutable(), bs);
                    } else if (mat == EssenceType.SPORE && (bs.is(Blocks.MYCELIUM) || bs.is(Blocks.PODZOL))
                            && level.getBlockState(p.above()).isAir() && level.random.nextFloat() < 0.08F) {
                        level.setBlockAndUpdate(p.above(), level.random.nextBoolean()
                                ? Blocks.RED_MUSHROOM.defaultBlockState() : Blocks.BROWN_MUSHROOM.defaultBlockState());
                    }
                }
                if (mat == EssenceType.SPORE) {
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(1.0D), this.getZ(),
                            10, 2.2D, 0.5D, 2.2D, 0.02D);
                }
            } else if (mat == EssenceType.RADIANT) {
                // Radiant Prism Sentry reflects incoming hostile projectiles!
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(6.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                    Vec3 rev = proj.getDeltaMovement().scale(-1.15D);
                    proj.setDeltaMovement(rev);
                    proj.setOwner(owner != null ? owner : this);
                    proj.hasImpulse = true;
                    level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 4, 0.1D, 0.1D, 0.1D, 0.03D);
                }
            } else if (mat == EssenceType.VOID && this.ageTicks % 20 == 0) {
                // Void Sentry emits periodic inward gravity pulses pulling distant enemies!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(0.50D);
                    foe.setDeltaMovement(foe.getDeltaMovement().add(inward.x, 0.14D, inward.z));
                    foe.hasImpulse = true;
                }
            } else if (mat == EssenceType.TAIGA && this.ageTicks % 20 == 0) {
                // Frost-Howler Spire: increases nearby friendly attack speed (+25%) & grants cold resistance!
                long until = level.getGameTime() + 45L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ally.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 0, true, true));
                        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(ally, level, 0.25D, 45L);
                        ally.setTicksFrozen(0);
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL, until);
                    }
                }
            } else if (mat == EssenceType.OASIS && this.ageTicks % 30 == 0) {
                // Aquifer Spring Spire: pulses area healing, farmland hydration, and crop/cactus growth ticks!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, 1.5F + tier * 0.35F);
                        ally.heal(1.5F + tier * 0.35F);
                    }
                }
                BlockPos c = this.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-6, -1, -6), c.offset(6, 2, 6))) {
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                        level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                    } else if (bs.getBlock() instanceof BonemealableBlock b && b.isValidBonemealTarget(level, p, bs) && level.random.nextFloat() < 0.20F) {
                        b.performBonemeal(level, level.random, p.immutable(), bs);
                    } else if ((bs.is(Blocks.CACTUS) || bs.is(Blocks.SUGAR_CANE)) && level.random.nextFloat() < 0.30F) {
                        bs.randomTick(level, p.immutable(), level.random);
                    }
                }
            } else if (mat == EssenceType.VITAE && this.ageTicks % 30 == 0) {
                // Cardiovascular Spire: audible heartbeat healing allies & curing debuffs in 6m!
                level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.55F, 1.15F);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, 2.0F + tier * 0.45F);
                        ally.heal(2.0F + tier * 0.45F);
                        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
                    }
                }
            } else if (mat == EssenceType.BLOOD && this.ageTicks % 20 == 0) {
                // Exsanguination Pylon: tethers up to 2 nearby hostiles to drain HP and transfer to nearest friendly player!
                int drainedCount = 0;
                float totalDrained = 0.0F;
                float drainPerTarget = Math.max(1.8F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.18F);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.hurtServer(level, level.damageSources().magic(), drainPerTarget);
                    totalDrained += drainPerTarget * 0.75F;
                    Vec3 from = this.getEyePosition();
                    Vec3 to = foe.getEyePosition();
                    Vec3 step = to.subtract(from);
                    for (int s = 1; s <= 5; s++) {
                        Vec3 tp = from.add(step.scale(s / 5.0D));
                        level.sendParticles(ParticleTypes.CRIMSON_SPORE, tp.x, tp.y, tp.z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
                    }
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, foe.getX(), foe.getY(0.7D), foe.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.02D);
                    if (++drainedCount >= 2) break;
                }
                if (totalDrained > 0.0F) {
                    Player recipient = (owner instanceof Player p && p.isAlive()) ? p : level.getNearestPlayer(this, 10.0D);
                    if (recipient != null && recipient.isAlive() && (recipient == owner || this.isFriendlyTo(recipient))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(recipient, totalDrained);
                        recipient.heal(totalDrained);
                    }
                }
            } else if (mat == EssenceType.AURA && this.ageTicks % 20 == 0) {
                // Chakra Respiration Spire: 8m aura granting allies a 20% Materia fuel discount & Slow Falling!
                long until = level.getGameTime() + 35L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0, true, false));
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL, until);
                    }
                }
            } else if (mat == EssenceType.AMBER && this.ageTicks % 20 == 0) {
                // Resinite Stasis Spire: 8m aura providing +4 Armor to allies and item despawn/fire prevention!
                AABB box = this.getBoundingBox().inflate(8.0D);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 4.0D, 0.0D, 0.0D, 0.0D, 35L);
                    }
                }
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
                    if (item.isAlive()) {
                        item.setUnlimitedLifetime();
                        item.setInvulnerable(true);
                    }
                }
            } else if (mat == EssenceType.ECLIPSE && this.ageTicks % 20 == 0) {
                // Black Sun Corona Spire: 6m blinding twilight aura!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));
                }
            } else if (mat == EssenceType.BLIGHT && this.ageTicks % 20 == 0) {
                // Necro-Briar Spire: 8m 30% slow aura!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                }
            } else if (mat == EssenceType.ASTRAL && this.ageTicks % 20 == 0) {
                // Pulsar Singularity Spire: 8m pulling gravitational aura + redstone power pulse!
                SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, this.blockPosition(), 4, 30);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(0.38D);
                    foe.setDeltaMovement(foe.getDeltaMovement().add(inward.x, 0.10D, inward.z));
                    foe.hasImpulse = true;
                }
            } else if (mat == EssenceType.SOULFIRE && this.ageTicks % 10 == 0) {
                // Phantasmal Pyre Spire: 8m projectile-burning aura!
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(8.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this || proj instanceof SpellProjectileEntity) continue;
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, proj.getX(), proj.getY(), proj.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.03D);
                    proj.discard();
                }
            } else if (mat == EssenceType.VAPOR && this.ageTicks % 20 == 0) {
                // Steam-Vent Obelisk: continuous blinding smoke ring slowing enemies in 8m!
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(0.8D), this.getZ(), 16, 3.2D, 0.4D, 3.2D, 0.02D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
                }
            } else if ((mat == EssenceType.NULL_R || mat == EssenceType.NULL_U || mat == EssenceType.ABYSS) && this.ageTicks % 20 == 0) {
                // Event Horizon / Oblivion / Trench Spire: 8m pulling / whirlpool + blinding/silencing aura!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 radial = this.position().subtract(foe.position()).normalize();
                    Vec3 pullVec = radial.scale(0.42D);
                    if (mat == EssenceType.ABYSS) {
                        // Trench Spire whirlpool rotational current!
                        Vec3 swirl = new Vec3(-radial.z, 0.0D, radial.x).scale(0.32D);
                        pullVec = pullVec.add(swirl);
                        level.sendParticles(ParticleTypes.BUBBLE_POP, foe.getX(), foe.getY(0.3D), foe.getZ(), 4, 0.25D, 0.1D, 0.25D, 0.02D);
                    }
                    foe.setDeltaMovement(foe.getDeltaMovement().add(pullVec.x, 0.10D, pullVec.z));
                    foe.hasImpulse = true;
                    if (mat == EssenceType.NULL_R) {
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                    } else if (mat == EssenceType.NULL_U) {
                        SpellLingeringZoneManager.applyArcaneSilence(foe, 40);
                    } else {
                        SpellLingeringZoneManager.applySoaked(foe, 60);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                    }
                }
            } else if (mat == EssenceType.DAWN && this.ageTicks % 20 == 0) {
                // Sol-Zephyr Spire: 8m aura granting allies Slow Falling and Speed!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false));
                        ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false));
                    }
                }
            } else if (mat == EssenceType.AEGIS && this.ageTicks % 20 == 0) {
                // Citadel Fortress Spire: +6 Armor aura to nearby allies!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 0.0D, 0.0D, 0.0D, 35L);
                    }
                }
            } else if (mat == EssenceType.AURORA && this.ageTicks % 10 == 0) {
                // Prismatic Aurora Spire: 8m aura granting allies Frost Walker & deflecting hostile projectiles!
                long fwUntil = level.getGameTime() + 35L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, fwUntil);
                    }
                }
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(8.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                    proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.1D));
                    proj.setOwner(owner != null ? owner : this);
                    proj.hasImpulse = true;
                }
            } else if (mat == EssenceType.MIRAGE && this.ageTicks % 20 == 0) {
                // Fata Morgana Obelisk: 12m false player illusions drawing all mob attacks away!
                List<SpellEidolonEntity> activeIllusions = level.getEntitiesOfClass(
                        SpellEidolonEntity.class,
                        this.getBoundingBox().inflate(12.0D),
                        e -> e.isAlive() && e.shortLivedIllusion && e.getEffectiveMateria() == EssenceType.MIRAGE
                );
                if (activeIllusions.size() < 2 && this.ageTicks % 60 == 20) {
                    double angle = level.random.nextDouble() * Math.PI * 2.0D;
                    double dist = 3.0D + level.random.nextDouble() * 4.0D;
                    Vec3 illPos = this.position().add(Math.cos(angle) * dist, 0.0D, Math.sin(angle) * dist);
                    SpellEidolonEntity spawned = spawnShortLivedDecoy(
                            level, owner, illPos, EssenceType.MIRAGE, 12.0F, 80, "Fata Morgana Illusion", null);
                    activeIllusions = new java.util.ArrayList<>(activeIllusions);
                    activeIllusions.add(spawned);
                }
                for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(12.0D))) {
                    if (!mob.isAlive() || this.isFriendlyTo(mob)) continue;
                    if (mob instanceof Monster || mob.getTarget() == owner) {
                        LivingEntity lure = activeIllusions.isEmpty()
                                ? this
                                : activeIllusions.get(level.random.nextInt(activeIllusions.size()));
                        mob.setTarget(lure);
                    }
                }
            } else if (mat == EssenceType.WRAITH && this.ageTicks % 20 == 0) {
                // Banshee Wail Obelisk: 8m shriek silencing abilities & slowing mobs 40%!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 45);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
                }
            } else if (mat == EssenceType.BARROW && this.ageTicks % 20 == 0) {
                // Cairn Beacon: 8m slow 40% on enemies & +4 Armor to allied undead/summons!
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if ((ent instanceof SpellEidolonEntity || ent.getType().is(EntityTypeTags.UNDEAD))
                            && (ent == owner || this.isFriendlyTo(ent))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 4.0D, 0.0D, 0.0D, 0.0D, 35L);
                    } else if (ent instanceof Monster && !this.isFriendlyTo(ent) && ent != owner) {
                        ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
                    }
                }
            } else if (mat == EssenceType.PYRE && this.ageTicks % 20 == 0) {
                // Cremation Pyre Spire: +4 Armor to nearby summons & continuous 8m wildfire jets!
                level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.9D), this.getZ(), 18, 2.8D, 0.5D, 2.8D, 0.04D);
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if (ent instanceof SpellEidolonEntity && this.isFriendlyTo(ent)) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 4.0D, 0.0D, 0.0D, 0.0D, 35L);
                    } else if (ent instanceof Monster && !this.isFriendlyTo(ent) && ent != owner) {
                        ent.igniteForSeconds(4);
                        ent.hurtServer(level, level.damageSources().onFire(), 2.0F + tier * 0.55F);
                    }
                }
            } else if (mat == EssenceType.PENUMBRA && this.ageTicks % 5 == 0) {
                // Twilight Corona Citadel: +6 Armor, +3 Toughness, complete debuff immunity; 8m repulsion pulse every 4.0s (80t)!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        if (this.ageTicks % 20 == 0) {
                            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                        }
                        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
                    }
                }
                if (this.ageTicks % 80 == 0) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.15D);
                        foe.setDeltaMovement(away.x, 0.32D, away.z);
                        foe.hasImpulse = true;
                    }
                }
            } else if (mat == EssenceType.RIME && this.ageTicks % 20 == 0) {
                // Hoarfrost Glacial Citadel Spire: +6 Armor, +3 Toughness, complete Fire & Freezing immunity; 8m root burst every 4.0s!
                long coldUntil = level.getGameTime() + 45L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                        ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 45, 0, true, false));
                        ally.clearFire();
                        ally.setTicksFrozen(0);
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL, coldUntil);
                    }
                }
                if (this.ageTicks % 80 == 0) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                    }
                }
            } else if (mat == EssenceType.SPRING && this.ageTicks % 20 == 0) {
                // Aquifer Springwater Font Spire: +6 Armor, +3 Toughness, Water Breathing; 8m 3-heart heal & cleanse every 4.0s!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                        ally.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 45, 0, true, false));
                        if (this.ageTicks % 80 == 0) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, 6.0F);
                            ally.heal(6.0F);
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
                        }
                    }
                }
            } else if (mat == EssenceType.STATIC && this.ageTicks % 20 == 0) {
                // Dielectric Tesla Citadel Spire: +6 Armor, +3 Toughness, Lightning/Fall immunity; 8m EMP every 4.0s!
                long until = level.getGameTime() + 35L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_STATIC_SENTRY_IMMUNE_UNTIL, until);
                    }
                }
                if (this.ageTicks % 80 == 0) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        if (foe instanceof net.minecraft.world.entity.monster.Creeper creeper) creeper.setSwellDir(-1);
                        Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.05D);
                        foe.setDeltaMovement(away.x, 0.30D, away.z);
                        foe.hasImpulse = true;
                    }
                }
            } else if (mat == EssenceType.SYLVAN && this.ageTicks % 20 == 0) {
                // Petrified Monolith Sentry: +6 Armor, +3 Toughness, KB Resistance; 8m root wave every 4.0s!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.65D, 0.0D, 35L);
                    }
                }
                if (this.ageTicks % 80 == 0) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                    }
                }
            } else if (mat == EssenceType.MIASMA && this.ageTicks % 60 == 0) {
                // Cadaverous Geyser Pylon: boiling swamp acid every 3.0s in 6m launching airborne, -2 armor, buff purge!
                level.sendParticles(ParticleTypes.SNEEZE, this.getX(), this.getY(0.6D), this.getZ(), 24, 2.2D, 0.8D, 2.2D, 0.05D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.hurtServer(level, level.damageSources().magic(), 3.0F + this.spell.getEffectiveTier() * 0.85F);
                    foe.setDeltaMovement(foe.getDeltaMovement().x, 0.78D, foe.getDeltaMovement().z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    for (MobEffectInstance eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                        if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                    }
                }
            } else if (mat == EssenceType.GENESIS && this.ageTicks % 20 == 0) {
                // Dawn-Glory Spire: +6 Armor, +3 Toughness to adjacent allies + multi-target golden water beams!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                    }
                }
                int fired = 0;
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    fireConstructBolt(level, owner, foe);
                    if (++fired >= 3) break;
                }
            } else if (mat == EssenceType.OBLIVION && this.ageTicks % 20 == 0) {
                // Pyre of Nonexistence Obelisk: 8m Extinction Aura melting 1 armor/s (up to -8) & burning 1 heart/s!
                long now = level.getGameTime();
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    long lastTick = fData.getLong("SpellExtinctionAuraLastTick").orElse(0L);
                    int stacks = (now - lastTick <= 35L) ? Math.min(8, fData.getInt("SpellExtinctionAuraStacks").orElse(0) + 1) : 1;
                    fData.putLong("SpellExtinctionAuraLastTick", now);
                    fData.putInt("SpellExtinctionAuraStacks", stacks);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, stacks, 0.0D, 45L);
                    foe.hurtServer(level, level.damageSources().onFire(), 2.0F);
                }
            } else if (mat == EssenceType.CHIMERA && this.ageTicks % 20 == 0) {
                // Chromatic Crucible Spire: 8m aura rotating randomly between Base 4 every 4.0s (80t)!
                var pData = ddraig.net.entropica.util.EntityHelper.getPersistentData(this);
                if (this.ageTicks % 80 == 0) {
                    String next = switch (level.random.nextInt(4)) {
                        case 0 -> "AIR";
                        case 1 -> "EARTH";
                        case 2 -> "WATER";
                        default -> "NETHER";
                    };
                    pData.putString("SpellChimeraSummonElement", next);
                }
                String mode = pData.getString("SpellChimeraSummonElement").orElse("NETHER");
                if ("AIR".equals(mode)) {
                    for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(8.0D))) {
                        if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                        proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.1D));
                        proj.hasImpulse = true;
                    }
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    switch (mode) {
                        case "EARTH" -> SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 45L);
                        case "WATER" -> {
                            for (MobEffectInstance eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                                if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                            }
                        }
                        case "NETHER" -> foe.igniteForSeconds(3);
                        default -> {
                        }
                    }
                }
            } else if (mat == EssenceType.AETHER && this.ageTicks % 20 == 0) {
                // Apex Firmament Spire of Equilibrium: 10m Firmament Resonance Field lifting hostiles, -1 armor/s, cleansing ally debuffs!
                long now = level.getGameTime();
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if (ent == owner || this.isFriendlyTo(ent)) {
                        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                    } else if (ent instanceof Monster) {
                        ent.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 25, 0, true, true));
                        var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(ent);
                        long last = fData.getLong("SpellAetherSentryLastTick").orElse(0L);
                        int stacks = (now - last <= 35L) ? Math.min(8, fData.getInt("SpellAetherSentryStacks").orElse(0) + 1) : 1;
                        fData.putLong("SpellAetherSentryLastTick", now);
                        fData.putInt("SpellAetherSentryStacks", stacks);
                        SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, stacks, 0.0D, 45L);
                    }
                }
            } else if (mat == EssenceType.ENTROPIC && this.ageTicks % 20 == 0) {
                // Apex Event Horizon Monolith: 10m Decay Field draining 1.5 hearts/s (3.0 HP), -1 armor/s, blinding hostiles!
                long now = level.getGameTime();
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.hurtServer(level, level.damageSources().magic(), 3.0F);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                    var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    long last = fData.getLong("SpellEntropicSentryLastTick").orElse(0L);
                    int stacks = (now - last <= 35L) ? Math.min(8, fData.getInt("SpellEntropicSentryStacks").orElse(0) + 1) : 1;
                    fData.putLong("SpellEntropicSentryLastTick", now);
                    fData.putInt("SpellEntropicSentryStacks", stacks);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, stacks, 0.0D, 45L);
                }
            } else if (mat == EssenceType.CELESTIAL && this.ageTicks % 20 == 0) {
                // Apex White-Hole Dawn Spire: 10m Genesis Field tethering hostiles, -2 armor/s, & 6m waves of healing and smiting!
                long now = level.getGameTime();
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if (ent == owner || this.isFriendlyTo(ent)) {
                        if (ent.distanceToSqr(this) <= 36.0D) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ent, 3.0F);
                            ent.heal(3.0F);
                        }
                    } else if (ent instanceof Monster) {
                        Vec3 inward = this.position().subtract(ent.position()).normalize().scale(0.38D);
                        ent.setDeltaMovement(ent.getDeltaMovement().add(inward.x, 0.06D, inward.z));
                        ent.hasImpulse = true;
                        var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(ent);
                        long last = fData.getLong("SpellCelestialSentryLastTick").orElse(0L);
                        int stacks = (now - last <= 35L) ? Math.min(10, fData.getInt("SpellCelestialSentryStacks").orElse(0) + 2) : 2;
                        fData.putLong("SpellCelestialSentryLastTick", now);
                        fData.putInt("SpellCelestialSentryStacks", stacks);
                        SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, stacks, 0.0D, 45L);
                        if (ent.distanceToSqr(this) <= 36.0D) {
                            float smite = ent.getType().is(EntityTypeTags.UNDEAD) ? 5.0F : 2.5F;
                            ent.hurtServer(level, level.damageSources().magic(), smite);
                        }
                    }
                }
            } else if (mat == EssenceType.PRISMATIC && this.ageTicks % 5 == 0) {
                // Refractive Mirror Monolith: 12m refractive dome reflecting 100% of hostile projectile and magic damage back at attackers!
                long reflectUntil = level.getGameTime() + 15L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_PRISMATIC_DEFLECT_SENTRY_UNTIL, reflectUntil);
                    }
                }
                ddraig.net.entropica.util.EntityHelper.getPersistentData(this)
                        .putLong(SpellLingeringZoneManager.TAG_PRISMATIC_DEFLECT_SENTRY_UNTIL, reflectUntil);
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(12.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                    if (proj instanceof SpellProjectileEntity sp && (sp.getOwner() == owner || this.isFriendlyTo(sp.getOwner()))) continue;
                    Vec3 rev = proj.getOwner() != null
                            ? proj.getOwner().getEyePosition().subtract(proj.position()).normalize().scale(Math.max(1.25D, proj.getDeltaMovement().length()))
                            : proj.getDeltaMovement().scale(-1.15D);
                    proj.setDeltaMovement(rev);
                    proj.setOwner(owner != null ? owner : this);
                    proj.hasImpulse = true;
                    level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 4, 0.1D, 0.1D, 0.1D, 0.03D);
                }
            } else if (mat == EssenceType.EMPYREAN && this.ageTicks % 20 == 0) {
                // Heartwood Canopy Obelisk: 16m Sanctuary Field granting +8 Armor, +4 Toughness, and continuous absorption up to 10.0 HP!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 8.0D, 4.0D, 0.0D, 0.0D, 35L);
                        if (ally.getAbsorptionAmount() < 10.0F) {
                            ally.setAbsorptionAmount(Math.min(10.0F, ally.getAbsorptionAmount() + 2.5F));
                        }
                    }
                }
            } else if (mat == EssenceType.CATACLYSM && this.ageTicks % 20 == 0) {
                // Volcanic Eruption Spire: 16m Gravitational Caldera Field dragging hostiles inward, -1 armor/s, burning 1.5 hearts/s (3.0 HP)!
                long now = level.getGameTime();
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(0.42D);
                    foe.setDeltaMovement(foe.getDeltaMovement().add(inward.x, 0.08D, inward.z));
                    foe.hasImpulse = true;
                    foe.igniteForSeconds(4);
                    foe.hurtServer(level, level.damageSources().onFire(), 3.0F);
                    var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    long last = fData.getLong("SpellCataclysmSentryLastTick").orElse(0L);
                    int stacks = (now - last <= 35L) ? Math.min(10, fData.getInt("SpellCataclysmSentryStacks").orElse(0) + 1) : 1;
                    fData.putLong("SpellCataclysmSentryLastTick", now);
                    fData.putInt("SpellCataclysmSentryStacks", stacks);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, stacks, 0.0D, 45L);
                }
            } else if (mat == EssenceType.TERMINUS && this.ageTicks % 20 == 0) {
                // Anchored Permafrost Elemental Spire: 16m Polar Freeze Field cutting hostile speed 50% and continuously freezing water!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, true, true));
                    foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 40, foe.getTicksFrozen() + 25));
                }
                BlockPos c = this.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-8, -1, -8), c.offset(8, 1, 8))) {
                    if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir() && level.random.nextFloat() < 0.35F) {
                        level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            } else if (mat == EssenceType.ESCHATON && this.ageTicks % 20 == 0) {
                // Accretion Disk Singularity Spire: 20m Gravitational Accretion Field dragging hostiles & projectiles inward, melting 1.5 armor/s!
                long now = level.getGameTime();
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(20.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this || proj instanceof SpellProjectileEntity) continue;
                    Vec3 pull = this.position().add(0.0D, 1.0D, 0.0D).subtract(proj.position()).normalize().scale(0.65D);
                    proj.setDeltaMovement(proj.getDeltaMovement().scale(0.35D).add(pull));
                    proj.hasImpulse = true;
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(0.50D);
                    foe.setDeltaMovement(foe.getDeltaMovement().add(inward.x, 0.08D, inward.z));
                    foe.hasImpulse = true;
                    var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    long last = fData.getLong("SpellEschatonSentryLastTick").orElse(0L);
                    double stacks = (now - last <= 35L) ? Math.min(12.0D, fData.getDouble("SpellEschatonSentryStacks").orElse(0.0D) + 1.5D) : 1.5D;
                    fData.putLong("SpellEschatonSentryLastTick", now);
                    fData.putDouble("SpellEschatonSentryStacks", stacks);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, stacks, 0.0D, 45L);
                }
            } else if (mat == EssenceType.APOTHEOSIS && this.ageTicks % 20 == 0) {
                // White-Hole Dawn Obelisk: 20m Sanctuary Field granting allies +10 Armor, +6 Toughness, and absorption up to 15.0 HP!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 10.0D, 6.0D, 0.0D, 0.0D, 35L);
                        if (ally.getAbsorptionAmount() < 15.0F) {
                            ally.setAbsorptionAmount(Math.min(15.0F, ally.getAbsorptionAmount() + 3.5F));
                        }
                    }
                }
            } else if (mat == EssenceType.ENTROPICA && this.ageTicks % 20 == 0) {
                // Altar of the Horizon: 20m Equilibrium Field dealing 2 hearts/s (4.0 HP) true damage to hostiles & +2 hearts/s (4.0 HP) healing to allies!
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if (ent == owner || this.isFriendlyTo(ent)) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                        ent.heal(4.0F);
                    } else if (ent instanceof Monster) {
                        ent.hurtServer(level, level.damageSources().fellOutOfWorld(), 4.0F);
                    }
                }
            }
        }

        // Pure-aura, tether, dome, and geyser spires do not fire generic single-target turret bolts!
        if (mutated && isPureAuraOrFieldSentry(mat)) {
            return;
        }

        double sentryRange = !mutated ? 18.0D : switch (mat) {
            case ESCHATON, APOTHEOSIS, ENTROPICA -> 24.0D;
            case EMPYREAN, CATACLYSM, TERMINUS -> 20.0D;
            case AETHER, ENTROPIC, CELESTIAL -> 18.0D;
            default -> isLifecycleRefreshMateria(mat) ? 16.0D : 18.0D;
        };
        LivingEntity target = findNearestHostile(level, sentryRange);
        if (target == null) return;

        Vec3 aim = target.getEyePosition().subtract(this.getEyePosition());
        this.setYRot((float) (Math.atan2(aim.z, aim.x) * (180.0D / Math.PI)) - 90.0F);
        this.yHeadRot = this.getYRot();

        if (this.actionCooldown <= 0) {
            this.actionCooldown = (mutated && (mat == EssenceType.FROZEN || mat == EssenceType.GLACIAL || mat == EssenceType.LIGHTNING)) ? 16 : 26;
            fireConstructBolt(level, owner, target);
        }
    }

    public static boolean isPureAuraOrFieldSentry(EssenceType mat) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.isPureAuraOrFieldSentry(mat);
    }

    private void fireConstructBolt(ServerLevel level, @Nullable LivingEntity owner, LivingEntity target) {
        EssenceType mat = getEffectiveMateria();
        Vec3 dir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
        if (isMutated() && mat == EssenceType.PENUMBRA && getArchetype() == TriadArchetype.SENTRY) {
            // Section 6.41 Twilight Corona Citadel fires twin light/shadow suppression lances!
            Vec3 side = new Vec3(-dir.z, 0.0D, dir.x).normalize().scale(0.32D);
            spawnSingleConstructBolt(level, owner, mat, dir, side);
            spawnSingleConstructBolt(level, owner, mat, dir, side.scale(-1.0D));
            level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.45F, 1.55F);
            return;
        }
        spawnSingleConstructBolt(level, owner, mat, dir, Vec3.ZERO);
        level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.45F, 1.55F);
    }

    private void spawnSingleConstructBolt(
            ServerLevel level,
            @Nullable LivingEntity owner,
            EssenceType mat,
            Vec3 dir,
            Vec3 lateralOffset
    ) {
        SpellProjectileEntity bolt = new SpellProjectileEntity(level, owner != null ? owner : this);
        bolt.setPos(
                this.getX() + dir.x * 0.7D + lateralOffset.x,
                this.getEyeY() - 0.1D + dir.y * 0.7D + lateralOffset.y,
                this.getZ() + dir.z * 0.7D + lateralOffset.z
        );
        bolt.configurePayload(this.spell, this.slotIndex, this.fallbackMateria, this.apparatusPotency * 0.65F);
        if (isMutated()) {
            bolt.setSentryBoltMateria(mat);
            if (mat == EssenceType.LIGHTNING || mat == EssenceType.UNDEAD
                    || mat == EssenceType.ASTRAL || mat == EssenceType.SOULFIRE || mat == EssenceType.EMPYREAN) {
                bolt.setHomingGuidance(true);
            }
            if (mat == EssenceType.VOID || mat == EssenceType.NULL_R || mat == EssenceType.NULL_U
                    || mat == EssenceType.AETHER || mat == EssenceType.ENTROPIC || mat == EssenceType.CELESTIAL
                    || mat == EssenceType.EMPYREAN || mat == EssenceType.CATACLYSM || mat == EssenceType.TERMINUS
                    || mat == EssenceType.ESCHATON || mat == EssenceType.APOTHEOSIS || mat == EssenceType.ENTROPICA) {
                bolt.setWallPhasing(true);
            }
            if (mat == EssenceType.OVERGROWTH || mat == EssenceType.BLIGHT
                    || mat == EssenceType.NULL_R || mat == EssenceType.DAWN || mat == EssenceType.AURORA
                    || mat == EssenceType.RIME || mat == EssenceType.SPRING || mat == EssenceType.PRISMATIC) {
                bolt.addBonusPierces((mat == EssenceType.AURORA || mat == EssenceType.DAWN || mat == EssenceType.RIME || mat == EssenceType.PRISMATIC) ? 3 : 2);
            }
        }
        if (isMutated() && (mat == EssenceType.NETHER || mat == EssenceType.MAGMA
                || mat == EssenceType.SYLVAN || mat == EssenceType.OBLIVION || mat == EssenceType.CHIMERA)) {
            bolt.setNoGravity(false);
            bolt.shoot(dir.x, dir.y + 0.20D, dir.z, 1.25F, 0.0F);
        } else if (isMutated() && mat == EssenceType.ARID) {
            bolt.shoot(dir.x, dir.y, dir.z, 2.15F, 0.0F);
        } else {
            bolt.shoot(dir.x, dir.y, dir.z, 1.55F, 0.0F);
        }
        level.addFreshEntity(bolt);
    }

    private void tickPhantomDecoy(ServerLevel level) {
        LivingEntity owner = resolveSummoner(level);
        EssenceType mat = getEffectiveMateria();
        boolean mutated = isMutated();

        // Taunt all nearby hostile monsters onto this Decoy every 15 ticks (or immediately on spawn for short-lived illusions)!
        if (this.ageTicks == 1 || this.ageTicks % 15 == 0) {
            double tauntRad = !mutated ? 9.0D : switch (mat) {
                case ESCHATON, APOTHEOSIS, ENTROPICA -> 20.0D;
                case AETHER, ENTROPIC, CELESTIAL, PRISMATIC, EMPYREAN, CATACLYSM, TERMINUS -> 16.0D;
                case PENUMBRA, RIME, SPRING, STATIC, SYLVAN, GENESIS, OBLIVION, CHIMERA -> 14.0D;
                case ARID, SPORE, DUST, MIRAGE, MIASMA -> 12.0D;
                default -> 9.0D;
            };
            for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(tauntRad))) {
                if (!mob.isAlive() || this.isFriendlyTo(mob)) continue;
                if (mob instanceof Monster || mob.getTarget() == owner) {
                    mob.setTarget(this);
                }
            }
        }

        if (this.illusionRunVelocity != null) {
            this.setDeltaMovement(this.illusionRunVelocity.x, this.onGround() ? 0.0D : this.getDeltaMovement().y, this.illusionRunVelocity.z);
            this.hasImpulse = true;
        }

        // Air & Storm Decoys spin in a shimmering cyclone; Nature Decoy emits sweet pollen!
        if (mutated && (mat == EssenceType.AIR || mat == EssenceType.STORM)) {
            this.setYRot(this.getYRot() + 24.0F);
            this.yHeadRot = this.getYRot();
            if (this.ageTicks % 4 == 0) {
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(0.8D), this.getZ(), 3, 0.45D, 0.55D, 0.45D, 0.02D);
            }
        } else if (mutated && mat == EssenceType.NATURE && this.ageTicks % 6 == 0) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(1.1D), this.getZ(), 3, 0.4D, 0.4D, 0.4D, 0.01D);
        }

        // Lightning Volt-Spark Decoy shocks any hostile entity passing through its hitbox!
        if (mutated && mat == EssenceType.LIGHTNING && this.ageTicks % 8 == 0) {
            for (LivingEntity touching : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.85D))) {
                if (touching == this || touching == owner || !touching.isAlive() || this.isFriendlyTo(touching)) continue;
                touching.hurtServer(level, level.damageSources().lightningBolt(), 2.0F + this.spell.getEffectiveTier() * 0.5F);
                touching.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 12, 0));
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() != null && isOwnedBy(source.getEntity())) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (!hurt || !isMutated()) {
            return hurt;
        }

        EssenceType mat = getEffectiveMateria();
        LivingEntity owner = resolveSummoner(level);
        Entity directAttacker = source.getDirectEntity();

        if (getArchetype() == TriadArchetype.DECOY) {
            switch (mat) {
                case WATER -> {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                        SpellLingeringZoneManager.applySoaked(foe, 120);
                    }
                    level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(0.8D), this.getZ(), 18, 0.5D, 0.4D, 0.5D, 0.1D);
                }
                case NETHER, MAGMA -> {
                    if (directAttacker instanceof LivingEntity attacker) {
                        attacker.igniteForSeconds(4);
                    }
                }
                case FROZEN, GLACIAL, TAIGA -> {
                    if (directAttacker instanceof LivingEntity attacker) {
                        attacker.setTicksFrozen(attacker.getTicksFrozen() + 60);
                        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                    }
                }
                case RADIANT -> {
                    // Solar Seraph Decoy converts absorbed damage into healing pulses for caster & allies!
                    float pulseHeal = Math.max(1.0F, amount * 0.50F);
                    for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                        if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, pulseHeal);
                            ally.heal(pulseHeal);
                            level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.02D);
                        }
                    }
                }
                case LIGHTNING -> {
                    if (directAttacker instanceof LivingEntity attacker && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                        attacker.hurtServer(level, level.damageSources().lightningBolt(), 2.5F);
                        attacker.addEffect(new MobEffectInstance(
                                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 15, 0));
                    }
                }
                case AMBER -> {
                    if (directAttacker instanceof LivingEntity attacker) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                    }
                }
                case SYLVAN, OBLIVION, CHIMERA -> {
                    if (directAttacker instanceof LivingEntity attacker) {
                        attacker.hurtServer(level, level.damageSources().thorns(this), Math.max(1.5F, amount * 0.35F));
                    }
                }
                case AETHER, ENTROPIC, CELESTIAL -> {
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.hurtServer(level, level.damageSources().thorns(this), Math.max(1.5F, amount * 0.40F));
                        if (mat == EssenceType.CELESTIAL) {
                            attacker.igniteForSeconds(4);
                        }
                    }
                }
                case EMPYREAN, CATACLYSM, TERMINUS -> {
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.hurtServer(level, level.damageSources().thorns(this), Math.max(2.0F, amount * 0.45F));
                    }
                }
                case ESCHATON, APOTHEOSIS, ENTROPICA -> {
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.hurtServer(level, level.damageSources().thorns(this), Math.max(2.5F, amount * 0.50F));
                    }
                }
                default -> {
                }
            }
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel srv) {
            triggerElementalDeathBurst(srv);
        }
        super.die(damageSource);
    }

    public static double resolveCanonicalDeathBurstRadius(EssenceType mat) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(mat);
    }

    private void triggerElementalDeathBurst(ServerLevel level) {
        if (this.deathBurstTriggered) return;
        this.deathBurstTriggered = true;
        if (!isMutated()) return;

        EssenceType mat = getEffectiveMateria();
        LivingEntity owner = resolveSummoner(level);
        int tier = this.spell.getEffectiveTier();
        double burstRad = resolveCanonicalDeathBurstRadius(mat);
        AABB box = this.getBoundingBox().inflate(burstRad);

        // Beneficial ally death-burst effects
        if (mat == EssenceType.VITAE || mat == EssenceType.GENESIS) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 12.0F);
                    ally.heal(12.0F); // 6 full hearts!
                    level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 8, 0.35D, 0.35D, 0.35D, 0.04D);
                }
            }
        } else if (mat == EssenceType.SPRING) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    float missing = ally.getMaxHealth();
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, missing);
                    ally.heal(missing); // Fully restores friendly ally health + Regeneration III 6.0s!
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 2, true, true));
                    level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.05D);
                }
            }
        } else if (mat == EssenceType.RADIANT) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 4.0F);
                    ally.heal(4.0F);
                }
            }
        } else if (mat == EssenceType.BLOOD) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    ally.setAbsorptionAmount(Math.max(ally.getAbsorptionAmount(), 8.0F)); // +4 Absorption hearts!
                }
            }
        } else if (mat == EssenceType.OASIS) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 5.0F);
                    ally.heal(5.0F);
                }
            }
        } else if (mat == EssenceType.AEGIS) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 4.0D, 0.0D, 0.50D, 0.0D, 120L);
                }
            }
        } else if (mat == EssenceType.WRAITH) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally instanceof SpellEidolonEntity && ally.isAlive() && this.isFriendlyTo(ally)) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 8.0F);
                    ally.heal(8.0F);
                }
            }
        } else if (mat == EssenceType.PYRE && owner != null && owner.isAlive()) {
            ddraig.net.entropica.util.EntityHelper.getPersistentData(owner)
                    .putLong(SpellLingeringZoneManager.TAG_PYRE_MELEE_BOOST_UNTIL, level.getGameTime() + 160L);
        } else if (mat == EssenceType.SOULFIRE && owner != null && owner.isAlive()) {
            float healAmt = owner.getMaxHealth() * 0.15F;
            SpellCombatReactionEngine.notifyMagicalHeal(owner, healAmt);
            owner.heal(healAmt);
        } else if (mat == EssenceType.NULL_U && owner != null && owner.isAlive()) {
            owner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, true, false));
        } else if (mat == EssenceType.CELESTIAL) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    float full = ally.getMaxHealth();
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, full);
                    ally.heal(full);
                }
            }
        } else if (mat == EssenceType.PRISMATIC) {
            // Sacrificial Solar Relic: grants nearby party members 15.0s (300t) of complete damage invulnerability!
            long invulnUntil = level.getGameTime() + 300L;
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                            .putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, invulnUntil);
                    level.sendParticles(ParticleTypes.END_ROD, ally.getX(), ally.getY(1.0D), ally.getZ(), 10, 0.35D, 0.45D, 0.35D, 0.04D);
                }
            }
        } else if (mat == EssenceType.EMPYREAN) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 16.0F); // 8 full hearts!
                    ally.heal(16.0F);
                }
            }
        } else if (mat == EssenceType.ESCHATON) {
            ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(
                    level, owner, this.blockPosition(), 6, EssenceType.ESCHATON, 32L);
        } else if (mat == EssenceType.APOTHEOSIS) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 24.0F); // 12 full hearts!
                    ally.heal(24.0F);
                }
            }
            SpellLingeringZoneManager.spawnTypedEssenceOrb(level, this.position(), EssenceType.RADIANT, 3);
        }

        // Environmental death-burst effects (Magma Caldera Nova crust, Storm fire extinguish + projectile deflect, Dust torch snuff, Soulfire soul sand, Astral EMP, Blood crimson mist)
        if (mat == EssenceType.MAGMA) {
            BlockPos c = this.blockPosition();
            Map<BlockPos, BlockState> savedCrust = new java.util.HashMap<>();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 0, 3))) {
                BlockState bs = level.getBlockState(p);
                if (bs.isSolidRender() && level.getBlockState(p.above()).isAir()
                        && !bs.is(Blocks.MAGMA_BLOCK)
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)
                        && level.random.nextFloat() < 0.45F) {
                    savedCrust.put(p.immutable(), bs);
                    level.setBlockAndUpdate(p, Blocks.MAGMA_BLOCK.defaultBlockState());
                }
            }
            if (!savedCrust.isEmpty()) {
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        owner, this.position(), 3.5D, 120, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                        (lvl, z, el) -> {
                            if (el >= z.maxDurationTicks) {
                                for (Map.Entry<BlockPos, BlockState> e : savedCrust.entrySet()) {
                                    if (lvl.getBlockState(e.getKey()).is(Blocks.MAGMA_BLOCK)) {
                                        lvl.setBlockAndUpdate(e.getKey(), e.getValue());
                                    }
                                }
                                return false;
                            }
                            return true;
                        }
                ));
            }
        } else if (mat == EssenceType.STORM) {
            BlockPos c = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-4, -1, -4), c.offset(4, 2, 4))) {
                if (level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                }
            }
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
                if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.15D));
                proj.hasImpulse = true;
            }
        } else if (mat == EssenceType.DUST) {
            BlockPos c = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-4, -1, -4), c.offset(4, 2, 4))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.getBlock() instanceof TorchBlock || bs.getBlock() instanceof WallTorchBlock) {
                    level.destroyBlock(p, true);
                }
            }
        } else if (mat == EssenceType.SOULFIRE) {
            BlockPos c = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 0, 3))) {
                BlockState bs = level.getBlockState(p);
                if ((bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND))
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                    level.setBlockAndUpdate(p, Blocks.SOUL_SAND.defaultBlockState());
                }
            }
        } else if (mat == EssenceType.ASTRAL) {
            SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, this.blockPosition(), 4, 40);
        } else if (mat == EssenceType.BLOOD) {
            Vec3 mistPos = this.position();
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    owner, mistPos, 5.0D, 80, level.getGameTime(), null, null, EssenceType.BLOOD, 1.0F,
                    (lvl, z, el) -> {
                        if (el % 10 == 0) {
                            lvl.sendParticles(ParticleTypes.CRIMSON_SPORE, z.center.x, z.center.y + 0.9D, z.center.z,
                                    22, 2.2D, 0.8D, 2.2D, 0.03D);
                            LivingEntity sum = z.resolveCaster(lvl);
                            for (LivingEntity f : lvl.getEntitiesOfClass(LivingEntity.class,
                                    new AABB(z.center.x - 5.0D, z.center.y - 1.5D, z.center.z - 5.0D,
                                            z.center.x + 5.0D, z.center.y + 2.5D, z.center.z + 5.0D))) {
                                if (!f.isAlive() || f == sum || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(f, sum, true)) continue;
                                f.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                            }
                        }
                        return el < z.maxDurationTicks;
                    }
            ));
        }

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
            switch (mat) {
                case AIR -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.35D);
                    foe.setDeltaMovement(away.x, 0.65D, away.z);
                    foe.hasImpulse = true;
                }
                case WATER -> {
                    SpellLingeringZoneManager.applySoaked(foe, 180);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                }
                case NATURE -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
                }
                case EARTH -> {
                    foe.hurtServer(level, level.damageSources().magic(), 4.0F + tier * 1.2F);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
                }
                case FROZEN, GLACIAL -> {
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 60);
                }
                case NETHER -> {
                    foe.igniteForSeconds(5);
                    foe.hurtServer(level, level.damageSources().onFire(), 4.5F + tier * 1.3F);
                }
                case RADIANT -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                }
                case UMBRAL -> foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                case UNDEAD -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.25D);
                    foe.setDeltaMovement(away.x, 0.42D, away.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                    foe.hurtServer(level, level.damageSources().magic(), 4.0F + tier * 1.2F);
                }
                case VOID -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.25D);
                    foe.setDeltaMovement(inward.x, 0.35D, inward.z);
                    foe.hasImpulse = true;
                    foe.hurtServer(level, level.damageSources().magic(), 4.0F + tier * 1.2F);
                }
                case ARID -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                }
                case LIGHTNING -> {
                    foe.hurtServer(level, level.damageSources().lightningBolt(), 3.5F + tier);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                }
                case MAGMA -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.2D);
                    foe.setDeltaMovement(away.x, 0.45D, away.z);
                    foe.hasImpulse = true;
                    foe.igniteForSeconds(6);
                    foe.hurtServer(level, level.damageSources().onFire(), 5.0F + tier * 1.35F);
                }
                case STORM -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.65D);
                    foe.setDeltaMovement(away.x, 0.55D, away.z);
                    foe.hasImpulse = true;
                    SpellLingeringZoneManager.applySoaked(foe, 200);
                }
                case OVERGROWTH -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                }
                case DUST -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    if (foe instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                }
                case SPORE -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                    SpellLingeringZoneManager.applyPacified(foe, 80);
                }
                case TAIGA -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 2));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 40);
                }
                case OASIS -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2));
                    SpellLingeringZoneManager.applySoaked(foe, 140);
                    SpellLingeringZoneManager.applyPacified(foe, 80);
                }
                case BLOOD -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 80, 0));
                }
                case AURA -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.15D);
                    foe.setDeltaMovement(inward.x, 0.30D, inward.z);
                    foe.hasImpulse = true;
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                }
                case AMBER -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                }
                case ECLIPSE -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.15D);
                    foe.setDeltaMovement(inward.x, 0.30D, inward.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.hurtServer(level, level.damageSources().magic(), 4.5F + tier * 1.25F);
                }
                case NULL_R -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.15D);
                    foe.setDeltaMovement(inward.x, 0.30D, inward.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    for (var eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                        if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                    }
                    if (foe instanceof Mob mob) mob.setTarget(null);
                    foe.hurtServer(level, level.damageSources().magic(), 4.5F + tier * 1.25F);
                }
                case NULL_U -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.15D);
                    foe.setDeltaMovement(inward.x, 0.30D, inward.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 80);
                    if (foe instanceof Mob mob) mob.setTarget(null);
                    foe.hurtServer(level, level.damageSources().magic(), 4.5F + tier * 1.25F);
                }
                case BLIGHT -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                    BlockPos fp = foe.blockPosition();
                    if (level.getBlockState(fp).isAir() && level.getBlockState(fp.below()).isSolidRender()) {
                        level.setBlockAndUpdate(fp, Blocks.WITHER_ROSE.defaultBlockState());
                    }
                }
                case ASTRAL -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.20D);
                    foe.setDeltaMovement(inward.x, 0.45D, inward.z);
                    foe.hasImpulse = true;
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
                    foe.hurtServer(level, level.damageSources().magic(), 4.5F + tier * 1.2F);
                }
                case SOULFIRE -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.35D);
                    foe.setDeltaMovement(away.x, 0.45D, away.z);
                    foe.hasImpulse = true;
                    foe.igniteForSeconds(6);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 120L);
                    foe.hurtServer(level, level.damageSources().magic(), 5.0F + tier * 1.3F);
                }
                case VAPOR -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
                    SpellLingeringZoneManager.applySoaked(foe, 180);
                }
                case DAWN -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.6D);
                    foe.setDeltaMovement(away.x, 0.55D, away.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    float dawnDmg = (5.0F + tier * 1.25F) * (foe.getType().is(EntityTypeTags.UNDEAD) ? 2.0F : 1.0F);
                    foe.hurtServer(level, level.damageSources().magic(), dawnDmg);
                }
                case ABYSS -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 80);
                    SpellLingeringZoneManager.applySoaked(foe, 180);
                }
                case AEGIS -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.55D);
                    foe.setDeltaMovement(away.x, 0.45D, away.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    float aegisDmg = (5.0F + tier * 1.25F) * (foe.getType().is(EntityTypeTags.UNDEAD) ? 1.5F : 1.0F);
                    foe.hurtServer(level, level.damageSources().magic(), aegisDmg);
                }
                case AURORA -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.55D);
                    foe.setDeltaMovement(away.x, 0.48D, away.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 60);
                    float auroraDmg = (5.0F + tier * 1.25F) * (foe.getType().is(EntityTypeTags.UNDEAD) ? 1.5F : 1.0F);
                    foe.hurtServer(level, level.damageSources().magic(), auroraDmg);
                }
                case MIRAGE -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellLingeringZoneManager.applyMirageTraitorFrenzy(foe, 100);
                }
                case WRAITH -> {
                    SpellLingeringZoneManager.applyIntangiblePhase(foe, 60, false);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 100L);
                    foe.hurtServer(level, level.damageSources().magic(), 4.5F + tier * 1.2F);
                }
                case BARROW -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    foe.hurtServer(level, level.damageSources().magic(), 5.0F + tier * 1.25F);
                }
                case PYRE -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.55D);
                    foe.setDeltaMovement(away.x, 0.45D, away.z);
                    foe.hasImpulse = true;
                    foe.igniteForSeconds(6);
                    foe.hurtServer(level, level.damageSources().onFire(), 5.5F + tier * 1.35F);
                }
                case PENUMBRA -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.45D);
                    foe.setDeltaMovement(inward.x, 0.35D, inward.z);
                    foe.hasImpulse = true;
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 160); // 8.0s silence!
                    foe.hurtServer(level, level.damageSources().magic(), 6.0F + tier * 1.5F);
                }
                case RIME -> {
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 70, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 6));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 70);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.hurtServer(level, level.damageSources().magic(), 6.0F + tier * 1.5F);
                }
                case SPRING -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.75D);
                    foe.setDeltaMovement(away.x, 0.48D, away.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.hurtServer(level, level.damageSources().magic(), 6.0F + tier * 1.5F);
                }
                case STATIC -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.75D);
                    foe.setDeltaMovement(away.x, 0.48D, away.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
                    foe.hurtServer(level, level.damageSources().lightningBolt(), 6.0F + tier * 1.5F);
                }
                case SYLVAN -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                    foe.hurtServer(level, level.damageSources().magic(), 6.0F + tier * 1.5F);
                }
                case MIASMA -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                    foe.hurtServer(level, level.damageSources().magic(), 5.5F + tier * 1.4F);
                }
                case GENESIS -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    float dmg = (6.0F + tier * 1.5F) * (foe.getType().is(EntityTypeTags.UNDEAD) ? 1.5F : 1.0F);
                    foe.hurtServer(level, level.damageSources().magic(), dmg);
                }
                case OBLIVION -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    foe.igniteForSeconds(5);
                    foe.hurtServer(level, level.damageSources().magic(), 6.5F + tier * 1.55F);
                }
                case CHIMERA -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.igniteForSeconds(5);
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 80);
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    foe.hurtServer(level, level.damageSources().magic(), 6.5F + tier * 1.55F);
                }
                case AETHER -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 160L);
                    foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0)); // 5.0s mid-air suspension!
                    foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 8.0F + tier * 1.8F);
                }
                case ENTROPIC -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 160L);
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2)); // Wither III 5.0s!
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 8.0F + tier * 1.8F);
                }
                case CELESTIAL -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 160L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0)); // 8.0s Blindness!
                    float smite = foe.getType().is(EntityTypeTags.UNDEAD) ? 2.0F : 1.0F;
                    foe.hurtServer(level, level.damageSources().magic(), (8.0F + tier * 1.8F) * smite);
                }
                case PRISMATIC -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    foe.hurtServer(level, level.damageSources().magic(), 7.0F + tier * 1.6F);
                }
                case EMPYREAN -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 100, 0));
                    foe.hurtServer(level, level.damageSources().magic(), 10.0F + tier * 2.1F);
                }
                case CATACLYSM -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.55D);
                    foe.setDeltaMovement(inward.x, 0.35D, inward.z);
                    foe.hasImpulse = true;
                    foe.igniteForSeconds(6);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 120L);
                    foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 10.0F + tier * 2.1F);
                }
                case TERMINUS -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 100);
                    foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 10.0F + tier * 2.1F);
                }
                case ESCHATON -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 200L);
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.85D);
                    foe.setDeltaMovement(inward.x, 0.38D, inward.z);
                    foe.hasImpulse = true;
                    foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 14.0F + tier * 2.5F);
                }
                case APOTHEOSIS -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 200L);
                    float smite = foe.getType().is(EntityTypeTags.UNDEAD) ? 2.0F : 1.0F;
                    foe.hurtServer(level, level.damageSources().magic(), (14.0F + tier * 2.5F) * smite);
                }
                case ENTROPICA -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 200L);
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.85D);
                    foe.setDeltaMovement(inward.x, 0.35D, inward.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                    foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 15.0F + tier * 2.6F);
                }
                default -> {
                }
            }
        }

        if (mat == EssenceType.EARTH) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    this.getX(), this.getY(0.8D), this.getZ(), 28, 0.8D, 0.5D, 0.8D, 0.1D);
        } else if (mat == EssenceType.UNDEAD || mat == EssenceType.BARROW) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                    this.getX(), this.getY(0.8D), this.getZ(), 30, 0.9D, 0.5D, 0.9D, 0.1D);
        } else if (mat == EssenceType.NETHER || mat == EssenceType.PYRE) {
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.8D), this.getZ(), 32, 0.9D, 0.5D, 0.9D, 0.12D);
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.8D), this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
        } else if (mat == EssenceType.NATURE || mat == EssenceType.SPORE) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(0.9D), this.getZ(), 28, 0.9D, 0.6D, 0.9D, 0.05D);
        } else if (mat == EssenceType.RADIANT || mat == EssenceType.DAWN || mat == EssenceType.AURORA) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.9D), this.getZ(), 28, 0.9D, 0.6D, 0.9D, 0.08D);
        } else if (mat == EssenceType.ARID || mat == EssenceType.DUST) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                    this.getX(), this.getY(0.8D), this.getZ(), 32, 1.0D, 0.6D, 1.0D, 0.1D);
        } else if (mat == EssenceType.MAGMA) {
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.8D), this.getZ(), 26, 0.8D, 0.5D, 0.8D, 0.1D);
        } else if (mat == EssenceType.AMBER) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                    this.getX(), this.getY(0.8D), this.getZ(), 28, 0.8D, 0.5D, 0.8D, 0.1D);
        } else {
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(0.8D), this.getZ(), 24, 0.7D, 0.5D, 0.7D, 0.08D);
        }
    }

    @Nullable
    private LivingEntity findNearestHostile(ServerLevel level, double radius) {
        LivingEntity owner = resolveSummoner(level);
        List<LivingEntity> list = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e.isAlive()
                        && e != this
                        && e != owner
                        && !this.isFriendlyTo(e)
                        && (e instanceof Monster
                        || (owner != null && (e == owner.getLastHurtMob() || e == owner.getLastHurtByMob())))
        );
        if (list.isEmpty()) return null;
        list.sort(Comparator.comparingDouble(this::distanceToSqr));
        // Radiant Solar Seraph prioritizes Undead targets; Undead Necrotic Obelisk prioritizes living targets!
        if (isMutated() && getEffectiveMateria() == EssenceType.RADIANT) {
            for (LivingEntity e : list) {
                if (e.getType().is(EntityTypeTags.UNDEAD)) return e;
            }
        } else if (isMutated() && getEffectiveMateria() == EssenceType.UNDEAD) {
            for (LivingEntity e : list) {
                if (!e.getType().is(EntityTypeTags.UNDEAD)) return e;
            }
        }
        return list.get(0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.summonerUuid != null) {
            output.store("SummonerUUID", Codec.STRING, this.summonerUuid.toString());
        }
        output.store("SpellAssembly", SpellAssembly.CODEC, this.spell);
        output.store("SlotIndex", Codec.INT, this.slotIndex);
        if (this.fallbackMateria != null) {
            output.store("FallbackMateria", EssenceType.CODEC, this.fallbackMateria);
        }
        output.store("ApparatusPotency", Codec.FLOAT, this.apparatusPotency);
        output.store("MaxLifetimeTicks", Codec.INT, this.maxLifetimeTicks);
        output.store("AgeTicks", Codec.INT, this.ageTicks);
        output.store("AnchorX", Codec.DOUBLE, this.anchorPos.x);
        output.store("AnchorY", Codec.DOUBLE, this.anchorPos.y);
        output.store("AnchorZ", Codec.DOUBLE, this.anchorPos.z);
        output.store("Materia", Codec.STRING, this.entityData.get(DATA_MATERIA));
        output.store("TintRGB", Codec.INT, this.entityData.get(DATA_TINT_RGB));
        output.store("Archetype", Codec.STRING, this.entityData.get(DATA_ARCHETYPE));
        output.store("Mutated", Codec.BOOL, this.entityData.get(DATA_MUTATED));
        output.store("DisplayItem", ItemStack.OPTIONAL_CODEC, this.entityData.get(DATA_DISPLAY_ITEM));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("SummonerUUID", Codec.STRING).ifPresent(s -> {
            try {
                this.summonerUuid = UUID.fromString(s);
            } catch (IllegalArgumentException ignored) {
            }
        });
        input.read("SpellAssembly", SpellAssembly.CODEC).ifPresent(s -> this.spell = s);
        this.slotIndex = input.read("SlotIndex", Codec.INT).orElse(0);
        this.fallbackMateria = input.read("FallbackMateria", EssenceType.CODEC).orElse(EssenceType.REGULAR);
        this.apparatusPotency = input.read("ApparatusPotency", Codec.FLOAT).orElse(1.0F);
        this.maxLifetimeTicks = input.read("MaxLifetimeTicks", Codec.INT).orElse(600);
        this.ageTicks = input.read("AgeTicks", Codec.INT).orElse(0);
        double ax = input.read("AnchorX", Codec.DOUBLE).orElse(this.getX());
        double ay = input.read("AnchorY", Codec.DOUBLE).orElse(this.getY());
        double az = input.read("AnchorZ", Codec.DOUBLE).orElse(this.getZ());
        this.anchorPos = new Vec3(ax, ay, az);
        this.entityData.set(DATA_MATERIA, input.read("Materia", Codec.STRING).orElse(EssenceType.REGULAR.name()));
        this.entityData.set(DATA_TINT_RGB, input.read("TintRGB", Codec.INT).orElse(0x88CCFF));
        this.entityData.set(DATA_ARCHETYPE, input.read("Archetype", Codec.STRING).orElse(TriadArchetype.AGENT.name()));
        this.entityData.set(DATA_MUTATED, input.read("Mutated", Codec.BOOL).orElse(false));
        this.entityData.set(DATA_DISPLAY_ITEM, input.read("DisplayItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }
}
