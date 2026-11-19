package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Aura Materia (Vital Fusion #23)</b>
 * (Spellcrafting System Guide Section 6.24):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Dual Vitae + Air harmonic matrix — Strong against
 *     <b>UNDEAD (+1)</b> and <b>WRAITH (+1)</b> (Dominance {@code aura_air_dom}: Strong vs Earth) |
 *     Weak against <b>UMBRAL (-1)</b>, <b>VOID (-1)</b>, and <b>NETHER (-1)</b>, computed dynamically
 *     at runtime by {@code VisInteractionManager}.</li>
 *     <li><b>Flow (Chains)</b>: <i>Pranic Slipstream Chains</i> — branches across targets via glowing
 *     golden-pink wind threads ({@code #FF6B9D} / {@code #A88654}); each leap launches the struck victim
 *     3 blocks airborne, silences abilities for 1.5s, and siphons 15% of damage dealt into caster fuel.</li>
 *     <li><b>Projectile</b>: <i>Spiritual Gale Lance</i> — piercing bolt of compressed pranic wind
 *     ({@code #FF6B9D} / {@code #A88654}) piercing up to 2 enemies, dragging them backward in a swirling
 *     vortex and deflecting hostile physical arrows.</li>
 *     <li><b>Beam</b>: <i>Pranic Jetstream Tether</i> — sustained beam of rushing spiritual wind repelling
 *     hostiles, siphoning 20% of damage dealt into caster Materia fuel, acting as a vertical jetpack lifter
 *     when pointed down, and granting Slow Falling &amp; +30% speed to allies.</li>
 *     <li><b>Radial Burst</b>: <i>Spiritual Vacuum Implosion</i> — implodes the air inward before detonating
 *     (6-block+ radius, {@code #FF6B9D} / {@code #A88654}), pulling nearby mobs to a central point, silencing
 *     abilities for 2.0s, and vacuuming dropped items directly to the caster.</li>
 *     <li><b>Touch / Strike</b>: <i>Chakra Disruptor Palm</i> — severs spiritual channels, disabling mob
 *     special abilities &amp; spellcasting for 4.0s while siphoning 30% of damage dealt into caster fuel.</li>
 *     <li><b>Aura / Self</b>: <i>Chakra Respiration Shroud</i> — meditative aura ({@code #FF6B9D} / {@code #A88654})
 *     cutting all spell Materia fuel consumption by 20%, granting complete immunity to drowning &amp; suffocation,
 *     and passively regenerating hunger saturation every 4.0s.</li>
 *     <li><b>Glyph / Trap</b>: <i>Chakra Disruption Glyph</i> — disarmable Single/Repeating trap releasing a
 *     wave of spiritual interference that silences abilities &amp; spellcasting for 5.0s and strips 25% melee attack power.</li>
 *     <li><b>Cone / Spray</b>: <i>Pranic Gale Deluge</i> — 60° cone of spiritual wind &amp; vital mist knocking
 *     enemies 8 blocks back, deflecting arrows &amp; fireballs, extinguishing fires, and silencing abilities for
 *     2.5s while granting caught allies debuff cleansing &amp; +25% speed.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Spiritual Gale Barrage</i> — tier-scaled aerial rift dropping compressed
 *     atmospheric torpedoes over 6.0s; each crash creates a 3-block updraft geyser launching enemies 10 blocks
 *     upward with heavy fall damage.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Pranic Spiritualist Eidolon Suite</i> — {@code #FF6B9D} / {@code #A88654}
 *     Zephyr-Monk Agent (glider swiftness, airborne juggles &amp; 20% fuel siphon), Chakra Respiration Spire
 *     Sentry (8m -20% fuel discount &amp; Slow Falling aura), and Spirit-Vane Effigy Decoy (5m vacuum implosion
 *     silencing enemies for 3.0s on defeat).</li>
 *     <li><b>Blink / Step</b>: <i>Pranic Slipstream Blink</i> — teleports up to 14+ blocks silently, leaving a
 *     vacuum pop that drags &amp; disorients foes while granting the caster Slow Falling.</li>
 *     <li><b>Transmute</b>: <i>Aerated Purifying Transmute</i> — clears lingering potion clouds &amp; cobwebs,
 *     dries sponges, and purifies water.</li>
 *     <li><b>Affliction</b>: <i>Chakra Severance Hex</i> — milk-immune pranic curse permanently silencing mob
 *     abilities &amp; spellcasting, inflicting continuous suffocation tick damage, and doubling (2x) all knockback
 *     taken until cleansed via Restoration magic or caster defeat.</li>
 * </ul>
 */
public final class AuraSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PRANIC_PINK_HEX_COLOR = 0xFF6B9D;
    public static final int BUFF_AIR_GOLD_HEX_COLOR = 0xA88654;

    @Override
    public EssenceType materiaType() {
        return EssenceType.AURA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Pranic Slipstream Chains";
            default -> "Pranic Slipstream " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Spiritual Gale Lance";
            case BEAM -> "Pranic Jetstream Tether";
            case RADIAL_BURST -> "Spiritual Vacuum Implosion";
            case TOUCH_STRIKE -> "Chakra Disruptor Palm";
            case AURA -> "Chakra Respiration Shroud";
            case GLYPH -> "Chakra Disruption Glyph";
            case CONE -> "Pranic Gale Deluge";
            case SKYFALL -> "Spiritual Gale Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Pranic Slipstream Blink";
            case TRANSMUTE, BREAK -> "Aerated Purifying Transmute";
            case AFFLICTION -> "Chakra Severance Hex";
            case RESTORATION -> "Pranic Chakra Restoration";
            default -> "Pranic " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — PRANIC SLIPSTREAM CHAINS
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
        // Section 6.24 Pranic Slipstream Chains:
        // Branches across targets via glowing golden-pink wind threads; each leap launches the struck victim
        // 3 blocks airborne, silences abilities for 1.5s (30t), and siphons 15% of damage dealt into caster fuel!
        int tier = spell.getEffectiveTier();
        Vec3 fromEye = fromTarget.position().add(0.0D, fromTarget.getBbHeight() * 0.58D, 0.0D);
        Vec3 toEye = toTarget.position().add(0.0D, toTarget.getBbHeight() * 0.58D, 0.0D);
        SpellWorldVfxManager.broadcastChainArc(level, fromEye, toEye, EssenceType.AURA, null, tier);

        SpellLingeringZoneManager.applyArcaneSilence(toTarget, 30);
        if (!SpellCombatReactionEngine.isBossEntity(level, toTarget)) {
            Vec3 cur = toTarget.getDeltaMovement();
            SpellExecutionEngine.applySyncedMotion(toTarget, new Vec3(cur.x * 0.40D, 0.68D, cur.z * 0.40D)); // ~3 blocks airborne!
        }

        if (!slot.hasHarmVerb()) {
            float chainDmg = (2.2F + tier * 0.65F) * spell.flawProfile().getPotencyMultiplier();
            float dealt = SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(level, toTarget, caster, chainDmg, false, true);
            if (dealt > 0.0F && caster instanceof Player player && player.isAlive()) {
                SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.AURA, tier, dealt, 0.15F);
            }
        }

        level.sendParticles(ParticleTypes.GUST,
                toTarget.getX(), toTarget.getY(0.5D), toTarget.getZ(), 2, 0.20D, 0.20D, 0.20D, 0.02D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), PRANIC_PINK_HEX_COLOR, 18, 0.90F, 0.05F),
                toTarget.getX(), toTarget.getY(0.75D), toTarget.getZ(), 6, 0.25D, 0.28D, 0.25D, 0.03D
        );
    }

    // =========================================================================
    // FORM MUTATION 1 — SPIRITUAL GALE LANCE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // High-velocity zero-drop compressed pranic wind lance (pierces up to 2 enemies via SpellMutationRegistry)!
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D && vel.length() < 2.00D) {
            projectile.setDeltaMovement(vel.normalize().scale(2.05D));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.80F, 1.32F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        double phase = projectile.tickCount * 0.75D;
        double ox = Math.cos(phase) * 0.22D;
        double oz = Math.sin(phase) * 0.22D;
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), PRANIC_PINK_HEX_COLOR, 16, 0.88F, 0.05F),
                projectile.getX() + ox, projectile.getY(), projectile.getZ() + oz, 1, 0.03D, 0.03D, 0.03D, 0.0D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 16, 0.85F, 0.04F),
                projectile.getX() - ox, projectile.getY(), projectile.getZ() - oz, 1, 0.03D, 0.03D, 0.03D, 0.0D
        );

        // Deflects hostile physical arrows in flight!
        Entity owner = projectile.getOwner();
        Vec3 flightDir = projectile.getDeltaMovement().lengthSqr() > 1.0E-4D
                ? projectile.getDeltaMovement().normalize()
                : new Vec3(0.0D, 0.0D, 1.0D);
        for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, projectile.getBoundingBox().inflate(2.2D), Entity::isAlive)) {
            Entity arrowOwner = arrow.getOwner();
            if (arrowOwner == owner) continue;
            if (arrowOwner instanceof LivingEntity livOwner && owner instanceof LivingEntity livCaster
                    && SpellExecutionEngine.isFriendlyAllyForOasis(livCaster, livOwner)) {
                continue;
            }
            arrow.setDeltaMovement(flightDir.scale(1.45D).add(0.0D, 0.18D, 0.0D));
            arrow.hasImpulse = true;
            level.sendParticles(ParticleTypes.GUST, arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.0D);
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
        // Section 6.24 Spiritual Gale Lance:
        // Pierces up to 2 enemies, dragging them backward along its swirling vortex wake!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive() || living == caster) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        if (!slot.hasHarmVerb()) {
            int tier = spell.getEffectiveTier();
            float lanceDmg = (2.8F + tier * 0.85F) * spell.flawProfile().getPotencyMultiplier();
            SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(level, living, caster, lanceDmg, false, true);
        }

        if (!SpellCombatReactionEngine.isBossEntity(level, living)) {
            Vec3 vel = projectile.getDeltaMovement();
            Vec3 fwd = vel.lengthSqr() > 1.0E-4D ? vel.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 swirl = new Vec3(-fwd.z, 0.0D, fwd.x).scale((living.getId() % 2 == 0 ? 1.0D : -1.0D) * 0.24D);
            SpellExecutionEngine.applySyncedMotion(living, new Vec3(fwd.x * 0.98D + swirl.x, 0.32D, fwd.z * 0.98D + swirl.z));
        }

        level.sendParticles(ParticleTypes.GUST,
                living.getX(), living.getY(0.70D), living.getZ(), 2, 0.20D, 0.20D, 0.20D, 0.02D);
    }

    // =========================================================================
    // FORM MUTATION 2 — PRANIC JETSTREAM TETHER (BEAM)
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
        // Section 6.24 Pranic Jetstream Tether:
        // Sustained beam of rushing spiritual wind; repels hostiles, siphons 20% of damage dealt into caster's
        // Materia fuel, acts as a vertical jetpack lifter when pointed down, and grants Slow Falling & +30% speed to allies!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(hit, level, 0.30D, 100L);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) {
                if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                    SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normDir.x * 1.05D, 0.30D, normDir.z * 1.05D));
                }
                if (!slot.hasHarmVerb()) {
                    float beamDmg = (1.28F + tier * 0.36F) * potency;
                    float dealt = SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(level, hit, caster, beamDmg, false, true);
                    if (dealt > 0.0F && caster instanceof Player player && player.isAlive()) {
                        SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.AURA, tier, dealt, 0.20F);
                    }
                }
            }
        }

        // Sweep friendly allies along the full jetstream corridor so allies always catch the slipstream tailwind!
        double actualReach = blockHit != null ? caster.getEyePosition().distanceTo(blockHit.getLocation()) : maxReach;
        AABB corridor = caster.getBoundingBox().expandTowards(normDir.scale(actualReach)).inflate(1.30D);
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, corridor, LivingEntity::isAlive)) {
            if (ally != caster && SpellExecutionEngine.isFriendlyAllyForOasis(caster, ally)) {
                ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
                ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(ally, level, 0.30D, 100L);
            }
        }

        // Acts as a vertical jetpack lifter when pointed down!
        if (normDir.y < -0.35D) {
            double liftY = Math.min(0.84D, Math.max(caster.getDeltaMovement().y + 0.38D, 0.68D));
            SpellExecutionEngine.applySyncedMotion(caster, new Vec3(
                    caster.getDeltaMovement().x - normDir.x * 0.16D,
                    liftY,
                    caster.getDeltaMovement().z - normDir.z * 0.16D
            ));
            caster.resetFallDistance();
            caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 45, 0, true, false));
            level.sendParticles(ParticleTypes.GUST,
                    caster.getX(), caster.getY() + 0.1D, caster.getZ(), 2, 0.22D, 0.08D, 0.22D, 0.02D);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SPIRITUAL VACUUM IMPLOSION (RADIAL BURST)
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
        // Section 6.24 Spiritual Vacuum Implosion:
        // Implodes the air inward before detonating (6-block+ radius, #FF6B9D / #A88654); pulls nearby mobs
        // to a central point, silences abilities for 2.0s (40t), and vacuums dropped items directly to the caster!
        double r = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastVacuumSphere(level, center, r, true, EssenceType.AURA, null, 42, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            double dx = foe.getX() - center.x;
            double dz = foe.getZ() - center.z;
            if (dx * dx + dz * dz > (r + 0.6D) * (r + 0.6D)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float burstDmg = (3.4F + tier * 0.90F) * potency;
                SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(level, foe, caster, burstDmg, false, true);
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 toCenter = center.subtract(foe.position());
                double dist = toCenter.horizontalDistance();
                if (dist > 0.20D) {
                    Vec3 normIn = new Vec3(toCenter.x / dist, 0.0D, toCenter.z / dist);
                    double pullMag = Math.min(1.25D, 0.45D + dist * 0.16D);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(normIn.x * pullMag, 0.28D, normIn.z * pullMag));
                }
            }
            SpellLingeringZoneManager.applyArcaneSilence(foe, 40);
        }

        // Vacuum dropped items immediately AND for 10 ticks afterward so loot from mobs slain by the implosion comes straight to caster!
        vacuumDroppedItemsToCaster(level, caster, box);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 10, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (owner != null && owner.isAlive()) {
                        AABB itemBox = new AABB(
                                zone.center.x - zone.radius - 1.0D, zone.center.y - 2.5D, zone.center.z - zone.radius - 1.0D,
                                zone.center.x + zone.radius + 1.0D, zone.center.y + 3.5D, zone.center.z + zone.radius + 1.0D
                        );
                        vacuumDroppedItemsToCaster(srv, owner, itemBox);
                    }
                    return elapsed < zone.maxDurationTicks;
                }
        ));

        level.playSound(null, BlockPos.containing(center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 1.35F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.80F, 1.40F);
    }

    // =========================================================================
    // FORM MUTATION 4 — CHAKRA DISRUPTOR PALM (TOUCH / STRIKE)
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
        // Section 6.24 Chakra Disruptor Palm:
        // Strikes the target to sever spiritual channels; disables mob special abilities and spellcasting
        // for 4.0s (80t) while siphoning exact 30% of damage dealt back into the caster's Materia fuel reserves!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.position().add(0.0D, caster.getBbHeight() * 0.58D, 0.0D),
                direction, 3.4D, EssenceType.AURA, null, tier
        );

        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            SpellLingeringZoneManager.applyArcaneSilence(hit, 80);

            if (!slot.hasHarmVerb()) {
                float cleaveScale = (i == 0) ? 1.0F : 0.50F;
                float palmDmg = (3.5F + tier * 1.00F) * cleaveScale * potency;
                float dealt = SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(level, hit, caster, palmDmg, true, true);
                if (dealt > 0.0F && caster instanceof Player player && player.isAlive()) {
                    SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.AURA, tier, dealt, 0.30F);
                }
            }

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), PRANIC_PINK_HEX_COLOR, 22, 1.00F, 0.06F),
                    hit.getX(), hit.getY(0.85D), hit.getZ(), 10, 0.28D, 0.32D, 0.28D, 0.04D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 22, 0.95F, 0.05F),
                    hit.getX(), hit.getY(0.95D), hit.getZ(), 8, 0.28D, 0.32D, 0.28D, 0.04D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.80F, 1.42F);
    }

    // =========================================================================
    // FORM MUTATION 5 — CHAKRA RESPIRATION SHROUD (AURA / SELF)
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
        // Section 6.24 Chakra Respiration Shroud:
        // Meditative aura (#FF6B9D / #A88654) cutting all spell Materia fuel consumption by 20%,
        // granting complete immunity to drowning & suffocation, and passively regenerating hunger saturation every 4.0s!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long until = level.getGameTime() + dur;

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL, until);
        data.putInt("SpellAuraShroudTier", tier);
        if (caster.getAirSupply() < caster.getMaxAirSupply()) {
            caster.setAirSupply(caster.getMaxAirSupply());
        }

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), PRANIC_PINK_HEX_COLOR, 26, 1.00F, 0.06F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 14, 0.45D, 0.50D, 0.45D, 0.03D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 26, 0.95F, 0.05F),
                caster.getX(), caster.getY(0.95D), caster.getZ(), 14, 0.45D, 0.50D, 0.45D, 0.03D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.38F);
    }

    // =========================================================================
    // FORM MUTATION 6 — CHAKRA DISRUPTION GLYPH (GLYPH / TRAP)
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
        // Section 6.24 Chakra Disruption Glyph:
        // Disarmable (Sneak + Right-Click for 50% refund) Single/Repeating rune (1s arming delay);
        // releases a wave of spiritual interference silencing abilities & spellcasting for 5.0s (100t)
        // and stripping 25% of melee attack power!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.5D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.AURA, null, maxDuration, tier
        );
        level.playSound(null, surface.above(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.80F, 1.32F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    // Canonical 1.0s (20t) Glyph arming delay
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    if (elapsed % 12 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 18, 0.85F, 0.04F),
                                zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, zone.radius * 0.42D, 0.05D, zone.radius * 0.42D, 0.01D
                        );
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (victims.isEmpty()) return true;

                    for (LivingEntity v : victims) {
                        // Exact 5.0s (100t) ability/spell silence & -25% melee attack power!
                        SpellLingeringZoneManager.applyArcaneSilence(v, 100);
                        SpellCombatReactionEngine.applyTimedAttackDebuff(v, srv, 0.25D, 0.0D, 100L);
                        v.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float glyphDmg = (3.3F + tier * 0.88F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(srv, v, owner, glyphDmg, false, true);
                        }
                    }

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.AURA, null, tier);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.90F, 1.38F);

                    triggersLeft[0]--;
                    rearmCooldown[0] = 35;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — PRANIC GALE DELUGE (CONE / SPRAY)
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
        // Section 6.24 Pranic Gale Deluge:
        // Wide 60-degree cone of spiritual wind and vital mist; knocks enemies 8 blocks back, deflects arrows
        // and fireballs, extinguishes fires, and silences mob abilities for 2.5s (50t), while granting caught
        // allies debuff cleansing and +25% speed!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D),
                direction, coneRange, EssenceType.AURA, null, tier
        );

        Vec3 flatPush = new Vec3(direction.x, 0.0D, direction.z);
        Vec3 normPush = flatPush.lengthSqr() > 1.0E-4D ? flatPush.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || hit == caster) continue;
            hit.clearFire();
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(hit, level, 0.25D, 120L);
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0));
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) {
                if (!slot.hasHarmVerb()) {
                    float delugeDmg = (2.7F + tier * 0.78F) * potency;
                    SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
                }
                SpellLingeringZoneManager.applyArcaneSilence(hit, 50);
                if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                    // Exact 8-block gale knockback!
                    SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normPush.x * 1.85D, 0.42D, normPush.z * 1.85D));
                }
            }
        }

        // Deflect incoming arrows AND fireballs across the full cone!
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : normPush;
        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(coneRange)).inflate(2.6D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, coneBox, Entity::isAlive)) {
            if (proj instanceof SpellProjectileEntity sp && sp.getOwner() == caster) continue;
            Entity projOwner = proj.getOwner();
            if (projOwner == caster) continue;
            if (projOwner instanceof LivingEntity livOwner && SpellExecutionEngine.isFriendlyAllyForOasis(caster, livOwner)) {
                continue;
            }
            proj.setOwner(caster);
            if (proj instanceof AbstractHurtingProjectile fireball) {
                fireball.accelerationPower = Math.max(0.12D, fireball.accelerationPower);
            }
            proj.setDeltaMovement(normDir.scale(1.65D));
            proj.hasImpulse = true;
            level.sendParticles(ParticleTypes.GUST, proj.getX(), proj.getY(), proj.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }

        // Extinguish fires & lit campfires/candles along the cone!
        int steps = Math.min(10, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.position().add(normDir.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-2, -1, -2), bp.offset(2, 2, 2))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.getBlock() instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT)) {
                    level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                } else if (bs.getBlock() instanceof CandleBlock && bs.hasProperty(CandleBlock.LIT) && bs.getValue(CandleBlock.LIT)) {
                    level.setBlockAndUpdate(p, bs.setValue(CandleBlock.LIT, false));
                }
            }
        }

        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 8 — SPIRITUAL GALE BARRAGE (RAIN / SKYFALL)
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
        // Section 6.24 Spiritual Gale Barrage:
        // Opens a tier-scaled cave-safe aerial rift dropping compressed atmospheric torpedoes over 6.0s (120t);
        // each crash creates a 3-block updraft geyser launching enemies 10 blocks upward with heavy fall damage!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.8D, radius);
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        BlockPos baseAir = surface.above();
        Vec3 snappedCenter = new Vec3(surface.getX() + 0.5D, baseAir.getY(), surface.getZ() + 0.5D);
        double canopyHeight = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);
        int totalTicks = 120; // Canonical 6.0 seconds

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, snappedCenter, r, EssenceType.AURA, null, totalTicks, tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snappedCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.AURA, null, totalTicks, tier
        );
        level.playSound(null, baseAir, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.15F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, r, totalTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float potency = Math.max(0.5F, zone.apparatusPotency);
                        int torpedoesPerSalvo = 1 + tier / 4;

                        for (int t = 0; t < torpedoesPerSalvo; t++) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * Math.max(1.0D, zone.radius - 1.5D);
                            BlockPos sample = BlockPos.containing(
                                    zone.center.x + Math.cos(ang) * dist,
                                    zone.center.y,
                                    zone.center.z + Math.sin(ang) * dist
                            );
                            BlockPos crashSurf = findGroundSurfaceNear(srv, sample);
                            Vec3 torpedoPos = new Vec3(crashSurf.getX() + 0.5D, crashSurf.above().getY(), crashSurf.getZ() + 0.5D);

                            // 3-block updraft geyser plume!
                            SpellWorldVfxManager.broadcastGeyserPlume(
                                    srv, torpedoPos, 3.0D, Math.min(canopyHeight, 10.5D), EssenceType.AURA, null, 40, tier
                            );
                            srv.sendParticles(ParticleTypes.GUST_EMITTER_LARGE,
                                    torpedoPos.x, torpedoPos.y + 0.5D, torpedoPos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                            srv.playSound(null, BlockPos.containing(torpedoPos),
                                    SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.18F);

                            AABB geyserBox = new AABB(
                                    torpedoPos.x - 3.0D, torpedoPos.y - 1.5D, torpedoPos.z - 3.0D,
                                    torpedoPos.x + 3.0D, torpedoPos.y + 4.0D, torpedoPos.z + 3.0D
                            );
                            for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, geyserBox, LivingEntity::isAlive)) {
                                if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.60F
                                    );
                                }
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    float torpedoDmg = (2.60F + tier * 0.72F) * potency;
                                    SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(srv, v, owner, torpedoDmg, false, true);
                                }

                                // Strip Slow Falling & launch 10 blocks upward (`vy = 1.35D`) so enemies crash down with heavy fall damage!
                                v.removeEffect(MobEffects.SLOW_FALLING);
                                if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                    Vec3 cur = v.getDeltaMovement();
                                    SpellExecutionEngine.applySyncedMotion(v, new Vec3(cur.x * 0.25D, 1.35D, cur.z * 0.25D));
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — PRANIC SPIRITUALIST EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.AURA.name());
        tag.putInt("SpellEidolonTintRGB", PRANIC_PINK_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.AURA, null, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), PRANIC_PINK_HEX_COLOR, 28, 1.05F, 0.06F),
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.03D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 28, 1.00F, 0.05F),
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.35F);
    }

    // =========================================================================
    // CORE EFFECT SYNERGIES — BLINK, TRANSMUTE, AFFLICTION & RESTORATION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.24 Pranic Slipstream Blink:
        // Teleports up to 14+ blocks silently, leaving a vacuum pop that drags and disorients foes
        // while granting the caster Slow Falling!
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 100L
        );
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, departurePos.add(0.0D, 0.30D, 0.0D), 3.8D, true, EssenceType.AURA, null, 38, potencyTier
        );

        long disorientUntil = level.getGameTime() + 70L;
        AABB depBox = new AABB(
                departurePos.x - 3.8D, departurePos.y - 1.5D, departurePos.z - 3.8D,
                departurePos.x + 3.8D, departurePos.y + 2.6D, departurePos.z + 3.8D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 in = departurePos.subtract(foe.position());
                Vec3 normIn = in.lengthSqr() > 1.0E-4D ? in.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(normIn.x * 0.90D, 0.26D, normIn.z * 0.90D));
            }
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 70, 0));
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_VITAE_DISORIENTED_UNTIL, disorientUntil);
        }

        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0));
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 22, 0.92F, 0.04F),
                arrivalPos.x, arrivalPos.y + 0.85D, arrivalPos.z, 12, 0.35D, 0.45D, 0.35D, 0.02D
        );
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.24 Aerated Purifying Transmute:
        // Clears lingering potion clouds and cobwebs, dries sponges, and purifies water!
        boolean purifiedAny = false;
        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(pos).inflate(4.5D))) {
            cloud.discard();
            purifiedAny = true;
        }
        if (state.is(Blocks.WATER)) {
            for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(2.2D), LivingEntity::isAlive)) {
                if (caster == null || SpellExecutionEngine.isFriendlyAllyForOasis(caster, ent)) {
                    SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                    purifiedAny = true;
                }
            }
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (purifiedAny) maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return purifiedAny;
        }

        if (state.getBlock() instanceof BaseFireBlock) {
            level.removeBlock(pos, false);
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.getBlock() instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.COBWEB)) {
            level.destroyBlock(pos, true, caster);
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.WET_SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
            level.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.03D);
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, Blocks.DANDELION.defaultBlockState());
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (purifiedAny) {
            maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
        }
        return purifiedAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack in = itemEntity.getItem();
        int count = in.getCount();
        ItemStack out = ItemStack.EMPTY;

        if (in.is(Items.WET_SPONGE)) {
            out = new ItemStack(Items.SPONGE, count);
        } else if (in.is(Items.POISONOUS_POTATO)) {
            out = new ItemStack(Items.POTATO, count);
        } else if (in.is(Items.ROTTEN_FLESH)) {
            out = new ItemStack(Items.LEATHER, count);
        } else if (in.is(Items.WITHER_ROSE)) {
            out = new ItemStack(Items.DANDELION, count);
        } else if (in.is(Items.POTION) || in.is(Items.SPLASH_POTION) || in.is(Items.LINGERING_POTION)) {
            // Purifies harmful / tainted potion bottles back into crystal-clear pure Water Bottles!
            PotionContents contents = in.get(DataComponents.POTION_CONTENTS);
            if (contents != null && !contents.is(Potions.WATER)) {
                out = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                out.setCount(count);
            }
        }

        if (out.isEmpty()) {
            return false;
        }
        itemEntity.setItem(out);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 20, 0.90F, 0.04F),
                itemEntity.getX(), itemEntity.getY(0.35D), itemEntity.getZ(), 8, 0.20D, 0.20D, 0.20D, 0.02D
        );
        maybeGrantAuraBatchTransmuteDiscount(level, caster, potencyTier);
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.24 Chakra Severance Hex:
        // Persistent milk-immune pranic curse (cleansed strictly via Restoration magic or caster defeat):
        // permanently silences mob special abilities & spellcasting, inflicts continuous suffocation tick damage,
        // and doubles (2x) all knockback taken!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellLingeringZoneManager.applyArcaneSilence(target, 45);
        target.setAirSupply(Math.max(-20, target.getAirSupply() - 45));

        float suffocateDmg = (1.45F + potencyTier * 0.45F) * mult;
        target.hurtServer(level, level.damageSources().inWall(), suffocateDmg);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), PRANIC_PINK_HEX_COLOR, 18, 0.88F, 0.05F),
                target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.30D, 0.25D, 0.02D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), BUFF_AIR_GOLD_HEX_COLOR, 18, 0.85F, 0.04F),
                target.getX(), target.getY(0.95D), target.getZ(), 3, 0.25D, 0.30D, 0.25D, 0.02D
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
        if (effect == SpellCoreEffect.RESTORATION) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            target.setAirSupply(target.getMaxAirSupply());
            target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0));
            float bonusHeal = 3.0F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
            target.heal(bonusHeal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void vacuumDroppedItemsToCaster(ServerLevel level, LivingEntity caster, AABB box) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive)) {
            item.teleportTo(caster.getX(), caster.getY() + 0.5D, caster.getZ());
            item.setNoPickUpDelay();
        }
    }

    private static void maybeGrantAuraBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellAuraBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellAuraBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellAuraBatchTick", now);
        pData.putInt("SpellAuraBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.AURA, refund);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -5; dy--) {
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
}
