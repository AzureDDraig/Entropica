package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Blood Materia (Vital Fusion #22)</b>
 * (Spellcrafting System Guide Section 6.23):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Dual Vitae + Nether/Undead harmonic matrix — Strong against
 *     <b>FROZEN (+1)</b>, <b>NATURE (+1)</b>, and <b>SPRING (+1)</b> (Dominance {@code blood_vitae_dom}:
 *     Strong vs Undead) | Weak against <b>RADIANT (-1)</b>, <b>NETHER (-1)</b>, and <b>UMBRAL (-1)</b>,
 *     computed dynamically at runtime by {@code VisInteractionManager}.</li>
 *     <li><b>Flow (Chains)</b>: <i>Sanguine Capillary Chains</i> — branches across targets via crimson blood
 *     tendrils; each leap inflicts Hemorrhage, siphons 20% of damage dealt back to heal the caster, and pulls
 *     chained enemies 2 blocks closer to each other.</li>
 *     <li><b>Projectile</b>: <i>Sanguine Coagulation Needle</i> — razor-sharp needle of hardened crimson blood
 *     ({@code #8A0303}) piercing up to 2 enemies, inflicting Hemorrhage for 4.0s (bleeding on movement or attack)
 *     and leaching 30% of damage dealt to heal the caster.</li>
 *     <li><b>Beam</b>: <i>Sanguine Exsanguination Tether</i> — writhing crimson tether leaching 35% of damage
 *     dealt to heal the caster, stacking Hemorrhage &amp; Slowness up to 45%, and creating slowing blood puddles
 *     under the target's feet.</li>
 *     <li><b>Radial Burst</b>: <i>Boiling Blood Mist Detonation</i> — 6-block+ cloud of boiling crimson vapor
 *     ({@code #8A0303}) blinding caught hostiles for 3.0s, melting incoming arrows, and granting the caster
 *     +2 temporary Absorption hearts (4.0 HP) per enemy hit.</li>
 *     <li><b>Touch / Strike</b>: <i>Vampiric Exsanguination Strike</i> — jagged crystallized blood blow inflicting
 *     severe Hemorrhage for 6.0s, leaching 40% of damage dealt as instant health, and restoring 2 hunger shanks
 *     + Speed II for 5.0s if the blow kills the target.</li>
 *     <li><b>Aura / Self</b>: <i>Coagulated Crimson Carapace</i> — hardened blood-shell granting +6 Armor &amp;
 *     +2 Armor Toughness while slowly consuming hunger saturation over time to maintain the shell.</li>
 *     <li><b>Glyph / Trap</b>: <i>Sanguine Spike Snare</i> — disarmable Single/Repeating crimson rune erupting
 *     into hardened blood spikes that root trapped enemies for 3.5s, inflict severe Hemorrhage, and leach 30%
 *     of damage dealt back to heal the caster.</li>
 *     <li><b>Cone / Spray</b>: <i>Sanguine Shrapnel Deluge</i> — 60° spray of crystallized blood needles and
 *     crimson mist shredding enemy Armor Toughness by -25% for 5.0s, knocking enemies back 3 blocks, inflicting
 *     Hemorrhage, and leaching 20% of damage dealt to heal the caster.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Sanguine Deluge Downpour</i> — tier-scaled 16m+ crimson bloodstorm for 12.0s
 *     inflicting stacking Hemorrhage (slowing enemies up to 50%), leaching 15% of damage dealt to heal the caster,
 *     and coating the floor in blood slicks that grant the caster +30% sprint speed.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Sanguine Hemomancer Eidolon Suite</i> — {@code #8A0303} Blood-Thrall Knight
 *     Agent (25% melee lifesteal to caster + Hemorrhage), Exsanguination Pylon Sentry (tethering up to 2 hostiles
 *     to transfer health to the nearest player), and Crimson Effigy Decoy (5m boiling blood burst blinding foes
 *     &amp; granting +4 Absorption hearts on defeat).</li>
 *     <li><b>Blink / Step</b>: <i>Sanguine Dash Blink</i> — surges forward 16+ blocks in a jet of crimson spray,
 *     knocking aside mobs along the path.</li>
 *     <li><b>Transmute</b>: <i>Flesh Grafting Transmute</i> — converts leather and rotten flesh into fresh raw porkchops.</li>
 *     <li><b>Affliction</b>: <i>Sanguine Exsanguination Hex</i> — milk-immune hemomantic curse siphoning health
 *     from victim directly to caster every 3.0s, inflicting severe Hemorrhage on movement, preventing coagulation,
 *     and projecting the 32-block +100% <i>Scent of Hemorrhage</i> predator spawn-lure on afflicted players.</li>
 * </ul>
 */
public final class BloodSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SANGUINE_HEMOMANCER_HEX_COLOR = 0x8A0303;
    public static final int ARTERIAL_SCARLET_HEX_COLOR = 0xFF384C;

    @Override
    public EssenceType materiaType() {
        return EssenceType.BLOOD;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Sanguine Capillary Chains";
            default -> "Sanguine Capillary " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Sanguine Coagulation Needle";
            case BEAM -> "Sanguine Exsanguination Tether";
            case RADIAL_BURST -> "Boiling Blood Mist Detonation";
            case TOUCH_STRIKE -> "Vampiric Exsanguination Strike";
            case AURA -> "Coagulated Crimson Carapace";
            case GLYPH -> "Sanguine Spike Snare";
            case CONE -> "Sanguine Shrapnel Deluge";
            case SKYFALL -> "Sanguine Deluge Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Sanguine Dash Blink";
            case TRANSMUTE, BREAK -> "Flesh Grafting Transmute";
            case AFFLICTION -> "Sanguine Exsanguination Hex";
            case RESTORATION -> "Sanguine Transfusion Mending";
            default -> "Sanguine " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — SANGUINE CAPILLARY CHAINS
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
        // Section 6.23 Sanguine Capillary Chains:
        // Branches across targets via crimson blood tendrils; each leap inflicts Hemorrhage,
        // siphons 20% of damage dealt back to heal the caster, and pulls chained enemies 2 blocks closer to each other!
        int tier = spell.getEffectiveTier();
        Vec3 fromEye = fromTarget.position().add(0.0D, fromTarget.getBbHeight() * 0.58D, 0.0D);
        Vec3 toEye = toTarget.position().add(0.0D, toTarget.getBbHeight() * 0.58D, 0.0D);
        SpellWorldVfxManager.broadcastChainArc(level, fromEye, toEye, EssenceType.BLOOD, EssenceType.VITAE, tier);

        inflictHemorrhage(level, fromTarget, 80, 0);
        inflictHemorrhage(level, toTarget, 80, 0);

        if (!slot.hasHarmVerb()) {
            float chainDmg = (2.2F + tier * 0.65F) * spell.flawProfile().getPotencyMultiplier();
            float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, toTarget, caster, chainDmg, false, true);
            if (dealt > 0.0F && caster.isAlive()) {
                float siphon = dealt * 0.20F;
                SpellCombatReactionEngine.notifyMagicalHeal(caster, siphon);
                caster.heal(siphon);
            }
        }

        // Pull both chained enemies 2 blocks closer to each other (~1.0m each toward their midpoint)!
        Vec3 span = toTarget.position().subtract(fromTarget.position());
        Vec3 flatSpan = new Vec3(span.x, 0.0D, span.z);
        double flatDist = flatSpan.length();
        if (flatDist > 0.35D) {
            Vec3 unit = flatSpan.scale(1.0D / flatDist);
            double pullVel = Math.min(0.62D, Math.max(0.32D, flatDist * 0.22D));
            if (!SpellCombatReactionEngine.isBossEntity(level, fromTarget)) {
                SpellExecutionEngine.applySyncedMotion(fromTarget, new Vec3(unit.x * pullVel, 0.16D, unit.z * pullVel));
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, toTarget)) {
                SpellExecutionEngine.applySyncedMotion(toTarget, new Vec3(-unit.x * pullVel, 0.16D, -unit.z * pullVel));
            }
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 20, 0.95F, 0.05F),
                toTarget.getX(), toTarget.getY(0.75D), toTarget.getZ(), 8, 0.28D, 0.32D, 0.28D, 0.04D
        );
    }

    // =========================================================================
    // FORM MUTATION 1 — SANGUINE COAGULATION NEEDLE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Razor-sharp high-velocity hardened crimson blood needle (zero drop, pierces up to 2 enemies via SpellMutationRegistry)!
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D && vel.length() < 2.05D) {
            projectile.setDeltaMovement(vel.normalize().scale(2.10D));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.70F, 1.48F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 16, 0.85F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.04D, 0.04D, 0.04D, 0.01D
        );
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
        // Section 6.23 Sanguine Coagulation Needle:
        // Pierces up to 2 enemies; inflicts Hemorrhage for 4.0s (80t, causing extra bleeding damage when moving or attacking)
        // and leaches exact 30% of damage dealt to heal the caster!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive() || living == caster) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        inflictHemorrhage(level, living, 80, 0);

        if (!slot.hasHarmVerb()) {
            int tier = spell.getEffectiveTier();
            float needleDmg = (2.8F + tier * 0.85F) * spell.flawProfile().getPotencyMultiplier();
            float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, living, caster, needleDmg, false, true);
            if (dealt > 0.0F && caster != null && caster.isAlive()) {
                float leech = dealt * 0.30F;
                SpellCombatReactionEngine.notifyMagicalHeal(caster, leech);
                caster.heal(leech);
            }
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 22, 1.00F, 0.06F),
                living.getX(), living.getY(0.80D), living.getZ(), 12, 0.28D, 0.35D, 0.28D, 0.05D
        );
    }

    // =========================================================================
    // FORM MUTATION 2 — SANGUINE EXSANGUINATION TETHER (BEAM)
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
        // Section 6.23 Sanguine Exsanguination Tether:
        // Leaches exact 35% of damage dealt to heal the caster, stacks Hemorrhage & Slowness up to exact 45%,
        // and creates slowing blood puddles under the target's feet!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            long lastBeam = hData.getLong("SpellBloodBeamLastTick").orElse(0L);
            int stacks = (now - lastBeam <= 30L) ? Math.min(2, hData.getInt("SpellBloodBeamStacks").orElse(0) + 1) : 0;
            hData.putLong("SpellBloodBeamLastTick", now);
            hData.putInt("SpellBloodBeamStacks", stacks);

            // Stack Slowness cleanly from 15% -> 30% -> exact 45%!
            double slowFrac = switch (stacks) {
                case 0 -> 0.15D;
                case 1 -> 0.30D;
                default -> 0.45D;
            };
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, slowFrac, 55L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 55, stacks));
            inflictHemorrhage(level, hit, 90, Math.min(1, stacks));

            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.30F + tier * 0.38F) * potency;
                float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, hit, caster, beamDmg, false, true);
                if (dealt > 0.0F && caster.isAlive()) {
                    float leech = dealt * 0.35F;
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, leech);
                    caster.heal(leech);
                }
            }

            // Spawn slowing blood puddles under the target's feet (throttled per victim so continuous beams stay clean)!
            long nextPuddle = hData.getLong("SpellBloodPuddleNextTick").orElse(0L);
            if (now >= nextPuddle) {
                hData.putLong("SpellBloodPuddleNextTick", now + 18L);
                BlockPos surf = findGroundSurfaceNear(level, hit.blockPosition());
                Vec3 puddleCenter = new Vec3(hit.getX(), surf.above().getY(), hit.getZ());
                SpellWorldVfxManager.broadcastArcaneSeal(
                        level, puddleCenter.add(0.0D, 0.03D, 0.0D), 2.1D, EssenceType.BLOOD, EssenceType.VITAE, 70, tier
                );
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, puddleCenter, 2.1D, 70, now, spell, slot, fallbackMateria, apparatusPotency,
                        (srv, zone, elapsed) -> {
                            if (elapsed > zone.maxDurationTicks) return false;
                            if (elapsed % 8 == 0) {
                                srv.sendParticles(
                                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 16, 0.85F, 0.04F),
                                        zone.center.x, zone.center.y + 0.08D, zone.center.z, 5, 1.1D, 0.05D, 1.1D, 0.01D
                                );
                                LivingEntity owner = zone.resolveCaster(srv);
                                boolean safe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                                AABB box = new AABB(
                                        zone.center.x - 2.1D, zone.center.y - 1.0D, zone.center.z - 2.1D,
                                        zone.center.x + 2.1D, zone.center.y + 1.6D, zone.center.z + 2.1D
                                );
                                for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, safe)) continue;
                                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.35D, 22L);
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 24, 1));
                                }
                            }
                            return true;
                        }
                ));
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — BOILING BLOOD MIST DETONATION (RADIAL BURST)
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
        // Section 6.23 Boiling Blood Mist Detonation:
        // Expanding cloud of boiling crimson vapor (6-block+ radius, #8A0303); blinds caught hostiles for 3.0s (60t),
        // melts incoming arrows, and grants the caster +2 temporary Absorption hearts (4.0 HP) per enemy hit!
        double r = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        int enemiesHit = 0;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            double dx = foe.getX() - center.x;
            double dz = foe.getZ() - center.z;
            if (dx * dx + dz * dz > (r + 0.6D) * (r + 0.6D)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float burstDmg = (3.4F + tier * 0.90F) * potency;
                SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, foe, caster, burstDmg, false, true);
            }
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            inflictHemorrhage(level, foe, 60, 0);
            enemiesHit++;
        }

        // Melt incoming hostile arrows immediately AND sustain a brief 2.0s (40t) boiling blood vapor curtain!
        meltHostileArrowsInBox(level, caster, box);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 40, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB vaporBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 3.0D, zone.center.z + zone.radius
                    );
                    meltHostileArrowsInBox(srv, owner, vaporBox);
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 22, 1.05F, 0.06F),
                                zone.center.x, zone.center.y + 0.95D, zone.center.z, 10, zone.radius * 0.45D, 0.55D, zone.radius * 0.45D, 0.02D
                        );
                    }
                    return true;
                }
        ));

        if (enemiesHit > 0 && caster.isAlive()) {
            float absGain = enemiesHit * 4.0F; // Exact +2 temporary Absorption hearts (4.0 HP) per enemy hit!
            caster.setAbsorptionAmount(Math.min(32.0F, caster.getAbsorptionAmount() + absGain));
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 30, 1.15F, 0.08F),
                center.x, center.y + 1.0D, center.z, 38, r * 0.42D, 0.75D, r * 0.42D, 0.05D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 24, 1.00F, 0.06F),
                center.x, center.y + 0.85D, center.z, 26, r * 0.38D, 0.65D, r * 0.38D, 0.06D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.95F, 1.22F);
    }

    // =========================================================================
    // FORM MUTATION 4 — VAMPIRIC EXSANGUINATION STRIKE (TOUCH / STRIKE)
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
        // Section 6.23 Vampiric Exsanguination Strike:
        // Inflicts severe Hemorrhage for 6.0s (120t), leaches exact 40% of damage dealt back as instant health,
        // and if the blow kills the target, restores 2 hunger shanks (4 food) and grants Speed II for 5.0s (100t)!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.position().add(0.0D, caster.getBbHeight() * 0.58D, 0.0D),
                direction, 3.4D, EssenceType.BLOOD, EssenceType.VITAE, tier
        );

        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            // Stamp Blood Feast BEFORE damage so lethal one-shot blows always grant +2 hunger shanks & Speed II!
            CompoundTag hData = EntityHelper.getPersistentData(hit);
            hData.putLong(SpellLingeringZoneManager.TAG_BLOOD_FEAST_UNTIL, now + 60L);
            hData.putString("SpellBloodFeastCaster", caster.getUUID().toString());
            inflictHemorrhage(level, hit, 120, 1);

            if (!slot.hasHarmVerb()) {
                float cleaveScale = (i == 0) ? 1.0F : 0.50F;
                float strikeDmg = (3.6F + tier * 1.00F) * cleaveScale * potency;
                float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
                if (dealt > 0.0F && caster.isAlive()) {
                    float leech = dealt * 0.40F;
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, leech);
                    caster.heal(leech);
                }
            }

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 24, 1.05F, 0.06F),
                    hit.getX(), hit.getY(0.85D), hit.getZ(), 14, 0.30D, 0.35D, 0.30D, 0.05D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.90F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 5 — COAGULATED CRIMSON CARAPACE (AURA / SELF)
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
        // Section 6.23 Coagulated Crimson Carapace:
        // Hardens a thick armor shell of solidified blood over the caster (+6 Armor & +2 Armor Toughness),
        // slowly consuming hunger saturation over time to maintain the hardened blood shell!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long until = level.getGameTime() + dur;

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_BLOOD_CARAPACE_UNTIL, until);
        data.putInt("SpellBloodCarapaceTier", tier);

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.0D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 28, 1.05F, 0.06F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 22, 0.45D, 0.50D, 0.45D, 0.04D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 0.90F, 0.82F);
    }

    // =========================================================================
    // FORM MUTATION 6 — SANGUINE SPIKE SNARE (GLYPH / TRAP)
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
        // Section 6.23 Sanguine Spike Snare:
        // Disarmable (Sneak + Right-Click for 50% refund) crimson rune (1s arming delay, Single/Repeating)
        // that erupts into a thicket of razor-sharp hardened blood spikes when stepped on; roots trapped enemies
        // for exact 3.5 seconds (70t), inflicts severe Hemorrhage, and leaches 30% of damage dealt back to heal caster!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.BLOOD, EssenceType.VITAE, maxDuration, tier
        );
        level.playSound(null, surface.above(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.80F, 0.72F);

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
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 18, 0.85F, 0.04F),
                                zone.center.x, zone.center.y + 0.10D, zone.center.z, 4, zone.radius * 0.42D, 0.05D, zone.radius * 0.42D, 0.01D
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

                    long rootUntil = srv.getGameTime() + 70L; // Exact 3.5 seconds (70 ticks)!
                    var anchorHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get());
                    for (LivingEntity v : victims) {
                        // Hard 3.5-second (70t) spike root + severe Hemorrhage!
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 6));
                        v.addEffect(new MobEffectInstance(anchorHolder, 70, 0));
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.99D, 70L);
                        EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, rootUntil);
                        SpellExecutionEngine.applySyncedMotion(v, new Vec3(0.0D, Math.min(0.0D, v.getDeltaMovement().y), 0.0D));
                        inflictHemorrhage(srv, v, 100, 1);

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float snareDmg = (3.5F + tier * 0.90F) * Math.max(0.5F, zone.apparatusPotency);
                            float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(srv, v, owner, snareDmg, false, true);
                            if (dealt > 0.0F && owner != null && owner.isAlive()) {
                                float leech = dealt * 0.30F;
                                SpellCombatReactionEngine.notifyMagicalHeal(owner, leech);
                                owner.heal(leech);
                            }
                        }
                    }

                    // Erupt temporary coagulated crimson spikes around the snare for the 3.5s root duration!
                    List<BlockPos> spikes = new ArrayList<>();
                    BlockPos c = BlockPos.containing(zone.center);
                    for (BlockPos p : BlockPos.betweenClosed(c.offset(-1, 0, -1), c.offset(1, 0, 1))) {
                        BlockState cur = srv.getBlockState(p);
                        if (cur.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, cur)
                                && !srv.getBlockState(p.below()).getCollisionShape(srv, p.below()).isEmpty()) {
                            srv.setBlockAndUpdate(p, Blocks.CRIMSON_ROOTS.defaultBlockState());
                            spikes.add(p.immutable());
                        }
                    }
                    if (!spikes.isEmpty()) {
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, zone.center, 2.0D, 70, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                                (ssrv, szone, selapsed) -> {
                                    if (selapsed < szone.maxDurationTicks) return true;
                                    for (BlockPos sp : spikes) {
                                        if (ssrv.getBlockState(sp).is(Blocks.CRIMSON_ROOTS)) {
                                            ssrv.removeBlock(sp, false);
                                        }
                                    }
                                    return false;
                                }
                        ));
                    }

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.BLOOD, EssenceType.VITAE, tier);
                    srv.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 24, 1.05F, 0.06F),
                            zone.center.x, zone.center.y + 0.65D, zone.center.z, 22, 1.1D, 0.45D, 1.1D, 0.06D
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.0F, 0.78F);

                    triggersLeft[0]--;
                    rearmCooldown[0] = 35;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — SANGUINE SHRAPNEL DELUGE (CONE / SPRAY)
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
        // Section 6.23 Sanguine Shrapnel Deluge:
        // Wide 60-degree cone of high-velocity crystallized blood needles and crimson mist; shreds enemy Armor Toughness
        // by -25% for 5.0s (100t), knocks enemies back 3 blocks, inflicts Hemorrhage, and leaches 20% of damage dealt!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D),
                direction, coneRange, EssenceType.BLOOD, EssenceType.VITAE, tier
        );

        Vec3 flatPush = new Vec3(direction.x, 0.0D, direction.z);
        Vec3 normPush = flatPush.lengthSqr() > 1.0E-4D ? flatPush.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            // Exact -25% Armor Toughness for 5.0 seconds (100 ticks)!
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.25D, 100L);
            inflictHemorrhage(level, hit, 80, 0);

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                // Exact 3-block knockback!
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normPush.x * 0.88D, 0.26D, normPush.z * 0.88D));
            }

            if (!slot.hasHarmVerb()) {
                float delugeDmg = (2.8F + tier * 0.80F) * potency;
                float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
                if (dealt > 0.0F && caster.isAlive()) {
                    float leech = dealt * 0.20F;
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, leech);
                    caster.heal(leech);
                }
            }
        }

        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.85F, 1.32F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.65F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 8 — SANGUINE DELUGE DOWNPOUR (RAIN / SKYFALL)
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
        // Section 6.23 Sanguine Deluge Downpour:
        // Heavy 16-block+ crimson bloodstorm for 12.0 seconds (240t); inflicts stacking Hemorrhage (slowing enemies
        // up to 50%), leaches 15% of damage dealt to heal the caster, and coats the floor in blood slicks that grant
        // the caster +30% sprint speed!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        BlockPos baseAir = surface.above();
        Vec3 snappedCenter = new Vec3(surface.getX() + 0.5D, baseAir.getY(), surface.getZ() + 0.5D);
        double canopyHeight = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);
        int totalTicks = 240; // Canonical 12.0 seconds

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, snappedCenter, r, EssenceType.BLOOD, EssenceType.VITAE, totalTicks, tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snappedCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.BLOOD, EssenceType.VITAE, totalTicks, tier
        );
        level.playSound(null, baseAir, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.PLAYERS, 0.95F, 0.72F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, r, totalTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 5 == 0) {
                        double rainY = zone.center.y + Math.min(canopyHeight * 0.68D, 11.0D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 22, 0.95F, 0.05F),
                                zone.center.x, rainY, zone.center.z, 16 + tier * 2, zone.radius * 0.50D, 1.3D, zone.radius * 0.50D, 0.05D
                        );
                        // Blood slicks grant the caster exact +30% sprint speed whenever sprinting inside the downpour!
                        if (owner != null && owner.isAlive() && owner.isSprinting()) {
                            double dx = owner.getX() - zone.center.x;
                            double dz = owner.getZ() - zone.center.z;
                            if (dx * dx + dz * dz <= (zone.radius + 0.8D) * (zone.radius + 0.8D)
                                    && Math.abs(owner.getY() - zone.center.y) <= Math.max(4.5D, canopyHeight * 0.55D)) {
                                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(owner, srv, 0.30D, 18L);
                            }
                        }
                    }

                    if (elapsed % 20 == 0) {
                        long now = srv.getGameTime();
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float potency = Math.max(0.5F, zone.apparatusPotency);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.0D, canopyHeight * 0.60D), zone.center.z + zone.radius
                        );
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = e.getX() - zone.center.x;
                            double dz = e.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > (zone.radius + 0.6D) * (zone.radius + 0.6D)) continue;

                            if (e == owner) {
                                if (e.isSprinting()) {
                                    SpellCombatReactionEngine.applyTimedMovementSpeedBoost(e, srv, 0.30D, 28L);
                                }
                            } else if (!SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))) {
                                CompoundTag ed = EntityHelper.getPersistentData(e);
                                long lastRain = ed.getLong("BloodRainLastTick").orElse(0L);
                                int stacks = (now - lastRain <= 35L) ? Math.min(2, ed.getInt("BloodRainStacks").orElse(0) + 1) : 0;
                                ed.putLong("BloodRainLastTick", now);
                                ed.putInt("BloodRainStacks", stacks);

                                // Stacking Hemorrhage slowing enemies cleanly from 20% -> 35% -> exact 50%!
                                double slowFrac = switch (stacks) {
                                    case 0 -> 0.20D;
                                    case 1 -> 0.35D;
                                    default -> 0.50D;
                                };
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(e, srv, slowFrac, 38L);
                                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 38, stacks));
                                inflictHemorrhage(srv, e, 55, Math.min(1, stacks));

                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, e, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                    );
                                }
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    float downpourDmg = (1.95F + tier * 0.55F) * potency;
                                    float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(srv, e, owner, downpourDmg, false, true);
                                    if (dealt > 0.0F && owner != null && owner.isAlive()) {
                                        float leech = dealt * 0.15F;
                                        SpellCombatReactionEngine.notifyMagicalHeal(owner, leech);
                                        owner.heal(leech);
                                    }
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
    // FORM MUTATION 9 — SANGUINE HEMOMANCER EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.BLOOD.name());
        tag.putInt("SpellEidolonTintRGB", SANGUINE_HEMOMANCER_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.BLOOD, EssenceType.VITAE, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 28, 1.05F, 0.06F),
                spawnPos.x, spawnPos.y + 0.90D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 30, 1.05F, 0.06F),
                spawnPos.x, spawnPos.y + 0.70D, spawnPos.z, 16, 0.48D, 0.45D, 0.48D, 0.03D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.85F, 0.88F);
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
        // Section 6.23 Sanguine Dash Blink:
        // Surges forward 16+ blocks in a jet of crimson spray, knocking aside small mobs along the path!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.BLOOD, potencyTier);

        Vec3 span = arrivalPos.subtract(departurePos);
        double dist = span.length();
        Vec3 surgeDir = dist > 1.0E-4D ? span.scale(1.0D / dist) : caster.getLookAngle().normalize();
        Vec3 right = new Vec3(-surgeDir.z, 0.0D, surgeDir.x);
        if (right.lengthSqr() < 1.0E-4D) right = new Vec3(1.0D, 0.0D, 0.0D);
        else right = right.normalize();

        // Continuous jet of #8A0303 crimson spray along the full surge path!
        int samples = Math.clamp((int) (dist * 2.0D), 6, 36);
        for (int s = 0; s <= samples; s++) {
            double t = s / (double) samples;
            Vec3 pt = departurePos.add(span.scale(t)).add(0.0D, 0.75D, 0.0D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 20, 0.95F, 0.05F),
                    pt.x, pt.y, pt.z, 3, 0.22D, 0.22D, 0.22D, 0.03D
            );
            if (s % 2 == 0) {
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 22, 0.95F, 0.05F),
                        pt.x, pt.y, pt.z, 2, 0.25D, 0.22D, 0.25D, 0.02D
                );
            }
        }

        AABB corridor = new AABB(departurePos, arrivalPos).inflate(1.85D, 1.45D, 1.85D);
        float dashDmg = 1.65F + potencyTier * 0.45F;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, corridor, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;

            // Check distance to the actual dash line segment so only mobs along the path are knocked aside!
            Vec3 rel = foe.position().subtract(departurePos);
            double proj = Math.clamp(rel.dot(surgeDir), 0.0D, Math.max(0.1D, dist));
            Vec3 closest = departurePos.add(surgeDir.scale(proj));
            if (foe.position().distanceToSqr(closest) > 4.0D) continue;

            Vec3 lateral = foe.position().subtract(closest);
            Vec3 flatLat = new Vec3(lateral.x, 0.0D, lateral.z);
            Vec3 pushOut = flatLat.lengthSqr() > 1.0E-3D
                    ? flatLat.normalize()
                    : (foe.getId() % 2 == 0 ? right : right.scale(-1.0D));

            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(pushOut.x * 0.88D, 0.28D, pushOut.z * 0.88D));
            }
            SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, foe, caster, dashDmg, false, true);
            inflictHemorrhage(level, foe, 60, 0);
        }

        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 0.85F, 1.18F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 0.85F, 1.25F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.23 Flesh Grafting Transmute (container-safe block enrichment + dropped item conversion)
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.NETHERRACK)) {
            level.setBlockAndUpdate(pos, Blocks.CRIMSON_NYLIUM.defaultBlockState());
            maybeGrantBloodBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.WITHER_ROSE) || state.is(Blocks.DEAD_BUSH)) {
            BlockState lily = ModBlocks.SANGUINE_LILY.get().defaultBlockState();
            level.setBlockAndUpdate(pos, lily.canSurvive(level, pos) ? lily : Blocks.CRIMSON_ROOTS.defaultBlockState());
            maybeGrantBloodBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        // Section 6.23 Flesh Grafting Transmute: converts leather and rotten flesh into fresh raw porkchops!
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.ROTTEN_FLESH) || in.is(Items.LEATHER)) {
            itemEntity.setItem(new ItemStack(Items.PORKCHOP, in.getCount()));
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 20, 0.92F, 0.05F),
                    itemEntity.getX(), itemEntity.getY(0.35D), itemEntity.getZ(), 10, 0.20D, 0.20D, 0.20D, 0.03D
            );
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.70F, 1.15F);
            maybeGrantBloodBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.23 Sanguine Exsanguination Hex:
        // Milk-immune hemomantic curse (cleansed via Restoration/Radiant magic or caster defeat):
        // continuously siphons health from the victim directly to the caster every 3.0 seconds,
        // inflicts severe Hemorrhage (taking bonus bleed damage whenever moving), and prevents blood coagulation!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.removeEffect(MobEffects.REGENERATION);
        inflictHemorrhage(level, target, 45, 1);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        // Siphons health from the victim directly to the caster every 3.0 seconds (every 3rd 20-tick hex pulse)!
        if ((level.getGameTime() / 20L) % 3L == 0L) {
            float siphonAmt = (2.40F + potencyTier * 0.70F) * mult;
            float dealt = SpellCombatReactionEngine.hurtWithBloodMateriaAdvantage(level, target, hexCaster, siphonAmt, false, true);
            float actualDrain = dealt > 0.0F ? dealt : siphonAmt * 0.80F;
            if (hexCaster != null && hexCaster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(hexCaster, actualDrain);
                hexCaster.heal(actualDrain);
                if (hexCaster.distanceToSqr(target) <= 1024.0D) {
                    SpellWorldVfxManager.broadcastChainArc(
                            level,
                            target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D),
                            hexCaster.position().add(0.0D, hexCaster.getBbHeight() * 0.60D, 0.0D),
                            EssenceType.BLOOD, EssenceType.VITAE, potencyTier
                    );
                }
            }
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 18, 0.88F, 0.04F),
                target.getX(), target.getY(0.80D), target.getZ(), 5, 0.26D, 0.32D, 0.26D, 0.02D
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
            float bonusHeal = 3.2F + potencyTier * 1.00F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
            target.heal(bonusHeal);
            target.setAbsorptionAmount(Math.min(24.0F, target.getAbsorptionAmount() + 4.0F));
            level.sendParticles(ParticleTypes.HEART,
                    target.getX(), target.getY(0.95D), target.getZ(), 6, 0.30D, 0.35D, 0.30D, 0.03D);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void inflictHemorrhage(ServerLevel level, LivingEntity target, int ticks, int amp) {
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), ticks, amp));
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL,
                Math.max(level.getGameTime() + ticks, data.getLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL).orElse(0L)));
        data.putInt("SpellBloodHemorrhageAmp", Math.max(amp, data.getInt("SpellBloodHemorrhageAmp").orElse(0)));
    }

    private static void meltHostileArrowsInBox(ServerLevel level, @Nullable LivingEntity caster, AABB box) {
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box, Entity::isAlive)) {
            if (proj instanceof SpellProjectileEntity) continue;
            Entity projOwner = proj.getOwner();
            if (projOwner == caster) continue;
            if (projOwner instanceof LivingEntity livOwner && caster != null && SpellExecutionEngine.isFriendlyAllyForOasis(caster, livOwner)) {
                continue;
            }
            if (proj instanceof AbstractArrow || projOwner != null) {
                level.sendParticles(ParticleTypes.SMOKE, proj.getX(), proj.getY(), proj.getZ(), 5, 0.08D, 0.08D, 0.08D, 0.02D);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), SANGUINE_HEMOMANCER_HEX_COLOR, 16, 0.85F, 0.04F),
                        proj.getX(), proj.getY(), proj.getZ(), 4, 0.08D, 0.08D, 0.08D, 0.02D
                );
                proj.discard();
            }
        }
    }

    private static void maybeGrantBloodBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellBloodBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellBloodBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellBloodBatchTick", now);
        pData.putInt("SpellBloodBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.BLOOD, refund);
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
