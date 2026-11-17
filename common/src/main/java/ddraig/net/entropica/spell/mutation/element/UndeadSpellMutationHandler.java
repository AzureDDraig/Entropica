package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Undead Materia (Base Type #9)</b>
 * (Spellcrafting System Guide Sections 1.2B, 4.1–4.9, 5.1–5.7 &amp; 6.10):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>FROZEN (+1)</b> | Weak against <b>RADIANT (-1)</b>.</li>
 *     <li><b>Projectile</b>: <i>Necrotic Bone-Shard / Skull Missile</i> — fires a chattering 3D bleached skull /
 *     skeletal bone-shard that shatters on entity impact, terrain collision, or max-range airburst, dealing Undead
 *     Materia damage (+1 vs Frozen, -1 vs Radiant), splintering into bone shrapnel across surrounding foes, and
 *     inflicting Wither I for 4.0 seconds (80 ticks).</li>
 *     <li><b>Beam</b>: <i>Siphon Life / Necrotic Drain</i> — channels a sustained, dark shadowy vertebra tether
 *     (rigid ray or flexible 45° locking leash) that continuously drains the victim's life force, dealing Undead
 *     Materia damage and siphoning 25% of that damage back into both the caster's health and Undead Materia fuel pool.</li>
 *     <li><b>Radial Burst</b>: <i>Corpse Explosion / Bone Nova</i> — detonates in a violent 360° eruption of 18
 *     jagged curved 3D ribcage spikes in a 6-block+ radius, dealing distance-scaled Undead Materia damage, inflicting
 *     Wither II for 4.0 seconds (80 ticks), and knocking enemies backward.</li>
 *     <li><b>Touch / Strike</b>: <i>Vampiric Touch</i> — channels necrotic energy through a 3D bone-claw melee
 *     strike (stacking held weapon Materia Advantage), siphoning 30% of damage dealt directly into the caster's
 *     health pool (converting excess overheal at 100% HP into saturation/vitality).</li>
 *     <li><b>Aura / Self</b>: <i>Necrotic Shroud</i> — registers defense as Undead (+1 vs Frozen -&gt; 50% damage,
 *     -1 vs Radiant -&gt; 150% damage), causes unprovoked passive and hostile vanilla Undead mobs (zombies, skeletons,
 *     phantoms, drowned, husks, strays, etc.) to treat the caster (or anchored sanctuary allies) as friendly every
 *     tick, and steadily drains 1 HP/s from all living hostile creatures within 3 blocks.</li>
 *     <li><b>Glyph / Trap</b>: <i>Corpse Bloom Mine</i> — 1.0s arming delay, 10m (or Tier 5+ indefinite) lifetime,
 *     50% Sneak-Right-Click disarm refund, and Single-Use / Repeating Sentry support; invisible floor trap that
 *     triggers a violent 3D skull explosion inflicting Wither II and converting slain mobs directly into 3–5
 *     harvestable Bone Meal.</li>
 *     <li><b>Cone / Spray</b>: <i>Screaming Skull Swarm (Haunting Banshee Wave)</i> — blasts a conical spray of 14
 *     screaming 3D ethereal skulls, dealing Undead Materia damage, pushing mobs back, and panicking struck entities
 *     so they smoothly turn and flee in terror for 2.0 seconds (40 ticks).</li>
 *     <li><b>Rain / Skyfall</b>: <i>Spectral Soul Swarm (Grave Haunt Tempest)</i> — opens a cave-ceiling-adaptive
 *     ghostly aerial rift for 6.0 seconds (120 ticks) where 14 swooping 3D wraiths dive-bomb living enemies across a
 *     12-block+ area, dealing Undead damage, ignoring physical shields, and sapping movement speed by an exact 40%.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Cryptguard Eidolon Suite</i> ({@code #4D6453}) — desiccated tombstone green-grey
 *     triad with necrotic soul particles: health-siphoning bone-claw Agent, living-only seeking-skull Ossuary Sentry,
 *     and living-taunting Bone-Nova Skeletal Decoy.</li>
 *     <li><b>Flow (Slow)</b>: <i>Creeping Tomb-Dust</i> — sluggish creeping fog of grave dust adding +0.4 Materia
 *     Advantage (+1.4 total vs Frozen) and draining an exact 50% movement speed from entities caught along its wake.</li>
 *     <li><b>Blink / Step</b>: <i>Grave-Phasing</i> — teleports safely forward while leaving a persistent 3.0s
 *     clutching 3D skeletal ribcage at departure that roots pursuing enemies.</li>
 *     <li><b>Transmute / Break</b>: <i>Necrotic Fossilization</i> — petrifies organic wood into fossilized stone,
 *     dries foliage into dead bushes, and transmutes meat and organic dropped items directly into solid Bone Blocks
 *     (with up to 30% bulk fuel discount &amp; strict container protection).</li>
 *     <li><b>Affliction</b>: <i>Cadaverous Wither-Blight Hex</i> — persistent milk-immune rotting curse continuously
 *     dealing necrotic damage, permanently disabling all health regeneration, shredding 30% armor toughness,
 *     contagiously infecting adjacent living enemies upon victim death, and triggering <i>Necrotic Resonance</i>
 *     (+150% undead spawns within 32 blocks up to light level 7 on cursed players).</li>
 *     <li><b>Restoration</b>: <i>Metabolic Transfusion</i> — converts caster food saturation and hunger points
 *     directly into rapid burst health points.</li>
 * </ul>
 */
public final class UndeadSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Section 6.10 Cryptguard desiccated tombstone green-grey ({@code #4D6453}). */
    public static final int CRYPTGUARD_EIDOLON_HEX_COLOR = 0x4D6453;
    /** Bleached ossuary bone ivory ({@code #E6DFCF}). */
    public static final int BLEACHED_BONE_HEX_COLOR = 0xE6DFCF;
    /** Necrotic ectoplasm soul-green ({@code #5EE088}). */
    public static final int NECROTIC_SOUL_HEX_COLOR = 0x5EE088;

    private static final int VFX_SKULL_SHATTER_TICKS = 54;
    private static final int VFX_SIPHON_BEAM_TICKS = 16;
    private static final int VFX_BONE_NOVA_TICKS = 64;
    private static final int VFX_VAMPIRIC_SLASH_TICKS = 52;
    private static final int VFX_CORPSE_MINE_TICKS = 60;
    private static final int VFX_BANSHEE_CONE_TICKS = 56;
    private static final int VFX_SOUL_SWARM_TICKS = 132;
    private static final int VFX_CRYPT_SUMMON_TICKS = 56;
    private static final int VFX_RIBCAGE_SNARE_TICKS = 64;

    private static final String TAG_REPEATING_PULSES_LEFT = "SpellUndeadMinePulsesLeft";
    private static final String TAG_REPEATING_COOLDOWN_UNTIL = "SpellUndeadMineCooldownUntil";

    @Override
    public EssenceType materiaType() {
        return EssenceType.UNDEAD;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Creeping Tomb-Dust";
            default -> "Necrotic Ossuary " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Creeping Tomb-Dust: Sluggish, heavy creeping fog of grave dust (+0.4 Materia Advantage, +1.4 total vs Frozen) that drains 50% movement speed from targets along its wake.";
            default -> "Necrotic Ossuary Flow: Channels desiccated ossuary grave-winds across the spell's trajectory.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Necrotic Bone-Shard";
            case BEAM -> "Siphon Life";
            case RADIAL_BURST -> "Corpse Explosion (Bone Nova)";
            case TOUCH_STRIKE -> "Vampiric Touch";
            case AURA -> "Necrotic Shroud";
            case GLYPH -> "Corpse Bloom Mine";
            case CONE -> "Screaming Skull Swarm";
            case SKYFALL -> "Spectral Soul Swarm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Necrotic Bone-Shard / Skull Missile: Fires a flying skeletal shard or cursed skull that shatters on impact, dealing Undead Materia damage (+1 vs Frozen, -1 vs Radiant), splintering into bone shrapnel, and inflicting Wither I for 4s.";
            case BEAM -> "Siphon Life / Necrotic Drain: Sustained shadowy tether draining the victim's life force, dealing Undead Materia damage and siphoning 25% of that damage back into both caster health and Undead Materia fuel.";
            case RADIAL_BURST -> "Corpse Explosion / Bone Nova: Violent 6-block 360-degree eruption of 18 jagged ribcage spikes dealing Undead Materia damage, inflicting Wither II for 4s, and knocking enemies backward.";
            case TOUCH_STRIKE -> "Vampiric Touch: Channels necrotic energy through melee strikes, siphoning 30% of damage dealt directly into the caster's health pool.";
            case AURA -> "Necrotic Shroud: Registers defense as Undead; unprovoked undead monsters treat the caster as friendly, while living hostiles within 3m suffer 1 HP/s continuous life drain.";
            case GLYPH -> "Corpse Bloom Mine: Invisible floor trap that triggers a violent skull explosion when stepped on, inflicting Wither II and converting slain mobs directly into 3-5 harvestable Bone Meal.";
            case CONE -> "Screaming Skull Swarm: Blasts a conical spray of screaming ethereal skulls that deal Undead Materia damage, push mobs back, and cause them to turn and flee in terror for 2s.";
            case SKYFALL -> "Spectral Soul Swarm: Opens a ghostly aerial rift for 6s across a 12-block area where swooping wraiths dive-bomb living enemies through shields and sap 40% movement speed.";
            case SUMMON_AGENT -> "Cryptguard Eidolon: Desiccated tombstone green-grey (#4D6453) skeletal warrior attacking with withering bone claws that siphon health on hit.";
            case SUMMON_SENTRY -> "Cryptguard Ossuary Totem: Anchored ossuary turret launching seeking cursed skulls strictly at living targets.";
            case SUMMON_DECOY -> "Bone-Nova Skeletal Effigy: Ghostly skeletal effigy that taunts living mobs within 12m and detonates into a 6-block Bone Nova corpse explosion when destroyed.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Grave-Phasing";
            case TRANSMUTE, BREAK -> "Necrotic Fossilization";
            case AFFLICTION -> "Cadaverous Wither-Blight Hex";
            case RESTORATION -> "Metabolic Transfusion";
            default -> "Necrotic " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Grave-Phasing: Teleports safely forward while leaving a clutching 3D skeletal ribcage at departure that roots pursuing enemies for 3s.";
            case TRANSMUTE, BREAK -> "Necrotic Fossilization: Petrifies organic wood into fossilized stone, dries foliage into dead bushes, and transmutes meat and organic items directly into solid Bone Blocks.";
            case AFFLICTION -> "Cadaverous Wither-Blight Hex: Persistent rotting curse (immune to milk) dealing necrotic damage, disabling all health regeneration, shredding 30% armor toughness, contagiously spreading to living foes on death, and triggering +150% 32m Necrotic Resonance undead spawns on cursed players.";
            case RESTORATION -> "Metabolic Transfusion: Converts caster food saturation and hunger points directly into rapid burst healing.";
            default -> null;
        };
    }

    private static boolean slotExecutesHarmVerb(@Nullable SubSpellSlot slot) {
        if (slot == null) return false;
        if (slot.primaryEffect().effect().getRootFamily() == SpellCoreEffect.RootFamily.HARM) {
            return true;
        }
        return slot.secondaryEffect()
                .map(s -> s.effect().getRootFamily() == SpellCoreEffect.RootFamily.HARM)
                .orElse(false);
    }

    private static boolean isCreepingTombDust(SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        return spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && spell.flowPart().mutationEnabled()
                && spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.UNDEAD;
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — CREEPING TOMB-DUST (SLOW)
    // =========================================================================

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        // Section 6.10 Creeping Tomb-Dust: +0.4 Materia Advantage score (+1.4 total vs Frozen)!
        return flow == SpellFlowModifier.SLOW_HEAVY ? 0.4F : 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            SpellFlowModifier flow
    ) {
        if (flow != SpellFlowModifier.SLOW_HEAVY) {
            return;
        }
        // Creeping Tomb-Dust: sluggish, heavy creeping fog of grave dust draining an exact 50% movement speed along its wake!
        level.sendParticles(ParticleTypes.ASH, projectile.getX(), projectile.getY(), projectile.getZ(),
                6, 0.34D, 0.25D, 0.34D, 0.010D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), CRYPTGUARD_EIDOLON_HEX_COLOR, 30, 1.45F, 0.16F),
                projectile.getX(), projectile.getY(), projectile.getZ(),
                4, 0.32D, 0.24D, 0.32D, 0.008D
        );

        Entity owner = projectile.getOwner();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(2.25D), LivingEntity::isAlive)) {
            if (e == owner) continue;
            if (owner instanceof LivingEntity livOwner && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, livOwner, true)) {
                continue;
            }
            // Exact -50% movement speed drain via Attributes.MOVEMENT_SPEED!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(e, level, 0.50D, 60L);
        }
    }

    @Override
    public void onProjectileImpactOrExpireFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 impactPos,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        if (spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY) {
            return;
        }
        // Lingering 3.0s Creeping Tomb-Dust grave-fog pocket at impact draining 50% speed from crossing foes!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactPos, 2.8D, 60, level.getGameTime(), null, null, EssenceType.UNDEAD, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), CRYPTGUARD_EIDOLON_HEX_COLOR, 26, 1.40F, 0.14F),
                                zone.center.x, zone.center.y + 0.35D, zone.center.z,
                                6, 1.15D, 0.30D, 1.15D, 0.01D
                        );
                        srv.sendParticles(ParticleTypes.ASH, zone.center.x, zone.center.y + 0.45D, zone.center.z,
                                6, 1.10D, 0.30D, 1.10D, 0.01D);
                    }
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB fogBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 1.2D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, fogBox, LivingEntity::isAlive)) {
                            if (v == owner) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    v, owner, spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.50D, 45L);
                        }
                    }
                    return true;
                }
        ));
    }

    // =========================================================================
    // FORM MUTATION 1 — NECROTIC BONE-SHARD / SKULL MISSILE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            SubSpellSlot slot
    ) {
        level.sendParticles(ParticleTypes.SOUL, projectile.getX(), projectile.getY(), projectile.getZ(),
                1, 0.05D, 0.05D, 0.05D, 0.01D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 20, 0.75F, 0.06F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        Vec3 hitPos = hitEntity.getBoundingBox().getCenter();
        Vec3 normal = projectile.getDeltaMovement().lengthSqr() > 1.0E-4D
                ? projectile.getDeltaMovement().normalize()
                : new Vec3(0.0D, 1.0D, 0.0D);
        LivingEntity directLiv = hitEntity instanceof LivingEntity l ? l : null;
        detonateBoneShardSkullShatter(level, caster, hitPos, normal, directLiv, spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        detonateBoneShardSkullShatter(level, caster, blockHit.getLocation(), normal, null, spell, slot);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        detonateBoneShardSkullShatter(level, caster, projectile.position(), new Vec3(0.0D, 1.0D, 0.0D), null, spell, slot);
    }

    private static void detonateBoneShardSkullShatter(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            Vec3 impactNormal,
            @Nullable LivingEntity directHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        boolean tombDust = isCreepingTombDust(spell, EssenceType.UNDEAD);
        float flawPot = spell.flawProfile().getPotencyMultiplier();

        // Lingering 54-tick 3D exploding cursed skull + 10 flying bleached bone shrapnel splinters & #5EE088 ring!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                center, impactNormal, EssenceType.UNDEAD, null,
                1.65F, VFX_SKULL_SHATTER_TICKS, tier
        );
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                center.x, center.y, center.z, 24, 0.48D, 0.40D, 0.48D, 0.14D
        );
        level.sendParticles(ParticleTypes.SOUL, center.x, center.y + 0.15D, center.z,
                10, 0.35D, 0.35D, 0.35D, 0.05D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SKELETON_HURT, SoundSource.PLAYERS, 0.90F, 0.82F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.88F, 0.92F);

        if (spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }

        // 1. Direct struck target impact + Wither I for 4.0s (80t)
        if (directHit != null && directHit.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        directHit, caster, spell.hasWardFriendlyFireSafeguard()))) {
            directHit.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
            if (!slotExecutesHarmVerb(slot)) {
                float baseShardDmg = Math.max(3.4F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.75F) * flawPot;
                SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                        level, directHit, caster, baseShardDmg, tombDust, false, false, true
                );
            }
        }

        // 2. Bone shrapnel splintering into surrounding enemies within 3.0 blocks!
        double shrapnelRad = 3.0D;
        float shrapnelBase = Math.max(2.2F + tier * 0.85F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.36F) * flawPot;
        AABB shrapnelBox = new AABB(
                center.x - shrapnelRad, center.y - 2.0D, center.z - shrapnelRad,
                center.x + shrapnelRad, center.y + 2.5D, center.z + shrapnelRad
        );
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, shrapnelBox, LivingEntity::isAlive)) {
            if (adj == caster || adj == directHit) continue;
            double dist = adj.position().distanceTo(center);
            if (dist > shrapnelRad + 0.35D) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    adj, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            float falloff = (float) Mth.clamp(1.0D - (dist / shrapnelRad) * 0.67D, 0.33D, 1.0D);
            adj.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
            SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                    level, adj, caster, shrapnelBase * falloff, tombDust, false, false, true
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 2 — SIPHON LIFE / NECROTIC DRAIN (BEAM)
    // =========================================================================

    @Override
    public void onBeamFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            List<LivingEntity> hitEntities,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.10 Siphon Life / Necrotic Drain:
        // Sustained dark shadowy tether wrapped in 3D vertebrae rings (rigid ray or flexible 45° locking tether)
        // that drains the victim's life force, dealing Undead Materia damage and siphoning 25% of that damage
        // back into BOTH the caster's health AND Undead Materia fuel pool!
        int tier = spell.getEffectiveTier();
        boolean tombDust = isCreepingTombDust(spell, fallbackMateria);
        boolean alreadySiphonedViaHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float baseBeamDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.35F, apparatusPotency) * flawPot;

        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 muzzle = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D).add(normDir.scale(0.35D));
        Vec3 beamEnd = blockHit.getType() == HitResult.Type.BLOCK
                ? blockHit.getLocation()
                : muzzle.add(normDir.scale(maxReach));

        // Flexible 45° locking tether support (Section 4.2): stays latched onto target while strafing!
        List<LivingEntity> targets = new ArrayList<>(hitEntities);
        if (targets.isEmpty()) {
            AABB corridor = new AABB(muzzle, beamEnd).inflate(1.30D);
            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, corridor, LivingEntity::isAlive)) {
                if (cand == caster) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                Vec3 toCand = cand.getEyePosition().subtract(muzzle);
                double len = toCand.length();
                if (len <= maxReach + 0.5D && len > 1.0E-4D && toCand.scale(1.0D / len).dot(normDir) >= 0.707D
                        && distanceToRaySegment(cand.getBoundingBox().getCenter(), muzzle, normDir, maxReach) <= 1.20D) {
                    targets.add(cand);
                }
            }
        }

        Vec3 visualEnd = !targets.isEmpty()
                ? targets.getFirst().getBoundingBox().getCenter()
                : beamEnd;

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_BEAM,
                muzzle, visualEnd, EssenceType.UNDEAD, null,
                0.28F + tier * 0.02F, VFX_SIPHON_BEAM_TICKS, tier
        );

        Set<UUID> seen = new HashSet<>();
        boolean drainedAny = false;
        for (LivingEntity hit : targets) {
            if (!hit.isAlive() || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            drainedAny = true;
            hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));

            // Stream glowing #5EE088 soul orbs along the tether from the victim straight back into the caster!
            Vec3 fromVictim = hit.getBoundingBox().getCenter();
            Vec3 toCaster = caster.getBoundingBox().getCenter();
            Vec3 flowVel = toCaster.subtract(fromVictim).normalize().scale(0.18D);
            for (int i = 1; i <= 4; i++) {
                Vec3 pt = fromVictim.lerp(toCaster, i * 0.22D);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 20, 0.86F, 0.06F),
                        pt.x, pt.y, pt.z, 1, 0.05D, 0.05D, 0.05D, 0.01D
                );
            }
            level.sendParticles(ParticleTypes.SOUL, fromVictim.x + flowVel.x, fromVictim.y + flowVel.y, fromVictim.z + flowVel.z,
                    2, 0.12D, 0.15D, 0.12D, 0.02D);

            if (!spell.prefix().enforcesZeroEntityHarm() && !alreadySiphonedViaHarm) {
                float dealt = SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                        level, hit, caster, Math.max(2.0F, baseBeamDmg * 0.70F), tombDust, false, false
                );
                if (dealt > 0.0F && caster.isAlive()) {
                    float siphonHeal = dealt * 0.25F;
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, siphonHeal);
                    caster.heal(siphonHeal);
                    if (caster instanceof Player player) {
                        float dmgRatio = Math.clamp(dealt / Math.max(1.0F, MateriaStageTable.getBaseCombatDamageHp(tier)), 0.25F, 2.50F);
                        long fuelRestore = Math.max(8L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25F * dmgRatio));
                        SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.UNDEAD, fuelRestore);
                    }
                }
            }
        }
        if (drainedAny) {
            level.playSound(null, caster.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.42F, 1.45F);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — CORPSE EXPLOSION / BONE NOVA (RADIAL BURST)
    // =========================================================================

    @Override
    public void onRadialBurstFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 center,
            double radius,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.10 Corpse Explosion / Bone Nova:
        // Violent 360-degree eruption of 18 jagged curved 3D ribcage spikes in a 6-block+ radius,
        // dealing Undead Materia damage, inflicting Wither II for 4.0s (80t), and knocking enemies backward!
        double effRadius = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean tombDust = isCreepingTombDust(spell, fallbackMateria);
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float baseNovaDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.40F, apparatusPotency) * 0.78F * flawPot;

        // Lingering 64-tick (3.2s) 3D eruption of 18 curved bleached ribcage spikes & #5EE088 necrotic shock-rings!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UNDEAD, null, (float) effRadius, VFX_BONE_NOVA_TICKS, tier
        );

        AABB box = new AABB(
                center.x - effRadius, center.y - 2.2D, center.z - effRadius,
                center.x + effRadius, center.y + 3.2D, center.z + effRadius
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (entity == caster) continue;
            double dist = entity.position().distanceTo(center);
            if (dist > effRadius + 0.45D) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, effRadius)) * 0.67D, 0.33D, 1.0D);
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));

            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                        level, entity, caster, Math.max(3.0F, baseNovaDmg * falloff), tombDust, false, false, true
                );
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, entity)) {
                Vec3 delta = entity.position().subtract(center);
                Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                SpellExecutionEngine.applySyncedMotion(entity, new Vec3(push.x * 1.25D, 0.42D, push.z * 1.25D));
            } else if (!spell.prefix().enforcesZeroEntityHarm()) {
                // Section 5.2 Boss Kinetic Compensation!
                SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                        level, entity, caster, (2.2F + tier * 0.75F) * falloff, tombDust, false, false, true
                );
            }
        }

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                center.x, center.y + 0.45D, center.z, 38, 1.85D, 0.55D, 1.85D, 0.14D
        );
        level.sendParticles(ParticleTypes.SOUL, center.x, center.y + 0.55D, center.z,
                20, 1.65D, 0.50D, 1.65D, 0.05D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.85F, 1.15F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BONE_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.75F);
    }

    // =========================================================================
    // FORM MUTATION 4 — VAMPIRIC TOUCH (TOUCH / MELEE STRIKE)
    // =========================================================================

    @Override
    public void onTouchStrikeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            List<LivingEntity> hitEntities,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.10 Vampiric Touch:
        // Melee strike channels necrotic energy (stacking held weapon Materia Advantage & 50% sweeping cleave!)
        // that siphons 30% of the damage dealt directly into the caster's health pool!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double reach = Math.max(2.6D, SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.TOUCH_STRIKE, tier));

        // Lingering 52-tick 3D bone-claw necrotic crescent slash!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir,
                EssenceType.UNDEAD, null, (float) reach, VFX_VAMPIRIC_SLASH_TICKS, tier
        );

        if (hitEntities.isEmpty()) {
            return;
        }

        boolean tombDust = isCreepingTombDust(spell, fallbackMateria);
        boolean alreadySiphonedViaHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float baseTouchDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, apparatusPotency) * flawPot;
        boolean landed = false;
        float totalHeal = 0.0F;

        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (!hit.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            float cleaveScale = (i == 0) ? 1.0F : 0.50F;
            hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            landed = true;

            if (!spell.prefix().enforcesZeroEntityHarm() && !alreadySiphonedViaHarm) {
                float dealt = SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                        level, hit, caster, Math.max(2.5F, baseTouchDmg * 0.75F) * cleaveScale, tombDust, true, false, true
                );
                if (dealt > 0.0F) {
                    totalHeal += dealt * 0.30F;
                }
            }
        }

        if (totalHeal > 0.0F && caster.isAlive()) {
            boolean alreadyFullHp = caster.getHealth() >= caster.getMaxHealth() - 0.1F;
            SpellCombatReactionEngine.notifyMagicalHeal(caster, totalHeal);
            caster.heal(totalHeal);
            if (alreadyFullHp && caster instanceof Player player) {
                player.getFoodData().eat(Math.min(3, 1 + tier / 4), 0.4F);
            }
        }

        if (landed) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 24, 0.92F, 0.08F),
                    caster.getX(), caster.getY(0.85D), caster.getZ(), 9, 0.32D, 0.32D, 0.32D, 0.03D
            );
            level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY(1.05D), caster.getZ(),
                    3, 0.25D, 0.25D, 0.25D, 0.02D);
            level.playSound(null, caster.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.55F, 1.30F);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — NECROTIC SHROUD (AURA / SELF)
    // =========================================================================

    @Override
    public void onAuraFormCast(
            ServerLevel level,
            LivingEntity caster,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.10 Necrotic Shroud:
        // Registers defense as Undead (+1 vs Frozen -> 50% damage, -1 vs Radiant -> 150% damage),
        // causes passive and hostile vanilla Undead mobs to treat the caster as friendly every tick,
        // and steadily drains 1 HP/s from all living hostile creatures within 3 blocks!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        // Immediately calm unprovoked nearby Undead mobs on activation!
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(20.0D), LivingEntity::isAlive)) {
            if (!mob.getType().is(EntityTypeTags.UNDEAD)) continue;
            if (mob.getLastHurtByMob() == caster || caster.getLastHurtMob() == mob) continue;
            if (mob instanceof NeutralMob neutral) {
                neutral.stopBeingAngry();
                neutral.setPersistentAngerTarget(null);
            }
            mob.setTarget(null);
            mob.setAggressive(false);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.75F, null);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                caster.position().add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UNDEAD, null, 3.0F, 54, tier
        );
        level.sendParticles(ParticleTypes.SOUL, caster.getX(), caster.getY(0.8D), caster.getZ(),
                18, 0.55D, 0.55D, 0.55D, 0.03D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 30, 1.05F, 0.10F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 12, 0.65D, 0.50D, 0.65D, 0.02D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.65F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 6 — CORPSE BLOOM MINE (GLYPH / TRAP)
    // =========================================================================

    @Override
    public boolean onGlyphFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 glyphCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 4.6 & 6.10 Corpse Bloom Mine:
        // - Invisible floor trap (1.0s arming delay, 10m or Tier 5+ indefinite lifetime, 50% Sneak-Right-Click disarm)
        // - Supports both Single-Use Detonator (100%) and Repeating Glyphic Sentry (3 pulses @ 60%, 2s re-arm)
        // - Triggers a violent 3D skull explosion inflicting Wither II and converting slain mobs directly into 3-5 Bone Meal!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceBlock = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(glyphCenter.x, surfaceBlock.getY() + 1.02D, glyphCenter.z);
        double padRad = Math.max(2.6D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeatingSentry = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        CompoundTag casterTag = EntityHelper.getPersistentData(caster);
        String pulseKey = TAG_REPEATING_PULSES_LEFT + "_" + level.getGameTime();
        String cdKey = TAG_REPEATING_COOLDOWN_UNTIL + "_" + level.getGameTime();
        if (repeatingSentry) {
            casterTag.putInt(pulseKey, 3);
            casterTag.putLong(cdKey, 0L);
        }

        // Subtle placement ossuary dust puff (remains completely invisible on the floor afterward!)
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_EIHWAZ.get(), CRYPTGUARD_EIDOLON_HEX_COLOR, 26, 0.85F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 3, 0.35D, 0.05D, 0.35D, 0.005D
        );
        level.playSound(null, BlockPos.containing(snappedCenter), SoundEvents.BONE_BLOCK_PLACE, SoundSource.PLAYERS, 0.38F, 0.75F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, padRad, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 20) return true; // 1.0s arming delay

                    LivingEntity owner = zone.resolveCaster(srv);
                    long now = srv.getGameTime();
                    if (repeatingSentry && owner != null) {
                        long cdUntil = EntityHelper.getPersistentData(owner).getLong(cdKey).orElse(0L);
                        if (now < cdUntil) return true;
                    }

                    AABB triggerBox = new AABB(
                            zone.center.x - 2.2D, zone.center.y - 1.0D, zone.center.z - 2.2D,
                            zone.center.x + 2.2D, zone.center.y + 2.3D, zone.center.z + 2.2D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            e, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard()))
                    );
                    if (stepped.isEmpty()) return true;

                    float pulseScale = repeatingSentry ? 0.60F : 1.0F;
                    double blastRad = Math.max(4.0D, zone.radius + 1.2D);
                    AABB blastBox = new AABB(
                            zone.center.x - blastRad, zone.center.y - 1.5D, zone.center.z - blastRad,
                            zone.center.x + blastRad, zone.center.y + 3.2D, zone.center.z + blastRad
                    );
                    long boneMealWindow = now + 140L;
                    boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                    boolean tombDust = zone.spell != null && isCreepingTombDust(zone.spell, zone.fallbackMateria);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    float baseMineDmg = Math.max(4.8F + tier * 1.40F,
                            MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.50F, zone.apparatusPotency)) * flawPot * pulseScale;

                    // Violent 60-tick 3D skull & ribcage explosion!
                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                            zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.UNDEAD, null, (float) blastRad, VFX_CORPSE_MINE_TICKS, tier
                    );
                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                            zone.center.add(0.0D, 0.55D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.UNDEAD, null, 2.35F, VFX_CORPSE_MINE_TICKS, tier
                    );
                    srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, zone.center.x, zone.center.y + 0.6D, zone.center.z,
                            30, 0.95D, 0.55D, 0.95D, 0.07D);
                    srv.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                            zone.center.x, zone.center.y + 0.5D, zone.center.z, 36, 1.0D, 0.5D, 1.0D, 0.15D
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.95F, 0.95F);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.72F, 1.25F);

                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                        if (v == owner) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        CompoundTag vData = EntityHelper.getPersistentData(v);
                        vData.putLong(SpellLingeringZoneManager.TAG_CORPSE_BLOOM_BONEMEAL_UNTIL, boneMealWindow);
                        v.addEffect(new MobEffectInstance(MobEffects.WITHER, (int) (90 * pulseScale), 1));

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * pulseScale
                            );
                        }
                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                            SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                                    srv, v, owner, baseMineDmg, tombDust, false, false, true
                            );
                        }
                        if (!v.isAlive() && !(v instanceof Player) && vData.contains(SpellLingeringZoneManager.TAG_CORPSE_BLOOM_BONEMEAL_UNTIL)) {
                            vData.remove(SpellLingeringZoneManager.TAG_CORPSE_BLOOM_BONEMEAL_UNTIL);
                            Block.popResource(srv, v.blockPosition(), new ItemStack(Items.BONE_MEAL, 3 + srv.random.nextInt(3)));
                        }
                    }

                    if (repeatingSentry && owner != null) {
                        CompoundTag oTag = EntityHelper.getPersistentData(owner);
                        int rem = oTag.getInt(pulseKey).orElse(1) - 1;
                        if (rem > 0) {
                            oTag.putInt(pulseKey, rem);
                            oTag.putLong(cdKey, now + 40L); // 2.0s re-arm delay (Section 4.6)
                            return true;
                        }
                        oTag.remove(pulseKey);
                        oTag.remove(cdKey);
                    }
                    return false;
                }
        ).markDisarmableGlyph());
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — SCREAMING SKULL SWARM (CONE / SPRAY)
    // =========================================================================

    @Override
    public void onConeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double coneRange,
            List<LivingEntity> hitTargets,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.10 Screaming Skull Swarm (Haunting Banshee Wave):
        // Blasts a conical spray of 14 screaming 3D ethereal skulls in front of the caster;
        // deals Undead Materia damage, pushes mobs, and panics struck entities so they turn and flee in terror for 2.0s!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double effRange = Math.max(8.0D, coneRange);
        Vec3 origin = caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D);

        // Lingering 56-tick 3D wave of 14 screaming skulls & #5EE088 banshee sonic shock-rings!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                origin, normDir, EssenceType.UNDEAD, null,
                (float) effRange, VFX_BANSHEE_CONE_TICKS, tier
        );

        boolean tombDust = isCreepingTombDust(spell, fallbackMateria);
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float baseConeDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.40F, apparatusPotency) * 0.75F * flawPot;

        Set<UUID> seen = new HashSet<>();
        List<LivingEntity> candidates = new ArrayList<>(hitTargets);
        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(effRange)).inflate(effRange * 0.55D);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (cand == caster) continue;
            Vec3 to = cand.getBoundingBox().getCenter().subtract(origin);
            double dist = to.length();
            if (dist <= effRange + 0.4D && dist > 1.0E-4D && to.scale(1.0D / dist).dot(normDir) >= 0.50D) {
                candidates.add(cand);
            }
        }

        for (LivingEntity hit : candidates) {
            if (!hit.isAlive() || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                        level, hit, caster, Math.max(2.5F, baseConeDmg), tombDust, false, false, true
                );
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normDir.x * 1.25D, 0.32D, normDir.z * 1.25D));
                SpellLingeringZoneManager.applyFearFlee(hit, caster.position(), 40);
            }
            level.sendParticles(ParticleTypes.SOUL, hit.getX(), hit.getY(0.95D), hit.getZ(),
                    6, 0.25D, 0.30D, 0.25D, 0.03D);
        }

        level.playSound(null, caster.blockPosition(), SoundEvents.GHAST_SCREAM, SoundSource.PLAYERS, 0.72F, 1.35F);
        level.playSound(null, caster.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.55F, 1.55F);
    }

    // =========================================================================
    // FORM MUTATION 8 — SPECTRAL SOUL SWARM (SKYFALL)
    // =========================================================================

    @Override
    public boolean onSkyfallFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 impactCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.10 Spectral Soul Swarm (Grave Haunt Tempest):
        // Opens a cave-ceiling-adaptive ghostly aerial rift for 6.0 seconds (120t) across a 12-block+ area
        // where 14 swooping 3D wraiths dive-bomb living enemies, dealing Undead damage, ignoring physical shields,
        // and sapping target movement speed by an exact 40%!
        int tier = spell.getEffectiveTier();
        double hauntRad = Math.max(6.0D, radius); // >= 6.0m radius = 12.0m diameter!
        BlockPos surfacePos = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 anchoredCenter = new Vec3(impactCenter.x, surfacePos.getY() + 1.0D, impactCenter.z);
        double riftClearance = probeOverheadClearance(level, BlockPos.containing(anchoredCenter), 9);

        // Broadcast 132-tick (6.6s) cave-adaptive ghostly aerial rift & 14 swooping 3D hooded spectral wraiths!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                anchoredCenter, new Vec3(0.0D, -1.0D, 0.0D),
                EssenceType.UNDEAD, null, (float) hauntRad, VFX_SOUL_SWARM_TICKS, tier
        );
        level.playSound(null, BlockPos.containing(anchoredCenter), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.55F, 1.55F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, hauntRad, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        double topY = zone.center.y + Math.max(2.4D, riftClearance - 0.6D);
                        srv.sendParticles(ParticleTypes.SOUL, zone.center.x, topY, zone.center.z,
                                14, zone.radius * 0.52D, 1.1D, zone.radius * 0.52D, 0.04D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 26, 1.05F, 0.10F),
                                zone.center.x, topY - 0.6D, zone.center.z,
                                9, zone.radius * 0.48D, 1.0D, zone.radius * 0.48D, 0.02D
                        );
                    }

                    // Swooping wraith dive-bomb wave every 15 ticks (8 waves over 6.0s)!
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                        boolean tombDust = zone.spell != null && isCreepingTombDust(zone.spell, zone.fallbackMateria);
                        float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                        float waveDmg = Math.max(2.4F + tier * 0.65F,
                                MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.35F, zone.apparatusPotency) * 0.34F) * flawPot;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.0D, riftClearance + 0.5D), zone.center.z + zone.radius
                        );
                        boolean struckAny = false;
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            // Swooping wraiths strictly dive-bomb LIVING enemies!
                            if (v.getType().is(EntityTypeTags.UNDEAD)) continue;
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            double dist = Math.sqrt(dx * dx + dz * dz);
                            if (dist > zone.radius + 0.5D) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }

                            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, zone.radius)) * 0.67D, 0.33D, 1.0D);

                            // Ignore physical shields!
                            if (v.isBlocking()) {
                                if (v instanceof Player p) {
                                    ItemStack shield = p.getUseItem();
                                    p.stopUsingItem();
                                    if (!shield.isEmpty()) {
                                        p.getCooldowns().addCooldown(shield, 80);
                                    }
                                } else {
                                    v.stopUsingItem();
                                }
                            }

                            // Sap target movement speed by an exact 40%!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.40D, 50L);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.34F * falloff
                                );
                            }
                            if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                                SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                                        srv, v, owner, waveDmg * falloff, tombDust, false, true
                                );
                            }
                            srv.sendParticles(ParticleTypes.SOUL, v.getX(), v.getY(0.9D), v.getZ(),
                                    5, 0.22D, 0.35D, 0.22D, 0.03D);
                            struckAny = true;
                        }
                        if (struckAny) {
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 0.68F, 0.85F);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — CRYPTGUARD EIDOLON SUITE (AGENT / SENTRY / DECOY)
    // =========================================================================

    @Override
    public void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.UNDEAD.name());
        tag.putInt("SpellEidolonTintRGB", CRYPTGUARD_EIDOLON_HEX_COLOR);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SUMMON_PILLAR,
                spawnPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UNDEAD, null, (float) Math.max(1.80D, radius * 0.60D), VFX_CRYPT_SUMMON_TICKS, tier
        );
        level.sendParticles(ParticleTypes.SOUL, spawnPos.x, spawnPos.y + 0.85D, spawnPos.z,
                24, 0.50D, 0.60D, 0.50D, 0.05D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 32, 1.05F, 0.08F),
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 14, 0.45D, 0.55D, 0.45D, 0.02D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BONE_BLOCK_PLACE, SoundSource.PLAYERS, 0.85F, 0.82F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT SYNERGIES — GRAVE-PHASING, FOSSILIZATION, HEX & TRANSFUSION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.10 Grave-Phasing:
        // Teleports the caster safely forward, leaving a clutching 3D root cage of 12 skeletal ribs at the departure
        // point that roots pursuing enemies for 3.0 seconds (60 ticks)!
        double cageRadius = 2.8D;
        SpellWorldVfxManager.broadcastWardShell(level, departurePos, cageRadius, EssenceType.UNDEAD, null, VFX_RIBCAGE_SNARE_TICKS, potencyTier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, departurePos, cageRadius + 0.4D, EssenceType.UNDEAD, null, VFX_RIBCAGE_SNARE_TICKS, potencyTier);

        rootPursuersInDepartureRibcage(level, caster, departurePos, cageRadius, 60, potencyTier);

        // Active 3.0s (60t) departure Ribcage Snare Zone so chasers running into the ribs after cast also get clutched!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, cageRadius, 60, level.getGameTime(), null, null, EssenceType.UNDEAD, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed > 0 && elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        int remTicks = Math.max(20, zone.maxDurationTicks - elapsed);
                        rootPursuersInDepartureRibcage(srv, owner, zone.center, zone.radius, remTicks, potencyTier);
                    }
                    return true;
                }
        ));

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                departurePos.x, departurePos.y + 0.5D, departurePos.z, 24, 0.65D, 0.45D, 0.65D, 0.08D
        );
        level.sendParticles(ParticleTypes.SOUL, departurePos.x, departurePos.y + 0.6D, departurePos.z,
                18, 0.55D, 0.50D, 0.55D, 0.04D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 28, 0.98F, 0.08F),
                arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z, 16, 0.40D, 0.55D, 0.40D, 0.02D
        );
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.BONE_BLOCK_PLACE, SoundSource.PLAYERS, 0.95F, 0.78F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.75F, 0.85F);
    }

    private static void rootPursuersInDepartureRibcage(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            double radius,
            int rootTicks,
            int tier
    ) {
        AABB box = new AABB(
                center.x - (radius + 0.4D), center.y - 1.5D, center.z - (radius + 0.4D),
                center.x + (radius + 0.4D), center.y + 2.8D, center.z + (radius + 0.4D)
        );
        long noJumpUntil = level.getGameTime() + rootTicks;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, rootTicks, 6));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), rootTicks, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0));
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, noJumpUntil);
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(0.0D, Math.min(0.0D, foe.getDeltaMovement().y), 0.0D));
                if (foe instanceof Mob mob) {
                    mob.getNavigation().stop();
                }
            }
            SpellWorldVfxManager.broadcastWardShell(
                    level, foe.position(), Math.max(0.85D, foe.getBbWidth() * 0.90D), EssenceType.UNDEAD, null,
                    Math.min(55, rootTicks), tier
            );
        }
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 5.5 & 6.10 Necrotic Fossilization:
        // Petrifies organic wood into fossilized stone, dries foliage into dead bushes, and transmutes meat/organic
        // dropped items directly into solid Bone Blocks (strictly preserving containers)!
        boolean didAny = false;
        AABB itemArea = new AABB(pos).inflate(2.25D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, itemArea, Entity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        // 1. Logs & tree trunks petrify into Fossilized Stone (Polished Basalt preserving grain axis!)
        if (state.is(BlockTags.LOGS) || state.is(Blocks.BAMBOO_BLOCK)) {
            BlockState fossilStone = Blocks.POLISHED_BASALT.defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS) && fossilStone.hasProperty(RotatedPillarBlock.AXIS)) {
                fossilStone = fossilStone.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
            level.setBlockAndUpdate(pos, fossilStone);
            level.sendParticles(ParticleTypes.ASH, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.3D, 0.3D, 0.3D, 0.02D);
            level.playSound(null, pos, SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.70F, 0.85F);
            return true;
        }

        // 2. Living roots, coral, and giant mushrooms ossify into solid Bone Blocks!
        if (state.is(Blocks.MANGROVE_ROOTS)
                || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || state.is(Blocks.MUSHROOM_STEM)
                || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(Blocks.RED_MUSHROOM_BLOCK)
                || state.is(BlockTags.CORAL_BLOCKS)) {
            level.setBlockAndUpdate(pos, Blocks.BONE_BLOCK.defaultBlockState());
            level.sendParticles(ParticleTypes.ASH, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.3D, 0.3D, 0.3D, 0.02D);
            level.playSound(null, pos, SoundEvents.BONE_BLOCK_PLACE, SoundSource.PLAYERS, 0.68F, 0.85F);
            return true;
        }

        // 3. Planks / wooden stairs / slabs / fences petrify into Tuff stone!
        if (state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_FENCES)) {
            level.setBlockAndUpdate(pos, Blocks.TUFF.defaultBlockState());
            level.sendParticles(ParticleTypes.ASH, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.3D, 0.3D, 0.3D, 0.02D);
            level.playSound(null, pos, SoundEvents.TUFF_PLACE, SoundSource.PLAYERS, 0.65F, 0.85F);
            return true;
        }

        // 4. Foliage, flowers, crops, saplings, and leaves dry into Dead Bushes!
        if (state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.MOSS_CARPET)) {
            BlockState dead = Blocks.DEAD_BUSH.defaultBlockState();
            if (dead.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, dead);
            } else {
                level.removeBlock(pos, false);
                Block.popResource(level, pos, new ItemStack(Items.DEAD_BUSH, 1));
            }
            level.sendParticles(ParticleTypes.ASH, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.02D);
            return true;
        }

        // 5. Lush turf desiccates into Coarse Dirt!
        if (state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        // Section 5.5 & 6.10 Necrotic Fossilization:
        // Transmutes meat and organic items directly into solid Bone Blocks (with up to 30% bulk fuel discount)!
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || stack.is(Items.BONE_BLOCK)) return false;
        int count = stack.getCount();

        boolean isMeatOrOrganic = stack.is(ItemTags.MEAT)
                || stack.is(ItemTags.FISHES)
                || stack.is(ItemTags.LOGS)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.LEAVES)
                || stack.is(ItemTags.FLOWERS)
                || stack.is(ItemTags.SAPLINGS)
                || stack.is(ModItems.RAW_AURORAFOWL.get())
                || stack.is(ModItems.COOKED_AURORAFOWL.get())
                || stack.has(DataComponents.FOOD)
                || stack.is(Items.ROTTEN_FLESH) || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.FERMENTED_SPIDER_EYE)
                || stack.is(Items.LEATHER) || stack.is(Items.RABBIT_HIDE)
                || stack.is(Items.FEATHER) || stack.is(Items.WHEAT)
                || stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BONE_MEAL)
                || stack.is(Items.BONE) || stack.is(Items.SUGAR_CANE)
                || stack.is(Items.KELP) || stack.is(Items.DRIED_KELP)
                || stack.is(Items.RED_MUSHROOM) || stack.is(Items.BROWN_MUSHROOM)
                || stack.is(Items.SLIME_BALL);

        if (isMeatOrOrganic) {
            itemEntity.setItem(new ItemStack(Items.BONE_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            level.sendParticles(ParticleTypes.SOUL, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    8, 0.20D, 0.20D, 0.20D, 0.03D);
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                    itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 12, 0.22D, 0.22D, 0.22D, 0.06D
            );
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.BONE_BLOCK_PLACE, SoundSource.PLAYERS, 0.65F, 0.90F);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 5.6 & 6.10 Cadaverous Wither-Blight Hex:
        // Persistent milk-immune rotting curse clinging until cleansed via Restoration/Radiant magic or caster defeat;
        // continuously deals necrotic damage, permanently disables all health regeneration, shreds 30% armor toughness,
        // contagiously infects adjacent living enemies upon victim death, and triggers Necrotic Resonance on players!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        if (target.hasEffect(MobEffects.REGENERATION)) {
            target.removeEffect(MobEffects.REGENERATION);
        }
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1, true, false));
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.30D * mult, 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                level, target, hexCaster, (1.40F + potencyTier * 0.58F) * mult, false, false, false
        );

        level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY(0.70D), target.getZ(),
                4, 0.25D, 0.30D, 0.25D, 0.025D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), CRYPTGUARD_EIDOLON_HEX_COLOR, 26, 0.88F, 0.08F),
                target.getX(), target.getY(0.82D), target.getZ(), 3, 0.28D, 0.32D, 0.28D, 0.015D
        );
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellCoreEffect effect,
            int potencyTier,
            SpellAssembly spell
    ) {
        // Section 5.7 & 6.10 Metabolic Transfusion:
        // Converts caster hunger points directly into burst health points, consuming food saturation
        // (and hunger points down to a safe 2-shank floor) to rapidly heal mortal wounds!
        if (effect != SpellCoreEffect.RESTORATION || !(caster instanceof Player player)) {
            return;
        }
        if (target.getType().is(EntityTypeTags.UNDEAD) && target != caster) {
            return;
        }

        float missingHp = Math.max(0.0F, target.getMaxHealth() - target.getHealth());
        if (missingHp <= 0.05F && !player.isCreative()) {
            return;
        }

        FoodData food = player.getFoodData();
        float sat = food.getSaturationLevel();
        int foodLvl = food.getFoodLevel();
        float bonusHeal = 0.0F;

        if (sat >= 0.5F) {
            float consumedSat = Math.min(6.0F, sat);
            food.setSaturation(Math.max(0.0F, sat - consumedSat));
            bonusHeal += consumedSat * (1.70F + potencyTier * 0.18F);
        }
        if (foodLvl > 4) {
            int consumedFood = Math.min(4, foodLvl - 4);
            food.setFoodLevel(foodLvl - consumedFood);
            bonusHeal += consumedFood * (1.85F + potencyTier * 0.38F);
        } else if (player.isCreative()) {
            bonusHeal += 8.5F + potencyTier * 1.6F;
        }

        if (bonusHeal > 0.0F) {
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
            target.heal(bonusHeal);
            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                    target.position().add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.UNDEAD, null, 1.85F, 48, potencyTier
            );
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY(1.0D), target.getZ(),
                    6, 0.32D, 0.35D, 0.32D, 0.03D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), NECROTIC_SOUL_HEX_COLOR, 28, 0.98F, 0.08F),
                    target.getX(), target.getY(0.85D), target.getZ(), 12, 0.35D, 0.40D, 0.35D, 0.02D
            );
            level.playSound(null, target.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.80F, 1.35F);
            player.displayClientMessage(
                    Component.literal(String.format("§2✦ Metabolic Transfusion §7— §a+%.1f HP §7converted from hunger reserves!", bonusHeal)),
                    true
            );
        }
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private static void refundSubLinearBatchFuelDiscount(@Nullable LivingEntity caster, int potencyTier, int batchCount) {
        if (!(caster instanceof Player player) || batchCount <= 1) {
            return;
        }
        double discountFrac = Math.min(0.30D, 0.06D + (Math.min(64, batchCount) / 64.0D) * 0.24D);
        long baseCastMfum = MateriaStageTable.getBaseMfumCastCost(Math.clamp(potencyTier, 1, 10));
        long refund = Math.max(1L, Math.round(baseCastMfum * discountFrac));
        SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.UNDEAD, refund);
    }

    private static double distanceToRaySegment(Vec3 point, Vec3 rayStart, Vec3 rayDirNorm, double rayLen) {
        Vec3 rel = point.subtract(rayStart);
        double proj = Math.clamp(rel.dot(rayDirNorm), 0.0D, rayLen);
        Vec3 closest = rayStart.add(rayDirNorm.scale(proj));
        return point.distanceTo(closest);
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 1; dy >= -4; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty())
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return start.below();
    }

    private static double probeOverheadClearance(ServerLevel level, BlockPos groundAir, int maxScan) {
        for (int dy = 3; dy <= maxScan; dy++) {
            BlockPos p = groundAir.above(dy);
            BlockState bs = level.getBlockState(p);
            if (!bs.isAir() && !bs.getCollisionShape(level, p).isEmpty()) {
                return Math.max(3.2D, dy - 0.35D);
            }
        }
        return maxScan;
    }
}
