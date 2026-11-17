package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModEffects;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 * Complete per-part Signature Mutation implementation for <b>Umbral Materia (Base Type #8)</b>
 * (Spellcrafting System Guide Sections 1.2A, 1.7, 2.2, 4.1–4.9, 5.1–5.6 &amp; 6.9):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>RADIANT (+1)</b> | Weak against <b>VOID (-1)</b>.</li>
 *     <li><b>Section 1.7 Stealth Assassination</b>: Suppresses all cast/flight/impact sound and bright light
 *     flashes; striking unaware mobs deals full damage without alerting nearby pack mates.</li>
 *     <li><b>Projectile</b>: <i>Shadow Snare Missile</i> — silent, light-absorbing zero-drop obsidian needle
 *     ({@code #211A21}) that pierces through entire lines of targets, physically pinning each victim's shadow
 *     to the floor with 6 3D obsidian stakes for 2.0 seconds (40 ticks) and dealing Umbral Materia damage
 *     (+1 vs Radiant, -1 vs Void).</li>
 *     <li><b>Beam</b>: <i>Abyssal Shadow Lance</i> — continuous light-absorbing smoky black beam (rigid ray or
 *     45° locking tether) with inward-draining twilight eclipse rings that blinds and silences enemies held in
 *     its path, drains all outgoing attack strength (melee, ranged, and magic) by 30%, and deals continuous
 *     Umbral Materia damage (+1 vs Radiant).</li>
 *     <li><b>Radial Burst</b>: <i>Smoke Veil Shockwave</i> — detonates in a completely silent, expanding 6-block+
 *     dome of dense black umbral mist, dealing linear-falloff Umbral Materia damage, blinding all enemies,
 *     completely wiping mob aggro, and rendering the caster Invisible for 4.0 seconds (80 ticks).</li>
 *     <li><b>Touch / Strike</b>: <i>Umbral Shroud Cloaking</i> — converts melee strike to pure Umbral Materia
 *     damage (stacking held weapon Materia Advantage) with a 9-tooth 3D obsidian crescent slash; landing the hit
 *     cloaks the player in shadowy mist, granting 2.0 seconds of Invisibility and dropping active mob aggro.</li>
 *     <li><b>Aura / Self</b>: <i>Shroud of Silence &amp; Stealth</i> — registers defense as Umbral (+1 vs Radiant
 *     -&gt; 50% damage, -1 vs Void -&gt; 150% damage), suppresses 100% of acoustic {@code GameEvent} vibrations
 *     (completely undetectable to Sculk Sensors, Calibrated Sculk Sensors, Sculk Shriekers, and Wardens), reduces
 *     hostile mob detection range by 60%, and cloaks the caster (or anchored sanctuary) in a 3-block obscuring
 *     mist that blinds enemies who step too close.</li>
 *     <li><b>Glyph / Trap</b>: <i>Void-Blind Mine</i> — 1.0s arming delay, 10m (or Tier 5+ indefinite) lifetime,
 *     50% Sneak-Right-Click disarm refund, and Single-Use / Repeating Sentry support; detonates silently in black
 *     smoke that wipes the aggro table of all mobs in an 8-block radius so they lose target and wander aimlessly.</li>
 *     <li><b>Cone / Spray</b>: <i>Shadow Dart Fan</i> — fires a silent 60° arc of 20 3D dark obsidian needles
 *     that stick into struck mobs, cutting their movement speed by an exact 50% and dealing Umbral Materia damage.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Black Rain of Obfuscation</i> — cave-ceiling-adaptive brooding 16-block umbral
 *     eclipse storm raining 32 dark 3D umbral droplets over 6.0 seconds (120 ticks), repeatedly battering enemies
 *     with Umbral Materia damage (+1 vs Radiant), blinding targets, and dropping mob aggro across the entire storm.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Shadow Stalker Suite</i> ({@code #211A21}) — silent triad: indoor ceiling-stalking
 *     Agent pouncing downward for stealth ambushes, 16-block dark-spawn-suppressing Sentry obelisk, and live
 *     equipment-copying Decoy mirage.</li>
 *     <li><b>Flow (Chains)</b>: <i>Silent Shadow Chains</i> — leaps along completely silent, invisible shadow
 *     threads, binding chained victims together so 25% of damage dealt to any one echoes across all others.</li>
 *     <li><b>Blink / Step</b>: <i>Shadow-Step</i> — silently teleports the caster from shadow to shadow with
 *     extended dark-zone reach and zero sound or arrival particles.</li>
 *     <li><b>Transmute / Break</b>: <i>Nocturnal Darkening</i> — heat-free conversion of vibrant blocks and
 *     dropped item stacks (with up to 30% bulk fuel discount &amp; strict container protection) into darkened
 *     variants (Dark Oak, Blackstone, Tinted Glass, Soul Torches/Lanterns/Campfires, Charcoal).</li>
 *     <li><b>Affliction</b>: <i>Sensory Void Hex</i> — persistent milk-immune pitch-black curse inflicting total
 *     Blindness, Slowness II, and silencing mob alerts/spellcasting; triggers <i>Vulnerable Darkness</i> on
 *     cursed players (+25% hostile attack damage &amp; Speed I within 16 blocks).</li>
 * </ul>
 */
public final class UmbralSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Section 6.9 Shadow Stalker black-violet hex color ({@code #211A21}). */
    public static final int SHADOW_STALKER_HEX_COLOR = 0x211A21;
    /** Twilight umbral-eclipse violet rim accent ({@code #8F5CFF}). */
    public static final int ECLIPSE_VIOLET_HEX_COLOR = 0x8F5CFF;

    private static final int VFX_SHADOW_SNARE_TICKS = 48;
    private static final int VFX_SHADOW_LANCE_TICKS = 16;
    private static final int VFX_SMOKE_VEIL_TICKS = 64;
    private static final int VFX_CLOAKING_SLASH_TICKS = 52;
    private static final int VFX_VOID_BLIND_TICKS = 64;
    private static final int VFX_SHADOW_DART_CONE_TICKS = 54;
    private static final int VFX_BLACK_RAIN_TICKS = 132;
    private static final int VFX_SHADOW_SUMMON_TICKS = 54;

    private static final String TAG_REPEATING_PULSES_LEFT = "SpellUmbralMinePulsesLeft";
    private static final String TAG_REPEATING_COOLDOWN_UNTIL = "SpellUmbralMineCooldownUntil";

    @Override
    public EssenceType materiaType() {
        return EssenceType.UMBRAL;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Silent Shadow Chains";
            default -> "Shadow Eclipse " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Shadow Snare Missile";
            case BEAM -> "Abyssal Shadow Lance";
            case RADIAL_BURST -> "Smoke Veil Shockwave";
            case TOUCH_STRIKE -> "Umbral Shroud Cloaking";
            case AURA -> "Shroud of Silence & Stealth";
            case GLYPH -> "Void-Blind Mine";
            case CONE -> "Shadow Dart Fan";
            case SKYFALL -> "Black Rain of Obfuscation";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Shadow-Step";
            case TRANSMUTE, BREAK -> "Nocturnal Darkening";
            case AFFLICTION -> "Sensory Void Hex";
            default -> "Umbral " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — SILENT SHADOW CHAINS
    // =========================================================================

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.9 Silent Shadow Chains:
        // Jumps between targets along completely silent, invisible shadow threads (0 sound, 0 bright arc beam!),
        // binding chained victims together so 25% of damage taken by any one echoes to all other chained mobs!
        long now = level.getGameTime();
        long duration = 200L + spell.getEffectiveTier() * 20L;
        CompoundTag fromData = EntityHelper.getPersistentData(fromTarget);
        long groupId = fromData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL).orElse(0L) > now
                ? fromData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP).orElse(now)
                : (now ^ caster.getUUID().getLeastSignificantBits());

        String casterUuid = caster.getUUID().toString();
        fromData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP, groupId);
        fromData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL, now + duration);
        fromData.putString(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_CASTER, casterUuid);

        CompoundTag toData = EntityHelper.getPersistentData(toTarget);
        toData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP, groupId);
        toData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL, now + duration);
        toData.putString(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_CASTER, casterUuid);
    }

    // =========================================================================
    // FORM MUTATION 1 — SHADOW SNARE MISSILE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.9 Shadow Snare Missile:
        // Silent, light-absorbing needle bolt that flies razor-straight with zero gravitational drop
        // and pierces cleanly through entire lines of targets!
        projectile.setSilent(true);
        projectile.setNoGravity(true);
        projectile.addBonusPierces(16 + spell.getEffectiveTier() * 2);
    }

    @Override
    public void onProjectileTickFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            SubSpellSlot slot
    ) {
        Vec3 pos = projectile.position();
        level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z,
                2, 0.04D, 0.04D, 0.04D, 0.002D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 20, 0.72F, 0.09F),
                    pos.x, pos.y, pos.z, 2, 0.05D, 0.05D, 0.05D, 0.004D
            );
        }
        if (projectile.tickCount % 4 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.RUNE_PERTHRO.get(), ECLIPSE_VIOLET_HEX_COLOR, 18, 0.62F, 0.05F),
                    pos.x, pos.y, pos.z, 1, 0.03D, 0.03D, 0.03D, 0.002D
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
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        float potMult = spell.flawProfile().getPotencyMultiplier();

        // Pin target's shadow to the floor for an exact 2.0 seconds (40 ticks) with 6 3D obsidian stakes (silent!)
        pinEntityShadowToFloor(level, living, 40, tier);

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            float snareDmg = slot.hasHarmVerb()
                    ? ((1.8F + tier * 0.55F) * potMult)
                    : ((3.4F + tier * 1.05F) * potMult);
            SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                    level, living, caster, snareDmg, false, true
            );
        }
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
        int tier = spell.getEffectiveTier();
        Vec3 hitLoc = blockHit.getLocation();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                hitLoc, normal, EssenceType.UMBRAL, null, 1.30F, VFX_SHADOW_SNARE_TICKS, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 28, 1.05F, 0.12F),
                hitLoc.x, hitLoc.y + 0.1D, hitLoc.z, 8, 0.25D, 0.20D, 0.25D, 0.015D
        );
    }

    // =========================================================================
    // FORM MUTATION 2 — ABYSSAL SHADOW LANCE (BEAM)
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
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 muzzle = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D).add(normDir.scale(0.35D));
        Vec3 beamEnd = blockHit.getType() == HitResult.Type.BLOCK
                ? blockHit.getLocation()
                : muzzle.add(normDir.scale(maxReach));

        // Flexible 45° tether support (Section 4.2): keeps locked onto tethered victim while moving!
        List<LivingEntity> targets = new ArrayList<>(hitEntities);
        if (targets.isEmpty()) {
            AABB corridor = new AABB(muzzle, beamEnd).inflate(1.25D);
            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, corridor, LivingEntity::isAlive)) {
                if (cand == caster) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                Vec3 toCand = cand.getEyePosition().subtract(muzzle);
                double len = toCand.length();
                if (len <= maxReach + 0.5D && len > 1.0E-4D && toCand.scale(1.0D / len).dot(normDir) >= 0.707D
                        && distanceToRaySegment(cand.getBoundingBox().getCenter(), muzzle, normDir, maxReach) <= 1.15D) {
                    targets.add(cand);
                }
            }
        }

        Vec3 visualEnd = !targets.isEmpty()
                ? targets.getFirst().getBoundingBox().getCenter()
                : beamEnd;

        // Silent 3D smoky black Abyssal Shadow Lance with 6 inward-draining twilight eclipse rings (#8F5CFF)!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_BEAM,
                muzzle, visualEnd, EssenceType.UMBRAL, null,
                0.28F + tier * 0.02F, VFX_SHADOW_LANCE_TICKS, tier
        );

        float flawPot = spell.flawProfile().getPotencyMultiplier();
        Set<UUID> seen = new HashSet<>();
        for (LivingEntity hit : targets) {
            if (!hit.isAlive() || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            // 1. Blind & Silence enemies held in the path!
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 65, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 65, 0));
            SpellLingeringZoneManager.applyArcaneSilence(hit, 80);

            // 2. Drain ALL target attack strength (melee, ranged bows/crossbows, fireballs, magic) by an exact 30%!
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.30D, 0.0D, 80L);
            EntityHelper.getPersistentData(hit)
                    .putLong(SpellLingeringZoneManager.TAG_UMBRAL_WEAKENED_30_UNTIL, now + 80L);

            // 3. Continuous Umbral Materia damage (+1 vs Radiant, -1 vs Void)!
            if (!spell.prefix().enforcesZeroEntityHarm()) {
                float lanceDmg = slot.hasHarmVerb()
                        ? ((1.15F + tier * 0.35F) * apparatusPotency * flawPot)
                        : ((1.95F + tier * 0.55F) * apparatusPotency * flawPot);
                SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(level, hit, caster, lanceDmg, false);
            }

            // Inward-draining shadow motes pulled along the lance from victim toward the eclipse rings
            Vec3 pullVel = muzzle.subtract(hit.getBoundingBox().getCenter()).normalize().scale(0.16D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 22, 0.95F, 0.10F),
                    hit.getX(), hit.getY(0.65D), hit.getZ(), 3, 0.22D, 0.28D, 0.22D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), ECLIPSE_VIOLET_HEX_COLOR, 18, 0.70F, 0.05F),
                    hit.getX() + pullVel.x, hit.getY(0.70D) + pullVel.y, hit.getZ() + pullVel.z,
                    2, 0.12D, 0.15D, 0.12D, 0.02D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SMOKE VEIL SHOCKWAVE (RADIAL BURST)
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
        // Section 6.9 Smoke Veil Shockwave:
        // Completely silent, expanding cloud of dense black umbral mist in a 6-block+ radius,
        // dealing Umbral Materia damage, blinding all enemies, dropping mob aggro completely,
        // and rendering the caster Invisible for 4.0 seconds (80 ticks)!
        int tier = spell.getEffectiveTier();
        double veilRadius = Math.max(6.0D, radius);
        float flawPot = spell.flawProfile().getPotencyMultiplier();

        // Broadcast 64-tick (3.2s) 3D expanding black umbral mist dome & 16 creeping smoke tendrils with ZERO sound!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UMBRAL, null, (float) veilRadius, VFX_SMOKE_VEIL_TICKS, tier
        );
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.75D, center.z,
                46, veilRadius * 0.44D, 0.55D, veilRadius * 0.44D, 0.028D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 48, 2.05F, 0.26F),
                center.x, center.y + 0.85D, center.z,
                30, veilRadius * 0.42D, 0.50D, veilRadius * 0.42D, 0.020D
        );

        AABB box = new AABB(
                center.x - veilRadius, center.y - 2.5D, center.z - veilRadius,
                center.x + veilRadius, center.y + 3.5D, center.z + veilRadius
        );
        long obfuscateUntil = level.getGameTime() + 60L;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            double dist = foe.position().distanceTo(center);
            if (dist > veilRadius + 0.35D) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, veilRadius)) * 0.67D, 0.33D, 1.0D);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
            EntityHelper.getPersistentData(foe)
                    .putLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL, obfuscateUntil);

            if (!spell.prefix().enforcesZeroEntityHarm()) {
                float burstDmg = slot.hasHarmVerb()
                        ? ((2.0F + tier * 0.60F) * apparatusPotency * flawPot * falloff)
                        : ((3.8F + tier * 1.15F) * apparatusPotency * flawPot * falloff);
                SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                        level, foe, caster, burstDmg, false, true
                );
            }
        }

        // Completely wipe mob aggro across the smoke veil & cloak caster in 4.0s particle-free Invisibility!
        wipeMobAggroAround(level, center, Math.max(10.0D, veilRadius + 3.0D), true);
        if (caster.position().distanceToSqr(center) > 4.0D) {
            wipeMobAggroAround(level, caster.position(), 10.0D, false);
        }
        grantCleanShadowInvisibility(caster, 80);
    }

    // =========================================================================
    // FORM MUTATION 4 — UMBRAL SHROUD CLOAKING (TOUCH / MELEE STRIKE)
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
        // Section 6.9 Umbral Shroud Cloaking:
        // Converts melee strike to pure Umbral Materia damage (stacking held weapon Materia Advantage);
        // landing the hit cloaks the player in shadowy mist, granting 2.0s (40t) Invisibility & dropping active mob aggro!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double reach = Math.max(2.6D, SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.TOUCH_STRIKE, tier));

        // Silent 52-tick 3D 9-tooth obsidian shadow-dagger crescent slash!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir,
                EssenceType.UMBRAL, null, (float) reach, VFX_CLOAKING_SLASH_TICKS, tier
        );

        if (hitEntities.isEmpty()) {
            return;
        }

        float flawPot = spell.flawProfile().getPotencyMultiplier();
        long obfuscateUntil = level.getGameTime() + 50L;

        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (!hit.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            float cleaveScale = (i == 0) ? 1.0F : 0.50F;
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                        level, hit, caster, (3.2F + tier * 0.90F) * apparatusPotency * flawPot * cleaveScale, true, true
                );
            }
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
            EntityHelper.getPersistentData(hit)
                    .putLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL, obfuscateUntil);
        }

        // Cloak caster in shadowy mist, grant 2.0s (40t) Invisibility, & drop active mob aggro!
        grantCleanShadowInvisibility(caster, 40);
        wipeMobAggroAround(level, caster.position(), 14.0D, false);

        level.sendParticles(ParticleTypes.SMOKE, caster.getX(), caster.getY(0.75D), caster.getZ(),
                18, 0.42D, 0.55D, 0.42D, 0.018D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 30, 1.45F, 0.16F),
                caster.getX(), caster.getY(0.85D), caster.getZ(),
                12, 0.45D, 0.48D, 0.45D, 0.014D
        );
    }

    // =========================================================================
    // FORM MUTATION 5 — SHROUD OF SILENCE & STEALTH (AURA / SELF)
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
        // Section 6.9 Shroud of Silence & Stealth:
        // Registers defense as Umbral (+1 vs Radiant -> 50% damage, -1 vs Void -> 150% damage),
        // suppresses 100% of acoustic vibrations (Sculk Sensors, Shriekers & Wardens),
        // cuts hostile mob detection range by 60%, and cloaks the caster in a 3m blinding shadow veil!
        int tier = spell.getEffectiveTier();
        int dur = 260 + tier * 40;
        long until = level.getGameTime() + dur;

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.remove(SpellLingeringZoneManager.TAG_RADIANT_HALO_REVEAL_UNTIL);
        caster.removeEffect(MobEffects.GLOWING);
        data.putLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL, until);
        data.putInt("SpellWardTier", tier);

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.45F, null);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                caster.position().add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UMBRAL, null, 3.0F, 54, tier
        );
        wipeMobAggroAround(level, caster.position(), 12.0D, false);
    }

    // =========================================================================
    // FORM MUTATION 6 — VOID-BLIND MINE (GLYPH / TRAP)
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
        // Section 4.6 & 6.9 Void-Blind Mine:
        // - 1.0s (20t) arming delay
        // - 10 minutes (12,000t) lifetime at Tiers 1–4, or INDEFINITE at Tier 5+
        // - 50% Sneak-Right-Click Materia refund via .markDisarmableGlyph()
        // - Supports both Single-Use Detonator (100% burst) & Repeating Glyphic Sentry (3 pulses @ 60%, 2s re-arm)
        // - Detonates silently in a flash of black smoke that wipes the aggro table of all mobs in an 8-block radius
        //   so they lose target and wander aimlessly!
        int tier = spell.getEffectiveTier();
        int maxTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceBlock = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(glyphCenter.x, surfaceBlock.getY() + 1.02D, glyphCenter.z);
        double sealRad = Math.max(2.0D, Math.min(3.4D, radius));

        String subMode = slot.primaryEffect().subMode();
        boolean repeatingSentry = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        CompoundTag casterTag = EntityHelper.getPersistentData(caster);
        String pulseKey = TAG_REPEATING_PULSES_LEFT + "_" + level.getGameTime();
        String cdKey = TAG_REPEATING_COOLDOWN_UNTIL + "_" + level.getGameTime();
        if (repeatingSentry) {
            casterTag.putInt(pulseKey, 3);
            casterTag.putLong(cdKey, 0L);
        }

        // Silent placement eclipse sigil visible to caster/allies, completely camouflaged to hostile mobs!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                snappedCenter, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UMBRAL, null, (float) sealRad, 52, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, sealRad, maxTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed > 0 && elapsed % 40 == 0) {
                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                                zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.UMBRAL, null, (float) zone.radius, 46, tier
                        );
                    }

                    // Canonical 1.0s (20t) arming delay
                    if (elapsed < 20) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    long now = srv.getGameTime();
                    if (repeatingSentry && owner != null) {
                        long cdUntil = EntityHelper.getPersistentData(owner).getLong(cdKey).orElse(0L);
                        if (now < cdUntil) return true;
                    }

                    AABB trig = new AABB(
                            zone.center.x - 2.15D, zone.center.y - 1.0D, zone.center.z - 2.15D,
                            zone.center.x + 2.15D, zone.center.y + 2.4D, zone.center.z + 2.15D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(
                            LivingEntity.class,
                            trig,
                            e -> e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            e, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard()))
                    );
                    if (stepped.isEmpty()) {
                        return true;
                    }

                    float pulseScale = repeatingSentry ? 0.60F : 1.0F;

                    // Detonate silent 8-block dome of dense black umbral smoke!
                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                            zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.UMBRAL, null, 8.0F, VFX_VOID_BLIND_TICKS, tier
                    );
                    srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + 0.85D, zone.center.z,
                            54, 3.1D, 0.75D, 3.1D, 0.04D);
                    srv.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 52, 2.25F, 0.28F),
                            zone.center.x, zone.center.y + 0.95D, zone.center.z,
                            36, 3.0D, 0.70D, 3.0D, 0.025D
                    );

                    // Wipe aggro table across the full 8.0-block radius & make mobs wander aimlessly!
                    wipeMobAggroAround(srv, zone.center, 8.0D, true);
                    long amnesiaUntil = now + (long) ((80 + tier * 8) * pulseScale);

                    AABB wipeBox = new AABB(
                            zone.center.x - 8.0D, zone.center.y - 3.5D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 4.5D, zone.center.z + 8.0D
                    );
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, wipeBox, LivingEntity::isAlive)) {
                        if (victim == owner) continue;
                        if (victim.position().distanceToSqr(zone.center) > 64.0D) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                victim, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) (100 * pulseScale), 0));
                        victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, (int) (100 * pulseScale), 0));
                        EntityHelper.getPersistentData(victim)
                                .putLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL, amnesiaUntil);

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * pulseScale
                            );
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
    // FORM MUTATION 7 — SHADOW DART FAN (CONE / SPRAY)
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
        // Section 6.9 Shadow Dart Fan:
        // Fires a silent 60° arc of 20 dark 3D obsidian needles sticking into mobs,
        // reducing their movement speed by an exact 50% and dealing Umbral Materia damage!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double effRange = Math.max(8.0D, coneRange);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        Vec3 origin = caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D);

        // Silent 54-tick 3D volley of 20 fanning #211A21 & #8F5CFF obsidian shadow needles!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                origin, normDir, EssenceType.UMBRAL, null,
                (float) effRange, VFX_SHADOW_DART_CONE_TICKS, tier
        );

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

            // Exact -50% movement speed reduction for 4.5s (90t) via Attributes.MOVEMENT_SPEED!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.50D, 90L);

            // Visibly embed 3D dark obsidian needles into the struck mob's body!
            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                    hit.position().add(0.0D, hit.getBbHeight() * 0.48D, 0.0D),
                    normDir.scale(-1.0D),
                    EssenceType.UMBRAL, null, 0.92F, 46, tier
            );

            if (!spell.prefix().enforcesZeroEntityHarm()) {
                float dartDmg = slot.hasHarmVerb()
                        ? ((1.8F + tier * 0.50F) * apparatusPotency * flawPot)
                        : ((3.2F + tier * 0.95F) * apparatusPotency * flawPot);
                SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                        level, hit, caster, dartDmg, false, true
                );
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 8 — BLACK RAIN OF OBFUSCATION (SKYFALL)
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
        // Section 6.9 Black Rain of Obfuscation:
        // Rains dark umbral droplets over 6.0 seconds (120t) across an entire 16-block radius (cave-ceiling adaptive!),
        // repeatedly battering enemies with Umbral Materia damage (+1 vs Radiant, -1 vs Void),
        // blinding all targets caught inside, and dropping mob aggro across the entire 16-block storm!
        int tier = spell.getEffectiveTier();
        double stormRad = Math.max(16.0D, radius);
        BlockPos surfacePos = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 anchoredCenter = new Vec3(impactCenter.x, surfacePos.getY() + 1.0D, impactCenter.z);
        double canopyClearance = probeOverheadClearance(level, BlockPos.containing(anchoredCenter), 9);

        // Silent 132-tick (6.6s) brooding 16m eclipse storm-canopy & 32 falling 3D umbral shadow-droplets!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                anchoredCenter, new Vec3(0.0D, -1.0D, 0.0D),
                EssenceType.UMBRAL, null, (float) stormRad, VFX_BLACK_RAIN_TICKS, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, stormRad, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed % 5 == 0) {
                        double dropY = zone.center.y + Math.max(2.5D, canopyClearance - 0.6D);
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, dropY, zone.center.z,
                                28, zone.radius * 0.48D, 0.85D, zone.radius * 0.48D, 0.025D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), SHADOW_STALKER_HEX_COLOR, 28, 0.98F, 0.12F),
                                zone.center.x, dropY - 0.3D, zone.center.z,
                                20, zone.radius * 0.46D, 1.2D, zone.radius * 0.46D, 0.035D
                        );
                    }

                    // Batter enemies, blind targets, & drop mob aggro across the entire 16-block storm every 15 ticks (8 waves over 6.0s)!
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        wipeMobAggroAround(srv, zone.center, zone.radius, true);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 3.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.5D, canopyClearance + 0.5D), zone.center.z + zone.radius
                        );
                        double rSq = zone.radius * zone.radius;
                        float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;

                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dSq = Math.pow(v.getX() - zone.center.x, 2) + Math.pow(v.getZ() - zone.center.z, 2);
                            if (dSq > rSq) continue;

                            if (v == owner || (owner != null && v.isAlliedTo(owner))) {
                                // Cloak friendly caster/allies standing inside their own Black Rain!
                                v.removeEffect(MobEffects.GLOWING);
                                EntityHelper.getPersistentData(v).remove(SpellLingeringZoneManager.TAG_RADIANT_HALO_REVEAL_UNTIL);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }

                            double dist = Math.sqrt(dSq);
                            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, zone.radius)) * 0.67D, 0.33D, 1.0D);

                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 55, 0));
                            EntityHelper.getPersistentData(v)
                                    .putLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL, now + 45L);

                            if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm()) {
                                float rainDmg = (zone.slot != null && zone.slot.hasHarmVerb())
                                        ? ((1.45F + tier * 0.45F) * zone.apparatusPotency * flawPot * falloff)
                                        : ((2.15F + tier * 0.65F) * zone.apparatusPotency * flawPot * falloff);
                                SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(srv, v, owner, rainDmg, false);
                            }
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.30F * falloff
                                );
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — SHADOW STALKER SUITE (SUMMON AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.UMBRAL.name());
        tag.putInt("SpellEidolonTintRGB", SHADOW_STALKER_HEX_COLOR);
        summonEntity.setSilent(true);

        // Silent 54-tick 3D umbral shadow-pillar & aggro wipe around summon manifestation!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SUMMON_PILLAR,
                spawnPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UMBRAL, null, (float) Math.max(1.75D, radius * 0.60D), VFX_SHADOW_SUMMON_TICKS, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 34, 1.25F, 0.14F),
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.015D
        );
        wipeMobAggroAround(level, spawnPos, Math.max(8.0D, radius), false);
    }

    // =========================================================================
    // PART 3: CORE EFFECT SYNERGIES — SHADOW-STEP, NOCTURNAL DARKENING & HEX
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.9 Shadow-Step:
        // Silently teleports the caster from one shadow into another with ZERO sound and ZERO arrival particles!
        grantCleanShadowInvisibility(caster, 45 + potencyTier * 5);
        wipeMobAggroAround(level, departurePos, 9.0D, false);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 5.5 & 6.9 Nocturnal Darkening:
        // Converts vibrant materials into darkened variants (Dark Oak, Blackstone, Tinted Glass, Soul lights, Charcoal)
        // without heat, strictly preserving chests & containers!
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

        BlockState darkened = resolveDarkenedBlockState(level, pos, state);
        if (darkened != null) {
            if (darkened.isAir()) {
                level.removeBlock(pos, false);
                Block.popResource(level, pos, new ItemStack(Items.CHARCOAL, 2));
            } else {
                level.setBlockAndUpdate(pos, darkened);
            }
            emitNocturnalDarkeningBlockVfx(level, pos);
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
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        int count = stack.getCount();
        ItemStack darkened = resolveDarkenedItemStack(stack, count);
        if (darkened == null) {
            return false;
        }
        itemEntity.setItem(darkened);
        refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 26, 0.95F, 0.10F),
                itemEntity.getX(), itemEntity.getY(0.30D), itemEntity.getZ(),
                8, 0.20D, 0.20D, 0.20D, 0.015D
        );
        level.sendParticles(ParticleTypes.SMOKE, itemEntity.getX(), itemEntity.getY(0.30D), itemEntity.getZ(),
                6, 0.18D, 0.18D, 0.18D, 0.012D);
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 5.6 & 6.9 Sensory Void Hex:
        // Persistent milk-immune pitch-black curse clinging until cleansed via Restoration / Radiant light magic
        // or caster defeat; permanently inflicts total Blindness, Slowness II+, and silences all mob alerts & spellcasting!
        // Player Hex Vector ("Vulnerable Darkness"): surrounding hostiles within 16m gain +25% attack damage & Speed I!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        int slowAmp = Math.min(3, 1 + (SpellCombatReactionEngine.getHexStackLevel(target) - 1));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, false));
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 45, 0, true, false));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, slowAmp, true, false));
        SpellLingeringZoneManager.applyArcaneSilence(target, 45);

        // Silence mob social alerts & call-for-help memory on hexed mobs
        if (target instanceof Mob hexedMob) {
            hexedMob.setLastHurtByMob(null);
            hexedMob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            if (hexedMob instanceof Warden warden && hexedMob.getTarget() != null) {
                warden.clearAnger(hexedMob.getTarget());
            }
        }

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                level, target, hexCaster, (1.15F + potencyTier * 0.50F) * mult, false
        );

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 26, 1.15F, 0.14F),
                target.getX(), target.getY(0.75D), target.getZ(),
                4, 0.28D, 0.38D, 0.28D, 0.010D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_PERTHRO.get(), ECLIPSE_VIOLET_HEX_COLOR, 24, 0.85F, 0.04F),
                target.getX(), target.getY(0.90D), target.getZ(),
                1, 0.20D, 0.25D, 0.20D, 0.005D
        );

        if (target instanceof Player) {
            for (Mob m : level.getEntitiesOfClass(Mob.class, target.getBoundingBox().inflate(16.0D), LivingEntity::isAlive)) {
                if (!(m instanceof Enemy) && m.getTarget() != target) continue;
                if (m.distanceToSqr(target) > 256.0D) continue;
                m.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false));
            }
        }
        return true;
    }

    // =========================================================================
    // PUBLIC UMBRAL STEALTH & SNARE HELPERS
    // =========================================================================

    /**
     * Pins {@code victim}'s shadow directly onto the floor for {@code durationTicks} (e.g. {@code 40t = 2.0s}),
     * preventing horizontal movement and jumping while rendering 6 3D obsidian shadow-stakes around their feet.
     */
    public static void pinEntityShadowToFloor(ServerLevel level, LivingEntity victim, int durationTicks, int tier) {
        long until = level.getGameTime() + durationTicks;
        CompoundTag data = EntityHelper.getPersistentData(victim);
        data.putLong(SpellLingeringZoneManager.TAG_UMBRAL_SNARE_UNTIL,
                Math.max(until, data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SNARE_UNTIL).orElse(0L)));
        data.putDouble(SpellLingeringZoneManager.TAG_UMBRAL_SNARE_X, victim.getX());
        data.putDouble(SpellLingeringZoneManager.TAG_UMBRAL_SNARE_Y, victim.getY());
        data.putDouble(SpellLingeringZoneManager.TAG_UMBRAL_SNARE_Z, victim.getZ());
        data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, until);

        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, durationTicks, 6));
        victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), durationTicks, 0));
        if (!SpellCombatReactionEngine.isBossEntity(level, victim)) {
            SpellExecutionEngine.applySyncedMotion(victim, new Vec3(0.0D, Math.min(0.0D, victim.getDeltaMovement().y), 0.0D));
            if (victim instanceof Mob mob) {
                mob.getNavigation().stop();
            }
        }

        // Silent 48-tick 3D pitch-black shadow pool & 6 obsidian stakes pinning the victim's shadow!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                victim.position().add(0.0D, 0.04D, 0.0D),
                new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.UMBRAL, null, 1.50F, VFX_SHADOW_SNARE_TICKS, tier
        );
    }

    /**
     * Completely wipes active targets, hurt-by revenge memory, NeutralMob anger, Warden anger, and Brain attack memory
     * across {@code radius}, optionally making disoriented PathfinderMobs wander aimlessly.
     */
    public static void wipeMobAggroAround(ServerLevel level, Vec3 center, double radius, boolean wanderAimlessly) {
        AABB box = new AABB(
                center.x - radius, center.y - 3.5D, center.z - radius,
                center.x + radius, center.y + 4.5D, center.z + radius
        );
        double rSq = radius * radius;
        long amnesiaUntil = level.getGameTime() + (wanderAimlessly ? 60L : 35L);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, LivingEntity::isAlive)) {
            if (mob.position().distanceToSqr(center) > rSq) continue;
            LivingEntity prevTarget = mob.getTarget();
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.setAggressive(false);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            mob.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.DISTURBANCE_LOCATION);
            if (mob instanceof NeutralMob neutral) {
                neutral.stopBeingAngry();
                neutral.setPersistentAngerTarget(null);
            }
            if (mob instanceof Warden warden && prevTarget != null) {
                warden.clearAnger(prevTarget);
            }
            EntityHelper.getPersistentData(mob)
                    .putLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL,
                            Math.max(amnesiaUntil, EntityHelper.getPersistentData(mob)
                                    .getLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL).orElse(0L)));
            if (wanderAimlessly && mob instanceof PathfinderMob pf) {
                double ang = level.random.nextDouble() * Math.PI * 2.0D;
                double dist = 3.0D + level.random.nextDouble() * 4.5D;
                pf.getNavigation().moveTo(
                        mob.getX() + Math.cos(ang) * dist,
                        mob.getY(),
                        mob.getZ() + Math.sin(ang) * dist,
                        0.85D
                );
            }
        }
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private static void grantCleanShadowInvisibility(LivingEntity entity, int durationTicks) {
        entity.removeEffect(MobEffects.GLOWING);
        EntityHelper.getPersistentData(entity).remove(SpellLingeringZoneManager.TAG_RADIANT_HALO_REVEAL_UNTIL);
        // visible = false so zero potion-swirl particles ever betray the caster's position; showIcon = true for HUD clarity!
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, durationTicks, 0, true, false, true));
    }

    @Nullable
    private static BlockState resolveDarkenedBlockState(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE)) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (state.is(Blocks.POLISHED_ANDESITE) || state.is(Blocks.POLISHED_DIORITE) || state.is(Blocks.POLISHED_GRANITE)
                || state.is(Blocks.SMOOTH_STONE)) {
            return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        }
        if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.MOSSY_STONE_BRICKS) || state.is(Blocks.CRACKED_STONE_BRICKS)) {
            return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }
        if (state.is(Blocks.CHISELED_STONE_BRICKS)) {
            return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            return Blocks.SOUL_SOIL.defaultBlockState();
        }
        if (state.is(Blocks.QUARTZ_BLOCK)) {
            return Blocks.SMOOTH_BASALT.defaultBlockState();
        }
        if (state.is(Blocks.GLASS) || (state.is(BlockTags.IMPERMEABLE) && !state.is(Blocks.TINTED_GLASS))) {
            return Blocks.TINTED_GLASS.defaultBlockState();
        }
        if (state.is(Blocks.TORCH)) {
            return Blocks.SOUL_TORCH.defaultBlockState();
        }
        if (state.is(Blocks.WALL_TORCH)) {
            BlockState soulWall = Blocks.SOUL_WALL_TORCH.defaultBlockState();
            if (state.hasProperty(WallTorchBlock.FACING) && soulWall.hasProperty(WallTorchBlock.FACING)) {
                soulWall = soulWall.setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING));
            }
            return soulWall;
        }
        if (state.is(Blocks.LANTERN)) {
            BlockState soul = Blocks.SOUL_LANTERN.defaultBlockState();
            if (state.hasProperty(BlockStateProperties.HANGING) && soul.hasProperty(BlockStateProperties.HANGING)) {
                soul = soul.setValue(BlockStateProperties.HANGING, state.getValue(BlockStateProperties.HANGING));
            }
            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && soul.hasProperty(BlockStateProperties.WATERLOGGED)) {
                soul = soul.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
            }
            return soul;
        }
        if (state.is(Blocks.CAMPFIRE)) {
            BlockState soul = Blocks.SOUL_CAMPFIRE.defaultBlockState();
            if (state.hasProperty(CampfireBlock.LIT) && soul.hasProperty(CampfireBlock.LIT)) {
                soul = soul.setValue(CampfireBlock.LIT, state.getValue(CampfireBlock.LIT));
            }
            if (state.hasProperty(CampfireBlock.FACING) && soul.hasProperty(CampfireBlock.FACING)) {
                soul = soul.setValue(CampfireBlock.FACING, state.getValue(CampfireBlock.FACING));
            }
            if (state.hasProperty(CampfireBlock.SIGNAL_FIRE) && soul.hasProperty(CampfireBlock.SIGNAL_FIRE)) {
                soul = soul.setValue(CampfireBlock.SIGNAL_FIRE, state.getValue(CampfireBlock.SIGNAL_FIRE));
            }
            if (state.hasProperty(CampfireBlock.WATERLOGGED) && soul.hasProperty(CampfireBlock.WATERLOGGED)) {
                soul = soul.setValue(CampfireBlock.WATERLOGGED, state.getValue(CampfireBlock.WATERLOGGED));
            }
            return soul;
        }
        if (state.is(Blocks.DARK_OAK_LOG) || state.is(Blocks.DARK_OAK_WOOD)
                || state.is(Blocks.STRIPPED_DARK_OAK_LOG) || state.is(Blocks.STRIPPED_DARK_OAK_WOOD)) {
            return Blocks.AIR.defaultBlockState(); // Pops 2 heat-free Charcoal in caller!
        }
        if (state.is(BlockTags.LOGS)) {
            BlockState darkOak = Blocks.DARK_OAK_LOG.defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS) && darkOak.hasProperty(RotatedPillarBlock.AXIS)) {
                darkOak = darkOak.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
            return darkOak;
        }
        if (state.is(BlockTags.PLANKS) && !state.is(Blocks.DARK_OAK_PLANKS)) {
            return Blocks.DARK_OAK_PLANKS.defaultBlockState();
        }
        if (state.is(BlockTags.WOODEN_SLABS) && !state.is(Blocks.DARK_OAK_SLAB)) {
            BlockState darkSlab = Blocks.DARK_OAK_SLAB.defaultBlockState();
            if (state.hasProperty(SlabBlock.TYPE) && darkSlab.hasProperty(SlabBlock.TYPE)) {
                darkSlab = darkSlab.setValue(SlabBlock.TYPE, state.getValue(SlabBlock.TYPE));
            }
            if (state.hasProperty(SlabBlock.WATERLOGGED) && darkSlab.hasProperty(SlabBlock.WATERLOGGED)) {
                darkSlab = darkSlab.setValue(SlabBlock.WATERLOGGED, state.getValue(SlabBlock.WATERLOGGED));
            }
            return darkSlab;
        }
        if (state.is(BlockTags.WOODEN_STAIRS) && !state.is(Blocks.DARK_OAK_STAIRS)) {
            BlockState darkStairs = Blocks.DARK_OAK_STAIRS.defaultBlockState();
            if (state.hasProperty(StairBlock.FACING) && darkStairs.hasProperty(StairBlock.FACING)) {
                darkStairs = darkStairs.setValue(StairBlock.FACING, state.getValue(StairBlock.FACING));
            }
            if (state.hasProperty(StairBlock.HALF) && darkStairs.hasProperty(StairBlock.HALF)) {
                darkStairs = darkStairs.setValue(StairBlock.HALF, state.getValue(StairBlock.HALF));
            }
            if (state.hasProperty(StairBlock.SHAPE) && darkStairs.hasProperty(StairBlock.SHAPE)) {
                darkStairs = darkStairs.setValue(StairBlock.SHAPE, state.getValue(StairBlock.SHAPE));
            }
            if (state.hasProperty(StairBlock.WATERLOGGED) && darkStairs.hasProperty(StairBlock.WATERLOGGED)) {
                darkStairs = darkStairs.setValue(StairBlock.WATERLOGGED, state.getValue(StairBlock.WATERLOGGED));
            }
            return darkStairs;
        }
        if (state.is(BlockTags.LEAVES) && !state.is(Blocks.DARK_OAK_LEAVES)) {
            return Blocks.DARK_OAK_LEAVES.defaultBlockState();
        }
        if (state.is(BlockTags.SAPLINGS) && !state.is(Blocks.DARK_OAK_SAPLING)
                && Blocks.DARK_OAK_SAPLING.defaultBlockState().canSurvive(level, pos)) {
            return Blocks.DARK_OAK_SAPLING.defaultBlockState();
        }
        if (state.is(BlockTags.WOOL) && !state.is(Blocks.BLACK_WOOL)) {
            return Blocks.BLACK_WOOL.defaultBlockState();
        }
        if (state.is(BlockTags.WOOL_CARPETS) && !state.is(Blocks.BLACK_CARPET)) {
            return Blocks.BLACK_CARPET.defaultBlockState();
        }
        if (state.is(BlockTags.TERRACOTTA) && !state.is(Blocks.BLACK_TERRACOTTA)) {
            return Blocks.BLACK_TERRACOTTA.defaultBlockState();
        }
        if (state.is(BlockTags.CANDLES) && !state.is(Blocks.BLACK_CANDLE)) {
            BlockState blk = Blocks.BLACK_CANDLE.defaultBlockState();
            if (state.hasProperty(CandleBlock.CANDLES) && blk.hasProperty(CandleBlock.CANDLES)) {
                blk = blk.setValue(CandleBlock.CANDLES, state.getValue(CandleBlock.CANDLES));
            }
            if (state.hasProperty(CandleBlock.LIT) && blk.hasProperty(CandleBlock.LIT)) {
                blk = blk.setValue(CandleBlock.LIT, state.getValue(CandleBlock.LIT));
            }
            return blk;
        }
        if (state.is(Blocks.GLOWSTONE) || state.is(Blocks.SHROOMLIGHT) || state.is(Blocks.SEA_LANTERN)) {
            return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        }
        return null;
    }

    @Nullable
    private static ItemStack resolveDarkenedItemStack(ItemStack stack, int count) {
        if (stack.is(Items.TORCH)) return new ItemStack(Items.SOUL_TORCH, count);
        if (stack.is(Items.LANTERN)) return new ItemStack(Items.SOUL_LANTERN, count);
        if (stack.is(Items.CAMPFIRE)) return new ItemStack(Items.SOUL_CAMPFIRE, count);
        if (stack.is(Items.GLASS)) return new ItemStack(Items.TINTED_GLASS, count);
        if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE) || stack.is(Items.MOSSY_COBBLESTONE)
                || stack.is(Items.ANDESITE) || stack.is(Items.DIORITE) || stack.is(Items.GRANITE)
                || stack.is(Items.SANDSTONE) || stack.is(Items.RED_SANDSTONE)) {
            return new ItemStack(Items.BLACKSTONE, count);
        }
        if (stack.is(Items.STONE_BRICKS) || stack.is(Items.MOSSY_STONE_BRICKS) || stack.is(Items.CRACKED_STONE_BRICKS)) {
            return new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS, count);
        }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            return new ItemStack(Items.SOUL_SOIL, count);
        }
        if (stack.is(Items.QUARTZ_BLOCK)) {
            return new ItemStack(Items.SMOOTH_BASALT, count);
        }
        if (stack.is(Items.GLOWSTONE) || stack.is(Items.SHROOMLIGHT) || stack.is(Items.SEA_LANTERN)) {
            return new ItemStack(Items.CRYING_OBSIDIAN, count);
        }
        if (stack.is(Items.GLOW_BERRIES)) {
            return new ItemStack(Items.SWEET_BERRIES, count);
        }
        if (stack.is(Items.GLOW_INK_SAC)) {
            return new ItemStack(Items.INK_SAC, count);
        }
        if (stack.is(ItemTags.WOOL) && !stack.is(Items.BLACK_WOOL)) {
            return new ItemStack(Items.BLACK_WOOL, count);
        }
        if (stack.is(ItemTags.TERRACOTTA) && !stack.is(Items.BLACK_TERRACOTTA)) {
            return new ItemStack(Items.BLACK_TERRACOTTA, count);
        }
        if (stack.is(ItemTags.SAPLINGS) && !stack.is(Items.DARK_OAK_SAPLING)) {
            return new ItemStack(Items.DARK_OAK_SAPLING, count);
        }
        if (stack.is(ItemTags.LEAVES) && !stack.is(Items.DARK_OAK_LEAVES)) {
            return new ItemStack(Items.DARK_OAK_LEAVES, count);
        }
        if (stack.is(ItemTags.PLANKS) && !stack.is(Items.DARK_OAK_PLANKS)) {
            return new ItemStack(Items.DARK_OAK_PLANKS, count);
        }
        if (stack.is(Items.DARK_OAK_LOG) || stack.is(Items.DARK_OAK_WOOD)
                || stack.is(Items.STRIPPED_DARK_OAK_LOG) || stack.is(Items.STRIPPED_DARK_OAK_WOOD)) {
            return new ItemStack(Items.CHARCOAL, count);
        }
        if (stack.is(ItemTags.LOGS)) {
            return new ItemStack(Items.DARK_OAK_LOG, count);
        }
        return null;
    }

    private static void emitNocturnalDarkeningBlockVfx(ServerLevel level, BlockPos pos) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SHADOW_STALKER_HEX_COLOR, 28, 1.05F, 0.12F),
                pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                7, 0.28D, 0.28D, 0.28D, 0.015D
        );
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                5, 0.24D, 0.24D, 0.24D, 0.010D);
    }

    private static void refundSubLinearBatchFuelDiscount(@Nullable LivingEntity caster, int potencyTier, int batchCount) {
        if (!(caster instanceof Player player) || batchCount <= 1) {
            return;
        }
        double discountFrac = Math.min(0.30D, 0.06D + (Math.min(64, batchCount) / 64.0D) * 0.24D);
        long baseCastMfum = MateriaStageTable.getBaseMfumCastCost(Math.clamp(potencyTier, 1, 10));
        long refund = Math.max(1L, Math.round(baseCastMfum * discountFrac));
        SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.UMBRAL, refund);
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
