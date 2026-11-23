package ddraig.net.entropica.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModParticles;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CampfireBlock;
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
    public static final int UPKEEP_INTERVAL_TICKS = ddraig.net.entropica.spell.engine.SpellEidolonSpecs.UPKEEP_INTERVAL_TICKS;

    private int maxLifetimeTicks = 600;
    private int ageTicks = 0;
    private int upkeepTicks = 0;
    private boolean sustainedPastCycleOne = false;
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
                if (formMateria == EssenceType.EARTH && archetype == TriadArchetype.DECOY) {
                    // Section 6.5 Granite Effigy Decoy: stationary carved granite decoy with immense health!
                    baseHp *= 2.50D;
                } else {
                    baseHp *= (archetype == TriadArchetype.DECOY ? 1.75D : 1.25D);
                }
            }
            armor += (formMateria == EssenceType.AEGIS ? 12.0D : 8.0D);
            kbRes = Math.max(kbRes, (formMateria == EssenceType.EARTH && archetype == TriadArchetype.DECOY) ? 1.0D : 0.85D);
            if (formMateria == EssenceType.EARTH) {
                AttributeInstance toughAttr = this.getAttribute(Attributes.ARMOR_TOUGHNESS);
                if (toughAttr != null) {
                    toughAttr.setBaseValue(archetype == TriadArchetype.DECOY ? 4.0D : 2.0D);
                }
            }
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

        // Section 6.9 Shadow Stalker Suite & Section 6.32 Void-Shadow Stalker execute completely silent strikes and footsteps!
        this.setSilent(mutated && (formMateria == EssenceType.UMBRAL || formMateria == EssenceType.NULL_U));

        if (mutated && (formMateria == EssenceType.UMBRAL || formMateria == EssenceType.MIRAGE
                || formMateria == EssenceType.DUST)
                && archetype == TriadArchetype.DECOY && caster != null) {
            // Umbral Phantom Decoy, Mirage Phantom Mirror Effigy & Dust Silhouette visually copy caster's held weapons & armor!
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

    /**
     * Guide Section 5.6 & 6.11 <i>Spacetime Rift Beacon</i>:
     * Pulls a hostile, un-owned <b>Void-Stalker</b> construct through a spatial micro-tear around an afflicted player!
     */
    public static SpellEidolonEntity spawnWildHostileVoidStalker(
            ServerLevel level,
            Vec3 pos,
            LivingEntity targetPlayer,
            int potencyTier
    ) {
        SpellEidolonEntity stalker = new SpellEidolonEntity(ModEntityTypes.SPELL_EIDOLON.get(), level);
        stalker.setPos(pos.x, pos.y, pos.z);
        stalker.summonerUuid = null;
        stalker.anchorPos = pos;
        stalker.spell = SpellAssembly.createMagicMissileStarter(Math.clamp(potencyTier, 1, 10));
        stalker.maxLifetimeTicks = 600;
        stalker.entityData.set(DATA_MATERIA, EssenceType.VOID.name());
        stalker.entityData.set(DATA_TINT_RGB, 0x41304E);
        stalker.entityData.set(DATA_ARCHETYPE, TriadArchetype.AGENT.name());
        stalker.entityData.set(DATA_MUTATED, true);

        double hp = 14.0D + potencyTier * 3.0D;
        AttributeInstance maxHpAttr = stalker.getAttribute(Attributes.MAX_HEALTH);
        if (maxHpAttr != null) maxHpAttr.setBaseValue(hp);
        AttributeInstance spd = stalker.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) spd.setBaseValue(0.38D);
        stalker.setHealth((float) hp);
        stalker.setCustomName(Component.literal("§5Void-Stalker"));
        stalker.setTarget(targetPlayer);
        level.addFreshEntity(stalker);
        return stalker;
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

        if (caster != null && (materia == EssenceType.MIRAGE || materia == EssenceType.AURORA
                || materia == EssenceType.UMBRAL || materia == EssenceType.FROZEN
                || materia == EssenceType.GLACIAL || materia == EssenceType.ARID
                || materia == EssenceType.DUST)) {
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

    public static int resolveMaxConcurrentSummons(int tier) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveMaxConcurrentSummons(tier);
    }

    /**
     * Lifecycle Refresh Engine (Sections 6.40–6.59): Re-casting Summon when at cap refreshes an existing
     * matching Agent or Sentry's lifecycle timer and restores 50% of its maximum health instead of evicting it!
     */
    public static boolean tryRefreshLifecycleConstruct(
            ServerLevel level,
            LivingEntity caster,
            EssenceType materia,
            TriadArchetype archetype,
            int tier,
            Vec3 spawnPos
    ) {
        int cap = resolveMaxConcurrentSummons(tier);
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
                candidate.upkeepTicks = 0;
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
            case MAGMA -> 0xB83C08;
            case STORM -> 0x429BA3;
            case GLACIAL -> 0x98DCF2;
            case OVERGROWTH -> 0x1A5923;
            case DUST -> 0xC2B28F;
            case SPORE -> 0x1A5923;
            case TAIGA -> 0x1A5923;
            case OASIS -> 0x1A5923;
            case VITAE -> 0xFF6B9D;
            case BLOOD -> 0x8A0303;
            case AURA -> 0xFF6B9D; // Dual-tone Living Pink (#FF6B9D) + Buff Air Gold (#A88654)
            case AMBER -> 0xD98218;
            case ECLIPSE -> 0x211A21; // Dual-tone Abyssal Void-Black (#211A21) + Celestial Gold (#FFEA78)
            case BLIGHT -> 0x4D6453; // Dual-tone Tombstone Green-Grey (#4D6453) + Necrotic Green (#34CC48)
            case ASTRAL -> 0x41304E;
            case SOULFIRE -> 0x1DF0E5;
            case VAPOR -> 0xE0F7FA;
            case NULL_R -> 0xFFFFFF; // Dual-tone Brilliant Antimatter White (#FFFFFF) + Cosmic Violet (#41304E)
            case NULL_U -> 0x110B17; // Dual-tone Abyssal Pitch Black (#110B17) + Cosmic Violet (#41304E)
            case DAWN -> 0xFFEA78; // Dual-tone Blazing Sunrise Gold (#FFEA78) + Buff Air Gold (#A88654)
            case ABYSS -> 0x211A21; // Dual-tone Shadow Ink Black (#211A21) + Deep Trench Navy (#225C7C)
            case AEGIS -> 0xFFEA78; // Dual-tone Holy Gold (#FFEA78) + Granite Brown (#773214)
            case AURORA -> 0xFFEA78; // Dual-tone Radiant Solar Gold (#FFEA78) + Crystalline Cryo Cyan (#98DCF2)
            case MIRAGE -> 0xFFEA78; // Dual-tone Sunlit Mirage Gold (#FFEA78) + Bleached Desert Sandstone (#C2B28F)
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
     * Enforces active summon cap per caster (Sections 1.8 & 4.9, scaling up to 8 at Tiers 8–10)
     * so older constructs cleanly dissipate when exceeded.
     */
    public static void enforceSummonCap(ServerLevel level, LivingEntity caster, int tier) {
        int cap = resolveMaxConcurrentSummons(tier);
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
        Vec3 sample = Vec3.atCenterOf(pos);
        AABB box = new AABB(pos).inflate(16.0D);
        return !level.getEntitiesOfClass(
                SpellEidolonEntity.class,
                box,
                e -> e.isAlive()
                        && e.isMutated()
                        && e.getEffectiveMateria() == EssenceType.UMBRAL
                        && e.getArchetype() == TriadArchetype.SENTRY
                        && e.position().distanceToSqr(sample) <= 256.0D
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

    private boolean isHostileTargetForSentryField(@Nullable LivingEntity owner, LivingEntity candidate) {
        if (candidate == this || candidate == owner || !candidate.isAlive() || this.isFriendlyTo(candidate)) {
            return false;
        }
        boolean wardSafeguard = this.spell != null && this.spell.hasWardFriendlyFireSafeguard();
        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(candidate, owner, wardSafeguard)) {
            return false;
        }
        if (candidate instanceof Enemy || candidate instanceof Monster) {
            return true;
        }
        if (candidate instanceof Mob mob && (mob.getTarget() == this || (owner != null && mob.getTarget() == owner))) {
            return true;
        }
        if (candidate instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            if (!wardSafeguard) {
                return true;
            }
            return owner != null && (player == owner.getLastHurtMob() || player == owner.getLastHurtByMob());
        }
        return owner != null && (candidate == owner.getLastHurtMob() || candidate == owner.getLastHurtByMob());
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
                EssenceType reqType = resolveUpkeepMateria();
                long healCostMfum = Math.max(16L, (20L * MateriaStageTable.getMfumPerNativeUnit(Math.max(1, tier - 1))) / 8L);
                SpellFuelService.FuelCheckResult res = SpellFuelService.consumeOrbisCellUpkeepFuel(
                        player,
                        tier,
                        reqType,
                        healCostMfum,
                        false
                );
                if (!res.success()) {
                    res = SpellFuelService.consumePlayerSpellFuel(
                            player,
                            tier,
                            Map.of(reqType, healCostMfum),
                            false
                    );
                }
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

        EssenceType currentMat = getEffectiveMateria();
        if (isMutated() && currentMat != EssenceType.REGULAR && currentMat.isDynamic() && this.ageTicks % 6 == 0) {
            int nextRgb;
            if (currentMat == EssenceType.ENTROPICA) {
                int[] entropicaCycle = new int[]{0xE0E5EC, 0xFAD02C, 0x1A153B};
                nextRgb = entropicaCycle[(this.ageTicks / 6) % entropicaCycle.length];
            } else if (currentMat == EssenceType.PRISMATIC) {
                int[] prismaticCycle = new int[]{0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0xAA55FF, 0xFFFFFF};
                nextRgb = prismaticCycle[(this.ageTicks / 6) % prismaticCycle.length];
            } else if (currentMat == EssenceType.CHIMERA) {
                int[] chimeraCycle = new int[]{0xA88654, 0x773214, 0x225C7C, 0xB83C08, 0xFF55FF, 0x55FFFF};
                nextRgb = chimeraCycle[(this.ageTicks / 6) % chimeraCycle.length];
            } else {
                int[] rgb = currentMat.getCurrentRGB(serverLevel.getGameTime());
                nextRgb = ((rgb[0] & 0xFF) << 16) | ((rgb[1] & 0xFF) << 8) | (rgb[2] & 0xFF);
            }
            this.entityData.set(DATA_TINT_RGB, nextRgb);
        }

        // Section 4.9 Continuous 15-Second (300-Tick) Materia Fuel Upkeep:
        // Cycle 1 is paid by the initial summon cast cost. Every 300 ticks thereafter, autonomous Agents & Sentries
        // owned by a Player drain 25% base cost (if HP >= 50%) or 100% full base cost + full heal (if HP < 50%)
        // from the player's equipped Orbis Cell first, then inventory Orbis Cells (Creative players bypass drain).
        if (!this.shortLivedIllusion && getArchetype() != TriadArchetype.DECOY) {
            this.upkeepTicks++;
            if (this.upkeepTicks >= UPKEEP_INTERVAL_TICKS) {
                this.upkeepTicks = 0;
                if (!tryPerformPlayerUpkeepRefresh(serverLevel) && this.sustainedPastCycleOne) {
                    expireAndDiscard(serverLevel);
                    return;
                }
            }
        }

        if (this.ageTicks >= this.maxLifetimeTicks) {
            if (!this.shortLivedIllusion
                    && getArchetype() != TriadArchetype.DECOY
                    && tryPerformPlayerUpkeepRefresh(serverLevel)) {
                this.upkeepTicks = 0;
                return;
            }
            expireAndDiscard(serverLevel);
            return;
        }

        if (isMutated() && getEffectiveMateria() == EssenceType.MAGMA && this.ageTicks % 6 == 0) {
            serverLevel.sendParticles(ParticleTypes.DRIPPING_LAVA,
                    this.getX(), this.getY(0.65D), this.getZ(), 2, 0.28D, 0.35D, 0.28D, 0.01D);
        } else if (isMutated() && getEffectiveMateria() == EssenceType.STORM && this.ageTicks % 6 == 0) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY(0.75D), this.getZ(), 3, 0.32D, 0.40D, 0.32D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY(0.65D), this.getZ(), 1, 0.28D, 0.30D, 0.28D, 0.02D);
        } else if (isMutated() && getEffectiveMateria() == EssenceType.GLACIAL && this.ageTicks % 6 == 0) {
            serverLevel.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x98DCF2, 18, 0.85F, 0.06F),
                    this.getX(), this.getY(0.70D), this.getZ(), 2, 0.30D, 0.38D, 0.30D, 0.015D
            );
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(0.75D), this.getZ(), 2, 0.28D, 0.35D, 0.28D, 0.015D);
        } else if (isMutated() && getEffectiveMateria() == EssenceType.OVERGROWTH && this.ageTicks % 6 == 0) {
            serverLevel.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 24, 0.95F, 0.07F),
                    this.getX(), this.getY(0.85D), this.getZ(), 2, 0.32D, 0.42D, 0.32D, 0.02D
            );
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSS_BLOCK.defaultBlockState()),
                    this.getX(), this.getY(0.15D), this.getZ(), 2, 0.25D, 0.10D, 0.25D, 0.04D);
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
        if (mutated && mat == EssenceType.LIGHTNING && this.ageTicks % 6 == 0
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.003D) {
            Vec3 trailPos = this.position();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, trailPos.x, trailPos.y + 0.25D, trailPos.z, 6, 0.22D, 0.14D, 0.22D, 0.08D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), 0xFFE600, 22, 0.80F, 0.10F),
                    trailPos.x, trailPos.y + 0.18D, trailPos.z, 2, 0.20D, 0.08D, 0.20D, 0.01D);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    owner, trailPos, 1.35D, 55, level.getGameTime(), null, null, EssenceType.LIGHTNING, 1.0F,
                    (lvl, z, el) -> {
                        if (el % 10 == 0) {
                            lvl.sendParticles(ParticleTypes.ELECTRIC_SPARK, z.center.x, z.center.y + 0.18D, z.center.z,
                                    5, 0.35D, 0.10D, 0.35D, 0.05D);
                            LivingEntity sum = z.resolveCaster(lvl);
                            for (LivingEntity foe : lvl.getEntitiesOfClass(LivingEntity.class,
                                    new AABB(z.center.x - 1.35D, z.center.y - 0.5D, z.center.z - 1.35D,
                                            z.center.x + 1.35D, z.center.y + 1.6D, z.center.z + 1.35D))) {
                                if (!foe.isAlive() || foe == sum || foe instanceof SpellEidolonEntity
                                        || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sum, true)) {
                                    continue;
                                }
                                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(
                                        lvl, foe, sum, 1.8F + tier * 0.50F, false, false
                                );
                                foe.addEffect(new MobEffectInstance(
                                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 8, 0));
                            }
                        }
                        return el < z.maxDurationTicks;
                    }
            ));
        }

        // Magma-Brute Agent (#13) leaves burning footprints & temporary magma crust behind while striding (cleanly restoring after 80t)!
        if (mutated && mat == EssenceType.MAGMA && this.ageTicks % 10 == 0
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.003D) {
            BlockPos foot = this.blockPosition();
            BlockPos ground = foot.below();
            BlockState orig = level.getBlockState(ground);
            boolean placedFire = false;
            if (level.getBlockState(foot).isAir() && orig.isSolidRender() && level.random.nextFloat() < 0.45F) {
                level.setBlockAndUpdate(foot, Blocks.FIRE.defaultBlockState());
                placedFire = true;
            }
            boolean placedMagma = false;
            if (orig.isSolidRender() && !orig.is(Blocks.MAGMA_BLOCK)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, ground, orig)
                    && level.random.nextFloat() < 0.55F) {
                level.setBlockAndUpdate(ground, Blocks.MAGMA_BLOCK.defaultBlockState());
                placedMagma = true;
            }
            if (placedFire || placedMagma) {
                BlockPos savedFoot = foot.immutable();
                BlockPos savedGround = ground.immutable();
                boolean restoreFire = placedFire;
                boolean restoreMagma = placedMagma;
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        owner, Vec3.atCenterOf(savedGround), 1.2D, 80, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                        (lvl, z, el) -> {
                            if (el >= z.maxDurationTicks) {
                                if (restoreFire && lvl.getBlockState(savedFoot).is(Blocks.FIRE)) {
                                    lvl.removeBlock(savedFoot, false);
                                }
                                if (restoreMagma && lvl.getBlockState(savedGround).is(Blocks.MAGMA_BLOCK)) {
                                    lvl.setBlockAndUpdate(savedGround, orig);
                                }
                                return false;
                            }
                            if (el % 12 == 0) {
                                LivingEntity sum = z.resolveCaster(lvl);
                                for (LivingEntity foe : lvl.getEntitiesOfClass(LivingEntity.class, new AABB(savedFoot).inflate(0.65D))) {
                                    if (!foe.isAlive() || foe == sum || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sum, true)) continue;
                                    foe.igniteForSeconds(3);
                                }
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

        // Vitae Vitality Knight pulses passive Regeneration to adjacent allies & emits pink heart / cellular pulse particles!
        if (mutated && mat == EssenceType.VITAE) {
            if (this.ageTicks % 6 == 0) {
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), 0xFF6B9D, 20, 0.88F, 0.05F),
                        this.getX(), this.getY(0.75D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.02D);
                if (this.ageTicks % 12 == 0) {
                    level.sendParticles(ParticleTypes.HEART,
                            this.getX(), this.getY(1.05D), this.getZ(), 1, 0.25D, 0.25D, 0.25D, 0.01D);
                }
            }
            if (this.ageTicks % 30 == 0) {
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 55, 0, true, true));
                    }
                }
            }
        }

        if (mutated && mat == EssenceType.OASIS && this.ageTicks % 6 == 0) {
            // Section 6.21 Oasis-Warden Agent: sand dust and water droplet particle trails!
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                    this.getX(), this.getY(0.45D), this.getZ(), 2, 0.25D, 0.25D, 0.25D, 0.01D);
            level.sendParticles(ParticleTypes.DRIPPING_WATER,
                    this.getX(), this.getY(0.85D), this.getZ(), 2, 0.28D, 0.30D, 0.28D, 0.0D);
        }

        if (mutated && mat == EssenceType.BLOOD && this.ageTicks % 6 == 0) {
            // Section 6.23 Blood-Thrall Knight Agent: dark red droplets & crimson vapor particle trails (#8A0303)!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 20, 0.90F, 0.05F),
                    this.getX(), this.getY(0.75D), this.getZ(), 2, 0.26D, 0.30D, 0.26D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x8A0303, 24, 0.85F, 0.06F),
                    this.getX(), this.getY(0.55D), this.getZ(), 2, 0.28D, 0.28D, 0.28D, 0.01D);
        }

        if (mutated && mat == EssenceType.AURA) {
            // Section 6.24 Zephyr-Monk Agent (#FF6B9D / #A88654): glider swiftness + golden-pink spirit mist & floating pranic motes!
            if (this.ageTicks % 20 == 0) {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 35, 1, true, false));
                this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0, true, false));
            }
            if (this.ageTicks % 6 == 0) {
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0xFF6B9D, 22, 0.90F, 0.05F),
                        this.getX(), this.getY(0.75D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0xA88654, 24, 0.88F, 0.04F),
                        this.getX(), this.getY(0.95D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.015D);
            }
        }

        if (mutated && mat == EssenceType.AMBER && this.ageTicks % 6 == 0) {
            // Section 6.25 Amber-Clad Sentinel Agent (#FF6B9D / #773214): golden resin droplets & translucent amber crystals!
            level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                    this.getX(), this.getY(0.75D), this.getZ(), 1, 0.28D, 0.30D, 0.28D, 0.0D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), 0xFF6B9D, 22, 0.88F, 0.04F),
                    this.getX(), this.getY(0.95D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.015D);
        } else if (mutated && mat == EssenceType.ECLIPSE && this.ageTicks % 6 == 0) {
            // Section 6.26 Eclipse-Knight Agent (#FFEA78 / #211A21): black solar flares & golden coronal particles!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x211A21, 22, 0.90F, 0.05F),
                    this.getX(), this.getY(0.85D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), 0xFFEA78, 22, 0.88F, 0.04F),
                    this.getX(), this.getY(1.05D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.015D);
        } else if (mutated && mat == EssenceType.BLIGHT && this.ageTicks % 6 == 0) {
            // Section 6.27 Blight-Revenant Agent (#4D6453 / #34CC48): necrotic spore & bone-thorn particles!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x34CC48, 22, 0.90F, 0.05F),
                    this.getX(), this.getY(0.85D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), 0x4D6453, 22, 0.88F, 0.04F),
                    this.getX(), this.getY(1.00D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
        } else if (mutated && mat == EssenceType.NULL_R && this.ageTicks % 6 == 0) {
            // Section 6.31 White-Hole Paladin Agent (#FFFFFF / #41304E): incandescent antimatter flare & cosmic violet singularity motes!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), 0xFFFFFF, 22, 0.90F, 0.06F),
                    this.getX(), this.getY(0.95D), this.getZ(), 2, 0.28D, 0.34D, 0.28D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), 0x41304E, 22, 0.86F, 0.14F),
                    this.getX(), this.getY(0.80D), this.getZ(), 2, 0.28D, 0.30D, 0.28D, 0.01D);
        } else if (mutated && mat == EssenceType.NULL_U && this.ageTicks % 6 == 0) {
            // Section 6.32 Void-Shadow Stalker Agent (#110B17 / #41304E): shadow smoke & dark singularity particles!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x110B17, 22, 0.92F, 0.04F),
                    this.getX(), this.getY(0.85D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), 0x41304E, 22, 0.86F, 0.14F),
                    this.getX(), this.getY(0.98D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
        } else if (mutated && mat == EssenceType.DAWN && this.ageTicks % 6 == 0) {
            // Section 6.33 Dawn-Strider Agent (#FFEA78 / #A88654): daylight rays & floating wind-feather particles!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), 0xFFEA78, 22, 0.90F, 0.06F),
                    this.getX(), this.getY(0.98D), this.getZ(), 2, 0.28D, 0.34D, 0.28D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0xA88654, 24, 0.86F, 0.05F),
                    this.getX(), this.getY(0.82D), this.getZ(), 2, 0.28D, 0.30D, 0.28D, 0.01D);
        } else if (mutated && mat == EssenceType.ABYSS && this.ageTicks % 6 == 0) {
            // Section 6.34 Tentacled Kraken-Husk Agent (#211A21 / #225C7C): dark ink droplets & deep-sea bubble particles!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), 0x211A21, 22, 0.88F, -0.05F),
                    this.getX(), this.getY(0.92D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x225C7C, 22, 0.84F, 0.05F),
                    this.getX(), this.getY(0.75D), this.getZ(), 2, 0.28D, 0.30D, 0.28D, 0.01D);
        } else if (mutated && mat == EssenceType.AEGIS && this.ageTicks % 6 == 0) {
            // Section 6.35 Bulwark-Paladin Agent (#FFEA78 / #773214): glowing golden rune halos & granite bastion shields!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SIGIL.get(), 0xFFEA78, 22, 0.90F, 0.04F),
                    this.getX(), this.getY(1.02D), this.getZ(), 2, 0.28D, 0.32D, 0.28D, 0.01D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SHIELD.get(), 0x773214, 22, 0.82F, 0.02F),
                    this.getX(), this.getY(0.84D), this.getZ(), 2, 0.28D, 0.28D, 0.28D, 0.01D);
        } else if (mutated && mat == EssenceType.AURORA && this.ageTicks % 6 == 0) {
            // Section 6.36 Borealis-Archon Agent (#FFEA78 / #98DCF2): shimmering solar aurora ribbons & crystalline cryo sparkles!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), 0xFFEA78, 22, 0.90F, 0.05F),
                    this.getX(), this.getY(1.00D), this.getZ(), 2, 0.28D, 0.34D, 0.28D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), 0x98DCF2, 24, 0.86F, 0.06F),
                    this.getX(), this.getY(0.84D), this.getZ(), 2, 0.28D, 0.30D, 0.28D, 0.01D);
        } else if (mutated && mat == EssenceType.MIRAGE && this.ageTicks % 6 == 0) {
            // Section 6.37 Mirage-Duelist Agent (#FFEA78 / #C2B28F): heat-haze shimmer & 2 phantom step afterimages behind footsteps!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), 0xFFEA78, 20, 0.88F, 0.05F),
                    this.getX(), this.getY(0.98D), this.getZ(), 2, 0.28D, 0.34D, 0.28D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0xC2B28F, 22, 0.86F, 0.04F),
                    this.getX(), this.getY(0.82D), this.getZ(), 2, 0.28D, 0.30D, 0.28D, 0.01D);
            Vec3 vel = this.getDeltaMovement();
            if (vel.horizontalDistanceSqr() > 0.002D) {
                Vec3 back = new Vec3(-vel.x, 0.0D, -vel.z).normalize();
                for (int img = 1; img <= 2; img++) {
                    Vec3 trailPos = this.position().add(back.scale(0.65D * img));
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(),
                                    img == 1 ? 0xFFEA78 : 0xC2B28F, 16, 0.92F, 0.02F),
                            trailPos.x, trailPos.y + 0.90D, trailPos.z, 3, 0.16D, 0.42D, 0.16D, 0.01D);
                }
            }
        }

        // Section 6.19 Spore-Tender Agent: tends farms by walking to, harvesting, and replanting mature crops,
        // only engaging hostiles within a 6-block self-defense perimeter with spore puffs!
        if (mutated && mat == EssenceType.SPORE) {
            this.resetFallDistance();
            LivingEntity closeThreat = findNearestHostile(level, 6.0D);
            if (closeThreat == null) {
                BlockPos center = this.blockPosition();
                BlockPos nearestMature = null;
                double bestDistSq = Double.MAX_VALUE;
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-10, -1, -10), center.offset(10, 1, 10))) {
                    BlockState bs = level.getBlockState(p);
                    boolean mature = (bs.getBlock() instanceof CropBlock crop && crop.isMaxAge(bs))
                            || (bs.getBlock() instanceof net.minecraft.world.level.block.NetherWartBlock
                                && bs.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) >= 3)
                            || (bs.getBlock() instanceof net.minecraft.world.level.block.SweetBerryBushBlock
                                && bs.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) >= 3);
                    if (mature) {
                        double dSq = p.distToCenterSqr(this.position());
                        if (dSq < bestDistSq) {
                            bestDistSq = dSq;
                            nearestMature = p.immutable();
                        }
                    }
                }
                if (nearestMature != null) {
                    if (bestDistSq <= 6.25D && this.ageTicks % 16 == 0) {
                        BlockState bs = level.getBlockState(nearestMature);
                        boolean harvested = false;
                        if (bs.getBlock() instanceof CropBlock crop && crop.isMaxAge(bs)) {
                            Block.dropResources(bs, level, nearestMature, null, owner != null ? owner : this, ItemStack.EMPTY);
                            level.setBlock(nearestMature, crop.getStateForAge(0), Block.UPDATE_ALL);
                            harvested = true;
                        } else if (bs.getBlock() instanceof net.minecraft.world.level.block.NetherWartBlock
                                && bs.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) >= 3) {
                            Block.dropResources(bs, level, nearestMature, null, owner != null ? owner : this, ItemStack.EMPTY);
                            level.setBlock(nearestMature, bs.setValue(net.minecraft.world.level.block.NetherWartBlock.AGE, 0), Block.UPDATE_ALL);
                            harvested = true;
                        } else if (bs.getBlock() instanceof net.minecraft.world.level.block.SweetBerryBushBlock
                                && bs.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) >= 3) {
                            Block.popResource(level, nearestMature, new ItemStack(net.minecraft.world.item.Items.SWEET_BERRIES, 2 + level.random.nextInt(2)));
                            level.setBlock(nearestMature, bs.setValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE, 1), Block.UPDATE_ALL);
                            harvested = true;
                        }
                        if (harvested) {
                            level.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x1A5923, 24, 0.90F, 0.06F),
                                    nearestMature.getX() + 0.5D, nearestMature.getY() + 0.45D, nearestMature.getZ() + 0.5D,
                                    6, 0.25D, 0.25D, 0.25D, 0.02D
                            );
                            level.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xA88654, 24, 0.85F, 0.06F),
                                    nearestMature.getX() + 0.5D, nearestMature.getY() + 0.55D, nearestMature.getZ() + 0.5D,
                                    5, 0.25D, 0.25D, 0.25D, 0.02D
                            );
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    nearestMature.getX() + 0.5D, nearestMature.getY() + 0.5D, nearestMature.getZ() + 0.5D,
                                    4, 0.25D, 0.25D, 0.25D, 0.02D);
                            level.playSound(null, nearestMature, SoundEvents.CROP_PLANTED, SoundSource.PLAYERS, 0.65F, 1.15F);
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
            int ceilingY = findIndoorCeilingY(level, this.blockPosition(), 10);
            if (ceilingY > 0) {
                this.getNavigation().stop();
                Vec3 flat = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
                double flatDistSq = flat.lengthSqr();
                if (flatDistSq <= 8.0D && this.getY() >= target.getY() + 1.35D && this.actionCooldown <= 0) {
                    // Silent ceiling drop-ambush strike!
                    this.setNoGravity(false);
                    Vec3 pounce = target.position().subtract(this.position()).normalize().scale(0.98D);
                    this.setDeltaMovement(pounce.x, Math.min(-0.48D, pounce.y), pounce.z);
                    this.hasImpulse = true;
                    this.resetFallDistance();
                    this.actionCooldown = 22;
                    this.meleeHitCounter++;
                    target.invulnerableTime = 0;
                    ddraig.net.entropica.spell.mutation.element.UmbralSpellMutationHandler.pinEntityShadowToFloor(
                            level, target, 40, tier
                    );
                    SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                            level, target, owner != null ? owner : this, 3.2F + tier * 0.80F, false, true
                    );
                    performAgentMeleeStrike(level, owner, target, mat, true, tier);
                    level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.9D), target.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.03D);
                    return;
                }
                this.setNoGravity(true);
                double clingY = ceilingY - 1.35D;
                double dy = MthClamp((clingY - this.getY()) * 0.34D, -0.25D, 0.36D);
                Vec3 horizVel = flatDistSq > 0.25D ? flat.normalize().scale(0.38D) : Vec3.ZERO;
                this.setDeltaMovement(horizVel.x, dy, horizVel.z);
                this.hasImpulse = true;
                this.resetFallDistance();
                if (this.ageTicks % 8 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(1.1D), this.getZ(), 3, 0.2D, 0.1D, 0.2D, 0.01D);
                }
                return;
            } else if (this.isNoGravity()) {
                this.setNoGravity(false);
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
            int lTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                    level, this.position().add(0.0D, 1.05D, 0.0D), target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D),
                    EssenceType.ABYSS, EssenceType.WATER, lTier
            );
            Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.92D);
            target.setDeltaMovement(pull.x, 0.26D, pull.z);
            target.hasImpulse = true;
            SpellLingeringZoneManager.applySoaked(target, 120);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
            SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(
                    level, target, owner != null ? owner : this, 1.85F + lTier * 0.48F, false, true
            );
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
                if (mat == EssenceType.NETHER) {
                    level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.45D), this.getZ(), 6, 0.25D, 0.2D, 0.25D, 0.04D);
                    level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.65F, 1.18F);
                }
            }
        }

        // Void-Stalker short-range phase-stepping (#10: >3m every 28t with 3D rift streak!) & Umbral/Astral/Null/Wraith/Penumbra/Miasma phase-steps
        boolean voidShortPhase = mutated && mat == EssenceType.VOID && distSq > 9.0D && this.ageTicks % 28 == 0;
        if (voidShortPhase || (mutated && (mat == EssenceType.UMBRAL
                || mat == EssenceType.ASTRAL || mat == EssenceType.NULL_R || mat == EssenceType.NULL_U
                || mat == EssenceType.WRAITH || mat == EssenceType.PENUMBRA || mat == EssenceType.MIASMA)
                && distSq > 16.0D && this.ageTicks % 40 == 0)) {
            Vec3 fromPos = this.position();
            if (mat == EssenceType.MIASMA) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                        this.getX(), this.getY(0.2D), this.getZ(), 14, 0.35D, 0.2D, 0.35D, 0.05D);
            }
            Vec3 behind = target.position().subtract(target.getLookAngle().scale(1.2D));
            this.teleportTo(behind.x, target.getY(), behind.z);
            if (mat == EssenceType.VOID || mat == EssenceType.ASTRAL || mat == EssenceType.NULL_R) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(
                        level, fromPos, this.position(), mat, tier
                );
                level.playSound(null, this.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.65F, 1.25F);
            }
            if (mat == EssenceType.MIASMA) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(
                        level, fromPos, this.position(), EssenceType.MIASMA, tier
                );
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                        this.getX(), this.getY(0.2D), this.getZ(), 14, 0.35D, 0.2D, 0.35D, 0.05D);
                level.playSound(null, this.blockPosition(), SoundEvents.MUD_STEP, SoundSource.PLAYERS, 0.75F, 0.72F);
            }
            level.sendParticles(mat == EssenceType.MIASMA ? ParticleTypes.SPORE_BLOSSOM_AIR
                            : ((mat == EssenceType.UMBRAL || mat == EssenceType.NULL_U) ? ParticleTypes.SMOKE
                            : (mat == EssenceType.NULL_R ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL)),
                    this.getX(), this.getY(0.8D), this.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.05D);
        } else {
            boolean swiftPursuit = mutated && (mat == EssenceType.AIR || mat == EssenceType.ARID
                    || mat == EssenceType.NETHER || mat == EssenceType.UMBRAL || mat == EssenceType.VOID
                    || mat == EssenceType.LIGHTNING
                    || mat == EssenceType.STORM || mat == EssenceType.DUST
                    || mat == EssenceType.TAIGA || mat == EssenceType.AURA || mat == EssenceType.ASTRAL
                    || mat == EssenceType.NULL_R || mat == EssenceType.NULL_U
                    || mat == EssenceType.DAWN || mat == EssenceType.MIRAGE || mat == EssenceType.ECLIPSE
                    || mat == EssenceType.WRAITH || mat == EssenceType.PENUMBRA
                    || mat == EssenceType.SPRING || mat == EssenceType.STATIC || mat == EssenceType.GENESIS
                    || mat == EssenceType.AETHER || mat == EssenceType.ENTROPIC || mat == EssenceType.CELESTIAL
                    || mat == EssenceType.CATACLYSM || mat == EssenceType.TERMINUS || mat == EssenceType.ESCHATON
                    || mat == EssenceType.APOTHEOSIS || mat == EssenceType.ENTROPICA);
            this.getNavigation().moveTo(target, swiftPursuit ? 1.45D : 1.2D);
            // True wall-phasing step-through for Void-Stalker, Astral, Null_R, Null_U, Wraith Stalkers & Tier 4–6 Apex/Eschaton/Singularity Agents!
            if (mutated && (mat == EssenceType.VOID || mat == EssenceType.WRAITH || mat == EssenceType.ASTRAL
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
                                level.sendParticles(mat == EssenceType.NULL_R ? ParticleTypes.END_ROD
                                                : (mat == EssenceType.CATACLYSM ? ParticleTypes.LAVA
                                                : (mat == EssenceType.ESCHATON ? ParticleTypes.REVERSE_PORTAL
                                                : (mat == EssenceType.NULL_U ? ParticleTypes.SQUID_INK : ParticleTypes.SOUL))),
                                        this.getX(), this.getY(0.8D), this.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.02D);
                                break;
                            }
                        }
                    }
                }
            }
            // Section 6.55 Stygian Magma-Void Serpent: pulls distant enemies within 12m directly into its molten jaws while burrowing/closing!
            if (mutated && mat == EssenceType.CATACLYSM && distSq >= 9.0D && distSq <= 144.0D && this.ageTicks % 24 == 0) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position());
                    if (pull.lengthSqr() > 1.0E-4D) {
                        Vec3 v = pull.normalize().scale(0.68D);
                        foe.setDeltaMovement(foe.getDeltaMovement().add(v.x, 0.16D, v.z));
                        foe.hasImpulse = true;
                    }
                }
                level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.5D), this.getZ(), 6, 0.35D, 0.25D, 0.35D, 0.03D);
            } else if (mutated && mat == EssenceType.ESCHATON && distSq >= 9.0D && distSq <= 196.0D && this.ageTicks % 20 == 0) {
                // Section 6.57 Event-Horizon Leviathan: drags nearby mobs and loose matter inward while gliding through terrain!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(14.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position());
                    if (pull.lengthSqr() > 1.0E-4D) {
                        Vec3 v = pull.normalize().scale(0.75D);
                        foe.setDeltaMovement(foe.getDeltaMovement().add(v.x, 0.18D, v.z));
                        foe.hasImpulse = true;
                    }
                }
                for (net.minecraft.world.entity.item.ItemEntity item : level.getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (!item.isAlive()) continue;
                    Vec3 pull = this.position().add(0.0D, 0.6D, 0.0D).subtract(item.position());
                    if (pull.lengthSqr() > 1.0E-4D) {
                        Vec3 v = pull.normalize().scale(0.55D);
                        item.setDeltaMovement(item.getDeltaMovement().scale(0.35D).add(v));
                        item.hasImpulse = true;
                    }
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.7D), this.getZ(), 10, 0.45D, 0.35D, 0.45D, 0.04D);
            } else if (mutated && mat == EssenceType.ENTROPICA && distSq >= 9.0D && distSq <= 400.0D && this.ageTicks % 22 == 0) {
                // Section 6.59 Avatar of Entropica (90s / 140 HP): pulls distant foes into its aura & hurls alternating void/radiant orbs
                // (75% armor bypass, -5 armor per strike, wall-phasing)!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(14.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position());
                    if (pull.lengthSqr() > 1.0E-4D) {
                        Vec3 v = pull.normalize().scale(0.72D);
                        foe.setDeltaMovement(foe.getDeltaMovement().add(v.x, 0.16D, v.z));
                        foe.hasImpulse = true;
                    }
                }
                Vec3 aim = target.getEyePosition().subtract(this.getEyePosition());
                if (aim.lengthSqr() > 1.0E-4D) {
                    Vec3 orbDir = aim.normalize();
                    boolean radiantPhase = ((this.ageTicks / 22) & 1) == 0;
                    SpellProjectileEntity orb = new SpellProjectileEntity(level, owner != null ? owner : this);
                    orb.setPos(this.getX() + orbDir.x * 0.75D, this.getEyeY() - 0.1D + orbDir.y * 0.75D, this.getZ() + orbDir.z * 0.75D);
                    orb.configurePayload(this.spell, this.slotIndex, this.fallbackMateria, this.apparatusPotency * 0.75F);
                    orb.setSentryBoltMateria(EssenceType.ENTROPICA);
                    orb.setWallPhasing(true);
                    orb.setNoGravity(true);
                    orb.addBonusPierces(3);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(orb).putBoolean("SpellAvatarAlternatingOrb", true);
                    orb.shoot(orbDir.x, orbDir.y, orbDir.z, 2.15F, 0.0F);
                    level.addFreshEntity(orb);
                    level.sendParticles(radiantPhase ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL,
                            this.getX(), this.getY(1.1D), this.getZ(), 10, 0.35D, 0.35D, 0.35D, 0.05D);
                    level.playSound(null, this.blockPosition(),
                            radiantPhase ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.CHORUS_FRUIT_TELEPORT,
                            SoundSource.PLAYERS, 0.65F, 1.48F);
                }
            }
        }

        // Section 6.4 Sylvan Agent: lunges forward across mid-range gaps to lash & root foes with briar tendrils!
        if (mutated && mat == EssenceType.NATURE && distSq >= 9.0D && distSq <= 49.0D
                && this.onGround() && this.ageTicks % 32 == 0) {
            Vec3 lungeDelta = target.position().subtract(this.position());
            Vec3 horizLunge = new Vec3(lungeDelta.x, 0.0D, lungeDelta.z);
            if (horizLunge.lengthSqr() > 0.01D) {
                Vec3 lungeDir = horizLunge.normalize();
                this.setDeltaMovement(lungeDir.x * 0.92D, 0.38D, lungeDir.z * 0.92D);
                this.hasImpulse = true;
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x2CE668, 26, 1.0F, 0.09F),
                        this.getX(), this.getY(0.5D), this.getZ(), 12, 0.35D, 0.25D, 0.35D, 0.06D);
                level.playSound(null, this.blockPosition(), SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.PLAYERS, 0.65F, 1.25F);
            }
        } else if (mutated && mat == EssenceType.DUST && distSq >= 9.0D && distSq <= 64.0D
                && this.onGround() && this.ageTicks % 26 == 0) {
            // Section 6.18 Dust-Stalker Agent: dashes swiftly across gaps in a #C2B28F chalk powder gust!
            Vec3 dashDelta = target.position().subtract(this.position());
            Vec3 horizDash = new Vec3(dashDelta.x, 0.0D, dashDelta.z);
            if (horizDash.lengthSqr() > 0.01D) {
                Vec3 dashDir = horizDash.normalize();
                this.setDeltaMovement(dashDir.x * 1.12D, 0.34D, dashDir.z * 1.12D);
                this.hasImpulse = true;
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                        this.getX(), this.getY(0.55D), this.getZ(), 14, 0.35D, 0.28D, 0.35D, 0.06D);
                level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.45F, 1.45F);
            }
        } else if (mutated && mat == EssenceType.STATIC && distSq >= 9.0D && distSq <= 64.0D
                && this.onGround() && this.ageTicks % 24 == 0) {
            // Section 6.44 Galvanic Arc-Vanguard: executes rapid flashing dashes across mid-range gaps!
            Vec3 dashDelta = target.position().subtract(this.position());
            Vec3 horizDash = new Vec3(dashDelta.x, 0.0D, dashDelta.z);
            if (horizDash.lengthSqr() > 0.01D) {
                Vec3 dashDir = horizDash.normalize();
                Vec3 from = this.getEyePosition().subtract(0.0D, 0.35D, 0.0D);
                this.setDeltaMovement(dashDir.x * 1.25D, 0.32D, dashDir.z * 1.25D);
                this.hasImpulse = true;
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                        level, from, from.add(dashDir.scale(2.8D)), EssenceType.STATIC, EssenceType.LIGHTNING, tier
                );
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(0.65D), this.getZ(), 14, 0.30D, 0.25D, 0.30D, 0.06D);
                level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.28F, 1.75F);
            }
        }

        double maxMeleeReachSq = (mutated && mat == EssenceType.WATER) ? 20.25D
                : ((mutated && mat == EssenceType.NATURE) ? 16.0D : 6.25D);
        if (distSq <= maxMeleeReachSq && this.actionCooldown <= 0) {
            boolean rapidCadence = mutated && (mat == EssenceType.AIR || mat == EssenceType.ARID
                    || mat == EssenceType.STORM || mat == EssenceType.DUST || mat == EssenceType.TAIGA
                    || mat == EssenceType.AURA || mat == EssenceType.PENUMBRA
                    || mat == EssenceType.SPRING || mat == EssenceType.STATIC
                    || (mat == EssenceType.CHIMERA && "WATER".equals(
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(this)
                            .getString("SpellChimeraSummonElement").orElse(""))));
            this.actionCooldown = rapidCadence ? 13 : (mutated && mat == EssenceType.WATER ? 17 : 20);
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
                this.spell, slot, this.fallbackMateria, this.apparatusPotency * 0.85F);

        if (!mutated) {
            return;
        }

        switch (mat) {
            case AIR -> {
                if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                    SpellExecutionEngine.applySyncedMotion(target, new Vec3(dir.x * 0.72D, 0.62D, dir.z * 0.72D));
                }
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.3D, 0.0D), dir, 2.2D, EssenceType.AIR, null, tier
                );
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.7D), target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
            }
            case WATER -> {
                // Section 6.3 Aqueous Agent: lashes targets with pressurized 3D water tendrils!
                target.clearFire();
                SpellLingeringZoneManager.applySoaked(target, 180);
                Vec3 lashOrigin = this.getEyePosition().subtract(0.0D, 0.25D, 0.0D);
                Vec3 lashEnd = target.getEyePosition().subtract(0.0D, 0.25D, 0.0D);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                        level, lashOrigin, lashEnd, EssenceType.WATER, null, tier
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, lashOrigin, dir, 2.5D, EssenceType.WATER, null, tier
                );
                level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.7D), target.getZ(),
                        14, 0.3D, 0.3D, 0.3D, 0.10D);
                level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.65F, 1.28F);
                if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                    double dSq = this.distanceToSqr(target);
                    if (dSq > 10.0D) {
                        // Mid-range tendril whip snaps distant foes off-balance inward!
                        SpellExecutionEngine.applySyncedMotion(target, new Vec3(-dir.x * 0.55D, 0.26D, -dir.z * 0.55D));
                    } else {
                        SpellExecutionEngine.applySyncedMotion(target, new Vec3(dir.x * 0.78D, 0.28D, dir.z * 0.78D));
                    }
                }
            }
            case NATURE -> {
                // Section 6.4 Sylvan Agent: lashes targets with 3D thorny briar tendrils & roots them for 2.0s (40 ticks)!
                Vec3 briarOrigin = this.getEyePosition().subtract(0.0D, 0.25D, 0.0D);
                Vec3 briarEnd = target.getEyePosition().subtract(0.0D, 0.30D, 0.0D);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                        level, briarOrigin, briarEnd, EssenceType.NATURE, null, tier
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, briarOrigin, dir, 2.4D, EssenceType.NATURE, null, tier
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                        level, target.position().add(0.0D, 0.12D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                        EssenceType.NATURE, null, 1.15F, tier
                );
                if (!SpellCombatReactionEngine.isBossEntity(target)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                    target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                            .putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 40L);
                    target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
                    target.hasImpulse = true;
                }
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 28, 1.0F, 0.08F),
                        target.getX(), target.getY(0.5D), target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.04D);
                level.playSound(null, target.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.70F, 0.90F);
            }
            case EARTH -> {
                // Section 6.5 Tectonic Eidolon Agent: heavy ground-punching pure Earth Materia slam (+1 vs Void) & 3D stalagmite eruption!
                Vec3 slamOrigin = this.position().add(0.0D, 0.95D, 0.0D).add(dir.scale(0.55D));
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, slamOrigin, dir, 2.3D, EssenceType.EARTH, null, tier
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                        level, target.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                        EssenceType.EARTH, null, 1.35F, tier
                );
                SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.5F + tier * 0.85F, false, true
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                    SpellExecutionEngine.applySyncedMotion(target, new Vec3(dir.x * 0.55D, 0.56D, dir.z * 0.55D));
                }
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TERRACOTTA.defaultBlockState()),
                        target.getX(), target.getY(0.25D), target.getZ(), 14, 0.35D, 0.20D, 0.35D, 0.12D);
                level.playSound(null, target.blockPosition(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.85F, 0.78F);
            }
            case FROZEN, GLACIAL -> {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D),
                        dir, 1.95D, mat, null, tier
                );
                if (mat == EssenceType.GLACIAL) {
                    SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(
                            level, target, owner != null ? owner : this, 2.6F + tier * 0.80F, false, false, true
                    );
                    SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.30D, 60L);
                    int freezeGain = SpellLingeringZoneManager.isEntityWetOrSoaked(target) ? 120 : 45;
                    target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 80, target.getTicksFrozen() + freezeGain));
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.45D, 50L);
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                } else {
                    SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                            level, target, owner != null ? owner : this, 2.4F + tier * 0.75F, false, true
                    );
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.45D, 50L);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.65D), target.getZ(), 12, 0.30D, 0.30D, 0.30D, 0.04D);
                // 3 consecutive hits on the same target freeze them solid inside temporary solid ice!
                if (this.consecutiveTargetHits >= 3 && this.consecutiveTargetHits % 3 == 0) {
                    ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(
                            level, owner != null ? owner : this, target, 40, this.spell, null
                    );
                }
            }
            case NETHER -> {
                // Section 6.7 Infernal Eidolon Agent: slashes targets with 3D burning claws & ignites them in Netherfire!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                        level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                        this.getEyePosition().subtract(0.0D, 0.25D, 0.0D),
                        dir, EssenceType.NETHER, null, 2.2F, 50, tier
                );
                target.igniteForSeconds(5);
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target).putLong(
                        SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
                );
                SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.5F + tier * 0.75F, true
                );
                level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.65D), target.getZ(),
                        8, 0.25D, 0.25D, 0.25D, 0.04D);
                level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.70F, 1.15F);
            }
            case UMBRAL -> {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                        level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                        this.getEyePosition().subtract(0.0D, 0.25D, 0.0D),
                        dir, EssenceType.UMBRAL, null, 2.15F, 50, tier
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
                SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.5F + tier * 0.68F, false, true
                );
            }
            case UNDEAD -> {
                // Section 6.10 Cryptguard Eidolon Agent: slashes with 3D withering bone claws that siphon health on hit!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.25D, 0.0D),
                        dir, 2.15D, EssenceType.UNDEAD, null, tier
                );
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                float clawDealt = SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.5F + tier * 0.75F, false, false, false
                );
                float selfSiphon = Math.max(1.6F, clawDealt * 0.25F);
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(selfSiphon);
                }
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.25F, this.apparatusPotency * 0.75F);
                }
                level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY(0.7D), target.getZ(), 6, 0.22D, 0.25D, 0.22D, 0.03D);
                level.playSound(null, target.blockPosition(), SoundEvents.SKELETON_HURT, SoundSource.PLAYERS, 0.70F, 0.82F);
            }
            case VOID -> {
                // Section 6.11 Void-Stalker Agent: strikes with armor-piercing void claws (#41304E)!
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.5F + tier * 0.80F, false, 0.45F, true
                );
                target.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.VOID_TEAR.get()), 60, 0));
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 2.2D, EssenceType.VOID, null, tier
                );
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.75D), target.getZ(),
                        10, 0.25D, 0.30D, 0.25D, 0.06D);
                level.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.65F, 1.35F);
            }
            case ARID -> {
                // Section 6.12 Dune-Dervish Agent: attacks rapidly with twin sandblades, kicking up blinding sand-dust clouds!
                SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.2F + tier * 0.72F, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 2.2D, EssenceType.ARID, null, tier
                );
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        target.getX(), target.getY(0.8D), target.getZ(), 18, 1.1D, 0.45D, 1.1D, 0.05D);
                level.playSound(null, target.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.65F, 1.25F);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.5D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0));
                }
            }
            case LIGHTNING -> {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 2.25D, EssenceType.LIGHTNING, null, tier
                );
                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.4F + tier * 0.72F, false, true
                );
                target.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 16, 0));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.7D), target.getZ(),
                        12, 0.25D, 0.35D, 0.25D, 0.10D);
            }
            case MAGMA -> {
                // Section 6.14 Magma-Brute Agent: heavy ground-shaking magma punch igniting enemies & trembling surrounding foes!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, target.position().add(0.0D, 0.06D, 0.0D), 2.4D, EssenceType.MAGMA, EssenceType.EARTH, tier
                );
                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.6F + tier * 0.78F, false, true
                );
                target.igniteForSeconds(6);
                if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                    SpellExecutionEngine.applySyncedMotion(target, new Vec3(dir.x * 0.62D, 0.44D, dir.z * 0.62D));
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.4D))) {
                    if (foe == this || foe == owner || foe == target || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.igniteForSeconds(4);
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(foe.getDeltaMovement().x * 0.4D, 0.28D, foe.getDeltaMovement().z * 0.4D));
                    }
                }
                level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.6D), target.getZ(), 12, 0.30D, 0.30D, 0.30D, 0.06D);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                        target.getX(), target.getY(0.25D), target.getZ(), 14, 0.45D, 0.20D, 0.45D, 0.12D);
                level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.45F, 0.68F);
            }
            case STORM -> {
                // Section 6.15 Tempest-Zephyr Agent: swift squall strike drenching & knocking enemies back!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.32D, 0.0D), dir, 2.35D, EssenceType.STORM, EssenceType.AIR, tier
                );
                SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.5F + tier * 0.72F, false, true
                );
                target.clearFire();
                SpellLingeringZoneManager.applySoaked(target, 180);
                if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                    SpellExecutionEngine.applySyncedMotion(target, new Vec3(dir.x * 1.15D, 0.42D, dir.z * 1.15D));
                }
                level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.7D), target.getZ(), 14, 0.30D, 0.30D, 0.30D, 0.08D);
                level.playSound(null, target.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.55F, 1.25F);
            }
            case OVERGROWTH -> {
                // Section 6.17 Sylvan-Goliath Agent: heavy ground-smashing punches that root enemies in briars!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, target.position().add(0.0D, 0.05D, 0.0D), 1.85D, EssenceType.OVERGROWTH, EssenceType.EARTH, 48, tier
                );
                SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.8F + tier * 0.85F, false, true
                );
                long rootUntil = level.getGameTime() + 50L;
                CompoundTag tData = ddraig.net.entropica.util.EntityHelper.getPersistentData(target);
                tData.putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BRIAR_ROOT_UNTIL, rootUntil);
                tData.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, rootUntil);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                    SpellExecutionEngine.applySyncedMotion(target, new Vec3(0.0D, -0.55D, 0.0D));
                }
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                        target.getX(), target.getY(0.25D), target.getZ(), 14, 0.35D, 0.20D, 0.35D, 0.12D);
                level.playSound(null, target.blockPosition(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.85F, 0.78F);
                level.playSound(null, target.blockPosition(), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.80F, 0.88F);
            }
            case DUST -> {
                // Section 6.18 Dust-Stalker Agent: blinding abrasive strikes that deal Dust Materia damage & inflict Armor Erosion!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.32D, 0.0D), dir, 2.25D, EssenceType.DUST, EssenceType.AIR, tier
                );
                SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.5F + tier * 0.75F, false, true
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.stackArmorErosionPublic(target, 2, 140);
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                        target.getX(), target.getY(0.75D), target.getZ(), 14, 0.32D, 0.32D, 0.32D, 0.08D);
                level.playSound(null, target.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.75F, 1.15F);
            }
            case SPORE -> {
                // Section 6.19 Spore-Tender Agent: defensive dual-tone (#1A5923 / #A88654) spore puff cloud!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, this.position().add(0.0D, 0.15D, 0.0D), 3.0D, EssenceType.SPORE, EssenceType.AIR, tier
                );
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x1A5923, 28, 1.0F, 0.08F),
                        this.getX(), this.getY(0.8D), this.getZ(), 14, 1.1D, 0.5D, 1.1D, 0.04D
                );
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xA88654, 28, 0.95F, 0.08F),
                        this.getX(), this.getY(0.9D), this.getZ(), 12, 1.1D, 0.5D, 1.1D, 0.04D
                );
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(0.8D), this.getZ(),
                        16, 1.2D, 0.5D, 1.2D, 0.04D);
                level.playSound(null, this.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.85F, 0.95F);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.2D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 2.2F + tier * 0.68F, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = foe.position().subtract(this.position());
                        Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize() : dir;
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 0.65D, 0.24D, push.z * 0.65D));
                    }
                }
            }
            case TAIGA -> {
                // Section 6.20 Boreal Stalker Agent (#1A5923 / #98DCF2): leaping frost-resin claw strike!
                float taigaClawDmg = (2.15F + tier * 0.65F) * Math.max(0.5F, this.apparatusPotency);
                SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(
                        level, target, owner != null ? owner : this, taigaClawDmg, false, true
                );
                if (target.canFreeze()) {
                    target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 50, target.getTicksFrozen() + 60));
                }
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, level.getGameTime() + 80L);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.25D, EssenceType.TAIGA, EssenceType.FROZEN, tier
                );
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 24, 0.95F, 0.06F),
                        target.getX(), target.getY(0.70D), target.getZ(), 10, 0.32D, 0.35D, 0.32D, 0.05D);
                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        target.getX(), target.getY(0.75D), target.getZ(), 8, 0.32D, 0.35D, 0.32D, 0.04D);
            }
            case OASIS -> {
                // Section 6.21 Oasis-Warden Agent (#1A5923 / #C2B28F): deals Oasis Materia damage & siphons 20% healing to nearby allies!
                float wardenBaseDmg = (2.25F + tier * 0.68F) * Math.max(0.5F, this.apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(
                        level, target, owner != null ? owner : this, wardenBaseDmg, false, true
                );
                float siphonHeal = Math.max(1.0F, (dealt > 0.0F ? dealt : wardenBaseDmg) * 0.20F + tier * 0.22F);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.25D, EssenceType.OASIS, EssenceType.ARID, tier
                );
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.5D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, siphonHeal);
                        ally.heal(siphonHeal);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                ally.getX(), ally.getY(0.85D), ally.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                    }
                }
            }
            case VITAE -> {
                // Section 6.22 Vitality Knight Agent (#FF6B9D): deals Vitae Materia damage & leaches health on hit to heal the caster!
                float vitaeStrikeDmg = (2.35F + tier * 0.70F) * Math.max(0.5F, this.apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(
                        level, target, owner != null ? owner : this, vitaeStrikeDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.25D, EssenceType.VITAE, EssenceType.NATURE, tier
                );
                if (owner != null && owner.isAlive()) {
                    float siphonHeal = Math.max(1.2F, (dealt > 0.0F ? dealt : vitaeStrikeDmg) * 0.30F + tier * 0.25F);
                    SpellCombatReactionEngine.notifyMagicalHeal(owner, siphonHeal);
                    owner.heal(siphonHeal);
                    level.sendParticles(ParticleTypes.HEART,
                            owner.getX(), owner.getY(0.95D), owner.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                }
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), 0xFF6B9D, 22, 0.95F, 0.06F),
                        target.getX(), target.getY(0.75D), target.getZ(), 10, 0.32D, 0.35D, 0.32D, 0.04D);
            }
            case BLOOD -> {
                // Section 6.23 Blood-Thrall Knight (#8A0303): melee swings deal Blood Materia damage,
                // inflict Hemorrhage, and siphon exact 25% of damage dealt to heal the caster!
                float bloodStrikeDmg = (2.35F + tier * 0.70F) * Math.max(0.5F, this.apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(
                        level, target, owner != null ? owner : this, bloodStrikeDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.25D, EssenceType.BLOOD, EssenceType.VITAE, tier
                );
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 80, 0));
                CompoundTag tData = ddraig.net.entropica.util.EntityHelper.getPersistentData(target);
                tData.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, level.getGameTime() + 80L);
                tData.putInt("SpellBloodHemorrhageAmp", 0);
                if (owner != null && owner.isAlive()) {
                    float physBase = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float totalHit = Math.max(1.0F, (dealt > 0.0F ? dealt : bloodStrikeDmg) + physBase * 0.65F);
                    float siphonHeal = totalHit * 0.25F;
                    SpellCombatReactionEngine.notifyMagicalHeal(owner, siphonHeal);
                    owner.heal(siphonHeal);
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 20, 0.90F, 0.04F),
                            owner.getX(), owner.getY(0.95D), owner.getZ(), 5, 0.25D, 0.28D, 0.25D, 0.03D);
                }
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 22, 0.98F, 0.06F),
                        target.getX(), target.getY(0.75D), target.getZ(), 12, 0.32D, 0.35D, 0.32D, 0.05D);
            }
            case AURA -> {
                // Section 6.24 Zephyr-Monk Agent (#FF6B9D / #A88654): deals Aura Materia damage,
                // knocks hostiles airborne, and siphons exact 20% of damage dealt into caster Materia fuel!
                float auraStrikeDmg = (2.30F + tier * 0.68F) * Math.max(0.5F, this.apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(
                        level, target, owner != null ? owner : this, auraStrikeDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.25D, EssenceType.AURA, null, tier
                );
                if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                    SpellExecutionEngine.applySyncedMotion(target, new Vec3(dir.x * 0.45D, 0.68D, dir.z * 0.45D));
                }
                if (owner instanceof Player player && player.isAlive()) {
                    float physBase = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float totalHit = Math.max(1.0F, (dealt > 0.0F ? dealt : auraStrikeDmg) + physBase * 0.65F);
                    SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.AURA, tier, totalHit, 0.20F);
                }
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0xFF6B9D, 20, 0.95F, 0.06F),
                        target.getX(), target.getY(0.75D), target.getZ(), 8, 0.30D, 0.35D, 0.30D, 0.04D);
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0xA88654, 22, 0.92F, 0.05F),
                        target.getX(), target.getY(0.85D), target.getZ(), 8, 0.30D, 0.35D, 0.30D, 0.04D);
            }
            case AMBER -> {
                // Section 6.25 Amber-Clad Sentinel (#FF6B9D / #773214): heavy hammer swings deal Amber Materia damage,
                // inflicting exact 50% Slowness and -30% Armor Toughness!
                float amberHammerDmg = (2.45F + tier * 0.72F) * Math.max(0.5F, this.apparatusPotency);
                SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(
                        level, target, owner != null ? owner : this, amberHammerDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.35D, EssenceType.AMBER, null, tier
                );
                SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.50D, 80L);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.30D, 80L);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                        target.getX(), target.getY(0.75D), target.getZ(), 12, 0.30D, 0.32D, 0.30D, 0.06D);
                level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.45F, 1.35F);
            }
            case ECLIPSE -> {
                // Section 6.26 Eclipse-Knight Agent: shield-piercing strikes inflicting Blindness, Glowing & shredding armor!
                if (target.isBlocking()) {
                    if (target instanceof Player bp && !bp.getUseItem().isEmpty()) {
                        bp.getCooldowns().addCooldown(bp.getUseItem(), 60);
                    }
                    target.stopUsingItem();
                }
                float eclipseKnightDmg = (2.55F + tier * 0.78F) * Math.max(0.5F, this.apparatusPotency);
                SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(
                        level, target, owner != null ? owner : this, eclipseKnightDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.40D, EssenceType.ECLIPSE, null, tier
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 100L);
            }
            case BLIGHT -> {
                // Section 6.27 Blight-Revenant Agent: rotting briar claw swings inflicting Poison II & shredding armor!
                float blightClawDmg = (2.45F + tier * 0.75F) * Math.max(0.5F, this.apparatusPotency);
                SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(
                        level, target, owner != null ? owner : this, blightClawDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.35D, EssenceType.BLIGHT, null, tier
                );
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.0D, 0.0D, 100L);
            }
            case ASTRAL -> {
                float punchDmg = 1.65F + tier * 0.45F;
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(
                        level, target, owner != null ? owner : this, punchDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.35D, EssenceType.ASTRAL, EssenceType.LIGHTNING, tier
                );
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 16, 0));
                target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0));
                target.setDeltaMovement(dir.x * 0.42D, 0.68D, dir.z * 0.42D);
                target.hasImpulse = true;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.85D), target.getZ(), 12, 0.25D, 0.30D, 0.25D, 0.08D);
            }
            case SOULFIRE -> {
                float bladeDmg = 1.65F + tier * 0.45F;
                float dealt = SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(
                        level, target, owner != null ? owner : this, bladeDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.35D, EssenceType.SOULFIRE, EssenceType.UNDEAD, tier
                );
                target.igniteForSeconds(5);
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 100L);
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.5F);
                }
                if (owner != null && owner.isAlive()) {
                    float siphon = Math.max(0.6F, dealt * 0.20F);
                    SpellCombatReactionEngine.notifyMagicalHeal(owner, siphon);
                    owner.heal(siphon);
                }
            }
            case VAPOR -> {
                float punchDmg = 1.75F + tier * 0.48F;
                SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(
                        level, target, owner != null ? owner : this, punchDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.35D, EssenceType.VAPOR, EssenceType.WATER, tier
                );
                target.setDeltaMovement(dir.x * 0.98D, 0.40D, dir.z * 0.98D);
                target.hasImpulse = true;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                SpellLingeringZoneManager.applySoaked(target, 120);
                level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.85D), target.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.05D);
            }
            case NULL_R -> {
                float bladeDmg = 2.15F + tier * 0.62F;
                SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(
                        level, target, owner != null ? owner : this, bladeDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.40D, EssenceType.NULL_R, EssenceType.VOID, tier
                );
                ddraig.net.entropica.spell.mutation.element.EclipseSpellMutationHandler.purgeBeneficialEffects(target);
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.85D), target.getZ(), 12, 0.28D, 0.30D, 0.28D, 0.05D);
            }
            case NULL_U -> {
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.50D, 0.0D, 40L);
                float stalkerDmg = 2.40F + tier * 0.68F;
                SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(
                        level, target, owner != null ? owner : this, stalkerDmg, false, true
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.35D, EssenceType.NULL_U, EssenceType.UMBRAL, tier
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
                level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.85D), target.getZ(), 10, 0.25D, 0.28D, 0.25D, 0.03D);
            }
            case DAWN -> {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.55D, EssenceType.DAWN, EssenceType.AIR, tier
                );
                float striderDmg = 2.15F + tier * 0.60F;
                SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(
                        level, target, owner != null ? owner : this, striderDmg, false, true
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                if (target.getType().is(EntityTypeTags.UNDEAD)) {
                    target.igniteForSeconds(4);
                }
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.7D), target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.4D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    foe.setDeltaMovement(dir.x * 0.88D, 0.36D, dir.z * 0.88D);
                    foe.hasImpulse = true;
                    if (foe != target) {
                        SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(
                                level, foe, owner != null ? owner : this, striderDmg * 0.65F, false, true
                        );
                    }
                }
            }
            case ABYSS -> {
                float krakenDmg = 2.65F + tier * 0.70F;
                SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(
                        level, target, owner != null ? owner : this, krakenDmg, false, true
                );
                SpellLingeringZoneManager.applySoaked(target, 160);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0));
                Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.62D);
                target.setDeltaMovement(pull.x, 0.20D, pull.z);
                target.hasImpulse = true;
                level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.7D), target.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.03D);
            }
            case AEGIS -> {
                float paladinDmg = (2.85F + tier * 0.74F) * (target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F);
                SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                        level, target, owner != null ? owner : this, paladinDmg, false, true
                );
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 24, 0));
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 120L);
                Vec3 bash = dir.scale(0.82D);
                target.setDeltaMovement(bash.x, 0.26D, bash.z);
                target.hasImpulse = true;
                level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
            }
            case AURORA -> {
                float archonDmg = (2.75F + tier * 0.72F) * (target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F);
                SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                        level, target, owner != null ? owner : this, archonDmg, false, true
                );
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 40);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.22D, 0.25D, 0.22D, 0.03D);
            }
            case MIRAGE -> {
                float duelistDmg = 2.65F + tier * 0.70F;
                SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(
                        level, target, owner != null ? owner : this, duelistDmg, false, true
                );
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
                float wraithClawDmg = (1.65F + tier * 0.48F) * Math.max(0.5F, this.apparatusPotency);
                SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(
                        level, target, owner != null ? owner : this, wraithClawDmg, false, true
                );
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(2.0F);
                }
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.applyTierScaledLifesteal(level, owner, tier, 0.25F, this.apparatusPotency * 0.75F);
                }
            }
            case BARROW -> {
                float flailDmg = (1.75F + tier * 0.50F) * Math.max(0.5F, this.apparatusPotency);
                SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(
                        level, target, owner != null ? owner : this, flailDmg, false, true
                );
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.0D, 0.0D, 100L);
                if (target instanceof Mob mob) {
                    mob.setTarget(this);
                }
            }
            case PYRE -> {
                // Section 6.40 Pyre-Juggernaut: sweeping burning petrified-log & molten-coal cleave!
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.7D), target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                float juggernautDmg = 2.40F + tier * 0.70F;
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(
                        level, target, owner != null ? owner : this, juggernautDmg, false, true
                );
                target.igniteForSeconds(5);
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target).putLong(
                        SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.0D, 0.0D, 100L);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.2D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.igniteForSeconds(5);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe).putLong(
                            SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
                    );
                    if (foe != target) {
                        SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(
                                level, foe, owner != null ? owner : this, 1.85F + tier * 0.50F, false, true
                        );
                    }
                }
            }
            case PENUMBRA -> {
                // Section 6.41 Penumbral Void-Bladewarden: 35% armor-bypassing twin-scythe strike, melee pull & 5.0s Blindness!
                float bladeDmg = 2.65F + tier * 0.78F;
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                        level, target, owner != null ? owner : this, bladeDmg, false, true
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.45D);
                target.setDeltaMovement(pull.x, 0.15D, pull.z);
                target.hasImpulse = true;
            }
            case RIME -> {
                // Section 6.42 Glacial Rime-Vanguard: sweeping 35% armor-bypassing frost cleave & 2.5s solid flash-freeze!
                float rimeBladeDmg = 2.70F + tier * 0.80F;
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(
                        level, target, owner != null ? owner : this, rimeBladeDmg, false, true
                );
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 50);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.0D))) {
                    if (foe == this || foe == owner || foe == target || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    foe.setTicksFrozen(foe.getTicksFrozen() + 45);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));
                    SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 1.75F + tier * 0.45F, false, true
                    );
                }
            }
            case SPRING -> {
                // Section 6.43 Vernal Bloom-Bladewarden: lightning-fast floral thrusts bypassing 35% of enemy armor,
                // dealing heavy Spring Materia damage, smiting Undead, and splashing 2 hearts (4.0 HP) healing pollen to allies!
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.35D, 0.0D, 30L);
                float smiteMult = target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                        level, target, owner != null ? owner : this, (3.10F + tier * 0.85F) * smiteMult, false, true
                );
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY(0.9D), target.getZ(), 10, 0.4D, 0.3D, 0.4D, 0.03D);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, 4.0F);
                        ally.heal(4.0F); // 2 hearts healing pollen splash!
                        level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.02D);
                    }
                }
            }
            case STATIC -> {
                // Section 6.44 Galvanic Arc-Vanguard: twin lightning-khopeshes, 35% armor bypass, chain-lightning shocks & 2.0s paralyze on critical strikes!
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.35D, 0.0D, 35L);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.25D, 0.0D), dir, 2.3D, EssenceType.STATIC, EssenceType.LIGHTNING, tier
                );
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.85F + tier * 0.80F, false, true
                );
                if (this.meleeHitCounter % 2 == 0) {
                    target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.9D), target.getZ(), 14, 0.30D, 0.35D, 0.30D, 0.06D);
                }
                int arcs = 0;
                for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.5D))) {
                    if (adj == target || adj == this || adj == owner || !adj.isAlive() || this.isFriendlyTo(adj)) continue;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                            level, target.getEyePosition(), adj.getEyePosition(), EssenceType.STATIC, EssenceType.LIGHTNING, tier
                    );
                    SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(
                            level, adj, owner != null ? owner : this, 2.15F + tier * 0.55F, false, true
                    );
                    if (++arcs >= 2) break;
                }
            }
            case SYLVAN -> {
                // Section 6.45 Ironwood Juggernaut: earth-shattering ground slam bypassing 35% of enemy armor,
                // dealing heavy Sylvan Materia damage, smiting Undead, and rooting targets for 2.5s (50t)!
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.35D, 0.0D, 35L);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.25D, 0.0D), dir, 2.4D, EssenceType.SYLVAN, EssenceType.RADIANT, tier
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, target.position(), 2.6D, EssenceType.SYLVAN, EssenceType.EARTH, tier
                );
                boolean isUndead = target.getType().is(EntityTypeTags.UNDEAD);
                if (isUndead) {
                    target.igniteForSeconds(3);
                }
                SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                        level, target, owner != null ? owner : this, (2.95F + tier * 0.85F) * (isUndead ? 1.50F : 1.0F), false, true
                );
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.35F, 0.78F);
            }
            case MIASMA -> {
                // Section 6.46 Septic Bog-Stalker: heavy Miasma Materia ambush strike, siphons 2 hearts (4.0 HP) to self or caster, blinds 3.0s (60t)!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 0.95D, 0.0D), dir, 2.35D, EssenceType.MIASMA, EssenceType.UNDEAD, tier
                );
                SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(
                        level, target, owner != null ? owner : this, 2.85F + tier * 0.85F, false, true
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                if (this.getHealth() < this.getMaxHealth() - 0.5F) {
                    this.heal(4.0F);
                } else if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.notifyMagicalHeal(owner, 4.0F);
                    owner.heal(4.0F);
                }
                level.sendParticles(ParticleTypes.SNEEZE, target.getX(), target.getY(0.75D), target.getZ(), 8, 0.30D, 0.30D, 0.30D, 0.03D);
            }
            case GENESIS -> {
                // Section 6.47 Solar-Hydro Templar: water-jet thrust bypassing 35% armor, smiting Undead/Oblivion/Wraith (+50%), heavy Genesis Materia damage & 3.0s Blindness!
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.35D, 0.0D, 40L);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.02D, 0.0D), dir, 2.50D, EssenceType.GENESIS, EssenceType.WATER, tier
                );
                float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(target) ? 1.50F : 1.0F;
                SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                        level, target, owner != null ? owner : this, (3.00F + tier * 0.88F) * smite, false, true
                );
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.75D), target.getZ(), 10, 0.30D, 0.30D, 0.30D, 0.06D);
                level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 0.55F, 1.32F);
            }
            case OBLIVION -> {
                // Section 6.48 Stygian Dread-Juggernaut: earth-shaking cleave bypassing 35% armor, heavy Oblivion damage, 6m knockback & 5.0s black stygian fire!
                SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.35D, 0.0D, 40L);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.position().add(0.0D, 1.05D, 0.0D), dir, 2.55D, EssenceType.OBLIVION, EssenceType.NETHER, tier
                );
                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(
                        level, target, owner != null ? owner : this, 3.10F + tier * 0.90F, false, true
                );
                target.setDeltaMovement(dir.x * 1.45D, 0.38D, dir.z * 1.45D); // 6m knockback!
                target.hasImpulse = true;
                target.igniteForSeconds(5);
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(0.8D), target.getZ(), 10, 0.35D, 0.35D, 0.35D, 0.04D);
                level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.38F, 0.72F);
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
                // Section 6.50 Firmament Archon-Colossus (90 HP): sweeping quad-elemental cleaves bypassing 50% armor,
                // shattering 3 armor per strike, and launching foes 8m airborne into zero-g float!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 3.8F, EssenceType.AETHER, EssenceType.AIR, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(
                        level, target, owner != null ? owner : this, 5.0F + tier * 1.25F, false, true
                );
                target.setDeltaMovement(dir.x * 0.45D, 1.18D, dir.z * 0.45D); // 8m airborne zero-g float!
                target.hasImpulse = true;
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 0));
            }
            case ENTROPIC -> {
                // Section 6.51 Entropic Dread-Titan (90 HP): sweeping scythe cleaves bypassing 50% armor,
                // shattering 3 armor per strike, siphoning 35% damage as health, and knocking foes 8m back!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 3.8F, EssenceType.ENTROPIC, EssenceType.BLOOD, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                float hitDmg = SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(
                        level, target, owner != null ? owner : this, 5.10F + tier * 1.25F, false, true
                );
                target.setDeltaMovement(dir.x * 1.85D, 0.40D, dir.z * 1.85D); // 8m knockback!
                target.hasImpulse = true;
                float siphon = Math.max(1.2F, hitDmg * 0.35F);
                this.heal(siphon);
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.notifyMagicalHeal(owner, siphon);
                    owner.heal(siphon);
                }
            }
            case CELESTIAL -> {
                // Section 6.52 Celestial Seraph-Colossus (100 HP): sweeping greatblade cleaves bypassing 50% armor,
                // shattering 3 armor per strike, smiting undead, siphoning 35% damage as health to the party & knocking foes 8m back!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 3.9F, EssenceType.CELESTIAL, EssenceType.RADIANT, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                float hitDmg = SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(
                        level, target, owner != null ? owner : this, 5.15F + tier * 1.25F, false, true
                );
                target.setDeltaMovement(dir.x * 1.85D, 0.40D, dir.z * 1.85D); // 8m knockback!
                target.hasImpulse = true;
                float partyHeal = Math.max(1.4F, hitDmg * 0.35F);
                this.heal(partyHeal * 0.5F);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, partyHeal);
                        ally.heal(partyHeal);
                    }
                }
            }
            case PRISMATIC -> {
                // Section 6.53 Prismatic Valkyrie (100 HP, 45s): bypasses 50% armor, heavy Prismatic damage & smites Undead/Blight!
                target.invulnerableTime = 0;
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D, 0.0D, 140L);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(
                        level, target, owner != null ? owner : this, 4.2F + tier * 1.05F, false, true, 1.75F
                );
            }
            case EMPYREAN -> {
                // Section 6.54 Yggdrasil Arboreal Juggernaut (110 HP, 60s): sweeping root cleaves bypassing 60% armor,
                // dealing heavy Empyrean damage, shattering 4 armor per strike, rooting 3.0s (60t) & siphoning 40% into party absorption!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 4.1F, EssenceType.EMPYREAN, EssenceType.NATURE, tier
                );
                target.invulnerableTime = 0;
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D, 0.0D, 160L);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                float dealt = SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                        level, target, owner != null ? owner : this, 5.60F + tier * 1.35F, false, true, false, true
                );
                float totalDealt = dealt;
                for (LivingEntity splash : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.6D))) {
                    if (splash == this || splash == owner || splash == target || !splash.isAlive() || this.isFriendlyTo(splash)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(splash, level, 4.0D, 0.0D, 140L);
                    splash.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                    splash.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                    totalDealt += SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                            level, splash, owner != null ? owner : this, 3.40F + tier * 0.85F, false, true, false, true
                    );
                }
                SpellLingeringZoneManager.grantPartyAbsorption(level, owner != null ? owner : this, Math.max(2.0F, totalDealt * 0.40F), 32.0F, 16.0D);
            }
            case CATACLYSM -> {
                // Section 6.55 Stygian Magma-Void Serpent (110 HP, 60s): burrows/phases to coil & crush hostiles,
                // bypassing 60% armor, dealing heavy Cataclysm damage, shattering 4 armor per strike, & pulling distant foes into its molten jaws!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().subtract(0.0D, 0.35D, 0.0D), dir, 4.2F, EssenceType.CATACLYSM, EssenceType.NETHER, tier
                );
                target.invulnerableTime = 0;
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D, 0.0D, 160L);
                target.igniteForSeconds(6);
                ddraig.net.entropica.util.EntityHelper.getPersistentData(target)
                        .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 120L);
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                        level, target, owner != null ? owner : this, 5.80F + tier * 1.40F, false, true, false
                );
                for (LivingEntity splash : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.6D))) {
                    if (splash == this || splash == owner || splash == target || !splash.isAlive() || this.isFriendlyTo(splash)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(splash, level, 4.0D, 0.0D, 140L);
                    splash.igniteForSeconds(5);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(splash)
                            .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
                    SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                            level, splash, owner != null ? owner : this, 3.50F + tier * 0.90F, false, true, false
                    );
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position());
                    if (pull.lengthSqr() > 1.0E-4D) {
                        Vec3 v = pull.normalize().scale(0.75D);
                        foe.setDeltaMovement(v.x, 0.20D, v.z);
                        foe.hasImpulse = true;
                    }
                }
                level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.6D), target.getZ(), 10, 0.35D, 0.30D, 0.35D, 0.04D);
            }
            case TERMINUS -> {
                // Section 6.56 Titanic Glacial Elemental (110 HP / 60s): sweeping glacial slams bypassing 60% of enemy armor,
                // dealing heavy Terminus damage, shattering 4 armor points per strike, & freezing targets solid for 3.0s!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().add(this.getLookAngle().scale(1.2D)),
                        this.getLookAngle(), 4.8D, EssenceType.TERMINUS, EssenceType.FROZEN, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D, 0.0D, 160L);
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(
                        level, target, owner != null ? owner : this, 5.80F + tier * 1.40F, false, true, false
                );
                if (target.isAlive()) {
                    ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(
                            level, owner != null ? owner : this, target, 60, 0, null, null
                    );
                }
                for (LivingEntity splash : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D))) {
                    if (splash == this || splash == owner || splash == target || !splash.isAlive() || this.isFriendlyTo(splash) || !(splash instanceof Monster)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(splash, level, 4.0D, 0.0D, 160L);
                    SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(
                            level, splash, owner != null ? owner : this, 3.60F + tier * 0.90F, false, true, false
                    );
                    if (splash.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(
                                level, owner != null ? owner : this, splash, 60, 0, null, null
                        );
                    }
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.6D), target.getZ(), 14, 0.40D, 0.35D, 0.40D, 0.04D);
                level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.85F, 0.72F);
            }
            case ESCHATON -> {
                // Section 6.57 Event-Horizon Leviathan (90s / 140 HP): sweeping singularity cleaves bypassing 75% armor,
                // shattering 5 armor points per strike, and pulling distant enemies directly into its crushing jaws!
                target.invulnerableTime = 0;
                Vec3 slashDir = target.position().subtract(this.position());
                Vec3 normDir = slashDir.horizontalDistanceSqr() > 1.0E-4D
                        ? new Vec3(slashDir.x, 0.0D, slashDir.z).normalize()
                        : this.getLookAngle();
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().add(normDir.scale(1.2D)), normDir, 5.6D,
                        EssenceType.ESCHATON, EssenceType.VOID, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D, 0.0D, 180L);
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(
                        level, target, owner != null ? owner : this, 7.20F + tier * 1.55F, false, true, false
                );
                for (LivingEntity splash : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0D))) {
                    if (splash == this || splash == owner || splash == target || !splash.isAlive() || this.isFriendlyTo(splash) || !(splash instanceof Monster)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(splash, level, 5.0D, 0.0D, 180L);
                    SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(
                            level, splash, owner != null ? owner : this, 4.80F + tier * 1.15F, false, true, false
                    );
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(14.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 diff = this.position().subtract(foe.position());
                    if (diff.lengthSqr() > 1.0E-4D) {
                        Vec3 pull = diff.normalize().scale(0.78D);
                        foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                        foe.hasImpulse = true;
                    }
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.7D), target.getZ(), 16, 0.45D, 0.35D, 0.45D, 0.05D);
                level.playSound(null, target.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.55F, 1.35F);
            }
            case APOTHEOSIS -> {
                // Section 6.58 Celestial Seraph-Titan (90s / 140 HP): sweeping divine cleaves (75% armor bypass, -5 armor/strike,
                // smite undead & dark entities with holy fire, and heal nearby friendly players for 25% of damage dealt)!
                Vec3 slashDir = target.position().subtract(this.position());
                Vec3 normDir = slashDir.horizontalDistanceSqr() > 1.0E-4D
                        ? new Vec3(slashDir.x, 0.0D, slashDir.z).normalize()
                        : this.getLookAngle();
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().add(normDir.scale(1.2D)), normDir, 5.6D,
                        EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D, 0.0D, 180L);
                target.igniteForSeconds(5);
                float totalDealt = SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, target, owner != null ? owner : this, 7.20F + tier * 1.55F, false, true, true, false
                );
                for (LivingEntity splash : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0D))) {
                    if (splash == this || splash == owner || splash == target || !splash.isAlive() || this.isFriendlyTo(splash) || !(splash instanceof Monster)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(splash, level, 5.0D, 0.0D, 180L);
                    splash.igniteForSeconds(5);
                    totalDealt += SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                            level, splash, owner != null ? owner : this, 4.80F + tier * 1.15F, false, true, true, false
                    );
                }
                float healAmt = Math.max(2.0F, totalDealt * 0.25F);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(14.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, healAmt);
                        ally.heal(healAmt);
                        level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                    }
                }
                level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.7D), target.getZ(), 18, 0.45D, 0.35D, 0.45D, 0.05D);
                level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.65F, 1.55F);
            }
            case ENTROPICA -> {
                // Section 6.59 Avatar of Entropica (90s / 140 HP): dual-elemental sweeping cleaves (75% armor bypass,
                // -5 armor points per strike, and pulls distant foes into its aura)!
                Vec3 look = this.getLookAngle();
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, this.getEyePosition().add(look.scale(1.2D)), look, 4.6D, EssenceType.ENTROPICA, EssenceType.ASTRAL, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D, 0.0D, 180L);
                SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                        level, target, owner != null ? owner : this, 6.80F + tier * 1.55F, false, true, false
                );
                for (LivingEntity splash : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0D))) {
                    if (splash == this || splash == owner || splash == target || !splash.isAlive() || this.isFriendlyTo(splash) || !(splash instanceof Monster)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(splash, level, 5.0D, 0.0D, 180L);
                    SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                            level, splash, owner != null ? owner : this, 5.00F + tier * 1.20F, false, true, false
                    );
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(14.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 diff = this.position().subtract(foe.position());
                    if (diff.lengthSqr() > 1.0E-4D) {
                        Vec3 pull = diff.normalize().scale(0.75D);
                        foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                        foe.hasImpulse = true;
                    }
                }
                level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.45D, 0.35D, 0.45D, 0.05D);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.45D, 0.35D, 0.45D, 0.05D);
                level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.65F, 1.52F);
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
            if (mat == EssenceType.AIR && (this.ageTicks == 1 || this.ageTicks % 15 == 0)) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (!isHostileTargetForSentryField(owner, foe)) continue;
                    if (SpellCombatReactionEngine.isBossEntity(level, foe)) continue;
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(foe.getDeltaMovement().x, 0.85D, foe.getDeltaMovement().z));
                    level.sendParticles(ParticleTypes.GUST, foe.getX(), foe.getY(0.2D), foe.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.02D);
                }
            } else if ((mat == EssenceType.WATER || mat == EssenceType.STORM) && this.ageTicks % 20 == 0) {
                // Storm Tempest-Squall Sentry extinguishes fires in a 6-block radius (Water in 4-block radius + farmland hydration)!
                int fireRad = (mat == EssenceType.STORM) ? 6 : 4;
                BlockPos c = this.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-fireRad, -1, -fireRad), c.offset(fireRad, 2, fireRad))) {
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof BaseFireBlock) {
                        level.removeBlock(p, false);
                    } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                        level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                    } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                        level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
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
            } else if ((mat == EssenceType.OVERGROWTH || mat == EssenceType.SPORE) && this.ageTicks % 30 == 0) {
                int r = 6;
                BlockPos c = this.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-r, -1, -r), c.offset(r, 1, r))) {
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                        level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                    } else if (bs.getBlock() instanceof BonemealableBlock b && b.isValidBonemealTarget(level, p, bs)
                            && level.random.nextFloat() < (mat == EssenceType.SPORE ? 0.36F : 0.30F)) {
                        b.performBonemeal(level, level.random, p.immutable(), bs);
                        if (mat == EssenceType.SPORE) {
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.25D, 0.25D, 0.25D, 0.02D);
                        }
                    } else if (mat == EssenceType.SPORE && (bs.is(Blocks.MYCELIUM) || bs.is(Blocks.PODZOL))
                            && level.getBlockState(p.above()).isAir() && level.random.nextFloat() < 0.08F) {
                        level.setBlockAndUpdate(p.above(), level.random.nextBoolean()
                                ? Blocks.RED_MUSHROOM.defaultBlockState() : Blocks.BROWN_MUSHROOM.defaultBlockState());
                    }
                }
                if (mat == EssenceType.OVERGROWTH) {
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x2CE668, 28, 1.0F, 0.06F),
                            this.getX(), this.getY(0.9D), this.getZ(), 10, 2.4D, 0.45D, 2.4D, 0.02D
                    );
                } else {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.05D, 0.0D), 5.5D, EssenceType.SPORE, EssenceType.AIR, 28, tier
                    );
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x1A5923, 28, 0.95F, 0.06F),
                            this.getX(), this.getY(1.0D), this.getZ(), 10, 2.4D, 0.5D, 2.4D, 0.02D
                    );
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xA88654, 28, 0.95F, 0.06F),
                            this.getX(), this.getY(1.15D), this.getZ(), 10, 2.4D, 0.5D, 2.4D, 0.02D
                    );
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(1.0D), this.getZ(),
                            10, 2.2D, 0.5D, 2.2D, 0.02D);
                }
            } else if (mat == EssenceType.RADIANT) {
                // Section 6.8 Solar Seraph Sentry: 4 orbiting mirror-shields reflect incoming hostile projectiles within a 7.5-block sphere!
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(7.5D))) {
                    if (!proj.isAlive() || !(this.distanceToSqr(proj) <= 56.25D) || proj.getDeltaMovement().lengthSqr() < 0.02D) continue;
                    Entity pOwner = proj.getOwner();
                    if (pOwner == owner || pOwner == this) continue;
                    if (pOwner instanceof LivingEntity livOwner && this.isFriendlyTo(livOwner)) continue;
                    CompoundTag pTag = ddraig.net.entropica.util.EntityHelper.getPersistentData(proj);
                    if (pTag.getBoolean("SpellRadiantMirrorReflected").orElse(false)) continue;
                    pTag.putBoolean("SpellRadiantMirrorReflected", true);

                    double spd = Math.max(1.45D, proj.getDeltaMovement().length() * 1.30D);
                    Vec3 rev = (pOwner instanceof LivingEntity shooter && shooter.isAlive())
                            ? shooter.getEyePosition().subtract(proj.position()).normalize().scale(spd)
                            : proj.getDeltaMovement().normalize().scale(-spd);
                    proj.setDeltaMovement(rev);
                    proj.setYRot((float) (Math.atan2(rev.x, rev.z) * (180.0D / Math.PI)));
                    proj.setXRot((float) (Math.atan2(rev.y, Math.hypot(rev.x, rev.z)) * (180.0D / Math.PI)));
                    proj.setOwner(owner != null ? owner : this);
                    proj.hasImpulse = true;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                            level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                            proj.position(), rev.normalize(), EssenceType.RADIANT, null, 1.40F, 50, tier
                    );
                    level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 10, 0.16D, 0.16D, 0.16D, 0.06D);
                    level.playSound(null, proj.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.55F);
                }
            } else if (mat == EssenceType.UMBRAL && this.ageTicks % 20 == 0) {
                // Section 6.9 Shadow Stalker Sentry: silent 16-block spawn-suppressing eclipse ward pulse!
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x211A21, 32, 1.35F, 0.16F),
                        this.getX(), this.getY(0.95D), this.getZ(), 6, 0.85D, 0.45D, 0.85D, 0.015D);
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                            level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_ARCANE_SEAL,
                            this.position().add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.UMBRAL, null, 2.6F, 44, tier
                    );
                }
            } else if (mat == EssenceType.UNDEAD && this.ageTicks % 15 == 0) {
                // Section 6.10 Cryptguard Ossuary Totem: desiccated tombstone green-grey (#4D6453) with necrotic soul particles!
                level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(1.05D), this.getZ(), 3, 0.45D, 0.50D, 0.45D, 0.02D);
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0x5EE088, 28, 0.90F, 0.08F),
                        this.getX(), this.getY(1.10D), this.getZ(), 4, 0.55D, 0.45D, 0.55D, 0.015D);
            } else if (mat == EssenceType.VOID && this.ageTicks % 20 == 0) {
                // Section 6.11 Void Gravity Pylon Sentry (#41304E): emits periodic inward gravitational pulses pulling distant enemies!
                boolean pulledAny = false;
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    boolean hostile = foe instanceof Enemy
                            || (owner != null && (foe == owner.getLastHurtMob() || foe == owner.getLastHurtByMob()));
                    if (!hostile || SpellCombatReactionEngine.isBossEntity(level, foe)) continue;
                    Vec3 toPylon = this.position().subtract(foe.position());
                    if (toPylon.lengthSqr() > 1.44D) {
                        Vec3 inward = toPylon.normalize().scale(0.56D);
                        SpellExecutionEngine.applySyncedMotion(foe, foe.getDeltaMovement().add(inward.x, 0.16D, inward.z));
                        pulledAny = true;
                    }
                }
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), 0x41304E, 26, 1.05F, 0.10F),
                        this.getX(), this.getY(1.05D), this.getZ(), 5, 0.55D, 0.45D, 0.55D, 0.015D);
                if (pulledAny || this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                            level, this.position(), 3.6D, true, EssenceType.VOID, null, 22, tier
                    );
                }
            } else if (mat == EssenceType.ARID && this.ageTicks % 15 == 0) {
                // Section 6.12 Dune-Dervish Sandstone Obelisk Sentry (#C2B28F): swirling desert dust whirl particles!
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                        this.getX(), this.getY(1.05D), this.getZ(), 5, 0.45D, 0.50D, 0.45D, 0.03D);
            } else if (mat == EssenceType.TAIGA && this.ageTicks % 20 == 0) {
                // Section 6.20 Frost-Howler Spire (#1A5923 / #98DCF2): pulses an aura increasing nearby friendly attack speed (+25%) & cold resistance!
                long until = level.getGameTime() + 45L;
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.05D, 0.0D), 6.0D, EssenceType.TAIGA, EssenceType.FROZEN, 44, tier
                );
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 24, 0.90F, 0.05F),
                        this.getX(), this.getY(0.95D), this.getZ(), 6, 0.85D, 0.45D, 0.85D, 0.02D);
                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        this.getX(), this.getY(1.05D), this.getZ(), 6, 0.85D, 0.45D, 0.85D, 0.02D);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ally.addEffect(new MobEffectInstance(MobEffects.HASTE, 45, 0, true, true));
                        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(ally, level, 0.25D, 45L);
                        ally.setTicksFrozen(0);
                        ally.isInPowderSnow = false;
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL, until);
                    }
                }
            } else if (mat == EssenceType.OASIS && this.ageTicks % 20 == 0) {
                // Section 6.21 Aquifer Spring Spire (#1A5923 / #C2B28F): pulses area healing, farmland hydration, and crop/cactus/sugarcane growth ticks!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.05D, 0.0D), 6.0D, EssenceType.OASIS, EssenceType.ARID, 44, tier
                );
                level.sendParticles(ParticleTypes.SPLASH,
                        this.getX(), this.getY(1.05D), this.getZ(), 8, 0.65D, 0.35D, 0.65D, 0.04D);
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        this.getX(), this.getY(0.85D), this.getZ(), 4, 0.55D, 0.30D, 0.55D, 0.02D);
                float pulseHeal = 1.5F + tier * 0.45F;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ally.clearFire();
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, pulseHeal);
                        ally.heal(pulseHeal);
                    }
                }
                ddraig.net.entropica.spell.mutation.element.OasisSpellMutationHandler
                        .hydrateAndGrowFloraPublic(level, this.blockPosition(), 6);
            } else if (mat == EssenceType.VITAE) {
                if (this.ageTicks % 6 == 0) {
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), 0xFF6B9D, 20, 0.88F, 0.05F),
                            this.getX(), this.getY(0.85D), this.getZ(), 2, 0.30D, 0.35D, 0.30D, 0.02D);
                }
                if (this.ageTicks % 30 == 0) {
                    // Section 6.22 Cardiovascular Spire: audible heartbeat healing allies & curing debuffs in 6m!
                    level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.65F, 1.12F);
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 6.0D, EssenceType.VITAE, EssenceType.NATURE, 32, tier
                    );
                    float pulseHeal = 2.4F + tier * 0.50F;
                    for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                        if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, pulseHeal);
                            ally.heal(pulseHeal);
                            level.sendParticles(ParticleTypes.HEART,
                                    ally.getX(), ally.getY(0.95D), ally.getZ(), 2, 0.25D, 0.25D, 0.25D, 0.02D);
                        }
                    }
                }
            } else if (mat == EssenceType.BLOOD) {
                // Section 6.23 Exsanguination Pylon (#8A0303): dark red droplets & crimson vapor aura,
                // tethering up to 2 nearby hostiles to drain health and transfer it to the nearest friendly player!
                if (this.ageTicks % 6 == 0) {
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 20, 0.88F, 0.04F),
                            this.getX(), this.getY(1.10D), this.getZ(), 2, 0.28D, 0.35D, 0.28D, 0.015D);
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x8A0303, 24, 0.85F, 0.06F),
                            this.getX(), this.getY(0.45D), this.getZ(), 2, 0.32D, 0.28D, 0.32D, 0.01D);
                }
                if (this.ageTicks % 20 == 0) {
                    int drainedCount = 0;
                    float totalDrained = 0.0F;
                    float drainPerTarget = Math.max(2.0F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.22F)
                            * Math.max(0.5F, this.apparatusPotency);
                    Vec3 pylonCore = this.position().add(0.0D, 1.18D, 0.0D);
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                        if (!(foe instanceof Enemy) && !(foe instanceof Mob mob && mob.getTarget() != null)) continue;

                        float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(
                                level, foe, owner != null ? owner : this, drainPerTarget, false, true
                        );
                        totalDrained += (dealt > 0.0F ? dealt : drainPerTarget * 0.80F);
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 45, 0));
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                                .putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, level.getGameTime() + 45L);

                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChanneledBeamSlice(
                                level, pylonCore, foe.position().add(0.0D, foe.getBbHeight() * 0.58D, 0.0D),
                                EssenceType.BLOOD, EssenceType.VITAE, tier, 22
                        );
                        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 20, 0.95F, 0.05F),
                                foe.getX(), foe.getY(0.75D), foe.getZ(), 6, 0.25D, 0.28D, 0.25D, 0.04D);
                        if (++drainedCount >= 2) break;
                    }
                    if (totalDrained > 0.0F) {
                        Player recipient = null;
                        double bestDistSq = Double.MAX_VALUE;
                        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(14.0D), LivingEntity::isAlive)) {
                            if (p == owner || this.isFriendlyTo(p)) {
                                double dSq = p.distanceToSqr(this);
                                if (dSq < bestDistSq) {
                                    bestDistSq = dSq;
                                    recipient = p;
                                }
                            }
                        }
                        if (recipient == null && owner instanceof Player p && p.isAlive()) {
                            recipient = p;
                        }
                        if (recipient != null) {
                            SpellCombatReactionEngine.notifyMagicalHeal(recipient, totalDrained);
                            recipient.heal(totalDrained);
                            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChanneledBeamSlice(
                                    level, pylonCore, recipient.position().add(0.0D, recipient.getBbHeight() * 0.60D, 0.0D),
                                    EssenceType.VITAE, EssenceType.BLOOD, tier, 22
                            );
                            level.sendParticles(ParticleTypes.HEART,
                                    recipient.getX(), recipient.getY(0.95D), recipient.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                        }
                    }
                }
            } else if (mat == EssenceType.AURA) {
                // Section 6.24 Chakra Respiration Spire (#FF6B9D / #A88654): golden-pink spirit mist & floating pranic motes,
                // pulsing an 8-block aura granting allies a 20% Materia fuel discount & Slow Falling!
                if (this.ageTicks % 6 == 0) {
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0xFF6B9D, 22, 0.90F, 0.05F),
                            this.getX(), this.getY(0.90D), this.getZ(), 2, 0.30D, 0.35D, 0.30D, 0.01D);
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0xA88654, 24, 0.88F, 0.04F),
                            this.getX(), this.getY(1.25D), this.getZ(), 2, 0.32D, 0.35D, 0.32D, 0.015D);
                }
                if (this.ageTicks % 20 == 0) {
                    long until = level.getGameTime() + 35L;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.AURA, null, 24, tier
                    );
                    for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                            ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0, true, false));
                            ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                    .putLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL, until);
                        }
                    }
                }
            } else if (mat == EssenceType.AMBER) {
                // Section 6.25 Resinite Stasis Spire (#FF6B9D / #773214): golden resin droplets & amber crystals,
                // pulsing an 8m aura providing +4 Armor to allies and item despawn/fire prevention!
                if (this.ageTicks % 6 == 0) {
                    level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                            this.getX(), this.getY(1.00D), this.getZ(), 1, 0.28D, 0.35D, 0.28D, 0.0D);
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), 0xFF6B9D, 22, 0.88F, 0.04F),
                            this.getX(), this.getY(1.20D), this.getZ(), 2, 0.30D, 0.35D, 0.30D, 0.015D);
                }
                if (this.ageTicks % 20 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.AMBER, null, 24, tier
                    );
                    AABB box = this.getBoundingBox().inflate(8.0D);
                    for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                        if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 4.0D, 0.0D, 0.0D, 0.0D, 35L);
                        }
                    }
                    for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
                        if (item.isAlive()) {
                            item.clearFire();
                            item.setUnlimitedLifetime();
                            item.setInvulnerable(true);
                        }
                    }
                }
            } else if (mat == EssenceType.ECLIPSE) {
                // Section 6.26 Black Sun Corona Spire (#FFEA78 / #211A21): black solar flares & golden coronal motes,
                // pulsing a blinding twilight aura while firing buff-stripping shadow bolts!
                if (this.ageTicks % 6 == 0) {
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x211A21, 22, 0.90F, 0.05F),
                            this.getX(), this.getY(1.05D), this.getZ(), 2, 0.30D, 0.35D, 0.30D, 0.01D);
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), 0xFFEA78, 22, 0.88F, 0.04F),
                            this.getX(), this.getY(1.25D), this.getZ(), 2, 0.32D, 0.35D, 0.32D, 0.015D);
                }
                if (this.ageTicks % 20 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.ECLIPSE, null, 24, tier
                    );
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                        foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                    }
                }
            } else if (mat == EssenceType.BLIGHT) {
                // Section 6.27 Necro-Briar Spire (#4D6453 / #34CC48): pulsing an 8m 30% slow aura & firing piercing skeletal thorn darts!
                if (this.ageTicks % 6 == 0) {
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x34CC48, 22, 0.90F, 0.05F),
                            this.getX(), this.getY(1.05D), this.getZ(), 2, 0.30D, 0.35D, 0.30D, 0.01D);
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), 0x4D6453, 22, 0.88F, 0.04F),
                            this.getX(), this.getY(1.25D), this.getZ(), 2, 0.30D, 0.35D, 0.30D, 0.01D);
                }
                if (this.ageTicks % 20 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.BLIGHT, null, 24, tier
                    );
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.30D, 35L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                    }
                }
            } else if (mat == EssenceType.ASTRAL && this.ageTicks % 20 == 0) {
                // Pulsar Singularity Spire: 8m pulling gravitational aura + redstone power pulse!
                SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, this.blockPosition(), 5, 30);
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                            level, this.position().add(0.0D, 1.15D, 0.0D), 8.0D, true, EssenceType.ASTRAL, EssenceType.LIGHTNING, 40, tier
                    );
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                    if (!(foe instanceof Monster) && !(foe instanceof net.minecraft.world.entity.Mob m && (m.getTarget() == owner || m.getTarget() == this))) continue;
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(0.42D);
                    foe.setDeltaMovement(foe.getDeltaMovement().add(inward.x, 0.12D, inward.z));
                    foe.hasImpulse = true;
                }
            } else if (mat == EssenceType.SOULFIRE && this.ageTicks % 10 == 0) {
                // Phantasmal Pyre Spire: 8m projectile-burning aura!
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(8.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                    if (proj.getOwner() instanceof LivingEntity pOwner && this.isFriendlyTo(pOwner)) continue;
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, proj.getX(), proj.getY(), proj.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.03D);
                    level.sendParticles(ParticleTypes.SOUL, proj.getX(), proj.getY(), proj.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.02D);
                    proj.discard();
                }
            } else if (mat == EssenceType.VAPOR && this.ageTicks % 20 == 0) {
                // Steam-Vent Obelisk: continuous blinding smoke ring slowing enemies in 8m!
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(0.8D), this.getZ(), 18, 3.2D, 0.4D, 3.2D, 0.02D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                    if (!(foe instanceof Monster) && !(foe instanceof net.minecraft.world.entity.Mob m && (m.getTarget() == owner || m.getTarget() == this))) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
                    SpellLingeringZoneManager.applySoaked(foe, 60);
                }
            } else if ((mat == EssenceType.NULL_R || mat == EssenceType.NULL_U || mat == EssenceType.ABYSS) && this.ageTicks % 20 == 0) {
                // Event Horizon / Oblivion / Trench Spire: 8m pulling / whirlpool + blinding/silencing aura!
                if ((mat == EssenceType.NULL_R || mat == EssenceType.NULL_U || mat == EssenceType.ABYSS) && this.ageTicks % 40 == 0) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    if (mat == EssenceType.ABYSS) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                                level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.ABYSS, EssenceType.WATER, 40, sTier
                        );
                    } else {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                                level, this.position().add(0.0D, 1.15D, 0.0D), 8.0D, true,
                                mat, mat == EssenceType.NULL_R ? EssenceType.VOID : EssenceType.UMBRAL, 40, sTier
                        );
                    }
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                    if (!(foe instanceof Monster) && !(foe instanceof net.minecraft.world.entity.Mob m && (m.getTarget() == owner || m.getTarget() == this))) continue;
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
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                        level.sendParticles(ParticleTypes.END_ROD, foe.getX(), foe.getY(0.75D), foe.getZ(), 3, 0.22D, 0.25D, 0.22D, 0.02D);
                    } else if (mat == EssenceType.NULL_U) {
                        SpellLingeringZoneManager.applyArcaneSilence(foe, 40);
                    } else {
                        SpellLingeringZoneManager.applySoaked(foe, 60);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                    }
                }
            } else if (mat == EssenceType.DAWN && this.ageTicks % 20 == 0) {
                // Sol-Zephyr Spire: 8m aura granting allies Slow Falling and Speed!
                if (this.ageTicks % 40 == 0) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.DAWN, EssenceType.AIR, 40, sTier
                    );
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ally.removeEffect(MobEffects.BLINDNESS);
                        ally.removeEffect(MobEffects.DARKNESS);
                        ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 45, 0, true, false));
                        ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 45, 0, true, false));
                    }
                }
            } else if (mat == EssenceType.AEGIS && this.ageTicks % 20 == 0) {
                // Citadel Fortress Spire: +6 Armor aura to nearby allies!
                if (this.ageTicks % 40 == 0) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.AEGIS, EssenceType.RADIANT, 40, sTier
                    );
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 0.0D, 0.0D, 0.0D, 35L);
                    }
                }
            } else if (mat == EssenceType.AURORA && this.ageTicks % 10 == 0) {
                // Prismatic Aurora Spire: 8m aura granting allies Frost Walker & deflecting hostile projectiles!
                if (this.ageTicks % 40 == 0) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.AURORA, EssenceType.FROZEN, 40, sTier
                    );
                }
                long fwUntil = level.getGameTime() + 35L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, fwUntil);
                    }
                }
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(8.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                    if (proj.getOwner() instanceof LivingEntity pOwner && this.isFriendlyTo(pOwner)) continue;
                    proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.1D));
                    proj.setOwner(owner != null ? owner : this);
                    proj.hasImpulse = true;
                }
            } else if (mat == EssenceType.MIRAGE && this.ageTicks % 20 == 0) {
                // Fata Morgana Obelisk: 12m false player illusions drawing all mob attacks away!
                if (this.ageTicks % 40 == 0) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 12.0D, EssenceType.MIRAGE, EssenceType.ARID, 40, sTier
                    );
                }
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
                // Section 6.38 Banshee Wail Obelisk: 8m shriek silencing abilities & slowing mobs 40%!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.WRAITH, EssenceType.UNDEAD, 24, tier
                );
                level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(1.2D), this.getZ(), 10, 2.2D, 0.4D, 2.2D, 0.03D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 45);
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.40D, 45L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
                }
            } else if (mat == EssenceType.BARROW && this.ageTicks % 20 == 0) {
                // Section 6.39 Cairn Beacon: 8m burial dust slowing enemies 40% & granting allied undead/summons +4 Armor!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.BARROW, EssenceType.UNDEAD, 24, tier
                );
                level.sendParticles(ParticleTypes.ASH, this.getX(), this.getY(1.0D), this.getZ(), 14, 2.4D, 0.4D, 2.4D, 0.02D);
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if ((ent instanceof SpellEidolonEntity || ent.getType().is(EntityTypeTags.UNDEAD))
                            && (ent == owner || this.isFriendlyTo(ent))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 4.0D, 0.0D, 0.0D, 0.0D, 35L);
                    } else if (ent instanceof Monster && !this.isFriendlyTo(ent) && ent != owner) {
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(ent, level, 0.40D, 45L);
                        ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
                    }
                }
            } else if (mat == EssenceType.PYRE && this.ageTicks % 20 == 0) {
                // Section 6.40 Cremation Pyre Spire: +4 Armor to nearby summons & continuous 8m wildfire jets!
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.05D, 0.0D), 8.0D, EssenceType.PYRE, EssenceType.NETHER, 44, tier
                    );
                }
                level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.9D), this.getZ(), 18, 2.8D, 0.5D, 2.8D, 0.04D);
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if ((ent instanceof SpellEidolonEntity || ent == owner || this.isFriendlyTo(ent)) && !(ent instanceof Monster)) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 4.0D, 0.0D, 0.0D, 0.0D, 35L);
                    } else if (ent instanceof Monster && !this.isFriendlyTo(ent) && ent != owner) {
                        ent.igniteForSeconds(4);
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ent).putLong(
                                SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 80L
                        );
                        SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(
                                level, ent, owner != null ? owner : this, 2.05F + tier * 0.55F, false, true
                        );
                    }
                }
            } else if (mat == EssenceType.PENUMBRA && this.ageTicks % 5 == 0) {
                // Section 6.41 Twilight Corona Citadel: +6 Armor, +3 Toughness, complete debuff immunity; 8m repulsion pulse every 4.0s (80t)!
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.05D, 0.0D), 8.0D, EssenceType.PENUMBRA, EssenceType.RADIANT, 44, tier
                    );
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        if (this.ageTicks % 20 == 0) {
                            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                        }
                        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
                    }
                }
                if (this.ageTicks % 80 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, this.position(), 8.0D, EssenceType.PENUMBRA, EssenceType.RADIANT, tier
                    );
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.18D);
                        foe.setDeltaMovement(away.x, 0.34D, away.z);
                        foe.hasImpulse = true;
                        SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                                level, foe, owner != null ? owner : this, 2.00F + tier * 0.50F, false, true
                        );
                    }
                }
            } else if (mat == EssenceType.RIME && this.ageTicks % 20 == 0) {
                // Section 6.42 Hoarfrost Glacial Citadel Spire: +6 Armor, +3 Toughness, complete Fire & Freezing immunity; 8m root burst every 4.0s!
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.05D, 0.0D), 8.0D, EssenceType.RIME, EssenceType.FROZEN, 44, tier
                    );
                }
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
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, this.position(), 8.0D, EssenceType.RIME, EssenceType.FROZEN, tier
                    );
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                        foe.setTicksFrozen(foe.getTicksFrozen() + 50);
                        SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(
                                level, foe, owner != null ? owner : this, 1.95F + tier * 0.50F, false, true
                        );
                    }
                }
            } else if (mat == EssenceType.SPRING && this.ageTicks % 20 == 0) {
                // Section 6.43 Aquifer Springwater Font Spire: +6 Armor, +3 Toughness, permanent Water Breathing;
                // 8m restorative surge every 4.0s (80t) healing allies for 3 hearts (6.0 HP) & clearing debuffs!
                if (this.ageTicks % 60 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.03D, 0.0D), 8.0D, EssenceType.SPRING, EssenceType.WATER, 62, tier
                    );
                }
                if (this.ageTicks % 80 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, this.position(), 8.0D, EssenceType.SPRING, EssenceType.WATER, tier
                    );
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                        ally.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 45, 0, true, false));
                        ally.setAirSupply(ally.getMaxAirSupply());
                        if (this.ageTicks % 80 == 0) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, 6.0F);
                            ally.heal(6.0F);
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
                            level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.03D);
                        }
                    }
                }
            } else if (mat == EssenceType.STATIC && this.ageTicks % 20 == 0) {
                // Dielectric Tesla Citadel Spire: +6 Armor, +3 Toughness, Lightning/Fall immunity; 8m EMP every 4.0s!
                long until = level.getGameTime() + 35L;
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position(), 8.0D, EssenceType.STATIC, EssenceType.LIGHTNING, 45, tier
                    );
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                        ally.resetFallDistance();
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_STATIC_SENTRY_IMMUNE_UNTIL, until);
                    }
                }
                if (this.ageTicks % 80 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, this.position(), 8.0D, EssenceType.STATIC, EssenceType.ARID, tier
                    );
                    level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.55F, 1.55F);
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        if (foe instanceof net.minecraft.world.entity.monster.Creeper creeper) {
                            creeper.setSwellDir(-1);
                            SpellLingeringZoneManager.applyArcaneSilence(creeper, 100);
                        }
                        Vec3 delta = foe.position().subtract(this.position());
                        Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.15D) : new Vec3(0.0D, 0.32D, 0.0D);
                        foe.setDeltaMovement(away.x, 0.32D, away.z);
                        foe.hasImpulse = true;
                        SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(
                                level, foe, owner != null ? owner : this, 2.35F + tier * 0.55F, false, true
                        );
                    }
                }
            } else if (mat == EssenceType.SYLVAN && this.ageTicks % 20 == 0) {
                // Petrified Monolith Sentry: +6 Armor, +3 Toughness, KB Resistance; 8m root wave every 4.0s!
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position(), 8.0D, EssenceType.SYLVAN, EssenceType.RADIANT, 45, tier
                    );
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.65D, 0.0D, 35L);
                    }
                }
                if (this.ageTicks % 80 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, this.position(), 8.0D, EssenceType.SYLVAN, EssenceType.EARTH, tier
                    );
                    level.playSound(null, this.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 0.78F, 0.75F);
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                        float smite = foe.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                        SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                                level, foe, owner != null ? owner : this, (2.25F + tier * 0.55F) * smite, false, true
                        );
                    }
                }
            } else if (mat == EssenceType.MIASMA && this.ageTicks % 60 == 0) {
                // Section 6.46 Cadaverous Geyser Pylon: boiling swamp acid every 3.0s in 6m launching airborne, -2 armor, buff purge & Miasma Materia damage!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, this.position(), 6.0D, EssenceType.MIASMA, EssenceType.WATER, tier
                );
                level.sendParticles(ParticleTypes.SNEEZE, this.getX(), this.getY(0.6D), this.getZ(), 24, 2.2D, 0.8D, 2.2D, 0.05D);
                level.playSound(null, this.blockPosition(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.PLAYERS, 0.85F, 0.72F);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    foe.setDeltaMovement(foe.getDeltaMovement().x, 0.82D, foe.getDeltaMovement().z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    for (MobEffectInstance eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                        if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                    }
                    SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 3.0F + tier * 0.85F, false, true
                    );
                }
            } else if (mat == EssenceType.GENESIS && this.ageTicks % 20 == 0) {
                // Section 6.47 Dawn-Glory Spire: +6 Armor, +3 Toughness to adjacent allies + continuous golden water beams at up to 3 hostiles up to 16m (-2 armor/hit)!
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 6.0D, 3.0D, 0.0D, 0.0D, 35L);
                    }
                }
                Vec3 spireMuzzle = this.position().add(0.0D, 1.78D, 0.0D);
                int fired = 0;
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBeam(
                            level, spireMuzzle, foe.getEyePosition().subtract(0.0D, 0.25D, 0.0D),
                            EssenceType.GENESIS, EssenceType.RADIANT, tier
                    );
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(foe) ? 1.50F : 1.0F;
                    SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                            level, foe, owner != null ? owner : this, (2.60F + tier * 0.72F) * smite, false, true
                    );
                    if (++fired >= 3) break;
                }
            } else if (mat == EssenceType.OBLIVION && this.ageTicks % 20 == 0) {
                // Section 6.48 Pyre of Nonexistence Obelisk: 8m Extinction Aura melting 1 armor/s (up to -8) & burning 1 heart/s (2.0 HP)!
                long now = level.getGameTime();
                if (this.ageTicks % 60 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, this.position(), 8.0D, EssenceType.OBLIVION, EssenceType.NETHER, tier
                    );
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    long lastTick = fData.getLong("SpellExtinctionAuraLastTick").orElse(0L);
                    int stacks = (now - lastTick <= 35L) ? Math.min(8, fData.getInt("SpellExtinctionAuraStacks").orElse(0) + 1) : 1;
                    fData.putLong("SpellExtinctionAuraLastTick", now);
                    fData.putInt("SpellExtinctionAuraStacks", stacks);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, stacks, 0.0D, 45L);
                    fData.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 35L);
                    foe.igniteForSeconds(2);
                    SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 2.0F, false, true
                    );
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
                int sTier = this.spell != null ? Math.max(1, this.spell.getEffectiveTier()) : 6;
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position(), 10.0D, EssenceType.AETHER, EssenceType.WATER, 22, sTier
                    );
                }
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if (ent == owner || this.isFriendlyTo(ent)) {
                        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                    } else if (ent instanceof Monster || (owner != null && ent == owner.getLastHurtMob())) {
                        ent.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 28, 0, true, true));
                        ent.setDeltaMovement(ent.getDeltaMovement().x, Math.max(ent.getDeltaMovement().y, 0.16D), ent.getDeltaMovement().z);
                        ent.hasImpulse = true;
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
                int sTier = this.spell != null ? Math.max(1, this.spell.getEffectiveTier()) : 6;
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position(), 10.0D, EssenceType.ENTROPIC, EssenceType.BLOOD, 22, sTier
                    );
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                    if (!(foe instanceof Monster) && (owner == null || foe != owner.getLastHurtMob())) continue;
                    SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(level, foe, owner != null ? owner : this, 3.0F, false, true);
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
                int sTier = this.spell != null ? Math.max(1, this.spell.getEffectiveTier()) : 6;
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position(), 10.0D, EssenceType.CELESTIAL, EssenceType.VITAE, 22, sTier
                    );
                }
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if (ent == owner || this.isFriendlyTo(ent)) {
                        if (ent.distanceToSqr(this) <= 36.0D) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ent, 3.5F);
                            ent.heal(3.5F);
                        }
                    } else if (ent instanceof Monster || (owner != null && ent == owner.getLastHurtMob())) {
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
                            SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(
                                    level, ent, owner != null ? owner : this, 3.0F, false, true
                            );
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
                // Section 6.54 Heartwood Canopy Obelisk: 16m Sanctuary Field granting +8 Armor, +4 Toughness, and continuous absorption up to 10.0 HP!
                int sTier = this.spell != null ? Math.max(1, this.spell.getEffectiveTier()) : 8;
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 16.0D, EssenceType.EMPYREAN, EssenceType.VITAE, 24, sTier
                    );
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 8.0D, 4.0D, 0.0D, 0.0D, 35L);
                        if (ally.getAbsorptionAmount() < 10.0F) {
                            ally.setAbsorptionAmount(Math.min(10.0F, ally.getAbsorptionAmount() + 2.5F));
                        }
                    }
                }
            } else if (mat == EssenceType.CATACLYSM && this.ageTicks % 20 == 0) {
                // Section 6.55 Volcanic Eruption Spire: 16m Gravitational Caldera Field dragging hostiles inward, melting 1 armor/s, & burning 1.5 hearts/s (3.0 HP)!
                int sTier = this.spell != null ? Math.max(1, this.spell.getEffectiveTier()) : 8;
                if (this.ageTicks % 40 == 0) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.04D, 0.0D), 16.0D, EssenceType.CATACLYSM, EssenceType.NETHER, 24, sTier
                    );
                }
                long now = level.getGameTime();
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 delta = this.position().subtract(foe.position());
                    if (delta.lengthSqr() > 1.0E-4D) {
                        Vec3 inward = delta.normalize().scale(0.45D);
                        foe.setDeltaMovement(foe.getDeltaMovement().add(inward.x, 0.08D, inward.z));
                        foe.hasImpulse = true;
                    }
                    foe.igniteForSeconds(4);
                    var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    fData.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 50L);
                    long last = fData.getLong("SpellCataclysmSentryLastTick").orElse(0L);
                    int stacks = (now - last <= 35L) ? Math.min(10, fData.getInt("SpellCataclysmSentryStacks").orElse(0) + 1) : 1;
                    fData.putLong("SpellCataclysmSentryLastTick", now);
                    fData.putInt("SpellCataclysmSentryStacks", stacks);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, stacks, 0.0D, 45L);
                    SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 3.0F, false, false, true
                    );
                }
            } else if (mat == EssenceType.TERMINUS && this.ageTicks % 20 == 0) {
                // Section 6.56 Anchored Permafrost Elemental Spire (120 HP / 60s): 16m Polar Freeze Field cutting hostile speed
                // by exact 50% and continuously freezing water across the 16-block perimeter!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.06D, 0.0D), 16.0D, EssenceType.TERMINUS, EssenceType.FROZEN,
                        24, Math.max(1, this.spell.getEffectiveTier())
                );
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.50D, 35L);
                    foe.clearFire();
                    foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 50, foe.getTicksFrozen() + 30));
                }
                BlockPos c = this.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-16, -1, -16), c.offset(16, 1, 16))) {
                    if (p.distSqr(c) > 256.0D) continue;
                    if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir() && level.random.nextFloat() < 0.35F) {
                        level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            } else if (mat == EssenceType.ESCHATON && this.ageTicks % 20 == 0) {
                // Section 6.57 Accretion Disk Singularity Spire (90s / 150 HP): 20m Gravitational Accretion Field dragging hostiles & projectiles inward, melting 1.5 armor/s!
                long now = level.getGameTime();
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.04D, 0.0D), 20.0D,
                        EssenceType.ESCHATON, EssenceType.VOID, 26, Math.max(1, this.spell.getEffectiveTier())
                );
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(20.0D))) {
                    if (proj.getOwner() == owner || proj.getOwner() == this || proj instanceof SpellProjectileEntity) continue;
                    Vec3 diff = this.position().add(0.0D, 1.0D, 0.0D).subtract(proj.position());
                    if (diff.lengthSqr() > 1.0E-4D) {
                        Vec3 pull = diff.normalize().scale(0.65D);
                        proj.setDeltaMovement(proj.getDeltaMovement().scale(0.35D).add(pull));
                        proj.hasImpulse = true;
                    }
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 diff = this.position().subtract(foe.position());
                    if (diff.lengthSqr() > 1.0E-4D) {
                        Vec3 inward = diff.normalize().scale(0.50D);
                        foe.setDeltaMovement(foe.getDeltaMovement().add(inward.x, 0.08D, inward.z));
                        foe.hasImpulse = true;
                    }
                    var fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    long last = fData.getLong("SpellEschatonSentryLastTick").orElse(0L);
                    double stacks = (now - last <= 35L) ? Math.min(12.0D, fData.getDouble("SpellEschatonSentryStacks").orElse(0.0D) + 1.5D) : 1.5D;
                    fData.putLong("SpellEschatonSentryLastTick", now);
                    fData.putDouble("SpellEschatonSentryStacks", stacks);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, stacks, 0.0D, 45L);
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(1.2D), this.getZ(), 18, 2.5D, 0.8D, 2.5D, 0.05D);
            } else if (mat == EssenceType.APOTHEOSIS && this.ageTicks % 20 == 0) {
                // Section 6.58 White-Hole Dawn Obelisk (90s / 150 HP): 20m Sanctuary Field granting allies +10 Armor, +6 Toughness, and continuous absorption up to 15.0 HP!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.04D, 0.0D), 20.0D,
                        EssenceType.APOTHEOSIS, EssenceType.RADIANT, 26, Math.max(1, this.spell.getEffectiveTier())
                );
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 10.0D, 6.0D, 0.0D, 0.0D, 35L);
                        if (ally.getAbsorptionAmount() < 15.0F) {
                            ally.setAbsorptionAmount(Math.min(15.0F, ally.getAbsorptionAmount() + 5.0F));
                        }
                    }
                }
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(1.2D), this.getZ(), 18, 2.5D, 0.8D, 2.5D, 0.05D);
            } else if (mat == EssenceType.ENTROPICA && this.ageTicks % 20 == 0) {
                // Section 6.59 Altar of the Horizon (90s / 150 HP): 20m Equilibrium Field dealing 2 hearts/s (4.0 HP)
                // true damage to hostiles AND pulsing +2 hearts/s (4.0 HP) continuous healing to allies!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, this.position().add(0.0D, 0.04D, 0.0D), 20.0D,
                        EssenceType.ENTROPICA, EssenceType.ASTRAL, 26, Math.max(1, this.spell.getEffectiveTier())
                );
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0D))) {
                    if (ent == this || !ent.isAlive()) continue;
                    if (ent == owner || this.isFriendlyTo(ent)) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                        ent.heal(4.0F);
                    } else if (ent instanceof Monster) {
                        SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                                level, ent, owner != null ? owner : this, 4.0F, false, false, true
                        );
                    }
                }
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(1.2D), this.getZ(), 10, 2.5D, 0.8D, 2.5D, 0.05D);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(1.2D), this.getZ(), 10, 2.5D, 0.8D, 2.5D, 0.05D);
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
            this.actionCooldown = (mutated && (mat == EssenceType.FROZEN || mat == EssenceType.LIGHTNING)) ? 14
                    : ((mutated && mat == EssenceType.GLACIAL) ? 16
                    : ((mutated && mat == EssenceType.DUST) ? 18
                    : ((mutated && mat == EssenceType.NATURE) ? 20 : 26)));
            fireConstructBolt(level, owner, target);
        }
    }

    public static boolean isPureAuraOrFieldSentry(EssenceType mat) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.isPureAuraOrFieldSentry(mat);
    }

    private void fireConstructBolt(ServerLevel level, @Nullable LivingEntity owner, LivingEntity target) {
        EssenceType mat = getEffectiveMateria();
        Vec3 rawDelta = target.getEyePosition().subtract(this.getEyePosition());
        Vec3 dir = rawDelta.normalize();
        if (isMutated() && mat == EssenceType.WATER) {
            double horizDist = Math.hypot(rawDelta.x, rawDelta.z);
            dir = new Vec3(rawDelta.x, rawDelta.y + horizDist * 0.045D, rawDelta.z).normalize();
        } else if (isMutated() && (mat == EssenceType.NETHER || mat == EssenceType.MAGMA) && getArchetype() == TriadArchetype.SENTRY) {
            // Section 6.7 Infernal Magma Mortar & Section 6.14 Volcanic Magma Mortar: ballistic elevation compensation for 0.03g mortar arc!
            double horizDist = Math.hypot(rawDelta.x, rawDelta.z);
            dir = new Vec3(rawDelta.x, rawDelta.y + 0.009D * horizDist * horizDist, rawDelta.z).normalize();
        }
        if (isMutated() && mat == EssenceType.PENUMBRA && getArchetype() == TriadArchetype.SENTRY) {
            // Section 6.41 Twilight Corona Citadel fires twin light/shadow suppression lances!
            Vec3 side = new Vec3(-dir.z, 0.0D, dir.x).normalize().scale(0.32D);
            spawnSingleConstructBolt(level, owner, mat, dir, side);
            spawnSingleConstructBolt(level, owner, mat, dir, side.scale(-1.0D));
            level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.45F, 1.55F);
            return;
        }
        if (isMutated() && mat == EssenceType.STATIC && getArchetype() == TriadArchetype.SENTRY) {
            // Section 6.44 Dielectric Tesla Citadel Spire: fires crackling lightning beams to 16 blocks!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBeam(
                    level, this.getEyePosition(), target.getEyePosition(), EssenceType.STATIC, EssenceType.LIGHTNING,
                    Math.max(1, this.spell.getEffectiveTier())
            );
            spawnSingleConstructBolt(level, owner, mat, dir, Vec3.ZERO);
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.42F, 1.65F);
            return;
        }
        spawnSingleConstructBolt(level, owner, mat, dir, Vec3.ZERO);
        if (isMutated() && mat == EssenceType.WATER) {
            level.playSound(null, this.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.55F, 1.35F);
        } else if (isMutated() && (mat == EssenceType.NATURE || mat == EssenceType.OVERGROWTH)) {
            level.playSound(null, this.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.70F, 1.35F);
            level.playSound(null, this.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.45F, 1.45F);
        } else if (isMutated() && (mat == EssenceType.FROZEN || mat == EssenceType.GLACIAL)) {
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.70F, 1.55F);
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.45F, 1.65F);
        } else if (isMutated() && mat == EssenceType.NETHER) {
            level.playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.80F, 0.90F);
            level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.55F, 1.05F);
        } else if (isMutated() && mat == EssenceType.MAGMA) {
            level.playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.85F, 0.72F);
            level.playSound(null, this.blockPosition(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.65F, 0.78F);
        } else if (isMutated() && mat == EssenceType.STORM) {
            level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.65F, 1.32F);
            level.playSound(null, this.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.45F, 1.45F);
        } else if (isMutated() && mat == EssenceType.DUST) {
            level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.65F, 1.48F);
            level.playSound(null, this.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.55F, 1.18F);
        } else if (isMutated() && mat == EssenceType.SPORE) {
            level.playSound(null, this.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.70F, 1.18F);
            level.playSound(null, this.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.35F, 1.45F);
        } else if (isMutated() && mat == EssenceType.TAIGA) {
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.65F, 1.38F);
            level.playSound(null, this.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.60F, 1.28F);
        } else if (isMutated() && mat == EssenceType.OASIS) {
            level.playSound(null, this.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.50F, 1.38F);
            level.playSound(null, this.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.65F, 1.25F);
        } else if (isMutated() && mat == EssenceType.VITAE) {
            level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.55F, 1.38F);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.50F, 1.45F);
        } else {
            level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.45F, 1.55F);
        }
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
            if (mat == EssenceType.OVERGROWTH || mat == EssenceType.TAIGA || mat == EssenceType.BLIGHT
                    || mat == EssenceType.NULL_R || mat == EssenceType.DAWN || mat == EssenceType.AURORA
                    || mat == EssenceType.RIME || mat == EssenceType.SPRING || mat == EssenceType.PRISMATIC
                    || mat == EssenceType.ESCHATON || mat == EssenceType.APOTHEOSIS || mat == EssenceType.ENTROPICA) {
                bolt.addBonusPierces((mat == EssenceType.ESCHATON || mat == EssenceType.APOTHEOSIS || mat == EssenceType.ENTROPICA) ? 4
                        : ((mat == EssenceType.AURORA || mat == EssenceType.DAWN || mat == EssenceType.RIME || mat == EssenceType.PRISMATIC) ? 3 : 2));
            }
        }
        if (isMutated() && mat == EssenceType.WATER) {
            bolt.shoot(dir.x, dir.y, dir.z, 1.48F, 0.0F);
            bolt.enableSlightFluidArc(false);
        } else if (isMutated() && (mat == EssenceType.NETHER || mat == EssenceType.MAGMA)) {
            // Section 6.7 Infernal Magma Mortar & Section 6.14 Volcanic Magma Mortar: arcing molten fireball/boulder using ballistic elevation!
            bolt.setNoGravity(false);
            bolt.shoot(dir.x, dir.y, dir.z, 1.32F, 0.0F);
        } else if (isMutated() && (mat == EssenceType.SYLVAN || mat == EssenceType.OBLIVION || mat == EssenceType.CHIMERA)) {
            bolt.setNoGravity(false);
            bolt.shoot(dir.x, dir.y + 0.20D, dir.z, 1.25F, 0.0F);
        } else if (isMutated() && (mat == EssenceType.NATURE || mat == EssenceType.OVERGROWTH)) {
            // Sylvan Thorn Sentry & Living Root Totem fire high-speed straight Thorn Darts!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.92F, 0.0F);
        } else if (isMutated() && mat == EssenceType.EARTH) {
            // Section 6.5 Tectonic Plinth Sentry fires heavy Quake Spire stone bolts!
            bolt.shoot(dir.x, dir.y, dir.z, 1.65F, 0.0F);
        } else if (isMutated() && (mat == EssenceType.RADIANT || mat == EssenceType.PRISMATIC)) {
            // Section 6.8 Solar Seraph & Section 6.53 Prismatic Valkyrie: rain high-speed piercing light arrows/javelins!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, mat == EssenceType.PRISMATIC ? 2.08F : 1.98F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.65F, 1.55F);
        } else if (isMutated() && mat == EssenceType.UNDEAD) {
            // Section 6.10 Cryptguard Ossuary Totem: launches floating seeking cursed skulls at living targets!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.48F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.55F, 1.35F);
        } else if (isMutated() && mat == EssenceType.VOID) {
            // Section 6.11 Void Gravity Pylon: fires zero-gravity wall-phasing Void Darts (#41304E)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.65F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.55F, 1.42F);
        } else if (isMutated() && mat == EssenceType.CATACLYSM) {
            // Section 6.55 Volcanic Eruption Spire: fires ultra-high-velocity wall-penetrating railgun magma spikes (#41304E / #211A21 / #B83C08)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 2.15F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.70F, 0.78F);
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.55F, 1.55F);
        } else if (isMutated() && mat == EssenceType.TERMINUS) {
            // Section 6.56 Anchored Permafrost Elemental Spire: fires wall-penetrating cryogenic lances (#A88654 / #98DCF2 / #225C7C)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 2.05F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.70F, 1.58F);
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.50F, 1.68F);
        } else if (isMutated() && mat == EssenceType.ESCHATON) {
            // Section 6.57 Accretion Disk Singularity Spire: fires wall-penetrating singularity lances (#0B0813 / #7012A5 / #D4C2FC)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 2.25F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.65F, 1.38F);
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.55F, 1.62F);
        } else if (isMutated() && mat == EssenceType.APOTHEOSIS) {
            // Section 6.58 White-Hole Dawn Obelisk: fires wall-penetrating holy supernova lances (#FFFFFF / #FFE484 / #72F6FF)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 2.25F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.52F);
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.50F, 1.65F);
        } else if (isMutated() && mat == EssenceType.ENTROPICA) {
            // Section 6.59 Altar of the Horizon: fires wall-penetrating balance rays (#E0E5EC / #1A153B / #FAD02C)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 2.30F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.48F);
            level.playSound(null, this.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.55F, 1.55F);
        } else if (isMutated() && mat == EssenceType.NULL_R) {
            // Section 6.31 Event Horizon Spire: fires piercing zero-gravity antimatter lances (#FFFFFF / #41304E)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.92F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.65F, 1.48F);
        } else if (isMutated() && mat == EssenceType.NULL_U) {
            // Section 6.32 Oblivion Singularity Spire: silently fires zero-gravity pulling gravity darts (#110B17 / #41304E)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.82F, 0.0F);
        } else if (isMutated() && mat == EssenceType.DAWN) {
            // Section 6.33 Sol-Zephyr Spire: fires 3-pierce zero-gravity daylight javelins (#FFEA78 / #A88654) with gale knockback!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.98F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.55F, 1.45F);
        } else if (isMutated() && mat == EssenceType.DUST) {
            // Section 6.18 Abrasive Dust Pylon: fires high-pressure zero-drop mineral dust slugs (#C2B28F) with pushback & Blindness!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.88F, 0.0F);
        } else if (isMutated() && mat == EssenceType.SPORE) {
            // Section 6.19 Bio-Pollen Spire: launches zero-gravity buoyant Aerodynamic Fungal Puffballs (#1A5923 / #A88654)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.38F, 0.0F);
        } else if (isMutated() && mat == EssenceType.TAIGA) {
            // Section 6.20 Frost-Howler Spire: fires high-speed zero-drop 2-pierce Hardened Frost-Resin Spikes (#1A5923 / #98DCF2)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.92F, 0.0F);
        } else if (isMutated() && mat == EssenceType.OASIS) {
            // Section 6.21 Aquifer Spring Spire: fires sparkling Oasis springwater bolts at hostiles!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.72F, 0.0F);
        } else if (isMutated() && mat == EssenceType.VITAE) {
            // Section 6.22 Cardiovascular Spire: fires high-speed zero-drop #FF6B9D defensive vitality darts at foes!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.86F, 0.0F);
        } else if (isMutated() && mat == EssenceType.LIGHTNING) {
            // Section 6.13 Volt-Spark Sentry: fires rapid zero-gravity homing Ball-Lightning orbs (#FFE600)!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.95F, 0.0F);
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.45F, 1.75F);
        } else if (isMutated() && mat == EssenceType.STORM) {
            // Section 6.15 Tempest-Squall Spire: fires high-speed pressurized water/wind slugs (#429BA3) with heavy pushback!
            bolt.setNoGravity(true);
            bolt.shoot(dir.x, dir.y, dir.z, 1.92F, 0.0F);
        } else if (isMutated() && (mat == EssenceType.ARID || mat == EssenceType.AIR)) {
            bolt.shoot(dir.x, dir.y, dir.z, 2.05F, 0.0F);
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
                case PENUMBRA, RIME, SPRING, STATIC, SYLVAN, GENESIS, OBLIVION, CHIMERA, UMBRAL, SPORE, TAIGA, OASIS, VITAE, BLOOD, AURA, AMBER -> 14.0D;
                case AIR, ARID, DUST, MIRAGE, MIASMA, NATURE, EARTH, UNDEAD, VOID, MAGMA, STORM, GLACIAL, OVERGROWTH -> 12.0D;
                default -> 9.0D;
            };
            for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(tauntRad))) {
                if (!mob.isAlive() || this.isFriendlyTo(mob)) continue;
                // Section 6.10 Bone-Nova Effigy taunts LIVING mobs!
                if (mutated && mat == EssenceType.UNDEAD && mob.getType().is(EntityTypeTags.UNDEAD)) continue;
                // Section 6.5 Granite Effigy Decoy taunts strictly melee-capable mobs (skipping pure ranged mobs unless holding a melee weapon)!
                if (mutated && mat == EssenceType.EARTH && !isMeleeCapableMobForEarthDecoy(mob)) continue;
                if (mob instanceof Enemy || mob instanceof Monster || mob.getTarget() == owner
                        || (owner != null && mob.getLastHurtByMob() == owner)) {
                    mob.setTarget(this);
                    mob.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, this);
                }
            }
        }
        if (mutated && (mat == EssenceType.VOID || mat == EssenceType.ESCHATON) && this.ageTicks % 6 == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.95D), this.getZ(),
                    mat == EssenceType.ESCHATON ? 7 : 4, 0.34D, 0.55D, 0.34D, 0.04D);
        } else if (mutated && mat == EssenceType.ENTROPICA && this.ageTicks % 6 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.95D), this.getZ(), 4, 0.35D, 0.55D, 0.35D, 0.03D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.95D), this.getZ(), 4, 0.35D, 0.55D, 0.35D, 0.04D);
            // Section 6.59 Parallax Event-Horizon Fold departure decoy: taunts AND pulls pursuers for 4.0s before exploding!
            if (this.shortLivedIllusion) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D))) {
                    if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe) || !(foe instanceof Monster)) continue;
                    Vec3 pull = this.position().subtract(foe.position());
                    if (pull.lengthSqr() > 1.0E-4D) {
                        Vec3 v = pull.normalize().scale(0.58D);
                        foe.setDeltaMovement(foe.getDeltaMovement().scale(0.35D).add(v.x, 0.14D, v.z));
                        foe.hasImpulse = true;
                    }
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
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(1.1D), this.getZ(), 4, 0.45D, 0.45D, 0.45D, 0.01D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFD83D, 34, 1.05F, 0.05F),
                    this.getX(), this.getY(1.05D), this.getZ(), 5, 0.50D, 0.45D, 0.50D, 0.015D);
        } else if (mutated && mat == EssenceType.WATER && this.ageTicks % 5 == 0) {
            level.sendParticles(ParticleTypes.DRIPPING_WATER, this.getX(), this.getY(0.95D), this.getZ(), 3, 0.35D, 0.45D, 0.35D, 0.0D);
        } else if (mutated && mat == EssenceType.EARTH && this.ageTicks % 7 == 0) {
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), 0xFFC278, 30, 0.90F, 0.08F),
                    this.getX(), this.getY(1.05D), this.getZ(), 3, 0.45D, 0.55D, 0.45D, 0.015D);
        } else if (mutated && mat == EssenceType.RADIANT && this.ageTicks % 6 == 0) {
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), 0xFFE278, 32, 0.95F, 0.06F),
                    this.getX(), this.getY(1.10D), this.getZ(), 3, 0.45D, 0.50D, 0.45D, 0.012D);
        } else if (mutated && mat == EssenceType.UMBRAL && this.ageTicks % 6 == 0) {
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x211A21, 28, 1.10F, 0.14F),
                    this.getX(), this.getY(1.00D), this.getZ(), 3, 0.35D, 0.55D, 0.35D, 0.010D);
            if (owner != null && owner.isAlive() && this.ageTicks % 12 == 0) {
                this.entityData.set(DATA_DISPLAY_ITEM, owner.getMainHandItem().copy());
                this.setItemSlot(EquipmentSlot.OFFHAND, owner.getOffhandItem().copy());
                for (EquipmentSlot eq : EquipmentSlot.values()) {
                    if (eq.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                        this.setItemSlot(eq, owner.getItemBySlot(eq).copy());
                    }
                }
            }
        } else if (mutated && mat == EssenceType.UNDEAD && this.ageTicks % 6 == 0) {
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.95D), this.getZ(), 2, 0.35D, 0.50D, 0.35D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0x5EE088, 28, 0.90F, 0.08F),
                    this.getX(), this.getY(1.05D), this.getZ(), 3, 0.40D, 0.50D, 0.40D, 0.012D);
        } else if (mutated && mat == EssenceType.TAIGA && this.ageTicks % 6 == 0) {
            // Section 6.20 Phantom Snow Stalker Decoy: snow & evergreen pine-needle trails!
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(1.00D), this.getZ(), 3, 0.38D, 0.50D, 0.38D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 26, 0.92F, 0.06F),
                    this.getX(), this.getY(0.95D), this.getZ(), 3, 0.40D, 0.45D, 0.40D, 0.015D);
        } else if (mutated && mat == EssenceType.OASIS && this.ageTicks % 6 == 0) {
            // Section 6.21 Mirage Blossom Effigy Decoy: sand dust & water droplet trails!
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                    this.getX(), this.getY(0.85D), this.getZ(), 3, 0.36D, 0.45D, 0.36D, 0.015D);
            level.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY(1.05D), this.getZ(), 3, 0.36D, 0.45D, 0.36D, 0.03D);
        } else if (mutated && mat == EssenceType.VITAE && this.ageTicks % 6 == 0) {
            // Section 6.22 Heart-Husk Effigy Decoy: pink heart & #FF6B9D cellular pulse particles!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), 0xFF6B9D, 22, 0.92F, 0.05F),
                    this.getX(), this.getY(1.00D), this.getZ(), 3, 0.38D, 0.48D, 0.38D, 0.02D);
            if (this.ageTicks % 12 == 0) {
                level.sendParticles(ParticleTypes.HEART,
                        this.getX(), this.getY(1.15D), this.getZ(), 2, 0.32D, 0.35D, 0.32D, 0.02D);
            }
        } else if (mutated && mat == EssenceType.BLOOD && this.ageTicks % 6 == 0) {
            // Section 6.23 Crimson Effigy Decoy (#8A0303): dark red droplets & boiling crimson vapor trails!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 22, 0.92F, 0.05F),
                    this.getX(), this.getY(1.00D), this.getZ(), 3, 0.36D, 0.45D, 0.36D, 0.015D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x8A0303, 26, 0.95F, 0.07F),
                    this.getX(), this.getY(0.85D), this.getZ(), 3, 0.40D, 0.48D, 0.40D, 0.015D);
        } else if (mutated && mat == EssenceType.AURA && this.ageTicks % 6 == 0) {
            // Section 6.24 Spirit-Vane Effigy Decoy (#FF6B9D / #A88654): golden-pink spirit mist & floating pranic motes!
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0xFF6B9D, 22, 0.92F, 0.05F),
                    this.getX(), this.getY(0.95D), this.getZ(), 3, 0.36D, 0.45D, 0.36D, 0.012D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), 0xA88654, 24, 0.90F, 0.04F),
                    this.getX(), this.getY(1.10D), this.getZ(), 3, 0.38D, 0.45D, 0.38D, 0.015D);
        } else if (mutated && mat == EssenceType.AMBER && this.ageTicks % 6 == 0) {
            // Section 6.25 Petrified Amber Effigy Decoy (#FF6B9D / #773214): golden resin droplets & translucent amber crystals!
            level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                    this.getX(), this.getY(0.95D), this.getZ(), 2, 0.35D, 0.45D, 0.35D, 0.0D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), 0xFF6B9D, 22, 0.90F, 0.04F),
                    this.getX(), this.getY(1.05D), this.getZ(), 3, 0.36D, 0.45D, 0.36D, 0.015D);
        }

        // Lightning Volt-Spark Decoy shocks any hostile entity that attempts to pass through / touch it!
        if (mutated && mat == EssenceType.LIGHTNING) {
            if (this.ageTicks % 5 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(0.95D), this.getZ(),
                        4, 0.35D, 0.55D, 0.35D, 0.08D);
            }
            if (this.ageTicks % 8 == 0) {
                int sTier = this.spell.getEffectiveTier();
                for (LivingEntity touching : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.95D))) {
                    if (touching == this || touching == owner || !touching.isAlive() || this.isFriendlyTo(touching)) continue;
                    SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(
                            level, touching, owner != null ? owner : this, 2.2F + sTier * 0.55F, false, true
                    );
                    touching.addEffect(new MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 14, 0));
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                            level, this.getBoundingBox().getCenter(), touching.getBoundingBox().getCenter(),
                            EssenceType.LIGHTNING, null, sTier
                    );
                    level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.45F, 1.70F);
                }
            }
        }
    }

    public static float resolveSolarSeraphDecoyDamageTaken(float incomingDamage) {
        return Math.max(0.0F, incomingDamage * 0.55F);
    }

    public static float resolveSolarSeraphDecoyAbsorbedHeal(float incomingDamage) {
        return Math.max(0.5F, incomingDamage * 0.45F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() != null && isOwnedBy(source.getEntity())) {
            return false;
        }
        float rawHitAmount = amount;
        boolean radiantDecoyMitigation = isMutated()
                && getArchetype() == TriadArchetype.DECOY
                && getEffectiveMateria() == EssenceType.RADIANT
                && !source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)
                && !source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL);
        float effectiveAmount = radiantDecoyMitigation ? resolveSolarSeraphDecoyDamageTaken(amount) : amount;
        boolean hurt = super.hurtServer(level, source, effectiveAmount);
        if (!hurt || !isMutated()) {
            return hurt;
        }

        EssenceType mat = getEffectiveMateria();
        LivingEntity owner = resolveSummoner(level);
        Entity directAttacker = source.getDirectEntity();

        if (getArchetype() == TriadArchetype.DECOY) {
            switch (mat) {
                case WATER -> {
                    // Section 6.3 Aqueous Decoy: aquatic mirage that bursts immediately into a single 4.0m blinding splash when struck!
                    triggerWaterDecoyStruckSplash(level);
                    if (!this.isRemoved()) {
                        this.discard();
                    }
                    return true;
                }
                case NETHER, MAGMA -> {
                    // Section 6.7 Blazing Wicker Effigy & Section 6.14 Caldera Nova Effigy: burns attackers who strike it!
                    Entity atkEnt = directAttacker != null ? directAttacker : source.getEntity();
                    if (atkEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.igniteForSeconds(5);
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(attacker).putLong(
                                SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
                        );
                        if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
                            if (mat == EssenceType.MAGMA) {
                                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(
                                        level, attacker, owner != null ? owner : this, 2.2F + this.spell.getEffectiveTier() * 0.50F, false, true
                                );
                            } else {
                                SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(
                                        level, attacker, owner != null ? owner : this, 2.0F + this.spell.getEffectiveTier() * 0.45F, true
                                );
                            }
                        }
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                                level, this.position().add(0.0D, 0.9D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                mat, null, 1.35F, this.spell.getEffectiveTier()
                        );
                        level.sendParticles(mat == EssenceType.MAGMA ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                                attacker.getX(), attacker.getY(0.7D), attacker.getZ(),
                                14, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                }
                case FROZEN, GLACIAL, TAIGA -> {
                    if (mat == EssenceType.GLACIAL
                            && ddraig.net.entropica.util.EntityHelper.getPersistentData(this)
                            .getBoolean("SpellGlacialStatuePopOnStrike").orElse(false)) {
                        // Section 6.16 Ice Statue Blink: lifelike frozen ice statue shatters immediately into a frost pop when struck!
                        if (this.isAlive() && !this.isRemoved()) {
                            triggerElementalDeathBurst(level);
                            this.discard();
                        }
                        return true;
                    }
                    if (directAttacker instanceof LivingEntity attacker) {
                        if (mat == EssenceType.FROZEN) {
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(attacker, level, 0.45D, 50L);
                        } else {
                            attacker.setTicksFrozen(attacker.getTicksFrozen() + 60);
                            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                            if (mat == EssenceType.GLACIAL) {
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(attacker, level, 0.40D, 50L);
                            }
                        }
                    }
                }
                case RADIANT -> {
                    // Section 6.8 Solar Seraph Decoy: absorbs 45% of incoming hit damage (taking 0.55x) and converts it into a circular 8m healing & 1-debuff/hex cleansing pulse!
                    float pulseHeal = resolveSolarSeraphDecoyAbsorbedHeal(rawHitAmount);
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            level, this.position().add(0.0D, 0.12D, 0.0D), 3.8D, EssenceType.RADIANT, null, 52, this.spell.getEffectiveTier()
                    );
                    level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.38F);
                    for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (ally != this && ally.isAlive() && this.distanceToSqr(ally) <= 64.0D && (ally == owner || this.isFriendlyTo(ally))) {
                            SpellCombatReactionEngine.cleanseSingleNegativeEffectOrHex(ally);
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, pulseHeal);
                            ally.heal(pulseHeal);
                            level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                            level.sendParticles(ParticleTypes.END_ROD, ally.getX(), ally.getY(0.95D), ally.getZ(), 6, 0.30D, 0.35D, 0.30D, 0.03D);
                        }
                    }
                }
                case UMBRAL -> {
                    // Section 6.9 Shadow Stalker Decoy: gear-copying shadow illusion that blinds attackers & draws aggro off the summoner!
                    level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(0.95D), this.getZ(), 14, 0.40D, 0.45D, 0.40D, 0.025D);
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        if (attacker instanceof Mob atkMob) {
                            atkMob.setTarget(this);
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
                case SYLVAN -> {
                    // Section 6.45 Thorn-Bramble Effigy: reflects 35% of melee damage with petrified briar thorns!
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        float smite = attacker.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                        SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(1.75F, amount * 0.35F) * smite, false, true
                        );
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, attacker.getX(), attacker.getY(0.8D), attacker.getZ(), 5, 0.25D, 0.25D, 0.25D, 0.03D);
                    }
                }
                case OBLIVION -> {
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(1.75F, amount * 0.35F), false, true
                        );
                        level.sendParticles(ParticleTypes.LARGE_SMOKE, attacker.getX(), attacker.getY(0.8D), attacker.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.03D);
                    }
                }
                case CHIMERA -> {
                    if (directAttacker instanceof LivingEntity attacker) {
                        attacker.hurtServer(level, level.damageSources().thorns(this), Math.max(1.5F, amount * 0.35F));
                    }
                }
                case NATURE -> {
                    // Section 6.4 Sylvan Pollen Effigy (A3): briar thorns reflect 35% melee damage + 1.5s Slowness II & puff golden pollen!
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFD83D, 32, 1.05F, 0.05F),
                            this.getX(), this.getY(1.0D), this.getZ(), 14, 0.65D, 0.45D, 0.65D, 0.04D);
                    boolean wardSafe = this.spell != null && this.spell.hasWardFriendlyFireSafeguard();
                    if (directAttacker instanceof LivingEntity attacker
                            && attacker != this
                            && !source.is(DamageTypeTags.IS_PROJECTILE)
                            && !this.isFriendlyTo(attacker)
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(attacker, owner, wardSafe)
                            && !(wardSafe && ddraig.net.entropica.spell.mutation.element.NatureSpellMutationHandler.isFriendlyOrWoodlandAlly(attacker, owner))) {
                        attacker.hurtServer(level, level.damageSources().thorns(this), Math.max(1.5F, amount * 0.35F));
                        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1));
                    }
                }
                case EARTH -> {
                    // Section 6.5 Granite Effigy Decoy: chips granite/terracotta splinters when struck!
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRANITE.defaultBlockState()),
                            this.getX(), this.getY(0.95D), this.getZ(), 14, 0.45D, 0.45D, 0.45D, 0.12D);
                    level.playSound(null, this.blockPosition(), SoundEvents.STONE_HIT, SoundSource.PLAYERS, 0.85F, 0.72F);
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 0));
                    }
                }
                case AETHER -> {
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(
                                level, attacker, resolveSummoner(level), Math.max(1.5F, amount * 0.40F), false, true
                        );
                    }
                }
                case ENTROPIC -> {
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(
                                level, attacker, resolveSummoner(level), Math.max(1.5F, amount * 0.40F), false, true
                        );
                    }
                }
                case CELESTIAL -> {
                    if (this.shortLivedIllusion) {
                        // Section 6.52 Holy Dawn-Decoy (Blink): explodes immediately in a white-hole flashbang if struck!
                        int dTier = this.spell != null ? Math.max(1, this.spell.getEffectiveTier()) : 6;
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                                level, this.position(), 6.0D, EssenceType.CELESTIAL, EssenceType.RADIANT, dTier
                        );
                        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                            if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                            Vec3 diff = foe.position().subtract(this.position());
                            Vec3 out = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0.0D, 0.3D, 1.0D)).scale(1.25D);
                            foe.setDeltaMovement(out.x, 0.35D, out.z);
                            foe.hasImpulse = true;
                            SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(
                                    level, foe, owner != null ? owner : this, 4.5F + dTier * 0.95F, false, true
                            );
                        }
                        level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.55F);
                        this.discard();
                        return true;
                    }
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.igniteForSeconds(4);
                        SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(1.5F, amount * 0.40F), false, true
                        );
                    }
                }
                case PRISMATIC -> {
                    if (this.shortLivedIllusion) {
                        // Section 6.53 Prismatic Mirror Decoy (Blink): flashes & explodes immediately in a blinding flashbang if struck!
                        int dTier = this.spell != null ? Math.max(1, this.spell.getEffectiveTier()) : 5;
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                                level, this.position(), 6.0D, EssenceType.PRISMATIC, EssenceType.RADIANT, dTier
                        );
                        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
                            if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
                            Vec3 diff = foe.position().subtract(this.position());
                            Vec3 out = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0.0D, 0.3D, 1.0D)).scale(1.20D);
                            foe.setDeltaMovement(out.x, 0.32D, out.z);
                            foe.hasImpulse = true;
                            SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(
                                    level, foe, owner != null ? owner : this, 4.4F + dTier * 0.90F, false, true
                            );
                        }
                        level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.45F);
                        this.discard();
                        return true;
                    }
                }
                case EMPYREAN -> {
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(2.0F, amount * 0.45F), false, true, false, false
                        );
                    }
                }
                case CATACLYSM -> {
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        attacker.igniteForSeconds(4);
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(attacker)
                                .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 80L);
                        SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(2.0F, amount * 0.45F), false, true, false
                        );
                    }
                }
                case TERMINUS -> {
                    if (directAttacker instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)
                            && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                        attacker.clearFire();
                        attacker.setTicksFrozen(Math.min(attacker.getTicksRequiredToFreeze() + 60, attacker.getTicksFrozen() + 50));
                        SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(2.0F, amount * 0.45F), false, true, false
                        );
                    }
                }
                case ESCHATON -> {
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(2.5F, amount * 0.50F), false, true, false
                        );
                    }
                }
                case APOTHEOSIS -> {
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(2.5F, amount * 0.50F), false, true, true, false
                        );
                    }
                }
                case ENTROPICA -> {
                    Entity attackerEnt = source.getEntity() != null ? source.getEntity() : directAttacker;
                    if (attackerEnt instanceof LivingEntity attacker && attacker != this && !this.isFriendlyTo(attacker)) {
                        SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                                level, attacker, owner != null ? owner : this, Math.max(2.5F, amount * 0.50F), false, true, false
                        );
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
            if (isMutated() && getEffectiveMateria() == EssenceType.WATER && getArchetype() == TriadArchetype.DECOY) {
                triggerWaterDecoyStruckSplash(srv);
            } else {
                triggerElementalDeathBurst(srv);
            }
        }
        super.die(damageSource);
    }

    private void triggerWaterDecoyStruckSplash(ServerLevel level) {
        if (this.deathBurstTriggered) return;
        this.deathBurstTriggered = true;

        LivingEntity owner = resolveSummoner(level);
        int tier = this.spell.getEffectiveTier();
        boolean wardSafeguard = this.spell != null && this.spell.hasWardFriendlyFireSafeguard();

        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                level, this.position().add(0.0D, 0.55D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.WATER, null, 4.0F, tier
        );
        BlockPos c = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-4, -1, -4), c.offset(4, 2, 4))) {
            if (level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0D))) {
            if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafeguard)) continue;
            double dx = foe.getX() - this.getX();
            double dz = foe.getZ() - this.getZ();
            if (dx * dx + dz * dz > 16.0D) continue;
            foe.clearFire();
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            SpellLingeringZoneManager.applySoaked(foe, 160);
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 0.85D, 0.28D, push.z * 0.85D));
            }
        }
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(0.85D), this.getZ(), 36, 1.05D, 0.50D, 1.05D, 0.16D);
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.95F, 1.12F);
    }

    public static double resolveCanonicalDeathBurstRadius(EssenceType mat) {
        return ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(mat);
    }

    public static boolean isMeleeCapableMobForEarthDecoy(@Nullable Mob mob) {
        if (mob == null) {
            return false;
        }
        boolean isPureRangedType = mob instanceof AbstractSkeleton
                || mob instanceof Pillager
                || mob instanceof Blaze
                || mob instanceof Ghast
                || mob instanceof Witch
                || mob instanceof Shulker
                || mob instanceof Breeze;
        if (!isPureRangedType) {
            return true;
        }
        ItemStack held = mob.getMainHandItem();
        return !held.isEmpty() && (held.is(ItemTags.SWORDS)
                || held.is(ItemTags.AXES)
                || held.is(ItemTags.PICKAXES)
                || held.is(ItemTags.SHOVELS)
                || held.is(ItemTags.HOES)
                || held.getItem() instanceof TridentItem
                || held.getItem() instanceof MaceItem);
    }

    public static boolean shouldTriggerEarthShrapnelBurst(EssenceType materia, TriadArchetype archetype) {
        return materia == EssenceType.EARTH && archetype == TriadArchetype.DECOY;
    }

    public static boolean shouldTriggerFrozenShatterNova(EssenceType materia, TriadArchetype archetype, boolean destroyedInCombat) {
        return materia == EssenceType.FROZEN && archetype == TriadArchetype.DECOY && destroyedInCombat;
    }

    public static boolean shouldTriggerRadiantDeathSmiteNova(EssenceType materia, TriadArchetype archetype) {
        return false;
    }

    private void triggerElementalDeathBurst(ServerLevel level) {
        if (this.deathBurstTriggered) return;
        this.deathBurstTriggered = true;
        if (!isMutated()) return;

        EssenceType mat = getEffectiveMateria();
        // Section 6.2 Zephyr Eidolon Agent & Sentry do NOT emit damaging death explosions (only Decoy emits a 0-damage cyclone gust)!
        if (mat == EssenceType.AIR && getArchetype() != TriadArchetype.DECOY) {
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(0.8D), this.getZ(), 16, 0.45D, 0.45D, 0.45D, 0.04D);
            return;
        }
        // Section 6.3 Aqueous Eidolon Agent & Sentry (and un-struck Decoy on peaceful expiry) dissolve gently into a water puddle puff without Blindness!
        if (mat == EssenceType.WATER) {
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(0.45D), this.getZ(), 18, 0.50D, 0.30D, 0.50D, 0.06D);
            level.sendParticles(ParticleTypes.DRIPPING_WATER, this.getX(), this.getY(0.65D), this.getZ(), 10, 0.40D, 0.35D, 0.40D, 0.0D);
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.55F, 1.25F);
            return;
        }
        // Section 6.5 Tectonic Eidolon Agent & Sentry crumble peacefully without stone shrapnel detonation (Decoy-only)!
        if (mat == EssenceType.EARTH && !shouldTriggerEarthShrapnelBurst(mat, getArchetype())) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRANITE.defaultBlockState()),
                    this.getX(), this.getY(0.65D), this.getZ(), 16, 0.45D, 0.35D, 0.45D, 0.06D);
            level.playSound(null, this.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.65F, 0.80F);
            return;
        }
        // Section 6.6 Glacial Eidolon Agent & Sentry dissipate gently without 4-block Frost Nova shatter (Decoy-only)!
        if (mat == EssenceType.FROZEN && !shouldTriggerFrozenShatterNova(mat, getArchetype(), true)) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(0.75D), this.getZ(), 16, 0.45D, 0.40D, 0.45D, 0.03D);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.65F, 1.35F);
            return;
        }
        // Section 6.8 Solar Seraph Suite: dissipates peacefully into solar starlight without any offensive death smite nova!
        if (mat == EssenceType.RADIANT) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.85D), this.getZ(), 16, 0.45D, 0.45D, 0.45D, 0.04D);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.65F, 1.42F);
            return;
        }
        LivingEntity owner = resolveSummoner(level);
        int tier = this.spell.getEffectiveTier();
        double burstRad = resolveCanonicalDeathBurstRadius(mat);
        AABB box = this.getBoundingBox().inflate(burstRad);

        // Beneficial ally death-burst effects & Air Decoy non-damaging cyclone gust VFX
        if (mat == EssenceType.AIR && getArchetype() == TriadArchetype.DECOY) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.8D, 0.0D), Math.max(4.5D, burstRad), false, EssenceType.AIR, null, 24, tier
            );
            level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, this.getX(), this.getY(0.6D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 1.05F);
        } else if (mat == EssenceType.VITAE || mat == EssenceType.GENESIS) {
            if (mat == EssenceType.VITAE) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, this.position().add(0.0D, 0.40D, 0.0D), burstRad, EssenceType.VITAE, EssenceType.NATURE, tier
                );
                level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 0.92F);
                level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.32F);
            } else {
                // Section 6.47 Amniotic Water-Phantom: blinding flash of holy mist healing all allies for 6 hearts (12.0 HP)!
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, this.position().add(0.0D, 0.40D, 0.0D), Math.max(10.0D, burstRad), EssenceType.GENESIS, EssenceType.WATER, tier
                );
                level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.95F, 1.25F);
                level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.40F);
            }
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally) || ally instanceof Player)) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 12.0F);
                    ally.heal(12.0F); // 6 full hearts!
                    level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 8, 0.35D, 0.35D, 0.35D, 0.04D);
                }
            }
        } else if (mat == EssenceType.SPRING) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), Math.max(10.0D, burstRad), EssenceType.SPRING, EssenceType.WATER, tier
            );
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    float missing = ally.getMaxHealth();
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, missing);
                    ally.heal(missing); // Fully restores friendly ally health + Regeneration III 6.0s!
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 2, true, true));
                    level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.05D);
                }
            }
        } else if (mat == EssenceType.STATIC) {
            // Section 6.44 Volatile Dielectric Spark Effigy: Catastrophic Tesla Nova (10m radius)!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), Math.max(10.0D, burstRad), EssenceType.STATIC, EssenceType.LIGHTNING, tier
            );
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.95F, 1.25F);
        } else if (mat == EssenceType.SYLVAN) {
            // Section 6.45 Thorn-Bramble Effigy: 10m petrified root-cage & solar canopy detonation!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), Math.max(10.0D, burstRad), EssenceType.SYLVAN, EssenceType.RADIANT, tier
            );
            level.playSound(null, this.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 0.95F, 0.68F);
        } else if (mat == EssenceType.MIASMA) {
            // Section 6.46 Decomposing Bog-Husk: 10m blinding swamp gas & toxic sludge detonation!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), Math.max(10.0D, burstRad), EssenceType.MIASMA, EssenceType.UNDEAD, tier
            );
            level.sendParticles(ParticleTypes.SNEEZE, this.getX(), this.getY(0.8D), this.getZ(), 28, 2.8D, 0.8D, 2.8D, 0.05D);
            level.playSound(null, this.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.95F, 0.65F);
        } else if (mat == EssenceType.OBLIVION) {
            // Section 6.48 Smoldering Wither-Effigy: 10m cataclysmic burst of black netherfire!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), Math.max(10.0D, burstRad), EssenceType.OBLIVION, EssenceType.NETHER, tier
            );
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.8D), this.getZ(), 32, 3.0D, 0.8D, 3.0D, 0.06D);
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 0.68F);
        } else if (mat == EssenceType.UMBRAL) {
            // Section 6.9 Shadow Stalker Death Burst: silent Smoke Veil dome that wipes mob aggro & cloaks the summoner!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    this.position().add(0.0D, 0.20D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.UMBRAL, null, (float) Math.max(4.5D, burstRad), 60, tier
            );
            ddraig.net.entropica.spell.mutation.element.UmbralSpellMutationHandler.wipeMobAggroAround(
                    level, this.position(), Math.max(6.0D, burstRad + 1.5D), true
            );
            if (owner != null && owner.isAlive() && owner.distanceToSqr(this) <= 144.0D) {
                owner.removeEffect(MobEffects.GLOWING);
                owner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false, true));
            }
        } else if (mat == EssenceType.UNDEAD && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.10 Bone-Nova Effigy: detonates into a full 6-block Bone Nova corpse explosion when destroyed!
            double boneNovaRad = Math.max(6.0D, burstRad);
            box = this.getBoundingBox().inflate(boneNovaRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    this.position().add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.UNDEAD, null, (float) boneNovaRad, 62, tier
            );
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                    this.getX(), this.getY(0.5D), this.getZ(), 34, 1.6D, 0.5D, 1.6D, 0.14D
            );
            level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.95F, 1.10F);
            level.playSound(null, this.blockPosition(), SoundEvents.BONE_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.78F);
        } else if (mat == EssenceType.BLOOD && getArchetype() != TriadArchetype.SENTRY) {
            // Section 6.23 Crimson Effigy Decoy (#8A0303): 5-block boiling blood burst granting +4 Absorption hearts (8.0F)!
            double bloodBurstRad = Math.max(5.0D, burstRad);
            box = this.getBoundingBox().inflate(bloodBurstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    this.position().add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.BLOOD, EssenceType.VITAE, (float) bloodBurstRad, 58, tier
            );
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), 0x8A0303, 30, 1.15F, 0.08F),
                    this.getX(), this.getY(0.80D), this.getZ(), 32, 1.6D, 0.7D, 1.6D, 0.05D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 26, 1.00F, 0.06F),
                    this.getX(), this.getY(0.75D), this.getZ(), 24, 1.4D, 0.6D, 1.4D, 0.06D);
            level.playSound(null, this.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.90F, 1.25F);
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    ally.setAbsorptionAmount(Math.min(28.0F, ally.getAbsorptionAmount() + 8.0F)); // +4 Absorption hearts!
                }
            }
        } else if (mat == EssenceType.AURA && getArchetype() != TriadArchetype.SENTRY) {
            // Section 6.24 Spirit-Vane Effigy Decoy (#FF6B9D / #A88654): 5-block vacuum implosion silencing enemies for 3s!
            double auraBurstRad = Math.max(5.0D, burstRad);
            box = this.getBoundingBox().inflate(auraBurstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.35D, 0.0D), auraBurstRad, true, EssenceType.AURA, null, 46, tier
            );
            level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 1.32F);
        } else if (mat == EssenceType.AMBER && getArchetype() != TriadArchetype.SENTRY) {
            // Section 6.25 Petrified Amber Effigy Decoy (#FF6B9D / #773214): shatters into a 5-block burst rooting hostiles for 3s!
            double amberBurstRad = Math.max(5.0D, burstRad);
            box = this.getBoundingBox().inflate(amberBurstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.20D, 0.0D), amberBurstRad, EssenceType.AMBER, null, tier
            );
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.95F);
            level.playSound(null, this.blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.95F, 0.85F);
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
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), 6.0D, EssenceType.WRAITH, EssenceType.UNDEAD, tier
            );
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally instanceof SpellEidolonEntity && ally.isAlive() && this.isFriendlyTo(ally)) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 8.0F);
                    ally.heal(8.0F);
                }
            }
        } else if (mat == EssenceType.BARROW) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), 5.0D, EssenceType.BARROW, EssenceType.UNDEAD, tier
            );
        } else if (mat == EssenceType.PYRE) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), this.shortLivedIllusion ? 3.5D : 6.0D, EssenceType.PYRE, EssenceType.NETHER, tier
            );
            if (!this.shortLivedIllusion && owner != null && owner.isAlive()) {
                ddraig.net.entropica.util.EntityHelper.getPersistentData(owner)
                        .putLong(SpellLingeringZoneManager.TAG_PYRE_MELEE_BOOST_UNTIL, level.getGameTime() + 160L);
            }
        } else if (mat == EssenceType.PENUMBRA) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), this.shortLivedIllusion ? 4.0D : 10.0D, EssenceType.PENUMBRA, EssenceType.RADIANT, tier
            );
        } else if (mat == EssenceType.RIME) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), 10.0D, EssenceType.RIME, EssenceType.FROZEN, tier
            );
        } else if (mat == EssenceType.SOULFIRE && owner != null && owner.isAlive()) {
            float healAmt = owner.getMaxHealth() * 0.15F;
            SpellCombatReactionEngine.notifyMagicalHeal(owner, healAmt);
            owner.heal(healAmt);
        } else if (mat == EssenceType.NULL_U && owner != null && owner.isAlive()) {
            owner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, true, false));
            ddraig.net.entropica.util.EntityHelper.getPersistentData(owner)
                    .putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 80L);
        } else if (mat == EssenceType.CELESTIAL) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), this.shortLivedIllusion ? 6.0D : 10.0D, EssenceType.CELESTIAL, EssenceType.RADIANT, tier
            );
            if (!this.shortLivedIllusion) {
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        float full = ally.getMaxHealth();
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, full);
                        ally.heal(full);
                    }
                }
            }
        } else if (mat == EssenceType.PRISMATIC) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), this.shortLivedIllusion ? 6.0D : 10.0D, EssenceType.PRISMATIC, EssenceType.RADIANT, tier
            );
            if (!this.shortLivedIllusion) {
                // Section 6.53 Sacrificial Solar Relic: grants nearby party members 15.0s (300t) of complete damage invulnerability!
                long invulnUntil = level.getGameTime() + 300L;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        ddraig.net.entropica.util.EntityHelper.getPersistentData(ally)
                                .putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, invulnUntil);
                        SpellCombatReactionEngine.syncEntityBarrierState(level, ally, 1.15F, this.position());
                        level.sendParticles(ParticleTypes.END_ROD, ally.getX(), ally.getY(1.0D), ally.getZ(), 14, 0.35D, 0.45D, 0.35D, 0.04D);
                    }
                }
            }
        } else if (mat == EssenceType.EMPYREAN) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), this.shortLivedIllusion ? 6.0D : 14.0D, EssenceType.EMPYREAN, EssenceType.VITAE, tier
            );
            if (!this.shortLivedIllusion) {
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, 16.0F); // 8 full hearts!
                        ally.heal(16.0F);
                        level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.03D);
                    }
                }
            }
        } else if (mat == EssenceType.CATACLYSM) {
            // Section 6.55 Molten Core Beacon: 14m burst of stygian hellfire & compressed dark matter!
            double catRad = this.shortLivedIllusion ? 6.0D : Math.max(14.0D, burstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.25D, 0.0D), catRad, EssenceType.CATACLYSM, EssenceType.NETHER, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.75D, 0.0D), catRad, true, EssenceType.CATACLYSM, EssenceType.VOID, 46, tier
            );
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.8D), this.getZ(), 36, 3.2D, 0.8D, 3.2D, 0.06D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.9D), this.getZ(), 32, 3.0D, 0.8D, 3.0D, 0.06D);
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1F, 0.68F);
        } else if (mat == EssenceType.TERMINUS) {
            // Section 6.56 Unstable Cryogenic Elemental Effigy: 14m cataclysmic burst of absolute-zero frost!
            double termRad = this.shortLivedIllusion ? 6.0D : Math.max(14.0D, burstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.25D, 0.0D), termRad, EssenceType.TERMINUS, EssenceType.FROZEN, tier
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(0.8D), this.getZ(), 42, 3.2D, 0.8D, 3.2D, 0.06D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                    this.getX(), this.getY(0.8D), this.getZ(), 30, 2.8D, 0.7D, 2.8D, 0.08D);
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.25F, 0.62F);
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.65F, 1.55F);
        } else if (mat == EssenceType.ESCHATON) {
            // Section 6.57 Collapsing Micro-Nova Effigy (20s / 20m taunt): apocalyptic 20m reality tear!
            double eschRad = this.shortLivedIllusion ? 8.0D : Math.max(20.0D, burstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.25D, 0.0D), eschRad, EssenceType.ESCHATON, EssenceType.VOID, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.85D, 0.0D), eschRad, true, EssenceType.ESCHATON, EssenceType.UMBRAL, 52, tier
            );
            ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(
                    level, owner, this.blockPosition(), 6, EssenceType.ESCHATON, 32L);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.9D), this.getZ(), 54, 4.2D, 0.9D, 4.2D, 0.08D);
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.25F, 0.58F);
            level.playSound(null, this.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.10F, 0.65F);
        } else if (mat == EssenceType.APOTHEOSIS) {
            // Section 6.58 Radiant Ascendance Mirage (20s / 20m taunt): apocalyptic 20m healing supernova burst!
            double apoRad = this.shortLivedIllusion ? 8.0D : Math.max(20.0D, burstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.25D, 0.0D), apoRad, EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier
            );
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (ally != this && ally.isAlive() && (ally == owner || this.isFriendlyTo(ally))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 24.0F); // 12 full hearts!
                    ally.heal(24.0F);
                    level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(), 8, 0.35D, 0.35D, 0.35D, 0.03D);
                }
            }
            SpellLingeringZoneManager.spawnTypedEssenceOrb(level, this.position(), EssenceType.RADIANT, 3);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.9D), this.getZ(), 56, 4.2D, 0.9D, 4.2D, 0.09D);
            level.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.25F, 1.32F);
            level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.95F, 1.48F);
        } else if (mat == EssenceType.ENTROPICA) {
            // Section 6.59 Fractured Paradox Core (20s / 20m reality rupture) & Parallax Event-Horizon Decoy (4s / 8m balanced stasis nova)!
            double entRad = this.shortLivedIllusion ? 8.0D : Math.max(20.0D, burstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.25D, 0.0D), entRad, EssenceType.ENTROPICA, EssenceType.ASTRAL, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.85D, 0.0D), Math.min(7.0D, entRad * 0.45D), true, EssenceType.ENTROPICA, EssenceType.ASTRAL, 36, tier
            );
            SpellLingeringZoneManager.spawnTypedEssenceOrb(level, this.position(), EssenceType.ENTROPICA, this.shortLivedIllusion ? 1 : 3);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.9D), this.getZ(), 36, 3.8D, 0.9D, 3.8D, 0.08D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.9D), this.getZ(), 36, 3.8D, 0.9D, 3.8D, 0.08D);
            level.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.20F, 1.30F);
            level.playSound(null, this.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.10F, 1.18F);
        }

        // Environmental death-burst effects (Magma Caldera Nova crust, Storm fire extinguish + projectile deflect, Dust torch snuff, Soulfire soul sand, Astral EMP, Blood crimson mist)
        if (mat == EssenceType.MAGMA && getArchetype() != TriadArchetype.SENTRY) {
            // Section 6.14 Caldera Nova Effigy: detonates into an explosive 6m Caldera Shockwave Nova turning random floor cells molten!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.08D, 0.0D), Math.max(6.0D, burstRad), EssenceType.MAGMA, EssenceType.EARTH, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastGeyserPlume(
                    level, this.position(), 1.8D, 4.2D, EssenceType.MAGMA, EssenceType.NETHER, 44, tier
            );
            ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.autoSmeltDroppedItems(level, box);
            BlockPos c = this.blockPosition();
            Map<BlockPos, BlockState> savedCrust = new java.util.HashMap<>();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-4, -1, -4), c.offset(4, 0, 4))) {
                if (p.distSqr(c) > 18.0D) continue;
                BlockState bs = level.getBlockState(p);
                if (bs.isSolidRender() && level.getBlockState(p.above()).isAir()
                        && !bs.is(Blocks.MAGMA_BLOCK) && !bs.is(Blocks.LAVA)
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)
                        && level.random.nextFloat() < 0.35F) {
                    BlockPos imm = p.immutable();
                    savedCrust.put(imm, bs);
                    boolean contained = level.getBlockState(imm.below()).isSolidRender()
                            && level.getBlockState(imm.north()).isSolidRender()
                            && level.getBlockState(imm.south()).isSolidRender()
                            && level.getBlockState(imm.east()).isSolidRender()
                            && level.getBlockState(imm.west()).isSolidRender();
                    level.setBlockAndUpdate(imm, (contained ? Blocks.LAVA : Blocks.MAGMA_BLOCK).defaultBlockState());
                }
            }
            if (!savedCrust.isEmpty()) {
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        owner, this.position(), 6.0D, 100, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                        (lvl, z, el) -> {
                            if (el >= z.maxDurationTicks) {
                                for (Map.Entry<BlockPos, BlockState> e : savedCrust.entrySet()) {
                                    BlockState cur = lvl.getBlockState(e.getKey());
                                    if (cur.is(Blocks.MAGMA_BLOCK) || cur.is(Blocks.LAVA)
                                            || cur.is(Blocks.OBSIDIAN) || cur.is(Blocks.COBBLESTONE)) {
                                        lvl.setBlockAndUpdate(e.getKey(), e.getValue());
                                    }
                                }
                                return false;
                            }
                            return true;
                        }
                ));
            }
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 0.76F);
        } else if (mat == EssenceType.STORM && getArchetype() != TriadArchetype.SENTRY) {
            // Section 6.15 Tempest Nova Effigy: detonates into a 6m Tempest Nova Burst extinguishing fires & deflecting projectiles!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.08D, 0.0D), Math.max(6.0D, burstRad), EssenceType.STORM, EssenceType.AIR, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.8D, 0.0D), Math.max(6.0D, burstRad), false, EssenceType.STORM, EssenceType.WATER, 24, tier
            );
            BlockPos c = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-6, -1, -6), c.offset(6, 2, 6))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                    level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                }
            }
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
                if (proj.getOwner() == owner || proj.getOwner() == this) continue;
                Vec3 away = proj.position().subtract(this.position());
                Vec3 defl = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.55D) : proj.getDeltaMovement().scale(-1.2D);
                proj.setDeltaMovement(defl);
                proj.hasImpulse = true;
            }
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(0.85D), this.getZ(), 48, 1.8D, 0.65D, 1.8D, 0.16D);
            level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, this.getX(), this.getY(0.6D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.88F);
        } else if (mat == EssenceType.OVERGROWTH) {
            // Section 6.17 Overgrowth Floral Effigy: detonates into a 4-block 3D Root Snare upon destruction!
            double snareRad = Math.max(4.0D, burstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(
                    level, this.position(), snareRad, EssenceType.OVERGROWTH, EssenceType.EARTH, 82, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position(), snareRad, EssenceType.OVERGROWTH, EssenceType.EARTH, tier
            );
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 36, 1.15F, 0.09F),
                    this.getX(), this.getY(0.85D), this.getZ(), 28, 1.6D, 0.65D, 1.6D, 0.04D);
            level.playSound(null, this.blockPosition(), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 1.0F, 0.78F);
            level.playSound(null, this.blockPosition(), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.0F, 0.68F);
        } else if (mat == EssenceType.DUST) {
            // Section 6.18 Whiteout Chalk Silhouette: detonates into a 5-block Whiteout Nova upon destruction!
            double whiteoutRad = Math.max(5.0D, burstRad);
            box = this.getBoundingBox().inflate(whiteoutRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.08D, 0.0D), whiteoutRad, EssenceType.DUST, EssenceType.AIR, tier
            );
            ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.snuffTorchesAndFiresPublic(
                    level, this.blockPosition(), 5
            );
            ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.polishBlocksInAreaPublic(
                    level, this.blockPosition(), 3, false
            );
            level.playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 0.92F);
            level.playSound(null, this.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.95F, 0.85F);
        } else if (mat == EssenceType.SPORE && getArchetype() != TriadArchetype.SENTRY) {
            // Section 6.19 Mushroom Decoy: bursts into a 5-block calming and nauseating fungal spore cloud upon defeat!
            double sporeRad = Math.max(5.0D, burstRad);
            box = this.getBoundingBox().inflate(sporeRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.08D, 0.0D), sporeRad, EssenceType.SPORE, EssenceType.AIR, tier
            );
            ddraig.net.entropica.spell.mutation.element.SporeSpellMutationHandler.sproutMushroomsPublic(
                    level, this.blockPosition(), 3, false
            );
            level.playSound(null, this.blockPosition(), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 1.0F, 0.88F);
            level.playSound(null, this.blockPosition(), SoundEvents.FUNGUS_BREAK, SoundSource.PLAYERS, 0.95F, 0.92F);
        } else if (mat == EssenceType.SOULFIRE) {
            if (getArchetype() == TriadArchetype.DECOY) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, this.position().add(0.0D, 0.1D, 0.0D), 5.0D, EssenceType.SOULFIRE, EssenceType.UNDEAD, tier
                );
                level.playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
            }
            BlockPos c = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 0, 3))) {
                BlockState bs = level.getBlockState(p);
                if ((bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND))
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                    level.setBlockAndUpdate(p, Blocks.SOUL_SAND.defaultBlockState());
                }
            }
        } else if (mat == EssenceType.ASTRAL) {
            SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, this.blockPosition(), 5, 60);
            if (getArchetype() == TriadArchetype.DECOY) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                        level, this.position().add(0.0D, 0.9D, 0.0D), 5.0D, true, EssenceType.ASTRAL, EssenceType.LIGHTNING, 44, tier
                );
            }
        } else if (mat == EssenceType.NULL_R && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.31 Antimatter Effigy: collapses into a 5-block White Hole Antimatter Nova upon defeat!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.9D, 0.0D), 5.0D, true, EssenceType.NULL_R, EssenceType.VOID, 18, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.15D, 0.0D), 5.0D, EssenceType.NULL_R, EssenceType.VOID, tier
            );
            SpellLingeringZoneManager.pulseTemporaryLight(level, this.blockPosition().above(), 15, 45);
            level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 1.0F, 1.0F),
                    this.getX(), this.getY(0.9D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.playSound(null, this.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.38F);
        } else if (mat == EssenceType.NULL_U && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.32 Abyssal Silhouette Effigy: silently collapses into a 5-block Abyssal Gravity Shockwave upon defeat!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.9D, 0.0D), 5.0D, true, EssenceType.NULL_U, EssenceType.UMBRAL, 18, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.15D, 0.0D), 5.0D, EssenceType.NULL_U, EssenceType.UMBRAL, tier
            );
            level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY(0.8D), this.getZ(), 24, 1.6D, 0.5D, 1.6D, 0.04D);
        } else if (mat == EssenceType.DAWN && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.33 Daybreak Mirage Effigy: detonates into a concussive 6-block Sunrise Horizon Nova upon defeat!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.15D, 0.0D), 6.0D, EssenceType.DAWN, EssenceType.AIR, tier
            );
            for (net.minecraft.world.entity.AreaEffectCloud cloud : level.getEntitiesOfClass(
                    net.minecraft.world.entity.AreaEffectCloud.class, this.getBoundingBox().inflate(6.0D))) {
                cloud.discard();
            }
            BlockPos c = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-5, -1, -5), c.offset(5, 3, 5))) {
                if (level.getBlockState(p).is(Blocks.COBWEB)) {
                    level.destroyBlock(p, true);
                }
            }
            level.playSound(null, c, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.1F, 1.28F);
        } else if (mat == EssenceType.ABYSS && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.34 Hadopelagic Effigy: bursts into a 5m ink smoke cloud that silences abilities upon defeat!
            Vec3 inkCenter = this.position();
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, inkCenter.add(0.0D, 0.15D, 0.0D), 5.0D, EssenceType.ABYSS, EssenceType.WATER, tier
            );
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    owner, inkCenter, 5.0D, 60, level.getGameTime(), null, null, EssenceType.ABYSS, 1.0F,
                    (lvl, z, el) -> {
                        if (el > z.maxDurationTicks) return false;
                        if (el % 6 == 0) {
                            lvl.sendParticles(ParticleTypes.SQUID_INK, z.center.x, z.center.y + 0.8D, z.center.z,
                                    16, 2.2D, 0.6D, 2.2D, 0.03D);
                            lvl.sendParticles(ParticleTypes.BUBBLE_POP, z.center.x, z.center.y + 0.6D, z.center.z,
                                    10, 2.0D, 0.4D, 2.0D, 0.03D);
                        }
                        if (el % 20 == 0) {
                            LivingEntity zOwner = z.resolveCaster(lvl);
                            for (LivingEntity foe : lvl.getEntitiesOfClass(LivingEntity.class,
                                    new AABB(z.center.x - 5.0D, z.center.y - 1.5D, z.center.z - 5.0D,
                                            z.center.x + 5.0D, z.center.y + 3.0D, z.center.z + 5.0D), LivingEntity::isAlive)) {
                                if (foe == zOwner || SpellExecutionEngine.isFriendlyAllyForOasis(zOwner, foe)) continue;
                                if (zOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, zOwner, true)) continue;
                                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                                SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
                                SpellLingeringZoneManager.applySoaked(foe, 100);
                            }
                        }
                        return true;
                    }
            ));
        } else if (mat == EssenceType.AEGIS && getArchetype() == TriadArchetype.DECOY && !this.shortLivedIllusion) {
            // Section 6.35 Gilded Granite Effigy: detonates into a 6-block Bulwark Rampart Nova upon destruction!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.15D, 0.0D), 6.0D, EssenceType.AEGIS, EssenceType.RADIANT, tier
            );
            level.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.9F, 1.25F);
        } else if (mat == EssenceType.AURORA && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.36 Cryo-Prism Effigy (6m Borealis Curtain Nova) & Borealis Prism-Flash Step 2.0s Hologram Shatter!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.15D, 0.0D),
                    this.shortLivedIllusion ? 3.0D : 6.0D,
                    EssenceType.AURORA, EssenceType.FROZEN, tier
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(0.85D), this.getZ(), 24, 1.1D, 0.55D, 1.1D, 0.06D);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.95D), this.getZ(), 18, 1.0D, 0.55D, 1.0D, 0.06D);
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.95F, 1.35F);
        } else if (mat == EssenceType.MIRAGE && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.37 Phantom Mirror Effigy: explodes into a blinding Fata Morgana flash forcing attackers to fight each other!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.15D, 0.0D),
                    this.shortLivedIllusion ? 2.5D : 5.0D,
                    EssenceType.MIRAGE, EssenceType.ARID, tier
            );
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                    this.getX(), this.getY(0.85D), this.getZ(), 26, 1.2D, 0.5D, 1.2D, 0.04D);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.95D), this.getZ(), 16, 1.0D, 0.5D, 1.0D, 0.05D);
            level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.45F);
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
        } else if (mat == EssenceType.NATURE && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.4 Sylvan Pollen Effigy (A3): only the Decoy bursts into a 3D toxic spore nova on collapse!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.1D, 0.0D), Math.max(4.0D, burstRad), EssenceType.NATURE, null, tier
            );
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xB8FF52, 48, 1.15F, 0.05F),
                    this.getX(), this.getY(0.85D), this.getZ(), 42, 1.6D, 0.65D, 1.6D, 0.06D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFD83D, 44, 1.10F, 0.05F),
                    this.getX(), this.getY(0.95D), this.getZ(), 28, 1.4D, 0.65D, 1.4D, 0.05D);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(0.9D), this.getZ(), 24, 1.4D, 0.6D, 1.4D, 0.03D);
            level.playSound(null, this.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.95F, 0.78F);
        } else if (mat == EssenceType.EARTH && getArchetype() == TriadArchetype.DECOY) {
            // Section 6.5 Granite Effigy Decoy: violently shatters into a 3D ring of stone shrapnel & granite monoliths on collapse!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.08D, 0.0D), Math.max(4.2D, burstRad), EssenceType.EARTH, null, tier
            );
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRANITE.defaultBlockState()),
                    this.getX(), this.getY(0.9D), this.getZ(), 36, 1.2D, 0.6D, 1.2D, 0.16D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TERRACOTTA.defaultBlockState()),
                    this.getX(), this.getY(0.8D), this.getZ(), 24, 1.2D, 0.6D, 1.2D, 0.14D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), 0xFFC278, 42, 1.15F, 0.14F),
                    this.getX(), this.getY(0.85D), this.getZ(), 22, 1.4D, 0.6D, 1.4D, 0.07D);
            level.playSound(null, this.blockPosition(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.68F);
        } else if ((mat == EssenceType.FROZEN && getArchetype() == TriadArchetype.DECOY) || mat == EssenceType.GLACIAL) {
            // Section 6.6 Glacial Ice Effigy (Decoy-only combat destruction) & Section 6.16 Crystalline Freeze-Nova Effigy:
            // shatters into an exact 4.0-block spherical Freeze-Nova!
            double novaVisualRad = mat == EssenceType.FROZEN ? 4.0D : Math.max(4.0D, burstRad);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    this.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    mat, EssenceType.STASIS, (float) novaVisualRad, 64, tier
            );
            BlockPos c = this.blockPosition();
            Vec3 burstCenter = this.position();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-4, -2, -4), c.offset(4, 1, 4))) {
                if (p.distToCenterSqr(burstCenter) > 16.0D) continue;
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER)) {
                    level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                } else if (mat == EssenceType.GLACIAL && bs.is(Blocks.LAVA)) {
                    level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                }
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                    this.getX(), this.getY(0.9D), this.getZ(), 36, 1.3D, 0.6D, 1.3D, 0.16D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), 0x98DCF2, 46, 1.18F, 0.14F),
                    this.getX(), this.getY(0.85D), this.getZ(), 28, 1.5D, 0.6D, 1.5D, 0.08D);
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(0.85D), this.getZ(), 34, 1.6D, 0.65D, 1.6D, 0.07D);
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.1F, 0.85F);
        } else if (mat == EssenceType.NETHER && getArchetype() != TriadArchetype.SENTRY) {
            // Section 6.7 Infernal Eidolon Agent & Blazing Wicker Effigy: explodes into a violent fiery 3D burst upon death!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    this.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.NETHER, null, (float) Math.max(4.5D, burstRad), 64, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastGeyserPlume(
                    level, this.position(), 1.5D, 3.8D, EssenceType.NETHER, null, 54, tier
            );
            ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.autoSmeltDroppedItems(level, box);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.85D), this.getZ(), 28, 1.2D, 0.55D, 1.2D, 0.12D);
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.85D), this.getZ(), 44, 1.5D, 0.65D, 1.5D, 0.10D);
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.95F);
            BlockPos c = this.blockPosition();
            List<BlockPos> placedFires = new java.util.ArrayList<>();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 1, 3))) {
                if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.35F) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                    placedFires.add(p.immutable());
                }
            }
            if (!placedFires.isEmpty()) {
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        owner, this.position(), 3.5D, 100, level.getGameTime(), null, null, EssenceType.NETHER, 1.0F,
                        (lvl, z, el) -> {
                            if (el >= z.maxDurationTicks) {
                                for (BlockPos fp : placedFires) {
                                    if (lvl.getBlockState(fp).is(Blocks.FIRE)) {
                                        lvl.removeBlock(fp, false);
                                    }
                                }
                                return false;
                            }
                            return true;
                        }
                ));
            }
        }

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == this || foe == owner || !foe.isAlive() || this.isFriendlyTo(foe)) continue;
            switch (mat) {
                case AIR -> {
                    // Section 6.2 Zephyr Eidolon Decoy: non-damaging (0 HP) cyclone knockback gust skipping bosses!
                    if (getArchetype() == TriadArchetype.DECOY && !SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                foe, owner, this.spell != null && this.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        Vec3 diff = foe.position().subtract(this.position());
                        Vec3 horiz = new Vec3(diff.x, 0.0D, diff.z);
                        Vec3 away = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D)).scale(1.45D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(away.x, 0.72D, away.z));
                    }
                }
                case NATURE -> {
                    if (getArchetype() == TriadArchetype.DECOY) {
                        double maxR = Math.max(4.0D, burstRad);
                        double dx = foe.getX() - this.getX();
                        double dz = foe.getZ() - this.getZ();
                        if (dx * dx + dz * dz > maxR * maxR) {
                            continue;
                        }
                        boolean wardSafe = this.spell != null && this.spell.hasWardFriendlyFireSafeguard();
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)
                                || (wardSafe && ddraig.net.entropica.spell.mutation.element.NatureSpellMutationHandler.isFriendlyOrWoodlandAlly(foe, owner))) {
                            continue;
                        }
                        foe.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                        foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0));
                        float sporeBurstDmg = Math.max(3.5F + tier * 1.10F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.45F);
                        SpellCombatReactionEngine.hurtWithNatureMateriaAdvantage(
                                level, foe, owner != null ? owner : this, sporeBurstDmg
                        );
                    }
                }
                case EARTH -> {
                    if (getArchetype() != TriadArchetype.DECOY) break;
                    boolean wardSafe = this.spell != null && this.spell.hasWardFriendlyFireSafeguard();
                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) break;
                    float shrapnelDmg = Math.max(4.5F + tier * 1.25F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.55F);
                    SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(
                            level, foe, owner != null ? owner : this, shrapnelDmg, false, false
                    );
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 160L);
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = foe.position().subtract(this.position());
                        Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                        Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.15D, 0.38D, push.z * 1.15D));
                    }
                }
                case FROZEN, GLACIAL -> {
                    if (mat == EssenceType.FROZEN && getArchetype() != TriadArchetype.DECOY) break;
                    if (mat == EssenceType.FROZEN && this.distanceToSqr(foe) > 16.0D) break;
                    boolean wardSafe = this.spell != null && this.spell.hasWardFriendlyFireSafeguard();
                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) break;
                    float frostNovaDmg = Math.max(4.0F + tier * 1.15F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.50F);
                    if (mat == EssenceType.GLACIAL) {
                        SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(
                                level, foe, owner != null ? owner : this, frostNovaDmg, false, false, false
                        );
                        ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(
                                level, owner != null ? owner : this, foe, 50, this.spell, null
                        );
                    } else {
                        SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                                level, foe, owner != null ? owner : this, frostNovaDmg, false, false
                        );
                        ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(
                                level, owner != null ? owner : this, foe, 40, this.spell, null
                        );
                    }
                }
                case NETHER -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    foe.igniteForSeconds(6);
                    float hellfireDmg = Math.max(4.8F + tier * 1.35F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.58F);
                    SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(
                            level, foe, owner != null ? owner : this, hellfireDmg, false
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = foe.position().subtract(this.position());
                        Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                        Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.15D, 0.38D, push.z * 1.15D));
                    }
                }
                case UMBRAL -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                    float umbralBurst = Math.max(3.5F + tier * 1.0F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.45F);
                    SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                            level, foe, owner != null ? owner : this, umbralBurst, false, true
                    );
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL, level.getGameTime() + 60L);
                }
                case UNDEAD -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    float boneNovaDmg = Math.max(4.5F + tier * 1.30F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.58F);
                    SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                            level, foe, owner != null ? owner : this, boneNovaDmg, false, false, false
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = foe.position().subtract(this.position());
                        Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                        Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.25D, 0.42D, push.z * 1.25D));
                    }
                }
                case VOID -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.11 Void Singularity Silhouette Decoy: collapses into a micro-singularity implosion upon death!
                    float voidImplosionDmg = Math.max(4.4F + tier * 1.25F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.55F);
                    SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                            level, foe, owner != null ? owner : this, voidImplosionDmg, false, 0.30F, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 toCore = this.position().subtract(foe.position());
                        if (toCore.lengthSqr() > 0.04D) {
                            Vec3 inward = toCore.normalize().scale(1.32D);
                            SpellExecutionEngine.applySyncedMotion(foe, new Vec3(inward.x, 0.36D, inward.z));
                        }
                    }
                }
                case ARID -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.12 Dune-Dervish Heat Mirage Decoy: explodes into a blinding desert dust blast upon destruction!
                    float haboobBlastDmg = Math.max(4.0F + tier * 1.15F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.52F);
                    SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(
                            level, foe, owner != null ? owner : this, haboobBlastDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                    foe.clearFire();
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = foe.position().subtract(this.position());
                        Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                        Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.18D, 0.36D, push.z * 1.18D));
                    }
                }
                case LIGHTNING -> {
                    float empDmg = Math.max(4.2F + tier * 1.15F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.52F);
                    SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(
                            level, foe, owner != null ? owner : this, empDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                }
                case MAGMA -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.14 Caldera Nova Effigy: 6m volcanic eruption knocking mobs back, setting ablaze & dealing Magma Materia damage!
                    foe.igniteForSeconds(6);
                    float calderaDmg = Math.max(5.0F + tier * 1.35F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.60F);
                    SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(
                            level, foe, owner != null ? owner : this, calderaDmg, false, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = foe.position().subtract(this.position());
                        Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                        Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.32D, 0.45D, push.z * 1.32D));
                    }
                }
                case STORM -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.15 Tempest Nova Effigy: blasts mobs 8 blocks backward, soaks all entities & deals Storm Materia damage!
                    foe.clearFire();
                    SpellLingeringZoneManager.applySoaked(foe, 200);
                    float tempestDmg = Math.max(4.6F + tier * 1.25F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.56F);
                    SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(
                            level, foe, owner != null ? owner : this, tempestDmg, false, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = foe.position().subtract(this.position());
                        Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                        Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.85D, 0.48D, push.z * 1.85D));
                    }
                }
                case OVERGROWTH -> {
                    float rootSnareDmg = Math.max(4.0F + tier * 1.15F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.50F);
                    SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(
                            level, foe, owner != null ? owner : this, rootSnareDmg, false, true
                    );
                    long rootUntil = level.getGameTime() + 80L;
                    CompoundTag fData = ddraig.net.entropica.util.EntityHelper.getPersistentData(foe);
                    fData.putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BRIAR_ROOT_UNTIL, rootUntil);
                    fData.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, rootUntil);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                }
                case DUST -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.18 Whiteout Chalk Silhouette: 5-block Whiteout Nova blinding foes for 4s, wiping aggro & dealing Dust Materia damage!
                    float whiteoutDmg = Math.max(3.8F + tier * 1.15F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.50F);
                    SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(
                            level, foe, owner != null ? owner : this, whiteoutDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.clearFire();
                    ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.wipeMobAggroPublic(level, foe, 60L);
                }
                case SPORE -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.19 Mushroom Decoy: calming (aggro-wiping + pacifying) and nauseating fungal spores!
                    float sporeBurstDmg = Math.max(2.6F + tier * 0.85F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.35F);
                    SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(
                            level, foe, owner != null ? owner : this, sporeBurstDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    SpellLingeringZoneManager.applyPacified(foe, 100);
                    ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.wipeMobAggroPublic(level, foe, 80L);
                }
                case TAIGA -> {
                    // Section 6.20 Phantom Snow Stalker Decoy: blinds & freezes attackers in a frosted pine-needle whiteout burst when destroyed!
                    float taigaBurstDmg = (3.4F + tier * 0.90F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(
                            level, foe, owner != null ? owner : this, taigaBurstDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                    if (foe.canFreeze()) {
                        foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 50);
                    }
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, level.getGameTime() + 100L);
                }
                case OASIS -> {
                    // Section 6.21 Mirage Blossom Effigy Decoy: 5-block pacifying water & pollen burst upon defeat!
                    float oasisBurstDmg = (2.8F + tier * 0.80F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(
                            level, foe, owner != null ? owner : this, oasisBurstDmg, false, true
                    );
                    foe.clearFire();
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
                    SpellLingeringZoneManager.applySoaked(foe, 140);
                    SpellLingeringZoneManager.applyPacified(foe, 100);
                    ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.wipeMobAggroPublic(level, foe, 100L);
                }
                case VITAE -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.22 Heart-Husk Effigy Decoy: 5-block cellular shockwave harming & disorienting hostiles while restoring 6 hearts to players!
                    float vitaeBurstDmg = (2.8F + tier * 0.80F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(
                            level, foe, owner != null ? owner : this, vitaeBurstDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_VITAE_DISORIENTED_UNTIL, level.getGameTime() + 80L);
                }
                case BLOOD -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.23 Crimson Effigy Decoy: 5-block boiling blood burst dealing Blood Materia damage, blinding & bleeding enemies!
                    float bloodBurstDmg = (2.8F + tier * 0.80F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(
                            level, foe, owner != null ? owner : this, bloodBurstDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 80, 0));
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, level.getGameTime() + 80L);
                }
                case AURA -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.24 Spirit-Vane Effigy Decoy: 5-block vacuum implosion pulling enemies in & silencing for 3.0s (60t)!
                    float auraBurstDmg = (2.8F + tier * 0.80F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(
                            level, foe, owner != null ? owner : this, auraBurstDmg, false, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 inward = this.position().subtract(foe.position());
                        Vec3 normIn = inward.lengthSqr() > 1.0E-4D ? inward.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(normIn.x * 1.15D, 0.30D, normIn.z * 1.15D));
                    }
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                }
                case AMBER -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.25 Petrified Amber Effigy Decoy: 5-block shatter burst rooting hostiles for 3.0s (60t)!
                    float amberBurstDmg = (2.8F + tier * 0.80F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(
                            level, foe, owner != null ? owner : this, amberBurstDmg, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.99D, 60L);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 60L);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(0.0D, Math.min(0.0D, foe.getDeltaMovement().y), 0.0D));
                }
                case ECLIPSE -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.26 Penumbra Silhouette Effigy Decoy: 5-block pulling, blinding nova upon defeat!
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 inward = this.position().subtract(foe.position());
                        Vec3 pull = inward.lengthSqr() > 1.0E-4D ? inward.normalize().scale(1.15D) : Vec3.ZERO;
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(pull.x, 0.30D, pull.z));
                    }
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    float eclipseBurstDmg = (3.4F + tier * 0.95F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(
                            level, foe, owner != null ? owner : this, eclipseBurstDmg, false, true
                    );
                }
                case NULL_R -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.31 Antimatter Effigy Decoy: 5-block White Hole Antimatter Nova upon defeat!
                    Vec3 away = foe.position().subtract(this.position());
                    Vec3 blast = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.55D) : new Vec3(0.0D, 0.45D, 0.0D);
                    foe.setDeltaMovement(blast.x, 0.48D, blast.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    ddraig.net.entropica.spell.mutation.element.EclipseSpellMutationHandler.purgeBeneficialEffects(foe);
                    if (foe instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                    SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 4.75F + tier * 1.25F, false, true
                    );
                }
                case NULL_U -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.32 Abyssal Silhouette Effigy Decoy: silent 5-block Abyssal Gravity Shockwave upon defeat!
                    Vec3 toSelf = this.position().subtract(foe.position());
                    Vec3 inward = toSelf.lengthSqr() > 1.0E-4D ? toSelf.normalize().scale(1.18D) : Vec3.ZERO;
                    foe.setDeltaMovement(inward.x, 0.30D, inward.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 80);
                    foe.setLastHurtByMob(null);
                    if (foe instanceof Mob mob) {
                        mob.setTarget(null);
                        mob.setAggressive(false);
                    }
                    SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 4.65F + tier * 1.25F, false, true
                    );
                }
                case BLIGHT -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.27 Cadaverous Effigy Decoy: 5-block Cadaverous Briar Nova rooting enemies for 3.0s (60t) & leaving Wither Roses!
                    float blightBurstDmg = (3.25F + tier * 0.90F) * Math.max(0.5F, this.apparatusPotency);
                    SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(
                            level, foe, owner != null ? owner : this, blightBurstDmg, false, true
                    );
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.95D, 60L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 60L);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(0.0D, Math.min(0.0D, foe.getDeltaMovement().y), 0.0D));
                    BlockPos fp = foe.blockPosition();
                    if (level.getBlockState(fp).isAir()
                            && level.getBlockState(fp.below()).isSolidRender()
                            && !SpellExecutionEngine.isProtectedBlockOrContainer(level, fp, level.getBlockState(fp))) {
                        level.setBlockAndUpdate(fp, Blocks.WITHER_ROSE.defaultBlockState());
                    }
                }
                case ASTRAL -> {
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.25D);
                    foe.setDeltaMovement(inward.x, 0.46D, inward.z);
                    foe.hasImpulse = true;
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
                    SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 4.75F + tier * 1.25F, false, true
                    );
                }
                case SOULFIRE -> {
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.35D);
                    foe.setDeltaMovement(away.x, 0.45D, away.z);
                    foe.hasImpulse = true;
                    foe.igniteForSeconds(6);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 120L);
                    float dealt = SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 5.0F + tier * 1.3F, false, true
                    );
                    if (dealt > 0.0F && owner != null && owner.isAlive()) {
                        float heal = dealt * 0.15F;
                        SpellCombatReactionEngine.notifyMagicalHeal(owner, heal);
                        owner.heal(heal);
                    }
                }
                case VAPOR -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellLingeringZoneManager.applySoaked(foe, 180);
                    SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 4.25F + tier * 1.15F, false, true
                    );
                }
                case DAWN -> {
                    if (getArchetype() == TriadArchetype.SENTRY) break;
                    // Section 6.33 Daybreak Mirage Effigy Decoy: concussive Sunrise Horizon Nova (8m knockback, 3m airborne, 2x undead smite)!
                    Vec3 fromSelf = foe.position().subtract(this.position());
                    Vec3 away = fromSelf.lengthSqr() > 1.0E-4D ? fromSelf.normalize().scale(1.82D) : new Vec3(0.0D, 0.70D, 0.0D);
                    foe.setDeltaMovement(away.x, 0.72D, away.z);
                    foe.hasImpulse = true;
                    foe.removeEffect(MobEffects.INVISIBILITY);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                        foe.igniteForSeconds(6);
                    }
                    float dawnDmg = (5.0F + tier * 1.25F) * (foe.getType().is(EntityTypeTags.UNDEAD) ? 2.0F : 1.0F);
                    SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(
                            level, foe, owner != null ? owner : this, dawnDmg, false, true
                    );
                }
                case ABYSS -> {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 80);
                    SpellLingeringZoneManager.applySoaked(foe, 180);
                    foe.setLastHurtByMob(null);
                    if (foe instanceof net.minecraft.world.entity.Mob mob) {
                        mob.setTarget(null);
                        mob.setAggressive(false);
                    }
                    float abyssDmg = 4.2F + tier * 1.05F;
                    SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(
                            level, foe, owner != null ? owner : this, abyssDmg, false, true
                    );
                }
                case AEGIS -> {
                    if (this.shortLivedIllusion) break;
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.65D);
                    foe.setDeltaMovement(away.x, 0.45D, away.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                        foe.igniteForSeconds(5);
                    }
                    float aegisDmg = (5.0F + tier * 1.25F) * (foe.getType().is(EntityTypeTags.UNDEAD) ? 1.5F : 1.0F);
                    SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                            level, foe, owner != null ? owner : this, aegisDmg, false, true
                    );
                }
                case AURORA -> {
                    if (this.shortLivedIllusion) {
                        if (foe.distanceToSqr(this) <= 9.0D) {
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                            foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 30, foe.getTicksFrozen() + 40));
                        }
                        break;
                    }
                    Vec3 away = foe.position().subtract(this.position());
                    Vec3 blast = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.55D) : new Vec3(0.0D, 0.48D, 0.0D);
                    foe.setDeltaMovement(blast.x, 0.48D, blast.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 60);
                    float auroraDmg = (5.0F + tier * 1.25F) * (foe.getType().is(EntityTypeTags.UNDEAD) ? 1.5F : 1.0F);
                    SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                            level, foe, owner != null ? owner : this, auroraDmg, false, true
                    );
                }
                case MIRAGE -> {
                    if (this.shortLivedIllusion) {
                        if (foe.distanceToSqr(this) <= 9.0D) {
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                            SpellLingeringZoneManager.applyMirageTraitorFrenzy(foe, 50);
                        }
                        break;
                    }
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
                    SpellLingeringZoneManager.applyMirageTraitorFrenzy(foe, 100);
                    float mirageBurstDmg = 3.80F + tier * 0.95F;
                    SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(
                            level, foe, owner != null ? owner : this, mirageBurstDmg, false, true
                    );
                }
                case WRAITH -> {
                    SpellLingeringZoneManager.applyIntangiblePhase(foe, 60, false);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    float wraithBurstDmg = 4.50F + tier * 1.20F;
                    SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(
                            level, foe, owner != null ? owner : this, wraithBurstDmg, false, true
                    );
                }
                case BARROW -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.15D);
                    foe.setDeltaMovement(away.x, 0.32D, away.z);
                    foe.hasImpulse = true;
                    float boneShrapnelDmg = 5.00F + tier * 1.25F;
                    SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(
                            level, foe, owner != null ? owner : this, boneShrapnelDmg, false, true
                    );
                }
                case PYRE -> {
                    if (this.shortLivedIllusion) {
                        if (foe.distanceToSqr(this) <= 12.25D) {
                            foe.igniteForSeconds(3);
                            SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(
                                    level, foe, owner != null ? owner : this, 2.50F + tier * 0.55F, false, true
                            );
                        }
                        break;
                    }
                    // Section 6.40 Smoldering Hearth Effigy: detonates into a 6m Raging Wildfire Nova upon destruction!
                    Vec3 away = foe.position().subtract(this.position()).normalize().scale(1.65D);
                    foe.setDeltaMovement(away.x, 0.42D, away.z);
                    foe.hasImpulse = true;
                    foe.igniteForSeconds(6);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe).putLong(
                            SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 120L
                    );
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 120L);
                    float wildfireNovaDmg = 5.60F + tier * 1.40F;
                    SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(
                            level, foe, owner != null ? owner : this, wildfireNovaDmg, false, true
                    );
                }
                case PENUMBRA -> {
                    if (this.shortLivedIllusion) {
                        if (foe.distanceToSqr(this) <= 16.0D) {
                            Vec3 pull = this.position().subtract(foe.position()).scale(0.45D);
                            foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                            foe.hasImpulse = true;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                            SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                                    level, foe, owner != null ? owner : this, 2.40F + tier * 0.55F, false, true
                            );
                        }
                        break;
                    }
                    // Section 6.41 Volatile Eclipse Singularity Effigy: 10m event horizon implosion + 8.0s silence!
                    Vec3 inward = this.position().subtract(foe.position()).normalize().scale(1.45D);
                    foe.setDeltaMovement(inward.x, 0.35D, inward.z);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 160); // 8.0s silence!
                    SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 6.20F + tier * 1.55F, false, true
                    );
                }
                case RIME -> {
                    // Section 6.42 Brittle Permafrost Effigy: 10m Deep Freeze Shatter Nova (3.5s ice statue + -4 armor)!
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 70, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 6));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 70);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 6.20F + tier * 1.55F, false, true
                    );
                }
                case SPRING -> {
                    Vec3 delta = foe.position().subtract(this.position());
                    Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.48D, 0.0D);
                    foe.setDeltaMovement(away.x, 0.48D, away.z); // 8m knockback!
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    float smiteMult = foe.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                    SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                            level, foe, owner != null ? owner : this, (6.20F + tier * 1.55F) * smiteMult, false, true
                    );
                }
                case STATIC -> {
                    // Section 6.44 Catastrophic Tesla Nova: blasts hostiles 8m back, strips 4 armor, magnetizes/paralyzes for 4.0s (80t), and deals massive Static Materia damage!
                    Vec3 delta = foe.position().subtract(this.position());
                    Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.48D, 0.0D);
                    foe.setDeltaMovement(away.x, 0.48D, away.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                    SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 6.60F + tier * 1.65F, false, true
                    );
                }
                case SYLVAN -> {
                    // Section 6.45 Thorn-Bramble Effigy: strips 4 armor, encases hostiles in petrified root-cages for 4.0s (80t), and deals heavy Sylvan Materia damage!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                    float smite = foe.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                    SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                            level, foe, owner != null ? owner : this, (6.35F + tier * 1.58F) * smite, false, true
                    );
                }
                case MIASMA -> {
                    // Section 6.46 Decomposing Bog-Husk: shreds 3 armor points, blinds enemies for 4.0s (80t), Poison II & heavy Miasma Materia damage!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                    SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 5.85F + tier * 1.45F, false, true
                    );
                }
                case GENESIS -> {
                    // Section 6.47 Amniotic Water-Phantom: shreds 4 armor points, blinds 4.0s (80t), smites Undead/Oblivion/Wraith (+50%) & heavy Genesis Materia damage!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(foe) ? 1.50F : 1.0F;
                    SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                            level, foe, owner != null ? owner : this, (6.20F + tier * 1.52F) * smite, false, true
                    );
                }
                case OBLIVION -> {
                    // Section 6.48 Smoldering Wither-Effigy: shreds 4 armor points, Wither II & Blindness 5.0s (100t), black netherfire & heavy Oblivion damage!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    foe.igniteForSeconds(5);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
                    SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 6.50F + tier * 1.55F, false, true
                    );
                }
                case CHIMERA -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.igniteForSeconds(5);
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 80);
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    foe.hurtServer(level, level.damageSources().magic(), 6.5F + tier * 1.55F);
                }
                case AETHER -> {
                    // Section 6.50 Harmonized Quintessence Effigy: 12m atmospheric ether burst, -5 armor shred, 5.0s mid-air suspension & heavy true Aether damage!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
                    foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, Math.max(foe.getDeltaMovement().y, 0.45D), foe.getDeltaMovement().z * 0.3D);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 8.5F + tier * 1.9F, false, true
                    );
                }
                case ENTROPIC -> {
                    // Section 6.51 Smoldering Void-Husk Effigy: 12m entropic dark matter burst, -5 armor shred, Wither III + Blindness 5.0s & massive Entropic damage!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 8.6F + tier * 1.9F, false, true
                    );
                }
                case CELESTIAL -> {
                    // Section 6.52 Holy Genesis Effigy: 10m Supernova Dawnburst (-6 armor, 8.0s Blindness, Celestial smite & full party heal)!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0)); // 8.0s Blindness!
                    SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 8.6F + tier * 1.9F, false, true
                    );
                }
                case PRISMATIC -> {
                    // Section 6.53 Sacrificial Solar Relic / Prismatic Mirror Decoy: blinding dawn-burst + Prismatic smite!
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
                    SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(
                            level, foe, owner != null ? owner : this,
                            this.shortLivedIllusion ? (4.4F + tier * 0.90F) : (7.8F + tier * 1.75F), false, true
                    );
                }
                case EMPYREAN -> {
                    // Section 6.54 Golden Willow Effigy: 14m burst of healing dew & titan root snares,
                    // massive TRUE damage, -6 armor shred, & 5.0s (100t) root!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 100, 0));
                    SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 10.5F + tier * 2.15F, false, false, true, true
                    );
                }
                case CATACLYSM -> {
                    // Section 6.55 Molten Core Beacon: 14m burst of stygian hellfire & compressed dark matter,
                    // massive TRUE damage, -6 armor shred, & pulling all surviving hostiles into its collapsing singularity epicenter!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
                    Vec3 diff = this.position().subtract(foe.position());
                    if (diff.lengthSqr() > 1.0E-4D) {
                        Vec3 inward = diff.normalize().scale(1.65D);
                        foe.setDeltaMovement(inward.x, 0.35D, inward.z);
                        foe.hasImpulse = true;
                    }
                    foe.igniteForSeconds(6);
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 120L);
                    SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 10.8F + tier * 2.20F, false, false, true
                    );
                }
                case TERMINUS -> {
                    // Section 6.56 Unstable Cryogenic Elemental Effigy (14m burst): massive TRUE damage, -6 armor shred,
                    // & freezing all surviving hostiles solid in ice for 5.0 seconds!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
                    SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 10.5F + tier * 2.15F, false, false, true
                    );
                    if (foe.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(
                                level, owner != null ? owner : this, foe, 100, 0, null, null
                        );
                    }
                }
                case ESCHATON -> {
                    // Section 6.57 Collapsing Micro-Nova Effigy (20m reality tear): massive TRUE Eschaton damage,
                    // -8 armor shred, and pulls all surviving hostiles into its collapsing singularity epicenter!
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 220L);
                    Vec3 diff = this.position().subtract(foe.position());
                    if (diff.lengthSqr() > 1.0E-4D) {
                        Vec3 inward = diff.normalize().scale(1.85D);
                        foe.setDeltaMovement(inward.x, 0.38D, inward.z);
                        foe.hasImpulse = true;
                    }
                    SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 14.5F + tier * 2.65F, false, false, true
                    );
                }
                case APOTHEOSIS -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 220L);
                    foe.igniteForSeconds(5);
                    SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 14.5F + tier * 2.65F, false, false, true, true
                    );
                }
                case ENTROPICA -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 220L);
                    Vec3 diff = this.position().subtract(foe.position());
                    if (diff.lengthSqr() > 1.0E-4D) {
                        Vec3 inward = diff.normalize().scale(1.85D);
                        foe.setDeltaMovement(inward.x, 0.35D, inward.z);
                        foe.hasImpulse = true;
                    }
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0)); // 5.0s stun!
                    SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                            level, foe, owner != null ? owner : this, 15.5F + tier * 2.75F, false, false, true
                    );
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
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(0.9D), this.getZ(), 32, 1.3D, 0.65D, 1.3D, 0.05D);
            if (mat == EssenceType.SPORE) {
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x1A5923, 34, 1.15F, 0.09F),
                        this.getX(), this.getY(0.85D), this.getZ(), 24, 1.5D, 0.6D, 1.5D, 0.05D);
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xA88654, 34, 1.10F, 0.09F),
                        this.getX(), this.getY(0.95D), this.getZ(), 20, 1.5D, 0.6D, 1.5D, 0.05D);
            }
        } else if (mat == EssenceType.RADIANT || mat == EssenceType.DAWN || mat == EssenceType.AURORA) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.9D), this.getZ(), 28, 0.9D, 0.6D, 0.9D, 0.08D);
        } else if (mat == EssenceType.ARID || mat == EssenceType.DUST) {
            if (mat == EssenceType.ARID && getArchetype() != TriadArchetype.SENTRY) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                        level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                        this.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                        EssenceType.ARID, null, 5.0F, 44, tier
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastTornadoVortex(
                        level, this.position(), 2.2D, 3.2D, EssenceType.ARID, null, 36, tier
                );
                level.playSound(null, this.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.72F);
            }
            if (mat == EssenceType.DUST) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                        this.getX(), this.getY(0.85D), this.getZ(), 48, 1.8D, 0.7D, 1.8D, 0.10D);
                level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(0.85D), this.getZ(), 28, 1.5D, 0.6D, 1.5D, 0.08D);
            } else {
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        this.getX(), this.getY(0.8D), this.getZ(), 42, 1.2D, 0.65D, 1.2D, 0.12D);
            }
        } else if (mat == EssenceType.MAGMA) {
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.8D), this.getZ(), 36, 1.3D, 0.6D, 1.3D, 0.12D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                    this.getX(), this.getY(0.75D), this.getZ(), 28, 1.2D, 0.55D, 1.2D, 0.14D);
        } else if (mat == EssenceType.AMBER) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                    this.getX(), this.getY(0.8D), this.getZ(), 28, 0.8D, 0.5D, 0.8D, 0.1D);
        } else if (mat == EssenceType.ECLIPSE && getArchetype() != TriadArchetype.SENTRY) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position().add(0.0D, 0.35D, 0.0D), 5.0D, true, EssenceType.ECLIPSE, null, 38, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.08D, 0.0D), 5.0D, EssenceType.ECLIPSE, null, tier
            );
            level.playSound(null, this.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 0.82F);
        } else if (mat == EssenceType.BLIGHT && getArchetype() != TriadArchetype.SENTRY) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, this.position().add(0.0D, 0.08D, 0.0D), 5.0D, EssenceType.BLIGHT, null, tier
            );
            BlockPos dp = this.blockPosition();
            if (level.getBlockState(dp).isAir()
                    && level.getBlockState(dp.below()).isSolidRender()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, dp, level.getBlockState(dp))) {
                level.setBlockAndUpdate(dp, Blocks.WITHER_ROSE.defaultBlockState());
            }
            level.playSound(null, dp, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.68F);
        } else if (mat == EssenceType.VOID && getArchetype() != TriadArchetype.SENTRY) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, this.position(), 5.0D, true, EssenceType.VOID, null, 38, tier
            );
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    this.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.VOID, null, 5.0F, 42, tier
            );
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.9D), this.getZ(),
                    40, 0.9D, 0.6D, 0.9D, 0.14D);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.62F);
        } else {
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(0.8D), this.getZ(), 24, 0.7D, 0.5D, 0.7D, 0.08D);
        }
    }

    @Nullable
    private LivingEntity findNearestHostile(ServerLevel level, double radius) {
        if (this.summonerUuid == null && this.getTarget() != null && this.getTarget().isAlive()) {
            return this.getTarget();
        }
        LivingEntity owner = resolveSummoner(level);
        List<LivingEntity> list = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e.isAlive()
                        && e != this
                        && e != owner
                        && !this.isFriendlyTo(e)
                        && (e instanceof Enemy
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
            if (getArchetype() == TriadArchetype.SENTRY) {
                return null; // Cryptguard Ossuary Totem strictly launches seeking cursed skulls at LIVING targets!
            }
        }
        return list.get(0);
    }

    private EssenceType resolveUpkeepMateria() {
        EssenceType mat = getEffectiveMateria();
        if (mat != EssenceType.REGULAR) {
            return mat;
        }
        if (!this.spell.subSpells().isEmpty()) {
            SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
            EssenceType formMat = slot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
            if (formMat != EssenceType.REGULAR) {
                return formMat;
            }
        }
        return this.fallbackMateria != null ? this.fallbackMateria : EssenceType.REGULAR;
    }

    private boolean tryPerformPlayerUpkeepRefresh(ServerLevel serverLevel) {
        LivingEntity owner = resolveSummoner(serverLevel);
        if (!(owner instanceof Player player) || !player.isAlive()) {
            return false;
        }

        boolean damagedBeyondHalf = this.getHealth() < (this.getMaxHealth() * 0.5F);
        if (!player.isCreative()) {
            int tier = this.spell.getEffectiveTier();
            EssenceType fuelMat = resolveUpkeepMateria();
            long upkeepMfum = ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveUpkeepCostMfum(tier, damagedBeyondHalf);
            SpellFuelService.FuelCheckResult fed = SpellFuelService.consumeOrbisCellUpkeepFuel(
                    player,
                    tier,
                    fuelMat,
                    upkeepMfum,
                    false
            );
            if (!fed.success()) {
                return false;
            }
        }

        this.ageTicks = 0;
        this.sustainedPastCycleOne = true;
        if (damagedBeyondHalf) {
            float missing = this.getMaxHealth() - this.getHealth();
            if (missing > 0.0F) {
                SpellCombatReactionEngine.notifyMagicalHeal(this, missing);
            }
            this.setHealth(this.getMaxHealth());
            serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY(1.15D), this.getZ(),
                    6, 0.30D, 0.30D, 0.30D, 0.04D);
        }
        serverLevel.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY(1.1D), this.getZ(),
                12, 0.35D, 0.45D, 0.35D, 0.08D);
        return true;
    }

    private void expireAndDiscard(ServerLevel serverLevel) {
        EssenceType mat = getEffectiveMateria();
        // Section 6.6 Glacial Eidolon Decoy & Frostwalker Statue: shatter nova triggers ONLY on combat destruction, NOT natural lifetime expiry!
        if (mat == EssenceType.FROZEN) {
            if (isMutated()) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(0.8D), this.getZ(), 18, 0.45D, 0.45D, 0.45D, 0.03D);
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.65F, 1.38F);
            }
            this.discard();
            return;
        }
        if (getArchetype() == TriadArchetype.DECOY
                || (getArchetype() == TriadArchetype.AGENT && mat == EssenceType.NETHER)) {
            triggerElementalDeathBurst(serverLevel);
        }
        this.discard();
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
        output.store("UpkeepTicks", Codec.INT, this.upkeepTicks);
        output.store("SustainedPastCycleOne", Codec.BOOL, this.sustainedPastCycleOne);
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
        this.upkeepTicks = input.read("UpkeepTicks", Codec.INT).orElse(0);
        this.sustainedPastCycleOne = input.read("SustainedPastCycleOne", Codec.BOOL).orElse(false);
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
